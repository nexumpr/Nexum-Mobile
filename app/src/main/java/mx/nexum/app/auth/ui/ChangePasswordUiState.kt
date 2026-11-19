package mx.nexum.app.auth.ui

data class ChangePasswordUiState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmNewPassword: String = "",
    val isCurrentPasswordVisible: Boolean = false,
    val isNewPasswordVisible: Boolean = false,
    val isConfirmNewPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val currentPasswordError: String? = null,
    val newPasswordError: String? = null,
    val confirmNewPasswordError: String? = null
)

sealed interface ChangePasswordIntent {
    data class CurrentPasswordChanged(val value: String) : ChangePasswordIntent
    data class NewPasswordChanged(val value: String) : ChangePasswordIntent
    data class ConfirmNewPasswordChanged(val value: String) : ChangePasswordIntent
    data object ToggleCurrentPasswordVisibility : ChangePasswordIntent
    data object ToggleNewPasswordVisibility : ChangePasswordIntent
    data object ToggleConfirmNewPasswordVisibility : ChangePasswordIntent
    data object Submit : ChangePasswordIntent
    data object NavigateBack : ChangePasswordIntent
}
