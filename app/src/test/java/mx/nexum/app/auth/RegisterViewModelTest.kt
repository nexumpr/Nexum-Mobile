package mx.nexum.app.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import mx.nexum.app.auth.data.dto.RegisterRequestDto
import mx.nexum.app.auth.data.dto.RegisterResponseDto
import mx.nexum.app.auth.data.network.AuthApiService
import mx.nexum.app.auth.ui.AuthEffect
import mx.nexum.app.auth.ui.ExpenseDraft
import mx.nexum.app.auth.ui.RegisterIntent
import mx.nexum.app.auth.ui.RegisterViewModel
import okhttp3.Headers
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {

    private class FakeAuthApiService : AuthApiService {
        var registerCalls = 0
        var sentIdempotencyKey: String? = null
        var sentRequest: RegisterRequestDto? = null
        var responseToReturn: Response<RegisterResponseDto> = Response.success(201, RegisterResponseDto(userId = "user-123"))
        var exceptionToThrow: Throwable? = null

        override suspend fun register(
            idempotencyKey: String,
            request: RegisterRequestDto
        ): Response<RegisterResponseDto> {
            registerCalls++
            sentIdempotencyKey = idempotencyKey
            sentRequest = request
            exceptionToThrow?.let { throw it }
            return responseToReturn
        }
    }

    private lateinit var fakeApi: FakeAuthApiService

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fakeApi = FakeAuthApiService()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        api: AuthApiService = fakeApi,
        locale: String = "es-MX",
        timeZone: String = "America/Mexico_City"
    ) = RegisterViewModel(
        apiService = api,
        localeProvider = { locale },
        timeZoneProvider = { timeZone }
    )

    private fun RegisterViewModel.fillValidForm(
        password: String = "ExamplePassphrase#2026",
        confirmPassword: String = "ExamplePassphrase#2026",
        withExpenses: Boolean = false
    ) {
        onIntent(RegisterIntent.NameChanged("Usuario Prueba"))
        onIntent(RegisterIntent.EmailChanged("prueba.movil001@example.com"))
        onIntent(RegisterIntent.PasswordChanged(password))
        onIntent(RegisterIntent.ConfirmPasswordChanged(confirmPassword))
        onIntent(RegisterIntent.IncomeAmountChanged("15000.00"))
        if (withExpenses) {
            onIntent(RegisterIntent.AddExpense)
            onIntent(
                RegisterIntent.ExpenseChanged(
                    0,
                    ExpenseDraft(
                        concept = "Internet",
                        amount = "650.00",
                        category = "Servicios",
                        type = "fixed"
                    )
                )
            )
        }
        onIntent(RegisterIntent.TermsAccepted(true))
    }

    @Test
    fun registroExitoso201EmiteNavegacionYFormateaDatos() = runTest {
        val viewModel = createViewModel()
        val effects = mutableListOf<AuthEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.collect { effects += it }
        }

        viewModel.fillValidForm(withExpenses = true)
        viewModel.onIntent(RegisterIntent.Submit)
        advanceUntilIdle()

        assertEquals(1, fakeApi.registerCalls)
        assertNotNull(fakeApi.sentIdempotencyKey)
        val request = fakeApi.sentRequest!!
        assertEquals("Usuario Prueba", request.name)
        assertEquals("prueba.movil001@example.com", request.email)
        assertEquals("ExamplePassphrase#2026", request.password)
        assertEquals("15000.00", request.income.amount)
        assertEquals("monthly", request.income.frequency)
        assertEquals(1, request.recurringExpenses.size)
        assertEquals("Internet", request.recurringExpenses[0].concept)
        assertEquals("650.00", request.recurringExpenses[0].amount)
        assertEquals("Servicios", request.recurringExpenses[0].category)
        assertEquals("fixed", request.recurringExpenses[0].type)
        assertEquals(true, request.privacy.accepted)
        assertEquals("2026-11", request.privacy.noticeVersion)
        assertEquals("es-MX", request.client.locale)
        assertEquals("America/Mexico_City", request.client.timeZone)

        assertTrue(viewModel.state.value.isSuccess)
        assertFalse(viewModel.state.value.isLoading)
        assertNull(viewModel.state.value.formError)
        assertEquals(listOf(AuthEffect.NavigateToApp), effects)
    }

    @Test
    fun gastosRecurrentesVaciosEnviaArregloVacio() = runTest {
        val viewModel = createViewModel()
        viewModel.fillValidForm(withExpenses = false)
        viewModel.onIntent(RegisterIntent.Submit)
        advanceUntilIdle()

        assertEquals(1, fakeApi.registerCalls)
        val request = fakeApi.sentRequest!!
        assertTrue(request.recurringExpenses.isEmpty())
    }

    @Test
    fun idempotencyKeySeReutilizaEnReintentoTrasFallo() = runTest {
        fakeApi.responseToReturn = Response.error(400, "".toResponseBody())
        val viewModel = createViewModel()
        viewModel.fillValidForm()

        // Primer intento (falla con 400)
        viewModel.onIntent(RegisterIntent.Submit)
        advanceUntilIdle()
        assertEquals(1, fakeApi.registerCalls)
        val firstKey = fakeApi.sentIdempotencyKey
        assertNotNull(firstKey)

        // Reintento sin modificar campos
        viewModel.onIntent(RegisterIntent.Submit)
        advanceUntilIdle()
        assertEquals(2, fakeApi.registerCalls)
        val secondKey = fakeApi.sentIdempotencyKey

        assertEquals("El UUID de idempotencia debe ser idéntico en un reintento", firstKey, secondKey)
    }

    @Test
    fun idempotencyKeySeRegeneraSiElUsuarioModificaFormulario() = runTest {
        fakeApi.responseToReturn = Response.error(400, "".toResponseBody())
        val viewModel = createViewModel()
        viewModel.fillValidForm()

        viewModel.onIntent(RegisterIntent.Submit)
        advanceUntilIdle()
        val firstKey = fakeApi.sentIdempotencyKey

        // Modifica un campo
        viewModel.onIntent(RegisterIntent.EmailChanged("otro.correo@example.com"))

        viewModel.onIntent(RegisterIntent.Submit)
        advanceUntilIdle()
        val secondKey = fakeApi.sentIdempotencyKey

        assertTrue("El UUID debe cambiar si se edita el formulario", firstKey != secondKey)
    }

    @Test
    fun error400MuestraErrorGeneral() = runTest {
        fakeApi.responseToReturn = Response.error(400, "".toResponseBody())
        val viewModel = createViewModel()
        val effects = mutableListOf<AuthEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.collect { effects += it }
        }

        viewModel.fillValidForm()
        viewModel.onIntent(RegisterIntent.Submit)
        advanceUntilIdle()

        assertEquals("Datos o JSON inválidos. Revisa el formulario.", viewModel.state.value.formError)
        assertFalse(viewModel.state.value.isLoading)
        assertTrue(effects.any { it is AuthEffect.ShowSnackbar && it.message.contains("inválidos") })
    }

    @Test
    fun error409CorreoDuplicadoMuestraMensajeEspecifico() = runTest {
        fakeApi.responseToReturn = Response.error(409, "".toResponseBody())
        val viewModel = createViewModel()
        val effects = mutableListOf<AuthEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.collect { effects += it }
        }

        viewModel.fillValidForm()
        viewModel.onIntent(RegisterIntent.Submit)
        advanceUntilIdle()

        assertEquals("El correo ya está registrado", viewModel.state.value.emailError)
        assertEquals("El correo ya está registrado", viewModel.state.value.formError)
        assertFalse(viewModel.state.value.isLoading)
        assertTrue(effects.any { it is AuthEffect.ShowSnackbar && it.message.contains("ya está registrado") })
    }

    @Test
    fun error422IncumplimientoReglasMuestraMensaje() = runTest {
        fakeApi.responseToReturn = Response.error(422, "".toResponseBody())
        val viewModel = createViewModel()
        viewModel.fillValidForm()
        viewModel.onIntent(RegisterIntent.Submit)
        advanceUntilIdle()

        assertEquals("Incumplimiento de una regla del registro. Por favor revisa los datos.", viewModel.state.value.formError)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun error429DemasiadasSolicitudesLeeHeaderRetryAfter() = runTest {
        fakeApi.responseToReturn = Response.error<RegisterResponseDto>(
            "".toResponseBody(),
            okhttp3.Response.Builder()
                .code(429)
                .message("Too Many Requests")
                .protocol(okhttp3.Protocol.HTTP_1_1)
                .request(okhttp3.Request.Builder().url("https://d2lpmqy5w6x9es.cloudfront.net/api/v1/auth/register").build())
                .headers(Headers.headersOf("Retry-After", "30"))
                .build()
        )

        val viewModel = createViewModel()
        viewModel.fillValidForm()
        viewModel.onIntent(RegisterIntent.Submit)
        advanceUntilIdle()

        assertEquals("30", viewModel.state.value.retryAfterSeconds)
        assertTrue(viewModel.state.value.formError?.contains("30 segundos") == true)
    }

    @Test
    fun validacionLocalPasswordMenorA12CaracteresBloqueaEnvio() = runTest {
        val viewModel = createViewModel()
        viewModel.fillValidForm(password = "Pass#123", confirmPassword = "Pass#123")
        viewModel.onIntent(RegisterIntent.Submit)
        advanceUntilIdle()

        assertEquals(0, fakeApi.registerCalls)
        assertEquals("La contraseña debe tener entre 12 y 128 caracteres", viewModel.state.value.passwordError)
    }

    @Test
    fun validacionLocalConfirmacionNoCoincidenteBloqueaEnvio() = runTest {
        val viewModel = createViewModel()
        viewModel.fillValidForm(password = "ExamplePassphrase#2026", confirmPassword = "DifferentPassphrase#2026")
        viewModel.onIntent(RegisterIntent.Submit)
        advanceUntilIdle()

        assertEquals(0, fakeApi.registerCalls)
        assertEquals("Las contraseñas no coinciden", viewModel.state.value.confirmPasswordError)
    }

    @Test
    fun validacionLocalAvisoPrivacidadSinAceptarBloqueaEnvio() = runTest {
        val viewModel = createViewModel()
        viewModel.fillValidForm()
        viewModel.onIntent(RegisterIntent.TermsAccepted(false))
        viewModel.onIntent(RegisterIntent.Submit)
        advanceUntilIdle()

        assertEquals(0, fakeApi.registerCalls)
        assertEquals("Debes aceptar el aviso de privacidad", viewModel.state.value.termsError)
    }
}
