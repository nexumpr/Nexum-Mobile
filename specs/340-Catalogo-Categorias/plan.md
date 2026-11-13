---
project: Nexum-Mobile
unit: 340-Catalogo-Categorias
kind: technical-plan
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# PLAN — Catálogo de Categorías (Categories Catalog)

## Estrategia técnica
Implementar la consulta del catálogo de categorías bajo el contrato **NEXUM-E022** en la arquitectura MVI y el patrón Repositorio:
1. **Contratos DTO y modelos de dominio:**
   - DTO de red: `CategoryResponse(id, name, icon, active)`.
   - Modelo de dominio: `Category(id, name, iconKey, isActive)`.
   - Resultado con origen: `CategoryCatalog(categories, source)` con `CatalogSource { REMOTE, CACHE, BUNDLED }`.
2. **Ampliación de interfaces:**
   - En `AuthApi`: `suspend fun getCategories(locale: String? = null): List<CategoryResponse>`.
   - En `AuthRepository`: `suspend fun getCategories(): AuthResult<CategoryCatalog>`.
3. **Flujo de lectura y respaldo en Repositorio:**
   - Consulta `authApi.getCategories(locale)`; si responde, filtra entradas sin `id` o `name`, persiste el catálogo en caché y devuelve `CategoryCatalog(..., REMOTE)`.
   - Ante fallo de red usa la caché vigente (`CACHE`) o el catálogo empaquetado en Android (`BUNDLED`).
   - Traduce el sobre de error estándar a `AuthErrorKind` conservando `traceId`.
4. **Estados e Intenciones MVI:**
   - `CategoryCatalogUiState` con categorías, carga, origen del catálogo, selección, vacío, error y sin conexión.
   - `CategoryCatalogIntent` para cargar, seleccionar, reintentar y descartar error.
5. **Simulación en `MockAuthApi`:**
   - Retardo determinista de 200–500 ms.
   - Devuelve las nueve categorías documentadas con `id` estable, `name`, `icon` y `active`.
   - Incluye una categoría con `active = false` para el escenario de deshabilitado.
   - Centinela `net@error.test` lanza `AuthApiException(AuthErrorKind.NETWORK, ..., retryable = true)`.

## Contratos y modelos de datos (NEXUM-E022)

```kotlin
// DTO de red (contrato NEXUM-E022)
data class CategoryResponse(
    val id: String,
    val name: String,
    val icon: String?,
    val active: Boolean
)

// Modelo de dominio
data class Category(
    val id: String,
    val name: String,
    val iconKey: String?,
    val isActive: Boolean
)

enum class CatalogSource { REMOTE, CACHE, BUNDLED }

data class CategoryCatalog(
    val categories: List<Category>,
    val source: CatalogSource
)

// Interfaz ampliada en AuthApi
interface AuthApi {
    // Métodos previos...
    suspend fun getCategories(locale: String? = null): List<CategoryResponse>
}

// Interfaz ampliada en AuthRepository
interface AuthRepository {
    // Métodos previos...
    suspend fun getCategories(): AuthResult<CategoryCatalog>
}
```

## Estados e Intenciones MVI

```kotlin
data class CategoryCatalogUiState(
    val isLoading: Boolean = false,
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: String? = null,
    val source: CatalogSource? = null,
    val isEmpty: Boolean = false,
    val isOffline: Boolean = false,
    val errorMessage: String? = null,
    val traceId: String? = null
)

sealed interface CategoryCatalogIntent {
    data object LoadCategories : CategoryCatalogIntent
    data class SelectCategory(val categoryId: String) : CategoryCatalogIntent
    data object Retry : CategoryCatalogIntent
    data object DismissError : CategoryCatalogIntent
}
```

## Orden de trabajo
| Paso | Acción | Requisito rector |
| --- | --- | --- |
| **N340-P01** | Definir modelos del contrato NEXUM-E022 (`CategoryResponse`, `Category`, `CategoryCatalog`). | M340-R02 |
| **N340-P02** | Ampliar `AuthApi` y `AuthRepository` con la consulta, el filtrado de entradas inválidas y el respaldo local. | M340-R02, M340-R04 |
| **N340-P03** | Implementar la simulación en `MockAuthApi` con las nueve categorías, categoría inactiva y centinela de red. | M340-R05 |
| **N340-P04** | Construir `CategoryCatalogViewModel` MVI con selección por `id`, estado vacío y manejo de origen del catálogo. | M340-R01, M340-R03, M340-R04 |
| **N340-P05** | Diseñar `CategorySelector` reutilizable en Compose con Material 3 e integrarlo en los puntos de consumo previstos. | M340-R01, M340-R03 |
| **N340-P06** | Desarrollar pruebas unitarias completas (`CategoryCatalogViewModelTest`, `MockAuthApiTest`) con `kotlinx-coroutines-test`. | M340-R01…M340-R06 |

## Dependencias y decisiones técnicas
- **Aprobado:** Material 3 y Jetpack Compose sin librerías externas superfluas.
- **Identificador, no nombre (MO-PR04):** el selector emite `Category.id`; el nombre visible es solo presentación y el servidor valida el `categoryId` en cada escritura.
- **Datos maestros de respaldo:** la ficha y las reglas de referencia permiten empaquetar categorías en Android; el respaldo se marca como `BUNDLED` para no confundirlo con la respuesta vigente del servidor.
- **Entradas no confiables (MO-PR06):** se descartan elementos sin `id` o `name` y los iconos desconocidos se resuelven con un recurso neutro.
- **Sin autenticación inventada:** la ficha no detalla sesión; el repositorio no añade cabeceras que el contrato no solicite.

## Estrategia de pruebas
- **Pruebas de ViewModel:**
  - `LoadCategories` publica las nueve categorías y `source = REMOTE`.
  - `SelectCategory(id)` guarda el `id` estable y no el nombre.
  - Una entrada con `active = false` no puede seleccionarse.
  - Respuesta vacía activa `isEmpty` y muestra un mensaje comprensible.
  - Fallo de red con respaldo disponible publica `isOffline = true` y `source = BUNDLED/CACHE`.
- **Pruebas de Repositorio y Mock:**
  - `getCategories()` filtra entradas sin `id` o `name`.
  - Se preservan los nueve nombres documentados y el `id` de ejemplo `antojos`.
  - Centinela `net@error.test` emite `AuthResult.Failure(NETWORK)` y el respaldo se usa cuando corresponde.
- **Pruebas de regresión:**
  - Verificación de `./gradlew assembleDebug` y aprobación continua de los 50 tests preexistentes de autenticación.
