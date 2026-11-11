---
project: Nexum-Mobile
unit: 210-Revocar-Sesion
kind: implementation-tasks
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# TASK — Revocación de Sesión Remota (Revoke Session)

**Estado:** Fase A documental completada; todas las actividades pendientes de autorización expresa para ejecución (`Fase B`). Ver **Cierre y evidencia** al final. Revisar dependencias en `specs/README.md` antes de cualquier implementación.

**Dependencias:** [`200-Sesiones-Activas`](../200-Sesiones-Activas/spec.md), [`140-Login`](../140-Login/spec.md), [`160-Logout`](../160-Logout/spec.md), [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md). **Regla:** no implementar código hasta recibir mandato explícito «implementa specs/210-Revocar-Sesion».

## Actividades verificables
- [ ] **N210-T01 — Ampliar contratos en `AuthApi` y `AuthRepository` con la operación `revokeSession`.**
  - **Requisito:** `M210-R02` · **Plan:** `N210-P01`.
  - **Aceptación:** Método `suspend fun revokeSession(accessToken: String, sessionId: String)` en `AuthApi` y `suspend fun revokeSession(sessionId: String): AuthResult<Unit>` en `AuthRepository`.
  - **Pruebas:** Pruebas unitarias de interfaces en `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** diff de interfaces, salida real de compilación y limitaciones.

- [ ] **N210-T02 — Implementar la simulación de revocación en `MockAuthApi`.**
  - **Requisito:** `M210-R03`, `M210-R05` · **Plan:** `N210-P02`.
  - **Aceptación:** Retardo de 400–800 ms, remoción de la sesión en el mock, respuesta `204 No Content`, fallo controlado ante `not_found_session` y error de red ante `net@error.test`.
  - **Pruebas:** Pruebas unitarias en `MockAuthApiTest`.
  - **Evidencia a registrar:** casos de prueba ejecutados, salida real, fecha y limitaciones.

- [ ] **N210-T03 — Implementar la lógica en `AuthRepositoryImpl` con purga si es la sesión actual.**
  - **Requisito:** `M210-R03`, `M210-R04` · **Plan:** `N210-P03`.
  - **Aceptación:** Invocación a `api.revokeSession()`; si la sesión revocada coincide con el dispositivo en uso, se ejecuta `tokenStorage.clear()`.
  - **Pruebas:** Pruebas unitarias en `AuthRepositoryTest`.
  - **Evidencia a registrar:** pruebas de repositorio ejecutadas, salida real, fecha y limitaciones.

- [ ] **N210-T04 — Ampliar `SessionsViewModel` con intenciones de revocación y actualización reactiva.**
  - **Requisito:** `M210-R01`, `M210-R04` · **Plan:** `N210-P04`.
  - **Aceptación:** Manejo de `RequestRevoke`, `ConfirmRevoke`, `DismissRevokeDialog`; remoción reactiva de la sesión en el listado y emisión de Snackbar o `NavigateToLogin` si es local.
  - **Pruebas:** Pruebas unitarias en `SessionsViewModelTest` con `kotlinx-coroutines-test`.
  - **Evidencia a registrar:** pruebas de ViewModel ejecutadas, salida real, fecha y limitaciones.

- [ ] **N210-T05 — Implementar diálogo Composable de confirmación de revocación en `ActiveSessionsScreen`.**
  - **Requisito:** `M210-R01`, `M210-R04` · **Plan:** `N210-P05`.
  - **Aceptación:** Diálogo accesible en Material 3 que indica el dispositivo a revocar, botones de confirmar y cancelar, e integración con el botón "Cerrar sesión" en cada tarjeta.
  - **Pruebas:** Revisión de componentes Compose; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** archivos de UI modificados, salida real y limitaciones.

- [ ] **N210-T06 — Añadir suite completa de pruebas unitarias de revocación de sesiones.**
  - **Requisito:** `M210-R01`…`M210-R05` · **Plan:** `N210-P06`.
  - **Aceptación:** Cobertura de revocación de sesión remota, revocación de sesión actual con desautenticación, sesión no encontrada (404) y fallo de transporte.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew lintDebug`.
  - **Evidencia a registrar:** reporte de pruebas unitarias, conteo total de tests, salida real, fecha y limitaciones.

## Cierre y evidencia
- **Fase A (documental):** especificación, plan técnico y actividades creadas. **Ninguna tarea implementada ni ejecutada en esta fase.**
- **Fase B (pendiente de mandato explícito):** requerirá orden humana expresa para modificar archivos `.kt`, ejecutar pruebas Gradle y registrar evidencia en `memoria.md`.
- No alterar código de producción, no realizar push ni despliegues en esta fase.
