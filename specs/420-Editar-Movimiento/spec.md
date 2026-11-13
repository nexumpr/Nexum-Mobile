---
project: Nexum-Mobile
unit: 420-Editar-Movimiento
kind: technical-spec
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# SPEC — Edición de Movimiento (Edit Movement)

## Propósito
Permitir al usuario titular autenticado corregir el concepto, el monto, la categoría o el tipo de un gasto ya registrado bajo el contrato **NEXUM-E030** (`PATCH /api/v1/movements/{movementId}`, `[M]`, catálogo V6.0.2), provocando el recálculo autoritativo de presupuestos, estadísticas y disponible, sin permitir convertir un gasto en ahorro mediante la edición genérica.

## Lugar en el plan SDD
- **Constitución:** [`.specify/memory/constitution.md`](../../.specify/memory/constitution.md) (vigente: MO-PR04 Backend como fuente autoritativa, MO-PR05 Veracidad del dinero: la corrección no inventa importes ni movimientos, MO-PR06 Privacidad y seguridad desde el diseño: identidad por token y `404` sin filtrar pertenencia ajena, MO-PR07 Usabilidad inclusiva Material 3, MO-PR08 Manejo explícito de conectividad, MO-PR12 Responsabilidades desacopladas).
- **Tecnología:** Kotlin / Jetpack Compose / MVI (Android nativo).
- **Contrato:** NEXUM-E030 del catálogo de endpoints de referencia V6.0.2 (`PATCH /api/v1/movements/{movementId}` [M]).
- **Dependencias previas:** [`410-Detalle-Movimiento`](../410-Detalle-Movimiento/spec.md) (carga del registro a editar), [`400-Registrar-Movimiento`](../400-Registrar-Movimiento/spec.md) (validación y modelo del alta), [`340-Catalogo-Categorias`](../340-Catalogo-Categorias/spec.md) (selector de categoría), [`380-Resumen-Financiero`](../380-Resumen-Financiero/spec.md) (resumen afectado por la corrección), [`140-Login`](../140-Login/spec.md) (sesión activa), [`150-Refresh-Token`](../150-Refresh-Token/spec.md) (renovación de token) y [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md) (cliente HTTP y errores).
- **Naturaleza:** especificación técnica de mutación parcial de un dato financiero con reglas de negocio.

## Alcance permitido
Formulario "Editar movimiento" precargado desde el detalle, DTO de contrato E030 (`UpdateMovementRequest`, `UpdateMovementResponse`), ampliación de `AuthApi` y `AuthRepository` con actualización parcial, validación local con `BigDecimal`, ViewModel MVI (`EditMovementViewModel`) con conservación del formulario ante error, invalidación de la caché del resumen afectado y pruebas unitarias de la unidad.

## Exclusiones explícitas
No convertir un movimiento a `saving` mediante la edición genérica; las aportaciones se gestionan con reglas propias fuera de esta unidad. No modificar `occurredAt` ni el origen del evento por este contrato; no crear movimientos nuevos; no listar ni paginar movimientos; no enviar ni aceptar `userId`; no recalcular localmente presupuestos, estadísticas o disponible como valores autoritativos; no usar `Float`/`Double`; no conectarse a un backend real en esta etapa; no publicar ni desplegar.

## Requisitos y criterios verificables
| ID | Requisito obligatorio | Aceptación mínima |
| --- | --- | --- |
| **M420-R01** | **Formulario de edición precargado:** Vista Compose Material 3 con concepto, importe, categoría y tipo precargados desde el detalle; solo esos campos son editables; botón "Guardar cambios" con progreso y confirmación; acción cancelar que no modifica nada; errores asociados al campo y áreas táctiles de al menos 48 dp. | Composable probado con precarga, cambio de un campo, guardado válido y cancelación. |
| **M420-R02** | **Contrato y payload E030:** Enviar `PATCH /api/v1/movements/{movementId}` con cabecera `Authorization: Bearer <accessToken>` y cuerpo parcial `{"concept":"...","amount":"...","categoryId":"...","type":"ant"}` con solo los campos modificados. Recibir `200 OK` con `movement` actualizado y `summaryVersion`. | DTOs acorde al catálogo V6.0.2 y verificación de serialización parcial y deserialización de la respuesta. |
| **M420-R03** | **Sin conversión a ahorro y monto válido (MO-PR04, MO-PR05):** El tipo solo admite `ant` o `fixed`; cualquier intento de enviar `saving` se rechaza antes de la red. `amount`, si se envía, debe ser mayor que cero y tener dos decimales. La categoría se envía por `categoryId` y la valida el servidor. La edición no crea ni duplica movimientos. | Pruebas de rechazo de `saving`, validación de `"0.00"`/`"-5.00"`/`"10.5"`, envío exclusivo de `categoryId` y ausencia de altas. |
| **M420-R04** | **Errores, estados y resiliencia (MO-PR08):** Manejar el sobre `{"error":{"code","message","details","traceId"}}` para `400`, `401`, `403`, `404`, `409`, `422`, `429` y `500`; estados MVI de carga del original, edición, guardando, guardado, error y sin conexión; conservar los valores capturados ante fallo; impedir doble envío; `404` neutro sin revelar pertenencia ajena. | Pruebas unitarias de error de validación, `404`, `409`, `422`, fallo de red y conservación del formulario. |
| **M420-R05** | **Simulación en `MockAuthApi`:** La fuente simulada aplica el cambio parcial sobre el movimiento sintético en memoria, valida monto y tipo, rechaza `saving` con `422`, devuelve `200` con `movement` actualizado y `summaryVersion` incrementado, responde tras 300–700 ms y soporta los centinelas `net@error.test` y `rule_error_movement` (`422`). | Casos de mock cubiertos: cambio de concepto, cambio de monto, rechazo de `saving`, movimiento inexistente y fallo de red. |
| **M420-R06** | **Cobertura de pruebas unitarias:** Suite `EditMovementViewModelTest`, `MovementEditValidatorTest` y `MockAuthApiTest` con `kotlinx-coroutines-test` que cubra precarga, campos parciales, validación, errores, ausencia de conversión a ahorro y conservación del formulario. | Pruebas unitarias aprobadas y conteo total de tests reportado sin alterar los 50 preexistentes. |

## Escenarios de prueba y evidencia
- **Caso favorable de concepto:** Se abre el detalle, se cambia solo el concepto y se guarda → `PATCH` con `concept` únicamente → `200 OK` con `movement` actualizado y `summaryVersion` → confirmación y regreso al detalle.
- **Caso cambio de monto:** Importe `"125.50"` válido → `PATCH` con `amount` → `200 OK`; el resumen afectado se marca como desactualizado en el cliente sin recalcularlo localmente.
- **Caso intento de ahorro:** Selección de tipo `saving` → la UI no lo ofrece y el validador lo rechaza antes de la red → no se envía `PATCH`.
- **Caso importe inválido:** `"0.00"`, `"-5.00"` o `"125.5"` → validación local bloquea el envío y asocia el error al campo.
- **Caso movimiento ajeno o inexistente:** `404` → mensaje neutro "Movimiento no disponible"; no se revela si pertenece a otra cuenta.
- **Caso conflicto o regla:** `409` o `422` → se conserva el formulario y se permite corregir; no se declara guardado.
- **Caso sin conexión:** Fallo de transporte → estado sin conexión con "Reintentar"; no se muestra éxito falso.
- **Caso doble pulsación:** Pulsaciones rápidas → `isSaving = true` deshabilita la acción y evita envíos concurrentes.
- **Regresión:** Compilación exitosa de `./gradlew assembleDebug` y aprobación continua de los 50 tests preexistentes de autenticación.
- **Comandos esperados (en Fase B):** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
- **Evidencia exigida:** ejecución real, reporte de cobertura de tests, trazabilidad a requisitos y registro en `memoria.md`.

## Riesgos y bloqueos
- **Tipo de `summaryVersion`:** la ficha no indica si es entero, cadena opaca o marca temporal; se modela como `String` y queda **por confirmar con Backend**.
- **Forma de `movement`:** el cuerpo de la respuesta se alinea con el elemento del listado y el detalle, pero su forma definitiva **por confirmar con Backend**.
- **Semántica de `PATCH` parcial:** no se define si un campo ausente conserva el valor o si `null` lo limpia: **por confirmar con Backend**.
- **Conflicto de versión:** no se detalla si `409` usa `summaryVersion` o `If-Match`: **por confirmar con Backend**.
- **Recálculo de presupuestos y estadísticas:** la ficha menciona que la edición los obliga a recalcular, pero sus contratos no forman parte de esta unidad: **por confirmar con Backend**.
- **Falta de Backend real:** la edición se valida contra `MockAuthApi`; el comportamiento con datos reales queda fuera de esta unidad.

## Criterio de salida de la unidad
Todas las tareas en `task.md` documentadas en estado pendiente (`- [ ]`); trazabilidad completa requisito → plan → tarea; ninguna ejecución de código en Fase A; sin cambios fuera del alcance autorizado.
