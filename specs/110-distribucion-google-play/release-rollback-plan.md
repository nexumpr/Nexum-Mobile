---
project: Nexum-Mobile
unit: 110-distribucion-google-play
kind: release-rollback-plan
version: 1.0.0
status: documentado_pendiente_de_ratificacion
---

# Flujo de release y plan de rollback/retirada

## Trazabilidad

| Elemento | Valor |
| --- | --- |
| Requisito | `M110-R03`: aprobación y trazabilidad de versiones. |
| Plan | `N110-P03`: acordar CI de release y rollback con aprobación. |
| Tarea | `N110-T03`. |
| Estado | Diseño documental. No se creó workflow, no se subió artefacto y no se publicó nada. |

## Límites

- `specs/README.md:26` prevé `151-github-actions` para el pipeline de release a Play; ese archivo no existe. No se crea ni modifica aquí.
- `.github/workflows/android-deploy.yml:5,42,99` tiene disparo automático en `push` a `main`, secreto de keystore y acción de subida a Play. Es un bloqueo preexistente; no fue ejecutado ni modificado.
- La publicación futura exige mandato explícito, cuenta autorizada y credenciales ratificadas.

## Pipeline propuesto, no activo

| Paso | Acción | Gate |
| --- | --- | --- |
| 1 | Disparo solo `workflow_dispatch`, sin `push` ni tags | Humano |
| 2 | GitHub Environment `play-release` con aprobadores obligatorios | Humano |
| 3 | `permissions: contents: read` | Automático |
| 4 | `lintDebug`, `testDebugUnitTest`, `assembleDebug` | Automático |
| 5 | Checklist de firma y privacidad aprobado | Humano |
| 6 | Matriz SDK, `versionCode`, R8 y `mapping.txt` | Automático; hoy BLOQUEADO |
| 7 | Artefacto interno sin llaves ni credenciales en logs | Solo si se autoriza build release |
| 8 | Pausa antes de tienda; sin acción de subida | Humano |

Si falta una aprobación o falla un gate, el flujo se detiene. No se reintenta con secretos ni se simula publicación.

## Versionado

- `versionCode`: `2` local y `100 + GITHUB_RUN_NUMBER` en CI, heredado de unidad 100.
- `versionName`: `1.0` en `app/build.gradle.kts:28`; todo cambio requiere changelog y aprobación.
- Revisar `applicationId = com.nexum.nexum` frente a `namespace = mx.nexum.app` antes de cualquier release; no cambiar sin decisión humana.
- Registro por release: fecha, `versionCode`, `versionName`, commit, aprobadores, gates, artefacto, pista y resultado. Sin secretos ni datos personales.

## Rollout y detención

Propuesta no ratificada: 5% a 20% a 50% a 100%, con duraciones y umbrales definidos por el responsable.

Detener el release ante: crash/ANR sostenido, fallo de sesión, regresión del flujo manual, incidente de privacidad, exposición de secretos, declaración incorrecta de permisos o Data Safety, error de firma, `versionCode` duplicado, artefacto sin `mapping.txt` cuando R8 esté activo, u orden del propietario/aprobador.

## Runbook

1. Pausar el rollout en Play Console; no borrar datos de usuarios.
2. Capturar release, `versionCode`, hora, porcentaje y evidencia de monitoreo, sin credenciales.
3. Decidir entre corregir, promover versión anterior estable o retirar.
4. Promover la última versión estable si la política de la cuenta lo permite.
5. Si no es seguro promover, despublicar u ocultar la versión con autorización del propietario.
6. Verificar estado final y disponibilidad del flujo manual.
7. Cerrar acta con causa, decisión, responsables y evidencia.

## Reversibilidad de esta unidad

Eliminar solo los tres documentos creados y revertir los bloques propios de `task.md`, `memoria.md` y `specs/README.md` mediante diff inverso. Sin reset, push, rewrite de historial ni cambios en otros repositorios.
