package mx.nexum.app.financial.data

import kotlinx.coroutines.CancellationException
import mx.nexum.app.auth.data.AuthError
import mx.nexum.app.auth.data.AuthErrorKind
import mx.nexum.app.auth.data.AuthResult
import mx.nexum.app.auth.data.TokenStorage
import mx.nexum.app.financial.data.dto.BootstrapResponse
import mx.nexum.app.financial.data.dto.CategoryDto
import mx.nexum.app.financial.data.dto.CreateMovementRequest
import mx.nexum.app.financial.data.dto.FinancialSummaryResponse
import mx.nexum.app.financial.data.dto.FrequencyDto
import mx.nexum.app.financial.data.dto.IncomeDto
import mx.nexum.app.financial.data.dto.MovementDto
import mx.nexum.app.financial.data.dto.UpdateIncomeRequest
import mx.nexum.app.financial.data.dto.UpdateMovementRequest
import mx.nexum.app.financial.data.network.FinancialApiService

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

    companion object {
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

    private fun Throwable.toFinancialError(): AuthError = when (this) {
        is CancellationException -> throw this
        else -> AuthError(AuthErrorKind.UNKNOWN, "Error de red al procesar movimientos financieros.", retryable = true)
    }
}
