---
project: Nexum-Mobile
unit: 150-Refresh-Token
kind: implementation-tasks
version: 2.0.0
status: completed
implementation: verified
---

# TASK — Renovación y Rotación de Tokens de Sesión (Refresh Token)

**Estado:** Fase B implementada y verificada bajo mandato humano explícito. 6/6 tareas completadas `[x]`. Ver **Cierre y evidencia** al final.

**Dependencias:** [`140-Login`](../140-Login/spec.md), [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md), [`050-sesion-credenciales`](../050-sesion-credenciales/spec.md).

## Actividades verificables
- [x] **N150-T01 — Crear los modelos DTO de NEXUM-E003 y tipos de resultado.**
  - **Requisito:** `M150-R01` · **Plan:** `N150-P01`.
  - **Aceptación:** Modelos `RefreshRequest` (`refreshToken`, `deviceId`), `RefreshResponse` (`accessToken`, `refreshToken`, `tokenType`, `expiresIn`, `sessionId`, `session: SessionIdDto`) y `RefreshOutcome` con `Session`.
  - **Pruebas:** Pruebas unitarias de instanciación, deserialización y soporte dual de `sessionId` y `session.id` en `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia registrada:** Modificado `app/src/main/java/mx/nexum/app/auth/data/dto/AuthDtos.kt` con DTOs de dominio y red (`RefreshRequestDto`, `RefreshResponseDto`), propiedad calculada `resolvedSessionId`. Pruebas en `AuthRepositoryRefreshTest.serializacionDualSoportaSessionIdPlanoYObjetoSessionAnidado`.

- [x] **N150-T02 — Ampliar `AuthApi` y `AuthRepository`, cliente OkHttp aislado y `TokenAuthenticator`.**
  - **Requisito:** `M150-R01`, `M150-R02`, `M150-R05` · **Plan:** `N150-P02`.
  - **Aceptación:** Métodos `refresh(request: RefreshRequest)` en `AuthApi`, `refreshSession(): AuthResult<RefreshOutcome>` en `AuthRepository`, cliente OkHttp dedicado en `NetworkModule` para auth pública (sin recursión), y `TokenAuthenticator` en peticiones protegidas con límite de 1 reintento y reutilización de token recién renovado.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia registrada:** Actualizados `AuthApi.kt`, `AuthRepository.kt`, `AuthApiService.kt`, `RetrofitAuthRepositoryApi.kt`, `NetworkModule.kt`. Creado `app/src/main/java/mx/nexum/app/auth/data/network/TokenAuthenticator.kt`. Pruebas en `TokenAuthenticatorTest` (5 tests en verde).

- [x] **N150-T03 — Implementar la simulación en `MockAuthApi` con rotación estricta, detección de replay (409) y revocación de familia (401).**
  - **Requisito:** `M150-R02`, `M150-R03`, `M150-R04` · **Plan:** `N150-P03`.
  - **Aceptación:** Generación de nuevo token rotado por llamada, invalidación del anterior, detección de token ya consumido retornando `409` (y revocación de familia posterior con `401`), `401` ante expirado, `503` ante centinela de servicio no disponible y error de red ante centinela de red.
  - **Pruebas:** Pruebas unitarias en `MockAuthApiTest` cubriendo éxito, rotación, replay 409, revocación de familia 401, 503 y red.
  - **Evidencia registrada:** Actualizado `app/src/main/java/mx/nexum/app/auth/data/MockAuthApi.kt` con gestión de familias de tokens, tokens consumidos y centinelas. 7 pruebas unitarias añadidas a `MockAuthApiTest` (18 tests totales en verde).

- [x] **N150-T04 — Implementar la lógica de renovación con Single-Flight Real y actualización atómica en `TokenStorage`.**
  - **Requisito:** `M150-R02`, `M150-R04`, `M150-R05` · **Plan:** `N150-P04`.
  - **Aceptación:** `AuthRepositoryImpl.refreshSession()` protegido por single-flight real (`CompletableDeferred` + `Mutex`); llamadas concurrentes comparten la misma ejecución HTTP sin duplicar llamadas; actualización atómica de sesión en éxito; purga de `TokenStorage` ante 401 o 409; preservación de sesión ante 503 o errores de red.
  - **Pruebas:** Pruebas unitarias en `AuthRepositoryRefreshTest` simulando peticiones concurrentes y fallos selectivos.
  - **Evidencia registrada:** Implementado en `AuthRepositoryImpl` con `CompletableDeferred` y `Mutex`. Pruebas en `AuthRepositoryRefreshTest` (8 tests en verde, incluyendo concurrencia con 5 corrutinas simultáneas resultando en exactamente 1 llamada HTTP).

- [x] **N150-T05 — Integrar emisión de efecto de sesión revocada hacia `AuthFlow` (`AuthEffect.NavigateToLogin`).**
  - **Requisito:** `M150-R03` · **Plan:** `N150-P05`.
  - **Aceptación:** Cuando `refreshSession()` retorne fallo de credenciales o conflicto (401/409), emitir `AuthEffect.NavigateToLogin` mediante `sessionEffects` para devolver al usuario a la pantalla de Login sin dejar rastro de sesión inválida.
  - **Pruebas:** Pruebas de integración de flujo con ViewModel / observador de sesión; `./gradlew assembleDebug`.
  - **Evidencia registrada:** `AuthFlow.kt` suscrito a `repository.sessionEffects` mediante corrutina dedicada en `LaunchedEffect`. Validación unitaria en `AuthRepositoryRefreshTest` ante 401 y 409 comprobando emisión de `AuthEffect.NavigateToLogin`.

- [x] **N150-T06 — Añadir suite completa de pruebas unitarias de renovación de tokens.**
  - **Requisito:** `M150-R01`…`M150-R05` · **Plan:** `N150-P06`.
  - **Aceptación:** Cobertura de todos los caminos de éxito, rotación, detección de replay 409, familia revocada 401, 503 sin purga de sesión, concurrencia real single-flight (múltiples corrutinas = 1 llamada HTTP) y `TokenAuthenticator` OkHttp con límite de 1 reintento.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew lintDebug`.
  - **Evidencia registrada:** Ejecutados 151 tests unitarios totales (20 nuevos atribuibles a esta unidad: 7 en `MockAuthApiTest`, 8 en `AuthRepositoryRefreshTest`, 5 en `TokenAuthenticatorTest`). 0 fallos, 0 errores. `lintDebug` y `assembleDebug` en verde.

## Cierre y evidencia
- **Fase A (documental):** completada previamente (2026-11-10).
- **Fase B (implementación y pruebas):** completada (2026-11-19). Mandato humano explícito ejecutado.
- Se implementaron 3 archivos nuevos (`TokenAuthenticator.kt`, `AuthRepositoryRefreshTest.kt`, `TokenAuthenticatorTest.kt`) y se actualizaron 8 archivos existentes de producción/pruebas.
- `./gradlew testDebugUnitTest` → **BUILD SUCCESSFUL** (151 tests, 0 failures, 0 errors).
- `./gradlew lintDebug` → **BUILD SUCCESSFUL** (0 errors).
- `./gradlew assembleDebug` → **BUILD SUCCESSFUL** (APK generado).
- Sin `git push`, sin `git merge`, sin dependencias externas fuera del catálogo oficial.
