---
project: Nexum-Mobile
unit: 240-Perfil-Usuario
kind: functional-plan
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# PLAN — Consulta de Perfil de Usuario (Get User Profile)

## Estrategia técnica
Implementar la consulta y presentación de perfil bajo el contrato **NEXUM-E012** en la arquitectura MVI y el patrón Repositorio:
1. **Contratos DTO y mapeos de datos:**
   - DTO de respuesta: `UserProfileResponse(val id: String, val name: String, val email: String, val emailVerified: Boolean, val createdAt: String)`.
   - Modelo de dominio UI: `UserProfile(val id: String, val name: String, val email: String, val isEmailVerified: Boolean, val memberSince: String)`.
2. **Ampliación de interfaces:**
   - En `AuthApi`: `suspend fun getProfile(accessToken: String): UserProfileResponse`.
   - En `AuthRepository`: `suspend fun getUserProfile(): AuthResult<UserProfile>`.
3. **Formateo de fechas y localización:**
   - Utilizar APIs estándar de fecha de Java/Kotlin (`java.time.Instant`) para transformar `createdAt` (UTC) a fecha legible (ej. "Noviembre 2026") respetando la configuración regional del sistema.
4. **Estados e Intenciones MVI:**
   - `ProfileUiState`:
     - `profile: UserProfile? = null`
     - `isLoading: Boolean = false`
     - `isRefreshing: Boolean = false`
     - `errorMessage: String? = null`
     - `isOffline: Boolean = false`
   - Intenciones `ProfileIntent`:
     - `LoadProfile`
     - `Refresh`
     - `DismissError`
     - `NavigateToEmailVerification`
5. **Simulación en `MockAuthApi`:**
   - Retardo simulado de 300–700 ms.
   - Retorna los datos asociados al usuario actual de `TokenStorage` enriquecidos con `emailVerified = true` y `createdAt = "2026-11-02T17:30:00Z"`.
   - Centinela `net@error.test` lanza `AuthApiException(AuthErrorKind.NETWORK, "Error de red al consultar perfil", retryable = true)`.

## Contratos y modelos de datos (NEXUM-E012)

```kotlin
// DTO de red (contrato NEXUM-E012)
data class UserProfileResponse(
    val id: String,
    val name: String,
    val email: String,
    val emailVerified: Boolean,
    val createdAt: String
)

// Modelo de dominio para presentación
data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val isEmailVerified: Boolean,
    val memberSince: String
)

// Interfaz ampliada en AuthApi
interface AuthApi {
    // Métodos previos...
    suspend fun getProfile(accessToken: String): UserProfileResponse
}

// Interfaz ampliada en AuthRepository
interface AuthRepository {
    // Métodos previos...
    suspend fun getUserProfile(): AuthResult<UserProfile>
}
```

## Estados e Intenciones MVI

```kotlin
data class ProfileUiState(
    val profile: UserProfile? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val isOffline: Boolean = false
)

sealed interface ProfileIntent {
    data object LoadProfile : ProfileIntent
    data object Refresh : ProfileIntent
    data object DismissError : ProfileIntent
    data object NavigateToEmailVerification : ProfileIntent
}
```

## Orden de trabajo
| Paso | Acción | Requisito rector |
| --- | --- | --- |
| **N240-P01** | Definir modelo DTO de NEXUM-E012 (`UserProfileResponse`) y modelo `UserProfile`. | M240-R02, M240-R03 |
| **N240-P02** | Ampliar contratos `AuthApi` y `AuthRepository` con la consulta de perfil. | M240-R02, M240-R03 |
| **N240-P03** | Implementar la simulación en `MockAuthApi` con retardo y centinelas de red. | M240-R05 |
| **N240-P04** | Construir `ProfileViewModel` MVI con estados de carga, refresco y preservación offline. | M240-R01, M240-R04, M240-R05 |
| **N240-P05** | Diseñar `ProfileScreen` en Compose con Material 3, badge de verificación y pull-to-refresh. | M240-R01, M240-R04 |
| **N240-P06** | Desarrollar pruebas unitarias completas (`ProfileViewModelTest`, `MockAuthApiTest`) con `kotlinx-coroutines-test`. | M240-R01…M240-R05 |

## Dependencias y decisiones técnicas
- **Aprobado**: Material 3 y Jetpack Compose sin dependencias externas.
- **Sin IDOR**: El contrato no recibe identificador de usuario; se deduce en servidor del token Bearer (MO-PR06).
- **Consistencia de datos**: Si el usuario actualiza su perfil en unidades futuras (`PATCH /users/me`), esta vista se recarga reactivamente.

## Estrategia de pruebas
- **Pruebas de ViewModel:**
  - `LoadProfile` transiciona `isLoading = true` → `Content` con datos mapeados y fecha formateada.
  - `Refresh` activa `isRefreshing = true` preservando el perfil existente.
  - Email no verificado (`emailVerified = false`) activa visualmente el banner de verificación.
  - Error de red mantiene el perfil en caché y activa `isOffline = true`.
- **Pruebas de Repositorio y Mock:**
  - `getUserProfile()` envía cabecera `Authorization` sin parámetros de ruta/query.
  - Mapeo correcto de DTO a `UserProfile`.
  - Centinela de red devuelve `Failure(NETWORK)`.
- **Pruebas de regresión:**
  - Mantener los 50 tests de autenticación previos en verde en `./gradlew testDebugUnitTest`.
