package mx.nexum.app.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeAuthRepository : AuthRepository {
        var sessionsResult: AuthResult<SessionsPage> = AuthResult.Success(
            SessionsPage(
                items = listOf(
                    UserSession(
                        id = "sess_1",
                        shortId = "Sesión #sess_1",
                        formattedCreatedAt = "Iniciada: 19/11/2026 10:00",
                        formattedLastSeenAt = "Última actividad: 19/11/2026 20:00",
                        isCurrent = true,
                        isRevoked = false
                    )
                ),
                nextCursor = "cursor_page_2"
            )
        )
        var lastLimit: Int? = null
        var lastCursor: String? = null
        var getSessionsCalls = 0

        override suspend fun register(request: mx.nexum.app.auth.data.dto.RegisterRequest): AuthResult<mx.nexum.app.auth.data.RegisterOutcome> =
            throw NotImplementedError()

        override suspend fun login(request: mx.nexum.app.auth.data.dto.LoginRequest): AuthResult<mx.nexum.app.auth.data.LoginOutcome> =
            throw NotImplementedError()

        override suspend fun refreshSession(): AuthResult<mx.nexum.app.auth.data.RefreshOutcome> =
            throw NotImplementedError()

        override suspend fun getSessions(limit: Int, cursor: String?): AuthResult<SessionsPage> {
            getSessionsCalls++
            lastLimit = limit
            lastCursor = cursor
            return sessionsResult
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
    fun cargaInicialDeSesionesActualizaEstadoExitosamente() = runTest(testDispatcher) {
        val viewModel = SessionsViewModel(fakeRepository)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertEquals(1, state.sessions.size)
        assertEquals("sess_1", state.sessions[0].id)
        assertTrue(state.sessions[0].isCurrent)
        assertEquals("cursor_page_2", state.nextCursor)
        assertEquals(1, fakeRepository.getSessionsCalls)
        assertNull(fakeRepository.lastCursor)
    }

    @Test
    fun errorEnCargaInicialEstableceMensajeDeError() = runTest(testDispatcher) {
        fakeRepository.sessionsResult = AuthResult.Failure(
            AuthError(AuthErrorKind.NETWORK, "Error de red al consultar sesiones.", retryable = true)
        )

        val viewModel = SessionsViewModel(fakeRepository)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("Error de red al consultar sesiones.", state.errorMessage)
        assertTrue(state.sessions.isEmpty())
    }

    @Test
    fun refrescoDeSesionesActualizaListaYQuitaIndicadorDeRefresco() = runTest(testDispatcher) {
        val viewModel = SessionsViewModel(fakeRepository)
        advanceUntilIdle()

        fakeRepository.sessionsResult = AuthResult.Success(
            SessionsPage(
                items = listOf(
                    UserSession(
                        id = "sess_refreshed",
                        shortId = "Sesión #sess_ref",
                        formattedCreatedAt = "Iniciada: 19/11/2026 12:00",
                        formattedLastSeenAt = "Última actividad: 19/11/2026 21:00",
                        isCurrent = true,
                        isRevoked = false
                    )
                ),
                nextCursor = null
            )
        )

        viewModel.onIntent(SessionsIntent.Refresh)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isRefreshing)
        assertEquals(1, state.sessions.size)
        assertEquals("sess_refreshed", state.sessions[0].id)
        assertNull(state.nextCursor)
    }

    @Test
    fun cargarMasSesionesConcatenaResultadosYActualizaCursor() = runTest(testDispatcher) {
        val viewModel = SessionsViewModel(fakeRepository)
        advanceUntilIdle()

        assertEquals("cursor_page_2", viewModel.state.value.nextCursor)

        fakeRepository.sessionsResult = AuthResult.Success(
            SessionsPage(
                items = listOf(
                    UserSession(
                        id = "sess_2",
                        shortId = "Sesión #sess_2",
                        formattedCreatedAt = "Iniciada: 10/11/2026 10:00",
                        formattedLastSeenAt = "Última actividad: 12/11/2026 15:00",
                        isCurrent = false,
                        isRevoked = true
                    )
                ),
                nextCursor = null
            )
        )

        viewModel.onIntent(SessionsIntent.LoadMore)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoadingMore)
        assertEquals(2, state.sessions.size)
        assertEquals("sess_1", state.sessions[0].id)
        assertEquals("sess_2", state.sessions[1].id)
        assertNull(state.nextCursor)
        assertEquals("cursor_page_2", fakeRepository.lastCursor)
    }

    @Test
    fun cargarMasSinCursorNoRealizaPeticion() = runTest(testDispatcher) {
        fakeRepository.sessionsResult = AuthResult.Success(
            SessionsPage(
                items = listOf(
                    UserSession(
                        id = "sess_single",
                        shortId = "Sesión #sess_sin",
                        formattedCreatedAt = "Iniciada: 19/11/2026 10:00",
                        formattedLastSeenAt = "Última actividad: 19/11/2026 20:00",
                        isCurrent = true,
                        isRevoked = false
                    )
                ),
                nextCursor = null
            )
        )

        val viewModel = SessionsViewModel(fakeRepository)
        advanceUntilIdle()

        assertEquals(1, fakeRepository.getSessionsCalls)
        viewModel.onIntent(SessionsIntent.LoadMore)
        advanceUntilIdle()

        assertEquals(1, fakeRepository.getSessionsCalls)
    }

    @Test
    fun descartarErrorLimpiaMensajeDeError() = runTest(testDispatcher) {
        fakeRepository.sessionsResult = AuthResult.Failure(
            AuthError(AuthErrorKind.NETWORK, "Fallo temporal", retryable = true)
        )

        val viewModel = SessionsViewModel(fakeRepository)
        advanceUntilIdle()

        assertEquals("Fallo temporal", viewModel.state.value.errorMessage)

        viewModel.onIntent(SessionsIntent.DismissError)
        advanceUntilIdle()

        assertNull(viewModel.state.value.errorMessage)
    }

    @Test
    fun navegarAtrasEmiteEfectoNavigateToApp() = runTest(testDispatcher) {
        val viewModel = SessionsViewModel(fakeRepository)
        advanceUntilIdle()

        var emittedEffect: AuthEffect? = null
        val job = launch {
            emittedEffect = viewModel.effects.first()
        }

        viewModel.onIntent(SessionsIntent.NavigateBack)
        advanceUntilIdle()

        assertEquals(AuthEffect.NavigateToApp, emittedEffect)
        job.cancel()
    }
}
