---
project: Nexum-Mobile
unit: 030-di-navegacion-ui
kind: implementation-tasks
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# TASK — Inyección de dependencias y shell UI

**Estado:** todas las tareas están pendientes. **Preparado para ejecución por fases**; iniciar solo tras una instrucción explícita que nombre `030-di-navegacion-ui` y revisar dependencias en `specs/README.md`.

**Dependencias:** `020-nucleo-mvi`. **Regla:** no implementar funcionalidades financieras ni cambios externos sin aprobación adicional.

## Actividades verificables
- [ ] **N030-T01 — Definir o seleccionar DI con justificación.**
  - **Requisito:** `M030-R01` · **Plan:** `N030-P01`.
  - **Aceptación:** cambio o decisión técnica comprobable en el alcance; no se aceptan afirmaciones sin revisión.
  - **Pruebas:** `./gradlew testDebugUnitTest`; pruebas Compose/instrumentadas en emulador si existe.
  - **Evidencia a registrar:** rutas cambiadas, comandos, salida real, fecha y limitaciones.
- [ ] **N030-T02 — Implementar shell técnico y navegación sin pantallas de negocio.**
  - **Requisito:** `M030-R02` · **Plan:** `N030-P02`.
  - **Aceptación:** cambio o decisión técnica comprobable en el alcance; no se aceptan afirmaciones sin revisión.
  - **Pruebas:** `./gradlew testDebugUnitTest`; pruebas Compose/instrumentadas en emulador si existe.
  - **Evidencia a registrar:** rutas cambiadas, comandos, salida real, fecha y limitaciones.
- [ ] **N030-T03 — Probar inyección de dobles y semántica de accesibilidad.**
  - **Requisito:** `M030-R03` · **Plan:** `N030-P03`.
  - **Aceptación:** cambio o decisión técnica comprobable en el alcance; no se aceptan afirmaciones sin revisión.
  - **Pruebas:** `./gradlew testDebugUnitTest`; pruebas Compose/instrumentadas en emulador si existe.
  - **Evidencia a registrar:** rutas cambiadas, comandos, salida real, fecha y limitaciones.

## Cierre y evidencia
- Reportar pruebas ejecutadas (**resultado real**) y no ejecutadas con causa. Para esta unidad: `./gradlew testDebugUnitTest`; pruebas Compose/instrumentadas en emulador si existe.
- Marcar `[x]` solo cuando se verifique la tarea correspondiente; conservar pendientes si no existe evidencia.
- Actualizar `memoria.md` con comandos, archivos, decisiones y riesgos; no avanzar a otra spec automáticamente.
- `git push`, merge, despliegue y operaciones destructivas siguen requiriendo autorización separada.
