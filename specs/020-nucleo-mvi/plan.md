---
project: Nexum-Mobile
unit: 020-nucleo-mvi
kind: technical-architecture-plan
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# PLAN — Núcleo MVI y estado inmutable

## Estrategia técnica
Preparar una arquitectura de presentación MVI desacoplada de componentes financieros. No seleccionar dependencias/proveedores pendientes sin aprobación. Mantener cambios mínimos, configuraciones por entorno, seguridad por defecto y reversibilidad.

## Orden de trabajo
| Paso | Acción | Requisito rector |
| --- | --- | --- |
| **N020-P01** | Definir límites Intention, State, Effect, reducer y coordinador de presentación. | M020-R01 |
| **N020-P02** | Establecer concurrencia y cancelación evitando ejecuciones duplicadas por recomposición. | M020-R02 |
| **N020-P03** | Diseñar pruebas deterministas de transición de estados. | M020-R03 |

## Dependencias y decisiones
- Requiere: `010-base-android`.
- Revisión humana necesaria si aparece una dependencia, costo, credencial, proveedor o ajuste de arquitectura no ratificado.
- No introducir Docker local; no modificar otros repositorios.

## Estrategia de pruebas
- **Automáticas o manuales documentadas:** `./gradlew testDebugUnitTest`; pruebas MVI con estados sintéticos.
- **Negativas:** validar errores de entrada, fallo de dependencia y protección de datos según la unidad.
- **Regresión:** mantener las verificaciones de unidades previas realmente existentes.
- **Evidencia:** consola, pruebas nuevas, archivos modificados y fallos explícitos.

## Riesgos
No copiar MVVM del ejemplo del profesor ni crear pantallas de negocio.

## Operación y reversibilidad
Al ejecutar la unidad, describir archivos y configuración antes de editar. Si el resultado no satisface los criterios, revertir los cambios propios de la unidad cuando sea seguro, sin tocar historial ajeno. `push`/despliegue/recursos externos son autorizaciones independientes.
