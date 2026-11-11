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
1. **Contratos DTO y mapeos de datos:** DTOs inmutables para `RefreshRequest` y `RefreshResponse`.
2. **Ampliación de interfaces:** Nuevos métodos en `AuthApi` y `AuthRepository` para ejecutar la renovación de credenciales.
3. **Mecanismo de concurrencia Single-Flight:** Uso de corrutinas Kotlin con `kotlinx.coroutines.sync.Mutex` en el repositorio para encolar o reutilizar el resultado de un refresh en vuelo, evitando carreras que provoquen rechazos de rotación.
4. **Sincronización con almacenamiento de sesión:** Reemplazo atómico de la sesión en `TokenStorage` tras un `200 OK`, y purga inmediata (`clear()`) ante un `401 Unauthorized` por token inválido o reuso detectado.
5. **Simulación rigurosa en `MockAuthApi`:** Estado interno en el mock que mantenga el historial de tokens emitidos y revocados, permitiendo simular rotación determinista, detección de reuso, expiración provocada y fallos de conectividad.
6. **Efectos de navegación de seguridad:** Emisión de `AuthEffect.NavigateToLogin` para que el orquestador visual (`AuthFlow`) redirija al usuario al login cuando la sesión expire definitivamente.

## Contratos y modelos de datos (NEXUM-E003)

```kotlin
// DTOs de red (contrato NEXUM-E003)
data class RefreshRequest(
    val refreshToken: String
)

data class RefreshResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long
)

// Resultado de operación de renovación
data class RefreshOutcome(
    val session: Session
)

// Interfaz ampliada en AuthApi
interface AuthApi {
    // Métodos previos: register, login
    suspend fun refresh(request: RefreshRequest): RefreshResponse
}

// Interfaz ampliada en AuthRepository
interface AuthRepository {
    // Métodos previos: register, login
    suspend fun refreshSession(): AuthResult<RefreshOutcome>
}
```

## Lógica del Repositorio y Concurrencia (Single-Flight con Mutex)

```kotlin
class AuthRepositoryImpl(
    private val api: AuthApi,
    private val tokenStorage: TokenStorage
) : AuthRepository {

    private val refreshMutex = Mutex()

    override suspend fun refreshSession(): AuthResult<RefreshOutcome> = refreshMutex.withLock {
        val currentSession = tokenStorage.current()
            ?: return AuthResult.Failure(
                AuthError(AuthErrorKind.INVALID_CREDENTIALS, "No hay sesión activa para renovar.", retryable = false)
            )

        try {
            val response = api.refresh(RefreshRequest(refreshToken = currentSession.refreshToken))
            val updatedSession = currentSession.copy(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken,
                expiresIn = response.expiresIn
            )
            tokenStorage.save(updatedSession)
            AuthResult.Success(RefreshOutcome(updatedSession))
        } catch (error: Throwable) {
            val authError = error.toAuthError()
            if (authError.kind == AuthErrorKind.INVALID_CREDENTIALS) {
                // Token revocado, expirado o reusado: invalidación obligatoria
                tokenStorage.clear()
            }
            AuthResult.Failure(authError)
        }
    }
}
```

## Simulación en `MockAuthApi`
- Historial en memoria de refresh tokens activos y tokens previamente revocados/rotados.
- Si el `refreshToken` presentado coincide con el activo: genera un nuevo par con prefijo rotado (`access_token_vX`, `refresh_token_vX`), invalida el anterior y retorna `RefreshResponse(..., expiresIn = 900)`.
- Si el `refreshToken` ya fue utilizado previamente: lanza `AuthApiException(AuthErrorKind.INVALID_CREDENTIALS, "Refresh token ya utilizado (posible compromiso de sesión)")`.
- Si el `refreshToken == "expired_token"`: lanza `AuthApiException(AuthErrorKind.INVALID_CREDENTIALS, "Refresh token expirado")`.
- Si el `refreshToken == "net_error_token"`: lanza `AuthApiException(AuthErrorKind.NETWORK, "Error de conexión con el servidor de autenticación", retryable = true)`.

## Orden de trabajo
| Paso | Acción | Requisito rector |
| --- | --- | --- |
| **N150-P01** | Definir modelos DTO de NEXUM-E003 (`RefreshRequest`, `RefreshResponse`) y `RefreshOutcome`. | M150-R01 |
| **N150-P02** | Ampliar contratos `AuthApi` y `AuthRepository` para soportar la renovación de sesión. | M150-R01, M150-R02 |
| **N150-P03** | Implementar la simulación en `MockAuthApi` con rotación, revocación, detección de reuso y tokens centinela. | M150-R02, M150-R03, M150-R05 |
| **N150-P04** | Implementar la lógica de renovación en `AuthRepositoryImpl` con `Mutex` y actualización de `TokenStorage`. | M150-R02, M150-R04, M150-R05 |
| **N150-P05** | Integrar el manejo de sesión revocada/expirada con emisión de `AuthEffect.NavigateToLogin` en el flujo de la app. | M150-R03 |
| **N150-P06** | Desarrollar suite de pruebas unitarias (`MockAuthApiTest`, `AuthRepositoryRefreshTest`) con `kotlinx-coroutines-test`. | M150-R01…M150-R05 |

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
