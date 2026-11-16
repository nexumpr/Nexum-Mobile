package mx.nexum.app.core.network

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * API de autenticación requerida por la unidad 050‑sesión‑credenciales.
 * Provee los endpoints de refresco de token y logout.
 */
interface AuthApi {

    @POST("auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): RefreshResponse

    @POST("auth/logout")
    suspend fun logout(
        @Header("Authorization") authHeader: String
    )
}

/** DTO de solicitud de refresco de token */
data class RefreshTokenRequest(
    val refreshToken: String
)

/** DTO de respuesta de refresco de token */
data class RefreshResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Int // segundos de vida del access token
)
