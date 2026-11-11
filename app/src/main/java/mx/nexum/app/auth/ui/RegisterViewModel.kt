package mx.nexum.app.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.nexum.app.auth.data.dto.ClientDto
import mx.nexum.app.auth.data.dto.IncomeDto
import mx.nexum.app.auth.data.dto.PrivacyDto
import mx.nexum.app.auth.data.dto.RecurringExpenseDto
import mx.nexum.app.auth.data.dto.RegisterRequestDto
import mx.nexum.app.auth.data.network.AuthApiService
import mx.nexum.app.auth.data.network.ClientInfoProvider
import mx.nexum.app.auth.data.network.NetworkModule
import mx.nexum.app.auth.domain.AuthValidator
import java.util.UUID

class RegisterViewModel(
    private val apiService: AuthApiService = NetworkModule.authApiService,
    private val localeProvider: () -> String = { ClientInfoProvider.getLocale() },
    private val timeZoneProvider: () -> String = { ClientInfoProvider.getTimeZone() },
    private val idempotencyKeyGenerator: () -> String = { UUID.randomUUID().toString() }
) : ViewModel() {

    private val mutableState = MutableStateFlow(RegisterUiState())
    val state: StateFlow<RegisterUiState> = mutableState.asStateFlow()

    private val mutableEffects = MutableSharedFlow<AuthEffect>(
        replay = 0,
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effects: SharedFlow<AuthEffect> = mutableEffects.asSharedFlow()

    private var currentIdempotencyKey: String? = null

    val lastIdempotencyKey: String?
        get() = currentIdempotencyKey

    fun onIntent(intent: RegisterIntent) {
        when (intent) {
            is RegisterIntent.NameChanged -> {
                currentIdempotencyKey = null
                mutableState.update { it.copy(name = intent.v, nameError = null, formError = null) }
            }

            is RegisterIntent.EmailChanged -> {
                currentIdempotencyKey = null
                mutableState.update { it.copy(email = intent.v, emailError = null, formError = null) }
            }

            is RegisterIntent.PasswordChanged -> {
                currentIdempotencyKey = null
                mutableState.update { it.copy(password = intent.v, passwordError = null, formError = null) }
            }

            is RegisterIntent.ConfirmPasswordChanged -> {
                currentIdempotencyKey = null
                mutableState.update { it.copy(confirmPassword = intent.v, confirmPasswordError = null, formError = null) }
            }

            RegisterIntent.TogglePasswordVisibility -> mutableState.update {
                it.copy(isPasswordVisible = !it.isPasswordVisible)
            }

            is RegisterIntent.IncomeAmountChanged -> {
                currentIdempotencyKey = null
                mutableState.update { it.copy(incomeAmount = intent.v, incomeError = null, formError = null) }
            }

            is RegisterIntent.ExpenseChanged -> {
                currentIdempotencyKey = null
                mutableState.update { state ->
                    state.copy(
                        expenses = state.expenses.mapIndexed { index, draft ->
                            if (index == intent.index) intent.draft else draft
                        },
                        expensesError = null,
                        formError = null
                    )
                }
            }

            RegisterIntent.AddExpense -> {
                currentIdempotencyKey = null
                mutableState.update {
                    it.copy(expenses = it.expenses + ExpenseDraft(), expensesError = null)
                }
            }

            is RegisterIntent.RemoveExpense -> {
                currentIdempotencyKey = null
                mutableState.update { state ->
                    state.copy(expenses = state.expenses.filterIndexed { index, _ -> index != intent.index })
                }
            }

            is RegisterIntent.TermsAccepted -> {
                currentIdempotencyKey = null
                mutableState.update {
                    it.copy(termsAccepted = intent.v, termsError = null)
                }
            }

            RegisterIntent.Submit -> submit()
        }
    }

    private fun submit() {
        val current = mutableState.value
        if (current.isLoading) return

        val nameError = AuthValidator.nameError(current.name)
        val emailError = AuthValidator.emailError(current.email)
        val passwordError = AuthValidator.passwordError(current.password)
        val confirmPasswordError = AuthValidator.confirmationError(current.password, current.confirmPassword)
        val incomeError = AuthValidator.amountError(current.incomeAmount)
        val expensesError = validateExpenses(current.expenses)
        val termsError = AuthValidator.privacyAcceptedError(current.termsAccepted)

        val hasErrors = listOf(
            nameError, emailError, passwordError, confirmPasswordError,
            incomeError, expensesError, termsError
        ).any { it != null }

        if (hasErrors) {
            mutableState.update {
                it.copy(
                    nameError = nameError,
                    emailError = emailError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmPasswordError,
                    incomeError = incomeError,
                    expensesError = expensesError,
                    termsError = termsError,
                    formError = null
                )
            }
            return
        }

        // Generate Idempotency-Key if not already set; reuse on retry after failure
        val idempotencyKey = currentIdempotencyKey ?: idempotencyKeyGenerator().also {
            currentIdempotencyKey = it
        }

        mutableState.update {
            it.copy(
                isLoading = true,
                nameError = null,
                emailError = null,
                passwordError = null,
                confirmPasswordError = null,
                incomeError = null,
                expensesError = null,
                termsError = null,
                formError = null,
                retryAfterSeconds = null
            )
        }

        val request = buildRequest(current)

        viewModelScope.launch {
            try {
                val response = apiService.register(idempotencyKey, request)
                when (response.code()) {
                    201 -> {
                        currentIdempotencyKey = null
                        mutableState.update {
                            it.copy(isLoading = false, isSuccess = true, formError = null)
                        }
                        mutableEffects.tryEmit(AuthEffect.NavigateToApp)
                    }

                    400 -> {
                        val message = "Datos o JSON inválidos. Revisa el formulario."
                        mutableState.update {
                            it.copy(isLoading = false, formError = message)
                        }
                        mutableEffects.tryEmit(AuthEffect.ShowSnackbar(message, retry = true))
                    }

                    409 -> {
                        val message = "El correo ya está registrado"
                        mutableState.update {
                            it.copy(
                                isLoading = false,
                                emailError = message,
                                formError = message
                            )
                        }
                        mutableEffects.tryEmit(AuthEffect.ShowSnackbar(message, retry = false))
                    }

                    422 -> {
                        val message = "Incumplimiento de una regla del registro. Por favor revisa los datos."
                        mutableState.update {
                            it.copy(isLoading = false, formError = message)
                        }
                        mutableEffects.tryEmit(AuthEffect.ShowSnackbar(message, retry = true))
                    }

                    429 -> {
                        val retryAfter = response.headers()["Retry-After"]
                        val message = if (!retryAfter.isNullOrBlank()) {
                            "Demasiadas solicitudes. Intenta más tarde en $retryAfter segundos."
                        } else {
                            "Demasiadas solicitudes. Intenta más tarde."
                        }
                        mutableState.update {
                            it.copy(
                                isLoading = false,
                                formError = message,
                                retryAfterSeconds = retryAfter
                            )
                        }
                        mutableEffects.tryEmit(AuthEffect.ShowSnackbar(message, retry = false))
                    }

                    else -> {
                        val message = "Error inesperado (${response.code()}). Intenta más tarde."
                        mutableState.update {
                            it.copy(isLoading = false, formError = message)
                        }
                        mutableEffects.tryEmit(AuthEffect.ShowSnackbar(message, retry = true))
                    }
                }
            } catch (e: Throwable) {
                if (e is CancellationException) throw e
                val message = "Error de conexión con el servidor. Verifica tu red e intenta de nuevo."
                mutableState.update {
                    it.copy(isLoading = false, formError = message)
                }
                mutableEffects.tryEmit(AuthEffect.ShowSnackbar(message, retry = true))
            }
        }
    }

    private fun validateExpenses(expenses: List<ExpenseDraft>): String? {
        if (expenses.isEmpty()) return null
        for (expense in expenses) {
            if (AuthValidator.conceptError(expense.concept) != null) {
                return "Completa el concepto de todos los gastos"
            }
            if (AuthValidator.amountError(expense.amount) != null) {
                return "Revisa los montos de tus gastos"
            }
        }
        return null
    }

    private fun buildRequest(state: RegisterUiState): RegisterRequestDto {
        val incomeAmountFormatted = AuthValidator.formatDecimal(state.incomeAmount)
        val formattedExpenses = state.expenses.map { draft ->
            RecurringExpenseDto(
                concept = draft.concept.trim(),
                amount = AuthValidator.formatDecimal(draft.amount),
                frequency = draft.frequency.ifBlank { "monthly" },
                category = draft.category.trim().ifBlank { "Servicios" },
                type = draft.type.ifBlank { "fixed" }
            )
        }

        return RegisterRequestDto(
            name = state.name.trim(),
            email = state.email.trim().lowercase(),
            password = state.password,
            income = IncomeDto(
                amount = incomeAmountFormatted,
                frequency = state.incomeFrequency.ifBlank { "monthly" }
            ),
            recurringExpenses = formattedExpenses,
            privacy = PrivacyDto(
                accepted = state.termsAccepted,
                noticeVersion = "2026-11"
            ),
            client = ClientDto(
                locale = localeProvider(),
                timeZone = timeZoneProvider()
            )
        )
    }
}
