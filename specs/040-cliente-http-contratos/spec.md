---
project: Nexum-Mobile
unit: 040-cliente-http-contratos
kind: technical-architecture-spec
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# SPEC — Cliente HTTP y contratos de integración

## Propósito
Definir adaptadores de red, errores y versionado coherentes con el backend.

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
| **M040-R01** | El cliente debe tratar errores HTTP/red sin presentar datos falsamente confirmados. | Prueba o revisión objetiva del comportamiento indicado, registrada con evidencia y sin datos reales. |
| **M040-R02** | La comunicación pública debe usar HTTPS/TLS y configuración por entorno. | Prueba o revisión objetiva del comportamiento indicado, registrada con evidencia y sin datos reales. |
| **M040-R03** | Los DTO/serializadores deben depender de contratos aprobados con Backend. | Prueba o revisión objetiva del comportamiento indicado, registrada con evidencia y sin datos reales. |

## Escenarios de prueba y evidencia
- **Caso favorable:** implementar el componente técnico con entradas sintéticas; verificar que satisface los requisitos anteriores.
- **Caso de fallo:** forzar error o ausencia de configuración; debe fallar de forma controlada y sin exponer secretos.
- **Regresión:** confirmar que el proyecto preexistente sigue compilando y que no se incluyen funcionalidades ajenas.
- **Comandos/medios de comprobación esperados:** `./gradlew testDebugUnitTest`; tests con servidor simulado.
- **Evidencia exigida:** comandos con salida real, tests añadidos, diff revisado, resultados fallidos y limitaciones; nunca marcar como aprobado por intuición.

## Riesgos / bloqueos
Revisión de contrato compartido por los responsables Backend y Mobile.

## Criterio de salida de la unidad
Todas las tareas de `task.md` tienen resultados o quedan explícitamente pendientes; pruebas de la unidad ejecutadas cuando el entorno lo permite; no existe cambio fuera del alcance; revisión humana del resultado antes de iniciar la siguiente unidad.
