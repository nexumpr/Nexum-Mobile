package mx.nexum.app.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
import mx.nexum.app.auth.data.ResetPasswordOutcome
import mx.nexum.app.auth.data.dto.LoginRequest
import mx.nexum.app.auth.data.dto.RegisterRequest
import mx.nexum.app.auth.ui.AuthEffect
import mx.nexum.app.auth.ui.ResetPasswordIntent
import mx.nexum.app.auth.ui.ResetPasswordViewModel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ResetPasswordViewModelTest {

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
        var resetPasswordResult: AuthResult<ResetPasswordOutcome> = AuthResult.Success(
            ResetPasswordOutcome(requestId = "req-reset-123")
        )
    ) : AuthRepository {
        var resetPasswordCalls = 0
        var lastTokenReceived: String? = null
        var lastPasswordReceived: String? = null

        override suspend fun register(request: RegisterRequest): AuthResult<RegisterOutcome> = error("no")
        override suspend fun login(request: LoginRequest): AuthResult<LoginOutcome> = error("no")
        override suspend fun refreshSession(): AuthResult<RefreshOutcome> = error("no")
        override suspend fun logout(): AuthResult<Unit> = AuthResult.Success(Unit)

        override suspend fun resetPassword(token: String, newPassword: String): AuthResult<ResetPasswordOutcome> {
            resetPasswordCalls++
            lastTokenReceived = token
            lastPasswordReceived = newPassword
            return resetPasswordResult
        }
    }

    @Test
    fun initialStateIsDefault() {
        val repo = FakeAuthRepository()
        val viewModel = ResetPasswordViewModel(repo)
        val state = viewModel.state.value

        assertEquals("", state.token)
        assertEquals("", state.newPassword)
        assertEquals("", state.confirmPassword)
        assertFalse(state.isPasswordVisible)
        assertFalse(state.isLoading)
        assertNull(state.tokenError)
        assertNull(state.passwordError)
        assertNull(state.confirmPasswordError)
        assertFalse(state.isTokenExpiredOrInvalid)
        assertNull(state.userEmail)
    }

    @Test
    fun initialEmailAndSetUserEmailUpdateState() {
        val repo = FakeAuthRepository()
        val viewModel = ResetPasswordViewModel(repo, initialEmail = "usuario@nexum.mx")
        assertEquals("usuario@nexum.mx", viewModel.state.value.userEmail)

        viewModel.setUserEmail("otro@nexum.mx")
        assertEquals("otro@nexum.mx", viewModel.state.value.userEmail)
    }

    @Test
    fun tokenChangedUpdatesStateAndClearsTokenErrorAndResetExpiredFlag() = runTest {
        val repo = FakeAuthRepository(
            resetPasswordResult = AuthResult.Failure(
                AuthError(
                    kind = AuthErrorKind.INVALID_CREDENTIALS,
                    statusCode = 400,
                    message = "Código expirado"
                )
            )
        )
        val viewModel = ResetPasswordViewModel(repo)
        viewModel.onIntent(ResetPasswordIntent.TokenChanged("token-123456789"))
        viewModel.onIntent(ResetPasswordIntent.PasswordChanged("Password123456!"))
        viewModel.onIntent(ResetPasswordIntent.ConfirmPasswordChanged("Password123456!"))
        viewModel.onIntent(ResetPasswordIntent.Submit)
        runCurrent()

        assertTrue(viewModel.state.value.isTokenExpiredOrInvalid)
        assertEquals("Código expirado", viewModel.state.value.tokenError)

        viewModel.onIntent(ResetPasswordIntent.TokenChanged("nuevo-token-valido"))
        assertEquals("nuevo-token-valido", viewModel.state.value.token)
        assertNull(viewModel.state.value.tokenError)
        assertFalse(viewModel.state.value.isTokenExpiredOrInvalid)
    }

    @Test
    fun passwordAndConfirmPasswordChangedClearTheirErrors() = runTest {
        val repo = FakeAuthRepository()
        val viewModel = ResetPasswordViewModel(repo)

        viewModel.onIntent(ResetPasswordIntent.TokenChanged("token-valido-123"))
        viewModel.onIntent(ResetPasswordIntent.PasswordChanged("corta"))
        viewModel.onIntent(ResetPasswordIntent.ConfirmPasswordChanged("otra"))
        viewModel.onIntent(ResetPasswordIntent.Submit)

        assertNotNull(viewModel.state.value.passwordError)
        assertNotNull(viewModel.state.value.confirmPasswordError)

        viewModel.onIntent(ResetPasswordIntent.PasswordChanged("Password123456!"))
        assertNull(viewModel.state.value.passwordError)
        assertNotNull(viewModel.state.value.confirmPasswordError)

        viewModel.onIntent(ResetPasswordIntent.ConfirmPasswordChanged("Password123456!"))
        assertNull(viewModel.state.value.confirmPasswordError)
    }

    @Test
    fun togglePasswordVisibilityTogglesState() {
        val repo = FakeAuthRepository()
        val viewModel = ResetPasswordViewModel(repo)

        assertFalse(viewModel.state.value.isPasswordVisible)
        viewModel.onIntent(ResetPasswordIntent.TogglePasswordVisibility)
        assertTrue(viewModel.state.value.isPasswordVisible)
        viewModel.onIntent(ResetPasswordIntent.TogglePasswordVisibility)
        assertFalse(viewModel.state.value.isPasswordVisible)
    }

    @Test
    fun submitWithBlankTokenSetsErrorWithoutCallingRepository() = runTest {
        val repo = FakeAuthRepository()
        val viewModel = ResetPasswordViewModel(repo)

        viewModel.onIntent(ResetPasswordIntent.TokenChanged("   "))
        viewModel.onIntent(ResetPasswordIntent.PasswordChanged("Password123456!"))
        viewModel.onIntent(ResetPasswordIntent.ConfirmPasswordChanged("Password123456!"))
        viewModel.onIntent(ResetPasswordIntent.Submit)
        advanceUntilIdle()

        assertEquals("Ingresa el código de recuperación", viewModel.state.value.tokenError)
        assertEquals(0, repo.resetPasswordCalls)
    }

    @Test
    fun submitWithTokenLessThan10CharsSetsErrorWithoutCallingRepository() = runTest {
        val repo = FakeAuthRepository()
        val viewModel = ResetPasswordViewModel(repo)

        viewModel.onIntent(ResetPasswordIntent.TokenChanged("123456789"))
        viewModel.onIntent(ResetPasswordIntent.PasswordChanged("Password123456!"))
        viewModel.onIntent(ResetPasswordIntent.ConfirmPasswordChanged("Password123456!"))
        viewModel.onIntent(ResetPasswordIntent.Submit)
        advanceUntilIdle()

        assertEquals("El código debe tener al menos 10 caracteres", viewModel.state.value.tokenError)
        assertEquals(0, repo.resetPasswordCalls)
    }

    @Test
    fun submitWithInvalidPasswordShorterThan12CharsSetsErrorWithoutCallingRepository() = runTest {
        val repo = FakeAuthRepository()
        val viewModel = ResetPasswordViewModel(repo)

        viewModel.onIntent(ResetPasswordIntent.TokenChanged("token-valido-123"))
        viewModel.onIntent(ResetPasswordIntent.PasswordChanged("Pass123!"))
        viewModel.onIntent(ResetPasswordIntent.ConfirmPasswordChanged("Pass123!"))
        viewModel.onIntent(ResetPasswordIntent.Submit)
        advanceUntilIdle()

        assertEquals("La contraseña debe tener entre 12 y 128 caracteres", viewModel.state.value.passwordError)
        assertEquals(0, repo.resetPasswordCalls)
    }

    @Test
    fun submitWithPasswordLongerThan128CharsSetsErrorWithoutCallingRepository() = runTest {
        val repo = FakeAuthRepository()
        val viewModel = ResetPasswordViewModel(repo)

        val longPassword = "A".repeat(129)
        viewModel.onIntent(ResetPasswordIntent.TokenChanged("token-valido-123"))
        viewModel.onIntent(ResetPasswordIntent.PasswordChanged(longPassword))
        viewModel.onIntent(ResetPasswordIntent.ConfirmPasswordChanged(longPassword))
        viewModel.onIntent(ResetPasswordIntent.Submit)
        advanceUntilIdle()

        assertEquals("La contraseña debe tener entre 12 y 128 caracteres", viewModel.state.value.passwordError)
        assertEquals(0, repo.resetPasswordCalls)
    }

    @Test
    fun submitWithMismatchedConfirmationSetsErrorWithoutCallingRepository() = runTest {
        val repo = FakeAuthRepository()
        val viewModel = ResetPasswordViewModel(repo)

        viewModel.onIntent(ResetPasswordIntent.TokenChanged("token-valido-123"))
        viewModel.onIntent(ResetPasswordIntent.PasswordChanged("Password123456!"))
        viewModel.onIntent(ResetPasswordIntent.ConfirmPasswordChanged("Password654321!"))
        viewModel.onIntent(ResetPasswordIntent.Submit)
        advanceUntilIdle()

        assertEquals("Las contraseñas no coinciden", viewModel.state.value.confirmPasswordError)
        assertEquals(0, repo.resetPasswordCalls)
    }

    @Test
    fun submitSuccessEmitsSnackbarAndNavigatesToLogin() = runTest {
        val repo = FakeAuthRepository(
            resetPasswordResult = AuthResult.Success(ResetPasswordOutcome(requestId = "req-ok-456"))
        )
        val viewModel = ResetPasswordViewModel(repo)
        val effects = mutableListOf<AuthEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.collect { effects += it }
        }

        viewModel.onIntent(ResetPasswordIntent.TokenChanged("  token-valido-123  "))
        viewModel.onIntent(ResetPasswordIntent.PasswordChanged("PasswordNueva123!"))
        viewModel.onIntent(ResetPasswordIntent.ConfirmPasswordChanged("PasswordNueva123!"))
        viewModel.onIntent(ResetPasswordIntent.Submit)
        runCurrent()

        assertEquals(1, repo.resetPasswordCalls)
        assertEquals("token-valido-123", repo.lastTokenReceived)
        assertEquals("PasswordNueva123!", repo.lastPasswordReceived)

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.tokenError)
        assertNull(state.passwordError)
        assertNull(state.confirmPasswordError)
        assertFalse(state.isTokenExpiredOrInvalid)

        assertEquals(2, effects.size)
        assertTrue(effects[0] is AuthEffect.ShowSnackbar)
        val snackbar = effects[0] as AuthEffect.ShowSnackbar
        assertEquals("Contraseña restablecida con éxito. Inicia sesión con tu nueva clave.", snackbar.message)
        assertEquals(AuthEffect.NavigateToLogin, effects[1])
    }

    @Test
    fun submitWith400SetsTokenExpiredOrInvalidAndTokenError() = runTest {
        val repo = FakeAuthRepository(
            resetPasswordResult = AuthResult.Failure(
                AuthError(
                    kind = AuthErrorKind.INVALID_CREDENTIALS,
                    statusCode = 400,
                    message = "El código de recuperación ha expirado o no es válido"
                )
            )
        )
        val viewModel = ResetPasswordViewModel(repo)

        viewModel.onIntent(ResetPasswordIntent.TokenChanged("token-valido-123"))
        viewModel.onIntent(ResetPasswordIntent.PasswordChanged("PasswordNueva123!"))
        viewModel.onIntent(ResetPasswordIntent.ConfirmPasswordChanged("PasswordNueva123!"))
        viewModel.onIntent(ResetPasswordIntent.Submit)
        runCurrent()

        assertEquals(1, repo.resetPasswordCalls)
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertTrue(state.isTokenExpiredOrInvalid)
        assertEquals("El código de recuperación ha expirado o no es válido", state.tokenError)
    }

    @Test
    fun submitWith409SetsTokenExpiredOrInvalidAndConflictMessage() = runTest {
        val repo = FakeAuthRepository(
            resetPasswordResult = AuthResult.Failure(
                AuthError(
                    kind = AuthErrorKind.CONFLICT,
                    statusCode = 409,
                    message = "Código ya utilizado"
                )
            )
        )
        val viewModel = ResetPasswordViewModel(repo)

        viewModel.onIntent(ResetPasswordIntent.TokenChanged("token-valido-123"))
        viewModel.onIntent(ResetPasswordIntent.PasswordChanged("PasswordNueva123!"))
        viewModel.onIntent(ResetPasswordIntent.ConfirmPasswordChanged("PasswordNueva123!"))
        viewModel.onIntent(ResetPasswordIntent.Submit)
        runCurrent()

        assertEquals(1, repo.resetPasswordCalls)
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertTrue(state.isTokenExpiredOrInvalid)
        assertEquals(
            "Este código de recuperación ya fue utilizado. Solicita uno nuevo.",
            state.tokenError
        )
    }

    @Test
    fun submitWith422SetsPasswordError() = runTest {
        val repo = FakeAuthRepository(
            resetPasswordResult = AuthResult.Failure(
                AuthError(
                    kind = AuthErrorKind.VALIDATION,
                    statusCode = 422,
                    message = "La contraseña debe contener al menos un carácter especial"
                )
            )
        )
        val viewModel = ResetPasswordViewModel(repo)

        viewModel.onIntent(ResetPasswordIntent.TokenChanged("token-valido-123"))
        viewModel.onIntent(ResetPasswordIntent.PasswordChanged("PasswordNueva123!"))
        viewModel.onIntent(ResetPasswordIntent.ConfirmPasswordChanged("PasswordNueva123!"))
        viewModel.onIntent(ResetPasswordIntent.Submit)
        runCurrent()

        assertEquals(1, repo.resetPasswordCalls)
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertFalse(state.isTokenExpiredOrInvalid)
        assertEquals("La contraseña debe contener al menos un carácter especial", state.passwordError)
    }

    @Test
    fun submitWithNetworkErrorShowsRetrySnackbar() = runTest {
        val repo = FakeAuthRepository(
            resetPasswordResult = AuthResult.Failure(
                AuthError(
                    kind = AuthErrorKind.NETWORK,
                    message = "Sin conexión a internet. Intenta de nuevo.",
                    retryable = true
                )
            )
        )
        val viewModel = ResetPasswordViewModel(repo)
        val effects = mutableListOf<AuthEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.collect { effects += it }
        }

        viewModel.onIntent(ResetPasswordIntent.TokenChanged("token-valido-123"))
        viewModel.onIntent(ResetPasswordIntent.PasswordChanged("PasswordNueva123!"))
        viewModel.onIntent(ResetPasswordIntent.ConfirmPasswordChanged("PasswordNueva123!"))
        viewModel.onIntent(ResetPasswordIntent.Submit)
        runCurrent()

        assertEquals(1, repo.resetPasswordCalls)
        val state = viewModel.state.value
        assertFalse(state.isLoading)

        assertEquals(1, effects.size)
        assertTrue(effects[0] is AuthEffect.ShowSnackbar)
        val snackbar = effects[0] as AuthEffect.ShowSnackbar
        assertEquals("Sin conexión a internet. Intenta de nuevo.", snackbar.message)
        assertTrue(snackbar.retry)
    }

    @Test
    fun navigateToLoginIntentEmitsNavigateToLoginEffect() = runTest {
        val repo = FakeAuthRepository()
        val viewModel = ResetPasswordViewModel(repo)
        val effects = mutableListOf<AuthEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.collect { effects += it }
        }

        viewModel.onIntent(ResetPasswordIntent.NavigateToLogin)
        advanceUntilIdle()

        assertEquals(listOf(AuthEffect.NavigateToLogin), effects)
    }

    @Test
    fun navigateToForgotPasswordIntentEmitsNavigateToForgotPasswordEffect() = runTest {
        val repo = FakeAuthRepository()
        val viewModel = ResetPasswordViewModel(repo)
        val effects = mutableListOf<AuthEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.collect { effects += it }
        }

        viewModel.onIntent(ResetPasswordIntent.NavigateToForgotPassword)
        advanceUntilIdle()

        assertEquals(listOf(AuthEffect.NavigateToForgotPassword), effects)
    }
}
