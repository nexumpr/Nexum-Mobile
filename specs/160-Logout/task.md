---
project: Nexum-Mobile
unit: 160-Logout
kind: implementation-tasks
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# TASK — Cierre de Sesión (Logout)

**Estado:** Fase A documental completada; todas las actividades pendientes de autorización expresa para ejecución (`Fase B`). Ver **Cierre y evidencia** al final. Revisar dependencias en `specs/README.md` antes de cualquier implementación.

**Dependencias:** [`140-Login`](../140-Login/spec.md), [`150-Refresh-Token`](../150-Refresh-Token/spec.md), [`030-di-navegacion-ui`](../030-di-navegacion-ui/spec.md), [`050-sesion-credenciales`](../050-sesion-credenciales/spec.md). **Regla:** no implementar código hasta recibir mandato explícito «implementa specs/160-Logout».

## Actividades verificables
- [ ] **N160-T01 — Ajustar contratos en `AuthApi` y `AuthRepository` (sin body, sin recursión).**
  - **Requisito:** `M160-R02`, `M160-R03` · **Plan:** `N160-P01`.
  - **Aceptación:** Métodos `suspend fun logout(accessToken: String)` en `AuthApi` y `suspend fun logout(): AuthResult<Unit>` en `AuthRepository`; exclusión de logout en `TokenAuthenticator`.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** diff de interfaces, salida real de compilación y limitaciones.

- [ ] **N160-T02 — Implementar `logout` en `AuthApiService` y `RetrofitAuthRepositoryApi` con `Response<Unit>` (204).**
  - **Requisito:** `M160-R02` · **Plan:** `N160-P02`.
  - **Aceptación:** Endpoint Retrofit `POST auth/logout` con header `Authorization: Bearer <accessToken>` sin body, retornando `Response<Unit>` sin intentar deserializar JSON en `204`.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** diff de Retrofit service y repositorio, salida real de compilación y limitaciones.

- [ ] **N160-T03 — Implementar la simulación de logout en `MockAuthApi`.**
  - **Requisito:** `M160-R02`, `M160-R05` · **Plan:** `N160-P03`.
  - **Aceptación:** Simulación con retardo de 300–800 ms, revocación durable de tokens de acceso y refresh en servidor simulado (401 en llamadas posteriores), respuesta `204 No Content` o `401` si ya estaba revocado, y fallo controlado ante centinela `net_error_logout`.
  - **Pruebas:** Pruebas unitarias en `MockAuthApiTest`.
  - **Evidencia a registrar:** casos de prueba ejecutados, salida real, fecha y limitaciones.

- [ ] **N160-T04 — Implementar `AuthRepositoryImpl.logout()` con purga local incondicional (fail-secure).**
  - **Requisito:** `M160-R03` · **Plan:** `N160-P04`.
  - **Aceptación:** Invocación a `api.logout(token)` si existe sesión activa; ejecución obligatoria de `tokenStorage.clear()` y emisión de `AuthEffect.NavigateToLogin` en bloque `finally`; retorno de `AuthResult.Success(Unit)` aun si la red falla o devuelve 401.
  - **Pruebas:** Pruebas unitarias en `AuthRepositoryLogoutTest` cubriendo salida limpia, salida con fallo de red y salida sin sesión previa.
  - **Evidencia a registrar:** pruebas de repositorio ejecutadas, salida real, fecha y limitaciones.

- [ ] **N160-T05 — Implementar componente de diálogo de confirmación, ViewModel y navegación en `AuthFlow`.**
  - **Requisito:** `M160-R01`, `M160-R04` · **Plan:** `N160-P05`.
  - **Aceptación:** Composable `LogoutConfirmDialog` en Jetpack Compose Material 3 (`AlertDialog`); `LogoutViewModel` con estados `LogoutUiState` e intenciones `LogoutIntent`; botón en la UI autenticada y emisión de `AuthEffect.NavigateToLogin`.
  - **Pruebas:** Pruebas unitarias de ViewModel y revisión de componentes Compose; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** código de UI, salida real y limitaciones.

- [ ] **N160-T06 — Añadir suite completa de pruebas unitarias de logout.**
  - **Requisito:** `M160-R01`…`M160-R05` · **Plan:** `N160-P06`.
  - **Aceptación:** Cobertura de diálogo de confirmación, revocación en mock, purga local incondicional ante éxito, 401 y error de red, y emisión de efecto de navegación.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew lintDebug`.
  - **Evidencia a registrar:** reporte de pruebas unitarias, conteo total de tests, salida real, fecha y limitaciones.

## Cierre y evidencia
- **Fase A (documental):** especificación, plan técnico y tareas creadas. **Ninguna tarea implementada ni ejecutada en esta fase.**
- **Fase B (pendiente de mandato explícito):** requerirá orden humana expresa para modificar archivos `.kt`, ejecutar pruebas Gradle y registrar evidencia en `memoria.md`.
- No alterar código de producción, no realizar push ni despliegues en esta fase.
