---
project: Nexum-Mobile
unit: 180-Restablecer-Contrasena
kind: implementation-tasks
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# TASK — Restablecimiento de Contraseña (Reset Password)

**Estado:** Fase B completada e implementada bajo mandato humano expreso. Trazabilidad completa con NEXUM-E006 (`specs/Nexum_Guia_Integracion_Android_E001_E011.md`). Ver **Cierre y evidencia** al final.

**Dependencias:** [`170-Recuperar-Contrasena`](../170-Recuperar-Contrasena/spec.md), [`140-Login`](../140-Login/spec.md), [`030-di-navegacion-ui`](../030-di-navegacion-ui/spec.md), [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md), [`150-Refresh-Token`](../150-Refresh-Token/spec.md), [`160-Logout`](../160-Logout/spec.md).

## Actividades verificables
- [x] **N180-T01 — Crear los modelos DTO de NEXUM-E006 (`ResetPasswordRequest`, `ResetPasswordResponse`) y `ResetPasswordOutcome`.**
  - **Requisito:** `M180-R02` · **Plan:** `N180-P01`.
  - **Aceptación:** Modelos inmutables en Kotlin con campos `token` y `newPassword`, respuesta con `status = "accepted"` y `requestId`, y DTOs de red con anotaciones Gson `@SerializedName` (`ResetPasswordRequestDto`, `ResetPasswordResponseDto`).
  - **Archivos creados/modificados:** `app/src/main/java/mx/nexum/app/auth/data/dto/AuthDtos.kt`, `app/src/main/java/mx/nexum/app/auth/data/AuthRepository.kt`.
  - **Evidencia:** DTOs estructurados conforme al catálogo OpenAPI V6.0.2 y guía E006.

- [x] **N180-T02 — Ampliar `AuthApi` y `AuthRepository` con la operación de restablecimiento.**
  - **Requisito:** `M180-R02`, `M180-R03` · **Plan:** `N180-P02`.
  - **Aceptación:** Método `suspend fun resetPassword(request: ResetPasswordRequest): ResetPasswordResponse` en `AuthApi`, endpoint `POST /api/v1/auth/password/reset` en `AuthApiService`, mapeo de respuestas HTTP (400, 409, 422, red) en `RetrofitAuthRepositoryApi`, y `suspend fun resetPassword(token: String, newPassword: String): AuthResult<ResetPasswordOutcome>` en `AuthRepositoryImpl` con purga incondicional de `tokenStorage.clear()` en éxito.
  - **Archivos creados/modificados:** `app/src/main/java/mx/nexum/app/auth/data/AuthApi.kt`, `app/src/main/java/mx/nexum/app/auth/data/AuthRepository.kt`, `app/src/main/java/mx/nexum/app/auth/data/network/AuthApiService.kt`, `app/src/main/java/mx/nexum/app/auth/data/RetrofitAuthRepositoryApi.kt`.
  - **Evidencia:** 6 pruebas unitarias exitosas en `AuthRepositoryResetPasswordTest`.

- [x] **N180-T03 — Implementar simulación en `MockAuthApi` con invalidación de token de un solo uso.**
  - **Requisito:** `M180-R04`, `M180-R05` · **Plan:** `N180-P03`.
  - **Aceptación:** Validación de `token.length >= 10` (400), token centinela `expired_reset_token` (400), registro en `usedResetTokens` con rechazo 409 `CONFLICT` ante reutilización, validación de contraseña de 12 a 128 caracteres (422), revocación de familias de sesión activas en memoria (cambio de época de sesión) y retorno `200 OK` con `requestId`.
  - **Archivos creados/modificados:** `app/src/main/java/mx/nexum/app/auth/data/MockAuthApi.kt`, `app/src/test/java/mx/nexum/app/auth/MockAuthApiTest.kt`.
  - **Evidencia:** 6 pruebas unitarias específicas de resetPassword añadidas en `MockAuthApiTest`.

- [x] **N180-T04 — Construir `ResetPasswordViewModel` MVI con validaciones locales de contraseña.**
  - **Requisito:** `M180-R01`, `M180-R03`, `M180-R04` · **Plan:** `N180-P04`.
  - **Aceptación:** Reducer determinista con intenciones `TokenChanged`, `PasswordChanged`, `ConfirmPasswordChanged`, `TogglePasswordVisibility`, `Submit`, `NavigateToLogin`, `NavigateToForgotPassword`; validación local con `AuthValidator` (token >= 10, clave 12..128, confirmación idéntica); manejo de errores HTTP 400 (`isTokenExpiredOrInvalid = true`), 409 (`isTokenExpiredOrInvalid = true` con mensaje de reuso), 422 (`passwordError`) y red (snackbar con reintento); emisión de `AuthEffect.NavigateToLogin` tras éxito.
  - **Archivos creados/modificados:** `app/src/main/java/mx/nexum/app/auth/ui/ResetPasswordUiState.kt`, `app/src/main/java/mx/nexum/app/auth/ui/ResetPasswordViewModel.kt`, `app/src/test/java/mx/nexum/app/auth/ResetPasswordViewModelTest.kt`.
  - **Evidencia:** 15 pruebas unitarias exhaustivas en `ResetPasswordViewModelTest`.

- [x] **N180-T05 — Implementar la pantalla `ResetPasswordScreen` en Compose e integrar en `AuthFlow`.**
  - **Requisito:** `M180-R01`, `M180-R03` · **Plan:** `N180-P05`.
  - **Aceptación:** Composable accesible en Material 3 con campos para código de recuperación, nueva contraseña y confirmación de contraseña con toggle de visibilidad; tarjeta de advertencia ante código expirado/utilizado con botón de acción para solicitar nuevo código; botón de envío con estado de carga; navegación conectada en `AuthFlow` (`AuthScreen.ResetPassword`) permitiendo precargar el email desde `ForgotPasswordScreen` y redirigir a Login tras éxito.
  - **Archivos creados/modificados:** `app/src/main/java/mx/nexum/app/auth/ui/ResetPasswordScreen.kt`, `app/src/main/java/mx/nexum/app/auth/ui/AuthFlow.kt`.
  - **Evidencia:** Integración completa en `AuthFlow` con manejo de efectos y preview Compose.

- [x] **N180-T06 — Añadir suite completa de pruebas unitarias de restablecimiento de contraseña.**
  - **Requisito:** `M180-R01`…`M180-R05` · **Plan:** `N180-P06`.
  - **Aceptación:** Cobertura de validaciones locales, éxito de reseteo, rechazo de token de un solo uso (409), token expirado (400), reglas de contraseña (422), fallos de red, purga de `TokenStorage` y navegación obligatoria a Login.
  - **Archivos creados/modificados:** `app/src/test/java/mx/nexum/app/auth/ResetPasswordViewModelTest.kt`, `app/src/test/java/mx/nexum/app/auth/AuthRepositoryResetPasswordTest.kt`, `app/src/test/java/mx/nexum/app/auth/MockAuthApiTest.kt`.
  - **Evidencia:** 27 pruebas unitarias nuevas distribuidas entre ViewModel (15), Repositorio (6) y MockApi (6).

## Cierre y evidencia
- **Fase B (implementación completada):** Implementación completa de la unidad 180 bajo mandato humano explícito.
- **Entorno:** Windows 11 · Kotlin 2.2.10 · Jetpack Compose Material 3 · Coroutines Test.
- **Git:** Sin `git push`, sin merges a ramas remotas; todos los cambios preservados estrictamente en el entorno local.
