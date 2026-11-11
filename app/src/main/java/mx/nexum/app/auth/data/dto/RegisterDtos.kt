package mx.nexum.app.auth.data.dto

import com.google.gson.annotations.SerializedName

data class IncomeDto(
    @SerializedName("amount") val amount: String,
    @SerializedName("frequency") val frequency: String = "monthly"
)

data class RecurringExpenseDto(
    @SerializedName("concept") val concept: String,
    @SerializedName("amount") val amount: String,
    @SerializedName("frequency") val frequency: String = "monthly",
    @SerializedName("category") val category: String = "Servicios",
    @SerializedName("type") val type: String = "fixed"
)

data class PrivacyDto(
    @SerializedName("accepted") val accepted: Boolean = true,
    @SerializedName("noticeVersion") val noticeVersion: String = "2026-11"
)

data class ClientDto(
    @SerializedName("locale") val locale: String,
    @SerializedName("timeZone") val timeZone: String
)

data class RegisterRequestDto(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("income") val income: IncomeDto,
    @SerializedName("recurringExpenses") val recurringExpenses: List<RecurringExpenseDto> = emptyList(),
    @SerializedName("privacy") val privacy: PrivacyDto,
    @SerializedName("client") val client: ClientDto
)

data class RegisterResponseDto(
    @SerializedName("userId") val userId: String? = null,
    @SerializedName("id") val id: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("onboardingStatus") val onboardingStatus: String? = null,
    @SerializedName("accessToken") val accessToken: String? = null,
    @SerializedName("refreshToken") val refreshToken: String? = null
)
