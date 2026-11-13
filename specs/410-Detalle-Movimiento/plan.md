---
project: Nexum-Mobile
unit: 410-Detalle-Movimiento
kind: technical-plan
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# PLAN — Detalle de Movimiento (Get Movement Detail)

## Estrategia técnica
Implementar la consulta puntual del movimiento bajo el contrato **NEXUM-E029** en la arquitectura MVI y el patrón Repositorio:
1. **Contratos DTO y modelos de dominio:**
   - DTO de red: `MovementDetailResponse(movementId, concept, amount, categoryId, type, occurredAt, origin, recurringPaymentId, goalId)`.
   - Modelo de dominio: `MovementDetail` con los mismos campos e importes como `String`.
2. **Ampliación de interfaces:**
   - En `AuthApi`: `suspend fun getMovementDetail(accessToken: String, movementId: String): MovementDetailResponse`.
   - En `AuthRepository`: `suspend fun getMovementDetail(movementId: String): AuthResult<MovementDetail>`.
3. **Flujo de lectura en Repositorio:**
   - Valida que `movementId` no esté vacío antes de llamar.
   - Obtiene el token de acceso vigente de `TokenStorage` y consulta el detalle.
   - En `200 OK` mapea el objeto `movement` conservando `occurredAt` y `origin`; no consulta la lista en memoria como respaldo.
   - En `404` devuelve `AuthResult.Failure(AuthErrorKind.NOT_FOUND)` sin distinguir entre inexistente y ajeno.
   - Traduce el sobre de error estándar a `AuthErrorKind` conservando `traceId`.
4. **Estados e Intenciones MVI:**
   - `MovementDetailUiState` con `movementId`, carga, detalle, no encontrado, sin conexión, error y `traceId`.
   - `MovementDetailIntent` para cargar, reintentar, descartar error, editar y volver.
   - Efectos `NavigateToEdit(movementId)` y `NavigateBack`.
5. **Simulación en `MockAuthApi`:**
   - Retardo determinista de 200–500 ms.
   - Catálogo sintético de detalles por `movementId` con importes como cadena decimal, moneda `MXN`, fechas RFC 3339 UTC y vínculos opcionales.
   - Identificadores desconocidos o marcados como ajenos responden `404`.
   - Centinela `net@error.test` lanza `AuthApiException(AuthErrorKind.NETWORK, ..., retryable = true)`.

## Contratos y modelos de datos (NEXUM-E029)

```kotlin
// DTO de red: detalle del movimiento.
// Los campos de vínculo son identificadores opcionales; su forma
// definitiva está por confirmar con Backend.
data class MovementDetailResponse(
    val movementId: String,
    val concept: String,
    val amount: String,
    val categoryId: String?,
    val type: String,
    val occurredAt: String,
    val origin: String?,
    val recurringPaymentId: String? = null,
    val goalId: String? = null
)

// Modelo de dominio
data class MovementDetail(
    val movementId: String,
    val concept: String,
    val amount: String,
    val categoryId: String?,
    val type: String,
    val occurredAt: String,
    val origin: String?,
    val recurringPaymentId: String? = null,
    val goalId: String? = null
)

// Interfaz ampliada en AuthApi
interface AuthApi {
    // Métodos previos...
    suspend fun getMovementDetail(
        accessToken: String,
        movementId: String
    ): MovementDetailResponse
}

// Interfaz ampliada en AuthRepository
interface AuthRepository {
    // Métodos previos...
    suspend fun getMovementDetail(movementId: String): AuthResult<MovementDetail>
}
```

## Estados e Intenciones MVI

```kotlin
data class MovementDetailUiState(
    val movementId: String = "",
    val isLoading: Boolean = false,
    val movement: MovementDetail? = null,
    val isNotFound: Boolean = false,
    val isOffline: Boolean = false,
    val errorMessage: String? = null,
    val traceId: String? = null
)

sealed interface MovementDetailIntent {
    data object Load : MovementDetailIntent
    data object Retry : MovementDetailIntent
    data object DismissError : MovementDetailIntent
    data object Edit : MovementDetailIntent
    data object Back : MovementDetailIntent
}

sealed interface MovementDetailEffect {
    data class NavigateToEdit(val movementId: String) : MovementDetailEffect
    data object NavigateBack : MovementDetailEffect
}
```

## Orden de trabajo
| Paso | Acción | Requisito rector |
| --- | --- | --- |
| **N410-P01** | Definir modelos del contrato NEXUM-E029 (`MovementDetailResponse`, `MovementDetail`). | M410-R02 |
| **N410-P02** | Ampliar `AuthApi` y `AuthRepository` con la consulta puntual y el mapeo de `404`. | M410-R02, M410-R04 |
| **N410-P03** | Implementar la simulación en `MockAuthApi` con detalles sintéticos, `404` y centinela de red. | M410-R05 |
| **N410-P04** | Construir `MovementDetailViewModel` MVI con estados de contenido, no encontrado, error y navegación. | M410-R01, M410-R03, M410-R04 |
| **N410-P05** | Integrar `MovementDetailScreen` en Compose con Material 3, vínculos opcionales y acciones. | M410-R01, M410-R03 |
| **N410-P06** | Desarrollar pruebas unitarias completas (`MovementDetailViewModelTest`, `MockAuthApiTest`) con `kotlinx-coroutines-test`. | M410-R01…M410-R06 |

## Dependencias y decisiones técnicas
- **Aprobado:** Material 3 y Jetpack Compose sin librerías externas superfluas.
- **Consulta puntual (MO-PR04):** el detalle se obtiene del endpoint E029; no se reconstruye desde la lista en memoria ni desde la caché de otra pantalla.
- **Sin filtración de pertenencia (MO-PR06):** el `404` se presenta de forma neutra; la UI no intenta distinguir si el movimiento existe en otra cuenta.
- **Fecha real y origen (MO-PR05):** se muestran tal como llegan; no se transforman ni se sustituyen por la fecha de consulta.
- **Vínculos opcionales:** `recurringPaymentId` y `goalId` solo se muestran si la respuesta los incluye; no se inventan nombres ni importes del pago recurrente o la meta.
- **Sin caché por defecto:** el detalle no se persiste localmente salvo que una decisión posterior lo autorice.

## Estrategia de pruebas
- **Pruebas de ViewModel:**
  - `Load` publica `movement` con importe, fecha y origen sin alteraciones.
  - `404` activa `isNotFound` y no publica un detalle parcial.
  - Fallo de transporte activa `isOffline` y permite `Retry`.
  - `Edit` emite `NavigateToEdit` con el `movementId` correcto.
  - Detalle sin vínculos no muestra secciones vacías ni ceros.
- **Pruebas de Repositorio y Mock:**
  - La ruta incluye `movementId` y la cabecera `Authorization`, sin `userId`.
  - Un identificador desconocido o ajeno devuelve `AuthResult.Failure(NOT_FOUND)`.
  - Importes como `"45.00"` permanecen como cadena; no se usan `Float`/`Double`.
  - Centinela `net@error.test` emite `AuthResult.Failure(NETWORK)`.
- **Pruebas de regresión:**
  - Verificación de `./gradlew assembleDebug` y aprobación continua de los 50 tests preexistentes de autenticación.
