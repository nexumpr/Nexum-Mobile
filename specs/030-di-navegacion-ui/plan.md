---
project: Nexum-Mobile
unit: 030-di-navegacion-ui
kind: technical-architecture-plan
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# PLAN — Inyección de dependencias y shell UI

## Estrategia técnica
Organizar módulos y navegación técnica sin flujos financieros. No seleccionar dependencias/proveedores pendientes sin aprobación. Mantener cambios mínimos, configuraciones por entorno, seguridad por defecto y reversibilidad.

## Orden de trabajo
| Paso | Acción | Requisito rector |
| --- | --- | --- |
| **N030-P01** | Evaluar DI mínima/manual vs librería bajo criterios de costo/beneficio. | M030-R01 |
| **N030-P02** | Definir shell de navegación Material 3 genérico y estados visuales. | M030-R02 |
| **N030-P03** | Diseñar pruebas de UI y DI con pantallas de demostración no funcionales. | M030-R03 |

## Dependencias y decisiones
- Requiere: `020-nucleo-mvi`.
- Revisión humana necesaria si aparece una dependencia, costo, credencial, proveedor o ajuste de arquitectura no ratificado.
- No introducir Docker local; no modificar otros repositorios.

## Estrategia de pruebas
- **Automáticas o manuales documentadas:** `./gradlew testDebugUnitTest`; pruebas Compose/instrumentadas en emulador si existe.
- **Negativas:** validar errores de entrada, fallo de dependencia y protección de datos según la unidad.
- **Regresión:** mantener las verificaciones de unidades previas realmente existentes.
- **Evidencia:** consola, pruebas nuevas, archivos modificados y fallos explícitos.

## Riesgos
El uso de navegación no autoriza pantallas financieras.

## Operación y reversibilidad
Al ejecutar la unidad, describir archivos y configuración antes de editar. Si el resultado no satisface los criterios, revertir los cambios propios de la unidad cuando sea seguro, sin tocar historial ajeno. `push`/despliegue/recursos externos son autorizaciones independientes.
