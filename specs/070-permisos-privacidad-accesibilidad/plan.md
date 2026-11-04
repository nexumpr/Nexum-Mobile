---
project: Nexum-Mobile
unit: 070-permisos-privacidad-accesibilidad
kind: technical-architecture-plan
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# PLAN — Permisos, privacidad y accesibilidad base

## Estrategia técnica
Definir políticas de permisos Android y UI accesible sin pedir micrófono aún. No seleccionar dependencias/proveedores pendientes sin aprobación. Mantener cambios mínimos, configuraciones por entorno, seguridad por defecto y reversibilidad.

## Orden de trabajo
| Paso | Acción | Requisito rector |
| --- | --- | --- |
| **N070-P01** | Auditar permisos/manifiesto y Network Security Configuration. | M070-R01 |
| **N070-P02** | Definir diseño accesible Material 3 y mensajes de privacidad. | M070-R02 |
| **N070-P03** | Preparar pruebas de permisos rechazados y semántica UI. | M070-R03 |

## Dependencias y decisiones
- Requiere: `030-di-navegacion-ui`.
- Revisión humana necesaria si aparece una dependencia, costo, credencial, proveedor o ajuste de arquitectura no ratificado.
- No introducir Docker local; no modificar otros repositorios.

## Estrategia de pruebas
- **Automáticas o manuales documentadas:** `./gradlew lintDebug`; pruebas Compose en dispositivo/emulador autorizado.
- **Negativas:** validar errores de entrada, fallo de dependencia y protección de datos según la unidad.
- **Regresión:** mantener las verificaciones de unidades previas realmente existentes.
- **Evidencia:** consola, pruebas nuevas, archivos modificados y fallos explícitos.

## Riesgos
No solicitar RECORD_AUDIO ni push sin una funcionalidad autorizada.

## Operación y reversibilidad
Al ejecutar la unidad, describir archivos y configuración antes de editar. Si el resultado no satisface los criterios, revertir los cambios propios de la unidad cuando sea seguro, sin tocar historial ajeno. `push`/despliegue/recursos externos son autorizaciones independientes.
