package mx.nexum.app.auth.data.network

import mx.nexum.app.auth.data.dto.ChangePasswordRequestDto
import mx.nexum.app.auth.data.dto.ChangePasswordResponseDto
import mx.nexum.app.auth.data.dto.ForgotPasswordRequestDto
import mx.nexum.app.auth.data.dto.ForgotPasswordResponseDto
import mx.nexum.app.auth.data.dto.LoginRequestDto
import mx.nexum.app.auth.data.dto.LoginResponseDto
import mx.nexum.app.auth.data.dto.RefreshRequestDto
import mx.nexum.app.auth.data.dto.RefreshResponseDto
import mx.nexum.app.auth.data.dto.RegisterRequestDto
import mx.nexum.app.auth.data.dto.RegisterResponseDto
import mx.nexum.app.auth.data.dto.ResetPasswordRequestDto
import mx.nexum.app.auth.data.dto.ResetPasswordResponseDto
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

    @Headers(
        "Accept: application/json",
        "Content-Type: application/json"
    )
    @POST("auth/refresh")
    suspend fun refresh(
        @Body request: RefreshRequestDto
    ): Response<RefreshResponseDto>

    @Headers("Accept: application/json")
    @POST("auth/logout")
    suspend fun logout(
        @Header("Authorization") authorization: String
    ): Response<Unit>

    @Headers(
        "Accept: application/json",
        "Content-Type: application/json"
    )
    @POST("auth/password/forgot")
    suspend fun forgotPassword(
        @Body request: ForgotPasswordRequestDto
    ): Response<ForgotPasswordResponseDto>

    @Headers(
        "Accept: application/json",
        "Content-Type: application/json"
    )
    @POST("auth/password/reset")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequestDto
    ): Response<ResetPasswordResponseDto>

    @Headers(
        "Accept: application/json",
        "Content-Type: application/json"
    )
    @POST("auth/password/change")
    suspend fun changePassword(
        @Header("Authorization") authorization: String,
        @Body request: ChangePasswordRequestDto
    ): Response<ChangePasswordResponseDto>
}
