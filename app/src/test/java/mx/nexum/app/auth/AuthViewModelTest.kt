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
import mx.nexum.app.auth.data.dto.LoginRequest
import mx.nexum.app.auth.data.dto.RegisterRequest
import mx.nexum.app.auth.ui.AuthEffect
import mx.nexum.app.auth.ui.RegisterIntent
import mx.nexum.app.auth.ui.AuthViewModel
import mx.nexum.app.auth.ui.ExpenseDraft
import mx.nexum.app.auth.ui.RegisterUiState
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private class FakeAuthRepository(
        private val registerResult: AuthResult<RegisterOutcome>
    ) : AuthRepository {
        var registerCalls = 0
        var lastRequest: RegisterRequest? = null

        override suspend fun register(request: RegisterRequest): AuthResult<RegisterOutcome> {
            registerCalls += 1
            lastRequest = request
            return registerResult
        }

        override suspend fun login(request: LoginRequest): AuthResult<LoginOutcome> =
            AuthResult.Failure(AuthError(AuthErrorKind.UNKNOWN, "no usado"))
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun successRepository() = FakeAuthRepository(
        AuthResult.Success(RegisterOutcome("user-1", "pending", false))
    )

    private fun conflictRepository() = FakeAuthRepository(
        AuthResult.Failure(AuthError(AuthErrorKind.CONFLICT, "El correo ya está registrado"))
    )

    private fun networkRepository() = FakeAuthRepository(
        AuthResult.Failure(AuthError(AuthErrorKind.NETWORK, "Sin conexión. Intenta de nuevo.", retryable = true))
    )

    private fun AuthViewModel.fillValidForm() {
        onIntent(RegisterIntent.NameChanged("Alex"))
        onIntent(RegisterIntent.EmailChanged("alex@example.com"))
        onIntent(RegisterIntent.PasswordChanged("Password123456"))
        onIntent(RegisterIntent.ConfirmPasswordChanged("Password123456"))
        onIntent(RegisterIntent.IncomeAmountChanged("12000.00"))
        onIntent(
            RegisterIntent.ExpenseChanged(
                0,
                ExpenseDraft(concept = "Renta", amount = "4000.00", categoryId = "vivienda")
            )
        )
        onIntent(RegisterIntent.TermsAccepted(true))
    }

    @Test
    fun registroExitosoEmiteNavegacionALogin() = runTest {
        val repository = successRepository()
        val viewModel = AuthViewModel(
            repository = repository,
            privacyNoticeVersion = "2026-11-v1",
            clockIso = { "2026-11-02T17:30:00Z" }
        )
        val effects = mutableListOf<AuthEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.effects.collect { effects += it } }

        viewModel.fillValidForm()
        viewModel.onIntent(RegisterIntent.Submit)
        advanceUntilIdle()

        assertEquals(listOf<AuthEffect>(AuthEffect.NavigateToLogin), effects)
        assertFalse(viewModel.state.value.isLoading)
        assertEquals(1, repository.registerCalls)
    }

    @Test
    fun registroGeneraPayloadDelContratoE001() = runTest {
        val repository = successRepository()
        val viewModel = AuthViewModel(
            repository = repository,
            privacyNoticeVersion = "2026-11-v1",
            clockIso = { "2026-11-02T17:30:00Z" }
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.effects.collect {} }

        viewModel.fillValidForm()
        viewModel.onIntent(RegisterIntent.Submit)
        advanceUntilIdle()

        val request = repository.lastRequest
        assertNotNull(request)
        assertEquals("Alex", request!!.name)
        assertEquals("alex@example.com", request.email)
        assertEquals("12000.00", request.income.amount)
        assertEquals(1, request.recurringExpenses.size)
        assertEquals("Renta", request.recurringExpenses[0].concept)
        assertEquals("4000.00", request.recurringExpenses[0].amount)
        assertEquals("vivienda", request.recurringExpenses[0].categoryId)
        assertEquals("2026-11-v1", request.privacyNoticeVersion)
        assertEquals("2026-11-02T17:30:00Z", request.consentAcceptedAt)
    }

    @Test
    fun sinTerminosNoEnviaYMarcaError() = runTest {
        val repository = successRepository()
        val viewModel = AuthViewModel(repository = repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.effects.collect {} }

        viewModel.fillValidForm()
        viewModel.onIntent(RegisterIntent.TermsAccepted(false))
        viewModel.onIntent(RegisterIntent.Submit)
        advanceUntilIdle()

        assertEquals(0, repository.registerCalls)
        assertNotNull(viewModel.state.value.termsError)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun emailInvalidoNoEnviaYMarcaError() = runTest {
        val repository = successRepository()
        val viewModel = AuthViewModel(repository = repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.effects.collect {} }

        viewModel.fillValidForm()
        viewModel.onIntent(RegisterIntent.EmailChanged("correo-malo"))
        viewModel.onIntent(RegisterIntent.Submit)
        advanceUntilIdle()

        assertEquals(0, repository.registerCalls)
        assertNotNull(viewModel.state.value.emailError)
    }

    @Test
    fun gastoFaltanteNoEnviaYMarcaError() = runTest {
        val repository = successRepository()
        val viewModel = AuthViewModel(repository = repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.effects.collect {} }

        viewModel.fillValidForm()
        viewModel.onIntent(RegisterIntent.ExpenseChanged(0, ExpenseDraft()))
        viewModel.onIntent(RegisterIntent.Submit)
        advanceUntilIdle()

        assertEquals(0, repository.registerCalls)
        assertNotNull(viewModel.state.value.expensesError)
    }

    @Test
    fun conflictoActualizaErrorYSnackbar() = runTest {
        val viewModel = AuthViewModel(repository = conflictRepository())
        val effects = mutableListOf<AuthEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.effects.collect { effects += it } }

        viewModel.fillValidForm()
        viewModel.onIntent(RegisterIntent.Submit)
        advanceUntilIdle()

        assertEquals("El correo ya está registrado", viewModel.state.value.formError)
        assertEquals(
            listOf<AuthEffect>(AuthEffect.ShowSnackbar("El correo ya está registrado", false)),
            effects
        )
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun errorDeRedEmiteSnackbarReintentable() = runTest {
        val viewModel = AuthViewModel(repository = networkRepository())
        val effects = mutableListOf<AuthEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.effects.collect { effects += it } }

        viewModel.fillValidForm()
        viewModel.onIntent(RegisterIntent.Submit)
        advanceUntilIdle()

        assertEquals("Sin conexión. Intenta de nuevo.", viewModel.state.value.formError)
        assertEquals(
            listOf<AuthEffect>(AuthEffect.ShowSnackbar("Sin conexión. Intenta de nuevo.", true)),
            effects
        )
    }

    @Test
    fun agregarYEliminarGastoMantieneAlMenosUno() = runTest {
        val viewModel = AuthViewModel(repository = successRepository())
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.effects.collect {} }

        viewModel.onIntent(RegisterIntent.AddExpense)
        assertEquals(2, viewModel.state.value.expenses.size)

        viewModel.onIntent(RegisterIntent.RemoveExpense(1))
        assertEquals(1, viewModel.state.value.expenses.size)

        viewModel.onIntent(RegisterIntent.RemoveExpense(0))
        assertEquals(1, viewModel.state.value.expenses.size)
    }

    @Test
    fun cambiarCampoLimpiaSuError() = runTest {
        val viewModel = AuthViewModel(repository = successRepository())
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.effects.collect {} }

        viewModel.onIntent(RegisterIntent.Submit)
        advanceUntilIdle()
        assertNotNull(viewModel.state.value.emailError)

        viewModel.onIntent(RegisterIntent.EmailChanged("alex@example.com"))
        assertNull(viewModel.state.value.emailError)
    }

    @Test
    fun estadoInicialEsInmutableYVacio() = runTest {
        val state: RegisterUiState = AuthViewModel(successRepository()).state.value
        assertEquals("", state.name)
        assertFalse(state.termsAccepted)
        assertFalse(state.isLoading)
        assertEquals(1, state.expenses.size)
    }
}
