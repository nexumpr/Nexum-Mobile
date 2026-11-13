---
project: Nexum-Mobile
unit: 360-Consultar-Ingreso
kind: technical-plan
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# PLAN — Consulta de Ingreso Habitual (Get Habitual Income)

## Estrategia técnica
Implementar la consulta del ingreso habitual bajo el contrato **NEXUM-E024** en la arquitectura MVI y el patrón Repositorio:
1. **Contratos DTO y modelos de dominio:**
   - DTO de red: `IncomeResponse(amount, frequency, monthlyEquivalent, currency)` con importes como `String`.
   - Modelo de dominio: `Income(amount, frequency, monthlyEquivalent, currency)`.
2. **Ampliación de interfaces:**
   - En `AuthApi`: `suspend fun getIncome(accessToken: String): IncomeResponse`.
   - En `AuthRepository`: `suspend fun getIncome(): AuthResult<Income>`.
3. **Flujo de lectura y caché en Repositorio:**
   - Obtiene el token de acceso vigente de `TokenStorage`.
   - Consulta `authApi.getIncome(accessToken)`.
   - En `200 OK` valida que `currency` sea `MXN` y que los importes sean cadenas decimales; mapea a `Income` y guarda copia local etiquetada con fecha de obtención.
   - Ante `404` o ausencia de ingreso devuelve `AuthResult.Failure(AuthErrorKind.NOT_FOUND)` para que la UI muestre estado vacío, sin fabricar `"0.00"`.
   - Ante fallo de transporte usa la copia local con indicador de antigüedad; sin copia, `AuthResult.Failure(NETWORK)`.
4. **Estados e Intenciones MVI:**
   - `IncomeUiState` con ingreso, carga, vacío, sin conexión, error y `traceId`.
   - `IncomeIntent` para cargar, refrescar, reintentar, descartar error y abrir editor.
   - Efecto `IncomeEffect.NavigateToIncomeEditor` para la unidad de actualización.
5. **Simulación en `MockAuthApi`:**
   - Retardo determinista de 250–600 ms.
   - Devuelve `IncomeResponse("12000.00", "monthly", "12000.00", "MXN")` para el usuario activo.
   - Escenario configurable sin ingreso (`404`) y centinela `net@error.test`.

## Contratos y modelos de datos (NEXUM-E024)

```kotlin
// DTO de red (contrato NEXUM-E024)
data class IncomeResponse(
    val amount: String,
    val frequency: String,
    val monthlyEquivalent: String,
    val currency: String
)

// Modelo de dominio (importes siempre String)
data class Income(
    val amount: String,
    val frequency: String,
    val monthlyEquivalent: String,
    val currency: String
)

// Interfaz ampliada en AuthApi
interface AuthApi {
    // Métodos previos...
    suspend fun getIncome(accessToken: String): IncomeResponse
}

// Interfaz ampliada en AuthRepository
interface AuthRepository {
    // Métodos previos...
    suspend fun getIncome(): AuthResult<Income>
}
```

## Estados e Intenciones MVI

```kotlin
data class IncomeUiState(
    val isLoading: Boolean = false,
    val income: Income? = null,
    val isEmpty: Boolean = false,
    val isOffline: Boolean = false,
    val errorMessage: String? = null,
    val traceId: String? = null
)

sealed interface IncomeIntent {
    data object LoadIncome : IncomeIntent
    data object Refresh : IncomeIntent
    data object Retry : IncomeIntent
    data object DismissError : IncomeIntent
    data object OpenEditor : IncomeIntent
}

sealed interface IncomeEffect {
    data object NavigateToIncomeEditor : IncomeEffect
}
```

## Orden de trabajo
| Paso | Acción | Requisito rector |
| --- | --- | --- |
| **N360-P01** | Definir modelos del contrato NEXUM-E024 (`IncomeResponse`, `Income`) con importes como cadena decimal. | M360-R02 |
| **N360-P02** | Ampliar `AuthApi` y `AuthRepository` con la consulta, validación de moneda y manejo de ausencia. | M360-R02, M360-R04 |
| **N360-P03** | Implementar la simulación en `MockAuthApi` con ingreso presente, ausente y centinela de red. | M360-R05 |
| **N360-P04** | Construir `IncomeViewModel` MVI con estados de contenido, vacío, error, sin conexión y apertura del editor. | M360-R01, M360-R03, M360-R04 |
| **N360-P05** | Integrar la tarjeta "Ingreso equivalente mensual" en `ProfileScreen` con Material 3 y etiqueta de estimación. | M360-R01, M360-R03 |
| **N360-P06** | Desarrollar pruebas unitarias completas (`IncomeViewModelTest`, `MockAuthApiTest`) con `kotlinx-coroutines-test`. | M360-R01…M360-R06 |

## Dependencias y decisiones técnicas
- **Aprobado:** Material 3 y Jetpack Compose sin librerías externas superfluas.
- **Sin IDOR (MO-PR06):** el contrato no recibe `userId`; el servidor delimita el ingreso por el token Bearer.
- **Modelo singular:** el contrato inicial admite un solo ingreso habitual activo; no se modela una lista.
- **Veracidad del dinero (MO-PR05):** `monthlyEquivalent` se rotula como estimación y no se usa como saldo ni como confirmación de depósito.
- **Sin recálculo autoritativo:** si la frecuencia no es `monthly`, el cliente muestra el valor del servidor; los factores de `350` son referencia de estimación, no una segunda fuente de verdad.

## Estrategia de pruebas
- **Pruebas de ViewModel:**
  - `LoadIncome` publica `income` con los cuatro campos y sin errores.
  - Respuesta `404` activa `isEmpty = true` y no muestra `"0.00"`.
  - Fallo de red con caché previa conserva el valor y activa `isOffline`.
  - `401` no borra la pantalla y muestra mensaje de sesión no autorizada.
  - `OpenEditor` emite `NavigateToIncomeEditor`.
- **Pruebas de Repositorio y Mock:**
  - El mapeo conserva `"12000.00"`, `"monthly"`, `"12000.00"` y `"MXN"` sin conversión a `Float`/`Double`.
  - Moneda distinta de `MXN` se rechaza como respuesta inválida.
  - Centinela `net@error.test` emite `AuthResult.Failure(NETWORK)`.
- **Pruebas de regresión:**
  - Verificación de `./gradlew assembleDebug` y aprobación continua de los 50 tests preexistentes de autenticación.
