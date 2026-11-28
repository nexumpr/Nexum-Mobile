package mx.nexum.app.financial.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import mx.nexum.app.auth.data.AuthResult
import mx.nexum.app.financial.data.FinancialRepository
import mx.nexum.app.financial.data.dto.*
import mx.nexum.app.financial.data.dto.BootstrapResponse
import mx.nexum.app.financial.data.dto.CategoryDto
import mx.nexum.app.financial.data.dto.ConsentItemDto
import mx.nexum.app.financial.data.dto.CreateGoalRequest
import mx.nexum.app.financial.data.dto.CreateGoalResponse
import mx.nexum.app.financial.data.dto.CreateMovementRequest
import mx.nexum.app.financial.data.dto.CreateRecurringPaymentRequest
import mx.nexum.app.financial.data.dto.EducationTipDto
import mx.nexum.app.financial.data.dto.FaqItemDto
import mx.nexum.app.financial.data.dto.FinancialSummaryResponse
import mx.nexum.app.financial.data.dto.FrequencyDto
import mx.nexum.app.financial.data.dto.GoalDto
import mx.nexum.app.financial.data.dto.IncomeDto
import mx.nexum.app.financial.data.dto.MovementDto
import mx.nexum.app.financial.data.dto.OnboardingStatusResponse
import mx.nexum.app.financial.data.dto.PreferencesDto
import mx.nexum.app.financial.data.dto.PrivacyNoticeDto
import mx.nexum.app.financial.data.dto.RecordConsentRequest
import mx.nexum.app.financial.data.dto.RecordPaymentRequest
import mx.nexum.app.financial.data.dto.RecordPaymentResponse
import mx.nexum.app.financial.data.dto.RecurringPaymentDto
import mx.nexum.app.financial.data.dto.UpdateIncomeRequest
import mx.nexum.app.financial.data.dto.UpdateMovementRequest
import mx.nexum.app.financial.data.dto.UpdateOnboardingRequest
import mx.nexum.app.financial.data.dto.UpdatePreferencesRequest
import mx.nexum.app.financial.data.dto.UpdateProfileRequest
import mx.nexum.app.financial.data.dto.UpdateRecurringPaymentRequest
import mx.nexum.app.financial.data.dto.UserProfileDto
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelRegressionTest {

    private val dispatcher = StandardTestDispatcher()

    private class FakeFinancialRepository(
        private val profile: AuthResult<UserProfileDto>,
        private val preferences: AuthResult<PreferencesDto>,
        private val preferencesFailure: Throwable? = null
    ) : FinancialRepository {
        override suspend fun getCategories(locale: String?): AuthResult<List<CategoryDto>> = throw NotImplementedError()
        override suspend fun getFrequencies(context: String?): AuthResult<List<FrequencyDto>> = throw NotImplementedError()
        override suspend fun getBootstrap(include: String?): AuthResult<BootstrapResponse> = throw NotImplementedError()
        override suspend fun getIncome(): AuthResult<IncomeDto> = throw NotImplementedError()
        override suspend fun updateIncome(amount: Double, frequency: String): AuthResult<IncomeDto> = throw NotImplementedError()
        override suspend fun getFinancialSummary(): AuthResult<FinancialSummaryResponse> = throw NotImplementedError()
        override suspend fun getMovements(page: Int?, limit: Int?): AuthResult<List<MovementDto>> = throw NotImplementedError()
        override suspend fun createMovement(request: CreateMovementRequest): AuthResult<MovementDto> = throw NotImplementedError()
        override suspend fun getMovementDetail(movementId: String): AuthResult<MovementDto> = throw NotImplementedError()
        override suspend fun updateMovement(movementId: String, request: UpdateMovementRequest): AuthResult<MovementDto> = throw NotImplementedError()
        override suspend fun deleteMovement(movementId: String, ifMatch: String?): AuthResult<Unit> = throw NotImplementedError()
        override suspend fun getRecurringPayments(active: Boolean?, page: Int?, limit: Int?): AuthResult<List<RecurringPaymentDto>> = throw NotImplementedError()
        override suspend fun createRecurringPayment(request: CreateRecurringPaymentRequest): AuthResult<RecurringPaymentDto> = throw NotImplementedError()
        override suspend fun getRecurringPaymentDetail(recurringPaymentId: String): AuthResult<RecurringPaymentDto> = throw NotImplementedError()
        override suspend fun updateRecurringPayment(recurringPaymentId: String, request: UpdateRecurringPaymentRequest): AuthResult<RecurringPaymentDto> = throw NotImplementedError()
        override suspend fun deleteRecurringPayment(recurringPaymentId: String): AuthResult<Unit> = throw NotImplementedError()
        override suspend fun recordRecurringPaymentPayment(recurringPaymentId: String, occurredAt: String, amount: Double?, idempotencyKey: String?): AuthResult<RecordPaymentResponse> = throw NotImplementedError()
        override suspend fun getGoals(status: String?, page: Int?, limit: Int?): AuthResult<List<GoalDto>> = throw NotImplementedError()
        override suspend fun createGoal(request: CreateGoalRequest): AuthResult<CreateGoalResponse> = throw NotImplementedError()
        override suspend fun getGoalDetail(goalId: String): AuthResult<GoalDto> = throw NotImplementedError()
        override suspend fun getOnboardingStatus(): AuthResult<OnboardingStatusResponse> = throw NotImplementedError()
        override suspend fun completeOnboarding(limitedMode: Boolean): AuthResult<OnboardingStatusResponse> = throw NotImplementedError()
        override suspend fun getUserProfile(): AuthResult<UserProfileDto> = profile
        override suspend fun updateProfile(displayName: String, ifMatch: String?): AuthResult<UserProfileDto> = throw NotImplementedError()
        override suspend fun getCurrentPrivacyNotice(locale: String?): AuthResult<PrivacyNoticeDto> = throw NotImplementedError()
        override suspend fun getUserConsents(): AuthResult<List<ConsentItemDto>> = throw NotImplementedError()
        override suspend fun recordConsent(noticeVersion: String, idempotencyKey: String?): AuthResult<ConsentItemDto> = throw NotImplementedError()
        override suspend fun getPreferences(): AuthResult<PreferencesDto> {
            preferencesFailure?.let { throw it }
            return preferences
        }
        override suspend fun updatePreferences(language: String?, timezone: String?, ifMatch: String?): AuthResult<PreferencesDto> = throw NotImplementedError()
        override suspend fun getFaq(locale: String?): AuthResult<List<FaqItemDto>> = throw NotImplementedError()
        override suspend fun getEducationTips(locale: String?): AuthResult<List<EducationTipDto>> = throw NotImplementedError()
        override suspend fun deleteAccount(currentPassword: String): AuthResult<DeleteAccountResponse> = throw NotImplementedError()
        override suspend fun getMovementsOleada2(limit: Int?, cursor: String?, type: String?, from: String?, to: String?): AuthResult<List<MovementOleada2Dto>> = throw NotImplementedError()
        override suspend fun createMovementOleada2(request: CreateMovementOleada2Request, idempotencyKey: String?): AuthResult<MovementOleada2Dto> = throw NotImplementedError()
        override suspend fun deleteMovementOleada2(movementId: String): AuthResult<Unit> = throw NotImplementedError()
        override suspend fun getRecurringPaymentsOleada2(limit: Int?, cursor: String?, active: Boolean?, type: String?): AuthResult<List<RecurringPaymentOleada2Dto>> = throw NotImplementedError()
        override suspend fun createRecurringPaymentOleada2(request: CreateRecurringPaymentOleada2Request, idempotencyKey: String?): AuthResult<RecurringPaymentOleada2Dto> = throw NotImplementedError()
        override suspend fun updateRecurringPaymentOleada2(recurringPaymentId: String, ifMatch: String, request: UpdateRecurringPaymentOleada2Request): AuthResult<RecurringPaymentOleada2Dto> = throw NotImplementedError()
        override suspend fun deleteRecurringPaymentOleada2(recurringPaymentId: String): AuthResult<Unit> = throw NotImplementedError()
        override suspend fun getGoalsOleada2(limit: Int?, cursor: String?, status: String?): AuthResult<List<GoalOleada2Dto>> = throw NotImplementedError()
        override suspend fun createGoalOleada2(request: CreateGoalOleada2Request, idempotencyKey: String?): AuthResult<GoalOleada2Dto> = throw NotImplementedError()
        override suspend fun getUserExport(limit: Int?, cursor: String?): AuthResult<UserExportResponse> = throw NotImplementedError()
        override suspend fun getFinancialSummaryOleada3(period: String?): AuthResult<FinancialSummaryOleada3Dto> = throw NotImplementedError()
        override suspend fun recordRecurringPaymentPaymentOleada3(recurringPaymentId: String, paidAt: String, amount: String, currency: String, idempotencyKey: String?): AuthResult<MovementOleada2Dto> = throw NotImplementedError()
        override suspend fun deleteGoalOleada3(goalId: String): AuthResult<Unit> = throw NotImplementedError()
        override suspend fun previewGoalContribution(amount: String, currency: String, allocations: List<GoalAllocationDto>): AuthResult<ContributionPreviewResponse> = throw NotImplementedError()
        override suspend fun createGoalContribution(amount: String, currency: String, allocations: List<GoalAllocationDto>, occurredAt: String, idempotencyKey: String?): AuthResult<ContributionDto> = throw NotImplementedError()
        override suspend fun getGoalContributions(limit: Int?, cursor: String?, goalId: String?, from: String?, to: String?, status: String?): AuthResult<PageOfContributionResponse> = throw NotImplementedError()
        override suspend fun getGoalContributionDetail(contributionId: String): AuthResult<ContributionDto> = throw NotImplementedError()
        override suspend fun reverseGoalContribution(contributionId: String, reason: String, idempotencyKey: String?): AuthResult<ContributionDto> = throw NotImplementedError()
        override suspend fun getBudgets(limit: Int?, cursor: String?, period: String?): AuthResult<Pair<PageOfBudgetResponse, String?>> = throw NotImplementedError()
        override suspend fun replaceBudgets(period: String, currency: String, budgets: List<BudgetReplaceItemDto>, ifMatch: String): AuthResult<Pair<PageOfBudgetResponse, String?>> = throw NotImplementedError()
        override suspend fun getBudgetUsage(period: String?): AuthResult<PageOfBudgetUsageResponse> = throw NotImplementedError()
        override suspend fun getDashboard(period: String?): AuthResult<DashboardResponse> = throw NotImplementedError()
        override suspend fun getInsights(period: String?): AuthResult<PageOfInsightResponse> = throw NotImplementedError()
        override suspend fun getCategoryAnalytics(period: String?): AuthResult<PageOfCategoryAnalyticResponse> = throw NotImplementedError()
        override suspend fun getSpendingPatterns(period: String?): AuthResult<PageOfSpendingPatternResponse> = throw NotImplementedError()
        override suspend fun getRecommendations(period: String?): AuthResult<PageOfRecommendationResponse> = throw NotImplementedError()
        override suspend fun getSavingsStreak(): AuthResult<SavingsStreakDto> = throw NotImplementedError()
        override suspend fun getNotificationPreferences(): AuthResult<NotificationPreferencesDto> = throw NotImplementedError()
        override suspend fun updateNotificationPreferences(request: UpdateNotificationPreferencesRequest, ifMatch: String?): AuthResult<NotificationPreferencesDto> = throw NotImplementedError()
        override suspend fun registerPushToken(token: String, deviceLabel: String, idempotencyKey: String?): AuthResult<PushDeviceResponse> = throw NotImplementedError()
        override suspend fun deletePushToken(deviceRegistrationId: String): AuthResult<Unit> = throw NotImplementedError()
        override suspend fun getNotifications(limit: Int?, cursor: String?, isRead: Boolean?, type: String?): AuthResult<PageOfNotificationResponse> = throw NotImplementedError()
        override suspend fun markNotificationRead(notificationId: String): AuthResult<NotificationDto> = throw NotImplementedError()
        override suspend fun markAllNotificationsRead(): AuthResult<Unit> = throw NotImplementedError()
        override suspend fun getReminders(limit: Int?, cursor: String?, active: Boolean?): AuthResult<PageOfReminderResponse> = throw NotImplementedError()
        override suspend fun createReminder(title: String, dueAt: String, timezone: String, frequency: String?, idempotencyKey: String?): AuthResult<ReminderDto> = throw NotImplementedError()
        override suspend fun updateReminder(reminderId: String, ifMatch: String, title: String?, dueAt: String?, active: Boolean?, frequency: String?): AuthResult<ReminderDto> = throw NotImplementedError()
        override suspend fun deleteReminder(reminderId: String): AuthResult<Unit> = throw NotImplementedError()
        override suspend fun getHealthLive(): AuthResult<HealthDto> = throw NotImplementedError()
        override suspend fun getHealthReady(): AuthResult<HealthDto> = throw NotImplementedError()
        override suspend fun getDemoBootstrap(): AuthResult<DemoBootstrapResponse> = throw NotImplementedError()
        override suspend fun parseVoice(text: String, locale: String): AuthResult<VoiceParseResponse> = throw NotImplementedError()
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun nullLocaleAndMissingProfileFieldsDoNotCrashLoad() = runTest {
        val repository = FakeFinancialRepository(
            profile = AuthResult.Success(UserProfileDto(email = "test@example.test")),
            preferences = AuthResult.Success(PreferencesDto(locale = "es-MX", timezone = "America/Mexico_City", version = 1))
        )

        val viewModel = ProfileViewModel(repository)
        advanceUntilIdle()

        assertEquals("es-MX", viewModel.uiState.value.language)
        assertEquals("America/Mexico_City", viewModel.uiState.value.timezone)
        assertEquals("", viewModel.uiState.value.displayName)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun unexpectedFailureDuringLoadBecomesErrorState() = runTest {
        val repository = FakeFinancialRepository(
            profile = AuthResult.Success(UserProfileDto(displayName = "Usuario")),
            preferences = AuthResult.Success(PreferencesDto()),
            preferencesFailure = IllegalStateException("fallo sintético")
        )

        val viewModel = ProfileViewModel(repository)
        advanceUntilIdle()

        assertEquals("No fue posible cargar el perfil.", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }
}
