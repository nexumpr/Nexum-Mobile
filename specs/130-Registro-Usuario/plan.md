---
project: Nexum-Mobile
unit: 130-Registro-Usuario
kind: functional-plan
version: 2.0.0
status: ready
implementation: only_on_explicit_request
---

# PLAN — Registro de Usuario (Sign Up)

## Estrategia técnica
MVI con flujo unidireccional (intención → reducer → estado inmutable → efecto), Repositorio con fuente simulada y retardo de red, validación local antes de cada request. Los contratos son `data class` Kotlin sin serializador hasta existir cliente HTTP real (unidad `040`). Dependencias nuevas aprobadas (M-D09): `androidx.lifecycle:lifecycle-viewmodel-compose` en producción y `kotlinx-coroutines-test` solo en pruebas; sin Retrofit, Hilt/Koin ni kotlinx-serialization.

## Contratos y modelos de datos (NEXUM-E001 / ejemplo 4.1)
```kotlin
// frequency: único valor confirmado por el PDF (M-D08); ampliar solo con Backend
enum class Frequency { monthly }

// type: único valor confirmado en el ejemplo 4.1; "variable" pendiente de catálogo (M-D08)
enum class ExpenseType { fixed }

data class Income(val amount: String, val frequency: Frequency)          // decimal string: "12000.00"

data class RecurringExpense(
    val concept: String,          // "Renta"
    val amount: String,           // "4000.00"
    val frequency: Frequency,
    val categoryId: String,       // "vivienda"
    val type: ExpenseType
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val income: Income,
    val recurringExpenses: List<RecurringExpense>,   // al menos 1 elemento (M130-R03)
    val privacyNoticeVersion: String,                // "2026-11-v1"
    val consentAcceptedAt: String                    // ISO-8601 UTC
)

// 201: userId + onboardingStatus + (tokens | indicación de verificación)
data class RegisterResponse(
    val userId: String,
    val onboardingStatus: String,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val expiresIn: Long? = null,
    val verificationRequired: Boolean = false
)
```

## Contrato compartido del Repositorio (referenciado por 140-Login)
```kotlin
interface AuthRepository {
    suspend fun register(request: RegisterRequest): AuthResult<RegisterOutcome>
    suspend fun login(request: LoginRequest): AuthResult<LoginOutcome>   // DTOs definidos en plan de 140
}

data class RegisterOutcome(
    val userId: String,
    val onboardingStatus: String,
    val verificationRequired: Boolean
)

sealed interface AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>
    data class Failure(val error: AuthError) : AuthResult<Nothing>
}

enum class AuthErrorKind { CONFLICT, INVALID_CREDENTIALS, RATE_LIMITED, NETWORK, UNKNOWN }

data class AuthError(val kind: AuthErrorKind, val message: String, val retryable: Boolean)
```

**Fuente simulada (registro):** retardo aleatorio de 800–1500 ms; correo ya registrado → `CONFLICT` (409); correo `net@error.test` → `NETWORK` para demostrar el Snackbar de reintento; éxito → `RegisterResponse` sintético.

## Estados MVI de la pantalla Registro
```kotlin
data class ExpenseDraft(   // renglón editable en pantalla; se valida antes del request
    val concept: String = "",
    val amount: String = "",
    val categoryId: String = "",
    val type: ExpenseType = ExpenseType.fixed
)

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val incomeAmount: String = "",
    val incomeFrequency: Frequency = Frequency.monthly,
    val expenses: List<ExpenseDraft> = listOf(ExpenseDraft()),   // mínimo 1
    val termsAccepted: Boolean = false,
    val isLoading: Boolean = false,
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val incomeError: String? = null,
    val expensesError: String? = null,
    val termsError: String? = null,
    val formError: String? = null      // conflicto y error de red (Snackbar)
)

sealed interface RegisterIntent {
    data class NameChanged(val v: String) : RegisterIntent
    data class EmailChanged(val v: String) : RegisterIntent
    data class PasswordChanged(val v: String) : RegisterIntent
    data class ConfirmPasswordChanged(val v: String) : RegisterIntent
    data object TogglePasswordVisibility : RegisterIntent
    data class IncomeAmountChanged(val v: String) : RegisterIntent
    data class ExpenseChanged(val index: Int, val draft: ExpenseDraft) : RegisterIntent
    data object AddExpense : RegisterIntent
    data class RemoveExpense(val index: Int) : RegisterIntent
    data class TermsAccepted(val v: Boolean) : RegisterIntent
    data object Submit : RegisterIntent
}

// Compartido con 140-Login (definido aquí, referenciado allí)
sealed interface AuthEffect {
    data object NavigateToLogin : AuthEffect
    data object NavigateToRegister : AuthEffect
    data object NavigateToApp : AuthEffect            // shell temporal; backstack real en unidad 030
    data class ShowSnackbar(val message: String, val retry: Boolean = false) : AuthEffect
}
```

**Reducer:** la validación local corre en cada intención y en `Submit`; `Submit` válido pone `isLoading` y llama al repositorio; `Success` emite `NavigateToLogin`; `Failure(CONFLICT|NETWORK)` actualiza `formError` y emite `ShowSnackbar` sin filtrar detalles técnicos.

## Orden de trabajo
| Paso | Acción | Requisito rector |
| --- | --- | --- |
| **N130-P01** | Modelos DTO de E001 y fixture del ejemplo 4.1. | M130-R02 |
| **N130-P02** | `AuthValidator`: email, contraseña, coincidencia, campos financieros y consentimiento. | M130-R01, M130-R03, M130-R04 |
| **N130-P03** | Interfaz `AuthRepository` y fuente simulada con retardo y errores deterministas. | M130-R05, M130-R02 |
| **N130-P04** | `AuthViewModel` MVI: intenciones, reducer, estados inmutables y efectos. | M130-R01…M130-R05 |
| **N130-P05** | `RegisterScreen` en Compose: campos financieros, toggle de visibilidad y checkbox. | M130-R01, M130-R03 |
| **N130-P06** | Pruebas: `AuthValidatorTest` y `AuthViewModelTest` con repositorio falso. | M130-R01…M130-R05 |

## Dependencias y decisiones
- Requiere: `030-di-navegacion-ui` y `040-cliente-http-contratos` en trazabilidad; su alcance mock no las consume.
- Aprobado (M-D09): `lifecycle-viewmodel-compose`; `kotlinx-coroutines-test` solo `testImplementation`.
- M-D08: `frequency` solo `monthly`, `type` solo `fixed`, ingreso obligatorio y al menos un gasto recurrente.
- Navegación temporal mediante shell de estados hasta la unidad `030`; sin singletons con estado sensible.
- Revisión humana necesaria si aparece dependencia, costo, credencial o ajuste de arquitectura no ratificado.

## Estrategia de pruebas
- **Automáticas:** `./gradlew assembleDebug`; `./gradlew testDebugUnitTest`; `./gradlew lintDebug`.
- **Unidad:** validador (regex, política, coincidencia, campos financieros, consentimiento) y transiciones del ViewModel (carga, éxito, conflicto, fallo de red) con repositorio falso y coroutines de prueba.
- **Negativas:** entradas inválidas, correo duplicado, red caída y envío sin consentimiento.
- **Regresión:** mantener las verificaciones de unidades previas realmente existentes.
- **Evidencia:** consola con salida real, tests nuevos, archivos modificados y fallos explícitos.

## Riesgos
- Fuente simulada no sustituye un contrato verificado con Backend; sustituir solo en la unidad `040`/`120`.
- Campos de `user` y semántica de verificación en el `201` sin confirmar.
- Compilación previa exitosa no sustituye una nueva verificación posterior a cambios.

## Operación y reversibilidad
Describir archivos antes de editar, mantener cambios dentro de la unidad, revertir los propios cambios si el resultado no satisface criterios. `push`, merge y despliegue requieren autorización independiente.
