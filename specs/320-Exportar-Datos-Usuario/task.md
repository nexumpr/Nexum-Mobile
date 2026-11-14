---
project: Nexum-Mobile
unit: 320-Exportar-Datos-Usuario
kind: implementation-tasks
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# TASK — Exportación de Datos del Usuario (Export User Data)

**Estado:** Fase A documental completada; todas las actividades pendientes de autorización expresa para ejecución (`Fase B`). Ver **Cierre y evidencia** al final. Revisar dependencias en `specs/README.md` antes de cualquier implementación.

**Dependencias:** [`240-Perfil-Usuario`](../240-Perfil-Usuario/spec.md), [`270-Aviso-Privacidad-Vigente`](../270-Aviso-Privacidad-Vigente/spec.md), [`140-Login`](../140-Login/spec.md), [`150-Refresh-Token`](../150-Refresh-Token/spec.md), [`030-di-navegacion-ui`](../030-di-navegacion-ui/spec.md), [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md), [`070-permisos-privacidad-accesibilidad`](../070-permisos-privacidad-accesibilidad/spec.md). **Regla:** no implementar código hasta recibir mandato explícito «implementa specs/320-Exportar-Datos-Usuario».

## Actividades verificables
- [ ] **N320-T01 — Crear los modelos del contrato NEXUM-E020 (`ExportFormat`, `ExportReadyResponse`, `ExportPendingResponse`, `ExportOutcome`).**
  - **Requisito:** `M320-R02` · **Plan:** `N320-P01`.
  - **Aceptación:** Modelos inmutables en Kotlin; `ExportFormat` serializa a `json`/`csv`; `ExportOutcome` distingue archivo listo y trabajo diferido.
  - **Pruebas:** Pruebas unitarias de serialización/deserialización en `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** archivos creados, comandos, salida real, fecha y limitaciones.

- [ ] **N320-T02 — Ampliar `AuthApi` y `AuthRepository` con la exportación y el mapeo del sobre de errores.**
  - **Requisito:** `M320-R02`, `M320-R04` · **Plan:** `N320-P02`.
  - **Aceptación:** `suspend fun exportUserData(accessToken: String, format: ExportFormat): ExportResponse` en `AuthApi` y `suspend fun exportUserData(format: ExportFormat): AuthResult<ExportOutcome>` en `AuthRepository`; el error estándar conserva `code`, `message`, `details` y `traceId`.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** diff de interfaces, salida real de compilación y limitaciones.

- [ ] **N320-T03 — Implementar simulación en `MockAuthApi`.**
  - **Requisito:** `M320-R05` · **Plan:** `N320-P03`.
  - **Aceptación:** Retardo de 400–800 ms; contenido sintético JSON y CSV con perfil, movimientos, metas y presupuestos; montos como cadena decimal (`"125.50"`), moneda `MXN`, fechas RFC 3339 UTC; escenario `202` con `exportJobId`; centinela `net@error.test`.
  - **Pruebas:** Pruebas unitarias en `MockAuthApiTest`.
  - **Evidencia a registrar:** casos de prueba ejecutados, salida real, fecha y limitaciones.

- [ ] **N320-T04 — Construir `ExportDataViewModel` MVI con protección de doble pulsación y descarte del contenido al salir.**
  - **Requisito:** `M320-R01`, `M320-R03`, `M320-R04` · **Plan:** `N320-P04`.
  - **Aceptación:** Reducer con `SelectFormat`, `Export`, `Retry`, `DismissError` y `ConsumeOutcome`; `isLoading = true` impide envíos concurrentes; el contenido exportado no se persiste ni se registra en logs; error y sin conexión no fingen éxito.
  - **Pruebas:** Pruebas unitarias en `ExportDataViewModelTest` con `kotlinx-coroutines-test`.
  - **Evidencia a registrar:** pruebas de ViewModel ejecutadas, salida real, fecha y limitaciones.

- [ ] **N320-T05 — Integrar la sección de exportación en `ProfileScreen` con Material 3 y accesibilidad.**
  - **Requisito:** `M320-R01`, `M320-R03` · **Plan:** `N320-P05`.
  - **Aceptación:** Acción "Exportar mis datos", selector `json`/`csv`, estado de progreso, mensaje de confirmación o `exportJobId`, y entrega del archivo por el mecanismo del sistema aprobado; etiquetas legibles y áreas táctiles adecuadas.
  - **Pruebas:** Revisión de componentes Compose; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** archivos de UI modificados, salida real y limitaciones.

- [ ] **N320-T06 — Añadir suite completa de pruebas unitarias de exportación.**
  - **Requisito:** `M320-R01`…`M320-R06` · **Plan:** `N320-P06`.
  - **Aceptación:** Cobertura de JSON, CSV, `202`, doble pulsación, `400`/`401`/`403`/`404`/`409`/`422`/`429`/`500`, pérdida de conectividad y conservación de `traceId`.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew lintDebug`.
  - **Evidencia a registrar:** reporte de pruebas unitarias, conteo total de tests, salida real, fecha y limitaciones.

## Cierre y evidencia
- **Fase A (documental):** especificación, plan técnico y actividades creadas. **Ninguna tarea implementada ni ejecutada en esta fase.**
- **Fase B (pendiente de mandato explícito):** requerirá orden humana expresa para modificar archivos `.kt`, ejecutar pruebas Gradle y registrar evidencia en `memoria.md`.
- No alterar código de producción, no realizar push ni despliegues en esta fase.
