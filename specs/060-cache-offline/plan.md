---
project: Nexum-Mobile
unit: 060-cache-offline
kind: technical-architecture-plan
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# PLAN — Caché local y sincronización controlada

## Estrategia técnica
Preparar datos locales sin representar operaciones pendientes como definitivas. No seleccionar dependencias/proveedores pendientes sin aprobación. Mantener cambios mínimos, configuraciones por entorno, seguridad por defecto y reversibilidad.

## Orden de trabajo
| Paso | Acción | Requisito rector |
| --- | --- | --- |
| **N060-P01** | Definir matriz de caché móvil y política de limpieza/logout. | M060-R01 |
| **N060-P02** | Evaluar SQLite/Room si se aprueba persistencia offline. | M060-R02 |
| **N060-P03** | Acordar sincronización, reconciliación y claves de operación con Backend. | M060-R03 |

## Dependencias y decisiones
- Requiere: `040-cliente-http-contratos`, `050-sesion-credenciales`.
- Revisión humana necesaria si aparece una dependencia, costo, credencial, proveedor o ajuste de arquitectura no ratificado.
- No introducir Docker local; no modificar otros repositorios.

## Estrategia de pruebas
- **Automáticas o manuales documentadas:** `./gradlew testDebugUnitTest`; pruebas aislamiento, expiración y estado offline.
- **Negativas:** validar errores de entrada, fallo de dependencia y protección de datos según la unidad.
- **Regresión:** mantener las verificaciones de unidades previas realmente existentes.
- **Evidencia:** consola, pruebas nuevas, archivos modificados y fallos explícitos.

## Riesgos
No habilitar cola de escrituras reales sin contrato servidor aprobado.

## Operación y reversibilidad
Al ejecutar la unidad, describir archivos y configuración antes de editar. Si el resultado no satisface los criterios, revertir los cambios propios de la unidad cuando sea seguro, sin tocar historial ajeno. `push`/despliegue/recursos externos son autorizaciones independientes.
