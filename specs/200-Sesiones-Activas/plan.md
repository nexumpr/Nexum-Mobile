---
project: Nexum-Mobile
unit: 200-Sesiones-Activas
kind: technical-plan
version: 1.1.0
status: ready
implementation: only_on_explicit_request
---

# PLAN — Consulta de Sesiones Propias (List Sessions)

## Reglas Operativas y Contexto
- **Lectura obligatoria previa:** Consultar `memoria.md` antes de ejecutar cualquier paso del plan para alinear convenciones del repositorio, manejo de tokens y rutas de navegación.
- **Restricción de repositorio:** No ejecutar ninguna acción en GitHub (sin commits, branches, pushes ni PRs).

## Estrategia técnica
Implementar la consulta y presentación de sesiones propias bajo el contrato **NEXUM-E008** (`GET /api/v1/auth/sessions`) en la arquitectura MVI y el patrón Repositorio del módulo `auth`:

1. **Contratos DTO y mapeos de datos (estrictos a E008):**
    - DTO de respuesta paginada: `SessionsPageResponse(val items: List<SessionItemResponse>, val nextCursor: String? = null)`.
    - DTO de ítem (exactamente los 5 campos del contrato): `SessionItemResponse(val id: String, val createdAt: String, val lastSeenAt: String, val current: Boolean, val revoked: Boolean)`.
    - Modelo de dominio UI (sin depender de nombre de dispositivo, plataforma, IP ni ubicación):
      `UserSession(val id: String, val shortId: String, val formattedCreatedAt: String, val formattedLastSeenAt: String, val isCurrent: Boolean, val isRevoked: Boolean)`.
    - Modelo de página de dominio: `SessionsPage(val items: List<UserSession>, val nextCursor: String?)`.

2. **Ampliación de interfaces y paginación por cursor:**
    - En `AuthApi`:
      `suspend fun getSessions(accessToken: String, limit: Int? = 25, cursor: String? = null): SessionsPageResponse`
      *(Endpoint: `GET /api/v1/auth/sessions` con header `Authorization: Bearer <accessToken>` y query params opcionales `limit` [1–100] y `cursor` [8–2048 chars opaco]).*
    - En `AuthRepository`:
      `suspend fun getSessions(limit: Int = 25, cursor: String? = null): AuthResult<SessionsPage>`

3. **Mapeo, Formato Local y Reglas de Presentación:**
    - Transformar `createdAt` y `lastSeenAt` (ISO-8601 UTC) a formato legible local (ej. `"Activo ahora"`, `"Última actividad: hoy 18:20"`, `"Iniciada el 17 nov 2026"`).
    - Generar `shortId` visual a partir de los primeros caracteres de `id` (ej. `"Sesión #a1b2c3d4"`) junto con el badge `"Sesión actual"` cuando `current == true` y estado `"Revocada"` si `revoked == true`, evitando cualquier dependencia de datos de hardware/IP inexistentes en E008.
    - Garantizar que la sesión actual (`isCurrent == true`) encabece la lista en la primera página.
    - Si el backend responde `400 Bad Request` por cursor manipulado, vencido o de otro usuario, mapear a error de validación/paginación (`AuthErrorKind.VALIDATION_ERROR` o equivalente en `AuthRepository`) y permitir recargar desde la primera página (`cursor = null`).

4. **Estados MVI de la pantalla:**
    - `SessionsUiState`:
        - `sessions: List<UserSession> = emptyList()`
        - `nextCursor: String? = null`
        - `isLoading: Boolean = false`
        - `isRefreshing: Boolean = false`
        - `isLoadingMore: Boolean = false`
        - `errorMessage: String? = null`
    - Intenciones `SessionsIntent`:
        - `LoadSessions`
        - `Refresh`
        - `LoadMore`
        - `DismissError`

5. **Simulación en `MockAuthApi`:**
    - Retardo determinista de 400–800 ms.
    - Valida rango de `limit` (`1..100`) y longitud de `cursor` (`8..2048`) cuando no sea nulo; si el cursor es inválido o igual al centinela `"invalid-cursor"`, lanza `AuthApiException(AuthErrorKind.VALIDATION_ERROR, "Cursor de paginación inválido o expirado")`.
    - Retorna `SessionsPageResponse` con ítems que respetan estrictamente los 5 campos (`id`, `createdAt`, `lastSeenAt`, `current`, `revoked`), soportando al menos 2 páginas con `nextCursor` opaco y omitiendo `nextCursor` (`null`) en la última página.
    - Token centinela `net@error.test` lanza `AuthApiException(AuthErrorKind.NETWORK, "Error al consultar sesiones", retryable = true)`.

## Contratos y modelos de datos (NEXUM-E008)

```kotlin
// DTOs de red (contrato NEXUM-E008)
@Serializable
data class SessionsPageResponse(
    val items: List<SessionItemResponse>,
    val nextCursor: String? = null
)

@Serializable
data class SessionItemResponse(
    val id: String,
    val createdAt: String,
    val lastSeenAt: String,
    val current: Boolean,
    val revoked: Boolean
)

// Modelos de dominio para presentación
data class UserSession(
    val id: String,
    val shortId: String,
    val formattedCreatedAt: String,
    val formattedLastSeenAt: String,
    val isCurrent: Boolean,
    val isRevoked: Boolean
)

data class SessionsPage(
    val items: List<UserSession>,
    val nextCursor: String?
)

// Interfaz ampliada en AuthApi
interface AuthApi {
    // GET /api/v1/auth/sessions · Bearer · 200 OK
    suspend fun getSessions(
        accessToken: String,
        limit: Int? = 25,
        cursor: String? = null
    ): SessionsPageResponse
}

// Interfaz ampliada en AuthRepository
interface AuthRepository {
    suspend fun getSessions(
        limit: Int = 25,
        cursor: String? = null
    ): AuthResult<SessionsPage>
}