---
project: Nexum-Mobile
unit: 230-Confirmar-Verificacion-Email
kind: technical-spec
version: 1.1.0
status: ready
implementation: only_on_explicit_request
---

# SPEC — Confirmación de Verificación de Correo (Confirm Email Verification)

## Propósito
Permitir al usuario confirmar la titularidad de su correo pegando manualmente el código recibido por E010 bajo el contrato **NEXUM-E011** (`POST /api/v1/auth/email-verification/confirm`, `[S]`, catálogo V6.0.2). El endpoint es público, usa el campo `token`, responde `200 OK` con `status` y `requestId`, y el consumo del código es de un solo uso en el backend. El cliente conserva el token literal (sin `trim()` ni cambios de mayúsculas), no lo registra en logs y actualiza solo el estado visual `emailVerified = true`; la fuente de verdad del servidor se sincroniza en el siguiente inicio de sesión E002.

## Lugar en el plan SDD
- **Constitución:** [`.specify/memory/constitution.md`](../../.specify/memory/constitution.md) (vigente: MO-PR04 Backend como fuente autoritativa, MO-PR06 Privacidad y seguridad desde el diseño, MO-PR10 Cero secretos, MO-PR15 Seguridad de sesión responsable).
- **Tecnología:** Kotlin / Jetpack Compose / MVI (Android nativo).
- **Contrato:** NEXUM-E011 del catálogo de endpoints de referencia V6.0.2 (`POST /api/v1/auth/email-verification/confirm` [S]) y [`specs/Nexum_Guia_Integracion_Android_E001_E011.md`](../Nexum_Guia_Integracion_Android_E001_E011.md) §13.
- **Dependencias previas:** [`220-Solicitud-Verificacion-Email`](../220-Solicitud-Verificacion-Email/spec.md) (entrega del código), [`140-Login`](../140-Login/spec.md) (navegación sin sesión y refresco de `user.emailVerified`) y [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md) (cliente HTTP).
- **Naturaleza:** especificación técnica de seguridad de identidad, confirmación de titularidad y soporte (`[S]`).

## Alcance permitido
DTOs `ConfirmEmailVerificationRequest(token)` y `ConfirmEmailVerificationResponse(status, requestId)`; modelo `ConfirmEmailVerificationOutcome`; ampliación de `AuthApiService`, `AuthApi`, `AuthRepository` e `AuthRepositoryImpl`; simulación en `MockAuthApi` con conjunto de tokens usados; `ConfirmEmailVerificationViewModel` MVI y `ConfirmEmailVerificationScreen` en Compose Material 3; botón de acceso desde E010, integración en `AuthFlow`/`MainAppShell`; y pruebas unitarias de la unidad.

## Exclusiones explícitas
No implementar deep links ni `intent-filter` (fuera del alcance aprobado para esta unidad); no enviar correos ni reenviar códigos desde el móvil (unidad `220`); no inventar `GET /me`; no conectar con backend real en esta etapa; no imprimir ni adjuntar el token en logs, excepciones o mensajes; no publicar ni desplegar.

## Contrato HTTP (NEXUM-E011)
| Elemento | Valor obligatorio |
| --- | --- |
| Petición | `POST /api/v1/auth/email-verification/confirm` (Retrofit: `auth/email-verification/confirm`) |
| Autenticación | Pública: **sin cabecera `Authorization`** |
| Cuerpo | `{ "token": "<CODIGO_RECIBIDO>" }`; campo exacto `token`, sin `verificationToken` |
| Token | Obligatorio, longitud mínima 10; sensible a mayúsculas y espacios; prohibido `.trim()`, `.lowercase()` o cualquier transformación |
| `200 OK` | `{ "status": "accepted", "requestId": "<uuid>" }` → `AuthResult.Success(ConfirmEmailVerificationOutcome)` |
| `400 Bad Request` | Código inválido, malformado o expirado → `INVALID_TOKEN`, no reintentable |
| `409 Conflict` | Código ya utilizado o confirmación concurrente → `CONFLICT`, no reintentable |
| Red / `5xx` | `NETWORK` / `SERVER_UNAVAILABLE`, reintentable según corresponda |

## Requisitos y criterios verificables
| ID | Requisito obligatorio | Aceptación mínima |
| --- | --- | --- |
| **M230-R01** | **Pantalla de confirmación manual:** campo para pegar el código recibido desde E010, botón "Confirmar verificación", indicador de carga, estados de éxito/error y enlaces contextuales a Login o a solicitud de reenvío. | Composable Material 3 integrado en `AuthFlow`; entrada conservada literalmente; botón deshabilitado con token corto o durante carga. |
| **M230-R02** | **Contrato E011 estricto:** `POST` público sin `Authorization`, body `{"token":"..."}`, `200` con `status`/`requestId`. | Prueba con `FixtureHttpSimulator`: ruta, cabeceras, JSON exacto con espacios/mayúsculas y parsing de `200`; DTOs y repositorio sin `verificationToken` ni `204`. |
| **M230-R03** | **Mapeo de errores y un solo uso:** `400` → `INVALID_TOKEN`, `409` → `CONFLICT`, red/5xx → reintentable; la UI distingue código expirado/inválido de código ya usado y ofrece "Solicitar nuevo código". | Pruebas de repositorio, ViewModel y simulador; mensajes sin exponer el token. |
| **M230-R04** | **Navegación contextual y estado visual:** tras `200`, si hay sesión activa marca `emailVerified = true` en la sesión local y emite `AuthEffect.NavigateToApp`; si no hay sesión emite `AuthEffect.NavigateToLogin`. El backend se sincroniza en E002, no mediante `GET /me`. | Pruebas de branching según `TokenStorage.current()` y de actualización condicional de la sesión. |
| **M230-R05** | **Simulación:** latencia 600–1000 ms, `200` con `status = "accepted"` y UUID, registro del token en `usedVerificationTokens` para rechazar reuso con `409`, centinelas `expired_verification_token`, `invalid_verification_token` y `net_error_token`. | Pruebas en `MockAuthApiTest` de éxito, reuso, centinelas y latencia. |

## Escenarios de prueba y evidencia
- **Favorable con sesión:** usuario pega token válido desde Ajustes → `POST` sin Bearer → `200` → `isConfirmed = true`, `requestId` visible, `emailVerified = true` local y `NavigateToApp`.
- **Favorable sin sesión:** token válido sin sesión → `200` → confirmación visible y `NavigateToLogin`; no se crea sesión local.
- **Token literal:** token con espacios iniciales/finales y mayúsculas se envía sin normalizar y se registra como cadena distinta en el simulador.
- **400:** token corto, expirado o inválido → `INVALID_TOKEN`, sin reintento automático, opción de solicitar nuevo código.
- **409:** token ya consumido → estado `isTokenAlreadyUsed`, distinto del expirado.
- **Fallo de red:** `net_error_token` → `NETWORK` reintentable, Snackbar sin filtrar el token.
- **Regresión y evidencia:** `./gradlew.bat testDebugUnitTest assembleDebug --no-daemon` y `./gradlew.bat lintDebug --no-daemon`; registro real en `memoria.md` con entorno, comandos, resultados y trazabilidad requisito → plan → tarea.

## Riesgos y bloqueos
- La invalidación atómica y la persistencia definitiva de `emailVerified = true` ocurren en el backend en Go; el móvil solo refleja el estado visual inmediato y lo consolida en el siguiente login E002.
- No existe `GET /me` en el contrato vigente; cualquier sincronización posterior debe resolverse en E002, no inventando un endpoint.
- El deep link quedó fuera del alcance aprobado; el acceso se realiza por el botón incorporado en E010.

## Criterio de salida de la unidad
Todas las tareas en `task.md` ejecutadas y verificadas, evidencia registrada en `memoria.md`, sin cambios fuera del alcance autorizado, sin push ni despliegue.
