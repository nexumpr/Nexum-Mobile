---
project: Nexum-Mobile
unit: 170-Recuperar-Contrasena
kind: functional-spec
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# SPEC — Solicitud de Recuperación de Contraseña (Forgot Password)

## Propósito
Permitir a los usuarios solicitar un enlace de restablecimiento de contraseña mediante su correo electrónico bajo el contrato **NEXUM-E005** (`POST /api/v1/auth/password/forgot`, extensión visible `[E]`, catálogo V6.0.2), implementando protección estricta contra la enumeración de usuarios (respuesta neutra `202 Accepted`), validación local previa y control antispam en interfaz Compose MVI.

## Lugar en el plan SDD
- **Constitución:** [`.specify/memory/constitution.md`](../../.specify/memory/constitution.md) (vigente: MO-PR04 Backend como fuente autoritativa, MO-PR06 Privacidad y seguridad desde el diseño: "prevenir exposición de datos y responder sin confiar en entradas remotas", MO-PR10 Cero secretos).
- **Tecnología:** Kotlin / Jetpack Compose / MVI (Android nativo).
- **Contrato:** NEXUM-E005 del catálogo de endpoints de referencia V6.0.2 (`POST /api/v1/auth/password/forgot` [E]).
- **Dependencias previas:** [`140-Login`](../140-Login/spec.md) (pantalla origen con enlace "¿Olvidaste tu contraseña?"), [`030-di-navegacion-ui`](../030-di-navegacion-ui/spec.md) (navegación entre pantallas de auth) y [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md) (cliente HTTP).
- **Naturaleza:** especificación funcional de extensión visible (`[E]`) de autenticación y autoservicio de cuenta.

## Alcance permitido
Pantalla `ForgotPasswordScreen` en Compose accesible desde el enlace de Login, validación local de formato de email, DTOs del contrato E005 (`ForgotPasswordRequest`, `ForgotPasswordResponse`), ampliación de `AuthApi` y `AuthRepository`, implementación simulada en `MockAuthApi` con retardo y respuesta neutra garantizada, ViewModel MVI (`ForgotPasswordViewModel`), estados de éxito/cooldown antispam y pruebas unitarias de la unidad.

## Exclusiones explícitas
No implementar el flujo de recepción y confirmación de token de restablecimiento (unidad `NEXUM-E006 /auth/password/reset`, que requiere enlace profundo o pantalla de ingreso de nueva contraseña); no revelar en UI si un correo existe o no en la base de datos; no enviar correos reales (responsabilidad exclusiva del backend); no conectar con backend real en esta etapa; no publicar ni desplegar.

## Requisitos y criterios verificables
| ID | Requisito obligatorio | Aceptación mínima |
| --- | --- | --- |
| **M170-R01** | **Pantalla de Recuperación y Validación Local:** Pantalla con campo de correo electrónico (`email`), botón "Enviar enlace de recuperación", enlace de navegación "Volver a Iniciar Sesión" e instrucciones claras. Validación local: email no vacío y conforme a `^[A-Za-z0-9+_.-]+@(.+)$` antes de disparar la petición HTTP. | Composable probado, errores visuales en campo ante formato inválido y botón bloqueado durante carga. |
| **M170-R02** | **Contrato y Payload E005:** Enviar `POST /api/v1/auth/password/forgot` con payload `{"email": "..."}`. Ante procesamiento correcto, recibir respuesta `202 Accepted` con mensaje neutro. | Modelado de DTOs acorde al catálogo V6.0.2 y verificación de serialización en pruebas. |
| **M170-R03** | **Protección contra Enumeración de Cuentas (Anti-Enumeration):** La respuesta en la UI debe ser **estrictamente idéntica** exista o no el correo en el sistema ("Si el correo está registrado, recibirás un enlace para restablecer tu contraseña"). Nunca mostrar alertas diferenciadas que revelen la existencia de la cuenta (MO-PR06). | Pruebas unitarias que verifiquen que correos existentes y no existentes devuelven el mismo estado de éxito neutro. |
| **M170-R04** | **Control Antispam y Tasa de Peticiones (Rate Limit):** Tras un envío exitoso, la interfaz debe deshabilitar temporalmente el botón con un temporizador de reintento (cooldown, ej. 60 s). Ante un código HTTP `429 Too Many Requests`, la UI debe mostrar un aviso específico de límite alcanzado con opción de reintento posterior. | Pruebas de manejo de `AuthErrorKind.RATE_LIMITED` y verificación del estado de cooldown. |
| **M170-R05** | **Simulación en `MockAuthApi` y Repositorio:** El repositorio y la API mock deben retornar `202 Accepted` con mensaje neutro para cualquier correo válido, simular retardo de red (800–1200 ms), responder con `429` ante el correo centinela `rate_limited@error.test` y simular error de red con `net@error.test`. | Pruebas unitarias de repositorio y mock cubriendo éxito neutro, rate limit y falla de transporte. |

## Escenarios de prueba y evidencia
- **Caso favorable (correo registrado o no registrado):** Formato válido → llamada `POST /api/v1/auth/password/forgot` → respuesta `202` → estado `isSubmitted = true` con mensaje neutro visible y botón en cooldown.
- **Caso de formato inválido:** Correo sin arroba o mal formado → validación local muestra "Ingresa un correo electrónico válido" sin emitir petición HTTP.
- **Caso de exceso de peticiones (429):** Petición repetida o centinela `rate_limited@error.test` → respuesta 429 → aviso en Snackbar "Demasiados intentos. Por favor espera antes de intentar nuevamente".
- **Caso de fallo de conectividad:** Error de red → Snackbar reintentable sin alterar el texto del formulario ni vaciar el campo de correo.
- **Navegación de retorno:** Pulsar "Volver a Iniciar Sesión" emite `AuthEffect.NavigateToLogin` y restaura la pantalla de login sin pérdida de estado.
- **Regresión:** Compilación exitosa de `./gradlew assembleDebug` y aprobación continua de los 50 tests preexistentes de autenticación.
- **Comandos esperados (en Fase B):** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
- **Evidencia exigida:** ejecución real, reporte de cobertura de tests, trazabilidad a requisitos y registro en `memoria.md`.

## Riesgos y bloqueos
- **Falta de Backend real:** el proceso real de envío de correo SMTP es del servidor en Go; la app móvil únicamente dispara la solicitud y muestra la confirmación neutra.
- **Flujo de restablecimiento posterior (E006):** el enlace enviado por correo conducirá a la unidad `E006 /auth/password/reset`; en esta unidad `170` solo se modela la solicitud inicial ("¿Olvidaste tu contraseña?").

## Criterio de salida de la unidad
Todas las tareas en `task.md` documentadas en estado pendiente (`- [ ]`); trazabilidad completa requisito → plan → tarea; ninguna ejecución de código en Fase A; sin cambios fuera del alcance autorizado.
