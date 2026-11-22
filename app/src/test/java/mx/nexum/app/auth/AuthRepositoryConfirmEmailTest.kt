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
import mx.nexum.app.auth.data.dto.ConfirmEmailVerificationRequest
import mx.nexum.app.auth.data.dto.ConfirmEmailVerificationResponse
import mx.nexum.app.auth.data.dto.LoginRequest
import mx.nexum.app.auth.data.dto.LoginResponse
import mx.nexum.app.auth.data.dto.RefreshRequest
import mx.nexum.app.auth.data.dto.RefreshResponse
import mx.nexum.app.auth.data.dto.RegisterRequest
import mx.nexum.app.auth.data.dto.RegisterResponse
import mx.nexum.app.auth.data.dto.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryConfirmEmailTest {

    private companion object {
        const val TOKEN = "  MiXeD Case123  "
    }

    private class FakeAuthApi(
        private val failure: Throwable? = null
    ) : AuthApi {
        var calls = 0
        var lastToken: String? = null

        override suspend fun register(request: RegisterRequest): RegisterResponse = error("no")
        override suspend fun login(request: LoginRequest): LoginResponse = error("no")
        override suspend fun refresh(request: RefreshRequest): RefreshResponse = error("no")
        override suspend fun logout(accessToken: String) = Unit

        override suspend fun confirmEmailVerification(
            request: ConfirmEmailVerificationRequest
        ): ConfirmEmailVerificationResponse {
            calls++
            lastToken = request.token
            failure?.let { throw it }
            return ConfirmEmailVerificationResponse(
                status = "accepted",
                requestId = "req-confirm-synthetic"
            )
        }
    }

    private fun storage(withSession: Boolean = true) = InMemoryTokenStorage().apply {
        if (withSession) {
            save(
                Session(
                    accessToken = "synthetic.access.token",
                    refreshToken = "synthetic.refresh.token",
                    expiresIn = 3600,
                    user = User(
                        id = "user-synthetic",
                        name = "Sintético",
                        email = "usuario@nexum.test",
                        emailVerified = false
                    )
                )
            )
        }
    }

    @Test
    fun tokenCortoSeRechazaLocalmenteSinLlamarARed() = runTest {
        val api = FakeAuthApi()
        val storage = storage()
        val repository = AuthRepositoryImpl(api, storage)

        val result = repository.confirmEmailVerification("short")

        assertTrue(result is AuthResult.Failure)
        val error = (result as AuthResult.Failure).error
        assertEquals(AuthErrorKind.INVALID_TOKEN, error.kind)
        assertEquals(400, error.statusCode)
        assertFalse(error.retryable)
        assertEquals(0, api.calls)
        assertNull(api.lastToken)
        assertFalse(storage.current()?.user?.emailVerified == true)
    }

    @Test
    fun confirmacionExitosaConservaElTokenYMuestraCorreoVerificado() = runTest {
        val api = FakeAuthApi()
        val storage = storage()
        val repository = AuthRepositoryImpl(api, storage)

        val result = repository.confirmEmailVerification(TOKEN)

        assertTrue(result is AuthResult.Success)
        val outcome = (result as AuthResult.Success).data
        assertEquals("accepted", outcome.status)
        assertEquals("req-confirm-synthetic", outcome.requestId)
        assertEquals(1, api.calls)
        assertEquals(TOKEN, api.lastToken)
        assertTrue(storage.current()?.user?.emailVerified == true)
    }

    @Test
    fun confirmacionSinSesionNoModificaEstadoLocal() = runTest {
        val api = FakeAuthApi()
        val storage = storage(withSession = false)
        val repository = AuthRepositoryImpl(api, storage)

        val result = repository.confirmEmailVerification(TOKEN)

        assertTrue(result is AuthResult.Success)
        assertEquals(1, api.calls)
        assertNull(storage.current())
    }

    @Test
    fun tokenInvalidoOExpiradoNoEsReintentable() = runTest {
        val api = FakeAuthApi(
            AuthApiException(AuthErrorKind.INVALID_TOKEN, "El código de verificación es inválido o ha expirado", false, statusCode = 400)
        )
        val storage = storage()
        val repository = AuthRepositoryImpl(api, storage)

        val result = repository.confirmEmailVerification(TOKEN)

        assertTrue(result is AuthResult.Failure)
        val error = (result as AuthResult.Failure).error
        assertEquals(AuthErrorKind.INVALID_TOKEN, error.kind)
        assertFalse(error.retryable)
        assertFalse(error.message.contains("MiXeD Case123"))
        assertFalse(storage.current()?.user?.emailVerified == true)
    }

    @Test
    fun tokenYaUtilizadoEsConflictoNoReintentable() = runTest {
        val api = FakeAuthApi(
            AuthApiException(AuthErrorKind.CONFLICT, "El código de verificación ya fue utilizado", false, statusCode = 409)
        )
        val repository = AuthRepositoryImpl(api, storage())

        val result = repository.confirmEmailVerification(TOKEN)

        assertTrue(result is AuthResult.Failure)
        val error = (result as AuthResult.Failure).error
        assertEquals(AuthErrorKind.CONFLICT, error.kind)
        assertFalse(error.retryable)
    }

    @Test
    fun falloDeRedEsReintentableYPreservaLaSesion() = runTest {
        val api = FakeAuthApi(IOException("synthetic"))
        val storage = storage()
        val repository = AuthRepositoryImpl(api, storage)

        val result = repository.confirmEmailVerification(TOKEN)

        assertTrue(result is AuthResult.Failure)
        val error = (result as AuthResult.Failure).error
        assertEquals(AuthErrorKind.NETWORK, error.kind)
        assertTrue(error.retryable)
        assertTrue(storage.current() != null)
        assertFalse(storage.current()?.user?.emailVerified == true)
    }
}
