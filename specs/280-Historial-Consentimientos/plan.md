---
project: Nexum-Mobile
unit: 280-Historial-Consentimientos
kind: technical-plan
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# PLAN — Historial de Consentimientos del Usuario (Consent History)

## Estrategia técnica
Implementar la consulta del historial de consentimientos bajo el contrato **NEXUM-E016** en la arquitectura MVI y el patrón Repositorio:
1. **Contratos DTO y modelos de dominio:**
   - DTO de respuesta: `ConsentItemResponse(val noticeVersion: String, val acceptedAt: String, val source: String)`.
   - Modelo de dominio: `ConsentRecord(val noticeVersion: String, val acceptedAt: String, val source: String, val formattedDate: String)`.
2. **Ampliación de interfaces:**
   - En `AuthApi`: `suspend fun getConsentHistory(accessToken: String): List<ConsentItemResponse>`.
   - En `AuthRepository`: `suspend fun getConsentHistory(): AuthResult<List<ConsentRecord>>`.
3. **Mapeo y formateo en Repositorio:**
   - Extrae el token de acceso vigente de `TokenStorage`.
   - Invoca `authApi.getConsentHistory(token)`.
   - Mapea cada `ConsentItemResponse` convirtiendo la fecha ISO-8601 UTC a la zona horaria del dispositivo y generando `formattedDate`.
   - Retorna `AuthResult.Success(sortedConsents)` ordenados cronológicamente de forma descendente.
4. **Estados e Intenciones MVI:**
   - `ConsentHistoryUiState`:
     - `isLoading: Boolean = false`
     - `isRefreshing: Boolean = false`
     - `consents: List<ConsentRecord> = emptyList()`
     - `errorMessage: String? = null`
   - Intenciones `ConsentHistoryIntent`:
     - `data object Load : ConsentHistoryIntent`
     - `data object Refresh : ConsentHistoryIntent`
     - `data object Retry : ConsentHistoryIntent`
5. **Simulación en `MockAuthApi`:**
   - Retardo determinista de 300–700 ms.
   - Si el usuario activo tiene sesión, devuelve una lista conteniendo al menos el registro inicial de registro (`noticeVersion = "2026-11-v1"`, `acceptedAt = "2026-11-02T17:30:00Z"`, `source = "registration"`).
   - Si el token corresponde al centinela `net@error.test` → lanza `AuthApiException(AuthErrorKind.NETWORK, "Error de red al consultar historial de consentimientos", retryable = true)`.

## Contratos y modelos de datos (NEXUM-E016)

```kotlin
// DTO de red (contrato NEXUM-E016)
data class ConsentItemResponse(
    val noticeVersion: String,
    val acceptedAt: String,
    val source: String
)

// Modelo de dominio
data class ConsentRecord(
    val noticeVersion: String,
    val acceptedAt: String,
    val source: String,
    val formattedDate: String = ""
)

// Interfaz ampliada en AuthApi
interface AuthApi {
    // Métodos previos...
    suspend fun getConsentHistory(accessToken: String): List<ConsentItemResponse>
}

// Interfaz ampliada en AuthRepository
interface AuthRepository {
    // Métodos previos...
    suspend fun getConsentHistory(): AuthResult<List<ConsentRecord>>
}
```

## Estados e Intenciones MVI

```kotlin
data class ConsentHistoryUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val consents: List<ConsentRecord> = emptyList(),
    val errorMessage: String? = null
) {
    val isEmpty: Boolean get() = !isLoading && consents.isEmpty() && errorMessage == null
}

sealed interface ConsentHistoryIntent {
    data object Load : ConsentHistoryIntent
    data object Refresh : ConsentHistoryIntent
    data object Retry : ConsentHistoryIntent
}
```

## Orden de trabajo
| Paso | Acción | Requisito rector |
| --- | --- | --- |
| **N280-P01** | Definir modelos DTO de NEXUM-E016 (`ConsentItemResponse`) y dominio (`ConsentRecord`). | M280-R02 |
| **N280-P02** | Ampliar contratos `AuthApi` y `AuthRepository` con la consulta de historial de consentimientos. | M280-R02, M280-R03 |
| **N280-P03** | Implementar la simulación en `MockAuthApi` con retardo, inmutabilidad y centinelas de error. | M280-R03, M280-R06 |
| **N280-P04** | Construir `ConsentHistoryViewModel` MVI con soporte de estados, pull-to-refresh y formateo temporal local. | M280-R04, M280-R05 |
| **N280-P05** | Diseñar pantalla Composable `ConsentHistoryScreen` en Jetpack Compose con Material 3 y tarjetas de auditoría. | M280-R01, M280-R03 |
| **N280-P06** | Desarrollar pruebas unitarias completas (`ConsentHistoryViewModelTest`, `MockAuthApiTest`) con `kotlinx-coroutines-test`. | M280-R01…M280-R06 |

## Dependencias y decisiones técnicas
- **Aprobado**: Material 3 y Jetpack Compose sin librerías externas superfluas.
- **Inmutabilidad estricta (MO-PR06)**: La lista de consentimientos es de solo lectura. No existen endpoints ni funciones de mutación individual; el historial solo crece mediante nuevos registros de consentimiento formal.
- **Seguridad anti-IDOR**: La consulta no admite parámetros de usuario; se basa exclusivamente en la identidad del JWT Bearer validada por el servidor.
- **Formateo temporal en dispositivo**: La conversión de marcas temporales RFC 3339 a hora local garantiza comprensión inmediata de la fecha y hora por el usuario.

## Estrategia de pruebas
- **Pruebas de ViewModel:**
  - Emisión de estado `Loading` al iniciar la carga.
  - Recepción exitosa transiciona a `Content` con lista formateada.
  - Recepción de lista vacía activa `isEmpty = true`.
  - Gesto de refresco actualiza `isRefreshing = true` y renueva datos.
  - Error de red conserva datos previos y muestra mensaje reintentable.
- **Pruebas de Repositorio y Mock:**
  - `getConsentHistory()` extrae el token y mapea respuestas a `ConsentRecord`.
  - Centinela de error emite `AuthResult.Failure(NETWORK)`.
- **Pruebas de regresión:**
  - Verificación de `./gradlew assembleDebug` y aprobación continua de los 50 tests preexistentes de autenticación.
