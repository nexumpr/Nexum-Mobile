package mx.nexum.app.auth

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import mx.nexum.app.auth.data.AuthErrorKind
import mx.nexum.app.auth.data.AuthRepositoryImpl
import mx.nexum.app.auth.data.AuthResult
import mx.nexum.app.auth.data.InMemoryTokenStorage
import mx.nexum.app.auth.data.RetrofitAuthRepositoryApi
import mx.nexum.app.auth.data.Session
import mx.nexum.app.auth.data.dto.ConfirmEmailVerificationRequest
import mx.nexum.app.auth.data.dto.User
import mx.nexum.app.auth.data.network.AuthApiService
import mx.nexum.app.core.contract.FixtureHttpSimulator
import mx.nexum.app.core.contract.FixtureScenario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConfirmEmailVerificationHttpContractTest {

    private companion object {
        const val PATH = "/api/v1/auth/email-verification/confirm"
        const val TOKEN = "  AbC123XY_z  "
    }

    private fun scenario(
        statusCode: Int,
        fixture: String? = null
    ) = mapOf(FixtureHttpSimulator.key("POST", PATH) to FixtureScenario(statusCode, fixture))

    private fun repository(simulator: FixtureHttpSimulator): Pair<AuthRepositoryImpl, InMemoryTokenStorage> {
        val storage = InMemoryTokenStorage()
        val service = simulator.retrofit().create(AuthApiService::class.java)
        return AuthRepositoryImpl(RetrofitAuthRepositoryApi(service), storage) to storage
    }

    private fun saveSession(storage: InMemoryTokenStorage) {
        storage.save(
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

    @Test
    fun confirmacionEsPublicaYConservaElJsonDelTokenLiteral() = runTest {
        val simulator = FixtureHttpSimulator(scenario(200, "email_verification_202.json"))
        val service = simulator.retrofit().create(AuthApiService::class.java)

        val response = service.confirmEmailVerification(ConfirmEmailVerificationRequest(TOKEN))

        assertEquals(200, response.code())
        val recorded = simulator.recorded.single()
        assertEquals("POST", recorded.method)
        assertEquals(PATH, recorded.path)
        assertNull(recorded.headers["Authorization"])
        assertEquals("{\"token\":\"  AbC123XY_z  \"}", recorded.body)
        assertTrue(recorded.contentType?.contains("application/json") == true)

        val body = response.body() ?: error("Cuerpo 200 ausente")
        assertEquals("accepted", body.status)
        assertEquals("req-email-verification-202", body.requestId)
    }

    @Test
    fun respuesta200ActualizaElEstadoVisualDeVerificacion() = runTest {
        val simulator = FixtureHttpSimulator(scenario(200, "email_verification_202.json"))
        val (repository, storage) = repository(simulator)
        saveSession(storage)

        val result = repository.confirmEmailVerification(TOKEN)

        assertTrue(result is AuthResult.Success)
        val outcome = (result as AuthResult.Success).data
        assertEquals("accepted", outcome.status)
        assertEquals("req-email-verification-202", outcome.requestId)
        assertTrue(storage.current()?.user?.emailVerified == true)
    }

    @Test
    fun respuesta200SinSesionNoCreaEstadoLocal() = runTest {
        val simulator = FixtureHttpSimulator(scenario(200, "email_verification_202.json"))
        val (repository, storage) = repository(simulator)

        val result = repository.confirmEmailVerification(TOKEN)

        assertTrue(result is AuthResult.Success)
        assertNull(storage.current())
    }

    @Test
    fun respuesta400MapeaATokenInvalidoSinFiltrarElToken() = runTest {
        val simulator = FixtureHttpSimulator(scenario(400, "error_400.json"))
        val (repository, storage) = repository(simulator)
        saveSession(storage)

        val result = repository.confirmEmailVerification(TOKEN)

        assertTrue(result is AuthResult.Failure)
        val error = (result as AuthResult.Failure).error
        assertEquals(AuthErrorKind.INVALID_TOKEN, error.kind)
        assertEquals(400, error.statusCode)
        assertFalse(error.retryable)
        assertFalse(error.message.contains("AbC123XY_z"))
        assertFalse(storage.current()?.user?.emailVerified == true)
    }

    @Test
    fun respuesta409MapeaAConflictNoReintentable() = runTest {
        val simulator = FixtureHttpSimulator(scenario(409, "error_409.json"))
        val (repository, storage) = repository(simulator)
        saveSession(storage)

        val result = repository.confirmEmailVerification(TOKEN)

        assertTrue(result is AuthResult.Failure)
        val error = (result as AuthResult.Failure).error
        assertEquals(AuthErrorKind.CONFLICT, error.kind)
        assertEquals(409, error.statusCode)
        assertFalse(error.retryable)
    }
}
