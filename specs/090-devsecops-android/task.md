---
project: Nexum-Mobile
unit: 090-devsecops-android
kind: implementation-tasks
version: 1.0.0
status: verified
implementation: only_on_explicit_request
---

# TASK — Seguridad estática y calidad Android

**Estado:** completada y verificada el 2026-11-17. Todas las tareas verificadas con evidencia real.

**Dependencias:** `010-base-android`. **Regla:** no implementar funcionalidades financieras ni cambios externos sin aprobación adicional.

## Actividades verificables
- [x] **N090-T01 — Configurar controles locales aprobados sin aumentar privilegios.**
  - **Requisito:** `M090-R01` · **Plan:** `N090-P01`.
  - **Aceptación:** Definición formal de gates progresivos (Local/Pre-commit, PR/CI, Release) y configuración de gobernanza estricta en `app/lint.xml` forzando reglas de seguridad críticas como `fatal`.
  - **Pruebas:** `./gradlew lintDebug`; `./gradlew testDebugUnitTest`.
  - **Evidencia registrada:** Creación de `app/lint.xml` con reglas `BadHostnameVerifier`, `NetworkSecurityConfig`, `HardcodedDebugMode`, `InsecureBaseConfiguration`, `TrustAllX509TrustManager`, etc. como `fatal`.
- [x] **N090-T02 — Ejecutar los controles realmente disponibles.**
  - **Requisito:** `M090-R02` · **Plan:** `N090-P02`.
  - **Aceptación:** Implementación y ejecución de la suite automatizada `DevSecOpsVerificationTest.kt` validando ausencia de secretos (regex), ausencia de permisos invasivos en `AndroidManifest.xml` (solo `INTERNET`), y ausencia de tráfico en texto claro en `network_security_config.xml`.
  - **Pruebas:** `./gradlew testDebugUnitTest` (109 tests totales, 8 nuevos de DevSecOps, 0 fallos); `./gradlew lintDebug` (0 errores, reportes HTML y SARIF generados); `gitleaks` auditado en entorno (no disponible en PATH local, suplido de forma reproducible por la suite de pruebas).
  - **Evidencia registrada:** `DevSecOpsVerificationTest.kt` con 8 pruebas (incluyendo pruebas negativas para detección de claves AWS, claves privadas PEM, permisos invasivos y cleartext simulado).
- [x] **N090-T03 — Registrar hallazgos, false positives y resultados en memoria.**
  - **Requisito:** `M090-R03` · **Plan:** `N090-P03`.
  - **Aceptación:** Registro de la matriz de severidades (Crítica, Alta, Media, Baja), SLAs de remediación y gobernanza de excepciones en `memoria.md` (decisión M-D16 y Sección 42 de evidencia).
  - **Pruebas:** Verificación de compilación limpia y pruebas de regresión en verde (`./gradlew testDebugUnitTest`, `./gradlew lintDebug`, `./gradlew assembleDebug`).
  - **Evidencia registrada:** Sección 42 y decisión M-D16 incorporadas en `memoria.md`.

## Cierre y evidencia
- Pruebas ejecutadas (**resultado real**):
  - `.\gradlew.bat testDebugUnitTest` → **BUILD SUCCESSFUL** (109 tests ejecutados, 8 nuevos de `DevSecOpsVerificationTest`, 0 fallos).
  - `.\gradlew.bat lintDebug` → **BUILD SUCCESSFUL** (0 errores, reportes SARIF y HTML generados).
  - `.\gradlew.bat assembleDebug` → **BUILD SUCCESSFUL** (APK de depuración generado).
  - `gitleaks` → `NO DISPONIBLE EN PATH LOCAL` (confirmado con `Get-Command gitleaks`, código 1; suplido de forma nativa por `DevSecOpsVerificationTest.kt` sin dependencias externas ni Docker).
- Tareas completadas `[x]` con evidencia comprobada.
- Registro consolidado en `memoria.md` y `specs/README.md`.
- `git push`, merge y despliegues permanecen sin ejecutar conforme a las reglas del repositorio.
