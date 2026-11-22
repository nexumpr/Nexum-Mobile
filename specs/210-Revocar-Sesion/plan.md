---
project: Nexum-Mobile
unit: 210-Revocar-Sesion
kind: technical-plan
version: 1.1.0
status: ready
implementation: only_on_explicit_request
---

# PLAN — Revocación de Sesión Remota (NEXUM-E009)

## Estrategia técnica
Implementar la revocación individual de sesiones activas bajo el contrato **NEXUM-E009** (`DELETE /api/v1/auth/sessions/{sessionId}`) dentro de la arquitectura MVI y el patrón Repositorio del módulo `auth`:

1. **Contrato HTTP y manejo de `204 No Content` (Retrofit / OkHttp):**
    - Endpoint: `DELETE /api/v1/auth/sessions/{sessionId}` con cabecera `Authorization: Bearer <accessToken>` y **sin body** (no enviar `userId` ni `sessionId` en el cuerpo).
    - Validación previa de `sessionId` en formato UUID válido antes de ejecutar la petición.
    - En Retrofit, declarar el retorno como `retrofit2.Response<Unit>` (o `Response<ResponseBody>` sin deserializador JSON) para procesar `204 No Content` sin intentar parsear un JSON vacío (`EOFException`).
    - Mapeo estricto de códigos HTTP de error:
        - `204 No Content`: éxito (incluye revocaciones idempotentes ya aplicadas).
        - `404 Not Found`: sesión ajena, inexistente o expirada (sin confirmar su existencia) → `AuthErrorKind.NOT_FOUND` (no reintentable).
        - `409 Conflict`: conflicto concurrente en el servidor → `AuthErrorKind.CONFLICT` (reintentable).
        - `401 Unauthorized`: token inválido/expirado → flujo estándar de refresh/logout.

2. **Ampliación de interfaces y coordinación en el Repositorio:**
    - En `AuthApi`: `suspend fun revokeSession(accessToken: String, sessionId: String)`
    - En `AuthRepository`: `suspend fun revokeSession(sessionId: String, isCurrent: Boolean): AuthResult<Unit>`
    - En `AuthRepositoryImpl`:
        - Valida que `sessionId` cumpla con formato UUID (excepto centinelas de prueba en entorno mock).
        - Ejecuta `api.revokeSession(accessToken, sessionId)`.
        - Si la respuesta es exitosa (`204`) y `isCurrent == true`, invoca obligatoriamente `tokenStorage.clear()` (el `accessToken` deja de estar autorizado).
        - Retorna `AuthResult.Success(Unit)`.

3. **Manejo reactivo en `SessionsViewModel` (Sincronización con E008):**
    - Se amplía `SessionsUiState` con:
        - `sessionToRevoke: ActiveSession? = null` (controla el diálogo de confirmación).
        - `revokingSessionId: String? = null` (deshabilita el botón y muestra progreso en la tarjeta específica).
    - Intenciones adicionales en `SessionsIntent`:
        - `RequestRevoke(val session: ActiveSession)`
        - `ConfirmRevoke`
        - `DismissRevokeDialog`
    - Flujo tras `ConfirmRevoke`:
        - Invoca `authRepository.revokeSession(session.id, session.current)`.
        - **Si `session.current == true` y éxito (`204`):** limpia el estado local y emite `AuthEffect.NavigateToLogin` de inmediato (sin llamar a E008, ya que el token fue revocado).
        - **Si `session.current == false` y éxito (`204`):** emite `ShowSnackbar("Sesión cerrada exitosamente")` y **dispara la recarga obligatoria llamando a E008 (`getSessions`)** para reflejar el estado real del backend.
        - **Si responde `404`:** remueve la sesión obsoleta de la vista o recarga E008 e informa que la sesión ya no estaba activa.
        - **Si responde `409`:** muestra mensaje de conflicto concurrente permitiendo reintentar la acción.

4. **Simulación en `MockAuthApi`:**
    - Retardo simulado de 400–800 ms.
    - Si `sessionId` ya fue eliminado previamente (idempotencia) → completa exitosamente (`Unit` equivalente a `204`).
    - Elimina la sesión con `id == sessionId` de su lista interna en memoria (para que la siguiente llamada a `getSessions()` de E008 ya no la devuelva).
    - Si `sessionId == "not_found_session"` → lanza `AuthApiException(AuthErrorKind.NOT_FOUND, "La sesión no existe o ya expiró", retryable = false)`.
    - Si `sessionId == "conflict_session"` → lanza `AuthApiException(AuthErrorKind.CONFLICT, "Conflicto concurrente al cerrar la sesión, intenta de nuevo", retryable = true)`.
    - Si `sessionId == "net_error_session"` → lanza `AuthApiException(AuthErrorKind.NETWORK, "Error de red al revocar sesión", retryable = true)`.

## Contratos e Interfaces (NEXUM-E009)

```kotlin
// Interfaz Retrofit (RetrofitAuthService)
@DELETE("api/v1/auth/sessions/{sessionId}")
suspend fun revokeSession(
    @Header("Authorization") bearerToken: String,
    @Path("sessionId") sessionId: String
): Response<Unit>

// Interfaz de dominio/datos en AuthApi
interface AuthApi {
    // Métodos previos: register, login, refresh, logout, forgotPassword, resetPassword, changePassword, getSessions
    suspend fun revokeSession(accessToken: String, sessionId: String)
}

// Interfaz ampliada en AuthRepository
interface AuthRepository {
    // Métodos previos...
    suspend fun revokeSession(sessionId: String, isCurrent: Boolean): AuthResult<Unit>
}