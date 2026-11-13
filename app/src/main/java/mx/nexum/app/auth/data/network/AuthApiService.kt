package mx.nexum.app.auth.data.network

import mx.nexum.app.auth.data.dto.LoginRequestDto
import mx.nexum.app.auth.data.dto.LoginResponseDto
import mx.nexum.app.auth.data.dto.RegisterRequestDto
import mx.nexum.app.auth.data.dto.RegisterResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface AuthApiService {

    @Headers(
        "Accept: application/json",
        "Content-Type: application/json"
    )
    @POST("auth/register")
    suspend fun register(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body request: RegisterRequestDto
    ): Response<RegisterResponseDto>

    @Headers(
        "Accept: application/json",
        "Content-Type: application/json"
    )
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequestDto
    ): Response<LoginResponseDto>
}
