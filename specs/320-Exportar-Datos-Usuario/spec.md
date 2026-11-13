---
project: Nexum-Mobile
unit: 320-Exportar-Datos-Usuario
kind: technical-spec
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# SPEC — Exportación de Datos del Usuario (Export User Data)

## Propósito
Permitir al usuario titular autenticado solicitar una exportación estructurada de sus datos personales (perfil, movimientos, metas y presupuestos) bajo el contrato **NEXUM-E020** (`GET /api/v1/users/me/export`, soporte y consistencia `[S]`, catálogo V6.0.2), en formato JSON o CSV, en apoyo a los derechos de acceso y portabilidad, sin exponer datos de terceros ni conservar el contenido exportado en el dispositivo.

## Lugar en el plan SDD
- **Constitución:** [`.specify/memory/constitution.md`](../../.specify/memory/constitution.md) (vigente: MO-PR04 Backend como fuente autoritativa, MO-PR06 Privacidad y seguridad desde el diseño: minimizar datos, proteger tokens y evitar exposición en logs/capturas, MO-PR07 Usabilidad inclusiva Material 3, MO-PR08 Manejo explícito de conectividad con estados de carga, error y sin conexión, MO-PR10 Cero secretos, MO-PR18 Trazabilidad honesta).
- **Tecnología:** Kotlin / Jetpack Compose / MVI (Android nativo).
- **Contrato:** NEXUM-E020 del catálogo de endpoints de referencia V6.0.2 (`GET /api/v1/users/me/export` [S]).
- **Dependencias previas:** [`240-Perfil-Usuario`](../240-Perfil-Usuario/spec.md) (punto de entrada desde Perfil y modelo de usuario), [`270-Aviso-Privacidad-Vigente`](../270-Aviso-Privacidad-Vigente/spec.md) (sección de privacidad y derechos del titular), [`140-Login`](../140-Login/spec.md) (sesión activa requerida), [`150-Refresh-Token`](../150-Refresh-Token/spec.md) (renovación de token durante una exportación diferida), [`030-di-navegacion-ui`](../030-di-navegacion-ui/spec.md) (shell y navegación), [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md) (cliente HTTP y formato de errores) y [`070-permisos-privacidad-accesibilidad`](../070-permisos-privacidad-accesibilidad/spec.md) (accesibilidad y tratamiento de datos).
- **Naturaleza:** especificación técnica de derechos de acceso y portabilidad, consistencia y soporte (`[S]`).

## Alcance permitido
Acción "Exportar mis datos" dentro de la sección de privacidad de `ProfileScreen`, con selector de formato (JSON/CSV) y estados accesibles de progreso, resultado y error; modelado de DTOs del contrato E020 (`ExportReadyResponse`, `ExportPendingResponse`); ampliación de `AuthApi` y `AuthRepository` con la consulta de exportación; implementación simulada en `MockAuthApi` con contenido sintético del titular; ViewModel MVI (`ExportDataViewModel`) que impide envíos duplicados y limpia el contenido exportado al salir de la pantalla; y pruebas unitarias de la unidad.

## Exclusiones explícitas
No enviar ni aceptar `userId` por parámetro; no exportar datos de terceros ni de otras cuentas; no persistir el archivo exportado en almacenamiento local ni en caché de la aplicación; no registrar el contenido exportado en logs; no definir aquí el sondeo ni la vigencia del trabajo diferido (`exportJobId`), que quedan **por confirmar con Backend**; no implementar eliminación de cuenta (unidad `260-Eliminar-Cuenta`); no conectarse a un backend real en esta etapa; no publicar ni desplegar.

## Requisitos y criterios verificables
| ID | Requisito obligatorio | Aceptación mínima |
| --- | --- | --- |
| **M320-R01** | **Interfaz de exportación en Perfil:** Sección de privacidad en `ProfileScreen` con acción "Exportar mis datos", selector Material 3 de formato (`json` \| `csv`), botón con estado de progreso, mensaje de confirmación y acción de entrega del archivo; textos y etiquetas legibles por lector de pantalla, sin depender solo del color. | Composable probado con interacción en selector y botón, y verificación de semántica accesible. |
| **M320-R02** | **Contrato y payload E020:** Enviar `GET /api/v1/users/me/export?format=json\|csv` con cabecera `Authorization: Bearer <accessToken>`. Aceptar `200 OK` con el archivo exportado o `202 Accepted` con `exportJobId`. | DTOs modelados acorde al catálogo V6.0.2 y verificación de serialización en pruebas. |
| **M320-R03** | **Autorización y limitación de alcance (MO-PR04, MO-PR06):** La identidad se deriva estrictamente del JWT; la exportación comprende únicamente perfil, movimientos, metas y presupuestos del titular. El archivo y cualquier URL temporal no se registran en logs ni se comparten con otras cuentas. | Inspección de arquitectura y pruebas que confirmen ausencia de `userId` y de datos de terceros. |
| **M320-R04** | **Errores, estados y resiliencia (MO-PR08):** Manejar el sobre estándar `{"error":{"code","message","details","traceId"}}` para `400` de formato inválido, `401`, `403`, `404`, `409`, `422`, `429` y `500`; estados MVI explícitos de reposo, carga, archivo listo, trabajo diferido, error y sin conexión; reintento manual sin bucles automáticos; no mostrar éxito si la respuesta no lo confirma. | Pruebas unitarias de transición de estados y de mapeo de errores con `traceId` visible para soporte. |
| **M320-R05** | **Simulación en `MockAuthApi`:** La fuente simulada devuelve contenido sintético del titular tras retardo de 400–800 ms, en JSON o CSV según `format`, con montos como cadena decimal de dos dígitos, moneda `MXN` y fechas RFC 3339 UTC; soporta un escenario `202 Accepted` con `exportJobId` y el centinela `net@error.test` para fallo de red. | Casos de mock cubiertos en pruebas unitarias: JSON, CSV, diferido y fallo de red. |
| **M320-R06** | **Cobertura de pruebas unitarias:** Suite `ExportDataViewModelTest` y `MockAuthApiTest` con `kotlinx-coroutines-test` que cubra selección de formato, doble pulsación, respuesta `200`, respuesta `202`, errores HTTP y pérdida de conectividad, sin `Float`/`Double`. | Pruebas unitarias aprobadas y conteo total de tests reportado sin alterar los 50 preexistentes. |

## Escenarios de prueba y evidencia
- **Caso favorable JSON:** Usuario autenticado entra a Perfil > Privacidad > Exportar mis datos, elige `json` y confirma → `GET /api/v1/users/me/export?format=json` → `200 OK` con contenido sintético → estado "Archivo listo" y opción de entrega.
- **Caso favorable CSV:** Misma ruta con `csv` → respuesta `200 OK` con texto delimitado por comas → estado "Archivo listo" y nombre de archivo sugerido.
- **Caso de exportación diferida:** Respuesta `202 Accepted` con `exportJobId` → estado "Exportación en preparación" mostrando el identificador; el sondeo y la URL temporal quedan por confirmar con Backend.
- **Caso de doble pulsación:** Pulsaciones rápidas consecutivas → `isLoading = true` deshabilita la acción y no se emiten peticiones concurrentes.
- **Caso sin conexión:** Fallo de transporte → estado sin conexión con mensaje comprensible y acción "Reintentar"; no se inventa un archivo.
- **Caso de error de autorización:** `401`/`403` → mensaje genérico de sesión no autorizada, sin filtrar detalles internos y conservando el `traceId`.
- **Regresión:** Compilación exitosa de `./gradlew assembleDebug` y aprobación continua de los 50 tests preexistentes de autenticación.
- **Comandos esperados (en Fase B):** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
- **Evidencia exigida:** ejecución real, reporte de cobertura de tests, trazabilidad a requisitos y registro en `memoria.md`.

## Riesgos y bloqueos
- **Trabajo diferido:** el endpoint de consulta del estado de `exportJobId`, el nombre del campo de la URL temporal y su vigencia: **por confirmar con Backend**.
- **Formato del archivo:** tipos MIME exactos, cabecera de nombre de archivo y codificación del CSV: **por confirmar con Backend**.
- **Catálogo de campos:** la ficha nombra perfil, movimientos, metas y presupuestos, pero no el detalle de cada campo exportado: **por confirmar con Backend**.
- **Entrega del archivo en Android:** el mecanismo de guardado o compartición y su política de retención no están definidos por la ficha; se decide en Fase B sin persistir el contenido en la app.
- **Falta de Backend real:** la exportación se valida contra `MockAuthApi`; el comportamiento con datos reales en MongoDB Atlas queda fuera de esta unidad.

## Criterio de salida de la unidad
Todas las tareas en `task.md` documentadas en estado pendiente (`- [ ]`); trazabilidad completa requisito → plan → tarea; ninguna ejecución de código en Fase A; sin cambios fuera del alcance autorizado.
