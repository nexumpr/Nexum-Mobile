# NEXUM — Guía de integración Android de la Oleada 1

**Entrega para el equipo móvil | 18 de noviembre de 2026**  
**Backend publicado:** `v0.16.1+615ac1c734f2`  
**Commit `main` de cierre documental:** `c9f7ec1688666718768fdfa3d7a19cbeea1fa1a2`  
**Alcance:** 15 endpoints nuevos de la Oleada 1 (E012, E013, E015–E019, E022–E025, E068–E071).  
**API:** `https://d2lpmqy5w6x9es.cloudfront.net/api/v1`  
**Health:** `GET https://d2lpmqy5w6x9es.cloudfront.net/api/v1/health/live`

> **Cómo usar esta guía.** Está redactada para conectar Android/Kotlin (Retrofit, OkHttp y Jetpack Compose). Los nombres de los campos y tipos se han extraído de la copia disponible del **OpenAPI/SDD H06** del proyecto y contrastado con los resultados de aceptación AWS de H08-E. La copia local del contrato es anterior a algunas correcciones de H08-E (notablemente RFC-020, fecha y contenido E015), por lo que **el OpenAPI más reciente de `main` es la autoridad final** antes de generar clientes o cerrar una integración móvil. Los cuerpos JSON de esta guía son **ejemplos sintéticos**, salvo los valores de versión/estado que se indican explícitamente como probados. No son capturas de datos de usuarios ni sustituyen pruebas del equipo Android.

**Contrato actual de referencia:** [OpenAPI YAML de `main`](https://github.com/nexumpr/Nexum-Backend/blob/main/sdd/03-endpoints/openapi/nexum-api-v1.yaml) · [OpenAPI JSON de `main`](https://github.com/nexumpr/Nexum-Backend/blob/main/sdd/03-endpoints/openapi/nexum-api-v1.json) · [Especificaciones E001–E075](https://github.com/nexumpr/Nexum-Backend/tree/main/sdd/03-endpoints) · [Repositorio backend](https://github.com/nexumpr/Nexum-Backend).

---

## 1. Resumen ejecutivo para móvil

La Oleada 1 añade consulta y edición de **perfil**, **aviso de privacidad y consentimientos**, **onboarding**, **catálogos de categorías y frecuencias**, **ingreso base**, **preferencias**, **FAQ** y **consejos educativos**. Los once endpoints previos E001–E011 continúan publicados y sirven para registro, autenticación y sesiones. **E014 (eliminar cuenta) aún NO está implementado; no conectar a él el botón de eliminación como si ya funcionara.**

| ID | Método | Ruta relativa a `/api/v1` | JWT | Éxito | Uso recomendado en Android |
|---|---|---|:---:|:---:|---|
| E012 | GET | `/users/me` | Sí | 200 | Perfil, encabezado de Home, datos de sesión |
| E013 | PATCH | `/users/me` | Sí | 200 | Editar nombre mostrado |
| E015 | GET | `/privacy/notices/current` | No | 200 | Mostrar aviso legal vigente completo |
| E016 | GET | `/users/me/consents` | Sí | 200 | Historial y estado de consentimientos |
| E017 | POST | `/users/me/consents` | Sí | 201 | Guardar aceptación/negativa expresa |
| E018 | GET | `/users/me/onboarding` | Sí | 200 | Recuperar progreso de configuración |
| E019 | PATCH | `/users/me/onboarding` | Sí | 200 | Guardar pasos y opciones de onboarding |
| E022 | GET | `/catalogs/categories` | No | 200 | Selector de categorías financieras |
| E023 | GET | `/catalogs/frequencies` | No | 200 | Selector de periodicidades |
| E024 | GET | `/income` | Sí | 200 | Consultar ingreso base actual |
| E025 | PUT | `/income` | Sí | 200 | Configurar o actualizar ingreso base |
| E068 | GET | `/users/me/preferences` | Sí | 200 | Cargar idioma y zona horaria |
| E069 | PATCH | `/users/me/preferences` | Sí | 200 | Cambiar idioma y zona horaria |
| E070 | GET | `/help/faq` | No | 200 | Ayuda y preguntas frecuentes |
| E071 | GET | `/education/tips` | No | 200 | Consejos de educación financiera |

**Evidencia de publicación:** 15/15 operaciones nuevas pasaron la suite HTTPS de aceptación (29/29 casos post-hotfix); E015 devolvió 200, dos targets AWS están saludables y una cuenta existente aprobó 7/7 pruebas de login/lectura. No confundir esto con garantía universal de cero errores en todas las versiones de Android.

## 2. Configuración de red común

### 2.1 Base URL, autenticación y cabeceras

```text
BASE_URL = https://d2lpmqy5w6x9es.cloudfront.net/api/v1/
Accept: application/json
Content-Type: application/json          # solicitudes con cuerpo JSON
Authorization: Bearer <accessToken>     # sólo rutas privadas
If-Match: "vN"                         # E013, E025 y E069; conservar las comillas
```

- **JWT:** tomar el `accessToken` recibido por E002 y renovarlo con E003 según su contrato. Nunca escribir el token en logs, Analytics, Crashlytics o repositorio.
- **Identidad:** el backend obtiene el `userId` del JWT. No enviar `userId` arbitrario por query/body para las rutas `/users/me`.
- **Rutas públicas:** E015, E022, E023, E070 y E071 pueden consultarse sin bearer. No forzar una sesión para mostrar el aviso.
- **JSON:** nombres de propiedades **camelCase** y UTF-8. El backend puede rechazar propiedades desconocidas: evitar enviar modelos UI completos como cuerpos HTTP.
- **Cache:** respuestas privadas de identidad/finanzas incluyen política de no almacenamiento o deben tratarse como privadas. Evitar guardarlas en caché HTTP compartida.
- **Errores:** leer `error.code` y `error.details`; no mostrar directamente errores internos en pantalla.
- **Conectividad:** configurar timeouts razonables y tratar 429 conforme a `Retry-After` si el backend lo envía; no reintentar escrituras no idempotentes indiscriminadamente.

### 2.2 ErrorEnvelope

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Datos inválidos",
    "traceId": "018f90ef-08e5-7a68-b09f-2b67a47c92f0",
    "details": [
      { "field": "displayName", "reason": "invalid" }
    ]
  }
}
```

El ejemplo es ilustrativo. Los textos/códigos específicos deben leerse de la respuesta efectiva y del OpenAPI actual. Estados de interés:

| HTTP | Significado para la app | Reacción de interfaz |
|---|---|---|
| 200 / 201 | Lectura/actualización o creación correcta | Actualizar estado local con la **respuesta** del servidor |
| 400 | JSON, cabecera o tipo inválido | Mostrar error de formulario, sin repetir automáticamente |
| 401 | Token ausente, expirado, revocado o sesión inválida | Intentar renovación por E003 una vez; de fallar, volver a login |
| 403 | Operación prohibida | Mensaje de falta de acceso; no insistir |
| 404 | Recurso no encontrado | Pantalla vacía/estado no encontrado según contrato |
| 409 | Conflicto del estado de negocio | Mostrar conflicto y recargar el recurso |
| 412 | `If-Match` ya no corresponde a la versión actual | Recargar con GET y pedir nueva confirmación si hay datos editados |
| 422 | Regla de dominio inválida | Mostrar validación específica |
| 428 | Falta una precondición obligatoria (`If-Match`) | Recuperar `ETag` y reenviar de forma controlada |
| 429 | Exceso de solicitudes | Respetar enfriamiento; evitar bucles |
| 500 / 503 | Error interno o dependencia temporal no disponible | Mensaje recuperable, observabilidad por `traceId` |

### 2.3 Regla clave: `ETag` + `If-Match`

Se utiliza en **E013, E025 y E069** para evitar pérdida de actualizaciones entre dispositivos.

1. Hacer primero el GET del recurso correspondiente (E012, E024 o E068).
2. Conservar el header `ETag` de la respuesta, por ejemplo `"v4"`. El JSON también entrega `version`, pero el **header real prevalece**.
3. Enviar PATCH/PUT con `If-Match: "v4"` (incluidas las comillas).
4. En caso de 200, reemplazar la entidad local por la respuesta y guardar el `ETag` nuevo.
5. Si recibe 412, recuperar el estado actualizado. No sobrescribir silenciosamente cambios hechos desde otro dispositivo.
6. No usar `W/"v4"` (ETag débil) para mutaciones.

Los tests AWS confirmaron 200 con ETag válido, **428 sin cabecera** y **412 con versión obsoleta** en los endpoints que lo requieren.

---

## 3. Contratos por endpoint y cómo consumirlos

Todos los ejemplos usan la base URL indicada, sin duplicar `/api/v1`. Las fechas son UTC ISO-8601; los valores de ejemplo deben sustituirse por la respuesta real.

### E012 — GET `/users/me` — Consultar el perfil

**Pantallas:** Inicio, Perfil, menú de cuenta. **Requiere JWT.** Sin body ni parámetros.

**Éxito `200` — `UserProfile`:**

```json
{
  "id": "018f90ef-08e5-7a68-b09f-2b67a47c92f0",
  "email": "usuario@example.invalid",
  "displayName": "Usuario Ejemplo",
  "emailVerified": true,
  "createdAt": "2026-11-05T17:00:00Z",
  "version": 4
}
```

| Campo | Tipo | Regla |
|---|---|---|
| `id` | UUID string | Obligatorio. Es el usuario autenticado, no editable |
| `email` | string/email | Obligatorio; no cambiarlo desde E013 |
| `displayName` | string | Obligatorio en respuesta |
| `emailVerified` | boolean | Obligatorio; refleja E011 |
| `createdAt` | date-time | Obligatorio |
| `version` | integer >= 1 | Obligatorio; sirve para control de versión |

**Android:** cargar tras E002 o al reabrir una sesión válida, mostrar `displayName` y `emailVerified` sin esperar a los catálogos. Guardar `ETag` para E013. No imprimir `email` completo en logs. **Errores contractuales:** 400, 401, 403, 404, 429, 500, 503.

### E013 — PATCH `/users/me` — Actualizar el nombre

**Pantallas:** Editar perfil. **JWT + `If-Match` obligatorios.** Content-Type JSON.

```http
PATCH /api/v1/users/me
Authorization: Bearer <accessToken>
If-Match: "v4"
Content-Type: application/json

{"displayName":"Nombre actualizado"}
```

**`ProfilePatch`:** único campo documentado `displayName` (string de 1 a 100 caracteres, opcional en schema). **No enviar** `email`, `emailVerified`, `passwordHash`, roles, identificadores ni otros atributos.

**Éxito `200`:** responde `UserProfile` completo con `version` actualizado. Confirmar visualmente el cambio y volver a leer E012 cuando haga falta. **Errores:** 400, 401, 403, 404, 409, **412**, 422, **428**, 429, 500, 503.

**UX recomendada:** deshabilitar Guardar mientras exista una petición activa. Ante 412, mostrar que el perfil cambió en otro dispositivo y ofrecer recargar.

### E015 — GET `/privacy/notices/current` — Aviso de privacidad vigente

**Pantallas:** Antes de consentir, Registro/Onboarding, Ajustes > Privacidad. **Público, sin JWT.**

```json
{
  "version": "2026-11-v2",
  "effectiveAt": "2026-11-18T00:00:00Z",
  "text": "Resumen del aviso de privacidad vigente",
  "url": "https://d2lpmqy5w6x9es.cloudfront.net/api/v1/privacy/notices/current",
  "content": "[TEXTO INTEGRAL DEL AVISO DEVUELTO POR LA API]",
  "requiresAcceptance": true
}
```

**Importante:** `text` y `content` del ejemplo son marcadores explicativos: **no hardcodearlos**. Mostrar exactamente el texto recibido de la API. En el release documentado, `content` tuvo **6761 bytes UTF-8**, versión `2026-11-v2`, `requiresAcceptance=true` y seis campos. Los campos obligatorios mínimos del schema son `version`, `effectiveAt`, `text`, `url`; `content` y `requiresAcceptance` están contemplados y fueron efectivamente devueltos por la release actual.

**Comportamiento de móvil:**

1. Obtener el aviso completo con E015.
2. Mostrar `content` (o presentación fiel del texto disponible) con desplazamiento vertical, versión y fecha.
3. Si `requiresAcceptance=true`, permitir una decisión explícita mediante E017, sin marcar aceptación por cargar la pantalla.
4. No usar un checkbox preseleccionado para finalidades opcionales.
5. La **página pública** de privacidad del equipo es para consulta web/Google Play; el contenido consumible por Android llega por E015.
6. Si cambia `version`, considerar la aceptación previa como histórica y evaluar una nueva decisión según las reglas del producto.

**Errores:** 400, 429, 500, 503. Si está temporalmente indisponible, no afirmar al usuario que ha aceptado una versión desconocida.

### E016 — GET `/users/me/consents` — Historial de consentimientos

**Pantallas:** Privacidad > Historial, configuración de permisos. **JWT.**

**Query:** `limit` entero 1–100 (por defecto 25); `cursor` opcional, opaco, de 8–2048 caracteres según el contrato. Ejemplo: `GET /users/me/consents?limit=25`.

```json
{
  "items": [
    {
      "id": "018f90ef-08e5-7a68-b09f-2b67a47c92f0",
      "noticeVersion": "2026-11-v2",
      "purpose": "privacyNotice",
      "granted": true,
      "recordedAt": "2026-11-18T17:00:00Z"
    }
  ],
  "nextCursor": "opaque.cursor.string"
}
```

`items` es obligatorio; `nextCursor` es opcional y puede faltar cuando no hay más elementos. Cada `Consent` contiene obligatoriamente `id`, `noticeVersion`, `purpose`, `granted` y `recordedAt`. **Enum `purpose`:** `privacyNotice`, `notifications`, `analytics`. **No confundir `granted=false` con no tener registros.**

**Paginación Android:** mientras venga un `nextCursor` válido, solicitar la siguiente página. No inventar `offset`, no descifrar ni modificar cursor. No sobrescribir el historial con sólo el último elemento.

**Errores:** 400, 401, 403, 404, 429, 500, 503.

### E017 — POST `/users/me/consents` — Registrar una decisión

**Pantallas:** aceptación del aviso, opciones de notificaciones/analítica. **JWT.**

```json
{
  "noticeVersion": "2026-11-v2",
  "purpose": "privacyNotice",
  "granted": true
}
```

**`ConsentRequest` — los tres campos son obligatorios:**

| Campo | Tipo | Valores |
|---|---|---|
| `noticeVersion` | string | Tomarlo del E015 vigente, no hardcodear versión anterior |
| `purpose` | enum | `privacyNotice`, `notifications`, `analytics` |
| `granted` | boolean | `true` **o** `false` explícito |

**Éxito `201`:** retorna el objeto `Consent` con ID y fecha `recordedAt` proporcionados por el servidor. Es un **registro histórico**, no un booleano local que reemplaza todo el historial.

**Android:** confirmar que la API respondió 201 antes de marcar la decisión como guardada; actualizar E016. No duplicar POST por recomposición de Compose o reintentos ciegos. El permiso Android del sistema para notificaciones o micrófono **no se concede** mediante este endpoint: se administra con las APIs del dispositivo. **Errores:** 400, 401, 403, 404, 409, 422, 429, 500, 503.

### E018 — GET `/users/me/onboarding` — Consultar progreso

**Pantallas:** asistente inicial, continuar configuración después de cerrar la app. **JWT.**

```json
{
  "completedSteps": ["profile", "income"],
  "done": false,
  "version": 2,
  "microphone": false,
  "notifications": true,
  "configured": true,
  "completedAt": null
}
```

**Obligatorios según schema:** `completedSteps` (array de strings), `done` (boolean), `version` (integer >=1). **Contemplados como opcionales:** `microphone`, `notifications`, `configured` (boolean) y `completedAt` (date-time o null). En el esquema los identificadores de pasos no contienen un enum cerrado: conservar los valores que devuelva el servidor y coordinar la lista con las pantallas de Android.

**Android:** preferir el estado persistido del backend sobre banderas locales. No inferir `done=true` simplemente porque se llegó visualmente a la última pantalla. **Errores:** 400, 401, 403, 404, 429, 500, 503.

### E019 — PATCH `/users/me/onboarding` — Actualizar progreso

**Pantallas:** botón Continuar / Finalizar configuración. **JWT**, body JSON.

```json
{
  "completedSteps": ["profile", "income"],
  "microphone": false,
  "notifications": true,
  "configured": true
}
```

**`OnboardingPatch`:** `completedSteps` **obligatorio**; `microphone`, `notifications`, `configured` opcionales. **No enviar** `done`, `version` o `completedAt` como campos editables si el contrato actual no los habilita. Los booleanos `false` tienen significado: no descartarlos al serializar.

**Éxito `200`:** responde `Onboarding` actualizado. La API valida transiciones y persiste el estado. En el contrato base E019 no declara un header `If-Match` obligatorio (a diferencia de E013/E025/E069); no inventarlo como requisito del cliente. **Errores:** 400, 401, 403, 404, 409, 422, 429, 500, 503.

**Nota de UX:** los pasos de permisos del SO son distintos de las preferencias o el consentimiento legal del backend. Si Android deniega permisos, reflejar el estado realmente conocido; no enviar `true` ficticio.

### E022 — GET `/catalogs/categories` — Catálogo de categorías

**Pantallas:** selectores de categoría y futuras pantallas de movimientos/presupuesto. **Público.** Sin body ni query declarada.

```json
{
  "items": [
    { "id": "food", "name": "Alimentación", "type": "expense" }
  ]
}
```

Cada `Category` tiene `id`, `name`, `type`. El ID se ajusta al patrón `^[a-z][a-z0-9-]{0,39}$`; enum `type`: `income`, `expense`, `saving`. El servidor reportó **7 categorías** en la verificación productiva. El ejemplo no pretende listar las siete ni fijar sus IDs: **pintar las opciones a partir de `items`**, no de una lista hardcodeada.

**Android:** usar `id` como valor enviado a futuros endpoints y `name` como etiqueta. No guardar etiquetas localizadas como clave de negocio. **Errores:** 400, 429, 500, 503.

### E023 — GET `/catalogs/frequencies` — Frecuencias admitidas

**Pantallas:** configuración de ingreso, recurrencias y selecciones de periodicidad. **Público.**

```json
{
  "items": [
    { "id": "weekly", "label": "Semanal" },
    { "id": "monthly", "label": "Mensual" }
  ]
}
```

El servidor reportó **7 frecuencias canónicas**, conforme a RFC-020 v1.1.0:

| Identificador canónico | Uso en Android |
|---|---|
| `weekly` | Semanal |
| `fortnightly` | Quincenal |
| `monthly` | Mensual |
| `bimonthly` | Bimestral |
| `quarterly` | Trimestral |
| `semiannual` | Semestral |
| `annual` | Anual |

**Compatibilidad autorizada:** `biweekly` y `yearly` son alias heredados admitidos por el contrato de escritura E025 y normalizados a los canónicos; **E023 devuelve los siete canónicos**, no los dos alias. La copia H06 antigua del OpenAPI mostraba sólo cuatro valores: para Android debe prevalecer **RFC-020 + OpenAPI de `main`**. Usar `id` de la API y `label` para la UI. **Errores:** 400, 429, 500, 503.

### E024 — GET `/income` — Consultar ingreso base

**Pantallas:** resumen financiero y configuración de ingreso. **JWT.** Sin body.

```json
{
  "amount": "12500.00",
  "currency": "MXN",
  "frequency": "monthly",
  "version": 1
}
```

`amount` es **string decimal con dos decimales**, nunca `Double` en transporte; patrón base `^(0|[1-9][0-9]{0,11})\.[0-9]{2}$`. `currency` actualmente `MXN`; `frequency` canónica; `version` entero >= 1. El ingreso base es distinto de un movimiento financiero contabilizado.

**Android:** parsear `amount` con `BigDecimal`; dar formato monetario para pantalla sin perder precisión. Conservar `ETag` para E025. Se verificó lectura real de ingreso previo a la oleada y lectura posterior a una actualización. **Errores:** 400, 401, 403, 404, 429, 500, 503.

### E025 — PUT `/income` — Actualizar ingreso base

**Pantallas:** formulario Editar ingreso. **JWT + `If-Match` obligatorios.**

```http
PUT /api/v1/income
Authorization: Bearer <accessToken>
If-Match: "v1"
Content-Type: application/json

{"amount":"13250.00","currency":"MXN","frequency":"fortnightly"}
```

**`IncomeUpdate`:** los tres campos son obligatorios: `amount` (decimal string con 2 decimales), `currency` (`MXN`) y `frequency` (7 canónicas + 2 alias admitidos por RFC-020). El backend normaliza los alias antes de persistir. **No enviar** `version` como sustituto de `If-Match` y no mandar número JSON como `13250.00`: se verificó que el float numérico se rechaza con 400.

**Éxito `200`:** retorna `Income` con nueva `version` y frecuencia canónica. La persistencia usa actualización condicionada en DynamoDB (CAS). El cliente debe tratar 412 como conflicto real, no como error genérico. **Errores:** 400, 401, 403, 404, 409, **412**, 422, **428**, 429, 500, 503.

**Recomendación Android:** al hacer PUT mostrar indicador de guardado, deshabilitar doble toque y volver a calcular la presentación con el JSON regresado; no usar `Float/Double` para importes.

### E068 — GET `/users/me/preferences` — Leer preferencias

**Pantallas:** Ajustes, idioma, zona horaria. **JWT.**

```json
{
  "locale": "es-MX",
  "timezone": "America/Mexico_City",
  "version": 1
}
```

Campos obligatorios: `locale`, `timezone`, `version`. Enum `locale`: `es-MX`, `en-US`. `timezone` es string IANA (3–64 caracteres). El OpenAPI base también describe la propiedad opcional `timeZone` además de `timezone`; por ello, **aceptar su presencia sin inferir que ambas pueden escribirse indistintamente**, y contrastar el JSON actual con el cliente generado desde `main`.

**Android:** conservar `timezone` de forma independiente del ajuste de zona horaria del dispositivo si el usuario la personaliza. Guardar ETag para E069. **Errores:** 400, 401, 403, 404, 429, 500, 503.

### E069 — PATCH `/users/me/preferences` — Actualizar preferencias

**Pantallas:** Guardar idioma/zona horaria. **JWT + `If-Match` obligatorios.**

```http
PATCH /api/v1/users/me/preferences
Authorization: Bearer <accessToken>
If-Match: "v1"
Content-Type: application/json

{"locale":"es-MX","timezone":"America/Mexico_City"}
```

**`UserPreferencesPatch`:** `locale`, `timezone` opcionales individualmente; modelo base también contempla `timeZone` opcional. Para nuevas integraciones, utilizar la grafía `timezone` del contrato principal y comprobar en `main` antes de adoptar una alias. Enviar únicamente campos que el usuario decidió editar.

**Éxito `200`:** `UserPreferences` actualizado con versión. Se probó persistencia y casos 428/412. **Errores:** 400, 401, 403, 404, 409, **412**, 422, **428**, 429, 500, 503.

### E070 — GET `/help/faq` — Preguntas frecuentes

**Pantallas:** Centro de ayuda, sección de preguntas frecuentes. **Público.**

```json
{
  "items": [
    { "id": "faq-01", "question": "¿Cómo registro un movimiento?", "answer": "Usa la función correspondiente cuando esté disponible." }
  ]
}
```

`PageOfFaq`: `items` obligatorio, `nextCursor` opcional en schema. Cada elemento exige `id`, `question`, `answer`. En AWS se verificaron **10 FAQ**, pero **no se certifica aquí cada texto**. Mostrar contenido exacto devuelto; no inventar rutas de soporte ni funciones no publicadas. **Errores:** 400, 429, 500, 503.

### E071 — GET `/education/tips` — Consejos educativos

**Pantallas:** Inicio, tarjetas informativas, Educación financiera. **Público.**

```json
{
  "items": [
    { "id": "tip-01", "title": "Presupuesto responsable", "text": "Revisa tus gastos frecuentes." }
  ]
}
```

`PageOfTip`: `items` obligatorio, `nextCursor` opcional. Cada `Tip` exige `id`, `title`, `text`. En AWS se verificaron **8 consejos**. Son contenidos educativos; no tratarlos como recomendaciones personalizadas ni promesas de rendimiento. **Errores:** 400, 429, 500, 503.

---

## 4. Flujos funcionales para conectar pantallas

### 4.1 Arranque tras login

1. E002 `POST /auth/login` (ya disponible anteriormente): guardar el `accessToken` de forma protegida.
2. En paralelo, cargar E012 (perfil), E018 (onboarding), E024 (ingreso) y E068 (preferencias).
3. Cargar E022 y E023 como catálogos públicos; se pueden recuperar aun sin sesión.
4. Cargar E015 para mostrar versión vigente del aviso y E016 para conocer el historial de consentimientos.
5. Si corresponde presentar aceptación/negativa nueva, guardar una decisión E017 **sólo al interactuar el usuario**.
6. Renderizar Home cuando haya datos suficientes; un fallo de FAQ o de consejos no debe bloquear el login.

### 4.2 Editar perfil e ingreso

```text
GET E012 ──> editar nombre ──> PATCH E013 + If-Match ──> UserProfile(version nueva)
GET E024 ──> editar ingreso ──> PUT   E025 + If-Match ──> Income(version nueva)
```

No usar el mismo ETag de perfil para ingreso; son **recursos versionados distintos**.

### 4.3 Privacidad y consentimiento

```text
GET E015 (version actual)
       ↓
Mostrar aviso íntegro en Android
       ↓
Usuario elige explícitamente aceptar / rechazar una finalidad
       ↓
POST E017 (noticeVersion, purpose, granted)
       ↓
GET E016 (historial / confirmación)
```

`granted=false` debe guardarse como decisión explícita cuando proceda. Una pantalla visitada no implica consentimiento.

### 4.4 Onboarding

```text
GET E018 → leer completedSteps/done/version
      ↓
Usuario realiza un paso en la app
      ↓
PATCH E019 → enviar completedSteps y flags conocidos
      ↓
Actualizar UI con la respuesta del servidor
```

No modificar localmente `done` ni `completedAt` como si fueran campos aceptados en el PATCH.

### 4.5 Preferencias y contenido

E068 → E069 para preferencias, ambos privados; E070 y E071 para contenido público. No hace falta bloquear navegación por una descarga fallida de contenido educativo.

---

## 5. Ejemplos Kotlin / Retrofit listos para adaptar

Este código usa Retrofit 2, OkHttp y Gson (o un convertidor compatible). Los DTO son orientativos **según los campos documentados**, y deben contrastarse con el OpenAPI vigente del backend; no añaden rutas inexistentes.

### 5.1 DTO de respuesta y paginación

```kotlin
data class UserProfileDto(
    val id: String,
    val email: String,
    val displayName: String,
    val emailVerified: Boolean,
    val createdAt: String,
    val version: Long
)

data class ProfilePatchDto(val displayName: String)

data class PrivacyNoticeDto(
    val version: String,
    val effectiveAt: String,
    val text: String,
    val url: String,
    val content: String? = null,
    val requiresAcceptance: Boolean? = null
)

data class PageDto<T>(val items: List<T>, val nextCursor: String? = null)

data class ConsentDto(
    val id: String,
    val noticeVersion: String,
    val purpose: String,
    val granted: Boolean,
    val recordedAt: String
)

data class ConsentRequestDto(
    val noticeVersion: String,
    val purpose: String,
    val granted: Boolean
)

data class OnboardingDto(
    val completedSteps: List<String>,
    val done: Boolean,
    val version: Long,
    val microphone: Boolean? = null,
    val notifications: Boolean? = null,
    val configured: Boolean? = null,
    val completedAt: String? = null
)

data class OnboardingPatchDto(
    val completedSteps: List<String>,
    val microphone: Boolean? = null,
    val notifications: Boolean? = null,
    val configured: Boolean? = null
)

data class CategoryDto(val id: String, val name: String, val type: String)
data class FrequencyDto(val id: String, val label: String)

data class IncomeDto(
    val amount: String,
    val currency: String,
    val frequency: String,
    val version: Long
)

data class IncomeUpdateDto(
    val amount: String,
    val currency: String = "MXN",
    val frequency: String
)

data class UserPreferencesDto(
    val locale: String,
    val timezone: String,
    val version: Long,
    val timeZone: String? = null
)

data class UserPreferencesPatchDto(
    val locale: String? = null,
    val timezone: String? = null
)

data class FaqDto(val id: String, val question: String, val answer: String)
data class TipDto(val id: String, val title: String, val text: String)
```

**Importante en PATCH:** con Gson, los campos nulos suelen omitirse según configuración; si el contrato distingue `null` y **ausencia**, usar un serializador explícito compatible. Para `OnboardingPatch`, `completedSteps` no puede omitirse. No inferir que `null` borra un campo si el contrato no lo establece.

### 5.2 Interfaz Retrofit

```kotlin
interface NexumWave1Api {
    @GET("users/me")
    suspend fun getProfile(): retrofit2.Response<UserProfileDto>

    @PATCH("users/me")
    suspend fun patchProfile(
        @Header("If-Match") etag: String,
        @Body body: ProfilePatchDto
    ): retrofit2.Response<UserProfileDto>

    @GET("privacy/notices/current")
    suspend fun getCurrentPrivacyNotice(): PrivacyNoticeDto

    @GET("users/me/consents")
    suspend fun getConsents(
        @Query("limit") limit: Int = 25,
        @Query("cursor") cursor: String? = null
    ): PageDto<ConsentDto>

    @POST("users/me/consents")
    suspend fun createConsent(@Body body: ConsentRequestDto): ConsentDto

    @GET("users/me/onboarding")
    suspend fun getOnboarding(): OnboardingDto

    @PATCH("users/me/onboarding")
    suspend fun patchOnboarding(@Body body: OnboardingPatchDto): OnboardingDto

    @GET("catalogs/categories")
    suspend fun getCategories(): PageDto<CategoryDto>

    @GET("catalogs/frequencies")
    suspend fun getFrequencies(): PageDto<FrequencyDto>

    @GET("income")
    suspend fun getIncome(): retrofit2.Response<IncomeDto>

    @PUT("income")
    suspend fun putIncome(
        @Header("If-Match") etag: String,
        @Body body: IncomeUpdateDto
    ): retrofit2.Response<IncomeDto>

    @GET("users/me/preferences")
    suspend fun getPreferences(): retrofit2.Response<UserPreferencesDto>

    @PATCH("users/me/preferences")
    suspend fun patchPreferences(
        @Header("If-Match") etag: String,
        @Body body: UserPreferencesPatchDto
    ): retrofit2.Response<UserPreferencesDto>

    @GET("help/faq")
    suspend fun getFaq(): PageDto<FaqDto>

    @GET("education/tips")
    suspend fun getTips(): PageDto<TipDto>
}
```

**Por qué algunos métodos retornan `Response<T>`:** para acceder al header `ETag` (`response.headers()["ETag"]`) junto al JSON y manejar 412/428. Puede usarse `Response<T>` uniformemente en todos los métodos si el proyecto lo prefiere. Si el backend no expusiera el header en alguna lectura, registrar el hallazgo y coordinar con el equipo backend; no inventar otra cabecera.

### 5.3 Interceptor de token y cliente

```kotlin
class AccessTokenInterceptor(
    private val accessTokenProvider: () -> String?
) : okhttp3.Interceptor {
    override fun intercept(chain: okhttp3.Interceptor.Chain): okhttp3.Response {
        val request = chain.request()
        val token = accessTokenProvider()
        val withHeaders = request.newBuilder()
            .header("Accept", "application/json")
            .apply {
                if (!token.isNullOrBlank() && request.header("Authorization") == null) {
                    header("Authorization", "Bearer $token")
                }
            }
            .build()
        return chain.proceed(withHeaders)
    }
}

val client = okhttp3.OkHttpClient.Builder()
    .addInterceptor(AccessTokenInterceptor { sessionStore.currentAccessToken() })
    .build()

val retrofit = retrofit2.Retrofit.Builder()
    .baseUrl("https://d2lpmqy5w6x9es.cloudfront.net/api/v1/")
    .client(client)
    .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
    .build()

val api = retrofit.create(NexumWave1Api::class.java)
```

`sessionStore` es un componente de ejemplo que debe vincularse con el sistema de sesión que ya usa el equipo móvil. **No guardar tokens en un objeto global persistido en texto plano.** Para E002/E003 manejar renovación con un `Authenticator` o una capa de repositorio que evite múltiples refresh simultáneos; no reutilizar refresh tokens después de rotarlos.

### 5.4 Lectura y actualización segura usando ETag

```kotlin
val result = api.getIncome()
if (result.isSuccessful) {
    val income = requireNotNull(result.body())
    val etag = result.headers()["ETag"]
        ?: error("Falta ETag: coordinar con backend antes de editar")

    val response = api.putIncome(
        etag = etag,
        body = IncomeUpdateDto(
            amount = java.math.BigDecimal("12500.00").setScale(2).toPlainString(),
            frequency = "monthly"
        )
    )
    when (response.code()) {
        200 -> { /* Actualizar UI y ETag del recurso */ }
        412 -> { /* Otro dispositivo cambió el ingreso; recargar E024 */ }
        428 -> { /* Falta If-Match; revisar interceptor y cabecera */ }
        else -> { /* Parsear ErrorEnvelope y mostrar error útil */ }
    }
}
```

**No copiar literalmente la excepción `error(...)` a la UX final:** es un indicador de integración durante desarrollo; en producción mostrar un error recuperable. Para importes usar `BigDecimal`, no `Double`.

---

## 6. Checklist de aceptación Android

- [ ] `BASE_URL` HTTPS correcta, sin duplicar `/api/v1`.
- [ ] E002 permite iniciar sesión; acceso Bearer funciona en todos los endpoints privados.
- [ ] E012 carga perfil; E013 edita `displayName` y actualiza versión.
- [ ] E015 muestra `content` real, versión y fecha; no hardcodea un aviso antiguo.
- [ ] E017 registra decisiones `granted=true/false`; E016 permite releerlas y paginarlas.
- [ ] E018 recupera el progreso guardado; E019 persiste los pasos y flags reales.
- [ ] E022 presenta IDs y nombres reales de categorías (7 en la release verificada).
- [ ] E023 presenta siete frecuencias canónicas; E025 acepta sólo valores contractuales.
- [ ] E024 muestra un `amount` decimal string; E025 actualiza con `If-Match` y confirma GET posterior.
- [ ] E068 y E069 conservan preferencias y manejan conflictos de versión.
- [ ] E070 muestra 10 FAQ y E071 muestra 8 tips en la release verificada.
- [ ] Manejo de 401 / expiración JWT / refresh E003 sin loops.
- [ ] Manejo de 412 / 428 sin perder ediciones del usuario.
- [ ] Tratamiento de 429 y fallos temporales de red; no enviar POST repetidos por recomposición.
- [ ] Pruebas con un usuario nuevo y uno existente; persistencia después de cerrar y reabrir la app.
- [ ] Tokens, contraseñas y datos financieros fuera de logs y capturas públicas.
- [ ] E014 no se muestra como eliminación funcional ya publicada. Coordinación pendiente con backend.

### Pruebas de integración sugeridas (móvil)

| Flujo | Pasos | Resultado esperado |
|---|---|---|
| Login + perfil | E002 → E012 → E013 → E012 | Se conserva identidad; nombre cambiado sólo en cuenta propia |
| Privacidad | E015 → E017 → E016 | Versión correcta y decisión persistida |
| Onboarding | E018 → E019 → reiniciar app → E018 | Progreso persistente |
| Ingreso | E024 → E025 → E024 | Importe exacto, frecuencia canónica, versión nueva |
| Preferencias | E068 → E069 → E068 | `locale` / `timezone` guardados |
| Conflictos | GET → actualizar en otra sesión → PATCH/PUT con ETag viejo | 412 y recuperación controlada |
| Sesión | E002 → uso → E003 refresh → uso → E004 logout | Tokens rotados y sesión revocada |
| Contenido público | E022/E023/E070/E071 sin JWT | Se muestra contenido real |

## 7. Limitaciones conocidas que hay que comunicar al equipo

1. **E014 pendiente:** `DELETE /api/v1/users/me` (eliminar cuenta) no forma parte de esta oleada ni está aún publicado. E004 sólo cierra sesión.
2. **SES sandbox:** E005/E010 pueden aceptar la solicitud o producir resultados contractuales, pero **no está habilitado el envío a cualquier correo de usuarios**. No anunciar recuperación/verificación universal hasta salir del sandbox.
3. **E006/E011:** su flujo positivo se validó en `v0.15.0`; la release `v0.16.1` conservó pruebas negativas y regresión crítica, pero no se repitió el flujo positivo completo por correo en esa release. No equivaler esta limitación de evidencia a 404 o caída del endpoint.
4. **Catálogo de frecuencias:** RFC-020 modificó H06. No generar el cliente desde el JSON antiguo del ZIP: usar el OpenAPI final de `main`.
5. **Disponibilidad:** en el cierre, el ASG AWS estaba en 2/2/2 con dos targets saludables; ello no garantiza disponibilidad absoluta ni redundancia entre zonas (las dos instancias estaban en `us-east-2a`).
6. **No hay portal funcional web:** Android debe consumir directamente el backend; la página de privacidad de Google Play es un recurso público separado para lectura humana.

## 8. Fuentes y trazabilidad

- OpenAPI/SDD disponible en el ZIP del backend compartido antes de la Oleada 1: `sdd/03-endpoints/openapi/nexum-api-v1.json` y 15 archivos `sdd/03-endpoints/E*/spec.md`.
- Contrato rector actual: [OpenAPI `main`](https://github.com/nexumpr/Nexum-Backend/blob/main/sdd/03-endpoints/openapi/nexum-api-v1.yaml).
- Resultados de producción y cierre de H08-E según reporte de OpenCode del 18/11/2026: `v0.16.1+615ac1c734f2`, quince nuevos con suite 29/29 PASS, cuenta existente 7/7 PASS, PR #99 MERGED, CI main SUCCESS.
- RFC-020 v1.1.0: siete frecuencias canónicas + dos alias heredados en escritura; la versión H06 antigua no refleja esa enmienda.
- Ejemplos JSON en este documento: **sintéticos**, aptos para orientar integración, no fixtures contractuales canónicos ni valores personales.

**Entrega:** esta guía cubre exclusivamente los quince endpoints de Oleada 1. Para implementación automática de DTOs y validaciones, generar o validar los modelos **contra el OpenAPI actualizado de `main`** y no contra este documento estático.
