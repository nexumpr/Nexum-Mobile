---
project: Nexum-Mobile
unit: 090-devsecops-android
kind: implementation-tasks
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# TASK — Seguridad estática y calidad Android

**Estado:** todas las tareas están pendientes. **Preparado para ejecución por fases**; iniciar solo tras una instrucción explícita que nombre `090-devsecops-android` y revisar dependencias en `specs/README.md`.

**Dependencias:** `010-base-android`. **Regla:** no implementar funcionalidades financieras ni cambios externos sin aprobación adicional.

## Actividades verificables
- [ ] **N090-T01 — Configurar controles locales aprobados sin aumentar privilegios.**
  - **Requisito:** `M090-R01` · **Plan:** `N090-P01`.
  - **Aceptación:** cambio o decisión técnica comprobable en el alcance; no se aceptan afirmaciones sin revisión.
  - **Pruebas:** `./gradlew lintDebug`; `./gradlew testDebugUnitTest`; Gitleaks si está disponible.
  - **Evidencia a registrar:** rutas cambiadas, comandos, salida real, fecha y limitaciones.
- [ ] **N090-T02 — Ejecutar los controles realmente disponibles.**
  - **Requisito:** `M090-R02` · **Plan:** `N090-P02`.
  - **Aceptación:** cambio o decisión técnica comprobable en el alcance; no se aceptan afirmaciones sin revisión.
  - **Pruebas:** `./gradlew lintDebug`; `./gradlew testDebugUnitTest`; Gitleaks si está disponible.
  - **Evidencia a registrar:** rutas cambiadas, comandos, salida real, fecha y limitaciones.
- [ ] **N090-T03 — Registrar hallazgos, false positives y resultados en memoria.**
  - **Requisito:** `M090-R03` · **Plan:** `N090-P03`.
  - **Aceptación:** cambio o decisión técnica comprobable en el alcance; no se aceptan afirmaciones sin revisión.
  - **Pruebas:** `./gradlew lintDebug`; `./gradlew testDebugUnitTest`; Gitleaks si está disponible.
  - **Evidencia a registrar:** rutas cambiadas, comandos, salida real, fecha y limitaciones.

## Cierre y evidencia
- Reportar pruebas ejecutadas (**resultado real**) y no ejecutadas con causa. Para esta unidad: `./gradlew lintDebug`; `./gradlew testDebugUnitTest`; Gitleaks si está disponible.
- Marcar `[x]` solo cuando se verifique la tarea correspondiente; conservar pendientes si no existe evidencia.
- Actualizar `memoria.md` con comandos, archivos, decisiones y riesgos; no avanzar a otra spec automáticamente.
- `git push`, merge, despliegue y operaciones destructivas siguen requiriendo autorización separada.
