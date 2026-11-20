---
project: Nexum-Mobile
unit: 160-Logout
kind: functional-plan
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# PLAN — Cierre de Sesión (Logout)

## Estrategia técnica
Implementar el flujo de cierre de sesión bajo el contrato **NEXUM-E004** respetando la arquitectura MVI y el patrón Repositorio establecido en el paquete `auth`:
1. **Contrato de red (E004):** `POST /api/v1/auth/logout` con header `Authorization: Bearer <accessToken>` y **sin body**. Manejo de `204 No Content` con `Response<Unit>` en Retrofit sin deserializar JSON.
2. **Ampliación de interfaces y aislamiento OkHttp:**
   - En `AuthApi`: `suspend fun logout(accessToken: String)`
   - En `AuthRepository`: `suspend fun logout(): AuthResult<Unit>`
   - Aislamiento en cliente OkHttp: ejecución en cliente sin `TokenAuthenticator` (o exclusión explícita de `auth/logout`) para no reintentar jamás ante un `401`.
3. **Mecanismo Fail-Secure (Purga Local Incondicional):**
   - El repositorio obtiene la sesión de `TokenStorage`.
   - Si no hay sesión, purga local y retorna éxito inmediato.
   - Si hay sesión, invoca `api.logout(currentSession.accessToken)`.
   - En el bloque `finally` obligatorio, ejecuta `tokenStorage.clear()` y emite `AuthEffect.NavigateToLogin`.
   - Retorna siempre `AuthResult.Success(Unit)`, garantizando que el usuario jamás quede atrapado con sesión local abierta ante caídas de red o `401`.
4. **Estados MVI y UI:**
   - Modelado de `LogoutUiState(val isConfirmDialogOpen: Boolean = false, val isLoading: Boolean = false, val userMessage: String? = null)`.
   - Intenciones: `LogoutIntent.RequestLogout`, `LogoutIntent.ConfirmLogout`, `LogoutIntent.DismissDialog`.
   - Diálogo Material 3 de confirmación (`AlertDialog`) con botón destructivo "Cerrar sesión" y botón "Cancelar", deshabilitando controles mientras `isLoading == true`.
   - Emisión del efecto `AuthEffect.NavigateToLogin` tras la confirmación y purga.
5. **Simulación en `MockAuthApi`:**
   - Retardo simulado de 300–800 ms.
   - Marca de forma durable tanto el `accessToken` como el `refreshToken` de la sesión como revocados.
   - Responde `204 No Content`. Si el token ya estaba revocado, responde `401` (que el cliente resuelve con éxito local).
   - Maneja caso de fallo de red con token centinela `net_error_logout`.

## Contratos y modelos de datos (NEXUM-E004)

```kotlin
// Interfaz en AuthApiService (Retrofit)
@Headers("Accept: application/json")
@POST("auth/logout")
suspend fun logout(@Header("Authorization") authorization: String): Response<Unit>

// Interfaz ampliada en AuthApi
interface AuthApi {
    // Métodos previos: register, login, refresh
    suspend fun logout(accessToken: String)
}

// Interfaz ampliada en AuthRepository
interface AuthRepository {
    // Métodos previos: register, login, refreshSession
    suspend fun logout(): AuthResult<Unit>
}
```

## Lógica del Repositorio y Purga Local Incondicional

```kotlin
class AuthRepositoryImpl(
    private val api: AuthApi,
    private val tokenStorage: TokenStorage
) : AuthRepository {

    override suspend fun logout(): AuthResult<Unit> {
        val currentSession = tokenStorage.current()
        if (currentSession == null) {
            tokenStorage.clear()
            _sessionEffects.tryEmit(AuthEffect.NavigateToLogin)
            return AuthResult.Success(Unit)
        }

        return try {
            api.logout(currentSession.accessToken)
            AuthResult.Success(Unit)
        } catch (error: Throwable) {
            // Incluso ante error de red o de servidor, la salida local es obligatoria
            AuthResult.Success(Unit)
        } finally {
            // Purga garantizada de credenciales locales
            tokenStorage.clear()
            _sessionEffects.tryEmit(AuthEffect.NavigateToLogin)
        }
    }
}
```

## Estados MVI de Logout en UI

```kotlin
data class LogoutUiState(
    val isConfirmDialogOpen: Boolean = false,
    val isLoading: Boolean = false,
    val userMessage: String? = null
)

sealed interface LogoutIntent {
    data object RequestLogout : LogoutIntent
    data object ConfirmLogout : LogoutIntent
    data object DismissDialog : LogoutIntent
}
```

## Orden de trabajo
| Paso | Acción | Requisito rector |
| --- | --- | --- |
| **N160-P01** | Ajustar contratos en `AuthApi` y `AuthRepository` (`logout` sin body, compatible con 204 y sin Authenticator recursivo). | M160-R02, M160-R03 |
| **N160-P02** | Implementar `logout` en `RetrofitAuthRepositoryApi` y `AuthApiService` manejando cabecera Bearer y respuesta 204. | M160-R02 |
| **N160-P03** | Actualizar `MockAuthApi` con retardo, revocación durable de access/refresh tokens y centinela `net_error_logout`. | M160-R05 |
| **N160-P04** | Implementar `AuthRepositoryImpl.logout()` con `tokenStorage.clear()` garantizado en `finally` y retorno `Success(Unit)`. | M160-R03 |
| **N160-P05** | Implementar ViewModel/estado MVI (`LogoutViewModel`), diálogo Material 3 y navegación con `AuthEffect.NavigateToLogin` en `AuthFlow`. | M160-R01, M160-R04 |
| **N160-P06** | Desarrollar pruebas unitarias completas de logout (`MockAuthApiTest`, `AuthRepositoryLogoutTest`, `LogoutViewModelTest`) con `kotlinx-coroutines-test`. | M160-R01…M160-R05 |

## Dependencias y decisiones técnicas
- **Aprobado**: Arquitectura MVI sin añadir dependencias externas; uso de `kotlinx-coroutines-test` en pruebas unitarias.
- **Transversalidad**: La acción de logout vive lógicamente en el módulo de Perfil / Menú de sesión, pero se apoya en el repositorio de autenticación compartido (`AuthRepository`).
- **Resiliencia**: Ante cualquier contingencia técnica en la llamada remota, la seguridad del dispositivo prima sobre la confirmación del servidor (fail-secure).

## Estrategia de pruebas
- **Pruebas de Repositorio:**
  - Logout exitoso llama a `api.logout()` y limpia `TokenStorage`.
  - Logout con excepción de red limpia `TokenStorage` incondicionalmente.
  - Logout sin sesión previa limpia `TokenStorage` sin lanzar excepciones.
- **Pruebas de ViewModel / UI:**
  - `RequestLogout` activa `isConfirmDialogOpen = true`.
  - `DismissDialog` restablece `isConfirmDialogOpen = false`.
  - `ConfirmLogout` invoca al repositorio, pone `isLoading = true` y emite `AuthEffect.NavigateToLogin`.
- **Pruebas de regresión:**
  - Mantener los 50 tests de autenticación previos en verde sin regresiones en `assembleDebug`.
