package mx.nexum.app.core.network

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import retrofit2.Response
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

/**
 * Adaptador seguro para ejecución de llamadas HTTP a través de Retrofit.
 * Captura excepciones de transporte y mapea códigos de estado de forma determinista.
 */
suspend fun <T> safeApiCall(
    gson: Gson = Gson(),
    apiCall: suspend () -> Response<T>
): ApiResult<T> {
    return try {
        val response = apiCall()
        val statusCode = response.code()

        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                ApiResult.Success(value = body, statusCode = statusCode)
            } else if (statusCode == 204) {
                @Suppress("UNCHECKED_CAST")
                ApiResult.Success(value = Unit as T, statusCode = statusCode)
            } else {
                ApiResult.NetworkFailure(NetworkErrorType.MalformedResponse("El cuerpo de la respuesta es nulo para el código $statusCode"))
            }
        } else {
            val errorBodyString = response.errorBody()?.string()
            val apiErrorDetail = parseErrorDetail(gson, errorBodyString)

            val httpError = when (statusCode) {
                400 -> HttpErrorType.BadRequest(apiErrorDetail)
                401 -> HttpErrorType.Unauthorized(apiErrorDetail)
                403 -> HttpErrorType.Forbidden(apiErrorDetail)
                404 -> HttpErrorType.NotFound(apiErrorDetail)
                409 -> HttpErrorType.Conflict(apiErrorDetail)
                422 -> HttpErrorType.UnprocessableEntity(apiErrorDetail)
                429 -> {
                    val retryAfter = response.headers()["Retry-After"]?.toIntOrNull()
                    HttpErrorType.RateLimited(retryAfterSeconds = retryAfter, errorDetail = apiErrorDetail)
                }
                in 500..599 -> HttpErrorType.ServerError(statusCode, apiErrorDetail?.message ?: "Error interno del servidor")
                else -> HttpErrorType.Other(statusCode, apiErrorDetail?.message ?: "Error HTTP no catalogado ($statusCode)")
            }

            ApiResult.HttpFailure(errorType = httpError, statusCode = statusCode)
        }
    } catch (e: SocketTimeoutException) {
        ApiResult.NetworkFailure(NetworkErrorType.Timeout(e.message ?: "Tiempo de espera agotado al conectar con el servidor"))
    } catch (e: UnknownHostException) {
        ApiResult.NetworkFailure(NetworkErrorType.NoConnectivity(e.message ?: "Sin resolución de nombre o sin conexión a internet"))
    } catch (e: ConnectException) {
        ApiResult.NetworkFailure(NetworkErrorType.NoConnectivity(e.message ?: "Fallo de conexión con el host remoto"))
    } catch (e: SSLException) {
        ApiResult.NetworkFailure(NetworkErrorType.SslFailure(e.message ?: "Fallo en la negociación TLS/SSL"))
    } catch (e: JsonSyntaxException) {
        ApiResult.NetworkFailure(NetworkErrorType.MalformedResponse(e.message ?: "Respuesta JSON no cumple con el esquema esperado"))
    } catch (e: IOException) {
        ApiResult.NetworkFailure(NetworkErrorType.NoConnectivity(e.message ?: "Error de E/S en transporte"))
    } catch (e: Throwable) {
        ApiResult.NetworkFailure(NetworkErrorType.Unknown(e))
    }
}

private fun parseErrorDetail(gson: Gson, json: String?): ApiErrorDetail? {
    if (json.isNullOrBlank()) return null
    return try {
        val parsed = gson.fromJson(json, ApiErrorResponse::class.java)
        parsed?.error
    } catch (_: Exception) {
        null
    }
}
