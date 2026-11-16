---
project: Nexum-Mobile
unit: 370-Actualizar-Ingreso
kind: implementation-tasks
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# TASK — Actualización de Ingreso Habitual (Update Habitual Income)

**Estado:** Fase A documental completada; todas las actividades pendientes de autorización expresa para ejecución (`Fase B`). Ver **Cierre y evidencia** al final. Revisar dependencias en `specs/README.md` antes de cualquier implementación.

**Dependencias:** [`360-Consultar-Ingreso`](../360-Consultar-Ingreso/spec.md), [`350-Catalogo-Frecuencias`](../350-Catalogo-Frecuencias/spec.md), [`140-Login`](../140-Login/spec.md), [`150-Refresh-Token`](../150-Refresh-Token/spec.md), [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md). **Regla:** no implementar código hasta recibir mandato explícito «implementa specs/370-Actualizar-Ingreso».

## Actividades verificables
- [ ] **N370-T01 — Crear los modelos del contrato NEXUM-E025 (`UpdateIncomeRequest`) y `IncomeValidator`.**
  - **Requisito:** `M370-R02` · **Plan:** `N370-P01`.
  - **Aceptación:** DTO inmutable con `amount` y `frequency` como `String`; validador con `BigDecimal` que exige importe mayor que cero y dos decimales, sin `Float`/`Double`.
  - **Pruebas:** Pruebas unitarias de serialización y validación en `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** archivos creados, comandos, salida real, fecha y limitaciones.

- [ ] **N370-T02 — Ampliar `AuthApi` y `AuthRepository` con la actualización e invalidación de resumen.**
  - **Requisito:** `M370-R02`, `M370-R04` · **Plan:** `N370-P02`.
  - **Aceptación:** `suspend fun updateIncome(accessToken: String, request: UpdateIncomeRequest): IncomeResponse` en `AuthApi` y `suspend fun updateIncome(amount: String, frequency: String): AuthResult<Income>` en `AuthRepository`; no se crean movimientos; la caché de resumen se invalida; el error estándar conserva `traceId`.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** diff de interfaces, salida real de compilación y limitaciones.

- [ ] **N370-T03 — Implementar simulación en `MockAuthApi`.**
  - **Requisito:** `M370-R05` · **Plan:** `N370-P03`.
  - **Aceptación:** Retardo de 300–700 ms; validación de importe y decimales; recálculo de `monthlyEquivalent` con los factores documentados; actualización del ingreso en memoria; centinelas `net@error.test` y `rule_error_income`.
  - **Pruebas:** Pruebas unitarias en `MockAuthApiTest`.
  - **Evidencia a registrar:** casos de prueba ejecutados, salida real, fecha y limitaciones.

- [ ] **N370-T04 — Construir `UpdateIncomeViewModel` MVI con validación, guardado y conservación del formulario.**
  - **Requisito:** `M370-R01`, `M370-R03`, `M370-R04` · **Plan:** `N370-P04`.
  - **Aceptación:** Reducer con `AmountChanged`, `FrequencySelected`, `Save`, `Retry` y `DismissError`; validación en línea; `isSaving` evita doble envío; error conserva valores; éxito emite `ShowConfirmation` y `NavigateBack`.
  - **Pruebas:** Pruebas unitarias en `UpdateIncomeViewModelTest` con `kotlinx-coroutines-test`.
  - **Evidencia a registrar:** pruebas de ViewModel ejecutadas, salida real, fecha y limitaciones.

- [ ] **N370-T05 — Integrar el editor de ingreso en Compose con Material 3.**
  - **Requisito:** `M370-R01`, `M370-R03` · **Plan:** `N370-P05`.
  - **Aceptación:** Campo de importe con teclado decimal, selector de frecuencia, texto de equivalente estimado, errores asociados al campo, botón "Guardar ingreso" con progreso y cancelación sin cambios.
  - **Pruebas:** Revisión de componentes Compose; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** archivos de UI modificados, salida real y limitaciones.

- [ ] **N370-T06 — Añadir suite completa de pruebas unitarias de actualización de ingreso.**
  - **Requisito:** `M370-R01`…`M370-R06` · **Plan:** `N370-P06`.
  - **Aceptación:** Cobertura de `"125.50"`, `"0.00"`, `"-5.00"`, `"125.5"`, factores por frecuencia, `400`/`401`/`403`/`404`/`409`/`422`/`429`/`500`, sin conexión, doble envío y ausencia de depósitos.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew lintDebug`.
  - **Evidencia a registrar:** reporte de pruebas unitarias, conteo total de tests, salida real, fecha y limitaciones.

## Cierre y evidencia
- **Fase A (documental):** especificación, plan técnico y actividades creadas. **Ninguna tarea implementada ni ejecutada en esta fase.**
- **Fase B (pendiente de mandato explícito):** requerirá orden humana expresa para modificar archivos `.kt`, ejecutar pruebas Gradle y registrar evidencia en `memoria.md`.
- No alterar código de producción, no realizar push ni despliegues en esta fase.
