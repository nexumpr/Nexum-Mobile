package mx.nexum.app.core.session

import java.time.Instant

/**
 * Representa la sesión autenticada del usuario.
 *
 * - `accessToken`  : JWT de corta vida.
 * - `refreshToken` : JWT de larga vida para renovación.
 * - `expiresAt`    : instante de expiración del access token.
 */
data class Session(
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: Instant
) {
    /** Verdadero si el token de acceso ha expirado. */
    val isExpired: Boolean
        get() = Instant.now().isAfter(expiresAt)
}
