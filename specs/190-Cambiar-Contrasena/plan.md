---
project: Nexum-Mobile
unit: 190-Cambiar-Contrasena
kind: technical-plan
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# PLAN — Cambio de Contraseña Autenticado (Change Password)

## Estrategia técnica
Implementar el cambio de contraseña autenticado bajo el contrato **NEXUM-E007** en la arquitectura MVI y el patrón Repositorio del módulo `auth`:
1. **Contratos DTO y mapeos de datos:**
   - DTO de entrada: `ChangePasswordRequest(val currentPassword: String, val newPassword: String)`.
   - Respuesta: HTTP `204 No Content` (mapeada a `AuthResult.Success(Unit)`).
2. **Ampliación de interfaces:**
   - En `AuthApi`: `suspend fun changePassword(accessToken: String, request: ChangePasswordRequest)`
   - En `AuthRepository`: `suspend fun changePassword(currentPassword: String, newPassword: String): AuthResult<Unit>`
3. **Validación local previa al request:**
   - `currentPassword.isNotBlank()`.
   - `AuthValidator.isValidPassword(newPassword)` (≥8 caracteres, alfanumérico).
   - `newPassword != currentPassword`.
   - `newPassword == confirmNewPassword`.
4. **Estados MVI de la pantalla:**
   - `ChangePasswordUiState`:
     - `currentPassword: String = ""`
     - `newPassword: String = ""`
     - `confirmNewPassword: String = ""`
     - `isCurrentPasswordVisible: Boolean = false`
     - `isNewPasswordVisible: Boolean = false`
     - `isLoading: Boolean = false`
     - `currentPasswordError: String? = null`
     - `newPasswordError: String? = null`
     - `confirmNewPasswordError: String? = null`
     - `successMessage: String? = null`
   - Intenciones `ChangePasswordIntent`:
     - `CurrentPasswordChanged(val value: String)`
     - `NewPasswordChanged(val value: String)`
     - `ConfirmNewPasswordChanged(val value: String)`
     - `ToggleCurrentPasswordVisibility`
     - `ToggleNewPasswordVisibility`
     - `Submit`
     - `DismissSuccess`
5. **Efectos y seguridad de memoria:**
   - Al recibir `204 No Content`, el ViewModel limpia inmediatamente los tres campos de contraseña de su estado (`currentPassword = ""`, `newPassword = ""`, `confirmNewPassword = ""`) para evitar retención prolongada en memoria (MO-PR06, MO-PR10).
   - Se emite `AuthEffect.ShowSnackbar("Contraseña actualizada con éxito")`.
6. **Simulación en `MockAuthApi`:**
   - Verifica que `currentPassword` coincida con la contraseña simulada del usuario activo (por defecto `"Password123"` en el fixture).
   - Si no coincide → lanza `AuthApiException(AuthErrorKind.INVALID_CREDENTIALS, "La contraseña actual no es correcta")`.
   - Si `newPassword == currentPassword` → lanza `AuthApiException(AuthErrorKind.CONFLICT, "La nueva contraseña no puede ser igual a la actual")`.
   - Si coincide y es válida → retardo de 600–1000 ms y retorna `Unit`.

## Contratos y modelos de datos (NEXUM-E007)

```kotlin
// DTO de red (contrato NEXUM-E007)
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

// Interfaz ampliada en AuthApi
interface AuthApi {
    // Métodos previos: register, login, refresh, logout, forgotPassword, resetPassword
    suspend fun changePassword(accessToken: String, request: ChangePasswordRequest)
}

// Interfaz ampliada en AuthRepository
interface AuthRepository {
    // Métodos previos: register, login, refreshSession, logout, requestPasswordReset, resetPassword
    suspend fun changePassword(currentPassword: String, newPassword: String): AuthResult<Unit>
}
```

## Estados e Intenciones MVI

```kotlin
data class ChangePasswordUiState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmNewPassword: String = "",
    val isCurrentPasswordVisible: Boolean = false,
    val isNewPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val currentPasswordError: String? = null,
    val newPasswordError: String? = null,
    val confirmNewPasswordError: String? = null,
    val isSuccess: Boolean = false
)

sealed interface ChangePasswordIntent {
    data class CurrentPasswordChanged(val value: String) : ChangePasswordIntent
    data class NewPasswordChanged(val value: String) : ChangePasswordIntent
    data class ConfirmNewPasswordChanged(val value: String) : ChangePasswordIntent
    data object ToggleCurrentPasswordVisibility : ChangePasswordIntent
    data object ToggleNewPasswordVisibility : ChangePasswordIntent
    data object Submit : ChangePasswordIntent
    data object DismissSuccess : ChangePasswordIntent
}
```

## Orden de trabajo
| Paso | Acción | Requisito rector |
| --- | --- | --- |
| **N190-P01** | Definir modelo DTO de NEXUM-E007 (`ChangePasswordRequest`). | M190-R03 |
| **N190-P02** | Ampliar contratos `AuthApi` y `AuthRepository` con la operación de cambio autenticado. | M190-R03, M190-R04 |
| **N190-P03** | Implementar la simulación en `MockAuthApi` con verificación de contraseña actual y centinelas. | M190-R04, M190-R05 |
| **N190-P04** | Construir `ChangePasswordViewModel` MVI con validaciones locales y reseteo de memoria en éxito. | M190-R01, M190-R02, M190-R04 |
| **N190-P05** | Diseñar `ChangePasswordScreen` en Compose con Material 3 e integrarlo en la navegación de Perfil. | M190-R01, M190-R04 |
| **N190-P06** | Desarrollar pruebas unitarias completas (`ChangePasswordViewModelTest`, `MockAuthApiTest`) con `kotlinx-coroutines-test`. | M190-R01…M190-R05 |

## Dependencias y decisiones técnicas
- **Aprobado**: Material 3 y Jetpack Compose sin dependencias externas.
- **Seguridad en memoria**: Los estados de contraseña se resetean inmediatamente al completar la mutación.
- **Aislamiento**: No altera el token de acceso local; la sesión del dispositivo actual continúa activa mientras el backend revoca otras sesiones remotas.

## Estrategia de pruebas
- **Pruebas de ViewModel:**
  - Validación local de campos (actual vacía, nueva corta, idéntica a la actual, confirmación no coincidente).
  - Estado de carga (`isLoading = true`) durante el procesamiento.
  - Éxito purga campos de contraseña y emite `ShowSnackbar`.
  - Error de contraseña actual errónea asigna mensaje en `currentPasswordError`.
- **Pruebas de Repositorio y Mock:**
  - `changePassword()` con contraseña correcta responde `204`.
  - `changePassword()` con contraseña incorrecta devuelve `Failure(INVALID_CREDENTIALS)`.
  - Simulación de error de red devuelve `Failure(NETWORK, retryable = true)`.
- **Pruebas de regresión:**
  - Mantener los 50 tests de autenticación previos en verde en `./gradlew testDebugUnitTest`.
