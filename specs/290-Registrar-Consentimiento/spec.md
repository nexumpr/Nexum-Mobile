---
project: Nexum-Mobile
unit: 290-Registrar-Consentimiento
kind: technical-spec
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# SPEC — Registro de Consentimiento del Usuario (Record Consent)

## Propósito
Permitir a un usuario titular autenticado formalizar y almacenar de manera auditable la aceptación de una nueva versión del aviso de privacidad bajo el contrato **NEXUM-E017** (`POST /api/v1/users/me/consents`, soporte y consistencia `[S]`, catálogo V6.0.2), garantizando la trazabilidad legal posterior al registro inicial, evitando envíos duplicados de consentimientos ya registrados y sincronizando de inmediato el historial de privacidad del dispositivo.

## Lugar en el plan SDD
- **Constitución:** [`.specify/memory/constitution.md`](../../.specify/memory/constitution.md) (vigente: MO-PR04 Backend como fuente autoritativa, MO-PR06 Privacidad y seguridad desde el diseño: "el consentimiento inicial se guarda en /auth/register; registrar de forma auditable nuevas versiones; evitar envíos duplicados", MO-PR07 Usabilidad inclusiva Material 3, MO-PR08 Manejo explícito de conectividad).
- **Tecnología:** Kotlin / Jetpack Compose / MVI (Android nativo).
- **Contrato:** NEXUM-E017 del catálogo de endpoints de referencia V6.0.2 (`POST /api/v1/users/me/consents` [S]).
- **Dependencias previas:** [`270-Aviso-Privacidad-Vigente`](../270-Aviso-Privacidad-Vigente/spec.md) (obtiene la versión vigente a aceptar), [`280-Historial-Consentimientos`](../280-Historial-Consentimientos/spec.md) (verifica versiones previas para evitar duplicados), [`140-Login`](../140-Login/spec.md) (sesión activa requerida), [`150-Refresh-Token`](../150-Refresh-Token/spec.md) (renovación de token) y [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md) (cliente HTTP).
- **Naturaleza:** especificación técnica de cumplimiento legal, consistencia y soporte (`[S]`).

## Alcance permitido
Diálogo modal o banner de confirmación `UpdateConsentDialog` en Jetpack Compose (invocable al detectar actualización del aviso o desde `ProfileScreen > Privacidad`), modelado de DTOs del contrato E017 (`RecordConsentRequest`, `RecordConsentResponse`), cabecera opcional `Idempotency-Key`, ampliación de contratos en `AuthApi` y `AuthRepository`, implementación simulada en `MockAuthApi` con persistencia en memoria y generación de `consentId`, ViewModel MVI (`RecordConsentViewModel`) con control de estado y prevención de envíos redundantes, y pruebas unitarias de la unidad.

## Exclusiones explícitas
No registrar consentimientos de terceros ni aceptar `userId` ajeno por parámetro (anti-IDOR); no permitir registros con `accepted = false` (el rechazo mantiene el estado previo sin llamar a este endpoint de registro positivo); no reenviar consentimientos si la versión ya figura aceptada en el historial; no conectarse a un backend real en esta etapa; no publicar ni desplegar.

## Requisitos y criterios verificables
| ID | Requisito obligatorio | Aceptación mínima |
| --- | --- | --- |
| **M290-R01** | **Disparador y Diálogo de Aceptación en UI (Perfil > Privacidad / Notificación de Cambio):** Componente Compose `UpdateConsentDialog` accesible que presenta la versión del aviso actualizada (`noticeVersion`), un resumen de los cambios o enlace para leer el texto completo (`270`), checkbox o confirmación explícita de aceptación y botón "Aceptar y continuar". El botón solo se habilita cuando el usuario marca la casilla de confirmación. | Composable probado con control reactivo de habilitación del botón y enlace al visor completo de privacidad. |
| **M290-R02** | **Contrato y Payload E017:** Enviar `POST /api/v1/users/me/consents` con cabecera `Authorization: Bearer <accessToken>`, cabecera opcional `Idempotency-Key: <UUID>` y payload JSON `{"noticeVersion": "2026-11-v2", "accepted": true}`. Recibir respuesta `201 Created` con payload: `{"consentId": "...", "recordedAt": "..."}`. | Modelado de DTOs acorde al catálogo V6.0.2 y verificación de contrato en pruebas unitarias. |
| **M290-R03** | **Prevención de Envíos Duplicados e Idempotencia (MO-PR04, MO-PR06):** Antes de enviar la solicitud, el cliente debe validar contra el historial local o en memoria (`280`) que la versión no haya sido ya aceptada por el usuario. Si ya fue aceptada, el formulario informa que el consentimiento está vigente sin disparar peticiones redundantes. Si el backend responde `409 Conflict`, se trata como idempotente y se sincroniza el estado. | Pruebas unitarias de detección previa de versión ya aceptada y control de respuestas 409. |
| **M290-R04** | **Sincronización Inmediata con Historial (280) y Perfil:** Tras recibir `201 Created`, el nuevo consentimiento debe incorporarse de inmediato al listado de `ConsentHistoryViewModel` (o caché en memoria) y cerrarse el modal de aviso pendiente, propagando la confirmación sin forzar re-login. | Pruebas de integración del ViewModel donde el registro generado se refleja en el historial local. |
| **M290-R05** | **Manejo Resiliente de Red y Errores (MO-PR08):** Si la solicitud falla por pérdida de conectividad o timeout, el diálogo permanece disponible con opción de reintentar, preservando la selección del usuario. La pérdida de red no debe impedir temporalmente el uso de funciones no afectadas salvo que la política legal exija bloqueo preventivo. | Pruebas de manejo de errores de red y reintentos en ViewModel. |
| **M290-R06** | **Simulación en `MockAuthApi` y Cobertura de Pruebas Unitarias:** La fuente simulada debe registrar el nuevo consentimiento en la memoria del mock, devolver `201 Created` con `consentId` estructurado tras retardo (400–800 ms), rechazar solicitudes con `accepted = false` (`400/422`), rechazar versiones ya existentes (`409`), responder error ante el centinela `net@error.test` y contar con suite de pruebas completa en `RecordConsentViewModelTest` y `MockAuthApiTest`. | Pruebas unitarias aprobadas con `kotlinx-coroutines-test` cubriendo éxito, duplicado, error de red y centinelas. |

## Escenarios de prueba y evidencia
- **Caso favorable:** Se publica versión `2026-11-v2` → usuario abre diálogo de actualización → marca "Acepto los términos actualizados" → pulsa "Aceptar y continuar" → llamada `POST /api/v1/users/me/consents` → respuesta `201 Created` con `consentId` y `recordedAt` → modal se cierra, se despliega Snackbar de confirmación y el historial `280` refleja el nuevo registro.
- **Caso de versión ya aceptada (local):** Usuario intenta registrar consentimiento de `2026-11-v1` que ya fue aceptada en el registro → validación local deshabilita el envío con aviso "Esta versión ya se encuentra registrada".
- **Caso de versión ya aceptada en servidor (409 Conflict):** Solicitud concurrente o reintento de red responde `409` → el repositorio interpreta el conflicto como éxito de idempotencia, actualiza el estado local y cierra el diálogo.
- **Caso de checkbox desmarcado:** Usuario abre diálogo pero no marca la casilla → el botón "Aceptar y continuar" permanece deshabilitado; no se emite ninguna petición de red.
- **Caso de fallo de transporte:** Caída de red durante el envío → Snackbar con opción "Reintentar", conservando el estado de la casilla marcada para permitir reintento ágil.
- **Regresión:** Compilación exitosa de `./gradlew assembleDebug` y aprobación continua de los 50 tests preexistentes de autenticación.
- **Comandos esperados (en Fase B):** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
- **Evidencia exigida:** ejecución real, reporte de cobertura de tests, trazabilidad a requisitos y registro en `memoria.md`.

## Riesgos y bloqueos
- **Falta de Backend real:** la persistencia transaccional y la prevención de colisiones en MongoDB Atlas (`consents.userId_1_noticeVersion_1`) reside en el servicio Go; en el cliente móvil se garantiza el contrato, la no duplicidad y la experiencia fluida MVI.
- **Interrupción de flujos de usuario:** la notificación de nueva versión del aviso debe presentarse sin degradar la usabilidad de la pantalla principal ni interrumpir operaciones financieras en curso.

## Criterio de salida de la unidad
Todas las tareas en `task.md` documentadas en estado pendiente (`- [ ]`); trazabilidad completa requisito → plan → tarea; ninguna ejecución de código en Fase A; sin cambios fuera del alcance autorizado.
