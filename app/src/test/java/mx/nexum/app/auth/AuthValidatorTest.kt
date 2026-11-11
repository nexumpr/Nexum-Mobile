package mx.nexum.app.auth

import mx.nexum.app.auth.domain.AuthValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class AuthValidatorTest {

    @Test
    fun emailValidoNoTieneError() {
        assertNull(AuthValidator.emailError("alex@example.com"))
    }

    @Test
    fun emailVacioTieneError() {
        assertNotNull(AuthValidator.emailError(""))
    }

    @Test
    fun emailSinArrobaTieneError() {
        assertNotNull(AuthValidator.emailError("alexexample.com"))
    }

    @Test
    fun passwordCortaMenorA12TieneError() {
        assertEquals("La contraseña debe tener entre 12 y 128 caracteres", AuthValidator.passwordError("Short#123"))
    }

    @Test
    fun passwordVaciaTieneError() {
        assertEquals("Ingresa tu contraseña", AuthValidator.passwordError(""))
    }

    @Test
    fun passwordMayorA128TieneError() {
        val longPassword = "A".repeat(129)
        assertEquals("La contraseña debe tener entre 12 y 128 caracteres", AuthValidator.passwordError(longPassword))
    }

    @Test
    fun passwordValidaEntre12Y128NoTieneError() {
        assertNull(AuthValidator.passwordError("ExamplePassphrase#2026"))
    }

    @Test
    fun confirmacionDistintaTieneError() {
        assertEquals("Las contraseñas no coinciden", AuthValidator.confirmationError("ExamplePassphrase#2026", "DifferentPassphrase#2026"))
    }

    @Test
    fun confirmacionIgualNoTieneError() {
        assertNull(AuthValidator.confirmationError("ExamplePassphrase#2026", "ExamplePassphrase#2026"))
    }

    @Test
    fun nombreVacioTieneError() {
        assertNotNull(AuthValidator.nameError(" "))
    }

    @Test
    fun nombreValidoNoTieneError() {
        assertNull(AuthValidator.nameError("Alex"))
    }

    @Test
    fun montoDecimalValidoNoTieneError() {
        assertNull(AuthValidator.amountError("15000.00"))
    }

    @Test
    fun montoCeroTieneError() {
        assertEquals("Debe ser mayor a 0", AuthValidator.amountError("0"))
    }

    @Test
    fun montoNoNumericoTieneError() {
        assertEquals("Monto inválido", AuthValidator.amountError("abc"))
    }

    @Test
    fun montoVacioTieneError() {
        assertNotNull(AuthValidator.amountError(""))
    }

    @Test
    fun formateoDecimalConvierteAFormatoEsperado() {
        assertEquals("15000.00", AuthValidator.formatDecimal("15000"))
        assertEquals("650.00", AuthValidator.formatDecimal("650"))
        assertEquals("650.50", AuthValidator.formatDecimal("650.5"))
    }

    @Test
    fun conceptoVacioTieneError() {
        assertNotNull(AuthValidator.conceptError(""))
    }

    @Test
    fun avisoPrivacidadSinAceptarTieneError() {
        assertNotNull(AuthValidator.privacyAcceptedError(false))
    }

    @Test
    fun avisoPrivacidadAceptadoNoTieneError() {
        assertNull(AuthValidator.privacyAcceptedError(true))
    }
}
