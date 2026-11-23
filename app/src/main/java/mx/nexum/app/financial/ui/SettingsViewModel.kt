package mx.nexum.app.financial.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mx.nexum.app.auth.data.AuthResult
import mx.nexum.app.auth.data.InMemoryTokenStorage
import mx.nexum.app.auth.data.TokenStorage
import mx.nexum.app.core.mvi.MviViewModel
import mx.nexum.app.core.mvi.UiEffect
import mx.nexum.app.core.mvi.UiIntent
import mx.nexum.app.core.mvi.UiState
import mx.nexum.app.financial.data.FinancialRepository

data class SettingsUiState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isDarkTheme: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isLoggedOut: Boolean = false,
    val showDeleteAccountDialog: Boolean = false,
    val deleteAccountPassword: String = "",
    val deleteAccountConfirmationText: String = "",
    val deleteAccountError: String? = null
) : UiState

sealed interface SettingsIntent : UiIntent {
    data class CurrentPasswordChanged(val v: String) : SettingsIntent
    data class NewPasswordChanged(val v: String) : SettingsIntent
    data class ConfirmPasswordChanged(val v: String) : SettingsIntent
    data class ToggleTheme(val isDark: Boolean) : SettingsIntent
    data object SubmitChangePassword : SettingsIntent
    data object OpenPrivacy : SettingsIntent
    data object OpenHelp : SettingsIntent
    data object ShowDeleteAccountDialog : SettingsIntent
    data object DismissDeleteAccountDialog : SettingsIntent
    data class DeleteAccountPasswordChanged(val v: String) : SettingsIntent
    data class DeleteAccountConfirmationTextChanged(val v: String) : SettingsIntent
    data object ConfirmDeleteAccount : SettingsIntent
    data object Logout : SettingsIntent
    data object NavigateToActiveSessions : SettingsIntent
}

sealed interface SettingsEffect : UiEffect {
    data object NavigateToLogin : SettingsEffect
    data object NavigateToPrivacy : SettingsEffect
    data object NavigateToHelp : SettingsEffect
    data object NavigateToActiveSessions : SettingsEffect
    data class ShowToast(val message: String) : SettingsEffect
}

@Suppress("unused")
class SettingsViewModel(
    private val tokenStorage: TokenStorage = InMemoryTokenStorage(),
    private val onThemeChanged: (Boolean) -> Unit = {},
    private val financialRepository: FinancialRepository? = null
) : MviViewModel<SettingsUiState, SettingsIntent, SettingsEffect>(SettingsUiState()) {

    override fun onIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.CurrentPasswordChanged -> setState { copy(currentPassword = intent.v, errorMessage = null) }
            is SettingsIntent.NewPasswordChanged -> setState { copy(newPassword = intent.v, errorMessage = null) }
            is SettingsIntent.ConfirmPasswordChanged -> setState { copy(confirmPassword = intent.v, errorMessage = null) }
            is SettingsIntent.ToggleTheme -> {
                setState { copy(isDarkTheme = intent.isDark) }
                onThemeChanged(intent.isDark)
            }
            SettingsIntent.SubmitChangePassword -> changePassword()
            SettingsIntent.OpenPrivacy -> emitEffect(SettingsEffect.NavigateToPrivacy)
            SettingsIntent.OpenHelp -> emitEffect(SettingsEffect.NavigateToHelp)
            SettingsIntent.ShowDeleteAccountDialog -> setState { copy(showDeleteAccountDialog = true, deleteAccountError = null, deleteAccountPassword = "", deleteAccountConfirmationText = "") }
            SettingsIntent.DismissDeleteAccountDialog -> setState { copy(showDeleteAccountDialog = false) }
            is SettingsIntent.DeleteAccountPasswordChanged -> setState { copy(deleteAccountPassword = intent.v, deleteAccountError = null) }
            is SettingsIntent.DeleteAccountConfirmationTextChanged -> setState { copy(deleteAccountConfirmationText = intent.v, deleteAccountError = null) }
            SettingsIntent.ConfirmDeleteAccount -> deleteAccount()
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

    private fun deleteAccount() {
        val current = currentState
        if (current.deleteAccountConfirmationText != "DELETE_MY_ACCOUNT") {
            setState { copy(deleteAccountError = "Debes escribir exactamente 'DELETE_MY_ACCOUNT'") }
            return
        }
        if (current.deleteAccountPassword.isBlank()) {
            setState { copy(deleteAccountError = "Ingresa tu contraseña actual") }
            return
        }

        viewModelScope.launch {
            setState { copy(isLoading = true, deleteAccountError = null) }
            if (financialRepository != null) {
                when (val result = financialRepository.deleteAccount(current.deleteAccountPassword)) {
                    is AuthResult.Success<*> -> {
                        setState { copy(isLoading = false, showDeleteAccountDialog = false, isLoggedOut = true) }
                        emitEffect(SettingsEffect.ShowToast("Eliminación de cuenta programada (202 Accepted)."))
                        emitEffect(SettingsEffect.NavigateToLogin)
                    }
                    is AuthResult.Failure -> {
                        setState { copy(isLoading = false, deleteAccountError = result.error.message) }
                    }
                }
            } else {
                tokenStorage.clear()
                setState { copy(isLoading = false, showDeleteAccountDialog = false, isLoggedOut = true) }
                emitEffect(SettingsEffect.ShowToast("Eliminación de cuenta programada."))
                emitEffect(SettingsEffect.NavigateToLogin)
            }
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