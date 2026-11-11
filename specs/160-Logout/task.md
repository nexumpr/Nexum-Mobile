---
project: Nexum-Mobile
unit: 160-Logout
kind: implementation-tasks
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# TASK — Cierre de Sesión (Logout)

**Estado:** Fase A documental completada; todas las actividades pendientes de autorización expresa para ejecución (`Fase B`). Ver **Cierre y evidencia** al final. Revisar dependencias en `specs/README.md` antes de cualquier implementación.

**Dependencias:** [`140-Login`](../140-Login/spec.md), [`150-Refresh-Token`](../150-Refresh-Token/spec.md), [`030-di-navegacion-ui`](../030-di-navegacion-ui/spec.md), [`050-sesion-credenciales`](../050-sesion-credenciales/spec.md). **Regla:** no implementar código hasta recibir mandato explícito «implementa specs/160-Logout».

## Actividades verificables
- [ ] **N160-T01 — Crear el modelo DTO de NEXUM-E004 (`LogoutRequest`).**
  - **Requisito:** `M160-R02` · **Plan:** `N160-P01`.
  - **Aceptación:** Modelo `LogoutRequest(val refreshToken: String)` con tipos exactos acorde al contrato E004.
  - **Pruebas:** Pruebas unitarias de instanciación en `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** archivos creados, comandos, salida real, fecha y limitaciones.

- [ ] **N160-T02 — Ampliar `AuthApi` y `AuthRepository` con la operación de logout.**
  - **Requisito:** `M160-R02`, `M160-R03` · **Plan:** `N160-P02`.
  - **Aceptación:** Método `suspend fun logout(accessToken: String, request: LogoutRequest)` en `AuthApi` y `suspend fun logout(): AuthResult<Unit>` en `AuthRepository`.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** diff de interfaces, salida real de compilación y limitaciones.

- [ ] **N160-T03 — Implementar la simulación de logout en `MockAuthApi`.**
  - **Requisito:** `M160-R02`, `M160-R05` · **Plan:** `N160-P03`.
  - **Aceptación:** Simulación con retardo de 300–800 ms, revocación de refresh token activo en servidor simulado y respuesta `204 No Content`; fallo controlado ante token centinela `net_error_logout`.
  - **Pruebas:** Pruebas unitarias en `MockAuthApiTest`.
  - **Evidencia a registrar:** casos de prueba ejecutados, salida real, fecha y limitaciones.

- [ ] **N160-T04 — Implementar `AuthRepositoryImpl.logout()` con purga local incondicional (fail-secure).**
  - **Requisito:** `M160-R03` · **Plan:** `N160-P04`.
  - **Aceptación:** Invocación a `api.logout()` si existe sesión activa; ejecución obligatoria de `tokenStorage.clear()` en bloque de finalización; retorno exitoso para la app aun si la red falla.
  - **Pruebas:** Pruebas unitarias en `AuthRepositoryTest` cubriendo salida limpia, salida con fallo de red y salida sin sesión previa.
  - **Evidencia a registrar:** pruebas de repositorio ejecutadas, salida real, fecha y limitaciones.

- [ ] **N160-T05 — Implementar componente de diálogo de confirmación y emisión de `AuthEffect.NavigateToLogin`.**
  - **Requisito:** `M160-R01`, `M160-R04` · **Plan:** `N160-P05`.
  - **Aceptación:** Composable `LogoutConfirmDialog` en Jetpack Compose Material 3; ViewModel con estados `LogoutUiState` e intenciones `LogoutIntent`; emisión de `AuthEffect.NavigateToLogin` tras confirmación.
  - **Pruebas:** Pruebas unitarias de ViewModel y revisión de componentes Compose; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** código de UI, salida real y limitaciones.

- [ ] **N160-T06 — Añadir suite completa de pruebas unitarias de logout.**
  - **Requisito:** `M160-R01`…`M160-R05` · **Plan:** `N160-P06`.
  - **Aceptación:** Cobertura de diálogo de confirmación, revocación en mock, purga local incondicional ante éxito y error de red, y emisión de efecto de navegación.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew lintDebug`.
  - **Evidencia a registrar:** reporte de pruebas unitarias, conteo total de tests, salida real, fecha y limitaciones.

## Cierre y evidencia
- **Fase A (documental):** especificación, plan técnico y tareas creadas. **Ninguna tarea implementada ni ejecutada en esta fase.**
- **Fase B (pendiente de mandato explícito):** requerirá orden humana expresa para modificar archivos `.kt`, ejecutar pruebas Gradle y registrar evidencia en `memoria.md`.
- No alterar código de producción, no realizar push ni despliegues en esta fase.
