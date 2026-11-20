package mx.nexum.app.auth

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import mx.nexum.app.auth.data.AuthApi
import mx.nexum.app.auth.data.AuthApiException
import mx.nexum.app.auth.data.AuthErrorKind
import mx.nexum.app.auth.data.AuthRepositoryImpl
import mx.nexum.app.auth.data.AuthResult
import mx.nexum.app.auth.data.InMemoryTokenStorage
import mx.nexum.app.auth.data.Session
import mx.nexum.app.auth.data.dto.ChangePasswordRequest
import mx.nexum.app.auth.data.dto.ChangePasswordResponse
import mx.nexum.app.auth.data.dto.LoginRequest
import mx.nexum.app.auth.data.dto.LoginResponse
import mx.nexum.app.auth.data.dto.RefreshRequest
import mx.nexum.app.auth.data.dto.RefreshResponse
import mx.nexum.app.auth.data.dto.RegisterRequest
import mx.nexum.app.auth.data.dto.RegisterResponse
import mx.nexum.app.auth.data.dto.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryChangePasswordTest {

    private fun sampleSession() = Session(
        accessToken = "test-access-token",
        refreshToken = "test-refresh-token",
        expiresIn = 3600L,
        user = User("u1", "Usuario", "usuario@nexum.test", true)
    )

    @Test
    fun changePasswordLlamaApiConAccessTokenYPurgaTokenStorageAlTenerExito() = runTest {
        val storage = InMemoryTokenStorage()
        storage.save(sampleSession())
        assertNotNull(storage.current())

        var receivedAccessToken: String? = null
        var receivedRequest: ChangePasswordRequest? = null

        val fakeApi = object : AuthApi {
            override suspend fun register(request: RegisterRequest): RegisterResponse = error("no")
            override suspend fun login(request: LoginRequest): LoginResponse = error("no")
            override suspend fun refresh(request: RefreshRequest): RefreshResponse = error("no")
            override suspend fun logout(accessToken: String) {}
            override suspend fun changePassword(
                accessToken: String,
                request: ChangePasswordRequest
            ): ChangePasswordResponse {
                receivedAccessToken = accessToken
                receivedRequest = request
                return ChangePasswordResponse(status = "accepted", requestId = "chg-req-123")
            }
        }

        val repository = AuthRepositoryImpl(fakeApi, storage)
        val result = repository.changePassword("ClaveActual#2026", "NuevaClaveEjemplo#2026")

        assertTrue(result is AuthResult.Success)
        val success = result as AuthResult.Success
        assertEquals("chg-req-123", success.data.requestId)
        assertEquals("accepted", success.data.status)
        assertEquals("test-access-token", receivedAccessToken)
        assertEquals("ClaveActual#2026", receivedRequest?.currentPassword)
        assertEquals("NuevaClaveEjemplo#2026", receivedRequest?.newPassword)

        // Seguridad de sesión (E007): el cambio incrementa la época de sesión en backend,
        // por lo que el repositorio DEBE purgar incondicionalmente el TokenStorage local.
        assertNull(storage.current())
    }

    @Test
    fun changePasswordSinSesionActivaFallaCon401SinLlamarApi() = runTest {
        val storage = InMemoryTokenStorage()
        var apiCalled = false

        val fakeApi = object : AuthApi {
            override suspend fun register(request: RegisterRequest): RegisterResponse = error("no")
            override suspend fun login(request: LoginRequest): LoginResponse = error("no")
            override suspend fun refresh(request: RefreshRequest): RefreshResponse = error("no")
            override suspend fun logout(accessToken: String) {}
            override suspend fun changePassword(
                accessToken: String,
                request: ChangePasswordRequest
            ): ChangePasswordResponse {
                apiCalled = true
                return ChangePasswordResponse(status = "accepted", requestId = "req-1")
            }
        }

        val repository = AuthRepositoryImpl(fakeApi, storage)
        val result = repository.changePassword("Actual#123", "NuevaClaveEjemplo#2026")

        assertTrue(result is AuthResult.Failure)
        val error = (result as AuthResult.Failure).error
        assertEquals(401, error.statusCode)
        assertEquals(AuthErrorKind.INVALID_CREDENTIALS, error.kind)
        assertEquals(false, apiCalled)
    }

    @Test
    fun changePassword401MapeaErrorYNoPurgaSesion() = runTest {
        val storage = InMemoryTokenStorage()
        storage.save(sampleSession())

        val fakeApi = object : AuthApi {
            override suspend fun register(request: RegisterRequest): RegisterResponse = error("no")
            override suspend fun login(request: LoginRequest): LoginResponse = error("no")
            override suspend fun refresh(request: RefreshRequest): RefreshResponse = error("no")
            override suspend fun logout(accessToken: String) {}
            override suspend fun changePassword(
                accessToken: String,
                request: ChangePasswordRequest
            ): ChangePasswordResponse {
                throw AuthApiException(
                    kind = AuthErrorKind.INVALID_CREDENTIALS,
                    message = "La contraseña actual no es correcta.",
                    statusCode = 401
                )
            }
        }

        val repository = AuthRepositoryImpl(fakeApi, storage)
        val result = repository.changePassword("Erronea#123", "NuevaClaveEjemplo#2026")

        assertTrue(result is AuthResult.Failure)
        val error = (result as AuthResult.Failure).error
        assertEquals(401, error.statusCode)
        assertEquals(AuthErrorKind.INVALID_CREDENTIALS, error.kind)
        assertEquals("La contraseña actual no es correcta.", error.message)
        assertNotNull(storage.current())
    }

    @Test
    fun changePassword409MapeaConflictoYNoPurgaSesion() = runTest {
        val storage = InMemoryTokenStorage()
        storage.save(sampleSession())

        val fakeApi = object : AuthApi {
            override suspend fun register(request: RegisterRequest): RegisterResponse = error("no")
            override suspend fun login(request: LoginRequest): LoginResponse = error("no")
            override suspend fun refresh(request: RefreshRequest): RefreshResponse = error("no")
            override suspend fun logout(accessToken: String) {}
            override suspend fun changePassword(
                accessToken: String,
                request: ChangePasswordRequest
            ): ChangePasswordResponse {
                throw AuthApiException(
                    kind = AuthErrorKind.CONFLICT,
                    message = "La nueva contraseña no puede ser igual a la actual.",
                    statusCode = 409
                )
            }
        }

        val repository = AuthRepositoryImpl(fakeApi, storage)
        val result = repository.changePassword("MismaClave#2026", "MismaClave#2026")

        assertTrue(result is AuthResult.Failure)
        val error = (result as AuthResult.Failure).error
        assertEquals(409, error.statusCode)
        assertEquals(AuthErrorKind.CONFLICT, error.kind)
        assertNotNull(storage.current())
    }

    @Test
    fun changePassword422MapeaValidacion() = runTest {
        val storage = InMemoryTokenStorage()
        storage.save(sampleSession())

        val fakeApi = object : AuthApi {
            override suspend fun register(request: RegisterRequest): RegisterResponse = error("no")
            override suspend fun login(request: LoginRequest): LoginResponse = error("no")
            override suspend fun refresh(request: RefreshRequest): RefreshResponse = error("no")
            override suspend fun logout(accessToken: String) {}
            override suspend fun changePassword(
                accessToken: String,
                request: ChangePasswordRequest
            ): ChangePasswordResponse {
                throw AuthApiException(
                    kind = AuthErrorKind.VALIDATION,
                    message = "La nueva contraseña debe tener al menos 12 caracteres.",
                    statusCode = 422
                )
            }
        }

        val repository = AuthRepositoryImpl(fakeApi, storage)
        val result = repository.changePassword("Actual#2026", "corta")

        assertTrue(result is AuthResult.Failure)
        val error = (result as AuthResult.Failure).error
        assertEquals(422, error.statusCode)
        assertEquals(AuthErrorKind.VALIDATION, error.kind)
    }

    @Test
    fun changePasswordErrorDeRedMapeaNetworkReintentable() = runTest {
        val storage = InMemoryTokenStorage()
        storage.save(sampleSession())

        val fakeApi = object : AuthApi {
            override suspend fun register(request: RegisterRequest): RegisterResponse = error("no")
            override suspend fun login(request: LoginRequest): LoginResponse = error("no")
            override suspend fun refresh(request: RefreshRequest): RefreshResponse = error("no")
            override suspend fun logout(accessToken: String) {}
            override suspend fun changePassword(
                accessToken: String,
                request: ChangePasswordRequest
            ): ChangePasswordResponse {
                throw AuthApiException(
                    kind = AuthErrorKind.NETWORK,
                    message = "Error de red al cambiar la contraseña.",
                    retryable = true
                )
            }
        }

        val repository = AuthRepositoryImpl(fakeApi, storage)
        val result = repository.changePassword("Actual#2026", "NuevaClaveEjemplo#2026")

        assertTrue(result is AuthResult.Failure)
        val error = (result as AuthResult.Failure).error
        assertEquals(AuthErrorKind.NETWORK, error.kind)
        assertTrue(error.retryable)
    }
}
