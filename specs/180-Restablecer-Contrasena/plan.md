---
project: Nexum-Mobile
unit: 180-Restablecer-Contrasena
kind: functional-plan
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# PLAN — Restablecimiento de Contraseña (Reset Password)

## Estrategia técnica
Implementar la pantalla y lógica de restablecimiento de contraseña bajo el contrato **NEXUM-E006** en la arquitectura MVI y el patrón Repositorio del módulo `auth`:
1. **Contratos DTO y mapeos de datos:**
   - DTO de entrada: `ResetPasswordRequest(val resetToken: String, val newPassword: String)`.
   - Respuesta: HTTP `204 No Content` (mapeada a `AuthResult.Success(Unit)`).
2. **Ampliación de interfaces:**
   - En `AuthApi`: `suspend fun resetPassword(request: ResetPasswordRequest)`
   - En `AuthRepository`: `suspend fun resetPassword(token: String, newPassword: String): AuthResult<Unit>`
3. **Validación local estricta:**
   - Reutilización de `AuthValidator.isValidPassword(newPassword)` (mínimo 8 caracteres, alfanumérico).
   - Verificación de igualdad exacta: `newPassword == confirmPassword`.
   - Token no vacío: `token.isNotBlank()`.
4. **Estados MVI de la pantalla:**
   - `ResetPasswordUiState`:
     - `token: String = ""`
     - `newPassword: String = ""`
     - `confirmPassword: String = ""`
     - `isPasswordVisible: Boolean = false`
     - `isLoading: Boolean = false`
     - `tokenError: String? = null`
     - `passwordError: String? = null`
     - `confirmPasswordError: String? = null`
     - `isTokenExpiredOrInvalid: Boolean = false`
   - Intenciones `ResetPasswordIntent`:
     - `TokenChanged(val value: String)`
     - `PasswordChanged(val value: String)`
     - `ConfirmPasswordChanged(val value: String)`
     - `TogglePasswordVisibility`
     - `Submit`
     - `NavigateToLogin`
     - `NavigateToForgotPassword`
5. **Efectos y navegación:**
   - Al completar con éxito, se emite `AuthEffect.ShowSnackbar("Contraseña restablecida con éxito...")` y `AuthEffect.NavigateToLogin`.
   - Si el token está expirado o es inválido, se expone la opción de navegar a `AuthScreen.ForgotPassword`.
6. **Simulación en `MockAuthApi`:**
   - Conjunto en memoria de tokens de reseteo utilizados (`usedResetTokens`).
   - Retardo simulado de 800–1200 ms.
   - Si `token` ya fue usado → lanza `AuthApiException(AuthErrorKind.INVALID_CREDENTIALS, "El token de recuperación ya fue utilizado")`.
   - Si `token == "expired_reset_token"` → lanza `AuthApiException(AuthErrorKind.INVALID_CREDENTIALS, "El token de recuperación ha expirado")`.
   - Si `token == "net_error_token"` → lanza `AuthApiException(AuthErrorKind.NETWORK, "Error de red al restablecer la contraseña", retryable = true)`.
   - Éxito → agrega el token a `usedResetTokens` y retorna `Unit`.

## Contratos y modelos de datos (NEXUM-E006)

```kotlin
// DTO de red (contrato NEXUM-E006)
data class ResetPasswordRequest(
    val resetToken: String,
    val newPassword: String
)

// Interfaz ampliada en AuthApi
interface AuthApi {
    // Métodos previos: register, login, refresh, logout, forgotPassword
    suspend fun resetPassword(request: ResetPasswordRequest)
}

// Interfaz ampliada en AuthRepository
interface AuthRepository {
    // Métodos previos: register, login, refreshSession, logout, requestPasswordReset
    suspend fun resetPassword(token: String, newPassword: String): AuthResult<Unit>
}
```

## Estados e Intenciones MVI

```kotlin
data class ResetPasswordUiState(
    val token: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val tokenError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isTokenExpiredOrInvalid: Boolean = false
)

sealed interface ResetPasswordIntent {
    data class TokenChanged(val value: String) : ResetPasswordIntent
    data class PasswordChanged(val value: String) : ResetPasswordIntent
    data class ConfirmPasswordChanged(val value: String) : ResetPasswordIntent
    data object TogglePasswordVisibility : ResetPasswordIntent
    data object Submit : ResetPasswordIntent
    data object NavigateToLogin : ResetPasswordIntent
    data object NavigateToForgotPassword : ResetPasswordIntent
}
```

## Integración en `AuthFlow`
Se amplía el enum de navegación en `AuthFlow.kt`:
```kotlin
enum class AuthScreen { Login, Register, ForgotPassword, ResetPassword, App }
```
Permite inicializar la pantalla con un token recibido (por ejemplo, desde parámetros de lanzamiento o deep link) y orquestar el retorno hacia Login.

## Orden de trabajo
| Paso | Acción | Requisito rector |
| --- | --- | --- |
| **N180-P01** | Definir modelo DTO de NEXUM-E006 (`ResetPasswordRequest`). | M180-R02 |
| **N180-P02** | Ampliar contratos `AuthApi` y `AuthRepository` con la operación de reseteo. | M180-R02, M180-R03 |
| **N180-P03** | Implementar la simulación en `MockAuthApi` con invalidación de un solo uso, retardo y centinelas. | M180-R04, M180-R05 |
| **N180-P04** | Construir `ResetPasswordViewModel` MVI con validaciones locales de contraseña y token. | M180-R01, M180-R03, M180-R04 |
| **N180-P05** | Diseñar `ResetPasswordScreen` en Compose con Material 3 e integrarlo en `AuthFlow`. | M180-R01, M180-R03 |
| **N180-P06** | Desarrollar pruebas unitarias completas (`ResetPasswordViewModelTest`, `MockAuthApiTest`) con `kotlinx-coroutines-test`. | M180-R01…M180-R05 |

## Dependencias y decisiones técnicas
- **Aprobado**: Jetpack Compose y Material 3 existentes; sin auto-login tras cambio de clave (MO-PR15).
- **Validación**: Coherencia en la regla de contraseñas con el registro (8 caracteres alfanuméricos).
- **Protección de Token**: El token no se almacena en `SharedPreferences` ni en texto persistente tras el reseteo.

## Estrategia de pruebas
- **Pruebas de ViewModel:**
  - Validación local de campos (contraseñas distintas, contraseña corta, token vacío).
  - Estado de carga (`isLoading = true`) durante el procesamiento.
  - Éxito emite `AuthEffect.NavigateToLogin` y mensaje informativo.
  - Token expirado activa `isTokenExpiredOrInvalid = true` y ofrece acción a `ForgotPassword`.
- **Pruebas de Repositorio y Mock:**
  - `resetPassword()` con token válido responde éxito y registra el token como usado.
  - Segundo intento con el mismo token devuelve error de token consumido.
  - Token `expired_reset_token` devuelve error de expiración.
  - Centinela `net_error_token` devuelve fallo de red reintentable.
- **Pruebas de regresión:**
  - Asegurar que `assembleDebug` y los 50 tests de autenticación previos sigan aprobándose.
