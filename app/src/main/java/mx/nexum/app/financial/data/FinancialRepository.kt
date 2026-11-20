package mx.nexum.app.financial.data

import kotlinx.coroutines.CancellationException
import mx.nexum.app.auth.data.AuthError
import mx.nexum.app.auth.data.AuthErrorKind
import mx.nexum.app.auth.data.AuthResult
import mx.nexum.app.auth.data.TokenStorage
import mx.nexum.app.financial.data.dto.BootstrapResponse
import mx.nexum.app.financial.data.dto.CategoryDto
import mx.nexum.app.financial.data.dto.CreateGoalRequest
import mx.nexum.app.financial.data.dto.CreateGoalResponse
import mx.nexum.app.financial.data.dto.CreateMovementRequest
import mx.nexum.app.financial.data.dto.CreateRecurringPaymentRequest
import mx.nexum.app.financial.data.dto.FinancialSummaryResponse
import mx.nexum.app.financial.data.dto.FrequencyDto
import mx.nexum.app.financial.data.dto.GoalDto
import mx.nexum.app.financial.data.dto.IncomeDto
import mx.nexum.app.financial.data.dto.MovementDto
import mx.nexum.app.financial.data.dto.OnboardingStatusResponse
import mx.nexum.app.financial.data.dto.RecordPaymentRequest
import mx.nexum.app.financial.data.dto.RecordPaymentResponse
import mx.nexum.app.financial.data.dto.RecurringPaymentDto
import mx.nexum.app.financial.data.dto.UpdateIncomeRequest
import mx.nexum.app.financial.data.dto.UpdateMovementRequest
import mx.nexum.app.financial.data.dto.UpdateOnboardingRequest
import mx.nexum.app.financial.data.dto.UpdateRecurringPaymentRequest
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
            val response = api.getFinancialSummary("Bearer $token")
            AuthResult.Success(response)
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

    private fun Throwable.toFinancialError(): AuthError = when (this) {
        is CancellationException -> throw this
        else -> AuthError(AuthErrorKind.UNKNOWN, "Error de red al procesar onboarding.", retryable = true)
    }
}
