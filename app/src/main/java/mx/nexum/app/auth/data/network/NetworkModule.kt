
package mx.nexum.app.auth.data.network

import mx.nexum.app.auth.data.AuthRepository
import mx.nexum.app.auth.data.TokenStorage
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {
    private const val BASE_URL = "https://d2lpmqy5w6x9es.cloudfront.net/api/v1/"

    /**
     * Cliente OkHttp aislado exclusivamente para endpoints públicos de autenticación
     * (registro, login, refresh). No contiene Authorization interceptor ni TokenAuthenticator
     * para evitar recursión infinita.
     */
    val authOkHttpClient: OkHttpClient = OkHttpClient.Builder()
        .build()

    // Compatibilidad regresiva con referencias existentes
    val okHttpClient: OkHttpClient = authOkHttpClient

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(authOkHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val authApiService: AuthApiService = retrofit.create(AuthApiService::class.java)

    /**
     * Construye un cliente OkHttp autenticado para endpoints protegidos,
     * inyectando el token Bearer vigente y delegando la renovación a [TokenAuthenticator].
     */
    fun createAuthenticatedOkHttpClient(
        tokenStorage: TokenStorage,
        authRepository: AuthRepository
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val original = chain.request()
            val session = tokenStorage.current()
            val request = if (session != null && !session.accessToken.isBlank()) {
                original.newBuilder()
                    .header("Authorization", "Bearer ${session.accessToken}")
                    .build()
            } else {
                original
            }
            chain.proceed(request)
        }
        .authenticator(TokenAuthenticator(tokenStorage, authRepository))
        .build()
}
