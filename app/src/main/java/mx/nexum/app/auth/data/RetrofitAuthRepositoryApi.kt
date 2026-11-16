
package mx.nexum.app.auth.data

import java.util.UUID
import mx.nexum.app.auth.data.dto.ClientDto
import mx.nexum.app.auth.data.dto.IncomeDto
import mx.nexum.app.auth.data.dto.LoginRequest
import mx.nexum.app.auth.data.dto.LoginRequestDto
import mx.nexum.app.auth.data.dto.LoginResponse
import mx.nexum.app.auth.data.dto.PrivacyDto
import mx.nexum.app.auth.data.dto.RecurringExpenseDto
import mx.nexum.app.auth.data.dto.RegisterRequest
import mx.nexum.app.auth.data.dto.RegisterRequestDto
import mx.nexum.app.auth.data.dto.RegisterResponse
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
}
