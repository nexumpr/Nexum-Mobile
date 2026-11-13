---
project: Nexum-Mobile
unit: 390-Listar-Movimientos
kind: technical-plan
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# PLAN — Listado de Movimientos (List Movements)

## Estrategia técnica
Implementar el listado paginado bajo el contrato **NEXUM-E027** en la arquitectura MVI y el patrón Repositorio:
1. **Contratos DTO y modelos de dominio:**
   - DTO de elemento: `MovementItemResponse(movementId, concept, amount, categoryId, type, occurredAt, origin, recurringPaymentId)`.
   - DTO de página: `MovementsPageResponse(items, pagination, totals)` con `pagination` y `totals` como contenedores provisionales.
   - Modelo de dominio: `MovementItem` y `MovementsPage(items, pagination, totals)`.
   - Filtro tipado: `MovementFilter { ALL, ANT, FIXED, SAVING }` con serialización a `all`/`ant`/`fixed`/`saving`.
2. **Ampliación de interfaces:**
   - En `AuthApi`: `suspend fun getMovements(accessToken: String, filter: MovementFilter, from: String?, to: String?, page: Int, limit: Int, sort: String?): MovementsPageResponse`.
   - En `AuthRepository`: `suspend fun getMovements(filter: MovementFilter, from: String?, to: String?, page: Int, limit: Int): AuthResult<MovementsPage>`.
3. **Flujo de lectura en Repositorio:**
   - Obtiene el token de acceso vigente de `TokenStorage`.
   - Envía los parámetros de filtro sin incluir `userId`.
   - Mapea `items` conservando `occurredAt` y `origin` tal como llegan; no reordena por fecha de captura.
   - Devuelve `AuthResult.Success(MovementsPage(...))`; traduce el sobre de error estándar a `AuthErrorKind` conservando `traceId`.
4. **Estados e Intenciones MVI:**
   - `MovementsListUiState` con filtro, rango, elementos acumulados, página, carga inicial, carga de página siguiente, vacío, sin conexión, error y `traceId`.
   - `MovementsListIntent` para cambiar filtro, fijar rango, cargar primera página, cargar página siguiente, reintentar y descartar error.
5. **Simulación en `MockAuthApi`:**
   - Retardo determinista de 250–600 ms.
   - Historial sintético determinista de al menos 25 elementos repartidos entre `ant`, `fixed` y `saving`.
   - Importes como cadena decimal (`"10.00"`, `"125.50"`, `"6500.00"`), moneda `MXN`, `occurredAt` en RFC 3339 UTC y `origin` (`"manual"`/`"voice"`).
   - Paginación por `page`/`limit`, filtro por `type` y rango `from`/`to`.
   - Centinela `net@error.test` lanza `AuthApiException(AuthErrorKind.NETWORK, ..., retryable = true)`.

## Contratos y modelos de datos (NEXUM-E027)

```kotlin
// Filtro de tipo (query param)
enum class MovementFilter(val wireValue: String) {
    ALL("all"),
    ANT("ant"),
    FIXED("fixed"),
    SAVING("saving")
}

// DTO de red: elemento de la página
data class MovementItemResponse(
    val movementId: String,
    val concept: String,
    val amount: String,
    val categoryId: String?,
    val type: String,
    val occurredAt: String,
    val origin: String?,
    val recurringPaymentId: String? = null
)

// DTO de red: página.
// `pagination` y `totals` son contenedores provisionales:
// su estructura interna está por confirmar con Backend.
data class MovementsPageResponse(
    val items: List<MovementItemResponse>,
    val pagination: Map<String, String> = emptyMap(),
    val totals: Map<String, String> = emptyMap()
)

// Modelo de dominio
data class MovementItem(
    val movementId: String,
    val concept: String,
    val amount: String,
    val categoryId: String?,
    val type: String,
    val occurredAt: String,
    val origin: String?
)

data class MovementsPage(
    val items: List<MovementItem>,
    val pagination: Map<String, String> = emptyMap(),
    val totals: Map<String, String> = emptyMap()
)

// Interfaz ampliada en AuthApi
interface AuthApi {
    // Métodos previos...
    suspend fun getMovements(
        accessToken: String,
        filter: MovementFilter,
        from: String?,
        to: String?,
        page: Int,
        limit: Int,
        sort: String? = null
    ): MovementsPageResponse
}

// Interfaz ampliada en AuthRepository
interface AuthRepository {
    // Métodos previos...
    suspend fun getMovements(
        filter: MovementFilter,
        from: String? = null,
        to: String? = null,
        page: Int = 1,
        limit: Int = 20
    ): AuthResult<MovementsPage>
}
```

## Estados e Intenciones MVI

```kotlin
data class MovementsListUiState(
    val filter: MovementFilter = MovementFilter.ALL,
    val from: String? = null,
    val to: String? = null,
    val items: List<MovementItem> = emptyList(),
    val page: Int = 1,
    val limit: Int = 20,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val canLoadMore: Boolean = true,
    val isEmpty: Boolean = false,
    val isOffline: Boolean = false,
    val errorMessage: String? = null,
    val traceId: String? = null
)

sealed interface MovementsListIntent {
    data class SelectFilter(val filter: MovementFilter) : MovementsListIntent
    data class SetDateRange(val from: String?, val to: String?) : MovementsListIntent
    data object LoadFirstPage : MovementsListIntent
    data object LoadNextPage : MovementsListIntent
    data object Retry : MovementsListIntent
    data object DismissError : MovementsListIntent
}
```

## Orden de trabajo
| Paso | Acción | Requisito rector |
| --- | --- | --- |
| **N390-P01** | Definir modelos del contrato NEXUM-E027 (`MovementFilter`, `MovementItemResponse`, `MovementsPageResponse`, `MovementItem`, `MovementsPage`). | M390-R02 |
| **N390-P02** | Ampliar `AuthApi` y `AuthRepository` con la consulta paginada, los filtros y el mapeo del sobre de errores. | M390-R02, M390-R04 |
| **N390-P03** | Implementar la simulación en `MockAuthApi` con historial determinista, paginación, filtros y centinela de red. | M390-R05 |
| **N390-P04** | Construir `MovementsListViewModel` MVI con carga inicial, página siguiente, cancelación al cambiar filtro y preservación de fecha y origen. | M390-R01, M390-R03, M390-R04 |
| **N390-P05** | Integrar `MovementsScreen` con pestañas, filtros de rango, celdas accesibles y acciones de carga. | M390-R01, M390-R03 |
| **N390-P06** | Desarrollar pruebas unitarias completas (`MovementsListViewModelTest`, `MockAuthApiTest`) con `kotlinx-coroutines-test`. | M390-R01…M390-R06 |

## Dependencias y decisiones técnicas
- **Aprobado:** Material 3 y Jetpack Compose sin librerías externas superfluas.
- **Sin IDOR (MO-PR06):** el contrato no recibe `userId`; el servidor delimita el historial por el token Bearer.
- **Fecha real y origen (MO-PR05):** `occurredAt` y `origin` se muestran tal como llegan; no se sustituyen por la fecha de consulta ni por un valor inferido.
- **Paginación provisional:** como la estructura de `pagination` no está confirmada, la UI solicita `page + 1` mediante una acción explícita "Cargar más" y deshabilita la acción cuando la última página devuelve menos elementos que `limit`; esta heurística se documenta como provisional y **por confirmar con Backend**.
- **Totales:** `totals` se conserva sin recalcular ni interpretar hasta que Backend defina su estructura.
- **Cancelación de filtros:** cada cambio de filtro o rango cancela la carga en curso y reinicia la página a 1.

## Estrategia de pruebas
- **Pruebas de ViewModel:**
  - `LoadFirstPage` publica los elementos y desactiva `isLoading`.
  - `LoadNextPage` añade la página 2 sin duplicar `movementId`.
  - `SelectFilter(ANT)` cancela la carga anterior y solicita `type=ant` con `page=1`.
  - Rango sin resultados activa `isEmpty` sin inventar filas.
  - Fallo de red activa `isOffline` y permite `Retry` sin dejar la lista a medias.
- **Pruebas de Repositorio y Mock:**
  - Los parámetros enviados incluyen `type`, `from`, `to`, `page` y `limit`, y nunca `userId`.
  - `occurredAt` y `origin` se conservan sin transformación.
  - Importes como `"125.50"` permanecen como cadena; no se usan `Float`/`Double`.
  - Centinela `net@error.test` emite `AuthResult.Failure(NETWORK)`.
- **Pruebas de regresión:**
  - Verificación de `./gradlew assembleDebug` y aprobación continua de los 50 tests preexistentes de autenticación.
