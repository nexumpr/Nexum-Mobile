package mx.nexum.app.auth.domain

import java.math.BigDecimal

object AuthValidator {

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@(.+)$")
    private val AMOUNT_REGEX = Regex("^\\d+(\\.\\d{1,2})?$")

    fun nameError(name: String): String? =
        if (name.trim().length < 2) "Ingresa tu nombre" else null

    fun emailError(email: String): String? = when {
        email.isBlank() -> "Ingresa tu correo"
        !EMAIL_REGEX.matches(email.trim()) -> "Correo inválido"
        else -> null
    }

    fun passwordError(password: String): String? = when {
        password.isBlank() -> "Ingresa tu contraseña"
        password.length < 8 -> "Mínimo 8 caracteres"
        !password.all { it.isAsciiLetterOrDigit() } -> "Solo caracteres alfanuméricos"
        !password.any { it in '0'..'9' } || !password.any { it.isAsciiLetter() } -> "Debe incluir letras y números"
        else -> null
    }

    fun confirmationError(password: String, confirmation: String): String? = when {
        confirmation.isBlank() -> "Confirma tu contraseña"
        password != confirmation -> "Las contraseñas no coinciden"
        else -> null
    }

    fun amountError(amount: String): String? {
        val trimmed = amount.trim()
        if (trimmed.isBlank()) return "Ingresa un monto"
        if (!AMOUNT_REGEX.matches(trimmed)) return "Monto inválido"
        val value = trimmed.toBigDecimalOrNull() ?: return "Monto inválido"
        if (value <= BigDecimal.ZERO) return "Debe ser mayor a 0"
        return null
    }

    fun conceptError(concept: String): String? =
        if (concept.isBlank()) "Ingresa el concepto" else null

    fun termsAcceptedError(accepted: Boolean): String? =
        if (accepted) null else "Debes aceptar los Términos y Condiciones"

    private fun Char.isAsciiLetter(): Boolean = this in 'a'..'z' || this in 'A'..'Z'

    private fun Char.isAsciiLetterOrDigit(): Boolean = isAsciiLetter() || this in '0'..'9'
}
