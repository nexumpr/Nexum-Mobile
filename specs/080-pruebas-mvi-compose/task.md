---
project: Nexum-Mobile
unit: 080-pruebas-mvi-compose
kind: implementation-tasks
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# TASK — Pruebas de estado, UI e integración aislada

**Estado:** todas las tareas están pendientes. **Preparado para ejecución por fases**; iniciar solo tras una instrucción explícita que nombre `080-pruebas-mvi-compose` y revisar dependencias en `specs/README.md`.

**Dependencias:** `020-nucleo-mvi`, `030-di-navegacion-ui`. **Regla:** no implementar funcionalidades financieras ni cambios externos sin aprobación adicional.

## Actividades verificables
- [ ] **N080-T01 — Crear/actualizar pruebas mínimas de estado y errores.**
  - **Requisito:** `M080-R01` · **Plan:** `N080-P01`.
  - **Aceptación:** cambio o decisión técnica comprobable en el alcance; no se aceptan afirmaciones sin revisión.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew connectedDebugAndroidTest` solo si hay dispositivo/emulador.
  - **Evidencia a registrar:** rutas cambiadas, comandos, salida real, fecha y limitaciones.
- [ ] **N080-T02 — Añadir pruebas Compose/instrumentadas si el entorno las admite.**
  - **Requisito:** `M080-R02` · **Plan:** `N080-P02`.
  - **Aceptación:** cambio o decisión técnica comprobable en el alcance; no se aceptan afirmaciones sin revisión.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew connectedDebugAndroidTest` solo si hay dispositivo/emulador.
  - **Evidencia a registrar:** rutas cambiadas, comandos, salida real, fecha y limitaciones.
- [ ] **N080-T03 — Ejecutar tests y reportar cuáles son NO EJECUTADOS o fallidos.**
  - **Requisito:** `M080-R03` · **Plan:** `N080-P03`.
  - **Aceptación:** cambio o decisión técnica comprobable en el alcance; no se aceptan afirmaciones sin revisión.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew connectedDebugAndroidTest` solo si hay dispositivo/emulador.
  - **Evidencia a registrar:** rutas cambiadas, comandos, salida real, fecha y limitaciones.

## Cierre y evidencia
- Reportar pruebas ejecutadas (**resultado real**) y no ejecutadas con causa. Para esta unidad: `./gradlew testDebugUnitTest`; `./gradlew connectedDebugAndroidTest` solo si hay dispositivo/emulador.
- Marcar `[x]` solo cuando se verifique la tarea correspondiente; conservar pendientes si no existe evidencia.
- Actualizar `memoria.md` con comandos, archivos, decisiones y riesgos; no avanzar a otra spec automáticamente.
- `git push`, merge, despliegue y operaciones destructivas siguen requiriendo autorización separada.
