---
project: Nexum-Mobile
unit: 300-Estado-Onboarding
kind: technical-plan
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# PLAN — Consulta de Estado de Onboarding (Get Onboarding Status)

## Estrategia técnica
Implementar la consulta y resolución de onboarding bajo el contrato **NEXUM-E018** en la arquitectura MVI y el patrón Repositorio:
1. **Contratos DTO y modelos de dominio:**
   - DTO de respuesta: `OnboardingStatusResponse(val stepsCompleted: List<String>, val finishedAt: String?)`.
   - Modelo de dominio: `OnboardingStatus(val stepsCompleted: List<String>, val finishedAt: String?, val isFinished: Boolean = !finishedAt.isNullOrBlank())`.
2. **Ampliación de interfaces:**
   - En `AuthApi`: `suspend fun getOnboardingStatus(accessToken: String): OnboardingStatusResponse`.
   - En `AuthRepository`: `suspend fun getOnboardingStatus(): AuthResult<OnboardingStatus>`.
3. **Flujo de resolución y caché en Repositorio:**
   - Extrae el token de acceso vigente de `TokenStorage`.
   - Consulta `authApi.getOnboardingStatus(accessToken)`.
   - Ante respuesta exitosa:
     - Persiste el estado de onboarding en almacenamiento local seguro (`TokenStorage` o SharedPreferences/DataStore).
     - Retorna `AuthResult.Success(status)`.
   - Ante fallo de transporte o sin conexión:
     - Si existe estado guardado previamente en caché local, lo utiliza de contingencia retornando `AuthResult.Success(cachedStatus)`.
     - Si no hay caché local previa, retorna `AuthResult.Failure(NETWORK)`.
4. **Estados e Intenciones MVI:**
   - Destinos: `OnboardingDestination` (`None`, `Home`, `Onboarding`).
   - `OnboardingRoutingUiState`:
     - `isChecking: Boolean = true`
     - `destination: OnboardingDestination = OnboardingDestination.None`
     - `status: OnboardingStatus? = null`
     - `errorMessage: String? = null`
   - Intenciones `OnboardingRoutingIntent`:
     - `data object CheckStatus : OnboardingRoutingIntent`
     - `data object Retry : OnboardingRoutingIntent`
5. **Simulación en `MockAuthApi`:**
   - Retardo determinista de 200–500 ms.
   - Si el usuario en memoria tiene `onboardingFinished = true`, retorna `OnboardingStatusResponse(listOf("welcome", "financial_profile", "permissions_info"), "2026-11-02T17:35:00Z")`.
   - Si es nuevo usuario o incompleto, retorna `OnboardingStatusResponse(listOf("welcome"), null)`.
   - Si el token corresponde al centinela `net@error.test`, lanza `AuthApiException(AuthErrorKind.NETWORK, "Error de red al consultar onboarding", retryable = true)`.

## Contratos y modelos de datos (NEXUM-E018)

```kotlin
// DTO de red (contrato NEXUM-E018)
data class OnboardingStatusResponse(
    val stepsCompleted: List<String>,
    val finishedAt: String?
)

// Modelo de dominio
data class OnboardingStatus(
    val stepsCompleted: List<String>,
    val finishedAt: String?,
    val isFinished: Boolean = !finishedAt.isNullOrBlank()
)

// Interfaz ampliada en AuthApi
interface AuthApi {
    // Métodos previos...
    suspend fun getOnboardingStatus(accessToken: String): OnboardingStatusResponse
}

// Interfaz ampliada en AuthRepository
interface AuthRepository {
    // Métodos previos...
    suspend fun getOnboardingStatus(): AuthResult<OnboardingStatus>
}
```

## Estados e Intenciones MVI

```kotlin
sealed interface OnboardingDestination {
    data object None : OnboardingDestination
    data object Home : OnboardingDestination
    data object Onboarding : OnboardingDestination
}

data class OnboardingRoutingUiState(
    val isChecking: Boolean = true,
    val destination: OnboardingDestination = OnboardingDestination.None,
    val status: OnboardingStatus? = null,
    val errorMessage: String? = null
)

sealed interface OnboardingRoutingIntent {
    data object CheckStatus : OnboardingRoutingIntent
    data object Retry : OnboardingRoutingIntent
}
```

## Orden de trabajo
| Paso | Acción | Requisito rector |
| --- | --- | --- |
| **N300-P01** | Definir modelos DTO de NEXUM-E018 (`OnboardingStatusResponse`) y dominio (`OnboardingStatus`). | M300-R02 |
| **N300-P02** | Ampliar contratos `AuthApi` y `AuthRepository` con la consulta de onboarding y fallback de caché local. | M300-R02, M300-R04 |
| **N300-P03** | Implementar la simulación en `MockAuthApi` con retardo, escenarios incompleto/completado y centinelas. | M300-R05, M300-R06 |
| **N300-P04** | Construir `OnboardingRoutingViewModel` MVI con resolución determinista de destino (`Home` vs `Onboarding`). | M300-R01, M300-R05 |
| **N300-P05** | Integrar enrutamiento en el shell de navegación de Compose (`AppNavHost` / `AuthFlow`) con transiciones fluidas. | M300-R01, M300-R03 |
| **N300-P06** | Desarrollar pruebas unitarias completas (`OnboardingRoutingViewModelTest`, `MockAuthApiTest`) con `kotlinx-coroutines-test`. | M300-R01…M300-R06 |

## Dependencias y decisiones técnicas
- **Aprobado**: Material 3 y Jetpack Compose sin librerías externas superfluas.
- **Desacoplamiento de permisos de Android (MO-PR09)**: Los valores de `stepsCompleted` solo representan hitos en el backend; las peticiones de permisos en Android siguen gobernadas por `070-permisos-privacidad-accesibilidad` y APIs del SO.
- **Arranque offline seguro (MO-PR08)**: El uso de la caché local persistida previene que usuarios en zonas sin cobertura queden retenidos en pantallas de bienvenida si ya habían completado el proceso.

## Estrategia de pruebas
- **Pruebas de ViewModel:**
  - Usuario con `finishedAt != null` transiciona a `destination = Home`.
  - Usuario con `finishedAt == null` transiciona a `destination = Onboarding`.
  - Fallo de transporte con caché previa enruta a `Home`.
  - Fallo de transporte sin caché transiciona a `errorMessage != null` y permite reintento.
- **Pruebas de Repositorio y Mock:**
  - `getOnboardingStatus()` extrae token y resuelve DTO a modelo de dominio.
  - Centinela `net@error.test` emite `AuthResult.Failure(NETWORK)`.
- **Pruebas de regresión:**
  - Verificación de `./gradlew assembleDebug` y aprobación continua de los 50 tests preexistentes de autenticación.
