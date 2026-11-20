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
import mx.nexum.app.auth.data.dto.ChangePasswordRequest
import mx.nexum.app.auth.data.dto.ForgotPasswordRequest
import mx.nexum.app.auth.data.dto.Frequency
import mx.nexum.app.auth.data.dto.Income
import mx.nexum.app.auth.data.dto.LoginRequest
import mx.nexum.app.auth.data.dto.RefreshRequest
import mx.nexum.app.auth.data.dto.RegisterRequest
import mx.nexum.app.auth.data.dto.RecurringExpense
import mx.nexum.app.auth.data.dto.ResetPasswordRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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

    @Test
    fun refreshExitosoRotaTokensYDevuelveNuevaVersion() = runTest {
        val api = MockAuthApi()
        val loginResponse = api.login(LoginRequest(MockAuthApi.DEMO_EMAIL, MockAuthApi.DEMO_PASSWORD))
        val initialRefresh = loginResponse.refreshToken

        val refreshResponse = api.refresh(RefreshRequest(refreshToken = initialRefresh))

        assertNotNull(refreshResponse.accessToken)
        assertNotNull(refreshResponse.refreshToken)
        assertFalse(refreshResponse.refreshToken == initialRefresh)
        assertEquals(900L, refreshResponse.expiresIn)
        assertEquals("Bearer", refreshResponse.tokenType)
    }

    @Test
    fun reusoDeRefreshTokenDisparaConflicto409YRevocaFamilia() = runTest {
        val api = MockAuthApi()
        val login = api.login(LoginRequest(MockAuthApi.DEMO_EMAIL, MockAuthApi.DEMO_PASSWORD))
        val tokenOriginal = login.refreshToken

        // Primera rotación válida
        val rotacion1 = api.refresh(RefreshRequest(refreshToken = tokenOriginal))
        val tokenNuevo = rotacion1.refreshToken

        // Intento de replay con el token original ya consumido -> 409
        try {
            api.refresh(RefreshRequest(refreshToken = tokenOriginal))
            fail("Se esperaba 409 CONFLICT ante reutilización de token")
        } catch (e: AuthApiException) {
            assertEquals(AuthErrorKind.CONFLICT, e.kind)
            assertEquals(409, e.statusCode)
        }

        // Toda la familia debe haber quedado revocada; el token nuevo ahora falla con 401
        try {
            api.refresh(RefreshRequest(refreshToken = tokenNuevo))
            fail("Se esperaba 401 INVALID_CREDENTIALS por familia revocada")
        } catch (e: AuthApiException) {
            assertEquals(AuthErrorKind.INVALID_CREDENTIALS, e.kind)
            assertEquals(401, e.statusCode)
        }
    }

    @Test
    fun tokenExpiradoDispara401() = runTest {
        val api = MockAuthApi()
        try {
            api.refresh(RefreshRequest(refreshToken = MockAuthApi.EXPIRED_TOKEN))
            fail("Se esperaba 401 para token expirado")
        } catch (e: AuthApiException) {
            assertEquals(AuthErrorKind.INVALID_CREDENTIALS, e.kind)
            assertEquals(401, e.statusCode)
            assertFalse(e.retryable)
        }
    }

    @Test
    fun tokenDesconocidoDispara401() = runTest {
        val api = MockAuthApi()
        try {
            api.refresh(RefreshRequest(refreshToken = "token_inexistente_12345"))
            fail("Se esperaba 401 para token desconocido")
        } catch (e: AuthApiException) {
            assertEquals(AuthErrorKind.INVALID_CREDENTIALS, e.kind)
            assertEquals(401, e.statusCode)
        }
    }

    @Test
    fun servicioNoDisponible503EsReintentable() = runTest {
        val api = MockAuthApi()
        try {
            api.refresh(RefreshRequest(refreshToken = MockAuthApi.SERVICE_UNAVAILABLE_TOKEN))
            fail("Se esperaba 503 SERVER_UNAVAILABLE")
        } catch (e: AuthApiException) {
            assertEquals(AuthErrorKind.SERVER_UNAVAILABLE, e.kind)
            assertEquals(503, e.statusCode)
            assertTrue(e.retryable)
        }
    }

    @Test
    fun errorDeRedEsReintentable() = runTest {
        val api = MockAuthApi()
        try {
            api.refresh(RefreshRequest(refreshToken = MockAuthApi.NETWORK_ERROR_TOKEN))
            fail("Se esperaba error de red")
        } catch (e: AuthApiException) {
            assertEquals(AuthErrorKind.NETWORK, e.kind)
            assertTrue(e.retryable)
        }
    }

    @Test
    fun discrepanciaDeDeviceIdDispara401() = runTest {
        val api = MockAuthApi()
        val login = api.login(
            LoginRequest(
                email = MockAuthApi.DEMO_EMAIL,
                password = MockAuthApi.DEMO_PASSWORD,
                deviceId = "device-alpha"
            )
        )
        try {
            api.refresh(
                RefreshRequest(
                    refreshToken = login.refreshToken,
                    deviceId = "device-beta"
                )
            )
            fail("Se esperaba 401 por deviceId discordante")
        } catch (e: AuthApiException) {
            assertEquals(AuthErrorKind.INVALID_CREDENTIALS, e.kind)
            assertEquals(401, e.statusCode)
        }
    }

    @Test
    fun logoutExitosoMarcaTokensComoRevocadosYSubiguienteRefreshFallaCon401() = runTest {
        val api = MockAuthApi()
        val login = api.login(LoginRequest(MockAuthApi.DEMO_EMAIL, MockAuthApi.DEMO_PASSWORD))

        api.logout(login.accessToken)

        // El refresh token posterior debe fallar con 401 porque la familia fue revocada
        try {
            api.refresh(RefreshRequest(refreshToken = login.refreshToken))
            fail("Se esperaba 401 INVALID_CREDENTIALS tras logout")
        } catch (e: AuthApiException) {
            assertEquals(AuthErrorKind.INVALID_CREDENTIALS, e.kind)
            assertEquals(401, e.statusCode)
        }
    }

    @Test
    fun logoutConTokenYaRevocadoResponde401() = runTest {
        val api = MockAuthApi()
        val login = api.login(LoginRequest(MockAuthApi.DEMO_EMAIL, MockAuthApi.DEMO_PASSWORD))

        api.logout(login.accessToken)

        try {
            api.logout(login.accessToken)
            fail("Se esperaba 401 para token ya revocado en logout")
        } catch (e: AuthApiException) {
            assertEquals(AuthErrorKind.INVALID_CREDENTIALS, e.kind)
            assertEquals(401, e.statusCode)
        }
    }

    @Test
    fun logoutConErrorDeRedLanzaExcepcionReintentable() = runTest {
        val api = MockAuthApi()
        try {
            api.logout(MockAuthApi.NETWORK_ERROR_LOGOUT_TOKEN)
            fail("Se esperaba error de red")
        } catch (e: AuthApiException) {
            assertEquals(AuthErrorKind.NETWORK, e.kind)
            assertTrue(e.retryable)
        }
    }

    @Test
    fun forgotPasswordRetorna202NeutralParaCorreoRegistrado() = runTest {
        val api = MockAuthApi()
        val response = api.forgotPassword(ForgotPasswordRequest(MockAuthApi.DEMO_EMAIL))
        assertEquals("accepted", response.status)
        assertNotNull(response.requestId)
    }

    @Test
    fun forgotPasswordRetorna202NeutralParaCorreoNoRegistrado() = runTest {
        val api = MockAuthApi()
        val response = api.forgotPassword(ForgotPasswordRequest("no_registrado@nexum.test"))
        assertEquals("accepted", response.status)
        assertNotNull(response.requestId)
    }

    @Test
    fun forgotPasswordCentinelaRateLimitedLanza429ConRetryAfter() = runTest {
        val api = MockAuthApi()
        try {
            api.forgotPassword(ForgotPasswordRequest(MockAuthApi.RATE_LIMITED_EMAIL))
            fail("Se esperaba RATE_LIMITED")
        } catch (e: AuthApiException) {
            assertEquals(AuthErrorKind.RATE_LIMITED, e.kind)
            assertEquals(429, e.statusCode)
            assertEquals(60, e.retryAfterSeconds)
        }
    }

    @Test
    fun forgotPasswordCentinelaNetworkErrorLanzaExcepcionRed() = runTest {
        val api = MockAuthApi()
        try {
            api.forgotPassword(ForgotPasswordRequest(MockAuthApi.NETWORK_ERROR_EMAIL))
            fail("Se esperaba NETWORK")
        } catch (e: AuthApiException) {
            assertEquals(AuthErrorKind.NETWORK, e.kind)
            assertTrue(e.retryable)
        }
    }

    @Test
    fun resetPasswordExitosoConTokenValidoYContrasenaValida() = runTest {
        val api = MockAuthApi()
        val response = api.resetPassword(
            ResetPasswordRequest(
                token = "token-valido-12345",
                newPassword = "NuevaPasswordValida123"
            )
        )
        assertEquals("accepted", response.status)
        assertNotNull(response.requestId)
    }

    @Test
    fun resetPasswordRechazaTokenMenorDe10CaracteresCon400() = runTest {
        val api = MockAuthApi()
        try {
            api.resetPassword(
                ResetPasswordRequest(
                    token = "token9chr",
                    newPassword = "NuevaPasswordValida123"
                )
            )
            fail("Se esperaba 400 por token < 10 chars")
        } catch (e: AuthApiException) {
            assertEquals(400, e.statusCode)
        }
    }

    @Test
    fun resetPasswordRechazaTokenExpiradoCon400() = runTest {
        val api = MockAuthApi()
        try {
            api.resetPassword(
                ResetPasswordRequest(
                    token = MockAuthApi.EXPIRED_RESET_TOKEN,
                    newPassword = "NuevaPasswordValida123"
                )
            )
            fail("Se esperaba 400 por token expirado")
        } catch (e: AuthApiException) {
            assertEquals(400, e.statusCode)
        }
    }

    @Test
    fun resetPasswordRechazaTokenYaUtilizadoCon409() = runTest {
        val api = MockAuthApi()
        val token = "token-unico-12345678"
        val response = api.resetPassword(
            ResetPasswordRequest(token = token, newPassword = "NuevaPasswordValida123")
        )
        assertEquals("accepted", response.status)

        try {
            api.resetPassword(
                ResetPasswordRequest(token = token, newPassword = "OtraPasswordValida123")
            )
            fail("Se esperaba 409 por token ya utilizado")
        } catch (e: AuthApiException) {
            assertEquals(AuthErrorKind.CONFLICT, e.kind)
            assertEquals(409, e.statusCode)
        }
    }

    @Test
    fun resetPasswordRechazaContrasenaFueraDeRangoCon422() = runTest {
        val api = MockAuthApi()
        try {
            api.resetPassword(
                ResetPasswordRequest(
                    token = "token-valido-12345",
                    newPassword = "corta"
                )
            )
            fail("Se esperaba 422 por contraseña menor a 12 caracteres")
        } catch (e: AuthApiException) {
            assertEquals(422, e.statusCode)
        }
    }

    @Test
    fun resetPasswordErrorDeRedLanzaExcepcionReintentable() = runTest {
        val api = MockAuthApi()
        try {
            api.resetPassword(
                ResetPasswordRequest(
                    token = MockAuthApi.NETWORK_ERROR_TOKEN,
                    newPassword = "NuevaPasswordValida123"
                )
            )
            fail("Se esperaba error de red")
        } catch (e: AuthApiException) {
            assertEquals(AuthErrorKind.NETWORK, e.kind)
            assertTrue(e.retryable)
        }
    }

    @Test
    fun changePasswordExitosoConCredencialesValidas() = runTest {
        val api = MockAuthApi()
        val login = api.login(LoginRequest("demo@nexum.test", "Demo1234"))
        val response = api.changePassword(
            accessToken = login.accessToken,
            request = ChangePasswordRequest(
                currentPassword = "Demo1234",
                newPassword = "NuevaClaveEjemplo#2026"
            )
        )
        assertEquals("accepted", response.status)
        assertNotNull(response.requestId)

        // Verificación de revocación post-cambio (E007): el accessToken previo queda revocado (401)
        try {
            api.changePassword(
                accessToken = login.accessToken,
                request = ChangePasswordRequest(
                    currentPassword = "NuevaClaveEjemplo#2026",
                    newPassword = "OtraClaveValida#2026"
                )
            )
            fail("Se esperaba 401 por token previo revocado tras cambio de clave")
        } catch (e: AuthApiException) {
            assertEquals(AuthErrorKind.INVALID_CREDENTIALS, e.kind)
            assertEquals(401, e.statusCode)
        }

        // Login con contraseña anterior falla
        try {
            api.login(LoginRequest("demo@nexum.test", "Demo1234"))
            fail("Se esperaba 401 para contraseña anterior")
        } catch (e: AuthApiException) {
            assertEquals(AuthErrorKind.INVALID_CREDENTIALS, e.kind)
        }

        // Login con nueva contraseña tiene éxito
        val nuevoLogin = api.login(LoginRequest("demo@nexum.test", "NuevaClaveEjemplo#2026"))
        assertNotNull(nuevoLogin.accessToken)
    }

    @Test
    fun changePasswordRechazaContrasenaActualErroneaCon401() = runTest {
        val api = MockAuthApi()
        val login = api.login(LoginRequest("demo@nexum.test", "Demo1234"))
        try {
            api.changePassword(
                accessToken = login.accessToken,
                request = ChangePasswordRequest(
                    currentPassword = "ClaveIncorrecta#2026",
                    newPassword = "NuevaClaveEjemplo#2026"
                )
            )
            fail("Se esperaba 401 por contraseña actual errónea")
        } catch (e: AuthApiException) {
            assertEquals(AuthErrorKind.INVALID_CREDENTIALS, e.kind)
            assertEquals(401, e.statusCode)
        }
    }

    @Test
    fun changePasswordRechazaNuevaContrasenaMenorDe12CaracteresCon422() = runTest {
        val api = MockAuthApi()
        val login = api.login(LoginRequest("demo@nexum.test", "Demo1234"))
        try {
            api.changePassword(
                accessToken = login.accessToken,
                request = ChangePasswordRequest(
                    currentPassword = "Demo1234",
                    newPassword = "Corta#123"
                )
            )
            fail("Se esperaba 422 por nueva contraseña menor a 12 caracteres")
        } catch (e: AuthApiException) {
            assertEquals(AuthErrorKind.VALIDATION, e.kind)
            assertEquals(422, e.statusCode)
        }
    }

    @Test
    fun changePasswordRechazaNuevaContrasenaIgualALaActualCon409() = runTest {
        val api = MockAuthApi()
        val login = api.login(LoginRequest("demo@nexum.test", "Demo1234"))
        try {
            api.changePassword(
                accessToken = login.accessToken,
                request = ChangePasswordRequest(
                    currentPassword = "Demo1234",
                    newPassword = "Demo1234"
                )
            )
            fail("Se esperaba 409 por nueva contraseña igual a la actual")
        } catch (e: AuthApiException) {
            assertEquals(AuthErrorKind.CONFLICT, e.kind)
            assertEquals(409, e.statusCode)
        }
    }

    @Test
    fun changePasswordConTokenInvalidoORevocadoFallaCon401() = runTest {
        val api = MockAuthApi()
        try {
            api.changePassword(
                accessToken = MockAuthApi.EXPIRED_TOKEN,
                request = ChangePasswordRequest(
                    currentPassword = "Demo1234",
                    newPassword = "NuevaClaveEjemplo#2026"
                )
            )
            fail("Se esperaba 401 por token expirado")
        } catch (e: AuthApiException) {
            assertEquals(AuthErrorKind.INVALID_CREDENTIALS, e.kind)
            assertEquals(401, e.statusCode)
        }
    }

    @Test
    fun changePasswordErrorDeRedLanzaExcepcionReintentable() = runTest {
        val api = MockAuthApi()
        try {
            api.changePassword(
                accessToken = MockAuthApi.NETWORK_ERROR_TOKEN,
                request = ChangePasswordRequest(
                    currentPassword = "Demo1234",
                    newPassword = "NuevaClaveEjemplo#2026"
                )
            )
            fail("Se esperaba error de red")
        } catch (e: AuthApiException) {
            assertEquals(AuthErrorKind.NETWORK, e.kind)
            assertTrue(e.retryable)
        }
    }

    @Test
    fun getSessionsExitosoRetornaPrimeraPaginaConSesionActualYCursor() = runTest {
        val api = MockAuthApi()
        val login = api.login(LoginRequest("demo@nexum.test", "Demo1234"))
        val response = api.getSessions(accessToken = login.accessToken, limit = 25, cursor = null)

        assertNotNull(response)
        assertEquals(2, response.items.size)
        assertTrue(response.items[0].current)
        assertFalse(response.items[0].revoked)
        assertNotNull(response.items[0].id)
        assertNotNull(response.items[0].createdAt)
        assertNotNull(response.items[0].lastSeenAt)
        assertEquals(MockAuthApi.PAGE_2_CURSOR, response.nextCursor)
    }

    @Test
    fun getSessionsExitosoSegundaPaginaRetornaItemsYCursorNulo() = runTest {
        val api = MockAuthApi()
        val login = api.login(LoginRequest("demo@nexum.test", "Demo1234"))
        val response = api.getSessions(
            accessToken = login.accessToken,
            limit = 25,
            cursor = MockAuthApi.PAGE_2_CURSOR
        )

        assertNotNull(response)
        assertEquals(2, response.items.size)
        assertFalse(response.items[0].current)
        assertTrue(response.items[0].revoked)
        assertNull(response.nextCursor)
    }

    @Test
    fun getSessionsRechazaLimitFueraDeRangoCon400() = runTest {
        val api = MockAuthApi()
        val login = api.login(LoginRequest("demo@nexum.test", "Demo1234"))

        try {
            api.getSessions(accessToken = login.accessToken, limit = 0, cursor = null)
            fail("Se esperaba 400 por limit = 0")
        } catch (e: AuthApiException) {
            assertEquals(AuthErrorKind.VALIDATION, e.kind)
            assertEquals(400, e.statusCode)
        }

        try {
            api.getSessions(accessToken = login.accessToken, limit = 101, cursor = null)
            fail("Se esperaba 400 por limit = 101")
        } catch (e: AuthApiException) {
            assertEquals(AuthErrorKind.VALIDATION, e.kind)
            assertEquals(400, e.statusCode)
        }
    }

    @Test
    fun getSessionsRechazaCursorInvalidoCon400() = runTest {
        val api = MockAuthApi()
        val login = api.login(LoginRequest("demo@nexum.test", "Demo1234"))

        try {
            api.getSessions(accessToken = login.accessToken, limit = 25, cursor = "short")
            fail("Se esperaba 400 por cursor menor a 8 caracteres")
        } catch (e: AuthApiException) {
            assertEquals(AuthErrorKind.VALIDATION, e.kind)
            assertEquals(400, e.statusCode)
        }

        try {
            api.getSessions(
                accessToken = login.accessToken,
                limit = 25,
                cursor = MockAuthApi.INVALID_CURSOR
            )
            fail("Se esperaba 400 por cursor centinela inválido")
        } catch (e: AuthApiException) {
            assertEquals(AuthErrorKind.VALIDATION, e.kind)
            assertEquals(400, e.statusCode)
        }
    }

    @Test
    fun getSessionsConTokenInvalidoORevocadoFallaCon401() = runTest {
        val api = MockAuthApi()
        try {
            api.getSessions(accessToken = MockAuthApi.EXPIRED_TOKEN, limit = 25, cursor = null)
            fail("Se esperaba 401 por token expirado")
        } catch (e: AuthApiException) {
            assertEquals(AuthErrorKind.INVALID_CREDENTIALS, e.kind)
            assertEquals(401, e.statusCode)
        }
    }

    @Test
    fun getSessionsConTokenDeRedLanzaExcepcionReintentable() = runTest {
        val api = MockAuthApi()
        try {
            api.getSessions(
                accessToken = MockAuthApi.NETWORK_ERROR_TOKEN,
                limit = 25,
                cursor = null
            )
            fail("Se esperaba error de red")
        } catch (e: AuthApiException) {
            assertEquals(AuthErrorKind.NETWORK, e.kind)
            assertTrue(e.retryable)
        }
    }

    private fun assertNullSafe(value: Any?) {
        if (value != null) fail("Se esperaba almacenamiento vacío")
    }
}
