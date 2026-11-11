---
project: Nexum-Mobile
unit: 280-Historial-Consentimientos
kind: technical-spec
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# SPEC — Historial de Consentimientos del Usuario (Consent History)

## Propósito
Permitir al usuario titular autenticado consultar la bitácora auditable de los avisos de privacidad y términos que ha aceptado bajo el contrato **NEXUM-E016** (`GET /api/v1/users/me/consents`, soporte y consistencia `[S]`, catálogo V6.0.2), garantizando la inmutabilidad de los registros históricos, demostrando qué versiones legales rigen su cuenta y en qué momento exacto fueron aceptadas, sin permitir alteraciones retroactivas.

## Lugar en el plan SDD
- **Constitución:** [`.specify/memory/constitution.md`](../../.specify/memory/constitution.md) (vigente: MO-PR04 Backend como fuente autoritativa, MO-PR06 Privacidad y seguridad desde el diseño: "no cambiar retroactivamente un consentimiento; derivación estricta por token sin aceptar userId ajeno", MO-PR07 Usabilidad inclusiva Material 3, MO-PR08 Manejo explícito de conectividad y estados sin conexión).
- **Tecnología:** Kotlin / Jetpack Compose / MVI (Android nativo).
- **Contrato:** NEXUM-E016 del catálogo de endpoints de referencia V6.0.2 (`GET /api/v1/users/me/consents` [S]).
- **Dependencias previas:** [`270-Aviso-Privacidad-Vigente`](../270-Aviso-Privacidad-Vigente/spec.md) (versiones de aviso y texto legal), [`240-Perfil-Usuario`](../240-Perfil-Usuario/spec.md) (punto de acceso desde Perfil > Privacidad), [`140-Login`](../140-Login/spec.md) (sesión activa requerida), [`150-Refresh-Token`](../150-Refresh-Token/spec.md) (renovación de token) y [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md) (cliente HTTP).
- **Naturaleza:** especificación técnica de auditoría de consentimiento, privacidad y soporte (`[S]`).

## Alcance permitido
Pantalla accesible `ConsentHistoryScreen` en Jetpack Compose invocable desde `ProfileScreen` ("Privacidad > Historial de aceptación"), modelado de DTOs del contrato E016 (`ConsentItemResponse`), modelo de dominio inmutable (`ConsentRecord`), ampliación de `AuthApi` y `AuthRepository`, implementación simulada en `MockAuthApi` con historial derivado del registro del usuario activo, ViewModel MVI (`ConsentHistoryViewModel`) con soporte de estados `Loading`, `Content`, `Empty`, `Refreshing` y `Error`, formateo de marcas temporales RFC 3339 a la zona horaria local del dispositivo, y pruebas unitarias de la unidad.

## Exclusiones explícitas
No permitir edición, reemplazo ni eliminación de registros de consentimiento en la interfaz (los consentimientos son inmutables por diseño); no registrar nuevas aceptaciones en este endpoint (reservado para `NEXUM-E017 POST /api/v1/users/me/consents`); no enviar ni aceptar identificadores de usuario (`userId`) por parámetro de ruta o query; no conectarse a un backend real en esta etapa; no publicar ni desplegar.

## Requisitos y criterios verificables
| ID | Requisito obligatorio | Aceptación mínima |
| --- | --- | --- |
| **M280-R01** | **Disparador y Pantalla de Historial en UI (Perfil > Privacidad > Historial):** Componente Compose `ConsentHistoryScreen` navegable desde `ProfileScreen` ("Privacidad > Historial de aceptación"). Presenta barra superior con botón de retorno accesible, banner informativo de auditoría inmutable y listado ordenado cronológicamente de las versiones aceptadas. | Composable probado con navegación de entrada y salida, respetando directrices Material 3. |
| **M280-R02** | **Contrato y Payload E016:** Enviar `GET /api/v1/users/me/consents` con cabecera `Authorization: Bearer <accessToken>`. Recibir respuesta exitosa `200 OK` con un array JSON: `[{"noticeVersion": "2026-11-v1", "acceptedAt": "2026-11-02T17:30:00Z", "source": "registration"}]`. | Modelado de DTOs acorde al catálogo V6.0.2 y verificación de serialización en pruebas. |
| **M280-R03** | **Derivación Estricta de Identidad e Inmutabilidad (MO-PR04, MO-PR06):** El endpoint no acepta `userId` por parámetro; la identidad se deriva estrictamente del token JWT (anti-IDOR). Los registros expuestos son de estricta solo lectura; la interfaz no debe incluir acciones que simulen revocación o borrado individual de consentimientos pasados. | Inspección de la firma de red y ausencia de controles destructivos o editables en la UI. |
| **M280-R04** | **Formateo Temporal en Huso Horario Local (MO-PR07):** La marca temporal `acceptedAt` recibida en UTC (RFC 3339) debe formatearse a la zona horaria del dispositivo del usuario (ej. `dd/MM/yyyy HH:mm`) con traducción semántica de la fuente (`source`: "Registro inicial", "Actualización", etc.). | Pruebas unitarias de formateo temporal que verifiquen conversión correcta de UTC a hora local. |
| **M280-R05** | **Estados MVI y Resiliencia de Red (MO-PR08):** El estado `ConsentHistoryUiState` debe contemplar `Loading`, `Content`, `Empty` (si no existen registros), `Refreshing` (soporte de pull-to-refresh) y `Error`. Si la red falla, se conservan los datos previos en memoria con botón de reintento. | Pruebas de transiciones de estado en `ConsentHistoryViewModelTest`. |
| **M280-R06** | **Simulación en `MockAuthApi` y Cobertura de Pruebas Unitarias:** La fuente simulada debe retornar el registro histórico inicial vinculado a la cuenta activa tras retardo (300–700 ms), soportar el centinela de red `net@error.test` y disponer de suite completa de pruebas unitarias (`ConsentHistoryViewModelTest`, `MockAuthApiTest`) con `kotlinx-coroutines-test`. | Pruebas unitarias aprobadas cubriendo respuesta exitosa, lista vacía, pull-to-refresh y fallo de transporte. |

## Escenarios de prueba y evidencia
- **Caso favorable con historial:** Usuario entra a Perfil > Privacidad > Historial de aceptación → llamada `GET /api/v1/users/me/consents` → respuesta `200 OK` con un registro (`version: "2026-11-v1"`, `source: "registration"`) → se despliega la tarjeta con la versión, fecha local formateada y etiqueta "Aceptado en registro inicial".
- **Caso con múltiples consentimientos:** Respuesta contiene dos o más versiones (ej. registro inicial + actualización posterior) → se presentan en orden cronológico descendente (el más reciente primero).
- **Caso de actualización (Pull-to-refresh):** Gesto de deslizamiento hacia abajo → `isRefreshing = true` → recarga de la lista de consentimientos sin parpadeo visual.
- **Caso sin registros (Empty state):** Respuesta con lista vacía `[]` → visualización de ilustración o texto accesible ("No hay registros de consentimiento registrados").
- **Caso de fallo de transporte con datos previos:** Caída de red durante refresco → se mantienen los datos cargados previamente en memoria y se muestra Snackbar informativa con opción de reintentar.
- **Regresión:** Compilación exitosa de `./gradlew assembleDebug` y aprobación continua de los 50 tests preexistentes de autenticación.
- **Comandos esperados (en Fase B):** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
- **Evidencia exigida:** ejecución real, reporte de cobertura de tests, trazabilidad a requisitos y registro en `memoria.md`.

## Riesgos y bloqueos
- **Falta de Backend real:** en producción, la inmutabilidad y la persistencia auditable residen en la colección `consents` de MongoDB Atlas vinculada al `userId`; en el cliente móvil se garantiza la representación veraz y la ausencia de modificaciones locales no auditadas.
- **Coherencia de fuentes (`source`):** el backend define valores cerrados para `source` (`"registration"`, `"modal_prompt"`, `"profile_settings"`); la UI debe manejar un fallback neutro si recibe una fuente desconocida.

## Criterio de salida de la unidad
Todas las tareas en `task.md` documentadas en estado pendiente (`- [ ]`); trazabilidad completa requisito → plan → tarea; ninguna ejecución de código en Fase A; sin cambios fuera del alcance autorizado.
