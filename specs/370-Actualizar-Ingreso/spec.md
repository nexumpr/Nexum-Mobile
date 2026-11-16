---
project: Nexum-Mobile
unit: 370-Actualizar-Ingreso
kind: technical-spec
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# SPEC — Actualización de Ingreso Habitual (Update Habitual Income)

## Propósito
Permitir al usuario titular autenticado reemplazar su ingreso habitual y su periodicidad bajo el contrato **NEXUM-E025** (`PUT /api/v1/income`, `[M]`, catálogo V6.0.2), recalculando el equivalente mensual y actualizando el dinero disponible de forma consistente, sin inventar depósitos ni movimientos.

## Lugar en el plan SDD
- **Constitución:** [`.specify/memory/constitution.md`](../../.specify/memory/constitution.md) (vigente: MO-PR04 Backend como fuente autoritativa, MO-PR05 Veracidad del dinero: no inventar depósitos ni presentar estimaciones como confirmadas, MO-PR06 Privacidad y seguridad desde el diseño, MO-PR07 Usabilidad inclusiva Material 3, MO-PR08 Manejo explícito de conectividad y estados).
- **Tecnología:** Kotlin / Jetpack Compose / MVI (Android nativo).
- **Contrato:** NEXUM-E025 del catálogo de endpoints de referencia V6.0.2 (`PUT /api/v1/income` [M]).
- **Dependencias previas:** [`360-Consultar-Ingreso`](../360-Consultar-Ingreso/spec.md) (valor actual y apertura del editor), [`350-Catalogo-Frecuencias`](../350-Catalogo-Frecuencias/spec.md) (códigos y etiquetas de frecuencia), [`140-Login`](../140-Login/spec.md) (sesión activa), [`150-Refresh-Token`](../150-Refresh-Token/spec.md) (renovación de token) y [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md) (cliente HTTP y errores).
- **Naturaleza:** especificación técnica de mutación de dato financiero privado, validación y consistencia con el resumen.

## Alcance permitido
Formulario "Guardar ingreso" en el editor de ingreso habitual, DTO de contrato E025 (`UpdateIncomeRequest`), validación local con `BigDecimal`, ampliación de `AuthApi` y `AuthRepository`, cálculo del equivalente mensual en la simulación con los factores documentados, ViewModel MVI (`UpdateIncomeViewModel`) con control de doble envío, invalidación de la instantánea cacheada de resumen y pruebas unitarias de la unidad.

## Exclusiones explícitas
No crear depósitos, movimientos ni ingresos adicionales; no enviar ni aceptar `userId`; no habilitar frecuencias distintas de `monthly` mientras M-D08 siga vigente; no implementar el endpoint de resumen financiero (`380-Resumen-Financiero`) ni sus cálculos autoritativos; no permitir importes negativos, cero o con más de dos decimales; no usar `Float`/`Double` para dinero; no conectarse a un backend real en esta etapa; no publicar ni desplegar.

## Requisitos y criterios verificables
| ID | Requisito obligatorio | Aceptación mínima |
| --- | --- | --- |
| **M370-R01** | **Editor de ingreso habitual:** Formulario Compose Material 3 con campo de importe (teclado decimal), selector de frecuencia, texto de ayuda con el equivalente mensual estimado, validación en línea, botón "Guardar ingreso" con estado de guardado y confirmación; cancelar no modifica nada. Etiquetas legibles y errores asociados al campo. | Composable probado con importe válido, importe inválido, guardado y cancelación. |
| **M370-R02** | **Contrato y payload E025:** Enviar `PUT /api/v1/income` con cabecera `Authorization: Bearer <accessToken>` y cuerpo `{"amount":"12500.00","frequency":"monthly"}`. Recibir `200 OK` con el ingreso actualizado y `monthlyEquivalent`. | DTOs acorde al catálogo V6.0.2 y verificación de serialización con importes como cadena decimal. |
| **M370-R03** | **Importe positivo, decimales precisos y sin depósitos (MO-PR04, MO-PR05):** `amount` debe ser mayor que cero y expresarse con dos decimales; la operación reemplaza el ingreso habitual y no genera depósitos ni movimientos. El resumen se actualiza de forma consistente: el cliente invalida cualquier instantánea cacheada y no presenta el nuevo disponible como confirmado hasta recibirlo del servidor. | Pruebas de validación (`"0.00"`, `"-5.00"`, `"125.5"`, `"125.50"`) y de que no se crea ningún movimiento. |
| **M370-R04** | **Errores, estados y resiliencia (MO-PR08):** Manejar el sobre `{"error":{"code","message","details","traceId"}}` para `400`, `401`, `403`, `404`, `409`, `422`, `429` y `500`; estados MVI de edición, guardando, guardado, error y sin conexión; conservar los valores capturados ante fallo; impedir doble envío; no mostrar éxito sin respuesta del servidor. | Pruebas unitarias de error de validación, `422`, `401`, fallo de red y conservación del formulario. |
| **M370-R05** | **Simulación en `MockAuthApi`:** La fuente simulada valida `amount > 0` y dos decimales, aplica los factores documentados para recalcular `monthlyEquivalent`, actualiza el ingreso en memoria, responde tras 300–700 ms y soporta los centinelas `net@error.test` y `rule_error_income` (respuesta `422`). | Casos de mock cubiertos: actualización válida por frecuencia, importe inválido y fallo de red/regla. |
| **M370-R06** | **Cobertura de pruebas unitarias:** Suite `UpdateIncomeViewModelTest`, `IncomeValidatorTest` y `MockAuthApiTest` con `kotlinx-coroutines-test` que cubra validación, factores, guardado, errores y ausencia de depósitos. | Pruebas unitarias aprobadas y conteo total de tests reportado sin alterar los 50 preexistentes. |

## Escenarios de prueba y evidencia
- **Caso favorable mensual:** Usuario edita el ingreso a `"12500.00"` con `monthly` → `PUT /api/v1/income` → `200 OK` con `monthlyEquivalent = "12500.00"` → confirmación y regreso al perfil con el valor actualizado.
- **Caso frecuencia quincenal (referencia):** `"6000.00"` con `fortnightly` → el servidor devuelve `monthlyEquivalent = "12000.00"` aplicando el factor de estimación documentado → la UI muestra el valor recibido, sin recalcularlo como autoritativo.
- **Caso importe inválido:** `"0.00"`, `"-5.00"` o `"125.5"` → validación local impide el envío y asocia el error al campo.
- **Caso error de regla:** `422` → mensaje comprensible, se conserva el importe capturado y se permite corregir; no se crea ningún movimiento.
- **Caso sin conexión:** Fallo de transporte → estado sin conexión con "Reintentar"; no se declara guardado.
- **Caso doble pulsación:** Pulsaciones rápidas → `isSaving = true` deshabilita el botón y evita envíos concurrentes.
- **Regresión:** Compilación exitosa de `./gradlew assembleDebug` y aprobación continua de los 50 tests preexistentes de autenticación.
- **Comandos esperados (en Fase B):** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
- **Evidencia exigida:** ejecución real, reporte de cobertura de tests, trazabilidad a requisitos y registro en `memoria.md`.

## Riesgos y bloqueos
- **Redondeo del equivalente:** la regla exacta de redondeo y el número de decimales intermedios del servidor: **por confirmar con Backend**.
- **Consistencia del resumen:** el contrato de `summaryVersion` o de invalidación posterior pertenece a `380-Resumen-Financiero`; aquí solo se invalida la caché local: **por confirmar con Backend**.
- **Conflicto concurrente:** la ficha no define `409` ni versión optimista para este `PUT`: **por confirmar con Backend**.
- **M-D08 vigente:** el selector limita la frecuencia a `monthly` hasta confirmar el catálogo; no se deroga la decisión.
- **Campos de respuesta:** la ficha indica "ingreso actualizado y monthlyEquivalent"; la forma exacta del cuerpo se alinea con E024 y queda **por confirmar con Backend**.

## Criterio de salida de la unidad
Todas las tareas en `task.md` documentadas en estado pendiente (`- [ ]`); trazabilidad completa requisito → plan → tarea; ninguna ejecución de código en Fase A; sin cambios fuera del alcance autorizado.
