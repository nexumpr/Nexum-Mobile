package mx.nexum.app.financial.data.network

import mx.nexum.app.financial.data.dto.BootstrapResponse
import mx.nexum.app.financial.data.dto.BudgetDto
import mx.nexum.app.financial.data.dto.BudgetReplaceRequest
import mx.nexum.app.financial.data.dto.CategoryDto
import mx.nexum.app.financial.data.dto.ConsentItemDto
import mx.nexum.app.financial.data.dto.ContributionCreateRequest
import mx.nexum.app.financial.data.dto.ContributionDto
import mx.nexum.app.financial.data.dto.ContributionPreviewRequest
import mx.nexum.app.financial.data.dto.ContributionPreviewResponse
import mx.nexum.app.financial.data.dto.CreateGoalRequest
import mx.nexum.app.financial.data.dto.CreateGoalResponse
import mx.nexum.app.financial.data.dto.CreateMovementRequest
import mx.nexum.app.financial.data.dto.CreateRecurringPaymentRequest
import mx.nexum.app.financial.data.dto.DashboardResponse
import mx.nexum.app.financial.data.dto.DeleteAccountRequest
import mx.nexum.app.financial.data.dto.DeleteAccountResponse
import mx.nexum.app.financial.data.dto.EducationTipDto
import mx.nexum.app.financial.data.dto.FaqItemDto
import mx.nexum.app.financial.data.dto.FinancialSummaryOleada3Dto
import mx.nexum.app.financial.data.dto.FinancialSummaryResponse
import mx.nexum.app.financial.data.dto.FrequencyDto
import mx.nexum.app.financial.data.dto.GoalDto
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
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface FinancialApiService {

    @GET("catalogs/categories")
    suspend fun getCategories(
        @Query("locale") locale: String? = null
    ): List<CategoryDto>

    @GET("catalogs/frequencies")
    suspend fun getFrequencies(
        @Query("context") context: String? = null
    ): List<FrequencyDto>

    @GET("bootstrap")
    suspend fun getBootstrap(
        @Header("Authorization") authorization: String,
        @Query("include") include: String? = null
    ): BootstrapResponse

    @GET("users/me/income")
    suspend fun getIncome(
        @Header("Authorization") authorization: String
    ): IncomeDto

    @PATCH("users/me/income")
    suspend fun updateIncome(
        @Header("Authorization") authorization: String,
        @Body request: UpdateIncomeRequest
    ): IncomeDto

    @GET("summary")
    suspend fun getFinancialSummary(
        @Header("Authorization") authorization: String
    ): FinancialSummaryResponse

    @GET("movements")
    suspend fun getMovements(
        @Header("Authorization") authorization: String,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null
    ): List<MovementDto>

    @POST("movements")
    suspend fun createMovement(
        @Header("Authorization") authorization: String,
        @Body request: CreateMovementRequest
    ): MovementDto

    @GET("movements/{movementId}")
    suspend fun getMovementDetail(
        @Header("Authorization") authorization: String,
        @Path("movementId") movementId: String
    ): MovementDto

    @PATCH("movements/{movementId}")
    suspend fun updateMovement(
        @Header("Authorization") authorization: String,
        @Path("movementId") movementId: String,
        @Body request: UpdateMovementRequest
    ): MovementDto

    @DELETE("movements/{movementId}")
    suspend fun deleteMovement(
        @Header("Authorization") authorization: String,
        @Path("movementId") movementId: String,
        @Header("If-Match") ifMatch: String? = null
    )

    @GET("recurring-payments")
    suspend fun getRecurringPayments(
        @Header("Authorization") authorization: String,
        @Query("active") active: Boolean? = null,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null
    ): List<RecurringPaymentDto>

    @POST("recurring-payments")
    suspend fun createRecurringPayment(
        @Header("Authorization") authorization: String,
        @Body request: CreateRecurringPaymentRequest
    ): RecurringPaymentDto

    @GET("recurring-payments/{recurringPaymentId}")
    suspend fun getRecurringPaymentDetail(
        @Header("Authorization") authorization: String,
        @Path("recurringPaymentId") recurringPaymentId: String
    ): RecurringPaymentDto

    @PATCH("recurring-payments/{recurringPaymentId}")
    suspend fun updateRecurringPayment(
        @Header("Authorization") authorization: String,
        @Path("recurringPaymentId") recurringPaymentId: String,
        @Body request: UpdateRecurringPaymentRequest
    ): RecurringPaymentDto

    @DELETE("recurring-payments/{recurringPaymentId}")
    suspend fun deleteRecurringPayment(
        @Header("Authorization") authorization: String,
        @Path("recurringPaymentId") recurringPaymentId: String
    )

    @POST("recurring-payments/{recurringPaymentId}/payments")
    suspend fun recordRecurringPaymentPayment(
        @Header("Authorization") authorization: String,
        @Path("recurringPaymentId") recurringPaymentId: String,
        @Body request: RecordPaymentRequest,
        @Header("Idempotency-Key") idempotencyKey: String? = null
    ): RecordPaymentResponse

    @GET("goals")
    suspend fun getGoals(
        @Header("Authorization") authorization: String,
        @Query("status") status: String? = null,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null
    ): List<GoalDto>

    @POST("goals")
    suspend fun createGoal(
        @Header("Authorization") authorization: String,
        @Body request: CreateGoalRequest
    ): CreateGoalResponse

    @GET("goals/{goalId}")
    suspend fun getGoalDetail(
        @Header("Authorization") authorization: String,
        @Path("goalId") goalId: String
    ): GoalDto

    @GET("users/me/onboarding")
    suspend fun getOnboardingStatus(
        @Header("Authorization") authorization: String
    ): OnboardingStatusResponse

    @PATCH("users/me/onboarding")
    suspend fun updateOnboardingStatus(
        @Header("Authorization") authorization: String,
        @Body request: UpdateOnboardingRequest,
        @Header("Idempotency-Key") idempotencyKey: String? = null
    ): OnboardingStatusResponse

    // Oleada 1 Endpoints (E012, E013, E015, E016, E017, E068, E069, E070, E071)
    @GET("users/me")
    suspend fun getUserProfile(
        @Header("Authorization") authorization: String
    ): UserProfileDto

    @PATCH("users/me")
    suspend fun updateProfile(
        @Header("Authorization") authorization: String,
        @Header("If-Match") ifMatch: String?,
        @Body request: UpdateProfileRequest
    ): UserProfileDto

    @GET("privacy/notices/current")
    suspend fun getCurrentPrivacyNotice(
        @Query("locale") locale: String? = null
    ): PrivacyNoticeDto

    @GET("users/me/consents")
    suspend fun getUserConsents(
        @Header("Authorization") authorization: String
    ): List<ConsentItemDto>

    @POST("users/me/consents")
    suspend fun recordConsent(
        @Header("Authorization") authorization: String,
        @Body request: RecordConsentRequest,
        @Header("Idempotency-Key") idempotencyKey: String? = null
    ): ConsentItemDto

    @GET("users/me/preferences")
    suspend fun getPreferences(
        @Header("Authorization") authorization: String
    ): PreferencesDto

    @PATCH("users/me/preferences")
    suspend fun updatePreferences(
        @Header("Authorization") authorization: String,
        @Header("If-Match") ifMatch: String?,
        @Body request: UpdatePreferencesRequest
    ): PreferencesDto

    @GET("help/faq")
    suspend fun getFaq(
        @Query("locale") locale: String? = null
    ): List<FaqItemDto>

    @GET("education/tips")
    suspend fun getEducationTips(
        @Query("locale") locale: String? = null
    ): List<EducationTipDto>

    // Oleada 2 (E014)
    @HTTP(method = "DELETE", path = "users/me", hasBody = true)
    suspend fun deleteAccount(
        @Header("Authorization") authorization: String,
        @Body request: DeleteAccountRequest
    ): DeleteAccountResponse

    // Oleada 3 Endpoints (E020, E026, E037, E042–E051)
    @GET("users/me/export")
    suspend fun getUserExport(
        @Header("Authorization") authorization: String,
        @Query("limit") limit: Int? = null,
        @Query("cursor") cursor: String? = null
    ): UserExportResponse

    @GET("financial-summary")
    suspend fun getFinancialSummaryOleada3(
        @Header("Authorization") authorization: String,
        @Query("period") period: String? = null
    ): FinancialSummaryOleada3Dto

    @POST("recurring-payments/{recurringPaymentId}/payments")
    suspend fun recordRecurringPaymentPaymentOleada3(
        @Header("Authorization") authorization: String,
        @Path("recurringPaymentId") recurringPaymentId: String,
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body request: RecurringPaymentRecordPaymentRequest
    ): MovementOleada2Dto

    @DELETE("goals/{goalId}")
    suspend fun deleteGoalOleada3(
        @Header("Authorization") authorization: String,
        @Path("goalId") goalId: String
    )

    @POST("goal-contributions/preview")
    suspend fun previewGoalContribution(
        @Header("Authorization") authorization: String,
        @Body request: ContributionPreviewRequest
    ): ContributionPreviewResponse

    @POST("goal-contributions")
    suspend fun createGoalContribution(
        @Header("Authorization") authorization: String,
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body request: ContributionCreateRequest
    ): ContributionDto

    @GET("goal-contributions")
    suspend fun getGoalContributions(
        @Header("Authorization") authorization: String,
        @Query("limit") limit: Int? = null,
        @Query("cursor") cursor: String? = null,
        @Query("goalId") goalId: String? = null,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Query("status") status: String? = null
    ): PageOfContributionResponse

    @GET("goal-contributions/{contributionId}")
    suspend fun getGoalContributionDetail(
        @Header("Authorization") authorization: String,
        @Path("contributionId") contributionId: String
    ): ContributionDto

    @POST("goal-contributions/{contributionId}/reverse")
    suspend fun reverseGoalContribution(
        @Header("Authorization") authorization: String,
        @Path("contributionId") contributionId: String,
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body request: ReverseContributionRequest
    ): ContributionDto

    @GET("budgets")
    suspend fun getBudgets(
        @Header("Authorization") authorization: String,
        @Query("limit") limit: Int? = null,
        @Query("cursor") cursor: String? = null,
        @Query("period") period: String? = null
    ): retrofit2.Response<PageOfBudgetResponse>

    @PUT("budgets")
    suspend fun replaceBudgets(
        @Header("Authorization") authorization: String,
        @Header("If-Match") ifMatch: String,
        @Body request: BudgetReplaceRequest
    ): retrofit2.Response<PageOfBudgetResponse>

    @GET("budgets/usage")
    suspend fun getBudgetUsage(
        @Header("Authorization") authorization: String,
        @Query("period") period: String? = null
    ): PageOfBudgetUsageResponse

    @GET("dashboard")
    suspend fun getDashboard(
        @Header("Authorization") authorization: String,
        @Query("period") period: String? = null
    ): DashboardResponse
}
