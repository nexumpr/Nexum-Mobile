---
project: Nexum-Mobile
unit: 170-Recuperar-Contrasena
kind: implementation-tasks
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# TASK — Solicitud de Recuperación de Contraseña (Forgot Password)

**Estado:** Fase A documental completada; todas las actividades pendientes de autorización expresa para ejecución (`Fase B`). Ver **Cierre y evidencia** al final. Revisar dependencias en `specs/README.md` antes de cualquier implementación.

**Dependencias:** [`140-Login`](../140-Login/spec.md), [`030-di-navegacion-ui`](../030-di-navegacion-ui/spec.md), [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md). **Regla:** no implementar código hasta recibir mandato explícito «implementa specs/170-Recuperar-Contrasena».

## Actividades verificables
- [ ] **N170-T01 — Crear los modelos DTO de NEXUM-E005 (`ForgotPasswordRequest`, `ForgotPasswordResponse`) y `ForgotPasswordOutcome`.**
  - **Requisito:** `M170-R02` · **Plan:** `N170-P01`.
  - **Aceptación:** Modelos inmutables en Kotlin con campo `email` y `message` neutro.
  - **Pruebas:** Pruebas unitarias de instanciación en `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** archivos creados, comandos, salida real, fecha y limitaciones.

- [ ] **N170-T02 — Ampliar `AuthApi` y `AuthRepository` con la operación de solicitud de recuperación.**
  - **Requisito:** `M170-R02`, `M170-R03` · **Plan:** `N170-P02`.
  - **Aceptación:** Método `suspend fun forgotPassword(request: ForgotPasswordRequest): ForgotPasswordResponse` en `AuthApi` y `suspend fun requestPasswordReset(email: String): AuthResult<ForgotPasswordOutcome>` en `AuthRepository`.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** diff de interfaces, salida real de compilación y limitaciones.

- [ ] **N170-T03 — Implementar simulación de recuperación en `MockAuthApi` con respuesta neutra garantizada.**
  - **Requisito:** `M170-R03`, `M170-R05` · **Plan:** `N170-P03`.
  - **Aceptación:** Retardo de 800–1200 ms, retorno `202 Accepted` idéntico para cualquier correo válido, excepción `RATE_LIMITED` ante `rate_limited@error.test` y `NETWORK` ante `net@error.test`.
  - **Pruebas:** Pruebas unitarias en `MockAuthApiTest`.
  - **Evidencia a registrar:** casos de prueba ejecutados, salida real, fecha y limitaciones.

- [ ] **N170-T04 — Construir `ForgotPasswordViewModel` MVI con validación local y control antispam.**
  - **Requisito:** `M170-R01`, `M170-R03`, `M170-R04` · **Plan:** `N170-P04`.
  - **Aceptación:** Reducer determinista con intenciones `EmailChanged`, `Submit`, `NavigateToLogin`; validación de email con `AuthValidator`; estado de carga y estado de confirmación con mensaje neutro.
  - **Pruebas:** Pruebas unitarias en `ForgotPasswordViewModelTest` cubriendo validación, éxito neutro, rate limit y red.
  - **Evidencia a registrar:** pruebas de ViewModel ejecutadas, salida real, fecha y limitaciones.

- [ ] **N170-T05 — Implementar la pantalla `ForgotPasswordScreen` en Compose y conectar navegación en `AuthFlow`.**
  - **Requisito:** `M170-R01`, `M170-R04` · **Plan:** `N170-P05`.
  - **Aceptación:** Composable accesible en Material 3 con campo de email, botón "Enviar enlace", feedback neutro tras envío, botón de retorno a Login y conexión desde `LoginScreen` en `AuthFlow`.
  - **Pruebas:** Revisión visual de componentes Compose; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** archivos de UI creados/modificados, salida real y limitaciones.

- [ ] **N170-T06 — Añadir suite completa de pruebas unitarias de recuperación de contraseña.**
  - **Requisito:** `M170-R01`…`M170-R05` · **Plan:** `N170-P06`.
  - **Aceptación:** Cobertura de validaciones locales, respuesta neutra (anti-enumeración), rate limiting (429), fallos de red y navegación de retorno.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew lintDebug`.
  - **Evidencia a registrar:** reporte de pruebas unitarias, conteo total de tests, salida real, fecha y limitaciones.

## Cierre y evidencia
- **Fase A (documental):** especificación, plan técnico y actividades creadas. **Ninguna tarea implementada ni ejecutada en esta fase.**
- **Fase B (pendiente de mandato explícito):** requerirá orden humana expresa para modificar archivos `.kt`, ejecutar pruebas Gradle y registrar evidencia en `memoria.md`.
- No alterar código de producción, no realizar push ni despliegues en esta fase.
