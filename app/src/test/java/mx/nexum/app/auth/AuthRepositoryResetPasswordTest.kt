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
import mx.nexum.app.auth.data.dto.LoginRequest
import mx.nexum.app.auth.data.dto.LoginResponse
import mx.nexum.app.auth.data.dto.RefreshRequest
import mx.nexum.app.auth.data.dto.RefreshResponse
import mx.nexum.app.auth.data.dto.RegisterRequest
import mx.nexum.app.auth.data.dto.RegisterResponse
import mx.nexum.app.auth.data.dto.ResetPasswordRequest
import mx.nexum.app.auth.data.dto.ResetPasswordResponse
import mx.nexum.app.auth.data.dto.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryResetPasswordTest {

    private fun sampleSession() = Session(
        accessToken = "prev-access-token",
        refreshToken = "prev-refresh-token",
        expiresIn = 3600L,
        user = User("u1", "Usuario", "usuario@nexum.test", true)
    )

    @Test
    fun resetPasswordLlamaApiConTokenNormalizado() = runTest {
        var receivedToken: String? = null
        var receivedPassword: String? = null

        val fakeApi = object : AuthApi {
            override suspend fun register(request: RegisterRequest): RegisterResponse = error("no")
            override suspend fun login(request: LoginRequest): LoginResponse = error("no")
            override suspend fun refresh(request: RefreshRequest): RefreshResponse = error("no")
            override suspend fun logout(accessToken: String) {}
            override suspend fun resetPassword(request: ResetPasswordRequest): ResetPasswordResponse {
                receivedToken = request.token
                receivedPassword = request.newPassword
                return ResetPasswordResponse(status = "accepted", requestId = "req-123")
            }
        }

        val repository = AuthRepositoryImpl(fakeApi, InMemoryTokenStorage())
        val result = repository.resetPassword("   token-con-espacios-12345   ", "NuevaPasswordValida123")

        assertTrue(result is AuthResult.Success)
        assertEquals("token-con-espacios-12345", receivedToken)
        assertEquals("NuevaPasswordValida123", receivedPassword)
    }

    @Test
    fun resetPasswordExitosoPurgaTokenStorage() = runTest {
        val storage = InMemoryTokenStorage()
        storage.save(sampleSession())
        assertNotNull(storage.current())

        val fakeApi = object : AuthApi {
            override suspend fun register(request: RegisterRequest): RegisterResponse = error("no")
            override suspend fun login(request: LoginRequest): LoginResponse = error("no")
            override suspend fun refresh(request: RefreshRequest): RefreshResponse = error("no")
            override suspend fun logout(accessToken: String) {}
            override suspend fun resetPassword(request: ResetPasswordRequest): ResetPasswordResponse {
                return ResetPasswordResponse(status = "accepted", requestId = "req-reset-ok")
            }
        }

        val repository = AuthRepositoryImpl(fakeApi, storage)
        val result = repository.resetPassword("token-valido-12345", "NuevaPasswordValida123")

        assertTrue(result is AuthResult.Success)
        assertEquals("req-reset-ok", (result as AuthResult.Success).data.requestId)
        // TokenStorage purgado incondicionalmente tras éxito
        assertNull(storage.current())
    }

    @Test
    fun resetPassword400MapeaErrorYNoPurgaSesion() = runTest {
        val storage = InMemoryTokenStorage()
        storage.save(sampleSession())

        val fakeApi = object : AuthApi {
            override suspend fun register(request: RegisterRequest): RegisterResponse = error("no")
            override suspend fun login(request: LoginRequest): LoginResponse = error("no")
            override suspend fun refresh(request: RefreshRequest): RefreshResponse = error("no")
            override suspend fun logout(accessToken: String) {}
            override suspend fun resetPassword(request: ResetPasswordRequest): ResetPasswordResponse {
                throw AuthApiException(
                    kind = AuthErrorKind.INVALID_CREDENTIALS,
                    message = "El código de recuperación ha expirado o es inválido.",
                    statusCode = 400
                )
            }
        }

        val repository = AuthRepositoryImpl(fakeApi, storage)
        val result = repository.resetPassword("token-invalido-12345", "NuevaPasswordValida123")

        assertTrue(result is AuthResult.Failure)
        val error = (result as AuthResult.Failure).error
        assertEquals(400, error.statusCode)
        assertNotNull(storage.current())
    }

    @Test
    fun resetPassword409MapeaConflictoYNoPurgaSesion() = runTest {
        val storage = InMemoryTokenStorage()
        storage.save(sampleSession())

        val fakeApi = object : AuthApi {
            override suspend fun register(request: RegisterRequest): RegisterResponse = error("no")
            override suspend fun login(request: LoginRequest): LoginResponse = error("no")
            override suspend fun refresh(request: RefreshRequest): RefreshResponse = error("no")
            override suspend fun logout(accessToken: String) {}
            override suspend fun resetPassword(request: ResetPasswordRequest): ResetPasswordResponse {
                throw AuthApiException(
                    kind = AuthErrorKind.CONFLICT,
                    message = "Este código de recuperación ya fue utilizado.",
                    statusCode = 409
                )
            }
        }

        val repository = AuthRepositoryImpl(fakeApi, storage)
        val result = repository.resetPassword("token-reusado-12345", "NuevaPasswordValida123")

        assertTrue(result is AuthResult.Failure)
        val error = (result as AuthResult.Failure).error
        assertEquals(AuthErrorKind.CONFLICT, error.kind)
        assertEquals(409, error.statusCode)
        assertNotNull(storage.current())
    }

    @Test
    fun resetPassword422MapeaValidacion() = runTest {
        val fakeApi = object : AuthApi {
            override suspend fun register(request: RegisterRequest): RegisterResponse = error("no")
            override suspend fun login(request: LoginRequest): LoginResponse = error("no")
            override suspend fun refresh(request: RefreshRequest): RefreshResponse = error("no")
            override suspend fun logout(accessToken: String) {}
            override suspend fun resetPassword(request: ResetPasswordRequest): ResetPasswordResponse {
                throw AuthApiException(
                    kind = AuthErrorKind.VALIDATION,
                    message = "La contraseña debe tener entre 12 y 128 caracteres.",
                    statusCode = 422
                )
            }
        }

        val repository = AuthRepositoryImpl(fakeApi, InMemoryTokenStorage())
        val result = repository.resetPassword("token-valido-12345", "invalida")

        assertTrue(result is AuthResult.Failure)
        val error = (result as AuthResult.Failure).error
        assertEquals(422, error.statusCode)
        assertEquals(AuthErrorKind.VALIDATION, error.kind)
    }

    @Test
    fun resetPasswordErrorDeRedMapeaNetwork() = runTest {
        val fakeApi = object : AuthApi {
            override suspend fun register(request: RegisterRequest): RegisterResponse = error("no")
            override suspend fun login(request: LoginRequest): LoginResponse = error("no")
            override suspend fun refresh(request: RefreshRequest): RefreshResponse = error("no")
            override suspend fun logout(accessToken: String) {}
            override suspend fun resetPassword(request: ResetPasswordRequest): ResetPasswordResponse {
                throw AuthApiException(
                    kind = AuthErrorKind.NETWORK,
                    message = "Error de red al restablecer la contraseña.",
                    retryable = true
                )
            }
        }

        val repository = AuthRepositoryImpl(fakeApi, InMemoryTokenStorage())
        val result = repository.resetPassword("token-valido-12345", "NuevaPasswordValida123")

        assertTrue(result is AuthResult.Failure)
        val error = (result as AuthResult.Failure).error
        assertEquals(AuthErrorKind.NETWORK, error.kind)
        assertTrue(error.retryable)
    }
}
