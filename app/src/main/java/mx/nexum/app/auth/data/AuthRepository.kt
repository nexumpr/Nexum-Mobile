package mx.nexum.app.auth.data

import kotlinx.coroutines.CancellationException
import mx.nexum.app.auth.data.dto.LoginRequest
import mx.nexum.app.auth.data.dto.LoginResponse
import mx.nexum.app.auth.data.dto.RegisterRequest
import mx.nexum.app.auth.data.dto.User

enum class AuthErrorKind { CONFLICT, INVALID_CREDENTIALS, RATE_LIMITED, NETWORK, UNKNOWN }

data class AuthError(
    val kind: AuthErrorKind,
    val message: String,
    val retryable: Boolean = false,
    val attemptsRemaining: Int? = null
)

sealed interface AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>

    data class Failure(val error: AuthError) : AuthResult<Nothing>
}

data class Session(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val user: User
)

data class RegisterOutcome(
    val userId: String,
    val onboardingStatus: String,
    val verificationRequired: Boolean
)

data class LoginOutcome(val session: Session)

interface AuthRepository {
    suspend fun register(request: RegisterRequest): AuthResult<RegisterOutcome>

    suspend fun login(request: LoginRequest): AuthResult<LoginOutcome>
}

class AuthRepositoryImpl(
    private val api: AuthApi,
    private val tokenStorage: TokenStorage
) : AuthRepository {

    override suspend fun register(request: RegisterRequest): AuthResult<RegisterOutcome> =
        try {
            val response = api.register(request)
            AuthResult.Success(
                RegisterOutcome(
                    userId = response.userId,
                    onboardingStatus = response.onboardingStatus,
                    verificationRequired = response.verificationRequired
                )
            )
        } catch (error: Throwable) {
            AuthResult.Failure(error.toAuthError())
        }

    override suspend fun login(request: LoginRequest): AuthResult<LoginOutcome> =
        try {
            val response = api.login(request)
            val session = response.toSession()
            tokenStorage.save(session)
            AuthResult.Success(LoginOutcome(session))
        } catch (error: Throwable) {
            AuthResult.Failure(error.toAuthError())
        }

    private fun Throwable.toAuthError(): AuthError = when (this) {
        is CancellationException -> throw this
        is AuthApiException -> AuthError(kind, message, retryable, attemptsRemaining)
        else -> AuthError(AuthErrorKind.UNKNOWN, "Error inesperado. Intenta de nuevo.", retryable = true)
    }

    private fun LoginResponse.toSession() = Session(
        accessToken = accessToken,
        refreshToken = refreshToken,
        expiresIn = expiresIn,
        user = user
    )
}
