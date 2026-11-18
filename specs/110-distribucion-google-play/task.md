---
project: Nexum-Mobile
unit: 110-distribucion-google-play
kind: implementation-tasks
version: 1.0.0
status: documentado_y_verificado
implementation: only_on_explicit_request
---

# TASK — Preparación para distribución segura

**Estado:** documentación creada y validada estáticamente el 2026-11-17. Las decisiones humanas de cuenta Play, firma, `applicationId`, R8 y workflow de deploy permanecen **pendientes**; este estado no autoriza publicación ni indica aprobación humana.

**Dependencias:** `100-ci-versiones-android` (verificada, `memoria.md` §43). **Regla:** no implementar funcionalidades financieras ni cambios externos sin aprobación adicional.

## Actividades verificables
- [x] **N110-T01 — Documentar matriz de versión y firma segura, no crear claves sin autorización.**
  - **Requisito:** `M110-R01` · **Plan:** `N110-P01`.
  - **Aceptación verificada:** documento `release-governance.md` con matriz de responsabilidades, registro administrativo/costos y política de firma; sin llaves ni credenciales creadas.
  - **Pruebas:** revisión documental ejecutada; revisión humana de firma **NO EJECUTADA**; build release **NO EJECUTADO**.
  - **Evidencia:** `specs/110-distribucion-google-play/release-governance.md`; comprobaciones `git grep` de secretos y `git check-ignore` registradas en `memoria.md` §44.

- [x] **N110-T02 — Preparar checklist de revisión, ficha de tienda y avisos futuros.**
  - **Requisito:** `M110-R02` · **Plan:** `N110-P02`.
  - **Aceptación verificada:** plantilla auditable `play-prepublication-checklist.md` con estado real de privacidad, permisos, `targetSdk`, R8/`mapping.txt`, tamaño de AAB y pistas de prueba.
  - **Pruebas:** revisión documental ejecutada; formulario oficial de *Data Safety* y URL de eliminación **PENDIENTES** de humano.
  - **Evidencia:** `specs/110-distribucion-google-play/play-prepublication-checklist.md`; hallazgos de `app/build.gradle.kts:64-66` y `AndroidManifest.xml:5` registrados en `memoria.md` §44.

- [x] **N110-T03 — Validar documentación; no ejecutar publicación.**
  - **Requisito:** `M110-R03` · **Plan:** `N110-P03`.
  - **Aceptación verificada:** documento `release-rollback-plan.md` con compuerta humana, trazabilidad de versión, criterios de detención y retirada; workflow de deploy preexistente reportado como bloqueo sin modificar.
  - **Pruebas:** `git diff --check`, verificaciones negativas y regresión de unidad 100 ejecutadas; publicación y subida de artefactos **NO EJECUTADAS**.
  - **Evidencia:**
    - `.\gradlew.bat testDebugUnitTest` → BUILD SUCCESSFUL; reejecución con `--rerun-tasks` → BUILD SUCCESSFUL, 15 suites XML, 115 tests, 0 failures, 0 errors.
    - `.\gradlew.bat lintDebug` → BUILD SUCCESSFUL.
    - `.\gradlew.bat assembleDebug` → BUILD SUCCESSFUL.
    - `git grep` confirmó el workflow de publicación preexistente y solo secretos sintéticos en pruebas; `git check-ignore` mostró `*.jks` cubierto y `*.pepk` no cubierto.

## Cierre y evidencia
- Pruebas ejecutadas con resultado real: ver lista anterior y `memoria.md` §44.
- No ejecutados: `bundleRelease` con firma real, generación de llaves, subida a Google Play, trámites, pagos, push, merge ni despliegue.
- Revisión humana de firma y consentimiento: **NO EJECUTADA**; continúa como compuerta obligatoria antes de cualquier release.
- Los puntos P110-D01 a P110-D05 siguen pendientes en `release-governance.md`.
- `git push`, merge y despliegues permanecen sin ejecutar conforme a las reglas del repositorio.
