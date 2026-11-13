---
project: Nexum-Mobile
unit: 360-Consultar-Ingreso
kind: implementation-tasks
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# TASK — Consulta de Ingreso Habitual (Get Habitual Income)

**Estado:** Fase A documental completada; todas las actividades pendientes de autorización expresa para ejecución (`Fase B`). Ver **Cierre y evidencia** al final. Revisar dependencias en `specs/README.md` antes de cualquier implementación.

**Dependencias:** [`350-Catalogo-Frecuencias`](../350-Catalogo-Frecuencias/spec.md), [`240-Perfil-Usuario`](../240-Perfil-Usuario/spec.md), [`140-Login`](../140-Login/spec.md), [`150-Refresh-Token`](../150-Refresh-Token/spec.md), [`030-di-navegacion-ui`](../030-di-navegacion-ui/spec.md), [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md). **Regla:** no implementar código hasta recibir mandato explícito «implementa specs/360-Consultar-Ingreso».

## Actividades verificables
- [ ] **N360-T01 — Crear los modelos del contrato NEXUM-E024 (`IncomeResponse`, `Income`).**
  - **Requisito:** `M360-R02` · **Plan:** `N360-P01`.
  - **Aceptación:** Modelos inmutables con `amount`, `frequency`, `monthlyEquivalent` y `currency` como `String`; sin `Float`/`Double`.
  - **Pruebas:** Pruebas unitarias de deserialización en `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** archivos creados, comandos, salida real, fecha y limitaciones.

- [ ] **N360-T02 — Ampliar `AuthApi` y `AuthRepository` con la consulta y el manejo de ausencia.**
  - **Requisito:** `M360-R02`, `M360-R04` · **Plan:** `N360-P02`.
  - **Aceptación:** `suspend fun getIncome(accessToken: String): IncomeResponse` en `AuthApi` y `suspend fun getIncome(): AuthResult<Income>` en `AuthRepository`; moneda distinta de `MXN` se rechaza; ausencia se mapea a `NOT_FOUND`; el error estándar conserva `traceId`.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** diff de interfaces, salida real de compilación y limitaciones.

- [ ] **N360-T03 — Implementar simulación en `MockAuthApi`.**
  - **Requisito:** `M360-R05` · **Plan:** `N360-P03`.
  - **Aceptación:** Retardo de 250–600 ms; `IncomeResponse("12000.00", "monthly", "12000.00", "MXN")`; escenario sin ingreso; centinela `net@error.test`.
  - **Pruebas:** Pruebas unitarias en `MockAuthApiTest`.
  - **Evidencia a registrar:** casos de prueba ejecutados, salida real, fecha y limitaciones.

- [ ] **N360-T04 — Construir `IncomeViewModel` MVI con estados de vacío, error, sin conexión y apertura del editor.**
  - **Requisito:** `M360-R01`, `M360-R03`, `M360-R04` · **Plan:** `N360-P04`.
  - **Aceptación:** Reducer con `LoadIncome`, `Refresh`, `Retry`, `DismissError` y `OpenEditor`; `isEmpty` sin ceros inventados; `NavigateToIncomeEditor` como efecto único.
  - **Pruebas:** Pruebas unitarias en `IncomeViewModelTest` con `kotlinx-coroutines-test`.
  - **Evidencia a registrar:** pruebas de ViewModel ejecutadas, salida real, fecha y limitaciones.

- [ ] **N360-T05 — Integrar la tarjeta de ingreso en `ProfileScreen` con Material 3.**
  - **Requisito:** `M360-R01`, `M360-R03` · **Plan:** `N360-P05`.
  - **Aceptación:** Importe original, etiqueta de frecuencia, equivalente mensual, moneda `MXN`, aviso de estimación, estado vacío y acción "Editar"; etiquetas accesibles.
  - **Pruebas:** Revisión de componentes Compose; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** archivos de UI modificados, salida real y limitaciones.

- [ ] **N360-T06 — Añadir suite completa de pruebas unitarias de consulta de ingreso.**
  - **Requisito:** `M360-R01`…`M360-R06` · **Plan:** `N360-P06`.
  - **Aceptación:** Cobertura de mapeo decimal, frecuencia distinta de `monthly`, vacío, `401`/`403`/`404`/`429`/`500`, sin conexión y apertura del editor.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew lintDebug`.
  - **Evidencia a registrar:** reporte de pruebas unitarias, conteo total de tests, salida real, fecha y limitaciones.

## Cierre y evidencia
- **Fase A (documental):** especificación, plan técnico y actividades creadas. **Ninguna tarea implementada ni ejecutada en esta fase.**
- **Fase B (pendiente de mandato explícito):** requerirá orden humana expresa para modificar archivos `.kt`, ejecutar pruebas Gradle y registrar evidencia en `memoria.md`.
- No alterar código de producción, no realizar push ni despliegues en esta fase.
