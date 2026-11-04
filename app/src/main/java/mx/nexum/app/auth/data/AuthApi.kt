package mx.nexum.app.auth.data

import mx.nexum.app.auth.data.dto.LoginRequest
import mx.nexum.app.auth.data.dto.LoginResponse
import mx.nexum.app.auth.data.dto.RegisterRequest
import mx.nexum.app.auth.data.dto.RegisterResponse

class AuthApiException(
    val kind: AuthErrorKind,
    override val message: String,
    val retryable: Boolean = false,
    val attemptsRemaining: Int? = null
) : Exception(message)

interface AuthApi {
    suspend fun register(request: RegisterRequest): RegisterResponse

    suspend fun login(request: LoginRequest): LoginResponse
}
