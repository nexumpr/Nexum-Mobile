---
project: Nexum-Mobile
unit: 420-Editar-Movimiento
kind: technical-plan
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# PLAN — Edición de Movimiento (Edit Movement)

## Estrategia técnica
Implementar la corrección parcial del movimiento bajo el contrato **NEXUM-E030** en la arquitectura MVI y el patrón Repositorio:
1. **Contratos DTO y modelos de dominio:**
   - DTO de entrada parcial: `UpdateMovementRequest(concept, amount, categoryId, type)` con campos nulos omitidos en la serialización.
   - DTO de respuesta: `UpdateMovementResponse(movement, summaryVersion)`.
   - Modelo de dominio: `MovementEditDraft` y `MovementEditResult(movement, summaryVersion)`.
   - Validación: `MovementEditValidator` con `BigDecimal`, sin `Float`/`Double`.
2. **Ampliación de interfaces:**
   - En `AuthApi`: `suspend fun updateMovement(accessToken: String, movementId: String, request: UpdateMovementRequest): UpdateMovementResponse`.
   - En `AuthRepository`: `suspend fun updateMovement(movementId: String, draft: MovementEditDraft): AuthResult<MovementEditResult>`.
3. **Flujo de ejecución en Repositorio:**
   - Recibe el detalle original de `410-Detalle-Movimiento` y construye un `PATCH` con **solo los campos modificados**.
   - Rechaza `type = saving` antes de la red y valida `amount > 0` con dos decimales cuando se envía.
   - Envía el `PATCH` con el token vigente y sin `userId`.
   - En `200 OK` mapea `movement` y `summaryVersion`, e invalida la caché del resumen afectado sin recalcularlo localmente.
   - En `404` devuelve `AuthResult.Failure(NOT_FOUND)` neutro; traduce el sobre de error estándar conservando `traceId`.
4. **Estados e Intenciones MVI:**
   - `EditMovementUiState` con original, campos editables, errores, guardado, resultado, sin conexión, error y `traceId`.
   - `EditMovementIntent` para cargar el original, cambiar campos, guardar, reintentar, descartar error, consumir resultado y cancelar.
   - Efectos `NavigateBack` y `ShowConfirmation(summaryVersion)`.
5. **Simulación en `MockAuthApi`:**
   - Retardo determinista de 300–700 ms.
   - Aplica el cambio parcial sobre el movimiento sintético en memoria y devuelve `movement` actualizado con `summaryVersion` incrementado.
   - Rechaza `saving` y montos inválidos con `422`; identificador desconocido con `404`.
   - Centinelas `net@error.test` (red) y `rule_error_movement` (`422`).

## Contratos y modelos de datos (NEXUM-E030)

```kotlin
// DTO de red: edición parcial (contrato NEXUM-E030).
// Los campos nulos se omiten del cuerpo.
data class UpdateMovementRequest(
    val concept: String? = null,
    val amount: String? = null,
    val categoryId: String? = null,
    val type: String? = null      // "ant" | "fixed"
)

// DTO de red: respuesta 200.
// `summaryVersion` se modela como String opaco; su tipo exacto
// está por confirmar con Backend.
data class UpdateMovementResponse(
    val movement: MovementItemResponse,
    val summaryVersion: String
)

// Borrador de edición de dominio (importes siempre String)
data class MovementEditDraft(
    val concept: String? = null,
    val amount: String? = null,
    val categoryId: String? = null,
    val type: String? = null
)

data class MovementEditResult(
    val movement: MovementItem,
    val summaryVersion: String
)

// Interfaz ampliada en AuthApi
interface AuthApi {
    // Métodos previos...
    suspend fun updateMovement(
        accessToken: String,
        movementId: String,
        request: UpdateMovementRequest
    ): UpdateMovementResponse
}

// Interfaz ampliada en AuthRepository
interface AuthRepository {
    // Métodos previos...
    suspend fun updateMovement(
        movementId: String,
        draft: MovementEditDraft
    ): AuthResult<MovementEditResult>
}
```

## Estados e Intenciones MVI

```kotlin
data class EditMovementUiState(
    val movementId: String = "",
    val isLoadingOriginal: Boolean = false,
    val concept: String = "",
    val amountInput: String = "",
    val categoryId: String? = null,
    val type: String = "ant",
    val fieldErrors: Map<MovementField, String> = emptyMap(),
    val isSaving: Boolean = false,
    val result: MovementEditResult? = null,
    val isOffline: Boolean = false,
    val errorMessage: String? = null,
    val traceId: String? = null
)

sealed interface EditMovementIntent {
    data object LoadOriginal : EditMovementIntent
    data class ConceptChanged(val value: String) : EditMovementIntent
    data class AmountChanged(val value: String) : EditMovementIntent
    data class CategorySelected(val categoryId: String) : EditMovementIntent
    data class TypeSelected(val type: String) : EditMovementIntent
    data object Save : EditMovementIntent
    data object Retry : EditMovementIntent
    data object DismissError : EditMovementIntent
    data object ConsumeResult : EditMovementIntent
    data object Cancel : EditMovementIntent
}

sealed interface EditMovementEffect {
    data object NavigateBack : EditMovementEffect
    data class ShowConfirmation(val summaryVersion: String) : EditMovementEffect
}
```

## Orden de trabajo
| Paso | Acción | Requisito rector |
| --- | --- | --- |
| **N420-P01** | Definir modelos del contrato NEXUM-E030 (`UpdateMovementRequest`, `UpdateMovementResponse`, `MovementEditDraft`, `MovementEditResult`, `MovementEditValidator`). | M420-R02 |
| **N420-P02** | Ampliar `AuthApi` y `AuthRepository` con la edición parcial, la omisión de campos no modificados y la invalidación del resumen. | M420-R02, M420-R04 |
| **N420-P03** | Implementar la simulación en `MockAuthApi` con cambio en memoria, rechazo de `saving` y centinelas. | M420-R05 |
| **N420-P04** | Construir `EditMovementViewModel` MVI con precarga, validación, conservación del formulario y control de doble envío. | M420-R01, M420-R03, M420-R04 |
| **N420-P05** | Integrar el formulario "Editar movimiento" en Compose con Material 3 y accesibilidad. | M420-R01, M420-R03 |
| **N420-P06** | Desarrollar pruebas unitarias completas (`EditMovementViewModelTest`, `MovementEditValidatorTest`, `MockAuthApiTest`) con `kotlinx-coroutines-test`. | M420-R01…M420-R06 |

## Dependencias y decisiones técnicas
- **Aprobado:** Material 3 y Jetpack Compose sin librerías externas superfluas.
- **Sin conversión a ahorro (MO-PR04):** el tipo se restringe a `ant`/`fixed`; `saving` se rechaza en el validador y en el mock, y las aportaciones quedan para reglas propias.
- **PATCH parcial:** el repositorio envía solo los campos modificados; si el servidor interpreta de otro modo los campos ausentes, se ajustará al contrato confirmado.
- **Dinero con `BigDecimal`:** la validación y la canonicalización usan `java.math.BigDecimal`; nunca `Float`/`Double`.
- **Consistencia del resumen:** el cliente invalida la caché afectada al recibir `summaryVersion`; no recalcula presupuestos, estadísticas ni disponible.
- **Sin filtración de pertenencia (MO-PR06):** un `404` se muestra de forma neutra.
- **`summaryVersion` opaco:** se conserva como cadena para no imponer un tipo no confirmado.

## Estrategia de pruebas
- **Pruebas de ViewModel:**
  - `LoadOriginal` precarga concepto, importe, categoría y tipo del detalle.
  - `ConceptChanged` + `Save` envía solo `concept` y produce `ShowConfirmation`.
  - `TypeSelected("saving")` se rechaza con error y no emite petición.
  - `"0.00"`, `"-5.00"` y `"125.5"` bloquean el envío con error de campo.
  - `404` activa mensaje neutro; `409`/`422` conservan el formulario.
  - Doble pulsación genera un solo envío.
- **Pruebas de Repositorio y Mock:**
  - El `PATCH` incluye solo los campos modificados y nunca `userId`.
  - `saving` devuelve `AuthResult.Failure(VALIDATION)` sin tocar la red simulada.
  - La misma operación no crea un segundo movimiento.
  - Centinelas `net@error.test` y `rule_error_movement` producen `NETWORK` y `VALIDATION`.
- **Pruebas de regresión:**
  - Verificación de `./gradlew assembleDebug` y aprobación continua de los 50 tests preexistentes de autenticación.
