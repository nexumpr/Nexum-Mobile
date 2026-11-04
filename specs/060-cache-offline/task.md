---
project: Nexum-Mobile
unit: 060-cache-offline
kind: implementation-tasks
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# TASK — Caché local y sincronización controlada

**Estado:** todas las tareas están pendientes. **Preparado para ejecución por fases**; iniciar solo tras una instrucción explícita que nombre `060-cache-offline` y revisar dependencias en `specs/README.md`.

**Dependencias:** `040-cliente-http-contratos`, `050-sesion-credenciales`. **Regla:** no implementar funcionalidades financieras ni cambios externos sin aprobación adicional.

## Actividades verificables
- [ ] **N060-T01 — Documentar alcance de caché/offline con estados sintéticos.**
  - **Requisito:** `M060-R01` · **Plan:** `N060-P01`.
  - **Aceptación:** cambio o decisión técnica comprobable en el alcance; no se aceptan afirmaciones sin revisión.
  - **Pruebas:** `./gradlew testDebugUnitTest`; pruebas aislamiento, expiración y estado offline.
  - **Evidencia a registrar:** rutas cambiadas, comandos, salida real, fecha y limitaciones.
- [ ] **N060-T02 — Implementar interfaces y storage solo tras aprobar opción.**
  - **Requisito:** `M060-R02` · **Plan:** `N060-P02`.
  - **Aceptación:** cambio o decisión técnica comprobable en el alcance; no se aceptan afirmaciones sin revisión.
  - **Pruebas:** `./gradlew testDebugUnitTest`; pruebas aislamiento, expiración y estado offline.
  - **Evidencia a registrar:** rutas cambiadas, comandos, salida real, fecha y limitaciones.
- [ ] **N060-T03 — Probar aislamiento por usuario, caducidad y reintentos sin duplicados.**
  - **Requisito:** `M060-R03` · **Plan:** `N060-P03`.
  - **Aceptación:** cambio o decisión técnica comprobable en el alcance; no se aceptan afirmaciones sin revisión.
  - **Pruebas:** `./gradlew testDebugUnitTest`; pruebas aislamiento, expiración y estado offline.
  - **Evidencia a registrar:** rutas cambiadas, comandos, salida real, fecha y limitaciones.

## Cierre y evidencia
- Reportar pruebas ejecutadas (**resultado real**) y no ejecutadas con causa. Para esta unidad: `./gradlew testDebugUnitTest`; pruebas aislamiento, expiración y estado offline.
- Marcar `[x]` solo cuando se verifique la tarea correspondiente; conservar pendientes si no existe evidencia.
- Actualizar `memoria.md` con comandos, archivos, decisiones y riesgos; no avanzar a otra spec automáticamente.
- `git push`, merge, despliegue y operaciones destructivas siguen requiriendo autorización separada.
