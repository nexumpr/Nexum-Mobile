---
project: Nexum-Mobile
unit: 250-Editar-Perfil
kind: implementation-tasks
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# TASK — Edición de Perfil de Usuario (Update Profile)

**Estado:** Fase A documental completada; todas las actividades pendientes de autorización expresa para ejecución (`Fase B`). Ver **Cierre y evidencia** al final. Revisar dependencias en `specs/README.md` antes de cualquier implementación.

**Dependencias:** [`240-Perfil-Usuario`](../240-Perfil-Usuario/spec.md), [`140-Login`](../140-Login/spec.md), [`150-Refresh-Token`](../150-Refresh-Token/spec.md), [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md). **Regla:** no implementar código hasta recibir mandato explícito «implementa specs/250-Editar-Perfil».

## Actividades verificables
- [ ] **N250-T01 — Crear el modelo DTO de NEXUM-E013 (`UpdateProfileRequest`).**
  - **Requisito:** `M250-R03` · **Plan:** `N250-P01`.
  - **Aceptación:** Modelo inmutable en Kotlin con campo `name: String`.
  - **Pruebas:** Pruebas unitarias de instanciación en `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** archivos creados, comandos, salida real, fecha y limitaciones.

- [ ] **N250-T02 — Ampliar `AuthApi` y `AuthRepository` con la mutación de perfil.**
  - **Requisito:** `M250-R03`, `M250-R04` · **Plan:** `N250-P02`.
  - **Aceptación:** Método `suspend fun updateProfile(accessToken: String, request: UpdateProfileRequest): UserProfileResponse` en `AuthApi` y `suspend fun updateProfile(name: String): AuthResult<UserProfile>` en `AuthRepository`.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** diff de interfaces, salida real de compilación y limitaciones.

- [ ] **N250-T03 — Implementar simulación de edición en `MockAuthApi`.**
  - **Requisito:** `M250-R05` · **Plan:** `N250-P03`.
  - **Aceptación:** Retardo de 400–800 ms, actualización en memoria del nombre del usuario activo, respuesta `200 OK` con datos actualizados y error ante centinela `net_error_name`.
  - **Pruebas:** Pruebas unitarias en `MockAuthApiTest`.
  - **Evidencia a registrar:** casos de prueba ejecutados, salida real, fecha y limitaciones.

- [ ] **N250-T04 — Construir `EditProfileViewModel` MVI con validaciones locales y sincronización.**
  - **Requisito:** `M250-R01`, `M250-R02`, `M250-R04` · **Plan:** `N250-P04`.
  - **Aceptación:** Reducer con validación de longitud (2..50 caracteres), control de habilitación de botón solo si hubo cambios reales y sincronización en `TokenStorage` tras éxito.
  - **Pruebas:** Pruebas unitarias en `EditProfileViewModelTest` con `kotlinx-coroutines-test`.
  - **Evidencia a registrar:** pruebas de ViewModel ejecutadas, salida real, fecha y limitaciones.

- [ ] **N250-T05 — Implementar componente `EditProfileDialog` en Compose con Material 3.**
  - **Requisito:** `M250-R01`, `M250-R02` · **Plan:** `N250-P05`.
  - **Aceptación:** Diálogo o pantalla accesible con campo de texto, botón "Guardar cambios" con indicador de carga, botón "Cancelar" y enlace desde `ProfileScreen`.
  - **Pruebas:** Revisión de componentes Compose; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** archivos de UI creados/modificados, salida real y limitaciones.

- [ ] **N250-T06 — Añadir suite completa de pruebas unitarias de edición de perfil.**
  - **Requisito:** `M250-R01`…`M250-R05` · **Plan:** `N250-P06`.
  - **Aceptación:** Cobertura de validaciones de longitud, detección de sin cambios, actualización exitosa con propagación en `TokenStorage` y manejo de fallos de red.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew lintDebug`.
  - **Evidencia a registrar:** reporte de pruebas unitarias, conteo total de tests, salida real, fecha y limitaciones.

## Cierre y evidencia
- **Fase A (documental):** especificación, plan técnico y actividades creadas. **Ninguna tarea implementada ni ejecutada en esta fase.**
- **Fase B (pendiente de mandato explícito):** requerirá orden humana expresa para modificar archivos `.kt`, ejecutar pruebas Gradle y registrar evidencia en `memoria.md`.
- No alterar código de producción, no realizar push ni despliegues en esta fase.
