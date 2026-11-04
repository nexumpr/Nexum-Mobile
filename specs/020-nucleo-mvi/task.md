---
project: Nexum-Mobile
unit: 020-nucleo-mvi
kind: implementation-tasks
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# TASK — Núcleo MVI y estado inmutable

**Estado:** todas las tareas están pendientes. **Preparado para ejecución por fases**; iniciar solo tras una instrucción explícita que nombre `020-nucleo-mvi` y revisar dependencias en `specs/README.md`.

**Dependencias:** `010-base-android`. **Regla:** no implementar funcionalidades financieras ni cambios externos sin aprobación adicional.

## Actividades verificables
- [ ] **N020-T01 — Documentar y construir la abstracción mínima de estado/intención sin flujos financieros.**
  - **Requisito:** `M020-R01` · **Plan:** `N020-P01`.
  - **Aceptación:** cambio o decisión técnica comprobable en el alcance; no se aceptan afirmaciones sin revisión.
  - **Pruebas:** `./gradlew testDebugUnitTest`; pruebas MVI con estados sintéticos.
  - **Evidencia a registrar:** rutas cambiadas, comandos, salida real, fecha y limitaciones.
- [ ] **N020-T02 — Preparar reductores y prueba de transición sintética.**
  - **Requisito:** `M020-R02` · **Plan:** `N020-P02`.
  - **Aceptación:** cambio o decisión técnica comprobable en el alcance; no se aceptan afirmaciones sin revisión.
  - **Pruebas:** `./gradlew testDebugUnitTest`; pruebas MVI con estados sintéticos.
  - **Evidencia a registrar:** rutas cambiadas, comandos, salida real, fecha y limitaciones.
- [ ] **N020-T03 — Ejecutar pruebas de estado, recomposición y manejo de efectos.**
  - **Requisito:** `M020-R03` · **Plan:** `N020-P03`.
  - **Aceptación:** cambio o decisión técnica comprobable en el alcance; no se aceptan afirmaciones sin revisión.
  - **Pruebas:** `./gradlew testDebugUnitTest`; pruebas MVI con estados sintéticos.
  - **Evidencia a registrar:** rutas cambiadas, comandos, salida real, fecha y limitaciones.

## Cierre y evidencia
- Reportar pruebas ejecutadas (**resultado real**) y no ejecutadas con causa. Para esta unidad: `./gradlew testDebugUnitTest`; pruebas MVI con estados sintéticos.
- Marcar `[x]` solo cuando se verifique la tarea correspondiente; conservar pendientes si no existe evidencia.
- Actualizar `memoria.md` con comandos, archivos, decisiones y riesgos; no avanzar a otra spec automáticamente.
- `git push`, merge, despliegue y operaciones destructivas siguen requiriendo autorización separada.
