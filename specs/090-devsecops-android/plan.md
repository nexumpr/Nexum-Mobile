---
project: Nexum-Mobile
unit: 090-devsecops-android
kind: technical-architecture-plan
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# PLAN — Seguridad estática y calidad Android

## Estrategia técnica
Aplicar Lint, auditoría de secretos y riesgos de dependencias proporcionalmente. No seleccionar dependencias/proveedores pendientes sin aprobación. Mantener cambios mínimos, configuraciones por entorno, seguridad por defecto y reversibilidad.

## Orden de trabajo
| Paso | Acción | Requisito rector |
| --- | --- | --- |
| **N090-P01** | Definir gates progresivos para Android Lint, tests y dependencia. | M090-R01 |
| **N090-P02** | Elegir Detekt/SBOM/secret scanning según versión y presupuesto. | M090-R02 |
| **N090-P03** | Proponer matriz de hallazgos, severidad y excepciones. | M090-R03 |

## Dependencias y decisiones
- Requiere: `010-base-android`.
- Revisión humana necesaria si aparece una dependencia, costo, credencial, proveedor o ajuste de arquitectura no ratificado.
- No introducir Docker local; no modificar otros repositorios.

## Estrategia de pruebas
- **Automáticas o manuales documentadas:** `./gradlew lintDebug`; `./gradlew testDebugUnitTest`; Gitleaks si está disponible.
- **Negativas:** validar errores de entrada, fallo de dependencia y protección de datos según la unidad.
- **Regresión:** mantener las verificaciones de unidades previas realmente existentes.
- **Evidencia:** consola, pruebas nuevas, archivos modificados y fallos explícitos.

## Riesgos
No introducir escáneres externos ni keys sin revisión.

## Operación y reversibilidad
Al ejecutar la unidad, describir archivos y configuración antes de editar. Si el resultado no satisface los criterios, revertir los cambios propios de la unidad cuando sea seguro, sin tocar historial ajeno. `push`/despliegue/recursos externos son autorizaciones independientes.
