---
project: Nexum-Mobile
unit: 150-Refresh-Token
kind: technical-plan
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# PLAN — Renovación y Rotación de Tokens de Sesión (Refresh Token)

## Estrategia técnica
Implementar el ciclo de renovación y rotación de tokens bajo el contrato **NEXUM-E003** sobre la arquitectura MVI y el patrón Repositorio existente en el módulo `auth`. La solución se basa en:
1. **Contratos DTO y mapeos de datos:** DTOs inmutables para `RefreshRequest` (`refreshToken`, `deviceId`) y `RefreshResponse` (`accessToken`, `refreshToken`, `tokenType`, `expiresIn`, `sessionId`, `session`).
2. **Ampliación de interfaces y clientes OkHttp:** Nuevos métodos en `AuthApi` y `AuthRepository`. Cliente OkHttp aislado sin interceptores de autenticación ni `Authenticator` para llamadas públicas (`login`, `register`, `refresh`), y `TokenAuthenticator` con límite de 1 reintento para peticiones protegidas.
3. **Mecanismo de concurrencia Single-Flight Real:** Uso de `kotlinx.coroutines.sync.Mutex` junto con un `CompletableDeferred<AuthResult<RefreshOutcome>>` compartido en el repositorio. Si un refresh ya está en vuelo, las corrutinas concurrentes esperan y reutilizan ese mismo resultado, evitando replays accidentales por llamadas duplicadas.
4. **Sincronización con almacenamiento de sesión:** Reemplazo atómico de la sesión en `TokenStorage` tras un `200 OK`. Ante `401 Unauthorized` o `409 Conflict` (reuso de token rotado/revocación de familia), purga inmediata (`clear()`) y emisión de `AuthEffect.NavigateToLogin`. Ante `503 Service Unavailable` o errores de red, **no** borrar sesión local (`retryable = true`).
5. **Simulación rigurosa en `MockAuthApi`:** Estado en memoria que gestiona familias de tokens y tokens consumidos. Retorna `409` si se reusa un token consumido (y revoca la familia con `401` posterior), `401` ante `expired_token` o familia revocada, `503` ante `service_unavailable_token`, y error de red ante `net_error_token`.
6. **Efectos de navegación de seguridad:** Emisión de `AuthEffect.NavigateToLogin` expuesto en `AuthRepository.sessionEffects` para que el orquestador visual (`AuthFlow`) redirija al usuario al login cuando la sesión sea revocada o expire definitivamente.

## Contratos y modelos de datos (NEXUM-E003)

```kotlin
// DTOs de red (contrato NEXUM-E003)
data class RefreshRequest(
    val refreshToken: String,
    val deviceId: String? = null
)

data class RefreshResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long,
    val sessionId: String? = null,
    val session: SessionIdDto? = null
)

// Resultado de operación de renovación
data class RefreshOutcome(
    val session: Session
)

// Interfaz ampliada en AuthApi
interface AuthApi {
    suspend fun register(request: RegisterRequest): RegisterResponse
    suspend fun login(request: LoginRequest): LoginResponse
    suspend fun refresh(request: RefreshRequest): RefreshResponse
}

// Interfaz ampliada en AuthRepository
interface AuthRepository {
    val sessionEffects: SharedFlow<AuthEffect>
    suspend fun register(request: RegisterRequest): AuthResult<RegisterOutcome>
    suspend fun login(request: LoginRequest): AuthResult<LoginOutcome>
    suspend fun refreshSession(): AuthResult<RefreshOutcome>
}
```

## Lógica del Repositorio y Concurrencia (Single-Flight Real)

```kotlin
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
                    deviceId = currentSession.sessionId // o deviceId disponible
                )
            )
            val updatedSession = currentSession.copy(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken,
                expiresIn = response.expiresIn,
                sessionId = response.sessionId ?: response.session?.id ?: currentSession.sessionId
            )
            tokenStorage.save(updatedSession)
            AuthResult.Success(RefreshOutcome(updatedSession))
        } catch (error: Throwable) {
            val authError = error.toAuthError()
            if (authError.kind == AuthErrorKind.INVALID_CREDENTIALS || authError.kind == AuthErrorKind.CONFLICT) {
                tokenStorage.clear()
                _sessionEffects.tryEmit(AuthEffect.NavigateToLogin)
            }
            AuthResult.Failure(authError)
        }
    }
}
```

## Simulación en `MockAuthApi`
- Historial en memoria de refresh tokens activos, tokens consumidos y familias revocadas.
- Si el `refreshToken` presentado es válido y activo: genera un nuevo par de tokens rotados (`mock-access-vX`, `mock-refresh-vX`), registra el anterior en consumidos y retorna `RefreshResponse(..., expiresIn = 900)`.
- Si el `refreshToken` ya fue consumido previamente (intento de replay): revoca la familia completa de tokens y lanza `AuthApiException(AuthErrorKind.CONFLICT, "Refresh token ya consumido (posible compromiso)", statusCode = 409)`.
- Si el token pertenece a una familia revocada o es `"expired_token"`: lanza `AuthApiException(AuthErrorKind.INVALID_CREDENTIALS, "Token inválido o familia revocada", statusCode = 401)`.
- Si el token es `"service_unavailable_token"`: lanza `AuthApiException(AuthErrorKind.SERVER_UNAVAILABLE, "Servicio de autenticación no disponible", retryable = true, statusCode = 503)`.
- Si el token es `"net_error_token"`: lanza `AuthApiException(AuthErrorKind.NETWORK, "Error de conexión con el servidor", retryable = true)`.

## Orden de trabajo
| Paso | Acción | Requisito rector |
| --- | --- | --- |
| **N150-P01** | Definir modelos DTO de NEXUM-E003 (`RefreshRequest`, `RefreshResponse`) y `RefreshOutcome`. | M150-R01 |
| **N150-P02** | Ampliar contratos `AuthApi` y `AuthRepository`, configurar cliente OkHttp aislado y `TokenAuthenticator` con límite de 1 reintento. | M150-R01, M150-R02, M150-R05 |
| **N150-P03** | Implementar la simulación en `MockAuthApi` con rotación, detección de replay (409), revocación de familia (401), 503 y error de red. | M150-R02, M150-R03, M150-R04 |
| **N150-P04** | Implementar la lógica de renovación en `AuthRepositoryImpl` con Single-Flight real (`CompletableDeferred` + `Mutex`) y actualización atómica de `TokenStorage`. | M150-R02, M150-R04, M150-R05 |
| **N150-P05** | Integrar emisión de efecto de sesión revocada hacia `AuthFlow` (`AuthEffect.NavigateToLogin`) ante 401/409. | M150-R03 |
| **N150-P06** | Desarrollar suite de pruebas unitarias (`MockAuthApiTest`, `AuthRepositoryRefreshTest`, `TokenAuthenticatorTest`) verificando concurrencia real, replay 409, 503 y regresión. | M150-R01…M150-R05 |

## Dependencias y decisiones técnicas
- **Dependencias existentes**: `kotlinx-coroutines-core` (para `Mutex`), `kotlinx-coroutines-test` (pruebas). Sin dependencias externas adicionales.
- **Relación con `140-Login`**: Comparte la estructura de `Session`, `TokenStorage`, `AuthError` y `AuthResult`.
- **Relación con `050-sesion-credenciales`**: `TokenStorage` permanece desacoplado; cuando se implemente cifrado con Keystore en la unidad `050`, la renovación funcionará de forma transparente.
- **Sin afectación en UI**: Al ser un endpoint de soporte (`[S]`), no introduce pantallas adicionales; opera a nivel de servicio de sesión y emite efectos a la UI cuando la sesión no puede renovarse.

## Estrategia de pruebas
- **Pruebas unitarias de API Mock:**
  - Renovación exitosa: verifica que devuelve nuevo token y que el token anterior queda registrado como consumido.
  - Detección de reuso: segundo intento con el mismo token genera 401 por reutilización.
  - Tokens inválidos/expirados devuelven 401.
  - Fallos de red devuelven error de red reintentable.
- **Pruebas unitarias de Repositorio:**
  - `refreshSession()` con éxito actualiza `TokenStorage` con los nuevos valores.
  - `refreshSession()` con 401 ejecuta `TokenStorage.clear()`.
  - `refreshSession()` con fallo de red conserva los tokens en `TokenStorage`.
  - Peticiones concurrentes con `Mutex` no provocan colisiones de rotación.
- **Pruebas de regresión:**
  - Asegurar que `assembleDebug` y los 50 tests previos siguen pasando sin alteraciones.

## Riesgos y mitigaciones
- **Desincronización de token:** Mitigado asegurando que la sustitución en `TokenStorage` es atómica y gobernada por el `Mutex`.
- **Cierre de sesión falso por caída de red:** Mitigado diferenciando explícitamente `AuthErrorKind.NETWORK` (no purga sesión) de `AuthErrorKind.INVALID_CREDENTIALS` (purga sesión).
