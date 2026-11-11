---
project: Nexum-Mobile
unit: 150-Refresh-Token
kind: implementation-tasks
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# TASK — Renovación y Rotación de Tokens de Sesión (Refresh Token)

**Estado:** Fase A documental completada; todas las tareas pendientes de autorización explícita para ejecución (`Fase B`). Ver **Cierre y evidencia** al final. Revisar dependencias en `specs/README.md` antes de cualquier implementación.

**Dependencias:** [`140-Login`](../140-Login/spec.md), [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md), [`050-sesion-credenciales`](../050-sesion-credenciales/spec.md). **Regla:** no implementar código hasta recibir mandato explícito «implementa specs/150-Refresh-Token».

## Actividades verificables
- [ ] **N150-T01 — Crear los modelos DTO de NEXUM-E003 y tipos de resultado.**
  - **Requisito:** `M150-R01` · **Plan:** `N150-P01`.
  - **Aceptación:** Modelos `RefreshRequest` y `RefreshResponse` con tipos exactos acorde al contrato E003; `RefreshOutcome` con `Session`.
  - **Pruebas:** Pruebas unitarias de instanciación e inmutabilidad en `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** archivos creados, comandos, salida real, fecha y limitaciones.

- [ ] **N150-T02 — Ampliar `AuthApi` y `AuthRepository` con la operación de renovación de sesión.**
  - **Requisito:** `M150-R01`, `M150-R02` · **Plan:** `N150-P02`.
  - **Aceptación:** Métodos `refresh(request: RefreshRequest)` en `AuthApi` y `refreshSession(): AuthResult<RefreshOutcome>` en `AuthRepository`.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** diff de interfaces, salida real de compilación y limitaciones.

- [ ] **N150-T03 — Implementar la simulación en `MockAuthApi` con rotación estricta y detección de reuso.**
  - **Requisito:** `M150-R02`, `M150-R03`, `M150-R05` · **Plan:** `N150-P03`.
  - **Aceptación:** Generación de nuevo token de refresh por llamada, invalidación del anterior, detección de token ya utilizado (401), token expirado y fallo de red con tokens centinela.
  - **Pruebas:** Pruebas unitarias en `MockAuthApiTest` cubriendo éxito, rotación, reuso, expiración y red.
  - **Evidencia a registrar:** casos de prueba ejecutados, salida real, fecha y limitaciones.

- [ ] **N150-T04 — Implementar la lógica de renovación con `Mutex` y actualización en `TokenStorage`.**
  - **Requisito:** `M150-R02`, `M150-R04`, `M150-R05` · **Plan:** `N150-P04`.
  - **Aceptación:** `AuthRepositoryImpl.refreshSession()` protegido por `Mutex` (single-flight); actualización atómica de sesión en éxito; purga de `TokenStorage` ante 401; preservación de sesión ante errores de red.
  - **Pruebas:** Pruebas unitarias en `AuthRepositoryTest` simulando peticiones concurrentes y fallos selectivos.
  - **Evidencia a registrar:** pruebas concurrentes, salida real, fecha y limitaciones.

- [ ] **N150-T05 — Integrar emisión de efecto de sesión revocada hacia `AuthFlow`.**
  - **Requisito:** `M150-R03` · **Plan:** `N150-P05`.
  - **Aceptación:** Cuando `refreshSession()` retorne fallo de credenciales, emitir `AuthEffect.NavigateToLogin` para devolver al usuario a la pantalla de Login sin dejar rastro de sesión inválida.
  - **Pruebas:** Pruebas de integración de flujo con ViewModel / observador de sesión; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** flujo verificado, salida real y limitaciones.

- [ ] **N150-T06 — Añadir suite completa de pruebas unitarias de renovación de tokens.**
  - **Requisito:** `M150-R01`…`M150-R05` · **Plan:** `N150-P06`.
  - **Aceptación:** Cobertura de todos los caminos de éxito, rotación, detección de reuso, sesión purgada ante 401, error de red reintentable y llamadas simultáneas sin colisión de rotación.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew lintDebug`.
  - **Evidencia a registrar:** reporte de tests con conteo total, salida real, fecha y limitaciones.

## Cierre y evidencia
- **Fase A (documental):** especificación, plan técnico y actividades creadas. **Ninguna tarea implementada ni ejecutada en esta fase.**
- **Fase B (pendiente de mandato explícito):** requerirá orden humana expresa para modificar archivos `.kt`, ejecutar pruebas Gradle y registrar evidencia en `memoria.md`.
- No alterar código de producción, no realizar push ni despliegues en esta fase.
