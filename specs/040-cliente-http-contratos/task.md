---
project: Nexum-Mobile
unit: 040-cliente-http-contratos
kind: implementation-tasks
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# TASK — Cliente HTTP y contratos de integración

**Estado:** tareas verificadas e implementadas.

**Dependencias:** `020-nucleo-mvi`. **Regla:** no implementar funcionalidades financieras ni cambios externos sin aprobación adicional.

## Actividades verificables
- [x] **N040-T01 — Definir interfaz de cliente y contrato de error de infraestructura.**
  - **Requisito:** `M040-R01` · **Plan:** `N040-P01`.
  - **Aceptación:** Modelados contratos canónicos de error e infraestructura en `mx.nexum.app.core.network` (`ApiErrorResponse`, `ApiErrorDetail`, `HttpErrorType`, `NetworkErrorType`, `ApiResult<T>`) asegurando que errores de red o servidor nunca presenten datos simulados ni éxito falso (MO-PR04, MO-PR05).
  - **Pruebas:** `.\gradlew.bat testDebugUnitTest` (`HttpClientAdapterTest.http400BadRequestParsesErrorEnvelope`, `http401UnauthorizedMapsCorrectly`, `http404NotFoundMapsCorrectly`, `http409ConflictMapsCorrectly`, `http500ServerErrorNeverPresentsFakeSuccess`).
  - **Evidencia a registrar:** `app/src/main/java/mx/nexum/app/core/network/HttpContracts.kt`, `app/src/main/java/mx/nexum/app/core/network/SafeApiCall.kt`.
- [x] **N040-T02 — Implementar adaptadores simulables, sin endpoints financieros.**
  - **Requisito:** `M040-R02` · **Plan:** `N040-P02`.
  - **Aceptación:** Configurado `network_security_config.xml` enlazado en `AndroidManifest.xml` con `cleartextTrafficPermitted=false` y dominios aprobados (`d2lpmqy5w6x9es.cloudfront.net`, `nexum.mx`) para forzar TLS/HTTPS; construido `SimulatedHttpAdapter` para ejecución y testeo sin endpoints financieros ni backend en vivo.
  - **Pruebas:** `.\gradlew.bat testDebugUnitTest` (`HttpClientAdapterTest.simulatedHttpAdapterAllowsInjectingArbitraryOutcomes`). `assembleDebug` y `lintDebug` BUILD SUCCESSFUL.
  - **Evidencia a registrar:** `app/src/main/res/xml/network_security_config.xml`, `app/src/main/AndroidManifest.xml`, `app/src/main/java/mx/nexum/app/core/network/SimulatedHttpAdapter.kt`.
- [x] **N040-T03 — Probar timeout, JSON malformado, 401/403/5xx y estados de carga/error.**
  - **Requisito:** `M040-R03` · **Plan:** `N040-P03`.
  - **Aceptación:** Pruebas unitarias de parsing con Gson, extracción de cabeceras (`Retry-After` en 429), captura de `SocketTimeoutException` (Timeout), `UnknownHostException` (NoConnectivity) y `JsonSyntaxException` (MalformedResponse).
  - **Pruebas:** Suite de 75 tests unitarios ejecutados, 0 fallos, 100% de éxito (`HttpClientAdapterTest` 11 pruebas passing).
  - **Evidencia a registrar:** `app/build/reports/tests/testDebugUnitTest/index.html`, `memoria.md` §38.

## Cierre y evidencia
- Reportar pruebas ejecutadas (**resultado real**) y no ejecutadas con causa. Para esta unidad: `./gradlew testDebugUnitTest`; tests con servidor simulado.
- Marcar `[x]` solo cuando se verifique la tarea correspondiente; conservar pendientes si no existe evidencia.
- Actualizar `memoria.md` con comandos, archivos, decisiones y riesgos; no avanzar a otra spec automáticamente.
- `git push`, merge, despliegue y operaciones destructivas siguen requiriendo autorización separada.
