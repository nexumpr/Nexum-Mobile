---
project: Nexum-Mobile
unit: 180-Restablecer-Contrasena
kind: implementation-tasks
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# TASK — Restablecimiento de Contraseña (Reset Password)

**Estado:** Fase A documental completada; todas las actividades pendientes de autorización expresa para ejecución (`Fase B`). Ver **Cierre y evidencia** al final. Revisar dependencias en `specs/README.md` antes de cualquier implementación.

**Dependencias:** [`170-Recuperar-Contrasena`](../170-Recuperar-Contrasena/spec.md), [`140-Login`](../140-Login/spec.md), [`030-di-navegacion-ui`](../030-di-navegacion-ui/spec.md), [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md). **Regla:** no implementar código hasta recibir mandato explícito «implementa specs/180-Restablecer-Contrasena».

## Actividades verificables
- [ ] **N180-T01 — Crear el modelo DTO de NEXUM-E006 (`ResetPasswordRequest`).**
  - **Requisito:** `M180-R02` · **Plan:** `N180-P01`.
  - **Aceptación:** Modelo inmutable en Kotlin con campos `resetToken` y `newPassword`.
  - **Pruebas:** Pruebas unitarias de instanciación en `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** archivos creados, comandos, salida real, fecha y limitaciones.

- [ ] **N180-T02 — Ampliar `AuthApi` y `AuthRepository` con la operación de restablecimiento.**
  - **Requisito:** `M180-R02`, `M180-R03` · **Plan:** `N180-P02`.
  - **Aceptación:** Método `suspend fun resetPassword(request: ResetPasswordRequest)` en `AuthApi` y `suspend fun resetPassword(token: String, newPassword: String): AuthResult<Unit>` en `AuthRepository`.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** diff de interfaces, salida real de compilación y limitaciones.

- [ ] **N180-T03 — Implementar simulación en `MockAuthApi` con invalidación de token de un solo uso.**
  - **Requisito:** `M180-R04`, `M180-R05` · **Plan:** `N180-P03`.
  - **Aceptación:** Retardo de 800–1200 ms, retorno exitoso marcando el token como usado para impedir reutilización, rechazo ante token centinela `expired_reset_token` y fallo de red con `net_error_token`.
  - **Pruebas:** Pruebas unitarias en `MockAuthApiTest`.
  - **Evidencia a registrar:** casos de prueba ejecutados, salida real, fecha y limitaciones.

- [ ] **N180-T04 — Construir `ResetPasswordViewModel` MVI con validaciones locales de contraseña.**
  - **Requisito:** `M180-R01`, `M180-R03`, `M180-R04` · **Plan:** `N180-P04`.
  - **Aceptación:** Reducer con intenciones de cambio de texto, toggle de visibilidad, validación de fortaleza y coincidencia exacta; emisión de `AuthEffect.NavigateToLogin` tras éxito y manejo de token inválido.
  - **Pruebas:** Pruebas unitarias en `ResetPasswordViewModelTest` con `kotlinx-coroutines-test`.
  - **Evidencia a registrar:** pruebas de ViewModel ejecutadas, salida real, fecha y limitaciones.

- [ ] **N180-T05 — Implementar la pantalla `ResetPasswordScreen` en Compose e integrar en `AuthFlow`.**
  - **Requisito:** `M180-R01`, `M180-R03` · **Plan:** `N180-P05`.
  - **Aceptación:** Composable accesible en Material 3 con campos para token, nueva contraseña y confirmación; botón de restablecimiento con indicador de carga; botón de retorno a Login y enlace a ForgotPassword ante token expirado.
  - **Pruebas:** Revisión de componentes Compose; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** archivos de UI creados/modificados, salida real y limitaciones.

- [ ] **N180-T06 — Añadir suite completa de pruebas unitarias de restablecimiento de contraseña.**
  - **Requisito:** `M180-R01`…`M180-R05` · **Plan:** `N180-P06`.
  - **Aceptación:** Cobertura de validaciones locales, éxito de reseteo, rechazo de token de un solo uso, token expirado, fallos de transporte y navegación forzada a Login.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew lintDebug`.
  - **Evidencia a registrar:** reporte de pruebas unitarias, conteo total de tests, salida real, fecha y limitaciones.

## Cierre y evidencia
- **Fase A (documental):** especificación, plan técnico y actividades creadas. **Ninguna tarea implementada ni ejecutada en esta fase.**
- **Fase B (pendiente de mandato explícito):** requerirá orden humana expresa para modificar archivos `.kt`, ejecutar pruebas Gradle y registrar evidencia en `memoria.md`.
- No alterar código de producción, no realizar push ni despliegues en esta fase.
