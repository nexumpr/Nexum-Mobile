---
project: Nexum-Mobile
unit: 410-Detalle-Movimiento
kind: technical-spec
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# SPEC — Detalle de Movimiento (Get Movement Detail)

## Propósito
Permitir al usuario titular autenticado consultar el registro exacto de un movimiento y sus vínculos con un pago recurrente o una meta bajo el contrato **NEXUM-E029** (`GET /api/v1/movements/{movementId}`, soporte y consistencia `[S]`, catálogo V6.0.2), sin depender de conservar la lista completa en memoria y sin revelar datos de terceros.

## Lugar en el plan SDD
- **Constitución:** [`.specify/memory/constitution.md`](../../.specify/memory/constitution.md) (vigente: MO-PR04 Backend como fuente autoritativa, MO-PR05 Veracidad del dinero: el detalle refleja el registro confirmado, MO-PR06 Privacidad y seguridad desde el diseño: 404 sin filtrar pertenencia ajena, MO-PR07 Usabilidad inclusiva Material 3, MO-PR08 Manejo explícito de conectividad, MO-PR12 Responsabilidades desacopladas).
- **Tecnología:** Kotlin / Jetpack Compose / MVI (Android nativo).
- **Contrato:** NEXUM-E029 del catálogo de endpoints de referencia V6.0.2 (`GET /api/v1/movements/{movementId}` [S]).
- **Dependencias previas:** [`390-Listar-Movimientos`](../390-Listar-Movimientos/spec.md) (origen de la navegación y modelo del movimiento), [`140-Login`](../140-Login/spec.md) (sesión activa), [`150-Refresh-Token`](../150-Refresh-Token/spec.md) (renovación de token), [`030-di-navegacion-ui`](../030-di-navegacion-ui/spec.md) (navegación al detalle y al editor) y [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md) (cliente HTTP y errores).
- **Naturaleza:** especificación técnica de lectura puntual de un dato financiero privado.

## Alcance permitido
Pantalla `MovementDetailScreen` que recibe `movementId`, DTO de contrato E029 (`MovementDetailResponse`), modelo de dominio (`MovementDetail`), ampliación de `AuthApi` y `AuthRepository`, implementación simulada en `MockAuthApi` con movimientos sintéticos y `404` para identificadores desconocidos o ajenos, ViewModel MVI (`MovementDetailViewModel`) con estados de carga, contenido, no encontrado y error, acción hacia el editor y pruebas unitarias de la unidad.

## Exclusiones explícitas
No listar ni paginar movimientos (unidad `390-Listar-Movimientos`); no editar desde aquí (unidad `420-Editar-Movimiento`) ni registrar movimientos (unidad `400-Registrar-Movimiento`); no enviar ni aceptar `userId`; no deducir el movimiento desde la lista en memoria cuando la consulta falla; no mostrar datos de terceras cuentas; no inventar vínculos con pago recurrente o meta; no conectarse a un backend real en esta etapa; no publicar ni desplegar.

## Requisitos y criterios verificables
| ID | Requisito obligatorio | Aceptación mínima |
| --- | --- | --- |
| **M410-R01** | **Pantalla de detalle:** Vista Compose Material 3 con concepto, importe en dos decimales, categoría, tipo, fecha real del evento, origen y vínculos disponibles (pago recurrente o meta); acciones "Editar" y "Volver"; estados de carga, contenido, no encontrado y error; etiquetas legibles y áreas táctiles de al menos 48 dp. | Composable probado con contenido completo, sin vínculos, no encontrado y acción Editar. |
| **M410-R02** | **Contrato y payload E029:** Enviar `GET /api/v1/movements/{movementId}` con cabecera `Authorization: Bearer <accessToken>`. Recibir `200 OK` con el objeto `movement` y sus campos. | DTO modelado acorde al catálogo V6.0.2 y verificación de deserialización con importes como cadena decimal. |
| **M410-R03** | **Pertenencia y no revelación (MO-PR04, MO-PR06):** La identidad se deriva del JWT; un movimiento inexistente o de otra cuenta responde `404` sin distinguir el motivo ni exponer datos de terceros. El detalle se obtiene del endpoint y no de una lista conservada en memoria. | Pruebas de `404` por identificador ajeno y de ausencia de `userId`; verificación de que no se usa la lista como sustituto. |
| **M410-R04** | **Errores, estados y resiliencia (MO-PR08):** Manejar el sobre `{"error":{"code","message","details","traceId"}}` para `400` de identificador inválido, `401`, `403`, `404`, `429` y `500`; estados MVI de carga, contenido, no encontrado, error y sin conexión; reintento manual; `404` se presenta como "Movimiento no disponible" sin sugerir datos ajenos. | Pruebas unitarias de contenido, `404`, `401`, error de red y reintento. |
| **M410-R05** | **Simulación en `MockAuthApi`:** La fuente simulada devuelve el detalle de movimientos sintéticos predefinidos tras retardo de 200–500 ms, con importes como cadena decimal, moneda `MXN`, fechas RFC 3339 UTC y vínculos opcionales; devuelve `404` para identificadores desconocidos o marcados como ajenos y soporta el centinela `net@error.test`. | Casos de mock cubiertos: detalle con vínculo, sin vínculo, identificador desconocido y fallo de red. |
| **M410-R06** | **Cobertura de pruebas unitarias:** Suite `MovementDetailViewModelTest` y `MockAuthApiTest` con `kotlinx-coroutines-test` que cubra carga, `404`, `401`, error de red, vínculos y navegación al editor. | Pruebas unitarias aprobadas y conteo total de tests reportado sin alterar los 50 preexistentes. |

## Escenarios de prueba y evidencia
- **Caso favorable:** Desde el historial se abre un movimiento → `GET /api/v1/movements/{movementId}` → `200 OK` → la pantalla muestra concepto, importe, categoría, tipo, fecha real, origen y vínculo si existe.
- **Caso sin vínculos:** Movimiento manual sin `recurringPaymentId` ni meta → el detalle omite esas secciones sin mostrar campos vacíos ni ceros.
- **Caso identificador ajeno o inexistente:** `404` → estado "Movimiento no disponible" con acción Volver; no se revela si existe en otra cuenta.
- **Caso sin conexión:** Fallo de transporte → estado sin conexión con "Reintentar"; no se rellena el detalle con datos de la lista.
- **Caso acción Editar:** Pulsación de "Editar" → efecto de navegación al editor de `420-Editar-Movimiento` con el `movementId`.
- **Regresión:** Compilación exitosa de `./gradlew assembleDebug` y aprobación continua de los 50 tests preexistentes de autenticación.
- **Comandos esperados (en Fase B):** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
- **Evidencia exigida:** ejecución real, reporte de cobertura de tests, trazabilidad a requisitos y registro en `memoria.md`.

## Riesgos y bloqueos
- **Forma exacta de `movement`:** la ficha indica solo `200: movement`; campos, tipos y nombres de los vínculos con pago recurrente o meta **por confirmar con Backend**. El modelo usa los campos del listado y deja los vínculos como identificadores opcionales.
- **Código de identificador inválido:** no se define si un `movementId` mal formado produce `400` o `404`: **por confirmar con Backend**.
- **Datos del vínculo:** no se detalla si el detalle incluye el nombre de la meta o del pago recurrente o solo su identificador: **por confirmar con Backend**.
- **Caché del detalle:** la política de conservación local no está definida; se propone no persistir el detalle salvo decisión expresa.
- **Falta de Backend real:** el detalle se valida contra `MockAuthApi`; el comportamiento con datos reales queda fuera de esta unidad.

## Criterio de salida de la unidad
Todas las tareas en `task.md` documentadas en estado pendiente (`- [ ]`); trazabilidad completa requisito → plan → tarea; ninguna ejecución de código en Fase A; sin cambios fuera del alcance autorizado.
