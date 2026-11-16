package mx.nexum.app.core.network

/**
 * Estructura de error canónica devuelta por el Backend según catálogo NEXUM (M040-R01, M040-R03).
 */
data class ApiErrorDetail(
    val code: String,
    val message: String,
    val details: Map<String, Any>? = null,
    val traceId: String? = null
)

data class ApiErrorResponse(
    val error: ApiErrorDetail
)

/**
 * Clasificación de errores HTTP estándar.
 */
sealed interface HttpErrorType {
    data class BadRequest(val errorDetail: ApiErrorDetail?) : HttpErrorType
    data class Unauthorized(val errorDetail: ApiErrorDetail?) : HttpErrorType
    data class Forbidden(val errorDetail: ApiErrorDetail?) : HttpErrorType
    data class NotFound(val errorDetail: ApiErrorDetail?) : HttpErrorType
    data class Conflict(val errorDetail: ApiErrorDetail?) : HttpErrorType
    data class UnprocessableEntity(val errorDetail: ApiErrorDetail?) : HttpErrorType
    data class RateLimited(val retryAfterSeconds: Int? = null, val errorDetail: ApiErrorDetail? = null) : HttpErrorType
    data class ServerError(val statusCode: Int, val message: String) : HttpErrorType
    data class Other(val statusCode: Int, val message: String) : HttpErrorType
}

/**
 * Clasificación de errores a nivel transporte o red.
 */
sealed interface NetworkErrorType {
    data class Timeout(val message: String) : NetworkErrorType
    data class NoConnectivity(val message: String) : NetworkErrorType
    data class SslFailure(val message: String) : NetworkErrorType
    data class MalformedResponse(val message: String) : NetworkErrorType
    data class Unknown(val cause: Throwable) : NetworkErrorType
}

/**
 * Envoltorio discriminado de resultados HTTP.
 * Garantiza que fallos de red o errores del servidor nunca se presenten como éxito (M040-R01).
 */
sealed interface ApiResult<out T> {
    data class Success<out T>(val value: T, val statusCode: Int = 200) : ApiResult<T>
    data class HttpFailure(val errorType: HttpErrorType, val statusCode: Int) : ApiResult<Nothing>
    data class NetworkFailure(val errorType: NetworkErrorType) : ApiResult<Nothing>
}
