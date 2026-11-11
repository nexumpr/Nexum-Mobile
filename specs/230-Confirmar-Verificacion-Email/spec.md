---
project: Nexum-Mobile
unit: 230-Confirmar-Verificacion-Email
kind: technical-spec
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# SPEC — Confirmación de Verificación de Correo (Confirm Email Verification)

## Propósito
Permitir la validación y consumo del token de verificación de correo recibido por el usuario bajo el contrato **NEXUM-E011** (`POST /api/v1/auth/email-verification/confirm`, soporte y seguridad `[S]`, catálogo V6.0.2), marcando la cuenta como verificada (`emailVerified = true`) antes de habilitar operaciones sensibles, con soporte para deep links, invalidación de un solo uso y navegación contextual.

## Lugar en el plan SDD
- **Constitución:** [`.specify/memory/constitution.md`](../../.specify/memory/constitution.md) (vigente: MO-PR04 Backend como fuente autoritativa, MO-PR06 Privacidad y seguridad desde el diseño, MO-PR10 Cero secretos, MO-PR15 Seguridad de sesión responsable).
- **Tecnología:** Kotlin / Jetpack Compose / MVI (Android nativo).
- **Contrato:** NEXUM-E011 del catálogo de endpoints de referencia V6.0.2 (`POST /api/v1/auth/email-verification/confirm` [S]).
- **Dependencias previas:** [`220-Solicitud-Verificacion-Email`](../220-Solicitud-Verificacion-Email/spec.md) (solicitud previa del enlace), [`140-Login`](../140-Login/spec.md) (navegación si no hay sesión), [`030-di-navegacion-ui`](../030-di-navegacion-ui/spec.md) (soporte de deep links) y [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md) (cliente HTTP).
- **Naturaleza:** especificación técnica de seguridad de identidad, confirmación de titularidad y soporte (`[S]`).

## Alcance permitido
Pantalla `ConfirmEmailVerificationScreen` en Compose (soporta recepción de `token` vía deep link o entrada manual), validación de presencia del token antes de la llamada, DTOs del contrato E011 (`ConfirmEmailVerificationRequest`), ampliación de `AuthApi` y `AuthRepository`, implementación simulada en `MockAuthApi` con invalidación de un solo uso y respuesta `204 No Content`, ViewModel MVI (`ConfirmEmailVerificationViewModel`), actualización del estado de verificación en sesión local (si aplica), redirección contextual (hacia `App` si está autenticado, o hacia `Login` si no hay sesión), y pruebas unitarias de la unidad.

## Exclusiones explícitas
No enviar correos de verificación (cubierto en la unidad `220`); no omitir la validación del token; no conectar con backend real en esta etapa; no publicar ni desplegar.

## Requisitos y criterios verificables
| ID | Requisito obligatorio | Aceptación mínima |
| --- | --- | --- |
| **M230-R01** | **Pantalla de Confirmación y Recepción de Token:** Pantalla capaz de capturar el token de verificación (precargado automáticamente mediante deep link `https://app.nexum.mx/verify-email?token=...` o ingresado en campo de texto), con botón "Confirmar verificación", estado de procesamiento y enlace a Login o reenvío ante fallo. | Composable probado con estados de procesamiento automático y formulario manual. |
| **M230-R02** | **Contrato y Payload E011:** Enviar `POST /api/v1/auth/email-verification/confirm` con payload `{"verificationToken": "..."}`. Ante éxito, recibir respuesta `204 No Content` sin cuerpo. | Modelado de DTOs acordes al catálogo V6.0.2 y verificación de contrato en pruebas. |
| **M230-R03** | **Consumo de Un Solo Uso y Expiración:** El token de verificación es estrictamente de un solo uso. Si el servidor responde error por token expirado, inválido o ya consumido (`400/404/410`), la UI debe desplegar un mensaje claro ("El enlace de verificación ha expirado o ya fue utilizado") y ofrecer un botón directo para solicitar un nuevo enlace (`220-Solicitud-Verificacion-Email`). | Pruebas unitarias que verifiquen el mapeo de error de token y la navegación hacia reenvío. |
| **M230-R04** | **Navegación Contextual y Actualización de Estado:** Tras recibir `204 No Content`: si el usuario tiene sesión activa, se marca `emailVerified = true` en el estado local y se emite `AuthEffect.NavigateToApp`; si no tiene sesión activa, se muestra confirmación de éxito y se emite `AuthEffect.NavigateToLogin`. | Pruebas de branching de navegación según el estado de `TokenStorage`. |
| **M230-R05** | **Simulación en `MockAuthApi` y Repositorio:** El mock debe responder `204 No Content` ante tokens válidos, registrar el token en un conjunto de usados para impedir un segundo uso, responder error ante el centinela `expired_verification_token` y simular error de red con `net_error_token`. | Pruebas unitarias en `MockAuthApiTest` cubriendo éxito de un solo uso, token expirado y error de red. |

## Escenarios de prueba y evidencia
- **Caso favorable (apertura por deep link):** App se abre con token válido → llamada automática a confirmación → respuesta `204` → pantalla de éxito → redirección al flujo correspondiente.
- **Caso de ingreso manual:** Usuario pega token válido → pulsa "Confirmar verificación" → respuesta `204` → confirmación afirmativa.
- **Caso de token ya utilizado o expirado:** Servidor devuelve error de token → UI muestra aviso de caducidad con botón "Solicitar nuevo enlace".
- **Caso de fallo de transporte:** Error de red → Snackbar reintentable conservando el token capturado.
- **Regresión:** Compilación exitosa de `./gradlew assembleDebug` y aprobación continua de los 50 tests preexistentes de autenticación.
- **Comandos esperados (en Fase B):** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
- **Evidencia exigida:** ejecución real, reporte de cobertura de tests, trazabilidad a requisitos y registro en `memoria.md`.

## Riesgos y bloqueos
- **Falta de Backend real:** la invalidación atómica y actualización en MongoDB Atlas (`emailVerified = true` en colección `users`) es del servidor en Go; el cliente móvil garantiza el consumo seguro del contrato y la experiencia reactiva.
- **Filtro de Deep Link:** la apertura directa desde el navegador o cliente de correo requiere la configuración de `intent-filter` en `AndroidManifest.xml` (formalizada en la unidad de navegación `030`).

## Criterio de salida de la unidad
Todas las tareas en `task.md` documentadas en estado pendiente (`- [ ]`); trazabilidad completa requisito → plan → tarea; ninguna ejecución de código en Fase A; sin cambios fuera del alcance autorizado.
