---
project: Nexum-Mobile
unit: 220-Solicitud-Verificacion-Email
kind: technical-spec
version: 1.1.0
status: ready
implementation: only_on_explicit_request
---

# SPEC — Solicitud de Verificación de Correo (Request Email Verification)

## Propósito
Permitir a un usuario autenticado solicitar o reenviar el enlace de verificación de correo electrónico bajo el contrato **NEXUM-E010** (`POST /api/v1/auth/email-verification/request`, `[S]`, catálogo V6.0.2). El backend obtiene el destinatario del usuario autenticado, por lo que el cliente envía únicamente `Authorization: Bearer <ACCESS_TOKEN>` y **ningún cuerpo HTTP**. La respuesta `202 Accepted` incluye `status` y `requestId`; el enlace tiene 30 minutos de vigencia. La UI gestiona el cooldown antispam y distingue explícitamente `409`, `429` y `503` sin enmascarar fallos del proveedor de correo.

## Lugar en el plan SDD
- **Constitución:** [`.specify/memory/constitution.md`](../../.specify/memory/constitution.md) (vigente: MO-PR04 Backend como fuente autoritativa, MO-PR06 Privacidad y seguridad desde el diseño, MO-PR10 Cero secretos, MO-PR15 Seguridad de sesión responsable).
- **Tecnología:** Kotlin / Jetpack Compose / MVI (Android nativo).
- **Contrato:** NEXUM-E010 del catálogo de endpoints de referencia V6.0.2 (`POST /api/v1/auth/email-verification/request` [S]).
- **Dependencias previas:** [`130-Registro-Usuario`](../130-Registro-Usuario/spec.md), [`140-Login`](../140-Login/spec.md) y [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md).
- **Naturaleza:** especificación técnica de seguridad de identidad, comprobación de titularidad y soporte (`[S]`).

## Alcance permitido
DTO de respuesta `EmailVerificationResponse(status, requestId)` y modelo de dominio `EmailVerificationOutcome`; ampliación de `AuthApiService`, `AuthApi`, `AuthRepository` e `AuthRepositoryImpl`; simulación en `MockAuthApi`; `EmailVerificationViewModel` MVI con cooldown de 60 s; pantalla/banner `EmailVerificationRequestScreen` en Compose Material 3 integrada en Ajustes de cuenta; y pruebas unitarias de la unidad.

## Exclusiones explícitas
No crear ni enviar `EmailVerificationRequest` ni JSON con `email`; no validar ni consumir el token de verificación (unidad `NEXUM-E011`); no enviar correos SMTP reales desde el móvil; no reportar éxito ante `503 DEPENDENCY_UNAVAILABLE`; no conectarse a un backend real en esta etapa; no publicar ni desplegar.

## Contrato HTTP (NEXUM-E010)
| Elemento | Valor obligatorio |
| --- | --- |
| Petición | `POST /api/v1/auth/email-verification/request` |
| Cabecera | `Authorization: Bearer <ACCESS_TOKEN>` (obtenido de `TokenStorage`; sin sesión, fallo local sin red) |
| Cuerpo | Vacío (`contentLength == 0`); prohibido `{ "email": "..." }` |
| `202 Accepted` | `{ "status": "accepted", "requestId": "<uuid>" }`; enlace vigente 30 min |
| `409 Conflict` | Correo ya verificado → `CONFLICT`, no reintentable |
| `429 Too Many Requests` | Cuota de 3 solicitudes por usuario cada 15 min → `RATE_LIMITED`, reintentable |
| `503 Service Unavailable` | `DEPENDENCY_UNAVAILABLE` (proveedor AWS SES) → `SERVICE_UNAVAILABLE`, reintentable; **no enmascarar** |
| `401 Unauthorized` | Manejo estándar de sesión no autorizada |

## Requisitos y criterios verificables
| ID | Requisito obligatorio | Aceptación mínima |
| --- | --- | --- |
| **M220-R01** | **Disparador en UI y control antispam:** banner/acción "Verificar mi correo" cuando `user.emailVerified == false`; tras `202` el botón se bloquea 60 s mostrando "Reenviar en Xs". | Composable Material 3 accesible; bloqueo durante carga y cooldown; cuenta regresiva verificada en ViewModel. |
| **M220-R02** | **Contrato E010 estricto:** `POST` con Bearer del usuario autenticado y cuerpo HTTP vacío; respuesta `202` con `status` y `requestId`. | Prueba con simulador HTTP: ruta, cabecera, `contentLength == 0`, sin JSON de email, parsing de `status`/`requestId`. |
| **M220-R03** | **Sin enumeración de cuentas:** el destinatario nunca viaja en la petición; el backend responde de forma neutra. El estado `409` solo se usa para sincronizar al usuario autenticado. | Ausencia total de `email` en el body y en la interfaz de dominio; `202` muestra mensaje neutro de aceptación. |
| **M220-R04** | **Errores explícitos:** `409` marca correo verificado sin cooldown; `429` avisa de la cuota 3/15 min; `503` muestra el mensaje real del proveedor; `401` sigue el flujo de sesión. | Pruebas unitarias de `AuthErrorKind.CONFLICT`, `RATE_LIMITED`, `SERVICE_UNAVAILABLE` e `INVALID_CREDENTIALS` en repositorio y ViewModel. |
| **M220-R05** | **Simulación:** latencia 600–1000 ms, `202` con UUID, cuota de 3 (4.º intento → `RATE_LIMITED`) y centinelas `already_verified_token`, `ses_sandbox_503_token` y `net_error_token`. | Pruebas en `MockAuthApiTest` sobre éxito, cuota y centinelas. |

## Escenarios de prueba y evidencia
- **Favorable:** usuario autenticado pulsa "Verificar mi correo" → `POST` sin body → `202` → `isAccepted = true`, `requestId` guardado, mensaje con vigencia de 30 min y cooldown de 60 s.
- **Reintento durante cooldown:** botón deshabilitado; el ViewModel ignora nuevos intents.
- **409:** marca `isEmailVerified = true`, limpia error, sin cooldown de reenvío.
- **429:** aviso de límite 3/15 min, sin `isAccepted` ni cooldown.
- **503:** mensaje real `DEPENDENCY_UNAVAILABLE`, `isError = true`, sin `isAccepted` ni cooldown; permite reintentar.
- **Sin sesión:** fallo local antes de llamar a red.
- **Regresión y evidencia:** `./gradlew.bat testDebugUnitTest assembleDebug --no-daemon`; registro en `memoria.md` con entorno, comandos y resultados reales; trazabilidad requisito → plan → tarea.

## Riesgos y bloqueos
- La emisión y entrega del enlace dependen del backend; el móvil solo gestiona la petición, el cooldown y la visibilidad del error real.
- `TokenStorage` es en memoria; la integración de Ajustes comparte la instancia de `AuthFlow` para que el Bearer esté disponible.
- Dependencia con E011 para la confirmación final del correo (unidad `230`).

## Criterio de salida de la unidad
Todas las tareas en `task.md` ejecutadas y verificadas, evidencia registrada en `memoria.md`, sin cambios fuera del alcance autorizado, sin push ni despliegue.
