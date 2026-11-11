---
project: Nexum-Mobile
unit: 190-Cambiar-Contrasena
kind: technical-spec
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# SPEC — Cambio de Contraseña Autenticado (Change Password)

## Propósito
Permitir a un usuario con sesión activa cambiar su contraseña mediante el contrato **NEXUM-E007** (`POST /api/v1/auth/password/change`, soporte y seguridad `[S]`, catálogo V6.0.2), implementando reautenticación obligatoria con la contraseña actual, validación local de requisitos de fortaleza y revocación selectiva de sesiones en servidor.

## Lugar en el plan SDD
- **Constitución:** [`.specify/memory/constitution.md`](../../.specify/memory/constitution.md) (vigente: MO-PR04 Backend como fuente autoritativa, MO-PR06 Privacidad y seguridad desde el diseño, MO-PR10 Cero secretos: "no retener contraseñas en memoria más de lo necesario", MO-PR15 Seguridad de sesión responsable).
- **Tecnología:** Kotlin / Jetpack Compose / MVI (Android nativo).
- **Contrato:** NEXUM-E007 del catálogo de endpoints de referencia V6.0.2 (`POST /api/v1/auth/password/change` [S]).
- **Dependencias previas:** [`140-Login`](../140-Login/spec.md) (sesión activa requerida), [`150-Refresh-Token`](../150-Refresh-Token/spec.md) (renovación de token si `accessToken` expira durante la operación), [`030-di-navegacion-ui`](../030-di-navegacion-ui/spec.md) (pantalla de Perfil/Seguridad) y [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md) (cliente HTTP).
- **Naturaleza:** especificación técnica de seguridad de cuenta y soporte (`[S]`).

## Alcance permitido
Pantalla/formulario `ChangePasswordScreen` accesible desde la configuración de seguridad del perfil, DTOs del contrato E007 (`ChangePasswordRequest`), validación local previa (contraseña actual no vacía, nueva contraseña de al menos 8 caracteres alfanuméricos, distinta a la actual y coincidente con confirmación), ampliación de `AuthApi` y `AuthRepository`, implementación en `MockAuthApi` con verificación simulada de contraseña actual y respuesta `204 No Content`, ViewModel MVI (`ChangePasswordViewModel`), purga inmediata de contraseñas de la memoria de la UI tras el éxito, y pruebas unitarias de la unidad.

## Exclusiones explícitas
No implementar recuperación por correo sin sesión (unidades `170` y `180`); no revocar la sesión del dispositivo actual a menos que la política del backend lo requiera; no almacenar contraseñas en texto plano en ningún almacén local; no conectarse a un backend real en esta etapa; no publicar ni desplegar.

## Requisitos y criterios verificables
| ID | Requisito obligatorio | Aceptación mínima |
| --- | --- | --- |
| **M190-R01** | **Formulario de Cambio con Reautenticación:** Formulario accesible con campos para contraseña actual (`currentPassword`), nueva contraseña (`newPassword`) y confirmación (`confirmNewPassword`), con alternadores independientes de visibilidad y botón "Actualizar contraseña". | Componente Composable probado y validación de estados de interacción en UI. |
| **M190-R02** | **Validación Local Estricta:** Validación previa al request: `currentPassword` no vacía, `newPassword` con mínimo 8 caracteres alfanuméricos, `newPassword != currentPassword` (para evitar cambios redundantes) y `newPassword == confirmNewPassword`. | Pruebas unitarias de validación local cubriendo los cuatro criterios antes del envío HTTP. |
| **M190-R03** | **Contrato y Payload E007:** Enviar `POST /api/v1/auth/password/change` con header `Authorization: Bearer <accessToken>` y payload `{"currentPassword": "...", "newPassword": "..."}`. Manejar respuesta exitosa `204 No Content`. | DTOs acordes al catálogo V6.0.2 y verificación de contrato en pruebas. |
| **M190-R04** | **Tratamiento de Credenciales Incorrectas y Purga de Memoria:** Si `currentPassword` no coincide con la registrada, el servidor responde error de credenciales (`401 Unauthorized` o `400 Bad Request`); la UI debe mostrar "La contraseña actual es incorrecta". Tras un cambio exitoso, las contraseñas deben limpiarse inmediatamente de los campos de texto del ViewModel (MO-PR06, MO-PR10). | Pruebas de mapeo de error de contraseña actual y verificación de reseteo de campos en éxito. |
| **M190-R05** | **Simulación en `MockAuthApi` y Repositorio:** El mock debe validar la contraseña actual simulada, responder `204 No Content` tras retardo de red (600–1000 ms), rechazar contraseñas actuales incorrectas y simular falla de red con el centinela `net@error.test`. | Pruebas unitarias en `MockAuthApiTest` cubriendo éxito, contraseña actual errónea y fallo de transporte. |

## Escenarios de prueba y evidencia
- **Caso favorable:** Usuario autenticado ingresa contraseña actual correcta + nueva contraseña válida y confirmada → llamada `POST /api/v1/auth/password/change` → respuesta `204` → campos de formulario se limpian de la memoria y se notifica éxito al usuario con Snackbar.
- **Caso de contraseña actual incorrecta:** `currentPassword` errónea → respuesta 401/400 del servidor simulado → mensaje de error específico en el campo de contraseña actual.
- **Caso de nueva contraseña idéntica a la actual:** `newPassword == currentPassword` → validación local muestra "La nueva contraseña debe ser distinta a la actual" sin emitir petición HTTP.
- **Caso de confirmación discrepante:** `newPassword != confirmNewPassword` → validación local muestra "Las contraseñas no coinciden".
- **Caso de fallo de transporte:** Error de red → Snackbar reintentable conservando los campos para facilitar la corrección o reintento sin frustrar al usuario.
- **Regresión:** Compilación exitosa de `./gradlew assembleDebug` y aprobación continua de los 50 tests preexistentes de autenticación.
- **Comandos esperados (en Fase B):** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
- **Evidencia exigida:** ejecución real, reporte de cobertura de tests, trazabilidad a requisitos y registro en `memoria.md`.

## Riesgos y bloqueos
- **Falta de Backend real:** la revocación selectiva de sesiones concurrentes se ejecuta en servidor de Go (colección `sessions`); en el cliente móvil se garantiza el envío del contrato y la custodia segura del token actual.
- **Expiración de token durante el cambio:** si el `accessToken` expira mientras el usuario completa el formulario, la integración con `150-Refresh-Token` debe renovar el token transparentemente antes de disparar el cambio de contraseña.

## Criterio de salida de la unidad
Todas las tareas en `task.md` documentadas en estado pendiente (`- [ ]`); trazabilidad completa requisito → plan → tarea; ninguna ejecución de código en Fase A; sin cambios fuera del alcance autorizado.
