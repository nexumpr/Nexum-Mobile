---
project: Nexum-Mobile
unit: 030-di-navegacion-ui
kind: technical-architecture-spec
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# SPEC — Inyección de dependencias y shell UI

## Propósito
Organizar módulos y navegación técnica sin flujos financieros.

## Lugar en el plan SDD
- **Constitución:** `.specify/memory/constitution.md` (vigente).
- **Tecnología:** Kotlin / Jetpack Compose / MVI.
- **Dependencias previas:** `020-nucleo-mvi`.
- **Naturaleza:** **arquitectura técnica**, no especificación funcional de Nexum.
- **Estado:** lista para ser seleccionada individualmente; aún no implementada ni probada.

## Alcance permitido
Se permiten exclusivamente estructuras, contratos, verificaciones y artefactos técnicos que satisfagan estos requisitos. La autorización de esta unidad no alcanza otras especificaciones.

## Exclusiones explícitas
No implementar ingresos, gastos, presupuestos, metas, registro/login de cuentas, voz, recomendaciones, landing ni lógica de finanzas personales; no consumir datos reales; no desplegar o publicar por defecto.

## Requisitos y criterios verificables
| ID | Requisito obligatorio | Aceptación mínima |
| --- | --- | --- |
| **M030-R01** | Las dependencias de datos deben sustituirse por dobles en pruebas. | Prueba o revisión objetiva del comportamiento indicado, registrada con evidencia y sin datos reales. |
| **M030-R02** | La navegación y UI base deben separarse de casos de uso. | Prueba o revisión objetiva del comportamiento indicado, registrada con evidencia y sin datos reales. |
| **M030-R03** | Los Composables deben mantenerse accesibles y no contener accesos directos a red. | Prueba o revisión objetiva del comportamiento indicado, registrada con evidencia y sin datos reales. |

## Escenarios de prueba y evidencia
- **Caso favorable:** implementar el componente técnico con entradas sintéticas; verificar que satisface los requisitos anteriores.
- **Caso de fallo:** forzar error o ausencia de configuración; debe fallar de forma controlada y sin exponer secretos.
- **Regresión:** confirmar que el proyecto preexistente sigue compilando y que no se incluyen funcionalidades ajenas.
- **Comandos/medios de comprobación esperados:** `./gradlew testDebugUnitTest`; pruebas Compose/instrumentadas en emulador si existe.
- **Evidencia exigida:** comandos con salida real, tests añadidos, diff revisado, resultados fallidos y limitaciones; nunca marcar como aprobado por intuición.

## Riesgos / bloqueos
El uso de navegación no autoriza pantallas financieras.

## Criterio de salida de la unidad
Todas las tareas de `task.md` tienen resultados o quedan explícitamente pendientes; pruebas de la unidad ejecutadas cuando el entorno lo permite; no existe cambio fuera del alcance; revisión humana del resultado antes de iniciar la siguiente unidad.
