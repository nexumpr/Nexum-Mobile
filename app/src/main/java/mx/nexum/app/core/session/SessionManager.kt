package mx.nexum.app.core.session

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import mx.nexum.app.core.network.AuthApi
import mx.nexum.app.core.network.RefreshTokenRequest

/**
 * Coordina la lógica de sesión: carga, renovación y logout.
 *
 * - La renovación delega a `AuthApi` (definido en specs/040‑cliente‑http‑contratos).
 * - En caso de error o respuesta 401 se borra la sesión (fail‑secure).
 */
class SessionManager(
    private val storage: CredentialStorage,
    private val authApi: AuthApi
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val _sessionFlow = MutableStateFlow<Session?>(null)
    val sessionFlow: StateFlow<Session?> = _sessionFlow

    init {
        // Cargar sesión al iniciar la aplicación
        scope.launch {
            _sessionFlow.value = storage.load()
        }
    }

    /** Guarda una sesión nueva, por ejemplo después de login. */
    suspend fun setSession(session: Session) {
        storage.save(session)
        _sessionFlow.value = session
    }

    /** Refresca el token de acceso usando el refresh‑token guardado. */
    suspend fun refresh(): Result<Unit> = kotlin.runCatching {
        val current = storage.load() ?: throw IllegalStateException("No stored session")
        val resp = authApi.refreshToken(RefreshTokenRequest(refreshToken = current.refreshToken))
        // Suponemos que `RefreshResponse` contiene accessToken, refreshToken y expiresIn (segundos)
        val newSession = Session(
            accessToken = resp.accessToken,
            refreshToken = resp.refreshToken,
            expiresAt = Instant.now().plusSeconds(resp.expiresIn.toLong())
        )
        setSession(newSession)
    }

    /** Cierra la sesión y elimina los datos aunque la llamada al backend falle. */
    suspend fun logout(): Result<Unit> = kotlin.runCatching {
        try {
            val token = storage.load()?.accessToken ?: ""
            if (token.isNotBlank()) {
                authApi.logout("Bearer $token")
            }
        } catch (e: Exception) {
            // Ignorar errores de red; la spec exige borrado fail‑secure.
        } finally {
            storage.clear()
            _sessionFlow.value = null
        }
    }
}
