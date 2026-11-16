---
project: Nexum-Mobile
unit: 420-Editar-Movimiento
kind: implementation-tasks
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# TASK — Edición de Movimiento (Edit Movement)

**Estado:** Fase A documental completada; todas las actividades pendientes de autorización expresa para ejecución (`Fase B`). Ver **Cierre y evidencia** al final. Revisar dependencias en `specs/README.md` antes de cualquier implementación.

**Dependencias:** [`410-Detalle-Movimiento`](../410-Detalle-Movimiento/spec.md), [`400-Registrar-Movimiento`](../400-Registrar-Movimiento/spec.md), [`340-Catalogo-Categorias`](../340-Catalogo-Categorias/spec.md), [`380-Resumen-Financiero`](../380-Resumen-Financiero/spec.md), [`140-Login`](../140-Login/spec.md), [`150-Refresh-Token`](../150-Refresh-Token/spec.md), [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md). **Regla:** no implementar código hasta recibir mandato explícito «implementa specs/420-Editar-Movimiento».

## Actividades verificables
- [ ] **N420-T01 — Crear los modelos del contrato NEXUM-E030 (`UpdateMovementRequest`, `UpdateMovementResponse`, `MovementEditDraft`, `MovementEditResult`) y `MovementEditValidator`.**
  - **Requisito:** `M420-R02` · **Plan:** `N420-P01`.
  - **Aceptación:** Modelos inmutables con campos opcionales omitidos en la serialización; `summaryVersion` como cadena; validador con `BigDecimal` que rechaza `saving`, importe no positivo o con más de dos decimales.
  - **Pruebas:** Pruebas unitarias de serialización parcial y validación en `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** archivos creados, comandos, salida real, fecha y limitaciones.

- [ ] **N420-T02 — Ampliar `AuthApi` y `AuthRepository` con la edición parcial e invalidación del resumen.**
  - **Requisito:** `M420-R02`, `M420-R04` · **Plan:** `N420-P02`.
  - **Aceptación:** `suspend fun updateMovement(accessToken, movementId, request): UpdateMovementResponse` en `AuthApi` y `suspend fun updateMovement(movementId, draft): AuthResult<MovementEditResult>` en `AuthRepository`; solo campos modificados; sin `userId`; `404` neutro; `summaryVersion` conservado.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** diff de interfaces, salida real de compilación y limitaciones.

- [ ] **N420-T03 — Implementar simulación en `MockAuthApi`.**
  - **Requisito:** `M420-R05` · **Plan:** `N420-P03`.
  - **Aceptación:** Retardo de 300–700 ms; cambio parcial en memoria; `200` con `movement` y `summaryVersion`; rechazo de `saving` con `422`; `404` para identificador desconocido; centinelas `net@error.test` y `rule_error_movement`.
  - **Pruebas:** Pruebas unitarias en `MockAuthApiTest`.
  - **Evidencia a registrar:** casos de prueba ejecutados, salida real, fecha y limitaciones.

- [ ] **N420-T04 — Construir `EditMovementViewModel` MVI con precarga, validación y conservación del formulario.**
  - **Requisito:** `M420-R01`, `M420-R03`, `M420-R04` · **Plan:** `N420-P04`.
  - **Aceptación:** Reducer con `LoadOriginal`, cambios de campo, `Save`, `Retry`, `DismissError`, `ConsumeResult` y `Cancel`; `isSaving` evita doble envío; `404`/`409`/`422` conservan valores; éxito emite `ShowConfirmation` y `NavigateBack`.
  - **Pruebas:** Pruebas unitarias en `EditMovementViewModelTest` con `kotlinx-coroutines-test`.
  - **Evidencia a registrar:** pruebas de ViewModel ejecutadas, salida real, fecha y limitaciones.

- [ ] **N420-T05 — Integrar el formulario "Editar movimiento" en Compose con Material 3.**
  - **Requisito:** `M420-R01`, `M420-R03` · **Plan:** `N420-P05`.
  - **Aceptación:** Campos precargados, solo concepto/importe/categoría/tipo editables, tipo limitado a Hormiga/Obligatorio, errores por campo, botón con progreso y cancelación sin cambios.
  - **Pruebas:** Revisión de componentes Compose; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** archivos de UI modificados, salida real y limitaciones.

- [ ] **N420-T06 — Añadir suite completa de pruebas unitarias de edición de movimiento.**
  - **Requisito:** `M420-R01`…`M420-R06` · **Plan:** `N420-P06`.
  - **Aceptación:** Cobertura de precarga, campos parciales, rechazo de `saving`, importes `"0.00"`/`"-5.00"`/`"125.5"`, `400`/`401`/`403`/`404`/`409`/`422`/`429`/`500`, sin conexión y ausencia de altas.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew lintDebug`.
  - **Evidencia a registrar:** reporte de pruebas unitarias, conteo total de tests, salida real, fecha y limitaciones.

## Cierre y evidencia
- **Fase A (documental):** especificación, plan técnico y actividades creadas. **Ninguna tarea implementada ni ejecutada en esta fase.**
- **Fase B (pendiente de mandato explícito):** requerirá orden humana expresa para modificar archivos `.kt`, ejecutar pruebas Gradle y registrar evidencia en `memoria.md`.
- No alterar código de producción, no realizar push ni despliegues en esta fase.
