package mx.nexum.app.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import mx.nexum.app.auth.data.ActiveSession
import mx.nexum.app.auth.data.AuthError
import mx.nexum.app.auth.data.AuthErrorKind
import mx.nexum.app.auth.data.AuthRepository
import mx.nexum.app.auth.data.AuthResult
import mx.nexum.app.auth.data.SessionsPage
import mx.nexum.app.auth.data.UserSession
import mx.nexum.app.auth.ui.AuthEffect
import mx.nexum.app.auth.ui.SessionsIntent
import mx.nexum.app.auth.ui.SessionsViewModel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class SessionsViewModelRevokeTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeAuthRepository : AuthRepository {
        var revokeResult: AuthResult<Unit> = AuthResult.Success(Unit)
        var sessionsResult: AuthResult<SessionsPage> = AuthResult.Success(
            SessionsPage(
                items = listOf(
                    UserSession(
                        id = "sess_current_1",
                        shortId = "Sesión #sess_cur",
                        formattedCreatedAt = "Iniciada: 19/11/2026 10:00",
                        formattedLastSeenAt = "Última actividad: 19/11/2026 20:00",
                        isCurrent = true,
                        isRevoked = false
                    ),
                    UserSession(
                        id = "sess_remote_2",
                        shortId = "Sesión #sess_rem",
                        formattedCreatedAt = "Iniciada: 15/11/2026 08:30",
                        formattedLastSeenAt = "Última actividad: 18/11/2026 14:20",
                        isCurrent = false,
                        isRevoked = false
                    )
                ),
                nextCursor = null
            )
        )
        var getSessionsCalls = 0
        var revokeCalls = 0
        var lastRevokedSessionId: String? = null
        var lastRevokedIsCurrent: Boolean? = null

        override suspend fun register(request: mx.nexum.app.auth.data.dto.RegisterRequest): AuthResult<mx.nexum.app.auth.data.RegisterOutcome> =
            throw NotImplementedError()

        override suspend fun login(request: mx.nexum.app.auth.data.dto.LoginRequest): AuthResult<mx.nexum.app.auth.data.LoginOutcome> =
            throw NotImplementedError()

        override suspend fun refreshSession(): AuthResult<mx.nexum.app.auth.data.RefreshOutcome> =
            throw NotImplementedError()

        override suspend fun getSessions(limit: Int, cursor: String?): AuthResult<SessionsPage> {
            getSessionsCalls++
            return sessionsResult
        }

        override suspend fun revokeSession(sessionId: String, isCurrent: Boolean): AuthResult<Unit> {
            revokeCalls++
            lastRevokedSessionId = sessionId
            lastRevokedIsCurrent = isCurrent
            return revokeResult
        }
    }

    private lateinit var fakeRepository: FakeAuthRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeAuthRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun requestRevokeEstableceSesionEnEstadoParaMostrarDialogo() = runTest(testDispatcher) {
        val viewModel = SessionsViewModel(fakeRepository)
        advanceUntilIdle()

        val targetSession = fakeRepository.sessionsResult.let { (it as AuthResult.Success).data.items[1] }
        viewModel.onIntent(SessionsIntent.RequestRevoke(targetSession))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertNotNull(state.sessionToRevoke)
        assertEquals("sess_remote_2", state.sessionToRevoke?.id)
    }

    @Test
    fun dismissRevokeDialogLimpiaSesionEnEstadoParaOcultarDialogo() = runTest(testDispatcher) {
        val viewModel = SessionsViewModel(fakeRepository)
        advanceUntilIdle()

        val targetSession = fakeRepository.sessionsResult.let { (it as AuthResult.Success).data.items[1] }
        viewModel.onIntent(SessionsIntent.RequestRevoke(targetSession))
        advanceUntilIdle()
        assertNotNull(viewModel.state.value.sessionToRevoke)

        viewModel.onIntent(SessionsIntent.DismissRevokeDialog)
        advanceUntilIdle()

        assertNull(viewModel.state.value.sessionToRevoke)
    }

    @Test
    fun confirmRevokeSesionRemotaExitosaRecargaSesionesYEmiteSnackbar() = runTest(testDispatcher) {
        val viewModel = SessionsViewModel(fakeRepository)
        advanceUntilIdle()
        assertEquals(1, fakeRepository.getSessionsCalls)

        val targetSession = fakeRepository.sessionsResult.let { (it as AuthResult.Success).data.items[1] }
        viewModel.onIntent(SessionsIntent.RequestRevoke(targetSession))
        advanceUntilIdle()

        val emittedEffects = mutableListOf<AuthEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.collect { emittedEffects.add(it) }
        }

        viewModel.onIntent(SessionsIntent.ConfirmRevoke)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertNull(state.sessionToRevoke)
        assertNull(state.revokingSessionId)
        assertEquals(1, fakeRepository.revokeCalls)
        assertEquals("sess_remote_2", fakeRepository.lastRevokedSessionId)
        assertEquals(false, fakeRepository.lastRevokedIsCurrent)
        // Se debe haber recargado la lista (getSessions)
        assertEquals(2, fakeRepository.getSessionsCalls)
        // Se debe haber emitido Snackbar de éxito
        assertTrue(emittedEffects.any { it is AuthEffect.ShowSnackbar && it.message.contains("Sesión cerrada exitosamente") })
        assertFalse(emittedEffects.contains(AuthEffect.NavigateToLogin))
    }

    @Test
    fun confirmRevokeSesionActualExitosaEmiteNavigateToLoginSinRecargar() = runTest(testDispatcher) {
        val viewModel = SessionsViewModel(fakeRepository)
        advanceUntilIdle()
        assertEquals(1, fakeRepository.getSessionsCalls)

        val currentSession = fakeRepository.sessionsResult.let { (it as AuthResult.Success).data.items[0] }
        viewModel.onIntent(SessionsIntent.RequestRevoke(currentSession))
        advanceUntilIdle()

        val emittedEffects = mutableListOf<AuthEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.collect { emittedEffects.add(it) }
        }

        viewModel.onIntent(SessionsIntent.ConfirmRevoke)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertNull(state.sessionToRevoke)
        assertNull(state.revokingSessionId)
        assertEquals(1, fakeRepository.revokeCalls)
        assertEquals("sess_current_1", fakeRepository.lastRevokedSessionId)
        assertEquals(true, fakeRepository.lastRevokedIsCurrent)
        // No se debe llamar a getSessions (porque el token ya no es válido)
        assertEquals(1, fakeRepository.getSessionsCalls)
        // Se debe haber emitido NavigateToLogin
        assertTrue(emittedEffects.contains(AuthEffect.NavigateToLogin))
    }

    @Test
    fun confirmRevokeFallo404EmiteSnackbarYRecargaSesiones() = runTest(testDispatcher) {
        fakeRepository.revokeResult = AuthResult.Failure(
            AuthError(AuthErrorKind.NOT_FOUND, "La sesión no existe o ya expiró", statusCode = 404)
        )

        val viewModel = SessionsViewModel(fakeRepository)
        advanceUntilIdle()
        assertEquals(1, fakeRepository.getSessionsCalls)

        val targetSession = fakeRepository.sessionsResult.let { (it as AuthResult.Success).data.items[1] }
        viewModel.onIntent(SessionsIntent.RequestRevoke(targetSession))
        advanceUntilIdle()

        val emittedEffects = mutableListOf<AuthEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.collect { emittedEffects.add(it) }
        }

        viewModel.onIntent(SessionsIntent.ConfirmRevoke)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertNull(state.revokingSessionId)
        // Debe recargar sesiones para sincronizar la lista
        assertEquals(2, fakeRepository.getSessionsCalls)
        // Emite Snackbar informando que ya había expirado
        assertTrue(emittedEffects.any { it is AuthEffect.ShowSnackbar && it.message.contains("ya había expirado") })
    }

    @Test
    fun confirmRevokeFallo409ConflictoConcurrenteEstableceErrorYEmiteSnackbarReintentable() = runTest(testDispatcher) {
        fakeRepository.revokeResult = AuthResult.Failure(
            AuthError(AuthErrorKind.CONFLICT, "Conflicto concurrente al cerrar la sesión", retryable = true, statusCode = 409)
        )

        val viewModel = SessionsViewModel(fakeRepository)
        advanceUntilIdle()

        val targetSession = fakeRepository.sessionsResult.let { (it as AuthResult.Success).data.items[1] }
        viewModel.onIntent(SessionsIntent.RequestRevoke(targetSession))
        advanceUntilIdle()

        val emittedEffects = mutableListOf<AuthEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.collect { emittedEffects.add(it) }
        }

        viewModel.onIntent(SessionsIntent.ConfirmRevoke)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertNull(state.revokingSessionId)
        assertEquals("Conflicto concurrente al cerrar la sesión", state.errorMessage)
        assertTrue(emittedEffects.any { it is AuthEffect.ShowSnackbar && it.retry })
    }
}
