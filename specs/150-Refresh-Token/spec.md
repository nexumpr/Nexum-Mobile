---
project: Nexum-Mobile
unit: 150-Refresh-Token
kind: technical-spec
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# SPEC — Renovación y Rotación de Tokens de Sesión (Refresh Token)

## Propósito
Renovación del token de acceso (`accessToken`) y rotación segura del token de actualización (`refreshToken`) bajo el contrato **NEXUM-E003** (`POST /api/v1/auth/refresh`, soporte/seguridad `[S]`, catálogo V6.0.2), manteniendo la sesión activa al reabrir la aplicación sin solicitar credenciales nuevamente, protegiendo contra la reutilización de tokens y gestionando la expiración o revocación definitiva.

## Lugar en el plan SDD
- **Constitución:** [`.specify/memory/constitution.md`](../../.specify/memory/constitution.md) (vigente: MO-PR04 Backend como fuente autoritativa, MO-PR06 Privacidad y seguridad desde el diseño, MO-PR10 Cero secretos, MO-PR15 Seguridad de sesión responsable).
- **Tecnología:** Kotlin / Jetpack Compose / MVI (Android nativo).
- **Contrato:** NEXUM-E003 del catálogo de endpoints de referencia V6.0.2 (`POST /api/v1/auth/refresh` [S]).
- **Dependencias previas:** [`140-Login`](../140-Login/spec.md) (creación de sesión inicial y `TokenStorage`), [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md) (cliente HTTP e interceptores) y [`050-sesion-credenciales`](../050-sesion-credenciales/spec.md) (custodia segura en dispositivo). Esta unidad documenta el contrato y su modelo en la capa de datos/repositorio mock sin bloquearse por dependencias no implementadas.
- **Naturaleza:** especificación técnica de seguridad de sesión y soporte (`[S]`).

## Alcance permitido
Estructuración del contrato de red DTO para E003, definición de la operación de renovación en `AuthApi` y `AuthRepository`, implementación de la simulación en `MockAuthApi` con rotación estricta y detección de reuso, actualización atómica en `TokenStorage`, manejo de concurrencia en la renovación (single-flight/Mutex), emisión del efecto de navegación forzada hacia Login (`AuthEffect.NavigateToLogin`) ante expiración/revocación, y pruebas unitarias de la unidad.

## Exclusiones explícitas
No conectarse a un backend real en esta etapa; no implementar almacenamiento cifrado definitivo (`EncryptedSharedPreferences`, unidad `050`); no implementar el interceptor de red de OkHttp/Ktor (unidad `040`); no alterar los flujos visuales de registro ni de login más allá del efecto de cierre de sesión por expiración; no implementar recuperación de contraseña; no publicar ni desplegar.

## Requisitos y criterios verificables
| ID | Requisito obligatorio | Aceptación mínima |
| --- | --- | --- |
| **M150-R01** | **Contrato y Payload E003:** El cliente envía `refreshToken` rotativo obligatorio y `deviceId` opcional (`RefreshRequest(refreshToken, deviceId)`). Ante éxito (`200 OK`), recibe `accessToken`, nuevo `refreshToken`, `tokenType`, `expiresIn`, y opcionalmente `sessionId` / `session: SessionIdDto` (`RefreshResponse`). | Modelado de DTOs acorde a la guía de integración E003 (catálogo V6.0.2) y pruebas de serialización/mapeo. |
| **M150-R02** | **Rotación Obligatoria de Refresh Token:** Cada operación de refresh exitosa (`200 OK`) debe sustituir de forma atómica el par (`accessToken`, `refreshToken`, `expiresIn`, `sessionId`) en `TokenStorage`, descartando el token previo para no reutilizarlo jamás. | Pruebas de sustitución atómica de tokens en el almacenamiento de sesión. |
| **M150-R03** | **Detección de Reutilización (409) y Revocación de Familia (401):** Si se detecta un intento de reutilización de un refresh token ya rotado (`409 Conflict`) o un token expirado/familia revocada (`401 Unauthorized`), el cliente debe invalidar de inmediato la sesión (`TokenStorage.clear()`), mapear a fallo no reintentable (`AuthErrorKind.INVALID_CREDENTIALS` / no retryable) y emitir `AuthEffect.NavigateToLogin` para exigir reautenticación humana. | Pruebas unitarias de revocación y purga de credenciales ante token inválido, expirado o reuso de token rotado. |
| **M150-R04** | **Aislamiento de Conectividad y Errores de Infraestructura (503 / Red):** Ante errores temporales como `503 Service Unavailable`, fallos de transporte o red, el cliente **no debe purgar la sesión**; debe conservar el token actual y reportar un error transitorio (`retryable = true`, `SERVER_UNAVAILABLE`/`NETWORK`), sin entrar en bucles de reintento indiscriminados (MO-PR08). | Pruebas que verifiquen que un 503 o fallo de red no destruye la sesión guardada. |
| **M150-R05** | **Control de Concurrencia (Single-Flight Real) e Integración OkHttp:** Si múltiples operaciones concurrentes requieren renovación, se debe ejecutar una sola petición HTTP en vuelo compartiendo el resultado (`CompletableDeferred` / `Mutex`); llamadas simultáneas reutilizan el mismo resultado sin disparar renovaciones duplicadas que provoquen replays accidentales. OkHttp debe aislar el cliente de refresh/login de interceptores recursivos y limitar el reintento de `Authenticator` en peticiones protegidas a máximo 1 vez. | Pruebas de sincronización concurrente real con corrutinas y prueba de `Authenticator` OkHttp. |

## Escenarios de prueba y evidencia
- **Caso favorable (Refresh normal):** sesión con token por expirar → invocación de refresh → `200` con nuevo par de tokens → `TokenStorage` actualizado con nuevo `refreshToken`.
- **Caso de expiración definitiva / 401:** token caducado o revocado → `401 Unauthorized` → `TokenStorage.clear()` → emisión de `AuthEffect.NavigateToLogin`.
- **Caso de reutilización detectada:** intento de usar un refresh token ya rotado → el backend/mock detecta reuso → respuesta `401` crítica → invalidación local inmediata de la sesión.
- **Caso de fallo de red:** error de conectividad durante el refresh → error `AuthErrorKind.NETWORK` reintentable → sesión y refresh token original preservados en `TokenStorage`.
- **Caso de concurrencia:** 3 solicitudes concurrentes de renovación → se ejecuta un solo request de red; las 3 reciben el nuevo token.
- **Regresión:** compilación de `./gradlew assembleDebug` y aprobación continua de los 50 tests de autenticación preexistentes.
- **Comandos esperados (en Fase B):** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
- **Evidencia exigida:** ejecución real, reporte de cobertura de tests, trazabilidad a requisitos y registro en `memoria.md`.

## Riesgos y bloqueos
- **Falta de Backend real:** el comportamiento de rotación y detección de reuso se modela en `MockAuthApi`; su posterior migración a la API de Go en `120-contratos-end-to-end` debe conservar esta semántica exacta.
- **Carreras de rotación:** sin sincronización por `Mutex`, dos solicitudes simultáneas invalidarían el refresh token del cliente (debido a la rotación en servidor). La implementación de un mecanismo single-flight es obligatoria.
- **Custodia en memoria:** mientras no se implemente `050-sesion-credenciales`, la sesión reside en memoria y se pierde al destruir el proceso. La interfaz `TokenStorage` aísla este cambio.

## Criterio de salida de la unidad
Todas las tareas en `task.md` documentadas y evaluables; revisión humana de la trazabilidad requisito → plan → tarea; ninguna ejecución de código en Fase A; sin cambios fuera de la unidad autorizada.
