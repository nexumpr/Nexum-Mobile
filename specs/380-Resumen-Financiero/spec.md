---
project: Nexum-Mobile
unit: 380-Resumen-Financiero
kind: technical-spec
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# SPEC — Resumen Financiero Mensual (Monthly Financial Summary)

## Propósito
Entregar al usuario titular autenticado los cinco importes de la tarjeta superior de Inicio —ingreso equivalente, obligatorios recurrentes, obligatorios únicos, gastos hormiga y reservado para metas— junto con el disponible estimado del mes seleccionado, bajo el contrato **NEXUM-E026** (`GET /api/v1/financial-summary`, `[M]`, catálogo V6.0.2), conservando la fórmula documentada y evitando duplicar un pago recurrente fijo al marcarlo como pagado.

## Lugar en el plan SDD
- **Constitución:** [`.specify/memory/constitution.md`](../../.specify/memory/constitution.md) (vigente: MO-PR04 Backend como fuente autoritativa, MO-PR05 Veracidad del dinero: estimaciones y registros confirmados se distinguen, MO-PR06 Privacidad y seguridad desde el diseño, MO-PR07 Usabilidad inclusiva Material 3, MO-PR08 Manejo explícito de conectividad, MO-PR12 Responsabilidades desacopladas).
- **Tecnología:** Kotlin / Jetpack Compose / MVI (Android nativo).
- **Contrato:** NEXUM-E026 del catálogo de endpoints de referencia V6.0.2 (`GET /api/v1/financial-summary` [M]).
- **Dependencias previas:** [`360-Consultar-Ingreso`](../360-Consultar-Ingreso/spec.md) (ingreso equivalente), [`330-Bootstrap-Inicial`](../330-Bootstrap-Inicial/spec.md) (instantánea de Inicio), [`140-Login`](../140-Login/spec.md) (sesión activa), [`150-Refresh-Token`](../150-Refresh-Token/spec.md) (renovación de token), [`030-di-navegacion-ui`](../030-di-navegacion-ui/spec.md) (pantalla de Inicio) y [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md) (cliente HTTP y errores).
- **Naturaleza:** especificación técnica de lectura de importes estimados y su explicación visual.

## Alcance permitido
Consulta del resumen por mes en `HomeScreen`, DTO de contrato E026 (`FinancialSummaryResponse`), modelo de dominio (`FinancialSummary`), validación de la fórmula documentada con `BigDecimal`, ampliación de `AuthApi` y `AuthRepository`, selector de mes `YYYY-MM`, tarjeta de cinco importes con etiqueta de estimación, ViewModel MVI (`FinancialSummaryViewModel`) con cancelación de peticiones anteriores y pruebas unitarias de la unidad.

## Exclusiones explícitas
No recalcular el resumen como valor autoritativo ni corregir importes del servidor; no duplicar un pago recurrente fijo al marcarlo pagado; no implementar el registro de movimientos ni el pago de recurrentes; no enviar ni aceptar `userId`; no mostrar importes como cadenas vacías o `0` cuando la respuesta no llegó; no usar `Float`/`Double`; no conectarse a un backend real en esta etapa; no publicar ni desplegar.

## Requisitos y criterios verificables
| ID | Requisito obligatorio | Aceptación mínima |
| --- | --- | --- |
| **M380-R01** | **Tarjeta de resumen mensual en Inicio:** Componente Compose Material 3 con el disponible estimado destacado y los importes de Ingreso, Obligatorios, Hormiga y Reservado; selector de mes `YYYY-MM`; etiqueta visible de estimación, moneda `MXN` y montos con dos decimales; valores legibles sin depender solo del color. | Composable probado con mes, importes, etiqueta de estimación y cambio de mes. |
| **M380-R02** | **Contrato y payload E026:** Enviar `GET /api/v1/financial-summary?month=2026-11&timezone=<zona>` con cabecera `Authorization: Bearer <accessToken>`. Recibir `200 OK` con `incomeEquivalent`, `recurringFixedEstimate`, `oneTimeFixed`, `antExpenses`, `goalReservations`, `availableEstimate` y `currency`. | DTOs acorde al catálogo V6.0.2 y verificación de deserialización con cadenas decimales. |
| **M380-R03** | **Fórmula documentada y no duplicación (MO-PR04, MO-PR05):** Validar de forma no destructiva que `availableEstimate = incomeEquivalent - recurringFixedEstimate - oneTimeFixed - antExpenses - goalReservations`; si no coincide, mostrar los valores del servidor y señalar la inconsistencia sin corregirlos. Marcar un recurrente como pagado no debe duplicar su importe en `oneTimeFixed`. | Pruebas con el ejemplo documentado y con un caso de inconsistencia; verificación de no duplicación. |
| **M380-R04** | **Errores, estados y concurrencia (MO-PR08):** Manejar el sobre `{"error":{"code","message","details","traceId"}}` para `400` de mes inválido, `401`, `403`, `422`, `429` y `500`; estados MVI de carga, contenido, vacío, error y sin conexión; cancelar la petición anterior al cambiar de mes; conservar el último resumen etiquetado como anterior. | Pruebas unitarias de mes inválido, cambio rápido de mes, error HTTP y sin conexión. |
| **M380-R05** | **Simulación en `MockAuthApi`:** La fuente simulada devuelve para `2026-11` el ejemplo documentado (`"12000.00"`, `"6500.00"`, `"0.00"`, `"350.00"`, `"200.00"`, `"4950.00"`, `"MXN"`) tras retardo de 300–700 ms, responde otros meses con datos sintéticos consistentes y soporta el centinela `net@error.test`. | Casos de mock cubiertos: ejemplo documentado, otro mes y fallo de red. |
| **M380-R06** | **Cobertura de pruebas unitarias:** Suite `FinancialSummaryViewModelTest` y `MockAuthApiTest` con `kotlinx-coroutines-test` que cubra la fórmula, decimales, cambio de mes, cancelación, inconsistencia y errores. | Pruebas unitarias aprobadas y conteo total de tests reportado sin alterar los 50 preexistentes. |

## Escenarios de prueba y evidencia
- **Caso favorable documentado:** `GET /api/v1/financial-summary?month=2026-11` → `200 OK` con los seis importes del ejemplo → la tarjeta muestra Disponible `"4950.00"`, Ingreso `"12000.00"`, Obligatorios `"6500.00"`, Hormiga `"350.00"` y Reservado `"200.00"`; la fórmula se cumple.
- **Caso otro mes:** Selección de `2026-10` → respuesta sintética consistente → la tarjeta actualiza el mes y los importes.
- **Caso de inconsistencia:** `availableEstimate` distinto de la resta documentada → se muestran los valores del servidor y un aviso de inconsistencia, sin recalcular ni ocultar.
- **Caso de recurrente pagado:** El servidor no duplica el importe en `oneTimeFixed` → la tarjeta mantiene la suma documentada y no añade el pago dos veces.
- **Caso de cambio rápido de mes:** Selección sucesiva de meses → solo la última petición actualiza el estado; las anteriores se cancelan.
- **Caso sin conexión:** Fallo de transporte con resumen previo → se conserva el último valor etiquetado como anterior; sin caché se muestra error con reintento.
- **Regresión:** Compilación exitosa de `./gradlew assembleDebug` y aprobación continua de los 50 tests preexistentes de autenticación.
- **Comandos esperados (en Fase B):** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
- **Evidencia exigida:** ejecución real, reporte de cobertura de tests, trazabilidad a requisitos y registro en `memoria.md`.

## Riesgos y bloqueos
- **Zona horaria:** la ficha indica `timezone` "si aplica", sin formato ni catálogo de valores: **por confirmar con Backend**.
- **Mes por defecto:** la selección inicial del mes y el rango de meses consultables no están definidos: **por confirmar con Backend**.
- **Significado de cada importe:** la ficha nombra los campos y la fórmula; la composición exacta de `recurringFixedEstimate`, `oneTimeFixed`, `antExpenses` y `goalReservations` **por confirmar con Backend**.
- **Inconsistencia de fórmula:** no se define tolerancia de redondeo ni código de error; se manejará como aviso visual sin alterar los valores.
- **Caché por mes:** TTL e invalidación dependen de `060-cache-offline`/M-D10 y del resumen de escrituras; **por confirmar**.

## Criterio de salida de la unidad
Todas las tareas en `task.md` documentadas en estado pendiente (`- [ ]`); trazabilidad completa requisito → plan → tarea; ninguna ejecución de código en Fase A; sin cambios fuera del alcance autorizado.
