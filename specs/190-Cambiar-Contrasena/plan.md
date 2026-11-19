---
project: Nexum-Mobile
unit: 190-Cambiar-Contrasena
kind: technical-plan
version: 1.1.0
status: ready
implementation: only_on_explicit_request
---

# PLAN — Cambio de Contraseña Autenticado (Change Password)

## Reglas Operativas y Contexto
- **Lectura obligatoria previa:** Consultar `memoria.md` antes de ejecutar cualquier paso del plan para alinear convenciones del repositorio, manejo de tokens y rutas de navegación.
- **Restricción de repositorio:** No ejecutar ninguna acción en GitHub (sin commits, branches, pushes ni PRs).

## Estrategia técnica
Implementar el cambio de contraseña autenticado bajo el contrato **NEXUM-E007** (`POST /api/v1/auth/password/change`) en la arquitectura MVI y el patrón Repositorio del módulo `auth`:

1. **Contratos DTO y mapeos de datos:**
    - DTO de entrada: `ChangePasswordRequest(val currentPassword: String, val newPassword: String)`.
    - DTO de salida: `ChangePasswordResponse(val status: String, val requestId: String)` sobre HTTP `200 OK`.
    - Mapeo de dominio: `AuthResult.Success(ChangePasswordResponse)` (o `AuthResult.Success(requestId: String)`).

2. **Ampliación de interfaces:**
    - En `AuthApi`: `suspend fun changePassword(accessToken: String, request: ChangePasswordRequest): ChangePasswordResponse`
    - En `AuthRepository`: `suspend fun changePassword(currentPassword: String, newPassword: String): AuthResult<ChangePasswordResponse>`

3. **Validación local previa al request:**
    - `currentPassword.isNotBlank()`.
    - Longitud mínima estricta de **12 caracteres** (`newPassword.length >= 12`) y rechazo de contraseñas débiles/comunes mediante `AuthValidator.isStrongPassword(newPassword)`.
    - `newPassword != currentPassword`.
    - `newPassword == confirmNewPassword`.

4. **Estados MVI de la pantalla:**
    - `ChangePasswordUiState`:
        - `currentPassword: String = ""`
        - `newPassword: String = ""`
        - `confirmNewPassword: String = ""`
        - `isCurrentPasswordVisible: Boolean = false`
        - `isNewPasswordVisible: Boolean = false`
        - `isConfirmNewPasswordVisible: Boolean = false`
        - `isLoading: Boolean = false`
        - `currentPasswordError: String? = null`
        - `newPasswordError: String? = null`
        - `confirmNewPasswordError: String? = null`
    - Intenciones `ChangePasswordIntent`:
        - `CurrentPasswordChanged(val value: String)`
        - `NewPasswordChanged(val value: String)`
        - `ConfirmNewPasswordChanged(val value: String)`
        - `ToggleCurrentPasswordVisibility`
        - `ToggleNewPasswordVisibility`
        - `ToggleConfirmNewPasswordVisibility`
        - `Submit`

5. **Efectos, revocación de sesión y seguridad de memoria (E007):**
    - **Limpieza de memoria:** Al recibir `200 OK` (`status == "accepted"`), el ViewModel purga inmediatamente los tres campos de contraseña (`currentPassword = ""`, `newPassword = ""`, `confirmNewPassword = ""`) para evitar retención prolongada en memoria (MO-PR06, MO-PR10).
    - **Invalidación total de sesión:** Dado que E007 incrementa la época de sesión en backend y revoca todos los JWT existentes (devolviendo `401` al token previo), `AuthRepository` / `SessionManager` debe:
        1. Eliminar `accessToken` y `refreshToken` del almacenamiento seguro local.
        2. Limpiar el estado de sesión autenticado en memoria.
    - **Navegación forzada:** Emitir `AuthEffect.SessionRevokedAfterPasswordChange` (o `AuthEffect.NavigateToLoginWithMessage(...)`) para expulsar al usuario de pantallas protegidas, volver al stack de **Login** limpiando el backstack y mostrar confirmación de cambio exitoso.

6. **Simulación en `MockAuthApi`:**
    - Verifica que el `accessToken` esté activo y que `currentPassword` coincida con la contraseña simulada del usuario activo (por defecto `"Password123#2026"` o el fixture definido en `memoria.md`).
    - Si `currentPassword` no coincide → lanza `AuthApiException(AuthErrorKind.INVALID_CREDENTIALS, "La contraseña actual no es correcta")`.
    - Si `newPassword.length < 12` o es débil → lanza `AuthApiException(AuthErrorKind.VALIDATION_ERROR, "La nueva contraseña debe tener al menos 12 caracteres")`.
    - Si `newPassword == currentPassword` → lanza `AuthApiException(AuthErrorKind.CONFLICT, "La nueva contraseña no puede ser igual a la actual")`.
    - Si es válida → retardo de 600–1000 ms, invalida el `accessToken` simulado actual (para que llamadas posteriores devuelvan `401`) y retorna `ChangePasswordResponse(status = "accepted", requestId = UUID.randomUUID().toString())`.

## Contratos y modelos de datos (NEXUM-E007)

```kotlin
// DTOs de red (contrato NEXUM-E007)
@Serializable
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

@Serializable
data class ChangePasswordResponse(
    val status: String,
    val requestId: String
)

// Interfaz ampliada en AuthApi
interface AuthApi {
    // POST /api/v1/auth/password/change · Bearer · 200 OK
    suspend fun changePassword(
        accessToken: String,
        request: ChangePasswordRequest
    ): ChangePasswordResponse
}

// Interfaz ampliada en AuthRepository
interface AuthRepository {
    suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): AuthResult<ChangePasswordResponse>
}