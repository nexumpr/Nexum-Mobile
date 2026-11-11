package mx.nexum.app.auth.ui

data class ExpenseDraft(
    val concept: String = "",
    val amount: String = "",
    val category: String = "Servicios",
    val frequency: String = "monthly",
    val type: String = "fixed",
    val categoryId: String = ""
)

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val incomeAmount: String = "",
    val incomeFrequency: String = "monthly",
    val expenses: List<ExpenseDraft> = emptyList(),
    val termsAccepted: Boolean = false,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val incomeError: String? = null,
    val expensesError: String? = null,
    val termsError: String? = null,
    val formError: String? = null,
    val retryAfterSeconds: String? = null
)

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val formError: String? = null,
    val attemptsRemaining: Int? = null
)
