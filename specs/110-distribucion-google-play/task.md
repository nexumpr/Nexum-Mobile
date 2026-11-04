---
project: Nexum-Mobile
unit: 110-distribucion-google-play
kind: implementation-tasks
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# TASK — Preparación para distribución segura

**Estado:** todas las tareas están pendientes. **Preparado para ejecución por fases**; iniciar solo tras una instrucción explícita que nombre `110-distribucion-google-play` y revisar dependencias en `specs/README.md`.

**Dependencias:** `100-ci-versiones-android`. **Regla:** no implementar funcionalidades financieras ni cambios externos sin aprobación adicional.

## Actividades verificables
- [ ] **N110-T01 — Documentar matriz de versión y firma segura, no crear claves sin autorización.**
  - **Requisito:** `M110-R01` · **Plan:** `N110-P01`.
  - **Aceptación:** cambio o decisión técnica comprobable en el alcance; no se aceptan afirmaciones sin revisión.
  - **Pruebas:** revisión humana de firma y consentimiento; build release solo si autorizado.
  - **Evidencia a registrar:** rutas cambiadas, comandos, salida real, fecha y limitaciones.
- [ ] **N110-T02 — Preparar checklist de revisión, ficha de tienda y avisos futuros.**
  - **Requisito:** `M110-R02` · **Plan:** `N110-P02`.
  - **Aceptación:** cambio o decisión técnica comprobable en el alcance; no se aceptan afirmaciones sin revisión.
  - **Pruebas:** revisión humana de firma y consentimiento; build release solo si autorizado.
  - **Evidencia a registrar:** rutas cambiadas, comandos, salida real, fecha y limitaciones.
- [ ] **N110-T03 — Validar documentación; no ejecutar publicación.**
  - **Requisito:** `M110-R03` · **Plan:** `N110-P03`.
  - **Aceptación:** cambio o decisión técnica comprobable en el alcance; no se aceptan afirmaciones sin revisión.
  - **Pruebas:** revisión humana de firma y consentimiento; build release solo si autorizado.
  - **Evidencia a registrar:** rutas cambiadas, comandos, salida real, fecha y limitaciones.

## Cierre y evidencia
- Reportar pruebas ejecutadas (**resultado real**) y no ejecutadas con causa. Para esta unidad: revisión humana de firma y consentimiento; build release solo si autorizado.
- Marcar `[x]` solo cuando se verifique la tarea correspondiente; conservar pendientes si no existe evidencia.
- Actualizar `memoria.md` con comandos, archivos, decisiones y riesgos; no avanzar a otra spec automáticamente.
- `git push`, merge, despliegue y operaciones destructivas siguen requiriendo autorización separada.
