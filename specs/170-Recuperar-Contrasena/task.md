---
project: Nexum-Mobile
unit: 170-Recuperar-Contrasena
kind: implementation-tasks
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# TASK — Solicitud de Recuperación de Contraseña (Forgot Password)

**Estado:** Fase B completada e implementada bajo mandato humano expreso. Trazabilidad completa con NEXUM-E005 (`specs/Nexum_Guia_Integracion_Android_E001_E011.md`). Ver **Cierre y evidencia** al final.

**Dependencias:** [`140-Login`](../140-Login/spec.md), [`030-di-navegacion-ui`](../030-di-navegacion-ui/spec.md), [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md), [`150-Refresh-Token`](../150-Refresh-Token/spec.md), [`160-Logout`](../160-Logout/spec.md).

## Actividades verificables
- [x] **N170-T01 — Crear los modelos DTO de NEXUM-E005 (`ForgotPasswordRequest`, `ForgotPasswordResponse`) y `ForgotPasswordOutcome`.**
  - **Requisito:** `M170-R02` · **Plan:** `N170-P01`.
  - **Aceptación:** Modelos inmutables en Kotlin con campo `email` y `message` neutro exacto ("Si la dirección puede recibir correos, revisa tu bandeja de entrada"), y DTOs de red con `@SerializedName` (`ForgotPasswordRequestDto`, `ForgotPasswordResponseDto`).
  - **Archivos creados/modificados:** `app/src/main/java/mx/nexum/app/auth/data/dto/AuthDtos.kt`, `app/src/main/java/mx/nexum/app/auth/data/AuthRepository.kt`.
  - **Evidencia:** Modelado de DTOs acorde al catálogo V6.0.2 y guía E005; soporte de `status = "accepted"` y `requestId`.

- [x] **N170-T02 — Ampliar `AuthApi` y `AuthRepository` con la operación de solicitud de recuperación.**
  - **Requisito:** `M170-R02`, `M170-R03` · **Plan:** `N170-P02`.
  - **Aceptación:** Método `suspend fun forgotPassword(request: ForgotPasswordRequest): ForgotPasswordResponse` en `AuthApi` (con implementación default para compatibilidad con fakes), `suspend fun requestPasswordReset(email: String): AuthResult<ForgotPasswordOutcome>` en `AuthRepository`, implementación en `AuthRepositoryImpl` normalizando email con `trim().lowercase()` y mapeando `429` con `retryAfterSeconds`.
  - **Archivos creados/modificados:** `app/src/main/java/mx/nexum/app/auth/data/AuthApi.kt`, `app/src/main/java/mx/nexum/app/auth/data/AuthRepository.kt`, `app/src/main/java/mx/nexum/app/auth/data/network/AuthApiService.kt`, `app/src/main/java/mx/nexum/app/auth/data/RetrofitAuthRepositoryApi.kt`.
  - **Evidencia:** Verificado mediante `AuthRepositoryForgotPasswordTest` con 5 pruebas unitarias exitosas.

- [x] **N170-T03 — Implementar simulación de recuperación en `MockAuthApi` con respuesta neutra garantizada.**
  - **Requisito:** `M170-R03`, `M170-R05` · **Plan:** `N170-P03`.
  - **Aceptación:** Retardo de 800–1200 ms, retorno `202 Accepted` idéntico para cualquier correo válido (`mock-req-${UUID.randomUUID()}`), excepción `RATE_LIMITED` ante `rate_limited@error.test` (con `retryAfterSeconds = 60`) y `NETWORK` ante `net@error.test`.
  - **Archivos creados/modificados:** `app/src/main/java/mx/nexum/app/auth/data/MockAuthApi.kt`, `app/src/test/java/mx/nexum/app/auth/MockAuthApiTest.kt`.
  - **Evidencia:** 4 nuevas pruebas en `MockAuthApiTest` cubriendo correos registrados, no registrados, centinela 429 y centinela de red.

- [x] **N170-T04 — Construir `ForgotPasswordViewModel` MVI con validación local y control antispam.**
  - **Requisito:** `M170-R01`, `M170-R03`, `M170-R04` · **Plan:** `N170-P04`.
  - **Aceptación:** Reducer determinista con intenciones `EmailChanged`, `Submit`, `NavigateToLogin`, `NavigateToResetPassword`; validación local con `AuthValidator.emailError`; control antispam con temporizador `cooldownSeconds` (cuenta regresiva); manejo de 429 propagando `retryAfterSeconds`; emisión de efectos `NavigateToLogin`, `NavigateToResetPassword` y `ShowSnackbar`.
  - **Archivos creados/modificados:** `app/src/main/java/mx/nexum/app/auth/ui/ForgotPasswordUiState.kt`, `app/src/main/java/mx/nexum/app/auth/ui/ForgotPasswordViewModel.kt`, `app/src/test/java/mx/nexum/app/auth/ForgotPasswordViewModelTest.kt`.
  - **Evidencia:** 9 pruebas unitarias exhaustivas en `ForgotPasswordViewModelTest`.

- [x] **N170-T05 — Implementar la pantalla `ForgotPasswordScreen` en Compose y conectar navegación en `AuthFlow`.**
  - **Requisito:** `M170-R01`, `M170-R04` · **Plan:** `N170-P05`.
  - **Aceptación:** Composable accesible en Material 3 con campo de email, botón de envío con estado de carga/cooldown, tarjeta de confirmación con mensaje neutro ("Si la dirección puede recibir correos, revisa tu bandeja de entrada") y aviso de 30 minutos de vigencia, acción para continuar a `E006` y retorno a Login. Conexión en `AuthFlow` y desde enlace de `LoginScreen`.
  - **Archivos creados/modificados:** `app/src/main/java/mx/nexum/app/auth/ui/ForgotPasswordScreen.kt`, `app/src/main/java/mx/nexum/app/auth/ui/AuthFlow.kt`, `app/src/main/java/mx/nexum/app/auth/ui/LoginViewModel.kt`, `app/src/main/java/mx/nexum/app/auth/ui/AuthIntent.kt`.
  - **Evidencia:** Integración en `AuthFlow` con `AuthScreen.ForgotPassword` y preview Compose.

- [x] **N170-T06 — Añadir suite completa de pruebas unitarias de recuperación de contraseña.**
  - **Requisito:** `M170-R01`…`M170-R05` · **Plan:** `N170-P06`.
  - **Aceptación:** Cobertura integral de validaciones locales, respuesta neutra (anti-enumeración), rate limiting (429 con `Retry-After`), fallos de red y navegación de retorno.
  - **Archivos creados/modificados:** `app/src/test/java/mx/nexum/app/auth/ForgotPasswordViewModelTest.kt`, `app/src/test/java/mx/nexum/app/auth/AuthRepositoryForgotPasswordTest.kt`, `app/src/test/java/mx/nexum/app/auth/MockAuthApiTest.kt`, `app/src/test/java/mx/nexum/app/auth/LoginViewModelTest.kt`, `app/src/test/java/mx/nexum/app/auth/RegisterViewModelTest.kt`.
  - **Evidencia:** 18 nuevas pruebas automatizadas (9 ViewModel, 5 Repositorio, 4 MockApi) sin fallos.

## Cierre y evidencia
- **Fase B (implementación completada):** Implementación completa de la unidad 170 bajo mandato humano explícito.
- **Entorno:** Windows 11 · Kotlin 2.2.10 · Jetpack Compose Material 3 · Coroutines Test.
- **Git:** Sin `git push`, sin merges a ramas remotas; todos los cambios preservados estrictamente en el entorno local.
