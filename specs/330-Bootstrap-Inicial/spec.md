---
project: Nexum-Mobile
unit: 330-Bootstrap-Inicial
kind: technical-spec
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# SPEC — Carga Inicial Agregada (Bootstrap)

## Propósito
Permitir al usuario titular autenticado obtener una instantánea coherente de los módulos de lectura más frecuentes al entrar a Nexum —perfil, resumen financiero, racha, configuración y conteos— bajo el contrato **NEXUM-E021** (`GET /api/v1/bootstrap`, soporte y consistencia `[S]`, catálogo V6.0.2), para que Inicio, Perfil, metas y notificaciones carguen con menos llamadas y menor latencia, sin sustituir los endpoints especializados de escritura.

## Lugar en el plan SDD
- **Constitución:** [`.specify/memory/constitution.md`](../../.specify/memory/constitution.md) (vigente: MO-PR04 Backend como fuente autoritativa, MO-PR05 Veracidad del dinero: distinguir estimaciones y registros confirmados, MO-PR06 Privacidad y seguridad desde el diseño: identidad por token sin aceptar `userId` ajeno, MO-PR07 Usabilidad inclusiva Material 3, MO-PR08 Manejo explícito de conectividad con estados de carga, error y sin conexión, MO-PR12 Responsabilidades desacopladas).
- **Tecnología:** Kotlin / Jetpack Compose / MVI (Android nativo).
- **Contrato:** NEXUM-E021 del catálogo de endpoints de referencia V6.0.2 (`GET /api/v1/bootstrap` [S]).
- **Dependencias previas:** [`240-Perfil-Usuario`](../240-Perfil-Usuario/spec.md) (modelo de perfil y presentación), [`300-Estado-Onboarding`](../300-Estado-Onboarding/spec.md) (enrutamiento de arranque autenticado), [`140-Login`](../140-Login/spec.md) (sesión activa requerida), [`150-Refresh-Token`](../150-Refresh-Token/spec.md) (renovación de token al iniciar), [`030-di-navegacion-ui`](../030-di-navegacion-ui/spec.md) (shell y navegación) y [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md) (cliente HTTP y formato de errores).
- **Naturaleza:** especificación técnica de carga agregada de solo lectura, consistencia y soporte (`[S]`).

## Alcance permitido
Carga inicial de `HomeScreen` en una sola petición agregada, DTOs del contrato E021 (`BootstrapResponse`), modelo de dominio (`BootstrapSnapshot`), ampliación de `AuthApi` y `AuthRepository`, implementación simulada en `MockAuthApi` con instantánea determinista del usuario activo, ViewModel MVI (`HomeViewModel`) con selección opcional de secciones mediante `include`, esqueleto de carga, degradación parcial y reintento, y pruebas unitarias de la unidad.

## Exclusiones explícitas
No usar el agregador para escribir, mutar ni confirmar operaciones (las escrituras conservan sus endpoints especializados); no enviar ni aceptar `userId` por parámetro; no implementar aquí los endpoints de resumen financiero, perfil o notificaciones, que mantienen sus propias unidades; no inferir datos ausentes de `streak`, `config` o `counts`; no conectarse a un backend real en esta etapa; no publicar ni desplegar.

## Requisitos y criterios verificables
| ID | Requisito obligatorio | Aceptación mínima |
| --- | --- | --- |
| **M330-R01** | **Interfaz de carga inicial agregada:** `HomeScreen` presenta en una sola carga el saludo con el nombre del perfil, la tarjeta de importes del resumen, la racha y los conteos disponibles, con esqueleto de carga, mensaje de error y acción "Reintentar"; etiquetas legibles y estados no dependientes solo del color. | Composable probado con estados de carga, contenido y error; verificación de semántica accesible. |
| **M330-R02** | **Contrato y payload E021:** Enviar `GET /api/v1/bootstrap` con cabecera `Authorization: Bearer <accessToken>` y parámetro opcional `include=summary,profile,preferences`. Recibir `200 OK` con `profile`, `summary`, `streak`, `config` y `counts`. | DTOs modelados acorde al catálogo V6.0.2 y verificación de serialización en pruebas. |
| **M330-R03** | **Agregador de solo lectura (MO-PR04, MO-PR06):** El bootstrap no sustituye endpoints especializados de escritura ni confirma operaciones; la identidad se deriva del JWT y la instantánea corresponde solo al titular. La UI no habilita acciones de escritura a partir de este payload. | Inspección de arquitectura y pruebas que confirmen ausencia de `userId` y de rutas de escritura. |
| **M330-R04** | **Degradación parcial y resiliencia (MO-PR08):** Si una sección llega nula o el agregador falla, la UI muestra lo disponible sin inventar datos, conserva la última instantánea local etiquetada como tal y ofrece reintento manual; estados MVI de carga, contenido, parcial, error y sin conexión; manejo del sobre `{"error":{"code","message","details","traceId"}}`. | Pruebas unitarias de secciones nulas, error total, sin conexión y reintento. |
| **M330-R05** | **Simulación en `MockAuthApi`:** La fuente simulada devuelve una instantánea coherente del usuario activo tras retardo de 250–600 ms, respeta el filtro `include`, usa montos como cadena decimal con moneda `MXN`, fechas RFC 3339 UTC y soporta el centinela `net@error.test`. | Casos de mock cubiertos con y sin `include`, además de fallo de red. |
| **M330-R06** | **Cobertura de pruebas unitarias:** Suite `HomeViewModelTest` y `MockAuthApiTest` con `kotlinx-coroutines-test` que cubra carga completa, carga filtrada, secciones parciales, doble carga, error HTTP y pérdida de conectividad. | Pruebas unitarias aprobadas y conteo total de tests reportado sin alterar los 50 preexistentes. |

## Escenarios de prueba y evidencia
- **Caso favorable completo:** Usuario autenticado abre Inicio → `GET /api/v1/bootstrap` → `200 OK` con perfil, resumen, racha, configuración y conteos → `HomeScreen` muestra nombre, importes, racha y conteos en una sola composición.
- **Caso con `include` filtrado:** `include=summary,profile` → la respuesta omite secciones no solicitadas → la UI muestra solo lo recibido sin bloquearse.
- **Caso de degradación parcial:** `summary` nulo y `profile` presente → estado parcial con saludo y tarjeta de resumen en modo no disponible; no se muestran ceros inventados.
- **Caso de fallo total:** `500` o error de red → estado de error o sin conexión con acción "Reintentar"; se conserva la última instantánea marcada como anterior.
- **Caso de doble carga:** Navegación rápida entre pantallas → una sola petición en curso; las repeticiones se ignoran mientras `isLoading = true`.
- **Regresión:** Compilación exitosa de `./gradlew assembleDebug` y aprobación continua de los 50 tests preexistentes de autenticación.
- **Comandos esperados (en Fase B):** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
- **Evidencia exigida:** ejecución real, reporte de cobertura de tests, trazabilidad a requisitos y registro en `memoria.md`.

## Riesgos y bloqueos
- **Estructura interna de `streak`, `config` y `counts`:** la ficha solo nombra las secciones; campos y tipos exactos **por confirmar con Backend**. La simulación usa claves sintéticas explícitamente provisionales.
- **Contenido de `summary`:** la ficha no detalla sus campos; se reutilizan los importes documentados del resumen financiero, **por confirmar con Backend**.
- **Semántica de `include`:** valores admitidos, orden y comportamiento ante secciones desconocidas: **por confirmar con Backend**.
- **Consistencia de la instantánea:** el momento de corte y la coherencia entre módulos no están definidos; se documenta como fotografía de lectura y no como confirmación transaccional.
- **Falta de Backend real:** la carga agregada se valida contra `MockAuthApi`; el comportamiento con MongoDB Atlas queda fuera de esta unidad.

## Criterio de salida de la unidad
Todas las tareas en `task.md` documentadas en estado pendiente (`- [ ]`); trazabilidad completa requisito → plan → tarea; ninguna ejecución de código en Fase A; sin cambios fuera del alcance autorizado.
