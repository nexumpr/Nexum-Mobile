---
project: Nexum-Mobile
unit: 410-Detalle-Movimiento
kind: implementation-tasks
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# TASK — Detalle de Movimiento (Get Movement Detail)

**Estado:** Fase A documental completada; todas las actividades pendientes de autorización expresa para ejecución (`Fase B`). Ver **Cierre y evidencia** al final. Revisar dependencias en `specs/README.md` antes de cualquier implementación.

**Dependencias:** [`390-Listar-Movimientos`](../390-Listar-Movimientos/spec.md), [`140-Login`](../140-Login/spec.md), [`150-Refresh-Token`](../150-Refresh-Token/spec.md), [`030-di-navegacion-ui`](../030-di-navegacion-ui/spec.md), [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md). **Regla:** no implementar código hasta recibir mandato explícito «implementa specs/410-Detalle-Movimiento».

## Actividades verificables
- [ ] **N410-T01 — Crear los modelos del contrato NEXUM-E029 (`MovementDetailResponse`, `MovementDetail`).**
  - **Requisito:** `M410-R02` · **Plan:** `N410-P01`.
  - **Aceptación:** Modelos inmutables con importe como `String`; `occurredAt` y `origin` presentes; vínculos opcionales con pago recurrente y meta.
  - **Pruebas:** Pruebas unitarias de deserialización en `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** archivos creados, comandos, salida real, fecha y limitaciones.

- [ ] **N410-T02 — Ampliar `AuthApi` y `AuthRepository` con la consulta puntual y el mapeo de `404`.**
  - **Requisito:** `M410-R02`, `M410-R04` · **Plan:** `N410-P02`.
  - **Aceptación:** `suspend fun getMovementDetail(accessToken, movementId): MovementDetailResponse` en `AuthApi` y `suspend fun getMovementDetail(movementId): AuthResult<MovementDetail>` en `AuthRepository`; `404` se mapea a `NOT_FOUND` sin distinguir pertenencia; error estándar con `traceId`.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** diff de interfaces, salida real de compilación y limitaciones.

- [ ] **N410-T03 — Implementar simulación en `MockAuthApi`.**
  - **Requisito:** `M410-R05` · **Plan:** `N410-P03`.
  - **Aceptación:** Retardo de 200–500 ms; detalles sintéticos con y sin vínculo; importes decimales y `MXN`; fechas RFC 3339 UTC; `404` para identificadores desconocidos o ajenos; centinela `net@error.test`.
  - **Pruebas:** Pruebas unitarias en `MockAuthApiTest`.
  - **Evidencia a registrar:** casos de prueba ejecutados, salida real, fecha y limitaciones.

- [ ] **N410-T04 — Construir `MovementDetailViewModel` MVI con estados de no encontrado y navegación.**
  - **Requisito:** `M410-R01`, `M410-R03`, `M410-R04` · **Plan:** `N410-P04`.
  - **Aceptación:** Reducer con `Load`, `Retry`, `DismissError`, `Edit` y `Back`; `404` neutro; sin detalle reconstruido desde la lista; efectos de navegación correctos.
  - **Pruebas:** Pruebas unitarias en `MovementDetailViewModelTest` con `kotlinx-coroutines-test`.
  - **Evidencia a registrar:** pruebas de ViewModel ejecutadas, salida real, fecha y limitaciones.

- [ ] **N410-T05 — Integrar `MovementDetailScreen` en Compose con Material 3.**
  - **Requisito:** `M410-R01`, `M410-R03` · **Plan:** `N410-P05`.
  - **Aceptación:** Concepto, importe, categoría, tipo, fecha real, origen y vínculos cuando existan; estados de carga, no encontrado y error; acciones Editar y Volver accesibles.
  - **Pruebas:** Revisión de componentes Compose; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** archivos de UI modificados, salida real y limitaciones.

- [ ] **N410-T06 — Añadir suite completa de pruebas unitarias del detalle de movimiento.**
  - **Requisito:** `M410-R01`…`M410-R06` · **Plan:** `N410-P06`.
  - **Aceptación:** Cobertura de detalle con y sin vínculo, `404` por identificador ajeno, `400`/`401`/`403`/`429`/`500`, sin conexión y navegación al editor.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew lintDebug`.
  - **Evidencia a registrar:** reporte de pruebas unitarias, conteo total de tests, salida real, fecha y limitaciones.

## Cierre y evidencia
- **Fase A (documental):** especificación, plan técnico y actividades creadas. **Ninguna tarea implementada ni ejecutada en esta fase.**
- **Fase B (pendiente de mandato explícito):** requerirá orden humana expresa para modificar archivos `.kt`, ejecutar pruebas Gradle y registrar evidencia en `memoria.md`.
- No alterar código de producción, no realizar push ni despliegues en esta fase.
