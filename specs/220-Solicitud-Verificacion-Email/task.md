---
project: Nexum-Mobile
unit: 220-Solicitud-Verificacion-Email
kind: implementation-tasks
version: 1.1.0
status: ready
implementation: only_on_explicit_request
---

# TASK — Solicitud de Verificación de Correo (NEXUM-E010)

**Estado:** Fase B ejecutada y verificada el 2026-11-20 con mandato humano explícito. Todas las tareas marcadas `[x]` tras validación real; evidencia en `memoria.md` §52.

**Dependencias:** [`130-Registro-Usuario`](../130-Registro-Usuario/spec.md), [`140-Login`](../140-Login/spec.md), [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md).

## Actividades verificables
- [x] **N220-T01 — Crear el DTO de respuesta de NEXUM-E010 y el modelo de dominio.**
  - **Requisito:** `M220-R02`, `M220-R03` · **Plan:** `N220-P01`.
  - **Aceptación:** `EmailVerificationResponse(status, requestId)` y `EmailVerificationOutcome(status, requestId)`. **No** crear DTO de petición con `email`.
  - **Pruebas:** parseo de `202` desde fixture sintética; `./gradlew.bat testDebugUnitTest assembleDebug --no-daemon`.
  - **Evidencia a registrar:** archivos creados, comandos, salida real, fecha y limitaciones.

- [x] **N220-T02 — Ampliar el servicio Retrofit, `AuthApi`, `AuthRepository` y su implementación.**
  - **Requisito:** `M220-R02`, `M220-R04` · **Plan:** `N220-P02`.
  - **Aceptación:** `@POST("auth/email-verification/request")` con único parámetro `@Header("Authorization")` y sin `@Body`; `EmailVerificationOutcome` devuelto por `AuthRepositoryImpl` usando el token activo de `TokenStorage`; mapeo `202` → Success, `409` → `CONFLICT`, `429` → `RATE_LIMITED`, `503` → `SERVICE_UNAVAILABLE`, `401` → flujo estándar. Sin sesión, fallo local sin red.
  - **Pruebas:** `EmailVerificationHttpContractTest`, `AuthRepositoryEmailVerificationTest`, regresión completa.
  - **Evidencia a registrar:** diff de interfaces y mapeos, salida real de compilación y limitaciones.

- [x] **N220-T03 — Implementar la simulación en `MockAuthApi`.**
  - **Requisito:** `M220-R05` · **Plan:** `N220-P03`.
  - **Aceptación:** latencia 600–1000 ms; `202` con `status = "accepted"` y UUID; cuota de 3 solicitudes (4.º intento lanza `RATE_LIMITED`); centinelas `already_verified_token` → `CONFLICT`, `ses_sandbox_503_token` → `SERVICE_UNAVAILABLE`, `net_error_token` → `NETWORK`.
  - **Pruebas:** nuevos casos en `MockAuthApiTest`.
  - **Evidencia a registrar:** casos ejecutados, salida real, fecha y limitaciones.

- [x] **N220-T04 — Construir `EmailVerificationViewModel` MVI con cooldown de 60 s.**
  - **Requisito:** `M220-R01`, `M220-R04` · **Plan:** `N220-P04`.
  - **Aceptación:** `EmailVerificationUiState` y `EmailVerificationIntent`; `202` activa `isAccepted`, guarda `requestId`, informa vigencia de 30 min y decrementa `cooldownSeconds` cada segundo desde 60; `409` marca `isEmailVerified` sin cooldown; `429` informa la cuota; `503` muestra el mensaje real sin marcar éxito ni cooldown; intents ignorados durante carga/cooldown.
  - **Pruebas:** `EmailVerificationViewModelTest` con `kotlinx-coroutines-test`.
  - **Evidencia a registrar:** pruebas ejecutadas, salida real, fecha y limitaciones.

- [x] **N220-T05 — Implementar `EmailVerificationRequestScreen` en Compose Material 3 e integrarla en Ajustes.**
  - **Requisito:** `M220-R01`, `M220-R04` · **Plan:** `N220-P05`.
  - **Aceptación:** banner/acción "Verificar mi correo" cuando `emailVerified == false`; botón deshabilitado durante carga o cooldown con texto "Reenviar en Xs"; estados de `202`, `409`, `429`, `503` y red visibles; `TokenStorage` compartido con `AuthFlow`.
  - **Pruebas:** `./gradlew.bat assembleDebug --no-daemon`.
  - **Evidencia a registrar:** archivos de UI, salida real y limitaciones.

- [x] **N220-T06 — Añadir la suite completa de pruebas unitarias de la solicitud.**
  - **Requisito:** `M220-R01`…`M220-R05` · **Plan:** `N220-P06`.
  - **Aceptación:** petición con cabecera Bearer y cuerpo vacío (`contentLength == 0`, sin JSON de email), `202` con parsing y cooldown, `409`, `429`, `503`, centinelas del mock y regresión previa.
  - **Pruebas:** `./gradlew.bat testDebugUnitTest assembleDebug --no-daemon`.
  - **Evidencia a registrar:** reporte de pruebas, conteo total de tests, salida real, fecha y limitaciones.

## Cierre y evidencia
- **Fase A (documental):** completada; sin código ejecutado.
- **Fase B (autorizada):** ejecución de las tareas anteriores; evidencia real a registrar en `memoria.md`.
- No realizar push, merge ni despliegue; no modificar unidades distintas a 220.
