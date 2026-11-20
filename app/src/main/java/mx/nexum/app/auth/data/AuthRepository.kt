package mx.nexum.app.auth.data

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mx.nexum.app.auth.data.dto.ChangePasswordRequest
import mx.nexum.app.auth.data.dto.ChangePasswordResponse
import mx.nexum.app.auth.data.dto.ForgotPasswordRequest
import mx.nexum.app.auth.data.dto.LoginRequest
import mx.nexum.app.auth.data.dto.LoginResponse
import mx.nexum.app.auth.data.dto.RefreshRequest
import mx.nexum.app.auth.data.dto.RegisterRequest
import mx.nexum.app.auth.data.dto.ResetPasswordRequest
import mx.nexum.app.auth.data.dto.User
import mx.nexum.app.auth.ui.AuthEffect

enum class AuthErrorKind { CONFLICT, INVALID_CREDENTIALS, RATE_LIMITED, NETWORK, SERVER_UNAVAILABLE, UNKNOWN, VALIDATION }

data class AuthError(
    val kind: AuthErrorKind,
    val message: String,
    val retryable: Boolean = false,
    val attemptsRemaining: Int? = null,
    val retryAfterSeconds: Int? = null,
    val statusCode: Int? = null
)

sealed interface AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>

    data class Failure(val error: AuthError) : AuthResult<Nothing>
}

data class Session(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val user: User,
    val sessionId: String? = null
)

data class RegisterOutcome(
    val userId: String,
    val onboardingStatus: String,
    val verificationRequired: Boolean
)

data class LoginOutcome(val session: Session)

data class RefreshOutcome(val session: Session)

data class ForgotPasswordOutcome(
    val requestId: String?,
    val message: String
)

data class ResetPasswordOutcome(
    val requestId: String? = null
)

data class UserSession(
    val id: String,
    val shortId: String,
    val formattedCreatedAt: String,
    val formattedLastSeenAt: String,
    val isCurrent: Boolean,
    val isRevoked: Boolean
)

data class SessionsPage(
    val items: List<UserSession>,
    val nextCursor: String?
)

interface AuthRepository {
    val sessionEffects: SharedFlow<AuthEffect>
        get() = MutableSharedFlow()

    suspend fun register(request: RegisterRequest): AuthResult<RegisterOutcome>

    suspend fun login(request: LoginRequest): AuthResult<LoginOutcome>

    suspend fun refreshSession(): AuthResult<RefreshOutcome>

    suspend fun logout(): AuthResult<Unit> = AuthResult.Success(Unit)

    suspend fun requestPasswordReset(email: String): AuthResult<ForgotPasswordOutcome> =
        AuthResult.Failure(AuthError(AuthErrorKind.UNKNOWN, "No implementado"))

    suspend fun resetPassword(token: String, newPassword: String): AuthResult<ResetPasswordOutcome> =
        AuthResult.Failure(AuthError(AuthErrorKind.UNKNOWN, "No implementado"))

    suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): AuthResult<ChangePasswordResponse> =
        AuthResult.Failure(AuthError(AuthErrorKind.UNKNOWN, "No implementado"))

    suspend fun getSessions(
        limit: Int = 25,
        cursor: String? = null
    ): AuthResult<SessionsPage> =
        AuthResult.Failure(AuthError(AuthErrorKind.UNKNOWN, "No implementado"))
}

class AuthRepositoryImpl(
    private val api: AuthApi,
    private val tokenStorage: TokenStorage
) : AuthRepository {

    private val refreshMutex = Mutex()
    private var activeRefresh: CompletableDeferred<AuthResult<RefreshOutcome>>? = null

    private val _sessionEffects = MutableSharedFlow<AuthEffect>(
        replay = 0,
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val sessionEffects: SharedFlow<AuthEffect> = _sessionEffects.asSharedFlow()

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

    override suspend fun logout(): AuthResult<Unit> {
        val currentSession = tokenStorage.current()
        if (currentSession == null) {
            tokenStorage.clear()
            _sessionEffects.tryEmit(AuthEffect.NavigateToLogin)
            return AuthResult.Success(Unit)
        }

        try {
            api.logout(currentSession.accessToken)
        } catch (_: Throwable) {
            // Failsafe: la pérdida de conectividad o error de servidor no debe impedir la salida local
        } finally {
            tokenStorage.clear()
            _sessionEffects.tryEmit(AuthEffect.NavigateToLogin)
        }
        return AuthResult.Success(Unit)
    }

    override suspend fun requestPasswordReset(email: String): AuthResult<ForgotPasswordOutcome> {
        val normalizedEmail = email.trim().lowercase()
        return try {
            val response = api.forgotPassword(ForgotPasswordRequest(email = normalizedEmail))
            AuthResult.Success(
                ForgotPasswordOutcome(
                    requestId = response.requestId,
                    message = "Si la dirección puede recibir correos, revisa tu bandeja de entrada"
                )
            )
        } catch (error: Throwable) {
            AuthResult.Failure(error.toAuthError())
        }
    }

    override suspend fun resetPassword(token: String, newPassword: String): AuthResult<ResetPasswordOutcome> {
        return try {
            val response = api.resetPassword(
                ResetPasswordRequest(
                    token = token.trim(),
                    newPassword = newPassword
                )
            )
            // Purga de sesión local tras 200 OK (Cambio de época de sesión)
            tokenStorage.clear()
            AuthResult.Success(ResetPasswordOutcome(requestId = response.requestId))
        } catch (error: Throwable) {
            AuthResult.Failure(error.toAuthError())
        }
    }

    override suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): AuthResult<ChangePasswordResponse> {
        val currentSession = tokenStorage.current()
            ?: return AuthResult.Failure(
                AuthError(
                    kind = AuthErrorKind.INVALID_CREDENTIALS,
                    message = "Sesión no disponible o expirada (401).",
                    statusCode = 401
                )
            )

        return try {
            val response = api.changePassword(
                accessToken = currentSession.accessToken,
                request = ChangePasswordRequest(
                    currentPassword = currentPassword,
                    newPassword = newPassword
                )
            )
            // Crítico post-cambio (E007): el backend incrementa la época de sesión y revoca
            // todos los JWT anteriores. Por tanto, la app móvil DEBE purgar inmediatamente
            // los tokens y la sesión local.
            tokenStorage.clear()
            _sessionEffects.tryEmit(AuthEffect.NavigateToLogin)
            AuthResult.Success(response)
        } catch (error: Throwable) {
            AuthResult.Failure(error.toAuthError())
        }
    }

    override suspend fun getSessions(
        limit: Int,
        cursor: String?
    ): AuthResult<SessionsPage> {
        val currentSession = tokenStorage.current()
            ?: return AuthResult.Failure(
                AuthError(
                    kind = AuthErrorKind.INVALID_CREDENTIALS,
                    message = "No hay sesión activa.",
                    statusCode = 401
                )
            )

        return try {
            val response = api.getSessions(
                accessToken = currentSession.accessToken,
                limit = limit,
                cursor = cursor
            )
            val domainItems = response.items.map { item ->
                UserSession(
                    id = item.id,
                    shortId = "Sesión #" + (if (item.id.length >= 8) item.id.take(8) else item.id),
                    formattedCreatedAt = formatSessionTimestamp(item.createdAt, isCreatedAt = true),
                    formattedLastSeenAt = formatSessionTimestamp(item.lastSeenAt, isCreatedAt = false),
                    isCurrent = item.current,
                    isRevoked = item.revoked
                )
            }
            // En la primera página (cursor == null), asegurar que la sesión actual encabece la lista
            val sortedItems = if (cursor == null) {
                domainItems.sortedByDescending { it.isCurrent }
            } else {
                domainItems
            }
            AuthResult.Success(
                SessionsPage(
                    items = sortedItems,
                    nextCursor = response.nextCursor
                )
            )
        } catch (error: Throwable) {
            val authError = error.toAuthError()
            if (authError.statusCode == 401) {
                tokenStorage.clear()
                _sessionEffects.tryEmit(AuthEffect.NavigateToLogin)
            }
            AuthResult.Failure(authError)
        }
    }

    private fun formatSessionTimestamp(isoTimestamp: String, isCreatedAt: Boolean): String {
        return try {
            val instant = java.time.Instant.parse(isoTimestamp)
            val zonedDateTime = instant.atZone(java.time.ZoneId.systemDefault())
            val formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            val formattedDate = zonedDateTime.format(formatter)
            if (isCreatedAt) {
                "Iniciada: $formattedDate"
            } else {
                "Última actividad: $formattedDate"
            }
        } catch (_: Exception) {
            if (isCreatedAt) "Iniciada: $isoTimestamp" else "Última actividad: $isoTimestamp"
        }
    }

    override suspend fun refreshSession(): AuthResult<RefreshOutcome> {
        val (deferred, isLeader) = refreshMutex.withLock {
            val inFlight = activeRefresh
            if (inFlight != null) {
                inFlight to false
            } else {
                val newDeferred = CompletableDeferred<AuthResult<RefreshOutcome>>()
                activeRefresh = newDeferred
                newDeferred to true
            }
        }

        if (!isLeader) {
            return deferred.await()
        }

        val result = executeRefresh()
        deferred.complete(result)
        refreshMutex.withLock {
            activeRefresh = null
        }
        return result
    }

    private suspend fun executeRefresh(): AuthResult<RefreshOutcome> {
        val currentSession = tokenStorage.current()
            ?: return AuthResult.Failure(
                AuthError(AuthErrorKind.INVALID_CREDENTIALS, "No hay sesión activa para renovar.", retryable = false)
            )

        return try {
            val response = api.refresh(
                RefreshRequest(
                    refreshToken = currentSession.refreshToken,
                    deviceId = currentSession.sessionId
                )
            )
            val updatedSession = currentSession.copy(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken,
                expiresIn = response.expiresIn,
                sessionId = response.resolvedSessionId ?: currentSession.sessionId
            )
            tokenStorage.save(updatedSession)
            AuthResult.Success(RefreshOutcome(updatedSession))
        } catch (error: Throwable) {
            val authError = error.toAuthError()
            // 401 Unauthorized y 409 Conflict (Replay / Revocación de familia):
            // Ambos deben mapearse a invalidación definitiva de sesión (AuthErrorKind.INVALID_CREDENTIALS / no retryable),
            // purgar tokenStorage de inmediato y disparar AuthEffect.NavigateToLogin.
            val finalError = if (authError.kind == AuthErrorKind.CONFLICT) {
                AuthError(AuthErrorKind.INVALID_CREDENTIALS, authError.message, retryable = false)
            } else {
                authError
            }

            if (finalError.kind == AuthErrorKind.INVALID_CREDENTIALS) {
                tokenStorage.clear()
                _sessionEffects.tryEmit(AuthEffect.NavigateToLogin)
            }
            AuthResult.Failure(finalError)
        }
    }

    private fun Throwable.toAuthError(): AuthError = when (this) {
        is CancellationException -> throw this
        is AuthApiException -> AuthError(kind, message, retryable, attemptsRemaining, retryAfterSeconds, statusCode)
        is java.io.IOException -> AuthError(AuthErrorKind.NETWORK, "Sin conexión. Intenta de nuevo.", retryable = true)
        else -> AuthError(AuthErrorKind.UNKNOWN, "Error inesperado. Intenta de nuevo.", retryable = true)
    }

    private fun LoginResponse.toSession() = Session(
        accessToken = accessToken,
        refreshToken = refreshToken,
        expiresIn = expiresIn,
        user = user,
        sessionId = sessionId ?: session?.id
    )
}
