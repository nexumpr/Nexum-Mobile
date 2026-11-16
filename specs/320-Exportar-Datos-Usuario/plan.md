---
project: Nexum-Mobile
unit: 320-Exportar-Datos-Usuario
kind: technical-plan
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# PLAN — Exportación de Datos del Usuario (Export User Data)

## Estrategia técnica
Implementar la exportación de datos bajo el contrato **NEXUM-E020** en la arquitectura MVI y el patrón Repositorio:
1. **Contratos DTO y modelos de dominio:**
   - Formato solicitado: `enum class ExportFormat { JSON, CSV }` con serialización a `json`/`csv`.
   - DTO de archivo listo: `ExportReadyResponse(contentType, fileName, content)`.
   - DTO de trabajo diferido: `ExportPendingResponse(exportJobId)`.
   - Resultado de dominio: `ExportOutcome.FileReady(...)` o `ExportOutcome.Deferred(exportJobId)`.
2. **Ampliación de interfaces:**
   - En `AuthApi`: `suspend fun exportUserData(accessToken: String, format: ExportFormat): ExportResponse`.
   - En `AuthRepository`: `suspend fun exportUserData(format: ExportFormat): AuthResult<ExportOutcome>`.
3. **Flujo de ejecución en Repositorio:**
   - Obtiene el token de acceso vigente de `TokenStorage`; si no hay sesión, retorna `AuthResult.Failure(AuthErrorKind.UNAUTHORIZED)` sin llamar a la red.
   - Envía `authApi.exportUserData(accessToken, format)`.
   - En `200 OK` mapea cabecera y cuerpo a `ExportOutcome.FileReady`, sin escribir en disco ni en caché.
   - En `202 Accepted` mapea a `ExportOutcome.Deferred(exportJobId)`.
   - Traduce el sobre `{"error":{"code","message","details","traceId"}}` a `AuthErrorKind` conservando `traceId` para diagnóstico.
4. **Estados e Intenciones MVI:**
   - `ExportDataUiState` con formato seleccionado, carga, resultado, error, estado sin conexión y `traceId`.
   - `ExportDataIntent` para seleccionar formato, exportar, reintentar, descartar error y consumir resultado.
5. **Simulación en `MockAuthApi`:**
   - Retardo determinista de 400–800 ms.
   - `format = JSON` devuelve un documento sintético con `profile`, `movements`, `goals` y `budgets`.
   - `format = CSV` devuelve texto delimitado por comas con las mismas secciones.
   - Montos como cadena decimal (`"125.50"`), moneda `MXN`, fechas RFC 3339 UTC.
   - Escenario configurable de respuesta `202` con `exportJobId = "export_20261112_0001"`.
   - Centinela `net@error.test` lanza `AuthApiException(AuthErrorKind.NETWORK, ..., retryable = true)`.

## Contratos y modelos de datos (NEXUM-E020)

```kotlin
// Formato de exportación (query param)
enum class ExportFormat(val wireValue: String) {
    JSON("json"),
    CSV("csv")
}

// DTO de red: 200 OK (archivo)
data class ExportReadyResponse(
    val contentType: String,
    val fileName: String?,
    val content: String
)

// DTO de red: 202 Accepted (trabajo diferido)
data class ExportPendingResponse(
    val exportJobId: String
)

// Resultado tipado que devuelve el adaptador
sealed interface ExportResponse {
    data class Ready(val file: ExportReadyResponse) : ExportResponse
    data class Pending(val exportJobId: String) : ExportResponse
}

// Resultado de dominio
sealed interface ExportOutcome {
    data class FileReady(
        val contentType: String,
        val fileName: String?,
        val content: String
    ) : ExportOutcome

    data class Deferred(val exportJobId: String) : ExportOutcome
}

// Interfaz ampliada en AuthApi
interface AuthApi {
    // Métodos previos...
    suspend fun exportUserData(
        accessToken: String,
        format: ExportFormat
    ): ExportResponse
}

// Interfaz ampliada en AuthRepository
interface AuthRepository {
    // Métodos previos...
    suspend fun exportUserData(format: ExportFormat): AuthResult<ExportOutcome>
}
```

## Estados e Intenciones MVI

```kotlin
data class ExportDataUiState(
    val selectedFormat: ExportFormat = ExportFormat.JSON,
    val isLoading: Boolean = false,
    val outcome: ExportOutcome? = null,
    val errorMessage: String? = null,
    val isOffline: Boolean = false,
    val traceId: String? = null
)

sealed interface ExportDataIntent {
    data class SelectFormat(val format: ExportFormat) : ExportDataIntent
    data object Export : ExportDataIntent
    data object Retry : ExportDataIntent
    data object DismissError : ExportDataIntent
    data object ConsumeOutcome : ExportDataIntent
}

sealed interface ExportDataEffect {
    data class PresentFile(
        val fileName: String?,
        val contentType: String,
        val content: String
    ) : ExportDataEffect

    data class ShowMessage(val message: String) : ExportDataEffect
}
```

## Orden de trabajo
| Paso | Acción | Requisito rector |
| --- | --- | --- |
| **N320-P01** | Definir modelos del contrato NEXUM-E020 (`ExportFormat`, `ExportReadyResponse`, `ExportPendingResponse`, `ExportOutcome`). | M320-R02 |
| **N320-P02** | Ampliar `AuthApi` y `AuthRepository` con la exportación y el mapeo del sobre de errores (`traceId`). | M320-R02, M320-R04 |
| **N320-P03** | Implementar la simulación en `MockAuthApi` con JSON/CSV sintéticos, escenario diferido y centinela de red. | M320-R05 |
| **N320-P04** | Construir `ExportDataViewModel` MVI con control de doble pulsación, expiración del resultado al salir y estados de resiliencia. | M320-R01, M320-R03, M320-R04 |
| **N320-P05** | Integrar la sección de privacidad en `ProfileScreen` con selector de formato, acción accesible y entrega del archivo. | M320-R01, M320-R03 |
| **N320-P06** | Desarrollar pruebas unitarias completas (`ExportDataViewModelTest`, `MockAuthApiTest`) con `kotlinx-coroutines-test`. | M320-R01…M320-R06 |

## Dependencias y decisiones técnicas
- **Aprobado:** Material 3 y Jetpack Compose sin librerías externas superfluas.
- **Sin IDOR (MO-PR06):** el contrato no recibe `userId`; el conjunto exportado lo delimita el servidor a partir del token Bearer.
- **Sin persistencia del archivo:** el contenido exportado vive solo en memoria de presentación y se descarta al consumir el resultado; no se escribe en caché ni en logs (MO-PR06, MO-PR10).
- **Trabajo diferido:** la UI solo muestra `exportJobId`; el sondeo y la descarga temporal no se implementan hasta que Backend confirme el contrato.
- **Entrega del archivo:** se delega en el mecanismo estándar de Android que se apruebe en Fase B; no se inventa almacenamiento ni permisos adicionales.

## Estrategia de pruebas
- **Pruebas de ViewModel:**
  - `SelectFormat(CSV)` actualiza `selectedFormat` y no dispara peticiones.
  - `Export` activa `isLoading = true`, deshabilita la acción y produce `PresentFile` en `200 OK`.
  - `Export` con respuesta `202` produce estado con `Deferred(exportJobId)` y mensaje informativo.
  - Doble pulsación no genera peticiones concurrentes.
  - Error de red activa `isOffline = true`, conserva el formato elegido y permite `Retry`.
- **Pruebas de Repositorio y Mock:**
  - `exportUserData(JSON)` y `exportUserData(CSV)` devuelven el tipo de contenido esperado y montos como cadena decimal.
  - La respuesta `202` se mapea a `Deferred` sin lanzar error.
  - Centinela `net@error.test` emite `AuthResult.Failure(NETWORK)`.
  - El sobre de error conserva `traceId` y no expone detalles internos.
- **Pruebas de regresión:**
  - Verificación de `./gradlew assembleDebug` y aprobación continua de los 50 tests preexistentes de autenticación.
