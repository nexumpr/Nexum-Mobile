---
project: Nexum-Mobile
unit: 400-Registrar-Movimiento
kind: implementation-tasks
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# TASK — Registro de Movimiento (Gasto Realizado) (Create Movement)

**Estado:** Fase A documental completada; todas las actividades pendientes de autorización expresa para ejecución (`Fase B`). Ver **Cierre y evidencia** al final. Revisar dependencias en `specs/README.md` antes de cualquier implementación.

**Dependencias:** [`390-Listar-Movimientos`](../390-Listar-Movimientos/spec.md), [`340-Catalogo-Categorias`](../340-Catalogo-Categorias/spec.md), [`380-Resumen-Financiero`](../380-Resumen-Financiero/spec.md), [`140-Login`](../140-Login/spec.md), [`150-Refresh-Token`](../150-Refresh-Token/spec.md), [`030-di-navegacion-ui`](../030-di-navegacion-ui/spec.md), [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md). **Regla:** no implementar código hasta recibir mandato explícito «implementa specs/400-Registrar-Movimiento».

## Actividades verificables
- [ ] **N400-T01 — Crear los modelos del contrato NEXUM-E028 (`CreateMovementRequest`, `CreateMovementResponse`, `MovementDraft`, `RegisteredMovement`) y `MovementInputValidator`.**
  - **Requisito:** `M400-R02` · **Plan:** `N400-P01`.
  - **Aceptación:** Modelos inmutables; importes como `String`; validador con `BigDecimal` que exige importe mayor que cero, dos decimales y tipo `ant`/`fixed`; sin `Float`/`Double`.
  - **Pruebas:** Pruebas unitarias de serialización y validación en `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** archivos creados, comandos, salida real, fecha y limitaciones.

- [ ] **N400-T02 — Ampliar `AuthApi` y `AuthRepository` con el alta y la `Idempotency-Key`.**
  - **Requisito:** `M400-R02`, `M400-R04` · **Plan:** `N400-P02`.
  - **Aceptación:** `suspend fun createMovement(accessToken, request, idempotencyKey): CreateMovementResponse` en `AuthApi` y `suspend fun createMovement(draft, idempotencyKey): AuthResult<RegisteredMovement>` en `AuthRepository`; sin `userId`; `409` y error estándar con `traceId`.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** diff de interfaces, salida real de compilación y limitaciones.

- [ ] **N400-T03 — Implementar simulación en `MockAuthApi`.**
  - **Requisito:** `M400-R05` · **Plan:** `N400-P03`.
  - **Aceptación:** Retardo de 300–700 ms; validación de importe y tipo; `201` con `movementId`, `record` y `updatedSummary`; deduplicación por `Idempotency-Key`; centinelas `net@error.test` y `rule_error_movement`.
  - **Pruebas:** Pruebas unitarias en `MockAuthApiTest`.
  - **Evidencia a registrar:** casos de prueba ejecutados, salida real, fecha y limitaciones.

- [ ] **N400-T04 — Construir `RegisterMovementViewModel` MVI con validación, voz y conservación del borrador.**
  - **Requisito:** `M400-R01`, `M400-R03`, `M400-R04` · **Plan:** `N400-P04`.
  - **Aceptación:** Reducer con cambios de campo, `ApplyVoiceDraft`, `Save`, `Retry`, `DismissError` y `ConsumeRegistered`; doble toque controlado; llave estable durante reintentos; error no finge éxito.
  - **Pruebas:** Pruebas unitarias en `RegisterMovementViewModelTest` con `kotlinx-coroutines-test`.
  - **Evidencia a registrar:** pruebas de ViewModel ejecutadas, salida real, fecha y limitaciones.

- [ ] **N400-T05 — Integrar el formulario "Agregar gasto" y la confirmación por voz en Compose con Material 3.**
  - **Requisito:** `M400-R01`, `M400-R03` · **Plan:** `N400-P05`.
  - **Aceptación:** Concepto, importe con teclado decimal, selector de categoría, tipo Hormiga/Obligatorio, fecha del evento, errores por campo, botón con progreso y confirmación; la ruta de voz precarga la propuesta y no crea recurrencia.
  - **Pruebas:** Revisión de componentes Compose; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** archivos de UI modificados, salida real y limitaciones.

- [ ] **N400-T06 — Añadir suite completa de pruebas unitarias de registro de movimiento.**
  - **Requisito:** `M400-R01`…`M400-R06` · **Plan:** `N400-P06`.
  - **Aceptación:** Cobertura de alta manual, propuesta de voz, `"0.00"`/`"-5.00"`/`"10.5"`, doble toque, idempotencia en reintento, `400`/`401`/`403`/`404`/`409`/`422`/`429`/`500` y sin conexión.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew lintDebug`.
  - **Evidencia a registrar:** reporte de pruebas unitarias, conteo total de tests, salida real, fecha y limitaciones.

## Cierre y evidencia
- **Fase A (documental):** especificación, plan técnico y actividades creadas. **Ninguna tarea implementada ni ejecutada en esta fase.**
- **Fase B (pendiente de mandato explícito):** requerirá orden humana expresa para modificar archivos `.kt`, ejecutar pruebas Gradle y registrar evidencia en `memoria.md`.
- No alterar código de producción, no realizar push ni despliegues en esta fase.
