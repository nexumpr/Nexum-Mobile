---
project: Nexum-Mobile
unit: 200-Sesiones-Activas
kind: implementation-tasks
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# TASK — Consulta de Sesiones Activas (List Sessions)

**Estado:** Fase A documental completada; todas las actividades pendientes de autorización expresa para ejecución (`Fase B`). Ver **Cierre y evidencia** al final. Revisar dependencias en `specs/README.md` antes de cualquier implementación.

**Dependencias:** [`140-Login`](../140-Login/spec.md), [`150-Refresh-Token`](../150-Refresh-Token/spec.md), [`030-di-navegacion-ui`](../030-di-navegacion-ui/spec.md), [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md). **Regla:** no implementar código hasta recibir mandato explícito «implementa specs/200-Sesiones-Activas».

## Actividades verificables
- [ ] **N200-T01 — Crear los modelos DTO de NEXUM-E008 (`SessionItemResponse`) y dominio (`ActiveSession`).**
  - **Requisito:** `M200-R02`, `M200-R03` · **Plan:** `N200-P01`.
  - **Aceptación:** Modelos inmutables en Kotlin con campos `id`, `device`, `lastSeenAt`, `current`; sin tokens ni campos criptográficos.
  - **Pruebas:** Pruebas unitarias de instanciación y mapeo en `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** archivos creados, comandos, salida real, fecha y limitaciones.

- [ ] **N200-T02 — Ampliar `AuthApi` y `AuthRepository` con la consulta de sesiones.**
  - **Requisito:** `M200-R02` · **Plan:** `N200-P02`.
  - **Aceptación:** Método `suspend fun getSessions(accessToken: String): List<SessionItemResponse>` en `AuthApi` y `suspend fun getActiveSessions(): AuthResult<List<ActiveSession>>` en `AuthRepository`.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** diff de interfaces, salida real de compilación y limitaciones.

- [ ] **N200-T03 — Implementar simulación en `MockAuthApi` con sesiones sintéticas.**
  - **Requisito:** `M200-R03`, `M200-R05` · **Plan:** `N200-P03`.
  - **Aceptación:** Retardo de 400–800 ms, retorno de al menos 2 sesiones simuladas con identificación de sesión actual y fallo controlado ante `net@error.test`.
  - **Pruebas:** Pruebas unitarias en `MockAuthApiTest`.
  - **Evidencia a registrar:** casos de prueba ejecutados, salida real, fecha y limitaciones.

- [ ] **N200-T04 — Construir `SessionsViewModel` MVI con estados de carga, refresco y errores.**
  - **Requisito:** `M200-R01`, `M200-R04` · **Plan:** `N200-P04`.
  - **Aceptación:** Reducer determinista con intenciones `LoadSessions`, `Refresh`, `DismissError`; ordenamiento de la sesión actual al tope de la lista.
  - **Pruebas:** Pruebas unitarias en `SessionsViewModelTest` con `kotlinx-coroutines-test`.
  - **Evidencia a registrar:** pruebas de ViewModel ejecutadas, salida real, fecha y limitaciones.

- [ ] **N200-T05 — Implementar la pantalla `ActiveSessionsScreen` en Compose con Material 3.**
  - **Requisito:** `M200-R01`, `M200-R04` · **Plan:** `N200-P05`.
  - **Aceptación:** Componente Composable accesible con lista reactiva, badge "Este dispositivo" para la sesión actual, indicador de carga, pull-to-refresh y estado de error con reintento.
  - **Pruebas:** Revisión de componentes Compose; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** archivos de UI creados, salida real y limitaciones.

- [ ] **N200-T06 — Añadir suite completa de pruebas unitarias de consulta de sesiones.**
  - **Requisito:** `M200-R01`…`M200-R05` · **Plan:** `N200-P06`.
  - **Aceptación:** Cobertura de carga inicial, refresco, ausencia total de tokens en DTOs, sesión actual identificada y recuperación ante errores de red.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew lintDebug`.
  - **Evidencia a registrar:** reporte de pruebas unitarias, conteo total de tests, salida real, fecha y limitaciones.

## Cierre y evidencia
- **Fase A (documental):** especificación, plan técnico y actividades creadas. **Ninguna tarea implementada ni ejecutada en esta fase.**
- **Fase B (pendiente de mandato explícito):** requerirá orden humana expresa para modificar archivos `.kt`, ejecutar pruebas Gradle y registrar evidencia en `memoria.md`.
- No alterar código de producción, no realizar push ni despliegues en esta fase.
