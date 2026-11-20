package mx.nexum.app.financial.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mx.nexum.app.auth.data.InMemoryTokenStorage
import mx.nexum.app.auth.data.TokenStorage
import mx.nexum.app.core.mvi.MviViewModel
import mx.nexum.app.core.mvi.UiEffect
import mx.nexum.app.core.mvi.UiIntent
import mx.nexum.app.core.mvi.UiState

data class SettingsUiState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isLoggedOut: Boolean = false
) : UiState

sealed interface SettingsIntent : UiIntent {
    data class CurrentPasswordChanged(val v: String) : SettingsIntent
    data class NewPasswordChanged(val v: String) : SettingsIntent
    data class ConfirmPasswordChanged(val v: String) : SettingsIntent
    data object SubmitChangePassword : SettingsIntent
    data object Logout : SettingsIntent
    data object NavigateToActiveSessions : SettingsIntent
}

sealed interface SettingsEffect : UiEffect {
    data object NavigateToLogin : SettingsEffect
    data object NavigateToActiveSessions : SettingsEffect
    data class ShowToast(val message: String) : SettingsEffect
}

@Suppress("unused")
class SettingsViewModel(
    private val tokenStorage: TokenStorage = InMemoryTokenStorage()
) : MviViewModel<SettingsUiState, SettingsIntent, SettingsEffect>(SettingsUiState()) {

    override fun onIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.CurrentPasswordChanged -> setState { copy(currentPassword = intent.v, errorMessage = null) }
            is SettingsIntent.NewPasswordChanged -> setState { copy(newPassword = intent.v, errorMessage = null) }
            is SettingsIntent.ConfirmPasswordChanged -> setState { copy(confirmPassword = intent.v, errorMessage = null) }
            SettingsIntent.SubmitChangePassword -> changePassword()
            SettingsIntent.Logout -> logout()
            SettingsIntent.NavigateToActiveSessions -> {
                viewModelScope.launch {
                    emitEffect(SettingsEffect.NavigateToActiveSessions)
                }
            }
        }
    }

    private fun changePassword() {
        val current = currentState
        if (current.currentPassword.isBlank() || current.newPassword.length < 8) {
            setState { copy(errorMessage = "La nueva contraseña debe tener al menos 8 caracteres.") }
            return
        }
        if (current.newPassword != current.confirmPassword) {
            setState { copy(errorMessage = "Las contraseñas no coinciden.") }
            return
        }

        viewModelScope.launch {
            setState { copy(isLoading = true, errorMessage = null, successMessage = null) }
            kotlinx.coroutines.delay(600)
            setState {
                copy(
                    isLoading = false,
                    successMessage = "Contraseña actualizada exitosamente.",
                    currentPassword = "",
                    newPassword = "",
                    confirmPassword = ""
                )
            }
            emitEffect(SettingsEffect.ShowToast("Contraseña actualizada exitosamente."))
        }
    }

    private fun logout() {
        viewModelScope.launch {
            tokenStorage.clear()
            setState { copy(isLoggedOut = true) }
            emitEffect(SettingsEffect.ShowToast("Sesión cerrada."))
            emitEffect(SettingsEffect.NavigateToLogin)
        }
    }
}
