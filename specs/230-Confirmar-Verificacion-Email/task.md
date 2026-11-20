---
project: Nexum-Mobile
unit: 230-Confirmar-Verificacion-Email
kind: implementation-tasks
version: 1.1.0
status: ready
implementation: only_on_explicit_request
---

# TASK — Confirmación de Verificación de Correo (NEXUM-E011)

**Estado:** Fase B ejecutada y verificada el 2026-11-20 con mandato humano explícito. Todas las tareas marcadas `[x]` tras validación real; evidencia en `memoria.md` §53.

**Dependencias:** [`220-Solicitud-Verificacion-Email`](../220-Solicitud-Verificacion-Email/spec.md), [`140-Login`](../140-Login/spec.md) y [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md). **Decisión vigente:** sin deep link en esta unidad; acceso manual desde E010. No se realizó push, merge ni despliegue.

## Actividades verificables
- [x] **N230-T01 — Crear los DTOs y el modelo de dominio de NEXUM-E011.**
  - **Requisito:** `M230-R02` · **Plan:** `N230-P01`.
  - **Aceptación:** `ConfirmEmailVerificationRequest(token)`, `ConfirmEmailVerificationResponse(status, requestId)` y `ConfirmEmailVerificationOutcome(status, requestId)`. Sin campo `verificationToken` ni respuesta `204`.
  - **Pruebas:** `ConfirmEmailVerificationHttpContractTest`, `AuthRepositoryConfirmEmailTest`; `./gradlew.bat testDebugUnitTest assembleDebug --no-daemon`.
  - **Evidencia registrada:** DTOs en `AuthDtos.kt`, outcome en `AuthRepository.kt`, compilación y pruebas reales en `memoria.md` §53.

- [x] **N230-T02 — Ampliar `AuthApiService`, `AuthApi`, `AuthRepository` e `AuthRepositoryImpl`.**
  - **Requisito:** `M230-R02`, `M230-R03`, `M230-R04` · **Plan:** `N230-P02`.
  - **Aceptación:** `@POST("auth/email-verification/confirm")` público con `@Body` y sin `@Header("Authorization")`; `200` → Success, `400` → `INVALID_TOKEN`, `409` → `CONFLICT`, `5xx`/red → reintentable; validación local `token.length >= 10` sin `trim()`; actualización condicional de `emailVerified` si hay sesión; token ausente en mensajes.
  - **Pruebas:** `ConfirmEmailVerificationHttpContractTest` (5), `AuthRepositoryConfirmEmailTest` (6).
  - **Evidencia registrada:** mapeos verificados contra `FixtureHttpSimulator` y repositorio falso; sin `GET /me`.

- [x] **N230-T03 — Implementar la simulación en `MockAuthApi`.**
  - **Requisito:** `M230-R03`, `M230-R05` · **Plan:** `N230-P03`.
  - **Aceptación:** latencia 600–1000 ms; `200` con `status = "accepted"` y UUID; `usedVerificationTokens` rechaza reuso con `409`; centinelas `expired_verification_token`, `invalid_verification_token` → `INVALID_TOKEN` (`400`) y `net_error_token` → `NETWORK` reintentable; espacios y mayúsculas se conservan como tokens distintos.
  - **Pruebas:** 5 casos nuevos en `MockAuthApiTest`.
  - **Evidencia registrada:** ejecución real de los centinelas, reuso y latencia en `memoria.md` §53.

- [x] **N230-T04 — Construir `ConfirmEmailVerificationViewModel` MVI con branching de navegación.**
  - **Requisito:** `M230-R01`, `M230-R03`, `M230-R04` · **Plan:** `N230-P04`.
  - **Aceptación:** `ConfirmEmailVerificationUiState` con los siete campos previstos e intents `TokenChanged`, `Submit`, `NavigateToLogin`, `NavigateToRequestVerification`; `TokenChanged` guarda el valor literal; `Submit` exige longitud ≥ 10 y no carga; `200` con sesión → `NavigateToApp`, sin sesión → `NavigateToLogin`; estados diferenciados para expirado/inválido y ya usado; red emite Snackbar reintentable.
  - **Pruebas:** 8 pruebas en `ConfirmEmailVerificationViewModelTest` con `kotlinx-coroutines-test`.
  - **Evidencia registrada:** branching y efectos verificados; collector de efectos ejecutado en `backgroundScope`.

- [x] **N230-T05 — Implementar `ConfirmEmailVerificationScreen` e integrarla en la navegación.**
  - **Requisito:** `M230-R01`, `M230-R04` · **Plan:** `N230-P05`.
  - **Aceptación:** Compose Material 3 con campo de token, botón "Confirmar verificación" deshabilitado durante carga o token corto, tarjeta de error con "Solicitar nuevo código", éxito con `requestId`, accesos a Login/reenvío, preview y ruta `AuthScreen.ConfirmEmailVerification`; botón "Ya tengo un código de verificación" en E010; `RefreshStatus` en `MainAppShell` para reflejar el cambio visual.
  - **Pruebas:** `./gradlew.bat assembleDebug --no-daemon` y compilación de la suite completa.
  - **Evidencia registrada:** APK debug generado; sin deep link ni pantallas financieras nuevas.

- [x] **N230-T06 — Añadir la suite completa de pruebas unitarias de confirmación de correo.**
  - **Requisito:** `M230-R01`…`M230-R05` · **Plan:** `N230-P06`.
  - **Aceptación:** 24 pruebas nuevas: contrato HTTP (5), repositorio (6), ViewModel (8) y simulador (5). Cobertura de body/headers, token literal, `200` con y sin sesión, `400`, `409`, red, reuso y regresión previa.
  - **Pruebas:** `./gradlew.bat testDebugUnitTest assembleDebug --no-daemon`; `./gradlew.bat lintDebug --no-daemon`.
  - **Evidencia registrada:** 329/329 tests unitarios, `BUILD SUCCESSFUL`, reporte de lint en `app/build/reports/lint-results-debug.html`.

## Cierre y evidencia
- **Fase A (documental):** completada; `spec.md`, `plan.md` y `task.md` alineados a `v1.1.0` y al §13 de la guía de integración.
- **Fase B (autorizada):** ejecución verificada de `N230-T01`…`N230-T06`; evidencia real en `memoria.md` §53.
- No alterar unidades distintas a 230; no realizar push, merge ni despliegue.
