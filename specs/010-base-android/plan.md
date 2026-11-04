---
project: Nexum-Mobile
unit: 010-base-android
kind: technical-architecture-plan
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# PLAN — Base Android y compilación reproducible

## Estrategia técnica
Validar la aplicación Compose creada en Android Studio sin pantallas financieras. No seleccionar dependencias/proveedores pendientes sin aprobación. Mantener cambios mínimos, configuraciones por entorno, seguridad por defecto y reversibilidad.

## Orden de trabajo
| Paso | Acción | Requisito rector |
| --- | --- | --- |
| **N010-P01** | Inventariar Gradle Wrapper, catálogo de versiones y herramientas existentes. | M010-R01 |
| **N010-P02** | Fijar política de versiones Android compatible y sin actualización forzada. | M010-R02 |
| **N010-P03** | Definir pruebas básicas de sincronización/build y `.gitignore`. | M010-R03 |

## Dependencias y decisiones
- Requiere: ninguna unidad anterior.
- Revisión humana necesaria si aparece una dependencia, costo, credencial, proveedor o ajuste de arquitectura no ratificado.
- No introducir Docker local; no modificar otros repositorios.

## Estrategia de pruebas
- **Automáticas o manuales documentadas:** `./gradlew assembleDebug`; `./gradlew testDebugUnitTest`; `./gradlew lintDebug`.
- **Negativas:** validar errores de entrada, fallo de dependencia y protección de datos según la unidad.
- **Regresión:** mantener las verificaciones de unidades previas realmente existentes.
- **Evidencia:** consola, pruebas nuevas, archivos modificados y fallos explícitos.

## Riesgos
Compilación previa exitosa no sustituye una nueva verificación posterior a cambios.

## Operación y reversibilidad
Al ejecutar la unidad, describir archivos y configuración antes de editar. Si el resultado no satisface los criterios, revertir los cambios propios de la unidad cuando sea seguro, sin tocar historial ajeno. `push`/despliegue/recursos externos son autorizaciones independientes.
