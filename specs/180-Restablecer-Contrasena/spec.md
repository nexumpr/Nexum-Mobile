---
project: Nexum-Mobile
unit: 180-Restablecer-Contrasena
kind: functional-spec
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# SPEC — Restablecimiento de Contraseña (Reset Password)

## Propósito
Permitir a los usuarios definir una nueva contraseña utilizando el token de recuperación recibido bajo el contrato **NEXUM-E006** (`POST /api/v1/auth/password/reset`, extensión visible `[E]`, catálogo V6.0.2), con validación local estricta de fortaleza, invalidación de sesiones previas en servidor y redirección segura hacia el inicio de sesión sin auto-login.

## Lugar en el plan SDD
- **Constitución:** [`.specify/memory/constitution.md`](../../.specify/memory/constitution.md) (vigente: MO-PR04 Backend como fuente autoritativa, MO-PR06 Privacidad y seguridad desde el diseño, MO-PR10 Cero secretos, MO-PR15 Seguridad de sesión responsable).
- **Tecnología:** Kotlin / Jetpack Compose / MVI (Android nativo).
- **Contrato:** NEXUM-E006 del catálogo de endpoints de referencia V6.0.2 (`POST /api/v1/auth/password/reset` [E]).
- **Dependencias previas:** [`170-Recuperar-Contrasena`](../170-Recuperar-Contrasena/spec.md) (solicitud previa del enlace), [`140-Login`](../140-Login/spec.md) (destino tras reseteo exitoso), [`030-di-navegacion-ui`](../030-di-navegacion-ui/spec.md) (manejo de deep links y navegación) y [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md) (cliente HTTP).
- **Naturaleza:** especificación funcional de extensión visible (`[E]`) de autenticación y seguridad de cuenta.

## Alcance permitido
Diseño de la pantalla `ResetPasswordScreen` en Compose (soporta precarga de `token` desde deep link o entrada manual), validación local de requisitos de contraseña y coincidencia estricta, DTOs del contrato E006 (`ResetPasswordRequest`), ampliación de `AuthApi` y `AuthRepository`, implementación simulada en `MockAuthApi` con invalidación de un solo uso y respuesta `204 No Content`, ViewModel MVI (`ResetPasswordViewModel`), emisión del efecto de navegación hacia Login con mensaje de éxito, y pruebas unitarias de la unidad.

## Exclusiones explícitas
No iniciar sesión automáticamente tras cambiar la contraseña (el usuario debe autenticarse explícitamente con su nueva clave); no almacenar ni transmitir el token de restablecimiento una vez consumido; no conectarse a un backend real en esta etapa; no publicar ni desplegar.

## Requisitos y criterios verificables
| ID | Requisito obligatorio | Aceptación mínima |
| --- | --- | --- |
| **M180-R01** | **Pantalla de Restablecimiento y Validación Local:** Pantalla con campo para `resetToken` (opcionalmente oculto si viene provisto por deep link), nueva contraseña (`newPassword`) y confirmación (`confirmPassword`) con alternador de visibilidad, botón "Restablecer contraseña" y botón "Volver a Iniciar Sesión". Validación local: mínimo 8 caracteres alfanuméricos, coincidencia exacta y token no vacío antes del envío. | Composable probado, errores visuales en campo ante incumplimiento y botón deshabilitado durante carga. |
| **M180-R02** | **Contrato y Payload E006:** Enviar `POST /api/v1/auth/password/reset` con payload `{"resetToken": "...", "newPassword": "..."}`. Manejar respuesta exitosa `204 No Content` sin cuerpo. | Modelado de DTOs acorde al catálogo V6.0.2 y verificación de contrato en pruebas. |
| **M180-R03** | **Seguridad sin Auto-Login (Login Obligatorio):** Tras el cambio exitoso de contraseña, la aplicación **no debe iniciar sesión automáticamente** ni generar tokens de acceso; debe emitir `AuthEffect.NavigateToLogin` acompañado de un mensaje afirmativo ("Contraseña restablecida con éxito. Inicia sesión con tu nueva contraseña"), obligando a una reautenticación limpia. | Pruebas unitarias que verifiquen la redirección a Login sin guardar sesión en `TokenStorage`. |
| **M180-R04** | **Tratamiento de Token Inválido o Ya Utilizado:** Si el servidor responde error por token expirado, inválido o ya consumido (`400/401/410`), la UI debe desplegar un mensaje claro ("El enlace de recuperación es inválido o ha expirado") y ofrecer una acción directa para volver a solicitar un enlace (`AuthEffect.NavigateToForgotPassword`). | Pruebas de mapeo de errores de token a avisos claros y acción de recuperación. |
| **M180-R05** | **Simulación en `MockAuthApi` de Un Solo Uso:** El mock debe responder `204 No Content` ante tokens válidos, registrar el token como utilizado para rechazar cualquier intento subsiguiente con el mismo token, responder error de token expirado ante `expired_reset_token`, y simular error de transporte ante `net@error.test`. | Pruebas unitarias en `MockAuthApiTest` cubriendo éxito de un solo uso, reuso bloqueado, expiración y red. |

## Escenarios de prueba y evidencia
- **Caso favorable:** Token válido + nueva contraseña válida y coincidente → llamada `POST /api/v1/auth/password/reset` → respuesta `204` → emisión de `AuthEffect.NavigateToLogin` con mensaje de éxito.
- **Caso de contraseñas no coincidentes:** `newPassword != confirmPassword` → validación local muestra "Las contraseñas no coinciden" sin disparar petición HTTP.
- **Caso de contraseña débil (<8 caracteres o no alfanumérica):** Validación local muestra "La contraseña debe tener al menos 8 caracteres y contener letras y números".
- **Caso de token ya utilizado o expirado:** Servidor devuelve error de token → UI muestra advertencia y botón para solicitar nuevo enlace hacia `170-Recuperar-Contrasena`.
- **Caso de error de red:** Falla de transporte → Snackbar con opción de reintento sin perder los datos introducidos en el formulario.
- **Regresión:** Compilación exitosa de `./gradlew assembleDebug` y aprobación continua de los 50 tests preexistentes de autenticación.
- **Comandos esperados (en Fase B):** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
- **Evidencia exigida:** ejecución real, reporte de cobertura de tests, trazabilidad a requisitos y registro en `memoria.md`.

## Riesgos y bloqueos
- **Falta de Backend real:** la regla de revocación de sesiones en servidor y consumo de un solo uso se modela en `MockAuthApi`; su contraparte en Go deberá invalidar todas las sesiones activas en la colección `sessions`.
- **Manejo de Deep Links:** la captura del parámetro `token` desde un enlace universal `https://app.nexum.mx/reset-password?token=...` depende de la configuración de filtros de intención en `AndroidManifest.xml`, los cuales se habilitarán formalmente en la unidad de navegación `030`.

## Criterio de salida de la unidad
Todas las tareas en `task.md` documentadas en estado pendiente (`- [ ]`); trazabilidad completa requisito → plan → tarea; ninguna ejecución de código en Fase A; sin cambios fuera del alcance autorizado.
