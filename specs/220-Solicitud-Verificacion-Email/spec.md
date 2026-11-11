---
project: Nexum-Mobile
unit: 220-Solicitud-Verificacion-Email
kind: technical-spec
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# SPEC — Solicitud de Verificación de Correo (Request Email Verification)

## Propósito
Permitir a un usuario solicitar o reenviar el enlace de verificación de correo electrónico bajo el contrato **NEXUM-E00010** (`POST /api/v1/auth/email-verification/request`, soporte y seguridad `[S]`, catálogo V6.0.2), implementando protección estricta contra la enumeración de cuentas (respuesta neutra `202 Accepted`), temporizador antispam de reenvío y soporte tanto para el flujo posterior al alta como para usuarios con sesión activa no verificada.

## Lugar en el plan SDD
- **Constitución:** [`.specify/memory/constitution.md`](../../.specify/memory/constitution.md) (vigente: MO-PR04 Backend como fuente autoritativa, MO-PR06 Privacidad y seguridad desde el diseño: "no enumerar cuentas ni exponer datos personales", MO-PR10 Cero secretos, MO-PR15 Seguridad de sesión responsable).
- **Tecnología:** Kotlin / Jetpack Compose / MVI (Android nativo).
- **Contrato:** NEXUM-E010 del catálogo de endpoints de referencia V6.0.2 (`POST /api/v1/auth/email-verification/request` [S]).
- **Dependencias previas:** [`130-Registro-Usuario`](../130-Registro-Usuario/spec.md) (disparador ante `verificationRequired == true`), [`140-Login`](../140-Login/spec.md) (acceso con credenciales válidas) y [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md) (cliente HTTP).
- **Naturaleza:** especificación técnica de seguridad de identidad, comprobación de titularidad y soporte (`[S]`).

## Alcance permitido
Pantalla/banner `EmailVerificationRequestScreen` en Compose (para flujo post-registro o banner en Inicio/Perfil), validación local de email si no hay sesión activa, DTOs del contrato E010 (`EmailVerificationRequest`, `EmailVerificationResponse`), ampliación de `AuthApi` y `AuthRepository`, implementación simulada en `MockAuthApi` con respuesta neutra garantizada y retardo, ViewModel MVI (`EmailVerificationViewModel`) con cooldown antispam de 60 segundos, y pruebas unitarias de la unidad.

## Exclusiones explícitas
No validar ni consumir el token de verificación (unidad `NEXUM-E011 POST /auth/email-verification/confirm`); no enviar correos SMTP reales desde el cliente móvil; no revelar en UI si un correo ya está verificado o si no existe en el sistema; no conectarse a un backend real en esta etapa; no publicar ni desplegar.

## Requisitos y criterios verificables
| ID | Requisito obligatorio | Aceptación mínima |
| --- | --- | --- |
| **M220-R01** | **Disparador en UI y Control Antispam (Cooldown):** Componente visual ("Revisa tu correo" o banner "Verifica tu cuenta") con botón "Reenviar correo de verificación". Tras cada solicitud exitosa, el botón debe deshabilitarse durante un temporizador de cuenta regresiva (cooldown de 60 s) para evitar envíos masivos. | Composable probado, botón bloqueado durante carga y en cooldown con visualización de segundos restantes. |
| **M220-R02** | **Contrato y Payload E010:** Enviar `POST /api/v1/auth/email-verification/request`. Si el usuario tiene sesión activa, enviar `Authorization: Bearer <accessToken>`; si es un alta reciente sin sesión, enviar payload `{"email": "..."}`. Ante procesamiento correcto, recibir respuesta `202 Accepted` con mensaje neutro. | Modelado de DTOs acordes al catálogo V6.0.2 y verificación de peticiones con/sin token en pruebas. |
| **M220-R03** | **Anti-Enumeración de Cuentas (MO-PR06):** La respuesta en la UI debe ser **rigurosamente idéntica** (`202 Accepted`, mensaje: *"Si la cuenta existe y requiere verificación, se ha enviado un enlace de confirmación"*), sin importar si el correo existe, si no existe o si ya fue verificado previamente. | Pruebas unitarias que confirmen que la UI no produce discrepancias observables según el estado del correo. |
| **M220-R04** | **Tratamiento de Exceso de Peticiones (429 Rate Limit):** Ante un código de error `429 Too Many Requests` proveniente del servidor, la UI debe mostrar un aviso claro en Snackbar ("Demasiadas solicitudes. Por favor espera unos minutos antes de intentar nuevamente") sin alterar el estado del usuario. | Pruebas unitarias de mapeo de `AuthErrorKind.RATE_LIMITED` en el ViewModel. |
| **M220-R05** | **Simulación en `MockAuthApi` y Repositorio:** El mock debe devolver `202 Accepted` neutro tras retardo (600–1000 ms), simular error `429` ante el centinela `rate_limited@error.test` y simular error de transporte ante `net@error.test`. | Pruebas unitarias en `MockAuthApiTest` cubriendo éxito neutro, rate limit y error de red. |

## Escenarios de prueba y evidencia
- **Caso favorable (post-registro o sesión activa):** Usuario pulsa "Reenviar correo de verificación" → llamada `POST /api/v1/auth/email-verification/request` → respuesta `202` → botón se bloquea por 60 s y se muestra mensaje neutro de confirmación.
- **Caso de reintento durante cooldown:** Botón deshabilitado mostrando cuenta regresiva ("Reenviar en 45 s") impidiendo disparar peticiones redundantes.
- **Caso de exceso de peticiones (429):** Respuesta `429` del servidor simulado → Snackbar informativo con opción de esperar.
- **Caso de fallo de transporte:** Error de red → Snackbar reintentable sin activar el temporizador de cooldown (permitiendo reintentar la conexión).
- **Regresión:** Compilación exitosa de `./gradlew assembleDebug` y aprobación continua de los 50 tests preexistentes de autenticación.
- **Comandos esperados (en Fase B):** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
- **Evidencia exigida:** ejecución real, reporte de cobertura de tests, trazabilidad a requisitos y registro en `memoria.md`.

## Riesgos y bloqueos
- **Falta de Backend real:** la emisión de enlaces con tokens de corta duración y la entrega de correos electrónicos reside exclusivamente en el servicio de Go; en el cliente móvil se garantiza la protección anti-enumeración y la gestión de la tasa de peticiones.
- **Dependencia con E011:** la confirmación final del correo depende de la especificación de `230-Confirmar-Verificacion-Email` (NEXUM-E011).

## Criterio de salida de la unidad
Todas las tareas en `task.md` documentadas en estado pendiente (`- [ ]`); trazabilidad completa requisito → plan → tarea; ninguna ejecución de código en Fase A; sin cambios fuera del alcance autorizado.
