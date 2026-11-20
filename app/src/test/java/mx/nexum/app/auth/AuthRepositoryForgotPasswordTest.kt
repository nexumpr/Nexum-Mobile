package mx.nexum.app.auth

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import mx.nexum.app.auth.data.AuthApi
import mx.nexum.app.auth.data.AuthApiException
import mx.nexum.app.auth.data.AuthErrorKind
import mx.nexum.app.auth.data.AuthRepositoryImpl
import mx.nexum.app.auth.data.AuthResult
import mx.nexum.app.auth.data.InMemoryTokenStorage
import mx.nexum.app.auth.data.dto.ForgotPasswordRequest
import mx.nexum.app.auth.data.dto.ForgotPasswordResponse
import mx.nexum.app.auth.data.dto.LoginRequest
import mx.nexum.app.auth.data.dto.LoginResponse
import mx.nexum.app.auth.data.dto.RefreshRequest
import mx.nexum.app.auth.data.dto.RefreshResponse
import mx.nexum.app.auth.data.dto.RegisterRequest
import mx.nexum.app.auth.data.dto.RegisterResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryForgotPasswordTest {

    private fun createRepository(api: AuthApi) =
        AuthRepositoryImpl(api, InMemoryTokenStorage())

    @Test
    fun forgotPasswordReturnsSuccessWithNeutralMessageAndRequestId() = runTest {
        val fakeApi = object : AuthApi {
            override suspend fun register(request: RegisterRequest): RegisterResponse = error("no")
            override suspend fun login(request: LoginRequest): LoginResponse = error("no")
            override suspend fun refresh(request: RefreshRequest): RefreshResponse = error("no")
            override suspend fun logout(accessToken: String) {}
            override suspend fun forgotPassword(request: ForgotPasswordRequest): ForgotPasswordResponse {
                return ForgotPasswordResponse(status = "accepted", requestId = "req-12345")
            }
        }

        val repository = createRepository(fakeApi)
        val result = repository.requestPasswordReset("usuario@nexum.test")

        assertTrue(result is AuthResult.Success)
        val outcome = (result as AuthResult.Success).data
        assertEquals("req-12345", outcome.requestId)
        assertEquals("Si la dirección puede recibir correos, revisa tu bandeja de entrada", outcome.message)
    }

    @Test
    fun forgotPasswordNormalizesEmailTrimAndLowercase() = runTest {
        var receivedEmail: String? = null
        val fakeApi = object : AuthApi {
            override suspend fun register(request: RegisterRequest): RegisterResponse = error("no")
            override suspend fun login(request: LoginRequest): LoginResponse = error("no")
            override suspend fun refresh(request: RefreshRequest): RefreshResponse = error("no")
            override suspend fun logout(accessToken: String) {}
            override suspend fun forgotPassword(request: ForgotPasswordRequest): ForgotPasswordResponse {
                receivedEmail = request.email
                return ForgotPasswordResponse(status = "accepted", requestId = "req-999")
            }
        }

        val repository = createRepository(fakeApi)
        repository.requestPasswordReset("   USUARIO.Demo@Nexum.TEST   ")

        assertEquals("usuario.demo@nexum.test", receivedEmail)
    }

    @Test
    fun forgotPasswordPropagatesRateLimitedWithRetryAfter() = runTest {
        val fakeApi = object : AuthApi {
            override suspend fun register(request: RegisterRequest): RegisterResponse = error("no")
            override suspend fun login(request: LoginRequest): LoginResponse = error("no")
            override suspend fun refresh(request: RefreshRequest): RefreshResponse = error("no")
            override suspend fun logout(accessToken: String) {}
            override suspend fun forgotPassword(request: ForgotPasswordRequest): ForgotPasswordResponse {
                throw AuthApiException(
                    kind = AuthErrorKind.RATE_LIMITED,
                    message = "Demasiados intentos. Intenta más tarde.",
                    statusCode = 429,
                    retryAfterSeconds = 120
                )
            }
        }

        val repository = createRepository(fakeApi)
        val result = repository.requestPasswordReset("demo@nexum.test")

        assertTrue(result is AuthResult.Failure)
        val error = (result as AuthResult.Failure).error
        assertEquals(AuthErrorKind.RATE_LIMITED, error.kind)
        assertEquals(120, error.retryAfterSeconds)
    }

    @Test
    fun forgotPasswordPropagatesNetworkError() = runTest {
        val fakeApi = object : AuthApi {
            override suspend fun register(request: RegisterRequest): RegisterResponse = error("no")
            override suspend fun login(request: LoginRequest): LoginResponse = error("no")
            override suspend fun refresh(request: RefreshRequest): RefreshResponse = error("no")
            override suspend fun logout(accessToken: String) {}
            override suspend fun forgotPassword(request: ForgotPasswordRequest): ForgotPasswordResponse {
                throw AuthApiException(
                    kind = AuthErrorKind.NETWORK,
                    message = "Sin conexión. Intenta de nuevo.",
                    retryable = true
                )
            }
        }

        val repository = createRepository(fakeApi)
        val result = repository.requestPasswordReset("demo@nexum.test")

        assertTrue(result is AuthResult.Failure)
        val error = (result as AuthResult.Failure).error
        assertEquals(AuthErrorKind.NETWORK, error.kind)
        assertTrue(error.retryable)
    }

    @Test
    fun forgotPasswordPropagatesIOExceptionAsNetworkError() = runTest {
        val fakeApi = object : AuthApi {
            override suspend fun register(request: RegisterRequest): RegisterResponse = error("no")
            override suspend fun login(request: LoginRequest): LoginResponse = error("no")
            override suspend fun refresh(request: RefreshRequest): RefreshResponse = error("no")
            override suspend fun logout(accessToken: String) {}
            override suspend fun forgotPassword(request: ForgotPasswordRequest): ForgotPasswordResponse {
                throw IOException("Connection reset by peer")
            }
        }

        val repository = createRepository(fakeApi)
        val result = repository.requestPasswordReset("demo@nexum.test")

        assertTrue(result is AuthResult.Failure)
        val error = (result as AuthResult.Failure).error
        assertEquals(AuthErrorKind.NETWORK, error.kind)
        assertTrue(error.retryable)
    }
}
