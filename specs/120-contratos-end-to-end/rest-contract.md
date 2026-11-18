---
project: Nexum-Mobile
unit: 120-contratos-end-to-end
kind: rest-contract
version: 1.0.0
status: documentado_pendiente_de_backend
---

# Contrato técnico REST/JSON Android ↔ Go

## Trazabilidad

| Elemento | Valor |
| --- | --- |
| Requisito | `M120-R01`: versiones y errores HTTP interpretados de forma consistente con Backend. |
| Plan | `N120-P01`. |
| Tarea | `N120-T01`. |
| Estado | Documento propuesto; **no constituye aprobación de Backend Go**. No se implementaron endpoints reales. |

## Contrato propuesto

| Aspecto | Regla propuesta | Estado en el cliente hoy | Acción requerida |
| --- | --- | --- | --- |
| Base de rutas | `/api/v1/` | Fijada en `NetworkModule.kt:9` | Confirmar versión con Go; evaluar configuración por entorno |
| `Accept` | `application/json` | `AuthApiService.kt:16,26` | Extender de forma consistente a refresh/logout |
| `Content-Type` | `application/json` para cuerpos JSON | `AuthApiService.kt:17,27` | Confirmar en todas las operaciones con cuerpo |
| `Authorization` | `Bearer <accessToken>` en operaciones autenticadas | `SessionManager.kt:58` solo logout | Definir interceptor global o anotación por endpoint |
| `Idempotency-Key` | Obligatoria en escrituras idempotentes | `AuthApiService.kt:21` solo registro | Confirmar vigencia y semántica de `409` con Go |
| `X-Request-ID` | ID de correlación por petición, devuelto por Backend | No existe | Aprobar cabecera y su relación con `traceId` |
| Fechas | ISO-8601 UTC, p. ej. `2026-11-17T12:00:00Z` | Solo cadenas en fixtures; sin adaptador `Instant` productivo | Confirmar formato y zona antes de crear adaptadores |
| Nulabilidad | Campos obligatorios explícitos; opcionales documentados | `RegisterResponseDto` los hace todos anulables (`RegisterDtos.kt:38-46`) | Confirmar required/optional con Go |
| Campos desconocidos | Ignorar sin fallar para evolución no *breaking* | Gson por defecto lo ignora (`NetworkModule.kt:17`) | Ratificar política de tolerancia |
| Sobre de error | `{"error":{"code","message","details","traceId"}}` | Existen dos formas incompatibles: `HttpContracts.kt:6-15` (mapa) y `AuthDtos.kt:101-115` (lista `{field,message}`) | Backend debe elegir forma canónica de `details` |
| Códigos | `400,401,403,404,409,422,429,5xx` | Mapeados en `SafeApiCall.kt:38-53` | Confirmar catálogo de `code` con Go |

## Matriz de responsabilidades

| Cambio | Responsable de proponer | Aprobación requerida | Efecto |
| --- | --- | --- | --- |
| Nueva ruta bajo `/api/v1/` | Backend Go | Cliente Android + Go | *Non-breaking* si no altera contratos existentes |
| Nuevo campo opcional en respuesta | Backend Go | Cliente Android | *Non-breaking*; Gson lo ignora hasta modelarlo |
| Cambio de nombre/tipo de campo existente | Cualquiera | Cliente Android + Go | *Breaking*; requiere versión nueva o migración |
| Cambio de forma de `details` o `traceId` | Backend Go | Cliente Android + Go | *Breaking*; sincronizar ambos modelos de error |
| Cambio de `code` semántico | Backend Go | Cliente Android + Go | *Breaking* para UI/reintentos |
| Cambio de `versionCode`/ruta de API | Cualquiera | Responsable de release + Go | *Breaking*; requiere spec y evidencia |
| Cambio de timeouts o cabeceras globales | Cliente Android | Arquitectura Android | Puede afectar compatibilidad; documentar |

## Reglas de interpretación en el cliente

- `2xx` con cuerpo: `ApiResult.Success`; `204`: `Success(Unit)`; `2xx` sin cuerpo distinto de `204`: `MalformedResponse`.
- Errores HTTP: parsear el sobre canónico; si no es parseable, conservar el código HTTP y degradar `errorDetail` a `null` sin exponer cuerpo crudo en logs.
- `429`: tomar `Retry-After` de cabecera (`SafeApiCall.kt:46`); si falta, usar la política de la matriz de reintentos.
- Errores de red: timeout, sin conectividad, TLS y JSON malformado se mantienen separados (`HttpContracts.kt:35-41`).
- Nunca presentar un error como éxito ni inventar valores obligatorios ausentes.

## Pendientes explícitos con Backend Go

1. Forma canónica de `details`: mapa libre o lista de `{field,message}`.
2. Si `traceId` va también en `X-Request-ID` y si debe devolverse igual en la respuesta.
3. Campos obligatorios reales de respuestas de sesión/registro; sustituir defaults inventados en `RetrofitAuthRepositoryApi.kt:63-69`.
4. Semántica de `409` frente a `Idempotency-Key` y recuperación segura.
5. Catálogo cerrado de `code` por código HTTP.
6. Formato de fecha/zona y tolerancia a campos desconocidos.

Este documento solo registra el contrato y sus discrepancias. No modifica el repositorio Go ni autoriza cambios en él.
