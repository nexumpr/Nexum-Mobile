---
project: Nexum-Mobile
unit: 340-Registrar-Pago-Recurrente
kind: implementation-tasks
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# TASK — Registro de Pago Recurrente Realizado (Record Recurring Payment)

**Estado:** Fase A documental completada.

- [ ] **N340-T01 — Definir DTOs de registro de pago (`RecordPaymentRequest`, `RecordPaymentResponse`).**
- [ ] **N340-T02 — Ampliar `RecurringPaymentsApi` y repositorio con el método de registro de ocurrencia.**
- [ ] **N340-T03 — Implementar simulación en `MockRecurringPaymentsApi` con generación de movimiento y deduplicación por `Idempotency-Key`.**
- [ ] **N340-T04 — Construir soporte en ViewModel MVI para acción de pago (✓).**
- [ ] **N340-T05 — Integrar botón de registro de pago en tarjeta de pagos recurrentes.**
- [ ] **N340-T06 — Añadir pruebas unitarias con `kotlinx-coroutines-test`.**
