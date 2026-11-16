package mx.nexum.app.auth.data

import mx.nexum.app.core.network.AuthApi
import mx.nexum.app.core.network.RefreshTokenRequest
import mx.nexum.app.core.network.RefreshResponse
import mx.nexum.app.auth.data.network.NetworkModule
import retrofit2.Retrofit

/**
 * Implementación Retrofit de la interfaz [AuthApi].
 */
class RetrofitAuthApi(
    private val retrofit: Retrofit = NetworkModule.retrofit
) : AuthApi {
    private val api = retrofit.create(AuthApi::class.java)

    override suspend fun refreshToken(request: RefreshTokenRequest): RefreshResponse =
        api.refreshToken(request)

    override suspend fun logout(authHeader: String) = api.logout(authHeader)
}
