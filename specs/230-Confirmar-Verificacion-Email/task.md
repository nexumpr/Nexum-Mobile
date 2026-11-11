---
project: Nexum-Mobile
unit: 230-Confirmar-Verificacion-Email
kind: implementation-tasks
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# TASK — Confirmación de Verificación de Correo (Confirm Email Verification)

**Estado:** Fase A documental completada; todas las actividades pendientes de autorización expresa para ejecución (`Fase B`). Ver **Cierre y evidencia** al final. Revisar dependencias en `specs/README.md` antes de cualquier implementación.

**Dependencias:** [`220-Solicitud-Verificacion-Email`](../220-Solicitud-Verificacion-Email/spec.md), [`140-Login`](../140-Login/spec.md), [`030-di-navegacion-ui`](../030-di-navegacion-ui/spec.md), [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md). **Regla:** no implementar código hasta recibir mandato explícito «implementa specs/230-Confirmar-Verificacion-Email».

## Actividades verificables
- [ ] **N230-T01 — Crear el modelo DTO de NEXUM-E011 (`ConfirmEmailVerificationRequest`).**
  - **Requisito:** `M230-R02` · **Plan:** `N230-P01`.
  - **Aceptación:** Modelo inmutable en Kotlin con campo `verificationToken`.
  - **Pruebas:** Pruebas unitarias de instanciación en `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** archivos creados, comandos, salida real, fecha y limitaciones.

- [ ] **N230-T02 — Ampliar `AuthApi` y `AuthRepository` con la operación de confirmación.**
  - **Requisito:** `M230-R02`, `M230-R04` · **Plan:** `N230-P02`.
  - **Aceptación:** Método `suspend fun confirmEmailVerification(request: ConfirmEmailVerificationRequest)` en `AuthApi` y `suspend fun confirmEmailVerification(token: String): AuthResult<Unit>` en `AuthRepository`.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** diff de interfaces, salida real de compilación y limitaciones.

- [ ] **N230-T03 — Implementar simulación en `MockAuthApi` con invalidación de un solo uso.**
  - **Requisito:** `M230-R03`, `M230-R05` · **Plan:** `N230-P03`.
  - **Aceptación:** Retardo de 600–1000 ms, retorno exitoso `204`, registro de token como usado para impedir reuso, rechazo de token expirado ante `expired_verification_token` y error de red ante `net_error_token`.
  - **Pruebas:** Pruebas unitarias en `MockAuthApiTest`.
  - **Evidencia a registrar:** casos de prueba ejecutados, salida real, fecha y limitaciones.

- [ ] **N230-T04 — Construir `ConfirmEmailVerificationViewModel` MVI con branching de navegación.**
  - **Requisito:** `M230-R01`, `M230-R03`, `M230-R04` · **Plan:** `N230-P04`.
  - **Aceptación:** Reducer con soporte de confirmación automática si el token viene precargado; branching hacia `NavigateToApp` si hay sesión o `NavigateToLogin` si no la hay; manejo de token expirado con enlace a reenvío.
  - **Pruebas:** Pruebas unitarias en `ConfirmEmailVerificationViewModelTest` con `kotlinx-coroutines-test`.
  - **Evidencia a registrar:** pruebas de ViewModel ejecutadas, salida real, fecha y limitaciones.

- [ ] **N230-T05 — Implementar pantalla `ConfirmEmailVerificationScreen` en Compose con Material 3.**
  - **Requisito:** `M230-R01`, `M230-R04` · **Plan:** `N230-P05`.
  - **Aceptación:** Composable accesible con indicador de progreso de validación, estado de éxito con botón contextual ("Continuar a Nexum" / "Iniciar Sesión") y estado de error con botón "Solicitar nuevo enlace".
  - **Pruebas:** Revisión de componentes Compose; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** archivos de UI creados, salida real y limitaciones.

- [ ] **N230-T06 — Añadir suite completa de pruebas unitarias de confirmación de correo.**
  - **Requisito:** `M230-R01`…`M230-R05` · **Plan:** `N230-P06`.
  - **Aceptación:** Cobertura de validación exitosa, consumo de un solo uso (rechazo de reuso), token expirado, fallos de red y branching contextual de navegación.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew lintDebug`.
  - **Evidencia a registrar:** reporte de pruebas unitarias, conteo total de tests, salida real, fecha y limitaciones.

## Cierre y evidencia
- **Fase A (documental):** especificación, plan técnico y actividades creadas. **Ninguna tarea implementada ni ejecutada en esta fase.**
- **Fase B (pendiente de mandato explícito):** requerirá orden humana expresa para modificar archivos `.kt`, ejecutar pruebas Gradle y registrar evidencia en `memoria.md`.
- No alterar código de producción, no realizar push ni despliegues en esta fase.
