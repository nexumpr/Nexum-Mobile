---
project: Nexum-Mobile
unit: 050-sesion-credenciales
kind: implementation-tasks
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# TASK — Sesión y custodia de credenciales

**Estado:** todas las tareas están pendientes. **Preparado para ejecución por fases**; iniciar solo tras una instrucción explícita que nombre `050-sesion-credenciales` y revisar dependencias en `specs/README.md`.

**Dependencias:** `040-cliente-http-contratos`. **Regla:** no implementar funcionalidades financieras ni cambios externos sin aprobación adicional.

## Actividades verificables
- [ ] **N050-T01 — Documentar política de sesión y bloqueo de proveedor identidad.**
  - **Requisito:** `M050-R01` · **Plan:** `N050-P01`.
  - **Aceptación:** cambio o decisión técnica comprobable en el alcance; no se aceptan afirmaciones sin revisión.
  - **Pruebas:** `./gradlew testDebugUnitTest`; tests de custodia y limpieza sin credenciales reales.
  - **Evidencia a registrar:** rutas cambiadas, comandos, salida real, fecha y limitaciones.
- [ ] **N050-T02 — Implementar abstracciones seguras con datos ficticios cuando haya decisión.**
  - **Requisito:** `M050-R02` · **Plan:** `N050-P02`.
  - **Aceptación:** cambio o decisión técnica comprobable en el alcance; no se aceptan afirmaciones sin revisión.
  - **Pruebas:** `./gradlew testDebugUnitTest`; tests de custodia y limpieza sin credenciales reales.
  - **Evidencia a registrar:** rutas cambiadas, comandos, salida real, fecha y limitaciones.
- [ ] **N050-T03 — Probar borrado de sesión, token vencido, 401 y usuario diferente.**
  - **Requisito:** `M050-R03` · **Plan:** `N050-P03`.
  - **Aceptación:** cambio o decisión técnica comprobable en el alcance; no se aceptan afirmaciones sin revisión.
  - **Pruebas:** `./gradlew testDebugUnitTest`; tests de custodia y limpieza sin credenciales reales.
  - **Evidencia a registrar:** rutas cambiadas, comandos, salida real, fecha y limitaciones.

## Cierre y evidencia
- Reportar pruebas ejecutadas (**resultado real**) y no ejecutadas con causa. Para esta unidad: `./gradlew testDebugUnitTest`; tests de custodia y limpieza sin credenciales reales.
- Marcar `[x]` solo cuando se verifique la tarea correspondiente; conservar pendientes si no existe evidencia.
- Actualizar `memoria.md` con comandos, archivos, decisiones y riesgos; no avanzar a otra spec automáticamente.
- `git push`, merge, despliegue y operaciones destructivas siguen requiriendo autorización separada.
