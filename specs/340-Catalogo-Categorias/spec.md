---
project: Nexum-Mobile
unit: 340-Catalogo-Categorias
kind: technical-spec
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# SPEC — Catálogo de Categorías (Categories Catalog)

## Propósito
Entregar a la aplicación las categorías permitidas y sus identificadores estables bajo el contrato **NEXUM-E022** (`GET /api/v1/catalogs/categories`, `[M]`, catálogo V6.0.2), para que el usuario seleccione una categoría en el registro manual, en la confirmación por voz y en la configuración de presupuestos, sin que el cliente invente nombres ni identificadores.

## Lugar en el plan SDD
- **Constitución:** [`.specify/memory/constitution.md`](../../.specify/memory/constitution.md) (vigente: MO-PR04 Backend como fuente autoritativa, MO-PR06 Privacidad y seguridad desde el diseño: validar respuestas sin confiar en entradas remotas, MO-PR07 Usabilidad inclusiva Material 3, MO-PR08 Manejo explícito de conectividad, MO-PR12 Responsabilidades desacopladas).
- **Tecnología:** Kotlin / Jetpack Compose / MVI (Android nativo).
- **Contrato:** NEXUM-E022 del catálogo de endpoints de referencia V6.0.2 (`GET /api/v1/catalogs/categories` [M]).
- **Dependencias previas:** [`030-di-navegacion-ui`](../030-di-navegacion-ui/spec.md) (shell y componentes Compose), [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md) (cliente HTTP y formato de errores) y [`060-cache-offline`](../060-cache-offline/spec.md) (caché local y datos maestros de respaldo).
- **Naturaleza:** especificación técnica de datos maestros de solo lectura y su respaldo local.

## Alcance permitido
Consulta del catálogo de categorías, DTO de contrato E022 (`CategoryResponse`), modelo de dominio (`Category`), ampliación de `AuthApi` y `AuthRepository`, caché de lectura con respaldo de datos maestros empaquetados en Android, componente reutilizable de selección de categoría (`CategorySelector`) en Compose con Material 3, ViewModel MVI (`CategoryCatalogViewModel`) y pruebas unitarias de la unidad.

## Exclusiones explícitas
No crear, editar ni eliminar categorías desde el cliente; no enviar nombres de categoría como identificador; no aceptar categorías con `active = false` como opción seleccionable; no definir aquí los formularios de registro manual, voz o presupuesto, que solo consumen el selector; no implementar el catálogo de frecuencias (unidad `350-Catalogo-Frecuencias`); no conectarse a un backend real en esta etapa; no publicar ni desplegar.

## Requisitos y criterios verificables
| ID | Requisito obligatorio | Aceptación mínima |
| --- | --- | --- |
| **M340-R01** | **Selector de categoría reutilizable:** Componente Compose Material 3 que lista las categorías activas con nombre e icono, permite seleccionar una sola, presenta estado vacío y deshabilita las inactivas; se reutiliza en registro manual, confirmación por voz y presupuesto. Etiquetas legibles por lector de pantalla y áreas táctiles adecuadas. | Composable probado con selección, categoría inactiva y estado vacío. |
| **M340-R02** | **Contrato y payload E022:** Enviar `GET /api/v1/catalogs/categories` con parámetro opcional `locale` (por ejemplo `es-MX`). Recibir `200 OK` con arreglo de `{id, name, icon, active}`. | DTOs modelados acorde al catálogo V6.0.2 y verificación de deserialización en pruebas. |
| **M340-R03** | **Identificador estable y validación en servidor (MO-PR04, MO-PR06):** El cliente envía únicamente `categoryId`; nunca deriva el identificador desde el nombre visible ni acepta nombres arbitrarios. Las categorías permitidas son Antojos, Comida, Transporte, Servicios, Vivienda, Entretenimiento, Suscripciones, Compras personales y Otros. | Pruebas que confirmen que el selector solo produce `id` y que el nombre no se usa como llave. |
| **M340-R04** | **Errores, estados y respaldo local (MO-PR08):** Manejar el sobre `{"error":{"code","message","details","traceId"}}` para `400` de `locale` inválido, `429` y `500`; estados MVI de carga, contenido, vacío, error y sin conexión; ante fallo de red usar el catálogo empaquetado o cacheado e indicar su origen; descartar entradas sin `id` o `name`. | Pruebas unitarias de error, sin conexión y respaldo con datos maestros locales. |
| **M340-R05** | **Simulación en `MockAuthApi`:** La fuente simulada devuelve las nueve categorías documentadas tras retardo de 200–500 ms, con identificadores estables, `icon` e `active`, respeta `locale` y soporta el centinela `net@error.test`. | Casos de mock cubiertos: catálogo completo, categoría inactiva y fallo de red. |
| **M340-R06** | **Cobertura de pruebas unitarias:** Suite `CategoryCatalogViewModelTest` y `MockAuthApiTest` con `kotlinx-coroutines-test` que cubra carga, selección, estado vacío, respuesta malformada, error HTTP y respaldo local. | Pruebas unitarias aprobadas y conteo total de tests reportado sin alterar los 50 preexistentes. |

## Escenarios de prueba y evidencia
- **Caso favorable:** Apertura del selector en registro manual → `GET /api/v1/catalogs/categories` → `200 OK` con las nueve categorías → se muestran nombres e iconos y se permite una selección.
- **Caso con `locale`:** Selección con `locale=es-MX` → la respuesta devuelve etiquetas en español → el selector conserva el `id` estable y no el texto.
- **Caso de categoría inactiva:** Elemento con `active = false` → se muestra deshabilitado o se omite, sin poder seleccionarse.
- **Caso sin conexión:** Fallo de transporte → el selector usa el catálogo empaquetado y muestra un aviso discreto de origen local.
- **Caso de error del servidor:** `500` con `traceId` → estado de error con reintento; el selector no queda bloqueado si hay respaldo local.
- **Regresión:** Compilación exitosa de `./gradlew assembleDebug` y aprobación continua de los 50 tests preexistentes de autenticación.
- **Comandos esperados (en Fase B):** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
- **Evidencia exigida:** ejecución real, reporte de cobertura de tests, trazabilidad a requisitos y registro en `memoria.md`.

## Riesgos y bloqueos
- **Identificadores exactos:** la ficha documenta `antojos` como ejemplo y los nombres visibles, pero no la lista completa de `id`; los identificadores definitivos **por confirmar con Backend**.
- **Catálogo de iconos:** el campo `icon` no define formato ni conjunto permitido (nombre, URL o código): **por confirmar con Backend**.
- **Autenticación:** la ficha no detalla si el endpoint exige sesión; si el backend la requiere, se aplicará `401`/`403` sin ampliar el contrato por suposición.
- **Vigencia de caché:** TTL e invalidación del catálogo local dependen de `060-cache-offline` y de M-D10, **por confirmar**.
- **Datos empaquetados:** el respaldo local debe actualizarse cuando cambie el catálogo del servidor; la estrategia de sincronización queda pendiente.

## Criterio de salida de la unidad
Todas las tareas en `task.md` documentadas en estado pendiente (`- [ ]`); trazabilidad completa requisito → plan → tarea; ninguna ejecución de código en Fase A; sin cambios fuera del alcance autorizado.
