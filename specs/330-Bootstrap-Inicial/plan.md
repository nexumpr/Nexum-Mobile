---
project: Nexum-Mobile
unit: 330-Bootstrap-Inicial
kind: technical-plan
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# PLAN — Carga Inicial Agregada (Bootstrap)

## Estrategia técnica
Implementar la carga inicial agregada bajo el contrato **NEXUM-E021** en la arquitectura MVI y el patrón Repositorio:
1. **Contratos DTO y modelos de dominio:**
   - DTO de respuesta: `BootstrapResponse(profile, summary, streak, config, counts)` con secciones opcionales.
   - `summary` reutiliza los importes documentados del resumen financiero como cadenas decimales.
   - Modelo de dominio: `BootstrapSnapshot(profile, summary, streak, config, counts)`.
2. **Ampliación de interfaces:**
   - En `AuthApi`: `suspend fun getBootstrap(accessToken: String, include: List<String>): BootstrapResponse`.
   - En `AuthRepository`: `suspend fun getBootstrap(include: List<String> = DEFAULT_INCLUDE): AuthResult<BootstrapSnapshot>`.
3. **Flujo de ejecución y caché en Repositorio:**
   - Obtiene el token de acceso vigente de `TokenStorage`.
   - Envía `authApi.getBootstrap(accessToken, include)`.
   - En `200 OK` mapea secciones nulas sin inventar valores y persiste la instantánea como caché de lectura etiquetada con su fecha de obtención.
   - Ante fallo de transporte, devuelve la última instantánea en caché con indicador de antigüedad; si no existe, `AuthResult.Failure(NETWORK)`.
   - Traduce el sobre de error estándar a `AuthErrorKind` conservando `traceId`.
4. **Estados e Intenciones MVI:**
   - `HomeUiState` con carga, instantánea, bandera de contenido parcial, sin conexión, error y `traceId`.
   - `HomeIntent` para cargar, refrescar, reintentar y descartar error.
5. **Simulación en `MockAuthApi`:**
   - Retardo determinista de 250–600 ms.
   - Instantánea del usuario activo con perfil, resumen documentado (`12000.00`, `6500.00`, `0.00`, `350.00`, `200.00`, `4950.00`, `MXN`), racha y conteos sintéticos provisionales.
   - Respeta `include`; si falta una sección solicitada devuelve `null`.
   - Centinela `net@error.test` lanza `AuthApiException(AuthErrorKind.NETWORK, ..., retryable = true)`.

## Contratos y modelos de datos (NEXUM-E021)

```kotlin
// DTO de red (contrato NEXUM-E021)
data class BootstrapResponse(
    val profile: UserProfileResponse?,
    val summary: BootstrapSummaryResponse?,
    // Contenedores provisionales: estructura interna por confirmar con Backend.
    val streak: Map<String, String> = emptyMap(),
    val config: Map<String, String> = emptyMap(),
    val counts: Map<String, String> = emptyMap()
)

data class BootstrapSummaryResponse(
    val incomeEquivalent: String,
    val recurringFixedEstimate: String,
    val oneTimeFixed: String,
    val antExpenses: String,
    val goalReservations: String,
    val availableEstimate: String,
    val currency: String
)

// Modelo de dominio
data class BootstrapSnapshot(
    val profile: UserProfile?,
    val summary: BootstrapSummary?,
    val streak: Map<String, String>,
    val config: Map<String, String>,
    val counts: Map<String, String>,
    val fetchedAt: String? = null
)

// Interfaz ampliada en AuthApi
interface AuthApi {
    // Métodos previos...
    suspend fun getBootstrap(
        accessToken: String,
        include: List<String>
    ): BootstrapResponse
}

// Interfaz ampliada en AuthRepository
interface AuthRepository {
    // Métodos previos...
    suspend fun getBootstrap(
        include: List<String> = listOf("summary", "profile", "preferences")
    ): AuthResult<BootstrapSnapshot>
}
```

## Estados e Intenciones MVI

```kotlin
data class HomeUiState(
    val isLoading: Boolean = true,
    val snapshot: BootstrapSnapshot? = null,
    val isPartial: Boolean = false,
    val isOffline: Boolean = false,
    val errorMessage: String? = null,
    val traceId: String? = null
)

sealed interface HomeIntent {
    data class LoadBootstrap(
        val include: List<String> = listOf("summary", "profile", "preferences")
    ) : HomeIntent

    data object Refresh : HomeIntent
    data object Retry : HomeIntent
    data object DismissError : HomeIntent
}

sealed interface HomeEffect {
    data class ShowMessage(val message: String) : HomeEffect
}
```

## Orden de trabajo
| Paso | Acción | Requisito rector |
| --- | --- | --- |
| **N330-P01** | Definir modelos del contrato NEXUM-E021 (`BootstrapResponse`, `BootstrapSummaryResponse`, `BootstrapSnapshot`). | M330-R02 |
| **N330-P02** | Ampliar `AuthApi` y `AuthRepository` con la carga agregada, `include` y degradación con caché de lectura. | M330-R02, M330-R04 |
| **N330-P03** | Implementar la simulación en `MockAuthApi` con instantánea determinista, filtro `include` y centinela de red. | M330-R05 |
| **N330-P04** | Construir `HomeViewModel` MVI con carga única, contenido parcial, reintento y preservación de la última instantánea. | M330-R01, M330-R03, M330-R04 |
| **N330-P05** | Integrar `HomeScreen` en Compose con esqueleto de carga, tarjeta de resumen, racha y conteos accesibles. | M330-R01, M330-R03 |
| **N330-P06** | Desarrollar pruebas unitarias completas (`HomeViewModelTest`, `MockAuthApiTest`) con `kotlinx-coroutines-test`. | M330-R01…M330-R06 |

## Dependencias y decisiones técnicas
- **Aprobado:** Material 3 y Jetpack Compose sin librerías externas superfluas.
- **Agregador opcional (MO-PR04):** el bootstrap es una optimización de lectura; ninguna escritura, confirmación ni mutación se deriva de él.
- **Sin IDOR (MO-PR06):** el contrato no recibe `userId`; el servidor delimita la instantánea por el token Bearer.
- **Secciones desconocidas:** `streak`, `config` y `counts` se conservan como contenedores provisionales hasta que Backend cierre su estructura; la UI no inventa ceros ni valores por defecto.
- **Caché de lectura:** la instantánea guardada se marca con `fetchedAt` y se presenta como anterior si no hay red, sin declararla confirmada.

## Estrategia de pruebas
- **Pruebas de ViewModel:**
  - `LoadBootstrap` activa `isLoading = true` y publica `snapshot` al recibir `200 OK`.
  - `include=summary,profile` produce estado sin racha ni conteos y sin error.
  - Secciones nulas activan `isPartial = true` sin valores inventados.
  - Fallo de transporte con caché previa conserva la instantánea y activa `isOffline`.
  - Fallo sin caché publica error con `traceId` y permite `Retry`.
- **Pruebas de Repositorio y Mock:**
  - `getBootstrap()` envía `include` y cabecera `Authorization` sin identificador de usuario.
  - Montos y moneda se conservan como cadena decimal; no se usa `Float`/`Double`.
  - Centinela `net@error.test` emite `AuthResult.Failure(NETWORK)`.
- **Pruebas de regresión:**
  - Verificación de `./gradlew assembleDebug` y aprobación continua de los 50 tests preexistentes de autenticación.
