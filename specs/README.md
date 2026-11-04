# Unidades SDD de arquitectura base — Nexum-Mobile

**Estado: VIGENTE, ejecutable de manera incremental bajo orden humana.** Constitución: [`.specify/memory/constitution.md`](../.specify/memory/constitution.md). No se implementa todo con un solo prompt.

## Convención
Cada unidad tiene `spec.md` (qué/aceptación), `plan.md` (cómo/orden/riesgos) y `task.md` (acciones, pruebas y evidencia). La unidad `000-arquitectura-base` conserva el marco transversal de la actividad universitaria y **no se ejecuta como un bloque**.

## Ruta sugerida (dependencias obligatorias, no calendario)
| Unidad | Resultado técnico esperado | Depende de |
| --- | --- | --- |
| [`010-base-android`](010-base-android/spec.md) | Base Android y compilación reproducible | Inicio |
| [`020-nucleo-mvi`](020-nucleo-mvi/spec.md) | Núcleo MVI y estado inmutable | `010-base-android` |
| [`030-di-navegacion-ui`](030-di-navegacion-ui/spec.md) | Inyección de dependencias y shell UI | `020-nucleo-mvi` |
| [`040-cliente-http-contratos`](040-cliente-http-contratos/spec.md) | Cliente HTTP y contratos de integración | `020-nucleo-mvi` |
| [`050-sesion-credenciales`](050-sesion-credenciales/spec.md) | Sesión y custodia de credenciales | `040-cliente-http-contratos` |
| [`060-cache-offline`](060-cache-offline/spec.md) | Caché local y sincronización controlada | `040-cliente-http-contratos`, `050-sesion-credenciales` |
| [`070-permisos-privacidad-accesibilidad`](070-permisos-privacidad-accesibilidad/spec.md) | Permisos, privacidad y accesibilidad base | `030-di-navegacion-ui` |
| [`080-pruebas-mvi-compose`](080-pruebas-mvi-compose/spec.md) | Pruebas de estado, UI e integración aislada | `020-nucleo-mvi`, `030-di-navegacion-ui` |
| [`090-devsecops-android`](090-devsecops-android/spec.md) | Seguridad estática y calidad Android | `010-base-android` |
| [`100-ci-versiones-android`](100-ci-versiones-android/spec.md) | CI Android y versión reproducible | `080-pruebas-mvi-compose`, `090-devsecops-android` |
| [`110-distribucion-google-play`](110-distribucion-google-play/spec.md) | Preparación para distribución segura | `100-ci-versiones-android` |
| [`120-contratos-end-to-end`](120-contratos-end-to-end/spec.md) | Integración técnica contractual con Go | `040-cliente-http-contratos`, `050-sesion-credenciales`, `060-cache-offline` |
| [`130-Registro-Usuario`](130-Registro-Usuario/spec.md) | Pantalla y flujo de registro (Sign Up) | `030-di-navegacion-ui`, `040-cliente-http-contratos` |
| [`140-Login`](140-Login/spec.md) | Pantalla y flujo de inicio de sesión | `030-di-navegacion-ui`, `040-cliente-http-contratos`, `050-sesion-credenciales` |

## Regla de uso para OpenCode
1. Abrir OpenCode **dentro de este repositorio**, no desde la carpeta padre.
2. Solicitar una única unidad por identificador, no todas las fases. Usar @phase-implementer cuando esté reconocido o el agente principal con iguales restricciones.
3. Revisar si la unidad exige una decisión aún pendiente; no simular autenticación, costos, pruebas ni despliegues.
4. Probar, reportar y marcar únicamente tareas verificadas. Después detenerse y pedir aprobación para la siguiente unidad.
5. **No realizar push ni despliegue automáticamente.** Los requisitos de producto (ingresos, gastos, presupuestos, metas, voz, ML, etc.) llevarán specs funcionales independientes cuando se autoricen.

## Progreso
- `000-arquitectura-base`: marco SDD documental existente.
- `010-*` a `120-*`: preparados, **ninguna implementación acreditada por la mera existencia de archivos**.
- `130-Registro-Usuario` y `140-Login`: **implementadas y probadas** el 2026-11-04 (evidencia en `memoria.md` §8 y en cada `task.md`); fuente mock sin Backend real.
- Estado de tests: **50 unitarios en verde** (2026-11-04); instrumentación **no ejecutada**.
