---
project: Nexum-Mobile
unit: 400-Registrar-Movimiento
kind: technical-plan
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# PLAN — Registro de Movimiento (Gasto Realizado) (Create Movement)

## Estrategia técnica
Implementar el alta de un gasto realizado bajo el contrato **NEXUM-E028** en la arquitectura MVI y el patrón Repositorio:
1. **Contratos DTO y modelos de dominio:**
   - DTO de entrada: `CreateMovementRequest(concept, amount, categoryId, type, occurredAt, recurringPaymentId)`.
   - DTO de respuesta: `CreateMovementResponse(movementId, record, updatedSummary)`.
   - Modelo de dominio: `MovementDraft` y `RegisteredMovement(movementId, record, updatedSummary)`.
   - Validación: `MovementInputValidator` con `BigDecimal`, sin `Float`/`Double`.
2. **Ampliación de interfaces:**
   - En `AuthApi`: `suspend fun createMovement(accessToken: String, request: CreateMovementRequest, idempotencyKey: String): CreateMovementResponse`.
   - En `AuthRepository`: `suspend fun createMovement(draft: MovementDraft, idempotencyKey: String): AuthResult<RegisteredMovement>`.
3. **Flujo de ejecución en Repositorio:**
   - Valida `amount > 0`, dos decimales, `type` en `ant`/`fixed` y `categoryId` no vacío.
   - Canonicaliza el importe a dos decimales antes de enviar.
   - Obtiene el token de acceso vigente de `TokenStorage` y envía la `Idempotency-Key` recibida del ViewModel.
   - En `201 Created` mapea `movementId`, `record` y `updatedSummary`; no crea nada localmente si la respuesta no confirma el alta.
   - Traduce el sobre de error estándar a `AuthErrorKind` conservando `traceId`; `409` se expone como conflicto de idempotencia.
4. **Estados e Intenciones MVI:**
   - `RegisterMovementUiState` con borrador, errores de campo, guardado, resultado, sin conexión, error y `traceId`.
   - `RegisterMovementIntent` para editar campos, aplicar propuesta de voz, guardar, reintentar, descartar error y consumir resultado.
   - La `Idempotency-Key` se genera una vez por borrador, se conserva durante reintentos y se renueva solo después de un alta confirmada.
5. **Simulación en `MockAuthApi`:**
   - Retardo determinista de 300–700 ms.
   - Valida importe, decimales y tipo; si falla responde `422`.
   - Genera `movementId` determinista, devuelve `record` y `updatedSummary` con importes como cadena decimal y moneda `MXN`.
   - Deduplica por `Idempotency-Key`: la misma llave devuelve el mismo resultado sin crear un segundo movimiento.
   - Centinelas `net@error.test` (red) y `rule_error_movement` (`422`).

## Contratos y modelos de datos (NEXUM-E028)

```kotlin
// DTO de red: alta de gasto (contrato NEXUM-E028)
data class CreateMovementRequest(
    val concept: String,
    val amount: String,
    val categoryId: String,
    val type: String,          // "ant" | "fixed"
    val occurredAt: String,    // RFC 3339 UTC
    val recurringPaymentId: String? = null
)

// DTO de red: respuesta 201.
// `record` se alinea con el elemento de E027 y `updatedSummary`
// con el resumen de E026; la forma exacta queda por confirmar con Backend.
data class CreateMovementResponse(
    val movementId: String,
    val record: MovementItemResponse,
    val updatedSummary: FinancialSummaryResponse? = null
)

// Borrador de dominio (importes siempre String)
data class MovementDraft(
    val concept: String,
    val amount: String,
    val categoryId: String,
    val type: String,
    val occurredAt: String,
    val recurringPaymentId: String? = null
)

data class RegisteredMovement(
    val movementId: String,
    val record: MovementItem,
    val updatedSummary: FinancialSummary?
)

// Interfaz ampliada en AuthApi
interface AuthApi {
    // Métodos previos...
    suspend fun createMovement(
        accessToken: String,
        request: CreateMovementRequest,
        idempotencyKey: String
    ): CreateMovementResponse
}

// Interfaz ampliada en AuthRepository
interface AuthRepository {
    // Métodos previos...
    suspend fun createMovement(
        draft: MovementDraft,
        idempotencyKey: String
    ): AuthResult<RegisteredMovement>
}
```

## Estados e Intenciones MVI

```kotlin
enum class MovementField { CONCEPT, AMOUNT, CATEGORY, TYPE, OCCURRED_AT }

data class RegisterMovementUiState(
    val concept: String = "",
    val amountInput: String = "",
    val categoryId: String? = null,
    val type: String = "ant",
    val occurredAt: String = "",
    val recurringPaymentId: String? = null,
    val isVoiceDraft: Boolean = false,
    val fieldErrors: Map<MovementField, String> = emptyMap(),
    val isSaving: Boolean = false,
    val registered: RegisteredMovement? = null,
    val isOffline: Boolean = false,
    val errorMessage: String? = null,
    val traceId: String? = null
)

sealed interface RegisterMovementIntent {
    data class ConceptChanged(val value: String) : RegisterMovementIntent
    data class AmountChanged(val value: String) : RegisterMovementIntent
    data class CategorySelected(val categoryId: String) : RegisterMovementIntent
    data class TypeSelected(val type: String) : RegisterMovementIntent
    data class OccurredAtChanged(val occurredAt: String) : RegisterMovementIntent
    data class ApplyVoiceDraft(
        val concept: String,
        val amount: String,
        val categoryId: String?,
        val occurredAt: String
    ) : RegisterMovementIntent

    data object Save : RegisterMovementIntent
    data object Retry : RegisterMovementIntent
    data object DismissError : RegisterMovementIntent
    data object ConsumeRegistered : RegisterMovementIntent
}

sealed interface RegisterMovementEffect {
    data object NavigateToMovements : RegisterMovementEffect
    data class ShowConfirmation(val movementId: String) : RegisterMovementEffect
}
```

## Orden de trabajo
| Paso | Acción | Requisito rector |
| --- | --- | --- |
| **N400-P01** | Definir modelos del contrato NEXUM-E028 (`CreateMovementRequest`, `CreateMovementResponse`, `MovementDraft`, `RegisteredMovement`, `MovementInputValidator`). | M400-R02 |
| **N400-P02** | Ampliar `AuthApi` y `AuthRepository` con el alta, la validación y el envío de `Idempotency-Key`. | M400-R02, M400-R04 |
| **N400-P03** | Implementar la simulación en `MockAuthApi` con alta en memoria, deduplicación por llave y centinelas. | M400-R05 |
| **N400-P04** | Construir `RegisterMovementViewModel` MVI con validación, propuesta de voz y conservación del borrador ante error. | M400-R01, M400-R03, M400-R04 |
| **N400-P05** | Integrar el formulario "Agregar gasto" y la confirmación por voz en Compose con Material 3. | M400-R01, M400-R03 |
| **N400-P06** | Desarrollar pruebas unitarias completas (`RegisterMovementViewModelTest`, `MovementInputValidatorTest`, `MockAuthApiTest`) con `kotlinx-coroutines-test`. | M400-R01…M400-R06 |

## Dependencias y decisiones técnicas
- **Aprobado:** Material 3 y Jetpack Compose sin librerías externas superfluas.
- **Dinero con `BigDecimal`:** la validación y la canonicalización usan `java.math.BigDecimal`; nunca `Float`/`Double`.
- **Idempotencia (MO-PR04):** el ViewModel conserva una llave por borrador; los reintentos reutilizan la misma llave y solo se genera otra tras un `201` confirmado.
- **Gasto realizado (MO-PR05):** el alta no programa vencimientos ni recurrencias; `occurredAt` es la fecha real del evento y no la fecha de captura.
- **Tipo limitado:** la UI y el validador solo admiten `ant` y `fixed`; `saving` se gestiona con reglas propias fuera de esta unidad.
- **Categoría por identificador (MO-PR04):** se envía `categoryId` validado por el servidor; el nombre visible nunca actúa como llave.
- **Resumen actualizado:** `updatedSummary` se presenta solo cuando llega en la respuesta; su estructura definitiva queda **por confirmar con Backend**.

## Estrategia de pruebas
- **Pruebas de ViewModel:**
  - Un borrador válido (`"Gomitas"`, `"10.00"`, `antojos`, `ant`) produce `isSaving = true` y luego `ShowConfirmation`.
  - `"0.00"`, `"-5.00"` y `"10.5"` bloquean el envío con error de campo.
  - Dos `Save` consecutivos generan una sola petición.
  - Fallo de red conserva el borrador y `Retry` reutiliza la misma llave.
  - `ApplyVoiceDraft` precarga los campos y marca `isVoiceDraft = true` sin enviar nada.
  - `422` y `404` de categoría conservan la selección y muestran el mensaje.
- **Pruebas de Repositorio y Mock:**
  - El payload enviado contiene `concept`, `amount`, `categoryId`, `type` y `occurredAt`, sin `userId`.
  - La misma `Idempotency-Key` no crea dos movimientos.
  - `record` conserva `occurredAt` y el importe como cadena decimal.
  - Centinelas `net@error.test` y `rule_error_movement` producen `NETWORK` y `VALIDATION`.
- **Pruebas de regresión:**
  - Verificación de `./gradlew assembleDebug` y aprobación continua de los 50 tests preexistentes de autenticación.
