package mx.nexum.app.auth.data

import kotlinx.coroutines.delay
import mx.nexum.app.auth.data.dto.LoginRequest
import mx.nexum.app.auth.data.dto.LoginResponse
import mx.nexum.app.auth.data.dto.RegisterRequest
import mx.nexum.app.auth.data.dto.RegisterResponse
import mx.nexum.app.auth.data.dto.User

class MockAuthApi(
    private val delayRange: LongRange = 800L..1500L,
    private val maxLoginAttempts: Int = 5
) : AuthApi {

    private val lock = Any()
    private val credentials = linkedMapOf(
        "demo@nexum.test" to ("Demo1234" to "Alex Demo"),
        "taken@nexum.test" to ("Taken1234" to "Correo Ya Registrado")
    )
    private var loginAttempts = 0
    private var userCounter = 0

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
            return RegisterResponse(
                userId = userId,
                onboardingStatus = "pending",
                accessToken = "mock-access-$userId",
                refreshToken = "mock-refresh-$userId",
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
            return LoginResponse(
                accessToken = "mock-access-$userId",
                refreshToken = "mock-refresh-$userId",
                expiresIn = 3600,
                user = User(id = userId, name = stored.second, email = email)
            )
        }
    }

    private suspend fun simulateNetwork() {
        delay(delayRange.random())
    }

    companion object {
        const val NETWORK_ERROR_EMAIL = "net@error.test"
        const val DEMO_EMAIL = "demo@nexum.test"
        const val DEMO_PASSWORD = "Demo1234"
        const val TAKEN_EMAIL = "taken@nexum.test"
    }
}
