---
project: Nexum-Mobile
unit: 250-Editar-Perfil
kind: technical-plan
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# PLAN — Edición de Perfil de Usuario (Update Profile)

## Estrategia técnica
Implementar la edición de perfil bajo el contrato **NEXUM-E013** en la arquitectura MVI y el patrón Repositorio:
1. **Contratos DTO y mapeos de datos:**
   - DTO de entrada: `UpdateProfileRequest(val name: String)`.
   - DTO de respuesta: `UserProfileResponse` (reutilizado de `240-Perfil-Usuario`).
2. **Ampliación de interfaces:**
   - En `AuthApi`: `suspend fun updateProfile(accessToken: String, request: UpdateProfileRequest): UserProfileResponse`.
   - En `AuthRepository`: `suspend fun updateProfile(name: String): AuthResult<UserProfile>`.
3. **Sincronización en `AuthRepositoryImpl`:**
   - Al recibir el perfil actualizado:
     - Si hay sesión en `TokenStorage`, actualiza `session.user` con el nuevo `name` usando `tokenStorage.save(...)`.
     - Retorna `AuthResult.Success(updatedUserProfile)`.
4. **Estados e Intenciones MVI:**
   - `EditProfileUiState`:
     - `name: String = ""`
     - `initialName: String = ""`
     - `nameError: String? = null`
     - `isLoading: Boolean = false`
     - `isSaved: Boolean = false`
     - `errorMessage: String? = null`
   - Intenciones `EditProfileIntent`:
     - `NameChanged(val value: String)`
     - `Submit`
     - `Reset`
5. **Simulación en `MockAuthApi`:**
   - Retardo determinista de 400–800 ms.
   - Si `request.name.isBlank()` → lanza `AuthApiException(AuthErrorKind.UNKNOWN, "El nombre no puede estar vacío")`.
   - Si `request.name == "net_error_name"` → lanza `AuthApiException(AuthErrorKind.NETWORK, "Error de red al actualizar perfil", retryable = true)`.
   - Éxito → actualiza el nombre del usuario en memoria y devuelve `UserProfileResponse` con el nuevo nombre.

## Contratos y modelos de datos (NEXUM-E013)

```kotlin
// DTOs de red (contrato NEXUM-E013)
data class UpdateProfileRequest(
    val name: String
)

// Interfaz ampliada en AuthApi
interface AuthApi {
    // Métodos previos...
    suspend fun updateProfile(accessToken: String, request: UpdateProfileRequest): UserProfileResponse
}

// Interfaz ampliada en AuthRepository
interface AuthRepository {
    // Métodos previos...
    suspend fun updateProfile(name: String): AuthResult<UserProfile>
}
```

## Estados e Intenciones MVI

```kotlin
data class EditProfileUiState(
    val name: String = "",
    val initialName: String = "",
    val nameError: String? = null,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
) {
    val canSubmit: Boolean
        get() = name.isNotBlank() && 
                name.trim() != initialName.trim() && 
                name.trim().length in 2..50 && 
                nameError == null && 
                !isLoading
}

sealed interface EditProfileIntent {
    data class NameChanged(val value: String) : EditProfileIntent
    data object Submit : EditProfileIntent
    data object Reset : EditProfileIntent
}
```

## Orden de trabajo
| Paso | Acción | Requisito rector |
| --- | --- | --- |
| **N250-P01** | Definir modelo DTO de NEXUM-E013 (`UpdateProfileRequest`). | M250-R03 |
| **N250-P02** | Ampliar contratos `AuthApi` y `AuthRepository` con la mutación de perfil. | M250-R03, M250-R04 |
| **N250-P03** | Implementar la simulación en `MockAuthApi` con actualización en memoria y centinelas. | M250-R05 |
| **N250-P04** | Construir `EditProfileViewModel` MVI con validaciones locales y sincronización de sesión. | M250-R01, M250-R02, M250-R04 |
| **N250-P05** | Diseñar el formulario o modal Composable `EditProfileDialog` en Jetpack Compose con Material 3. | M250-R01, M250-R02 |
| **N250-P06** | Desarrollar pruebas unitarias completas (`EditProfileViewModelTest`, `MockAuthApiTest`) con `kotlinx-coroutines-test`. | M250-R01…M250-R05 |

## Dependencias y decisiones técnicas
- **Aprobado**: Material 3 y Jetpack Compose sin dependencias externas.
- **Sincronización de sesión**: La sesión en `TokenStorage` se actualiza atómicamente para que vistas como el encabezado de Inicio o Perfil no muestren datos desactualizados.
- **Detección de cambios**: El cálculo booleano `canSubmit` evita llamadas innecesarias al backend si no hay modificaciones reales.

## Estrategia de pruebas
- **Pruebas de ViewModel:**
  - Validación local de nombre (muy corto, vacío, válido).
  - `canSubmit` es falso si el nombre no cambia.
  - Guardado exitoso transiciona a `isSaved = true` y actualiza `TokenStorage`.
  - Error de red conserva el texto ingresado y emite mensaje de error reintentable.
- **Pruebas de Repositorio y Mock:**
  - `updateProfile()` actualiza el nombre del usuario y retorna el perfil modificado.
  - Centinela de red devuelve `Failure(NETWORK)`.
- **Pruebas de regresión:**
  - Asegurar que `assembleDebug` y los 50 tests de autenticación previos sigan aprobándose.
