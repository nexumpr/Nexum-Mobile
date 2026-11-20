# NEXUM — Guía de integración Android ↔ Backend (E001–E011)

**Destinatarios:** equipo de aplicación móvil Android (Kotlin / Jetpack Compose)  
**Fecha de referencia:** 18 de noviembre de 2026  
**Backend funcional reportado:** `v0.15.0+8f13c86d2ca0` · Launch Template v10 · AWS `us-east-2`  
**Documentación cerrada:** PR #80 integrado en `main` (`88ef920f74da375b588f3e850a760aa0530df050`)  
**Alcance:** exclusivamente los once endpoints de autenticación y verificación E001–E011. **No es la especificación de los endpoints financieros restantes.**

> **IMPORTANTE PARA EL EQUIPO MÓVIL: aún no cuentan con correos habilitados en Amazon SES.** Cuando lleguen a las pantallas de *Olvidé mi contraseña* (E005–E006) o *Verificar correo* (E010–E011), **deben solicitar al responsable del backend que habilite/verifique el correo de prueba en Amazon SES, región `us-east-2`**, antes de ejecutar el flujo real. El usuario del correo tendrá que abrir el enlace de verificación que envía AWS. No pedir acceso a las cuentas AWS ni compartir contraseñas. Hasta entonces pueden implementar las interfaces, DTO, navegación, mocks locales y pruebas de validación HTTP, pero **no deben marcar la recepción de correos como probada**.

## 1. Conexión y reglas comunes

| Configuración | Valor o regla |
|---|---|
| URL base para Retrofit | `https://d2lpmqy5w6x9es.cloudfront.net/api/v1/` |
| Protocolo | HTTPS obligatorio |
| Tipo de cuerpo | `Content-Type: application/json` para las peticiones que llevan JSON |
| Autenticación | `Authorization: Bearer <accessToken>` solo en endpoints protegidos |
| Duración de access token | `expiresIn: 900` segundos (15 minutos) en el contrato E002 |
| Refresh | Token opaco de un solo uso; **hay que sustituirlo después de cada E003 exitoso** |
| Montos E001 | Cadenas decimales de dos posiciones: `"12000.00"`, **no** `12000.0` |
| Fechas | ISO 8601 / RFC3339; guardar y convertir a zona local en la interfaz |
| Errores | JSON con `error.code`, `error.message`, `error.details` y, donde esté disponible, `error.traceId` |
| Límites de peticiones | Respetar `429` y cabecera `Retry-After`; no reintentar en bucle |
| Datos de seguridad | Jamás registrar tokens, contraseñas, códigos de correo ni bodies de autenticación en logs/Crashlytics |

**Construcción correcta de la URL:** el prefijo `/api/v1/` va en la base URL y las rutas de Retrofit empiezan por `auth/`. No concatenar `/api/v1/` dos veces. La URL `api.example.invalid` del OpenAPI es un **placeholder documental**, no el servidor de producción.

### Qué endpoints requieren iniciar sesión

| Código | Método | Ruta relativa | Bearer | Body |
|---|---|---|---|---|
| E001 | POST | `auth/register` | No | JSON |
| E002 | POST | `auth/login` | No | JSON |
| E003 | POST | `auth/refresh` | No | JSON |
| E004 | POST | `auth/logout` | **Sí** | **Ninguno** |
| E005 | POST | `auth/password/forgot` | No | JSON |
| E006 | POST | `auth/password/reset` | No | JSON |
| E007 | POST | `auth/password/change` | **Sí** | JSON |
| E008 | GET | `auth/sessions` | **Sí** | Ninguno |
| E009 | DELETE | `auth/sessions/{sessionId}` | **Sí** | Ninguno |
| E010 | POST | `auth/email-verification/request` | **Sí** | **Ninguno** |
| E011 | POST | `auth/email-verification/confirm` | No | JSON |

## 2. Flujo recomendado de pantallas

```text
Bienvenida
 ├─ Crear cuenta → E001 → Guardar estado inicial → Pantalla de iniciar sesión → E002
 ├─ Iniciar sesión → E002 → Guardar tokens → Resolver onboarding y navegación principal
 └─ Olvidé mi contraseña → E005 → Leer correo → E006 → Iniciar sesión E002

Usuario autenticado
 ├─ Continuar sesión → E003 automáticamente al necesitar renovar credenciales
 ├─ Cerrar sesión → E004 → Limpiar sesión local → Login
 ├─ Cambiar contraseña → E007 → Limpiar sesiones locales → Login con nueva contraseña
 ├─ Dispositivos y sesiones → E008 → E009 para revocar una sesión elegida
 └─ Verificación de correo
      ├─ Si user.emailVerified == true: mostrar "Verificado"; no volver a solicitar E010
      └─ Si user.emailVerified == false:
            solicitar habilitación de correo SES al responsable (mientras siga sandbox)
            → E010 → abrir correo y copiar código → E011
            → nuevo E002 cuando sea necesario para reflejar user.emailVerified == true
```

**Diferencias importantes:** `emailVerified` es un estado del usuario **dentro de Nexum**; una identidad de correo *verificada en Amazon SES* es un permiso del proveedor para enviar mensajes durante el sandbox. **No son lo mismo.** El equipo móvil no verifica el correo en AWS por sí mismo.

---

## 3. E001 — Registrar usuario

**Pantalla:** registro, ingresos habituales, gastos fijos y aceptación del aviso de privacidad.  
**Endpoint:** `POST /api/v1/auth/register`  
**Autenticación:** pública.  
**Header obligatorio:** `Idempotency-Key: <UUID aleatorio por intento lógico>` (cadena de 16–128 caracteres). En reintentos de **la misma operación y el mismo JSON**, reutilizar la misma clave; para un registro diferente, generar otra.  
**Respuesta:** `201 Created`. **No genera accessToken ni sesión.**

### JSON de solicitud

```json
{
  "name": "Usuario de Prueba",
  "email": "usuario@correo-de-prueba.com",
  "password": "EjemploSeguro#2026",
  "income": { "amount": "12000.00", "frequency": "monthly" },
  "recurringExpenses": [
    { "concept": "Internet", "amount": "699.00", "frequency": "monthly", "category": "Servicios", "type": "fixed" }
  ],
  "privacy": { "accepted": true, "noticeVersion": "2026-11" },
  "client": { "locale": "es-MX", "timeZone": "America/Mexico_City" }
}
```

**Campos obligatorios:** `name`, `email`, `password`, `income`, `recurringExpenses`, `privacy`. El objeto `client` es opcional. Dentro de `income` se exigen `amount` y `frequency`; dentro de cada gasto, los cinco campos. `recurringExpenses` **puede ser `[]`** y admite máximo 94 registros. `privacy.accepted` debe ser `true`, y `noticeVersion` debe corresponder a la versión real del aviso mostrado (no fijarla siempre a `2026-11` si el aviso cambia).

| Campo | Reglas |
|---|---|
| `name` | 1–100 caracteres. **No usar `displayName`: el contrato exige `name`.** |
| `email` | Formato email, máximo 254 caracteres |
| `password` | 12–128 caracteres |
| `amount` | String con patrón `^[0-9]+\.[0-9]{2}$`, positivo, en MXN |
| `frequency` | `weekly`, `fortnightly`, `monthly`, `bimonthly`, `quarterly`, `semiannual`, `annual` |
| `category` | `Vivienda`, `Servicios`, `Transporte`, `Comida`, `Otros` |
| `type` | Únicamente `fixed` (compromiso programado, **no** gasto ejecutado) |
| `client.locale` | Idioma; ejemplo `es-MX` |
| `client.timeZone` | Zona IANA; ejemplo `America/Mexico_City` |

### Respuesta 201 (ejemplo reducido, con la estructura real)

```json
{
  "user": { "id": "<uuid>", "name": "Usuario de Prueba", "email": "usuario@correo-de-prueba.com", "emailVerified": false, "createdAt": "2026-11-18T01:00:00Z" },
  "income": { "amount": "12000.00", "frequency": "monthly", "monthlyEquivalent": "12000.00" },
  "recurringPayments": [
    { "id": "<uuid>", "concept": "Internet", "amount": "699.00", "frequency": "monthly", "category": "Servicios", "type": "fixed" }
  ],
  "consent": { "noticeVersion": "2026-11", "acceptedAt": "2026-11-18T01:00:00Z", "acceptedByServer": true },
  "onboarding": { "microphone": "pending", "notifications": "pending", "configured": false }
}
```

**Aplicación móvil:** crear formularios de ingreso y gastos fijos, validar antes de enviar y usar el `monthlyEquivalent` calculado por backend. Si recibe `201`, dirigir a login (E002). No inventar tokens. Errores relevantes: `400` datos/cabeceras, `409` conflicto (por ejemplo correo ya existente), `422` regla de negocio, `429` cuota.

## 4. E002 — Iniciar sesión

**Pantalla:** login.  
**Endpoint:** `POST /api/v1/auth/login` · pública · `200 OK`.

```json
{
  "email": "usuario@correo-de-prueba.com",
  "password": "<contraseña-del-usuario>",
  "deviceId": "<identificador-estable-de-instalacion>",
  "deviceName": "Android",
  "platform": "android"
}
```

Obligatorios: `email`, `password`. Opcionales: `deviceId` (máx. 128), `deviceName` (máx. 80), `platform` (solo `android` si se envía). Para `deviceId`, generar y conservar un UUID por instalación; **no usar IMEI ni otros identificadores sensibles**.

```json
{
  "accessToken": "<JWT>",
  "refreshToken": "<TOKEN_OPACO>",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "sessionId": "<uuid-sesion>",
  "session": { "id": "<uuid-sesion>" },
  "user": { "id": "<uuid-usuario>", "name": "Usuario de Prueba", "email": "usuario@correo-de-prueba.com", "emailVerified": false },
  "onboarding": { "configured": false }
}
```

**Aplicación móvil:** almacenar `accessToken`, `refreshToken` y `sessionId` en almacenamiento cifrado respaldado por Android Keystore. Guardar `user.emailVerified` y `onboarding.configured` en estado de sesión. Una cuenta sin correo verificado **puede iniciar sesión**; no imponer un bloqueo general que el contrato de E002 no define. `401` es contraseña/credenciales inválidas (respuesta real del backend); `429` significa límite, no contraseña errónea. No reintentar repetidamente.

## 5. E003 — Renovar sesión (refresh)

**Operación automática:** obtener nuevos tokens cuando el access token expire o sea necesario renovar la sesión.  
**Endpoint:** `POST /api/v1/auth/refresh` · pública (no enviar Bearer para autenticarla) · `200 OK`.

```json
{
  "refreshToken": "<REFRESH_ACTUAL>",
  "deviceId": "<identificador-estable-de-instalacion>"
}
```

`refreshToken` obligatorio y `deviceId` opcional. El `deviceId`, si se envía, debe coincidir con el contexto de la sesión creada; no sustituye al token.

```json
{
  "accessToken": "<NUEVO_JWT>",
  "refreshToken": "<NUEVO_REFRESH>",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "sessionId": "<uuid-sesion>",
  "session": { "id": "<uuid-sesion>" }
}
```

**Regla crítica:** el refresh es **de un solo uso**. Al recibir `200`, reemplazar **ambos** tokens de forma atómica en almacenamiento local y no reutilizar el anterior. En producción se verificó `200` con refresh válido; un replay devolvió `409` y la familia después respondió `401` (revocación de seguridad). No hacer dos refresh simultáneos con el mismo token. Implementar *single-flight* mediante `Mutex`/sincronización: una renovación a la vez para toda la app, y las demás peticiones reutilizan su resultado. Ante `401` o replay `409`, eliminar credenciales y llevar a login. Ante `503`, informar incidencia temporal sin reusar indiscriminadamente ni entrar en bucles.

**Integración con OkHttp:** utilizar un cliente separado para login/refresh, sin un `Authenticator` que se invoque recursivamente. Considerar el `401` de una petición protegida, obtener nuevos tokens una vez y repetir esa petición una sola vez. Una revocación de sesión no se resuelve intentando refresh indefinidamente.

## 6. E004 — Cerrar sesión

**Pantalla:** botón "Cerrar sesión".  
**Endpoint:** `POST /api/v1/auth/logout` · **Bearer obligatorio** · **sin body** · `204 No Content`.

Ejemplo de cabecera:

```http
Authorization: Bearer <ACCESS_TOKEN_ACTUAL>
```

**Aplicación móvil:** al obtener `204`, borrar credenciales locales y navegar a login. No intentar parsear JSON de una respuesta `204`. La sesión queda revocada de manera durable. Access y refresh posteriores deben fallar con `401`. Si la sesión ya fue revocada, puede responder `401`: limpiar la sesión local igualmente. No reintentar logout con tokens antiguos.

## 7. E005 — Solicitar recuperación de contraseña

**Pantalla:** "Olvidé mi contraseña", introducir email.  
**Endpoint:** `POST /api/v1/auth/password/forgot` · pública · `202 Accepted`.

```json
{ "email": "usuario@correo-de-prueba.com" }
```

```json
{ "status": "accepted", "requestId": "<uuid>" }
```

**Aplicación móvil:** mostrar mensaje neutral "Si la dirección puede recibir correos, revisa tu bandeja de entrada". No usar la respuesta para afirmar que el email existe. Abrir pantalla de código + nueva contraseña (E006). El código de recuperación llega **por correo** y tiene vigencia de 30 minutos según la implementación. Respetar `429` / `Retry-After`.

**Aviso SES para pruebas actuales:** mientras la cuenta AWS siga en sandbox, el equipo debe **pedir al responsable que verifique su destinatario antes de probar la entrega real**. La respuesta `202` no significa que Gmail ya lo recibió: E005 trabaja mediante una cola/worker de envío. No usar `@example.invalid` para probar recepción.

## 8. E006 — Restablecer contraseña

**Pantalla:** introducir código de recuperación recibido y contraseña nueva.  
**Endpoint:** `POST /api/v1/auth/password/reset` · pública · `200 OK`.

```json
{
  "token": "<CODIGO_DE_RECUPERACION_RECIBIDO>",
  "newPassword": "NuevaClaveEjemplo#2026"
}
```

```json
{ "status": "accepted", "requestId": "<uuid>" }
```

`token` obligatorio (mínimo 10 caracteres), `newPassword` obligatorio (12–128 caracteres). El token es de **un solo uso**; reutilizarlo devolvió `409` en pruebas de producción. La operación invalida las sesiones anteriores mediante la época de sesión. **Aplicación móvil:** después de `200`, borrar cualquier sesión local y solicitar login E002 con la nueva contraseña. Código expirado/inválido: `400`; usado: `409`; reglas de contraseña: `422`.

## 9. E007 — Cambiar contraseña autenticado

**Pantalla:** ajustes de seguridad → cambiar contraseña.  
**Endpoint:** `POST /api/v1/auth/password/change` · **Bearer** · `200 OK`.

```json
{
  "currentPassword": "<CONTRASENA_ACTUAL>",
  "newPassword": "NuevaClaveEjemplo#2026"
}
```

Respuesta:

```json
{ "status": "accepted", "requestId": "<uuid>" }
```

`currentPassword` obligatorio; `newPassword` mínimo 12 caracteres (no mandar claves débiles). **Aplicación móvil:** al confirmar el cambio, **limpiar access y refresh tokens**, cerrar la sesión visual y volver a login: E007 incrementa la época de sesión y revoca las sesiones anteriores. No quedarse en pantallas protegidas con un JWT anterior. Confirmado en producción: cambio `200`, token anterior `401`, login con contraseña nueva `200`, anterior `401`.

## 10. E008 — Consultar sesiones propias

**Pantalla:** perfil → dispositivos y sesiones.  
**Endpoint:** `GET /api/v1/auth/sessions` · **Bearer** · `200 OK`.

Ejemplo:

```http
GET /api/v1/auth/sessions?limit=25
Authorization: Bearer <ACCESS_TOKEN>
```

`limit` es opcional: predeterminado 25; admite **1–100**. `cursor` es opcional (cadena opaca firmada de 8–2048 caracteres). Para más páginas, **pasar exactamente el `nextCursor` retornado**; no inventar offset ni decodificarlo.

```json
{
  "items": [
    { "id": "<uuid-sesion-1>", "createdAt": "2026-11-18T01:00:00Z", "lastSeenAt": "2026-11-18T01:05:00Z", "current": true, "revoked": false },
    { "id": "<uuid-sesion-2>", "createdAt": "2026-11-17T23:00:00Z", "lastSeenAt": "2026-11-18T00:00:00Z", "current": false, "revoked": false }
  ],
  "nextCursor": "<CURSOR_OPACO_SI_HAY_OTRA_PAGINA>"
}
```

`nextCursor` se omite si no hay más páginas. `items` puede ser `[]`. **Son exactamente cinco campos por sesión**: `id`, `createdAt`, `lastSeenAt`, `current`, `revoked`. **No existen en esta respuesta**: nombre del dispositivo, plataforma, IP, ubicación, refresh token, etc. No diseñar UI que dependa de esos datos hasta que exista un nuevo contrato. Un cursor manipulado, vencido o de otro usuario produce `400`.

## 11. E009 — Revocar una sesión concreta

**Pantalla:** la misma lista E008; acción "Cerrar sesión" para un registro.  
**Endpoint:** `DELETE /api/v1/auth/sessions/{sessionId}` · **Bearer** · **sin body** · `204 No Content`.

La `sessionId` viene de `items[].id` de E008. Debe ser UUID. No enviar `userId` ni `sessionId` en el body. El backend valida que la sesión sea del usuario autenticado; ajena, inexistente o expirada → `404` sin confirmar su existencia.

**Aplicación móvil:** al obtener `204`, actualizar la lista llamando E008. Si la sesión revocada era la **actual**, eliminar la sesión local y regresar a login (el access token dejará de estar autorizado). Una revocación ya aplicada puede ser idempotente (`204`); `409` indica conflicto concurrente. No interpretar el `204` como un JSON vacío.

## 12. E010 — Solicitar verificación de correo

**Pantalla:** ajustes de cuenta → "Verificar mi correo" o aviso si `user.emailVerified` es `false`.  
**Endpoint:** `POST /api/v1/auth/email-verification/request` · **Bearer** · **sin body** · `202 Accepted`.

```http
POST /api/v1/auth/email-verification/request
Authorization: Bearer <ACCESS_TOKEN>
```

```json
{ "status": "accepted", "requestId": "<uuid>" }
```

**MUY IMPORTANTE: no mandar `{ "email": "..." }` en E010.** El backend toma el destinatario del perfil autenticado y rechaza un body. El token de verificación tiene 30 minutos de vigencia y la solicitud está limitada (implementación: 3 solicitudes por usuario cada 15 minutos). El endpoint responde `409` cuando el correo **ya está verificado**. `202` confirma aceptación por SES en este flujo, no lectura humana del correo.

**Restricción temporal obligatoria del equipo móvil:** el SES real permanece en **sandbox** (`ProductionAccessEnabled = false` en la evidencia de despliegue). El equipo **no dispone todavía de destinatarios verificados en SES**. Antes de probar E010 deben contactar al responsable del backend, proporcionar **el correo de prueba que controlen** y completar la verificación AWS del destinatario. Si usan un email no verificado, SES puede rechazar el envío y Nexum responder `503 DEPENDENCY_UNAVAILABLE`. **No cambiar el código Android para ocultar ese 503 o mostrar “correo enviado” falsamente.** Para los usuarios finales futuros, el responsable deberá gestionar la salida del sandbox de SES; la habilitación manual actual es una limitación del entorno, no un flujo previsto para cada usuario final.

## 13. E011 — Confirmar verificación de correo

**Pantalla:** campo para pegar el código que llegó por E010.  
**Endpoint:** `POST /api/v1/auth/email-verification/confirm` · **pública** (sin Bearer) · `200 OK`.

```json
{ "token": "<CODIGO_DE_VERIFICACION_RECIBIDO>" }
```

```json
{ "status": "accepted", "requestId": "<uuid>" }
```

`token` obligatorio, mínimo 10 caracteres, sensible a mayúsculas y espacios. No modificarlo ni guardar su contenido en logs. Código inválido o expirado → `400`; reutilizado o carrera de confirmación → `409`. La confirmación realiza una transacción de uso único y marca `emailVerified=true` en DynamoDB. Se verificó en producción: `200`, segundo uso `409`, y login posterior devolvió `user.emailVerified=true`.

**Aplicación móvil:** al recibir `200`, mostrar éxito, actualizar el estado del perfil y no permitir solicitar otra verificación si `emailVerified` ya es `true`. Como los endpoints disponibles actualmente no incluyen un GET de perfil general, la forma respaldada de obtener el `user.emailVerified` actualizado es la **respuesta de un login E002 posterior**; no inventar un GET `/me` o asumir que E003 devuelve `user`. Puede actualizarse el estado visual después de E011, pero conservar el estado real del backend como fuente de verdad.

---

## 14. Modelos sugeridos para Kotlin / Retrofit

El siguiente ejemplo muestra **los contratos esenciales**; adáptenlo a su arquitectura existente (Retrofit + Gson/Moshi/Kotlinx Serialization). Los nombres JSON deben conservarse exactamente. Agreguen modelos de registro completos según E001, no reemplacen `income` por un número ni `recurringExpenses` por un string.

```kotlin
// Modelos de autenticación y sesiones (ejemplo para Gson/Retrofit).
data class LoginRequest(
    val email: String,
    val password: String,
    val deviceId: String? = null,
    val deviceName: String? = null,
    val platform: String? = "android"
)

data class SessionRef(val id: String)
data class AuthUser(
    val id: String,
    val name: String,
    val email: String,
    val emailVerified: Boolean
)
data class LoginOnboarding(val configured: Boolean)

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val expiresIn: Int,
    val sessionId: String,
    val session: SessionRef,
    val user: AuthUser,
    val onboarding: LoginOnboarding
)

data class RefreshRequest(val refreshToken: String, val deviceId: String? = null)
data class RefreshResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val expiresIn: Int,
    val sessionId: String,
    val session: SessionRef? = null
)

data class EmailRequest(val email: String)
data class PasswordResetRequest(val token: String, val newPassword: String)
data class PasswordChangeRequest(val currentPassword: String, val newPassword: String)
data class VerifyEmailRequest(val token: String)
data class AcceptedResponse(val status: String, val requestId: String)

data class RemoteSession(
    val id: String,
    val createdAt: String,
    val lastSeenAt: String,
    val current: Boolean,
    val revoked: Boolean
)
data class SessionsPage(val items: List<RemoteSession>, val nextCursor: String? = null)

data class ApiErrorDetail(val field: String? = null, val reason: String? = null)
data class ApiErrorObject(
    val code: String,
    val message: String,
    val details: List<ApiErrorDetail> = emptyList(),
    val traceId: String? = null
)
data class ErrorEnvelope(val error: ApiErrorObject)
```

### Modelos E001

```kotlin
data class IncomeInput(val amount: String, val frequency: String)
data class RecurringExpenseInput(
    val concept: String,
    val amount: String,
    val frequency: String,
    val category: String,
    val type: String = "fixed"
)
data class PrivacyInput(val accepted: Boolean, val noticeVersion: String)
data class ClientInput(val locale: String? = null, val timeZone: String? = null)
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val income: IncomeInput,
    val recurringExpenses: List<RecurringExpenseInput>,
    val privacy: PrivacyInput,
    val client: ClientInput? = null
)
data class RegisterUser(
    val id: String, val name: String, val email: String,
    val emailVerified: Boolean, val createdAt: String
)
data class RegisterIncome(
    val amount: String, val frequency: String, val monthlyEquivalent: String
)
data class RecurringPayment(
    val id: String, val concept: String, val amount: String,
    val frequency: String, val category: String, val type: String
)
data class RegisterConsent(
    val noticeVersion: String, val acceptedAt: String, val acceptedByServer: Boolean
)
data class RegisterOnboarding(
    val microphone: String, val notifications: String, val configured: Boolean
)
data class RegisterResponse(
    val user: RegisterUser,
    val income: RegisterIncome,
    val recurringPayments: List<RecurringPayment>,
    val consent: RegisterConsent,
    val onboarding: RegisterOnboarding
)
```

### Interfaz Retrofit para los once endpoints

```kotlin
import retrofit2.Response
import retrofit2.http.*

interface NexumAuthApi {
    @POST("auth/register")
    suspend fun register(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body body: RegisterRequest
    ): Response<RegisterResponse>

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshRequest): Response<RefreshResponse>

    @POST("auth/logout")
    suspend fun logout(@Header("Authorization") authorization: String): Response<Unit>

    @POST("auth/password/forgot")
    suspend fun forgotPassword(@Body body: EmailRequest): Response<AcceptedResponse>

    @POST("auth/password/reset")
    suspend fun resetPassword(@Body body: PasswordResetRequest): Response<AcceptedResponse>

    @POST("auth/password/change")
    suspend fun changePassword(
        @Header("Authorization") authorization: String,
        @Body body: PasswordChangeRequest
    ): Response<AcceptedResponse>

    @GET("auth/sessions")
    suspend fun sessions(
        @Header("Authorization") authorization: String,
        @Query("limit") limit: Int = 25,
        @Query("cursor") cursor: String? = null
    ): Response<SessionsPage>

    @DELETE("auth/sessions/{sessionId}")
    suspend fun revokeSession(
        @Header("Authorization") authorization: String,
        @Path("sessionId") sessionId: String
    ): Response<Unit>

    @POST("auth/email-verification/request")
    suspend fun requestEmailVerification(
        @Header("Authorization") authorization: String
    ): Response<AcceptedResponse>

    @POST("auth/email-verification/confirm")
    suspend fun confirmEmailVerification(
        @Body body: VerifyEmailRequest
    ): Response<AcceptedResponse>
}
```

**Uso:** `authorization = "Bearer ${accessToken}"`. No adjuntar Bearer a endpoints públicos por costumbre. Los métodos `logout` y `revokeSession` usan `Response<Unit>` porque su `204` **no contiene JSON**. El cliente HTTP debe configurarse con Retrofit `baseUrl("https://d2lpmqy5w6x9es.cloudfront.net/api/v1/")` y un convertidor JSON instalado.

**Nota sobre serialización:** en Kotlin, campos opcionales `null` pueden omitirse o serializarse como `null` según el convertidor; para E001/E002/E003 es preferible **omitir** los opcionales ausentes. No habilitar serialización de valores extra porque el backend rechaza propiedades desconocidas.

## 15. Manejo centralizado de errores y sesión

| HTTP | Conducta recomendada en Android |
|---|---|
| 200/201/202 | Manejar el DTO de cada endpoint; en 202 distinguir aceptación de entrega de correo |
| 204 | Éxito **sin cuerpo**; no parsear JSON |
| 400 | Corregir JSON, ruta, tipos o tokens mal formados según `error.details` |
| 401 | Credencial/sesión inválida. En endpoint protegido intentar E003 **como máximo una vez** si hay refresh válido; si E003 falla, cerrar sesión local |
| 403 | No autorizado por estado o política; no seguir reintentando |
| 404 | Recurso ausente/no propio; E009 no revela sesiones ajenas |
| 409 | Conflicto: registro duplicado, token usado, replay de refresh o verificación ya hecha; **no tratar todos igual** |
| 422 | Regla de negocio (p. ej. validación de contraseñas/consentimiento) |
| 429 | Límite. Leer `Retry-After`, presentar espera y bloquear reenvíos temporales |
| 500/502/503 | Problema de servicio/gateway; informar. No repetir automáticamente mutaciones sensibles sin verificar estado |

**En especial E003:** un `409` por replay puede revocar **toda la familia de sesiones**; nunca reintentar automáticamente con el mismo refresh token. Coordinar el almacenamiento atómico y evitar duplicaciones por recomposición de Compose, múltiples ViewModels o interceptores simultáneos. No implementar retries automáticos para E005/E010 porque consumen cuotas y envían correos.

**Seguridad:** conservar sesiones con Keystore + almacenamiento cifrado; nunca en SharedPreferences sin cifrar, logs, URL o analytics. Usar HTTPS, `Cache-Control`/política local sin persistencia de respuestas sensibles. No incluir credenciales en capturas compartidas. Si se usan interceptores HTTP para depuración, redactar completamente `Authorization`, `accessToken`, `refreshToken`, `password`, `newPassword` y `token`.

## 16. Plan de pruebas de aceptación del equipo Android

Marquen cada caso cuando **la aplicación móvil** lo compruebe; las pruebas AWS del backend **no sustituyen** esta aceptación.

- [ ] E001: registro con cero y con varios gastos fijos; validación de montos como string; visualización de ingreso y consentimiento; clave de idempotencia.
- [ ] E002: login, almacenamiento cifrado, nombre, onboarding y estado `emailVerified`.
- [ ] E003: renovación única a los 15 minutos o antes si se necesita; tokens nuevos reemplazados y gestión de `401`/`409`.
- [ ] E004: logout sin body y limpieza de sesión local.
- [ ] E005: solicitud, pantalla de "revisar correo" y manejo de `429` (**entrega real pendiente hasta verificar destinatario SES**).
- [ ] E006: código de recuperación, nueva contraseña y regreso a login (**depende de E005 real**).
- [ ] E007: cambio de contraseña y cierre de sesión local tras éxito.
- [ ] E008: sesiones propias, estados `current`/`revoked`, paginación con `nextCursor`.
- [ ] E009: revocación de otra sesión y de la actual, limpieza de autenticación cuando aplica.
- [ ] E010: solicitud sin body y `202` tras verificación SES del correo de pruebas (**pendiente de habilitación**).
- [ ] E011: código recibido, `200`, actualización de estado y `409` por reutilización (**depende de E010 real**).
- [ ] Pruebas con redes lentas, app en segundo plano, cierre y reapertura sin exponer tokens.
- [ ] Prueba de no regresión en Android al cambiar contraseña, revocar sesión y renovar tokens.

### Solicitud que móvil debe enviar al responsable cuando necesite probar correo

> **Asunto: Habilitación de correo de pruebas Nexum — E005/E006 y E010/E011**  
> Ya integramos en Android las pantallas de recuperación y verificación. Necesitamos comprobar la entrega real del correo. Solicitamos habilitar en Amazon SES (`us-east-2`, actualmente sandbox) este correo de pruebas, bajo nuestro control: `<correo-del-tester>`. Confirmaremos la identidad desde el enlace que llegue al buzón. Favor de avisarnos cuando esté verificada para ejecutar E005/E006 y E010/E011, sin compartir credenciales de AWS.

**No pedir al responsable que el equipo comparta su contraseña de Gmail.** La aprobación SES se completa a través del enlace del buzón. Una vez que SES pase a producción, el responsable deberá comunicar formalmente ese cambio de restricción; este documento no afirma que ya haya sucedido.

## 17. Alcance, fuentes y criterios de verdad

Documento elaborado con el **ZIP del backend entregado para esta guía**. Se contrastaron:

- `sdd/03-endpoints/openapi/nexum-api-v1.yaml`: rutas, esquemas JSON, campos obligatorios y respuestas.
- `internal/transport/http/` y `cmd/server/`: DTO, autenticación, montajes, errores, ausencia de body y particularidades efectivas.
- `internal/application/identity/`: tokens de un solo uso, rotación, revocación y duración de verificación.
- `sdd/_control/H08/GATE_H08E_E007_E011_DESPLIEGUE_AWS.md` y `memoria.md`: publicación de E007–E011 y hotfix E003, baseline y pruebas funcionales.

**Advertencia sobre el OpenAPI:** el encabezado conserva terminología documental de la fase H06 y un servidor placeholder; **no significa que los once endpoints sigan sin implementar**. Se utilizó el contrato de rutas y modelos junto con el código del backend y el registro de producción v0.15.0. Algunas rutas emiten códigos efectivos adicionales o variantes de mensajes de error; diseñen el cliente para tolerar campos opcionales y mostrar mensajes seguros, sin suponer que todos los errores incluyen un `traceId`.

**Estado de aceptación:** el backend reportó E001–E011 funcionales en producción, pero **la integración/aceptación del equipo Android aún no está confirmada**. Esta guía sirve precisamente para completar esa fase. No declara que móvil ya haya realizado sus pruebas.

**Cambios de contrato:** si el equipo encuentra una discrepancia concreta entre este documento, OpenAPI y la API real, comunicar endpoint, método, HTTP, body redactado y `traceId` (sin secretos). El equipo backend decide y documenta cualquier corrección; **no inventar campos ni cambiar el contrato unilateralmente**.
