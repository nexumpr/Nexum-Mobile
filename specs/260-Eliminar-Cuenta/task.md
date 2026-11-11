---
project: Nexum-Mobile
unit: 260-Eliminar-Cuenta
kind: implementation-tasks
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# TASK — Eliminación Definitiva de Cuenta (Delete Account)

**Estado:** Fase A documental completada; todas las actividades pendientes de autorización expresa para ejecución (`Fase B`). Ver **Cierre y evidencia** al final. Revisar dependencias en `specs/README.md` antes de cualquier implementación.

**Dependencias:** [`240-Perfil-Usuario`](../240-Perfil-Usuario/spec.md), [`140-Login`](../140-Login/spec.md), [`150-Refresh-Token`](../150-Refresh-Token/spec.md), [`160-Logout`](../160-Logout/spec.md), [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md), [`050-sesion-credenciales`](../050-sesion-credenciales/spec.md). **Regla:** no implementar código hasta recibir mandato explícito «implementa specs/260-Eliminar-Cuenta».

## Actividades verificables
- [ ] **N260-T01 — Crear los modelos DTO de NEXUM-E014 (`DeleteAccountRequest`, `DeleteAccountResponse`).**
  - **Requisito:** `M260-R02` · **Plan:** `N260-P01`.
  - **Aceptación:** Modelos inmutables en Kotlin: `DeleteAccountRequest(password, confirmation)` y `DeleteAccountResponse(deletionRequestId?, status?)`.
  - **Pruebas:** Pruebas unitarias de serialización/deserialización en `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** archivos creados, comandos, salida real, fecha y limitaciones.

- [ ] **N260-T02 — Ampliar `AuthApi` y `AuthRepository` con la operación de eliminación de cuenta.**
  - **Requisito:** `M260-R02`, `M260-R03` · **Plan:** `N260-P02`.
  - **Aceptación:** Método `suspend fun deleteAccount(accessToken: String, request: DeleteAccountRequest, idempotencyKey: String? = null): DeleteAccountResponse?` en `AuthApi` y `suspend fun deleteAccount(password: String, confirmation: String): AuthResult<DeleteAccountResponse?>` en `AuthRepository`.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** diff de interfaces, salida real de compilación y limitaciones.

- [ ] **N260-T03 — Implementar simulación de eliminación en `MockAuthApi`.**
  - **Requisito:** `M260-R05`, `M260-R06` · **Plan:** `N260-P03`.
  - **Aceptación:** Retardo de 500–1000 ms, validación de contraseña y confirmación "ELIMINAR", soporte de respuesta síncrona `204`, centinela asíncrono `async_delete` (`202 Accepted`), rechazo de contraseña errónea (`401`) y centinela de red `net_error_delete`.
  - **Pruebas:** Pruebas unitarias en `MockAuthApiTest`.
  - **Evidencia a registrar:** casos de prueba ejecutados, salida real, fecha y limitaciones.

- [ ] **N260-T04 — Construir `DeleteAccountViewModel` MVI con doble confirmación, purga de sesión y aislamiento demo.**
  - **Requisito:** `M260-R01`, `M260-R03`, `M260-R04` · **Plan:** `N260-P04`.
  - **Aceptación:** Reducer con control estricto de habilitación (`password.isNotBlank() && confirmation == "ELIMINAR"`), branching transparente para modo invitado (sin peticiones HTTP), purga incondicional de `TokenStorage` tras éxito y emisión de `AuthEffect.NavigateToLogin`.
  - **Pruebas:** Pruebas unitarias en `DeleteAccountViewModelTest` con `kotlinx-coroutines-test`.
  - **Evidencia a registrar:** pruebas de ViewModel ejecutadas, salida real, fecha y limitaciones.

- [ ] **N260-T05 — Implementar componente Compose `DeleteAccountDialog` con Material 3 y advertencias de alto impacto.**
  - **Requisito:** `M260-R01`, `M260-R05` · **Plan:** `N260-P05`.
  - **Aceptación:** Diálogo modal accesible con advertencia de irreversibilidad, campos de contraseña con alternador de visibilidad, campo de confirmación textual en mayúsculas, botón destructivo habilitado reactivamente y botón de cancelación con purga de memoria.
  - **Pruebas:** Revisión de componentes Compose; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** archivos de UI creados/modificados, salida real y limitaciones.

- [ ] **N260-T06 — Añadir suite completa de pruebas unitarias de eliminación de cuenta.**
  - **Requisito:** `M260-R01`…`M260-R06` · **Plan:** `N260-P06`.
  - **Aceptación:** Cobertura exhaustiva: validación de confirmación textual, validación de contraseña, branching en modo demo, éxito síncrono 204 y asíncrono 202 con purga en `TokenStorage`, manejo de error 401 y fallas de red sin pérdida de sesión.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew lintDebug`.
  - **Evidencia a registrar:** reporte de pruebas unitarias, conteo total de tests, salida real, fecha y limitaciones.

## Cierre y evidencia
- **Fase A (documental):** especificación, plan técnico y actividades creadas. **Ninguna tarea implementada ni ejecutada en esta fase.**
- **Fase B (pendiente de mandato explícito):** requerirá orden humana expresa para modificar archivos `.kt`, ejecutar pruebas Gradle y registrar evidencia en `memoria.md`.
- No alterar código de producción, no realizar push ni despliegues en esta fase.
