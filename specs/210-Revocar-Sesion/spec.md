---
project: Nexum-Mobile
unit: 210-Revocar-Sesion
kind: technical-spec
version: 1.1.0
status: in-progress
implementation: only_on_explicit_request
---

# SPEC — Revocación de Sesión Remota (Revoke Session)

## Propósito
Permitir a un usuario autenticado revocar una sesión específica (remota o actual) bajo el contrato **NEXUM-E009** (`DELETE /api/v1/auth/sessions/{sessionId}`, soporte y seguridad `[S]`, catálogo V6.0.2), invalidando el acceso de dicho dispositivo sin eliminar la cuenta y sin afectar sus registros financieros. Si la sesión revocada es remota, se recarga el listado E008 manteniendo activa la sesión local; si es la sesión actual, se purgan credenciales y se redirige a Login.

## Lugar en el plan SDD
- **Constitución:** [`.specify/memory/constitution.md`](../../.specify/memory/constitution.md) (vigente: MO-PR04 Backend como fuente autoritativa, MO-PR06 Privacidad y seguridad desde el diseño, MO-PR10 Cero secretos, MO-PR15 Seguridad de sesión responsable: "sesiones revocables bajo demanda").
- **Tecnología:** Kotlin / Jetpack Compose / MVI (Android nativo).
- **Contrato:** NEXUM-E009 del catálogo de endpoints de referencia V6.0.2 (`DELETE /api/v1/auth/sessions/{sessionId}` [S], `204 No Content` sin body).
- **Dependencias previas:** [`200-Sesiones-Activas`](../200-Sesiones-Activas/spec.md) (provee la lista de sesiones y el identificador `sessionId`), [`140-Login`](../140-Login/spec.md) (sesión activa requerida), [`160-Logout`](../160-Logout/spec.md) (coordinación si se revoca la sesión actual) y [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md) (cliente HTTP).
- **Naturaleza:** especificación técnica de seguridad de cuenta, control de accesos y soporte (`[S]`).

## Alcance permitido
Acción de revocación por sesión en la pantalla `ActiveSessionsScreen`, diálogo de confirmación previa con diferenciación clara si se revoca la sesión en uso, validación de formato UUID en `sessionId`, integración del endpoint `DELETE /api/v1/auth/sessions/{sessionId}` sin body, ampliación de `AuthApi` y `AuthRepository`, implementación en `RetrofitAuthRepositoryApi` con `Response<Unit>` para tolerar `204 No Content` sin deserializar JSON vacío, implementación simulada en `MockAuthApi` con idempotencia y centinelas (`not_found_session`, `conflict_session`, `net_error_session`), recarga reactiva de E008 tras revocar sesiones remotas en `SessionsViewModel`, y pruebas unitarias de la unidad.

## Exclusiones explícitas
No eliminar cuentas de usuario (unidad `DELETE /users/me`, NEXUM-E014); no borrar movimientos financieros; no conectarse a un backend real en esta etapa; no publicar ni desplegar.

## Requisitos y criterios verificables
| ID | Requisito obligatorio | Aceptación mínima |
| --- | --- | --- |
| **M210-R01** | **Acción de Revocación y Diálogo de Confirmación:** Cada tarjeta de sesión activa debe contar con un botón "Cerrar sesión". Al pulsarlo, debe desplegarse un diálogo accesible de confirmación Material 3 (`AlertDialog`) advirtiendo expresamente si se trata del dispositivo actual o de una sesión remota. | Composable de confirmación probado con eventos de confirmación y cancelación. |
| **M210-R02** | **Contrato y Payload E009 (204 No Content):** Enviar `DELETE /api/v1/auth/sessions/{sessionId}` con cabecera `Authorization: Bearer <accessToken>` y sin body. Ante éxito, recibir `204 No Content` procesado con `Response<Unit>` sin intentar deserializar un JSON vacío. | Métodos y llamadas de red conformes a NEXUM-E009 verificados en pruebas de repositorio y servicio Retrofit. |
| **M210-R03** | **Validación UUID y Manejo de Errores (404 / 409):** Validación local de formato UUID en `sessionId`. Manejo de `404 Not Found` (sesión inexistente, expirada o ajena) removiendo la sesión y recargando E008 con aviso informativo; y `409 Conflict` (conflicto concurrente) permitiendo reintentar. | Pruebas unitarias que verifiquen validación UUID y manejo de errores `404` y `409`. |
| **M210-R04** | **Tratamiento Diferenciado de Sesión Actual:** Si `current == true`, tras recibir `204` se invoca obligatoriamente `TokenStorage.clear()` y se emite `AuthEffect.NavigateToLogin` de inmediato (sin llamar a E008). Si `current == false`, la sesión actual permanece intacta, se emite Snackbar de éxito y se recarga E008 (`getSessions`). | Pruebas de branching entre revocación remota y revocación local. |
| **M210-R05** | **Simulación en `MockAuthApi` e Idempotencia:** El mock simula retardo (400–800 ms), elimina la sesión de su lista en memoria para que E008 ya no la devuelva, soporta revocaciones idempotentes (retorna éxito si ya fue revocada), responde `404` ante `not_found_session`, `409` ante `conflict_session` y error reintentable ante `net_error_session`. | Pruebas unitarias en `MockAuthApiTest` cubriendo éxito, idempotencia, 404, 409 y error de red. |

## Escenarios de prueba y evidencia
- **Caso favorable (sesión remota):** Usuario confirma revocar sesión en "Chrome en Windows 11" → llamada `DELETE /api/v1/auth/sessions/{sessionId}` → respuesta `204` → sesión se elimina de la lista en pantalla y se muestra confirmación en Snackbar; la sesión local continúa activa.
- **Caso de revocación de sesión actual:** Usuario confirma revocar el dispositivo en uso → llamada responde `204` → `TokenStorage.clear()` → redirección a pantalla de Login (`AuthEffect.NavigateToLogin`).
- **Caso de cancelación:** Usuario presiona "Cancelar" en el diálogo de confirmación → diálogo se cierra sin llamadas de red y la lista de sesiones permanece intacta.
- **Caso de sesión ya expirada (404 en revocación):** Sesión ya cerrada en backend → respuesta 404 → la app la remueve de la lista con aviso informativo ("La sesión ya había expirado").
- **Caso de fallo de transporte:** Error de red → Snackbar de error con reintento sin remover la sesión de la lista.
- **Regresión:** Compilación exitosa de `./gradlew assembleDebug` y aprobación continua de los 50 tests preexistentes de autenticación.
- **Comandos esperados (en Fase B):** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
- **Evidencia exigida:** ejecución real, reporte de cobertura de tests, trazabilidad a requisitos y registro en `memoria.md`.

## Riesgos y bloqueos
- **Falta de Backend real:** la revocación remota vive en el estado de `MockAuthApi`; en producción con Go, el backend marca `revokedAt = now()` en la colección `sessions` e invalida el `refreshToken` asociado.
- **Condición de carrera:** si dos peticiones de revocación ocurren rápidamente sobre la misma sesión, la segunda responderá 404, caso contemplado como idempotente en la UI.

## Criterio de salida de la unidad
Todas las tareas en `task.md` documentadas en estado pendiente (`- [ ]`); trazabilidad completa requisito → plan → tarea; ninguna ejecución de código en Fase A; sin cambios fuera del alcance autorizado.
