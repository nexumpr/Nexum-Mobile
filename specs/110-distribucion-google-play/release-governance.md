---
project: Nexum-Mobile
unit: 110-distribucion-google-play
kind: release-governance
version: 1.0.0
status: documentado_pendiente_de_ratificacion
---

# Gobierno de cuenta Play y política de firma

## Trazabilidad

| Elemento | Valor |
| --- | --- |
| Requisito rector | `M110-R01` — la firma de lanzamiento debe custodiarse fuera de Git. |
| Plan | `N110-P01` — identificar responsable de la cuenta Play y política de firma. |
| Tarea | `N110-T01`. |
| Dependencia | `100-ci-versiones-android` (verificada el 2026-11-17, `memoria.md` §43). |
| Estado | Documento verificado estáticamente; decisiones humanas **pendientes**. No autoriza publicación. |

## Hechos comprobables del repositorio

| Hecho | Evidencia | Consecuencia |
| --- | --- | --- |
| `namespace = mx.nexum.app` | `app/build.gradle.kts:8` | Coincide con la base esperada por la unidad 100. |
| `applicationId = com.nexum.nexum` | `app/build.gradle.kts:24` | El identificador publicable no coincide con el namespace. No se cambia sin decisión humana: la identidad de una app en Play es sensible y puede ser inmutable tras el primer AAB. |
| Configuración de firma release condicionada a variables de entorno | `app/build.gradle.kts:34-70` | No hay llave real en el repositorio; la configuración actual depende de secretos externos no ratificados para esta unidad. |
| Optimización/R8 desactivada | `app/build.gradle.kts:64-66` | No se puede afirmar generación de `mapping.txt`. |
| `.gitignore` excluye `*.jks` y `*.keystore` | `.gitignore:16-17` | No excluye explícitamente `*.pepk`; propuesta pendiente. |
| Workflow preexistente con subida a Play | `.github/workflows/android-deploy.yml:5,42,99,102-106` | Riesgo material: disparo en `push` a `main`, secreto de keystore, cuenta de servicio y pista interna en borrador. No ejecutado ni modificado por esta unidad. |
| Intento previo de subida registrado | `memoria.md` §30 | Indica que pudo existir interacción humana previa con Play Console; no se asume cuenta, acceso ni artefacto disponible. |

## Matriz de responsabilidades — propuesta, no ratificada

Los nombres se completarán solo por el responsable del proyecto. Ninguna fila implica acceso concedido.

| Rol | Responsabilidad | Separación requerida | Designación | Evidencia de aprobación |
| --- | --- | --- | --- | --- |
| Propietario de la cuenta Play Console | Titularidad legal, verificación de desarrollador, costos y decisiones de suspensión/despublicación | No debe ser el mismo que ejecuta cambios no revisados | PENDIENTE | Acta o correo humano, sin credenciales |
| Administrador de consola | Alta/baja de usuarios, manejo de pistas, configuración de Play App Signing | Acceso nominal, MFA y principio de menor privilegio | PENDIENTE | Lista nominal y fecha de otorgamiento |
| Release manager | Preparación de versión, `versionCode`, changelog, compuertas y registro de release | No aprueba su propio release | PENDIENTE | Registro de aprobación en GitHub Environment o acta |
| Aprobador de seguridad/privacidad | Revisión de permisos, *Data Safety*, política de privacidad y eliminación de datos | Veto sobre la publicación | PENDIENTE | Checklist firmado (`play-prepublication-checklist.md`) |
| Auditor/lectura | Revisión de trazabilidad y evidencias sin permisos de modificación | Solo lectura | PENDIENTE | Asignación de rol en consola, sin secretos |
| Responsable de llaves | Custodia de la *Upload Key* fuera del repositorio y rotación | No COMMITEA llaves ni las comparte en canales informales | PENDIENTE | Declaración de custodia, sin copiar la llave |

## Registro administrativo y de costos — pendiente de ratificación humana

| Ítem | Acción requerida | Estado por esta unidad | Responsable | Evidencia esperada |
| --- | --- | --- | --- | --- |
| Cuenta de desarrollador | Confirmar existencia, titularidad y país de la cuenta | PENDIENTE | Propietario | Confirmación humana; no se crea cuenta |
| Tarifa de registro | Confirmar importe vigente y quién lo autoriza | PENDIENTE | Propietario | Aprobación de pago; agente no paga |
| Verificación de identidad/datos fiscales | Completar trámites que apliquen | PENDIENTE | Propietario | Confirmación fuera del repositorio; sin datos personales aquí |
| Cuenta de servicio de Play | Solo si se decide automatizar una futura subida | PENDIENTE | Responsable de release | No se crea ni almacena en esta unidad |
| Accesos a consola | Mínimos, nominales y con MFA | PENDIENTE | Administrador | Lista de roles; no se otorgan aquí |

## Política de firma — propuesta técnica

1. **Play App Signing:** política objetivo para la clave de aplicación; la llave del firmante de Play queda gestionada por el servicio de Play una vez ratificado el proceso por el responsable.
2. **Upload Key:** debe generarse y custodiarse fuera del repositorio por el responsable de llaves. No se genera, commitea ni exige en esta unidad.
3. **Custodia:** almacenamiento en gestor de secretos o medio controlado fuera de Git; nunca en `app/`, logs, prompts, artefactos ni variables de CI no autorizadas.
4. **Archivos prohibidos:** `*.jks`, `*.keystore`, `*.pepk` y JSON de cuenta de servicio. Los dos primeros ya están ignorados; `*.pepk` queda como propuesta pendiente.
5. **Rotación:** ante sospecha de exposición, el responsable genera nueva *Upload Key*, registra huella/alias de forma segura y coordina el cambio con Play Console; el agente no ejecuta la rotación.
6. **Separación de firmas:** `debug` usa la firma de depuración estándar del proyecto; `release` nunca reutiliza esa firma. No se activa firma productiva en esta unidad.
7. **Trazabilidad:** cada cambio de política debe registrar responsable, fecha, motivo y evidencia sin secretos en `memoria.md`.

## Compuertas de esta unidad

- Revisión documental: este archivo, el checklist y el plan de rollback.
- Verificación negativa: ausencia de llaves/secretos reales en el árbol versionado y detección del workflow de publicación preexistente como bloqueo.
- No se ejecuta `bundleRelease` con firma real, no se sube AAB/APK y no se crea release en Play.

## Puntos abiertos

| ID | Decisión requerida | Estado |
| --- | --- | --- |
| P110-D01 | Desactivar, aislar o conservar documentado `.github/workflows/android-deploy.yml` | PENDIENTE |
| P110-D02 | Confirmar `applicationId` canónico frente a `mx.nexum.app` | PENDIENTE |
| P110-D03 | Ratificar propietario, administradores, aprobadores y responsable de llaves | PENDIENTE |
| P110-D04 | Ratificar trámites, costos y responsable de pago | PENDIENTE |
| P110-D05 | Incorporar `*.pepk` a `.gitignore` | PENDIENTE |

Estos puntos se rigen por `M-P06` y `M-P07` en `memoria.md` y por la decisión constitucional `M-D08` (firma/publicación Google Play). Un documento no equivale a aprobación.
