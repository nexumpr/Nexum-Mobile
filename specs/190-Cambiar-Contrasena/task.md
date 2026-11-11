---
project: Nexum-Mobile
unit: 190-Cambiar-Contrasena
kind: implementation-tasks
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# TASK — Cambio de Contraseña Autenticado (Change Password)

**Estado:** Fase A documental completada; todas las actividades pendientes de autorización expresa para ejecución (`Fase B`). Ver **Cierre y evidencia** al final. Revisar dependencias en `specs/README.md` antes de cualquier implementación.

**Dependencias:** [`140-Login`](../140-Login/spec.md), [`150-Refresh-Token`](../150-Refresh-Token/spec.md), [`030-di-navegacion-ui`](../030-di-navegacion-ui/spec.md), [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md). **Regla:** no implementar código hasta recibir mandato explícito «implementa specs/190-Cambiar-Contrasena».

## Actividades verificables
- [ ] **N190-T01 — Crear el modelo DTO de NEXUM-E007 (`ChangePasswordRequest`).**
  - **Requisito:** `M190-R03` · **Plan:** `N190-P01`.
  - **Aceptación:** Modelo inmutable en Kotlin con campos `currentPassword` y `newPassword`.
  - **Pruebas:** Pruebas unitarias de instanciación en `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** archivos creados, comandos, salida real, fecha y limitaciones.

- [ ] **N190-T02 — Ampliar `AuthApi` y `AuthRepository` con la operación de cambio autenticado.**
  - **Requisito:** `M190-R03`, `M190-R04` · **Plan:** `N190-P02`.
  - **Aceptación:** Método `suspend fun changePassword(accessToken: String, request: ChangePasswordRequest)` en `AuthApi` y `suspend fun changePassword(currentPassword: String, newPassword: String): AuthResult<Unit>` en `AuthRepository`.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** diff de interfaces, salida real de compilación y limitaciones.

- [ ] **N190-T03 — Implementar simulación en `MockAuthApi` con verificación de contraseña actual.**
  - **Requisito:** `M190-R04`, `M190-R05` · **Plan:** `N190-P03`.
  - **Aceptación:** Retardo de 600–1000 ms, verificación de contraseña actual simulada, retorno exitoso `204`, rechazo ante contraseña actual errónea y fallo de red con `net@error.test`.
  - **Pruebas:** Pruebas unitarias en `MockAuthApiTest`.
  - **Evidencia a registrar:** casos de prueba ejecutados, salida real, fecha y limitaciones.

- [ ] **N190-T04 — Construir `ChangePasswordViewModel` MVI con validaciones locales y purga en memoria.**
  - **Requisito:** `M190-R01`, `M190-R02`, `M190-R04` · **Plan:** `N190-P04`.
  - **Aceptación:** Reducer con validaciones (campos no vacíos, contraseña ≥8 alfanumérica, distinta a la actual, coincidencia con confirmación), estado de carga, reseteo inmediato de campos de texto en éxito y emisión de Snackbar.
  - **Pruebas:** Pruebas unitarias en `ChangePasswordViewModelTest` con `kotlinx-coroutines-test`.
  - **Evidencia a registrar:** pruebas de ViewModel ejecutadas, salida real, fecha y limitaciones.

- [ ] **N190-T05 — Implementar la pantalla `ChangePasswordScreen` en Compose con Material 3.**
  - **Requisito:** `M190-R01`, `M190-R04` · **Plan:** `N190-P05`.
  - **Aceptación:** Formulario accesible con 3 campos de contraseña, alternadores de visibilidad, indicador de progreso en botón y mensajes de error contextuales.
  - **Pruebas:** Revisión de componentes Compose; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** archivos de UI creados, salida real y limitaciones.

- [ ] **N190-T06 — Añadir suite completa de pruebas unitarias de cambio de contraseña.**
  - **Requisito:** `M190-R01`…`M190-R05` · **Plan:** `N190-P06`.
  - **Aceptación:** Cobertura de validaciones locales, contraseña actual errónea, nueva contraseña idéntica, éxito con purga de memoria y error de red reintentable.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew lintDebug`.
  - **Evidencia a registrar:** reporte de pruebas unitarias, conteo total de tests, salida real, fecha y limitaciones.

## Cierre y evidencia
- **Fase A (documental):** especificación, plan técnico y actividades creadas. **Ninguna tarea implementada ni ejecutada en esta fase.**
- **Fase B (pendiente de mandato explícito):** requerirá orden humana expresa para modificar archivos `.kt`, ejecutar pruebas Gradle y registrar evidencia en `memoria.md`.
- No alterar código de producción, no realizar push ni despliegues en esta fase.
