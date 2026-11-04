---
project: Nexum-Mobile
unit: 070-permisos-privacidad-accesibilidad
kind: implementation-tasks
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# TASK — Permisos, privacidad y accesibilidad base

**Estado:** todas las tareas están pendientes. **Preparado para ejecución por fases**; iniciar solo tras una instrucción explícita que nombre `070-permisos-privacidad-accesibilidad` y revisar dependencias en `specs/README.md`.

**Dependencias:** `030-di-navegacion-ui`. **Regla:** no implementar funcionalidades financieras ni cambios externos sin aprobación adicional.

## Actividades verificables
- [ ] **N070-T01 — Documentar reglas de permisos por función futura sin activar permisos nuevos.**
  - **Requisito:** `M070-R01` · **Plan:** `N070-P01`.
  - **Aceptación:** cambio o decisión técnica comprobable en el alcance; no se aceptan afirmaciones sin revisión.
  - **Pruebas:** `./gradlew lintDebug`; pruebas Compose en dispositivo/emulador autorizado.
  - **Evidencia a registrar:** rutas cambiadas, comandos, salida real, fecha y limitaciones.
- [ ] **N070-T02 — Preparar componentes de permiso/estado genéricos si son necesarios.**
  - **Requisito:** `M070-R02` · **Plan:** `N070-P02`.
  - **Aceptación:** cambio o decisión técnica comprobable en el alcance; no se aceptan afirmaciones sin revisión.
  - **Pruebas:** `./gradlew lintDebug`; pruebas Compose en dispositivo/emulador autorizado.
  - **Evidencia a registrar:** rutas cambiadas, comandos, salida real, fecha y limitaciones.
- [ ] **N070-T03 — Probar accesibilidad y bloqueo tráfico claro en configuración de producción.**
  - **Requisito:** `M070-R03` · **Plan:** `N070-P03`.
  - **Aceptación:** cambio o decisión técnica comprobable en el alcance; no se aceptan afirmaciones sin revisión.
  - **Pruebas:** `./gradlew lintDebug`; pruebas Compose en dispositivo/emulador autorizado.
  - **Evidencia a registrar:** rutas cambiadas, comandos, salida real, fecha y limitaciones.

## Cierre y evidencia
- Reportar pruebas ejecutadas (**resultado real**) y no ejecutadas con causa. Para esta unidad: `./gradlew lintDebug`; pruebas Compose en dispositivo/emulador autorizado.
- Marcar `[x]` solo cuando se verifique la tarea correspondiente; conservar pendientes si no existe evidencia.
- Actualizar `memoria.md` con comandos, archivos, decisiones y riesgos; no avanzar a otra spec automáticamente.
- `git push`, merge, despliegue y operaciones destructivas siguen requiriendo autorización separada.
