---
project: Nexum-Mobile
unit: 110-distribucion-google-play
kind: play-prepublication-checklist
version: 1.0.0
status: plantilla_auditable_pendiente_de_ratificacion
---

# Checklist de privacidad y validación pre-publicación

## Trazabilidad

| Elemento | Valor |
| --- | --- |
| Requisito rector | `M110-R02` — la publicación debe cumplir privacidad, eliminación de cuenta y permisos aplicables. |
| Plan | `N110-P02` — diseñar checklist de privacidad y validación de publicación. |
| Tarea | `N110-T02`. |
| Estado | Plantilla y estado actual verificados documentalmente. **No valida una publicación**; no se ha subido nada a Play. |

## Instrucciones de uso

- Cada fila debe completarse con evidencia real antes de cualquier compilación release o envío a una pista.
- Estados permitidos: `VERIFICADO` (evidencia objetiva adjunta), `PENDIENTE` (falta decisión o evidencia), `BLOQUEADO` (configuración actual impide cumplirlo), `NO APLICABLE` (justificado por escrito).
- La fecha y el responsable se completan con datos reales. No se aceptan afirmaciones sin evidencia ni datos personales o financieros reales.
- Esta plantilla no sustituye la revisión humana de privacidad ni el formulario oficial de Google Play Console.

## A. Seguridad y privacidad

| ID | Control | Evidencia exigida | Estado actual | Responsable | Fecha |
| --- | --- | --- | --- | --- | --- |
| CHK-P01 | URL pública de política de privacidad | URL HTTPS accesible y revisada por privacidad | **VERIFICADO en código**: `https://nexumpr.github.io/nexum-privacidad/` en `app/src/main/java/mx/nexum/app/core/privacy/PrivacyContracts.kt:55`; accesibilidad pública pendiente de revisión humana | PENDIENTE | — |
| CHK-P02 | URL pública de eliminación de cuenta/datos | URL HTTPS o mecanismo publicado y verificable | **PENDIENTE**: no se encontró URL pública en el repositorio; no inventar | Privacidad/Backend | — |
| CHK-P03 | Formulario *Data Safety* de Play Console | Respuestas revisadas, captura o acta sin secretos | **PENDIENTE**: no completar por el agente | Privacidad | — |
| CHK-P04 | Declaración de permisos sensibles | Manifiesto revisado y correspondencia con *Data Safety* | **VERIFICADO en código**: solo `android.permission.INTERNET` en `app/src/main/AndroidManifest.xml:5`; sin micrófono, cámara, ubicación ni notificaciones | Seguridad | — |
| CHK-P05 | Cumplimiento de privacidad financiera | Declaración de que Nexum no transfiere dinero, no accede a saldos bancarios y muestra registros/estimaciones del usuario | **PENDIENTE de ratificación**: alineado con constitución §1.1 y MO-PR05; requiere revisión legal/privacidad | Privacidad | — |
| CHK-P06 | Protección de transporte y dominios | `network_security_config.xml` sin texto claro y dominios revisados | **VERIFICADO en código** por `DevSecOpsVerificationTest.kt:71-92`; dominio de privacidad declarado | Seguridad | — |
| CHK-P07 | Consentimiento y aviso vigente en la app | Revisión del aviso enlazado y del registro de consentimiento | **PENDIENTE**: implementación funcional completa fuera de esta unidad | Privacidad/Producto | — |
| CHK-P08 | Ausencia de datos reales en pruebas y artefactos | Revisión de fixtures sintéticas y logs | **VERIFICADO documentalmente** por suites existentes; requiere repetir antes del release real | Seguridad | — |

## B. Artefacto y calidad

| ID | Control | Evidencia exigida | Estado actual | Responsable | Fecha |
| --- | --- | --- | --- | --- | --- |
| CHK-A01 | `targetSdk` vigente y documentado | Valor en Gradle y matriz de versión | **VERIFICADO**: `targetSdk = 36` en `app/build.gradle.kts:26`; matriz ratificada en unidad 100 | Release | — |
| CHK-A02 | `versionCode` monótono y trazable | Registro de versión, changelog y regla determinista | **VERIFICADO en unidad 100**: `2` local, `100 + GITHUB_RUN_NUMBER` en CI (`CiVersioningVerificationTest.kt:27-44`) | Release | — |
| CHK-A03 | Reglas de desofuscación R8/ProGuard | Reglas versionadas y revisadas para Retrofit/Gson/modelos | **BLOQUEADO**: `app/build.gradle.kts:64-66` desactiva optimización y no existe `app/proguard-rules.pro` | Release/Arquitectura | — |
| CHK-A04 | Generación de `mapping.txt` | Ruta del artefacto de desofuscación y retención segura | **BLOQUEADO**: depende de CHK-A03; no afirmar generación | Release | — |
| CHK-A05 | Tamaño del AAB | Medición real del AAB de release y umbral acordado | **PENDIENTE**: no se generó AAB en esta unidad; no inventar umbral | Release | — |
| CHK-A06 | Pruebas en pista interna | Registro de *Internal Testing* con versión, dispositivos y resultado | **PENDIENTE**: no se ejecuta ni simula subida | Release/QA | — |
| CHK-A07 | Pruebas en pista cerrada | Registro de *Closed Testing* y criterios de salida | **PENDIENTE**: depende de decisión de rollout | Release/QA | — |
| CHK-A08 | Regresión Android | `lintDebug`, `testDebugUnitTest`, `assembleDebug` en verde | **VERIFICADO**: tres `BUILD SUCCESSFUL`; rerun de tests con 115 tests, 0 failures, 0 errors (`memoria.md` §44) | QA | 2026-11-17 |
| CHK-A09 | Secretos en logs/artefactos | Revisión de workflows, Gradle y salidas | **PARCIAL**: `.gitignore` cubre JKS/keystore; persiste workflow de deploy preexistente sin autorizar | Seguridad | — |

## C. Aprobación humana previa

| ID | Control | Evidencia exigida | Estado actual |
| --- | --- | --- | --- |
| CHK-H01 | Política de firma ratificada | Documento firmado y responsable designado | PENDIENTE (`release-governance.md`) |
| CHK-H02 | Privacidad y eliminación de cuenta ratificadas | Checklist completado por privacidad | PENDIENTE |
| CHK-H03 | Aprobación de release | Registro en GitHub Environment o acta equivalente | PENDIENTE |
| CHK-H04 | Resolución del workflow de deploy preexistente | Decisión humana documentada; sin publicación automática | PENDIENTE |

## Cierre

El checklist queda como plantilla auditable. No acredita que la app cumple las políticas de Play ni que puede publicarse. La publicación, la subida de AAB/APK y el uso de credenciales requieren autorización independiente.
