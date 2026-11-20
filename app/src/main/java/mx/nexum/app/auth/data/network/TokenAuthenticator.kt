package mx.nexum.app.auth.data.network

import kotlinx.coroutines.runBlocking
import mx.nexum.app.auth.data.AuthRepository
import mx.nexum.app.auth.data.AuthResult
import mx.nexum.app.auth.data.TokenStorage
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

/**
 * Autenticador OkHttp para peticiones protegidas.
 *
 * Cumple con los requisitos de seguridad de la especificación NEXUM-E003:
 * 1. Límite de reintento: Máximo 1 reintento para evitar bucles infinitos ante tokens inválidos.
 * 2. Concurrencia y replay avoidance: Si el token ya fue renovado por otra petición en vuelo,
 *    reutiliza el nuevo token de inmediato sin llamar de nuevo a renovación.
 * 3. Ejecución segura: Si la sesión es revocada o la renovación falla (401/409), aborta retornando null.
 */
class TokenAuthenticator(
    private val tokenStorage: TokenStorage,
    private val authRepository: AuthRepository
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Prohibido reintentar con refresh ante 401 en llamadas de logout
        if (response.request.url.encodedPath.contains("auth/logout")) {
            return null
        }

        // Máximo 1 reintento (si ya hubo una respuesta previa, no volver a reintentar)
        if (responseCount(response) >= 2) {
            return null
        }

        val currentSession = tokenStorage.current() ?: return null

        val authHeader = response.request.header("Authorization")
        val currentBearer = "Bearer ${currentSession.accessToken}"

        // Si el token actual en almacenamiento difiere del que causó el 401,
        // significa que otra corrutina ya ejecutó el refresh. Reutilizar el nuevo token directamente.
        val tokenToUse = if (authHeader != null && authHeader != currentBearer) {
            currentSession.accessToken
        } else {
            val result = runBlocking {
                authRepository.refreshSession()
            }
            when (result) {
                is AuthResult.Success -> result.data.session.accessToken
                is AuthResult.Failure -> return null
            }
        }

        return response.request.newBuilder()
            .header("Authorization", "Bearer $tokenToUse")
            .build()
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
