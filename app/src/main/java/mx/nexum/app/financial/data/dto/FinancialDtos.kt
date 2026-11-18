package mx.nexum.app.financial.data.dto

import com.google.gson.annotations.SerializedName

// NEXUM-E022: Categories Catalog DTOs
data class CategoryDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("icon") val icon: String?,
    @SerializedName("active") val active: Boolean
)

// NEXUM-E023: Frequencies Catalog DTOs
data class FrequencyDto(
    @SerializedName("code") val code: String,
    @SerializedName("label") val label: String,
    @SerializedName("validFor") val validFor: List<String>?
)

// NEXUM-E021: Bootstrap DTOs
data class BootstrapResponse(
    @SerializedName("profile") val profile: BootstrapProfileDto?,
    @SerializedName("summary") val summary: BootstrapSummaryDto?,
    @SerializedName("streak") val streak: Map<String, Any>?,
    @SerializedName("config") val config: Map<String, Any>?,
    @SerializedName("counts") val counts: Map<String, Int>?
)

data class BootstrapProfileDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("emailVerified") val emailVerified: Boolean
)

data class BootstrapSummaryDto(
    @SerializedName("income") val income: Double,
    @SerializedName("fixedExpenses") val fixedExpenses: Double,
    @SerializedName("variableExpenses") val variableExpenses: Double,
    @SerializedName("savings") val savings: Double,
    @SerializedName("available") val available: Double
)
