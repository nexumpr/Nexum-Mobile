---
project: Nexum-Mobile
unit: 100-ci-versiones-android
kind: technical-architecture-plan
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# PLAN — CI Android y versión reproducible

## Estrategia técnica
Preparar pipeline Android y matriz de versiones, sin publicar Google Play. No seleccionar dependencias/proveedores pendientes sin aprobación. Mantener cambios mínimos, configuraciones por entorno, seguridad por defecto y reversibilidad.

## Orden de trabajo
| Paso | Acción | Requisito rector |
| --- | --- | --- |
| **N100-P01** | Definir versiones y estrategia de release con responsables. | M100-R01 |
| **N100-P02** | Diseñar GitHub Actions de Android sin secretos de firma. | M100-R02 |
| **N100-P03** | Preparar validación de reproducibilidad, cache seguro y reportes. | M100-R03 |

## Dependencias y decisiones
- Requiere: `080-pruebas-mvi-compose`, `090-devsecops-android`.
- Revisión humana necesaria si aparece una dependencia, costo, credencial, proveedor o ajuste de arquitectura no ratificado.
- No introducir Docker local; no modificar otros repositorios.

## Estrategia de pruebas
- **Automáticas o manuales documentadas:** `./gradlew assembleDebug`; `./gradlew testDebugUnitTest`; `./gradlew lintDebug`.
- **Negativas:** validar errores de entrada, fallo de dependencia y protección de datos según la unidad.
- **Regresión:** mantener las verificaciones de unidades previas realmente existentes.
- **Evidencia:** consola, pruebas nuevas, archivos modificados y fallos explícitos.

## Riesgos
No publicar AAB ni subir a Google Play sin permiso.

## Operación y reversibilidad
Al ejecutar la unidad, describir archivos y configuración antes de editar. Si el resultado no satisface los criterios, revertir los cambios propios de la unidad cuando sea seguro, sin tocar historial ajeno. `push`/despliegue/recursos externos son autorizaciones independientes.
