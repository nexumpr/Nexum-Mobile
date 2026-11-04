---
project: Nexum-Mobile
unit: 040-cliente-http-contratos
kind: technical-architecture-plan
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# PLAN — Cliente HTTP y contratos de integración

## Estrategia técnica
Definir adaptadores de red, errores y versionado coherentes con el backend. No seleccionar dependencias/proveedores pendientes sin aprobación. Mantener cambios mínimos, configuraciones por entorno, seguridad por defecto y reversibilidad.

## Orden de trabajo
| Paso | Acción | Requisito rector |
| --- | --- | --- |
| **N040-P01** | Acordar esquemas HTTP/JSON y errores con `Nexum-Backend` sin tocar su repo. | M040-R01 |
| **N040-P02** | Elegir cliente HTTP/serializador con pruebas y justificación. | M040-R02 |
| **N040-P03** | Diseñar manejo de timeout, cancelación, reintentos permitidos y red sin conexión. | M040-R03 |

## Dependencias y decisiones
- Requiere: `020-nucleo-mvi`.
- Revisión humana necesaria si aparece una dependencia, costo, credencial, proveedor o ajuste de arquitectura no ratificado.
- No introducir Docker local; no modificar otros repositorios.

## Estrategia de pruebas
- **Automáticas o manuales documentadas:** `./gradlew testDebugUnitTest`; tests con servidor simulado.
- **Negativas:** validar errores de entrada, fallo de dependencia y protección de datos según la unidad.
- **Regresión:** mantener las verificaciones de unidades previas realmente existentes.
- **Evidencia:** consola, pruebas nuevas, archivos modificados y fallos explícitos.

## Riesgos
Revisión de contrato compartido por los responsables Backend y Mobile.

## Operación y reversibilidad
Al ejecutar la unidad, describir archivos y configuración antes de editar. Si el resultado no satisface los criterios, revertir los cambios propios de la unidad cuando sea seguro, sin tocar historial ajeno. `push`/despliegue/recursos externos son autorizaciones independientes.
