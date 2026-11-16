package mx.nexum.app.core.network

/**
 * Adaptador HTTP simulable para pruebas unitarias e integración aislada (M040-R02).
 * Permite inyectar respuestas de éxito, errores de red o códigos HTTP específicos
 * sin necesidad de levantar servidores reales ni conectar a internet.
 */
class SimulatedHttpAdapter<T> {

    var responseToReturn: ApiResult<T>? = null

    suspend fun execute(): ApiResult<T> {
        return responseToReturn ?: ApiResult.NetworkFailure(
            NetworkErrorType.NoConnectivity("Adaptador simulado sin respuesta configurada")
        )
    }

    fun simulateSuccess(value: T, statusCode: Int = 200) {
        responseToReturn = ApiResult.Success(value, statusCode)
    }

    fun simulateHttpError(errorType: HttpErrorType, statusCode: Int) {
        responseToReturn = ApiResult.HttpFailure(errorType, statusCode)
    }

    fun simulateNetworkTimeout(message: String = "SocketTimeoutException simulado") {
        responseToReturn = ApiResult.NetworkFailure(NetworkErrorType.Timeout(message))
    }

    fun simulateNoConnectivity(message: String = "UnknownHostException simulado") {
        responseToReturn = ApiResult.NetworkFailure(NetworkErrorType.NoConnectivity(message))
    }

    fun simulateMalformedJson(message: String = "JsonSyntaxException simulado") {
        responseToReturn = ApiResult.NetworkFailure(NetworkErrorType.MalformedResponse(message))
    }
}
