package mx.nexum.app.core.session

/**
 * Abstrae el mecanismo de persistencia de la sesión.
 *
 * - No debe registrar tokens en logs ni guardarlos en texto plano.
 * - La implementación debe poder eliminar todos los datos de forma segura
 *   incluso si la llamada al backend falla (logout fail‑secure).
 */
interface CredentialStorage {
    /** Guarda la sesión completa. */
    suspend fun save(session: Session)

    /** Recupera la sesión almacenada, o `null` si no hay ninguna. */
    suspend fun load(): Session?

    /** Elimina cualquier dato de sesión guardado. */
    suspend fun clear()
}
