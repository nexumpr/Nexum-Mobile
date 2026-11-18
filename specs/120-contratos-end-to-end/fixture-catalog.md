---
project: Nexum-Mobile
unit: 120-contratos-end-to-end
kind: fixture-catalog
version: 1.0.0
status: sintetico_verificado
---

# Catálogo de fixtures sintéticas

## Reglas

- Todas las fixtures viven en `app/src/test/resources/contract/`.
- No contienen PII, tokens de producción, credenciales, importes financieros reales ni datos bancarios.
- Los identificadores usan prefijos `synthetic_`, `tr_synthetic_` o dominios reservados `.invalid`.
- Se usan únicamente en pruebas JVM locales con un simulador HTTP en memoria; nunca contra Backend real.

## Inventario

| Archivo | Escenario | Uso principal |
| --- | --- | --- |
| `health_success.json` | `200` con `status` y fecha ISO-8601 | Éxito base y conservación de fecha como texto |
| `session_refresh_success.json` | `200` con tokens sintéticos y `expiresIn` | Renovación de sesión |
| `metadata_success.json` | `200` con versión técnica | Metadatos técnicos |
| `unknown_fields_success.json` | `200` con campo futuro desconocido | Tolerancia a evolución no *breaking* |
| `register_success.json` | `201` con identificadores sintéticos | Cabeceras y serialización de registro |
| `error_400.json` | `400` con sobre canónico | Validación |
| `error_401.json` | `401` con código de sesión expirada | Refresh |
| `error_403.json` | `403` | Aborto |
| `error_404.json` | `404` | Aborto |
| `error_409.json` | `409` | Conflicto / idempotencia |
| `error_422.json` | `422` con `details` | Campos inválidos |
| `error_429.json` | `429` con `Retry-After: 30` | Rate limit |
| `error_500.json` | `500` | Error transient del servidor |
| `error_503.json` | `503` | Servicio no disponible |
| `malformed.json` | JSON válido con tipos incompatibles | `MalformedResponse` |

## Códigos sintéticos

Los campos `code` usan valores de prueba como `VALIDATION_FAILED`, `SESSION_EXPIRED`, `RATE_LIMITED` y `SERVICE_UNAVAILABLE`. Deben contrastarse con el catálogo real de Backend Go antes de cualquier integración; este catálogo no lo sustituye.
