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
import mx.nexum.app.auth.data.LoginOutcome
import mx.nexum.app.auth.data.RefreshOutcome
import mx.nexum.app.auth.data.RegisterOutcome
import mx.nexum.app.auth.data.dto.ChangePasswordResponse
import mx.nexum.app.auth.data.dto.LoginRequest
import mx.nexum.app.auth.data.dto.RegisterRequest
import mx.nexum.app.auth.ui.AuthEffect
import mx.nexum.app.auth.ui.ChangePasswordIntent
import mx.nexum.app.auth.ui.ChangePasswordViewModel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChangePasswordViewModelTest {

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
        var changePasswordResult: AuthResult<ChangePasswordResponse> = AuthResult.Success(
            ChangePasswordResponse(status = "accepted", requestId = "chg-test-123")
        )
    ) : AuthRepository {
        var changePasswordCalls = 0
        var lastCurrentPasswordReceived: String? = null
        var lastNewPasswordReceived: String? = null

        override suspend fun register(request: RegisterRequest): AuthResult<RegisterOutcome> = error("no")
        override suspend fun login(request: LoginRequest): AuthResult<LoginOutcome> = error("no")
        override suspend fun refreshSession(): AuthResult<RefreshOutcome> = error("no")
        override suspend fun logout(): AuthResult<Unit> = AuthResult.Success(Unit)

        override suspend fun changePassword(
            currentPassword: String,
            newPassword: String
        ): AuthResult<ChangePasswordResponse> {
            changePasswordCalls++
            lastCurrentPasswordReceived = currentPassword
            lastNewPasswordReceived = newPassword
            return changePasswordResult
        }
    }

    @Test
    fun initialStateIsDefault() {
        val repo = FakeAuthRepository()
        val viewModel = ChangePasswordViewModel(repo)
        val state = viewModel.state.value

        assertEquals("", state.currentPassword)
        assertEquals("", state.newPassword)
        assertEquals("", state.confirmNewPassword)
        assertFalse(state.isCurrentPasswordVisible)
        assertFalse(state.isNewPasswordVisible)
        assertFalse(state.isConfirmNewPasswordVisible)
        assertFalse(state.isLoading)
        assertNull(state.currentPasswordError)
        assertNull(state.newPasswordError)
        assertNull(state.confirmNewPasswordError)
    }

    @Test
    fun currentPasswordChangedUpdatesStateAndClearsError() = runTest {
        val repo = FakeAuthRepository()
        val viewModel = ChangePasswordViewModel(repo)

        viewModel.onIntent(ChangePasswordIntent.Submit)
        assertNotNull(viewModel.state.value.currentPasswordError)

        viewModel.onIntent(ChangePasswordIntent.CurrentPasswordChanged("ClaveActual#2026"))
        assertEquals("ClaveActual#2026", viewModel.state.value.currentPassword)
        assertNull(viewModel.state.value.currentPasswordError)
    }

    @Test
    fun newPasswordChangedUpdatesStateAndClearsError() = runTest {
        val repo = FakeAuthRepository()
        val viewModel = ChangePasswordViewModel(repo)

        viewModel.onIntent(ChangePasswordIntent.CurrentPasswordChanged("ClaveActual#2026"))
        viewModel.onIntent(ChangePasswordIntent.NewPasswordChanged("corta"))
        viewModel.onIntent(ChangePasswordIntent.Submit)
        assertNotNull(viewModel.state.value.newPasswordError)

        viewModel.onIntent(ChangePasswordIntent.NewPasswordChanged("NuevaClaveEjemplo#2026"))
        assertEquals("NuevaClaveEjemplo#2026", viewModel.state.value.newPassword)
        assertNull(viewModel.state.value.newPasswordError)
    }

    @Test
    fun confirmNewPasswordChangedUpdatesStateAndClearsError() = runTest {
        val repo = FakeAuthRepository()
        val viewModel = ChangePasswordViewModel(repo)

        viewModel.onIntent(ChangePasswordIntent.CurrentPasswordChanged("ClaveActual#2026"))
        viewModel.onIntent(ChangePasswordIntent.NewPasswordChanged("NuevaClaveEjemplo#2026"))
        viewModel.onIntent(ChangePasswordIntent.ConfirmNewPasswordChanged("Discrepante#2026"))
        viewModel.onIntent(ChangePasswordIntent.Submit)
        assertNotNull(viewModel.state.value.confirmNewPasswordError)

        viewModel.onIntent(ChangePasswordIntent.ConfirmNewPasswordChanged("NuevaClaveEjemplo#2026"))
        assertEquals("NuevaClaveEjemplo#2026", viewModel.state.value.confirmNewPassword)
        assertNull(viewModel.state.value.confirmNewPasswordError)
    }

    @Test
    fun togglesVisibilityOfEachFieldIndependently() {
        val repo = FakeAuthRepository()
        val viewModel = ChangePasswordViewModel(repo)

        assertFalse(viewModel.state.value.isCurrentPasswordVisible)
        assertFalse(viewModel.state.value.isNewPasswordVisible)
        assertFalse(viewModel.state.value.isConfirmNewPasswordVisible)

        viewModel.onIntent(ChangePasswordIntent.ToggleCurrentPasswordVisibility)
        assertTrue(viewModel.state.value.isCurrentPasswordVisible)
        assertFalse(viewModel.state.value.isNewPasswordVisible)
        assertFalse(viewModel.state.value.isConfirmNewPasswordVisible)

        viewModel.onIntent(ChangePasswordIntent.ToggleNewPasswordVisibility)
        assertTrue(viewModel.state.value.isCurrentPasswordVisible)
        assertTrue(viewModel.state.value.isNewPasswordVisible)
        assertFalse(viewModel.state.value.isConfirmNewPasswordVisible)

        viewModel.onIntent(ChangePasswordIntent.ToggleConfirmNewPasswordVisibility)
        assertTrue(viewModel.state.value.isCurrentPasswordVisible)
        assertTrue(viewModel.state.value.isNewPasswordVisible)
        assertTrue(viewModel.state.value.isConfirmNewPasswordVisible)

        viewModel.onIntent(ChangePasswordIntent.ToggleCurrentPasswordVisibility)
        assertFalse(viewModel.state.value.isCurrentPasswordVisible)
        assertTrue(viewModel.state.value.isNewPasswordVisible)
        assertTrue(viewModel.state.value.isConfirmNewPasswordVisible)
    }

    @Test
    fun submitWithBlankCurrentPasswordSetsErrorWithoutCallingRepo() = runTest {
        val repo = FakeAuthRepository()
        val viewModel = ChangePasswordViewModel(repo)

        viewModel.onIntent(ChangePasswordIntent.CurrentPasswordChanged("   "))
        viewModel.onIntent(ChangePasswordIntent.NewPasswordChanged("NuevaClaveEjemplo#2026"))
        viewModel.onIntent(ChangePasswordIntent.ConfirmNewPasswordChanged("NuevaClaveEjemplo#2026"))
        viewModel.onIntent(ChangePasswordIntent.Submit)
        advanceUntilIdle()

        assertEquals("Ingresa tu contraseña actual", viewModel.state.value.currentPasswordError)
        assertEquals(0, repo.changePasswordCalls)
    }

    @Test
    fun submitWithNewPasswordLessThan12CharsSetsErrorWithoutCallingRepo() = runTest {
        val repo = FakeAuthRepository()
        val viewModel = ChangePasswordViewModel(repo)

        viewModel.onIntent(ChangePasswordIntent.CurrentPasswordChanged("ClaveActual#2026"))
        viewModel.onIntent(ChangePasswordIntent.NewPasswordChanged("Corta#123")) // 9 chars
        viewModel.onIntent(ChangePasswordIntent.ConfirmNewPasswordChanged("Corta#123"))
        viewModel.onIntent(ChangePasswordIntent.Submit)
        advanceUntilIdle()

        assertEquals("La nueva contraseña debe tener al menos 12 caracteres", viewModel.state.value.newPasswordError)
        assertEquals(0, repo.changePasswordCalls)
    }

    @Test
    fun submitWithNewPasswordEqualToCurrentPasswordSetsErrorWithoutCallingRepo() = runTest {
        val repo = FakeAuthRepository()
        val viewModel = ChangePasswordViewModel(repo)

        viewModel.onIntent(ChangePasswordIntent.CurrentPasswordChanged("ClaveActual#2026"))
        viewModel.onIntent(ChangePasswordIntent.NewPasswordChanged("ClaveActual#2026"))
        viewModel.onIntent(ChangePasswordIntent.ConfirmNewPasswordChanged("ClaveActual#2026"))
        viewModel.onIntent(ChangePasswordIntent.Submit)
        advanceUntilIdle()

        assertEquals("La nueva contraseña debe ser distinta a la actual", viewModel.state.value.newPasswordError)
        assertEquals(0, repo.changePasswordCalls)
    }

    @Test
    fun submitWithMismatchedConfirmationSetsErrorWithoutCallingRepo() = runTest {
        val repo = FakeAuthRepository()
        val viewModel = ChangePasswordViewModel(repo)

        viewModel.onIntent(ChangePasswordIntent.CurrentPasswordChanged("ClaveActual#2026"))
        viewModel.onIntent(ChangePasswordIntent.NewPasswordChanged("NuevaClaveEjemplo#2026"))
        viewModel.onIntent(ChangePasswordIntent.ConfirmNewPasswordChanged("OtraClaveDiscrepante#2026"))
        viewModel.onIntent(ChangePasswordIntent.Submit)
        advanceUntilIdle()

        assertEquals("Las contraseñas no coinciden", viewModel.state.value.confirmNewPasswordError)
        assertEquals(0, repo.changePasswordCalls)
    }

    @Test
    fun submitSuccessPurgesPasswordsFromMemoryEmitsSnackbarAndNavigatesToLogin() = runTest {
        val repo = FakeAuthRepository(
            changePasswordResult = AuthResult.Success(
                ChangePasswordResponse(status = "accepted", requestId = "chg-success-789")
            )
        )
        val viewModel = ChangePasswordViewModel(repo)
        val effects = mutableListOf<AuthEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.collect { effects += it }
        }

        viewModel.onIntent(ChangePasswordIntent.CurrentPasswordChanged("ClaveActual#2026"))
        viewModel.onIntent(ChangePasswordIntent.NewPasswordChanged("NuevaClaveEjemplo#2026"))
        viewModel.onIntent(ChangePasswordIntent.ConfirmNewPasswordChanged("NuevaClaveEjemplo#2026"))
        viewModel.onIntent(ChangePasswordIntent.Submit)
        runCurrent()

        assertEquals(1, repo.changePasswordCalls)
        assertEquals("ClaveActual#2026", repo.lastCurrentPasswordReceived)
        assertEquals("NuevaClaveEjemplo#2026", repo.lastNewPasswordReceived)

        // Seguridad de memoria (MO-PR06, MO-PR10, E007):
        // Todos los campos de contraseña deben ser purgados de la memoria inmediatamente tras éxito.
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("", state.currentPassword)
        assertEquals("", state.newPassword)
        assertEquals("", state.confirmNewPassword)
        assertNull(state.currentPasswordError)
        assertNull(state.newPasswordError)
        assertNull(state.confirmNewPasswordError)

        // Verificación de efectos: Snackbar de confirmación y redirección obligatoria a Login
        assertEquals(2, effects.size)
        assertTrue(effects[0] is AuthEffect.ShowSnackbar)
        val snackbar = effects[0] as AuthEffect.ShowSnackbar
        assertEquals("Contraseña cambiada con éxito. Inicia sesión con tu nueva clave.", snackbar.message)
        assertEquals(AuthEffect.NavigateToLogin, effects[1])
    }

    @Test
    fun submitWith401CurrentPasswordIncorrectSetsCurrentPasswordErrorAndRetainsFields() = runTest {
        val repo = FakeAuthRepository(
            changePasswordResult = AuthResult.Failure(
                AuthError(
                    kind = AuthErrorKind.INVALID_CREDENTIALS,
                    statusCode = 401,
                    message = "La contraseña actual no es correcta."
                )
            )
        )
        val viewModel = ChangePasswordViewModel(repo)

        viewModel.onIntent(ChangePasswordIntent.CurrentPasswordChanged("Erronea#123"))
        viewModel.onIntent(ChangePasswordIntent.NewPasswordChanged("NuevaClaveEjemplo#2026"))
        viewModel.onIntent(ChangePasswordIntent.ConfirmNewPasswordChanged("NuevaClaveEjemplo#2026"))
        viewModel.onIntent(ChangePasswordIntent.Submit)
        runCurrent()

        assertEquals(1, repo.changePasswordCalls)
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("La contraseña actual no es correcta.", state.currentPasswordError)
        // En caso de error se conservan para que el usuario pueda corregirlos
        assertEquals("Erronea#123", state.currentPassword)
        assertEquals("NuevaClaveEjemplo#2026", state.newPassword)
    }

    @Test
    fun submitWith409NewPasswordSameAsCurrentSetsNewPasswordError() = runTest {
        val repo = FakeAuthRepository(
            changePasswordResult = AuthResult.Failure(
                AuthError(
                    kind = AuthErrorKind.CONFLICT,
                    statusCode = 409,
                    message = "La nueva contraseña no puede ser igual a la actual."
                )
            )
        )
        val viewModel = ChangePasswordViewModel(repo)

        viewModel.onIntent(ChangePasswordIntent.CurrentPasswordChanged("ClaveActual#2026"))
        viewModel.onIntent(ChangePasswordIntent.NewPasswordChanged("NuevaClaveEjemplo#2026"))
        viewModel.onIntent(ChangePasswordIntent.ConfirmNewPasswordChanged("NuevaClaveEjemplo#2026"))
        viewModel.onIntent(ChangePasswordIntent.Submit)
        runCurrent()

        assertEquals(1, repo.changePasswordCalls)
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("La nueva contraseña no puede ser igual a la actual.", state.newPasswordError)
    }

    @Test
    fun submitWith422ValidationSetsNewPasswordError() = runTest {
        val repo = FakeAuthRepository(
            changePasswordResult = AuthResult.Failure(
                AuthError(
                    kind = AuthErrorKind.VALIDATION,
                    statusCode = 422,
                    message = "La nueva contraseña debe tener al menos 12 caracteres."
                )
            )
        )
        val viewModel = ChangePasswordViewModel(repo)

        viewModel.onIntent(ChangePasswordIntent.CurrentPasswordChanged("ClaveActual#2026"))
        viewModel.onIntent(ChangePasswordIntent.NewPasswordChanged("NuevaClaveEjemplo#2026"))
        viewModel.onIntent(ChangePasswordIntent.ConfirmNewPasswordChanged("NuevaClaveEjemplo#2026"))
        viewModel.onIntent(ChangePasswordIntent.Submit)
        runCurrent()

        assertEquals(1, repo.changePasswordCalls)
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("La nueva contraseña debe tener al menos 12 caracteres.", state.newPasswordError)
    }

    @Test
    fun submitWithNetworkErrorEmitsRetrySnackbar() = runTest {
        val repo = FakeAuthRepository(
            changePasswordResult = AuthResult.Failure(
                AuthError(
                    kind = AuthErrorKind.NETWORK,
                    message = "Sin conexión a internet. Intenta de nuevo.",
                    retryable = true
                )
            )
        )
        val viewModel = ChangePasswordViewModel(repo)
        val effects = mutableListOf<AuthEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.collect { effects += it }
        }

        viewModel.onIntent(ChangePasswordIntent.CurrentPasswordChanged("ClaveActual#2026"))
        viewModel.onIntent(ChangePasswordIntent.NewPasswordChanged("NuevaClaveEjemplo#2026"))
        viewModel.onIntent(ChangePasswordIntent.ConfirmNewPasswordChanged("NuevaClaveEjemplo#2026"))
        viewModel.onIntent(ChangePasswordIntent.Submit)
        runCurrent()

        assertEquals(1, repo.changePasswordCalls)
        val state = viewModel.state.value
        assertFalse(state.isLoading)

        assertEquals(1, effects.size)
        assertTrue(effects[0] is AuthEffect.ShowSnackbar)
        val snackbar = effects[0] as AuthEffect.ShowSnackbar
        assertEquals("Sin conexión a internet. Intenta de nuevo.", snackbar.message)
        assertTrue(snackbar.retry)
    }

    @Test
    fun navigateBackIntentEmitsNavigateToAppEffect() = runTest {
        val repo = FakeAuthRepository()
        val viewModel = ChangePasswordViewModel(repo)
        val effects = mutableListOf<AuthEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.collect { effects += it }
        }

        viewModel.onIntent(ChangePasswordIntent.NavigateBack)
        advanceUntilIdle()

        assertEquals(listOf(AuthEffect.NavigateToApp), effects)
    }
}
