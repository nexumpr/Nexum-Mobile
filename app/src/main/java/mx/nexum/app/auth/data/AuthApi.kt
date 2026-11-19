package mx.nexum.app.auth.data

import mx.nexum.app.auth.data.dto.ChangePasswordRequest
import mx.nexum.app.auth.data.dto.ChangePasswordResponse
import mx.nexum.app.auth.data.dto.ForgotPasswordRequest
import mx.nexum.app.auth.data.dto.ForgotPasswordResponse
import mx.nexum.app.auth.data.dto.LoginRequest
import mx.nexum.app.auth.data.dto.LoginResponse
import mx.nexum.app.auth.data.dto.RefreshRequest
import mx.nexum.app.auth.data.dto.RefreshResponse
import mx.nexum.app.auth.data.dto.RegisterRequest
import mx.nexum.app.auth.data.dto.RegisterResponse
import mx.nexum.app.auth.data.dto.ResetPasswordRequest
import mx.nexum.app.auth.data.dto.ResetPasswordResponse

class AuthApiException(
    val kind: AuthErrorKind,
    override val message: String,
    val retryable: Boolean = false,
    val attemptsRemaining: Int? = null,
    val statusCode: Int? = null,
    val retryAfterSeconds: Int? = null
) : Exception(message)

interface AuthApi {
    suspend fun register(request: RegisterRequest): RegisterResponse

    suspend fun login(request: LoginRequest): LoginResponse

    suspend fun refresh(request: RefreshRequest): RefreshResponse

    suspend fun logout(accessToken: String)

    suspend fun forgotPassword(request: ForgotPasswordRequest): ForgotPasswordResponse =
        throw UnsupportedOperationException("No implementado")

    suspend fun resetPassword(request: ResetPasswordRequest): ResetPasswordResponse =
        throw UnsupportedOperationException("No implementado")

    suspend fun changePassword(
        accessToken: String,
        request: ChangePasswordRequest
    ): ChangePasswordResponse =
        throw UnsupportedOperationException("No implementado")
}
