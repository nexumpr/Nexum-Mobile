---
project: Nexum-Mobile
unit: 310-Completar-Onboarding
kind: technical-plan
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# PLAN — Finalización de Onboarding (Complete Onboarding)

## Estrategia técnica
Implementar la finalización del onboarding bajo el contrato **NEXUM-E019** en la arquitectura MVI y el patrón Repositorio:
1. **Contratos DTO y modelos de datos:**
   - DTO de entrada: `UpdateOnboardingRequest(val completed: Boolean = true, val limitedMode: Boolean = false)`.
   - DTO de respuesta: `OnboardingStatusResponse` (reutilizado de `300-Estado-Onboarding`).
2. **Ampliación de interfaces:**
   - En `AuthApi`: `suspend fun updateOnboardingStatus(accessToken: String, request: UpdateOnboardingRequest, idempotencyKey: String? = null): OnboardingStatusResponse`.
   - En `AuthRepository`: `suspend fun completeOnboarding(limitedMode: Boolean): AuthResult<OnboardingStatus>`.
3. **Flujo de ejecución y sincronización local en Repositorio:**
   - Obtiene el token de acceso vigente de `TokenStorage`.
   - Genera cabecera `Idempotency-Key` (UUIDv4) opcional para prevenir mutaciones duplicadas.
   - Envía `authApi.updateOnboardingStatus(...)`.
   - Ante respuesta exitosa `200 OK`:
     - Actualiza el estado local en `TokenStorage` marcando `finishedAt` con el valor del servidor.
     - Retorna `AuthResult.Success(updatedStatus)`.
   - Ante fallo de transporte de red (fail-open controlado, MO-PR07, MO-PR08):
     - Graba preventivamente en almacenamiento local `finishedAt = Instant.now().toString()`.
     - Permite que el usuario avance sin quedar bloqueado en el onboarding.
     - Retorna `AuthResult.Success(...)` con indicación de sincronización pendiente.
4. **Estados e Intenciones MVI:**
   - `CompleteOnboardingUiState`:
     - `isSubmitting: Boolean = false`
     - `isCompleted: Boolean = false`
     - `isLimitedMode: Boolean = false`
     - `errorMessage: String? = null`
   - Intenciones `CompleteOnboardingIntent`:
     - `data object ContinueFull : CompleteOnboardingIntent`
     - `data object ContinueLimited : CompleteOnboardingIntent`
     - `data object Retry : CompleteOnboardingIntent`
5. **Simulación en `MockAuthApi`:**
   - Retardo determinista de 300–700 ms.
   - Si `request.limitedMode == true` → actualiza usuario en memoria con `limitedMode = true`, añade `"permissions_skipped"` a `stepsCompleted` y asigna `finishedAt = "2026-11-11T15:40:00Z"`.
   - Si `request.limitedMode == false` → actualiza usuario con `limitedMode = false`, añade `"permissions_info"` y asigna `finishedAt = "2026-11-11T15:40:00Z"`.
   - Si el request contiene el centinela `net_error_onboarding` → lanza `AuthApiException(AuthErrorKind.NETWORK, "Error de red al actualizar onboarding", retryable = true)`.

## Contratos y modelos de datos (NEXUM-E019)

```kotlin
// DTO de red (contrato NEXUM-E019)
data class UpdateOnboardingRequest(
    val completed: Boolean = true,
    val limitedMode: Boolean = false
)

// Interfaz ampliada en AuthApi
interface AuthApi {
    // Métodos previos...
    suspend fun updateOnboardingStatus(
        accessToken: String,
        request: UpdateOnboardingRequest,
        idempotencyKey: String? = null
    ): OnboardingStatusResponse
}

// Interfaz ampliada en AuthRepository
interface AuthRepository {
    // Métodos previos...
    suspend fun completeOnboarding(
        limitedMode: Boolean
    ): AuthResult<OnboardingStatus>
}
```

## Estados e Intenciones MVI

```kotlin
data class CompleteOnboardingUiState(
    val isSubmitting: Boolean = false,
    val isCompleted: Boolean = false,
    val isLimitedMode: Boolean = false,
    val errorMessage: String? = null
)

sealed interface CompleteOnboardingIntent {
    data object ContinueFull : CompleteOnboardingIntent
    data object ContinueLimited : CompleteOnboardingIntent
    data object Retry : CompleteOnboardingIntent
}

sealed interface OnboardingEffect {
    data object NavigateToHome : OnboardingEffect
    data class ShowToast(val message: String) : OnboardingEffect
}
```

## Orden de trabajo
| Paso | Acción | Requisito rector |
| --- | --- | --- |
| **N310-P01** | Definir modelo DTO de NEXUM-E019 (`UpdateOnboardingRequest`). | M310-R02 |
| **N310-P02** | Ampliar contratos `AuthApi` y `AuthRepository` con la mutación de estado de onboarding y fail-open local. | M310-R02, M310-R04 |
| **N310-P03** | Implementar la simulación en `MockAuthApi` con persistencia en memoria, branching de modo limitado y centinelas. | M310-R05, M310-R06 |
| **N310-P04** | Construir `CompleteOnboardingViewModel` MVI con manejo de intents, control de doble toque y efectos de navegación. | M310-R01, M310-R04, M310-R05 |
| **N310-P05** | Integrar botones de acción en `OnboardingScreen` con diseño accesible Material 3 y desapilado en Compose Navigation. | M310-R01, M310-R05 |
| **N310-P06** | Desarrollar pruebas unitarias completas (`CompleteOnboardingViewModelTest`, `MockAuthApiTest`) con `kotlinx-coroutines-test`. | M310-R01…M310-R06 |

## Dependencias y decisiones técnicas
- **Aprobado**: Material 3 y Jetpack Compose sin librerías externas superfluas.
- **Fail-open no bloqueante (MO-PR07, MO-PR08)**: Los errores transitorios de conectividad no deben impedir al usuario ingresar a la aplicación; la persistencia local de la finalización garantiza fluidez sin sacrificar consistencia eventual.
- **Independencia de permisos (MO-PR09)**: `limitedMode` remoto es un flag estadístico/analítico; no concede ni sustituye los permisos en el SO Android.

## Estrategia de pruebas
- **Pruebas de ViewModel:**
  - `ContinueFull` envía `limitedMode = false` y emite `NavigateToHome`.
  - `ContinueLimited` envía `limitedMode = true` y emite `NavigateToHome`.
  - Doble pulsación no genera peticiones concurrentes (`isSubmitting = true`).
  - Fallo de transporte ejecuta fail-open local y permite navegación sin atrapar al usuario.
- **Pruebas de Repositorio y Mock:**
  - `completeOnboarding()` muta el usuario en memoria y devuelve `OnboardingStatusResponse` con `finishedAt != null`.
  - Centinela de error activa el manejo resiliente local.
- **Pruebas de regresión:**
  - Verificación de `./gradlew assembleDebug` y aprobación continua de los 50 tests preexistentes de autenticación.
