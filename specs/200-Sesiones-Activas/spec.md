---
project: Nexum-Mobile
unit: 200-Sesiones-Activas
kind: technical-spec
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# SPEC — Consulta de Sesiones Activas (List Sessions)

## Propósito
Permitir a un usuario autenticado consultar la lista de sesiones y dispositivos conectados a su cuenta bajo el contrato **NEXUM-E008** (`GET /api/v1/auth/sessions`, soporte y seguridad `[S]`, catálogo V6.0.2), auditando accesos recientes, identificando el dispositivo actual y detectando equipos desconocidos sin exponer información sensible o tokens.

## Lugar en el plan SDD
- **Constitución:** [`.specify/memory/constitution.md`](../../.specify/memory/constitution.md) (vigente: MO-PR04 Backend como fuente autoritativa, MO-PR06 Privacidad y seguridad desde el diseño, MO-PR08 Manejo explícito de conectividad, MO-PR10 Cero secretos: "nunca exponer refresh tokens ni secretos en contratos o logs", MO-PR15 Seguridad de sesión responsable).
- **Tecnología:** Kotlin / Jetpack Compose / MVI (Android nativo).
- **Contrato:** NEXUM-E008 del catálogo de endpoints de referencia V6.0.2 (`GET /api/v1/auth/sessions` [S]).
- **Dependencias previas:** [`140-Login`](../140-Login/spec.md) (sesión activa requerida), [`150-Refresh-Token`](../150-Refresh-Token/spec.md) (renovación de token si expira durante la consulta), [`030-di-navegacion-ui`](../030-di-navegacion-ui/spec.md) (pantalla de Perfil/Seguridad) y [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md) (cliente HTTP).
- **Naturaleza:** especificación técnica de seguridad de cuenta, auditoría de accesos y soporte (`[S]`).

## Alcance permitido
Pantalla `ActiveSessionsScreen` accesible desde "Perfil > Seguridad > Dispositivos conectados", DTOs del contrato E008 (`SessionItemResponse`), ampliación de `AuthApi` y `AuthRepository`, implementación simulada en `MockAuthApi` con sesiones de prueba e identificación de la sesión actual (`current: Boolean`), ViewModel MVI (`SessionsViewModel`) con soporte de pull-to-refresh y estados de red explícitos (`Loading`, `Content`, `Empty`, `Error`), y pruebas unitarias de la unidad.

## Exclusiones explícitas
No incluir revocación individual de sesión (reservada para la unidad `NEXUM-E009 DELETE /auth/sessions/{sessionId}`); no devolver tokens, hashes de refresh token ni firmas en la respuesta; no conectarse a un backend real en esta etapa; no publicar ni desplegar.

## Requisitos y criterios verificables
| ID | Requisito obligatorio | Aceptación mínima |
| --- | --- | --- |
| **M200-R01** | **Pantalla de Dispositivos Conectados:** Pantalla con listado de sesiones activas, soporte de actualización (pull-to-refresh), indicador visual claro para la sesión actual ("Este dispositivo") y fecha de última actividad (`lastSeenAt`) en formato legible. | Composable probado con lista reactiva, badge distintivo en sesión actual y estados de carga/vacío. |
| **M200-R02** | **Contrato y Payload E008:** Enviar `GET /api/v1/auth/sessions` con cabecera `Authorization: Bearer <accessToken>`. Recibir respuesta `200 OK` con un array JSON de sesiones: `[{id, device, lastSeenAt, current}]`. | Modelado de DTOs acorde al catálogo V6.0.2 y verificación de deserialización en pruebas. |
| **M200-R03** | **Cero Fuga de Secretos (MO-PR06, MO-PR10):** La respuesta y el modelo de datos **nunca deben contener `refreshToken`, hashes ni credenciales**. Solo identificadores opacos (`sessionId`), nombre del dispositivo/plataforma y marcas temporales. | Inspección de DTOs y pruebas que verifiquen la ausencia total de campos criptográficos en las respuestas. |
| **M200-R04** | **Manejo Explícito de Estados y Conectividad (MO-PR08):** La UI debe diferenciar de forma visual los estados: `Loading` (indicador de progreso inicial o de refresco), `Content` (lista poblada), `Empty` (sin sesiones secundarias), `Error` (falla de backend con botón de reintento) y `Offline` (sin conexión). | Pruebas unitarias de las transiciones de estado del ViewModel ante cada escenario. |
| **M200-R05** | **Simulación en `MockAuthApi` y Repositorio:** El mock debe devolver al menos dos sesiones sintéticas (la actual derivada del dispositivo y otra remota en navegador o móvil secundario), responder con retardo (400–800 ms), y simular fallo de red determinista con token centinela `net@error.test`. | Pruebas unitarias en `MockAuthApiTest` verificando la lista de sesiones y el aislamiento ante errores de red. |

## Escenarios de prueba y evidencia
- **Caso favorable:** Usuario ingresa a "Dispositivos conectados" → llamada `GET /api/v1/auth/sessions` → respuesta `200` → lista visible con la sesión actual resaltada y detalles de sesión secundaria.
- **Caso de actualización (Pull-to-refresh):** Gesto de refresco → `isRefreshing = true` → nueva consulta a repositorio → lista actualizada sin parpadeos bruscos.
- **Caso de fallo de transporte:** Error de red → estado `Error` con aviso claro y botón "Reintentar" que permite recuperar la lista sin salir de la pantalla.
- **Caso de expiración de token:** `accessToken` caducado → interceptor / repositorio refresca sesión transparentemente (`150-Refresh-Token`) o redirige a Login si el refresh falla.
- **Regresión:** Compilación exitosa de `./gradlew assembleDebug` y aprobación continua de los 50 tests preexistentes de autenticación.
- **Comandos esperados (en Fase B):** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
- **Evidencia exigida:** ejecución real, reporte de cobertura de tests, trazabilidad a requisitos y registro en `memoria.md`.

## Riesgos y bloqueos
- **Falta de Backend real:** la lista de sesiones activas vive en el estado de `MockAuthApi`; su posterior migración a Go en `120-contratos-end-to-end` consumirá la colección `sessions` filtrada por `userId`.
- **Ambigüedad de nombres de dispositivo:** en Android, el nombre del dispositivo reportado (`Build.MODEL` / `Build.MANUFACTURER`) puede ser genérico; se sugiere que el contrato permita etiquetas amigables.

## Criterio de salida de la unidad
Todas las tareas en `task.md` documentadas en estado pendiente (`- [ ]`); trazabilidad completa requisito → plan → tarea; ninguna ejecución de código en Fase A; sin cambios fuera del alcance autorizado.
