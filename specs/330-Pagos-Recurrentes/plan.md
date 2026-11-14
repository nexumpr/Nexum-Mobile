---
project: Nexum-Mobile
unit: 330-Pagos-Recurrentes
kind: technical-plan
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# PLAN — Gestión de Pagos Recurrentes (Recurring Payments CRUD)

## Estrategia técnica
1. **Contratos DTO y Endpoints:**
   - `RecurringPaymentDto`, `CreateRecurringPaymentRequest`, `UpdateRecurringPaymentRequest`, `RecurringPaymentsListResponse`.
   - Métodos en `RecurringPaymentsApi` y `RecurringPaymentsRepository`.
2. **Estados MVI y UI:**
   - `RecurringPaymentsUiState`: listado, equivalencia mensual fija, carga, errores.
   - Pantallas de listado (`RecurringPaymentsScreen`), creación y edición (`RecurringPaymentFormScreen`).
3. **Simulación en Mock:**
   - Listado en memoria, filtrado por `active`, mutación y soft-delete con preservación de historial de pagos.
