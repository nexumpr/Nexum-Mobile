---
project: Nexum-Mobile
unit: 210-Revocar-Sesion
kind: technical-spec
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# SPEC — Revocación de Sesión Remota (Revoke Session)

## Propósito
Permitir a un usuario autenticado revocar una sesión específica en un dispositivo remoto bajo el contrato **NEXUM-E009** (`DELETE /api/v1/auth/sessions/{sessionId}`, soporte y seguridad `[S]`, catálogo V6.0.2), invalidando el acceso de dicho dispositivo sin eliminar la cuenta, sin afectar sus registros financieros y sin desautenticar la sesión en curso.

## Lugar en el plan SDD
- **Constitución:** [`.specify/memory/constitution.md`](../../.specify/memory/constitution.md) (vigente: MO-PR04 Backend como fuente autoritativa, MO-PR06 Privacidad y seguridad desde el diseño, MO-PR10 Cero secretos, MO-PR15 Seguridad de sesión responsable: "sesiones revocables bajo demanda").
- **Tecnología:** Kotlin / Jetpack Compose / MVI (Android nativo).
- **Contrato:** NEXUM-E009 del catálogo de endpoints de referencia V6.0.2 (`DELETE /api/v1/auth/sessions/{sessionId}` [S]).
- **Dependencias previas:** [`200-Sesiones-Activas`](../200-Sesiones-Activas/spec.md) (provee la lista de sesiones y el identificador `sessionId`), [`140-Login`](../140-Login/spec.md) (sesión activa requerida), [`160-Logout`](../160-Logout/spec.md) (coordinación si se revoca la sesión actual) y [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md) (cliente HTTP).
- **Naturaleza:** especificación técnica de seguridad de cuenta, control de accesos y soporte (`[S]`).

## Alcance permitido
Acción de revocación por sesión en la pantalla `ActiveSessionsScreen`, diálogo de confirmación previa para evitar cierres accidentales, integración del endpoint `DELETE /api/v1/auth/sessions/{sessionId}`, ampliación de `AuthApi` y `AuthRepository`, implementación simulada en `MockAuthApi` con respuesta `204 No Content`, actualización reactiva del listado de sesiones en `SessionsViewModel`, y pruebas unitarias de la unidad.

## Exclusiones explícitas
No eliminar cuentas de usuario (unidad `DELETE /users/me`, NEXUM-E014); no borrar movimientos financieros; no conectarse a un backend real en esta etapa; no publicar ni desplegar.

## Requisitos y criterios verificables
| ID | Requisito obligatorio | Aceptación mínima |
| --- | --- | --- |
| **M210-R01** | **Acción de Revocación y Diálogo de Confirmación:** Cada tarjeta de sesión remota debe contar con un botón "Cerrar sesión" / "Revocar". Al pulsarlo, debe desplegarse un diálogo accesible de confirmación indicando el nombre del dispositivo afectado. | Composable de confirmación probado con eventos de confirmación y cancelación. |
| **M210-R02** | **Contrato y Payload E009:** Enviar `DELETE /api/v1/auth/sessions/{sessionId}` con cabecera `Authorization: Bearer <accessToken>`. Ante éxito, recibir `204 No Content` sin cuerpo. | Métodos y llamadas de red conformes al contrato del catálogo V6.0.2 verificados en pruebas. |
| **M210-R03** | **Autorización y Aislamiento de Usuario (MO-PR04, MO-PR06):** El servidor valida que `sessionId` pertenezca al usuario autenticado; ante una sesión ajena o inexistente responde `404 Not Found` (o `403 Forbidden`) sin revelar información de terceros. La UI remueve la sesión de la lista si ya no existía. | Pruebas unitarias que verifiquen el manejo de errores `404` con depuración local limpia. |
| **M210-R04** | **Tratamiento Diferenciado de Sesión Actual:** Si el usuario selecciona revocar la sesión marcada como `current == true`, la UI debe advertir expresamente que se cerrará la sesión en el dispositivo actual. Si se confirma, el cliente debe invocar `TokenStorage.clear()` y emitir `AuthEffect.NavigateToLogin` (MO-PR15). Si es remota, la sesión actual permanece intacta. | Pruebas de branching entre revocación remota y revocación local. |
| **M210-R05** | **Simulación en `MockAuthApi` y Repositorio:** El mock debe remover la sesión de la lista en memoria tras retardo (400–800 ms), devolver `204 No Content`, responder `404` ante el identificador centinela `not_found_session` y simular error de red con `net@error.test`. | Pruebas unitarias en `MockAuthApiTest` cubriendo éxito, 404 y error de transporte. |

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
