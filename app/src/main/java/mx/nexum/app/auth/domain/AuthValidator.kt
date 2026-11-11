package mx.nexum.app.auth.domain

import java.math.BigDecimal
import java.util.Locale

object AuthValidator {

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@(.+)$")

    fun nameError(name: String): String? =
        if (name.trim().length < 2) "Ingresa tu nombre" else null

    fun emailError(email: String): String? = when {
        email.isBlank() -> "Ingresa tu correo"
        !EMAIL_REGEX.matches(email.trim()) -> "Correo inválido"
        else -> null
    }

    fun passwordError(password: String): String? = when {
        password.isBlank() -> "Ingresa tu contraseña"
        password.length !in 12..128 -> "La contraseña debe tener entre 12 y 128 caracteres"
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
        val value = trimmed.toBigDecimalOrNull() ?: return "Monto inválido"
        if (value <= BigDecimal.ZERO) return "Debe ser mayor a 0"
        return null
    }

    fun formatDecimal(amount: String): String {
        val bd = amount.trim().toBigDecimalOrNull() ?: BigDecimal.ZERO
        return String.format(Locale.US, "%.2f", bd)
    }

    fun conceptError(concept: String): String? =
        if (concept.isBlank()) "Ingresa el concepto" else null

    fun privacyAcceptedError(accepted: Boolean): String? =
        if (accepted) null else "Debes aceptar el aviso de privacidad"

    fun termsAcceptedError(accepted: Boolean): String? = privacyAcceptedError(accepted)
}
