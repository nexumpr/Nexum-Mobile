package mx.nexum.app.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import mx.nexum.app.auth.data.AuthError
import mx.nexum.app.auth.data.AuthErrorKind
import mx.nexum.app.auth.data.AuthRepository
import mx.nexum.app.auth.data.AuthResult
import mx.nexum.app.auth.data.LoginOutcome
import mx.nexum.app.auth.data.RegisterOutcome
import mx.nexum.app.auth.data.Session
import mx.nexum.app.auth.data.dto.LoginRequest
import mx.nexum.app.auth.data.dto.RegisterRequest
import mx.nexum.app.auth.data.dto.User
import mx.nexum.app.auth.ui.AuthEffect
import mx.nexum.app.auth.ui.LoginIntent
import mx.nexum.app.auth.ui.LoginViewModel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private class FakeAuthRepository(
        private val loginResult: AuthResult<LoginOutcome>
    ) : AuthRepository {
        var loginCalls = 0
        var lastRequest: LoginRequest? = null

        override suspend fun register(request: RegisterRequest): AuthResult<RegisterOutcome> =
            AuthResult.Failure(AuthError(AuthErrorKind.UNKNOWN, "no usado"))

        override suspend fun login(request: LoginRequest): AuthResult<LoginOutcome> {
            loginCalls += 1
            lastRequest = request
            return loginResult
        }
    }

    private fun session() = Session(
        accessToken = "access",
        refreshToken = "refresh",
        expiresIn = 3600,
        user = User("user-1", "Alex", "alex@example.com")
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun successRepository() = FakeAuthRepository(AuthResult.Success(LoginOutcome(session())))

    private fun invalidCredentialsRepository(attemptsRemaining: Int? = 4) = FakeAuthRepository(
        AuthResult.Failure(
            AuthError(
                kind = AuthErrorKind.INVALID_CREDENTIALS,
                message = "Correo o contraseña incorrectos",
                attemptsRemaining = attemptsRemaining
            )
        )
    )

    private fun LoginViewModel.fillCredentials() {
        onIntent(LoginIntent.EmailChanged("alex@example.com"))
        onIntent(LoginIntent.PasswordChanged("Password1"))
    }

    @Test
    fun loginExitosoEmiteNavegacionAlNucleo() = runTest {
        val repository = successRepository()
        val viewModel = LoginViewModel(repository = repository, deviceName = "Pixel Test")
        val effects = mutableListOf<AuthEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.effects.collect { effects += it } }

        viewModel.fillCredentials()
        viewModel.onIntent(LoginIntent.Submit)
        advanceUntilIdle()

        assertEquals(listOf<AuthEffect>(AuthEffect.NavigateToApp), effects)
        assertFalse(viewModel.state.value.isLoading)
        assertEquals(1, repository.loginCalls)
        assertEquals("alex@example.com", repository.lastRequest?.email)
        assertEquals("Pixel Test", repository.lastRequest?.deviceName)
    }

    @Test
    fun credencialesInvalidasMuestranMensajeGenerico() = runTest {
        val viewModel = LoginViewModel(repository = invalidCredentialsRepository())
        val effects = mutableListOf<AuthEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.effects.collect { effects += it } }

        viewModel.fillCredentials()
        viewModel.onIntent(LoginIntent.Submit)
        advanceUntilIdle()

        assertEquals("Correo o contraseña incorrectos", viewModel.state.value.formError)
        assertEquals(4, viewModel.state.value.attemptsRemaining)
        assertEquals(
            listOf<AuthEffect>(AuthEffect.ShowSnackbar("Correo o contraseña incorrectos", false)),
            effects
        )
    }

    @Test
    fun camposVaciosNoEnvianYMarcanError() = runTest {
        val repository = successRepository()
        val viewModel = LoginViewModel(repository = repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.effects.collect {} }

        viewModel.onIntent(LoginIntent.Submit)
        advanceUntilIdle()

        assertEquals(0, repository.loginCalls)
        assertNotNull(viewModel.state.value.emailError)
        assertNotNull(viewModel.state.value.passwordError)
    }

    @Test
    fun emailInvalidoNoEnvia() = runTest {
        val repository = successRepository()
        val viewModel = LoginViewModel(repository = repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.effects.collect {} }

        viewModel.onIntent(LoginIntent.EmailChanged("no-es-correo"))
        viewModel.onIntent(LoginIntent.PasswordChanged("Password1"))
        viewModel.onIntent(LoginIntent.Submit)
        advanceUntilIdle()

        assertEquals(0, repository.loginCalls)
        assertNotNull(viewModel.state.value.emailError)
        assertNull(viewModel.state.value.passwordError)
    }

    @Test
    fun limiteDeIntentosActualizaContador() = runTest {
        val viewModel = LoginViewModel(
            repository = FakeAuthRepository(
                AuthResult.Failure(
                    AuthError(
                        kind = AuthErrorKind.RATE_LIMITED,
                        message = "Demasiados intentos. Intenta más tarde.",
                        attemptsRemaining = 0
                    )
                )
            )
        )
        val effects = mutableListOf<AuthEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.effects.collect { effects += it } }

        viewModel.fillCredentials()
        viewModel.onIntent(LoginIntent.Submit)
        advanceUntilIdle()

        assertEquals(0, viewModel.state.value.attemptsRemaining)
        assertEquals(
            listOf<AuthEffect>(AuthEffect.ShowSnackbar("Demasiados intentos. Intenta más tarde.", false)),
            effects
        )
    }

    @Test
    fun errorDeRedEmiteSnackbarReintentable() = runTest {
        val viewModel = LoginViewModel(
            repository = FakeAuthRepository(
                AuthResult.Failure(
                    AuthError(
                        kind = AuthErrorKind.NETWORK,
                        message = "Sin conexión. Intenta de nuevo.",
                        retryable = true
                    )
                )
            )
        )
        val effects = mutableListOf<AuthEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.effects.collect { effects += it } }

        viewModel.fillCredentials()
        viewModel.onIntent(LoginIntent.Submit)
        advanceUntilIdle()

        assertEquals("Sin conexión. Intenta de nuevo.", viewModel.state.value.formError)
        assertEquals(
            listOf<AuthEffect>(AuthEffect.ShowSnackbar("Sin conexión. Intenta de nuevo.", true)),
            effects
        )
    }

    @Test
    fun enlaceRegistroEmiteEfectoDeNavegacion() = runTest {
        val viewModel = LoginViewModel(repository = successRepository())
        val effects = mutableListOf<AuthEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.effects.collect { effects += it } }

        viewModel.onIntent(LoginIntent.NavigateToRegister)
        advanceUntilIdle()

        assertEquals(listOf<AuthEffect>(AuthEffect.NavigateToRegister), effects)
    }

    @Test
    fun enlaceOlvidePasswordEmiteAvisoInformativo() = runTest {
        val viewModel = LoginViewModel(repository = successRepository())
        val effects = mutableListOf<AuthEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.effects.collect { effects += it } }

        viewModel.onIntent(LoginIntent.NavigateToForgotPassword)
        advanceUntilIdle()

        assertEquals(1, effects.size)
        assertTrue(effects[0] is AuthEffect.ShowSnackbar)
        assertFalse((effects[0] as AuthEffect.ShowSnackbar).retry)
    }

    @Test
    fun cambiarCampoLimpiaSuError() = runTest {
        val viewModel = LoginViewModel(repository = successRepository())
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.effects.collect {} }

        viewModel.onIntent(LoginIntent.Submit)
        advanceUntilIdle()
        assertNotNull(viewModel.state.value.emailError)

        viewModel.onIntent(LoginIntent.EmailChanged("alex@example.com"))
        assertNull(viewModel.state.value.emailError)
    }
}
