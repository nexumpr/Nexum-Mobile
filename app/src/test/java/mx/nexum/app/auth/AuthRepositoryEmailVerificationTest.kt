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
import mx.nexum.app.auth.data.dto.EmailVerificationResponse
import mx.nexum.app.auth.data.dto.ForgotPasswordRequest
import mx.nexum.app.auth.data.dto.ForgotPasswordResponse
import mx.nexum.app.auth.data.dto.LoginRequest
import mx.nexum.app.auth.data.dto.LoginResponse
import mx.nexum.app.auth.data.dto.RefreshRequest
import mx.nexum.app.auth.data.dto.RefreshResponse
import mx.nexum.app.auth.data.dto.RegisterRequest
import mx.nexum.app.auth.data.dto.RegisterResponse
import mx.nexum.app.auth.data.dto.ResetPasswordRequest
import mx.nexum.app.auth.data.dto.ResetPasswordResponse
import mx.nexum.app.auth.data.dto.SessionsPageResponse
import mx.nexum.app.auth.data.dto.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryEmailVerificationTest {

    private fun activeSession(accessToken: String = "synthetic.access.token") = Session(
        accessToken = accessToken,
        refreshToken = "synthetic.refresh.token",
        expiresIn = 3600,
        user = User(
            id = "user-synthetic",
            name = "Sintético",
            email = "usuario@nexum.test",
            emailVerified = false
        )
    )

    private fun repositoryWithSession(
        api: AuthApi,
        accessToken: String = "synthetic.access.token"
    ): Pair<AuthRepositoryImpl, InMemoryTokenStorage> {
        val storage = InMemoryTokenStorage()
        storage.save(activeSession(accessToken))
        return AuthRepositoryImpl(api, storage) to storage
    }

    private open class FakeAuthApi(
        private val response: EmailVerificationResponse = EmailVerificationResponse(
            status = "accepted",
            requestId = "req-synthetic"
        ),
        private val failure: Throwable? = null
    ) : AuthApi {
        var calls = 0
        var lastAccessToken: String? = null

        override suspend fun register(request: RegisterRequest): RegisterResponse = error("no")
        override suspend fun login(request: LoginRequest): LoginResponse = error("no")
        override suspend fun refresh(request: RefreshRequest): RefreshResponse = error("no")
        override suspend fun logout(accessToken: String) = Unit
        override suspend fun forgotPassword(request: ForgotPasswordRequest): ForgotPasswordResponse = error("no")
        override suspend fun resetPassword(request: ResetPasswordRequest): ResetPasswordResponse = error("no")
        override suspend fun changePassword(
            accessToken: String,
            request: ChangePasswordRequest
        ): ChangePasswordResponse = error("no")
        override suspend fun getSessions(
            accessToken: String,
            limit: Int?,
            cursor: String?
        ): SessionsPageResponse = error("no")
        override suspend fun revokeSession(accessToken: String, sessionId: String) = Unit

        override suspend fun requestEmailVerification(accessToken: String): EmailVerificationResponse {
            calls++
            lastAccessToken = accessToken
            failure?.let { throw it }
            return response
        }
    }

    @Test
    fun acceptedResponseMapsToOutcomeUsingActiveBearerToken() = runTest {
        val api = FakeAuthApi(
            response = EmailVerificationResponse(status = "accepted", requestId = "req-email-202")
        )
        val (repository, _) = repositoryWithSession(api)

        val result = repository.requestEmailVerification()

        assertTrue(result is AuthResult.Success)
        val outcome = (result as AuthResult.Success).data
        assertEquals("accepted", outcome.status)
        assertEquals("req-email-202", outcome.requestId)
        assertEquals(1, api.calls)
        assertEquals("synthetic.access.token", api.lastAccessToken)
    }

    @Test
    fun missingSessionFailsLocallyWithoutCallingApi() = runTest {
        val api = FakeAuthApi()
        val repository = AuthRepositoryImpl(api, InMemoryTokenStorage())

        val result = repository.requestEmailVerification()

        assertTrue(result is AuthResult.Failure)
        val error = (result as AuthResult.Failure).error
        assertEquals(AuthErrorKind.INVALID_CREDENTIALS, error.kind)
        assertEquals(401, error.statusCode)
        assertEquals(0, api.calls)
        assertNull(api.lastAccessToken)
    }

    @Test
    fun conflictMapsToNonRetryableError() = runTest {
        val failure = AuthApiException(
            kind = AuthErrorKind.CONFLICT,
            message = "Tu correo electrónico ya está verificado",
            retryable = false,
            statusCode = 409
        )
        val (repository, _) = repositoryWithSession(FakeAuthApi(failure = failure))

        val result = repository.requestEmailVerification()

        assertTrue(result is AuthResult.Failure)
        val error = (result as AuthResult.Failure).error
        assertEquals(AuthErrorKind.CONFLICT, error.kind)
        assertFalse(error.retryable)
        assertEquals("Tu correo electrónico ya está verificado", error.message)
        assertEquals(409, error.statusCode)
    }

    @Test
    fun rateLimitedPreservesRetryAfter() = runTest {
        val failure = AuthApiException(
            kind = AuthErrorKind.RATE_LIMITED,
            message = "Has alcanzado el límite de 3 solicitudes cada 15 minutos",
            retryable = true,
            statusCode = 429,
            retryAfterSeconds = 30
        )
        val (repository, _) = repositoryWithSession(FakeAuthApi(failure = failure))

        val result = repository.requestEmailVerification()

        assertTrue(result is AuthResult.Failure)
        val error = (result as AuthResult.Failure).error
        assertEquals(AuthErrorKind.RATE_LIMITED, error.kind)
        assertTrue(error.retryable)
        assertEquals(429, error.statusCode)
        assertEquals(30, error.retryAfterSeconds)
    }

    @Test
    fun serviceUnavailableIsNeverReportedAsSuccess() = runTest {
        val message = "El servicio de correo no está disponible temporalmente (DEPENDENCY_UNAVAILABLE)"
        val failure = AuthApiException(
            kind = AuthErrorKind.SERVICE_UNAVAILABLE,
            message = message,
            retryable = true,
            statusCode = 503
        )
        val (repository, _) = repositoryWithSession(FakeAuthApi(failure = failure))

        val result = repository.requestEmailVerification()

        assertTrue(result is AuthResult.Failure)
        val error = (result as AuthResult.Failure).error
        assertEquals(AuthErrorKind.SERVICE_UNAVAILABLE, error.kind)
        assertTrue(error.retryable)
        assertEquals(message, error.message)
        assertEquals(503, error.statusCode)
    }

    @Test
    fun unauthorizedClearsSession() = runTest {
        val failure = AuthApiException(
            kind = AuthErrorKind.INVALID_CREDENTIALS,
            message = "Sesión no autorizada o expirada (401).",
            statusCode = 401
        )
        val (repository, storage) = repositoryWithSession(FakeAuthApi(failure = failure))

        val result = repository.requestEmailVerification()

        assertTrue(result is AuthResult.Failure)
        assertEquals(AuthErrorKind.INVALID_CREDENTIALS, (result as AuthResult.Failure).error.kind)
        assertNull(storage.current())
    }

    @Test
    fun ioExceptionMapsToRetryableNetworkError() = runTest {
        val (repository, _) = repositoryWithSession(
            FakeAuthApi(failure = IOException("Connection reset by peer"))
        )

        val result = repository.requestEmailVerification()

        assertTrue(result is AuthResult.Failure)
        val error = (result as AuthResult.Failure).error
        assertEquals(AuthErrorKind.NETWORK, error.kind)
        assertTrue(error.retryable)
    }
}
