---
project: Nexum-Mobile
unit: 350-Catalogo-Frecuencias
kind: technical-spec
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# SPEC — Catálogo de Frecuencias (Frequencies Catalog)

## Propósito
Publicar a la aplicación los códigos y etiquetas de frecuencia de ingreso, gasto y recurrencia bajo el contrato **NEXUM-E023** (`GET /api/v1/catalogs/frequencies`, soporte y consistencia `[S]`, catálogo V6.0.2), para alimentar los menús de frecuencia sin usar factores aproximados como si fueran reglas de calendario.

## Lugar en el plan SDD
- **Constitución:** [`.specify/memory/constitution.md`](../../.specify/memory/constitution.md) (vigente: MO-PR04 Backend como fuente autoritativa, MO-PR05 Veracidad del dinero: las estimaciones no se presentan como calendario ni como registro confirmado, MO-PR06 Privacidad y seguridad desde el diseño: validar respuestas remotas, MO-PR07 Usabilidad inclusiva Material 3, MO-PR08 Manejo explícito de conectividad, MO-PR18 Trazabilidad honesta).
- **Tecnología:** Kotlin / Jetpack Compose / MVI (Android nativo).
- **Contrato:** NEXUM-E023 del catálogo de endpoints de referencia V6.0.2 (`GET /api/v1/catalogs/frequencies` [S]).
- **Dependencias previas:** [`130-Registro-Usuario`](../130-Registro-Usuario/spec.md) (frecuencia de ingreso y gasto recurrente del alta), [`340-Catalogo-Categorias`](../340-Catalogo-Categorias/spec.md) (patrón de catálogo y respaldo local), [`030-di-navegacion-ui`](../030-di-navegacion-ui/spec.md) (componentes Compose), [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md) (cliente HTTP) y [`060-cache-offline`](../060-cache-offline/spec.md) (caché y datos maestros de respaldo).
- **Naturaleza:** especificación técnica de datos maestros de solo lectura con impacto en estimaciones, consistencia y soporte (`[S]`).

## Alcance permitido
Consulta del catálogo de frecuencias por contexto, DTO de contrato E023 (`FrequencyResponse`), modelo de dominio (`FrequencyOption`), ampliación de `AuthApi` y `AuthRepository`, respaldo local de datos maestros, componente reutilizable de selección de frecuencia (`FrequencySelector`) en Compose con Material 3, ViewModel MVI (`FrequencyCatalogViewModel`) y pruebas unitarias de la unidad.

## Exclusiones explícitas
No generar fechas de vencimiento ni calendarios a partir de los factores mensuales; no usar los factores como si fueran reglas exactas de ocurrencia; no habilitar códigos distintos de `monthly` en el alta mientras M-D08 siga vigente; no implementar el cálculo del ingreso mensual (unidad `360-Consultar-Ingreso`) ni la actualización de ingreso (`370-Actualizar-Ingreso`); no inventar códigos fuera de los nueve documentados; no conectarse a un backend real en esta etapa; no publicar ni desplegar.

## Requisitos y criterios verificables
| ID | Requisito obligatorio | Aceptación mínima |
| --- | --- | --- |
| **M350-R01** | **Menús de frecuencia por contexto:** Componente Compose Material 3 que presenta las etiquetas del catálogo para `context=income`, `recurring` y `movement`, permite una sola selección, deshabilita las opciones no válidas para el contexto y mantiene etiquetas legibles y áreas táctiles adecuadas. | Composable probado con los tres contextos y una opción no válida. |
| **M350-R02** | **Contrato y payload E023:** Enviar `GET /api/v1/catalogs/frequencies?context=income\|recurring\|movement` y recibir `200 OK` con arreglo de `{code, label, validFor}`. Códigos documentados: `once`, `daily`, `weekly`, `fortnightly`, `monthly`, `bimonthly`, `quarterly`, `semiannual` y `annual`. | DTOs modelados acorde al catálogo V6.0.2 y verificación de deserialización en pruebas. |
| **M350-R03** | **Estimación mensual separada del calendario (MO-PR05):** Los factores `daily=365/12`, `weekly=52/12`, `fortnightly=2`, `monthly=1`, `bimonthly=1/2`, `quarterly=1/3`, `semiannual=1/6` y `annual=1/12` son solo estimaciones de equivalencia mensual y nunca se usan para calcular vencimientos. M-D08 limita el alta a `monthly` hasta confirmar el catálogo con Backend. | Pruebas que confirmen que ningún factor produce fechas ni vencimientos y que el alta solo ofrece `monthly`. |
| **M350-R04** | **Errores, estados y respaldo local (MO-PR08):** Manejar el sobre `{"error":{"code","message","details","traceId"}}` para `400` de contexto inválido, `429` y `500`; estados MVI de carga, contenido, vacío, error y sin conexión; usar el catálogo empaquetado o cacheado cuando falle la red e indicar su origen; descartar códigos desconocidos sin inventar etiquetas. | Pruebas unitarias de contexto inválido, error, sin conexión y respaldo local. |
| **M350-R05** | **Simulación en `MockAuthApi`:** La fuente simulada devuelve los nueve códigos con etiquetas en español tras retardo de 200–500 ms, filtra por `context`, incluye `validFor` y soporta el centinela `net@error.test`. | Casos de mock cubiertos para los tres contextos y fallo de red. |
| **M350-R06** | **Cobertura de pruebas unitarias:** Suite `FrequencyCatalogViewModelTest` y `MockAuthApiTest` con `kotlinx-coroutines-test` que cubra los nueve códigos, filtrado por contexto, opción no válida, error HTTP, sin conexión y respaldo local. | Pruebas unitarias aprobadas y conteo total de tests reportado sin alterar los 50 preexistentes. |

## Escenarios de prueba y evidencia
- **Caso favorable ingreso:** Usuario abre el menú de frecuencia de ingreso → `GET /api/v1/catalogs/frequencies?context=income` → `200 OK` con `monthly` habilitado y el resto según `validFor` → selección única.
- **Caso recurrencia:** `context=recurring` → se muestran los códigos válidos para recurrencia → la selección conserva el `code` y no la etiqueta.
- **Caso de opción no válida:** Código ausente del contexto → la opción no se ofrece o se muestra deshabilitada, sin enviarse al alta.
- **Caso M-D08:** En el alta, solo `monthly` está habilitado mientras el catálogo no se confirme con Backend.
- **Caso sin conexión:** Fallo de transporte → el menú usa el catálogo local y muestra un aviso de origen; no se inventan etiquetas.
- **Regresión:** Compilación exitosa de `./gradlew assembleDebug` y aprobación continua de los 50 tests preexistentes de autenticación.
- **Comandos esperados (en Fase B):** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
- **Evidencia exigida:** ejecución real, reporte de cobertura de tests, trazabilidad a requisitos y registro en `memoria.md`.

## Riesgos y bloqueos
- **M-D08 vigente:** la frecuencia queda limitada a `monthly` en el alta hasta que Backend confirme el catálogo; este documento no deroga la decisión.
- **Forma de `validFor`:** la ficha no especifica si es una lista de contextos, un valor único o un booleano: **por confirmar con Backend**.
- **Etiquetas:** los textos exactos por idioma y su localización no están detallados: **por confirmar con Backend**.
- **Reglas de calendario:** los vencimientos reales requieren reglas separadas que esta unidad no define: **por confirmar con Backend**.
- **Autenticación:** la ficha no detalla si el endpoint exige sesión; no se añaden cabeceras por suposición.

## Criterio de salida de la unidad
Todas las tareas en `task.md` documentadas en estado pendiente (`- [ ]`); trazabilidad completa requisito → plan → tarea; ninguna ejecución de código en Fase A; sin cambios fuera del alcance autorizado.
