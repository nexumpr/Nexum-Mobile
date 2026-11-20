package mx.nexum.app.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import mx.nexum.app.auth.data.dto.User
import mx.nexum.app.auth.ui.AuthEffect
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryLogoutTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun sampleSession() = Session(
        accessToken = "test-access-token",
        refreshToken = "test-refresh-token",
        expiresIn = 3600L,
        user = User("u1", "Usuario", "usuario@nexum.test", true),
        sessionId = "session-123"
    )

    @Test
    fun logoutSinSesionActivaLimpiaStorageYRetornaExito() = runTest {
        val storage = InMemoryTokenStorage()
        var apiCalled = false
        val fakeApi = object : AuthApi {
            override suspend fun register(request: RegisterRequest): RegisterResponse = error("no")
            override suspend fun login(request: LoginRequest): LoginResponse = error("no")
            override suspend fun refresh(request: RefreshRequest): RefreshResponse = error("no")
            override suspend fun logout(accessToken: String) {
                apiCalled = true
            }
        }

        val repository = AuthRepositoryImpl(fakeApi, storage)
        val emittedEffects = mutableListOf<AuthEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.sessionEffects.collect { emittedEffects.add(it) }
        }

        val result = repository.logout()

        assertTrue(result is AuthResult.Success)
        assertFalse(apiCalled)
        assertNull(storage.current())
        assertTrue(emittedEffects.contains(AuthEffect.NavigateToLogin))
    }

    @Test
    fun logoutExitosoLlamaApiPurgaStorageYEmiteNavegacion() = runTest {
        val storage = InMemoryTokenStorage()
        storage.save(sampleSession())

        var loggedOutToken: String? = null
        val fakeApi = object : AuthApi {
            override suspend fun register(request: RegisterRequest): RegisterResponse = error("no")
            override suspend fun login(request: LoginRequest): LoginResponse = error("no")
            override suspend fun refresh(request: RefreshRequest): RefreshResponse = error("no")
            override suspend fun logout(accessToken: String) {
                loggedOutToken = accessToken
            }
        }

        val repository = AuthRepositoryImpl(fakeApi, storage)
        val emittedEffects = mutableListOf<AuthEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.sessionEffects.collect { emittedEffects.add(it) }
        }

        val result = repository.logout()

        assertTrue(result is AuthResult.Success)
        assertEquals("test-access-token", loggedOutToken)
        assertNull(storage.current())
        assertTrue(emittedEffects.contains(AuthEffect.NavigateToLogin))
    }

    @Test
    fun logoutConFalloDeRedPurgaStorageIncondicionalmenteYRetornaExito() = runTest {
        val storage = InMemoryTokenStorage()
        storage.save(sampleSession())

        val fakeApi = object : AuthApi {
            override suspend fun register(request: RegisterRequest): RegisterResponse = error("no")
            override suspend fun login(request: LoginRequest): LoginResponse = error("no")
            override suspend fun refresh(request: RefreshRequest): RefreshResponse = error("no")
            override suspend fun logout(accessToken: String) {
                throw IOException("Sin conexión a Internet")
            }
        }

        val repository = AuthRepositoryImpl(fakeApi, storage)
        val emittedEffects = mutableListOf<AuthEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.sessionEffects.collect { emittedEffects.add(it) }
        }

        val result = repository.logout()

        // Fail-secure: la falla de transporte no impide cerrar sesión localmente
        assertTrue(result is AuthResult.Success)
        assertNull(storage.current())
        assertTrue(emittedEffects.contains(AuthEffect.NavigateToLogin))
    }

    @Test
    fun logoutCon401SesionYaRevocadaPurgaStorageYRetornaExito() = runTest {
        val storage = InMemoryTokenStorage()
        storage.save(sampleSession())

        val fakeApi = object : AuthApi {
            override suspend fun register(request: RegisterRequest): RegisterResponse = error("no")
            override suspend fun login(request: LoginRequest): LoginResponse = error("no")
            override suspend fun refresh(request: RefreshRequest): RefreshResponse = error("no")
            override suspend fun logout(accessToken: String) {
                throw AuthApiException(
                    kind = AuthErrorKind.INVALID_CREDENTIALS,
                    message = "Sesión ya revocada en servidor (401)",
                    statusCode = 401
                )
            }
        }

        val repository = AuthRepositoryImpl(fakeApi, storage)
        val emittedEffects = mutableListOf<AuthEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.sessionEffects.collect { emittedEffects.add(it) }
        }

        val result = repository.logout()

        assertTrue(result is AuthResult.Success)
        assertNull(storage.current())
        assertTrue(emittedEffects.contains(AuthEffect.NavigateToLogin))
    }

    @Test
    fun logoutCon500ErrorDeServidorPurgaStorageYRetornaExito() = runTest {
        val storage = InMemoryTokenStorage()
        storage.save(sampleSession())

        val fakeApi = object : AuthApi {
            override suspend fun register(request: RegisterRequest): RegisterResponse = error("no")
            override suspend fun login(request: LoginRequest): LoginResponse = error("no")
            override suspend fun refresh(request: RefreshRequest): RefreshResponse = error("no")
            override suspend fun logout(accessToken: String) {
                throw AuthApiException(
                    kind = AuthErrorKind.SERVER_UNAVAILABLE,
                    message = "Internal Server Error (500)",
                    statusCode = 500
                )
            }
        }

        val repository = AuthRepositoryImpl(fakeApi, storage)
        val emittedEffects = mutableListOf<AuthEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.sessionEffects.collect { emittedEffects.add(it) }
        }

        val result = repository.logout()

        assertTrue(result is AuthResult.Success)
        assertNull(storage.current())
        assertTrue(emittedEffects.contains(AuthEffect.NavigateToLogin))
    }
}
