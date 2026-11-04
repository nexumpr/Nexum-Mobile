---
project: Nexum-Mobile
unit: 080-pruebas-mvi-compose
kind: technical-architecture-plan
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# PLAN — Pruebas de estado, UI e integración aislada

## Estrategia técnica
Establecer pruebas reales para el cliente sin usar datos financieros verdaderos. No seleccionar dependencias/proveedores pendientes sin aprobación. Mantener cambios mínimos, configuraciones por entorno, seguridad por defecto y reversibilidad.

## Orden de trabajo
| Paso | Acción | Requisito rector |
| --- | --- | --- |
| **N080-P01** | Mapear requisitos técnicos a tests y aislar dependencias. | M080-R01 |
| **N080-P02** | Definir pruebas unitarias vs instrumentadas y fixtures ficticias. | M080-R02 |
| **N080-P03** | Establecer evidencia reproducible con versión de emulador/dispositivo. | M080-R03 |

## Dependencias y decisiones
- Requiere: `020-nucleo-mvi`, `030-di-navegacion-ui`.
- Revisión humana necesaria si aparece una dependencia, costo, credencial, proveedor o ajuste de arquitectura no ratificado.
- No introducir Docker local; no modificar otros repositorios.

## Estrategia de pruebas
- **Automáticas o manuales documentadas:** `./gradlew testDebugUnitTest`; `./gradlew connectedDebugAndroidTest` solo si hay dispositivo/emulador.
- **Negativas:** validar errores de entrada, fallo de dependencia y protección de datos según la unidad.
- **Regresión:** mantener las verificaciones de unidades previas realmente existentes.
- **Evidencia:** consola, pruebas nuevas, archivos modificados y fallos explícitos.

## Riesgos
No reportar cobertura sin medición.

## Operación y reversibilidad
Al ejecutar la unidad, describir archivos y configuración antes de editar. Si el resultado no satisface los criterios, revertir los cambios propios de la unidad cuando sea seguro, sin tocar historial ajeno. `push`/despliegue/recursos externos son autorizaciones independientes.
