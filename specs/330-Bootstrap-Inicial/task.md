---
project: Nexum-Mobile
unit: 330-Bootstrap-Inicial
kind: implementation-tasks
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# TASK — Carga Inicial Agregada (Bootstrap)

**Estado:** Fase A documental completada; todas las actividades pendientes de autorización expresa para ejecución (`Fase B`). Ver **Cierre y evidencia** al final. Revisar dependencias en `specs/README.md` antes de cualquier implementación.

**Dependencias:** [`240-Perfil-Usuario`](../240-Perfil-Usuario/spec.md), [`300-Estado-Onboarding`](../300-Estado-Onboarding/spec.md), [`140-Login`](../140-Login/spec.md), [`150-Refresh-Token`](../150-Refresh-Token/spec.md), [`030-di-navegacion-ui`](../030-di-navegacion-ui/spec.md), [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md). **Regla:** no implementar código hasta recibir mandato explícito «implementa specs/330-Bootstrap-Inicial».

## Actividades verificables
- [ ] **N330-T01 — Crear los modelos del contrato NEXUM-E021 (`BootstrapResponse`, `BootstrapSummaryResponse`, `BootstrapSnapshot`).**
  - **Requisito:** `M330-R02` · **Plan:** `N330-P01`.
  - **Aceptación:** Modelos inmutables con secciones opcionales; importes como cadena decimal; `summary` conserva la moneda `MXN`.
  - **Pruebas:** Pruebas unitarias de serialización/deserialización en `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** archivos creados, comandos, salida real, fecha y limitaciones.

- [ ] **N330-T02 — Ampliar `AuthApi` y `AuthRepository` con la carga agregada y su degradación.**
  - **Requisito:** `M330-R02`, `M330-R04` · **Plan:** `N330-P02`.
  - **Aceptación:** `suspend fun getBootstrap(accessToken: String, include: List<String>): BootstrapResponse` en `AuthApi` y `suspend fun getBootstrap(include: List<String>): AuthResult<BootstrapSnapshot>` en `AuthRepository`; secciones nulas no generan valores inventados y el error estándar conserva `traceId`.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** diff de interfaces, salida real de compilación y limitaciones.

- [ ] **N330-T03 — Implementar simulación en `MockAuthApi`.**
  - **Requisito:** `M330-R05` · **Plan:** `N330-P03`.
  - **Aceptación:** Retardo de 250–600 ms; instantánea determinista del usuario activo; respeto de `include`; montos como cadena decimal, moneda `MXN`, fechas RFC 3339 UTC; centinela `net@error.test`.
  - **Pruebas:** Pruebas unitarias en `MockAuthApiTest`.
  - **Evidencia a registrar:** casos de prueba ejecutados, salida real, fecha y limitaciones.

- [ ] **N330-T04 — Construir `HomeViewModel` MVI con carga única, contenido parcial y reintento.**
  - **Requisito:** `M330-R01`, `M330-R03`, `M330-R04` · **Plan:** `N330-P04`.
  - **Aceptación:** Reducer con `LoadBootstrap`, `Refresh`, `Retry` y `DismissError`; una sola petición en curso; `isPartial` para secciones ausentes; sin acciones de escritura derivadas del agregador; error y sin conexión no fingen éxito.
  - **Pruebas:** Pruebas unitarias en `HomeViewModelTest` con `kotlinx-coroutines-test`.
  - **Evidencia a registrar:** pruebas de ViewModel ejecutadas, salida real, fecha y limitaciones.

- [ ] **N330-T05 — Integrar `HomeScreen` en Compose con esqueleto de carga y secciones accesibles.**
  - **Requisito:** `M330-R01`, `M330-R03` · **Plan:** `N330-P05`.
  - **Aceptación:** Saludo con nombre, tarjeta de importes, racha y conteos cuando existan; esqueleto de carga, mensaje de error y "Reintentar"; etiquetas legibles y áreas táctiles adecuadas.
  - **Pruebas:** Revisión de componentes Compose; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** archivos de UI modificados, salida real y limitaciones.

- [ ] **N330-T06 — Añadir suite completa de pruebas unitarias de bootstrap.**
  - **Requisito:** `M330-R01`…`M330-R06` · **Plan:** `N330-P06`.
  - **Aceptación:** Cobertura de carga completa, carga filtrada, secciones parciales, doble carga, `401`/`403`/`422`/`429`/`500`, pérdida de conectividad y conservación de `traceId`.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew lintDebug`.
  - **Evidencia a registrar:** reporte de pruebas unitarias, conteo total de tests, salida real, fecha y limitaciones.

## Cierre y evidencia
- **Fase A (documental):** especificación, plan técnico y actividades creadas. **Ninguna tarea implementada ni ejecutada en esta fase.**
- **Fase B (pendiente de mandato explícito):** requerirá orden humana expresa para modificar archivos `.kt`, ejecutar pruebas Gradle y registrar evidencia en `memoria.md`.
- No alterar código de producción, no realizar push ni despliegues en esta fase.
