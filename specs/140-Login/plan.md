---
project: Nexum-Mobile
unit: 140-Login
kind: functional-plan
version: 2.0.0
status: ready
implementation: only_on_explicit_request
---

# PLAN — Inicio de Sesión (Login)

## Estrategia técnica
MVI con flujo unidireccional (intención → reducer → estado inmutable → efecto), Repositorio con fuente simulada y retardo de red, validación local antes del request. Los contratos son `data class` Kotlin sin serializador hasta existir cliente HTTP real (unidad `040`). Dependencias nuevas aprobadas (M-D09): `androidx.lifecycle:lifecycle-viewmodel-compose` en producción y `kotlinx-coroutines-test` solo en pruebas; sin Retrofit, Hilt/Koin ni kotlinx-serialization.

**Trazabilidad:** el contrato compartido `AuthRepository`, `AuthResult` y `AuthEffect` está definido en `specs/130-Registro-Usuario/plan.md`. Si esta unidad se autoriza sin `130`, copiar ese bloque aquí antes de implementar y registrarlo en `memoria.md`.

## Contratos y modelos de datos (NEXUM-E002)
```kotlin
data class LoginRequest(
    val email: String,
    val password: String,
    val deviceName: String? = null    // opcional; la fuente simulada usa Build.MODEL
)

data class User(val id: String, val name: String, val email: String)   // campos de `user` supuestos: confirmar con Backend

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val user: User
)

data class LoginOutcome(val session: Session)

data class Session(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val user: User
)

interface TokenStorage {
    fun save(session: Session)
    fun current(): Session?
    fun clear()
}
// InMemoryTokenStorage (mock, esta unidad) → EncryptedSharedPreferences en 050-sesion-credenciales
```

**Fuente simulada (login):** retardo aleatorio de 800–1500 ms; 5 fallos consecutivos → `RATE_LIMITED`; correo `net@error.test` → `NETWORK`; credenciales inválidas → `INVALID_CREDENTIALS` (401 genérico); éxito → `LoginResponse` sintético y `TokenStorage.save`.

## Estados MVI de la pantalla Login
```kotlin
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val formError: String? = null,          // 401 genérico, límite de intentos, error de red
    val attemptsRemaining: Int? = null      // aviso del límite de 5 intentos
)

sealed interface LoginIntent {
    data class EmailChanged(val v: String) : LoginIntent
    data class PasswordChanged(val v: String) : LoginIntent
    data object TogglePasswordVisibility : LoginIntent
    data object Submit : LoginIntent
    data object NavigateToRegister : LoginIntent
    data object NavigateToForgotPassword : LoginIntent   // sin flujo aún: efecto informativo
}

// Efectos compartidos: AuthEffect definido en specs/130-Registro-Usuario/plan.md
// (NavigateToApp, NavigateToLogin, NavigateToRegister, ShowSnackbar)
```

**Reducer:** validación local en cada intención y en `Submit`; `Submit` válido pone `isLoading` y llama al repositorio; `Success` guarda la sesión y emite `NavigateToApp` (limpia el historial del shell temporal); `Failure(INVALID_CREDENTIALS|RATE_LIMITED|NETWORK)` actualiza `formError`/`attemptsRemaining` y emite `ShowSnackbar` con mensaje genérico.

## Orden de trabajo
| Paso | Acción | Requisito rector |
| --- | --- | --- |
| **N140-P01** | Modelos DTO de E002, `Session` e interfaz `TokenStorage`. | M140-R02, M140-R04 |
| **N140-P02** | Validador local de login: campos no vacíos y formato de email. | M140-R01 |
| **N140-P03** | Fuente simulada de login: retardo, 5 intentos, 401 genérico y error de red. | M140-R03 |
| **N140-P04** | `LoginViewModel` MVI: intenciones, reducer, estados inmutables y efectos. | M140-R01…M140-R05 |
| **N140-P05** | `LoginScreen` en Compose: toggle, ProgressBar, enlaces y Snackbar. | M140-R01, M140-R03 |
| **N140-P06** | Pruebas: validador y ViewModel (éxito, 401, límite, red) con repositorio falso. | M140-R01…M140-R05 |

## Dependencias y decisiones
- Requiere: `030-di-navegacion-ui` y `040-cliente-http-contratos` en trazabilidad; el alcance mock no las consume.
- Aprobado (M-D09): `lifecycle-viewmodel-compose`; `kotlinx-coroutines-test` solo `testImplementation`.
- M-P04 (JWT/renovación/logout) sigue pendiente; aquí solo se custodia el token de forma explícita.
- Navegación temporal mediante shell de estados hasta la unidad `030`.
- Revisión humana necesaria si aparece dependencia, costo, credencial o ajuste de arquitectura no ratificado.

## Estrategia de pruebas
- **Automáticas:** `./gradlew assembleDebug`; `./gradlew testDebugUnitTest`; `./gradlew lintDebug`.
- **Unidad:** validador de entrada y transiciones del ViewModel (carga, éxito, 401, límite de intentos, red) con repositorio falso, `kotlinx-coroutines-test` y doble de `TokenStorage`.
- **Negativas:** campos vacíos, formato de email inválido, credenciales incorrectas, límite alcanzado y red caída.
- **Regresión:** mantener las verificaciones de unidades previas realmente existentes.
- **Evidencia:** consola con salida real, tests nuevos, archivos modificados y fallos explícitos.

## Riesgos
- Fuente simulada no sustituye un contrato verificado con Backend; sustituir solo en la unidad `040`/`120`.
- `EncryptedSharedPreferences` y renovación/revocación de tokens dependen de `050`.
- Campos de `user` sin confirmar; el enlace de recuperación de contraseña no tiene endpoint definido.
- Compilación previa exitosa no sustituye una nueva verificación posterior a cambios.

## Operación y reversibilidad
Describir archivos antes de editar, mantener cambios dentro de la unidad, revertir los propios cambios si el resultado no satisface criterios. `push`, merge y despliegue requieren autorización independiente.
