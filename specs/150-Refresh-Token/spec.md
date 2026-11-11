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
| **M150-R01** | **Contrato y Payload E003:** El cliente envía exactamente `refreshToken` rotativo (`RefreshRequest(refreshToken)`). Ante éxito (`200 OK`), recibe `accessToken`, nuevo `refreshToken` y `expiresIn` (`RefreshResponse`). | Modelado de DTOs acorde al catálogo V6.0.2 y pruebas de serialización/mapeo. |
| **M150-R02** | **Rotación Obligatoria de Refresh Token:** Cada operación de refresh exitosa debe invalidar el refresh token previamente utilizado y guardar de forma atómica el nuevo par (`accessToken`, `refreshToken`) en `TokenStorage`. | Pruebas de sustitución atómica de tokens en el almacenamiento de sesión. |
| **M150-R03** | **Detección de Reutilización y Revocación:** Si el refresh token presentado ya fue revocado, expiró o se detecta un intento de reutilización ilegítima (`401 Unauthorized`), el cliente debe purgar de inmediato la sesión local (`TokenStorage.clear()`) y emitir el efecto `AuthEffect.NavigateToLogin` para exigir reautenticación humana. | Pruebas unitarias de revocación y purga de credenciales ante token inválido/reutilizado. |
| **M150-R04** | **Aislamiento de Conectividad y Errores de Red:** Ante errores de transporte, caída de red o errores de infraestructura (`5xx`), el cliente **no debe purgar la sesión**; debe conservar el token actual y reportar un error transitorio reintentable, preservando la experiencia offline del usuario (MO-PR08). | Pruebas que verifiquen que un fallo de red no destruye la sesión guardada. |
| **M150-R05** | **Control de Concurrencia (Single-Flight Refresh):** Si múltiples operaciones asíncronas detectan simultáneamente la necesidad de renovar token, solo se debe disparar **una única petición de refresh** a la red; las demás llamadas concurrentes deben pausarse y esperar el nuevo token sin disparar renovaciones duplicadas que invalidarían el token por rotación. | Pruebas de sincronización concurrente con corrutinas y `Mutex`. |

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
