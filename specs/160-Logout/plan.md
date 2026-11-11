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
1. **Contratos DTO:** Definición de `LogoutRequest(val refreshToken: String)`.
2. **Ampliación de interfaces:**
   - En `AuthApi`: `suspend fun logout(accessToken: String, request: LogoutRequest)`
   - En `AuthRepository`: `suspend fun logout(): AuthResult<Unit>`
3. **Mecanismo Fail-Secure (Purga Local Incondicional):**
   - El repositorio obtiene el token actual de `TokenStorage`.
   - Si no hay sesión, se retorna éxito inmediato limpiando cualquier remanente.
   - Si hay sesión, se intenta notificar al backend (`api.logout`).
   - Independientemente de si la llamada HTTP tiene éxito (`204`) o falla (`NetworkException`, `401`, `500`), el bloque `finally` o de manejo de error ejecuta obligatoriamente `tokenStorage.clear()`.
   - Esto garantiza que en ningún escenario un usuario quede atrapado con una sesión abierta en el dispositivo al haber solicitado salir.
4. **Estados MVI y UI:**
   - Modelado de `LogoutUiState(val isConfirmDialogOpen: Boolean, val isLoading: Boolean, val userMessage: String?)`.
   - Intenciones: `LogoutIntent.RequestLogout`, `LogoutIntent.ConfirmLogout`, `LogoutIntent.DismissDialog`.
   - Diálogo Material 3 de confirmación (`AlertDialog`) con botón de confirmación destructivo y botón de cancelación.
   - Emisión del efecto `AuthEffect.NavigateToLogin` tras la confirmación y purga.
5. **Simulación en `MockAuthApi`:**
   - Retardo simulado de 300–800 ms.
   - Marca el token de sesión como revocado en memoria.
   - Maneja caso de fallo de red con token centinela `net_error_logout`.

## Contratos y modelos de datos (NEXUM-E004)

```kotlin
// DTO de red (contrato NEXUM-E004)
data class LogoutRequest(
    val refreshToken: String
)

// Interfaz ampliada en AuthApi
interface AuthApi {
    // Métodos previos: register, login, refresh
    suspend fun logout(accessToken: String, request: LogoutRequest)
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
            return AuthResult.Success(Unit)
        }

        return try {
            api.logout(
                accessToken = currentSession.accessToken,
                request = LogoutRequest(refreshToken = currentSession.refreshToken)
            )
            AuthResult.Success(Unit)
        } catch (error: Throwable) {
            // Incluso ante error de red o de servidor, la salida local es obligatoria
            AuthResult.Success(Unit)
        } finally {
            // Purga garantizada de credenciales locales
            tokenStorage.clear()
        }
    }
}
```

## Estados MVI de Logout en UI

```kotlin
data class LogoutUiState(
    val isConfirmDialogOpen: Boolean = false,
    val isLoading: Boolean = false
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
| **N160-P01** | Definir modelo DTO de NEXUM-E004 (`LogoutRequest`). | M160-R02 |
| **N160-P02** | Ampliar contratos `AuthApi` y `AuthRepository` con la operación de logout. | M160-R02, M160-R03 |
| **N160-P03** | Implementar la simulación en `MockAuthApi` con revocación en servidor y retardo. | M160-R05 |
| **N160-P04** | Implementar `AuthRepositoryImpl.logout()` con garantía de purga incondicional de `TokenStorage`. | M160-R03 |
| **N160-P05** | Diseñar el diálogo de confirmación Composable en Compose y coordinar la emisión de `AuthEffect.NavigateToLogin` en `AuthFlow`. | M160-R01, M160-R04 |
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
