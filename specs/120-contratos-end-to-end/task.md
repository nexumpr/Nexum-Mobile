---
project: Nexum-Mobile
unit: 120-contratos-end-to-end
kind: implementation-tasks
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# TASK — Integración técnica contractual con Go

**Estado:** todas las tareas están pendientes. **Preparado para ejecución por fases**; iniciar solo tras una instrucción explícita que nombre `120-contratos-end-to-end` y revisar dependencias en `specs/README.md`.

**Dependencias:** `040-cliente-http-contratos`, `050-sesion-credenciales`, `060-cache-offline`. **Regla:** no implementar funcionalidades financieras ni cambios externos sin aprobación adicional.

## Actividades verificables
- [ ] **N120-T01 — Producir especificación técnica compartida aprobada.**
  - **Requisito:** `M120-R01` · **Plan:** `N120-P01`.
  - **Aceptación:** cambio o decisión técnica comprobable en el alcance; no se aceptan afirmaciones sin revisión.
  - **Pruebas:** `./gradlew testDebugUnitTest`; pruebas con mock server sin datos reales.
  - **Evidencia a registrar:** rutas cambiadas, comandos, salida real, fecha y limitaciones.
- [ ] **N120-T02 — Implementar pruebas de contrato sin rutas de negocio.**
  - **Requisito:** `M120-R02` · **Plan:** `N120-P02`.
  - **Aceptación:** cambio o decisión técnica comprobable en el alcance; no se aceptan afirmaciones sin revisión.
  - **Pruebas:** `./gradlew testDebugUnitTest`; pruebas con mock server sin datos reales.
  - **Evidencia a registrar:** rutas cambiadas, comandos, salida real, fecha y limitaciones.
- [ ] **N120-T03 — Ejecutar tests disponibles y registrar diferencias para Backend.**
  - **Requisito:** `M120-R03` · **Plan:** `N120-P03`.
  - **Aceptación:** cambio o decisión técnica comprobable en el alcance; no se aceptan afirmaciones sin revisión.
  - **Pruebas:** `./gradlew testDebugUnitTest`; pruebas con mock server sin datos reales.
  - **Evidencia a registrar:** rutas cambiadas, comandos, salida real, fecha y limitaciones.

## Cierre y evidencia
- Reportar pruebas ejecutadas (**resultado real**) y no ejecutadas con causa. Para esta unidad: `./gradlew testDebugUnitTest`; pruebas con mock server sin datos reales.
- Marcar `[x]` solo cuando se verifique la tarea correspondiente; conservar pendientes si no existe evidencia.
- Actualizar `memoria.md` con comandos, archivos, decisiones y riesgos; no avanzar a otra spec automáticamente.
- `git push`, merge, despliegue y operaciones destructivas siguen requiriendo autorización separada.
