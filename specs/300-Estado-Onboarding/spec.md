---
project: Nexum-Mobile
unit: 300-Estado-Onboarding
kind: technical-spec
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# SPEC — Consulta de Estado de Onboarding (Get Onboarding Status)

## Propósito
Permitir a la aplicación consultar al inicio de sesión o al abrir la app si el usuario titular autenticado ha completado el recorrido inicial y pasos explicativos bajo el contrato **NEXUM-E018** (`GET /api/v1/users/me/onboarding`, soporte y consistencia `[S]`, catálogo V6.0.2), evitando repetir pantallas de bienvenida en reinstalaciones o inicios en nuevos dispositivos, sin confundir los flags remotos con la concesión nativa de permisos en Android.

## Lugar en el plan SDD
- **Constitución:** [`.specify/memory/constitution.md`](../../.specify/memory/constitution.md) (vigente: MO-PR04 Backend como fuente autoritativa, MO-PR06 Privacidad y seguridad desde el diseño: "derivación por token sin aceptar userId ajeno", MO-PR07 Usabilidad inclusiva Material 3, MO-PR08 Manejo explícito de conectividad y estados sin conexión, MO-PR09 Cuidado con permisos: "el permiso real de micrófono/notificaciones lo gobierna Android y debe comprobarse allí; los flags remotos no suplantan autorizaciones del sistema operativo").
- **Tecnología:** Kotlin / Jetpack Compose / MVI (Android nativo).
- **Contrato:** NEXUM-E018 del catálogo de endpoints de referencia V6.0.2 (`GET /api/v1/users/me/onboarding` [S]).
- **Dependencias previas:** [`140-Login`](../140-Login/spec.md) (sesión activa requerida), [`150-Refresh-Token`](../150-Refresh-Token/spec.md) (renovación de token si expira al inicio), [`030-di-navegacion-ui`](../030-di-navegacion-ui/spec.md) (shell de navegación y enrutador inicial), [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md) (cliente HTTP) y [`070-permisos-privacidad-accesibilidad`](../070-permisos-privacidad-accesibilidad/spec.md) (gestión de permisos nativos y accesibilidad).
- **Naturaleza:** especificación técnica de ciclo de vida de bienvenida, consistencia y soporte (`[S]`).

## Alcance permitido
Consulta y resolución de estado de onboarding en el arranque autenticado, DTOs del contrato E018 (`OnboardingStatusResponse`), modelo de dominio (`OnboardingStatus`), ampliación de `AuthApi` y `AuthRepository`, implementación simulada en `MockAuthApi` con retardo (200–500 ms) y escenarios de onboarding completo/pendiente, ViewModel o interceptor MVI (`OnboardingRoutingViewModel`) con soporte de caché local para arranque offline sin bloqueos, y pruebas unitarias de la unidad.

## Exclusiones explícitas
No mutar ni marcar el onboarding como completado en este endpoint (reservado para `NEXUM-E019 PATCH /api/v1/users/me/onboarding`); no asumir que un flag remoto concede permisos en el sistema operativo Android (`RECORD_AUDIO`, `POST_NOTIFICATIONS`); no requerir `userId` por parámetro; no conectarse a un backend real en esta etapa; no publicar ni desplegar.

## Requisitos y criterios verificables
| ID | Requisito obligatorio | Aceptación mínima |
| --- | --- | --- |
| **M300-R01** | **Disparador y Enrutamiento Inicial en UI (Arranque de App):** Al autenticar o restaurar sesión, el enrutador de la aplicación debe consultar el estado de onboarding del usuario para decidir si navegar hacia la pantalla de bienvenida/onboarding (`OnboardingScreen`) o directamente a la pantalla principal (`HomeScreen`/`DashboardScreen`), sin parpadeos visuales ni pantallas intermedias no deseadas. | Enrutador probado bajo ciclo de vida de Compose con navegación determinista basada en el estado. |
| **M300-R02** | **Contrato y Payload E018:** Enviar `GET /api/v1/users/me/onboarding` con cabecera `Authorization: Bearer <accessToken>`. Recibir respuesta exitosa `200 OK` con payload JSON: `{"stepsCompleted": ["welcome", "financial_profile"], "finishedAt": "2026-11-02T17:35:00Z"}` (o `finishedAt: null` si está incompleto). | Modelado de DTOs acorde al catálogo V6.0.2 y verificación de serialización en pruebas. |
| **M300-R03** | **Gobernanza de Permisos de Android vs Flags Remotos (MO-PR09):** La presencia de pasos como `"permissions_info"` en `stepsCompleted` indica únicamente que el usuario vio las pantallas informativas en el servidor; el cliente móvil **debe verificar siempre el permiso real en Android** mediante las APIs del sistema operativo antes de activar micrófono o notificaciones, sin inferir autorización legal a partir del payload remoto. | Inspección de arquitectura y pruebas unitarias que garanticen el desacoplamiento entre el flag remoto y el permiso nativo. |
| **M300-R04** | **Caché Local y Resiliencia sin Conexión (MO-PR08):** El estado de onboarding verificado debe persistirse localmente en memoria/preferencias seguras. Si la app se inicia sin conexión a red, el cliente utiliza el estado local para permitir el acceso al Dashboard sin bloquear al usuario en bucles de carga o errores de bienvenida. | Pruebas unitarias de resolución offline donde el estado persistido dirige al usuario a Inicio sin depender de red inmediata. |
| **M300-R05** | **Estados MVI y Derivación Anti-IDOR (MO-PR04, MO-PR06):** El endpoint no acepta `userId` por parámetro; la identidad se deriva estrictamente del token JWT. El estado MVI (`OnboardingRoutingUiState`) debe manejar `Checking`, `RouteToHome`, `RouteToOnboarding` y `Error`, permitiendo reintento manual si la resolución inicial falla y no hay caché previa. | Pruebas unitarias de transiciones de estado en `OnboardingRoutingViewModelTest`. |
| **M300-R06** | **Simulación en `MockAuthApi` y Cobertura de Pruebas Unitarias:** La fuente simulada debe devolver el estado de onboarding correspondiente al usuario activo (incompleto para nuevo usuario, completo con `finishedAt` para usuario recurrente), responder tras retardo (200–500 ms), soportar el centinela `net@error.test` y disponer de suite completa de pruebas unitarias en `OnboardingRoutingViewModelTest` y `MockAuthApiTest`. | Pruebas unitarias aprobadas con `kotlinx-coroutines-test` cubriendo usuario nuevo, recurrente, fallo de red y fallback local. |

## Escenarios de prueba y evidencia
- **Caso favorable usuario nuevo:** Usuario recién registrado abre la app → llamada `GET /api/v1/users/me/onboarding` → respuesta `200 OK` con `stepsCompleted: ["welcome"]` y `finishedAt: null` → la UI enruta hacia `OnboardingScreen` para continuar el recorrido explicativo.
- **Caso favorable usuario recurrente:** Usuario con onboarding previo inicia sesión en nuevo teléfono → respuesta con `finishedAt: "2026-11-02T17:35:00Z"` → la UI enruta directamente a `HomeScreen`, evitando repetir pantallas de bienvenida.
- **Caso sin conexión con caché local:** Arranque sin red con estado previo `finishedAt != null` guardado en el dispositivo → la app accede a Inicio sin demoras ni pantallas de error.
- **Caso de fallo de transporte sin caché:** Dispositivo nuevo sin conexión en el primer arranque tras login → estado `Error` con botón de reintento ("No se pudo verificar el estado de inicio. Reintentar") para no saltar pasos críticos de manera involuntaria.
- **Regresión:** Compilación exitosa de `./gradlew assembleDebug` y aprobación continua de los 50 tests preexistentes de autenticación.
- **Comandos esperados (en Fase B):** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
- **Evidencia exigida:** ejecución real, reporte de cobertura de tests, trazabilidad a requisitos y registro en `memoria.md`.

## Riesgos y bloqueos
- **Falta de Backend real:** la persistencia del objeto `profiles.onboarding` reside en MongoDB Atlas; en el móvil se asegura el contrato DTO, la resiliencia offline y la separación estricta frente a los permisos del SO.
- **Riesgo de navegación en bucle:** una deserialización incorrecta de `finishedAt` podría provocar que un usuario quede atrapado en el onboarding; se mitiga mediante validación estricta de nulabilidad (`isFinished = !finishedAt.isNullOrBlank()`).

## Criterio de salida de la unidad
Todas las tareas en `task.md` documentadas en estado pendiente (`- [ ]`); trazabilidad completa requisito → plan → tarea; ninguna ejecución de código en Fase A; sin cambios fuera del alcance autorizado.
