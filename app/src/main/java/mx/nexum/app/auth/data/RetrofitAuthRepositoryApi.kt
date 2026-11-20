
package mx.nexum.app.auth.data

import java.util.UUID
import mx.nexum.app.auth.data.dto.ChangePasswordRequest
import mx.nexum.app.auth.data.dto.ChangePasswordRequestDto
import mx.nexum.app.auth.data.dto.ChangePasswordResponse
import mx.nexum.app.auth.data.dto.ClientDto
import mx.nexum.app.auth.data.dto.ForgotPasswordRequest
import mx.nexum.app.auth.data.dto.ForgotPasswordRequestDto
import mx.nexum.app.auth.data.dto.ForgotPasswordResponse
import mx.nexum.app.auth.data.dto.IncomeDto
import mx.nexum.app.auth.data.dto.LoginRequest
import mx.nexum.app.auth.data.dto.LoginRequestDto
import mx.nexum.app.auth.data.dto.LoginResponse
import mx.nexum.app.auth.data.dto.PrivacyDto
import mx.nexum.app.auth.data.dto.RecurringExpenseDto
import mx.nexum.app.auth.data.dto.RefreshRequest
import mx.nexum.app.auth.data.dto.RefreshRequestDto
import mx.nexum.app.auth.data.dto.RefreshResponse
import mx.nexum.app.auth.data.dto.RegisterRequest
import mx.nexum.app.auth.data.dto.RegisterRequestDto
import mx.nexum.app.auth.data.dto.RegisterResponse
import mx.nexum.app.auth.data.dto.ResetPasswordRequest
import mx.nexum.app.auth.data.dto.ResetPasswordRequestDto
import mx.nexum.app.auth.data.dto.ResetPasswordResponse
import mx.nexum.app.auth.data.dto.SessionItemResponse
import mx.nexum.app.auth.data.dto.SessionsPageResponse
import mx.nexum.app.auth.data.dto.User
import mx.nexum.app.auth.data.network.AuthApiService
import mx.nexum.app.auth.data.network.ClientInfoProvider
import mx.nexum.app.auth.data.network.NetworkModule

class RetrofitAuthRepositoryApi(
    private val service: AuthApiService = NetworkModule.authApiService
) : AuthApi {

    override suspend fun register(request: RegisterRequest): RegisterResponse {
        val dto = RegisterRequestDto(
            name = request.name,
            email = request.email,
            password = request.password,
            income = IncomeDto(
                amount = request.income.amount,
                frequency = request.income.frequency.name
            ),
            recurringExpenses = request.recurringExpenses.map {
                RecurringExpenseDto(
                    concept = it.concept,
                    amount = it.amount,
                    frequency = it.frequency.name,
                    category = it.categoryId,
                    type = it.type.name
                )
            },
            privacy = PrivacyDto(
                accepted = true,
                noticeVersion = request.privacyNoticeVersion
            ),
            client = ClientDto(
                locale = ClientInfoProvider.getLocale(),
                timeZone = ClientInfoProvider.getTimeZone()
            )
        )

        val response = service.register(
            idempotencyKey = UUID.randomUUID().toString(),
            request = dto
        )
        val body = response.body()
            ?: throw AuthApiException(
                kind = AuthErrorKind.UNKNOWN,
                message = "Error al registrar usuario (${response.code()})",
                retryable = response.code() >= 500
            )

        return RegisterResponse(
            userId = body.userId ?: body.id ?: "",
            onboardingStatus = body.onboardingStatus ?: body.status ?: "pending",
            accessToken = body.accessToken,
            refreshToken = body.refreshToken,
            verificationRequired = false
        )
    }

    override suspend fun login(request: LoginRequest): LoginResponse {
        val dto = LoginRequestDto(
            email = request.email,
            password = request.password,
            deviceId = request.deviceId,
            deviceName = request.deviceName,
            platform = request.platform
        )

        val response = service.login(request = dto)
        val body = response.body()
            ?: throw AuthApiException(
                kind = if (response.code() == 401) AuthErrorKind.INVALID_CREDENTIALS else AuthErrorKind.UNKNOWN,
                message = "Error al iniciar sesiÃ³n (${response.code()})",
                retryable = response.code() >= 500
            )

        return LoginResponse(
            accessToken = body.accessToken,
            refreshToken = body.refreshToken,
            tokenType = body.tokenType,
            expiresIn = body.expiresIn,
            sessionId = body.sessionId,
            session = body.session,
            user = User(
                id = body.user.id,
                name = body.user.name,
                email = body.user.email,
                emailVerified = body.user.emailVerified
            ),
            onboarding = body.onboarding
        )
    }

    override suspend fun refresh(request: RefreshRequest): RefreshResponse {
        val dto = RefreshRequestDto(
            refreshToken = request.refreshToken,
            deviceId = request.deviceId
        )

        val response = service.refresh(request = dto)
        val body = response.body()
            ?: throw when (response.code()) {
                401 -> AuthApiException(
                    kind = AuthErrorKind.INVALID_CREDENTIALS,
                    message = "Sesión expirada o token inválido (401)",
                    retryable = false,
                    statusCode = 401
                )
                409 -> AuthApiException(
                    kind = AuthErrorKind.CONFLICT,
                    message = "Token de actualización reutilizado detectado (409)",
                    retryable = false,
                    statusCode = 409
                )
                503 -> AuthApiException(
                    kind = AuthErrorKind.SERVER_UNAVAILABLE,
                    message = "Servicio de autenticación no disponible temporalmente (503)",
                    retryable = true,
                    statusCode = 503
                )
                else -> AuthApiException(
                    kind = if (response.code() >= 500) AuthErrorKind.SERVER_UNAVAILABLE else AuthErrorKind.UNKNOWN,
                    message = "Error al renovar sesión (${response.code()})",
                    retryable = response.code() >= 500,
                    statusCode = response.code()
                )
            }

        return RefreshResponse(
            accessToken = body.accessToken,
            refreshToken = body.refreshToken,
            tokenType = body.tokenType ?: "Bearer",
            expiresIn = body.expiresIn,
            sessionId = body.sessionId,
            session = body.session
        )
    }

    override suspend fun logout(accessToken: String) {
        val authHeader = if (accessToken.startsWith("Bearer ")) accessToken else "Bearer $accessToken"
        val response = service.logout(authorization = authHeader)
        if (!response.isSuccessful && response.code() != 401) {
            throw AuthApiException(
                kind = if (response.code() >= 500) AuthErrorKind.SERVER_UNAVAILABLE else AuthErrorKind.UNKNOWN,
                message = "Error al cerrar sesión (${response.code()})",
                retryable = response.code() >= 500,
                statusCode = response.code()
            )
        }
    }

    override suspend fun forgotPassword(request: ForgotPasswordRequest): ForgotPasswordResponse {
        val dto = ForgotPasswordRequestDto(
            email = request.email.trim().lowercase()
        )
        val response = service.forgotPassword(request = dto)
        if (response.code() == 429) {
            val retryAfterHeader = response.headers()["Retry-After"]
            val retryAfterSeconds = retryAfterHeader?.toIntOrNull() ?: 60
            throw AuthApiException(
                kind = AuthErrorKind.RATE_LIMITED,
                message = "Demasiados intentos. Intenta más tarde.",
                retryable = true,
                statusCode = 429,
                retryAfterSeconds = retryAfterSeconds
            )
        }

        if (!response.isSuccessful && response.code() != 202) {
            throw AuthApiException(
                kind = if (response.code() >= 500) AuthErrorKind.SERVER_UNAVAILABLE else AuthErrorKind.UNKNOWN,
                message = "Error al solicitar recuperación de contraseña (${response.code()})",
                retryable = response.code() >= 500,
                statusCode = response.code()
            )
        }

        val body = response.body()
        return ForgotPasswordResponse(
            status = body?.status ?: "accepted",
            requestId = body?.requestId,
            message = body?.message
        )
    }

    override suspend fun resetPassword(request: ResetPasswordRequest): ResetPasswordResponse {
        val dto = ResetPasswordRequestDto(
            token = request.token,
            newPassword = request.newPassword
        )
        val response = service.resetPassword(request = dto)
        if (!response.isSuccessful) {
            val code = response.code()
            throw when (code) {
                400 -> AuthApiException(
                    kind = AuthErrorKind.INVALID_CREDENTIALS,
                    message = "El código de recuperación ha expirado o es inválido.",
                    statusCode = 400
                )
                409 -> AuthApiException(
                    kind = AuthErrorKind.CONFLICT,
                    message = "Este código de recuperación ya fue utilizado. Solicita uno nuevo.",
                    statusCode = 409
                )
                422 -> AuthApiException(
                    kind = AuthErrorKind.VALIDATION,
                    message = "La contraseña debe tener entre 12 y 128 caracteres.",
                    statusCode = 422
                )
                else -> AuthApiException(
                    kind = if (code >= 500) AuthErrorKind.SERVER_UNAVAILABLE else AuthErrorKind.UNKNOWN,
                    message = "Error al restablecer la contraseña ($code)",
                    retryable = code >= 500,
                    statusCode = code
                )
            }
        }
        val body = response.body()
        return ResetPasswordResponse(
            status = body?.status ?: "accepted",
            requestId = body?.requestId
        )
    }

    override suspend fun changePassword(
        accessToken: String,
        request: ChangePasswordRequest
    ): ChangePasswordResponse {
        val authHeader = if (accessToken.startsWith("Bearer ")) accessToken else "Bearer $accessToken"
        val dto = ChangePasswordRequestDto(
            currentPassword = request.currentPassword,
            newPassword = request.newPassword
        )
        val response = service.changePassword(
            authorization = authHeader,
            request = dto
        )
        if (!response.isSuccessful) {
            val code = response.code()
            throw when (code) {
                400, 401 -> AuthApiException(
                    kind = AuthErrorKind.INVALID_CREDENTIALS,
                    message = "La contraseña actual no es correcta.",
                    statusCode = code
                )
                409 -> AuthApiException(
                    kind = AuthErrorKind.CONFLICT,
                    message = "La nueva contraseña no puede ser igual a la actual.",
                    statusCode = 409
                )
                422 -> AuthApiException(
                    kind = AuthErrorKind.VALIDATION,
                    message = "La nueva contraseña debe tener al menos 12 caracteres.",
                    statusCode = 422
                )
                else -> AuthApiException(
                    kind = if (code >= 500) AuthErrorKind.SERVER_UNAVAILABLE else AuthErrorKind.UNKNOWN,
                    message = "Error al cambiar la contraseña ($code)",
                    retryable = code >= 500,
                    statusCode = code
                )
            }
        }
        val body = response.body()
        return ChangePasswordResponse(
            status = body?.status ?: "accepted",
            requestId = body?.requestId ?: UUID.randomUUID().toString()
        )
    }

    override suspend fun getSessions(
        accessToken: String,
        limit: Int?,
        cursor: String?
    ): SessionsPageResponse {
        val authHeader = if (accessToken.startsWith("Bearer ")) accessToken else "Bearer $accessToken"
        val response = service.getSessions(
            authorization = authHeader,
            limit = limit,
            cursor = cursor
        )
        if (!response.isSuccessful) {
            val code = response.code()
            throw when (code) {
                400 -> AuthApiException(
                    kind = AuthErrorKind.VALIDATION,
                    message = "Cursor o parámetros de paginación inválidos.",
                    statusCode = 400
                )
                401 -> AuthApiException(
                    kind = AuthErrorKind.INVALID_CREDENTIALS,
                    message = "Sesión expirada o no autorizada.",
                    statusCode = 401
                )
                else -> AuthApiException(
                    kind = if (code >= 500) AuthErrorKind.SERVER_UNAVAILABLE else AuthErrorKind.UNKNOWN,
                    message = "Error al consultar sesiones ($code)",
                    retryable = code >= 500,
                    statusCode = code
                )
            }
        }
        val body = response.body()
        val items = body?.items?.map { dto ->
            SessionItemResponse(
                id = dto.id,
                createdAt = dto.createdAt,
                lastSeenAt = dto.lastSeenAt,
                current = dto.current,
                revoked = dto.revoked
            )
        } ?: emptyList()
        return SessionsPageResponse(
            items = items,
            nextCursor = body?.nextCursor
        )
    }
}
