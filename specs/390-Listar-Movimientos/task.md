---
project: Nexum-Mobile
unit: 390-Listar-Movimientos
kind: implementation-tasks
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# TASK — Listado de Movimientos (List Movements)

**Estado:** Fase A documental completada; todas las actividades pendientes de autorización expresa para ejecución (`Fase B`). Ver **Cierre y evidencia** al final. Revisar dependencias en `specs/README.md` antes de cualquier implementación.

**Dependencias:** [`340-Catalogo-Categorias`](../340-Catalogo-Categorias/spec.md), [`380-Resumen-Financiero`](../380-Resumen-Financiero/spec.md), [`140-Login`](../140-Login/spec.md), [`150-Refresh-Token`](../150-Refresh-Token/spec.md), [`030-di-navegacion-ui`](../030-di-navegacion-ui/spec.md), [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md). **Regla:** no implementar código hasta recibir mandato explícito «implementa specs/390-Listar-Movimientos».

## Actividades verificables
- [ ] **N390-T01 — Crear los modelos del contrato NEXUM-E027 (`MovementFilter`, `MovementItemResponse`, `MovementsPageResponse`, `MovementItem`, `MovementsPage`).**
  - **Requisito:** `M390-R02` · **Plan:** `N390-P01`.
  - **Aceptación:** Modelos inmutables; importes como `String`; `occurredAt` y `origin` conservados; `pagination` y `totals` como contenedores provisionales documentados.
  - **Pruebas:** Pruebas unitarias de serialización de filtros y deserialización de página en `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** archivos creados, comandos, salida real, fecha y limitaciones.

- [ ] **N390-T02 — Ampliar `AuthApi` y `AuthRepository` con la consulta paginada y sus filtros.**
  - **Requisito:** `M390-R02`, `M390-R04` · **Plan:** `N390-P02`.
  - **Aceptación:** `suspend fun getMovements(accessToken, filter, from, to, page, limit, sort)` en `AuthApi` y `suspend fun getMovements(filter, from, to, page, limit): AuthResult<MovementsPage>` en `AuthRepository`; sin `userId`; error estándar con `traceId`.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** diff de interfaces, salida real de compilación y limitaciones.

- [ ] **N390-T03 — Implementar simulación en `MockAuthApi`.**
  - **Requisito:** `M390-R05` · **Plan:** `N390-P03`.
  - **Aceptación:** Retardo de 250–600 ms; historial sintético con `ant`, `fixed` y `saving`; paginación por `page`/`limit`; filtro por tipo y rango; montos decimales y `MXN`; fechas RFC 3339 UTC; centinela `net@error.test`.
  - **Pruebas:** Pruebas unitarias en `MockAuthApiTest`.
  - **Evidencia a registrar:** casos de prueba ejecutados, salida real, fecha y limitaciones.

- [ ] **N390-T04 — Construir `MovementsListViewModel` MVI con paginación y cancelación por filtro.**
  - **Requisito:** `M390-R01`, `M390-R03`, `M390-R04` · **Plan:** `N390-P04`.
  - **Aceptación:** Reducer con `SelectFilter`, `SetDateRange`, `LoadFirstPage`, `LoadNextPage`, `Retry` y `DismissError`; sin páginas duplicadas; fecha y origen intactos; estado vacío, error y sin conexión sin fingir contenido.
  - **Pruebas:** Pruebas unitarias en `MovementsListViewModelTest` con `kotlinx-coroutines-test`.
  - **Evidencia a registrar:** pruebas de ViewModel ejecutadas, salida real, fecha y limitaciones.

- [ ] **N390-T05 — Integrar `MovementsScreen` con pestañas y filtros en Compose con Material 3.**
  - **Requisito:** `M390-R01`, `M390-R03` · **Plan:** `N390-P05`.
  - **Aceptación:** Pestañas Todos, Hormiga, Obligatorios y Ahorro; filtro de rango; celdas con concepto, importe, categoría, fecha real y origen; carga inicial, carga adicional, vacío y error accesibles.
  - **Pruebas:** Revisión de componentes Compose; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** archivos de UI modificados, salida real y limitaciones.

- [ ] **N390-T06 — Añadir suite completa de pruebas unitarias del listado de movimientos.**
  - **Requisito:** `M390-R01`…`M390-R06` · **Plan:** `N390-P06`.
  - **Aceptación:** Cobertura de carga inicial, página siguiente, filtros por tipo y rango, vacío, `400`/`401`/`403`/`429`/`500`, sin conexión y conservación de `occurredAt`/`origin`.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew lintDebug`.
  - **Evidencia a registrar:** reporte de pruebas unitarias, conteo total de tests, salida real, fecha y limitaciones.

## Cierre y evidencia
- **Fase A (documental):** especificación, plan técnico y actividades creadas. **Ninguna tarea implementada ni ejecutada en esta fase.**
- **Fase B (pendiente de mandato explícito):** requerirá orden humana expresa para modificar archivos `.kt`, ejecutar pruebas Gradle y registrar evidencia en `memoria.md`.
- No alterar código de producción, no realizar push ni despliegues en esta fase.
