---
project: Nexum-Mobile
unit: 220-Solicitud-Verificacion-Email
kind: technical-plan
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# PLAN — Solicitud de Verificación de Correo (Request Email Verification)

## Estrategia técnica
Implementar la solicitud y reenvío de verificación de correo bajo el contrato **NEXUM-E010** en la arquitectura MVI y el patrón Repositorio del módulo `auth`:
1. **Contratos DTO y mapeos de datos:**
   - DTO de entrada: `EmailVerificationRequest(val email: String? = null)`.
   - DTO de respuesta: `EmailVerificationResponse(val message: String)`.
   - Modelo de resultado: `EmailVerificationOutcome(val message: String)`.
2. **Ampliación de interfaces:**
   - En `AuthApi`: `suspend fun requestEmailVerification(accessToken: String?, request: EmailVerificationRequest): EmailVerificationResponse`.
   - En `AuthRepository`: `suspend fun requestEmailVerification(email: String?): AuthResult<EmailVerificationOutcome>`.
     - Si hay sesión en `TokenStorage`, envía el `accessToken`.
     - Si no hay sesión (flujo posterior a registro), envía `email` en el cuerpo.
3. **Manejo del temporizador de Cooldown:**
   - El ViewModel gestiona un contador de segundos (`cooldownSeconds: Int = 0`).
   - Al recibir éxito `202`, inicia una corrutina con un bucle de retardo de 60 segundos que decrementa el contador en cada segundo.
   - Mientras `cooldownSeconds > 0`, el botón permanece inhabilitado para evitar peticiones redundantes.
4. **Estados e Intenciones MVI:**
   - `EmailVerificationUiState`:
     - `email: String = ""`
     - `isLoading: Boolean = false`
     - `isSent: Boolean = false`
     - `cooldownSeconds: Int = 0`
     - `feedbackMessage: String? = null`
   - `EmailVerificationIntent`:
     - `RequestVerification(val targetEmail: String? = null)`
     - `DismissFeedback`
5. **Simulación en `MockAuthApi`:**
   - Retardo determinista de 600–1000 ms.
   - Devuelve `EmailVerificationResponse(message = "Si la cuenta existe y requiere verificación, se ha enviado un enlace de confirmación.")`.
   - Si `email == "rate_limited@error.test"` → lanza `AuthApiException(AuthErrorKind.RATE_LIMITED, "Demasiadas solicitudes...")`.
   - Si `email == "net@error.test"` → lanza `AuthApiException(AuthErrorKind.NETWORK, "Error de red...")`.

## Contratos y modelos de datos (NEXUM-E010)

```kotlin
// DTOs de red (contrato NEXUM-E010)
data class EmailVerificationRequest(
    val email: String? = null
)

data class EmailVerificationResponse(
    val message: String
)

data class EmailVerificationOutcome(
    val message: String
)

// Interfaz ampliada en AuthApi
interface AuthApi {
    // Métodos previos...
    suspend fun requestEmailVerification(accessToken: String?, request: EmailVerificationRequest): EmailVerificationResponse
}

// Interfaz ampliada en AuthRepository
interface AuthRepository {
    // Métodos previos...
    suspend fun requestEmailVerification(email: String? = null): AuthResult<EmailVerificationOutcome>
}
```

## Estados e Intenciones MVI

```kotlin
data class EmailVerificationUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val isSent: Boolean = false,
    val cooldownSeconds: Int = 0,
    val feedbackMessage: String? = null
)

sealed interface EmailVerificationIntent {
    data class RequestVerification(val targetEmail: String? = null) : EmailVerificationIntent
    data object DismissFeedback : EmailVerificationIntent
}
```

## Orden de trabajo
| Paso | Acción | Requisito rector |
| --- | --- | --- |
| **N220-P01** | Definir modelos DTO de NEXUM-E010 (`EmailVerificationRequest`, `EmailVerificationResponse`) y `EmailVerificationOutcome`. | M220-R02 |
| **N220-P02** | Ampliar contratos `AuthApi` y `AuthRepository` con la solicitud de verificación (soporte dual: con o sin token). | M220-R02, M220-R03 |
| **N220-P03** | Implementar la simulación en `MockAuthApi` con respuesta neutra, retardo y centinelas. | M220-R03, M220-R05 |
| **N220-P04** | Construir `EmailVerificationViewModel` MVI con corrutina de cooldown de 60 s y manejo de rate limit. | M220-R01, M220-R04 |
| **N220-P05** | Diseñar `EmailVerificationRequestScreen` en Compose con Material 3 y estado de cuenta regresiva. | M220-R01, M220-R04 |
| **N220-P06** | Desarrollar pruebas unitarias completas (`EmailVerificationViewModelTest`, `MockAuthApiTest`) con `kotlinx-coroutines-test`. | M220-R01…M220-R05 |

## Dependencias y decisiones técnicas
- **Aprobado**: Material 3 y Jetpack Compose sin dependencias externas.
- **Anti-enumeración estricta**: Cero diferencias observables en UI entre correos no registrados, correos ya verificados y correos pendientes (MO-PR06).
- **Flexibilidad de invocación**: Puede consumirse como pantalla independiente tras el registro o como diálogo/banner desde la configuración de perfil.

## Estrategia de pruebas
- **Pruebas de ViewModel:**
  - `RequestVerification` activa `isLoading = true` → respuesta neutra → activa `isSent = true` e inicia `cooldownSeconds = 60`.
  - Intento de envío durante cooldown es ignorado.
  - Manejo de `429 Rate Limit` muestra mensaje de espera sin bloquear permanentemente.
  - Manejo de error de red permite reintentar sin activar cooldown.
- **Pruebas de Repositorio y Mock:**
  - `requestEmailVerification()` envía token de `TokenStorage` si existe; si no, envía `email`.
  - Retorna `Success(EmailVerificationOutcome)` con mensaje neutro.
  - Centinela de red devuelve `Failure(NETWORK, retryable = true)`.
- **Pruebas de regresión:**
  - Asegurar que `assembleDebug` y los 50 tests de autenticación previos sigan aprobándose.
