package mx.nexum.app.auth.data

import com.google.gson.Gson
import mx.nexum.app.auth.data.dto.*
import mx.nexum.app.auth.data.network.AuthApiService
import java.io.IOException
import java.util.UUID

class RetrofitAuthApi(
    private val apiService: AuthApiService,
    private val gson: Gson = Gson()
) : AuthApi {

    override suspend fun register(request: RegisterRequest): RegisterResponse {
        val idempotencyKey = UUID.randomUUID().toString()
        val requestDto = RegisterRequestDto(
            name = request.name,
            email = request.email,
            password = request.password,
            income = IncomeDto(amount = request.income.amount, frequency = request.income.frequency.name),
            recurringExpenses = request.recurringExpenses.map {
                RecurringExpenseDto(
                    concept = it.concept,
                    amount = it.amount,
                    frequency = it.frequency.name,
                    category = it.categoryId.ifBlank { "Servicios" },
                    type = it.type.name
                )
            },
            privacy = PrivacyDto(accepted = true, noticeVersion = request.privacyNoticeVersion),
            client = ClientDto(locale = "es-MX", timeZone = "America/Mexico_City")
        )

        val response = try {
            apiService.register(idempotencyKey, requestDto)
        } catch (e: IOException) {
            throw AuthApiException(AuthErrorKind.NETWORK, "Sin conexión o error de red.", retryable = true)
        }

        if (response.isSuccessful && response.body() != null) {
            val body = response.body()!!
            return RegisterResponse(
                userId = body.userId ?: body.id ?: "unknown-id",
                onboardingStatus = body.onboardingStatus ?: "pending",
                accessToken = body.accessToken,
                refreshToken = body.refreshToken,
                verificationRequired = false
            )
        } else {
            val errorBodyString = response.errorBody()?.string()
            val apiError = parseError(errorBodyString)
            val code = response.code()
            val errorKind = when (code) {
                409 -> AuthErrorKind.CONFLICT
                422 -> AuthErrorKind.UNKNOWN
                429 -> AuthErrorKind.RATE_LIMITED
                else -> AuthErrorKind.UNKNOWN
            }
            val msg = apiError?.error?.message ?: "Error en el registro ($code)"
            throw AuthApiException(errorKind, msg, retryable = code >= 500)
        }
    }

    override suspend fun login(request: LoginRequest): LoginResponse {
        val requestDto = LoginRequestDto(
            email = request.email,
            password = request.password,
            deviceId = request.deviceId,
            deviceName = request.deviceName ?: "Android",
            platform = request.platform ?: "android"
        )

        val response = try {
            apiService.login(requestDto)
        } catch (e: IOException) {
            throw AuthApiException(AuthErrorKind.NETWORK, "Sin conexión o error de red.", retryable = true)
        }

        if (response.isSuccessful && response.body() != null) {
            val dto = response.body()!!
            return LoginResponse(
                accessToken = dto.accessToken,
                refreshToken = dto.refreshToken,
                tokenType = dto.tokenType,
                expiresIn = dto.expiresIn,
                sessionId = dto.sessionId ?: dto.session?.id,
                session = dto.session,
                user = User(
                    id = dto.user.id,
                    name = dto.user.name,
                    email = dto.user.email,
                    emailVerified = dto.user.emailVerified
                ),
                onboarding = dto.onboarding
            )
        } else {
            val code = response.code()
            val errorBodyString = response.errorBody()?.string()
            val apiError = parseError(errorBodyString)
            val serverMessage = apiError?.error?.message

            val errorKind = when (code) {
                401 -> AuthErrorKind.INVALID_CREDENTIALS
                429 -> AuthErrorKind.RATE_LIMITED
                in 500..599 -> AuthErrorKind.UNKNOWN
                else -> AuthErrorKind.UNKNOWN
            }

            val msg = when (code) {
                401 -> "Correo o contraseña incorrectos"
                429 -> serverMessage ?: "Demasiados intentos. Intenta más tarde."
                else -> serverMessage ?: "Error en el inicio de sesión ($code)"
            }

            throw AuthApiException(
                kind = errorKind,
                message = msg,
                retryable = code >= 500 || code == 429
            )
        }
    }

    private fun parseError(json: String?): ApiErrorResponse? {
        if (json.isNullOrBlank()) return null
        return runCatching {
            Gson().fromJson(json, ApiErrorResponse::class.java)
        }.getOrNull()
    }
}
