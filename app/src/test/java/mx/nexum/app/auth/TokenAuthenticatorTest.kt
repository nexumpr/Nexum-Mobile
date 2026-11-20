package mx.nexum.app.auth

import mx.nexum.app.auth.data.AuthError
import mx.nexum.app.auth.data.AuthErrorKind
import mx.nexum.app.auth.data.AuthRepository
import mx.nexum.app.auth.data.AuthResult
import mx.nexum.app.auth.data.InMemoryTokenStorage
import mx.nexum.app.auth.data.RefreshOutcome
import mx.nexum.app.auth.data.Session
import mx.nexum.app.auth.data.dto.LoginRequest
import mx.nexum.app.auth.data.dto.LoginResponse
import mx.nexum.app.auth.data.dto.RegisterRequest
import mx.nexum.app.auth.data.RegisterOutcome
import mx.nexum.app.auth.data.dto.User
import mx.nexum.app.auth.data.network.TokenAuthenticator
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class TokenAuthenticatorTest {

    private fun dummySession(token: String = "token_abc") = Session(
        accessToken = token,
        refreshToken = "refresh_xyz",
        expiresIn = 900L,
        user = User("u1", "Alex", "alex@nexum.test", true),
        sessionId = "s1"
    )

    private fun create401Response(
        authHeader: String? = "Bearer token_abc",
        priorResponse: Response? = null
    ): Response {
        val request = Request.Builder()
            .url("https://example.com/api/v1/protected")
            .apply {
                if (authHeader != null) {
                    header("Authorization", authHeader)
                }
            }
            .build()

        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .priorResponse(priorResponse)
            .build()
    }

    @Test
    fun ante401EjecutaRefreshYReintentaConNuevoToken() {
        val storage = InMemoryTokenStorage()
        storage.save(dummySession("token_expirado"))

        val refreshCalls = AtomicInteger(0)
        val repo = object : AuthRepository {
            override suspend fun register(request: RegisterRequest) = error("no")
            override suspend fun login(request: LoginRequest) = error("no")
            override suspend fun refreshSession(): AuthResult<RefreshOutcome> {
                refreshCalls.incrementAndGet()
                val newSession = dummySession("nuevo_token_jwt")
                storage.save(newSession)
                return AuthResult.Success(RefreshOutcome(newSession))
            }
        }

        val authenticator = TokenAuthenticator(storage, repo)
        val response = create401Response(authHeader = "Bearer token_expirado")

        val retriedRequest = authenticator.authenticate(null, response)

        assertNotNull(retriedRequest)
        assertEquals("Bearer nuevo_token_jwt", retriedRequest?.header("Authorization"))
        assertEquals(1, refreshCalls.get())
    }

    @Test
    fun siRefreshFallaRetornaNullSinReintentar() {
        val storage = InMemoryTokenStorage()
        storage.save(dummySession("token_expirado"))

        val repo = object : AuthRepository {
            override suspend fun register(request: RegisterRequest) = error("no")
            override suspend fun login(request: LoginRequest) = error("no")
            override suspend fun refreshSession(): AuthResult<RefreshOutcome> {
                storage.clear()
                return AuthResult.Failure(
                    AuthError(AuthErrorKind.INVALID_CREDENTIALS, "Sesión revocada", retryable = false)
                )
            }
        }

        val authenticator = TokenAuthenticator(storage, repo)
        val response = create401Response(authHeader = "Bearer token_expirado")

        val retriedRequest = authenticator.authenticate(null, response)

        assertNull(retriedRequest)
        assertNull(storage.current())
    }

    @Test
    fun maximoUnReintentoEvitaBucleInfinitoAnteFalloPersistente() {
        val storage = InMemoryTokenStorage()
        storage.save(dummySession("token_actual"))

        var refreshCalled = false
        val repo = object : AuthRepository {
            override suspend fun register(request: RegisterRequest) = error("no")
            override suspend fun login(request: LoginRequest) = error("no")
            override suspend fun refreshSession(): AuthResult<RefreshOutcome> {
                refreshCalled = true
                return AuthResult.Success(RefreshOutcome(dummySession("token_nuevo")))
            }
        }

        val authenticator = TokenAuthenticator(storage, repo)

        // Simular que esta respuesta 401 ya es el resultado de un reintento anterior
        val priorResponse = create401Response(authHeader = "Bearer token_original")
        val second401Response = create401Response(
            authHeader = "Bearer token_actual",
            priorResponse = priorResponse
        )

        val result = authenticator.authenticate(null, second401Response)

        assertNull(result)
        assertEquals(false, refreshCalled)
    }

    @Test
    fun siTokenYaFueRenovadoPorOtraCorrutinaReutilizaElNuevoSinLlamarRefresh() {
        val storage = InMemoryTokenStorage()
        // El almacenamiento ya tiene el token renovado
        storage.save(dummySession("token_ya_renovado"))

        var refreshCalled = false
        val repo = object : AuthRepository {
            override suspend fun register(request: RegisterRequest) = error("no")
            override suspend fun login(request: LoginRequest) = error("no")
            override suspend fun refreshSession(): AuthResult<RefreshOutcome> {
                refreshCalled = true
                error("No debe llamarse si el token ya cambió")
            }
        }

        val authenticator = TokenAuthenticator(storage, repo)

        // La petición original que falló con 401 llevaba el token anterior ("token_viejo")
        val response = create401Response(authHeader = "Bearer token_viejo")

        val retriedRequest = authenticator.authenticate(null, response)

        assertNotNull(retriedRequest)
        assertEquals("Bearer token_ya_renovado", retriedRequest?.header("Authorization"))
        assertEquals(false, refreshCalled)
    }

    @Test
    fun sinSesionEnAlmacenamientoRetornaNull() {
        val storage = InMemoryTokenStorage()
        val repo = object : AuthRepository {
            override suspend fun register(request: RegisterRequest) = error("no")
            override suspend fun login(request: LoginRequest) = error("no")
            override suspend fun refreshSession(): AuthResult<RefreshOutcome> = error("no")
        }

        val authenticator = TokenAuthenticator(storage, repo)
        val response = create401Response(authHeader = "Bearer cualquier_token")

        val result = authenticator.authenticate(null, response)
        assertNull(result)
    }

    @Test
    fun llamadaDeLogoutCon401NoDisparaRefreshYRetornaNull() {
        val storage = InMemoryTokenStorage()
        storage.save(dummySession("token_logout"))

        var refreshCalled = false
        val repo = object : AuthRepository {
            override suspend fun register(request: RegisterRequest) = error("no")
            override suspend fun login(request: LoginRequest) = error("no")
            override suspend fun refreshSession(): AuthResult<RefreshOutcome> {
                refreshCalled = true
                error("No debe disparar refreshSession ante 401 en logout")
            }
        }

        val authenticator = TokenAuthenticator(storage, repo)

        val logoutRequest = Request.Builder()
            .url("https://example.com/api/v1/auth/logout")
            .header("Authorization", "Bearer token_logout")
            .build()

        val logout401Response = Response.Builder()
            .request(logoutRequest)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .build()

        val result = authenticator.authenticate(null, logout401Response)
        assertNull(result)
        assertEquals(false, refreshCalled)
    }
}
