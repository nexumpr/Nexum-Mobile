package mx.nexum.app.financial.data

import kotlinx.coroutines.CancellationException
import mx.nexum.app.auth.data.AuthError
import mx.nexum.app.auth.data.AuthErrorKind
import mx.nexum.app.auth.data.AuthResult
import mx.nexum.app.auth.data.TokenStorage
import mx.nexum.app.financial.data.dto.BootstrapResponse
import mx.nexum.app.financial.data.dto.BudgetReplaceItemDto
import mx.nexum.app.financial.data.dto.BudgetReplaceRequest
import mx.nexum.app.financial.data.dto.CategoryDto
import mx.nexum.app.financial.data.dto.ConsentItemDto
import mx.nexum.app.financial.data.dto.ContributionCreateRequest
import mx.nexum.app.financial.data.dto.ContributionDto
import mx.nexum.app.financial.data.dto.ContributionPreviewRequest
import mx.nexum.app.financial.data.dto.ContributionPreviewResponse
import mx.nexum.app.financial.data.dto.CreateGoalOleada2Request
import mx.nexum.app.financial.data.dto.CreateGoalRequest
import mx.nexum.app.financial.data.dto.CreateGoalResponse
import mx.nexum.app.financial.data.dto.CreateMovementOleada2Request
import mx.nexum.app.financial.data.dto.CreateMovementRequest
import mx.nexum.app.financial.data.dto.CreateRecurringPaymentOleada2Request
import mx.nexum.app.financial.data.dto.CreateRecurringPaymentRequest
import mx.nexum.app.financial.data.dto.DashboardResponse
import mx.nexum.app.financial.data.dto.DeleteAccountRequest
import mx.nexum.app.financial.data.dto.DeleteAccountResponse
import mx.nexum.app.financial.data.dto.EducationTipDto
import mx.nexum.app.financial.data.dto.FaqItemDto
import mx.nexum.app.financial.data.dto.FinancialSummaryOleada3Dto
import mx.nexum.app.financial.data.dto.FinancialSummaryResponse
import mx.nexum.app.financial.data.dto.FrequencyDto
import mx.nexum.app.financial.data.dto.GoalAllocationDto
import mx.nexum.app.financial.data.dto.GoalDto
import mx.nexum.app.financial.data.dto.GoalOleada2Dto
import mx.nexum.app.financial.data.dto.IncomeDto
import mx.nexum.app.financial.data.dto.MovementDto
import mx.nexum.app.financial.data.dto.MovementOleada2Dto
import mx.nexum.app.financial.data.dto.OnboardingStatusResponse
import mx.nexum.app.financial.data.dto.PageOfBudgetResponse
import mx.nexum.app.financial.data.dto.PageOfBudgetUsageResponse
import mx.nexum.app.financial.data.dto.PageOfContributionResponse
import mx.nexum.app.financial.data.dto.PreferencesDto
import mx.nexum.app.financial.data.dto.PrivacyNoticeDto
import mx.nexum.app.financial.data.dto.RecordConsentRequest
import mx.nexum.app.financial.data.dto.RecordPaymentRequest
import mx.nexum.app.financial.data.dto.RecordPaymentResponse
import mx.nexum.app.financial.data.dto.RecurringPaymentDto
import mx.nexum.app.financial.data.dto.RecurringPaymentOleada2Dto
import mx.nexum.app.financial.data.dto.RecurringPaymentRecordPaymentRequest
import mx.nexum.app.financial.data.dto.ReverseContributionRequest
import mx.nexum.app.financial.data.dto.UpdateIncomeRequest
import mx.nexum.app.financial.data.dto.UpdateMovementRequest
import mx.nexum.app.financial.data.dto.UpdateOnboardingRequest
import mx.nexum.app.financial.data.dto.UpdatePreferencesRequest
import mx.nexum.app.financial.data.dto.UpdateProfileRequest
import mx.nexum.app.financial.data.dto.UpdateRecurringPaymentRequest
import mx.nexum.app.financial.data.dto.UserExportResponse
import mx.nexum.app.financial.data.dto.UserProfileDto
import mx.nexum.app.financial.data.network.FinancialApiService
import java.util.UUID

interface FinancialRepository {
    suspend fun getCategories(locale: String? = null): AuthResult<List<CategoryDto>>
    suspend fun getFrequencies(context: String? = null): AuthResult<List<FrequencyDto>>
    suspend fun getBootstrap(include: String? = null): AuthResult<BootstrapResponse>
    suspend fun getIncome(): AuthResult<IncomeDto>
    suspend fun updateIncome(amount: Double, frequency: String): AuthResult<IncomeDto>
    suspend fun getFinancialSummary(): AuthResult<FinancialSummaryResponse>
    suspend fun getMovements(page: Int? = null, limit: Int? = null): AuthResult<List<MovementDto>>
    suspend fun createMovement(request: CreateMovementRequest): AuthResult<MovementDto>
    suspend fun getMovementDetail(movementId: String): AuthResult<MovementDto>
    suspend fun updateMovement(movementId: String, request: UpdateMovementRequest): AuthResult<MovementDto>
    suspend fun deleteMovement(movementId: String, ifMatch: String? = null): AuthResult<Unit>
    suspend fun getRecurringPayments(active: Boolean? = null, page: Int? = null, limit: Int? = null): AuthResult<List<RecurringPaymentDto>>
    suspend fun createRecurringPayment(request: CreateRecurringPaymentRequest): AuthResult<RecurringPaymentDto>
    suspend fun getRecurringPaymentDetail(recurringPaymentId: String): AuthResult<RecurringPaymentDto>
    suspend fun updateRecurringPayment(recurringPaymentId: String, request: UpdateRecurringPaymentRequest): AuthResult<RecurringPaymentDto>
    suspend fun deleteRecurringPayment(recurringPaymentId: String): AuthResult<Unit>
    suspend fun recordRecurringPaymentPayment(recurringPaymentId: String, occurredAt: String, amount: Double?, idempotencyKey: String? = null): AuthResult<RecordPaymentResponse>
    suspend fun getGoals(status: String? = null, page: Int? = null, limit: Int? = null): AuthResult<List<GoalDto>>
    suspend fun createGoal(request: CreateGoalRequest): AuthResult<CreateGoalResponse>
    suspend fun getGoalDetail(goalId: String): AuthResult<GoalDto>
    suspend fun getOnboardingStatus(): AuthResult<OnboardingStatusResponse>
    suspend fun completeOnboarding(limitedMode: Boolean): AuthResult<OnboardingStatusResponse>

    // Oleada 1 Methods (E012, E013, E015, E016, E017, E068, E069, E070, E071)
    suspend fun getUserProfile(): AuthResult<UserProfileDto>
    suspend fun updateProfile(displayName: String, ifMatch: String? = null): AuthResult<UserProfileDto>
    suspend fun getCurrentPrivacyNotice(locale: String? = null): AuthResult<PrivacyNoticeDto>
    suspend fun getUserConsents(): AuthResult<List<ConsentItemDto>>
    suspend fun recordConsent(noticeVersion: String, idempotencyKey: String? = null): AuthResult<ConsentItemDto>
    suspend fun getPreferences(): AuthResult<PreferencesDto>
    suspend fun updatePreferences(language: String?, timezone: String?, ifMatch: String? = null): AuthResult<PreferencesDto>
    suspend fun getFaq(locale: String? = null): AuthResult<List<FaqItemDto>>
    suspend fun getEducationTips(locale: String? = null): AuthResult<List<EducationTipDto>>

    // Oleada 2 Methods (E014, E027–E041)
    suspend fun deleteAccount(currentPassword: String): AuthResult<DeleteAccountResponse>
    suspend fun getMovementsOleada2(limit: Int? = null, cursor: String? = null, type: String? = null, from: String? = null, to: String? = null): AuthResult<List<MovementOleada2Dto>>
    suspend fun createMovementOleada2(request: CreateMovementOleada2Request, idempotencyKey: String? = null): AuthResult<MovementOleada2Dto>
    suspend fun deleteMovementOleada2(movementId: String): AuthResult<Unit>

    suspend fun getRecurringPaymentsOleada2(limit: Int? = null, cursor: String? = null, active: Boolean? = null, type: String? = null): AuthResult<List<RecurringPaymentOleada2Dto>>
    suspend fun createRecurringPaymentOleada2(request: CreateRecurringPaymentOleada2Request, idempotencyKey: String? = null): AuthResult<RecurringPaymentOleada2Dto>
    suspend fun deleteRecurringPaymentOleada2(recurringPaymentId: String): AuthResult<Unit>

    suspend fun getGoalsOleada2(limit: Int? = null, cursor: String? = null, status: String? = null): AuthResult<List<GoalOleada2Dto>>
    suspend fun createGoalOleada2(request: CreateGoalOleada2Request, idempotencyKey: String? = null): AuthResult<GoalOleada2Dto>

    // Oleada 3 Methods (E020, E026, E037, E042–E051)
    suspend fun getUserExport(limit: Int? = null, cursor: String? = null): AuthResult<UserExportResponse>
    suspend fun getFinancialSummaryOleada3(period: String? = null): AuthResult<FinancialSummaryOleada3Dto>
    suspend fun recordRecurringPaymentPaymentOleada3(recurringPaymentId: String, paidAt: String, amount: String, currency: String = "MXN", idempotencyKey: String? = null): AuthResult<MovementOleada2Dto>
    suspend fun deleteGoalOleada3(goalId: String): AuthResult<Unit>
    suspend fun previewGoalContribution(amount: String, currency: String = "MXN", allocations: List<GoalAllocationDto>): AuthResult<ContributionPreviewResponse>
    suspend fun createGoalContribution(amount: String, currency: String = "MXN", allocations: List<GoalAllocationDto>, occurredAt: String, idempotencyKey: String? = null): AuthResult<ContributionDto>
    suspend fun getGoalContributions(limit: Int? = null, cursor: String? = null, goalId: String? = null, from: String? = null, to: String? = null, status: String? = null): AuthResult<PageOfContributionResponse>
    suspend fun getGoalContributionDetail(contributionId: String): AuthResult<ContributionDto>
    suspend fun reverseGoalContribution(contributionId: String, reason: String, idempotencyKey: String? = null): AuthResult<ContributionDto>
    suspend fun getBudgets(limit: Int? = null, cursor: String? = null, period: String? = null): AuthResult<Pair<PageOfBudgetResponse, String?>>
    suspend fun replaceBudgets(period: String, currency: String = "MXN", budgets: List<BudgetReplaceItemDto>, ifMatch: String): AuthResult<Pair<PageOfBudgetResponse, String?>>
    suspend fun getBudgetUsage(period: String? = null): AuthResult<PageOfBudgetUsageResponse>
    suspend fun getDashboard(period: String? = null): AuthResult<DashboardResponse>

    companion object {
        @Suppress("unused")
        fun create(
            api: FinancialApiService,
            tokenStorage: TokenStorage
        ): FinancialRepository = FinancialRepositoryImpl(api, tokenStorage)
    }
}

@Suppress("unused")
class FinancialRepositoryImpl(
    private val api: FinancialApiService,
    private val tokenStorage: TokenStorage
) : FinancialRepository {

    override suspend fun getCategories(locale: String?): AuthResult<List<CategoryDto>> =
        try {
            val categories = api.getCategories(locale)
            AuthResult.Success(categories)
        } catch (error: Throwable) {
            AuthResult.Failure(error.toFinancialError())
        }

    override suspend fun getFrequencies(context: String?): AuthResult<List<FrequencyDto>> =
        try {
            val frequencies = api.getFrequencies(context)
            AuthResult.Success(frequencies)
        } catch (error: Throwable) {
            AuthResult.Failure(error.toFinancialError())
        }

    override suspend fun getBootstrap(include: String?): AuthResult<BootstrapResponse> =
        try {
            val token = tokenStorage.current()?.accessToken ?: ""
            val authHeader = "Bearer $token"
            val response = api.getBootstrap(authHeader, include)
            AuthResult.Success(response)
        } catch (error: Throwable) {
            AuthResult.Failure(error.toFinancialError())
        }

    override suspend fun getIncome(): AuthResult<IncomeDto> =
        try {
            val token = tokenStorage.current()?.accessToken ?: ""
            val response = api.getIncome("Bearer $token")
            AuthResult.Success(response)
        } catch (error: Throwable) {
            AuthResult.Failure(error.toFinancialError())
        }

    override suspend fun updateIncome(amount: Double, frequency: String): AuthResult<IncomeDto> =
        try {
            val token = tokenStorage.current()?.accessToken ?: ""
            val request = UpdateIncomeRequest(amount, frequency)
            val response = api.updateIncome("Bearer $token", request)
            AuthResult.Success(response)
        } catch (error: Throwable) {
            AuthResult.Failure(error.toFinancialError())
        }

    override suspend fun getFinancialSummary(): AuthResult<FinancialSummaryResponse> =
        try {
            val token = tokenStorage.current()?.accessToken ?: ""
            val response = api.getFinancialSummaryOleada3("Bearer $token", null)
            val summary = FinancialSummaryResponse(
                income = response.income.toDoubleOrNull() ?: 0.0,
                fixedExpenses = response.expenses.toDoubleOrNull() ?: 0.0,
                variableExpenses = 0.0,
                savings = response.savings.toDoubleOrNull() ?: 0.0,
                available = response.net.toDoubleOrNull() ?: 0.0
            )
            AuthResult.Success(summary)
        } catch (error: Throwable) {
            AuthResult.Failure(error.toFinancialError())
        }

    override suspend fun getMovements(page: Int?, limit: Int?): AuthResult<List<MovementDto>> =
        try {
            val token = tokenStorage.current()?.accessToken ?: ""
            val response = api.getMovements("Bearer $token", page, limit)
            AuthResult.Success(response)
        } catch (error: Throwable) {
            AuthResult.Failure(error.toFinancialError())
        }

    override suspend fun createMovement(request: CreateMovementRequest): AuthResult<MovementDto> =
        try {
            val token = tokenStorage.current()?.accessToken ?: ""
            val response = api.createMovement("Bearer $token", request)
            AuthResult.Success(response)
        } catch (error: Throwable) {
            AuthResult.Failure(error.toFinancialError())
        }

    override suspend fun getMovementDetail(movementId: String): AuthResult<MovementDto> =
        try {
            val token = tokenStorage.current()?.accessToken ?: ""
            val response = api.getMovementDetail("Bearer $token", movementId)
            AuthResult.Success(response)
        } catch (error: Throwable) {
            AuthResult.Failure(error.toFinancialError())
        }

    override suspend fun updateMovement(movementId: String, request: UpdateMovementRequest): AuthResult<MovementDto> =
        try {
            val token = tokenStorage.current()?.accessToken ?: ""
            val response = api.updateMovement("Bearer $token", movementId, request)
            AuthResult.Success(response)
        } catch (error: Throwable) {
            AuthResult.Failure(error.toFinancialError())
        }

    override suspend fun deleteMovement(movementId: String, ifMatch: String?): AuthResult<Unit> =
        try {
            val token = tokenStorage.current()?.accessToken ?: ""
            api.deleteMovement("Bearer $token", movementId, ifMatch)
            AuthResult.Success(Unit)
        } catch (error: Throwable) {
            AuthResult.Failure(error.toFinancialError())
        }

    override suspend fun getRecurringPayments(active: Boolean?, page: Int?, limit: Int?): AuthResult<List<RecurringPaymentDto>> =
        try {
            val token = tokenStorage.current()?.accessToken ?: ""
            val response = api.getRecurringPayments("Bearer $token", active, page, limit)
            AuthResult.Success(response)
        } catch (error: Throwable) {
            AuthResult.Failure(error.toFinancialError())
        }

    override suspend fun createRecurringPayment(request: CreateRecurringPaymentRequest): AuthResult<RecurringPaymentDto> =
        try {
            val token = tokenStorage.current()?.accessToken ?: ""
            val response = api.createRecurringPayment("Bearer $token", request)
            AuthResult.Success(response)
        } catch (error: Throwable) {
            AuthResult.Failure(error.toFinancialError())
        }

    override suspend fun getRecurringPaymentDetail(recurringPaymentId: String): AuthResult<RecurringPaymentDto> =
        try {
            val token = tokenStorage.current()?.accessToken ?: ""
            val response = api.getRecurringPaymentDetail("Bearer $token", recurringPaymentId)
            AuthResult.Success(response)
        } catch (error: Throwable) {
            AuthResult.Failure(error.toFinancialError())
        }

    override suspend fun updateRecurringPayment(recurringPaymentId: String, request: UpdateRecurringPaymentRequest): AuthResult<RecurringPaymentDto> =
        try {
            val token = tokenStorage.current()?.accessToken ?: ""
            val response = api.updateRecurringPayment("Bearer $token", recurringPaymentId, request)
            AuthResult.Success(response)
        } catch (error: Throwable) {
            AuthResult.Failure(error.toFinancialError())
        }

    override suspend fun deleteRecurringPayment(recurringPaymentId: String): AuthResult<Unit> =
        try {
            val token = tokenStorage.current()?.accessToken ?: ""
            api.deleteRecurringPayment("Bearer $token", recurringPaymentId)
            AuthResult.Success(Unit)
        } catch (error: Throwable) {
            AuthResult.Failure(error.toFinancialError())
        }

    override suspend fun recordRecurringPaymentPayment(recurringPaymentId: String, occurredAt: String, amount: Double?, idempotencyKey: String?): AuthResult<RecordPaymentResponse> =
        try {
            val token = tokenStorage.current()?.accessToken ?: ""
            val request = RecordPaymentRequest(occurredAt, amount)
            val response = api.recordRecurringPaymentPayment("Bearer $token", recurringPaymentId, request, idempotencyKey)
            AuthResult.Success(response)
        } catch (error: Throwable) {
            AuthResult.Failure(error.toFinancialError())
        }

    override suspend fun getGoals(status: String?, page: Int?, limit: Int?): AuthResult<List<GoalDto>> =
        try {
            val token = tokenStorage.current()?.accessToken ?: ""
            val response = api.getGoals("Bearer $token", status, page, limit)
            AuthResult.Success(response)
        } catch (error: Throwable) {
            AuthResult.Failure(error.toFinancialError())
        }

    override suspend fun createGoal(request: CreateGoalRequest): AuthResult<CreateGoalResponse> =
        try {
            val token = tokenStorage.current()?.accessToken ?: ""
            val response = api.createGoal("Bearer $token", request)
            AuthResult.Success(response)
        } catch (error: Throwable) {
            AuthResult.Failure(error.toFinancialError())
        }

    override suspend fun getGoalDetail(goalId: String): AuthResult<GoalDto> =
        try {
            val token = tokenStorage.current()?.accessToken ?: ""
            val response = api.getGoalDetail("Bearer $token", goalId)
            AuthResult.Success(response)
        } catch (error: Throwable) {
            AuthResult.Failure(error.toFinancialError())
        }

    override suspend fun getOnboardingStatus(): AuthResult<OnboardingStatusResponse> =
        try {
            val token = tokenStorage.current()?.accessToken ?: ""
            val response = api.getOnboardingStatus("Bearer $token")
            AuthResult.Success(response)
        } catch (error: Throwable) {
            AuthResult.Failure(error.toFinancialError())
        }

    override suspend fun completeOnboarding(limitedMode: Boolean): AuthResult<OnboardingStatusResponse> =
        try {
            val token = tokenStorage.current()?.accessToken ?: ""
            val request = UpdateOnboardingRequest(completed = true, limitedMode = limitedMode)
            val idempotencyKey = UUID.randomUUID().toString()
            val response = api.updateOnboardingStatus("Bearer $token", request, idempotencyKey)
            AuthResult.Success(response)
        } catch (error: Throwable) {
            AuthResult.Failure(error.toFinancialError())
        }

    override suspend fun getUserProfile(): AuthResult<UserProfileDto> =
        try {
            val token = tokenStorage.current()?.accessToken ?: ""
            val response = api.getUserProfile("Bearer $token")
            AuthResult.Success(response)
        } catch (error: Throwable) {
            AuthResult.Failure(error.toFinancialError())
        }

    override suspend fun updateProfile(displayName: String, ifMatch: String?): AuthResult<UserProfileDto> =
        try {
            val token = tokenStorage.current()?.accessToken ?: ""
            val request = UpdateProfileRequest(displayName)
            val response = api.updateProfile("Bearer $token", ifMatch, request)
            AuthResult.Success(response)
        } catch (error: Throwable) {
            AuthResult.Failure(error.toFinancialError())
        }

    override suspend fun getCurrentPrivacyNotice(locale: String?): AuthResult<PrivacyNoticeDto> =
        try {
            val response = api.getCurrentPrivacyNotice(locale)
            AuthResult.Success(response)
        } catch (error: Throwable) {
            AuthResult.Failure(error.toFinancialError())
        }

    override suspend fun getUserConsents(): AuthResult<List<ConsentItemDto>> =
        try {
            val token = tokenStorage.current()?.accessToken ?: ""
            val response = api.getUserConsents("Bearer $token")
            AuthResult.Success(response)
        } catch (error: Throwable) {
            AuthResult.Failure(error.toFinancialError())
        }

    override suspend fun recordConsent(noticeVersion: String, idempotencyKey: String?): AuthResult<ConsentItemDto> =
        try {
            val token = tokenStorage.current()?.accessToken ?: ""
            val request = RecordConsentRequest(noticeVersion, accepted = true)
            val key = idempotencyKey ?: UUID.randomUUID().toString()
            val response = api.recordConsent("Bearer $token", request, key)
            AuthResult.Success(response)
        } catch (error: Throwable) {
            AuthResult.Failure(error.toFinancialError())
        }

    override suspend fun getPreferences(): AuthResult<PreferencesDto> =
        try {
            val token = tokenStorage.current()?.accessToken ?: ""
            val response = api.getPreferences("Bearer $token")
            AuthResult.Success(response)
        } catch (error: Throwable) {
            AuthResult.Failure(error.toFinancialError())
        }

    override suspend fun updatePreferences(language: String?, timezone: String?, ifMatch: String?): AuthResult<PreferencesDto> =
        try {
            val token = tokenStorage.current()?.accessToken ?: ""
            val request = UpdatePreferencesRequest(language, timezone)
            val response = api.updatePreferences("Bearer $token", ifMatch, request)
            AuthResult.Success(response)
        } catch (error: Throwable) {
            AuthResult.Failure(error.toFinancialError())
        }

    override suspend fun getFaq(locale: String?): AuthResult<List<FaqItemDto>> =
        try {
            val response = api.getFaq(locale)
            AuthResult.Success(response)
        } catch (error: Throwable) {
            AuthResult.Failure(error.toFinancialError())
        }

    override suspend fun getEducationTips(locale: String?): AuthResult<List<EducationTipDto>> =
        try {
            val response = api.getEducationTips(locale)
            AuthResult.Success(response)
        } catch (error: Throwable) {
            AuthResult.Failure(error.toFinancialError())
        }

    // Oleada 2 Implementation (E014, E027–E041)
    override suspend fun deleteAccount(currentPassword: String): AuthResult<DeleteAccountResponse> =
        try {
            val token = tokenStorage.current()?.accessToken ?: ""
            val request = DeleteAccountRequest(confirmation = "DELETE_MY_ACCOUNT", currentPassword = currentPassword)
            val response = api.deleteAccount("Bearer $token", request)
            tokenStorage.clear()
            AuthResult.Success(response)
        } catch (error: Throwable) {
            tokenStorage.clear()
            AuthResult.Failure(error.toFinancialError())
        }

    override suspend fun getMovementsOleada2(limit: Int?, cursor: String?, type: String?, from: String?, to: String?): AuthResult<List<MovementOleada2Dto>> =
        try {
            val token = tokenStorage.current()?.accessToken ?: ""
            val response = api.getMovementsOleada2("Bearer $token", limit, cursor, type, from, to)
            AuthResult.Success(response.items)
        } catch (error: Throwable) {
            AuthResult.Failure(error.toFinancialError())
        }

    override suspend fun createMovementOleada2(request: CreateMovementOleada2Request, idempotencyKey: String?): AuthResult<MovementOleada2Dto> =
        try {
            val token = tokenStorage.current()?.accessToken ?: ""
            val key = idempotencyKey ?: ("android-mov-" + UUID.randomUUID().toString())
            val response = api.createMovementOleada2("Bearer $token", key, request)
            AuthResult.Success(response)
        } catch (error: Throwable) {
            AuthResult.Failure(error.toFinancialError())
        }

    override suspend fun deleteMovementOleada2(movementId: String): AuthResult<Unit> =
        try {
            val token = tokenStorage.current()?.accessToken ?: ""
            api.deleteMovementOleada2("Bearer $token", movementId)
            AuthResult.Success(Unit)
        } catch (error: Throwable) {
            AuthResult.Failure(error.toFinancialError())
        }

    override suspend fun getRecurringPaymentsOleada2(limit: Int?, cursor: String?, active: Boolean?, type: String?): AuthResult<List<RecurringPaymentOleada2Dto>> =
        try {
            val token = tokenStorage.current()?.accessToken ?: ""
            val response = api.getRecurringPaymentsOleada2("Bearer $token", limit, cursor, active, type)
            AuthResult.Success(response.items)
        } catch (error: Throwable) {
            AuthResult.Failure(error.toFinancialError())
        }

    override suspend fun createRecurringPaymentOleada2(request: CreateRecurringPaymentOleada2Request, idempotencyKey: String?): AuthResult<RecurringPaymentOleada2Dto> =
        try {
            val token = tokenStorage.current()?.accessToken ?: ""
            val key = idempotencyKey ?: ("android-rec-" + UUID.randomUUID().toString())
            val response = api.createRecurringPaymentOleada2("Bearer $token", key, request)
            AuthResult.Success(response)
        } catch (error: Throwable) {
            AuthResult.Failure(error.toFinancialError())
        }

    override suspend fun deleteRecurringPaymentOleada2(recurringPaymentId: String): AuthResult<Unit> =
        try {
            val token = tokenStorage.current()?.accessToken ?: ""
            api.deleteRecurringPaymentOleada2("Bearer $token", recurringPaymentId)
            AuthResult.Success(Unit)
        } catch (error: Throwable) {
            AuthResult.Failure(error.toFinancialError())
        }

    override suspend fun getGoalsOleada2(limit: Int?, cursor: String?, status: String?): AuthResult<List<GoalOleada2Dto>> =
        try {
            val token = tokenStorage.current()?.accessToken ?: ""
            val response = api.getGoalsOleada2("Bearer $token", limit, cursor, status)
            AuthResult.Success(response.items)
        } catch (error: Throwable) {
            AuthResult.Failure(error.toFinancialError())
        }

    override suspend fun createGoalOleada2(request: CreateGoalOleada2Request, idempotencyKey: String?): AuthResult<GoalOleada2Dto> =
        try {
            val token = tokenStorage.current()?.accessToken ?: ""
            val key = idempotencyKey ?: ("android-goal-" + UUID.randomUUID().toString())
            val response = api.createGoalOleada2("Bearer $token", key, request)
            AuthResult.Success(response)
        } catch (error: Throwable) {
            AuthResult.Failure(error.toFinancialError())
        }

    // Oleada 3 Implementation (E020, E026, E037, E042–E051)
    override suspend fun getUserExport(limit: Int?, cursor: String?): AuthResult<UserExportResponse> =
        try {
            val token = tokenStorage.current()?.accessToken ?: ""
            val response = api.getUserExport("Bearer $token", limit, cursor)
            AuthResult.Success(response)
        } catch (error: Throwable) {
            AuthResult.Failure(error.toFinancialError())
        }

    override suspend fun getFinancialSummaryOleada3(period: String?): AuthResult<FinancialSummaryOleada3Dto> =
        try {
            val token = tokenStorage.current()?.accessToken ?: ""
            val response = api.getFinancialSummaryOleada3("Bearer $token", period)
            AuthResult.Success(response)
        } catch (error: Throwable) {
            AuthResult.Failure(error.toFinancialError())
        }

    override suspend fun recordRecurringPaymentPaymentOleada3(recurringPaymentId: String, paidAt: String, amount: String, currency: String, idempotencyKey: String?): AuthResult<MovementOleada2Dto> =
        try {
            val token = tokenStorage.current()?.accessToken ?: ""
            val key = idempotencyKey ?: ("android-pay-" + UUID.randomUUID().toString())
            val request = RecurringPaymentRecordPaymentRequest(paidAt, amount, currency)
            val response = api.recordRecurringPaymentPaymentOleada3("Bearer $token", recurringPaymentId, key, request)
            AuthResult.Success(response)
        } catch (error: Throwable) {
            AuthResult.Failure(error.toFinancialError())
        }

    override suspend fun deleteGoalOleada3(goalId: String): AuthResult<Unit> =
        try {
            val token = tokenStorage.current()?.accessToken ?: ""
            api.deleteGoalOleada3("Bearer $token", goalId)
            AuthResult.Success(Unit)
        } catch (error: Throwable) {
            AuthResult.Failure(error.toFinancialError())
        }

    override suspend fun previewGoalContribution(amount: String, currency: String, allocations: List<GoalAllocationDto>): AuthResult<ContributionPreviewResponse> =
        try {
            val token = tokenStorage.current()?.accessToken ?: ""
            val request = ContributionPreviewRequest(amount, currency, allocations)
            val response = api.previewGoalContribution("Bearer $token", request)
            AuthResult.Success(response)
        } catch (error: Throwable) {
            AuthResult.Failure(error.toFinancialError())
        }

    override suspend fun createGoalContribution(amount: String, currency: String, allocations: List<GoalAllocationDto>, occurredAt: String, idempotencyKey: String?): AuthResult<ContributionDto> =
        try {
            val token = tokenStorage.current()?.accessToken ?: ""
            val key = idempotencyKey ?: ("android-contrib-" + UUID.randomUUID().toString())
            val request = ContributionCreateRequest(amount, currency, allocations, occurredAt)
            val response = api.createGoalContribution("Bearer $token", key, request)
            AuthResult.Success(response)
        } catch (error: Throwable) {
            AuthResult.Failure(error.toFinancialError())
        }

    override suspend fun getGoalContributions(limit: Int?, cursor: String?, goalId: String?, from: String?, to: String?, status: String?): AuthResult<PageOfContributionResponse> =
        try {
            val token = tokenStorage.current()?.accessToken ?: ""
            val response = api.getGoalContributions("Bearer $token", limit, cursor, goalId, from, to, status)
            AuthResult.Success(response)
        } catch (error: Throwable) {
            AuthResult.Failure(error.toFinancialError())
        }

    override suspend fun getGoalContributionDetail(contributionId: String): AuthResult<ContributionDto> =
        try {
            val token = tokenStorage.current()?.accessToken ?: ""
            val response = api.getGoalContributionDetail("Bearer $token", contributionId)
            AuthResult.Success(response)
        } catch (error: Throwable) {
            AuthResult.Failure(error.toFinancialError())
        }

    override suspend fun reverseGoalContribution(contributionId: String, reason: String, idempotencyKey: String?): AuthResult<ContributionDto> =
        try {
            val token = tokenStorage.current()?.accessToken ?: ""
            val key = idempotencyKey ?: ("android-rev-" + UUID.randomUUID().toString())
            val request = ReverseContributionRequest(reason)
            val response = api.reverseGoalContribution("Bearer $token", contributionId, key, request)
            AuthResult.Success(response)
        } catch (error: Throwable) {
            AuthResult.Failure(error.toFinancialError())
        }

    override suspend fun getBudgets(limit: Int?, cursor: String?, period: String?): AuthResult<Pair<PageOfBudgetResponse, String?>> =
        try {
            val token = tokenStorage.current()?.accessToken ?: ""
            val response = api.getBudgets("Bearer $token", limit, cursor, period)
            val body = response.body()
            val etag = response.headers()["ETag"]
            if (response.isSuccessful && body != null) {
                AuthResult.Success(Pair(body, etag))
            } else {
                AuthResult.Failure(AuthError(AuthErrorKind.UNKNOWN, "Error al consultar presupuestos.", retryable = true))
            }
        } catch (error: Throwable) {
            AuthResult.Failure(error.toFinancialError())
        }

    override suspend fun replaceBudgets(period: String, currency: String, budgets: List<BudgetReplaceItemDto>, ifMatch: String): AuthResult<Pair<PageOfBudgetResponse, String?>> =
        try {
            val token = tokenStorage.current()?.accessToken ?: ""
            val request = BudgetReplaceRequest(period, currency, budgets)
            val response = api.replaceBudgets("Bearer $token", ifMatch, request)
            val body = response.body()
            val etag = response.headers()["ETag"]
            if (response.isSuccessful && body != null) {
                AuthResult.Success(Pair(body, etag))
            } else {
                AuthResult.Failure(AuthError(AuthErrorKind.UNKNOWN, "Error al guardar presupuestos.", retryable = true))
            }
        } catch (error: Throwable) {
            AuthResult.Failure(error.toFinancialError())
        }

    override suspend fun getBudgetUsage(period: String?): AuthResult<PageOfBudgetUsageResponse> =
        try {
            val token = tokenStorage.current()?.accessToken ?: ""
            val response = api.getBudgetUsage("Bearer $token", period)
            AuthResult.Success(response)
        } catch (error: Throwable) {
            AuthResult.Failure(error.toFinancialError())
        }

    override suspend fun getDashboard(period: String?): AuthResult<DashboardResponse> =
        try {
            val token = tokenStorage.current()?.accessToken ?: ""
            val response = api.getDashboard("Bearer $token", period)
            AuthResult.Success(response)
        } catch (error: Throwable) {
            AuthResult.Failure(error.toFinancialError())
        }

    private fun Throwable.toFinancialError(): AuthError = when (this) {
        is CancellationException -> throw this
        is retrofit2.HttpException -> {
            val errorBody = response()?.errorBody()?.string()
            val errorMessage = try {
                val json = com.google.gson.JsonParser.parseString(errorBody).asJsonObject
                json.getAsJsonObject("error")?.get("message")?.asString ?: "Error del servidor (${code()})"
            } catch (_: Exception) {
                "Error HTTP ${code()}: ${message()}"
            }
            AuthError(AuthErrorKind.UNKNOWN, errorMessage, retryable = code() >= 500)
        }
        else -> AuthError(AuthErrorKind.UNKNOWN, "Error de red: ${localizedMessage ?: "desconocido"}", retryable = true)
    }
}
