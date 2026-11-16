---
project: Nexum-Mobile
unit: 010-base-android
kind: implementation-tasks
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# TASK — Base Android y compilación reproducible

**Estado:** tareas verificadas e implementadas.

**Dependencias:** ninguna unidad anterior. **Regla:** no implementar funcionalidades financieras ni cambios externos sin aprobación adicional.

## Actividades verificables
- [x] **N010-T01 — Registrar versiones realmente presentes y compatibilidad.**
  - **Requisito:** `M010-R01` · **Plan:** `N010-P01`.
  - **Aceptación:** Versiones ratificadas: Java 21 (Android Studio JBR), Gradle 9.5.0, AGP 9.3.3, Kotlin 2.2.10, Compose BOM 2026.02.01, minSdk 26, targetSdk 36, compileSdk 36 (minor 1), Java 11 bytecode compatibility.
  - **Pruebas:** `./gradlew assembleDebug`; `./gradlew testDebugUnitTest`; `./gradlew lintDebug`.
  - **Evidencia a registrar:** `gradle/wrapper/gradle-wrapper.properties`, `gradle/libs.versions.toml`, `app/build.gradle.kts`.
- [x] **N010-T02 — Verificar `.gitignore` de local.properties y builds sin eliminar código.**
  - **Requisito:** `M010-R02` · **Plan:** `N010-P02`.
  - **Aceptación:** Comprobado con `git check-ignore -v`: `local.properties`, `*.jks`, `*.keystore`, `.gradle`, `/build` y `app/build` están ignorados activamente y fuera de git.
  - **Pruebas:** `git status -s` (limpio), `git check-ignore -v local.properties release.jks release.keystore .gradle/ build/ app/build/`.
  - **Evidencia a registrar:** `.gitignore`, `app/.gitignore`.
- [x] **N010-T03 — Ejecutar assembleDebug y testDebugUnitTest y documentar resultados.**
  - **Requisito:** `M010-R03` · **Plan:** `N010-P03`.
  - **Aceptación:** Compilación y pruebas ejecutadas exitosamente con herramientas reales.
  - **Pruebas:** `.\gradlew.bat testDebugUnitTest` (BUILD SUCCESSFUL, 51 tests, 0 fallos); `.\gradlew.bat assembleDebug` (BUILD SUCCESSFUL); `.\gradlew.bat lintDebug` (BUILD SUCCESSFUL, 0 errores).
  - **Evidencia a registrar:** `memoria.md` §35, `app/build/reports/tests/testDebugUnitTest/index.html`.

## Cierre y evidencia
- Reportar pruebas ejecutadas (**resultado real**) y no ejecutadas con causa. Para esta unidad: `./gradlew assembleDebug`; `./gradlew testDebugUnitTest`; `./gradlew lintDebug`.
- Marcar `[x]` solo cuando se verifique la tarea correspondiente; conservar pendientes si no existe evidencia.
- Actualizar `memoria.md` con comandos, archivos, decisiones y riesgos; no avanzar a otra spec automáticamente.
- `git push`, merge, despliegue y operaciones destructivas siguen requiriendo autorización separada.
