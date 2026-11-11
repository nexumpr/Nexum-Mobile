---
project: Nexum-Mobile
unit: 200-Sesiones-Activas
kind: technical-plan
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# PLAN — Consulta de Sesiones Activas (List Sessions)

## Estrategia técnica
Implementar la consulta y presentación de sesiones activas bajo el contrato **NEXUM-E008** en la arquitectura MVI y el patrón Repositorio del módulo `auth`:
1. **Contratos DTO y mapeos de datos:**
   - DTO de respuesta: `SessionItemResponse(val id: String, val device: String, val lastSeenAt: String, val current: Boolean)`.
   - Modelo de dominio UI: `ActiveSession(val id: String, val deviceName: String, val formattedLastSeen: String, val isCurrentDevice: Boolean)`.
2. **Ampliación de interfaces:**
   - En `AuthApi`: `suspend fun getSessions(accessToken: String): List<SessionItemResponse>`.
   - En `AuthRepository`: `suspend fun getActiveSessions(): AuthResult<List<ActiveSession>>`.
3. **Mapeo y Formato Local:**
   - Transformar `lastSeenAt` (ISO-8601 UTC) a formato amigable relativo o de fecha local del usuario (ej. "Activo ahora", "Ayer a las 18:20").
   - Garantizar que la sesión actual (`current == true`) encabece la lista en la presentación.
4. **Estados MVI de la pantalla:**
   - `SessionsUiState`:
     - `sessions: List<ActiveSession> = emptyList()`
     - `isLoading: Boolean = false`
     - `isRefreshing: Boolean = false`
     - `errorMessage: String? = null`
   - Intenciones `SessionsIntent`:
     - `LoadSessions`
     - `Refresh`
     - `DismissError`
5. **Simulación en `MockAuthApi`:**
   - Retardo determinista de 400–800 ms.
   - Retorna lista sintética con:
     1. Dispositivo actual: `id = "sess-current"`, `device = "Pixel 8 Pro (Android 16)"`, `current = true`, `lastSeenAt = now()`.
     2. Dispositivo secundario: `id = "sess-web"`, `device = "Chrome en Windows 11"`, `current = false`, `lastSeenAt = now() - 1 día`.
   - Token centinela `net@error.test` lanza `AuthApiException(AuthErrorKind.NETWORK, "Error al consultar sesiones", retryable = true)`.

## Contratos y modelos de datos (NEXUM-E008)

```kotlin
// DTOs de red (contrato NEXUM-E008)
data class SessionItemResponse(
    val id: String,
    val device: String,
    val lastSeenAt: String,
    val current: Boolean
)

// Modelo de dominio para presentación
data class ActiveSession(
    val id: String,
    val deviceName: String,
    val formattedLastSeen: String,
    val isCurrentDevice: Boolean
)

// Interfaz ampliada en AuthApi
interface AuthApi {
    // Métodos previos: register, login, refresh, logout, forgotPassword, resetPassword, changePassword
    suspend fun getSessions(accessToken: String): List<SessionItemResponse>
}

// Interfaz ampliada en AuthRepository
interface AuthRepository {
    // Métodos previos...
    suspend fun getActiveSessions(): AuthResult<List<ActiveSession>>
}
```

## Estados e Intenciones MVI

```kotlin
data class SessionsUiState(
    val sessions: List<ActiveSession> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

sealed interface SessionsIntent {
    data object LoadSessions : SessionsIntent
    data object Refresh : SessionsIntent
    data object DismissError : SessionsIntent
}
```

## Orden de trabajo
| Paso | Acción | Requisito rector |
| --- | --- | --- |
| **N200-P01** | Definir modelo DTO de NEXUM-E008 (`SessionItemResponse`) y modelo `ActiveSession`. | M200-R02, M200-R03 |
| **N200-P02** | Ampliar contratos `AuthApi` y `AuthRepository` con la consulta de sesiones. | M200-R02 |
| **N200-P03** | Implementar la simulación en `MockAuthApi` con sesiones sintéticas y centinelas de red. | M200-R05 |
| **N200-P04** | Construir `SessionsViewModel` MVI con estados de carga, refresco y manejo de errores. | M200-R01, M200-R04 |
| **N200-P05** | Diseñar `ActiveSessionsScreen` en Compose con Material 3, badge para dispositivo actual y pull-to-refresh. | M200-R01, M200-R04 |
| **N200-P06** | Desarrollar pruebas unitarias completas (`SessionsViewModelTest`, `MockAuthApiTest`) con `kotlinx-coroutines-test`. | M200-R01…M200-R05 |

## Dependencias y decisiones técnicas
- **Aprobado**: Material 3 y Jetpack Compose sin dependencias externas.
- **Privacidad**: Se descarta expresamente cualquier transporte de `refreshToken` o hashes en la capa de transporte (MO-PR06, MO-PR10).
- **Preparación para revocación (E009)**: `ActiveSession.id` se expone de forma opaca y lista para conectarse con la acción de borrado en la siguiente unidad.

## Estrategia de pruebas
- **Pruebas de ViewModel:**
  - `LoadSessions` transiciona `isLoading = true` → `Content` con lista ordenada (sesión actual al inicio).
  - `Refresh` activa `isRefreshing = true` sin borrar la lista existente.
  - Error de red transiciona a estado `Error` con mensaje reintentable.
- **Pruebas de Repositorio y Mock:**
  - `getActiveSessions()` retorna lista de sesiones y mapea timestamps correctamente.
  - Verificación de que ningún DTO contiene campos de tokens.
  - Centinela `net@error.test` devuelve `Failure(NETWORK)`.
- **Pruebas de regresión:**
  - Asegurar que `assembleDebug` y los 50 tests previos de autenticación continúen pasando.
