# Walkthrough — Edición y Baja Lógica de Pagos Recurrentes (Endpoints E035 y E036)

He completado la implementación de los controles de **Edición** (`PATCH /recurring-payments/{id}` E035) y **Desactivación / Baja Lógica** (`DELETE /recurring-payments/{id}` E036) para la pantalla de Pagos Recurrentes.

## Cambios Realizados

### Módulo Financiero de Pagos Recurrentes (`mx.nexum.app.financial.ui`, `data`)

#### [MODIFY] [RecurringPaymentsViewModel.kt](file:///C:/Users/Rayqu/StudioProjects/Nexum-Mobile/app/src/main/java/mx/nexum/app/financial/ui/RecurringPaymentsViewModel.kt)
- Incorporada la intención `EditPayment(paymentId, name, amount, nextDueDate, version)` invocando `updateRecurringPaymentOleada2` con la cabecera de versión `If-Match: "v$version"`.

#### [MODIFY] [RecurringPaymentsScreen.kt](file:///C:/Users/Rayqu/StudioProjects/Nexum-Mobile/app/src/main/java/mx/nexum/app/financial/ui/RecurringPaymentsScreen.kt)
- Añadido el botón de **Lápiz / Editar (✎)** en cada tarjeta de pago recurrente.
- Creado el **Diálogo Modal de Edición (E035)** precargado con el nombre, monto y fecha de próximo vencimiento actual.
- Creado el **Diálogo Modal de Confirmación de Desactivación (E036)** al presionar el botón `×` para evitar borrados accidentales.

#### [MODIFY] [FinancialApiService.kt](file:///C:/Users/Rayqu/StudioProjects/Nexum-Mobile/app/src/main/java/mx/nexum/app/financial/data/network/FinancialApiService.kt), [FinancialRepository.kt](file:///C:/Users/Rayqu/StudioProjects/Nexum-Mobile/app/src/main/java/mx/nexum/app/financial/data/FinancialRepository.kt)
- Conectado el endpoint `updateRecurringPaymentOleada2` enviando la cabecera `If-Match` para concurrencia optimista.

---

## Resultados de Verificación

- **Compilación:** `./gradlew assembleDebug` → **BUILD SUCCESSFUL**.
- **Pruebas Unitarias:** `./gradlew testDebugUnitTest` → **329 tests pasados en verde (0 fallos)**.
