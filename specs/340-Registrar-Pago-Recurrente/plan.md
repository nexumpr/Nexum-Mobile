---
project: Nexum-Mobile
unit: 340-Registrar-Pago-Recurrente
kind: technical-plan
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# PLAN — Registro de Pago Recurrente Realizado (Record Recurring Payment)

## Estrategia técnica
1. **Contratos DTO:**
   - `RecordPaymentRequest(val occurredAt: String, val amount: Double?)`
   - `RecordPaymentResponse(val movementId: String, val recurringPaymentId: String, val updatedSummary: SummaryDto)`
2. **Métodos en Repositorio y API:**
   - `suspend fun recordRecurringPayment(recurringPaymentId: String, occurredAt: String, amount: Double?): AuthResult<RecordPaymentResponse>`
3. **Simulación en Mock:**
   - Valida la existencia de la regla, genera un nuevo movimiento en la colección de movimientos, asocia el ID y retorna `201 Created` con el resumen actualizado.
