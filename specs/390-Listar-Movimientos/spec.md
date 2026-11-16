---
project: Nexum-Mobile
unit: 390-Listar-Movimientos
kind: technical-spec
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# SPEC — Listado de Movimientos (List Movements)

## Propósito
Permitir al usuario titular autenticado consultar el historial de sus movimientos ordenado por fecha bajo el contrato **NEXUM-E027** (`GET /api/v1/movements`, `[M]`, catálogo V6.0.2), con filtros por tipo (`all`, `ant`, `fixed`, `saving`) y rango de fechas, paginación explícita y totales, preservando la fecha real del evento y su origen, sin mezclar datos de otras cuentas.

## Lugar en el plan SDD
- **Constitución:** [`.specify/memory/constitution.md`](../../.specify/memory/constitution.md) (vigente: MO-PR04 Backend como fuente autoritativa, MO-PR05 Veracidad del dinero: fechas y totales no se inventan, MO-PR06 Privacidad y seguridad desde el diseño: identidad por token sin aceptar `userId` ajeno, MO-PR07 Usabilidad inclusiva Material 3, MO-PR08 Manejo explícito de conectividad, MO-PR12 Responsabilidades desacopladas).
- **Tecnología:** Kotlin / Jetpack Compose / MVI (Android nativo).
- **Contrato:** NEXUM-E027 del catálogo de endpoints de referencia V6.0.2 (`GET /api/v1/movements` [M]).
- **Dependencias previas:** [`340-Catalogo-Categorias`](../340-Catalogo-Categorias/spec.md) (nombres e identificadores de categoría), [`380-Resumen-Financiero`](../380-Resumen-Financiero/spec.md) (importes coherentes con el resumen mensual), [`140-Login`](../140-Login/spec.md) (sesión activa), [`150-Refresh-Token`](../150-Refresh-Token/spec.md) (renovación de token), [`030-di-navegacion-ui`](../030-di-navegacion-ui/spec.md) (shell y navegación) y [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md) (cliente HTTP y formato de errores).
- **Naturaleza:** especificación técnica de lectura paginada de datos financieros privados.

## Alcance permitido
Pantalla `MovementsScreen` con pestañas Todos, Hormiga, Obligatorios y Ahorro, filtros de rango de fechas, DTOs del contrato E027 (`MovementItemResponse`, `MovementsPageResponse`), modelo de dominio (`MovementItem`, `MovementsPage`), ampliación de `AuthApi` y `AuthRepository`, implementación simulada en `MockAuthApi` con historial sintético paginado, ViewModel MVI (`MovementsListViewModel`) con carga inicial, carga de página siguiente y cancelación al cambiar filtros, y pruebas unitarias de la unidad.

## Exclusiones explícitas
No consultar el detalle individual aquí (unidad `410-Detalle-Movimiento`); no registrar movimientos (unidad `400-Registrar-Movimiento`) ni editarlos (unidad `420-Editar-Movimiento`); no enviar ni aceptar `userId` por parámetro; no alterar la fecha real del evento ni su origen; no recalcular totales ni importes del servidor; no inventar campos internos de `pagination` o `totals`; no conectarse a un backend real en esta etapa; no publicar ni desplegar.

## Requisitos y criterios verificables
| ID | Requisito obligatorio | Aceptación mínima |
| --- | --- | --- |
| **M390-R01** | **Listado con pestañas y filtros:** `MovementsScreen` con pestañas Todos, Hormiga, Obligatorios y Ahorro, filtros de rango de fechas, celdas con concepto, importe en dos decimales, categoría, fecha real del evento y origen; estados de carga inicial, carga de página siguiente, vacío y error; etiquetas legibles y acciones de al menos 48 dp. | Composable probado con cambio de pestaña, rango aplicado, elemento vacío y estado de carga. |
| **M390-R02** | **Contrato y payload E027:** Enviar `GET /api/v1/movements?type=all\|ant\|fixed\|saving&from=<fecha>&to=<fecha>&page=<n>&limit=<n>&sort=<orden>` con cabecera `Authorization: Bearer <accessToken>`. Recibir `200 OK` con `items[]`, `pagination` y `totals`. | DTOs acorde al catálogo V6.0.2 y verificación de serialización de filtros y deserialización de la página. |
| **M390-R03** | **Aislamiento y fecha real (MO-PR04, MO-PR05, MO-PR06):** La identidad se deriva del JWT y el servidor delimita el historial por titular. El cliente no reordena por fecha de captura ni sustituye `occurredAt` por la fecha de consulta; conserva y muestra el origen del registro tal como lo devuelve el servidor. | Inspección de arquitectura y pruebas que confirmen ausencia de `userId`, conservación de `occurredAt` y de `origin`. |
| **M390-R04** | **Errores, estados y paginación resiliente (MO-PR08):** Manejar el sobre `{"error":{"code","message","details","traceId"}}` para `400` de filtros inválidos, `401`, `403`, `429` y `500`; estados MVI de carga, contenido, vacío, carga de página siguiente, error y sin conexión; evitar páginas duplicadas o saltadas; cancelar la carga anterior al cambiar filtros; reintento manual sin bucles. | Pruebas unitarias de cambio de filtro, doble carga, error HTTP, sin conexión y ausencia de duplicados. |
| **M390-R05** | **Simulación en `MockAuthApi`:** La fuente simulada devuelve un historial sintético determinista con movimientos de tipo `ant`, `fixed` y `saving`, fechas RFC 3339 UTC, importes como cadena decimal de dos dígitos, moneda `MXN`, paginación por `page`/`limit`, filtrado por `type` y rango, y soporta el centinela `net@error.test`. | Casos de mock cubiertos: primera página, página siguiente, filtro por tipo, rango de fechas y fallo de red. |
| **M390-R06** | **Cobertura de pruebas unitarias:** Suite `MovementsListViewModelTest` y `MockAuthApiTest` con `kotlinx-coroutines-test` que cubra carga inicial, paginación, cambio de filtro, rango vacío, errores HTTP, sin conexión y conservación de fecha y origen. | Pruebas unitarias aprobadas y conteo total de tests reportado sin alterar los 50 preexistentes. |

## Escenarios de prueba y evidencia
- **Caso favorable:** Usuario abre Movimientos → `GET /api/v1/movements?type=all&page=1&limit=20` → `200 OK` con historial ordenado por fecha, importes de dos decimales y origen → la lista muestra los elementos y el botón de página siguiente si corresponde.
- **Caso pestaña Hormiga:** Selección de Hormiga → `type=ant` → solo se muestran movimientos hormiga; el filtro conserva el rango de fechas activo.
- **Caso rango sin resultados:** `from`/`to` sin coincidencias → `200 OK` con `items` vacío → estado vacío comprensible, sin inventar filas ni totales.
- **Caso página siguiente:** Desplazamiento o acción "Cargar más" → `page=2` → se añaden elementos sin duplicar los de la página anterior.
- **Caso cambio rápido de filtro:** Cambio de pestaña durante una carga → la petición anterior se cancela y solo la última actualiza el estado.
- **Caso sin conexión:** Fallo de transporte → estado sin conexión con "Reintentar"; no se conserva una página a medias como si estuviera completa.
- **Regresión:** Compilación exitosa de `./gradlew assembleDebug` y aprobación continua de los 50 tests preexistentes de autenticación.
- **Comandos esperados (en Fase B):** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
- **Evidencia exigida:** ejecución real, reporte de cobertura de tests, trazabilidad a requisitos y registro en `memoria.md`.

## Riesgos y bloqueos
- **Estructura de `pagination` y `totals`:** la ficha solo nombra ambos objetos; campos, tipos y significado de cada total **por confirmar con Backend**. La simulación usa contenedores provisionales explícitos.
- **Orden por defecto y valores de `sort`:** la ficha no enumera los valores admitidos: **por confirmar con Backend**.
- **Formato de `from`/`to`:** se asume fecha RFC 3339 o `YYYY-MM-DD`, sin definición explícita: **por confirmar con Backend**.
- **Límite máximo de `limit`:** no documentado; se usará un valor conservador en la simulación y queda **por confirmar con Backend**.
- **Significado de `saving`:** la pestaña Ahorro muestra movimientos de tipo `saving`, pero la ficha no detalla si provienen de metas o de aportaciones: **por confirmar con Backend**.
- **Falta de Backend real:** el listado se valida contra `MockAuthApi`; el comportamiento con datos reales queda fuera de esta unidad.

## Criterio de salida de la unidad
Todas las tareas en `task.md` documentadas en estado pendiente (`- [ ]`); trazabilidad completa requisito → plan → tarea; ninguna ejecución de código en Fase A; sin cambios fuera del alcance autorizado.
