---
project: Nexum-Mobile
unit: 360-Consultar-Ingreso
kind: technical-spec
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# SPEC — Consulta de Ingreso Habitual (Get Habitual Income)

## Propósito
Consultar el ingreso habitual del usuario titular autenticado, la frecuencia original con la que lo declaró y su equivalente mensual estimado bajo el contrato **NEXUM-E024** (`GET /api/v1/income`, `[M]`, catálogo V6.0.2), para que Perfil muestre el dato y permita abrir su edición sin confundir la estimación con un depósito real.

## Lugar en el plan SDD
- **Constitución:** [`.specify/memory/constitution.md`](../../.specify/memory/constitution.md) (vigente: MO-PR04 Backend como fuente autoritativa, MO-PR05 Veracidad del dinero: la estimación mensual no se presenta como saldo ni depósito, MO-PR06 Privacidad y seguridad desde el diseño: dato privado de un solo usuario e identidad por token, MO-PR07 Usabilidad inclusiva Material 3, MO-PR08 Manejo explícito de conectividad).
- **Tecnología:** Kotlin / Jetpack Compose / MVI (Android nativo).
- **Contrato:** NEXUM-E024 del catálogo de endpoints de referencia V6.0.2 (`GET /api/v1/income` [M]).
- **Dependencias previas:** [`350-Catalogo-Frecuencias`](../350-Catalogo-Frecuencias/spec.md) (códigos y etiquetas de frecuencia), [`240-Perfil-Usuario`](../240-Perfil-Usuario/spec.md) (pantalla de Perfil), [`140-Login`](../140-Login/spec.md) (sesión activa), [`150-Refresh-Token`](../150-Refresh-Token/spec.md) (renovación de token), [`030-di-navegacion-ui`](../030-di-navegacion-ui/spec.md) (navegación al editor) y [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md) (cliente HTTP y errores).
- **Naturaleza:** especificación técnica de lectura de dato financiero privado y su presentación como estimación.

## Alcance permitido
Consulta del ingreso habitual en `ProfileScreen`, DTO de contrato E024 (`IncomeResponse`), modelo de dominio (`Income`), ampliación de `AuthApi` y `AuthRepository`, presentación del importe original, la frecuencia y el equivalente mensual con moneda `MXN`, estado vacío cuando no exista ingreso declarado, ViewModel MVI (`IncomeViewModel`) y pruebas unitarias de la unidad.

## Exclusiones explícitas
No modificar ni reemplazar el ingreso (unidad `370-Actualizar-Ingreso`); no enviar ni aceptar `userId` por parámetro; no crear depósitos, movimientos ni saldos a partir de este dato; no manejar listas de ingresos mientras el contrato inicial sea de un solo ingreso activo; no recalcular localmente el equivalente mensual como valor autoritativo; no conectarse a un backend real en esta etapa; no publicar ni desplegar.

## Requisitos y criterios verificables
| ID | Requisito obligatorio | Aceptación mínima |
| --- | --- | --- |
| **M360-R01** | **Presentación del ingreso en Perfil:** Tarjeta "Ingreso equivalente mensual" con importe original, etiqueta de frecuencia, equivalente mensual y moneda; indicador visible de que el equivalente es una estimación; estado vacío con invitación a declarar el ingreso; acción "Editar" que abre el editor. Etiquetas legibles y sin depender solo del color. | Composable probado con contenido, estimación visible, estado vacío y acción Editar. |
| **M360-R02** | **Contrato y payload E024:** Enviar `GET /api/v1/income` con cabecera `Authorization: Bearer <accessToken>`. Recibir `200 OK` con `amount`, `frequency`, `monthlyEquivalent` y `currency`; los importes son cadenas decimales de dos dígitos y la moneda es `MXN`. | DTOs acorde al catálogo V6.0.2 y verificación de deserialización con cadenas decimales. |
| **M360-R03** | **Dato privado y estimación honesta (MO-PR04, MO-PR05, MO-PR06):** La identidad se deriva del JWT y la respuesta pertenece solo al titular. Inicialmente existe un único ingreso habitual activo. El cliente no presenta `monthlyEquivalent` como depósito ni como saldo confirmado. | Inspección de arquitectura y pruebas que confirmen ausencia de `userId`, modelo singular y etiqueta de estimación. |
| **M360-R04** | **Errores, estados y resiliencia (MO-PR08):** Manejar el sobre `{"error":{"code","message","details","traceId"}}` para `401`, `403`, `404` (sin ingreso declarado, semántica por confirmar), `429` y `500`; estados MVI de carga, contenido, vacío, error y sin conexión; conservar el último valor local etiquetado como anterior; no mostrar cero por defecto. | Pruebas unitarias de contenido, vacío, `401`, `404`, error de red y caché anterior. |
| **M360-R05** | **Simulación en `MockAuthApi`:** La fuente simulada devuelve el ingreso del usuario activo tras retardo de 250–600 ms, con importes como cadena decimal, frecuencia `monthly` (M-D08) y moneda `MXN`; soporta escenario sin ingreso y el centinela `net@error.test`. | Casos de mock cubiertos: ingreso presente, ingreso ausente y fallo de red. |
| **M360-R06** | **Cobertura de pruebas unitarias:** Suite `IncomeViewModelTest` y `MockAuthApiTest` con `kotlinx-coroutines-test` que cubra mapeo de DTO, formato decimal sin `Float`/`Double`, vacío, autorización, error HTTP y sin conexión. | Pruebas unitarias aprobadas y conteo total de tests reportado sin alterar los 50 preexistentes. |

## Escenarios de prueba y evidencia
- **Caso favorable:** Usuario autenticado abre Perfil → `GET /api/v1/income` → `200 OK` con `amount = "12000.00"`, `frequency = "monthly"`, `monthlyEquivalent = "12000.00"`, `currency = "MXN"` → tarjeta con importe y etiqueta de estimación.
- **Caso frecuencia distinta:** Respuesta con `frequency = "fortnightly"` → la UI muestra la etiqueta de la frecuencia original y el equivalente mensual estimado, sin recalcularlo como valor autoritativo.
- **Caso sin ingreso declarado:** Respuesta vacía o `404` → estado vacío con acción para declarar el ingreso; no se muestra `"0.00"` como si fuera un dato real.
- **Caso sin conexión:** Fallo de transporte con valor previo → se conserva el último importe etiquetado como anterior; sin red y sin caché se muestra error con reintento.
- **Caso de sesión inválida:** `401` → mensaje de sesión no autorizada sin cerrar la pantalla ni exponer detalles internos.
- **Regresión:** Compilación exitosa de `./gradlew assembleDebug` y aprobación continua de los 50 tests preexistentes de autenticación.
- **Comandos esperados (en Fase B):** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
- **Evidencia exigida:** ejecución real, reporte de cobertura de tests, trazabilidad a requisitos y registro en `memoria.md`.

## Riesgos y bloqueos
- **Semántica de ausencia:** la ficha no define si la ausencia de ingreso se representa con `404` o con un cuerpo vacío: **por confirmar con Backend**.
- **Redondeo y decimales:** la regla de redondeo del `monthlyEquivalent` la define el contrato; el cliente solo presenta el valor recibido: **por confirmar con Backend**.
- **Zona horaria y período:** la consulta no recibe `month`; el periodo al que corresponde el equivalente no está detallado: **por confirmar con Backend**.
- **Frecuencia limitada:** M-D08 mantiene `monthly` en el alta; si el backend devuelve otra frecuencia, la UI la muestra sin habilitar cambios no autorizados.
- **Falta de Backend real:** la consulta se valida contra `MockAuthApi`; el comportamiento con datos reales queda fuera de esta unidad.

## Criterio de salida de la unidad
Todas las tareas en `task.md` documentadas en estado pendiente (`- [ ]`); trazabilidad completa requisito → plan → tarea; ninguna ejecución de código en Fase A; sin cambios fuera del alcance autorizado.
