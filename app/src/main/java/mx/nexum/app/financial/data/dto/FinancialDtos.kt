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

// Units 360, 370, 380: Income & Financial Summary DTOs
data class IncomeDto(
    @SerializedName("amount") val amount: Double,
    @SerializedName("frequency") val frequency: String,
    @SerializedName("updatedAt") val updatedAt: String?
)

data class UpdateIncomeRequest(
    @SerializedName("amount") val amount: Double,
    @SerializedName("frequency") val frequency: String
)

data class FinancialSummaryResponse(
    @SerializedName("income") val income: Double,
    @SerializedName("fixedExpenses") val fixedExpenses: Double,
    @SerializedName("variableExpenses") val variableExpenses: Double,
    @SerializedName("savings") val savings: Double,
    @SerializedName("available") val available: Double
)

// Units 390, 400, 410, 420, 320: Movements DTOs
data class MovementDto(
    @SerializedName("id") val id: String,
    @SerializedName("concept") val concept: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("categoryId") val categoryId: String,
    @SerializedName("type") val type: String,
    @SerializedName("frequency") val frequency: String?,
    @SerializedName("goalId") val goalId: String?,
    @SerializedName("occurredAt") val occurredAt: String
)

data class CreateMovementRequest(
    @SerializedName("concept") val concept: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("categoryId") val categoryId: String,
    @SerializedName("type") val type: String,
    @SerializedName("frequency") val frequency: String?,
    @SerializedName("goalId") val goalId: String?,
    @SerializedName("occurredAt") val occurredAt: String
)

data class UpdateMovementRequest(
    @SerializedName("concept") val concept: String?,
    @SerializedName("amount") val amount: Double?,
    @SerializedName("categoryId") val categoryId: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("frequency") val frequency: String?,
    @SerializedName("goalId") val goalId: String?,
    @SerializedName("occurredAt") val occurredAt: String?
)

// Units 330, 340: Recurring Payments DTOs
data class RecurringPaymentDto(
    @SerializedName("id") val id: String,
    @SerializedName("concept") val concept: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("categoryId") val categoryId: String,
    @SerializedName("type") val type: String,
    @SerializedName("frequency") val frequency: String,
    @SerializedName("fixedMonthlyEquivalent") val fixedMonthlyEquivalent: Double?,
    @SerializedName("active") val active: Boolean
)

data class CreateRecurringPaymentRequest(
    @SerializedName("concept") val concept: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("categoryId") val categoryId: String,
    @SerializedName("type") val type: String,
    @SerializedName("frequency") val frequency: String
)

data class UpdateRecurringPaymentRequest(
    @SerializedName("concept") val concept: String?,
    @SerializedName("amount") val amount: Double?,
    @SerializedName("categoryId") val categoryId: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("frequency") val frequency: String?
)

data class RecordPaymentRequest(
    @SerializedName("occurredAt") val occurredAt: String,
    @SerializedName("amount") val amount: Double?
)

data class RecordPaymentResponse(
    @SerializedName("movementId") val movementId: String,
    @SerializedName("recurringPaymentId") val recurringPaymentId: String,
    @SerializedName("updatedSummary") val updatedSummary: FinancialSummaryResponse?
)

// Unit 350: Savings Goals DTOs
data class GoalDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("targetAmount") val targetAmount: Double,
    @SerializedName("savedAmount") val savedAmount: Double,
    @SerializedName("initialSaved") val initialSaved: Double,
    @SerializedName("emoji") val emoji: String?,
    @SerializedName("targetDate") val targetDate: String?,
    @SerializedName("status") val status: String,
    @SerializedName("progressPercent") val progressPercent: Double
)

data class CreateGoalRequest(
    @SerializedName("name") val name: String,
    @SerializedName("targetAmount") val targetAmount: Double,
    @SerializedName("initialSaved") val initialSaved: Double,
    @SerializedName("emoji") val emoji: String?,
    @SerializedName("targetDate") val targetDate: String?
)

data class CreateGoalResponse(
    @SerializedName("goalId") val goalId: String,
    @SerializedName("savedAmount") val savedAmount: Double,
    @SerializedName("remaining") val remaining: Double,
    @SerializedName("progressPercent") val progressPercent: Double
)

// Units 300, 310: Onboarding DTOs (NEXUM-E018 & NEXUM-E019)
data class OnboardingStatusResponse(
    @SerializedName("stepsCompleted") val stepsCompleted: List<String>,
    @SerializedName("finishedAt") val finishedAt: String?
)

data class UpdateOnboardingRequest(
    @SerializedName("completed") val completed: Boolean = true,
    @SerializedName("limitedMode") val limitedMode: Boolean = false
)

// Oleada 1 DTOs (E012, E013, E015, E016, E017, E068, E069, E070, E071)
data class UserProfileDto(
    @SerializedName("id") val id: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("displayName") val displayName: String?,
    @SerializedName("emailVerified") val emailVerified: Boolean?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("version") val version: Int?
)

data class UpdateProfileRequest(
    @SerializedName("displayName") val displayName: String
)

data class PrivacyNoticeDto(
    @SerializedName("version") val version: String,
    @SerializedName("title") val title: String,
    @SerializedName("body") val body: String,
    @SerializedName("publishedAt") val publishedAt: String
)

data class ConsentItemDto(
    @SerializedName("noticeVersion") val noticeVersion: String,
    @SerializedName("acceptedAt") val acceptedAt: String,
    @SerializedName("source") val source: String
)

data class RecordConsentRequest(
    @SerializedName("noticeVersion") val noticeVersion: String,
    @SerializedName("accepted") val accepted: Boolean = true
)

data class PreferencesDto(
    @SerializedName("language") val language: String?,
    @SerializedName("timezone") val timezone: String?,
    @SerializedName("version") val version: Int?
)

data class UpdatePreferencesRequest(
    @SerializedName("language") val language: String?,
    @SerializedName("timezone") val timezone: String?
)

data class FaqItemDto(
    @SerializedName("id") val id: String,
    @SerializedName("question") val question: String,
    @SerializedName("answer") val answer: String
)

data class EducationTipDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("category") val category: String?
)

// Oleada 2 DTOs (E014, E027–E041)
data class DeleteAccountRequest(
    @SerializedName("confirmation") val confirmation: String = "DELETE_MY_ACCOUNT",
    @SerializedName("currentPassword") val currentPassword: String
)

data class DeleteAccountResponse(
    @SerializedName("requestId") val requestId: String,
    @SerializedName("status") val status: String,
    @SerializedName("requestedAt") val requestedAt: String,
    @SerializedName("sessionsRevoked") val sessionsRevoked: Boolean
)

data class MovementOleada2Dto(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String,
    @SerializedName("amount") val amount: String,
    @SerializedName("currency") val currency: String = "MXN",
    @SerializedName("categoryId") val categoryId: String,
    @SerializedName("description") val description: String,
    @SerializedName("occurredAt") val occurredAt: String,
    @SerializedName("version") val version: Int,
    @SerializedName("contributionId") val contributionId: String? = null,
    @SerializedName("recurringPaymentId") val recurringPaymentId: String? = null
)

data class MovementsListResponse(
    @SerializedName("items") val items: List<MovementOleada2Dto>,
    @SerializedName("nextCursor") val nextCursor: String? = null
)

data class CreateMovementOleada2Request(
    @SerializedName("type") val type: String,
    @SerializedName("amount") val amount: String,
    @SerializedName("currency") val currency: String = "MXN",
    @SerializedName("categoryId") val categoryId: String,
    @SerializedName("description") val description: String,
    @SerializedName("occurredAt") val occurredAt: String
)

data class UpdateMovementOleada2Request(
    @SerializedName("categoryId") val categoryId: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("occurredAt") val occurredAt: String? = null
)

data class RecurringPaymentOleada2Dto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("amount") val amount: String,
    @SerializedName("currency") val currency: String = "MXN",
    @SerializedName("frequency") val frequency: String,
    @SerializedName("nextDueDate") val nextDueDate: String,
    @SerializedName("active") val active: Boolean,
    @SerializedName("version") val version: Int,
    @SerializedName("type") val type: String,
    @SerializedName("startDate") val startDate: String,
    @SerializedName("dueDay") val dueDay: Int? = null
)

data class RecurringPaymentsListResponse(
    @SerializedName("items") val items: List<RecurringPaymentOleada2Dto>,
    @SerializedName("nextCursor") val nextCursor: String? = null
)

data class CreateRecurringPaymentOleada2Request(
    @SerializedName("name") val name: String,
    @SerializedName("amount") val amount: String,
    @SerializedName("currency") val currency: String = "MXN",
    @SerializedName("frequency") val frequency: String,
    @SerializedName("nextDueDate") val nextDueDate: String,
    @SerializedName("type") val type: String,
    @SerializedName("startDate") val startDate: String,
    @SerializedName("dueDay") val dueDay: Int? = null
)

data class UpdateRecurringPaymentOleada2Request(
    @SerializedName("name") val name: String? = null,
    @SerializedName("amount") val amount: String? = null,
    @SerializedName("active") val active: Boolean? = null,
    @SerializedName("nextDueDate") val nextDueDate: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("startDate") val startDate: String? = null,
    @SerializedName("dueDay") val dueDay: Int? = null
)

data class GoalOleada2Dto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("targetAmount") val targetAmount: String,
    @SerializedName("savedAmount") val savedAmount: String,
    @SerializedName("currency") val currency: String = "MXN",
    @SerializedName("targetDate") val targetDate: String,
    @SerializedName("status") val status: String,
    @SerializedName("version") val version: Int,
    @SerializedName("openingSaved") val openingSaved: String? = null
)

data class GoalTotalsDto(
    @SerializedName("activeCount") val activeCount: Int,
    @SerializedName("saved") val saved: String
)

data class GoalsListResponse(
    @SerializedName("items") val items: List<GoalOleada2Dto>,
    @SerializedName("totals") val totals: GoalTotalsDto? = null,
    @SerializedName("nextCursor") val nextCursor: String? = null
)

data class CreateGoalOleada2Request(
    @SerializedName("name") val name: String,
    @SerializedName("targetAmount") val targetAmount: String,
    @SerializedName("currency") val currency: String = "MXN",
    @SerializedName("targetDate") val targetDate: String,
    @SerializedName("initialSavedAmount") val initialSavedAmount: String? = null
)

data class UpdateGoalOleada2Request(
    @SerializedName("name") val name: String? = null,
    @SerializedName("targetAmount") val targetAmount: String? = null,
    @SerializedName("targetDate") val targetDate: String? = null,
    @SerializedName("status") val status: String? = null
)

// Oleada 3 DTOs (E020, E026, E037, E042–E051)
data class UserExportResponse(
    @SerializedName("profile") val profile: UserProfileDto,
    @SerializedName("exportedAt") val exportedAt: String,
    @SerializedName("movements") val movements: List<MovementOleada2Dto>,
    @SerializedName("goals") val goals: List<GoalOleada2Dto>,
    @SerializedName("consents") val consents: List<ConsentItemDto>,
    @SerializedName("contributions") val contributions: List<ContributionDto>? = null,
    @SerializedName("budgets") val budgets: List<BudgetDto>? = null,
    @SerializedName("recurringPayments") val recurringPayments: List<RecurringPaymentOleada2Dto>? = null,
    @SerializedName("nextCursor") val nextCursor: String? = null
)

data class FinancialSummaryOleada3Dto(
    @SerializedName("period") val period: String,
    @SerializedName("currency") val currency: String = "MXN",
    @SerializedName("income") val income: String,
    @SerializedName("expenses") val expenses: String,
    @SerializedName("savings") val savings: String,
    @SerializedName("net") val net: String,
    @SerializedName("asOf") val asOf: String
)

data class RecurringPaymentRecordPaymentRequest(
    @SerializedName("paidAt") val paidAt: String,
    @SerializedName("amount") val amount: String,
    @SerializedName("currency") val currency: String = "MXN"
)

data class GoalAllocationDto(
    @SerializedName("goalId") val goalId: String,
    @SerializedName("amount") val amount: String
)

data class ContributionPreviewRequest(
    @SerializedName("amount") val amount: String,
    @SerializedName("currency") val currency: String = "MXN",
    @SerializedName("allocations") val allocations: List<GoalAllocationDto>
)

data class ContributionPreviewResponse(
    @SerializedName("amount") val amount: String,
    @SerializedName("currency") val currency: String = "MXN",
    @SerializedName("allocations") val allocations: List<GoalAllocationDto>,
    @SerializedName("remaining") val remaining: String,
    @SerializedName("canCommit") val canCommit: Boolean
)

data class ContributionCreateRequest(
    @SerializedName("amount") val amount: String,
    @SerializedName("currency") val currency: String = "MXN",
    @SerializedName("allocations") val allocations: List<GoalAllocationDto>,
    @SerializedName("occurredAt") val occurredAt: String
)

data class ContributionDto(
    @SerializedName("id") val id: String,
    @SerializedName("amount") val amount: String,
    @SerializedName("currency") val currency: String = "MXN",
    @SerializedName("allocations") val allocations: List<GoalAllocationDto>,
    @SerializedName("occurredAt") val occurredAt: String,
    @SerializedName("status") val status: String,
    @SerializedName("version") val version: Int
)

data class PageOfContributionResponse(
    @SerializedName("items") val items: List<ContributionDto>,
    @SerializedName("nextCursor") val nextCursor: String? = null
)

data class ReverseContributionRequest(
    @SerializedName("reason") val reason: String
)

data class BudgetDto(
    @SerializedName("categoryId") val categoryId: String,
    @SerializedName("period") val period: String,
    @SerializedName("limitAmount") val limitAmount: String,
    @SerializedName("currency") val currency: String = "MXN",
    @SerializedName("version") val version: Int
)

data class PageOfBudgetResponse(
    @SerializedName("items") val items: List<BudgetDto>,
    @SerializedName("nextCursor") val nextCursor: String? = null
)

data class BudgetReplaceItemDto(
    @SerializedName("categoryId") val categoryId: String,
    @SerializedName("limitAmount") val limitAmount: String
)

data class BudgetReplaceRequest(
    @SerializedName("period") val period: String,
    @SerializedName("currency") val currency: String = "MXN",
    @SerializedName("budgets") val budgets: List<BudgetReplaceItemDto>
)

data class BudgetUsageDto(
    @SerializedName("categoryId") val categoryId: String,
    @SerializedName("period") val period: String,
    @SerializedName("usedAmount") val usedAmount: String,
    @SerializedName("limitAmount") val limitAmount: String,
    @SerializedName("currency") val currency: String = "MXN"
)

data class PageOfBudgetUsageResponse(
    @SerializedName("items") val items: List<BudgetUsageDto>,
    @SerializedName("nextCursor") val nextCursor: String? = null
)

data class DashboardResponse(
    @SerializedName("period") val period: String,
    @SerializedName("summary") val summary: FinancialSummaryOleada3Dto,
    @SerializedName("goals") val goals: List<GoalOleada2Dto>,
    @SerializedName("latestMovements") val latestMovements: List<MovementOleada2Dto>,
    @SerializedName("asOf") val asOf: String
)
