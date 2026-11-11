---
project: Nexum-Mobile
unit: 260-Eliminar-Cuenta
kind: technical-plan
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# PLAN — Eliminación Definitiva de Cuenta (Delete Account)

## Estrategia técnica
Implementar el flujo de eliminación de cuenta bajo el contrato **NEXUM-E014** en la arquitectura MVI y el patrón Repositorio:
1. **Contratos DTO y mapeos de datos:**
   - DTO de entrada: `DeleteAccountRequest(val password: String, val confirmation: String)`.
   - DTO de respuesta: `DeleteAccountResponse(val deletionRequestId: String? = null, val status: String? = null)` para respuestas asíncronas `202 Accepted` o síncronas `204 No Content`.
2. **Ampliación de interfaces de autenticación:**
   - En `AuthApi`: `suspend fun deleteAccount(accessToken: String, request: DeleteAccountRequest, idempotencyKey: String? = null): DeleteAccountResponse?`.
   - En `AuthRepository`: `suspend fun deleteAccount(password: String, confirmation: String): AuthResult<DeleteAccountResponse?>`.
3. **Flujo de ejecución en `AuthRepositoryImpl`:**
   - Si la sesión actual es de Invitado/Demo (`isDemoUser == true`):
     - No realiza petición HTTP remota.
     - Ejecuta purga local incondicional `tokenStorage.clear()`.
     - Retorna `AuthResult.Success(null)`.
   - Si es usuario real:
     - Genera una cabecera `Idempotency-Key` (UUIDv4) opcional para prevenir mutaciones duplicadas ante reintentos.
     - Llama a `authApi.deleteAccount(...)`.
     - Ante respuesta exitosa (204 o 202) o error 409 (conflicto de eliminación ya procesada):
       - Ejecuta `tokenStorage.clear()` de forma inmediata e incondicional (fail-secure, MO-PR15).
       - Retorna `AuthResult.Success(response)`.
     - Ante credenciales inválidas (401/400) o fallo de red:
       - Preserva `TokenStorage` intacto y retorna `AuthResult.Failure(...)`.
4. **Estados e Intenciones MVI:**
   - `DeleteAccountUiState`:
     - `password: String = ""`
     - `confirmation: String = ""`
     - `isPasswordVisible: Boolean = false`
     - `passwordError: String? = null`
     - `confirmationError: String? = null`
     - `isLoading: Boolean = false`
     - `isDeleted: Boolean = false`
     - `isAsyncDeletion: Boolean = false`
     - `deletionRequestId: String? = null`
     - `errorMessage: String? = null`
     - `isDemoUser: Boolean = false`
   - Intenciones `DeleteAccountIntent`:
     - `PasswordChanged(val value: String)`
     - `ConfirmationChanged(val value: String)`
     - `TogglePasswordVisibility`
     - `SubmitDelete`
     - `Dismiss`
5. **Simulación en `MockAuthApi`:**
   - Retardo determinista de 500–1000 ms.
   - Si `request.confirmation != "ELIMINAR"` → lanza `AuthApiException(AuthErrorKind.VALIDATION, "La confirmación debe ser 'ELIMINAR'")`.
   - Si `request.password == "wrong_password"` o no coincide con la contraseña activa → lanza `AuthApiException(AuthErrorKind.UNAUTHORIZED, "Contraseña incorrecta")`.
   - Si `request.password == "net_error_delete"` → lanza `AuthApiException(AuthErrorKind.NETWORK, "Error de red al procesar la eliminación", retryable = true)`.
   - Si `request.password == "async_delete"` → responde `DeleteAccountResponse(deletionRequestId = "del-req-12345", status = "processing")` con código HTTP 202.
   - Éxito estándar → purga usuario en memoria y responde `null` (equivalente a 204 No Content).

## Contratos y modelos de datos (NEXUM-E014)

```kotlin
// DTOs de red (contrato NEXUM-E014)
data class DeleteAccountRequest(
    val password: String,
    val confirmation: String
)

data class DeleteAccountResponse(
    val deletionRequestId: String? = null,
    val status: String? = null
)

// Interfaz ampliada en AuthApi
interface AuthApi {
    // Métodos previos...
    suspend fun deleteAccount(
        accessToken: String,
        request: DeleteAccountRequest,
        idempotencyKey: String? = null
    ): DeleteAccountResponse?
}

// Interfaz ampliada en AuthRepository
interface AuthRepository {
    // Métodos previos...
    suspend fun deleteAccount(
        password: String,
        confirmation: String
    ): AuthResult<DeleteAccountResponse?>
}
```

## Estados e Intenciones MVI

```kotlin
data class DeleteAccountUiState(
    val password: String = "",
    val confirmation: String = "",
    val isPasswordVisible: Boolean = false,
    val passwordError: String? = null,
    val confirmationError: String? = null,
    val isLoading: Boolean = false,
    val isDeleted: Boolean = false,
    val isAsyncDeletion: Boolean = false,
    val deletionRequestId: String? = null,
    val errorMessage: String? = null,
    val isDemoUser: Boolean = false
) {
    val canSubmit: Boolean
        get() = if (isDemoUser) {
            !isLoading
        } else {
            password.isNotBlank() && 
            confirmation.trim() == "ELIMINAR" && 
            passwordError == null && 
            confirmationError == null && 
            !isLoading
        }
}

sealed interface DeleteAccountIntent {
    data class PasswordChanged(val value: String) : DeleteAccountIntent
    data class ConfirmationChanged(val value: String) : DeleteAccountIntent
    data object TogglePasswordVisibility : DeleteAccountIntent
    data object SubmitDelete : DeleteAccountIntent
    data object Dismiss : DeleteAccountIntent
}
```

## Orden de trabajo
| Paso | Acción | Requisito rector |
| --- | --- | --- |
| **N260-P01** | Definir modelos DTO de NEXUM-E014 (`DeleteAccountRequest`, `DeleteAccountResponse`). | M260-R02 |
| **N260-P02** | Ampliar contratos `AuthApi` y `AuthRepository` con la operación de eliminación de cuenta. | M260-R02, M260-R03 |
| **N260-P03** | Implementar la simulación en `MockAuthApi` con validación de credenciales, centinelas y respuestas 204/202. | M260-R05, M260-R06 |
| **N260-P04** | Construir `DeleteAccountViewModel` MVI con doble validación, branching de usuario demo y purga de sesión. | M260-R01, M260-R03, M260-R04 |
| **N260-P05** | Diseñar el diálogo Composable `DeleteAccountDialog` en Jetpack Compose con Material 3 y advertencias de alto impacto. | M260-R01, M260-R05 |
| **N260-P06** | Desarrollar pruebas unitarias completas (`DeleteAccountViewModelTest`, `MockAuthApiTest`) con `kotlinx-coroutines-test`. | M260-R01…M260-R06 |

## Dependencias y decisiones técnicas
- **Aprobado**: Material 3 y Jetpack Compose nativo sin librerías externas superfluas.
- **Doble confirmación obligatoria**: La combinación de contraseña + palabra clave `"ELIMINAR"` previene destrucciones accidentales por toques involuntarios.
- **Purga incondicional y fail-secure**: Tanto la respuesta 204 como la 202 o un conflicto 409 desencadenan la limpieza inmediata de `TokenStorage`, asegurando que el dispositivo no retenga credenciales de una cuenta revocada.
- **Tratamiento de modo Demo**: Aislamiento estricto que evita peticiones a la API para usuarios invitados conforme al hallazgo decisivo del catálogo V6.0.2.

## Estrategia de pruebas
- **Pruebas de ViewModel:**
  - `canSubmit` es falso si `confirmation != "ELIMINAR"` o si `password` está vacío.
  - `canSubmit` se habilita inmediatamente si el usuario está en modo demo.
  - Eliminación exitosa real limpia `TokenStorage`, actualiza `isDeleted = true` y emite `AuthEffect.NavigateToLogin`.
  - Manejo de respuesta asíncrona 202 (`isAsyncDeletion = true`, `deletionRequestId` presente).
  - Contraseña incorrecta emite error contextual sin cerrar sesión.
  - Cancelar el diálogo purga la contraseña de la memoria.
- **Pruebas de Repositorio y Mock:**
  - `deleteAccount()` valida contraseña y confirmación.
  - Respuesta 204 y 202 simuladas adecuadamente.
  - Centinela de red devuelve error reintentable sin purgar `TokenStorage`.
- **Pruebas de regresión:**
  - Verificación de `./gradlew assembleDebug` y aprobación continua de los 50 tests de autenticación previos.
