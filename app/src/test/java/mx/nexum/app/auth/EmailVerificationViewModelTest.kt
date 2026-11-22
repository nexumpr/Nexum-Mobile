package mx.nexum.app.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import mx.nexum.app.auth.data.AuthError
import mx.nexum.app.auth.data.AuthErrorKind
import mx.nexum.app.auth.data.AuthRepository
import mx.nexum.app.auth.data.AuthResult
import mx.nexum.app.auth.data.EmailVerificationOutcome
import mx.nexum.app.auth.data.InMemoryTokenStorage
import mx.nexum.app.auth.data.LoginOutcome
import mx.nexum.app.auth.data.RefreshOutcome
import mx.nexum.app.auth.data.RegisterOutcome
import mx.nexum.app.auth.data.Session
import mx.nexum.app.auth.data.dto.LoginRequest
import mx.nexum.app.auth.data.dto.RegisterRequest
import mx.nexum.app.auth.data.dto.User
import mx.nexum.app.auth.ui.EmailVerificationIntent
import mx.nexum.app.auth.ui.EmailVerificationViewModel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EmailVerificationViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakeAuthRepository(
        var result: AuthResult<EmailVerificationOutcome> = AuthResult.Success(
            EmailVerificationOutcome(status = "accepted", requestId = "req-synthetic")
        )
    ) : AuthRepository {
        var calls = 0

        override suspend fun register(request: RegisterRequest): AuthResult<RegisterOutcome> = error("no")
        override suspend fun login(request: LoginRequest): AuthResult<LoginOutcome> = error("no")
        override suspend fun refreshSession(): AuthResult<RefreshOutcome> = error("no")

        override suspend fun requestEmailVerification(): AuthResult<EmailVerificationOutcome> {
            calls++
            return result
        }
    }

    private fun tokenStorage(emailVerified: Boolean = false) = InMemoryTokenStorage().apply {
        save(
            Session(
                accessToken = "synthetic.access.token",
                refreshToken = "synthetic.refresh.token",
                expiresIn = 3600,
                user = User(
                    id = "user-synthetic",
                    name = "Sintético",
                    email = "usuario@nexum.test",
                    emailVerified = emailVerified
                )
            )
        )
    }

    private fun failure(kind: AuthErrorKind, message: String) = AuthResult.Failure(
        AuthError(kind = kind, message = message, retryable = kind != AuthErrorKind.CONFLICT)
    )

    @Test
    fun initialStateReflectsAuthenticatedUser() {
        val viewModel = EmailVerificationViewModel(FakeAuthRepository(), tokenStorage())
        val state = viewModel.uiState.value

        assertEquals("usuario@nexum.test", state.userEmail)
        assertFalse(state.isEmailVerified)
        assertFalse(state.isLoading)
        assertFalse(state.isAccepted)
        assertNull(state.lastRequestId)
        assertEquals(0, state.cooldownSeconds)
        assertNull(state.feedbackMessage)
        assertFalse(state.isError)
    }

    @Test
    fun acceptedRequestActivatesStateAndStartsSixtySecondCooldown() = runTest(testDispatcher) {
        val repository = FakeAuthRepository()
        val viewModel = EmailVerificationViewModel(repository, tokenStorage())

        viewModel.onIntent(EmailVerificationIntent.RequestVerification)
        runCurrent()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isAccepted)
        assertEquals("req-synthetic", state.lastRequestId)
        assertEquals(60, state.cooldownSeconds)
        assertFalse(state.isError)
        assertTrue(state.feedbackMessage!!.contains("30 minutos"))
        assertEquals(1, repository.calls)

        advanceTimeBy(1000L)
        runCurrent()
        assertEquals(59, viewModel.uiState.value.cooldownSeconds)

        viewModel.onIntent(EmailVerificationIntent.RequestVerification)
        runCurrent()
        assertEquals(1, repository.calls)

        advanceUntilIdle()
        assertEquals(0, viewModel.uiState.value.cooldownSeconds)

        viewModel.onIntent(EmailVerificationIntent.RequestVerification)
        runCurrent()
        assertEquals(2, repository.calls)
        advanceUntilIdle()
    }

    @Test
    fun conflictMarksVerifiedWithoutCooldown() = runTest(testDispatcher) {
        val repository = FakeAuthRepository(
            failure(AuthErrorKind.CONFLICT, "Tu correo electrónico ya está verificado")
        )
        val viewModel = EmailVerificationViewModel(repository, tokenStorage())

        viewModel.onIntent(EmailVerificationIntent.RequestVerification)
        runCurrent()

        val state = viewModel.uiState.value
        assertTrue(state.isEmailVerified)
        assertFalse(state.isAccepted)
        assertFalse(state.isError)
        assertEquals(0, state.cooldownSeconds)
        assertTrue(state.feedbackMessage!!.contains("ya está verificado"))

        viewModel.onIntent(EmailVerificationIntent.RequestVerification)
        runCurrent()
        assertEquals(1, repository.calls)
    }

    @Test
    fun rateLimitShowsQuotaWithoutAcceptedOrCooldown() = runTest(testDispatcher) {
        val repository = FakeAuthRepository(
            failure(AuthErrorKind.RATE_LIMITED, "Demasiadas solicitudes")
        )
        val viewModel = EmailVerificationViewModel(repository, tokenStorage())

        viewModel.onIntent(EmailVerificationIntent.RequestVerification)
        runCurrent()

        val state = viewModel.uiState.value
        assertTrue(state.isError)
        assertFalse(state.isAccepted)
        assertEquals(0, state.cooldownSeconds)
        assertTrue(state.feedbackMessage!!.contains("3 solicitudes cada 15 minutos"))
    }

    @Test
    fun serviceUnavailableShowsRealMessageAndAllowsRetry() = runTest(testDispatcher) {
        val message = "El servicio de correo no está disponible temporalmente (DEPENDENCY_UNAVAILABLE)"
        val repository = FakeAuthRepository(failure(AuthErrorKind.SERVICE_UNAVAILABLE, message))
        val viewModel = EmailVerificationViewModel(repository, tokenStorage())

        viewModel.onIntent(EmailVerificationIntent.RequestVerification)
        runCurrent()

        val failedState = viewModel.uiState.value
        assertTrue(failedState.isError)
        assertFalse(failedState.isAccepted)
        assertEquals(0, failedState.cooldownSeconds)
        assertEquals(message, failedState.feedbackMessage)

        repository.result = AuthResult.Success(
            EmailVerificationOutcome(status = "accepted", requestId = "req-after-retry")
        )
        viewModel.onIntent(EmailVerificationIntent.RequestVerification)
        runCurrent()

        val retriedState = viewModel.uiState.value
        assertTrue(retriedState.isAccepted)
        assertFalse(retriedState.isError)
        assertEquals("req-after-retry", retriedState.lastRequestId)
        assertEquals(60, retriedState.cooldownSeconds)
        assertEquals(2, repository.calls)
        advanceUntilIdle()
    }

    @Test
    fun networkErrorDoesNotMarkAsSent() = runTest(testDispatcher) {
        val repository = FakeAuthRepository(
            failure(AuthErrorKind.NETWORK, "Sin conexión. Intenta de nuevo.")
        )
        val viewModel = EmailVerificationViewModel(repository, tokenStorage())

        viewModel.onIntent(EmailVerificationIntent.RequestVerification)
        runCurrent()

        val state = viewModel.uiState.value
        assertTrue(state.isError)
        assertFalse(state.isAccepted)
        assertEquals(0, state.cooldownSeconds)
        assertEquals("Sin conexión. Intenta de nuevo.", state.feedbackMessage)
    }

    @Test
    fun dismissFeedbackClearsMessageAndError() = runTest(testDispatcher) {
        val repository = FakeAuthRepository(
            failure(AuthErrorKind.SERVICE_UNAVAILABLE, "DEPENDENCY_UNAVAILABLE")
        )
        val viewModel = EmailVerificationViewModel(repository, tokenStorage())

        viewModel.onIntent(EmailVerificationIntent.RequestVerification)
        runCurrent()
        assertTrue(viewModel.uiState.value.isError)

        viewModel.onIntent(EmailVerificationIntent.DismissFeedback)
        runCurrent()

        assertNull(viewModel.uiState.value.feedbackMessage)
        assertFalse(viewModel.uiState.value.isError)
    }

    @Test
    fun verifiedUserDoesNotRequestVerification() = runTest(testDispatcher) {
        val repository = FakeAuthRepository()
        val viewModel = EmailVerificationViewModel(repository, tokenStorage(emailVerified = true))

        assertTrue(viewModel.uiState.value.isEmailVerified)
        viewModel.onIntent(EmailVerificationIntent.RequestVerification)
        runCurrent()

        assertEquals(0, repository.calls)
    }
}
