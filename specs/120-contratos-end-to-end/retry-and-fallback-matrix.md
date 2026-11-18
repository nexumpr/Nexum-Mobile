---
project: Nexum-Mobile
unit: 120-contratos-end-to-end
kind: retry-and-fallback-matrix
version: 1.0.0
status: propuesta_pendiente_de_aprobacion
---

# Matriz de reintentos, sesión y fallback offline

## Trazabilidad

| Elemento | Valor |
| --- | --- |
| Requisito | `M120-R02`: JWT, timeout, idempotencia y conectividad coherentes entre proyectos. |
| Plan | `N120-P02`. |
| Tarea | `N120-T02`. |
| Estado | Política propuesta y ejecutada solo como tabla de decisión en pruebas. **No hay reintentos cableados en producción.** |

## Principios

- No reintentar de forma ciega. Solo se reintenta lo transitorio.
- `401` no se confunde con un error de negocio: se intenta renovar sesión una sola vez.
- La caché de `060` se usa como fallback etiquetado, nunca como confirmación servidora.
- Los errores `4xx` no transitorios se abortan y se registran sin datos sensibles.
- Toda clave de idempotencia existente se reutiliza; no se generan escrituras nuevas desde esta unidad.

## Matriz propuesta

| Situación | Clasificación actual en el cliente | Acción propuesta | Reintento | Fallback / observación |
| --- | --- | --- | --- | --- |
| `200/201/204` | `ApiResult.Success` | Aceptar y actualizar caché si aplica | No | Estado `CONFIRMED` |
| `400 BadRequest` | `HttpErrorType.BadRequest` | Abortar y mostrar validación | No | No cachear como confirmado |
| `401 Unauthorized` | `HttpErrorType.Unauthorized` | Refrescar sesión y repetir una vez | 1 tras refresh | Si falla refresh: limpiar credenciales (fail-secure) |
| `403 Forbidden` | `HttpErrorType.Forbidden` | Abortar; no refrescar | No | Registrar sin PII |
| `404 NotFound` | `HttpErrorType.NotFound` | Abortar | No | No reintento |
| `409 Conflict` | `HttpErrorType.Conflict` | Abortar y reconciliar idempotencia | No | Requiere semántica acordada con Go |
| `422 UnprocessableEntity` | `HttpErrorType.UnprocessableEntity` | Abortar con detalle de validación | No | No reintento |
| `429 RateLimited` | `HttpErrorType.RateLimited` | Esperar `Retry-After` o backoff | Hasta 3 intentos | Jitter; no superar el límite |
| `500..599` | `HttpErrorType.ServerError` | Reintento exponencial con jitter | Hasta 3 intentos | Luego fallback a caché si existe |
| `SocketTimeoutException` | `NetworkErrorType.Timeout` | Reintento exponencial con jitter | Hasta 3 intentos | Luego fallback a caché |
| `UnknownHost/Connect/IO` | `NetworkErrorType.NoConnectivity` | Fallback inmediato a caché | 1 opcional | Exponer estado sin conexión |
| `SSLException` | `NetworkErrorType.SslFailure` | Abortar | No | No degradar TLS |
| `JsonSyntaxException` | `NetworkErrorType.MalformedResponse` | Abortar | No | No reenviar cuerpo inválido |

## Parámetros propuestos, no ratificados

- Intentos máximos: 3 por operación (el inicial más 2 reintentos).
- Backoff: 250 ms, 500 ms, 1000 ms con jitter completo.
- `429`: `Retry-After` tiene prioridad sobre el backoff; si no viene, se aplica la tabla.
- `401`: un refresh y un reintento como máximo; si el refresh falla, logout local fail-secure.
- Sin conexión: fallback a caché con estado `STALE`, nunca `CONFIRMED`.
- Sin `mapping` de error o sobre inválido: el código HTTP manda; `errorDetail` puede ser `null`.

## Idempotencia y caché

- `OfflineCacheManager.enqueueOperation` deduplica por `userId + idempotencyKey` (`OfflineCacheManager.kt:91-123`).
- `recordOperationRetryFailure` incrementa `retryCount` y guarda `lastError` (`:145-152`); no decide reintentos.
- `invalidate` marca `STALE` (`:75-78`); un `Hit` con `STALE` devuelve datos etiquetados.
- No existe drenaje real de la cola hacia Backend. Habilitarlo requiere aprobación de Go.
- No se envía `Idempotency-Key` desde la cola offline en esta unidad.

## Estado de implementación

La tabla anterior se valida en `RetryAndFallbackContractTest` como decisión esperada a partir de `ApiResult`. No existe un componente productivo `RetryPolicy`; por tanto **no se afirma** que la app ya reintente, refresque o haga fallback automáticamente. Esos cambios necesitan mandato y aprobación de arquitectura.
