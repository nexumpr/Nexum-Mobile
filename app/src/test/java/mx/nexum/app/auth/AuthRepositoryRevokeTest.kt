package mx.nexum.app.auth

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
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
import mx.nexum.app.auth.data.dto.User
import mx.nexum.app.auth.ui.AuthEffect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryRevokeTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var tokenStorage: InMemoryTokenStorage

    private class FakeAuthApi : AuthApi {
        var lastAccessToken: String? = null
        var lastSessionId: String? = null
        var exceptionToThrow: Throwable? = null
        var revokeCalls = 0

        override suspend fun register(request: RegisterRequest): RegisterResponse = throw NotImplementedError()
        override suspend fun login(request: LoginRequest): LoginResponse = throw NotImplementedError()
        override suspend fun refresh(request: RefreshRequest): RefreshResponse = throw NotImplementedError()
        override suspend fun logout(accessToken: String) = throw NotImplementedError()

        override suspend fun revokeSession(accessToken: String, sessionId: String) {
            revokeCalls++
            lastAccessToken = accessToken
            lastSessionId = sessionId
            exceptionToThrow?.let { throw it }
        }
    }

    private lateinit var fakeApi: FakeAuthApi
    private lateinit var repository: AuthRepositoryImpl

    @Before
    fun setUp() {
        tokenStorage = InMemoryTokenStorage()
        fakeApi = FakeAuthApi()
        repository = AuthRepositoryImpl(fakeApi, tokenStorage)
    }

    @Test
    fun revokeSessionSinSesionActivaRetornaError401() = runTest(testDispatcher) {
        val result = repository.revokeSession(UUID.randomUUID().toString(), isCurrent = false)
        assertTrue(result is AuthResult.Failure)
        val failure = result as AuthResult.Failure
        assertEquals(AuthErrorKind.INVALID_CREDENTIALS, failure.error.kind)
        assertEquals(401, failure.error.statusCode)
    }

    @Test
    fun revokeSessionConIdInvalidoNoUUIDRetornaError400Validacion() = runTest(testDispatcher) {
        tokenStorage.save(
            Session(
                accessToken = "valid_token",
                refreshToken = "refresh_token",
                expiresIn = 3600,
                user = User(id = "user_1", email = "test@nexum.app", name = "Test User")
            )
        )

        val result = repository.revokeSession("id-no-valido-123", isCurrent = false)
        assertTrue(result is AuthResult.Failure)
        val failure = result as AuthResult.Failure
        assertEquals(AuthErrorKind.VALIDATION, failure.error.kind)
        assertEquals(400, failure.error.statusCode)
        assertEquals(0, fakeApi.revokeCalls)
    }

    @Test
    fun revokeSessionRemotaExitosaConservaSesionLocalYNoEmiteNavigateToLogin() = runTest(testDispatcher) {
        tokenStorage.save(
            Session(
                accessToken = "valid_token",
                refreshToken = "refresh_token",
                expiresIn = 3600,
                user = User(id = "user_1", email = "test@nexum.app", name = "Test User")
            )
        )

        val targetSessionId = UUID.randomUUID().toString()
        val emittedEffects = mutableListOf<AuthEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.sessionEffects.collect { emittedEffects.add(it) }
        }

        val result = repository.revokeSession(targetSessionId, isCurrent = false)
        advanceUntilIdle()

        assertTrue(result is AuthResult.Success)
        assertEquals(1, fakeApi.revokeCalls)
        assertEquals("valid_token", fakeApi.lastAccessToken)
        assertEquals(targetSessionId, fakeApi.lastSessionId)
        assertNotNull(tokenStorage.current())
        assertFalse(emittedEffects.contains(AuthEffect.NavigateToLogin))
    }

    @Test
    fun revokeSessionActualExitosaPurgaTokenStorageYEmiteNavigateToLogin() = runTest(testDispatcher) {
        tokenStorage.save(
            Session(
                accessToken = "valid_token",
                refreshToken = "refresh_token",
                expiresIn = 3600,
                user = User(id = "user_1", email = "test@nexum.app", name = "Test User")
            )
        )

        val currentSessionId = UUID.randomUUID().toString()
        val emittedEffects = mutableListOf<AuthEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.sessionEffects.collect { emittedEffects.add(it) }
        }

        val result = repository.revokeSession(currentSessionId, isCurrent = true)
        advanceUntilIdle()

        assertTrue(result is AuthResult.Success)
        assertEquals(1, fakeApi.revokeCalls)
        assertEquals(currentSessionId, fakeApi.lastSessionId)
        assertNull(tokenStorage.current())
        assertTrue(emittedEffects.contains(AuthEffect.NavigateToLogin))
    }

    @Test
    fun revokeSessionConError404MapeaANotFound() = runTest(testDispatcher) {
        tokenStorage.save(
            Session(
                accessToken = "valid_token",
                refreshToken = "refresh_token",
                expiresIn = 3600,
                user = User(id = "user_1", email = "test@nexum.app", name = "Test User")
            )
        )

        fakeApi.exceptionToThrow = AuthApiException(
            kind = AuthErrorKind.NOT_FOUND,
            message = "La sesión no existe o ya expiró",
            statusCode = 404
        )

        val result = repository.revokeSession(UUID.randomUUID().toString(), isCurrent = false)
        assertTrue(result is AuthResult.Failure)
        val failure = result as AuthResult.Failure
        assertEquals(AuthErrorKind.NOT_FOUND, failure.error.kind)
        assertEquals(404, failure.error.statusCode)
        assertNotNull(tokenStorage.current())
    }

    @Test
    fun revokeSessionConError409MapeaAConflict() = runTest(testDispatcher) {
        tokenStorage.save(
            Session(
                accessToken = "valid_token",
                refreshToken = "refresh_token",
                expiresIn = 3600,
                user = User(id = "user_1", email = "test@nexum.app", name = "Test User")
            )
        )

        fakeApi.exceptionToThrow = AuthApiException(
            kind = AuthErrorKind.CONFLICT,
            message = "Conflicto concurrente al cerrar la sesión",
            statusCode = 409
        )

        val result = repository.revokeSession(UUID.randomUUID().toString(), isCurrent = false)
        assertTrue(result is AuthResult.Failure)
        val failure = result as AuthResult.Failure
        assertEquals(AuthErrorKind.CONFLICT, failure.error.kind)
        assertEquals(409, failure.error.statusCode)
        assertNotNull(tokenStorage.current())
    }
}
