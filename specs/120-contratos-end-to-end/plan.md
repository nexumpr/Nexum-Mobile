---
project: Nexum-Mobile
unit: 120-contratos-end-to-end
kind: technical-architecture-plan
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# PLAN — Integración técnica contractual con Go

## Estrategia técnica
Verificar compatibilidad técnica sin endpoints ni flujos financieros. No seleccionar dependencias/proveedores pendientes sin aprobación. Mantener cambios mínimos, configuraciones por entorno, seguridad por defecto y reversibilidad.

## Orden de trabajo
| Paso | Acción | Requisito rector |
| --- | --- | --- |
| **N120-P01** | Consolidar contrato técnico REST/JSON entre responsables. | M120-R01 |
| **N120-P02** | Definir fixtures sintéticas de error/estado y matriz de reintentos. | M120-R02 |
| **N120-P03** | Preparar estrategia de pruebas contractuales en CI con servidor simulado. | M120-R03 |

## Dependencias y decisiones
- Requiere: `040-cliente-http-contratos`, `050-sesion-credenciales`, `060-cache-offline`.
- Revisión humana necesaria si aparece una dependencia, costo, credencial, proveedor o ajuste de arquitectura no ratificado.
- No introducir Docker local; no modificar otros repositorios.

## Estrategia de pruebas
- **Automáticas o manuales documentadas:** `./gradlew testDebugUnitTest`; pruebas con mock server sin datos reales.
- **Negativas:** validar errores de entrada, fallo de dependencia y protección de datos según la unidad.
- **Regresión:** mantener las verificaciones de unidades previas realmente existentes.
- **Evidencia:** consola, pruebas nuevas, archivos modificados y fallos explícitos.

## Riesgos
Cualquier cambio en Backend requiere revisión y autorización por ese equipo.

## Operación y reversibilidad
Al ejecutar la unidad, describir archivos y configuración antes de editar. Si el resultado no satisface los criterios, revertir los cambios propios de la unidad cuando sea seguro, sin tocar historial ajeno. `push`/despliegue/recursos externos son autorizaciones independientes.
