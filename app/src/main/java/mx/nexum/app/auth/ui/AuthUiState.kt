package mx.nexum.app.auth.ui

import mx.nexum.app.auth.data.dto.ExpenseType
import mx.nexum.app.auth.data.dto.Frequency

data class ExpenseDraft(
    val concept: String = "",
    val amount: String = "",
    val categoryId: String = "",
    val type: ExpenseType = ExpenseType.fixed
)

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val incomeAmount: String = "",
    val incomeFrequency: Frequency = Frequency.monthly,
    val expenses: List<ExpenseDraft> = listOf(ExpenseDraft()),
    val termsAccepted: Boolean = false,
    val isLoading: Boolean = false,
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val incomeError: String? = null,
    val expensesError: String? = null,
    val termsError: String? = null,
    val formError: String? = null
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
