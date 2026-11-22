package mx.nexum.app.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import mx.nexum.app.auth.data.AuthError
import mx.nexum.app.auth.data.AuthErrorKind
import mx.nexum.app.auth.data.AuthRepository
import mx.nexum.app.auth.data.AuthResult
import mx.nexum.app.auth.data.ConfirmEmailVerificationOutcome
import mx.nexum.app.auth.data.InMemoryTokenStorage
import mx.nexum.app.auth.data.LoginOutcome
import mx.nexum.app.auth.data.RefreshOutcome
import mx.nexum.app.auth.data.RegisterOutcome
import mx.nexum.app.auth.data.Session
import mx.nexum.app.auth.data.dto.LoginRequest
import mx.nexum.app.auth.data.dto.RegisterRequest
import mx.nexum.app.auth.data.dto.User
import mx.nexum.app.auth.ui.AuthEffect
import mx.nexum.app.auth.ui.ConfirmEmailVerificationIntent
import mx.nexum.app.auth.ui.ConfirmEmailVerificationViewModel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConfirmEmailVerificationViewModelTest {

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
        var result: AuthResult<ConfirmEmailVerificationOutcome> = AuthResult.Success(
            ConfirmEmailVerificationOutcome(status = "accepted", requestId = "req-confirm-synthetic")
        )
    ) : AuthRepository {
        var calls = 0
        var lastToken: String? = null

        override suspend fun register(request: RegisterRequest): AuthResult<RegisterOutcome> = error("no")
        override suspend fun login(request: LoginRequest): AuthResult<LoginOutcome> = error("no")
        override suspend fun refreshSession(): AuthResult<RefreshOutcome> = error("no")

        override suspend fun confirmEmailVerification(token: String): AuthResult<ConfirmEmailVerificationOutcome> {
            calls++
            lastToken = token
            return result
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

    private fun failure(kind: AuthErrorKind, message: String, retryable: Boolean = false) = AuthResult.Failure(
        AuthError(kind = kind, message = message, retryable = retryable, statusCode = when (kind) {
            AuthErrorKind.INVALID_TOKEN -> 400
            AuthErrorKind.CONFLICT -> 409
            else -> null
        })
    )

    private fun effectsOf(viewModel: ConfirmEmailVerificationViewModel, testScope: TestScope): MutableList<AuthEffect> {
        val effects = mutableListOf<AuthEffect>()
        testScope.backgroundScope.launch(UnconfinedTestDispatcher(testDispatcher.scheduler)) {
            viewModel.effects.collect { effects.add(it) }
        }
        return effects
    }

    @Test
    fun tokenChangedGuardaElValorLiteral() {
        val viewModel = ConfirmEmailVerificationViewModel(FakeAuthRepository(), storage())
        val token = "  AbC123XY_z  "

        viewModel.onIntent(ConfirmEmailVerificationIntent.TokenChanged(token))

        assertEquals(token, viewModel.state.value.token)
        assertFalse(viewModel.state.value.isTokenExpiredOrInvalid)
        assertFalse(viewModel.state.value.isTokenAlreadyUsed)
        assertNull(viewModel.state.value.errorMessage)
    }

    @Test
    fun tokenCortoNoDisparaConfirmacion() = runTest(testDispatcher) {
        val repository = FakeAuthRepository()
        val viewModel = ConfirmEmailVerificationViewModel(repository, storage())

        viewModel.onIntent(ConfirmEmailVerificationIntent.TokenChanged("corto"))
        viewModel.onIntent(ConfirmEmailVerificationIntent.Submit)
        advanceUntilIdle()

        assertEquals(0, repository.calls)
        assertFalse(viewModel.state.value.isConfirmed)
    }

    @Test
    fun confirmacionExitosaConSesionNavegaAApp() = runTest(testDispatcher) {
        val repository = FakeAuthRepository()
        val viewModel = ConfirmEmailVerificationViewModel(repository, storage())
        val effects = effectsOf(viewModel, this)
        viewModel.onIntent(ConfirmEmailVerificationIntent.TokenChanged("  AbC123XY_z  "))

        viewModel.onIntent(ConfirmEmailVerificationIntent.Submit)
        advanceUntilIdle()

        assertEquals(1, repository.calls)
        assertEquals("  AbC123XY_z  ", repository.lastToken)
        assertTrue(viewModel.state.value.isConfirmed)
        assertEquals("req-confirm-synthetic", viewModel.state.value.requestId)
        assertFalse(viewModel.state.value.isLoading)
        assertTrue(effects.contains(AuthEffect.NavigateToApp))
        assertFalse(effects.contains(AuthEffect.NavigateToLogin))
    }

    @Test
    fun confirmacionExitosaSinSesionNavegaALogin() = runTest(testDispatcher) {
        val repository = FakeAuthRepository()
        val viewModel = ConfirmEmailVerificationViewModel(repository, storage(withSession = false))
        val effects = effectsOf(viewModel, this)

        viewModel.onIntent(ConfirmEmailVerificationIntent.TokenChanged("valid_token_123"))
        viewModel.onIntent(ConfirmEmailVerificationIntent.Submit)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isConfirmed)
        assertTrue(effects.contains(AuthEffect.NavigateToLogin))
        assertFalse(effects.contains(AuthEffect.NavigateToApp))
    }

    @Test
    fun tokenExpiradoHabilitaSolicitudDeReenvio() = runTest(testDispatcher) {
        val repository = FakeAuthRepository(
            failure(AuthErrorKind.INVALID_TOKEN, "El código de verificación es inválido o ha expirado")
        )
        val viewModel = ConfirmEmailVerificationViewModel(repository, storage())

        viewModel.onIntent(ConfirmEmailVerificationIntent.TokenChanged("expired_verification_token"))
        viewModel.onIntent(ConfirmEmailVerificationIntent.Submit)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isTokenExpiredOrInvalid)
        assertFalse(viewModel.state.value.isTokenAlreadyUsed)
        assertFalse(viewModel.state.value.isConfirmed)
        assertEquals("El código de verificación es inválido o ha expirado", viewModel.state.value.errorMessage)
    }

    @Test
    fun tokenYaUsadoSeDiferenciaDelExpirado() = runTest(testDispatcher) {
        val repository = FakeAuthRepository(
            failure(AuthErrorKind.CONFLICT, "El código de verificación ya fue utilizado")
        )
        val viewModel = ConfirmEmailVerificationViewModel(repository, storage())

        viewModel.onIntent(ConfirmEmailVerificationIntent.TokenChanged("used_verification_token"))
        viewModel.onIntent(ConfirmEmailVerificationIntent.Submit)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isTokenAlreadyUsed)
        assertFalse(viewModel.state.value.isTokenExpiredOrInvalid)
        assertFalse(viewModel.state.value.isConfirmed)
    }

    @Test
    fun errorDeRedMuestraSnackbarReintentable() = runTest(testDispatcher) {
        val repository = FakeAuthRepository(
            failure(AuthErrorKind.NETWORK, "Sin conexión. Intenta de nuevo.", retryable = true)
        )
        val viewModel = ConfirmEmailVerificationViewModel(repository, storage())
        val effects = effectsOf(viewModel, this)

        viewModel.onIntent(ConfirmEmailVerificationIntent.TokenChanged("network_token_123"))
        viewModel.onIntent(ConfirmEmailVerificationIntent.Submit)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isConfirmed)
        assertEquals("Sin conexión. Intenta de nuevo.", viewModel.state.value.errorMessage)
        assertTrue(
            effects.any { it is AuthEffect.ShowSnackbar && it.message == "Sin conexión. Intenta de nuevo." && it.retry }
        )
    }

    @Test
    fun intencionesDeNavegacionEmitenEfectos() = runTest(testDispatcher) {
        val viewModel = ConfirmEmailVerificationViewModel(FakeAuthRepository(), storage())
        val effects = effectsOf(viewModel, this)

        viewModel.onIntent(ConfirmEmailVerificationIntent.NavigateToLogin)
        viewModel.onIntent(ConfirmEmailVerificationIntent.NavigateToRequestVerification)
        advanceUntilIdle()

        assertTrue(effects.contains(AuthEffect.NavigateToLogin))
        assertTrue(effects.contains(AuthEffect.NavigateToRequestVerification))
    }
}
