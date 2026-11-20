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

data class RefreshRequest(
    val refreshToken: String,
    val deviceId: String? = null
)

data class ForgotPasswordRequest(
    val email: String
)

data class ForgotPasswordResponse(
    val status: String? = "accepted",
    val requestId: String? = null,
    val message: String? = null
)

data class ResetPasswordRequest(
    val token: String,
    val newPassword: String
)

data class ResetPasswordResponse(
    val status: String? = "accepted",
    val requestId: String? = null
)

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

data class ChangePasswordResponse(
    val status: String = "accepted",
    val requestId: String
)

data class SessionsPageResponse(
    val items: List<SessionItemResponse>,
    val nextCursor: String? = null
)

data class SessionItemResponse(
    val id: String,
    val createdAt: String,
    val lastSeenAt: String,
    val current: Boolean,
    val revoked: Boolean
)

data class RefreshResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long,
    val sessionId: String? = null,
    val session: SessionIdDto? = null
) {
    val resolvedSessionId: String?
        get() = sessionId ?: session?.id
}

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

data class RefreshRequestDto(
    @SerializedName("refreshToken") val refreshToken: String,
    @SerializedName("deviceId") val deviceId: String? = null
)

data class RefreshResponseDto(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String,
    @SerializedName("tokenType") val tokenType: String? = "Bearer",
    @SerializedName("expiresIn") val expiresIn: Long,
    @SerializedName("sessionId") val sessionId: String? = null,
    @SerializedName("session") val session: SessionIdDto? = null
)

data class ForgotPasswordRequestDto(
    @SerializedName("email") val email: String
)

data class ForgotPasswordResponseDto(
    @SerializedName("status") val status: String? = "accepted",
    @SerializedName("requestId") val requestId: String? = null,
    @SerializedName("message") val message: String? = null
)

data class ResetPasswordRequestDto(
    @SerializedName("token") val token: String,
    @SerializedName("newPassword") val newPassword: String
)

data class ResetPasswordResponseDto(
    @SerializedName("status") val status: String? = "accepted",
    @SerializedName("requestId") val requestId: String? = null
)

data class ChangePasswordRequestDto(
    @SerializedName("currentPassword") val currentPassword: String,
    @SerializedName("newPassword") val newPassword: String
)

data class ChangePasswordResponseDto(
    @SerializedName("status") val status: String? = "accepted",
    @SerializedName("requestId") val requestId: String? = null
)

data class SessionsPageResponseDto(
    @SerializedName("items") val items: List<SessionItemResponseDto>,
    @SerializedName("nextCursor") val nextCursor: String? = null
)

data class SessionItemResponseDto(
    @SerializedName("id") val id: String,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("lastSeenAt") val lastSeenAt: String,
    @SerializedName("current") val current: Boolean,
    @SerializedName("revoked") val revoked: Boolean
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
