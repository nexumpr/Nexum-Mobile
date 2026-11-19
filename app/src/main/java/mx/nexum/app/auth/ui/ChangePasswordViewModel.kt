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

class ChangePasswordViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val mutableState = MutableStateFlow(ChangePasswordUiState())
    val state: StateFlow<ChangePasswordUiState> = mutableState.asStateFlow()

    private val mutableEffects = MutableSharedFlow<AuthEffect>(
        replay = 0,
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effects: SharedFlow<AuthEffect> = mutableEffects.asSharedFlow()

    fun onIntent(intent: ChangePasswordIntent) {
        when (intent) {
            is ChangePasswordIntent.CurrentPasswordChanged -> mutableState.update {
                it.copy(currentPassword = intent.value, currentPasswordError = null)
            }
            is ChangePasswordIntent.NewPasswordChanged -> mutableState.update {
                it.copy(newPassword = intent.value, newPasswordError = null)
            }
            is ChangePasswordIntent.ConfirmNewPasswordChanged -> mutableState.update {
                it.copy(confirmNewPassword = intent.value, confirmNewPasswordError = null)
            }
            ChangePasswordIntent.ToggleCurrentPasswordVisibility -> mutableState.update {
                it.copy(isCurrentPasswordVisible = !it.isCurrentPasswordVisible)
            }
            ChangePasswordIntent.ToggleNewPasswordVisibility -> mutableState.update {
                it.copy(isNewPasswordVisible = !it.isNewPasswordVisible)
            }
            ChangePasswordIntent.ToggleConfirmNewPasswordVisibility -> mutableState.update {
                it.copy(isConfirmNewPasswordVisible = !it.isConfirmNewPasswordVisible)
            }
            ChangePasswordIntent.Submit -> submit()
            ChangePasswordIntent.NavigateBack -> mutableEffects.tryEmit(AuthEffect.NavigateToApp)
        }
    }

    private fun submit() {
        val current = mutableState.value
        if (current.isLoading) return

        val currentError = if (current.currentPassword.isBlank()) "Ingresa tu contraseña actual" else null

        val newError = when {
            current.newPassword.isBlank() -> "Ingresa tu nueva contraseña"
            current.newPassword.length < 12 -> "La nueva contraseña debe tener al menos 12 caracteres"
            current.newPassword.length > 128 -> "La contraseña debe tener entre 12 y 128 caracteres"
            current.newPassword == current.currentPassword -> "La nueva contraseña debe ser distinta a la actual"
            !AuthValidator.isStrongPassword(current.newPassword) -> "La nueva contraseña es demasiado débil"
            else -> null
        }

        val confirmError = when {
            current.confirmNewPassword.isBlank() -> "Confirma tu nueva contraseña"
            current.newPassword != current.confirmNewPassword -> "Las contraseñas no coinciden"
            else -> null
        }

        if (currentError != null || newError != null || confirmError != null) {
            mutableState.update {
                it.copy(
                    currentPasswordError = currentError,
                    newPasswordError = newError,
                    confirmNewPasswordError = confirmError
                )
            }
            return
        }

        mutableState.update {
            it.copy(
                isLoading = true,
                currentPasswordError = null,
                newPasswordError = null,
                confirmNewPasswordError = null
            )
        }

        viewModelScope.launch {
            when (val result = repository.changePassword(current.currentPassword, current.newPassword)) {
                is AuthResult.Success -> {
                    // Purga inmediata de campos de contraseña en memoria tras éxito (MO-PR06, MO-PR10)
                    mutableState.update {
                        it.copy(
                            isLoading = false,
                            currentPassword = "",
                            newPassword = "",
                            confirmNewPassword = "",
                            currentPasswordError = null,
                            newPasswordError = null,
                            confirmNewPasswordError = null
                        )
                    }
                    mutableEffects.tryEmit(
                        AuthEffect.ShowSnackbar("Contraseña cambiada con éxito. Inicia sesión con tu nueva clave.")
                    )
                    mutableEffects.tryEmit(AuthEffect.NavigateToLogin)
                }
                is AuthResult.Failure -> {
                    val error = result.error
                    when {
                        error.statusCode == 401 || (error.statusCode == 400 && error.message.contains("actual", ignoreCase = true)) -> {
                            mutableState.update {
                                it.copy(
                                    isLoading = false,
                                    currentPasswordError = error.message
                                )
                            }
                        }
                        error.statusCode == 409 || error.kind == AuthErrorKind.CONFLICT -> {
                            mutableState.update {
                                it.copy(
                                    isLoading = false,
                                    newPasswordError = error.message
                                )
                            }
                        }
                        error.statusCode == 422 || error.kind == AuthErrorKind.VALIDATION -> {
                            mutableState.update {
                                it.copy(
                                    isLoading = false,
                                    newPasswordError = error.message
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
