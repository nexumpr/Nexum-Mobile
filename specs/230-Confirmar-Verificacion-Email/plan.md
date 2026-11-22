---
project: Nexum-Mobile
unit: 230-Confirmar-Verificacion-Email
kind: technical-plan
version: 1.1.0
status: ready
implementation: only_on_explicit_request
---

# PLAN — Confirmación de Verificación de Correo (NEXUM-E011)

## Estrategia técnica
Implementar la validación y confirmación de correo bajo el contrato **NEXUM-E011** (`POST /api/v1/auth/email-verification/confirm`) en la arquitectura MVI y el patrón Repositorio del módulo `auth`:

1. **Contrato HTTP público y modelos DTO (NEXUM-E011):**
    - Endpoint: `POST /api/v1/auth/email-verification/confirm` (ruta relativa Retrofit: `"auth/email-verification/confirm"`).
    - Autenticación: **Pública (sin cabecera `Authorization: Bearer`)**.
    - DTO de entrada (`ConfirmEmailVerificationRequest`):
      ```json
      { "token": "<CODIGO_DE_VERIFICACION_RECIBIDO>" }
      ```
    - DTO de respuesta (`ConfirmEmailVerificationResponse` — **HTTP `200 OK`**):
      ```json
      { "status": "accepted", "requestId": "<uuid>" }
      ```
    - Reglas estrictas del token:
        - Obligatorio, longitud mínima de **10 caracteres**.
        - **Sensible a mayúsculas y espacios**: **PROHIBIDO** aplicar `.trim()`, `.lowercase()` o alterar el string ingresado/pegado por el usuario.
        - **Seguridad / Logs**: **PROHIBIDO** imprimir o registrar el contenido del `token` en Logcat, excepciones o reportes de diagnóstico.
    - Mapeo de códigos HTTP:
        - `200 OK`: confirmación exitosa (transacción de uso único completada en DynamoDB) → `AuthResult.Success(ConfirmEmailVerificationOutcome)`.
        - `400 Bad Request`: código inválido, malformado o expirado → `AuthErrorKind.INVALID_TOKEN` (o error de validación no reintentable con el mismo código).
        - `409 Conflict`: código ya utilizado o carrera de confirmación concurrente → `AuthErrorKind.CONFLICT` (no reintentable).
        - Errores de red / servidor → `AuthErrorKind.NETWORK` / `AuthErrorKind.SERVER_UNAVAILABLE`.

2. **Ampliación de interfaces:**
    - En `AuthApiService` (Retrofit):
      ```kotlin
      @POST("auth/email-verification/confirm")
      suspend fun confirmEmailVerification(
          @Body request: ConfirmEmailVerificationRequest
      ): ConfirmEmailVerificationResponse
      ```
    - En `AuthApi`:
      `suspend fun confirmEmailVerification(request: ConfirmEmailVerificationRequest): ConfirmEmailVerificationResponse`
    - En `AuthRepository`:
      `suspend fun confirmEmailVerification(token: String): AuthResult<ConfirmEmailVerificationOutcome>`
        - Valida antes de invocar red que `token.isNotEmpty()` y `token.length >= 10` (sin aplicar `trim()`).

3. **Manejo de estado de verificación, sesión y navegación:**
    - Al recibir `200 OK`:
        - Marca `isConfirmed = true` y actualiza el estado visual del perfil (`emailVerified = true`) para impedir que el usuario vuelva a solicitar verificación en E010.
        - **Fuente de verdad del backend**: dado que no existe `GET /me` y `E003` (`refresh`) no devuelve el objeto `user`, la actualización definitiva del perfil del servidor ocurre en el siguiente inicio de sesión (**E002**); el estado local funciona como actualización visual inmediata tras E011.
        - Si hay sesión activa (`TokenStorage.current() != null`): muestra confirmación y permite continuar a la app (`AuthEffect.NavigateToApp`).
        - Si no hay sesión activa (`TokenStorage.current() == null`): muestra confirmación y botón para ir a inicio de sesión (`AuthEffect.NavigateToLogin`) donde E002 obtendrá `user.emailVerified = true`.

4. **Estados e Intenciones MVI (`ConfirmEmailVerificationScreen`):**
    - Pantalla dedicada con campo de texto para pegar el código recibido por E010 (sin auto-recorte de espacios ni transformación de mayúsculas).
    - `ConfirmEmailVerificationUiState`:
        - `token: String = ""`
        - `isLoading: Boolean = false`
        - `isConfirmed: Boolean = false`
        - `isTokenExpiredOrInvalid: Boolean = false`
        - `isTokenAlreadyUsed: Boolean = false`
        - `requestId: String? = null`
        - `errorMessage: String? = null`
    - Intenciones `ConfirmEmailVerificationIntent`:
        - `TokenChanged(val value: String)` (conserva el string exacto, sin `.trim()`)
        - `Submit`
        - `NavigateToLogin`
        - `NavigateToRequestVerification`

5. **Simulación en `MockAuthApi`:**
    - Conjunto en memoria de tokens utilizados (`usedVerificationTokens`).
    - Retardo determinista de 600–1000 ms.
    - Si `token.length < 10` o `token == "expired_verification_token"` o `token == "invalid_verification_token"` → lanza `AuthApiException(AuthErrorKind.INVALID_TOKEN, "El código de verificación es inválido o ha expirado", retryable = false)`.
    - Si `token` ya existe en `usedVerificationTokens` → lanza `AuthApiException(AuthErrorKind.CONFLICT, "Este código de verificación ya fue utilizado", retryable = false)`.
    - Si `token == "net_error_token"` → lanza `AuthApiException(AuthErrorKind.NETWORK, "Error de red al confirmar correo", retryable = true)`.
    - Éxito → registra `token` en `usedVerificationTokens` y responde `ConfirmEmailVerificationResponse(status = "accepted", requestId = UUID.randomUUID().toString())`.

## Contratos y modelos de datos (NEXUM-E011)

```kotlin
// DTOs de red (contrato NEXUM-E011)
data class ConfirmEmailVerificationRequest(
    val token: String
)

data class ConfirmEmailVerificationResponse(
    val status: String,
    val requestId: String
)

data class ConfirmEmailVerificationOutcome(
    val status: String,
    val requestId: String
)

// Interfaz ampliada en AuthApi
interface AuthApi {
    // Métodos previos...
    suspend fun confirmEmailVerification(
        request: ConfirmEmailVerificationRequest
    ): ConfirmEmailVerificationResponse
}

// Interfaz ampliada en AuthRepository
interface AuthRepository {
    // Métodos previos...
    suspend fun confirmEmailVerification(token: String): AuthResult<ConfirmEmailVerificationOutcome>
}