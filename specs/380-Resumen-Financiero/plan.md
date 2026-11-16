---
project: Nexum-Mobile
unit: 380-Resumen-Financiero
kind: technical-plan
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# PLAN — Resumen Financiero Mensual (Monthly Financial Summary)

## Estrategia técnica
Implementar la consulta del resumen financiero bajo el contrato **NEXUM-E026** en la arquitectura MVI y el patrón Repositorio:
1. **Contratos DTO y modelos de dominio:**
   - DTO de red: `FinancialSummaryResponse(incomeEquivalent, recurringFixedEstimate, oneTimeFixed, antExpenses, goalReservations, availableEstimate, currency)`.
   - Modelo de dominio: `FinancialSummary` con los mismos importes como `String`.
   - Verificación no destructiva: `SummaryFormula.check(...)` con `BigDecimal`.
2. **Ampliación de interfaces:**
   - En `AuthApi`: `suspend fun getFinancialSummary(accessToken: String, month: String, timezone: String? = null): FinancialSummaryResponse`.
   - En `AuthRepository`: `suspend fun getFinancialSummary(month: String): AuthResult<FinancialSummaryResult>`.
3. **Flujo de consulta y validación en Repositorio:**
   - Valida el formato `YYYY-MM` antes de llamar.
   - Consulta con `month` y `timezone` cuando aplique.
   - Comprueba la fórmula documentada con `BigDecimal`; si no coincide, conserva los importes del servidor y marca `isConsistent = false`.
   - Guarda el resumen por mes en caché; ante fallo de red devuelve el último valor con indicador de antigüedad.
   - Traduce el sobre de error estándar a `AuthErrorKind` conservando `traceId`.
4. **Estados e Intenciones MVI:**
   - `FinancialSummaryUiState` con mes seleccionado, resumen, consistencia, carga, sin conexión y error.
   - `FinancialSummaryIntent` para seleccionar mes, cargar, reintentar y descartar error.
   - La selección de mes cancela la petición anterior mediante el `Job` del ViewModel.
5. **Simulación en `MockAuthApi`:**
   - Retardo determinista de 300–700 ms.
   - `2026-11` devuelve el ejemplo documentado: `"12000.00"`, `"6500.00"`, `"0.00"`, `"350.00"`, `"200.00"`, `"4950.00"`, `"MXN"`.
   - Otros meses devuelven combinaciones sintéticas que respetan la fórmula.
   - Centinela `net@error.test` lanza `AuthApiException(AuthErrorKind.NETWORK, ..., retryable = true)`.

## Contratos y modelos de datos (NEXUM-E026)

```kotlin
// DTO de red (contrato NEXUM-E026)
data class FinancialSummaryResponse(
    val incomeEquivalent: String,
    val recurringFixedEstimate: String,
    val oneTimeFixed: String,
    val antExpenses: String,
    val goalReservations: String,
    val availableEstimate: String,
    val currency: String
)

// Modelo de dominio
data class FinancialSummary(
    val incomeEquivalent: String,
    val recurringFixedEstimate: String,
    val oneTimeFixed: String,
    val antExpenses: String,
    val goalReservations: String,
    val availableEstimate: String,
    val currency: String
)

data class FinancialSummaryResult(
    val summary: FinancialSummary,
    val isConsistent: Boolean,
    val fetchedAt: String? = null
)

// Interfaz ampliada en AuthApi
interface AuthApi {
    // Métodos previos...
    suspend fun getFinancialSummary(
        accessToken: String,
        month: String,
        timezone: String? = null
    ): FinancialSummaryResponse
}

// Interfaz ampliada en AuthRepository
interface AuthRepository {
    // Métodos previos...
    suspend fun getFinancialSummary(month: String): AuthResult<FinancialSummaryResult>
}
```

## Estados e Intenciones MVI

```kotlin
data class FinancialSummaryUiState(
    val selectedMonth: String = "2026-11",
    val isLoading: Boolean = false,
    val summary: FinancialSummary? = null,
    val isConsistent: Boolean = true,
    val isOffline: Boolean = false,
    val errorMessage: String? = null,
    val traceId: String? = null
)

sealed interface FinancialSummaryIntent {
    data class SelectMonth(val month: String) : FinancialSummaryIntent
    data object LoadSummary : FinancialSummaryIntent
    data object Retry : FinancialSummaryIntent
    data object DismissError : FinancialSummaryIntent
}
```

## Orden de trabajo
| Paso | Acción | Requisito rector |
| --- | --- | --- |
| **N380-P01** | Definir modelos del contrato NEXUM-E026 (`FinancialSummaryResponse`, `FinancialSummary`, `FinancialSummaryResult`) y `SummaryFormula`. | M380-R02 |
| **N380-P02** | Ampliar `AuthApi` y `AuthRepository` con la consulta por mes, la validación de la fórmula y la caché por mes. | M380-R02, M380-R04 |
| **N380-P03** | Implementar la simulación en `MockAuthApi` con el ejemplo documentado, otros meses y centinela de red. | M380-R05 |
| **N380-P04** | Construir `FinancialSummaryViewModel` MVI con cambio de mes, cancelación de peticiones y aviso de inconsistencia. | M380-R01, M380-R03, M380-R04 |
| **N380-P05** | Integrar la tarjeta de resumen en `HomeScreen` con Material 3, selector de mes y etiqueta de estimación. | M380-R01, M380-R03 |
| **N380-P06** | Desarrollar pruebas unitarias completas (`FinancialSummaryViewModelTest`, `SummaryFormulaTest`, `MockAuthApiTest`) con `kotlinx-coroutines-test`. | M380-R01…M380-R06 |

## Dependencias y decisiones técnicas
- **Aprobado:** Material 3 y Jetpack Compose sin librerías externas superfluas.
- **Fórmula autoritativa del servidor (MO-PR04):** el cliente verifica la resta con `BigDecimal` solo como control de consistencia; nunca corrige ni sustituye los importes recibidos.
- **No duplicación:** marcar un recurrente como pagado no debe añadirlo otra vez a `oneTimeFixed`; la comprobación se limita a la suma documentada.
- **Estimación visible (MO-PR05):** `availableEstimate` se rotula como estimación y no como saldo bancario.
- **Cancelación al cambiar de mes:** el ViewModel conserva un `Job` y cancela la consulta anterior antes de lanzar la nueva, evitando resultados tardíos.
- **Caché por mes:** cada mes se guarda por separado y se muestra con su fecha de obtención si no hay red.

## Estrategia de pruebas
- **Pruebas de ViewModel:**
  - `LoadSummary` publica los seis importes del ejemplo documentado.
  - `SelectMonth("2026-10")` cancela la petición anterior y publica solo el último resultado.
  - Una respuesta con fórmula incongruente activa `isConsistent = false` sin alterar importes.
  - Fallo de red con caché previa conserva el resumen y activa `isOffline`.
  - Mes con formato inválido no dispara la petición.
- **Pruebas de fórmula y mock:**
  - `SummaryFormula.check(...)` acepta el ejemplo (`4950.00 = 12000.00 - 6500.00 - 0.00 - 350.00 - 200.00`).
  - Rechaza una resta distinta sin lanzar excepción.
  - Centinela `net@error.test` emite `AuthResult.Failure(NETWORK)`.
- **Pruebas de regresión:**
  - Verificación de `./gradlew assembleDebug` y aprobación continua de los 50 tests preexistentes de autenticación.
