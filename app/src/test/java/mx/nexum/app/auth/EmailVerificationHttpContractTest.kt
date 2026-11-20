package mx.nexum.app.auth

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import mx.nexum.app.auth.data.AuthApiException
import mx.nexum.app.auth.data.AuthErrorKind
import mx.nexum.app.auth.data.AuthRepositoryImpl
import mx.nexum.app.auth.data.AuthResult
import mx.nexum.app.auth.data.InMemoryTokenStorage
import mx.nexum.app.auth.data.RetrofitAuthRepositoryApi
import mx.nexum.app.auth.data.Session
import mx.nexum.app.auth.data.dto.EmailVerificationResponse
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
class EmailVerificationHttpContractTest {

    private companion object {
        const val PATH = "/api/v1/auth/email-verification/request"
        const val TOKEN = "synthetic.access.token"
    }

    private fun scenario(
        statusCode: Int,
        fixture: String? = null,
        headers: Map<String, String> = emptyMap()
    ) = mapOf(FixtureHttpSimulator.key("POST", PATH) to FixtureScenario(statusCode, fixture, headers))

    private fun repositoryWithSession(
        simulator: FixtureHttpSimulator
    ): Pair<AuthRepositoryImpl, InMemoryTokenStorage> {
        val storage = InMemoryTokenStorage()
        storage.save(
            Session(
                accessToken = TOKEN,
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
        val service = simulator.retrofit().create(AuthApiService::class.java)
        return AuthRepositoryImpl(RetrofitAuthRepositoryApi(service), storage) to storage
    }

    @Test
    fun solicitudUsaBearerYCuerpoHttpCompletamenteVacio() = runTest {
        val simulator = FixtureHttpSimulator(
            scenario(202, "email_verification_202.json")
        )
        val service = simulator.retrofit().create(AuthApiService::class.java)

        val response = service.requestEmailVerification("Bearer $TOKEN")

        assertEquals(202, response.code())
        val recorded = simulator.recorded.single()
        assertEquals("POST", recorded.method)
        assertEquals(PATH, recorded.path)
        assertEquals("Bearer $TOKEN", recorded.headers["Authorization"])
        assertEquals(0L, recorded.contentLength)
        assertEquals("", recorded.body)
        assertFalse(recorded.body.contains("\"email\""))
        assertNull(recorded.contentType)

        val body = response.body() as EmailVerificationResponse
        assertEquals("accepted", body.status)
        assertEquals("req-email-verification-202", body.requestId)
    }

    @Test
    fun acceptedResponseMapsToDomainOutcome() = runTest {
        val (repository, _) = repositoryWithSession(
            FixtureHttpSimulator(scenario(202, "email_verification_202.json"))
        )

        val result = repository.requestEmailVerification()

        assertTrue(result is AuthResult.Success)
        val outcome = (result as AuthResult.Success).data
        assertEquals("accepted", outcome.status)
        assertEquals("req-email-verification-202", outcome.requestId)
    }

    @Test
    fun conflictNeverStartsAcceptedState() = runTest {
        val (repository, _) = repositoryWithSession(
            FixtureHttpSimulator(scenario(409, "error_409.json"))
        )

        val result = repository.requestEmailVerification()

        assertTrue(result is AuthResult.Failure)
        val error = (result as AuthResult.Failure).error
        assertEquals(AuthErrorKind.CONFLICT, error.kind)
        assertFalse(error.retryable)
        assertEquals(409, error.statusCode)
    }

    @Test
    fun rateLimitedReadsRetryAfterHeader() = runTest {
        val (repository, _) = repositoryWithSession(
            FixtureHttpSimulator(scenario(429, "error_429.json", mapOf("Retry-After" to "30")))
        )

        val result = repository.requestEmailVerification()

        assertTrue(result is AuthResult.Failure)
        val error = (result as AuthResult.Failure).error
        assertEquals(AuthErrorKind.RATE_LIMITED, error.kind)
        assertTrue(error.retryable)
        assertEquals(429, error.statusCode)
        assertEquals(30, error.retryAfterSeconds)
    }

    @Test
    fun dependencyUnavailableIsNeverMaskedAsAccepted() = runTest {
        val (repository, _) = repositoryWithSession(
            FixtureHttpSimulator(scenario(503, "error_503.json"))
        )

        val result = repository.requestEmailVerification()

        assertTrue(result is AuthResult.Failure)
        val error = (result as AuthResult.Failure).error
        assertEquals(AuthErrorKind.SERVICE_UNAVAILABLE, error.kind)
        assertTrue(error.retryable)
        assertEquals(503, error.statusCode)
        assertTrue(error.message.contains("DEPENDENCY_UNAVAILABLE"))
    }

    @Test
    fun unauthorizedUsesStandardSessionHandling() = runTest {
        val (repository, storage) = repositoryWithSession(
            FixtureHttpSimulator(scenario(401, "error_401.json"))
        )

        val result = repository.requestEmailVerification()

        assertTrue(result is AuthResult.Failure)
        assertEquals(AuthErrorKind.INVALID_CREDENTIALS, (result as AuthResult.Failure).error.kind)
        assertNull(storage.current())
    }

    @Test
    fun unexpectedStatusDoesNotThrowRawException() = runTest {
        val (repository, _) = repositoryWithSession(
            FixtureHttpSimulator(scenario(500, "error_500.json"))
        )

        val result = repository.requestEmailVerification()

        assertTrue(result is AuthResult.Failure)
        val error = (result as AuthResult.Failure).error
        assertEquals(AuthErrorKind.SERVICE_UNAVAILABLE, error.kind)
        assertTrue(error.retryable)
        assertEquals(500, error.statusCode)
    }

    @Test
    fun malformedAcceptedBodyMappedToRecoverableFailure() = runTest {
        val (repository, _) = repositoryWithSession(
            FixtureHttpSimulator(scenario(202, "malformed.json"))
        )

        val result = repository.requestEmailVerification()

        assertTrue(result is AuthResult.Failure)
        val error = (result as AuthResult.Failure).error
        assertEquals(AuthErrorKind.UNKNOWN, error.kind)
        assertTrue(error.retryable)
    }

    @Test
    fun authApiExceptionKeepsServiceUnavailableKind() {
        val exception = AuthApiException(
            kind = AuthErrorKind.SERVICE_UNAVAILABLE,
            message = "DEPENDENCY_UNAVAILABLE",
            retryable = true,
            statusCode = 503
        )

        assertEquals(AuthErrorKind.SERVICE_UNAVAILABLE, exception.kind)
        assertTrue(exception.retryable)
        assertEquals(503, exception.statusCode)
    }
}
