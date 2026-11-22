package mx.nexum.app.auth

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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
import mx.nexum.app.auth.data.dto.SessionItemResponse
import mx.nexum.app.auth.data.dto.SessionsPageResponse
import mx.nexum.app.auth.data.dto.User
import mx.nexum.app.auth.ui.AuthEffect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositorySessionsTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var tokenStorage: InMemoryTokenStorage

    private class FakeAuthApi : AuthApi {
        var sessionsResponse: SessionsPageResponse = SessionsPageResponse(
            items = listOf(
                SessionItemResponse(
                    id = "sess_other_1",
                    createdAt = "2026-11-15T08:30:00Z",
                    lastSeenAt = "2026-11-18T14:20:00Z",
                    current = false,
                    revoked = false
                ),
                SessionItemResponse(
                    id = "sess_current_2",
                    createdAt = "2026-11-19T10:00:00Z",
                    lastSeenAt = "2026-11-19T20:00:00Z",
                    current = true,
                    revoked = false
                )
            ),
            nextCursor = "next_cursor_xyz"
        )
        var exceptionToThrow: Throwable? = null
        var lastAccessToken: String? = null
        var lastLimit: Int? = null
        var lastCursor: String? = null

        override suspend fun register(request: RegisterRequest): RegisterResponse = throw NotImplementedError()
        override suspend fun login(request: LoginRequest): LoginResponse = throw NotImplementedError()
        override suspend fun refresh(request: RefreshRequest): RefreshResponse = throw NotImplementedError()
        override suspend fun logout(accessToken: String) = throw NotImplementedError()

        override suspend fun getSessions(accessToken: String, limit: Int?, cursor: String?): SessionsPageResponse {
            lastAccessToken = accessToken
            lastLimit = limit
            lastCursor = cursor
            exceptionToThrow?.let { throw it }
            return sessionsResponse
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
    fun getSessionsSinSesionActivaRetornaError401() = runTest(testDispatcher) {
        val result = repository.getSessions(limit = 25, cursor = null)
        assertTrue(result is AuthResult.Failure)
        val failure = result as AuthResult.Failure
        assertEquals(AuthErrorKind.INVALID_CREDENTIALS, failure.error.kind)
        assertEquals(401, failure.error.statusCode)
    }

    @Test
    fun getSessionsExitosoOrdenaPrimeraPaginaConSesionActualAlInicio() = runTest(testDispatcher) {
        tokenStorage.save(
            Session(
                accessToken = "valid_token",
                refreshToken = "refresh_token",
                expiresIn = 3600,
                user = User(id = "user_1", email = "test@nexum.app", name = "Test User")
            )
        )

        val result = repository.getSessions(limit = 25, cursor = null)
        assertTrue(result is AuthResult.Success)
        val data = (result as AuthResult.Success).data

        assertEquals(2, data.items.size)
        // La sesión actual debe encabezar la lista en la página 1
        assertTrue(data.items[0].isCurrent)
        assertEquals("sess_current_2", data.items[0].id)
        assertEquals("Sesión #sess_cur", data.items[0].shortId)
        assertFalse(data.items[1].isCurrent)
        assertEquals("sess_other_1", data.items[1].id)
        assertEquals("next_cursor_xyz", data.nextCursor)
    }

    @Test
    fun getSessionsConCursorDeSegundaPaginaNoReordena() = runTest(testDispatcher) {
        tokenStorage.save(
            Session(
                accessToken = "valid_token",
                refreshToken = "refresh_token",
                expiresIn = 3600,
                user = User(id = "user_1", email = "test@nexum.app", name = "Test User")
            )
        )

        fakeApi.sessionsResponse = SessionsPageResponse(
            items = listOf(
                SessionItemResponse(
                    id = "sess_p2_1",
                    createdAt = "2026-11-01T12:00:00Z",
                    lastSeenAt = "2026-11-05T09:15:00Z",
                    current = false,
                    revoked = true
                ),
                SessionItemResponse(
                    id = "sess_p2_2",
                    createdAt = "2026-10-20T16:45:00Z",
                    lastSeenAt = "2026-10-25T11:00:00Z",
                    current = false,
                    revoked = false
                )
            ),
            nextCursor = null
        )

        val result = repository.getSessions(limit = 25, cursor = "next_cursor_xyz")
        assertTrue(result is AuthResult.Success)
        val data = (result as AuthResult.Success).data

        assertEquals(2, data.items.size)
        assertEquals("sess_p2_1", data.items[0].id)
        assertTrue(data.items[0].isRevoked)
        assertNull(data.nextCursor)
    }

    @Test
    fun getSessionsError401PurgaTokenStorageYEmiteNavigateToLogin() = runTest(testDispatcher) {
        tokenStorage.save(
            Session(
                accessToken = "expired_token",
                refreshToken = "refresh_token",
                expiresIn = 3600,
                user = User(id = "user_1", email = "test@nexum.app", name = "Test User")
            )
        )

        fakeApi.exceptionToThrow = AuthApiException(
            kind = AuthErrorKind.INVALID_CREDENTIALS,
            message = "Sesión inválida o expirada (401).",
            statusCode = 401
        )

        val emittedEffects = mutableListOf<AuthEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.sessionEffects.collect { emittedEffects.add(it) }
        }

        val result = repository.getSessions(limit = 25, cursor = null)
        advanceUntilIdle()

        assertTrue(result is AuthResult.Failure)
        assertNull(tokenStorage.current())
        assertTrue(emittedEffects.contains(AuthEffect.NavigateToLogin))
    }

    @Test
    fun getSessionsError400CursorInvalidoRetornaValidacion() = runTest(testDispatcher) {
        tokenStorage.save(
            Session(
                accessToken = "valid_token",
                refreshToken = "refresh_token",
                expiresIn = 3600,
                user = User(id = "user_1", email = "test@nexum.app", name = "Test User")
            )
        )

        fakeApi.exceptionToThrow = AuthApiException(
            kind = AuthErrorKind.VALIDATION,
            message = "Cursor de paginación inválido o expirado",
            statusCode = 400
        )

        val result = repository.getSessions(limit = 25, cursor = "invalid-cursor")
        assertTrue(result is AuthResult.Failure)
        val failure = result as AuthResult.Failure
        assertEquals(AuthErrorKind.VALIDATION, failure.error.kind)
        assertEquals(400, failure.error.statusCode)
    }
}
