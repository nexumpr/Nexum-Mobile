package mx.nexum.app.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.time.Instant
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.nexum.app.auth.data.AuthErrorKind
import mx.nexum.app.auth.data.AuthRepository
import mx.nexum.app.auth.data.AuthResult
import mx.nexum.app.auth.data.dto.Frequency
import mx.nexum.app.auth.data.dto.Income
import mx.nexum.app.auth.data.dto.RegisterRequest
import mx.nexum.app.auth.data.dto.RecurringExpense
import mx.nexum.app.auth.domain.AuthValidator

class AuthViewModel(
    private val repository: AuthRepository,
    private val privacyNoticeVersion: String = "2026-11-v1",
    private val clockIso: () -> String = { Instant.now().toString() }
) : ViewModel() {

    private val mutableState = MutableStateFlow(RegisterUiState())
    val state: StateFlow<RegisterUiState> = mutableState.asStateFlow()

    private val mutableEffects = MutableSharedFlow<AuthEffect>(
        replay = 0,
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effects: SharedFlow<AuthEffect> = mutableEffects.asSharedFlow()

    fun onIntent(intent: RegisterIntent) {
        when (intent) {
            is RegisterIntent.NameChanged -> mutableState.update {
                it.copy(name = intent.v, nameError = null, formError = null)
            }

            is RegisterIntent.EmailChanged -> mutableState.update {
                it.copy(email = intent.v, emailError = null, formError = null)
            }

            is RegisterIntent.PasswordChanged -> mutableState.update {
                it.copy(password = intent.v, passwordError = null, formError = null)
            }

            is RegisterIntent.ConfirmPasswordChanged -> mutableState.update {
                it.copy(confirmPassword = intent.v, confirmPasswordError = null, formError = null)
            }

            RegisterIntent.TogglePasswordVisibility -> mutableState.update {
                it.copy(isPasswordVisible = !it.isPasswordVisible)
            }

            is RegisterIntent.IncomeAmountChanged -> mutableState.update {
                it.copy(incomeAmount = intent.v, incomeError = null, formError = null)
            }

            is RegisterIntent.ExpenseChanged -> mutableState.update { state ->
                state.copy(
                    expenses = state.expenses.mapIndexed { index, draft ->
                        if (index == intent.index) intent.draft else draft
                    },
                    expensesError = null,
                    formError = null
                )
            }

            RegisterIntent.AddExpense -> mutableState.update {
                it.copy(expenses = it.expenses + ExpenseDraft(), expensesError = null)
            }

            is RegisterIntent.RemoveExpense -> mutableState.update { state ->
                if (state.expenses.size <= 1) state
                else state.copy(expenses = state.expenses.filterIndexed { index, _ -> index != intent.index })
            }

            is RegisterIntent.TermsAccepted -> mutableState.update {
                it.copy(termsAccepted = intent.v, termsError = null)
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
        val expensesError = expensesError(current)
        val termsError = AuthValidator.termsAcceptedError(current.termsAccepted)

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
                formError = null
            )
        }

        viewModelScope.launch {
            when (val result = repository.register(buildRequest(current))) {
                is AuthResult.Success -> {
                    mutableState.update { it.copy(isLoading = false) }
                    mutableEffects.tryEmit(AuthEffect.NavigateToLogin)
                }

                is AuthResult.Failure -> {
                    mutableState.update { it.copy(isLoading = false, formError = result.error.message) }
                    if (result.error.kind == AuthErrorKind.CONFLICT || result.error.retryable) {
                        mutableEffects.tryEmit(
                            AuthEffect.ShowSnackbar(result.error.message, result.error.retryable)
                        )
                    }
                }
            }
        }
    }

    private fun expensesError(state: RegisterUiState): String? = when {
        state.expenses.isEmpty() -> "Agrega al menos un gasto recurrente"
        state.expenses.any { AuthValidator.conceptError(it.concept) != null } -> "Completa el concepto de tus gastos"
        state.expenses.any { AuthValidator.amountError(it.amount) != null } -> "Revisa los montos de tus gastos"
        else -> null
    }

    private fun buildRequest(state: RegisterUiState) = RegisterRequest(
        name = state.name.trim(),
        email = state.email.trim().lowercase(),
        password = state.password,
        income = Income(amount = state.incomeAmount.trim(), frequency = state.incomeFrequency),
        recurringExpenses = state.expenses.map { draft ->
            RecurringExpense(
                concept = draft.concept.trim(),
                amount = draft.amount.trim(),
                frequency = Frequency.monthly,
                categoryId = draft.categoryId.trim().ifBlank { "general" },
                type = draft.type
            )
        },
        privacyNoticeVersion = privacyNoticeVersion,
        consentAcceptedAt = clockIso()
    )
}
