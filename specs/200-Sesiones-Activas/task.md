---
project: Nexum-Mobile
unit: 200-Sesiones-Activas
kind: implementation-tasks
version: 1.1.0
status: completed
implementation: verified
---

# TASK — Consulta de Sesiones Activas (List Sessions)

**Estado:** Fase B completada y verificada. Todas las actividades ejecutadas bajo autorización explícita humana e integradas conforme al contrato de producción NEXUM-E008 (`GET /api/v1/auth/sessions`). Ver **Cierre y evidencia** al final.

**Dependencias:** [`140-Login`](../140-Login/spec.md), [`150-Refresh-Token`](../150-Refresh-Token/spec.md), [`030-di-navegacion-ui`](../030-di-navegacion-ui/spec.md), [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md).

## Actividades verificables
- [x] **N200-T01 — Crear los modelos DTO de NEXUM-E008 (`SessionItemResponse`, `SessionsPageResponse`) y dominio (`UserSession`, `SessionsPage`).**
  - **Requisito:** `M200-R02`, `M200-R03` · **Plan:** `N200-P01`.
  - **Aceptación:** Modelos inmutables en Kotlin con exactamente los 5 campos del contrato E008 (`id`, `createdAt`, `lastSeenAt`, `current`, `revoked`); sin datos de hardware/IP ni tokens criptográficos. DTOs de red con `@SerializedName` y modelos de dominio con timestamps formateados legibles y `shortId`.
  - **Pruebas:** Pruebas unitarias de instanciación y mapeo en `AuthRepositorySessionsTest`; `./gradlew testDebugUnitTest assembleDebug --no-daemon` en verde.
  - **Evidencia a registrar:** `AuthDtos.kt`, `AuthRepository.kt`.

- [x] **N200-T02 — Ampliar `AuthApi` y `AuthRepository` con la consulta de sesiones.**
  - **Requisito:** `M200-R02` · **Plan:** `N200-P02`.
  - **Aceptación:** Método `suspend fun getSessions(accessToken: String, limit: Int? = 25, cursor: String? = null): SessionsPageResponse` en `AuthApi`, endpoint `GET auth/sessions` en `AuthApiService`, implementación en `RetrofitAuthRepositoryApi` y `suspend fun getSessions(limit: Int = 25, cursor: String? = null): AuthResult<SessionsPage>` en `AuthRepository` y `AuthRepositoryImpl`. Ordenamiento de la sesión actual al inicio en la primera página (`cursor == null`) y purga de credenciales con emisión de `NavigateToLogin` ante error 401.
  - **Pruebas:** `AuthRepositorySessionsTest` (5 tests); `./gradlew testDebugUnitTest`.
  - **Evidencia a registrar:** `AuthApi.kt`, `AuthApiService.kt`, `RetrofitAuthRepositoryApi.kt`, `AuthRepository.kt`.

- [x] **N200-T03 — Implementar simulación en `MockAuthApi` con sesiones sintéticas.**
  - **Requisito:** `M200-R03`, `M200-R05` · **Plan:** `N200-P03`.
  - **Aceptación:** Retardo determinista de 400–800 ms, retorno de al menos 2 páginas de datos sintéticos respetando los 5 campos de E008 (`current = true` en página 1, `nextCursor` opaco, `nextCursor = null` en página final), validación estricta de `limit in 1..100` (400), validación de cursor `8..2048` y centinela `"invalid-cursor"` (400), y rechazo 401 si el token es revocado/expirado o fallo de red con `net_error_token`.
  - **Pruebas:** 6 pruebas unitarias específicas añadidas en `MockAuthApiTest`.
  - **Evidencia a registrar:** `MockAuthApi.kt`, `MockAuthApiTest.kt`.

- [x] **N200-T04 — Construir `SessionsViewModel` MVI con estados de carga, refresco y errores.**
  - **Requisito:** `M200-R01`, `M200-R04` · **Plan:** `N200-P04`.
  - **Aceptación:** Reducer reactivo con intenciones `LoadSessions`, `Refresh`, `LoadMore`, `DismissError` y `NavigateBack`; concatenación de páginas con cursor opaco; gestión de flags `isLoading`, `isRefreshing`, `isLoadingMore` y emisión del efecto `NavigateToApp`.
  - **Pruebas:** 7 pruebas unitarias en `SessionsViewModelTest` con `kotlinx-coroutines-test`.
  - **Evidencia a registrar:** `SessionsUiState.kt`, `SessionsViewModel.kt`, `SessionsViewModelTest.kt`.

- [x] **N200-T05 — Implementar la pantalla `ActiveSessionsScreen` en Compose con Material 3.**
  - **Requisito:** `M200-R01`, `M200-R04` · **Plan:** `N200-P05`.
  - **Aceptación:** Pantalla accesible Material 3 (`Aero-Futuristic`), TopAppBar con botón de retorno y botón de refresco, tarjeta para cada sesión con `shortId`, badge "Sesión actual", badge "Revocada" o "Activa", fechas legibles, botón "Cargar más sesiones" al haber cursor disponible, y estados de carga y error con reintento. Cableado con `AuthFlow.kt`, `SettingsScreen.kt` y `MainAppShell.kt`.
  - **Pruebas:** Preview Composable, compilación limpia en `./gradlew assembleDebug`.
  - **Evidencia a registrar:** `ActiveSessionsScreen.kt`, `AuthFlow.kt`, `MainAppShell.kt`, `SettingsScreen.kt`.

- [x] **N200-T06 — Añadir suite completa de pruebas unitarias de consulta de sesiones.**
  - **Requisito:** `M200-R01`…`M200-R05` · **Plan:** `N200-P06`.
  - **Aceptación:** 18 pruebas unitarias nuevas cubriendo ViewModel (7), Repositorio (5) y MockAuthApi (6), garantizando 100% éxito sin regresiones.
  - **Pruebas:** `./gradlew.bat testDebugUnitTest assembleDebug --no-daemon` ejecutado exitosamente con 258/258 tests en verde.
  - **Evidencia a registrar:** Reporte HTML de pruebas unitarias, 258 tests exitosos, 0 fallos.

## Cierre y evidencia
- **Fase B (implementación técnica):** Todas las tareas ejecutadas y verificadas localmente sin acciones en GitHub remoto ni merges.
- **Resultados:** 258 tests completados exitosamente en Gradle (`BUILD SUCCESSFUL in 1m 52s`).
