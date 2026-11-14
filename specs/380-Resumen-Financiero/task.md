---
project: Nexum-Mobile
unit: 380-Resumen-Financiero
kind: implementation-tasks
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# TASK — Resumen Financiero Mensual (Monthly Financial Summary)

**Estado:** Fase A documental completada; todas las actividades pendientes de autorización expresa para ejecución (`Fase B`). Ver **Cierre y evidencia** al final. Revisar dependencias en `specs/README.md` antes de cualquier implementación.

**Dependencias:** [`360-Consultar-Ingreso`](../360-Consultar-Ingreso/spec.md), [`330-Bootstrap-Inicial`](../330-Bootstrap-Inicial/spec.md), [`140-Login`](../140-Login/spec.md), [`150-Refresh-Token`](../150-Refresh-Token/spec.md), [`030-di-navegacion-ui`](../030-di-navegacion-ui/spec.md), [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md). **Regla:** no implementar código hasta recibir mandato explícito «implementa specs/380-Resumen-Financiero».

## Actividades verificables
- [ ] **N380-T01 — Crear los modelos del contrato NEXUM-E026 (`FinancialSummaryResponse`, `FinancialSummary`, `FinancialSummaryResult`) y `SummaryFormula`.**
  - **Requisito:** `M380-R02` · **Plan:** `N380-P01`.
  - **Aceptación:** Modelos inmutables con siete campos; importes como `String`; `SummaryFormula` verifica la resta con `BigDecimal` sin `Float`/`Double`.
  - **Pruebas:** Pruebas unitarias de deserialización y fórmula en `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** archivos creados, comandos, salida real, fecha y limitaciones.

- [ ] **N380-T02 — Ampliar `AuthApi` y `AuthRepository` con la consulta por mes y la validación no destructiva.**
  - **Requisito:** `M380-R02`, `M380-R04` · **Plan:** `N380-P02`.
  - **Aceptación:** `suspend fun getFinancialSummary(accessToken: String, month: String, timezone: String? = null): FinancialSummaryResponse` en `AuthApi` y `suspend fun getFinancialSummary(month: String): AuthResult<FinancialSummaryResult>` en `AuthRepository`; mes validado como `YYYY-MM`; error estándar con `traceId`.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** diff de interfaces, salida real de compilación y limitaciones.

- [ ] **N380-T03 — Implementar simulación en `MockAuthApi`.**
  - **Requisito:** `M380-R05` · **Plan:** `N380-P03`.
  - **Aceptación:** Retardo de 300–700 ms; `2026-11` con el ejemplo documentado; otros meses sintéticos consistentes; `currency = "MXN"`; centinela `net@error.test`.
  - **Pruebas:** Pruebas unitarias en `MockAuthApiTest`.
  - **Evidencia a registrar:** casos de prueba ejecutados, salida real, fecha y limitaciones.

- [ ] **N380-T04 — Construir `FinancialSummaryViewModel` MVI con cambio de mes y cancelación de peticiones.**
  - **Requisito:** `M380-R01`, `M380-R03`, `M380-R04` · **Plan:** `N380-P04`.
  - **Aceptación:** Reducer con `SelectMonth`, `LoadSummary`, `Retry` y `DismissError`; cancela la consulta anterior; expone `isConsistent` sin corregir importes; conserva caché anterior etiquetada.
  - **Pruebas:** Pruebas unitarias en `FinancialSummaryViewModelTest` con `kotlinx-coroutines-test`.
  - **Evidencia a registrar:** pruebas de ViewModel ejecutadas, salida real, fecha y limitaciones.

- [ ] **N380-T05 — Integrar la tarjeta de resumen en `HomeScreen` con Material 3.**
  - **Requisito:** `M380-R01`, `M380-R03` · **Plan:** `N380-P05`.
  - **Aceptación:** Disponible destacado, Ingreso, Obligatorios, Hormiga y Reservado con dos decimales y `MXN`; selector `YYYY-MM`; aviso de estimación y de inconsistencia cuando aplique; etiquetas accesibles.
  - **Pruebas:** Revisión de componentes Compose; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** archivos de UI modificados, salida real y limitaciones.

- [ ] **N380-T06 — Añadir suite completa de pruebas unitarias del resumen financiero.**
  - **Requisito:** `M380-R01`…`M380-R06` · **Plan:** `N380-P06`.
  - **Aceptación:** Cobertura de la fórmula documentada, inconsistencia, cambio rápido de mes, `400`/`401`/`403`/`422`/`429`/`500`, sin conexión y no duplicación de recurrentes pagados.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew lintDebug`.
  - **Evidencia a registrar:** reporte de pruebas unitarias, conteo total de tests, salida real, fecha y limitaciones.

## Cierre y evidencia
- **Fase A (documental):** especificación, plan técnico y actividades creadas. **Ninguna tarea implementada ni ejecutada en esta fase.**
- **Fase B (pendiente de mandato explícito):** requerirá orden humana expresa para modificar archivos `.kt`, ejecutar pruebas Gradle y registrar evidencia en `memoria.md`.
- No alterar código de producción, no realizar push ni despliegues en esta fase.
