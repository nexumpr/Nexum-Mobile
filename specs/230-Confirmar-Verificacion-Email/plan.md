---
project: Nexum-Mobile
unit: 230-Confirmar-Verificacion-Email
kind: technical-plan
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# PLAN — Confirmación de Verificación de Correo (Confirm Email Verification)

## Estrategia técnica
Implementar la validación y confirmación de correo bajo el contrato **NEXUM-E011** en la arquitectura MVI y el patrón Repositorio del módulo `auth`:
1. **Contratos DTO y mapeos de datos:**
   - DTO de entrada: `ConfirmEmailVerificationRequest(val verificationToken: String)`.
   - Respuesta: HTTP `204 No Content` (mapeada a `AuthResult.Success(Unit)`).
2. **Ampliación de interfaces:**
   - En `AuthApi`: `suspend fun confirmEmailVerification(request: ConfirmEmailVerificationRequest)`
   - En `AuthRepository`: `suspend fun confirmEmailVerification(token: String): AuthResult<Unit>`
3. **Manejo de navegación y estado de sesión:**
   - Si `TokenStorage.current() != null`: la sesión activa se actualiza localmente (`emailVerified = true`) y se ofrece continuar al núcleo de la app (`AuthEffect.NavigateToApp`).
   - Si `TokenStorage.current() == null`: se muestra confirmación de cuenta activada con botón de navegación a inicio de sesión (`AuthEffect.NavigateToLogin`).
4. **Estados MVI de la pantalla:**
   - `ConfirmEmailVerificationUiState`:
     - `token: String = ""`
     - `isLoading: Boolean = false`
     - `isConfirmed: Boolean = false`
     - `isTokenExpiredOrInvalid: Boolean = false`
     - `errorMessage: String? = null`
   - Intenciones `ConfirmEmailVerificationIntent`:
     - `TokenChanged(val value: String)`
     - `Submit`
     - `NavigateToLogin`
     - `NavigateToRequestVerification`
5. **Simulación en `MockAuthApi`:**
   - Conjunto en memoria de tokens de verificación utilizados (`usedVerificationTokens`).
   - Retardo determinista de 600–1000 ms.
   - Si `token` ya fue usado → lanza `AuthApiException(AuthErrorKind.CONFLICT, "El token de verificación ya fue utilizado")`.
   - Si `token == "expired_verification_token"` → lanza `AuthApiException(AuthErrorKind.INVALID_CREDENTIALS, "El enlace de verificación ha expirado")`.
   - Si `token == "net_error_token"` → lanza `AuthApiException(AuthErrorKind.NETWORK, "Error de red al confirmar correo", retryable = true)`.
   - Éxito → registra `token` en `usedVerificationTokens` y responde `Unit`.

## Contratos y modelos de datos (NEXUM-E011)

```kotlin
// DTOs de red (contrato NEXUM-E011)
data class ConfirmEmailVerificationRequest(
    val verificationToken: String
)

// Interfaz ampliada en AuthApi
interface AuthApi {
    // Métodos previos: register, login, refresh, logout, forgotPassword, resetPassword, changePassword, getSessions, revokeSession, requestEmailVerification
    suspend fun confirmEmailVerification(request: ConfirmEmailVerificationRequest)
}

// Interfaz ampliada en AuthRepository
interface AuthRepository {
    // Métodos previos...
    suspend fun confirmEmailVerification(token: String): AuthResult<Unit>
}
```

## Estados e Intenciones MVI

```kotlin
data class ConfirmEmailVerificationUiState(
    val token: String = "",
    val isLoading: Boolean = false,
    val isConfirmed: Boolean = false,
    val isTokenExpiredOrInvalid: Boolean = false,
    val errorMessage: String? = null
)

sealed interface ConfirmEmailVerificationIntent {
    data class TokenChanged(val value: String) : ConfirmEmailVerificationIntent
    data object Submit : ConfirmEmailVerificationIntent
    data object NavigateToLogin : ConfirmEmailVerificationIntent
    data object NavigateToRequestVerification : ConfirmEmailVerificationIntent
}
```

## Integración en `AuthFlow`
Se amplía el enum de navegación en `AuthFlow.kt`:
```kotlin
enum class AuthScreen { Login, Register, ForgotPassword, ResetPassword, EmailVerificationRequest, ConfirmEmailVerification, App }
```
Permite orquestar la apertura automática ante enlaces de verificación y transicionar sin fisuras tras el éxito.

## Orden de trabajo
| Paso | Acción | Requisito rector |
| --- | --- | --- |
| **N230-P01** | Definir modelo DTO de NEXUM-E011 (`ConfirmEmailVerificationRequest`). | M230-R02 |
| **N230-P02** | Ampliar contratos `AuthApi` y `AuthRepository` con la operación de confirmación. | M230-R02, M230-R04 |
| **N230-P03** | Implementar la simulación en `MockAuthApi` con invalidación de un solo uso, retardo y centinelas. | M230-R03, M230-R05 |
| **N230-P04** | Construir `ConfirmEmailVerificationViewModel` MVI con manejo de tokens y branching de navegación. | M230-R01, M230-R03, M230-R04 |
| **N230-P05** | Diseñar `ConfirmEmailVerificationScreen` en Compose con Material 3 e integrarlo en `AuthFlow`. | M230-R01, M230-R04 |
| **N230-P06** | Desarrollar pruebas unitarias completas (`ConfirmEmailVerificationViewModelTest`, `MockAuthApiTest`) con `kotlinx-coroutines-test`. | M230-R01…M230-R05 |

## Dependencias y decisiones técnicas
- **Aprobado**: Material 3 y Jetpack Compose sin dependencias externas.
- **Trazabilidad de un solo uso**: El token consumido se invalida inmediatamente en servidor/mock para impedir ataques de repetición.
- **Experiencia de usuario**: Si el token llega vía deep link, se procesa automáticamente para que el usuario no tenga que pulsar botones adicionales innecesarios.

## Estrategia de pruebas
- **Pruebas de ViewModel:**
  - Token precargado dispara confirmación automática.
  - Éxito con sesión activa emite `AuthEffect.NavigateToApp`.
  - Éxito sin sesión emite `AuthEffect.NavigateToLogin`.
  - Token expirado activa `isTokenExpiredOrInvalid = true` y ofrece navegación hacia reenvío.
- **Pruebas de Repositorio y Mock:**
  - `confirmEmailVerification()` con token válido responde éxito y registra el token como usado.
  - Segundo intento con el mismo token devuelve error de conflicto.
  - Centinela de red devuelve `Failure(NETWORK)`.
- **Pruebas de regresión:**
  - Asegurar que `assembleDebug` y los 50 tests de autenticación previos sigan aprobándose.
