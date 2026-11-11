---
project: Nexum-Mobile
unit: 290-Registrar-Consentimiento
kind: technical-plan
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# PLAN — Registro de Consentimiento del Usuario (Record Consent)

## Estrategia técnica
Implementar el registro de aceptación de una nueva versión del aviso bajo el contrato **NEXUM-E017** en la arquitectura MVI y el patrón Repositorio:
1. **Contratos DTO y modelos de dominio:**
   - DTO de entrada: `RecordConsentRequest(val noticeVersion: String, val accepted: Boolean = true)`.
   - DTO de respuesta: `RecordConsentResponse(val consentId: String, val recordedAt: String)`.
   - Modelo de dominio: `ConsentReceipt(val consentId: String, val recordedAt: String)`.
2. **Ampliación de interfaces:**
   - En `AuthApi`: `suspend fun recordConsent(accessToken: String, request: RecordConsentRequest, idempotencyKey: String? = null): RecordConsentResponse`.
   - En `AuthRepository`: `suspend fun recordConsent(noticeVersion: String): AuthResult<ConsentReceipt>`.
3. **Flujo de ejecución y verificación en Repositorio:**
   - Recupera el token de acceso vigente de `TokenStorage`.
   - Verifica en caché o memoria si `noticeVersion` ya existe registrada en el historial del usuario; si ya existe, evita el envío de red y retorna `AuthResult.Success(ConsentReceipt("already_recorded", "..."))`.
   - Genera cabecera opcional `Idempotency-Key` (UUIDv4) para prevenir duplicados por reintentos de transporte.
   - Envía `authApi.recordConsent(...)`.
   - Si el servidor responde `201 Created` o `409 Conflict` (idempotencia en backend):
     - Registra el nuevo apunte en la lista local de consentimientos.
     - Retorna `AuthResult.Success(receipt)`.
   - Si falla por red o token inválido, retorna `AuthResult.Failure(...)`.
4. **Estados e Intenciones MVI:**
   - `RecordConsentUiState`:
     - `noticeVersion: String = ""`
     - `isAcceptedChecked: Boolean = false`
     - `isLoading: Boolean = false`
     - `isRecorded: Boolean = false`
     - `consentReceipt: ConsentReceipt? = null`
     - `isAlreadyRecorded: Boolean = false`
     - `errorMessage: String? = null`
   - Intenciones `RecordConsentIntent`:
     - `data class SetNoticeVersion(val version: String) : RecordConsentIntent`
     - `data class AcceptanceToggled(val checked: Boolean) : RecordConsentIntent`
     - `data object SubmitConsent : RecordConsentIntent`
     - `data object Dismiss : RecordConsentIntent`
5. **Simulación en `MockAuthApi`:**
   - Retardo determinista de 400–800 ms.
   - Si `request.accepted == false` → lanza `AuthApiException(AuthErrorKind.VALIDATION, "El consentimiento debe ser afirmativo")`.
   - Si `request.noticeVersion == "2026-11-v1"` y ya existe en memoria → lanza `AuthApiException(AuthErrorKind.CONFLICT, "El consentimiento ya fue registrado previamente")`.
   - Si `request.noticeVersion == "net_error_consent"` → lanza `AuthApiException(AuthErrorKind.NETWORK, "Error de red al registrar consentimiento", retryable = true)`.
   - Éxito estándar → almacena el registro en la lista del usuario en memoria y responde `RecordConsentResponse(consentId = "cns-${System.currentTimeMillis()}", recordedAt = "2026-11-11T15:30:00Z")`.

## Contratos y modelos de datos (NEXUM-E017)

```kotlin
// DTOs de red (contrato NEXUM-E017)
data class RecordConsentRequest(
    val noticeVersion: String,
    val accepted: Boolean = true
)

data class RecordConsentResponse(
    val consentId: String,
    val recordedAt: String
)

// Modelo de dominio
data class ConsentReceipt(
    val consentId: String,
    val recordedAt: String
)

// Interfaz ampliada en AuthApi
interface AuthApi {
    // Métodos previos...
    suspend fun recordConsent(
        accessToken: String,
        request: RecordConsentRequest,
        idempotencyKey: String? = null
    ): RecordConsentResponse
}

// Interfaz ampliada en AuthRepository
interface AuthRepository {
    // Métodos previos...
    suspend fun recordConsent(
        noticeVersion: String
    ): AuthResult<ConsentReceipt>
}
```

## Estados e Intenciones MVI

```kotlin
data class RecordConsentUiState(
    val noticeVersion: String = "",
    val isAcceptedChecked: Boolean = false,
    val isLoading: Boolean = false,
    val isRecorded: Boolean = false,
    val consentReceipt: ConsentReceipt? = null,
    val isAlreadyRecorded: Boolean = false,
    val errorMessage: String? = null
) {
    val canSubmit: Boolean
        get() = noticeVersion.isNotBlank() && 
                isAcceptedChecked && 
                !isAlreadyRecorded && 
                !isLoading
}

sealed interface RecordConsentIntent {
    data class SetNoticeVersion(val version: String) : RecordConsentIntent
    data class AcceptanceToggled(val checked: Boolean) : RecordConsentIntent
    data object SubmitConsent : RecordConsentIntent
    data object Dismiss : RecordConsentIntent
}
```

## Orden de trabajo
| Paso | Acción | Requisito rector |
| --- | --- | --- |
| **N290-P01** | Definir modelos DTO de NEXUM-E017 (`RecordConsentRequest`, `RecordConsentResponse`) y dominio (`ConsentReceipt`). | M290-R02 |
| **N290-P02** | Ampliar contratos `AuthApi` y `AuthRepository` con la operación de registro de consentimiento y prevención de duplicados. | M290-R02, M290-R03 |
| **N290-P03** | Implementar la simulación en `MockAuthApi` con persistencia en memoria, centinelas y retardo determinista. | M290-R03, M290-R06 |
| **N290-P04** | Construir `RecordConsentViewModel` MVI con control reactivo de envío (`canSubmit`) y sincronización inmediata con historial. | M290-R01, M290-R03, M290-R04 |
| **N290-P05** | Diseñar componente Composable `UpdateConsentDialog` en Jetpack Compose con Material 3 y enlace al aviso legal completo. | M290-R01, M290-R05 |
| **N290-P06** | Desarrollar pruebas unitarias completas (`RecordConsentViewModelTest`, `MockAuthApiTest`) con `kotlinx-coroutines-test`. | M290-R01…M290-R06 |

## Dependencias y decisiones técnicas
- **Aprobado**: Material 3 y Jetpack Compose sin librerías externas superfluas.
- **Prevención estricta de duplicados (MO-PR06)**: Validar contra el historial local antes de emitir la llamada previene sobrecarga de red y evita duplicidad en la base de datos de auditoría.
- **Respuesta a conflicto 409**: Si el backend reporta colisión porque la versión ya fue grabada, el cliente lo asimila como éxito idempotente y actualiza la UI para no bloquear al usuario.
- **Sincronización en memoria**: Tras el guardado, la nueva versión aceptada se propaga a `ConsentHistoryViewModel` sin requerir recargar la sesión.

## Estrategia de pruebas
- **Pruebas de ViewModel:**
  - `canSubmit` es falso si `isAcceptedChecked` es falso o si `isAlreadyRecorded` es verdadero.
  - Envío exitoso transiciona a `isRecorded = true` y recibe `consentReceipt`.
  - Intento de enviar una versión existente localmente desactiva el botón y muestra advertencia.
  - Conflicto 409 del servidor se absorbe como éxito idempotente.
  - Fallo de transporte conserva la selección del checkbox y permite reintento.
- **Pruebas de Repositorio y Mock:**
  - `recordConsent()` valida que `accepted == true` y genera recibo con ID único.
  - Centinela de error de red emite `AuthResult.Failure(NETWORK)`.
- **Pruebas de regresión:**
  - Verificación de `./gradlew assembleDebug` y aprobación continua de los 50 tests preexistentes de autenticación.
