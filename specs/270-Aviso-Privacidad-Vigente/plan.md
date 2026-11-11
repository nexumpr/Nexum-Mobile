---
project: Nexum-Mobile
unit: 270-Aviso-Privacidad-Vigente
kind: technical-plan
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# PLAN — Consulta del Aviso de Privacidad Vigente (Current Privacy Notice)

## Estrategia técnica
Implementar la consulta y visualización del aviso de privacidad bajo el contrato **NEXUM-E015** en la arquitectura MVI y el patrón Repositorio:
1. **Contratos DTO y modelos de dominio:**
   - DTO de respuesta: `PrivacyNoticeResponse(val version: String, val title: String, val body: String, val publishedAt: String)`.
   - Modelo de dominio: `PrivacyNotice(val version: String, val title: String, val body: String, val publishedAt: String, val isFromLocalFallback: Boolean = false)`.
2. **Ampliación de interfaces:**
   - En `AuthApi`: `suspend fun getCurrentPrivacyNotice(locale: String? = null): PrivacyNoticeResponse`.
   - En `AuthRepository` (o `PrivacyRepository`): `suspend fun getCurrentPrivacyNotice(locale: String = "es-MX"): AuthResult<PrivacyNotice>`.
3. **Manejo de resiliencia y fallback en Repositorio:**
   - Si la llamada a la API tiene éxito:
     - Mapea a `PrivacyNotice(isFromLocalFallback = false)`.
     - Guarda en memoria/caché local como aviso vigente.
     - Retorna `AuthResult.Success(notice)`.
   - Si la llamada falla por error de red o timeout:
     - Carga la copia de respaldo empaquetada (fallback estático con versión `"2026-11-v1"` y texto legal aprobado).
     - Retorna `AuthResult.Success(fallbackNotice.copy(isFromLocalFallback = true))` con advertencia contextual para permitir que el usuario continúe su registro sin bloqueos.
4. **Estados e Intenciones MVI:**
   - `PrivacyNoticeUiState`:
     - `isLoading: Boolean = false`
     - `notice: PrivacyNotice? = null`
     - `isFallback: Boolean = false`
     - `errorMessage: String? = null`
   - Intenciones `PrivacyNoticeIntent`:
     - `data class Load(val locale: String = "es-MX") : PrivacyNoticeIntent`
     - `data object Retry : PrivacyNoticeIntent`
5. **Simulación en `MockAuthApi`:**
   - Retardo determinista de 200–500 ms.
   - Si `locale == "net_error"` → lanza `AuthApiException(AuthErrorKind.NETWORK, "Error de red al consultar aviso de privacidad", retryable = true)`.
   - Éxito estándar → devuelve `PrivacyNoticeResponse` con versión `"2026-11-v1"`, título "Aviso de Privacidad Integral de Nexum", cuerpo formal descriptivo de tratamiento de finanzas en la nube y fecha `"2026-11-02T00:00:00Z"`.

## Contratos y modelos de datos (NEXUM-E015)

```kotlin
// DTO de red (contrato NEXUM-E015)
data class PrivacyNoticeResponse(
    val version: String,
    val title: String,
    val body: String,
    val publishedAt: String
)

// Modelo de dominio
data class PrivacyNotice(
    val version: String,
    val title: String,
    val body: String,
    val publishedAt: String,
    val isFromLocalFallback: Boolean = false
)

// Interfaz ampliada en AuthApi
interface AuthApi {
    // Métodos previos...
    suspend fun getCurrentPrivacyNotice(locale: String? = null): PrivacyNoticeResponse
}

// Interfaz ampliada en AuthRepository
interface AuthRepository {
    // Métodos previos...
    suspend fun getCurrentPrivacyNotice(locale: String = "es-MX"): AuthResult<PrivacyNotice>
}
```

## Estados e Intenciones MVI

```kotlin
data class PrivacyNoticeUiState(
    val isLoading: Boolean = false,
    val notice: PrivacyNotice? = null,
    val isFallback: Boolean = false,
    val errorMessage: String? = null
)

sealed interface PrivacyNoticeIntent {
    data class Load(val locale: String = "es-MX") : PrivacyNoticeIntent
    data object Retry : PrivacyNoticeIntent
}
```

## Orden de trabajo
| Paso | Acción | Requisito rector |
| --- | --- | --- |
| **N270-P01** | Definir modelos DTO de NEXUM-E015 (`PrivacyNoticeResponse`) y dominio (`PrivacyNotice`). | M270-R02 |
| **N270-P02** | Ampliar contratos `AuthApi` y `AuthRepository` con la consulta del aviso vigente y fallback local. | M270-R02, M270-R04 |
| **N270-P03** | Implementar simulación en `MockAuthApi` con contenido legal estructurado, retardo y centinela de red. | M270-R03, M270-R06 |
| **N270-P04** | Construir `PrivacyNoticeViewModel` MVI con soporte de estados, reintento y propagación de versión. | M270-R03, M270-R05 |
| **N270-P05** | Diseñar pantalla/diálogo `PrivacyNoticeScreen` en Jetpack Compose con Material 3, scroll accesible y cabeceras semánticas. | M270-R01, M270-R05 |
| **N270-P06** | Desarrollar pruebas unitarias completas (`PrivacyNoticeViewModelTest`, `MockAuthApiTest`) con `kotlinx-coroutines-test`. | M270-R01…M270-R06 |

## Dependencias y decisiones técnicas
- **Aprobado**: Material 3 y Jetpack Compose sin librerías externas superfluas.
- **Endpoint público**: La consulta del aviso no requiere cabecera de autenticación (`Authorization`), posibilitando la lectura durante el registro previo a la existencia de cualquier token.
- **Resiliencia de registro (MO-PR08)**: Si el usuario no tiene red al pulsar "Ver aviso", el fallback local evita bloquear la lectura de los términos de servicio y permite registrar el consentimiento con la versión de contingencia.
- **Vinculación con el registro (MO-PR04)**: El campo `version` recuperado se conserva en `RegisterViewModel` para remitirse como `privacyNoticeVersion` en `POST /api/v1/auth/register`.

## Estrategia de pruebas
- **Pruebas de ViewModel:**
  - Emisión de estado `Loading` al iniciar la carga.
  - Recepción exitosa de contenido transiciona a `Content` con `isFallback = false`.
  - Simulación de fallo de red activa `isFallback = true` y carga el texto de contingencia sin dejar la pantalla vacía.
  - Acción de reintento (`Retry`) dispara nueva consulta a la API.
- **Pruebas de Repositorio y Mock:**
  - `getCurrentPrivacyNotice()` retorna los datos simulados con `version = "2026-11-v1"`.
  - Parámetro centinela `locale = "net_error"` ejecuta la ruta de contingencia local.
- **Pruebas de regresión:**
  - Verificación de `./gradlew assembleDebug` y aprobación continua de los 50 tests preexistentes de autenticación.
