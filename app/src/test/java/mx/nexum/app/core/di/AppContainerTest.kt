
package mx.nexum.app.core.di

import mx.nexum.app.auth.data.AuthError
import mx.nexum.app.auth.data.AuthErrorKind
import mx.nexum.app.auth.data.AuthRepository
import mx.nexum.app.auth.data.AuthResult
import mx.nexum.app.auth.data.LoginOutcome
import mx.nexum.app.auth.data.RegisterOutcome
import mx.nexum.app.auth.data.RetrofitAuthApi
import mx.nexum.app.auth.data.Session
import mx.nexum.app.auth.data.TokenStorage
import mx.nexum.app.auth.data.dto.LoginRequest
import mx.nexum.app.auth.data.dto.RegisterRequest
import mx.nexum.app.auth.data.dto.User
import mx.nexum.app.core.cache.OfflineCacheManager
import mx.nexum.app.core.session.CredentialStorage
import mx.nexum.app.core.session.InMemoryCredentialStorage
import mx.nexum.app.core.session.SessionManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppContainerTest {

    private class FakeTokenStorage : TokenStorage {
        var savedSession: Session? = null
        var clearCalled = false

        override fun save(session: Session) {
            savedSession = session
        }

        override fun current(): Session? = savedSession

        override fun clear() {
            clearCalled = true
            savedSession = null
        }
    }

    private class FakeAuthRepository : AuthRepository {
        var loginAttempts = 0

        override suspend fun register(request: RegisterRequest): AuthResult<RegisterOutcome> =
            AuthResult.Failure(AuthError(AuthErrorKind.UNKNOWN, "fake"))

        override suspend fun login(request: LoginRequest): AuthResult<LoginOutcome> {
            loginAttempts++
            return AuthResult.Failure(AuthError(AuthErrorKind.INVALID_CREDENTIALS, "fake creds"))
        }
    }

    private class TestContainer(
        override val tokenStorage: TokenStorage = FakeTokenStorage(),
        override val authRepository: AuthRepository = FakeAuthRepository(),
        override val credentialStorage: CredentialStorage = InMemoryCredentialStorage(),
        override val sessionManager: SessionManager = SessionManager(credentialStorage, RetrofitAuthApi()),
        override val cacheManager: OfflineCacheManager = OfflineCacheManager()
    ) : AppContainer

    @Test
    fun appContainerAllowsInjectingTestDoubles() {
        val fakeStorage = FakeTokenStorage()
        val fakeRepo = FakeAuthRepository()
        val container: AppContainer = TestContainer(
            tokenStorage = fakeStorage,
            authRepository = fakeRepo
        )

        assertNotNull(container.tokenStorage)
        assertNotNull(container.authRepository)
        assertNotNull(container.cacheManager)

        val testSession = Session(
            accessToken = "token_abc",
            refreshToken = "refresh_xyz",
            expiresIn = 3600L,
            user = User(id = "user_1", name = "Test User", email = "test@example.com")
        )

        container.tokenStorage.save(testSession)
        assertEquals(testSession, container.tokenStorage.current())

        container.tokenStorage.clear()
        assertTrue(fakeStorage.clearCalled)
        assertNull(container.tokenStorage.current())
    }

    @Test
    fun defaultAppContainerInitializesLazySingletons() {
        val defaultContainer = DefaultAppContainer()
        assertNotNull(defaultContainer.tokenStorage)
        assertNotNull(defaultContainer.authRepository)
        assertNotNull(defaultContainer.cacheManager)

        val storage1 = defaultContainer.tokenStorage
        val storage2 = defaultContainer.tokenStorage
        assertEquals(storage1, storage2)

        val cache1 = defaultContainer.cacheManager
        val cache2 = defaultContainer.cacheManager
        assertEquals(cache1, cache2)
    }
}
