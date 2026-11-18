package mx.nexum.app.financial.data

import kotlinx.coroutines.CancellationException
import mx.nexum.app.auth.data.AuthError
import mx.nexum.app.auth.data.AuthErrorKind
import mx.nexum.app.auth.data.AuthResult
import mx.nexum.app.auth.data.TokenStorage
import mx.nexum.app.financial.data.dto.BootstrapResponse
import mx.nexum.app.financial.data.dto.CategoryDto
import mx.nexum.app.financial.data.dto.FrequencyDto
import mx.nexum.app.financial.data.network.FinancialApiService

interface FinancialRepository {
    suspend fun getCategories(locale: String? = null): AuthResult<List<CategoryDto>>
    suspend fun getFrequencies(context: String? = null): AuthResult<List<FrequencyDto>>
    suspend fun getBootstrap(include: String? = null): AuthResult<BootstrapResponse>
}

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

    private fun Throwable.toFinancialError(): AuthError = when (this) {
        is CancellationException -> throw this
        else -> AuthError(AuthErrorKind.UNKNOWN, "Error de red al consultar datos financieros.", retryable = true)
    }
}
