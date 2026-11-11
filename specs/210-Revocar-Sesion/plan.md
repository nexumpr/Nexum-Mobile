---
project: Nexum-Mobile
unit: 210-Revocar-Sesion
kind: technical-plan
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# PLAN — Revocación de Sesión Remota (Revoke Session)

## Estrategia técnica
Implementar la revocación de sesiones bajo el contrato **NEXUM-E009** en la arquitectura MVI y el patrón Repositorio del módulo `auth`:
1. **Ampliación de interfaces:**
   - En `AuthApi`: `suspend fun revokeSession(accessToken: String, sessionId: String)`
   - En `AuthRepository`: `suspend fun revokeSession(sessionId: String): AuthResult<Unit>`
2. **Coordinación en el Repositorio:**
   - Invoca `api.revokeSession(...)`.
   - Si la sesión revocada corresponde a la sesión local del dispositivo (`current == true`), invoca obligatoriamente `tokenStorage.clear()`.
   - Retorna `AuthResult.Success(Unit)`.
3. **Manejo en `SessionsViewModel`:**
   - Se amplía `SessionsUiState` con:
     - `sessionToRevoke: ActiveSession? = null` (controla la visibilidad del diálogo de confirmación).
     - `revokingSessionId: String? = null` (controla el indicador de carga individual en la tarjeta).
   - Intenciones adicionales en `SessionsIntent`:
     - `RequestRevoke(val session: ActiveSession)`
     - `ConfirmRevoke`
     - `DismissRevokeDialog`
   - Reducer:
     - Tras `ConfirmRevoke` exitoso de una sesión remota: filtra la sesión eliminada de `state.sessions` y emite `ShowSnackbar("Sesión cerrada exitosamente")`.
     - Si la sesión era la actual: emite `AuthEffect.NavigateToLogin`.
4. **Simulación en `MockAuthApi`:**
   - Retardo simulado de 400–800 ms.
   - Elimina la sesión con `id == sessionId` de su lista interna en memoria.
   - Si `sessionId == "not_found_session"` → lanza `AuthApiException(AuthErrorKind.UNKNOWN, "Sesión no encontrada", retryable = false)`.
   - Si `sessionId == "net_error_session"` → lanza `AuthApiException(AuthErrorKind.NETWORK, "Error de red al revocar sesión", retryable = true)`.

## Contratos e Interfaces (NEXUM-E009)

```kotlin
// Interfaz ampliada en AuthApi
interface AuthApi {
    // Métodos previos: register, login, refresh, logout, forgotPassword, resetPassword, changePassword, getSessions
    suspend fun revokeSession(accessToken: String, sessionId: String)
}

// Interfaz ampliada en AuthRepository
interface AuthRepository {
    // Métodos previos...
    suspend fun revokeSession(sessionId: String): AuthResult<Unit>
}
```

## Estados e Intenciones MVI ampliados

```kotlin
data class SessionsUiState(
    val sessions: List<ActiveSession> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val sessionToRevoke: ActiveSession? = null,
    val revokingSessionId: String? = null
)

sealed interface SessionsIntent {
    data object LoadSessions : SessionsIntent
    data object Refresh : SessionsIntent
    data object DismissError : SessionsIntent
    data class RequestRevoke(val session: ActiveSession) : SessionsIntent
    data object ConfirmRevoke : SessionsIntent
    data object DismissRevokeDialog : SessionsIntent
}
```

## Orden de trabajo
| Paso | Acción | Requisito rector |
| --- | --- | --- |
| **N210-P01** | Ampliar contratos `AuthApi` y `AuthRepository` con la operación `revokeSession`. | M210-R02 |
| **N210-P02** | Implementar la simulación en `MockAuthApi` con eliminación en memoria, retardo y centinelas. | M210-R03, M210-R05 |
| **N210-P03** | Implementar la lógica en `AuthRepositoryImpl` con diferenciación y purga de sesión si es local. | M210-R03, M210-R04 |
| **N210-P04** | Ampliar `SessionsViewModel` con intenciones de revocación y actualización reactiva de la lista. | M210-R01, M210-R04 |
| **N210-P05** | Diseñar el diálogo de confirmación Composable en `ActiveSessionsScreen`. | M210-R01, M210-R04 |
| **N210-P06** | Desarrollar pruebas unitarias completas (`SessionsViewModelRevokeTest`, `MockAuthApiTest`) con `kotlinx-coroutines-test`. | M210-R01…M210-R05 |

## Dependencias y decisiones técnicas
- **Aprobado**: Material 3 y Jetpack Compose sin dependencias externas.
- **Idempotencia**: Si el servidor devuelve 404 (sesión ya revocada), el cliente la remueve igualmente de la lista sin alarmar al usuario.
- **Seguridad**: Si se revoca la sesión del equipo en uso, se garantiza el reseteo de `TokenStorage` (MO-PR15).

## Estrategia de pruebas
- **Pruebas de ViewModel:**
  - `RequestRevoke` asigna `sessionToRevoke` y abre diálogo.
  - `DismissRevokeDialog` limpia `sessionToRevoke`.
  - `ConfirmRevoke` para sesión remota llama a repositorio, remueve el elemento de `sessions` y emite Snackbar.
  - `ConfirmRevoke` para sesión local emite `AuthEffect.NavigateToLogin`.
- **Pruebas de Repositorio y Mock:**
  - `revokeSession()` exitoso elimina la sesión en el mock y devuelve `Success(Unit)`.
  - Sesión no encontrada devuelve error controlado.
  - Centinela de red devuelve `Failure(NETWORK)`.
- **Pruebas de regresión:**
  - Mantener los 50 tests previos de autenticación pasando en `./gradlew testDebugUnitTest`.
