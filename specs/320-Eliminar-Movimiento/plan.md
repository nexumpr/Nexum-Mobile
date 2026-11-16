---
project: Nexum-Mobile
unit: 320-Eliminar-Movimiento
kind: technical-plan
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# PLAN — Eliminación y Reversión de Movimiento (Delete Movement)

## Estrategia técnica
1. **Contratos DTO y métodos:**
   - Método en `MovementsApi`: `suspend fun deleteMovement(accessToken: String, movementId: String, ifMatch: String? = null)`
   - Método en `MovementsRepository`: `suspend fun deleteMovement(movementId: String): AuthResult<Unit>`
2. **Estados e Intenciones MVI:**
   - `DeleteMovementUiState`: `isDeleting: Boolean = false`, `error: String? = null`
   - `DeleteMovementIntent`: `ConfirmDelete(movementId)`
3. **Simulación en Mock:**
   - Borra el movimiento de la lista en memoria.
   - Si `movement.goalId != null`, busca la meta asociada y reduce `goal.saved -= movement.amount`.
   - Retorna `204 No Content`.
