package mx.nexum.app.auth

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import mx.nexum.app.auth.data.AuthRepository
import mx.nexum.app.auth.data.AuthResult
import mx.nexum.app.auth.data.LoginOutcome
import mx.nexum.app.auth.data.RefreshOutcome
import mx.nexum.app.auth.data.RegisterOutcome
import mx.nexum.app.auth.data.dto.LoginRequest
import mx.nexum.app.auth.data.dto.RegisterRequest
import mx.nexum.app.auth.ui.AuthEffect
import mx.nexum.app.auth.ui.LogoutIntent
import mx.nexum.app.auth.ui.LogoutViewModel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

@OptIn(ExperimentalCoroutinesApi::class)
class LogoutViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakeLogoutRepository(
        val onLogout: suspend () -> Unit = {}
    ) : AuthRepository {
        var logoutCalls = AtomicInteger(0)

        override suspend fun register(request: RegisterRequest): AuthResult<RegisterOutcome> = error("no")
        override suspend fun login(request: LoginRequest): AuthResult<LoginOutcome> = error("no")
        override suspend fun refreshSession(): AuthResult<RefreshOutcome> = error("no")

        override suspend fun logout(): AuthResult<Unit> {
            logoutCalls.incrementAndGet()
            onLogout()
            return AuthResult.Success(Unit)
        }
    }

    @Test
    fun estadoInicialEsInactivo() {
        val repo = FakeLogoutRepository()
        val viewModel = LogoutViewModel(repo)

        val state = viewModel.state.value
        assertFalse(state.isConfirmDialogOpen)
        assertFalse(state.isLoading)
        assertEquals(null, state.userMessage)
    }

    @Test
    fun requestLogoutAbreDialogoDeConfirmacion() {
        val repo = FakeLogoutRepository()
        val viewModel = LogoutViewModel(repo)

        viewModel.onIntent(LogoutIntent.RequestLogout)

        assertTrue(viewModel.state.value.isConfirmDialogOpen)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun dismissDialogCierraDialogoSiNoEstaCargando() {
        val repo = FakeLogoutRepository()
        val viewModel = LogoutViewModel(repo)

        viewModel.onIntent(LogoutIntent.RequestLogout)
        assertTrue(viewModel.state.value.isConfirmDialogOpen)

        viewModel.onIntent(LogoutIntent.DismissDialog)
        assertFalse(viewModel.state.value.isConfirmDialogOpen)
    }

    @Test
    fun confirmLogoutEjecutaRepositorioYEmitEfectoNavegacion() = runTest {
        val repo = FakeLogoutRepository()
        val viewModel = LogoutViewModel(repo)

        val effects = mutableListOf<AuthEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.collect { effects.add(it) }
        }

        viewModel.onIntent(LogoutIntent.RequestLogout)
        viewModel.onIntent(LogoutIntent.ConfirmLogout)

        advanceUntilIdle()

        assertEquals(1, repo.logoutCalls.get())
        assertFalse(viewModel.state.value.isLoading)
        assertFalse(viewModel.state.value.isConfirmDialogOpen)
        assertTrue(effects.contains(AuthEffect.NavigateToLogin))
    }

    @Test
    fun dismissDialogSeIgnoraMientrasIsLoadingEsVerdadero() = runTest {
        val logoutGate = CompletableDeferred<Unit>()
        val repo = FakeLogoutRepository(
            onLogout = { logoutGate.await() }
        )
        val viewModel = LogoutViewModel(repo)

        viewModel.onIntent(LogoutIntent.RequestLogout)
        viewModel.onIntent(LogoutIntent.ConfirmLogout)

        testScheduler.runCurrent()
        assertTrue(viewModel.state.value.isLoading)

        // Intentar descartar mientras está en vuelo
        viewModel.onIntent(LogoutIntent.DismissDialog)
        assertTrue(viewModel.state.value.isConfirmDialogOpen)

        // Liberar la llamada
        logoutGate.complete(Unit)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertFalse(viewModel.state.value.isConfirmDialogOpen)
    }
}
