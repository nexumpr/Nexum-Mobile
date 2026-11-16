---
project: Nexum-Mobile
unit: 350-Metas-Ahorro
kind: technical-spec
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# SPEC — Gestión de Metas de Ahorro (Savings Goals CRUD & Details)

## Propósito
Permitir a un usuario titular autenticado consultar, crear y precargar detalles de metas de ahorro bajo los contratos **NEXUM-E038**, **NEXUM-E039** y **NEXUM-E040** (`GET /api/v1/goals`, `POST /api/v1/goals`, `GET /api/v1/goals/{goalId}`, catálogo V6.0.2). La pantalla y los modelos de datos gestionan metas con emoji, fecha opcional, objetivo y monto ahorrado, garantizando que el saldo de la meta no quede desacoplado de sus aportaciones y que el porcentaje visual máximo sea del 100% sin ocultar metas alcanzadas.

## Lugar en el plan SDD
- **Constitución:** [`.specify/memory/constitution.md`](../../.specify/memory/constitution.md) (vigente: MO-PR04 Backend autoritativo, MO-PR06 Privacidad y seguridad, MO-PR07 Usabilidad inclusiva Material 3, MO-PR08 Resiliencia offline, MO-PR15 Custodia segura).
- **Tecnología:** Kotlin / Jetpack Compose / MVI.
- **Contratos:** NEXUM-E038 (`GET`), NEXUM-E039 (`POST`), NEXUM-E040 (`GET /{goalId}`).
- **Dependencias previas:** [`140-Login`](../140-Login/spec.md), [`150-Refresh-Token`](../150-Refresh-Token/spec.md), [`030-di-navegacion-ui`](../030-di-navegacion-ui/spec.md), [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md).

## Requisitos y criterios verificables
| ID | Requisito obligatorio | Aceptación mínima |
| --- | --- | --- |
| **M350-R01** | **Listado de Metas (E038):** `GET /api/v1/goals` con parámetro opcional `status=active|completed|all` devuelve `items[]`, `totalSaved` y `activeCount`. Porcentaje visual máximo 100% sin ocultar metas alcanzadas. | DTOs modelados y prueba de listado exitosa. |
| **M350-R02** | **Creación de Meta (E039):** `POST /api/v1/goals` con `name, targetAmount, initialSaved, emoji, targetDate?` retorna `201 Created` con `goalId`, `savedAmount`, `remaining` y `progressPercent`. Validación `0 <= initialSaved <= targetAmount`. | Prueba de creación con ahorro inicial y fecha límite. |
| **M350-R03** | **Consulta de Detalle para Edición (E040):** `GET /api/v1/goals/{goalId}` con verificación de propiedad y respuesta `200 OK` para precargar editores fiables. | Prueba de obtención de detalle unitario. |
| **M350-R04** | **Consistencia de Saldo:** El saldo ahorrado en metas (`goal.saved`) se actualiza mediante aportaciones o saldo inicial declarado, manteniéndose sincronizado. | Pruebas de consistencia de saldos. |
| **M350-R05** | **Simulación en Mock y Pruebas Unitarias:** Cobertura de la suite de pruebas unitarias para metas de ahorro con `kotlinx-coroutines-test`. | Pruebas unitarias en verde. |
