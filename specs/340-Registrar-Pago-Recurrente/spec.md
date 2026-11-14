---
project: Nexum-Mobile
unit: 340-Registrar-Pago-Recurrente
kind: technical-spec
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# SPEC — Registro de Pago Recurrente Realizado (Record Recurring Payment)

## Propósito
Permitir a un usuario registrar que se ha pagado una ocurrencia concreta de una programación periódica (accionando el icono **✓** de la maqueta) bajo el contrato **NEXUM-E037** (`POST /api/v1/recurring-payments/{recurringPaymentId}/payments` [M], catálogo V6.0.2). Esto crea un movimiento vinculado en el historial y los presupuestos sin sumar dos veces el mismo monto fijo al disponible mensual, garantizando deduplicación por ocurrencia mediante `Idempotency-Key`.

## Lugar en el plan SDD
- **Constitución:** [`.specify/memory/constitution.md`](../../.specify/memory/constitution.md) (vigente: MO-PR04 Backend autoritativo, MO-PR06 Privacidad y seguridad, MO-PR08 Resiliencia offline, MO-PR17 Transacciones atómicas).
- **Tecnología:** Kotlin / Jetpack Compose / MVI.
- **Contrato:** NEXUM-E037 (`POST /api/v1/recurring-payments/{recurringPaymentId}/payments` [M]).
- **Dependencias previas:** [`330-Pagos-Recurrentes`](../330-Pagos-Recurrentes/spec.md), [`140-Login`](../140-Login/spec.md), [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md).

## Requisitos y criterios verificables
| ID | Requisito obligatorio | Aceptación mínima |
| --- | --- | --- |
| **M340-R01** | **Disparador en UI (Icono ✓):** Botón o acción "Registrar pago realizado" en la tarjeta de pago recurrente. | Composable probado con acción de confirmación de pago. |
| **M340-R02** | **Contrato y Payload E037:** Enviar `POST /api/v1/recurring-payments/{recurringPaymentId}/payments` con cabecera `Authorization: Bearer <accessToken>`, `Idempotency-Key` y payload `RecordPaymentRequest(occurredAt, amount?)`. Recibir `201 Created` con `movementId`, `recurringPaymentId` y `updatedSummary`. | DTOs modelados y prueba de llamada HTTP exitosa. |
| **M340-R03** | **Deduplicación por Ocurrencia:** Uso estricto de `Idempotency-Key` (UUIDv4 por ocurrencia/fecha) para evitar cobros duplicados ante reintentos de red. | Prueba de idempotencia en repositorio y mock. |
| **M340-R04** | **Actualización de Presupuesto sin Duplicar:** El sistema registra el movimiento real vinculándolo a la regla, descontando el disponible una sola vez. | Pruebas de integración del resumen financiero. |
| **M340-R05** | **Simulación en Mock y Pruebas Unitarias:** La fuente simulada debe generar el movimiento, asociarlo al pago y retornar `201 Created` con test unitario asociado. | Pruebas unitarias aprobadas con `kotlinx-coroutines-test`. |
