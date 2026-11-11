---
project: Nexum-Mobile
unit: 270-Aviso-Privacidad-Vigente
kind: functional-spec
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# SPEC — Consulta del Aviso de Privacidad Vigente (Current Privacy Notice)

## Propósito
Permitir a los usuarios (tanto aspirantes en proceso de registro como titulares autenticados en perfil) consultar el aviso de privacidad legalmente vigente, versionado y aprobado bajo el contrato **NEXUM-E015** (`GET /api/v1/privacy/notices/current`, función de maqueta `[M]`, catálogo V6.0.2), garantizando información veraz sobre el tratamiento y custodia de datos en backend en lugar del texto simulado de la demo local, y proveyendo el identificador de versión (`version`) requerido para el consentimiento auditable en el alta de cuenta.

## Lugar en el plan SDD
- **Constitución:** [`.specify/memory/constitution.md`](../../.specify/memory/constitution.md) (vigente: MO-PR04 Backend como fuente autoritativa, MO-PR06 Privacidad y seguridad desde el diseño: "información legal versionada y aprobada; no afirmar que todo queda en dispositivo cuando exista servidor", MO-PR07 Usabilidad inclusiva Material 3, MO-PR08 Manejo explícito de conectividad y estados sin conexión, MO-PR17 Diseño fiel al prototipo: "Registro > Ver aviso; Perfil > Privacidad").
- **Tecnología:** Kotlin / Jetpack Compose / MVI (Android nativo).
- **Contrato:** NEXUM-E015 del catálogo de endpoints de referencia V6.0.2 (`GET /api/v1/privacy/notices/current` [M]).
- **Dependencias previas:** [`130-Registro-Usuario`](../130-Registro-Usuario/spec.md) (enlace "Ver aviso" y suministro de `privacyNoticeVersion` en el alta), [`240-Perfil-Usuario`](../240-Perfil-Usuario/spec.md) (acceso desde Perfil > Privacidad), [`030-di-navegacion-ui`](../030-di-navegacion-ui/spec.md) (shell modal o navegación Compose), [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md) (cliente HTTP) y [`070-permisos-privacidad-accesibilidad`](../070-permisos-privacidad-accesibilidad/spec.md) (políticas de privacidad base y accesibilidad).
- **Naturaleza:** especificación funcional y de cumplimiento legal de privacidad (`[M]`).

## Alcance permitido
Pantalla o diálogo modal navegable `PrivacyNoticeScreen` en Jetpack Compose accesible desde `RegisterScreen` ("Ver aviso") y desde `ProfileScreen` ("Privacidad > Aviso de privacidad"), soporte de parámetro opcional de consulta `locale` (`es-MX` por defecto), modelado de DTOs del contrato E015 (`PrivacyNoticeResponse`), ampliación de `AuthApi` (o `PrivacyApi`) y repositorios, implementación simulada en `MockAuthApi` con retardo (200–500 ms) y texto legal aprobado, ViewModel MVI (`PrivacyNoticeViewModel`) con estados `Loading`, `Content`, `Error` y fallback offline mediante copia local empaquetada, control de lectura con scroll accesible y pruebas unitarias de la unidad.

## Exclusiones explícitas
No registrar consentimientos de versiones posteriores en esta unidad (reservado para `NEXUM-E017 POST /api/v1/users/me/consents`); no consultar el historial de aceptaciones del usuario (reservado para `NEXUM-E016 GET /api/v1/users/me/consents`); no exigir token de autenticación obligatorio para consultar este endpoint público (debe poder consumirse antes del inicio de sesión); no conectarse a un backend real en esta etapa; no publicar ni desplegar.

## Requisitos y criterios verificables
| ID | Requisito obligatorio | Aceptación mínima |
| --- | --- | --- |
| **M270-R01** | **Disparadores de Acceso y Visor en UI (Registro y Perfil):** Componente accesible en Jetpack Compose (`PrivacyNoticeScreen` o diálogo modal a pantalla completa) invocable desde el enlace "Ver aviso" de `RegisterScreen` y desde la opción "Privacidad" en `ProfileScreen`. Incluye barra superior con título, botón de retroceso/cierre accesible y área de lectura con desplazamiento vertical fluido. | Composable probado con navegación de apertura y cierre, y verificación de jerarquía semántica. |
| **M270-R02** | **Contrato y Payload E015:** Enviar `GET /api/v1/privacy/notices/current?locale=es-MX` (sin requerir cabecera de autenticación obligatoria). Recibir respuesta exitosa `200 OK` con payload JSON: `{"version": "2026-11-v1", "title": "Aviso de Privacidad Integral de Nexum", "body": "...", "publishedAt": "2026-11-02T00:00:00Z"}`. | Modelado de DTOs acorde al catálogo V6.0.2 y verificación de serialización en pruebas. |
| **M270-R03** | **Veracidad del Tratamiento y Suministro de Versión (MO-PR04, MO-PR06):** El documento presentado debe reflejar el aviso del servidor y eliminar la afirmación de que "todo se almacena exclusivamente en el teléfono" cuando opera con backend. La versión recuperada (`version`) debe quedar disponible en memoria para que `RegisterViewModel` la envíe en `POST /auth/register` (`privacyNoticeVersion`). | Pruebas de integración del ViewModel donde la versión recibida coincide con la propagada al flujo de registro. |
| **M270-R04** | **Resiliencia Offline y Copia Local de Contingencia (MO-PR08):** Si la consulta de red falla o el dispositivo está fuera de línea, la aplicación debe cargar una copia local de contingencia del aviso aprobada y empaquetada en la app, mostrando una indicación informativa ("Aviso cargado desde copia local") sin impedir la lectura ni el avance del usuario. | Pruebas unitarias que verifiquen la carga exitosa del fallback local ante error de red. |
| **M270-R05** | **Accesibilidad y Formato de Lectura (MO-PR07):** El cuerpo del aviso debe presentarse con tipografía accesible Material 3 (tamaños mínimos de lectura, contraste conforme a WCAG AA), soporte de lectores de pantalla (TalkBack) mediante encabezados semánticos, y retención de la posición de scroll durante recomposiciones. | Inspección de accesibilidad y pruebas unitarias de estados MVI. |
| **M270-R06** | **Simulación en `MockAuthApi` y Cobertura de Pruebas Unitarias:** La fuente simulada debe responder con el aviso legal estructurado tras retardo realista (200–500 ms), soportar el centinela `locale=net_error` para validar la respuesta ante caídas de red, y disponer de suite completa de pruebas unitarias (`PrivacyNoticeViewModelTest`, `MockAuthApiTest`) con `kotlinx-coroutines-test`. | Pruebas unitarias aprobadas cubriendo éxito, fallback offline, soporte de locale y centinelas de error. |

## Escenarios de prueba y evidencia
- **Caso favorable desde Registro:** Usuario en `RegisterScreen` presiona "Ver aviso" → llamada `GET /api/v1/privacy/notices/current?locale=es-MX` → respuesta `200 OK` con versión `2026-11-v1` → se despliega `PrivacyNoticeScreen` con título, fecha y texto legal completo → usuario lee y presiona "Cerrar" → vuelve a Registro con la versión vinculada para el envío.
- **Caso favorable desde Perfil:** Usuario autenticado abre "Privacidad > Aviso de privacidad" → llamada `GET /api/v1/privacy/notices/current` → lectura del aviso vigente con indicación de fecha de publicación.
- **Caso sin conexión / Fallo de transporte:** Pérdida de red al abrir el aviso → la petición falla → el repositorio activa la copia local de respaldo empaquetada → la pantalla muestra el texto del aviso de contingencia con mensaje "Aviso de privacidad (copia local de respaldo)" y opción de reintento.
- **Caso de cambio de configuración (rotación/recomposición):** Usuario rota la pantalla mientras lee → el estado MVI conserva el contenido y la posición relativa de scroll sin recargar la llamada de red.
- **Regresión:** Compilación exitosa de `./gradlew assembleDebug` y aprobación continua de los 50 tests preexistentes de autenticación.
- **Comandos esperados (en Fase B):** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
- **Evidencia exigida:** ejecución real, reporte de cobertura de tests, trazabilidad a requisitos y registro en `memoria.md`.

## Riesgos y bloqueos
- **Falta de Backend real:** la fuente autoritativa del texto legal en producción será el servicio en Go con almacenamiento versionado; en el cliente móvil se garantiza el contrato, la visualización accesible y el fallback empaquetado.
- **Alineación con el registro:** `130-Registro-Usuario` utiliza `privacyNoticeVersion: "2026-11-v1"`; debe mantenerse exacta coincidencia semántica entre el contrato E015 y el DTO de registro.

## Criterio de salida de la unidad
Todas las tareas en `task.md` documentadas en estado pendiente (`- [ ]`); trazabilidad completa requisito → plan → tarea; ninguna ejecución de código en Fase A; sin cambios fuera del alcance autorizado.
