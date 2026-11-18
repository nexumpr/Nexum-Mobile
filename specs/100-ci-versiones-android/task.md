---
project: Nexum-Mobile
unit: 100-ci-versiones-android
kind: implementation-tasks
version: 1.0.0
status: verified
implementation: only_on_explicit_request
---

# TASK — CI Android y versión reproducible

**Estado:** completada y verificada el 2026-11-17. Todas las tareas verificadas con evidencia real.

**Dependencias:** `080-pruebas-mvi-compose`, `090-devsecops-android`. **Regla:** no implementar funcionalidades financieras ni cambios externos sin aprobación adicional.

## Actividades verificables
- [x] **N100-T01 — Documentar versionado y convenciones.**
  - **Requisito:** `M100-R01` · **Plan:** `N100-P01`.
  - **Aceptación:** Formalización del esquema de versionado determinista (`versionCode` local reproducible = 2, CI = 100 + `GITHUB_RUN_NUMBER`) y matriz SDK fijada (minSdk 26, targetSdk 36, compileSdk 36, JDK 21).
  - **Pruebas:** `./gradlew assembleDebug`; `./gradlew testDebugUnitTest`; `./gradlew lintDebug`.
  - **Evidencia registrada:** Creación de `CiVersioningVerificationTest.kt` validando determinismo de `versionCode` y coherencia de matriz de versiones.
- [x] **N100-T02 — Crear workflow CI tras autorización, sin publicar store.**
  - **Requisito:** `M100-R02` · **Plan:** `N100-P02`.
  - **Aceptación:** Creación de `.github/workflows/android-ci.yml` operando con permisos mínimos (`contents: read`), sin secretos de firma (`keystore`) ni credenciales de Google Play, ejecutando gates de Lint, tests y compilación debug.
  - **Pruebas:** `CiVersioningVerificationTest.kt` (pruebas de permisos de lectura, ausencia de secretos de firma, ausencia de acciones de despliegue a stores y pruebas negativas de rechazo).
  - **Evidencia registrada:** Archivo `.github/workflows/android-ci.yml` configurado con JDK 21 Temurin, `setup-gradle@v4` y recolección de artefactos internos.
- [x] **N100-T03 — Ejecutar build/test/lint local y anotar resultado; push separado.**
  - **Requisito:** `M100-R03` · **Plan:** `N100-P03`.
  - **Aceptación:** Ejecución y validación local de compilación reproducible, tests unitarios en verde, análisis de Lint sin errores y generación de reportes SARIF/HTML.
  - **Pruebas:** `./gradlew testDebugUnitTest` (115 tests en verde, 6 nuevos de `CiVersioningVerificationTest`); `./gradlew lintDebug` (0 errores); `./gradlew assembleDebug` (APK generado limpiamente).
  - **Evidencia registrada:** Registro en `memoria.md` (decisión M-D17 y Sección 43 de evidencia) y `specs/README.md`.

## Cierre y evidencia
- Pruebas ejecutadas (**resultado real**):
  - `.\gradlew.bat testDebugUnitTest` → **BUILD SUCCESSFUL** (115 tests ejecutados, 6 nuevos de `CiVersioningVerificationTest`, 0 fallos).
  - `.\gradlew.bat lintDebug` → **BUILD SUCCESSFUL** (0 errores, reportes HTML y SARIF generados).
  - `.\gradlew.bat assembleDebug` → **BUILD SUCCESSFUL** (APK debug sin firmar generado).
- Prohibición de publicación respetada: ninguna tarea de despliegue ni secretos de firma configurados en CI base.
- `git push`, merge y despliegues permanecen sin ejecutar conforme a las reglas del repositorio.
