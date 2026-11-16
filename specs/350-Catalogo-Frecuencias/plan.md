---
project: Nexum-Mobile
unit: 350-Catalogo-Frecuencias
kind: technical-plan
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# PLAN — Catálogo de Frecuencias (Frequencies Catalog)

## Estrategia técnica
Implementar la consulta del catálogo de frecuencias bajo el contrato **NEXUM-E023** en la arquitectura MVI y el patrón Repositorio:
1. **Contratos DTO y modelos de dominio:**
   - DTO de red: `FrequencyResponse(code, label, validFor)`.
   - Modelo de dominio: `FrequencyOption(code, label, validFor)`.
   - Contexto tipado: `FrequencyContext { INCOME, RECURRING, MOVEMENT }` con serialización a `income`/`recurring`/`movement`.
   - Resultado con origen: `FrequencyCatalog(options, source)` reutilizando `CatalogSource { REMOTE, CACHE, BUNDLED }`.
2. **Ampliación de interfaces:**
   - En `AuthApi`: `suspend fun getFrequencies(context: FrequencyContext): List<FrequencyResponse>`.
   - En `AuthRepository`: `suspend fun getFrequencies(context: FrequencyContext): AuthResult<FrequencyCatalog>`.
3. **Flujo de lectura, filtrado y respaldo en Repositorio:**
   - Consulta el catálogo del contexto solicitado.
   - Descarta códigos desconocidos o entradas sin `code`/`label`; no inventa etiquetas.
   - Persiste el catálogo en caché y devuelve `REMOTE`; ante fallo usa `CACHE` o `BUNDLED`.
   - Traduce el sobre de error estándar a `AuthErrorKind` conservando `traceId`.
4. **Estados e Intenciones MVI:**
   - `FrequencyCatalogUiState` con contexto, opciones, selección, origen, carga, error y sin conexión.
   - `FrequencyCatalogIntent` para cargar, cambiar contexto, seleccionar, reintentar y descartar error.
5. **Simulación en `MockAuthApi`:**
   - Retardo determinista de 200–500 ms.
   - Devuelve los nueve códigos (`once`, `daily`, `weekly`, `fortnightly`, `monthly`, `bimonthly`, `quarterly`, `semiannual`, `annual`) con etiquetas en español y `validFor` por contexto.
   - En el contexto `income` y `recurring` el mock puede restringir según `validFor`; el alta conserva la limitación M-D08 (`monthly`).
   - Centinela `net@error.test` lanza `AuthApiException(AuthErrorKind.NETWORK, ..., retryable = true)`.

## Contratos y modelos de datos (NEXUM-E023)

```kotlin
// Contexto de consulta
enum class FrequencyContext(val wireValue: String) {
    INCOME("income"),
    RECURRING("recurring"),
    MOVEMENT("movement")
}

// DTO de red (contrato NEXUM-E023)
data class FrequencyResponse(
    val code: String,
    val label: String,
    val validFor: List<String> = emptyList()
)

// Modelo de dominio
data class FrequencyOption(
    val code: String,
    val label: String,
    val validFor: List<String> = emptyList()
)

data class FrequencyCatalog(
    val options: List<FrequencyOption>,
    val source: CatalogSource
)

// Interfaz ampliada en AuthApi
interface AuthApi {
    // Métodos previos...
    suspend fun getFrequencies(context: FrequencyContext): List<FrequencyResponse>
}

// Interfaz ampliada en AuthRepository
interface AuthRepository {
    // Métodos previos...
    suspend fun getFrequencies(context: FrequencyContext): AuthResult<FrequencyCatalog>
}
```

## Estados e Intenciones MVI

```kotlin
data class FrequencyCatalogUiState(
    val context: FrequencyContext = FrequencyContext.MOVEMENT,
    val isLoading: Boolean = false,
    val options: List<FrequencyOption> = emptyList(),
    val selectedCode: String? = null,
    val source: CatalogSource? = null,
    val isOffline: Boolean = false,
    val errorMessage: String? = null,
    val traceId: String? = null
)

sealed interface FrequencyCatalogIntent {
    data class ChangeContext(val context: FrequencyContext) : FrequencyCatalogIntent
    data object LoadFrequencies : FrequencyCatalogIntent
    data class SelectFrequency(val code: String) : FrequencyCatalogIntent
    data object Retry : FrequencyCatalogIntent
    data object DismissError : FrequencyCatalogIntent
}
```

## Orden de trabajo
| Paso | Acción | Requisito rector |
| --- | --- | --- |
| **N350-P01** | Definir modelos del contrato NEXUM-E023 (`FrequencyContext`, `FrequencyResponse`, `FrequencyOption`, `FrequencyCatalog`). | M350-R02 |
| **N350-P02** | Ampliar `AuthApi` y `AuthRepository` con la consulta por contexto, el filtrado de códigos y el respaldo local. | M350-R02, M350-R04 |
| **N350-P03** | Implementar la simulación en `MockAuthApi` con los nueve códigos, `validFor` y centinela de red. | M350-R05 |
| **N350-P04** | Construir `FrequencyCatalogViewModel` MVI con cambio de contexto, selección por código y restricción M-D08. | M350-R01, M350-R03, M350-R04 |
| **N350-P05** | Diseñar `FrequencySelector` en Compose con Material 3 y etiquetas del catálogo. | M350-R01, M350-R03 |
| **N350-P06** | Desarrollar pruebas unitarias completas (`FrequencyCatalogViewModelTest`, `MockAuthApiTest`) con `kotlinx-coroutines-test`. | M350-R01…M350-R06 |

## Dependencias y decisiones técnicas
- **Aprobado:** Material 3 y Jetpack Compose sin librerías externas superfluas.
- **Factores solo de estimación (MO-PR05):** la tabla mensual (`daily=365/12`, `weekly=52/12`, `fortnightly=2`, `monthly=1`, `bimonthly=1/2`, `quarterly=1/3`, `semiannual=1/6`, `annual=1/12`) se usa únicamente para equivalencia estimada; no produce fechas de vencimiento.
- **M-D08 vigente:** el alta conserva `monthly` como única frecuencia habilitada hasta confirmación con Backend; la UI de catálogo puede mostrar el resto, pero no habilitarlo donde M-D08 aplica.
- **Datos maestros de respaldo:** se permite empaquetar los códigos y etiquetas en Android; el origen se indica al usuario cuando no proviene del servidor.
- **Códigos desconocidos:** se descartan en lugar de mapearse a una etiqueta inventada.

## Estrategia de pruebas
- **Pruebas de ViewModel:**
  - Carga de `income`, `recurring` y `movement` devuelve opciones del contexto.
  - `SelectFrequency(code)` guarda el `code` y no la etiqueta.
  - En el alta solo `monthly` queda habilitado por M-D08.
  - Fallo de red con respaldo publica `isOffline = true` y `source` local.
- **Pruebas de Repositorio y Mock:**
  - Se conservan los nueve códigos documentados.
  - Ningún factor mensual genera fechas ni vencimientos (`LocalDate`/`Instant` no se derivan de la frecuencia).
  - Centinela `net@error.test` emite `AuthResult.Failure(NETWORK)`.
- **Pruebas de regresión:**
  - Verificación de `./gradlew assembleDebug` y aprobación continua de los 50 tests preexistentes de autenticación.
