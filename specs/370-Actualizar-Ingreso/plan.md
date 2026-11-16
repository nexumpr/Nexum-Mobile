---
project: Nexum-Mobile
unit: 370-Actualizar-Ingreso
kind: technical-plan
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# PLAN — Actualización de Ingreso Habitual (Update Habitual Income)

## Estrategia técnica
Implementar la actualización del ingreso habitual bajo el contrato **NEXUM-E025** en la arquitectura MVI y el patrón Repositorio:
1. **Contratos DTO y modelos de dominio:**
   - DTO de entrada: `UpdateIncomeRequest(amount: String, frequency: String)`.
   - DTO de respuesta: `IncomeResponse` (reutilizado de `360-Consultar-Ingreso`).
   - Validación: `IncomeValidator` con `BigDecimal`, sin `Float`/`Double`.
2. **Ampliación de interfaces:**
   - En `AuthApi`: `suspend fun updateIncome(accessToken: String, request: UpdateIncomeRequest): IncomeResponse`.
   - En `AuthRepository`: `suspend fun updateIncome(amount: String, frequency: String): AuthResult<Income>`.
3. **Flujo de ejecución en Repositorio:**
   - Valida `amount > 0` y canonicaliza a dos decimales antes de enviar.
   - Obtiene el token de acceso vigente de `TokenStorage`.
   - Envía `authApi.updateIncome(...)`.
   - En `200 OK` actualiza la copia local del ingreso y marca la instantánea de resumen como inválida (`summaryDirty = true`), sin calcular el resumen aquí.
   - No crea movimientos ni depósitos; la operación es un reemplazo del ingreso habitual.
   - Traduce el sobre de error estándar a `AuthErrorKind` conservando `traceId`.
4. **Estados e Intenciones MVI:**
   - `UpdateIncomeUiState` con importe capturado, frecuencia, guardado en curso, éxito, error, sin conexión y `traceId`.
   - `UpdateIncomeIntent` para cambiar importe, elegir frecuencia, guardar, reintentar y descartar error.
   - Efectos `NavigateBack` y `ShowConfirmation`.
5. **Simulación en `MockAuthApi`:**
   - Retardo determinista de 300–700 ms.
   - Valida `amount > 0` y dos decimales; si falla, responde `422`.
   - Recalcula `monthlyEquivalent` con los factores documentados (`daily=365/12`, `weekly=52/12`, `fortnightly=2`, `monthly=1`, `bimonthly=1/2`, `quarterly=1/3`, `semiannual=1/6`, `annual=1/12`).
   - Persiste el ingreso en memoria del usuario activo y no crea movimientos.
   - Centinelas `net@error.test` (red) y `rule_error_income` (`422`).

## Contratos y modelos de datos (NEXUM-E025)

```kotlin
// DTO de red (contrato NEXUM-E025)
data class UpdateIncomeRequest(
    val amount: String,
    val frequency: String
)

// Reutiliza el DTO de respuesta de E024
data class IncomeResponse(
    val amount: String,
    val frequency: String,
    val monthlyEquivalent: String,
    val currency: String
)

// Interfaz ampliada en AuthApi
interface AuthApi {
    // Métodos previos...
    suspend fun updateIncome(
        accessToken: String,
        request: UpdateIncomeRequest
    ): IncomeResponse
}

// Interfaz ampliada en AuthRepository
interface AuthRepository {
    // Métodos previos...
    suspend fun updateIncome(
        amount: String,
        frequency: String
    ): AuthResult<Income>
}
```

## Estados e Intenciones MVI

```kotlin
data class UpdateIncomeUiState(
    val amountInput: String = "",
    val frequency: String = "monthly",
    val amountError: String? = null,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val isOffline: Boolean = false,
    val errorMessage: String? = null,
    val traceId: String? = null
)

sealed interface UpdateIncomeIntent {
    data class AmountChanged(val value: String) : UpdateIncomeIntent
    data class FrequencySelected(val code: String) : UpdateIncomeIntent
    data object Save : UpdateIncomeIntent
    data object Retry : UpdateIncomeIntent
    data object DismissError : UpdateIncomeIntent
}

sealed interface UpdateIncomeEffect {
    data object NavigateBack : UpdateIncomeEffect
    data class ShowConfirmation(val monthlyEquivalent: String) : UpdateIncomeEffect
}
```

## Orden de trabajo
| Paso | Acción | Requisito rector |
| --- | --- | --- |
| **N370-P01** | Definir modelos del contrato NEXUM-E025 (`UpdateIncomeRequest`, reutilización de `IncomeResponse`) y `IncomeValidator`. | M370-R02 |
| **N370-P02** | Ampliar `AuthApi` y `AuthRepository` con la actualización, canonicalización decimal e invalidación de resumen. | M370-R02, M370-R04 |
| **N370-P03** | Implementar la simulación en `MockAuthApi` con validación, factores documentados, actualización en memoria y centinelas. | M370-R05 |
| **N370-P04** | Construir `UpdateIncomeViewModel` MVI con validación en línea, guardado y conservación del formulario ante error. | M370-R01, M370-R03, M370-R04 |
| **N370-P05** | Integrar el editor "Guardar ingreso" en Compose con Material 3, teclado decimal y accesibilidad. | M370-R01, M370-R03 |
| **N370-P06** | Desarrollar pruebas unitarias completas (`UpdateIncomeViewModelTest`, `IncomeValidatorTest`, `MockAuthApiTest`) con `kotlinx-coroutines-test`. | M370-R01…M370-R06 |

## Dependencias y decisiones técnicas
- **Aprobado:** Material 3 y Jetpack Compose sin librerías externas superfluas.
- **Dinero con `BigDecimal`:** la validación y la canonicalización usan `java.math.BigDecimal`; nunca `Float`/`Double`.
- **Reemplazo, no depósito (MO-PR04, MO-PR05):** `PUT /api/v1/income` sustituye el ingreso habitual; no crea movimientos, depósitos ni saldos.
- **M-D08 vigente:** la UI limita la frecuencia a `monthly` hasta confirmar el catálogo con Backend.
- **Consistencia del resumen:** el repositorio solo invalida la caché y expone `summaryDirty`; el recálculo autoritativo pertenece a `380-Resumen-Financiero`.
- **Sin optimismo falso:** no se muestra el nuevo disponible hasta que el servidor confirme la actualización.

## Estrategia de pruebas
- **Pruebas de ViewModel:**
  - `AmountChanged("12500.00")` y `Save` producen `isSaving = true` y luego `ShowConfirmation("12500.00")`.
  - `"0.00"`, `"-5.00"` y `"125.5"` bloquean el envío con `amountError`.
  - `422` conserva el importe capturado y muestra el mensaje de regla.
  - Fallo de red activa `isOffline` y permite `Retry` sin declarar guardado.
  - Doble pulsación no genera envíos concurrentes.
- **Pruebas de validación y mock:**
  - `"125.50"` se canonicaliza y se envía con dos decimales.
  - `fortnightly` con `"6000.00"` produce `monthlyEquivalent = "12000.00"` según el factor documentado.
  - La actualización no crea movimientos en la memoria del mock.
  - Centinelas `net@error.test` y `rule_error_income` producen `NETWORK` y `VALIDATION` respectivamente.
- **Pruebas de regresión:**
  - Verificación de `./gradlew assembleDebug` y aprobación continua de los 50 tests preexistentes de autenticación.
