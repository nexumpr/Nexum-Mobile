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
    fun passwordCortaTieneError() {
        assertEquals("Mínimo 8 caracteres", AuthValidator.passwordError("Ab1"))
    }

    @Test
    fun passwordSinNumeroTieneError() {
        assertNotNull(AuthValidator.passwordError("Abcdefgh"))
    }

    @Test
    fun passwordSinLetraTieneError() {
        assertNotNull(AuthValidator.passwordError("12345678"))
    }

    @Test
    fun passwordConSimbolosTieneError() {
        assertNotNull(AuthValidator.passwordError("Abcdefg1!"))
    }

    @Test
    fun passwordValidaNoTieneError() {
        assertNull(AuthValidator.passwordError("Password1"))
    }

    @Test
    fun confirmacionDistintaTieneError() {
        assertEquals("Las contraseñas no coinciden", AuthValidator.confirmationError("Password1", "Password2"))
    }

    @Test
    fun confirmacionIgualNoTieneError() {
        assertNull(AuthValidator.confirmationError("Password1", "Password1"))
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
        assertNull(AuthValidator.amountError("12000.00"))
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
    fun conceptoVacioTieneError() {
        assertNotNull(AuthValidator.conceptError(""))
    }

    @Test
    fun terminosSinAceptarTieneError() {
        assertNotNull(AuthValidator.termsAcceptedError(false))
    }

    @Test
    fun terminosAceptadosNoTieneError() {
        assertNull(AuthValidator.termsAcceptedError(true))
    }
}
