# Walkthrough — Integración de la Guía Oleada 2 (15 Endpoints E014, E027–E041)

He implementado la integración técnica y funcional de los **15 endpoints de la Oleada 2** especificados en `Nexum_Oleada2_Guia_Integracion_Android_15_Endpoints.md`, incluyendo el flujo UI e interactivo de **Eliminación Programada de Cuenta** (`E014`).

## Cambios Realizados

### Capa de Datos Financiera y de Cuenta (`mx.nexum.app.financial.data`)

#### [MODIFY] [FinancialDtos.kt](file:///C:/Users/Rayqu/StudioProjects/Nexum-Mobile/app/src/main/java/mx/nexum/app/financial/data/dto/FinancialDtos.kt)
- Añadidos DTOs con anotaciones `@SerializedName` e importes decimales tipo `String` ("125.50"):
  - **Eliminación de Cuenta (E014):** `DeleteAccountRequest` ("DELETE_MY_ACCOUNT") y `DeleteAccountResponse` (`202 Scheduled`).
  - **Movimientos (E027–E030):** `MovementsListResponse`, `MovementOleada2Dto`, `CreateMovementOleada2Request`, `UpdateMovementOleada2Request`.
  - **Pagos Recurrentes (E032–E035):** `RecurringPaymentsListResponse`, `RecurringPaymentOleada2Dto`, `CreateRecurringPaymentOleada2Request`, `UpdateRecurringPaymentOleada2Request`.
  - **Metas de Ahorro (E038–E041):** `GoalsListResponse`, `GoalOleada2Dto`, `GoalTotalsDto`, `CreateGoalOleada2Request`, `UpdateGoalOleada2Request`.

#### [MODIFY] [FinancialApiService.kt](file:///C:/Users/Rayqu/StudioProjects/Nexum-Mobile/app/src/main/java/mx/nexum/app/financial/data/network/FinancialApiService.kt)
- Añadidas las 15 operaciones Retrofit con cabeceras `@Header("Idempotency-Key")` (para altas de movimientos, recurrentes y metas), `@Header("If-Match")` (para ediciones `PATCH`) y `@HTTP(method = "DELETE", path = "users/me", hasBody = true)` para eliminación de cuenta.

#### [MODIFY] [FinancialRepository.kt](file:///C:/Users/Rayqu/StudioProjects/Nexum-Mobile/app/src/main/java/mx/nexum/app/financial/data/FinancialRepository.kt)
- Expuestos los métodos correspondientes en `FinancialRepositoryImpl`, incluyendo `deleteAccount` que maneja el estado `202 Scheduled` y realiza la purga incondicional de `TokenStorage.clear()`.

### Módulo de Ajustes y Seguridad UI (`mx.nexum.app.financial.ui`)

#### [MODIFY] [SettingsViewModel.kt](file:///C:/Users/Rayqu/StudioProjects/Nexum-Mobile/app/src/main/java/mx/nexum/app/financial/ui/SettingsViewModel.kt), [SettingsScreen.kt](file:///C:/Users/Rayqu/StudioProjects/Nexum-Mobile/app/src/main/java/mx/nexum/app/financial/ui/SettingsScreen.kt)
- Añadido el botón **"⚠️ Eliminar Cuenta (E014)"** y un diálogo modal interactivo con alerta que exige la contraseña actual y el texto de confirmación exacto `"DELETE_MY_ACCOUNT"`.
- Al confirmar, ejecuta `financialRepository.deleteAccount(...)`, maneja la respuesta `202 Accepted` (`scheduled`), limpia las credenciales locales y devuelve al usuario a la pantalla de inicio de sesión.

---

## Resultados de Verificación

- **Compilación:** `./gradlew assembleDebug` → **BUILD SUCCESSFUL**.
- **Pruebas Unitarias:** `./gradlew testDebugUnitTest` → **240 tests pasados en verde (0 fallos)**.
