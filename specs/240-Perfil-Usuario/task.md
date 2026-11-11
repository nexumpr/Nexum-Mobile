---
project: Nexum-Mobile
unit: 240-Perfil-Usuario
kind: implementation-tasks
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# TASK — Consulta de Perfil de Usuario (Get User Profile)

**Estado:** Fase A documental completada; todas las actividades pendientes de autorización expresa para ejecución (`Fase B`). Ver **Cierre y evidencia** al final. Revisar dependencias en `specs/README.md` antes de cualquier implementación.

**Dependencias:** [`140-Login`](../140-Login/spec.md), [`150-Refresh-Token`](../150-Refresh-Token/spec.md), [`030-di-navegacion-ui`](../030-di-navegacion-ui/spec.md), [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md). **Regla:** no implementar código hasta recibir mandato explícito «implementa specs/240-Perfil-Usuario».

## Actividades verificables
- [ ] **N240-T01 — Crear los modelos DTO de NEXUM-E012 (`UserProfileResponse`) y dominio (`UserProfile`).**
  - **Requisito:** `M240-R02`, `M240-R03` · **Plan:** `N240-P01`.
  - **Aceptación:** Modelos inmutables en Kotlin con campos `id`, `name`, `email`, `emailVerified`, `createdAt` / `memberSince`.
  - **Pruebas:** Pruebas unitarias de instanciación y mapeo en `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** archivos creados, comandos, salida real, fecha y limitaciones.

- [ ] **N240-T02 — Ampliar `AuthApi` y `AuthRepository` con la consulta de perfil.**
  - **Requisito:** `M240-R02`, `M240-R03` · **Plan:** `N240-P02`.
  - **Aceptación:** Método `suspend fun getProfile(accessToken: String): UserProfileResponse` en `AuthApi` y `suspend fun getUserProfile(): AuthResult<UserProfile>` en `AuthRepository`, sin parámetros de identificación de usuario en la firma.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** diff de interfaces, salida real de compilación y limitaciones.

- [ ] **N240-T03 — Implementar la simulación de perfil en `MockAuthApi`.**
  - **Requisito:** `M240-R05` · **Plan:** `N240-P03`.
  - **Aceptación:** Retardo de 300–700 ms, retorno de datos de perfil derivados de la sesión actual, y fallo controlado ante `net@error.test`.
  - **Pruebas:** Pruebas unitarias en `MockAuthApiTest`.
  - **Evidencia a registrar:** casos de prueba ejecutados, salida real, fecha y limitaciones.

- [ ] **N240-T04 — Construir `ProfileViewModel` MVI con estados de carga, refresco y preservación offline.**
  - **Requisito:** `M240-R01`, `M240-R04`, `M240-R05` · **Plan:** `N240-P04`.
  - **Aceptación:** Reducer con intenciones `LoadProfile`, `Refresh`, `DismissError`; preservación de datos en memoria ante error de red y activación del banner si el correo no está verificado.
  - **Pruebas:** Pruebas unitarias en `ProfileViewModelTest` con `kotlinx-coroutines-test`.
  - **Evidencia a registrar:** pruebas de ViewModel ejecutadas, salida real, fecha y limitaciones.

- [ ] **N240-T05 — Implementar la pantalla `ProfileScreen` en Compose con Material 3.**
  - **Requisito:** `M240-R01`, `M240-R04` · **Plan:** `N240-P05`.
  - **Aceptación:** Composable accesible con avatar de usuario, nombre, email, badge de estado de verificación con enlace hacia reenvío y pull-to-refresh.
  - **Pruebas:** Revisión de componentes Compose; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** archivos de UI creados, salida real y limitaciones.

- [ ] **N240-T06 — Añadir suite completa de pruebas unitarias de consulta de perfil.**
  - **Requisito:** `M240-R01`…`M240-R05` · **Plan:** `N240-P06`.
  - **Aceptación:** Cobertura de carga inicial, refresco, mapeo de fecha, indicador de verificación y preservación ante fallos de red.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew lintDebug`.
  - **Evidencia a registrar:** reporte de pruebas unitarias, conteo total de tests, salida real, fecha y limitaciones.

## Cierre y evidencia
- **Fase A (documental):** especificación, plan técnico y actividades creadas. **Ninguna tarea implementada ni ejecutada en esta fase.**
- **Fase B (pendiente de mandato explícito):** requerirá orden humana expresa para modificar archivos `.kt`, ejecutar pruebas Gradle y registrar evidencia en `memoria.md`.
- No alterar código de producción, no realizar push ni despliegues en esta fase.
