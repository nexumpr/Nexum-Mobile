---
project: Nexum-Mobile
unit: 220-Solicitud-Verificacion-Email
kind: technical-plan
version: 1.1.0
status: ready
implementation: only_on_explicit_request
---

# PLAN — Solicitud de Verificación de Correo (NEXUM-E010)

## Estrategia técnica
Implementar la solicitud y reenvío de verificación de correo para usuarios autenticados bajo el contrato **NEXUM-E010** (`POST /api/v1/auth/email-verification/request`) en la arquitectura MVI y el patrón Repositorio del módulo `auth`:

1. **Contrato HTTP estricto (Bearer obligatorio y SIN body):**
    - Endpoint: `POST /api/v1/auth/email-verification/request`
    - Cabecera obligatoria: `Authorization: Bearer <ACCESS_TOKEN>` (obtenido de `TokenStorage`). Si no hay sesión activa, falla localmente sin invocar red.
    - **Cuerpo HTTP vacío:** NO crear ni enviar DTO de request (`{ "email": "..." }`). El backend extrae el destinatario del usuario autenticado y rechaza peticiones con body.
    - DTO de respuesta `202 Accepted`:
      ```json
      { "status": "accepted", "requestId": "<uuid>" }
      ```
    - Mapeo estricto de códigos HTTP:
        - `202 Accepted`: solicitud aceptada por AWS SES (token de verificación con 30 min de vigencia) → `AuthResult.Success(EmailVerificationOutcome)`.
        - `409 Conflict`: el correo del usuario ya se encuentra verificado → `AuthErrorKind.CONFLICT` (no reintentable; sincronizar estado de verificación en UI).
        - `429 Too Many Requests`: límite excedido (3 solicitudes por usuario cada 15 minutos) → `AuthErrorKind.RATE_LIMITED` (reintentable tras espera).
        - `503 Service Unavailable` (`DEPENDENCY_UNAVAILABLE`): rechazo del proveedor de correo (ej. destinatario no verificado en entorno sandbox de AWS SES) → `AuthErrorKind.SERVICE_UNAVAILABLE` (reintentable). **Prohibido enmascarar el `503` o reportarlo como envío exitoso.**
        - `401 Unauthorized`: token inválido/expirado → flujo estándar de refresh/logout.

2. **Ampliación de interfaces:**
    - En `RetrofitAuthService`:
        - `@POST("api/v1/auth/email-verification/request")` sin parámetro `@Body`.
    - En `AuthApi`:
        - `suspend fun requestEmailVerification(accessToken: String): EmailVerificationResponse`
    - En `AuthRepository`:
        - `suspend fun requestEmailVerification(): AuthResult<EmailVerificationOutcome>`
        - Lee el `accessToken` activo desde `TokenStorage`; nunca acepta ni envía un parámetro `email`.

3. **Manejo del temporizador de Cooldown y UX:**
    - Punto de entrada: Ajustes de cuenta → acción `"Verificar mi correo"` o banner/aviso visible cuando `user.emailVerified == false`.
    - El ViewModel gestiona un contador de segundos (`cooldownSeconds: Int = 0`).
    - Únicamente al recibir `202 Accepted`, activa `isAccepted = true` e inicia una corrutina de cooldown de 60 segundos que decrementa el contador cada segundo para evitar consumir innecesariamente la cuota de 3 solicitudes cada 15 minutos.
    - Si el backend responde `503 DEPENDENCY_UNAVAILABLE`, **NO** se activa `isAccepted` ni el cooldown; se muestra el mensaje de error real del servicio de correo.
    - Si el backend responde `409 Conflict`, se notifica que el correo ya estaba verificado y se actualiza el estado visual sin permitir reenvíos.

4. **Simulación en `MockAuthApi`:**
    - Retardo simulado de 600–1000 ms.
    - Por defecto devuelve `EmailVerificationResponse(status = "accepted", requestId = UUID.randomUUID().toString())`.
    - Simula cuota de 3 solicitudes por usuario (al 4.º intento consecutivo lanza `AuthApiException(AuthErrorKind.RATE_LIMITED, "Límite de 3 solicitudes cada 15 minutos alcanzado", retryable = true)`).
    - Centinelas basados en `accessToken`:
        - `accessToken == "already_verified_token"` → lanza `AuthApiException(AuthErrorKind.CONFLICT, "Tu correo electrónico ya está verificado", retryable = false)`.
        - `accessToken == "ses_sandbox_503_token"` → lanza `AuthApiException(AuthErrorKind.SERVICE_UNAVAILABLE, "Servicio de correo temporalmente no disponible (DEPENDENCY_UNAVAILABLE)", retryable = true)`.
        - `accessToken == "net_error_token"` → lanza `AuthApiException(AuthErrorKind.NETWORK, "Error de red al solicitar verificación", retryable = true)`.

## Contratos y modelos de datos (NEXUM-E010)

```kotlin
// Interfaz Retrofit (sin @Body)
@POST("api/v1/auth/email-verification/request")
suspend fun requestEmailVerification(
    @Header("Authorization") bearerToken: String
): EmailVerificationResponse

// DTO de respuesta (contrato NEXUM-E010 - 202 Accepted)
@Serializable // o @SerializedName según serializador del proyecto
data class EmailVerificationResponse(
    val status: String,
    val requestId: String
)

// Modelo de dominio
data class EmailVerificationOutcome(
    val status: String,
    val requestId: String
)

// Interfaz ampliada en AuthApi
interface AuthApi {
    // Métodos previos...
    suspend fun requestEmailVerification(accessToken: String): EmailVerificationResponse
}

// Interfaz ampliada en AuthRepository
interface AuthRepository {
    // Métodos previos...
    suspend fun requestEmailVerification(): AuthResult<EmailVerificationOutcome>
}