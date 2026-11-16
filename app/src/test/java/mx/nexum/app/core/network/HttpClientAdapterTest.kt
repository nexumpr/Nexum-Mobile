package mx.nexum.app.core.network

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.test.runTest
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class HttpClientAdapterTest {

    private val gson = Gson()

    data class SamplePayload(val id: String, val name: String)

    @Test
    fun successResponseReturnsParsedBody() = runTest {
        val expected = SamplePayload("123", "Item de prueba")
        val response = Response.success(expected)

        val result = safeApiCall(gson) { response }

        assertTrue(result is ApiResult.Success)
        val success = result as ApiResult.Success
        assertEquals("123", success.value.id)
        assertEquals("Item de prueba", success.value.name)
        assertEquals(200, success.statusCode)
    }

    @Test
    fun http400BadRequestParsesErrorEnvelope() = runTest {
        val errorJson = """{"error":{"code":"INVALID_FORMAT","message":"Campo inválido","traceId":"tr_1"}}"""
        val response = Response.error<SamplePayload>(
            400,
            errorJson.toResponseBody("application/json".toMediaTypeOrNull())
        )

        val result = safeApiCall(gson) { response }

        assertTrue(result is ApiResult.HttpFailure)
        val failure = result as ApiResult.HttpFailure
        assertEquals(400, failure.statusCode)
        assertTrue(failure.errorType is HttpErrorType.BadRequest)
        val badRequest = failure.errorType as HttpErrorType.BadRequest
        assertEquals("INVALID_FORMAT", badRequest.errorDetail?.code)
        assertEquals("Campo inválido", badRequest.errorDetail?.message)
        assertEquals("tr_1", badRequest.errorDetail?.traceId)
    }

    @Test
    fun http401UnauthorizedMapsCorrectly() = runTest {
        val errorJson = """{"error":{"code":"AUTH_EXPIRED","message":"Token caducado"}}"""
        val response = Response.error<SamplePayload>(
            401,
            errorJson.toResponseBody("application/json".toMediaTypeOrNull())
        )

        val result = safeApiCall(gson) { response }

        assertTrue(result is ApiResult.HttpFailure)
        val failure = result as ApiResult.HttpFailure
        assertEquals(401, failure.statusCode)
        assertTrue(failure.errorType is HttpErrorType.Unauthorized)
    }

    @Test
    fun http404NotFoundMapsCorrectly() = runTest {
        val errorJson = """{"error":{"code":"NOT_FOUND","message":"Recurso no encontrado"}}"""
        val response = Response.error<SamplePayload>(
            404,
            errorJson.toResponseBody("application/json".toMediaTypeOrNull())
        )

        val result = safeApiCall(gson) { response }

        assertTrue(result is ApiResult.HttpFailure)
        val failure = result as ApiResult.HttpFailure
        assertEquals(404, failure.statusCode)
        assertTrue(failure.errorType is HttpErrorType.NotFound)
    }

    @Test
    fun http409ConflictMapsCorrectly() = runTest {
        val errorJson = """{"error":{"code":"CONFLICT","message":"El recurso ya existe"}}"""
        val response = Response.error<SamplePayload>(
            409,
            errorJson.toResponseBody("application/json".toMediaTypeOrNull())
        )

        val result = safeApiCall(gson) { response }

        assertTrue(result is ApiResult.HttpFailure)
        val failure = result as ApiResult.HttpFailure
        assertEquals(409, failure.statusCode)
        assertTrue(failure.errorType is HttpErrorType.Conflict)
    }

    @Test
    fun http429RateLimitedExtractsRetryAfterHeader() = runTest {
        val errorJson = """{"error":{"code":"TOO_MANY_REQUESTS","message":"Límite de peticiones excedido"}}"""
        val headers = Headers.headersOf("Retry-After", "30")
        val response = Response.error<SamplePayload>(
            errorJson.toResponseBody("application/json".toMediaTypeOrNull()),
            okhttp3.Response.Builder()
                .code(429)
                .message("Too Many Requests")
                .protocol(okhttp3.Protocol.HTTP_1_1)
                .request(okhttp3.Request.Builder().url("https://nexum.mx").build())
                .headers(headers)
                .build()
        )

        val result = safeApiCall(gson) { response }

        assertTrue(result is ApiResult.HttpFailure)
        val failure = result as ApiResult.HttpFailure
        assertEquals(429, failure.statusCode)
        assertTrue(failure.errorType is HttpErrorType.RateLimited)
        val rateLimited = failure.errorType as HttpErrorType.RateLimited
        assertEquals(30, rateLimited.retryAfterSeconds)
    }

    @Test
    fun http500ServerErrorNeverPresentsFakeSuccess() = runTest {
        val errorJson = """{"error":{"code":"INTERNAL_SERVER_ERROR","message":"Fallo de base de datos"}}"""
        val response = Response.error<SamplePayload>(
            500,
            errorJson.toResponseBody("application/json".toMediaTypeOrNull())
        )

        val result = safeApiCall(gson) { response }

        assertFalse(result is ApiResult.Success)
        assertTrue(result is ApiResult.HttpFailure)
        val failure = result as ApiResult.HttpFailure
        assertEquals(500, failure.statusCode)
        assertTrue(failure.errorType is HttpErrorType.ServerError)
    }

    @Test
    fun socketTimeoutExceptionMapsToNetworkTimeout() = runTest {
        val result = safeApiCall<SamplePayload>(gson) {
            throw SocketTimeoutException("Conexión expirada tras 30s")
        }

        assertTrue(result is ApiResult.NetworkFailure)
        val failure = result as ApiResult.NetworkFailure
        assertTrue(failure.errorType is NetworkErrorType.Timeout)
        assertEquals("Conexión expirada tras 30s", (failure.errorType as NetworkErrorType.Timeout).message)
    }

    @Test
    fun unknownHostExceptionMapsToNoConnectivity() = runTest {
        val result = safeApiCall<SamplePayload>(gson) {
            throw UnknownHostException("No address associated with hostname")
        }

        assertTrue(result is ApiResult.NetworkFailure)
        val failure = result as ApiResult.NetworkFailure
        assertTrue(failure.errorType is NetworkErrorType.NoConnectivity)
    }

    @Test
    fun jsonSyntaxExceptionMapsToMalformedResponse() = runTest {
        val result = safeApiCall<SamplePayload>(gson) {
            throw JsonSyntaxException("Malformed JSON string at line 1 column 5")
        }

        assertTrue(result is ApiResult.NetworkFailure)
        val failure = result as ApiResult.NetworkFailure
        assertTrue(failure.errorType is NetworkErrorType.MalformedResponse)
    }

    @Test
    fun simulatedHttpAdapterAllowsInjectingArbitraryOutcomes() = runTest {
        val adapter = SimulatedHttpAdapter<SamplePayload>()

        // 1. Simulación de fallo de red
        adapter.simulateNoConnectivity("Modo avión activado")
        val offlineResult = adapter.execute()
        assertTrue(offlineResult is ApiResult.NetworkFailure)

        // 2. Simulación de timeout
        adapter.simulateNetworkTimeout()
        val timeoutResult = adapter.execute()
        assertTrue(timeoutResult is ApiResult.NetworkFailure)

        // 3. Simulación de éxito
        val mockData = SamplePayload("999", "Simulado")
        adapter.simulateSuccess(mockData)
        val successResult = adapter.execute()
        assertTrue(successResult is ApiResult.Success)
        assertEquals("Simulado", (successResult as ApiResult.Success).value.name)
    }
}
