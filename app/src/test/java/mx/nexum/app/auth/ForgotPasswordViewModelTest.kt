package mx.nexum.app.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
import mx.nexum.app.auth.data.ForgotPasswordOutcome
import mx.nexum.app.auth.data.LoginOutcome
import mx.nexum.app.auth.data.RefreshOutcome
import mx.nexum.app.auth.data.RegisterOutcome
import mx.nexum.app.auth.data.dto.LoginRequest
import mx.nexum.app.auth.data.dto.RegisterRequest
import mx.nexum.app.auth.ui.AuthEffect
import mx.nexum.app.auth.ui.ForgotPasswordIntent
import mx.nexum.app.auth.ui.ForgotPasswordViewModel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ForgotPasswordViewModelTest {

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
        var forgotPasswordResult: AuthResult<ForgotPasswordOutcome> = AuthResult.Success(
            ForgotPasswordOutcome(
                requestId = "req-1",
                message = "Si la dirección puede recibir correos, revisa tu bandeja de entrada"
            )
        )
    ) : AuthRepository {
        var forgotPasswordCalls = 0
        var lastEmailReceived: String? = null

        override suspend fun register(request: RegisterRequest): AuthResult<RegisterOutcome> = error("no")
        override suspend fun login(request: LoginRequest): AuthResult<LoginOutcome> = error("no")
        override suspend fun refreshSession(): AuthResult<RefreshOutcome> = error("no")
        override suspend fun logout(): AuthResult<Unit> = AuthResult.Success(Unit)

        override suspend fun requestPasswordReset(email: String): AuthResult<ForgotPasswordOutcome> {
            forgotPasswordCalls++
            lastEmailReceived = email
            return forgotPasswordResult
        }
    }

    @Test
    fun initialStateIsDefault() {
        val repo = FakeAuthRepository()
        val viewModel = ForgotPasswordViewModel(repo)
        val state = viewModel.state.value

        assertEquals("", state.email)
        assertFalse(state.isLoading)
        assertNull(state.emailError)
        assertFalse(state.isSubmitted)
        assertNull(state.requestId)
        assertNull(state.feedbackMessage)
        assertEquals(0, state.cooldownSeconds)
    }

    @Test
    fun emailChangedUpdatesStateAndClearsEmailError() = runTest {
        val repo = FakeAuthRepository()
        val viewModel = ForgotPasswordViewModel(repo)

        viewModel.onIntent(ForgotPasswordIntent.Submit)
        assertNotNull(viewModel.state.value.emailError)

        viewModel.onIntent(ForgotPasswordIntent.EmailChanged("usuario@nexum.test"))
        assertEquals("usuario@nexum.test", viewModel.state.value.email)
        assertNull(viewModel.state.value.emailError)
    }

    @Test
    fun submitWithBlankEmailSetsErrorWithoutCallingRepository() = runTest {
        val repo = FakeAuthRepository()
        val viewModel = ForgotPasswordViewModel(repo)

        viewModel.onIntent(ForgotPasswordIntent.Submit)
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.emailError)
        assertEquals(0, repo.forgotPasswordCalls)
    }

    @Test
    fun submitWithInvalidEmailFormatSetsErrorWithoutCallingRepository() = runTest {
        val repo = FakeAuthRepository()
        val viewModel = ForgotPasswordViewModel(repo)

        viewModel.onIntent(ForgotPasswordIntent.EmailChanged("correo-no-valido"))
        viewModel.onIntent(ForgotPasswordIntent.Submit)
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.emailError)
        assertEquals(0, repo.forgotPasswordCalls)
    }

    @Test
    fun submitSuccessSetsSubmittedStateNeutralMessageAndStartsCooldown() = runTest {
        val repo = FakeAuthRepository()
        val viewModel = ForgotPasswordViewModel(repo)

        viewModel.onIntent(ForgotPasswordIntent.EmailChanged("valido@nexum.test"))
        viewModel.onIntent(ForgotPasswordIntent.Submit)
        runCurrent()

        assertEquals(1, repo.forgotPasswordCalls)
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertTrue(state.isSubmitted)
        assertEquals("req-1", state.requestId)
        assertEquals("Si la dirección puede recibir correos, revisa tu bandeja de entrada", state.feedbackMessage)
        assertEquals(60, state.cooldownSeconds)

        advanceTimeBy(1000L)
        runCurrent()
        assertEquals(59, viewModel.state.value.cooldownSeconds)

        advanceUntilIdle()
        assertEquals(0, viewModel.state.value.cooldownSeconds)
    }

    @Test
    fun submitWith429RateLimitedSetsCooldownAndShowsSnackbar() = runTest {
        val repo = FakeAuthRepository(
            forgotPasswordResult = AuthResult.Failure(
                AuthError(
                    kind = AuthErrorKind.RATE_LIMITED,
                    message = "Demasiados intentos. Intenta más tarde.",
                    retryable = true,
                    retryAfterSeconds = 45
                )
            )
        )
        val viewModel = ForgotPasswordViewModel(repo)
        val effects = mutableListOf<AuthEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.collect { effects += it }
        }

        viewModel.onIntent(ForgotPasswordIntent.EmailChanged("demasiados@nexum.test"))
        viewModel.onIntent(ForgotPasswordIntent.Submit)
        runCurrent()

        assertEquals(1, repo.forgotPasswordCalls)
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(45, state.cooldownSeconds)
        assertEquals("Demasiados intentos. Intenta más tarde.", state.feedbackMessage)

        assertEquals(1, effects.size)
        assertTrue(effects[0] is AuthEffect.ShowSnackbar)
        val snackbar = effects[0] as AuthEffect.ShowSnackbar
        assertEquals("Demasiados intentos. Intenta más tarde.", snackbar.message)
        assertFalse(snackbar.retry)

        advanceUntilIdle()
    }

    @Test
    fun submitWithNetworkErrorRetainsEmailAndShowsRetrySnackbar() = runTest {
        val repo = FakeAuthRepository(
            forgotPasswordResult = AuthResult.Failure(
                AuthError(
                    kind = AuthErrorKind.NETWORK,
                    message = "Sin conexión. Intenta de nuevo.",
                    retryable = true
                )
            )
        )
        val viewModel = ForgotPasswordViewModel(repo)
        val effects = mutableListOf<AuthEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.collect { effects += it }
        }

        viewModel.onIntent(ForgotPasswordIntent.EmailChanged("red@nexum.test"))
        viewModel.onIntent(ForgotPasswordIntent.Submit)
        advanceUntilIdle()

        assertEquals(1, repo.forgotPasswordCalls)
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertFalse(state.isSubmitted)
        assertEquals("red@nexum.test", state.email)
        assertEquals(0, state.cooldownSeconds)

        assertEquals(1, effects.size)
        assertTrue(effects[0] is AuthEffect.ShowSnackbar)
        val snackbar = effects[0] as AuthEffect.ShowSnackbar
        assertEquals("Sin conexión. Intenta de nuevo.", snackbar.message)
        assertTrue(snackbar.retry)
    }

    @Test
    fun navigateToLoginIntentEmitsNavigateToLoginEffect() = runTest {
        val repo = FakeAuthRepository()
        val viewModel = ForgotPasswordViewModel(repo)
        val effects = mutableListOf<AuthEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.collect { effects += it }
        }

        viewModel.onIntent(ForgotPasswordIntent.NavigateToLogin)
        advanceUntilIdle()

        assertEquals(listOf(AuthEffect.NavigateToLogin), effects)
    }

    @Test
    fun navigateToResetPasswordIntentEmitsNavigateToResetPasswordEffectWithEmail() = runTest {
        val repo = FakeAuthRepository()
        val viewModel = ForgotPasswordViewModel(repo)
        val effects = mutableListOf<AuthEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.collect { effects += it }
        }

        viewModel.onIntent(ForgotPasswordIntent.EmailChanged("destino@nexum.test"))
        viewModel.onIntent(ForgotPasswordIntent.NavigateToResetPassword)
        advanceUntilIdle()

        assertEquals(listOf(AuthEffect.NavigateToResetPassword("destino@nexum.test")), effects)
    }

    @Test
    fun cooldownPreventsSubmissionUntilElapsed() = runTest {
        val repo = FakeAuthRepository()
        val viewModel = ForgotPasswordViewModel(repo)

        viewModel.onIntent(ForgotPasswordIntent.EmailChanged("valido@nexum.test"))
        viewModel.onIntent(ForgotPasswordIntent.Submit)
        runCurrent()

        assertEquals(1, repo.forgotPasswordCalls)
        assertTrue(viewModel.state.value.cooldownSeconds > 0)

        // Intento durante cooldown: debe ignorarse
        viewModel.onIntent(ForgotPasswordIntent.Submit)
        runCurrent()
        assertEquals(1, repo.forgotPasswordCalls)

        // Tras agotarse los 60s de cooldown, vuelve a permitir el envío
        advanceUntilIdle()
        assertEquals(0, viewModel.state.value.cooldownSeconds)

        viewModel.onIntent(ForgotPasswordIntent.Submit)
        runCurrent()
        assertEquals(2, repo.forgotPasswordCalls)

        advanceUntilIdle()
    }
}