---
project: Nexum-Mobile
unit: 190-Cambiar-Contrasena
kind: implementation-tasks
version: 1.1.0
status: ready
implementation: only_on_explicit_request
---

# TASK — Cambio de Contraseña Autenticado (Change Password)

**Estado:** Fase B completada e implementada bajo mandato humano expreso. Trazabilidad completa con NEXUM-E007 (`specs/Nexum_Guia_Integracion_Android_E001_E011.md`, §9 y `plan.md` v1.1.0). Ver **Cierre y evidencia** al final.

**Dependencias:** [`140-Login`](../140-Login/spec.md), [`150-Refresh-Token`](../150-Refresh-Token/spec.md), [`030-di-navegacion-ui`](../030-di-navegacion-ui/spec.md), [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md), [`160-Logout`](../160-Logout/spec.md), [`170-Recuperar-Contrasena`](../170-Recuperar-Contrasena/spec.md), [`180-Restablecer-Contrasena`](../180-Restablecer-Contrasena/spec.md).

## Actividades verificables
- [x] **N190-T01 — Crear los modelos DTO de NEXUM-E007 (`ChangePasswordRequest`, `ChangePasswordResponse`).**
  - **Requisito:** `M190-R03` · **Plan:** `N190-P01`.
  - **Aceptación:** Modelos inmutables en Kotlin con campos `currentPassword` y `newPassword`, respuesta `200 OK` con `status = "accepted"` y `requestId`, y DTOs de red con anotaciones Gson `@SerializedName` (`ChangePasswordRequestDto`, `ChangePasswordResponseDto`).
  - **Archivos creados/modificados:** `app/src/main/java/mx/nexum/app/auth/data/dto/AuthDtos.kt`.
  - **Evidencia:** DTOs estructurados conforme al catálogo OpenAPI V6.0.2 y guía E007.

- [x] **N190-T02 — Ampliar `AuthApi` y `AuthRepository` con la operación de cambio autenticado.**
  - **Requisito:** `M190-R03`, `M190-R04` · **Plan:** `N190-P02`.
  - **Aceptación:** Método `suspend fun changePassword(accessToken: String, request: ChangePasswordRequest): ChangePasswordResponse` en `AuthApi`, endpoint `POST /api/v1/auth/password/change` con header `Authorization: Bearer <accessToken>` en `AuthApiService`, mapeo de respuestas HTTP (400/401, 409, 422, red) en `RetrofitAuthRepositoryApi`, y `suspend fun changePassword(currentPassword: String, newPassword: String): AuthResult<ChangePasswordResponse>` en `AuthRepositoryImpl` que extrae el `accessToken` actual de `TokenStorage` (o 401 si no hay sesión), envía la solicitud y purga incondicionalmente el almacenamiento de credenciales locales (`tokenStorage.clear()`) en éxito debido al cambio de época de sesión en backend.
  - **Archivos creados/modificados:** `app/src/main/java/mx/nexum/app/auth/data/AuthApi.kt`, `app/src/main/java/mx/nexum/app/auth/data/AuthRepository.kt`, `app/src/main/java/mx/nexum/app/auth/data/network/AuthApiService.kt`, `app/src/main/java/mx/nexum/app/auth/data/RetrofitAuthRepositoryApi.kt`.
  - **Evidencia:** 6 pruebas unitarias exitosas en `AuthRepositoryChangePasswordTest`.

- [x] **N190-T03 — Implementar simulación en `MockAuthApi` con verificación de contraseña actual y revocación.**
  - **Requisito:** `M190-R04`, `M190-R05` · **Plan:** `N190-P03`.
  - **Aceptación:** Retardo de 600–1000 ms, verificación de contraseña actual simulada contra credenciales activas del usuario (401 si no coincide), validación de nueva contraseña de al menos 12 caracteres (422), rechazo si la nueva es idéntica a la actual (409), invalidación del `accessToken` previo agregándolo a `revokedAccessTokens` y marcando la familia de tokens revocada (`isRevoked = true`), asegurando que llamadas subsecuentes con ese token devuelvan `401`.
  - **Archivos creados/modificados:** `app/src/main/java/mx/nexum/app/auth/data/MockAuthApi.kt`, `app/src/test/java/mx/nexum/app/auth/MockAuthApiTest.kt`.
  - **Evidencia:** 6 pruebas unitarias específicas de changePassword añadidas en `MockAuthApiTest`.

- [x] **N190-T04 — Construir `ChangePasswordViewModel` MVI con validaciones locales y purga en memoria.**
  - **Requisito:** `M190-R01`, `M190-R02`, `M190-R04` · **Plan:** `N190-P04`.
  - **Aceptación:** Reducer determinista con intenciones `CurrentPasswordChanged`, `NewPasswordChanged`, `ConfirmNewPasswordChanged`, alternadores de visibilidad independientes, `Submit` y `NavigateBack`. Validaciones locales previas al envío: `currentPassword` no vacía, `newPassword` con mínimo 12 caracteres y fortaleza (`AuthValidator.isStrongPassword`), `newPassword != currentPassword` y `newPassword == confirmNewPassword`. En éxito: purga inmediata de contraseñas de la memoria del ViewModel (`currentPassword = ""`, `newPassword = ""`, `confirmNewPassword = ""`), emisión de `AuthEffect.ShowSnackbar` y redirección obligatoria a `AuthEffect.NavigateToLogin`. Manejo de errores 401, 409, 422 y red con reintento.
  - **Archivos creados/modificados:** `app/src/main/java/mx/nexum/app/auth/ui/ChangePasswordUiState.kt`, `app/src/main/java/mx/nexum/app/auth/ui/ChangePasswordViewModel.kt`, `app/src/test/java/mx/nexum/app/auth/ChangePasswordViewModelTest.kt`, `app/src/main/java/mx/nexum/app/auth/domain/AuthValidator.kt`.
  - **Evidencia:** 15 pruebas unitarias exhaustivas en `ChangePasswordViewModelTest`.

- [x] **N190-T05 — Implementar la pantalla `ChangePasswordScreen` en Compose con Material 3.**
  - **Requisito:** `M190-R01`, `M190-R04` · **Plan:** `N190-P05`.
  - **Aceptación:** Formulario accesible con 3 campos de contraseña con alternadores independientes de visibilidad, indicador de progreso en botón durante carga, mensajes de error contextuales y botón para cancelar y volver. Integración en `AuthFlow` (`AuthScreen.ChangePassword`) accesible desde `AppPlaceholder` y con redirección limpia al stack de Login tras éxito.
  - **Archivos creados/modificados:** `app/src/main/java/mx/nexum/app/auth/ui/ChangePasswordScreen.kt`, `app/src/main/java/mx/nexum/app/auth/ui/AuthFlow.kt`, `app/src/main/java/mx/nexum/app/auth/ui/AuthIntent.kt`.
  - **Evidencia:** Integración completa en `AuthFlow` y preview Compose.

- [x] **N190-T06 — Añadir suite completa de pruebas unitarias de cambio de contraseña.**
  - **Requisito:** `M190-R01`…`M190-R05` · **Plan:** `N190-P06`.
  - **Aceptación:** Cobertura de validaciones locales, contraseña actual errónea (401), nueva contraseña menor a 12 caracteres (422), nueva contraseña idéntica a la actual (409), éxito con purga de memoria, borrado de tokens de sesión y redirección obligatoria a Login.
  - **Archivos creados/modificados:** `app/src/test/java/mx/nexum/app/auth/ChangePasswordViewModelTest.kt`, `app/src/test/java/mx/nexum/app/auth/AuthRepositoryChangePasswordTest.kt`, `app/src/test/java/mx/nexum/app/auth/MockAuthApiTest.kt`.
  - **Evidencia:** 27 pruebas unitarias nuevas distribuidas entre ViewModel (15), Repositorio (6) y MockApi (6).

## Cierre y evidencia
- **Fase B (implementación completada):** Implementación completa de la unidad 190 bajo mandato humano explícito.
- **Entorno:** Windows 11 · Kotlin 2.2.10 · Jetpack Compose Material 3 · Coroutines Test.
- **Git:** Sin acciones en GitHub ni Git (`git add`, `git commit`, `git push`, PRs); todos los cambios preservados estrictamente en el entorno local.
