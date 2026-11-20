package mx.nexum.app.auth

import com.google.gson.Gson
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import mx.nexum.app.auth.data.AuthApi
import mx.nexum.app.auth.data.AuthApiException
import mx.nexum.app.auth.data.AuthErrorKind
import mx.nexum.app.auth.data.AuthRepositoryImpl
import mx.nexum.app.auth.data.AuthResult
import mx.nexum.app.auth.data.InMemoryTokenStorage
import mx.nexum.app.auth.data.Session
import mx.nexum.app.auth.data.dto.LoginRequest
import mx.nexum.app.auth.data.dto.LoginResponse
import mx.nexum.app.auth.data.dto.RefreshRequest
import mx.nexum.app.auth.data.dto.RefreshResponse
import mx.nexum.app.auth.data.dto.RegisterRequest
import mx.nexum.app.auth.data.dto.RegisterResponse
import mx.nexum.app.auth.data.dto.SessionIdDto
import mx.nexum.app.auth.data.dto.User
import mx.nexum.app.auth.ui.AuthEffect
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryRefreshTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun sampleSession(
        accessToken: String = "old-access",
        refreshToken: String = "old-refresh",
        expiresIn: Long = 3600L,
        sessionId: String = "session-123"
    ) = Session(
        accessToken = accessToken,
        refreshToken = refreshToken,
        expiresIn = expiresIn,
        user = User("u1", "Usuario", "usuario@nexum.test", true),
        sessionId = sessionId
    )

    @Test
    fun refreshSinSesionActivaFallaSinLlamarALaRed() = runTest {
        val storage = InMemoryTokenStorage()
        var apiCalled = false
        val fakeApi = object : AuthApi {
            override suspend fun register(request: RegisterRequest): RegisterResponse = error("no")
            override suspend fun login(request: LoginRequest): LoginResponse = error("no")
            override suspend fun refresh(request: RefreshRequest): RefreshResponse {
                apiCalled = true
                error("No debería llamarse")
            }
            override suspend fun logout(accessToken: String) { /* no-op */ }
        }
        val repository = AuthRepositoryImpl(fakeApi, storage)
        val result = repository.refreshSession()
        assertTrue(result is AuthResult.Failure)
        assertFalse(apiCalled)
        assertEquals(AuthErrorKind.INVALID_CREDENTIALS, (result as AuthResult.Failure).error.kind)
        assertFalse(result.error.retryable)
    }

    @Test
    fun refreshExitosoActualizaTokenStorageDeFormaAtomica() = runTest {
        val storage = InMemoryTokenStorage()
        storage.save(sampleSession())
        var apiCalled = false
        val fakeApi = object : AuthApi {
            override suspend fun register(request: RegisterRequest): RegisterResponse = error("no")
            override suspend fun login(request: LoginRequest): LoginResponse = error("no")
            override suspend fun refresh(request: RefreshRequest): RefreshResponse {
                apiCalled = true
                assertEquals("old-refresh", request.refreshToken)
                assertEquals("session-123", request.deviceId)
                return RefreshResponse(
                    accessToken = "new-access-jwt",
                    refreshToken = "new-refresh-token",
                    tokenType = "Bearer",
                    expiresIn = 900L,
                    sessionId = "session-456",
                    session = SessionIdDto("session-456")
                )
            }
            override suspend fun logout(accessToken: String) { /* no-op */ }
        }
        val repository = AuthRepositoryImpl(fakeApi, storage)
        val result = repository.refreshSession()
        assertTrue(result is AuthResult.Success)
        val outcome = (result as AuthResult.Success).data
        assertEquals("new-access-jwt", outcome.session.accessToken)
        assertEquals("new-refresh-token", outcome.session.refreshToken)
        assertEquals("session-456", outcome.session.sessionId)
        // Verify TokenStorage updated
        val updated = storage.current()
        assertNotNull(updated)
        assertEquals("new-access-jwt", updated?.accessToken)
        assertEquals("new-refresh-token", updated?.refreshToken)
        assertEquals(900L, updated?.expiresIn)
        assertEquals("session-456", updated?.sessionId)
        assertTrue(apiCalled)
    }

    @Test
    fun refreshCon401PurgaTokenStorageYEmiteNavegacionALogin() = runTest {
        val storage = InMemoryTokenStorage()
        storage.save(sampleSession())
        var apiCalled = false
        val fakeApi = object : AuthApi {
            override suspend fun register(request: RegisterRequest): RegisterResponse = error("no")
            override suspend fun login(request: LoginRequest): LoginResponse = error("no")
            override suspend fun refresh(request: RefreshRequest): RefreshResponse {
                apiCalled = true
                throw AuthApiException(
                    kind = AuthErrorKind.INVALID_CREDENTIALS,
                    message = "Sesión expirada o token revocado (401)",
                    statusCode = 401
                )
            }
            override suspend fun logout(accessToken: String) { /* no-op */ }
        }
        val repository = AuthRepositoryImpl(fakeApi, storage)
        val emittedEffects = mutableListOf<AuthEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.sessionEffects.collect { emittedEffects.add(it) }
        }
        val result = repository.refreshSession()
        assertTrue(result is AuthResult.Failure)
        assertEquals(AuthErrorKind.INVALID_CREDENTIALS, (result as AuthResult.Failure).error.kind)
        assertFalse(result.error.retryable)
        // TokenStorage should be cleared
        assertNull(storage.current())
        // Effect emitted
        assertTrue(emittedEffects.contains(AuthEffect.NavigateToLogin))
        assertTrue(apiCalled)
    }

    @Test
    fun refreshCon409ConflictoReplayPurgaTokenStorageYEmiteNavegacionALogin() = runTest {
        val storage = InMemoryTokenStorage()
        storage.save(sampleSession())
        var apiCalled = false
        val fakeApi = object : AuthApi {
            override suspend fun register(request: RegisterRequest): RegisterResponse = error("no")
            override suspend fun login(request: LoginRequest): LoginResponse = error("no")
            override suspend fun refresh(request: RefreshRequest): RefreshResponse {
                apiCalled = true
                throw AuthApiException(
                    kind = AuthErrorKind.CONFLICT,
                    message = "Token ya consumido (409 Replay)",
                    statusCode = 409
                )
            }
            override suspend fun logout(accessToken: String) { /* no-op */ }
        }
        val repository = AuthRepositoryImpl(fakeApi, storage)
        val emittedEffects = mutableListOf<AuthEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.sessionEffects.collect { emittedEffects.add(it) }
        }
        val result = repository.refreshSession()
        assertTrue(result is AuthResult.Failure)
        assertEquals(AuthErrorKind.INVALID_CREDENTIALS, (result as AuthResult.Failure).error.kind)
        assertFalse(result.error.retryable)
        // TokenStorage should be cleared
        assertNull(storage.current())
        assertTrue(emittedEffects.contains(AuthEffect.NavigateToLogin))
        assertTrue(apiCalled)
    }

    @Test
    fun refreshCon503NoPurgaSesionYRetornaReintentable() = runTest {
        val storage = InMemoryTokenStorage()
        val originalSession = sampleSession()
        storage.save(originalSession)
        var apiCalled = false
        val fakeApi = object : AuthApi {
            override suspend fun register(request: RegisterRequest): RegisterResponse = error("no")
            override suspend fun login(request: LoginRequest): LoginResponse = error("no")
            override suspend fun refresh(request: RefreshRequest): RefreshResponse {
                apiCalled = true
                throw AuthApiException(
                    kind = AuthErrorKind.SERVER_UNAVAILABLE,
                    message = "Servicio no disponible (503)",
                    retryable = true,
                    statusCode = 503
                )
            }
            override suspend fun logout(accessToken: String) { /* no-op */ }
        }
        val repository = AuthRepositoryImpl(fakeApi, storage)
        val result = repository.refreshSession()
        assertTrue(result is AuthResult.Failure)
        val error = (result as AuthResult.Failure).error
        assertEquals(AuthErrorKind.SERVER_UNAVAILABLE, error.kind)
        assertTrue(error.retryable)
        // Session remains intact
        assertEquals(originalSession, storage.current())
        assertTrue(apiCalled)
    }

    @Test
    fun refreshConErrorDeRedNoPurgaSesionYRetornaReintentable() = runTest {
        val storage = InMemoryTokenStorage()
        val originalSession = sampleSession()
        storage.save(originalSession)
        var apiCalled = false
        val fakeApi = object : AuthApi {
            override suspend fun register(request: RegisterRequest): RegisterResponse = error("no")
            override suspend fun login(request: LoginRequest): LoginResponse = error("no")
            override suspend fun refresh(request: RefreshRequest): RefreshResponse {
                apiCalled = true
                throw AuthApiException(
                    kind = AuthErrorKind.NETWORK,
                    message = "Fallo de conexión",
                    retryable = true
                )
            }
            override suspend fun logout(accessToken: String) { /* no-op */ }
        }
        val repository = AuthRepositoryImpl(fakeApi, storage)
        val result = repository.refreshSession()
        assertTrue(result is AuthResult.Failure)
        val error = (result as AuthResult.Failure).error
        assertEquals(AuthErrorKind.NETWORK, error.kind)
        assertTrue(error.retryable)
        // Session remains intact
        assertEquals(originalSession, storage.current())
        assertTrue(apiCalled)
    }

    @Test
    fun concurrenciaSingleFlightEjecutaUnaSolaPeticionHTTPParaMultiplesCorrutinas() = runTest {
        val storage = InMemoryTokenStorage()
        storage.save(sampleSession())
        val apiCallCount = AtomicInteger(0)
        val refreshRelease = CompletableDeferred<Unit>()
        val fakeApi = object : AuthApi {
            override suspend fun register(request: RegisterRequest): RegisterResponse = error("no")
            override suspend fun login(request: LoginRequest): LoginResponse = error("no")
            override suspend fun refresh(request: RefreshRequest): RefreshResponse {
                apiCallCount.incrementAndGet()
                refreshRelease.await()
                return RefreshResponse(
                    accessToken = "shared-single-flight-access",
                    refreshToken = "shared-single-flight-refresh",
                    tokenType = "Bearer",
                    expiresIn = 900L,
                    sessionId = null,
                    session = null
                )
            }
            override suspend fun logout(accessToken: String) { /* no-op */ }
        }
        val repository = AuthRepositoryImpl(fakeApi, storage)
        // Launch 5 concurrent refreshes
        val deferredResults = (1..5).map { async(testDispatcher) { repository.refreshSession() } }
        testScheduler.advanceUntilIdle()
        // Release the single in-flight request
        refreshRelease.complete(Unit)
        testScheduler.advanceUntilIdle()
        val results = deferredResults.awaitAll()
        // Verify only one HTTP call
        assertEquals(1, apiCallCount.get())
        // All results should be success with same token
        assertEquals(5, results.size)
        results.forEach { result ->
            assertTrue(result is AuthResult.Success)
            val outcome = (result as AuthResult.Success).data
            assertEquals("shared-single-flight-access", outcome.session.accessToken)
            assertEquals("shared-single-flight-refresh", outcome.session.refreshToken)
        }
        // Final storage should hold the new token
        assertEquals("shared-single-flight-access", storage.current()?.accessToken)
    }

    @Test
    fun serializacionDualSoportaSessionIdPlanoYObjetoSessionAnidado() {
        val gson = Gson()
        // Format A: plain sessionId string
        val jsonPlano = """
            {
                "accessToken": "jwt_a",
                "refreshToken": "refresh_a",
                "tokenType": "Bearer",
                "expiresIn": 900,
                "sessionId": "session-flat-uuid"
            }
        """.trimIndent()
        val respA = gson.fromJson(jsonPlano, RefreshResponse::class.java)
        assertEquals("session-flat-uuid", respA.resolvedSessionId)
        // Format B: nested session object
        val jsonAnidado = """
            {
                "accessToken": "jwt_b",
                "refreshToken": "refresh_b",
                "tokenType": "Bearer",
                "expiresIn": 900,
                "session": { "id": "session-nested-uuid" }
            }
        """.trimIndent()
        val respB = gson.fromJson(jsonAnidado, RefreshResponse::class.java)
        assertEquals("session-nested-uuid", respB.resolvedSessionId)
        // Format C: both present
        val jsonDual = """
            {
                "accessToken": "jwt_c",
                "refreshToken": "refresh_c",
                "tokenType": "Bearer",
                "expiresIn": 900,
                "sessionId": "primary-uuid",
                "session": { "id": "nested-uuid" }
            }
        """.trimIndent()
        val respC = gson.fromJson(jsonDual, RefreshResponse::class.java)
        assertEquals("primary-uuid", respC.resolvedSessionId)
    }
}
