package mx.nexum.app.core.testing

import mx.nexum.app.auth.data.Session
import mx.nexum.app.auth.data.TokenStorage
import mx.nexum.app.auth.data.dto.User
import mx.nexum.app.core.network.ApiErrorDetail
import mx.nexum.app.core.network.ApiErrorResponse
import mx.nexum.app.core.network.ApiResult
import mx.nexum.app.core.network.HttpErrorType
import mx.nexum.app.core.network.NetworkErrorType
import mx.nexum.app.core.session.CredentialStorage
import java.time.Instant

/**
 * Fixtures con datos sintéticos no reales ni financieros (M080-R01, M080-R02).
 * Prohibido el uso de credenciales reales, tokens de producción o datos bancarios.
 */
object TestFixtures {

    val syntheticUser = User(
        id = "usr_synthetic_001",
        name = "Usuario Sintético de Prueba",
        email = "test.synthetic@example.com"
    )

    val syntheticSession = Session(
        accessToken = "synthetic.access.jwt.token",
        refreshToken = "synthetic.refresh.jwt.token",
        expiresIn = 3600L,
        user = syntheticUser
    )

    val syntheticCoreSession = mx.nexum.app.core.session.Session(
        accessToken = "synthetic.core.access.jwt",
        refreshToken = "synthetic.core.refresh.jwt",
        expiresAt = Instant.now().plusSeconds(3600)
    )

    val syntheticValidationErrorDetail = ApiErrorDetail(
        code = "VALIDATION_FAILED",
        message = "Error sintético de validación de campos obligatorios",
        details = mapOf("field" to "El formato no cumple las reglas sintéticas"),
        traceId = "trace_synth_12345"
    )

    val syntheticValidationErrorResponse = ApiErrorResponse(
        error = syntheticValidationErrorDetail
    )

    val syntheticNetworkTimeoutResult: ApiResult<Nothing> = ApiResult.NetworkFailure(
        errorType = NetworkErrorType.Timeout(
            message = "Tiempo de espera agotado en conexión sintética"
        )
    )

    val syntheticHttp401Result: ApiResult<Nothing> = ApiResult.HttpFailure(
        statusCode = 401,
        errorType = HttpErrorType.Unauthorized(
            errorDetail = ApiErrorDetail(
                code = "UNAUTHORIZED",
                message = "Sesión inválida o expirada en prueba sintética"
            )
        )
    )
}

/**
 * Doble de prueba (Fake) en memoria para [TokenStorage].
 */
class FakeTokenStorage(
    private var initialSession: Session? = null
) : TokenStorage {
    private var currentSession: Session? = initialSession
    var clearCount = 0
        private set

    override fun save(session: Session) {
        currentSession = session
    }

    override fun current(): Session? = currentSession

    override fun clear() {
        clearCount++
        currentSession = null
    }
}

/**
 * Doble de prueba (Fake) en memoria para [CredentialStorage].
 */
class FakeCredentialStorage(
    private var initialSession: mx.nexum.app.core.session.Session? = null
) : CredentialStorage {
    private var storedSession: mx.nexum.app.core.session.Session? = initialSession
    var clearCount = 0
        private set

    override suspend fun save(session: mx.nexum.app.core.session.Session) {
        storedSession = session
    }

    override suspend fun load(): mx.nexum.app.core.session.Session? = storedSession

    override suspend fun clear() {
        clearCount++
        storedSession = null
    }
}
