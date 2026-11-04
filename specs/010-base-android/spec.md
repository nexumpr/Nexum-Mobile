---
project: Nexum-Mobile
unit: 010-base-android
kind: technical-architecture-spec
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# SPEC — Base Android y compilación reproducible

## Propósito
Validar la aplicación Compose creada en Android Studio sin pantallas financieras.

## Lugar en el plan SDD
- **Constitución:** `.specify/memory/constitution.md` (vigente).
- **Tecnología:** Kotlin / Jetpack Compose / MVI.
- **Dependencias previas:** ninguna unidad anterior.
- **Naturaleza:** **arquitectura técnica**, no especificación funcional de Nexum.
- **Estado:** lista para ser seleccionada individualmente; aún no implementada ni probada.

## Alcance permitido
Se permiten exclusivamente estructuras, contratos, verificaciones y artefactos técnicos que satisfagan estos requisitos. La autorización de esta unidad no alcanza otras especificaciones.

## Exclusiones explícitas
No implementar ingresos, gastos, presupuestos, metas, registro/login de cuentas, voz, recomendaciones, landing ni lógica de finanzas personales; no consumir datos reales; no desplegar o publicar por defecto.

## Requisitos y criterios verificables
| ID | Requisito obligatorio | Aceptación mínima |
| --- | --- | --- |
| **M010-R01** | La combinación Kotlin, AGP, Gradle, Compose y min SDK debe compilar de manera reproducible. | Prueba o revisión objetiva del comportamiento indicado, registrada con evidencia y sin datos reales. |
| **M010-R02** | El proyecto debe mantener configuraciones locales y secretos fuera del repositorio. | Prueba o revisión objetiva del comportamiento indicado, registrada con evidencia y sin datos reales. |
| **M010-R03** | La matriz de versiones y comandos de compilación debe estar documentada. | Prueba o revisión objetiva del comportamiento indicado, registrada con evidencia y sin datos reales. |

## Escenarios de prueba y evidencia
- **Caso favorable:** implementar el componente técnico con entradas sintéticas; verificar que satisface los requisitos anteriores.
- **Caso de fallo:** forzar error o ausencia de configuración; debe fallar de forma controlada y sin exponer secretos.
- **Regresión:** confirmar que el proyecto preexistente sigue compilando y que no se incluyen funcionalidades ajenas.
- **Comandos/medios de comprobación esperados:** `./gradlew assembleDebug`; `./gradlew testDebugUnitTest`; `./gradlew lintDebug`.
- **Evidencia exigida:** comandos con salida real, tests añadidos, diff revisado, resultados fallidos y limitaciones; nunca marcar como aprobado por intuición.

## Riesgos / bloqueos
Compilación previa exitosa no sustituye una nueva verificación posterior a cambios.

## Criterio de salida de la unidad
Todas las tareas de `task.md` tienen resultados o quedan explícitamente pendientes; pruebas de la unidad ejecutadas cuando el entorno lo permite; no existe cambio fuera del alcance; revisión humana del resultado antes de iniciar la siguiente unidad.
