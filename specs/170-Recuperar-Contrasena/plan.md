---
project: Nexum-Mobile
unit: 170-Recuperar-Contrasena
kind: functional-plan
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# PLAN — Solicitud de Recuperación de Contraseña (Forgot Password)

## Estrategia técnica
Implementar la pantalla y flujo de solicitud de recuperación de contraseña bajo el contrato **NEXUM-E005** dentro de la arquitectura MVI y el patrón Repositorio del módulo `auth`:
1. **Contratos DTO y mapeos de datos:**
   - DTO de entrada: `ForgotPasswordRequest(val email: String)`.
   - DTO de respuesta: `ForgotPasswordResponse(val message: String)`.
   - Modelo de resultado: `ForgotPasswordOutcome(val message: String)`.
2. **Ampliación de interfaces:**
   - En `AuthApi`: `suspend fun forgotPassword(request: ForgotPasswordRequest): ForgotPasswordResponse`.
   - En `AuthRepository`: `suspend fun requestPasswordReset(email: String): AuthResult<ForgotPasswordOutcome>`.
3. **Validación local previa al request:**
   - Reutilización de la regex estándar de email de `AuthValidator`: `^[A-Za-z0-9+_.-]+@(.+)$`.
   - Impedir disparos de red si el formato es inválido o el campo está vacío.
4. **Estados MVI de la pantalla:**
   - `ForgotPasswordUiState`:
     - `email: String = ""`
     - `isLoading: Boolean = false`
     - `emailError: String? = null`
     - `isSubmitted: Boolean = false`
     - `feedbackMessage: String? = null`
     - `cooldownSeconds: Int = 0`
   - Intenciones `ForgotPasswordIntent`:
     - `EmailChanged(val value: String)`
     - `Submit`
     - `NavigateToLogin`
5. **Efectos y navegación:**
   - Emisión de `AuthEffect.NavigateToLogin` para volver a la pantalla de Login.
   - Enlace desde `LoginScreen`: conectar `LoginIntent.NavigateToForgotPassword` en `AuthFlow` con una nueva pantalla `AuthScreen.ForgotPassword`.
6. **Simulación en `MockAuthApi`:**
   - Retardo determinista de 800–1200 ms.
   - Devuelve `202 Accepted` neutro para cualquier correo válido.
   - Si `email == "rate_limited@error.test"` → lanza `AuthApiException(AuthErrorKind.RATE_LIMITED, "Demasiados intentos...")`.
   - Si `email == "net@error.test"` → lanza `AuthApiException(AuthErrorKind.NETWORK, "Error de red...")`.

## Contratos y modelos de datos (NEXUM-E005)

```kotlin
// DTOs de red (contrato NEXUM-E005)
data class ForgotPasswordRequest(
    val email: String
)

data class ForgotPasswordResponse(
    val message: String
)

data class ForgotPasswordOutcome(
    val message: String
)

// Interfaz ampliada en AuthApi
interface AuthApi {
    // Métodos previos: register, login, refresh, logout
    suspend fun forgotPassword(request: ForgotPasswordRequest): ForgotPasswordResponse
}

// Interfaz ampliada en AuthRepository
interface AuthRepository {
    // Métodos previos: register, login, refreshSession, logout
    suspend fun requestPasswordReset(email: String): AuthResult<ForgotPasswordOutcome>
}
```

## Estados e Intenciones MVI

```kotlin
data class ForgotPasswordUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val isSubmitted: Boolean = false,
    val feedbackMessage: String? = null,
    val cooldownSeconds: Int = 0
)

sealed interface ForgotPasswordIntent {
    data class EmailChanged(val value: String) : ForgotPasswordIntent
    data object Submit : ForgotPasswordIntent
    data object NavigateToLogin : ForgotPasswordIntent
}
```

## Integración en `AuthFlow`
Se amplía el enum de navegación en `AuthFlow.kt`:
```kotlin
enum class AuthScreen { Login, Register, ForgotPassword, App }
```
`LoginViewModel` emite un efecto informativo o de navegación `AuthEffect.NavigateToForgotPassword`, y `AuthFlow` transiciona a `ForgotPasswordScreen`.

## Orden de trabajo
| Paso | Acción | Requisito rector |
| --- | --- | --- |
| **N170-P01** | Definir modelos DTO de NEXUM-E005 (`ForgotPasswordRequest`, `ForgotPasswordResponse`) y `ForgotPasswordOutcome`. | M170-R02 |
| **N170-P02** | Ampliar contratos `AuthApi` y `AuthRepository` con la operación de recuperación. | M170-R02, M170-R03 |
| **N170-P03** | Implementar la simulación en `MockAuthApi` con respuesta neutra, retardo y centinelas de rate limit/red. | M170-R03, M170-R05 |
| **N170-P04** | Construir `ForgotPasswordViewModel` MVI: intenciones, reducer, estados inmutables y validación local de email. | M170-R01…M170-R04 |
| **N170-P05** | Diseñar `ForgotPasswordScreen` en Compose con Material 3 y conectar navegación en `AuthFlow`. | M170-R01, M170-R04 |
| **N170-P06** | Desarrollar pruebas unitarias completas (`ForgotPasswordViewModelTest`, `MockAuthApiTest`) con `kotlinx-coroutines-test`. | M170-R01…M170-R05 |

## Dependencias y decisiones técnicas
- **Aprobado**: Jetpack Compose y Material 3 existentes, sin dependencias externas adicionales.
- **Validación**: Reutiliza la lógica de validación centralizada en `AuthValidator.isValidEmail(email)`.
- **Privacidad estricta**: Cero diferencias observables entre correos registrados y no registrados en la UI (MO-PR06).

## Estrategia de pruebas
- **Pruebas de ViewModel:**
  - Validación local de email (vacío, inválido, válido).
  - Estado de carga (`isLoading = true`) durante el envío.
  - Recepción de respuesta exitosa activa `isSubmitted = true` y mensaje neutro.
  - Manejo de error `429` (rate limit) y error de red con `ShowSnackbar`.
- **Pruebas de Repositorio y Mock:**
  - `forgotPassword()` retorna `Success` con mensaje neutro para correos cualesquiera.
  - Centinela `rate_limited@error.test` retorna `Failure(RATE_LIMITED)`.
  - Centinela `net@error.test` retorna `Failure(NETWORK, retryable = true)`.
- **Pruebas de regresión:**
  - Verificar que los 50 tests previos de registro y login sigan pasando en `./gradlew testDebugUnitTest`.
