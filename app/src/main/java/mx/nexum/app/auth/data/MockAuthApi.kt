package mx.nexum.app.auth.data

import kotlinx.coroutines.delay
import java.util.UUID
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
import mx.nexum.app.auth.data.dto.SessionIdDto
import mx.nexum.app.auth.data.dto.User

class MockAuthApi(
    private val delayRange: LongRange = 800L..1500L,
    private val maxLoginAttempts: Int = 5
) : AuthApi {

    private data class TokenFamily(
        val familyId: String,
        var activeRefreshToken: String,
        var version: Int = 1,
        var deviceId: String? = null,
        var isRevoked: Boolean = false
    )

    private val lock = Any()
    private val credentials = linkedMapOf(
        "demo@nexum.test" to ("Demo1234" to "Alex Demo"),
        "taken@nexum.test" to ("Taken1234" to "Correo Ya Registrado")
    )
    private var loginAttempts = 0
    private var userCounter = 0

    private val families = mutableMapOf<String, TokenFamily>()
    private val tokenToFamilyId = mutableMapOf<String, String>()
    private val accessTokenToFamilyId = mutableMapOf<String, String>()
    private val accessTokenToEmail = mutableMapOf<String, String>()
    private val familyIdToEmail = mutableMapOf<String, String>()
    private val consumedRefreshTokens = mutableSetOf<String>()
    private val revokedAccessTokens = mutableSetOf<String>()
    private val usedResetTokens = mutableSetOf<String>()

    override suspend fun register(request: RegisterRequest): RegisterResponse {
        simulateNetwork()
        val email = request.email.trim().lowercase()
        if (email == NETWORK_ERROR_EMAIL) {
            throw AuthApiException(AuthErrorKind.NETWORK, "Sin conexión. Intenta de nuevo.", retryable = true)
        }
        synchronized(lock) {
            if (credentials.containsKey(email)) {
                throw AuthApiException(AuthErrorKind.CONFLICT, "El correo ya está registrado")
            }
            userCounter += 1
            val userId = "user-$userCounter"
            credentials[email] = request.password to request.name

            val accessToken = "mock-access-$userId"
            val refreshToken = "mock-refresh-$userId"
            val familyId = "family-$userId"
            tokenToFamilyId[refreshToken] = familyId
            accessTokenToFamilyId[accessToken] = familyId
            accessTokenToEmail[accessToken] = email
            familyIdToEmail[familyId] = email
            families[familyId] = TokenFamily(
                familyId = familyId,
                activeRefreshToken = refreshToken,
                version = 1
            )

            return RegisterResponse(
                userId = userId,
                onboardingStatus = "pending",
                accessToken = accessToken,
                refreshToken = refreshToken,
                expiresIn = 3600,
                verificationRequired = false
            )
        }
    }

    override suspend fun login(request: LoginRequest): LoginResponse {
        simulateNetwork()
        val email = request.email.trim().lowercase()
        if (email == NETWORK_ERROR_EMAIL) {
            throw AuthApiException(AuthErrorKind.NETWORK, "Sin conexión. Intenta de nuevo.", retryable = true)
        }
        synchronized(lock) {
            if (loginAttempts >= maxLoginAttempts) {
                throw AuthApiException(
                    AuthErrorKind.RATE_LIMITED,
                    "Demasiados intentos. Intenta más tarde.",
                    attemptsRemaining = 0
                )
            }
            val stored = credentials[email]
            if (stored == null || stored.first != request.password) {
                loginAttempts += 1
                throw AuthApiException(
                    AuthErrorKind.INVALID_CREDENTIALS,
                    "Correo o contraseña incorrectos",
                    attemptsRemaining = (maxLoginAttempts - loginAttempts).coerceAtLeast(0)
                )
            }
            loginAttempts = 0
            val userId = "user-${email.substringBefore('@')}"
            val accessToken = "mock-access-$userId"
            val refreshToken = "mock-refresh-$userId"
            val familyId = "family-$userId"
            tokenToFamilyId[refreshToken] = familyId
            accessTokenToFamilyId[accessToken] = familyId
            accessTokenToEmail[accessToken] = email
            familyIdToEmail[familyId] = email
            families[familyId] = TokenFamily(
                familyId = familyId,
                activeRefreshToken = refreshToken,
                version = 1,
                deviceId = request.deviceId
            )

            return LoginResponse(
                accessToken = accessToken,
                refreshToken = refreshToken,
                expiresIn = 3600,
                sessionId = "session-$userId",
                session = SessionIdDto("session-$userId"),
                user = User(id = userId, name = stored.second, email = email)
            )
        }
    }

    override suspend fun refresh(request: RefreshRequest): RefreshResponse {
        simulateNetwork()

        val token = request.refreshToken.trim()
        if (token == NETWORK_ERROR_TOKEN) {
            throw AuthApiException(
                kind = AuthErrorKind.NETWORK,
                message = "Error de conexión al renovar sesión.",
                retryable = true
            )
        }
        if (token == SERVICE_UNAVAILABLE_TOKEN) {
            throw AuthApiException(
                kind = AuthErrorKind.SERVER_UNAVAILABLE,
                message = "Servicio de autenticación no disponible temporalmente (503).",
                retryable = true,
                statusCode = 503
            )
        }
        if (token == EXPIRED_TOKEN) {
            throw AuthApiException(
                kind = AuthErrorKind.INVALID_CREDENTIALS,
                message = "Refresh token expirado (401).",
                statusCode = 401
            )
        }

        synchronized(lock) {
            val familyId = tokenToFamilyId[token]
            val family = if (familyId != null) families[familyId] else null

            if (family == null) {
                throw AuthApiException(
                    kind = AuthErrorKind.INVALID_CREDENTIALS,
                    message = "Refresh token desconocido o inválido (401).",
                    statusCode = 401
                )
            }

            // 1. Si la familia ya fue revocada (por logout o replay previo), responder 401
            if (family.isRevoked) {
                throw AuthApiException(
                    kind = AuthErrorKind.INVALID_CREDENTIALS,
                    message = "Sesión o familia de tokens revocada (401).",
                    statusCode = 401
                )
            }

            // 2. Detección de replay: token ya consumido en una rotación previa -> 409 y revoca familia
            if (consumedRefreshTokens.contains(token)) {
                family.isRevoked = true
                throw AuthApiException(
                    kind = AuthErrorKind.CONFLICT,
                    message = "Refresh token ya utilizado (detección de replay). Sesión y familia revocadas.",
                    statusCode = 409
                )
            }

            if (family.activeRefreshToken != token) {
                family.isRevoked = true
                throw AuthApiException(
                    kind = AuthErrorKind.INVALID_CREDENTIALS,
                    message = "Refresh token no coincide con el activo. Familia revocada (401).",
                    statusCode = 401
                )
            }

            if (family.deviceId != null && request.deviceId != null && family.deviceId != request.deviceId) {
                throw AuthApiException(
                    kind = AuthErrorKind.INVALID_CREDENTIALS,
                    message = "DeviceId no coincide con el contexto de la sesión (401).",
                    statusCode = 401
                )
            }
            if (family.deviceId == null && request.deviceId != null) {
                family.deviceId = request.deviceId
            }

            consumedRefreshTokens.add(token)
            family.version += 1
            val newAccessToken = "mock-access-${family.familyId}-v${family.version}"
            val newRefreshToken = "mock-refresh-${family.familyId}-v${family.version}"
            family.activeRefreshToken = newRefreshToken
            tokenToFamilyId[newRefreshToken] = family.familyId
            accessTokenToFamilyId[newAccessToken] = family.familyId
            val familyEmail = familyIdToEmail[family.familyId]
            if (familyEmail != null) {
                accessTokenToEmail[newAccessToken] = familyEmail
            }

            val sessionId = "session-${family.familyId}"
            return RefreshResponse(
                accessToken = newAccessToken,
                refreshToken = newRefreshToken,
                tokenType = "Bearer",
                expiresIn = 900L,
                sessionId = sessionId,
                session = SessionIdDto(sessionId)
            )
        }
    }

    override suspend fun logout(accessToken: String) {
        val delayMs = (300L..800L).random()
        delay(delayMs)

        if (accessToken == NETWORK_ERROR_LOGOUT_TOKEN || accessToken.contains(NETWORK_ERROR_LOGOUT_TOKEN)) {
            throw AuthApiException(
                kind = AuthErrorKind.NETWORK,
                message = "Error de conexión al procesar el cierre de sesión.",
                retryable = true
            )
        }

        synchronized(lock) {
            if (revokedAccessTokens.contains(accessToken)) {
                throw AuthApiException(
                    kind = AuthErrorKind.INVALID_CREDENTIALS,
                    message = "La sesión ya había sido revocada (401).",
                    statusCode = 401
                )
            }
            revokedAccessTokens.add(accessToken)
            val familyId = accessTokenToFamilyId[accessToken]
            if (familyId != null) {
                families[familyId]?.isRevoked = true
            }
        }
    }

    override suspend fun forgotPassword(request: ForgotPasswordRequest): ForgotPasswordResponse {
        val delayMs = (800L..1200L).random()
        delay(delayMs)

        val email = request.email.trim().lowercase()
        if (email == NETWORK_ERROR_EMAIL) {
            throw AuthApiException(
                kind = AuthErrorKind.NETWORK,
                message = "Sin conexión. Intenta de nuevo.",
                retryable = true
            )
        }
        if (email == RATE_LIMITED_EMAIL) {
            throw AuthApiException(
                kind = AuthErrorKind.RATE_LIMITED,
                message = "Demasiados intentos. Intenta más tarde.",
                statusCode = 429,
                retryAfterSeconds = 60
            )
        }

        return ForgotPasswordResponse(
            status = "accepted",
            requestId = "mock-req-${UUID.randomUUID()}"
        )
    }

    override suspend fun resetPassword(request: ResetPasswordRequest): ResetPasswordResponse {
        val delayMs = (800L..1200L).random()
        delay(delayMs)

        val token = request.token.trim()
        val newPassword = request.newPassword

        if (token == NETWORK_ERROR_TOKEN) {
            throw AuthApiException(
                kind = AuthErrorKind.NETWORK,
                message = "Error de red al restablecer la contraseña.",
                retryable = true
            )
        }

        if (token.length < 10) {
            throw AuthApiException(
                kind = AuthErrorKind.VALIDATION,
                message = "El código debe tener al menos 10 caracteres.",
                statusCode = 400
            )
        }

        if (token == EXPIRED_RESET_TOKEN) {
            throw AuthApiException(
                kind = AuthErrorKind.INVALID_CREDENTIALS,
                message = "El código de recuperación ha expirado o es inválido.",
                statusCode = 400
            )
        }

        synchronized(lock) {
            if (usedResetTokens.contains(token)) {
                throw AuthApiException(
                    kind = AuthErrorKind.CONFLICT,
                    message = "El código de recuperación ya fue utilizado.",
                    statusCode = 409
                )
            }

            if (newPassword.length !in 12..128) {
                throw AuthApiException(
                    kind = AuthErrorKind.VALIDATION,
                    message = "La contraseña debe tener entre 12 y 128 caracteres.",
                    statusCode = 422
                )
            }

            usedResetTokens.add(token)
            // Revocar familias de sesión activas en memoria al restablecer (cambio de época de sesión)
            families.values.forEach { it.isRevoked = true }

            return ResetPasswordResponse(
                status = "accepted",
                requestId = "mock-reset-${UUID.randomUUID()}"
            )
        }
    }

    override suspend fun changePassword(
        accessToken: String,
        request: ChangePasswordRequest
    ): ChangePasswordResponse {
        val delayMs = (600L..1000L).random()
        delay(delayMs)

        if (accessToken == NETWORK_ERROR_TOKEN || accessToken.contains(NETWORK_ERROR_TOKEN) || accessToken == NETWORK_ERROR_LOGOUT_TOKEN) {
            throw AuthApiException(
                kind = AuthErrorKind.NETWORK,
                message = "Error de red al cambiar la contraseña.",
                retryable = true
            )
        }

        if (accessToken == EXPIRED_TOKEN || revokedAccessTokens.contains(accessToken)) {
            throw AuthApiException(
                kind = AuthErrorKind.INVALID_CREDENTIALS,
                message = "La sesión es inválida o ha expirado (401).",
                statusCode = 401
            )
        }

        synchronized(lock) {
            val email = accessTokenToEmail[accessToken] ?: DEMO_EMAIL
            val expectedPassword = credentials[email]?.first ?: DEMO_PASSWORD

            if (request.currentPassword != expectedPassword) {
                throw AuthApiException(
                    kind = AuthErrorKind.INVALID_CREDENTIALS,
                    message = "La contraseña actual no es correcta",
                    statusCode = 401
                )
            }

            if (request.newPassword.length < 12) {
                throw AuthApiException(
                    kind = AuthErrorKind.VALIDATION,
                    message = "La nueva contraseña debe tener al menos 12 caracteres.",
                    statusCode = 422
                )
            }

            if (request.newPassword == request.currentPassword) {
                throw AuthApiException(
                    kind = AuthErrorKind.CONFLICT,
                    message = "La nueva contraseña no puede ser igual a la actual.",
                    statusCode = 409
                )
            }

            val existing = credentials[email]
            if (existing != null) {
                credentials[email] = request.newPassword to existing.second
            }

            revokedAccessTokens.add(accessToken)
            val familyId = accessTokenToFamilyId[accessToken]
            if (familyId != null) {
                families[familyId]?.isRevoked = true
            }

            return ChangePasswordResponse(
                status = "accepted",
                requestId = "mock-chg-${UUID.randomUUID()}"
            )
        }
    }

    fun isAccessTokenRevoked(accessToken: String): Boolean = synchronized(lock) {
        revokedAccessTokens.contains(accessToken)
    }

    private suspend fun simulateNetwork() {
        delay(delayRange.random())
    }

    companion object {
        const val NETWORK_ERROR_EMAIL = "net@error.test"
        const val RATE_LIMITED_EMAIL = "rate_limited@error.test"
        const val DEMO_EMAIL = "demo@nexum.test"
        const val DEMO_PASSWORD = "Demo1234"
        const val TAKEN_EMAIL = "taken@nexum.test"

        const val EXPIRED_TOKEN = "expired_token"
        const val EXPIRED_RESET_TOKEN = "expired_reset_token"
        const val NETWORK_ERROR_TOKEN = "net_error_token"
        const val SERVICE_UNAVAILABLE_TOKEN = "service_unavailable_token"
        const val NETWORK_ERROR_LOGOUT_TOKEN = "net_error_logout"
    }
}