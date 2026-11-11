---
project: Nexum-Mobile
unit: 240-Perfil-Usuario
kind: functional-spec
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# SPEC — Consulta de Perfil de Usuario (Get User Profile)

## Propósito
Recuperar y presentar los atributos de identidad y estado de cuenta del usuario autenticado (`id`, `name`, `email`, `emailVerified`, `createdAt`) bajo el contrato **NEXUM-E012** (`GET /api/v1/users/me`, función de maqueta `[M]`, catálogo V6.0.2), derivando la identidad exclusivamente del token de acceso sin depender de almacenamiento local no verificado ni aceptar identificadores por parámetro.

## Lugar en el plan SDD
- **Constitución:** [`.specify/memory/constitution.md`](../../.specify/memory/constitution.md) (vigente: MO-PR04 Backend como fuente autoritativa, MO-PR06 Privacidad y seguridad desde el diseño: "el usuario se deriva del token, no aceptar userId ajeno", MO-PR07 Usabilidad inclusiva Material 3, MO-PR08 Manejo explícito de conectividad).
- **Tecnología:** Kotlin / Jetpack Compose / MVI (Android nativo).
- **Contrato:** NEXUM-E012 del catálogo de endpoints de referencia V6.0.2 (`GET /api/v1/users/me` [M]).
- **Dependencias previas:** [`140-Login`](../140-Login/spec.md) (sesión activa requerida), [`150-Refresh-Token`](../150-Refresh-Token/spec.md) (renovación de token ante caducidad), [`030-di-navegacion-ui`](../030-di-navegacion-ui/spec.md) (shell de pantalla de Perfil) y [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md) (cliente HTTP).
- **Naturaleza:** especificación funcional de presentación de identidad y cuenta del usuario (`[M]`).

## Alcance permitido
Pantalla/sección `ProfileScreen` en Compose con presentación de nombre, correo, badge de verificación de email y fecha de registro, DTOs del contrato E012 (`UserProfileResponse`), ampliación de `AuthApi` y `UserRepository` (o `AuthRepository`), implementación simulada en `MockAuthApi` con datos del usuario activo y retardo, ViewModel MVI (`ProfileViewModel`) con estados `Loading`, `Content`, `Error` y `Offline`, y pruebas unitarias de la unidad.

## Exclusiones explícitas
No incluir edición de datos personales (reservada para la unidad `NEXUM-E013 PATCH /users/me`); no incluir eliminación de cuenta (unidad `NEXUM-E014 DELETE /users/me`); no recibir ni enviar `userId` como parámetro de consulta o ruta; no conectarse a un backend real en esta etapa; no publicar ni desplegar.

## Requisitos y criterios verificables
| ID | Requisito obligatorio | Aceptación mínima |
| --- | --- | --- |
| **M240-R01** | **Pantalla de Perfil de Usuario:** Pantalla accesible en Jetpack Compose con encabezado de perfil, nombre (`name`), correo electrónico (`email`), badge visual de verificación de cuenta y fecha de registro formateada (`createdAt`). | Composable probado con soporte de pull-to-refresh y transiciones de estado limpias. |
| **M240-R02** | **Contrato y Payload E012:** Enviar `GET /api/v1/users/me` con cabecera `Authorization: Bearer <accessToken>`. Recibir respuesta `200 OK` con payload JSON: `{"id": "...", "name": "...", "email": "...", "emailVerified": true/false, "createdAt": "..."}`. | Modelado de DTOs acorde al catálogo V6.0.2 y verificación de contrato en pruebas. |
| **M240-R03** | **Derivación Estricta de Identidad (Anti-IDOR / MO-PR06):** El endpoint no acepta ningún parámetro de identificación (`userId`, `email`). La identidad es resuelta por el servidor a partir del JWT. El cliente nunca debe inyectar identificadores por query o path para consultar su propio perfil. | Inspección de firmas de métodos e interfaces que confirmen la ausencia de parámetros de usuario en la petición. |
| **M240-R04** | **Estado de Verificación de Correo y Navegación Contextual:** Si `emailVerified == false`, la interfaz debe mostrar un aviso destacado con enlace para solicitar verificación ([`220-Solicitud-Verificacion-Email`](../220-Solicitud-Verificacion-Email/spec.md)). Si es `true`, muestra el indicador afirmativo de cuenta verificada. | Pruebas de branching en la UI según el booleano `emailVerified`. |
| **M240-R05** | **Simulación en `MockAuthApi` y Resiliencia Offline (MO-PR08):** El mock debe retornar el perfil del usuario autenticado tras retardo (300–700 ms). Si la red falla, la UI debe mantener los datos previamente cargados (si existen) con indicación de modo sin conexión y botón de reintento. | Pruebas unitarias en `MockAuthApiTest` y `ProfileViewModelTest` cubriendo éxito y error de red. |

## Escenarios de prueba y evidencia
- **Caso favorable (correo verificado):** Usuario entra a Perfil → llamada `GET /api/v1/users/me` → respuesta `200` con `emailVerified = true` → se muestra el nombre, email, fecha de alta y badge verde de verificación.
- **Caso de correo no verificado:** Respuesta con `emailVerified = false` → se muestra banner de advertencia ("Tu correo no está verificado") con botón directo a solicitud de verificación.
- **Caso de actualización (Pull-to-refresh):** Gesto de deslizamiento hacia abajo → `isRefreshing = true` → actualización de datos sin parpadeo de pantalla.
- **Caso de fallo de transporte con datos previos:** Pérdida de red → se muestran los datos en memoria con mensaje informativo ("No se pudo actualizar el perfil. Mostrando información local") y opción de reintento.
- **Caso de sesión expirada:** Servidor responde `401 Unauthorized` → interceptor renueva token con `150-Refresh-Token` o emite `AuthEffect.NavigateToLogin` si no es recuperable.
- **Regresión:** Compilación exitosa de `./gradlew assembleDebug` y aprobación continua de los 50 tests preexistentes de autenticación.
- **Comandos esperados (en Fase B):** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
- **Evidencia exigida:** ejecución real, reporte de cobertura de tests, trazabilidad a requisitos y registro en `memoria.md`.

## Riesgos y bloqueos
- **Falta de Backend real:** la fuente de verdad de la cuenta vive en MongoDB Atlas (`users`); en el cliente móvil se garantiza la representación fiel del contrato y la experiencia MVI.
- **Formateo de fechas:** `createdAt` se transmite en RFC 3339 / ISO-8601 UTC; debe formatearse respetando la configuración regional del dispositivo del usuario (MO-PR07).

## Criterio de salida de la unidad
Todas las tareas en `task.md` documentadas en estado pendiente (`- [ ]`); trazabilidad completa requisito → plan → tarea; ninguna ejecución de código en Fase A; sin cambios fuera del alcance autorizado.
