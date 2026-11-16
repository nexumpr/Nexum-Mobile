---
project: Nexum-Mobile
unit: 330-Pagos-Recurrentes
kind: technical-spec
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# SPEC — Gestión de Pagos Recurrentes (Recurring Payments CRUD)

## Propósito
Permitir a un usuario titular autenticado listar, crear, consultar, editar y desactivar/eliminar reglas de pagos periódicos bajo los contratos **NEXUM-E032** a **NEXUM-E036** (`GET`, `POST`, `GET /{id}`, `PATCH /{id}`, `DELETE /{id}` `/api/v1/recurring-payments`, catálogo V6.0.2). Se diferencia estrictamente el compromiso periódico (regla) del pago efectivamente realizado, calculando la equivalencia mensual (`fixedMonthlyEquivalent`) y considerando como obligación estimada únicamente los pagos de tipo `fixed`.

## Lugar en el plan SDD
- **Constitución:** [`.specify/memory/constitution.md`](../../.specify/memory/constitution.md) (vigente: MO-PR04 Backend autoritativo, MO-PR06 Privacidad y seguridad, MO-PR07 Usabilidad inclusiva Material 3, MO-PR08 Resiliencia offline, MO-PR15 Custodia segura).
- **Tecnología:** Kotlin / Jetpack Compose / MVI.
- **Contratos:** NEXUM-E032 (`GET`), NEXUM-E033 (`POST`), NEXUM-E034 (`GET /{id}`), NEXUM-E035 (`PATCH /{id}`), NEXUM-E036 (`DELETE /{id}`).
- **Dependencias previas:** [`140-Login`](../140-Login/spec.md), [`150-Refresh-Token`](../150-Refresh-Token/spec.md), [`030-di-navegacion-ui`](../030-di-navegacion-ui/spec.md), [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md).

## Requisitos y criterios verificables
| ID | Requisito obligatorio | Aceptación mínima |
| --- | --- | --- |
| **M330-R01** | **Listado y Equivalencia Mensual (E032):** `GET /api/v1/recurring-payments` devuelve items activos y `fixedMonthlyEquivalent`. Distinción entre `fixed` y `variable`. | DTOs modelados y prueba de listado exitosa. |
| **M330-R02** | **Creación de Regla (E033):** `POST /api/v1/recurring-payments` con `concept, amount, categoryId, type, frequency` retorna `201 Created` con `recurringPaymentId` y equivalencia. | Prueba de creación de pago periódico. |
| **M330-R03** | **Consulta de Detalle (E034):** `GET /api/v1/recurring-payments/{recurringPaymentId}` para precarga en formularios de edición con aislamiento por usuario (404 seguro). | Prueba de obtención de detalle unitario. |
| **M330-R04** | **Edición de Regla (E035):** `PATCH /api/v1/recurring-payments/{recurringPaymentId}` modifica importes o frecuencias para futuras proyecciones sin reescribir historial de pagos pasados. | Prueba de patch con preservación de historia. |
| **M330-R05** | **Desactivación / Soft-Delete (E036):** `DELETE /api/v1/recurring-payments/{recurringPaymentId}` retorna `204 No Content`, aplicando soft-delete (estado inactivo) para conservar trazabilidad de movimientos históricos. | Prueba de borrado lógico/inactivo. |
| **M330-R06** | **Simulación en Mock y Pruebas Unitarias:** Cobertura de la suite de pruebas unitarias para el ciclo de vida completo de pagos recurrentes con `kotlinx-coroutines-test`. | Pruebas unitarias en verde. |
