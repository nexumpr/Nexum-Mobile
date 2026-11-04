---
project: Nexum-Mobile
unit: 040-cliente-http-contratos
kind: implementation-tasks
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# TASK — Cliente HTTP y contratos de integración

**Estado:** todas las tareas están pendientes. **Preparado para ejecución por fases**; iniciar solo tras una instrucción explícita que nombre `040-cliente-http-contratos` y revisar dependencias en `specs/README.md`.

**Dependencias:** `020-nucleo-mvi`. **Regla:** no implementar funcionalidades financieras ni cambios externos sin aprobación adicional.

## Actividades verificables
- [ ] **N040-T01 — Definir interfaz de cliente y contrato de error de infraestructura.**
  - **Requisito:** `M040-R01` · **Plan:** `N040-P01`.
  - **Aceptación:** cambio o decisión técnica comprobable en el alcance; no se aceptan afirmaciones sin revisión.
  - **Pruebas:** `./gradlew testDebugUnitTest`; tests con servidor simulado.
  - **Evidencia a registrar:** rutas cambiadas, comandos, salida real, fecha y limitaciones.
- [ ] **N040-T02 — Implementar adaptadores simulables, sin endpoints financieros.**
  - **Requisito:** `M040-R02` · **Plan:** `N040-P02`.
  - **Aceptación:** cambio o decisión técnica comprobable en el alcance; no se aceptan afirmaciones sin revisión.
  - **Pruebas:** `./gradlew testDebugUnitTest`; tests con servidor simulado.
  - **Evidencia a registrar:** rutas cambiadas, comandos, salida real, fecha y limitaciones.
- [ ] **N040-T03 — Probar timeout, JSON malformado, 401/403/5xx y estados de carga/error.**
  - **Requisito:** `M040-R03` · **Plan:** `N040-P03`.
  - **Aceptación:** cambio o decisión técnica comprobable en el alcance; no se aceptan afirmaciones sin revisión.
  - **Pruebas:** `./gradlew testDebugUnitTest`; tests con servidor simulado.
  - **Evidencia a registrar:** rutas cambiadas, comandos, salida real, fecha y limitaciones.

## Cierre y evidencia
- Reportar pruebas ejecutadas (**resultado real**) y no ejecutadas con causa. Para esta unidad: `./gradlew testDebugUnitTest`; tests con servidor simulado.
- Marcar `[x]` solo cuando se verifique la tarea correspondiente; conservar pendientes si no existe evidencia.
- Actualizar `memoria.md` con comandos, archivos, decisiones y riesgos; no avanzar a otra spec automáticamente.
- `git push`, merge, despliegue y operaciones destructivas siguen requiriendo autorización separada.
