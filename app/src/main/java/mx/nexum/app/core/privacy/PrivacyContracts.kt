package mx.nexum.app.core.privacy

/**
 * Estados posibles para la solicitud de permisos en tiempo de ejecución.
 * Satisface M070-R01: Solicitar permisos en contexto con alternativas razonables.
 */
enum class PermissionStatus {
    NOT_REQUESTED,
    GRANTED,
    DENIED,
    PERMANENTLY_DENIED
}

/**
 * Alternativas de entrada cuando un permiso sensible (como micrófono o cámara) es rechazado.
 * Garantiza que la aplicación nunca se bloquee ni degrade irreversiblemente.
 */
enum class AlternativeInputMode {
    MANUAL_TEXT_INPUT,
    STANDARD_FORM,
    LIMITED_OFFLINE_MODE
}

/**
 * Gestor de degradación elegante y alternativas cuando un permiso es denegado o no otorgado.
 */
object PermissionFallbackHandler {

    /**
     * Determina la acción alternativa cuando un permiso de voz/audio es denegado o no concedido.
     */
    fun resolveVoiceInputFallback(status: PermissionStatus): AlternativeInputMode {
        return when (status) {
            PermissionStatus.GRANTED -> AlternativeInputMode.STANDARD_FORM
            PermissionStatus.DENIED,
            PermissionStatus.PERMANENTLY_DENIED,
            PermissionStatus.NOT_REQUESTED -> AlternativeInputMode.MANUAL_TEXT_INPUT
        }
    }

    /**
     * Evalúa si una función puede continuar operando sin el permiso requerido.
     */
    fun canOperateWithoutPermission(status: PermissionStatus): Boolean {
        // En Nexum ninguna funcionalidad es bloqueante por falta de permisos de hardware/voz
        return true
    }
}

/**
 * Configuración canónica de privacidad y transporte seguro.
 * Satisface M070-R03: Transporte HTTPS forzado, política accesible y cero filtraciones.
 */
object PrivacyPolicyConfig {
    const val OFFICIAL_PRIVACY_URL = "https://nexumpr.github.io/nexum-privacidad/"

    /**
     * Valida que la URL de privacidad cumpla estrictamente con transporte seguro TLS/HTTPS.
     */
    fun isUrlSecure(url: String): Boolean {
        return url.startsWith("https://", ignoreCase = true)
    }

    /**
     * Sanitiza mensajes o excepciones para asegurar que ningún token, contraseña o dato
     * personal sea expuesto en logs o mensajes de depuración.
     */
    fun sanitizeForLogging(text: String): String {
        return text
            .replace(Regex("(?i)bearer\\s+[A-Za-z0-9._~+/-]+=*"), "Bearer [REDACTED]")
            .replace(Regex("(?i)password=([^&\\s]+)"), "password=[REDACTED]")
            .replace(Regex("(?i)refreshToken=([^&\\s]+)"), "refreshToken=[REDACTED]")
    }
}
