---
project: Nexum-Mobile
unit: 290-Registrar-Consentimiento
kind: implementation-tasks
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# TASK — Registro de Consentimiento del Usuario (Record Consent)

**Estado:** Fase A documental completada; todas las actividades pendientes de autorización expresa para ejecución (`Fase B`). Ver **Cierre y evidencia** al final. Revisar dependencias en `specs/README.md` antes de cualquier implementación.

**Dependencias:** [`270-Aviso-Privacidad-Vigente`](../270-Aviso-Privacidad-Vigente/spec.md), [`280-Historial-Consentimientos`](../280-Historial-Consentimientos/spec.md), [`140-Login`](../140-Login/spec.md), [`150-Refresh-Token`](../150-Refresh-Token/spec.md), [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md). **Regla:** no implementar código hasta recibir mandato explícito «implementa specs/290-Registrar-Consentimiento».

## Actividades verificables
- [ ] **N290-T01 — Crear los modelos DTO de NEXUM-E017 (`RecordConsentRequest`, `RecordConsentResponse`) y dominio (`ConsentReceipt`).**
  - **Requisito:** `M290-R02` · **Plan:** `N290-P01`.
  - **Aceptación:** Modelos inmutables en Kotlin: `RecordConsentRequest(noticeVersion, accepted)` y `RecordConsentResponse(consentId, recordedAt)`.
  - **Pruebas:** Pruebas unitarias de serialización/deserialización en `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** archivos creados, comandos, salida real, fecha y limitaciones.

- [ ] **N290-T02 — Ampliar `AuthApi` y `AuthRepository` con la operación de registro de consentimiento.**
  - **Requisito:** `M290-R02`, `M290-R03` · **Plan:** `N290-P02`.
  - **Aceptación:** Método `suspend fun recordConsent(accessToken: String, request: RecordConsentRequest, idempotencyKey: String? = null): RecordConsentResponse` en `AuthApi` y `suspend fun recordConsent(noticeVersion: String): AuthResult<ConsentReceipt>` en `AuthRepository`.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** diff de interfaces, salida real de compilación y limitaciones.

- [ ] **N290-T03 — Implementar simulación en `MockAuthApi`.**
  - **Requisito:** `M290-R03`, `M290-R06` · **Plan:** `N290-P03`.
  - **Aceptación:** Retardo de 400–800 ms, persistencia en memoria del nuevo consentimiento, detección de versiones ya registradas (`409 Conflict`), validación de `accepted = true` y centinela de fallo de red con `net_error_consent`.
  - **Pruebas:** Pruebas unitarias en `MockAuthApiTest`.
  - **Evidencia a registrar:** casos de prueba ejecutados, salida real, fecha y limitaciones.

- [ ] **N290-T04 — Construir `RecordConsentViewModel` MVI con control reactivo de envío y sincronización.**
  - **Requisito:** `M290-R01`, `M290-R03`, `M290-R04` · **Plan:** `N290-P04`.
  - **Aceptación:** Reducer con control de `canSubmit`, absorción resiliente de error 409 como éxito idempotente y propagación inmediata a la lista local de consentimientos.
  - **Pruebas:** Pruebas unitarias en `RecordConsentViewModelTest` con `kotlinx-coroutines-test`.
  - **Evidencia a registrar:** pruebas de ViewModel ejecutadas, salida real, fecha y limitaciones.

- [ ] **N290-T05 — Diseñar componente Compose `UpdateConsentDialog` con Material 3 y accesibilidad.**
  - **Requisito:** `M290-R01`, `M290-R05` · **Plan:** `N290-P05`.
  - **Aceptación:** Diálogo modal accesible con indicación de nueva versión, checkbox de confirmación, enlace al aviso completo (`270`), botón de aceptación reactivo y manejo de errores con opción de reintento.
  - **Pruebas:** Revisión de componentes Compose; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** archivos de UI creados/modificados, salida real y limitaciones.

- [ ] **N290-T06 — Añadir suite completa de pruebas unitarias de registro de consentimiento.**
  - **Requisito:** `M290-R01`…`M290-R06` · **Plan:** `N290-P06`.
  - **Aceptación:** Cobertura de registro exitoso, detección previa de versión existente, manejo de conflicto 409, control de checkbox desmarcado y resiliencia ante errores de red.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew lintDebug`.
  - **Evidencia a registrar:** reporte de pruebas unitarias, conteo total de tests, salida real, fecha y limitaciones.

## Cierre y evidencia
- **Fase A (documental):** especificación, plan técnico y actividades creadas. **Ninguna tarea implementada ni ejecutada en esta fase.**
- **Fase B (pendiente de mandato explícito):** requerirá orden humana expresa para modificar archivos `.kt`, ejecutar pruebas Gradle y registrar evidencia en `memoria.md`.
- No alterar código de producción, no realizar push ni despliegues en esta fase.
