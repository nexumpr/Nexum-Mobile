---
project: Nexum-Mobile
unit: 280-Historial-Consentimientos
kind: implementation-tasks
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# TASK — Historial de Consentimientos del Usuario (Consent History)

**Estado:** Fase A documental completada; todas las actividades pendientes de autorización expresa para ejecución (`Fase B`). Ver **Cierre y evidencia** al final. Revisar dependencias en `specs/README.md` antes de cualquier implementación.

**Dependencias:** [`270-Aviso-Privacidad-Vigente`](../270-Aviso-Privacidad-Vigente/spec.md), [`240-Perfil-Usuario`](../240-Perfil-Usuario/spec.md), [`140-Login`](../140-Login/spec.md), [`150-Refresh-Token`](../150-Refresh-Token/spec.md), [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md). **Regla:** no implementar código hasta recibir mandato explícito «implementa specs/280-Historial-Consentimientos».

## Actividades verificables
- [ ] **N280-T01 — Crear los modelos DTO de NEXUM-E016 (`ConsentItemResponse`) y dominio (`ConsentRecord`).**
  - **Requisito:** `M280-R02` · **Plan:** `N280-P01`.
  - **Aceptación:** Modelos inmutables en Kotlin: `ConsentItemResponse(noticeVersion, acceptedAt, source)` y `ConsentRecord(noticeVersion, acceptedAt, source, formattedDate)`.
  - **Pruebas:** Pruebas unitarias de serialización/deserialización en `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** archivos creados, comandos, salida real, fecha y limitaciones.

- [ ] **N280-T02 — Ampliar `AuthApi` y `AuthRepository` con la consulta de historial de consentimientos.**
  - **Requisito:** `M280-R02`, `M280-R03` · **Plan:** `N280-P02`.
  - **Aceptación:** Método `suspend fun getConsentHistory(accessToken: String): List<ConsentItemResponse>` en `AuthApi` y `suspend fun getConsentHistory(): AuthResult<List<ConsentRecord>>` en `AuthRepository`.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** diff de interfaces, salida real de compilación y limitaciones.

- [ ] **N280-T03 — Implementar simulación en `MockAuthApi`.**
  - **Requisito:** `M280-R03`, `M280-R06` · **Plan:** `N280-P03`.
  - **Aceptación:** Retardo de 300–700 ms, retorno de registros históricos inmutables basados en la sesión activa y centinela de fallo de red con `net@error.test`.
  - **Pruebas:** Pruebas unitarias en `MockAuthApiTest`.
  - **Evidencia a registrar:** casos de prueba ejecutados, salida real, fecha y limitaciones.

- [ ] **N280-T04 — Construir `ConsentHistoryViewModel` MVI con estados y formateo temporal local.**
  - **Requisito:** `M280-R04`, `M280-R05` · **Plan:** `N280-P04`.
  - **Aceptación:** Reducer con transiciones a `Loading`, `Content`, `Empty`, `Refreshing` y `Error`, formateo de timestamps UTC a huso horario local y conservación de datos ante error de refresco.
  - **Pruebas:** Pruebas unitarias en `ConsentHistoryViewModelTest` con `kotlinx-coroutines-test`.
  - **Evidencia a registrar:** pruebas de ViewModel ejecutadas, salida real, fecha y limitaciones.

- [ ] **N280-T05 — Diseñar componente Compose `ConsentHistoryScreen` con Material 3 y accesibilidad.**
  - **Requisito:** `M280-R01`, `M280-R03` · **Plan:** `N280-P05`.
  - **Aceptación:** Pantalla accesible con tarjetas de historial, banner de auditoría inmutable, badge de versión de aviso, soporte de pull-to-refresh y navegación de regreso.
  - **Pruebas:** Revisión de componentes Compose; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** archivos de UI creados/modificados, salida real y limitaciones.

- [ ] **N280-T06 — Añadir suite completa de pruebas unitarias de historial de consentimientos.**
  - **Requisito:** `M280-R01`…`M280-R06` · **Plan:** `N280-P06`.
  - **Aceptación:** Cobertura de respuesta con múltiples consentimientos, estado vacío, pull-to-refresh, formateo de zona horaria y error de transporte.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew lintDebug`.
  - **Evidencia a registrar:** reporte de pruebas unitarias, conteo total de tests, salida real, fecha y limitaciones.

## Cierre y evidencia
- **Fase A (documental):** especificación, plan técnico y actividades creadas. **Ninguna tarea implementada ni ejecutada en esta fase.**
- **Fase B (pendiente de mandato explícito):** requerirá orden humana expresa para modificar archivos `.kt`, ejecutar pruebas Gradle y registrar evidencia en `memoria.md`.
- No alterar código de producción, no realizar push ni despliegues en esta fase.
