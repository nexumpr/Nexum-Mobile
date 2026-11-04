package mx.nexum.app.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import mx.nexum.app.auth.data.AuthApiException
import mx.nexum.app.auth.data.AuthErrorKind
import mx.nexum.app.auth.data.AuthRepositoryImpl
import mx.nexum.app.auth.data.AuthResult
import mx.nexum.app.auth.data.InMemoryTokenStorage
import mx.nexum.app.auth.data.MockAuthApi
import mx.nexum.app.auth.data.dto.Frequency
import mx.nexum.app.auth.data.dto.Income
import mx.nexum.app.auth.data.dto.LoginRequest
import mx.nexum.app.auth.data.dto.RegisterRequest
import mx.nexum.app.auth.data.dto.RecurringExpense
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MockAuthApiTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun registerRequest(email: String) = RegisterRequest(
        name = "Alex",
        email = email,
        password = "Password1",
        income = Income(amount = "12000.00", frequency = Frequency.monthly),
        recurringExpenses = listOf(
            RecurringExpense(
                concept = "Renta",
                amount = "4000.00",
                frequency = Frequency.monthly,
                categoryId = "vivienda",
                type = mx.nexum.app.auth.data.dto.ExpenseType.fixed
            )
        ),
        privacyNoticeVersion = "2026-11-v1",
        consentAcceptedAt = "2026-11-02T17:30:00Z"
    )

    @Test
    fun registroExitosoResponde201Sintetico() = runTest {
        val api = MockAuthApi()
        val response = api.register(registerRequest("nuevo@nexum.test"))

        assertEquals("pending", response.onboardingStatus)
        assertFalse(response.verificationRequired)
        assertNotNull(response.userId)
        assertNotNull(response.accessToken)
        assertTrue(response.expiresIn != null && response.expiresIn > 0)
    }

    @Test
    fun registroDuplicadoDevuelveConflicto() = runTest {
        val api = MockAuthApi()
        api.register(registerRequest("duplicado@nexum.test"))

        try {
            api.register(registerRequest("duplicado@nexum.test"))
            fail("Se esperaba conflicto de correo")
        } catch (error: AuthApiException) {
            assertEquals(AuthErrorKind.CONFLICT, error.kind)
            assertEquals("El correo ya está registrado", error.message)
        }
    }

    @Test
    fun correoPreexistenteDevuelveConflicto() = runTest {
        val api = MockAuthApi()

        try {
            api.register(registerRequest(MockAuthApi.TAKEN_EMAIL))
            fail("Se esperaba conflicto de correo")
        } catch (error: AuthApiException) {
            assertEquals(AuthErrorKind.CONFLICT, error.kind)
        }
    }

    @Test
    fun correoDeErrorDeRedDevuelveFalloReintentable() = runTest {
        val api = MockAuthApi()

        try {
            api.register(registerRequest(MockAuthApi.NETWORK_ERROR_EMAIL))
            fail("Se esperaba fallo de red")
        } catch (error: AuthApiException) {
            assertEquals(AuthErrorKind.NETWORK, error.kind)
            assertTrue(error.retryable)
        }
    }

    @Test
    fun loginExitosoEntregaTokensYUsuario() = runTest {
        val api = MockAuthApi()
        val response = api.login(
            LoginRequest(MockAuthApi.DEMO_EMAIL, MockAuthApi.DEMO_PASSWORD, deviceName = "Pixel Test")
        )

        assertNotNull(response.accessToken)
        assertNotNull(response.refreshToken)
        assertTrue(response.expiresIn > 0)
        assertEquals(MockAuthApi.DEMO_EMAIL, response.user.email)
    }

    @Test
    fun loginIncorrectoDevuelve401GenericoConConteo() = runTest {
        val api = MockAuthApi()

        try {
            api.login(LoginRequest(MockAuthApi.DEMO_EMAIL, "Incorrecta1"))
            fail("Se esperaba credenciales inválidas")
        } catch (error: AuthApiException) {
            assertEquals(AuthErrorKind.INVALID_CREDENTIALS, error.kind)
            assertEquals("Correo o contraseña incorrectos", error.message)
            assertEquals(4, error.attemptsRemaining)
        }
    }

    @Test
    fun quintoFalloSigueInvalidoYSextoIntentoBloquea() = runTest {
        val api = MockAuthApi()

        repeat(5) {
            try {
                api.login(LoginRequest(MockAuthApi.DEMO_EMAIL, "Incorrecta1"))
                fail("Se esperaba credenciales inválidas")
            } catch (error: AuthApiException) {
                assertEquals(AuthErrorKind.INVALID_CREDENTIALS, error.kind)
            }
        }

        try {
            api.login(LoginRequest(MockAuthApi.DEMO_EMAIL, MockAuthApi.DEMO_PASSWORD))
            fail("Se esperaba límite de intentos")
        } catch (error: AuthApiException) {
            assertEquals(AuthErrorKind.RATE_LIMITED, error.kind)
            assertEquals(0, error.attemptsRemaining)
        }
    }

    @Test
    fun retardoDeRedSimuladoEstaEnRango() = runTest {
        val api = MockAuthApi()
        val start = currentTime

        api.login(LoginRequest(MockAuthApi.DEMO_EMAIL, MockAuthApi.DEMO_PASSWORD))

        val elapsed = currentTime - start
        assertTrue("Retardo fuera de rango: $elapsed", elapsed in 800L..1500L)
    }

    @Test
    fun repositorioGuardaTrasLoginExitoso() = runTest {
        val storage = InMemoryTokenStorage()
        val repository = AuthRepositoryImpl(MockAuthApi(), storage)

        val result = repository.login(LoginRequest(MockAuthApi.DEMO_EMAIL, MockAuthApi.DEMO_PASSWORD))

        assertTrue(result is AuthResult.Success)
        assertNotNull(storage.current())
        assertEquals(MockAuthApi.DEMO_EMAIL, storage.current()?.user?.email)
    }

    @Test
    fun repositorioNoGuardaTrasLoginFallido() = runTest {
        val storage = InMemoryTokenStorage()
        val repository = AuthRepositoryImpl(MockAuthApi(), storage)

        val result = repository.login(LoginRequest(MockAuthApi.DEMO_EMAIL, "Incorrecta1"))

        assertTrue(result is AuthResult.Failure)
        assertNullSafe(storage.current())
    }

    @Test
    fun registroTrasAltaPermiteLoginEnLaMismaFuente() = runTest {
        val api = MockAuthApi()
        val repository = AuthRepositoryImpl(api, InMemoryTokenStorage())

        repository.register(registerRequest("flujo@nexum.test"))
        val result = repository.login(LoginRequest("flujo@nexum.test", "Password1"))

        assertTrue(result is AuthResult.Success)
    }

    private fun assertNullSafe(value: Any?) {
        if (value != null) fail("Se esperaba almacenamiento vacío")
    }
}
