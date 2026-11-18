package mx.nexum.app.core.contract

import kotlinx.coroutines.test.runTest
import mx.nexum.app.core.cache.CacheResult
import mx.nexum.app.core.cache.InMemoryCacheStorage
import mx.nexum.app.core.cache.OfflineCacheManager
import mx.nexum.app.core.cache.SyncStatus
import mx.nexum.app.core.network.ApiResult
import mx.nexum.app.core.network.HttpErrorType
import mx.nexum.app.core.network.NetworkErrorType
import mx.nexum.app.core.network.safeApiCall
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

enum class ContractAction {
    COMPLETE,
    RETRY,
    REFRESH,
    FALLBACK_CACHE,
    ABORT
}

class RetryAndFallbackContractTest {
    private val gson = com.google.gson.Gson()

    @Test
    fun estadosDocumentadosGeneranLaAccionEsperada() = runTest {
        val cases = listOf(
            Triple(400, "error_400.json", ContractAction.ABORT),
            Triple(401, "error_401.json", ContractAction.REFRESH),
            Triple(403, "error_403.json", ContractAction.ABORT),
            Triple(404, "error_404.json", ContractAction.ABORT),
            Triple(409, "error_409.json", ContractAction.ABORT),
            Triple(422, "error_422.json", ContractAction.ABORT),
            Triple(429, "error_429.json", ContractAction.RETRY),
            Triple(500, "error_500.json", ContractAction.RETRY),
            Triple(503, "error_503.json", ContractAction.RETRY)
        )

        cases.forEach { (statusCode, fixture, expected) ->
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
            val api = simulator.retrofit().create(TechnicalContractApi::class.java)

            val result = safeApiCall(gson) { api.health() }

            assertEquals("Estado $statusCode", expected, decide(result))
        }
    }

    @Test
    fun limiteDePeticionesRespetaRetryAfter() = runTest {
        val simulator = FixtureHttpSimulator(
            mapOf(
                FixtureHttpSimulator.key("GET", "/api/v1/health") to FixtureScenario(
                    statusCode = 429,
                    fixture = "error_429.json",
                    headers = mapOf("Retry-After" to "30")
                )
            )
        )
        val api = simulator.retrofit().create(TechnicalContractApi::class.java)

        val result = safeApiCall(gson) { api.health() }

        assertTrue(result is ApiResult.HttpFailure)
        val type = (result as ApiResult.HttpFailure).errorType as HttpErrorType.RateLimited
        assertEquals(30, type.retryAfterSeconds)
        assertEquals("tr_synthetic_429", type.errorDetail?.traceId)
        assertEquals(ContractAction.RETRY, decide(result))
    }

    @Test
    fun timeoutSeReintentaYLuegoDegradaACacheStale() = runTest {
        val simulator = FixtureHttpSimulator(
            mapOf(
                FixtureHttpSimulator.key("GET", "/api/v1/health") to FixtureScenario(
                    statusCode = 200,
                    timeout = true
                )
            )
        )
        val api = simulator.retrofit().create(TechnicalContractApi::class.java)

        val result = safeApiCall(gson) { api.health() }

        assertTrue(result is ApiResult.NetworkFailure)
        assertTrue(result.errorTypeSafely() is NetworkErrorType.Timeout)
        assertEquals(ContractAction.RETRY, decide(result))

        val cache = OfflineCacheManager(InMemoryCacheStorage<Any>())
        val now = Instant.parse("2026-11-17T12:00:00Z")
        cache.put(
            userId = "usr_synthetic_120",
            key = "health",
            data = "synthetic-snapshot",
            ttlSeconds = 1,
            createdAt = now
        )

        val fallback = cache.get<String>("usr_synthetic_120", "health", now.plusSeconds(5))

        assertTrue(fallback is CacheResult.Hit)
        val hit = fallback as CacheResult.Hit<String>
        assertEquals(SyncStatus.STALE, hit.status)
        assertEquals("synthetic-snapshot", hit.data)
    }

    @Test
    fun sinConectividadUsaFallbackDeCacheSinConfirmar() = runTest {
        val simulator = FixtureHttpSimulator(emptyMap())
        val api = simulator.retrofit().create(TechnicalContractApi::class.java)

        val result = safeApiCall(gson) { api.health() }

        assertTrue(result is ApiResult.NetworkFailure)
        assertTrue(result.errorTypeSafely() is NetworkErrorType.NoConnectivity)
        assertEquals(ContractAction.FALLBACK_CACHE, decide(result))
    }

    @Test
    fun erroresNoTransitoriosSeAbortanSinReintento() = runTest {
        val sslSimulator = FixtureHttpSimulator(
            mapOf(
                FixtureHttpSimulator.key("GET", "/api/v1/health") to FixtureScenario(
                    statusCode = 200,
                    sslFailure = true
                )
            )
        )
        val sslResult = safeApiCall(gson) {
            sslSimulator.retrofit().create(TechnicalContractApi::class.java).health()
        }
        assertTrue(sslResult.errorTypeSafely() is NetworkErrorType.SslFailure)
        assertEquals(ContractAction.ABORT, decide(sslResult))

        val malformedSimulator = FixtureHttpSimulator(
            mapOf(
                FixtureHttpSimulator.key("GET", "/api/v1/health") to FixtureScenario(
                    statusCode = 200,
                    fixture = "malformed.json"
                )
            )
        )
        val malformedResult = safeApiCall(gson) {
            malformedSimulator.retrofit().create(TechnicalContractApi::class.java).health()
        }
        assertTrue(malformedResult.errorTypeSafely() is NetworkErrorType.MalformedResponse)
        assertEquals(ContractAction.ABORT, decide(malformedResult))
    }

    @Test
    fun idempotenciaDeColaReutilizaClaveYConservaReintentos() {
        val cache = OfflineCacheManager(InMemoryCacheStorage<Any>())

        val first = cache.enqueueOperation(
            userId = "usr_synthetic_120",
            actionType = "SYNC_CONTRACT",
            payload = mapOf("fixture" to "health_success.json"),
            idempotencyKey = "synthetic-key-120"
        )
        val second = cache.enqueueOperation(
            userId = "usr_synthetic_120",
            actionType = "SYNC_CONTRACT",
            payload = mapOf("fixture" to "error_503.json"),
            idempotencyKey = "synthetic-key-120"
        )

        assertEquals(first.idempotencyKey, second.idempotencyKey)
        assertEquals(1, cache.getPendingOperations("usr_synthetic_120").size)

        cache.recordOperationRetryFailure(
            userId = "usr_synthetic_120",
            idempotencyKey = "synthetic-key-120",
            error = "servicio sintético no disponible"
        )

        val pending = cache.getPendingOperations("usr_synthetic_120").single()
        assertEquals(1, pending.retryCount)
        assertEquals("servicio sintético no disponible", pending.lastError)
        assertEquals(SyncStatus.PENDING, pending.status)
    }

    private fun decide(result: ApiResult<*>): ContractAction = when (result) {
        is ApiResult.Success -> ContractAction.COMPLETE
        is ApiResult.HttpFailure -> when (result.errorType) {
            is HttpErrorType.BadRequest,
            is HttpErrorType.Forbidden,
            is HttpErrorType.NotFound,
            is HttpErrorType.Conflict,
            is HttpErrorType.UnprocessableEntity,
            is HttpErrorType.Other -> ContractAction.ABORT
            is HttpErrorType.Unauthorized -> ContractAction.REFRESH
            is HttpErrorType.RateLimited,
            is HttpErrorType.ServerError -> ContractAction.RETRY
        }
        is ApiResult.NetworkFailure -> when (result.errorType) {
            is NetworkErrorType.Timeout -> ContractAction.RETRY
            is NetworkErrorType.NoConnectivity -> ContractAction.FALLBACK_CACHE
            is NetworkErrorType.SslFailure,
            is NetworkErrorType.MalformedResponse,
            is NetworkErrorType.Unknown -> ContractAction.ABORT
        }
    }

    private fun ApiResult<*>.errorTypeSafely(): NetworkErrorType? =
        (this as? ApiResult.NetworkFailure)?.errorType
}
