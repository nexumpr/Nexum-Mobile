---
project: Nexum-Mobile
unit: 400-Registrar-Movimiento
kind: technical-spec
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# SPEC — Registro de Movimiento (Gasto Realizado) (Create Movement)

## Propósito
Permitir al usuario titular autenticado registrar un gasto efectivamente realizado —no una programación futura— bajo el contrato **NEXUM-E028** (`POST /api/v1/movements`, `[M]`, catálogo V6.0.2), conservando concepto, importe, categoría, tipo y fecha real del evento, tanto en captura manual como en la confirmación de una captura por voz, sin duplicar el gasto ante dobles toques o reintentos.

## Lugar en el plan SDD
- **Constitución:** [`.specify/memory/constitution.md`](../../.specify/memory/constitution.md) (vigente: MO-PR04 Backend como fuente autoritativa, MO-PR05 Veracidad del dinero: solo se registra lo efectivamente realizado, MO-PR06 Privacidad y seguridad desde el diseño: identidad por token sin aceptar `userId`, MO-PR07 Usabilidad inclusiva Material 3, MO-PR08 Manejo explícito de conectividad, MO-PR12 Responsabilidades desacopladas).
- **Tecnología:** Kotlin / Jetpack Compose / MVI (Android nativo).
- **Contrato:** NEXUM-E028 del catálogo de endpoints de referencia V6.0.2 (`POST /api/v1/movements` [M]).
- **Dependencias previas:** [`390-Listar-Movimientos`](../390-Listar-Movimientos/spec.md) (historial donde aparece el alta y modelo de movimiento), [`340-Catalogo-Categorias`](../340-Catalogo-Categorias/spec.md) (selector de categoría con identificador estable), [`380-Resumen-Financiero`](../380-Resumen-Financiero/spec.md) (resumen actualizado devuelto por el alta), [`140-Login`](../140-Login/spec.md) (sesión activa), [`150-Refresh-Token`](../150-Refresh-Token/spec.md) (renovación de token), [`030-di-navegacion-ui`](../030-di-navegacion-ui/spec.md) (navegación al formulario) y [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md) (cliente HTTP y errores).
- **Naturaleza:** especificación técnica de alta de dato financiero con idempotencia y validación.

## Alcance permitido
Formulario "Agregar gasto" y confirmación "Confirmar y guardar" para voz, DTO de contrato E028 (`CreateMovementRequest`, `CreateMovementResponse`), ampliación de `AuthApi` y `AuthRepository` con `Idempotency-Key`, validación local con `BigDecimal` de importe positivo y dos decimales, ViewModel MVI (`RegisterMovementViewModel`) con prevención de doble envío y reutilización de la llave de idempotencia en reintentos, y pruebas unitarias de la unidad.

## Exclusiones explícitas
No registrar programaciones futuras ni gastos recurrentes (su gestión pertenece a otras unidades); no permitir `type = saving` en el alta; no implementar la captura por voz ni el reconocimiento de audio (solo consume una propuesta ya confirmada); no consultar ni editar movimientos existentes (unidades `410` y `420`); no enviar ni aceptar `userId`; no inventar un `updatedSummary` cuyo contrato no esté definido; no usar `Float`/`Double`; no conectarse a un backend real en esta etapa; no publicar ni desplegar.

## Requisitos y criterios verificables
| ID | Requisito obligatorio | Aceptación mínima |
| --- | --- | --- |
| **M400-R01** | **Formulario de alta manual y confirmación por voz:** Campos concepto, importe con teclado decimal, categoría, tipo Hormiga u Obligatorio y fecha del evento; botón "Guardar gasto" con progreso y confirmación con el resumen actualizado. La ruta de voz reutiliza el mismo formulario precargado y muestra "Confirmar y guardar" con frecuencia única. Etiquetas legibles, errores asociados al campo y áreas táctiles de al menos 48 dp. | Composable probado con captura manual, propuesta de voz precargada, guardado y error de campo. |
| **M400-R02** | **Contrato y payload E028:** Enviar `POST /api/v1/movements` con cabecera `Authorization: Bearer <accessToken>` y `Idempotency-Key: <uuid>`, cuerpo `{"concept":"Gomitas","amount":"10.00","categoryId":"antojos","type":"ant","occurredAt":"2026-11-02T17:33:00Z"}` y `recurringPaymentId` opcional. Recibir `201 Created` con `movementId`, `record` y `updatedSummary`. | DTOs acorde al catálogo V6.0.2 y verificación de serialización con importes como cadena decimal. |
| **M400-R03** | **Gasto real, importe válido y categoría autoritativa (MO-PR04, MO-PR05):** `amount` debe ser mayor que cero y expresarse con dos decimales; el alta representa un gasto ya realizado y no una programación futura. El cliente envía `categoryId` y el servidor lo valida; el nombre de categoría no se usa como llave. El tipo solo admite `ant` o `fixed`. | Pruebas de validación (`"0.00"`, `"-5.00"`, `"10.5"`, `"10.00"`), de rechazo de `saving` y de envío exclusivo de `categoryId`. |
| **M400-R04** | **Idempotencia, errores y resiliencia (MO-PR08):** Reutilizar la misma `Idempotency-Key` al reintentar el mismo borrador y generar una nueva solo tras un alta confirmada. Manejar el sobre `{"error":{"code","message","details","traceId"}}` para `400`, `401`, `403`, `404` de categoría, `409` de idempotencia, `422`, `429` y `500`; estados MVI de edición, guardando, guardado, error y sin conexión; no mostrar éxito sin `201`. | Pruebas de doble toque, reintento con la misma llave, `409`, `422` y fallo de red con conservación del borrador. |
| **M400-R05** | **Simulación en `MockAuthApi`:** La fuente simulada valida `amount > 0` y dos decimales, exige `type` en `ant`/`fixed`, registra el movimiento en memoria y devuelve `201` con `movementId`, `record` y `updatedSummary`; deduplica por `Idempotency-Key`, responde tras 300–700 ms y soporta los centinelas `net@error.test` y `rule_error_movement` (`422`). | Casos de mock cubiertos: alta válida, doble toque con misma llave, importe inválido, `422` y fallo de red. |
| **M400-R06** | **Cobertura de pruebas unitarias:** Suite `RegisterMovementViewModelTest`, `MovementInputValidatorTest` y `MockAuthApiTest` con `kotlinx-coroutines-test` que cubra validación, alta manual, propuesta de voz, idempotencia, errores HTTP y sin conexión. | Pruebas unitarias aprobadas y conteo total de tests reportado sin alterar los 50 preexistentes. |

## Escenarios de prueba y evidencia
- **Caso manual favorable:** Concepto "Gomitas", importe `"10.00"`, categoría `antojos`, tipo `ant`, `occurredAt` real → `POST /api/v1/movements` → `201 Created` con `movementId`, `record` y `updatedSummary` → confirmación y aparición en el historial.
- **Caso por voz confirmada:** Propuesta "Café, 45.00, comida" precargada en el formulario → el usuario confirma → un único `POST` con `occurredAt` del evento y frecuencia única → `201`.
- **Caso importe inválido:** `"0.00"`, `"-5.00"` o `"10.5"` → validación local bloquea el envío y asocia el error al campo.
- **Caso doble toque:** Dos pulsaciones rápidas → `isSaving = true` deshabilita la acción y solo se emite una petición.
- **Caso reintento tras error de red:** Fallo de transporte → el borrador se conserva y el reintento envía la **misma** `Idempotency-Key`; no se duplica el gasto.
- **Caso categoría inválida:** `404`/`422` del servidor → mensaje comprensible, selección de categoría conservada y posibilidad de corregir.
- **Regresión:** Compilación exitosa de `./gradlew assembleDebug` y aprobación continua de los 50 tests preexistentes de autenticación.
- **Comandos esperados (en Fase B):** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
- **Evidencia exigida:** ejecución real, reporte de cobertura de tests, trazabilidad a requisitos y registro en `memoria.md`.

## Riesgos y bloqueos
- **Estructura de `updatedSummary`:** la ficha nombra el campo sin detallar sus miembros; se reutiliza la forma documentada del resumen financiero (E026) y queda **por confirmar con Backend**.
- **Forma de `record`:** la ficha no detalla el objeto; se alinea con el elemento del listado (E027) y queda **por confirmar con Backend**.
- **Vigencia de `Idempotency-Key`:** duración, formato y respuesta ante reutilización con cuerpo distinto **por confirmar con Backend**.
- **Fecha del evento:** el origen de `occurredAt` en la captura por voz y la zona horaria de presentación **por confirmar con Backend**.
- **Validación de categoría:** no se define si una categoría inactiva produce `404` o `422`: **por confirmar con Backend**.
- **Falta de Backend real:** el alta se valida contra `MockAuthApi`; el comportamiento con datos reales queda fuera de esta unidad.

## Criterio de salida de la unidad
Todas las tareas en `task.md` documentadas en estado pendiente (`- [ ]`); trazabilidad completa requisito → plan → tarea; ninguna ejecución de código en Fase A; sin cambios fuera del alcance autorizado.
