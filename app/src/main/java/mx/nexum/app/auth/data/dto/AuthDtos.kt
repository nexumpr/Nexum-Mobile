package mx.nexum.app.auth.data.dto

import com.google.gson.annotations.SerializedName

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

data class User(
    val id: String,
    val name: String,
    val email: String,
    val emailVerified: Boolean = false
)

data class LoginRequest(
    val email: String,
    val password: String,
    val deviceId: String? = null,
    val deviceName: String? = null,
    val platform: String? = null
)

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String? = null,
    val expiresIn: Long,
    val sessionId: String? = null,
    val session: SessionIdDto? = null,
    val user: User,
    val onboarding: OnboardingConfigDto? = null
)

data class SessionIdDto(
    @SerializedName("id") val id: String
)

data class OnboardingConfigDto(
    @SerializedName("configured") val configured: Boolean
)

data class LoginRequestDto(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("deviceId") val deviceId: String? = null,
    @SerializedName("deviceName") val deviceName: String? = null,
    @SerializedName("platform") val platform: String? = null
)

data class LoginUserDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("emailVerified") val emailVerified: Boolean
)

data class LoginResponseDto(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String,
    @SerializedName("tokenType") val tokenType: String?,
    @SerializedName("expiresIn") val expiresIn: Long,
    @SerializedName("sessionId") val sessionId: String?,
    @SerializedName("session") val session: SessionIdDto?,
    @SerializedName("user") val user: LoginUserDto,
    @SerializedName("onboarding") val onboarding: OnboardingConfigDto?
)

data class ApiErrorDetail(
    @SerializedName("field") val field: String?,
    @SerializedName("message") val message: String?
)

data class ApiErrorPayload(
    @SerializedName("code") val code: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("traceId") val traceId: String?,
    @SerializedName("details") val details: List<ApiErrorDetail>?
)

data class ApiErrorResponse(
    @SerializedName("error") val error: ApiErrorPayload?
)
