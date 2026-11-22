---
project: Nexum-Mobile
unit: 210-Revocar-Sesion
kind: implementation-tasks
version: 1.1.0
status: completed
implementation: verified
---

# TASK — Revocación de Sesión Remota (Revoke Session)

**Estado:** Fase B completada y verificada. Todas las actividades ejecutadas bajo autorización explícita humana e integradas conforme al contrato de producción NEXUM-E009 (`DELETE /api/v1/auth/sessions/{sessionId}`). Ver **Cierre y evidencia** al final.

**Dependencias:** [`200-Sesiones-Activas`](../200-Sesiones-Activas/spec.md), [`140-Login`](../140-Login/spec.md), [`160-Logout`](../160-Logout/spec.md), [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md).

## Actividades verificables
- [x] **N210-T01 — Ampliar contratos en `AuthApi` y `AuthRepository` con la operación `revokeSession`.**
  - **Requisito:** `M210-R02` · **Plan:** `N210-P01`.
  - **Aceptación:** Método `suspend fun revokeSession(accessToken: String, sessionId: String)` en `AuthApi` y `suspend fun revokeSession(sessionId: String, isCurrent: Boolean): AuthResult<Unit>` en `AuthRepository`. Declaración en `AuthApiService` con `@DELETE("auth/sessions/{sessionId}")` retornando `Response<Unit>` para tolerar `204 No Content` sin deserializar JSON vacío.
  - **Pruebas:** Pruebas unitarias en `AuthRepositoryRevokeTest`; `./gradlew testDebugUnitTest assembleDebug --no-daemon` en verde.
  - **Evidencia a registrar:** `AuthApi.kt`, `AuthApiService.kt`, `AuthRepository.kt`, `RetrofitAuthRepositoryApi.kt`.

- [x] **N210-T02 — Implementar la simulación de revocación en `MockAuthApi`.**
  - **Requisito:** `M210-R03`, `M210-R05` · **Plan:** `N210-P02`.
  - **Aceptación:** Retardo determinista de 400–800 ms, remoción inmediata de la sesión de `mockSessionsList` en memoria para que llamadas subsiguientes a `getSessions()` (E008) ya no la devuelvan; soporte de revocaciones idempotentes (`204`), fallo `404` ante `not_found_session`, fallo `409` ante `conflict_session` y fallo reintentable ante `net_error_session`.
  - **Pruebas:** 5 pruebas unitarias específicas en `MockAuthApiTest`.
  - **Evidencia a registrar:** `MockAuthApi.kt`, `MockAuthApiTest.kt`.

- [x] **N210-T03 — Implementar la lógica en `AuthRepositoryImpl` con purga si es la sesión actual.**
  - **Requisito:** `M210-R03`, `M210-R04` · **Plan:** `N210-P03`.
  - **Aceptación:** Validación local de formato UUID en `sessionId` (rechazando con 400 VALIDATION si no cumple). Invocación a `api.revokeSession()`; si `isCurrent == true`, se ejecuta obligatoriamente `tokenStorage.clear()` y se emite `AuthEffect.NavigateToLogin`; si `isCurrent == false`, la sesión local se mantiene activa.
  - **Pruebas:** 6 pruebas unitarias en `AuthRepositoryRevokeTest`.
  - **Evidencia a registrar:** `AuthRepository.kt`, `AuthRepositoryRevokeTest.kt`.

- [x] **N210-T04 — Ampliar `SessionsViewModel` con intenciones de revocación y actualización reactiva.**
  - **Requisito:** `M210-R01`, `M210-R04` · **Plan:** `N210-P04`.
  - **Aceptación:** Manejo de `RequestRevoke(session)`, `ConfirmRevoke`, `DismissRevokeDialog`; si `session.isCurrent == true` emite `NavigateToLogin` sin llamar a E008; si `session.isCurrent == false` emite Snackbar y recarga E008 (`getSessions()`); si responde 404 recarga E008 e informa; si responde 409 permite reintentar.
  - **Pruebas:** 6 pruebas unitarias en `SessionsViewModelRevokeTest` con `kotlinx-coroutines-test`.
  - **Evidencia a registrar:** `SessionsUiState.kt`, `SessionsViewModel.kt`, `SessionsViewModelRevokeTest.kt`.

- [x] **N210-T05 — Implementar diálogo Composable de confirmación de revocación en `ActiveSessionsScreen`.**
  - **Requisito:** `M210-R01`, `M210-R04` · **Plan:** `N210-P05`.
  - **Aceptación:** Diálogo accesible en Material 3 (`AlertDialog`) advirtiendo expresamente si se cerrará la sesión actual en el dispositivo en uso o si se revocará el acceso en un dispositivo remoto. Botón "Cerrar sesión" en cada tarjeta de sesión con indicador de progreso durante la revocación.
  - **Pruebas:** Revisión y preview de componentes Compose; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** `ActiveSessionsScreen.kt`.

- [x] **N210-T06 — Añadir suite completa de pruebas unitarias de revocación de sesiones.**
  - **Requisito:** `M210-R01`…`M210-R05` · **Plan:** `N210-P06`.
  - **Aceptación:** 17 pruebas unitarias nuevas cubriendo repositorio (6), ViewModel (6) y MockAuthApi (5), elevando el total del proyecto a 275 tests unitarios con 100% de éxito.
  - **Pruebas:** `./gradlew.bat testDebugUnitTest assembleDebug --no-daemon` ejecutado exitosamente con 275/275 tests en verde.
  - **Evidencia a registrar:** Reporte HTML de pruebas unitarias, 275 tests exitosos, 0 fallos.

## Cierre y evidencia
- **Fase B (implementación técnica):** Todas las tareas ejecutadas y verificadas localmente sin acciones en GitHub remoto ni merges.
- **Resultados:** 275 tests completados exitosamente en Gradle (`BUILD SUCCESSFUL in 6m 36s`).
