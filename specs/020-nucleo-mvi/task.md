---
project: Nexum-Mobile
unit: 020-nucleo-mvi
kind: implementation-tasks
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# TASK — Núcleo MVI y estado inmutable

**Estado:** tareas verificadas e implementadas.

**Dependencias:** `010-base-android`. **Regla:** no implementar funcionalidades financieras ni cambios externos sin aprobación adicional.

## Actividades verificables
- [x] **N020-T01 — Documentar y construir la abstracción mínima de estado/intención sin flujos financieros.**
  - **Requisito:** `M020-R01` · **Plan:** `N020-P01`.
  - **Aceptación:** Construidas interfaces `UiState`, `UiIntent`, `UiEffect` y reductor funcional `Reducer<S, I>` desacopladas de finanzas y de vistas concretas en `mx.nexum.app.core.mvi`.
  - **Pruebas:** `./gradlew testDebugUnitTest`; pruebas MVI con estados sintéticos (`MviCoreTest`).
  - **Evidencia a registrar:** `app/src/main/java/mx/nexum/app/core/mvi/MviContracts.kt`, `app/src/main/java/mx/nexum/app/core/mvi/MviViewModel.kt`.
- [x] **N020-T02 — Preparar reductores y prueba de transición sintética.**
  - **Requisito:** `M020-R02` · **Plan:** `N020-P02`.
  - **Aceptación:** Reductor puro determinista probado con `TestReducer` demostrando que el estado original no muta (`assertNotSame`, inmutabilidad estricta) y transiciona predeciblemente ante intenciones individuales y secuenciales acumuladas.
  - **Pruebas:** `.\gradlew.bat testDebugUnitTest` (`MviCoreTest.stateIsImmutableAndPureReducerReturnsNewInstance`, `unidirectionalDataFlowTransitionsStateDeterministically`, `sequentialIntentsProduceDeterministicAccumulatedState`).
  - **Evidencia a registrar:** `app/src/test/java/mx/nexum/app/core/mvi/MviCoreTest.kt`.
- [x] **N020-T03 — Ejecutar pruebas de estado, recomposición y manejo de efectos.**
  - **Requisito:** `M020-R03` · **Plan:** `N020-P03`.
  - **Aceptación:** Pruebas de emisión de efectos efímeros vía `SharedFlow` (`replay = 0`) verificando que no mutan el estado observable y que no son recibidos por colectores tardíos (resistencia ante recomposiciones o giros de pantalla).
  - **Pruebas:** `.\gradlew.bat testDebugUnitTest` (`MviCoreTest.effectsAreEmittedViaSharedFlowAndDoNotMutateState`, `effectsAreOneShotAndDoNotReplayToLateCollectors`). Suite total: 57 tests, 0 fallos. `assembleDebug` y `lintDebug` BUILD SUCCESSFUL.
  - **Evidencia a registrar:** `app/build/reports/tests/testDebugUnitTest/index.html`, `memoria.md` §36.

## Cierre y evidencia
- Reportar pruebas ejecutadas (**resultado real**) y no ejecutadas con causa. Para esta unidad: `./gradlew testDebugUnitTest`; pruebas MVI con estados sintéticos.
- Marcar `[x]` solo cuando se verifique la tarea correspondiente; conservar pendientes si no existe evidencia.
- Actualizar `memoria.md` con comandos, archivos, decisiones y riesgos; no avanzar a otra spec automáticamente.
- `git push`, merge, despliegue y operaciones destructivas siguen requiriendo autorización separada.
