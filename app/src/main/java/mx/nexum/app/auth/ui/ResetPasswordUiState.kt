package mx.nexum.app.auth.ui

data class ResetPasswordUiState(
    val token: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val tokenError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isTokenExpiredOrInvalid: Boolean = false,
    val userEmail: String? = null
)

sealed interface ResetPasswordIntent {
    data class TokenChanged(val value: String) : ResetPasswordIntent
    data class PasswordChanged(val value: String) : ResetPasswordIntent
    data class ConfirmPasswordChanged(val value: String) : ResetPasswordIntent
    data object TogglePasswordVisibility : ResetPasswordIntent
    data object Submit : ResetPasswordIntent
    data object NavigateToLogin : ResetPasswordIntent
    data object NavigateToForgotPassword : ResetPasswordIntent
}
