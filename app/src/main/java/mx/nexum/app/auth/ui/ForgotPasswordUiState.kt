package mx.nexum.app.auth.ui

data class ForgotPasswordUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val isSubmitted: Boolean = false,
    val requestId: String? = null,
    val feedbackMessage: String? = null,
    val cooldownSeconds: Int = 0
)

sealed interface ForgotPasswordIntent {
    data class EmailChanged(val value: String) : ForgotPasswordIntent
    data object Submit : ForgotPasswordIntent
    data object NavigateToLogin : ForgotPasswordIntent
    data object NavigateToResetPassword : ForgotPasswordIntent
}
