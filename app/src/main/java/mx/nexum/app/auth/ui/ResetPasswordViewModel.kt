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
import mx.nexum.app.auth.domain.AuthValidator

class ResetPasswordViewModel(
    private val repository: AuthRepository,
    initialEmail: String? = null
) : ViewModel() {

    private val mutableState = MutableStateFlow(ResetPasswordUiState(userEmail = initialEmail))
    val state: StateFlow<ResetPasswordUiState> = mutableState.asStateFlow()

    private val mutableEffects = MutableSharedFlow<AuthEffect>(
        replay = 0,
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effects: SharedFlow<AuthEffect> = mutableEffects.asSharedFlow()

    fun setUserEmail(email: String?) {
        mutableState.update { it.copy(userEmail = email) }
    }

    fun onIntent(intent: ResetPasswordIntent) {
        when (intent) {
            is ResetPasswordIntent.TokenChanged -> mutableState.update {
                it.copy(token = intent.value, tokenError = null, isTokenExpiredOrInvalid = false)
            }
            is ResetPasswordIntent.PasswordChanged -> mutableState.update {
                it.copy(newPassword = intent.value, passwordError = null)
            }
            is ResetPasswordIntent.ConfirmPasswordChanged -> mutableState.update {
                it.copy(confirmPassword = intent.value, confirmPasswordError = null)
            }
            ResetPasswordIntent.TogglePasswordVisibility -> mutableState.update {
                it.copy(isPasswordVisible = !it.isPasswordVisible)
            }
            ResetPasswordIntent.Submit -> submit()
            ResetPasswordIntent.NavigateToLogin -> mutableEffects.tryEmit(AuthEffect.NavigateToLogin)
            ResetPasswordIntent.NavigateToForgotPassword -> mutableEffects.tryEmit(AuthEffect.NavigateToForgotPassword)
        }
    }

    private fun submit() {
        val current = mutableState.value
        if (current.isLoading) return

        val tokenTrimmed = current.token.trim()
        val tokenError = when {
            tokenTrimmed.isBlank() -> "Ingresa el código de recuperación"
            tokenTrimmed.length < 10 -> "El código debe tener al menos 10 caracteres"
            else -> null
        }
        val passwordError = AuthValidator.passwordError(current.newPassword)
        val confirmError = AuthValidator.confirmationError(current.newPassword, current.confirmPassword)

        if (tokenError != null || passwordError != null || confirmError != null) {
            mutableState.update {
                it.copy(
                    tokenError = tokenError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmError
                )
            }
            return
        }

        mutableState.update {
            it.copy(
                isLoading = true,
                tokenError = null,
                passwordError = null,
                confirmPasswordError = null,
                isTokenExpiredOrInvalid = false
            )
        }

        viewModelScope.launch {
            when (val result = repository.resetPassword(current.token.trim(), current.newPassword)) {
                is AuthResult.Success -> {
                    mutableState.update { it.copy(isLoading = false) }
                    mutableEffects.tryEmit(
                        AuthEffect.ShowSnackbar("Contraseña restablecida con éxito. Inicia sesión con tu nueva clave.")
                    )
                    mutableEffects.tryEmit(AuthEffect.NavigateToLogin)
                }
                is AuthResult.Failure -> {
                    val error = result.error
                    when {
                        error.statusCode == 400 -> {
                            mutableState.update {
                                it.copy(
                                    isLoading = false,
                                    isTokenExpiredOrInvalid = true,
                                    tokenError = error.message
                                )
                            }
                        }
                        error.statusCode == 409 || error.kind == AuthErrorKind.CONFLICT -> {
                            mutableState.update {
                                it.copy(
                                    isLoading = false,
                                    isTokenExpiredOrInvalid = true,
                                    tokenError = "Este código de recuperación ya fue utilizado. Solicita uno nuevo."
                                )
                            }
                        }
                        error.statusCode == 422 || error.kind == AuthErrorKind.VALIDATION -> {
                            mutableState.update {
                                it.copy(
                                    isLoading = false,
                                    passwordError = error.message
                                )
                            }
                        }
                        else -> {
                            mutableState.update { it.copy(isLoading = false) }
                            mutableEffects.tryEmit(
                                AuthEffect.ShowSnackbar(
                                    message = error.message,
                                    retry = error.retryable
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
