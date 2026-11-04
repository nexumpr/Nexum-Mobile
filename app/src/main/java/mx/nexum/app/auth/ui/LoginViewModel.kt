package mx.nexum.app.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import mx.nexum.app.auth.data.dto.LoginRequest
import mx.nexum.app.auth.domain.AuthValidator

class LoginViewModel(
    private val repository: AuthRepository,
    private val deviceName: String? = null
) : ViewModel() {

    private val mutableState = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = mutableState.asStateFlow()

    private val mutableEffects = MutableSharedFlow<AuthEffect>(
        replay = 0,
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effects: SharedFlow<AuthEffect> = mutableEffects.asSharedFlow()

    fun onIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.EmailChanged -> mutableState.update {
                it.copy(email = intent.v, emailError = null, formError = null)
            }

            is LoginIntent.PasswordChanged -> mutableState.update {
                it.copy(password = intent.v, passwordError = null, formError = null)
            }

            LoginIntent.TogglePasswordVisibility -> mutableState.update {
                it.copy(isPasswordVisible = !it.isPasswordVisible)
            }

            LoginIntent.Submit -> submit()

            LoginIntent.NavigateToRegister -> mutableEffects.tryEmit(AuthEffect.NavigateToRegister)

            LoginIntent.NavigateToForgotPassword -> mutableEffects.tryEmit(
                AuthEffect.ShowSnackbar("La recuperación de contraseña aún no está disponible")
            )
        }
    }

    private fun submit() {
        val current = mutableState.value
        if (current.isLoading) return

        val emailError = AuthValidator.emailError(current.email)
        val passwordError = if (current.password.isBlank()) "Ingresa tu contraseña" else null

        if (emailError != null || passwordError != null) {
            mutableState.update {
                it.copy(emailError = emailError, passwordError = passwordError, formError = null)
            }
            return
        }

        mutableState.update {
            it.copy(
                isLoading = true,
                emailError = null,
                passwordError = null,
                formError = null,
                attemptsRemaining = null
            )
        }

        viewModelScope.launch {
            val request = LoginRequest(
                email = current.email.trim().lowercase(),
                password = current.password,
                deviceName = deviceName
            )
            when (val result = repository.login(request)) {
                is AuthResult.Success -> {
                    mutableState.update { it.copy(isLoading = false) }
                    mutableEffects.tryEmit(AuthEffect.NavigateToApp)
                }

                is AuthResult.Failure -> {
                    val message = when (result.error.kind) {
                        AuthErrorKind.INVALID_CREDENTIALS -> "Correo o contraseña incorrectos"
                        else -> result.error.message
                    }
                    mutableState.update {
                        it.copy(
                            isLoading = false,
                            formError = message,
                            attemptsRemaining = result.error.attemptsRemaining
                        )
                    }
                    if (result.error.kind == AuthErrorKind.INVALID_CREDENTIALS ||
                        result.error.kind == AuthErrorKind.RATE_LIMITED ||
                        result.error.retryable
                    ) {
                        mutableEffects.tryEmit(
                            AuthEffect.ShowSnackbar(message, result.error.retryable)
                        )
                    }
                }
            }
        }
    }
}
