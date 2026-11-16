---
project: Nexum-Mobile
unit: 320-Eliminar-Movimiento
kind: technical-spec
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# SPEC — Eliminación y Reversión de Movimiento (Delete Movement)

## Propósito
Permitir a un usuario titular autenticado borrar o revertir un movimiento financiero registrado en el sistema bajo el contrato **NEXUM-E031** (`DELETE /api/v1/movements/{movementId}` [M], catálogo V6.0.2). Si el movimiento representa una aportación a una meta de ahorro (`goal_id != null`), el backend y la UI deben garantizar una transacción atómica que reverta exactamente dicha asignación en la meta (`goal.saved = goal.saved - amount`), sin alterar otras aportaciones del mismo grupo.

## Lugar en el plan SDD
- **Constitución:** [`.specify/memory/constitution.md`](../../.specify/memory/constitution.md) (vigente: MO-PR04 Backend como fuente autoritativa, MO-PR06 Privacidad y seguridad desde el diseño, MO-PR08 Manejo explícito de conectividad y resiliencia offline, MO-PR15 Custodia segura, MO-PR17 Transacciones atómicas).
- **Tecnología:** Kotlin / Jetpack Compose / MVI (Android nativo).
- **Contrato:** NEXUM-E031 del catálogo de endpoints de referencia V6.0.2 (`DELETE /api/v1/movements/{movementId}` [M]).
- **Dependencias previas:** [`140-Login`](../140-Login/spec.md), [`150-Refresh-Token`](../150-Refresh-Token/spec.md), [`030-di-navegacion-ui`](../030-di-navegacion-ui/spec.md), [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md), [`050-sesion-credenciales`](../050-sesion-credenciales/spec.md).
- **Naturaleza:** especificación técnica de ciclo de vida de transacciones y soporte (`[M]`).

## Alcance permitido
Definición de DTOs y métodos en `MovementsApi`/`MovementsRepository`, simulación en `MockMovementsApi`, diálogo de confirmación accesible Material 3 en `MovementsScreen` ("Movimientos > ×"), manejo de errores HTTP (404, 409, 412), y pruebas unitarias con `kotlinx-coroutines-test`.

## Exclusiones explícitas
No modificar otras aportaciones del mismo grupo de ahorro; no alterar saldos globales sin reflejar la reversión de la meta; no conectarse a un backend real en esta etapa; no realizar push ni despliegues.

## Requisitos y criterios verificables
| ID | Requisito obligatorio | Aceptación mínima |
| --- | --- | --- |
| **M320-R01** | **Disparador en UI ("Movimientos > ×"):** Componente Compose en el listado de movimientos con icono "×" y diálogo de confirmación de eliminación accesible Material 3. | Composable probado con diálogo y eventos de confirmación. |
| **M320-R02** | **Contrato y Payload E031:** Enviar `DELETE /api/v1/movements/{movementId}` con cabecera `Authorization: Bearer <accessToken>` y parámetro opcional `If-Match`. Esperar respuesta `204 No Content` sin cuerpo. | Modelado de DTOs y prueba de petición HTTP exitosa. |
| **M320-R03** | **Transacción Atómica y Reversión de Ahorro:** Si el movimiento eliminado tiene `goal_id != null`, la operación debe revertir el saldo de la meta (`goal.saved = goal.saved - movement.amount`) de forma atómica en el servidor/mock. | Pruebas unitarias que verifiquen el descuento en la meta asociada. |
| **M320-R04** | **Manejo de Errores y Concurrencia:** Manejo correcto de códigos `404 Not Found` (movimiento ya borrado), `412 Precondition Failed` (por `If-Match`) y `409 Conflict` (meta bloqueada). | Pruebas de manejo de errores HTTP en repositorio y ViewModel. |
| **M320-R05** | **Resiliencia Offline y Fail-Secure:** Marcado de borrado local o encolado de sincronización ante pérdida de conectividad, evitando incoherencias de saldo. | Pruebas de resiliencia ante fallo de red. |
| **M320-R06** | **Simulación en Mock y Pruebas Unitarias:** La fuente simulada debe borrar el movimiento en memoria, recalcular `goal.saved` si aplica, retornar `204 No Content` y contar con suite de pruebas. | Pruebas unitarias aprobadas con `kotlinx-coroutines-test`. |
