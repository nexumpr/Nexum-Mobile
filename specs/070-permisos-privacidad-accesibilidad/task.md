---
project: Nexum-Mobile
unit: 070-permisos-privacidad-accesibilidad
kind: implementation-tasks
version: 1.0.0
status: verified
implementation: completed
---

# TASK — Permisos, privacidad y accesibilidad base

**Estado:** 3 de 3 tareas completadas y verificadas mediante análisis estático (`lintDebug`), pruebas unitarias (`testDebugUnitTest`) y empaquetado (`assembleDebug`).

**Dependencias:** `030-di-navegacion-ui`. **Regla:** no implementar funcionalidades financieras ni cambios externos sin aprobación adicional.

## Actividades verificables
- [x] **N070-T01 — Documentar reglas de permisos por función futura sin activar permisos nuevos.**
  - **Requisito:** `M070-R01` · **Plan:** `N070-P01`.
  - **Aceptación:** Auditoría estricta de `AndroidManifest.xml` confirmando que solo se solicita `INTERNET` y que NO se solicita `RECORD_AUDIO` ni `POST_NOTIFICATIONS` prematuramente. Manejador de degradación elegante `PermissionFallbackHandler` que resuelve alternativas no bloqueantes (`AlternativeInputMode.MANUAL_TEXT_INPUT`).
  - **Pruebas:** `./gradlew testDebugUnitTest` (`PrivacyAndPermissionsTest` - `voicePermissionDeniedFallsBackToManualInput`, `applicationCanOperateWithoutSensoryPermissions`, `manifestDoesNotRequestRecordAudioNorPushPrematurely`).
  - **Evidencia registrada:** `PrivacyContracts.kt` creado; auditoría de manifiesto validada en pruebas unitarias automáticas sin fallos.
- [x] **N070-T02 — Preparar componentes de permiso/estado genéricos si son necesarios.**
  - **Requisito:** `M070-R02` · **Plan:** `N070-P02`.
  - **Aceptación:** Componentes accesibles Material 3 creados (`AccessibleButton`, `AccessibleIconButton`, `PermissionRationaleCard`, `PrivacyPolicyBanner`) que garantizan tamaño táctil mínimo de 48dp (`AccessibilityConstants.MinTouchTargetSize = 48.dp`), descripciones semánticas (`contentDescription`) y roles accesibles explícitos para TalkBack.
  - **Pruebas:** `./gradlew testDebugUnitTest` (`PrivacyAndPermissionsTest` - `accessibilityMinTouchTargetCompliesWithMaterialGuidelines`), `./gradlew lintDebug` (0 errores).
  - **Evidencia registrada:** `AccessibleComponents.kt` creado en `mx.nexum.app.core.ui`.
- [x] **N070-T03 — Probar accesibilidad y bloqueo tráfico claro en configuración de producción.**
  - **Requisito:** `M070-R03` · **Plan:** `N070-P03`.
  - **Aceptación:** Tráfico en texto claro deshabilitado por defecto (`cleartextTrafficPermitted="false"`) en `network_security_config.xml`, inclusión del dominio del aviso de privacidad (`nexumpr.github.io`), forzado de transporte estricto HTTPS (`https://nexumpr.github.io/nexum-privacidad/`) y sanitización de registros para prevenir fuga de tokens y contraseñas.
  - **Pruebas:** `./gradlew testDebugUnitTest` (`PrivacyAndPermissionsTest` - `privacyPolicyUrlUsesStrictHttps`, `sensitiveTokensAndPasswordsAreRedactedFromLogs`, `networkSecurityConfigBlocksCleartextAndProtectsPrivacyDomain`), `./gradlew lintDebug`, `./gradlew assembleDebug`.
  - **Evidencia registrada:** 7 pruebas unitarias aprobadas en `PrivacyAndPermissionsTest`; total de 95 pruebas en verde.

## Cierre y evidencia
- Pruebas ejecutadas: `.\gradlew.bat lintDebug` (BUILD SUCCESSFUL, 0 errores), `.\gradlew.bat testDebugUnitTest` (BUILD SUCCESSFUL, 95 tests en total, 0 fallos) y `.\gradlew.bat assembleDebug` (BUILD SUCCESSFUL, APK generado).
- Tareas marcadas `[x]` con verificación real.
- Actualización registrada en `memoria.md` y `specs/README.md`.
- `git push`, merge, despliegue y operaciones destructivas no ejecutadas (requieren autorización separada).
