---
project: Nexum-Mobile
unit: 260-Eliminar-Cuenta
kind: functional-spec
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# SPEC — Eliminación Definitiva de Cuenta (Delete Account)

## Propósito
Permitir a un usuario titular autenticado solicitar y confirmar la eliminación definitiva de su cuenta y sus datos financieros bajo el contrato **NEXUM-E014** (`DELETE /api/v1/users/me`, función de maqueta `[M]`, catálogo V6.0.2), implementando reautenticación estricta con contraseña actual, confirmación textual explícita (`confirmation = "ELIMINAR"`), soporte opcional de idempotencia (`Idempotency-Key`), purga incondicional de credenciales locales (`TokenStorage.clear()`), invalidación de la sesión en el dispositivo y distinción clara frente al modo demo/invitado.

## Lugar en el plan SDD
- **Constitución:** [`.specify/memory/constitution.md`](../../.specify/memory/constitution.md) (vigente: MO-PR04 Backend como fuente autoritativa, MO-PR06 Privacidad y seguridad desde el diseño: "validar contraseña de nuevo; borrar/suprimir dependencias y tokens; no exponer borrado de terceros", MO-PR07 Usabilidad inclusiva Material 3, MO-PR10 Cero secretos: "limpiar contraseñas de la memoria inmediatamente tras su uso", MO-PR15 Seguridad de sesión responsable: "la eliminación de cuenta prevalece y exige purga incondicional de credenciales", MO-PR17 Diseño fiel al prototipo: "Perfil > Eliminar cuenta > contraseña + ELIMINAR; demo solo local").
- **Tecnología:** Kotlin / Jetpack Compose / MVI (Android nativo).
- **Contrato:** NEXUM-E014 del catálogo de endpoints de referencia V6.0.2 (`DELETE /api/v1/users/me` [M]).
- **Dependencias previas:** [`240-Perfil-Usuario`](../240-Perfil-Usuario/spec.md) (punto de acceso desde Perfil), [`140-Login`](../140-Login/spec.md) (sesión activa requerida), [`150-Refresh-Token`](../150-Refresh-Token/spec.md) (renovación de token si expira durante el flujo), [`160-Logout`](../160-Logout/spec.md) (rutinas de purga y navegación destructiva), [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md) (cliente HTTP) y [`050-sesion-credenciales`](../050-sesion-credenciales/spec.md) (almacén seguro de tokens).
- **Naturaleza:** especificación funcional y de seguridad de eliminación de cuenta (`[M]`).

## Alcance permitido
Diálogo modal o pantalla destructiva `DeleteAccountDialog` en Compose accesible desde `ProfileScreen` ("Perfil > Eliminar cuenta"), campos de entrada para reautenticación por contraseña (`password`) y confirmación textual en mayúsculas (`confirmation = "ELIMINAR"`), control reactivo de habilitación del botón de acción destructiva, DTOs del contrato E014 (`DeleteAccountRequest`, `DeleteAccountResponse`), cabecera opcional `Idempotency-Key`, ampliación de `AuthApi` y `AuthRepository`, implementación simulada en `MockAuthApi` con verificación de contraseña, soporte de respuestas síncrona `204 No Content` y asíncrona `202 Accepted` (`deletionRequestId`, `status`), purga incondicional de `TokenStorage` (fail-secure), navegación destructiva a Login/Bienvenida con reseteo de backstack (`AuthEffect.NavigateToLogin`), tratamiento local aislado para modo demo/invitado, y pruebas unitarias de la unidad.

## Exclusiones explícitas
No conectarse a un backend real en esta etapa; no borrar ni alterar datos en bases remotas sin autorización; no permitir eliminación de cuentas ajenas (anti-IDOR); no omitir la reautenticación por contraseña en cuentas reales; no retener contraseñas en memoria tras cerrar o enviar el diálogo; no realizar llamadas HTTP en modo demo/invitado; no publicar ni desplegar.

## Requisitos y criterios verificables
| ID | Requisito obligatorio | Aceptación mínima |
| --- | --- | --- |
| **M260-R01** | **Diálogo Destructivo con Doble Confirmación (Perfil > Eliminar cuenta):** Componente accesible en Jetpack Compose con advertencias claras de irreversibilidad (pérdida de historial financiero, presupuestos y metas), campo de contraseña (`password`) con alternador de visibilidad, y campo de confirmación textual (`confirmation`). El botón "Eliminar definitivamente" debe permanecer deshabilitado hasta que `password.isNotBlank()` y `confirmation.trim() == "ELIMINAR"`. | Composable probado con control reactivo de habilitación del botón destructivo y manejo accesible de estados de error. |
| **M260-R02** | **Contrato y Payload E014:** Enviar `DELETE /api/v1/users/me` con cabecera `Authorization: Bearer <accessToken>`, cabecera opcional `Idempotency-Key: <UUID>` y payload JSON `{"password": "...", "confirmation": "ELIMINAR"}`. Soportar respuesta síncrona `204 No Content` (cuerpo vacío) y respuesta asíncrona `202 Accepted` (`{"deletionRequestId": "...", "status": "pending|processing"}`). | Modelado de DTOs acorde al catálogo V6.0.2 y verificación de serialización/deserialización en pruebas. |
| **M260-R03** | **Purga Local Incondicional de Sesión (Fail-Secure / MO-PR06, MO-PR15):** Ante respuesta exitosa (`204` o `202`) o conflicto de eliminación concurrente ya procesada (`409 Conflict`), el cliente DEBE ejecutar de inmediato `TokenStorage.clear()`, invalidar cualquier estado de usuario en memoria y emitir efecto de navegación hacia Login/Bienvenida (`AuthEffect.NavigateToLogin`), limpiando el backstack para imposibilitar regresar a pantallas protegidas mediante el botón Atrás. | Pruebas unitarias que verifiquen que `TokenStorage` queda completamente vacío y se emite la navegación destructiva tras el éxito o conflicto 409. |
| **M260-R04** | **Aislamiento Estricto de Modo Demo / Invitado (MO-PR17):** Si el usuario opera bajo sesión de Invitado/Demo (`isDemo = true`): la eliminación es estrictamente local en memoria/almacenamiento simulado del dispositivo; NO debe solicitar contraseña de backend ni realizar peticiones HTTP autenticadas ni alterar cuentas reales. Al confirmar en demo, purga los datos simulados y redirige a la pantalla de bienvenida. | Pruebas unitarias de branching que demuestren cero llamadas de red y reseteo local al eliminar cuenta en modo demo. |
| **M260-R05** | **Manejo Resiliente de Errores y Cero Secretos (MO-PR06, MO-PR10):** Si la contraseña ingresada no coincide (`401 Unauthorized` o `400 Bad Request`), la UI muestra error específico ("Contraseña incorrecta"), el diálogo permanece abierto y la sesión NO se destruye. Si ocurre fallo de transporte de red, se muestra aviso de error reintentable. La contraseña debe purgarse de la memoria del ViewModel inmediatamente tras completarse o cancelarse la acción. | Pruebas unitarias de mapeo de error 401, error de red y verificación de limpieza de strings de contraseña en memoria. |
| **M260-R06** | **Simulación en `MockAuthApi` y Cobertura de Pruebas:** El mock debe validar la contraseña contra la sesión activa en memoria, responder `204 No Content` o `202 Accepted` tras retardo (500–1000 ms), rechazar contraseñas incorrectas con `401`, rechazar confirmaciones distintas a "ELIMINAR" con `422/400`, simular falla de red con el centinela `net@error.test` y simular eliminación asíncrona con el centinela `async@delete.test`. | Pruebas unitarias completas en `MockAuthApiTest` y `DeleteAccountViewModelTest` con `kotlinx-coroutines-test`. |

## Escenarios de prueba y evidencia
- **Caso favorable síncrono (204):** Usuario real ingresa contraseña correcta y "ELIMINAR" → pulsa botón destructivo → llamada `DELETE /api/v1/users/me` → respuesta `204 No Content` → `TokenStorage.clear()` → redirección a Login con aviso "Tu cuenta ha sido eliminada definitivamente".
- **Caso favorable asíncrono (202):** Usuario real activa eliminación → respuesta `202 Accepted` con `deletionRequestId` → `TokenStorage.clear()` → redirección a Login con aviso "Solicitud de eliminación de cuenta en proceso".
- **Caso de contraseña incorrecta (401):** Usuario ingresa contraseña errónea y "ELIMINAR" → llamada rechazada con `401 Unauthorized` → se muestra "Contraseña incorrecta. Por favor verifica tus credenciales" → la sesión local permanece intacta y el usuario no es deslogueado.
- **Caso de confirmación inválida:** Usuario escribe "eliminar" (minúsculas) o cualquier texto distinto a "ELIMINAR" → el botón permanece deshabilitado; no se emite ninguna petición de red.
- **Caso en modo Demo / Invitado:** Usuario invitado pulsa "Eliminar cuenta" → se presenta confirmación local sin exigir contraseña → al confirmar, se limpian las semillas y el estado en memoria → se regresa al inicio sin llamadas HTTP.
- **Caso de fallo de transporte:** Pérdida de conectividad durante la llamada → Snackbar de error reintentable ("No se pudo conectar con el servidor. Tu cuenta no ha sido eliminada") sin purgar la sesión local.
- **Caso de cancelación:** Usuario abre el diálogo y presiona "Cancelar" → se limpian los campos de contraseña y confirmación de la memoria y la pantalla de Perfil continúa visible sin alteraciones.
- **Regresión:** Compilación exitosa de `./gradlew assembleDebug` y aprobación continua de los 50 tests preexistentes de autenticación.
- **Comandos esperados (en Fase B):** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
- **Evidencia exigida:** ejecución real, reporte de cobertura de tests, trazabilidad a requisitos y registro en `memoria.md`.

## Riesgos y bloqueos
- **Falta de Backend real:** la eliminación en cascada de movimientos, presupuestos, metas y sesiones en MongoDB Atlas es responsabilidad del backend en Go; en el cliente móvil se garantiza el envío fidedigno del contrato, la reautenticación y la purga exhaustiva de credenciales locales.
- **Políticas de retención legal (MO-PR06):** el backend puede requerir retención documental o borrado diferido (respuesta 202 con `deletionRequestId`); el cliente móvil trata la cuenta como irrevocablemente inaccesible de inmediato.
- **Navegación y limpieza de Backstack:** debe garantizarse que tras `TokenStorage.clear()` el árbol de navegación no permita retroceder con el gesto o botón Atrás de Android a ninguna pantalla con datos financieros en caché.

## Criterio de salida de la unidad
Todas las tareas en `task.md` documentadas en estado pendiente (`- [ ]`); trazabilidad completa requisito → plan → tarea; ninguna ejecución de código en Fase A; sin cambios fuera del alcance autorizado.
