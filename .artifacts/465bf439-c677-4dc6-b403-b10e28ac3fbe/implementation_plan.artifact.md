# Plan de Implementación — Nexum Mobile (Oleada 3 / Endpoints E020, E026, E037, E042–E051)

Integrar los **13 endpoints de la Oleada 3** publicados en el backend de producción AWS (`https://d2lpmqy5w6x9es.cloudfront.net/api/v1/`) para completar los módulos de **Dashboard, Presupuestos, Aportaciones a Metas, Eliminación de Metas, Pagos Recurrentes y Exportación de Datos**.

## User Review Required

> [!IMPORTANT]
> - **URL de Producción:** Se utiliza la URL oficial de CloudFront `https://d2lpmqy5w6x9es.cloudfront.net/api/v1/`.
> - **Manejo de Dinero:** Todos los importes financieros se procesan estrictamente como cadenas decimales (`"250.00"`) con 2 decimales en moneda `MXN` usando `BigDecimal` / `String`, sin usar `Float`/`Double`.
> - **Idempotencia:** Operaciones mutativas (`POST goal-contributions`, `POST reverse`, `POST recurring-payments/payments`) envían `Idempotency-Key` (UUID aleatorio de 16–128 caracteres) reutilizado intacto en reintentos.
> - **Concurrencia Optimista:** `PUT /budgets` exige el encabezado `If-Match` con el `ETag` exacto obtenido en `GET /budgets`.
> - **Respuesta HTTP 204:** `DELETE /goals/{goalId}` responde HTTP 204 sin cuerpo JSON.

---

## Proposed Changes

### Capa de Datos (`mx.nexum.app.financial.data`)

#### [MODIFY] [FinancialDtos.kt](file:///C:/Users/Rayqu/StudioProjects/Nexum-Mobile/app/src/main/java/mx/nexum/app/financial/data/dto/FinancialDtos.kt)
- Ajustar y verificar la precisión de los DTOs para los 13 endpoints de Oleada 3:
  - **E020:** `UserExportResponse` (`profile`, `exportedAt`, `movements`, `goals`, `consents`, `income`, `recurringPayments`, `contributions`, `budgets`, `onboarding`, `preferences`, `nextCursor`).
  - **E026:** `FinancialSummaryOleada3Dto` (`period`, `currency`, `income`, `expenses`, `savings`, `net`, `asOf`).
  - **E037:** `RecurringPaymentRecordPaymentRequest` (`paidAt`, `amount`, `currency`).
  - **E043 & E044:** `ContributionPreviewRequest`, `ContributionPreviewResponse` (`remaining`, `canCommit`), `ContributionCreateRequest`, `GoalAllocationDto`.
  - **E045 & E046 & E047:** `ContributionDto`, `PageOfContributionResponse`, `ReverseContributionRequest`.
  - **E048 & E049 & E050:** `BudgetDto`, `PageOfBudgetResponse`, `BudgetReplaceRequest`, `BudgetUsageDto`, `PageOfBudgetUsageResponse`.
  - **E051:** `DashboardResponse` (`summary`, `goals`, `latestMovements`, `asOf`).

#### [MODIFY] [FinancialApiService.kt](file:///C:/Users/Rayqu/StudioProjects/Nexum-Mobile/app/src/main/java/mx/nexum/app/financial/data/network/FinancialApiService.kt)
- Declarar y verificar los 13 métodos Retrofit:
  - `@GET("users/me/export")` `exportUserData(@Query("limit") limit: Int?, @Query("cursor") cursor: String?)`
  - `@GET("financial-summary")` `getFinancialSummaryOleada3(@Query("period") period: String?)`
  - `@POST("recurring-payments/{id}/payments")` `recordRecurringPaymentPayment(@Path("id") id: String, @Header("Idempotency-Key") key: String, @Body body: RecurringPaymentRecordPaymentRequest)`
  - `@DELETE("goals/{goalId}")` `deleteGoal(@Path("goalId") goalId: String): Response<Unit>`
  - `@POST("goal-contributions/preview")` `previewContribution(@Body body: ContributionPreviewRequest)`
  - `@POST("goal-contributions")` `createContribution(@Header("Idempotency-Key") key: String, @Body body: ContributionCreateRequest)`
  - `@GET("goal-contributions")` `getContributions(@Query("limit") limit: Int?, @Query("cursor") cursor: String?, ...)`
  - `@GET("goal-contributions/{id}")` `getContributionDetail(@Path("id") id: String)`
  - `@POST("goal-contributions/{id}/reverse")` `reverseContribution(@Path("id") id: String, @Header("Idempotency-Key") key: String, @Body body: ReverseContributionRequest)`
  - `@GET("budgets")` `getBudgets(@Query("period") period: String?): Response<PageOfBudgetResponse>`
  - `@PUT("budgets")` `replaceBudgets(@Header("If-Match") ifMatch: String, @Body body: BudgetReplaceRequest): Response<PageOfBudgetResponse>`
  - `@GET("budgets/usage")` `getBudgetsUsage(@Query("period") period: String?)`
  - `@GET("dashboard")` `getDashboard(@Query("period") period: String?)`

#### [MODIFY] [FinancialRepository.kt](file:///C:/Users/Rayqu/StudioProjects/Nexum-Mobile/app/src/main/java/mx/nexum/app/financial/data/FinancialRepository.kt)
- Implementar los 13 métodos correspondientes en `FinancialRepositoryImpl` garantizando:
  - Inyección del token Bearer desde `TokenStorage`.
  - Captura del header `ETag` en `getBudgets()` y paso en `replaceBudgets()`.
  - Manejo de `Response<Unit>` 204 para `deleteGoal()`.
  - Captura e interpretación de `ErrorEnvelope` con `toFinancialError()` para errores en pantalla.

---

### Capa MVI y UI (`mx.nexum.app.financial.ui`)

#### [NEW / MODIFY] [Dashboard & Resumen]
- **`FinancialSummaryViewModel.kt` / `FinancialSummaryScreen.kt`:** Conectar `getDashboard` (E051) y `getFinancialSummaryOleada3` (E026) para presentar ingresos, egresos, ahorro transferido y saldo neto.

#### [NEW / MODIFY] [Presupuestos]
- **`BudgetsViewModel.kt` / `BudgetsScreen.kt`:**
  - Consulta de presupuestos (E048) reteniendo el `ETag`.
  - Consulta de uso real por categoría (E050) con barras de progreso.
  - Diálogo de edición/reemplazo completo (E049) enviando el `If-Match`.

#### [NEW / MODIFY] [Metas y Aportaciones]
- **`GoalsViewModel.kt` / `GoalsScreen.kt`:**
  - Eliminación de metas (E042).
  - Modal de Aportación: Previsualizar reparto (E043) con validación de `canCommit` y `remaining == 0.00`, y confirmación con `Idempotency-Key` (E044).
  - Historial de aportaciones (E045), detalle (E046) y diálogo para Revertir aportación (E047) enviando motivo e `Idempotency-Key`.

#### [MODIFY] [Pagos Recurrentes]
- **`RecurringPaymentsViewModel.kt` / `RecurringPaymentsScreen.kt`:**
  - Integrar el registro de pago real (E037) con `Idempotency-Key`.

#### [MODIFY] [Perfil y Ajustes]
- **`SettingsViewModel.kt` / `SettingsScreen.kt` / `ProfileScreen.kt`:**
  - Botón "Exportar Mis Datos" (E020) con paginación por cursor.

---

## Plan de Verificación

### Pruebas Automatizadas
- Compilación completa: `./gradlew assembleDebug`
- Pruebas unitarias de repositorios, DTOs y ViewModels MVI: `./gradlew testDebugUnitTest`
- Análisis estático: `./gradlew lintDebug`

### Verificación Manual
- Probar el flujo completo en emulador/dispositivo contra el servidor de producción CloudFront (`d2lpmqy5w6x9es.cloudfront.net`).
