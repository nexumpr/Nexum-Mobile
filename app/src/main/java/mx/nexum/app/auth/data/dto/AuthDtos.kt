package mx.nexum.app.auth.data.dto

enum class Frequency { monthly }

enum class ExpenseType { fixed }

data class Income(
    val amount: String,
    val frequency: Frequency
)

data class RecurringExpense(
    val concept: String,
    val amount: String,
    val frequency: Frequency,
    val categoryId: String,
    val type: ExpenseType
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val income: Income,
    val recurringExpenses: List<RecurringExpense>,
    val privacyNoticeVersion: String,
    val consentAcceptedAt: String
)

data class RegisterResponse(
    val userId: String,
    val onboardingStatus: String,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val expiresIn: Long? = null,
    val verificationRequired: Boolean = false
)

data class LoginRequest(
    val email: String,
    val password: String,
    val deviceName: String? = null
)

data class User(
    val id: String,
    val name: String,
    val email: String
)

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val user: User
)
