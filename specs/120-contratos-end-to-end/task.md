---
project: Nexum-Mobile
unit: 120-contratos-end-to-end
kind: implementation-tasks
version: 1.0.0
status: implementada_y_verificada_localmente
implementation: executed_on_explicit_request
---

# TASK — Integración técnica contractual con Go

**Estado:** tareas ejecutadas y verificadas localmente el 2026-11-17 bajo mandato explícito de `120-contratos-end-to-end`. La aprobación de Backend Go y los cambios productivos de reintentos, timeouts, cabeceras globales o DTOs siguen pendientes; no se acredita integración real.

**Dependencias:** `040-cliente-http-contratos`, `050-sesion-credenciales`, `060-cache-offline`. **Regla:** no implementar funcionalidades financieras ni cambios externos sin aprobación adicional.

## Actividades verificables
- [x] **N120-T01 — Producir especificación técnica compartida aprobada.**
  - **Requisito:** `M120-R01` · **Plan:** `N120-P01`.
  - **Aceptación:** documento técnico y catálogo de fixtures verificables; aprobación de Backend Go **pendiente**, por lo que no se afirma consenso externo.
  - **Pruebas:** `.\gradlew.bat testDebugUnitTest --rerun-tasks` → `BUILD SUCCESSFUL`; 18 suites, 131 tests, 0 fallos, 0 errores.
  - **Evidencia:** `specs/120-contratos-end-to-end/rest-contract.md`, `fixture-catalog.md` y `memoria.md` §45 (2026-11-17).
- [x] **N120-T02 — Implementar pruebas de contrato sin rutas de negocio.**
  - **Requisito:** `M120-R02` · **Plan:** `N120-P02`.
  - **Aceptación:** 16 pruebas JVM nuevas con simulador HTTP en memoria; sin MockWebServer, sin dependencias nuevas y sin rutas financieras.
  - **Pruebas:** `.\gradlew.bat testDebugUnitTest --rerun-tasks` → `BUILD SUCCESSFUL`.
  - **Evidencia:** `app/src/test/java/mx/nexum/app/core/contract/`, `app/src/test/resources/contract/` y `memoria.md` §45 (2026-11-17).
- [x] **N120-T03 — Ejecutar tests disponibles y registrar diferencias para Backend.**
  - **Requisito:** `M120-R03` · **Plan:** `N120-P03`.
  - **Aceptación:** diferencias documentadas en `rest-contract.md` y `retry-and-fallback-matrix.md`; sin cambios productivos de red ni reintentos.
  - **Pruebas:** `.\gradlew.bat testDebugUnitTest --rerun-tasks` y lectura de XML → `ARCHIVOS=18 TESTS=131 FAILURES=0 ERRORS=0`.
  - **Evidencia:** `git diff --check` sin errores de whitespace y `memoria.md` §45 (2026-11-17).

## Cierre y evidencia
- Reportar pruebas ejecutadas (**resultado real**) y no ejecutadas con causa. Para esta unidad: `./gradlew testDebugUnitTest`; pruebas con mock server sin datos reales.
- Marcar `[x]` solo cuando se verifique la tarea correspondiente; conservar pendientes si no existe evidencia.
- Actualizar `memoria.md` con comandos, archivos, decisiones y riesgos; no avanzar a otra spec automáticamente.
- `git push`, merge, despliegue y operaciones destructivas siguen requiriendo autorización separada.
