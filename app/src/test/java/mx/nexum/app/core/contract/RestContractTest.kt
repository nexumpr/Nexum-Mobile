package mx.nexum.app.core.contract

import com.google.gson.Gson
import mx.nexum.app.auth.data.dto.ClientDto
import mx.nexum.app.auth.data.dto.IncomeDto
import mx.nexum.app.auth.data.dto.PrivacyDto
import mx.nexum.app.auth.data.dto.RegisterRequestDto
import mx.nexum.app.auth.data.network.AuthApiService
import mx.nexum.app.core.network.ApiErrorDetail
import mx.nexum.app.core.network.ApiResult
import mx.nexum.app.core.network.HttpErrorType
import mx.nexum.app.core.network.NetworkErrorType
import mx.nexum.app.core.network.RefreshResponse
import mx.nexum.app.core.network.RefreshTokenRequest
import mx.nexum.app.core.network.safeApiCall
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface TechnicalContractApi {
    @GET("health")
    suspend fun health(): Response<HealthDto>

    @GET("metadata")
    suspend fun metadata(): Response<MetadataDto>

    @POST("auth/refresh")
    suspend fun refresh(@Body request: RefreshTokenRequest): Response<RefreshResponse>
}

data class HealthDto(val status: String, val checkedAt: String)

data class MetadataDto(val version: String, val generatedAt: String)

class RestContractTest {
    private val gson = Gson()

    @Test
    fun registroEnvioRutaVersionadaYCabecerasObligatorias() {
        val simulator = FixtureHttpSimulator(
            mapOf(
                FixtureHttpSimulator.key("POST", "/api/v1/auth/register") to FixtureScenario(
                    statusCode = 201,
                    fixture = "register_success.json"
                )
            )
        )
        val api = simulator.retrofit().create(AuthApiService::class.java)
        val request = RegisterRequestDto(
            name = "Sintetico 120",
            email = "sintetico@contract.invalid",
            password = "synthetic-password",
            income = IncomeDto(amount = "0"),
            privacy = PrivacyDto(),
            client = ClientDto(locale = "es-MX", timeZone = "America/Mexico_City")
        )

        val result = runBlockingSafe { safeApiCall(gson) { api.register("synthetic-idempotency-120", request) } }

        assertTrue(result is ApiResult.Success)
        assertEquals(201, (result as ApiResult.Success).statusCode)
        val recorded = simulator.recorded.single()
        assertEquals("POST", recorded.method)
        assertEquals("/api/v1/auth/register", recorded.path)
        assertEquals("synthetic-idempotency-120", recorded.headers["Idempotency-Key"])
        assertTrue(recorded.headers["Accept"]?.contains("application/json") == true)
        assertTrue(recorded.contentType?.contains("application/json") == true)
    }

    @Test
    fun respuestaExitosaConservaFechaIsoYIgnoraCamposFuturos() {
        val simulator = FixtureHttpSimulator(
            mapOf(
                FixtureHttpSimulator.key("GET", "/api/v1/health") to FixtureScenario(
                    statusCode = 200,
                    fixture = "health_success.json"
                ),
                FixtureHttpSimulator.key("GET", "/api/v1/metadata") to FixtureScenario(
                    statusCode = 200,
                    fixture = "unknown_fields_success.json"
                )
            )
        )
        val api = technicalApi(simulator)

        val health = runBlockingSafe { safeApiCall(gson) { api.health() } }
        val metadata = runBlockingSafe { safeApiCall(gson) { api.metadata() } }

        assertTrue(health is ApiResult.Success)
        assertEquals("ok", (health as ApiResult.Success).value.status)
        assertEquals("2026-11-17T12:00:00Z", health.value.checkedAt)
        assertTrue(metadata is ApiResult.Success)
        assertEquals("v1", (metadata as ApiResult.Success).value.version)
        assertEquals("2026-11-17T12:00:00Z", metadata.value.generatedAt)
    }

    @Test
    fun renovacionDeSesionUsaContratoSintetico() {
        val simulator = FixtureHttpSimulator(
            mapOf(
                FixtureHttpSimulator.key("POST", "/api/v1/auth/refresh") to FixtureScenario(
                    statusCode = 200,
                    fixture = "session_refresh_success.json"
                )
            )
        )
        val api = technicalApi(simulator)

        val result = runBlockingSafe {
            safeApiCall(gson) {
                api.refresh(RefreshTokenRequest(refreshToken = "synthetic.refresh.current"))
            }
        }

        assertTrue(result is ApiResult.Success)
        val response = (result as ApiResult.Success).value
        assertEquals("synthetic.access.rotated", response.accessToken)
        assertEquals("synthetic.refresh.rotated", response.refreshToken)
        assertEquals(3600, response.expiresIn)
    }

    @Test
    fun sobreDeErrorCanonicoSeInterpretaParaCadaEstadoDocumentado() {
        val cases = listOf(
            400 to "error_400.json",
            401 to "error_401.json",
            403 to "error_403.json",
            404 to "error_404.json",
            409 to "error_409.json",
            422 to "error_422.json",
            429 to "error_429.json",
            500 to "error_500.json",
            503 to "error_503.json"
        )

        cases.forEach { (statusCode, fixture) ->
            val headers = if (statusCode == 429) mapOf("Retry-After" to "30") else emptyMap()
            val simulator = FixtureHttpSimulator(
                mapOf(
                    FixtureHttpSimulator.key("GET", "/api/v1/health") to FixtureScenario(
                        statusCode = statusCode,
                        fixture = fixture,
                        headers = headers
                    )
                )
            )
            val api = technicalApi(simulator)

            val result = runBlockingSafe { safeApiCall(gson) { api.health() } }

            assertTrue("Estado $statusCode debería ser HttpFailure", result is ApiResult.HttpFailure)
            val failure = result as ApiResult.HttpFailure
            assertEquals(statusCode, failure.statusCode)
            assertTipoEsperado(statusCode, failure.errorType)

            when (val type = failure.errorType) {
                is HttpErrorType.RateLimited -> assertEquals(30, type.retryAfterSeconds)
                is HttpErrorType.ServerError -> assertTrue(type.message.contains("sintético"))
                else -> Unit
            }

            errorDetail(failure.errorType)?.let { detail: ApiErrorDetail ->
                assertTrue(detail.code.isNotBlank())
                assertTrue(detail.message.isNotBlank())
                assertEquals("tr_synthetic_$statusCode", detail.traceId)
                assertNotNull(detail.details)
            }
        }
    }

    @Test
    fun jsonMalformadoSeClasificaComoRespuestaInvalida() {
        val simulator = FixtureHttpSimulator(
            mapOf(
                FixtureHttpSimulator.key("GET", "/api/v1/health") to FixtureScenario(
                    statusCode = 200,
                    fixture = "malformed.json"
                )
            )
        )
        val api = technicalApi(simulator)

        val result = runBlockingSafe { safeApiCall(gson) { api.health() } }

        assertTrue(result is ApiResult.NetworkFailure)
        assertTrue((result as ApiResult.NetworkFailure).errorType is NetworkErrorType.MalformedResponse)
    }

    @Test
    fun escenarioNoDisponibleFallaCerradoSinFiltrarSecretos() {
        val simulator = FixtureHttpSimulator(emptyMap())
        val api = technicalApi(simulator)

        val result = runBlockingSafe { safeApiCall(gson) { api.health() } }

        assertTrue(result is ApiResult.NetworkFailure)
        val failure = result as ApiResult.NetworkFailure
        assertTrue(failure.errorType is NetworkErrorType.NoConnectivity)
        val message = (failure.errorType as NetworkErrorType.NoConnectivity).message
        assertTrue(message.contains("Escenario sintético no disponible"))
        assertFalse(message.contains("Bearer"))
        assertFalse(message.contains("synthetic.access"))
    }

    private fun technicalApi(simulator: FixtureHttpSimulator): TechnicalContractApi =
        simulator.retrofit().create(TechnicalContractApi::class.java)

    private fun errorDetail(type: HttpErrorType): ApiErrorDetail? = when (type) {
        is HttpErrorType.BadRequest -> type.errorDetail
        is HttpErrorType.Unauthorized -> type.errorDetail
        is HttpErrorType.Forbidden -> type.errorDetail
        is HttpErrorType.NotFound -> type.errorDetail
        is HttpErrorType.Conflict -> type.errorDetail
        is HttpErrorType.UnprocessableEntity -> type.errorDetail
        is HttpErrorType.RateLimited -> type.errorDetail
        is HttpErrorType.ServerError -> null
        is HttpErrorType.Other -> null
    }

    private fun assertTipoEsperado(statusCode: Int, type: HttpErrorType) {
        val expected = when (statusCode) {
            400 -> HttpErrorType.BadRequest::class
            401 -> HttpErrorType.Unauthorized::class
            403 -> HttpErrorType.Forbidden::class
            404 -> HttpErrorType.NotFound::class
            409 -> HttpErrorType.Conflict::class
            422 -> HttpErrorType.UnprocessableEntity::class
            429 -> HttpErrorType.RateLimited::class
            500, 503 -> HttpErrorType.ServerError::class
            else -> HttpErrorType.Other::class
        }
        assertEquals(expected, type::class)
    }

    private fun <T> runBlockingSafe(block: suspend () -> T): T =
        kotlinx.coroutines.runBlocking { block() }
}
