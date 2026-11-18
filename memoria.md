# Memoria inicial — Nexum-Mobile

**Fecha:** 2026-10-29 | **Estado:** documentación SDD vigente; implementación funcional todavía por especificar.

## 1. Estado verificado
- Proyecto Android Studio creado localmente en `Nexum-Mobile` con paquete `mx.nexum.app`, Kotlin, Jetpack Compose y Gradle Kotlin DSL.
- API mínima seleccionada provisionalmente: 26. Se ejecutó `./gradlew assembleDebug` con **BUILD SUCCESSFUL**, sin prueba de funcionalidades ni publicación Google Play.
- La documentación SDD operativa se limita a **arquitectura base**. No consta ejecución de tareas de esas unidades ni pantallas financieras.

## 2. Decisiones acordadas
| ID | Decisión | Referencia |
| --- | --- | --- |
| M-D01 | Android + Kotlin + Jetpack Compose | Descripción móvil y proyecto creado. |
| M-D02 | Material 3 | Descripción móvil vigente. |
| M-D03 | Arquitectura MVI, flujo unidireccional, estado inmutable | Arquitectura y descripción móvil. |
| M-D04 | Backend independiente Go; REST/HTTPS/JSON y autorización en servidor | Arquitectura consolidada. |
| M-D05 | Inyección de dependencias y pruebas automatizadas | Arquitectura conceptual, herramientas aún por definir. |
| M-D06 | Fuente constitucional `.specify/memory/constitution.md` | Metodología docente elegida. |
| M-D07 | Contratos de autenticación NEXUM-E001 (registro) y NEXUM-E002 (login) del catálogo V6.0.2; ejemplo 4.1 como fixture | Specs `130-Registro-Usuario` y `140-Login` (2026-11-02). |
| M-D08 | Registro exige `income` y al menos un gasto recurrente; `frequency` limitada a `monthly` y `type` a `fixed` hasta catálogo confirmado con Backend | Spec `130-Registro-Usuario` (2026-11-02). |
| M-D09 | `androidx.lifecycle:lifecycle-viewmodel-compose` (producción) y `kotlinx-coroutines-test` (solo pruebas) aprobados para autenticación; sin Retrofit, Hilt ni kotlinx-serialization | Fase A de autenticación (2026-11-02). |
| M-D10 | Matriz de versiones Android ratificada: Java 21 (JBR), Gradle 9.5.0, AGP 9.3.3, Kotlin 2.2.10, Compose BOM 2026.02.01, minSdk 26, targetSdk 36, compileSdk 36 | Spec `010-base-android` (2026-11-15). |
| M-D11 | Inyección de dependencias mediante `AppContainer` manual y navegación desacoplada mediante `AppNavigator` / `NexumAppShell` | Spec `030-di-navegacion-ui` (2026-11-15). |
| M-D12 | Adaptador HTTP seguro (`safeApiCall`), serialización Gson, contrato canónico de errores (`ApiResult`/`HttpErrorType`) y forzado de TLS/HTTPS vía `network_security_config.xml` | Spec `040-cliente-http-contratos` (2026-11-15). |
| M-D13 | Caché técnica en memoria con aislamiento por usuario (`userId`), TTL/expiración (`CONFIRMED`, `PENDING`, `STALE`), invalidación explícita y cola de operaciones offline con clave de idempotencia (`IdempotentOperation`) sin dependencias fuera de catálogo | Spec `060-cache-offline` (2026-11-17). |
| M-D14 | Permisos mínimos en manifiesto (solo `INTERNET`, sin `RECORD_AUDIO` ni push prematuro), componentes accesibles Material 3 (48dp mínimo, TalkBack), degradación elegante `PermissionFallbackHandler` y protección TLS de dominio de privacidad `https://nexumpr.github.io/nexum-privacidad/` | Spec `070-permisos-privacidad-accesibilidad` (2026-11-17). |
| M-D15 | Suite de pruebas de flujo MVI, fixtures sintéticas (`TestFixtures`, `FakeTokenStorage`, `FakeCredentialStorage`), no repetición de efectos efímeros (`replay = 0`), y separación de tests unitarios JVM frente a instrumentados Compose (`NexumAppShellComposeTest`) | Spec `080-pruebas-mvi-compose` (2026-11-17). |
| M-D16 | Arquitectura DevSecOps: gobernanza de Android Lint (`app/lint.xml`), gates progresivos (Local/CI/Release), suite automatizada de seguridad estática (`DevSecOpsVerificationTest`), matriz de severidad (P0..P3) y SLAs de remediación sin dependencias externas | Spec `090-devsecops-android` (2026-11-17). |
| M-D17 | Pipeline CI reproducible (`.github/workflows/android-ci.yml`) con permisos mínimos (`contents: read`), sin secretos de firma (`keystore`) ni publicación a tiendas, esquema de versionado determinista (`versionCode` incremental en CI y base local) y recolección de artefactos internos (SARIF, HTML, APK debug) | Spec `100-ci-versiones-android` (2026-11-17). |

## 3. Requerimientos docentes para la solución final
- Fidelidad a la maqueta sin quitar funciones; Google Play como forma inicial de evaluación; pruebas, DevSecOps y CI/CD.
- Caché cliente, logout, sesión persistente mientras la identidad siga autorizada, permisos, aviso de privacidad en app/landing, eliminar cuenta y notificaciones push.
- Al menos una función ML y una IA generativa; tratamiento del contenido subido cuando aplique. La rúbrica también menciona analítica en landing page; deberá revisarse junto con aviso de privacidad y consentimiento antes de usar identificadores o seguimiento.
- SQLite/sincronización **condicional** si el proyecto requiere operación local; seguridad y trazabilidad obligatorias.
- Fecha de evaluación: 2026-12-02. **Estas exigencias no habilitan desarrollar funcionalidades hoy**.

## 4. Decisiones pendientes
| ID | Decisión | Responsable sugerido |
| --- | --- | --- |
| M-P01 | (Resuelta en M-D10) SDK target y compatibilidad versiones Kotlin/AGP/Compose; API min 26 ratificada | Equipo Mobile (cerrada en 010-base-android). |
| M-P02 | (Resuelta en M-D11 y M-D12) DI, navegación, cliente HTTP y JSON ratificados | Equipo Mobile (cerrada en 040-cliente-http-contratos). |
| M-P03 | (Parcialmente resuelta en M-D13 para arquitectura de caché técnica y sincronización; persistencia en disco SQLite/Room condicional a decisión futura) Caché cliente y cola offline con idempotencia | Equipo Mobile (cerrada base técnica en 060-cache-offline). |
| M-P04 | JWT, renovación, expiración/revocación, logout y errores | Equipo Mobile + Backend. |
| M-P05 | (Parcialmente resuelta en M-D14 para permisos mínimos, accesibilidad y transporte de privacidad; voz/micrófono/push condicional a especificación autorizada futura) Push, ML/IA generativa, voz, permisos y privacidad de terceros | Equipo Mobile (cerrada base técnica en 070-permisos-privacidad-accesibilidad). |
| M-P06 | Firmado de app, matriz de versiones, Play Console y CI | Responsable de release. |
| M-P07 | Reglas de moderación, aviso/landing y eliminación de cuenta | Equipo + privacidad. |
| M-P08 | Alcance de documentos SDD en GitHub | Equipo; por ahora local. |

## 5. Fuente y límites
- «Requerimientos para el desarrollo del proyecto Móvil v2», pp. 1–4.
- Arquitectura corregida Nexum (0.2), descripción natural móvil, ejemplos docentes **solo metodológicos**.
- No copiar MVVM del ejemplo `MovilSDD` ni utilizar las tecnologías de su proyecto como decisiones Nexum.
- No asumir que Cloud Run/GKE, Redis, Firebase, APIs móviles, CI o publicar en Google Play están implementados.

## 6. Próximo paso
Consultar `specs/README.md`, escoger una unidad de arquitectura base y autorizarla individualmente; verificar pruebas y registrar evidencia. El equipo móvil conserva el control de su propia implementación y contratos compartidos.

## 7. Evidencia — Fase A de autenticación (documental)
- **Fecha:** 2026-11-02 · **Alcance:** solo documentación; **sin cambios en código `.kt` ni Gradle**.
- **Archivos modificados (7):** `specs/130-Registro-Usuario/{spec,plan,task}.md` (v2.0.0, requisitos `M130-R01…R05`, planes `N130-P01…P06`, tareas `N130-T01…T06`), `specs/140-Login/{spec,plan,task}.md` (v2.0.0, `M140-R01…R05`, `N140-P01…P06`, `N140-T01…T06`), `memoria.md` (M-D07, M-D08, M-D09 y esta sección).
- **Contenido:** contratos NEXUM-E001/E002 con fixture del ejemplo 4.1, modelos DTO, interfaz `AuthRepository`/`AuthResult`/`AuthEffect` compartida, `TokenStorage`, estados MVI (`RegisterUiState`, `LoginUiState`) e intenciones.
- **Verificación:** revisión de trazabilidad requisito → plan → tarea y de enlaces de `specs/README.md` (sin cambios de ruta).
- **Pruebas Gradle:** `NO EJECUTADAS` — fase documental sin ejecución de código.
- **Pendiente:** mandato humano para la Fase B (implementación de `130` y `140`); catálogos `frequency`/`type`, campos de `user` y forma de verificación en el `201` por confirmar con Backend.

## 8. Evidencia — Fase B de autenticación (implementación 130 y 140)
- **Fecha:** 2026-11-04 · **Mandato:** implementación explícita de `130-Registro-Usuario` y `140-Login`; sin push ni otras unidades.
- **Entorno:** Windows · JAVA_HOME = Android Studio JBR (OpenJDK 21.0.10) · Gradle 9.5.0 (wrapper) · AGP 9.3.3 · Kotlin 2.2.10 · Compose BOM 2026.02.01.
- **Comandos y resultados reales:**
  - `gradlew assembleDebug` → **BUILD SUCCESSFUL**.
  - `gradlew testDebugUnitTest` → **BUILD SUCCESSFUL** · 50 tests · 0 fallos (`AuthValidatorTest` 19, `AuthViewModelTest` 10, `LoginViewModelTest` 9, `MockAuthApiTest` 11, `ExampleUnitTest` 1).
  - `gradlew lintDebug` → **BUILD SUCCESSFUL** · 0 errores · 21 avisos (versiones y recursos preexistentes; ninguno atribuible al código nuevo).
- **Archivos creados (13 fuentes + 4 pruebas):** `app/src/main/java/mx/nexum/app/auth/` → `data/dto/AuthDtos.kt`, `data/AuthApi.kt`, `data/MockAuthApi.kt`, `data/TokenStorage.kt`, `data/AuthRepository.kt`, `domain/AuthValidator.kt`, `ui/AuthUiState.kt`, `ui/AuthIntent.kt`, `ui/AuthViewModel.kt`, `ui/LoginViewModel.kt`, `ui/LoginScreen.kt`, `ui/RegisterScreen.kt`, `ui/AuthFlow.kt`; `app/src/test/java/mx/nexum/app/auth/` → `AuthValidatorTest.kt`, `AuthViewModelTest.kt`, `LoginViewModelTest.kt`, `MockAuthApiTest.kt`.
- **Archivos modificados:** `gradle/libs.versions.toml` (+`lifecycle` 2.9.1, +`coroutines` 1.10.1), `app/build.gradle.kts` (+`lifecycle-viewmodel-compose`, +`kotlinx-coroutines-test`), `MainActivity.kt` (monta `AuthFlow`), `specs/130-Registro-Usuario/task.md` y `specs/140-Login/task.md` (12 tareas `[x]` con evidencia), `specs/README.md` (progreso).
- **Incidencias corregidas:** `RegisterScreen.kt` lambda composable en el elvis de `supportingText`; import `AuthIntent` inexistente en `AuthViewModelTest` → `RegisterIntent`; carrera efectos/colector en pruebas → `UnconfinedTestDispatcher`.
- **NO EJECUTADO:** pruebas de instrumentación/emulador (sin dispositivo); verificación visual manual; `git push` (requiere autorización aparte).
- **Riesgos abiertos:** fuente mock sin Backend real (sustitución en `040`/`120`); token en memoria hasta `050`; M-P02 (DI/red/JSON) sigue pendiente — se usó composición manual; catálogos `frequency`/`type`, campos de `user` y verificación del `201` por confirmar.

## 9. Evidencia — Fase A de `151-github-actions` (documental)
- **Fecha:** 2026-11-05 · **Mandato:** aprobación humana para crear la especificación y actualizar la memoria; **sin Fase B, sin push**.
- **Alcance:** solo documentación; **cero cambios en código `.kt`, Gradle o workflows reales**.
- **Archivos creados (1):** `specs/151-github-actions.md` (archivo único por decisión humana — desvío controlado de la convención de carpetas; frontmatter `format: single_file`, requisitos `M151-R01…R05`, planes `N151-P01…P05`, tareas `N151-T01…T05`).
- **Archivos modificados (1):** `memoria.md` (esta sección).
- **Contenido:** YAML exacto de `.github/workflows/google-play-release.yml` (trigger `push: main` + `workflow_dispatch`, `permissions: contents: read`, `concurrency`, setup-java Temurin 17 con cache Gradle, validación previa de los 5 secretos, decodificación de `KEYSTORE_BASE64`, `./gradlew bundleRelease` y `r0adkll/upload-google-play@v1` con `tracks: internal`); snippet Gradle de `signingConfigs` condicional por `KEYSTORE_FILE`; checklist manual de GitHub Secrets (`KEYSTORE_BASE64`, `KEY_ALIAS`, `KEY_PASSWORD`, `STORE_PASSWORD`, `SERVICE_ACCOUNT_JSON`) con comando PowerShell de base64 y pasos de Play Console; plan de Fase B pendiente.
- **Verificación:** inputs de la action contrastados con `action.yml`/README de `r0adkll/upload-google-play` (`tracks` en vez del deprecado `track`, `serviceAccountJsonPlainText`); trazabilidad requisito → plan → tarea revisada; sin secretos reales en el documento (MO-PR10).
- **Pruebas Gradle:** `NO EJECUTADAS` — fase documental sin ejecución de código.
- **Push/despliegue:** `NO EJECUTADO` — requiere autorización separada (MO-PR16).
- **Pendiente:** mandato humano para la Fase B (crear el `.yml`, parchear `app/build.gradle.kts`, `.gitignore`, `bundleRelease` y configurar secretos manualmente); decisión de track tras `internal`; estrategia de `versionCode` (hoy fijo en 1) dentro de M-P06.

## 10. Evidencia — Fase A de `150-Refresh-Token` (documental)
- **Fecha:** 2026-11-10 · **Mandato:** solicitud humana para crear la especificación 150 con `spec.md`, `plan.md` y `task.md` a partir del endpoint NEXUM-E003 (`POST /api/v1/auth/refresh` [S], catálogo V6.0.2); **Fase A documental, sin cambios de código .kt ni push**.
- **Alcance:** solo documentación; **cero cambios en código `.kt`, dependencias Gradle o mocks de ejecución**.
- **Archivos creados (3):** `specs/150-Refresh-Token/spec.md` (v1.0.0, requisitos `M150-R01…R05`), `specs/150-Refresh-Token/plan.md` (v1.0.0, planes `N150-P01…P06`), `specs/150-Refresh-Token/task.md` (v1.0.0, tareas `N150-T01…T06` todas `- [ ]`).
- **Archivos modificados (2):** `specs/README.md` (enlace al mapa y actualización de progreso), `memoria.md` (esta sección).
- **Contenido:** Contrato NEXUM-E003 con payload rotativo `RefreshRequest(refreshToken)`, respuesta `200` con `RefreshResponse(accessToken, refreshToken, expiresIn)`, rotación obligatoria de refresh token, detección de reutilización ilegítima / token revocado con purga obligatoria de `TokenStorage` y navegación a Login (`AuthEffect.NavigateToLogin`), aislamiento de fallos de red (preservación de sesión local ante errores 5xx o desconexión), y patrón de concurrencia single-flight con `Mutex` en `AuthRepository`.
- **Verificación:** Trazabilidad formal requisito → plan → tarea; consistencia con la constitución canónica (`.specify/memory/constitution.md` MO-PR04, MO-PR06, MO-PR10, MO-PR15); sin secretos ni tokens reales.
- **Pruebas Gradle:** `NO EJECUTADAS` — fase puramente documental sin ejecución de código.
- **Pendiente:** mandato humano para la Fase B (implementación de DTOs, métodos en `AuthApi`/`AuthRepository`, rotación en `MockAuthApi`, control con `Mutex`, observador en UI y pruebas unitarias con `kotlinx-coroutines-test`).

## 11. Evidencia — Fase A de `160-Logout` (documental)
- **Fecha:** 2026-11-10 · **Mandato:** solicitud humana para crear la especificación de cierre de sesión a partir del endpoint NEXUM-E004 (`POST /api/v1/auth/logout` [M], catálogo V6.0.2); **Fase A documental, sin cambios de código .kt ni push**.
- **Alcance:** solo documentación; **cero cambios en código `.kt`, Gradle o mocks de ejecución**.
- **Archivos creados (3):** `specs/160-Logout/spec.md` (v1.0.0, requisitos `M160-R01…R05`), `specs/160-Logout/plan.md` (v1.0.0, planes `N160-P01…P06`), `specs/160-Logout/task.md` (v1.0.0, tareas `N160-T01…T06` todas `- [ ]`).
- **Archivos modificados (2):** `specs/README.md` (enlace al mapa y actualización de progreso), `memoria.md` (esta sección).
- **Contenido:** Contrato NEXUM-E004 (`POST /api/v1/auth/logout`, `204 No Content`), payload `LogoutRequest(refreshToken)` y cabecera `Authorization: Bearer <accessToken>`, regla fail-secure de purga incondicional de credenciales locales en `TokenStorage` (incluso ante caída de red o 401), diálogo composable de confirmación de salida ("Perfil > Salir"), navegación con reseteo de historial (`AuthEffect.NavigateToLogin`), y simulación en `MockAuthApi`.
- **Verificación:** Trazabilidad formal requisito → plan → tarea; consistencia con la constitución canónica (`.specify/memory/constitution.md` MO-PR04, MO-PR06, MO-PR10, MO-PR15); sin secretos ni tokens reales.
- **Pruebas Gradle:** `NO EJECUTADAS` — fase puramente documental sin ejecución de código.
- **Pendiente:** mandato humano para la Fase B (implementación de DTOs, método en `AuthApi`/`AuthRepository`, simulación en `MockAuthApi`, diálogo Compose y pruebas unitarias con `kotlinx-coroutines-test`).

## 12. Evidencia — Fase A de `170-Recuperar-Contrasena` (documental)
- **Fecha:** 2026-11-10 · **Mandato:** solicitud humana para crear la especificación de solicitud de recuperación de contraseña a partir del endpoint NEXUM-E005 (`POST /api/v1/auth/password/forgot` [E], catálogo V6.0.2); **Fase A documental, sin cambios de código .kt ni push**.
- **Alcance:** solo documentación; **cero cambios en código `.kt`, Gradle o mocks de ejecución**.
- **Archivos creados (3):** `specs/170-Recuperar-Contrasena/spec.md` (v1.0.0, requisitos `M170-R01…R05`), `specs/170-Recuperar-Contrasena/plan.md` (v1.0.0, planes `N170-P01…P06`), `specs/170-Recuperar-Contrasena/task.md` (v1.0.0, tareas `N170-T01…T06` todas `- [ ]`).
- **Archivos modificados (2):** `specs/README.md` (enlace al mapa y actualización de progreso), `memoria.md` (esta sección).
- **Contenido:** Contrato NEXUM-E005 (`POST /api/v1/auth/password/forgot`, `202 Accepted`), payload `ForgotPasswordRequest(email)` con respuesta neutra idéntica `ForgotPasswordResponse(message)` para prevenir la enumeración de usuarios (MO-PR06), validación local previa de email, control antispam / cooldown en UI, manejo de `429 Too Many Requests`, y simulación en `MockAuthApi`.
- **Verificación:** Trazabilidad formal requisito → plan → tarea; consistencia con la constitución canónica (`.specify/memory/constitution.md` MO-PR04, MO-PR06, MO-PR10); sin secretos ni tokens reales.
- **Pruebas Gradle:** `NO EJECUTADAS` — fase puramente documental sin ejecución de código.
- **Pendiente:** mandato humano para la Fase B (implementación de DTOs, métodos en `AuthApi`/`AuthRepository`, simulación en `MockAuthApi`, `ForgotPasswordScreen` en Compose y pruebas unitarias con `kotlinx-coroutines-test`).

## 13. Evidencia — Fase A de `180-Restablecer-Contrasena` (documental)
- **Fecha:** 2026-11-10 · **Mandato:** solicitud humana para crear la especificación de restablecimiento de contraseña a partir del endpoint NEXUM-E006 (`POST /api/v1/auth/password/reset` [E], catálogo V6.0.2); **Fase A documental, sin cambios de código .kt ni push**.
- **Alcance:** solo documentación; **cero cambios en código `.kt`, Gradle o mocks de ejecución**.
- **Archivos creados (3):** `specs/180-Restablecer-Contrasena/spec.md` (v1.0.0, requisitos `M180-R01…R05`), `specs/180-Restablecer-Contrasena/plan.md` (v1.0.0, planes `N180-P01…P06`), `specs/180-Restablecer-Contrasena/task.md` (v1.0.0, tareas `N180-T01…T06` todas `- [ ]`).
- **Archivos modificados (2):** `specs/README.md` (enlace al mapa y actualización de progreso), `memoria.md` (esta sección).
- **Contenido:** Contrato NEXUM-E006 (`POST /api/v1/auth/password/reset`, `204 No Content`), payload `ResetPasswordRequest(resetToken, newPassword)`, validación local de contraseña (mínimo 8 caracteres alfanuméricos) y coincidencia estricta con confirmación, regla de seguridad que prohíbe el auto-login tras reseteo forzando inicio de sesión explícito (`AuthEffect.NavigateToLogin`), tratamiento de tokens expirados/consumidos de un solo uso con opción de re-solicitud, y simulación en `MockAuthApi`.
- **Verificación:** Trazabilidad formal requisito → plan → tarea; consistencia con la constitución canónica (`.specify/memory/constitution.md` MO-PR04, MO-PR06, MO-PR10, MO-PR15); sin secretos ni tokens reales.
- **Pruebas Gradle:** `NO EJECUTADAS` — fase puramente documental sin ejecución de código.
- **Pendiente:** mandato humano para la Fase B (implementación de DTOs, métodos en `AuthApi`/`AuthRepository`, simulación en `MockAuthApi`, `ResetPasswordScreen` en Compose y pruebas unitarias con `kotlinx-coroutines-test`).

## 14. Evidencia — Fase A de `190-Cambiar-Contrasena` (documental)
- **Fecha:** 2026-11-10 · **Mandato:** solicitud humana para crear la especificación de cambio autenticado de contraseña a partir del endpoint NEXUM-E007 (`POST /api/v1/auth/password/change` [S], catálogo V6.0.2); **Fase A documental, sin cambios de código .kt ni push**.
- **Alcance:** solo documentación; **cero cambios en código `.kt`, Gradle o mocks de ejecución**.
- **Archivos creados (3):** `specs/190-Cambiar-Contrasena/spec.md` (v1.0.0, requisitos `M190-R01…R05`), `specs/190-Cambiar-Contrasena/plan.md` (v1.0.0, planes `N190-P01…P06`), `specs/190-Cambiar-Contrasena/task.md` (v1.0.0, tareas `N190-T01…T06` todas `- [ ]`).
- **Archivos modificados (2):** `specs/README.md` (enlace al mapa y actualización de progreso), `memoria.md` (esta sección).
- **Contenido:** Contrato NEXUM-E007 (`POST /api/v1/auth/password/change`, `204 No Content`), payload `ChangePasswordRequest(currentPassword, newPassword)` con cabecera `Authorization: Bearer <accessToken>`, reautenticación obligatoria con contraseña actual, validación de contraseña nueva (≥8 caracteres alfanuméricos, distinta a la actual, coincidente con confirmación), purga inmediata de contraseñas de la memoria de la UI tras el éxito (MO-PR06, MO-PR10), revocación selectiva de sesiones remotas, y simulación en `MockAuthApi`.
- **Verificación:** Trazabilidad formal requisito → plan → tarea; consistencia con la constitución canónica (`.specify/memory/constitution.md` MO-PR04, MO-PR06, MO-PR10, MO-PR15); sin secretos ni tokens reales.
- **Pruebas Gradle:** `NO EJECUTADAS` — fase puramente documental sin ejecución de código.
- **Pendiente:** mandato humano para la Fase B (implementación de DTOs, métodos en `AuthApi`/`AuthRepository`, simulación en `MockAuthApi`, `ChangePasswordScreen` en Compose y pruebas unitarias con `kotlinx-coroutines-test`).

## 15. Evidencia — Fase A de `200-Sesiones-Activas` (documental)
- **Fecha:** 2026-11-10 · **Mandato:** solicitud humana para crear la especificación de consulta de sesiones activas a partir del endpoint NEXUM-E008 (`GET /api/v1/auth/sessions` [S], catálogo V6.0.2); **Fase A documental, sin cambios de código .kt ni push**.
- **Alcance:** solo documentación; **cero cambios en código `.kt`, Gradle o mocks de ejecución**.
- **Archivos creados (3):** `specs/200-Sesiones-Activas/spec.md` (v1.0.0, requisitos `M200-R01…R05`), `specs/200-Sesiones-Activas/plan.md` (v1.0.0, planes `N200-P01…P06`), `specs/200-Sesiones-Activas/task.md` (v1.0.0, tareas `N200-T01…T06` todas `- [ ]`).
- **Archivos modificados (2):** `specs/README.md` (enlace al mapa y actualización de progreso), `memoria.md` (esta sección).
- **Contenido:** Contrato NEXUM-E008 (`GET /api/v1/auth/sessions`, `200 OK`), payload `List<SessionItemResponse>` con `id`, `device`, `lastSeenAt`, `current`, regla estricta de cero fuga de secretos (prohibición de refresh tokens o hashes en la respuesta, MO-PR06, MO-PR10), estados MVI explícitos (`Loading`, `Content`, `Empty`, `Error`, `Offline`, MO-PR08), soporte de pull-to-refresh y badge de "Este dispositivo" para la sesión actual, y simulación en `MockAuthApi`.
- **Verificación:** Trazabilidad formal requisito → plan → tarea; consistencia con la constitución canónica (`.specify/memory/constitution.md` MO-PR04, MO-PR06, MO-PR08, MO-PR10, MO-PR15); sin secretos ni tokens reales.
- **Pruebas Gradle:** `NO EJECUTADAS` — fase puramente documental sin ejecución de código.
- **Pendiente:** mandato humano para la Fase B (implementación de DTOs, métodos en `AuthApi`/`AuthRepository`, simulación en `MockAuthApi`, `ActiveSessionsScreen` en Compose y pruebas unitarias con `kotlinx-coroutines-test`).

## 16. Evidencia — Fase A de `210-Revocar-Sesion` (documental)
- **Fecha:** 2026-11-10 · **Mandato:** solicitud humana para crear la especificación de revocación de sesión remota a partir del endpoint NEXUM-E009 (`DELETE /api/v1/auth/sessions/{sessionId}` [S], catálogo V6.0.2); **Fase A documental, sin cambios de código .kt ni push**.
- **Alcance:** solo documentación; **cero cambios en código `.kt`, Gradle o mocks de ejecución**.
- **Archivos creados (3):** `specs/210-Revocar-Sesion/spec.md` (v1.0.0, requisitos `M210-R01…R05`), `specs/210-Revocar-Sesion/plan.md` (v1.0.0, planes `N210-P01…P06`), `specs/210-Revocar-Sesion/task.md` (v1.0.0, tareas `N210-T01…T06` todas `- [ ]`).
- **Archivos modificados (2):** `specs/README.md` (enlace al mapa y actualización de progreso), `memoria.md` (esta sección).
- **Contenido:** Contrato NEXUM-E009 (`DELETE /api/v1/auth/sessions/{sessionId}`, `204 No Content`), cabecera `Authorization: Bearer <accessToken>` y parámetro de ruta `sessionId`, autorización estricta por titular (anti-IDOR), diálogo de confirmación accesible antes de revocar, actualización reactiva/optimista del listado de sesiones en `ActiveSessionsScreen`, branching seguro para purgar sesión local si se revoca el dispositivo actual (`TokenStorage.clear()`, MO-PR15), y simulación en `MockAuthApi`.
- **Verificación:** Trazabilidad formal requisito → plan → tarea; consistencia con la constitución canónica (`.specify/memory/constitution.md` MO-PR04, MO-PR06, MO-PR10, MO-PR15); sin secretos ni tokens reales.
- **Pruebas Gradle:** `NO EJECUTADAS` — fase puramente documental sin ejecución de código.
- **Pendiente:** mandato humano para la Fase B (implementación de métodos en `AuthApi`/`AuthRepository`, simulación en `MockAuthApi`, diálogo Compose y pruebas unitarias con `kotlinx-coroutines-test`).

## 17. Evidencia — Fase A de `220-Solicitud-Verificacion-Email` (documental)
- **Fecha:** 2026-11-11 · **Mandato:** solicitud humana para crear la especificación de solicitud de verificación de correo a partir del endpoint NEXUM-E010 (`POST /api/v1/auth/email-verification/request` [S], catálogo V6.0.2); **Fase A documental, sin cambios de código .kt ni push**.
- **Alcance:** solo documentación; **cero cambios en código `.kt`, Gradle o mocks de ejecución**.
- **Archivos creados (3):** `specs/220-Solicitud-Verificacion-Email/spec.md` (v1.0.0, requisitos `M220-R01…R05`), `specs/220-Solicitud-Verificacion-Email/plan.md` (v1.0.0, planes `N220-P01…P06`), `specs/220-Solicitud-Verificacion-Email/task.md` (v1.0.0, tareas `N220-T01…T06` todas `- [ ]`).
- **Archivos modificados (2):** `specs/README.md` (enlace al mapa y actualización de progreso), `memoria.md` (esta sección).
- **Contenido:** Contrato NEXUM-E010 (`POST /api/v1/auth/email-verification/request`, `202 Accepted`), soporte dual (con `Authorization: Bearer <accessToken>` o con payload `EmailVerificationRequest(email)`), regla de no enumeración de cuentas (respuesta neutra idéntica `EmailVerificationResponse(message)`, MO-PR06), temporizador de reintento en UI (*cooldown* de 60 s), manejo de `429 Too Many Requests`, y simulación en `MockAuthApi`.
- **Verificación:** Trazabilidad formal requisito → plan → tarea; consistencia con la constitución canónica (`.specify/memory/constitution.md` MO-PR04, MO-PR06, MO-PR10); sin secretos ni tokens reales.
- **Pruebas Gradle:** `NO EJECUTADAS` — fase puramente documental sin ejecución de código.
- **Pendiente:** mandato humano para la Fase B (implementación de DTOs, métodos en `AuthApi`/`AuthRepository`, simulación en `MockAuthApi`, `EmailVerificationRequestScreen` en Compose y pruebas unitarias con `kotlinx-coroutines-test`).

## 18. Evidencia — Fase A de `230-Confirmar-Verificacion-Email` (documental)
- **Fecha:** 2026-11-11 · **Mandato:** solicitud humana para crear la especificación de confirmación de verificación de correo a partir del endpoint NEXUM-E011 (`POST /api/v1/auth/email-verification/confirm` [S], catálogo V6.0.2); **Fase A documental, sin cambios de código .kt ni push**.
- **Alcance:** solo documentación; **cero cambios en código `.kt`, Gradle o mocks de ejecución**.
- **Archivos creados (3):** `specs/230-Confirmar-Verificacion-Email/spec.md` (v1.0.0, requisitos `M230-R01…R05`), `specs/230-Confirmar-Verificacion-Email/plan.md` (v1.0.0, planes `N230-P01…P06`), `specs/230-Confirmar-Verificacion-Email/task.md` (v1.0.0, tareas `N230-T01…T06` todas `- [ ]`).
- **Archivos modificados (2):** `specs/README.md` (enlace al mapa y actualización de progreso), `memoria.md` (esta sección).
- **Contenido:** Contrato NEXUM-E011 (`POST /api/v1/auth/email-verification/confirm`, `204 No Content`), payload `ConfirmEmailVerificationRequest(verificationToken)`, consumo e invalidación de token de un solo uso en servidor (anti-replay), soporte de deep link (`https://app.nexum.mx/verify-email?token=...`) o entrada manual, branching de navegación contextual (`AuthEffect.NavigateToApp` si hay sesión activa, o `AuthEffect.NavigateToLogin` si no hay sesión), tratamiento de token expirado con enlace hacia reenvío (`220-Solicitud-Verificacion-Email`), y simulación en `MockAuthApi`.
- **Verificación:** Trazabilidad formal requisito → plan → tarea; consistencia con la constitución canónica (`.specify/memory/constitution.md` MO-PR04, MO-PR06, MO-PR10, MO-PR15); sin secretos ni tokens reales.
- **Pruebas Gradle:** `NO EJECUTADAS` — fase puramente documental sin ejecución de código.
- **Pendiente:** mandato humano para la Fase B (implementación de DTOs, métodos en `AuthApi`/`AuthRepository`, simulación en `MockAuthApi`, `ConfirmEmailVerificationScreen` en Compose y pruebas unitarias con `kotlinx-coroutines-test`).

## 19. Evidencia — Fase A de `240-Perfil-Usuario` (documental)
- **Fecha:** 2026-11-11 · **Mandato:** solicitud humana para crear la especificación de consulta de perfil de usuario a partir del endpoint NEXUM-E012 (`GET /api/v1/users/me` [M], catálogo V6.0.2); **Fase A documental, sin cambios de código .kt ni push**.
- **Alcance:** solo documentación; **cero cambios en código `.kt`, Gradle o mocks de ejecución**.
- **Archivos creados (3):** `specs/240-Perfil-Usuario/spec.md` (v1.0.0, requisitos `M240-R01…R05`), `specs/240-Perfil-Usuario/plan.md` (v1.0.0, planes `N240-P01…P06`), `specs/240-Perfil-Usuario/task.md` (v1.0.0, tareas `N240-T01…T06` todas `- [ ]`).
- **Archivos modificados (2):** `specs/README.md` (enlace al mapa y actualización de progreso), `memoria.md` (esta sección).
- **Contenido:** Contrato NEXUM-E012 (`GET /api/v1/users/me`, `200 OK`), payload `UserProfileResponse(id, name, email, emailVerified, createdAt)`, derivación estricta de identidad mediante el JWT Bearer (prohibición de enviar `userId` por parámetro, anti-IDOR, MO-PR06), estados MVI de perfil con pull-to-refresh y preservación de datos ante caída de red (MO-PR08), badge visual según el estado de `emailVerified` con enlace hacia reenvío de verificación (`220-Solicitud-Verificacion-Email`), y simulación en `MockAuthApi`.
- **Verificación:** Trazabilidad formal requisito → plan → tarea; consistencia con la constitución canónica (`.specify/memory/constitution.md` MO-PR04, MO-PR06, MO-PR07, MO-PR08); sin secretos ni tokens reales.
- **Pruebas Gradle:** `NO EJECUTADAS` — fase puramente documental sin ejecución de código.
- **Pendiente:** mandato humano para la Fase B (implementación de DTOs, métodos en `AuthApi`/`AuthRepository`, simulación en `MockAuthApi`, `ProfileScreen` en Compose y pruebas unitarias con `kotlinx-coroutines-test`).

## 20. Evidencia — Fase A de `250-Editar-Perfil` (documental)
- **Fecha:** 2026-11-11 · **Mandato:** solicitud humana para crear la especificación de edición de perfil de usuario a partir del endpoint NEXUM-E013 (`PATCH /api/v1/users/me` [S], catálogo V6.0.2); **Fase A documental, sin cambios de código .kt ni push**.
- **Alcance:** solo documentación; **cero cambios en código `.kt`, Gradle o mocks de ejecución**.
- **Archivos creados (3):** `specs/250-Editar-Perfil/spec.md` (v1.0.0, requisitos `M250-R01…R05`), `specs/250-Editar-Perfil/plan.md` (v1.0.0, planes `N250-P01…P06`), `specs/250-Editar-Perfil/task.md` (v1.0.0, tareas `N250-T01…T06` todas `- [ ]`).
- **Archivos modificados (2):** `specs/README.md` (enlace al mapa y actualización de progreso), `memoria.md` (esta sección).
- **Contenido:** Contrato NEXUM-E013 (`PATCH /api/v1/users/me`, `200 OK`), payload `UpdateProfileRequest(name)` con cabecera `Authorization: Bearer <accessToken>`, validación local de nombre (2..50 caracteres), control de habilitación de botón mediante detección de cambios frente al valor actual, sincronización atómica del nuevo nombre en `Session.user` de `TokenStorage` (MO-PR04, MO-PR15), componente `EditProfileDialog` en Compose, y simulación en `MockAuthApi`.
- **Verificación:** Trazabilidad formal requisito → plan → tarea; consistencia con la constitución canónica (`.specify/memory/constitution.md` MO-PR04, MO-PR06, MO-PR07, MO-PR08); sin secretos ni tokens reales.
- **Pruebas Gradle:** `NO EJECUTADAS` — fase puramente documental sin ejecución de código.
- **Pendiente:** mandato humano para la Fase B (implementación de DTOs, métodos en `AuthApi`/`AuthRepository`, simulación en `MockAuthApi`, `EditProfileDialog` en Compose y pruebas unitarias con `kotlinx-coroutines-test`).

## 21. Evidencia — Fase A de `260-Eliminar-Cuenta` (documental)
- **Fecha:** 2026-11-11 · **Mandato:** solicitud humana para crear la especificación de eliminación definitiva de cuenta a partir del endpoint NEXUM-E014 (`DELETE /api/v1/users/me` [M], catálogo V6.0.2); **Fase A documental, sin cambios de código .kt ni push**.
- **Alcance:** solo documentación; **cero cambios en código `.kt`, Gradle o mocks de ejecución**.
- **Archivos creados (3):** `specs/260-Eliminar-Cuenta/spec.md` (v1.0.0, requisitos `M260-R01…R06`), `specs/260-Eliminar-Cuenta/plan.md` (v1.0.0, planes `N260-P01…P06`), `specs/260-Eliminar-Cuenta/task.md` (v1.0.0, tareas `N260-T01…T06` todas `- [ ]`).
- **Archivos modificados (2):** `specs/README.md` (enlace al mapa y actualización de progreso), `memoria.md` (esta sección).
- **Contenido:** Contrato NEXUM-E014 (`DELETE /api/v1/users/me`, respuesta síncrona `204 No Content` o asíncrona `202 Accepted` con `deletionRequestId`), cabecera `Authorization: Bearer <accessToken>` y cabecera opcional `Idempotency-Key`, payload `DeleteAccountRequest(password, confirmation = "ELIMINAR")`, doble confirmación estricta en UI mediante diálogo modal destructivo con advertencias de irreversibilidad, control reactivo de habilitación del botón ("Eliminar definitivamente"), purga incondicional de credenciales en `TokenStorage` (fail-secure, MO-PR06, MO-PR15) tras éxito o conflicto 409, navegación destructiva a Login (`AuthEffect.NavigateToLogin`), tratamiento local aislado para modo demo/invitado (sin peticiones HTTP ni contraseñas de red), manejo resiliente de error de credenciales (`401 Unauthorized`) sin cerrar sesión, y simulación en `MockAuthApi`.
- **Verificación:** Trazabilidad formal requisito → plan → tarea; consistencia con la constitución canónica (`.specify/memory/constitution.md` MO-PR04, MO-PR06, MO-PR07, MO-PR10, MO-PR15, MO-PR17); sin secretos ni tokens reales.
- **Pruebas Gradle:** `NO EJECUTADAS` — fase puramente documental sin ejecución de código.
- **Pendiente:** mandato humano para la Fase B (implementación de DTOs, métodos en `AuthApi`/`AuthRepository`, simulación en `MockAuthApi`, `DeleteAccountDialog` en Compose y pruebas unitarias con `kotlinx-coroutines-test`).

## 22. Evidencia — Fase A de `270-Aviso-Privacidad-Vigente` (documental)
- **Fecha:** 2026-11-11 · **Mandato:** solicitud humana para crear la especificación de consulta del aviso de privacidad legalmente vigente a partir del endpoint NEXUM-E015 (`GET /api/v1/privacy/notices/current` [M], catálogo V6.0.2); **Fase A documental, sin cambios de código .kt ni push**.
- **Alcance:** solo documentación; **cero cambios en código `.kt`, Gradle o mocks de ejecución**.
- **Archivos creados (3):** `specs/270-Aviso-Privacidad-Vigente/spec.md` (v1.0.0, requisitos `M270-R01…R06`), `specs/270-Aviso-Privacidad-Vigente/plan.md` (v1.0.0, planes `N270-P01…P06`), `specs/270-Aviso-Privacidad-Vigente/task.md` (v1.0.0, tareas `N270-T01…T06` todas `- [ ]`).
- **Archivos modificados (2):** `specs/README.md` (enlace al mapa y actualización de progreso), `memoria.md` (esta sección).
- **Contenido:** Contrato NEXUM-E015 (`GET /api/v1/privacy/notices/current`, `200 OK`), parámetro de consulta opcional `locale` (`es-MX`), payload `PrivacyNoticeResponse(version, title, body, publishedAt)`, endpoint público no autenticado consultable tanto antes de iniciar sesión (`RegisterScreen > Ver aviso`) como en perfil autenticado (`ProfileScreen > Privacidad`), veracidad del tratamiento de datos en backend eliminando la afirmación simulada de la demo local (MO-PR04, MO-PR06), suministro y propagación del campo `version` para su incorporación en el alta (`POST /api/v1/auth/register`), resiliencia y fallback con copia local de respaldo ante modo fuera de línea (MO-PR08), accesibilidad tipográfica y de lectura en pantalla (MO-PR07), y simulación en `MockAuthApi`.
- **Verificación:** Trazabilidad formal requisito → plan → tarea; consistencia con la constitución canónica (`.specify/memory/constitution.md` MO-PR04, MO-PR06, MO-PR07, MO-PR08, MO-PR17); sin secretos ni tokens reales.
- **Pruebas Gradle:** `NO EJECUTADAS` — fase puramente documental sin ejecución de código.
- **Pendiente:** mandato humano para la Fase B (implementación de DTOs, métodos en `AuthApi`/`AuthRepository`, simulación en `MockAuthApi`, `PrivacyNoticeScreen` en Compose y pruebas unitarias con `kotlinx-coroutines-test`).

## 23. Evidencia — Fase A de `280-Historial-Consentimientos` (documental)
- **Fecha:** 2026-11-11 · **Mandato:** solicitud humana para crear la especificación de consulta del historial de consentimientos del usuario a partir del endpoint NEXUM-E016 (`GET /api/v1/users/me/consents` [S], catálogo V6.0.2); **Fase A documental, sin cambios de código .kt ni push**.
- **Alcance:** solo documentación; **cero cambios en código `.kt`, Gradle o mocks de ejecución**.
- **Archivos creados (3):** `specs/280-Historial-Consentimientos/spec.md` (v1.0.0, requisitos `M280-R01…R06`), `specs/280-Historial-Consentimientos/plan.md` (v1.0.0, planes `N280-P01…P06`), `specs/280-Historial-Consentimientos/task.md` (v1.0.0, tareas `N280-T01…T06` todas `- [ ]`).
- **Archivos modificados (2):** `specs/README.md` (enlace al mapa y actualización de progreso), `memoria.md` (esta sección).
- **Contenido:** Contrato NEXUM-E016 (`GET /api/v1/users/me/consents`, `200 OK`), cabecera `Authorization: Bearer <accessToken>`, payload `List<ConsentItemResponse>` con `noticeVersion`, `acceptedAt`, `source`, inmutabilidad estricta y carácter de solo lectura de la bitácora auditable de consentimientos (prohibición de alteración retroactiva, MO-PR04, MO-PR06), derivación estricta de identidad mediante el token JWT sin aceptar `userId` ajeno (anti-IDOR), formateo temporal de timestamps UTC a la zona horaria local del dispositivo (MO-PR07), estados MVI con soporte de pull-to-refresh y conservación de memoria ante fallo de red (MO-PR08), y simulación en `MockAuthApi`.
- **Verificación:** Trazabilidad formal requisito → plan → tarea; consistencia con la constitución canónica (`.specify/memory/constitution.md` MO-PR04, MO-PR06, MO-PR07, MO-PR08); sin secretos ni tokens reales.
- **Pruebas Gradle:** `NO EJECUTADAS` — fase puramente documental sin ejecución de código.
- **Pendiente:** mandato humano para la Fase B (implementación de DTOs, métodos en `AuthApi`/`AuthRepository`, simulación en `MockAuthApi`, `ConsentHistoryScreen` en Compose y pruebas unitarias con `kotlinx-coroutines-test`).

## 24. Evidencia — Fase A de `290-Registrar-Consentimiento` (documental)
- **Fecha:** 2026-11-11 · **Mandato:** solicitud humana para crear la especificación de registro de aceptación de nueva versión del aviso a partir del endpoint NEXUM-E017 (`POST /api/v1/users/me/consents` [S], catálogo V6.0.2); **Fase A documental, sin cambios de código .kt ni push**.
- **Alcance:** solo documentación; **cero cambios en código `.kt`, Gradle o mocks de ejecución**.
- **Archivos creados (3):** `specs/290-Registrar-Consentimiento/spec.md` (v1.0.0, requisitos `M290-R01…R06`), `specs/290-Registrar-Consentimiento/plan.md` (v1.0.0, planes `N290-P01…P06`), `specs/290-Registrar-Consentimiento/task.md` (v1.0.0, tareas `N290-T01…T06` todas `- [ ]`).
- **Archivos modificados (2):** `specs/README.md` (enlace al mapa y actualización de progreso), `memoria.md` (esta sección).
- **Contenido:** Contrato NEXUM-E017 (`POST /api/v1/users/me/consents`, `201 Created`), cabecera `Authorization: Bearer <accessToken>` y cabecera opcional `Idempotency-Key`, payload `RecordConsentRequest(noticeVersion, accepted = true)`, respuesta `RecordConsentResponse(consentId, recordedAt)`, prevención estricta de envíos duplicados mediante validación contra historial local (`280`) y absorción resiliente de `409 Conflict` como éxito idempotente (MO-PR04, MO-PR06), diálogo modal de aceptación `UpdateConsentDialog` con Material 3 y control reactivo de habilitación, propagación inmediata del nuevo consentimiento al historial en memoria, y simulación en `MockAuthApi`.
- **Verificación:** Trazabilidad formal requisito → plan → tarea; consistencia con la constitución canónica (`.specify/memory/constitution.md` MO-PR04, MO-PR06, MO-PR07, MO-PR08); sin secretos ni tokens reales.
- **Pruebas Gradle:** `NO EJECUTADAS` — fase puramente documental sin ejecución de código.
- **Pendiente:** mandato humano para la Fase B (implementación de DTOs, métodos en `AuthApi`/`AuthRepository`, simulación en `MockAuthApi`, `UpdateConsentDialog` en Compose y pruebas unitarias con `kotlinx-coroutines-test`).

## 25. Evidencia — Fase A de `300-Estado-Onboarding` (documental)
- **Fecha:** 2026-11-11 · **Mandato:** solicitud humana para crear la especificación de consulta de estado y progreso de onboarding a partir del endpoint NEXUM-E018 (`GET /api/v1/users/me/onboarding` [S], catálogo V6.0.2); **Fase A documental, sin cambios de código .kt ni push**.
- **Alcance:** solo documentación; **cero cambios en código `.kt`, Gradle o mocks de ejecución**.
- **Archivos creados (3):** `specs/300-Estado-Onboarding/spec.md` (v1.0.0, requisitos `M300-R01…R06`), `specs/300-Estado-Onboarding/plan.md` (v1.0.0, planes `N300-P01…P06`), `specs/300-Estado-Onboarding/task.md` (v1.0.0, tareas `N300-T01…T06` todas `- [ ]`).
- **Archivos modificados (2):** `specs/README.md` (enlace al mapa y actualización de progreso), `memoria.md` (esta sección).
- **Contenido:** Contrato NEXUM-E018 (`GET /api/v1/users/me/onboarding`, `200 OK`), cabecera `Authorization: Bearer <accessToken>`, payload `OnboardingStatusResponse(stepsCompleted, finishedAt)`, desacoplamiento explícito entre el estado remoto de onboarding y la gobernanza nativa de permisos en Android (el permiso de micrófono y notificaciones se comprueba en el sistema operativo, MO-PR09), enrutamiento determinista de arranque a `HomeScreen` si `finishedAt != null` o a `OnboardingScreen` si está pendiente, resiliencia offline con uso de caché local persistida para evitar bloqueos en zonas sin cobertura (MO-PR08), y simulación en `MockAuthApi`.
- **Verificación:** Trazabilidad formal requisito → plan → tarea; consistencia con la constitución canónica (`.specify/memory/constitution.md` MO-PR04, MO-PR06, MO-PR07, MO-PR08, MO-PR09); sin secretos ni tokens reales.
- **Pruebas Gradle:** `NO EJECUTADAS` — fase puramente documental sin ejecución de código.
- **Pendiente:** mandato humano para la Fase B (implementación de DTOs, métodos en `AuthApi`/`AuthRepository`, simulación en `MockAuthApi`, integración de enrutamiento en Compose y pruebas unitarias con `kotlinx-coroutines-test`).

## 26. Evidencia — Fase A de `310-Completar-Onboarding` (documental)
- **Fecha:** 2026-11-11 · **Mandato:** solicitud humana para crear la especificación de finalización de onboarding a partir del endpoint NEXUM-E019 (`PATCH /api/v1/users/me/onboarding` [S], catálogo V6.0.2); **Fase A documental, sin cambios de código .kt ni push**.
- **Alcance:** solo documentación; **cero cambios en código `.kt`, Gradle o mocks de ejecución**.
- **Archivos creados (3):** `specs/310-Completar-Onboarding/spec.md` (v1.0.0, requisitos `M310-R01…R06`), `specs/310-Completar-Onboarding/plan.md` (v1.0.0, planes `N310-P01…P06`), `specs/310-Completar-Onboarding/task.md` (v1.0.0, tareas `N310-T01…T06` todas `- [ ]`).
- **Archivos modificados (2):** `specs/README.md` (enlace al mapa y actualización de progreso), `memoria.md` (esta sección).
- **Contenido:** Contrato NEXUM-E019 (`PATCH /api/v1/users/me/onboarding`, `200 OK`), cabecera `Authorization: Bearer <accessToken>` y cabecera opcional `Idempotency-Key`, payload `UpdateOnboardingRequest(completed = true, limitedMode = false/true)`, disparadores en UI con botones accesibles "Continuar a Nexum" y "Continuar con funciones limitadas" (MO-PR07), no suplantación de permisos de Android (los flags remotos no conceden permisos de micrófono o notificaciones en el SO, MO-PR09), avance no bloqueante con fail-open local ante fallo de red (MO-PR08), navegación definitiva con desapilado de Compose Navigation (`NavigateToHome`), y simulación en `MockAuthApi`.
- **Verificación:** Trazabilidad formal requisito → plan → tarea; consistencia con la constitución canónica (`.specify/memory/constitution.md` MO-PR04, MO-PR06, MO-PR07, MO-PR08, MO-PR09); sin secretos ni tokens reales.
- **Pruebas Gradle:** `NO EJECUTADAS` — fase puramente documental sin ejecución de código.
- **Pendiente:** mandato humano para la Fase B (implementación de DTOs, métodos en `AuthApi`/`AuthRepository`, simulación en `MockAuthApi`, integración en `OnboardingScreen` y pruebas unitarias con `kotlinx-coroutines-test`).

## 27. Evidencia — Corrección de compilación de `130-Registro-Usuario` (2026-11-11)
- **Fecha:** 2026-11-11 · **Mandato:** solicitud humana para corregir el error de compilación del registro y dejar la app compilable; alcance limitado a `130-Registro-Usuario`.
- **Causa:** migración incompleta del commit `6c7e2f3`; `RegisterUiState.incomeFrequency` y `ExpenseDraft.type` cambiaron a `String`, pero el `AuthViewModel` heredado aún los pasaba a `Income.frequency` (`Frequency`) y `RecurringExpense.type` (`ExpenseType`).
- **Archivos eliminados (2):** `app/src/main/java/mx/nexum/app/auth/ui/AuthViewModel.kt` (código muerto; `AuthFlow` ya usa `RegisterViewModel`) y `app/src/test/java/mx/nexum/app/auth/AuthViewModelTest.kt` (pruebas del flujo mock anterior, reemplazadas por `RegisterViewModelTest`).
- **Archivos modificados (2):** `app/src/main/java/mx/nexum/app/auth/ui/AuthUiState.kt` (imports sin uso) y `app/src/test/java/mx/nexum/app/auth/RegisterViewModelTest.kt` (tipado explícito de `Response.error<RegisterResponseDto>` y eliminación de un `let` redundante en la prueba 429).
- **Comandos y resultados reales:** `JAVA_HOME=C:\Users\demon\AppData\Local\Programs\Android Studio\jbr`; `.\gradlew.bat assembleDebug testDebugUnitTest` → **BUILD SUCCESSFUL** · 51 tests · 0 fallos (`AuthValidatorTest` 19, `LoginViewModelTest` 9, `MockAuthApiTest` 11, `RegisterViewModelTest` 11, `ExampleUnitTest` 1).
- **Limitaciones:** sin push, sin emulador ni pruebas de instrumentación; la verificación visual manual de la pantalla queda pendiente.

## 28. Evidencia — Fase A de `320-Exportar-Datos-Usuario` (documental)
- **Fecha:** 2026-11-12 · **Mandato:** solicitud humana para crear la especificación de exportación de datos del titular a partir del endpoint NEXUM-E020 (`GET /api/v1/users/me/export` [S], catálogo V6.0.2); **Fase A documental, sin cambios de código .kt ni push**.
- **Alcance:** solo documentación; **cero cambios en código `.kt`, Gradle o mocks de ejecución**.
- **Archivos creados (3):** `specs/320-Exportar-Datos-Usuario/spec.md` (v1.0.0, requisitos `M320-R01…R06`), `specs/320-Exportar-Datos-Usuario/plan.md` (v1.0.0, planes `N320-P01…P06`), `specs/320-Exportar-Datos-Usuario/task.md` (v1.0.0, tareas `N320-T01…T06` todas `- [ ]`).
- **Archivos modificados (2):** `specs/README.md` (enlace al mapa y actualización de progreso), `memoria.md` (esta sección).
- **Contenido:** Contrato NEXUM-E020 (`GET /api/v1/users/me/export`, `200 OK` con archivo o `202 Accepted` con `exportJobId`), parámetro `format=json|csv`, cabecera `Authorization: Bearer <accessToken>`, modelos `ExportFormat`/`ExportReadyResponse`/`ExportPendingResponse`/`ExportOutcome`, autorización y limitación de alcance por titular sin enviar `userId` (MO-PR04, MO-PR06), tratamiento del sobre de errores `{"error":{"code","message","details","traceId"}}` con estados MVI de carga, archivo listo, trabajo diferido, error y sin conexión (MO-PR08), descarte del contenido exportado sin persistencia ni logs (MO-PR10), y simulación en `MockAuthApi` con JSON/CSV sintéticos, montos como cadena decimal, moneda `MXN`, fechas RFC 3339 UTC, escenario `202` y centinela `net@error.test`.
- **Verificación:** Trazabilidad formal requisito → plan → tarea; consistencia con la constitución canónica (`.specify/memory/constitution.md` MO-PR04, MO-PR06, MO-PR07, MO-PR08, MO-PR10, MO-PR18); enlaces relativos a carpetas existentes; sin secretos ni tokens reales.
- **Pruebas Gradle:** `NO EJECUTADAS` — fase puramente documental sin ejecución de código.
- **Pendiente:** mandato humano para la Fase B (implementación de DTOs, métodos en `AuthApi`/`AuthRepository`, simulación en `MockAuthApi`, `ExportDataViewModel`, sección de exportación en `ProfileScreen` y pruebas unitarias con `kotlinx-coroutines-test`); endpoint de consulta del trabajo diferido, URL temporal y detalle de campos exportados **por confirmar con Backend**.

## 29. Evidencia — Fase A de `330-Bootstrap-Inicial` (documental)
- **Fecha:** 2026-11-12 · **Mandato:** solicitud humana para crear la especificación de carga inicial agregada a partir del endpoint NEXUM-E021 (`GET /api/v1/bootstrap` [S], catálogo V6.0.2); **Fase A documental, sin cambios de código .kt ni push**.
- **Alcance:** solo documentación; **cero cambios en código `.kt`, Gradle o mocks de ejecución**.
- **Archivos creados (3):** `specs/330-Bootstrap-Inicial/spec.md` (v1.0.0, requisitos `M330-R01…R06`), `specs/330-Bootstrap-Inicial/plan.md` (v1.0.0, planes `N330-P01…P06`), `specs/330-Bootstrap-Inicial/task.md` (v1.0.0, tareas `N330-T01…T06` todas `- [ ]`).
- **Archivos modificados (2):** `specs/README.md` (enlace al mapa y actualización de progreso), `memoria.md` (esta sección).
- **Contenido:** Contrato NEXUM-E021 (`GET /api/v1/bootstrap`, `200 OK` con `profile`, `summary`, `streak`, `config` y `counts`), parámetro opcional `include=summary,profile,preferences`, cabecera `Authorization: Bearer <accessToken>`, modelos `BootstrapResponse`/`BootstrapSummaryResponse`/`BootstrapSnapshot`, regla de agregador de solo lectura que no sustituye endpoints especializados de escritura (MO-PR04), identidad por JWT sin `userId` (MO-PR06), degradación parcial con secciones nulas sin inventar datos y caché de lectura etiquetada (MO-PR08), estados MVI de carga, contenido, parcial, error y sin conexión, y simulación en `MockAuthApi` con instantánea determinista, filtro `include`, montos como cadena decimal, moneda `MXN`, fechas RFC 3339 UTC y centinela `net@error.test`.
- **Verificación:** Trazabilidad formal requisito → plan → tarea; consistencia con la constitución canónica (`.specify/memory/constitution.md` MO-PR04, MO-PR05, MO-PR06, MO-PR07, MO-PR08, MO-PR12); enlaces relativos a carpetas existentes; sin secretos ni tokens reales.
- **Pruebas Gradle:** `NO EJECUTADAS` — fase puramente documental sin ejecución de código.
- **Pendiente:** mandato humano para la Fase B (implementación de DTOs, métodos en `AuthApi`/`AuthRepository`, simulación en `MockAuthApi`, `HomeViewModel`, `HomeScreen` y pruebas unitarias con `kotlinx-coroutines-test`); estructura interna de `streak`, `config`, `counts`, contenido detallado de `summary` y semántica de `include` **por confirmar con Backend**.

## 30. Evidencia — Fase A de `340-Catalogo-Categorias` (documental)
- **Fecha:** 2026-11-12 · **Mandato:** solicitud humana para crear la especificación del catálogo de categorías a partir del endpoint NEXUM-E022 (`GET /api/v1/catalogs/categories` [M], catálogo V6.0.2); **Fase A documental, sin cambios de código .kt ni push**.
- **Alcance:** solo documentación; **cero cambios en código `.kt`, Gradle o mocks de ejecución**.
- **Archivos creados (3):** `specs/340-Catalogo-Categorias/spec.md` (v1.0.0, requisitos `M340-R01…R06`), `specs/340-Catalogo-Categorias/plan.md` (v1.0.0, planes `N340-P01…P06`), `specs/340-Catalogo-Categorias/task.md` (v1.0.0, tareas `N340-T01…T06` todas `- [ ]`).
- **Archivos modificados (2):** `specs/README.md` (enlace al mapa y actualización de progreso), `memoria.md` (esta sección).
- **Contenido:** Contrato NEXUM-E022 (`GET /api/v1/catalogs/categories`, `200 OK` con `[{id,name,icon,active}]`), parámetro opcional `locale`, modelos `CategoryResponse`/`Category`/`CategoryCatalog`, regla de identificador estable donde el cliente envía solo `categoryId` y nunca el nombre visible (MO-PR04, MO-PR06), catálogo documentado de nueve categorías, exclusión de categorías inactivas, respaldo local con datos maestros empaquetados y origen `REMOTE`/`CACHE`/`BUNDLED` (MO-PR08), estados MVI de carga, contenido, vacío, error y sin conexión, componente `CategorySelector` accesible (MO-PR07) y simulación en `MockAuthApi` con retardo, caso inactivo y centinela `net@error.test`.
- **Verificación:** Trazabilidad formal requisito → plan → tarea; consistencia con la constitución canónica (`.specify/memory/constitution.md` MO-PR04, MO-PR06, MO-PR07, MO-PR08, MO-PR12); enlaces relativos a carpetas existentes; sin secretos ni tokens reales.
- **Pruebas Gradle:** `NO EJECUTADAS` — fase puramente documental sin ejecución de código.
- **Pendiente:** mandato humano para la Fase B (implementación de DTOs, métodos en `AuthApi`/`AuthRepository`, simulación en `MockAuthApi`, `CategoryCatalogViewModel`, `CategorySelector` y pruebas unitarias con `kotlinx-coroutines-test`); identificadores definitivos distintos de `antojos`, formato de `icon`, exigencia de autenticación y TTL de caché **por confirmar con Backend**.

## 31. Evidencia — Fase A de `350-Catalogo-Frecuencias` (documental)
- **Fecha:** 2026-11-12 · **Mandato:** solicitud humana para crear la especificación del catálogo de frecuencias a partir del endpoint NEXUM-E023 (`GET /api/v1/catalogs/frequencies` [S], catálogo V6.0.2); **Fase A documental, sin cambios de código .kt ni push**.
- **Alcance:** solo documentación; **cero cambios en código `.kt`, Gradle o mocks de ejecución**.
- **Archivos creados (3):** `specs/350-Catalogo-Frecuencias/spec.md` (v1.0.0, requisitos `M350-R01…R06`), `specs/350-Catalogo-Frecuencias/plan.md` (v1.0.0, planes `N350-P01…P06`), `specs/350-Catalogo-Frecuencias/task.md` (v1.0.0, tareas `N350-T01…T06` todas `- [ ]`).
- **Archivos modificados (2):** `specs/README.md` (enlace al mapa y actualización de progreso), `memoria.md` (esta sección).
- **Contenido:** Contrato NEXUM-E023 (`GET /api/v1/catalogs/frequencies`, `200 OK` con `[{code,label,validFor}]`), parámetro `context=income|recurring|movement`, nueve códigos documentados (`once`, `daily`, `weekly`, `fortnightly`, `monthly`, `bimonthly`, `quarterly`, `semiannual`, `annual`), modelos `FrequencyContext`/`FrequencyResponse`/`FrequencyOption`/`FrequencyCatalog`, separación estricta entre equivalencia mensual estimada (`daily=365/12`, `weekly=52/12`, `fortnightly=2`, `monthly=1`, `bimonthly=1/2`, `quarterly=1/3`, `semiannual=1/6`, `annual=1/12`) y reglas de calendario (MO-PR05), vigencia de M-D08 con `monthly` como única frecuencia habilitada en el alta hasta confirmación con Backend, respaldo local de datos maestros (MO-PR08), estados MVI de carga, contenido, vacío, error y sin conexión, y simulación en `MockAuthApi` con los nueve códigos y centinela `net@error.test`.
- **Verificación:** Trazabilidad formal requisito → plan → tarea; consistencia con la constitución canónica (`.specify/memory/constitution.md` MO-PR04, MO-PR05, MO-PR06, MO-PR07, MO-PR08, MO-PR18); enlaces relativos a carpetas existentes; sin secretos ni tokens reales.
- **Pruebas Gradle:** `NO EJECUTADAS` — fase puramente documental sin ejecución de código.
- **Pendiente:** mandato humano para la Fase B (implementación de DTOs, métodos en `AuthApi`/`AuthRepository`, simulación en `MockAuthApi`, `FrequencyCatalogViewModel`, `FrequencySelector` y pruebas unitarias con `kotlinx-coroutines-test`); forma de `validFor`, etiquetas localizadas, reglas de calendario y exigencia de autenticación **por confirmar con Backend**; M-D08 permanece vigente.

## 32. Evidencia — Fase A de `360-Consultar-Ingreso` (documental)
- **Fecha:** 2026-11-12 · **Mandato:** solicitud humana para crear la especificación de consulta del ingreso habitual a partir del endpoint NEXUM-E024 (`GET /api/v1/income` [M], catálogo V6.0.2); **Fase A documental, sin cambios de código .kt ni push**.
- **Alcance:** solo documentación; **cero cambios en código `.kt`, Gradle o mocks de ejecución**.
- **Archivos creados (3):** `specs/360-Consultar-Ingreso/spec.md` (v1.0.0, requisitos `M360-R01…R06`), `specs/360-Consultar-Ingreso/plan.md` (v1.0.0, planes `N360-P01…P06`), `specs/360-Consultar-Ingreso/task.md` (v1.0.0, tareas `N360-T01…T06` todas `- [ ]`).
- **Archivos modificados (2):** `specs/README.md` (enlace al mapa y actualización de progreso), `memoria.md` (esta sección).
- **Contenido:** Contrato NEXUM-E024 (`GET /api/v1/income`, `200 OK` con `amount`, `frequency`, `monthlyEquivalent` y `currency`), cabecera `Authorization: Bearer <accessToken>`, modelos `IncomeResponse`/`Income` con importes como cadena decimal y moneda `MXN`, regla de dato privado de un solo titular con identidad derivada del JWT sin `userId` (MO-PR06), modelo singular de un ingreso habitual activo, presentación de `monthlyEquivalent` como estimación y no como depósito ni saldo (MO-PR05), estados MVI de carga, contenido, vacío, error y sin conexión con caché anterior etiquetada (MO-PR08), apertura del editor hacia `370-Actualizar-Ingreso`, y simulación en `MockAuthApi` con ingreso presente, ausente y centinela `net@error.test`.
- **Verificación:** Trazabilidad formal requisito → plan → tarea; consistencia con la constitución canónica (`.specify/memory/constitution.md` MO-PR04, MO-PR05, MO-PR06, MO-PR07, MO-PR08); enlaces relativos a carpetas existentes; sin secretos ni tokens reales.
- **Pruebas Gradle:** `NO EJECUTADAS` — fase puramente documental sin ejecución de código.
- **Pendiente:** mandato humano para la Fase B (implementación de DTOs, métodos en `AuthApi`/`AuthRepository`, simulación en `MockAuthApi`, `IncomeViewModel`, tarjeta en `ProfileScreen` y pruebas unitarias con `kotlinx-coroutines-test`); semántica de ausencia (`404` o cuerpo vacío), regla de redondeo, periodo del equivalente y zona horaria **por confirmar con Backend**.

## 33. Evidencia — Fase A de `370-Actualizar-Ingreso` (documental)
- **Fecha:** 2026-11-12 · **Mandato:** solicitud humana para crear la especificación de actualización del ingreso habitual a partir del endpoint NEXUM-E025 (`PUT /api/v1/income` [M], catálogo V6.0.2); **Fase A documental, sin cambios de código .kt ni push**.
- **Alcance:** solo documentación; **cero cambios en código `.kt`, Gradle o mocks de ejecución**.
- **Archivos creados (3):** `specs/370-Actualizar-Ingreso/spec.md` (v1.0.0, requisitos `M370-R01…R06`), `specs/370-Actualizar-Ingreso/plan.md` (v1.0.0, planes `N370-P01…P06`), `specs/370-Actualizar-Ingreso/task.md` (v1.0.0, tareas `N370-T01…T06` todas `- [ ]`).
- **Archivos modificados (2):** `specs/README.md` (enlace al mapa y actualización de progreso), `memoria.md` (esta sección).
- **Contenido:** Contrato NEXUM-E025 (`PUT /api/v1/income`, `200 OK` con ingreso actualizado y `monthlyEquivalent`), payload `{"amount":"12500.00","frequency":"monthly"}`, cabecera `Authorization: Bearer <accessToken>`, modelos `UpdateIncomeRequest`/`IncomeResponse`, validación con `BigDecimal` de `amount > 0` y dos decimales sin `Float`/`Double`, regla de reemplazo del ingreso habitual sin inventar depósitos ni movimientos (MO-PR04, MO-PR05), recálculo de equivalencia con los factores documentados, invalidación de la caché de resumen sin implementar `380`, vigencia de M-D08 con `monthly`, estados MVI de edición, guardando, guardado, error y sin conexión con conservación del formulario (MO-PR08), y simulación en `MockAuthApi` con validación, `422` y centinelas `net@error.test`/`rule_error_income`.
- **Verificación:** Trazabilidad formal requisito → plan → tarea; consistencia con la constitución canónica (`.specify/memory/constitution.md` MO-PR04, MO-PR05, MO-PR06, MO-PR07, MO-PR08); enlaces relativos a carpetas existentes; sin secretos ni tokens reales.
- **Pruebas Gradle:** `NO EJECUTADAS` — fase puramente documental sin ejecución de código.
- **Pendiente:** mandato humano para la Fase B (implementación de DTOs, métodos en `AuthApi`/`AuthRepository`, simulación en `MockAuthApi`, `UpdateIncomeViewModel`, editor en Compose y pruebas unitarias con `kotlinx-coroutines-test`); regla exacta de redondeo, conflicto concurrente (`409`/versión), forma exacta de la respuesta y consistencia del resumen **por confirmar con Backend**.

## 34. Evidencia — Fase A de `380-Resumen-Financiero` (documental)
- **Fecha:** 2026-11-12 · **Mandato:** solicitud humana para crear la especificación del resumen financiero mensual a partir del endpoint NEXUM-E026 (`GET /api/v1/financial-summary` [M], catálogo V6.0.2); **Fase A documental, sin cambios de código .kt ni push**.
- **Alcance:** solo documentación; **cero cambios en código `.kt`, Gradle o mocks de ejecución**.
- **Archivos creados (3):** `specs/380-Resumen-Financiero/spec.md` (v1.0.0, requisitos `M380-R01…R06`), `specs/380-Resumen-Financiero/plan.md` (v1.0.0, planes `N380-P01…P06`), `specs/380-Resumen-Financiero/task.md` (v1.0.0, tareas `N380-T01…T06` todas `- [ ]`).
- **Archivos modificados (2):** `specs/README.md` (enlace al mapa y actualización de progreso), `memoria.md` (esta sección).
- **Contenido:** Contrato NEXUM-E026 (`GET /api/v1/financial-summary`, `200 OK` con `incomeEquivalent`, `recurringFixedEstimate`, `oneTimeFixed`, `antExpenses`, `goalReservations`, `availableEstimate` y `currency`), parámetros `month=YYYY-MM` y `timezone` (si aplica), cabecera `Authorization: Bearer <accessToken>`, modelos `FinancialSummaryResponse`/`FinancialSummary`/`FinancialSummaryResult`, fórmula documentada `availableEstimate = incomeEquivalent - recurringFixedEstimate - oneTimeFixed - antExpenses - goalReservations` verificada de forma no destructiva con `BigDecimal` (MO-PR04, MO-PR05), regla de no duplicar un pago recurrente fijo al marcarlo pagado, etiqueta visible de estimación, estados MVI de carga, contenido, vacío, error y sin conexión con cancelación de la petición anterior al cambiar de mes (MO-PR08), y simulación en `MockAuthApi` con el ejemplo `2026-11` (`"12000.00"`, `"6500.00"`, `"0.00"`, `"350.00"`, `"200.00"`, `"4950.00"`, `"MXN"`), otros meses sintéticos y centinela `net@error.test`.
- **Verificación:** Trazabilidad formal requisito → plan → tarea; consistencia con la constitución canónica (`.specify/memory/constitution.md` MO-PR04, MO-PR05, MO-PR06, MO-PR07, MO-PR08, MO-PR12); enlaces relativos a carpetas existentes; sin secretos ni tokens reales.
- **Pruebas Gradle:** `NO EJECUTADAS` — fase puramente documental sin ejecución de código.
- **Pendiente:** mandato humano para la Fase B (implementación de DTOs, métodos en `AuthApi`/`AuthRepository`, simulación en `MockAuthApi`, `FinancialSummaryViewModel`, tarjeta de resumen en `HomeScreen` y pruebas unitarias con `kotlinx-coroutines-test`); formato de `timezone`, mes por defecto y rango consultable, composición exacta de cada importe y tolerancia de redondeo **por confirmar con Backend**.

## 35. Evidencia — Fase A de `390-Listar-Movimientos` (documental)
- **Fecha:** 2026-11-12 · **Mandato:** solicitud humana para crear la especificación del listado paginado de movimientos a partir del endpoint NEXUM-E027 (`GET /api/v1/movements` [M], catálogo V6.0.2); **Fase A documental, sin cambios de código .kt ni push**.
- **Alcance:** solo documentación; **cero cambios en código `.kt`, Gradle o mocks de ejecución**.
- **Archivos creados (3):** `specs/390-Listar-Movimientos/spec.md` (v1.0.0, requisitos `M390-R01…R06`), `specs/390-Listar-Movimientos/plan.md` (v1.0.0, planes `N390-P01…P06`), `specs/390-Listar-Movimientos/task.md` (v1.0.0, tareas `N390-T01…T06` todas `- [ ]`).
- **Archivos modificados (2):** `specs/README.md` (enlace al mapa y actualización de progreso), `memoria.md` (esta sección).
- **Contenido:** Contrato NEXUM-E027 (`GET /api/v1/movements`, `200 OK` con `items[]`, `pagination` y `totals`), parámetros `type=all|ant|fixed|saving`, `from`, `to`, `page`, `limit` y `sort`, cabecera `Authorization: Bearer <accessToken>`, modelos `MovementFilter`/`MovementItemResponse`/`MovementsPageResponse`/`MovementItem`/`MovementsPage`, aislamiento por JWT sin `userId` (MO-PR06), conservación de la fecha real del evento `occurredAt` y del origen (MO-PR05), paginación explícita con `page`/`limit` y contenedores provisionales para `pagination` y `totals` (estructura interna **por confirmar con Backend**), estados MVI de carga inicial, carga adicional, vacío, error y sin conexión con cancelación al cambiar filtro (MO-PR08), pestañas Todos/Hormiga/Obligatorios/Ahorro con filtro de rango, y simulación en `MockAuthApi` con historial determinista, paginación, filtros y centinela `net@error.test`.
- **Verificación:** Trazabilidad formal requisito → plan → tarea; consistencia con la constitución canónica (`.specify/memory/constitution.md` MO-PR04, MO-PR05, MO-PR06, MO-PR07, MO-PR08, MO-PR12); enlaces relativos a carpetas existentes; sin secretos ni tokens reales.
- **Pruebas Gradle:** `NO EJECUTADAS` — fase puramente documental sin ejecución de código.
- **Pendiente:** mandato humano para la Fase B (implementación de DTOs, métodos en `AuthApi`/`AuthRepository`, simulación en `MockAuthApi`, `MovementsListViewModel`, `MovementsScreen` y pruebas unitarias con `kotlinx-coroutines-test`); estructura de `pagination`/`totals`, valores de `sort`, formato de `from`/`to`, límite máximo de `limit` y significado de `saving` **por confirmar con Backend**.

## 36. Evidencia — Fase A de `400-Registrar-Movimiento` (documental)
- **Fecha:** 2026-11-12 · **Mandato:** solicitud humana para crear la especificación del alta de un gasto realizado a partir del endpoint NEXUM-E028 (`POST /api/v1/movements` [M], catálogo V6.0.2); **Fase A documental, sin cambios de código .kt ni push**.
- **Alcance:** solo documentación; **cero cambios en código `.kt`, Gradle o mocks de ejecución**.
- **Archivos creados (3):** `specs/400-Registrar-Movimiento/spec.md` (v1.0.0, requisitos `M400-R01…R06`), `specs/400-Registrar-Movimiento/plan.md` (v1.0.0, planes `N400-P01…P06`), `specs/400-Registrar-Movimiento/task.md` (v1.0.0, tareas `N400-T01…T06` todas `- [ ]`).
- **Archivos modificados (2):** `specs/README.md` (enlace al mapa y actualización de progreso), `memoria.md` (esta sección).
- **Contenido:** Contrato NEXUM-E028 (`POST /api/v1/movements`, `201 Created` con `movementId`, `record` y `updatedSummary`), payload `{"concept":"Gomitas","amount":"10.00","categoryId":"antojos","type":"ant","occurredAt":"2026-11-02T17:33:00Z"}` con `recurringPaymentId` opcional, cabecera `Authorization: Bearer <accessToken>`, cabecera `Idempotency-Key` para evitar duplicados por doble toque o reintento, modelos `CreateMovementRequest`/`CreateMovementResponse`/`MovementDraft`/`RegisteredMovement`, regla de gasto efectivamente realizado y no programación futura (MO-PR05), `amount > 0` con dos decimales validado con `BigDecimal` sin `Float`/`Double`, tipo limitado a `ant`/`fixed` y categoría validada por el servidor mediante `categoryId` (MO-PR04), captura manual y confirmación por voz con frecuencia única, estados MVI de edición, guardando, guardado, error y sin conexión con conservación del borrador (MO-PR08), y simulación en `MockAuthApi` con deduplicación por llave y centinelas `net@error.test`/`rule_error_movement`.
- **Verificación:** Trazabilidad formal requisito → plan → tarea; consistencia con la constitución canónica (`.specify/memory/constitution.md` MO-PR04, MO-PR05, MO-PR06, MO-PR07, MO-PR08, MO-PR12); enlaces relativos a carpetas existentes; sin secretos ni tokens reales.
- **Pruebas Gradle:** `NO EJECUTADAS` — fase puramente documental sin ejecución de código.
- **Pendiente:** mandato humano para la Fase B (implementación de DTOs, métodos en `AuthApi`/`AuthRepository`, simulación en `MockAuthApi`, `RegisterMovementViewModel`, formulario y confirmación por voz en Compose, y pruebas unitarias con `kotlinx-coroutines-test`); forma exacta de `record` y `updatedSummary`, vigencia de `Idempotency-Key`, origen de `occurredAt`, zona horaria y código de categoría inactiva **por confirmar con Backend**.

## 37. Evidencia — Fase A de `410-Detalle-Movimiento` (documental)
- **Fecha:** 2026-11-12 · **Mandato:** solicitud humana para crear la especificación del detalle de un movimiento a partir del endpoint NEXUM-E029 (`GET /api/v1/movements/{movementId}` [S], catálogo V6.0.2); **Fase A documental, sin cambios de código .kt ni push**.
- **Alcance:** solo documentación; **cero cambios en código `.kt`, Gradle o mocks de ejecución**.
- **Archivos creados (3):** `specs/410-Detalle-Movimiento/spec.md` (v1.0.0, requisitos `M410-R01…R06`), `specs/410-Detalle-Movimiento/plan.md` (v1.0.0, planes `N410-P01…P06`), `specs/410-Detalle-Movimiento/task.md` (v1.0.0, tareas `N410-T01…T06` todas `- [ ]`).
- **Archivos modificados (2):** `specs/README.md` (enlace al mapa y actualización de progreso), `memoria.md` (esta sección).
- **Contenido:** Contrato NEXUM-E029 (`GET /api/v1/movements/{movementId}`, `200 OK` con `movement`), cabecera `Authorization: Bearer <accessToken>`, modelos `MovementDetailResponse`/`MovementDetail` con importe como cadena decimal y vínculos opcionales a pago recurrente y meta, consulta puntual sin depender de la lista en memoria (MO-PR04), regla `404` si el movimiento no existe o no pertenece al titular sin revelar datos de terceros (MO-PR06), conservación de `occurredAt` y `origin` (MO-PR05), estados MVI de carga, contenido, no encontrado, error y sin conexión (MO-PR08), navegación al editor de `420-Editar-Movimiento`, y simulación en `MockAuthApi` con detalles sintéticos, `404` y centinela `net@error.test`.
- **Verificación:** Trazabilidad formal requisito → plan → tarea; consistencia con la constitución canónica (`.specify/memory/constitution.md` MO-PR04, MO-PR05, MO-PR06, MO-PR07, MO-PR08, MO-PR12); enlaces relativos a carpetas existentes; sin secretos ni tokens reales.
- **Pruebas Gradle:** `NO EJECUTADAS` — fase puramente documental sin ejecución de código.
- **Pendiente:** mandato humano para la Fase B (implementación de DTOs, métodos en `AuthApi`/`AuthRepository`, simulación en `MockAuthApi`, `MovementDetailViewModel`, `MovementDetailScreen` y pruebas unitarias con `kotlinx-coroutines-test`); forma exacta de `movement`, código de identificador inválido, datos incluidos en los vínculos y política de caché **por confirmar con Backend**.

## 38. Evidencia — Fase A de `420-Editar-Movimiento` (documental)
- **Fecha:** 2026-11-12 · **Mandato:** solicitud humana para crear la especificación de la edición de un movimiento a partir del endpoint NEXUM-E030 (`PATCH /api/v1/movements/{movementId}` [M], catálogo V6.0.2); **Fase A documental, sin cambios de código .kt ni push**.
- **Alcance:** solo documentación; **cero cambios en código `.kt`, Gradle o mocks de ejecución**.
- **Archivos creados (3):** `specs/420-Editar-Movimiento/spec.md` (v1.0.0, requisitos `M420-R01…R06`), `specs/420-Editar-Movimiento/plan.md` (v1.0.0, planes `N420-P01…P06`), `specs/420-Editar-Movimiento/task.md` (v1.0.0, tareas `N420-T01…T06` todas `- [ ]`).
- **Archivos modificados (2):** `specs/README.md` (enlace al mapa y actualización de progreso), `memoria.md` (esta sección).
- **Contenido:** Contrato NEXUM-E030 (`PATCH /api/v1/movements/{movementId}`, `200 OK` con `movement` actualizado y `summaryVersion`), cuerpo parcial `concept?`, `amount?`, `categoryId?`, `type?`, cabecera `Authorization: Bearer <accessToken>`, modelos `UpdateMovementRequest`/`UpdateMovementResponse`/`MovementEditDraft`/`MovementEditResult`, regla de no convertir a `saving` mediante la edición genérica y de gestionar las aportaciones con reglas propias (MO-PR04), validación de `amount > 0` con dos decimales mediante `BigDecimal` sin `Float`/`Double` (MO-PR05), categoría validada por el servidor a partir de `categoryId`, invalidación de la caché del resumen afectado sin recálculo local, estados MVI de carga, edición, guardando, guardado, error y sin conexión con conservación del formulario (MO-PR08), `404` neutro sin revelar pertenencia ajena (MO-PR06), y simulación en `MockAuthApi` con cambio en memoria, `summaryVersion` incrementado y centinelas `net@error.test`/`rule_error_movement`.
- **Verificación:** Trazabilidad formal requisito → plan → tarea; consistencia con la constitución canónica (`.specify/memory/constitution.md` MO-PR04, MO-PR05, MO-PR06, MO-PR07, MO-PR08, MO-PR12); enlaces relativos a carpetas existentes; sin secretos ni tokens reales.
- **Pruebas Gradle:** `NO EJECUTADAS` — fase puramente documental sin ejecución de código.
- **Pendiente:** mandato humano para la Fase B (implementación de DTOs, métodos en `AuthApi`/`AuthRepository`, simulación en `MockAuthApi`, `EditMovementViewModel`, formulario en Compose y pruebas unitarias con `kotlinx-coroutines-test`); tipo exacto de `summaryVersion`, forma de `movement`, semántica de campos ausentes en `PATCH`, mecanismo de conflicto (`409`) y contratos de recálculo de presupuestos y estadísticas **por confirmar con Backend**.
## 28. Evidencia — Inclusión de Previews de Compose y soporte para emulador (2026-11-12)
- **Fecha:** 2026-11-12 · **Mandato:** solicitud humana para agregar previsualizaciones (`@Preview`) a todas las pantallas de la carpeta UI (`app/src/main/java/mx/nexum/app/auth/ui`) e instruir la ejecución en emulador.
- **Alcance:** incorporación de composables de previsualización con `NexumTheme` y visibilidad de previsualización en `LoginScreen.kt`, `RegisterScreen.kt` y `AuthFlow.kt`.
- **Archivos modificados (3):** `app/src/main/java/mx/nexum/app/auth/ui/LoginScreen.kt` (`@Preview LoginScreenPreview`), `app/src/main/java/mx/nexum/app/auth/ui/RegisterScreen.kt` (`@Preview RegisterScreenPreview`), y `app/src/main/java/mx/nexum/app/auth/ui/AuthFlow.kt` (`@Preview AuthFlowPreview` y `@Preview AppPlaceholderPreview`).
- **Comandos y resultados reales:** `./gradlew assembleDebug testDebugUnitTest` → **BUILD SUCCESSFUL** · 51 tests · 0 fallos (`AuthValidatorTest` 19, `LoginViewModelTest` 9, `MockAuthApiTest` 11, `RegisterViewModelTest` 11, `ExampleUnitTest` 1).
- **Limitaciones:** `git push` no ejecutado (requiere autorización explícita independiente).

## 29. Evidencia — Migración de Login Mock a Endpoint Real E002 (2026-11-12)
- **Fecha:** 2026-11-12 · **Mandato:** sustitución del inicio de sesión simulado (`MockAuthApi`) por el consumo real del endpoint NEXUM-E002 (`POST https://d2lpmqy5w6x9es.cloudfront.net/api/v1/auth/login`) con DTOs adaptados, metadatos Android (`deviceId`, `deviceName`, `platform`) y manejo de errores HTTP (`400`, `401`, `429`, `500`, `503`).
- **Archivos creados (1):** `app/src/main/java/mx/nexum/app/auth/data/RetrofitAuthApi.kt`.
- **Archivos modificados (6):** `app/src/main/java/mx/nexum/app/auth/data/dto/AuthDtos.kt`, `app/src/main/java/mx/nexum/app/auth/data/network/AuthApiService.kt`, `app/src/main/java/mx/nexum/app/auth/ui/AuthFlow.kt`, `app/src/main/java/mx/nexum/app/auth/ui/LoginViewModel.kt`, `app/src/test/java/mx/nexum/app/auth/RegisterViewModelTest.kt`, `memoria.md`.
- **Comandos y resultados reales:** `./gradlew assembleDebug testDebugUnitTest` → **BUILD SUCCESSFUL** · 51 tests · 0 fallos; `./gradlew lintDebug` → **BUILD SUCCESSFUL**.
- **Limitaciones:** sin push, sin emulador ni pruebas de instrumentación; `git push` requiere autorización independiente.

## 30. Evidencia — Corrección de `versionCode` para Google Play Release (2026-11-12)
- **Fecha:** 2026-11-12 · **Mandato:** resolución del error `Error: Version code 1 has already been used` al subir el AAB a Google Play Console mediante GitHub Actions.
- **Causa:** Google Play exige que cada AAB subido tenga un `versionCode` estrictamente superior al previo. Estaba fijo en `1`.
- **Solución:** Modificación de `app/build.gradle.kts` para calcular dinámicamente el `versionCode` basándose en `GITHUB_RUN_NUMBER` (`100 + GITHUB_RUN_NUMBER` en CI, o `2` en desarrollo local), garantizando que cada compilación automatizada tenga un código de versión único y creciente.
- **Archivos modificados (1):** `app/build.gradle.kts`.
- **Comandos y resultados reales:** `./gradlew assembleDebug testDebugUnitTest` → **BUILD SUCCESSFUL**.
- **Limitaciones:** sin push automático (requiere autorización explícita para git push).

## 31. Evidencia — Fase A de `320-Eliminar-Movimiento` (documental)
- **Fecha:** 2026-11-13 · **Mandato:** solicitud humana para crear la especificación de eliminación y reversión atómica de movimiento financiero a partir del contrato NEXUM-E031 (`DELETE /api/v1/movements/{movementId}` [M], catálogo V6.0.2); **Fase A documental, sin cambios de código .kt ni push**.
- **Alcance:** solo documentación; **cero cambios en código `.kt`, Gradle o mocks de ejecución**.
- **Archivos creados (3):** `specs/320-Eliminar-Movimiento/spec.md`, `plan.md`, `task.md`.
- **Archivos modificados (2):** `specs/README.md`, `memoria.md`.
- **Contenido:** Contrato NEXUM-E031 (`DELETE /api/v1/movements/{movementId}`, `204 No Content`), transacción atómica que reverta `goal.saved = goal.saved - amount` si `goal_id != null`, manejo de errores (404, 409, 412) y resiliencia offline.
- **Verificación:** Trazabilidad formal requisito → plan → tarea; sin secretos ni tokens reales.
- **Pruebas Gradle:** `NO EJECUTADAS` — fase puramente documental sin ejecución de código.
- **Pendiente:** mandato humano para la Fase B (implementación técnica).

## 32. Evidencia — Fase A de `330-Pagos-Recurrentes` (documental)
- **Fecha:** 2026-11-13 · **Mandato:** solicitud humana para crear la especificación de gestión CRUD de pagos recurrentes a partir de los contratos NEXUM-E032 a E036 (`/api/v1/recurring-payments`), catálogo V6.0.2; **Fase A documental, sin cambios de código .kt ni push**.
- **Alcance:** solo documentación; **cero cambios en código `.kt`, Gradle o mocks de ejecución**.
- **Archivos creados (3):** `specs/330-Pagos-Recurrentes/spec.md`, `plan.md`, `task.md`.
- **Archivos modificados (2):** `specs/README.md`, `memoria.md`.
- **Contenido:** Contratos NEXUM-E032 (`GET` listado con `fixedMonthlyEquivalent`), E033 (`POST` crear), E034 (`GET /{id}` detalle), E035 (`PATCH /{id}` editar), E036 (`DELETE /{id}` soft-delete inactivo), distinción de pagos fijos y variables.
- **Verificación:** Trazabilidad formal requisito → plan → tarea.
- **Pruebas Gradle:** `NO EJECUTADAS` — fase puramente documental.
- **Pendiente:** mandato humano para la Fase B.

## 33. Evidencia — Fase A de `340-Registrar-Pago-Recurrente` (documental)
- **Fecha:** 2026-11-13 · **Mandato:** solicitud humana para crear la especificación de registro de pago recurrente realizado (✓) a partir del contrato NEXUM-E037 (`POST /api/v1/recurring-payments/{recurringPaymentId}/payments` [M], catálogo V6.0.2); **Fase A documental, sin cambios de código .kt ni push**.
- **Alcance:** solo documentación; **cero cambios en código `.kt`, Gradle o mocks de ejecución**.
- **Archivos creados (3):** `specs/340-Registrar-Pago-Recurrente/spec.md`, `plan.md`, `task.md`.
- **Archivos modificados (2):** `specs/README.md`, `memoria.md`.
- **Contenido:** Contrato NEXUM-E037 (`201 Created`, `movementId`, `recurringPaymentId`, `updatedSummary`), deduplicación por ocurrencia mediante `Idempotency-Key`, creación de movimiento vinculado sin duplicar el monto fijo en el disponible mensual.
- **Verificación:** Trazabilidad formal requisito → plan → tarea.
- **Pruebas Gradle:** `NO EJECUTADAS` — fase puramente documental.
- **Pendiente:** mandato humano para la Fase B.

## 34. Evidencia — Fase A de `350-Metas-Ahorro` (documental)
- **Fecha:** 2026-11-13 · **Mandato:** solicitud humana para crear la especificación de gestión de metas de ahorro a partir de los contratos NEXUM-E038 a E040 (`/api/v1/goals`), catálogo V6.0.2; **Fase A documental, sin cambios de código .kt ni push**.
- **Alcance:** solo documentación; **cero cambios en código `.kt`, Gradle o mocks de ejecución**.
- **Archivos creados (3):** `specs/350-Metas-Ahorro/spec.md`, `plan.md`, `task.md`.
- **Archivos modificados (2):** `specs/README.md`, `memoria.md`.
- **Contenido:** Contratos NEXUM-E038 (`GET` listado de metas activas/completadas, `totalSaved`, porcentaje visual máximo 100%), E039 (`POST` crear con emoji, `targetAmount`, `initialSaved`, fecha límite), E040 (`GET /{goalId}` detalle).
- **Verificación:** Trazabilidad formal requisito → plan → tarea.
- **Pruebas Gradle:** `NO EJECUTADAS` — fase puramente documental.
- **Pendiente:** mandato humano para la Fase B.

## 35. Evidencia — Implementación y verificación de `010-base-android` (2026-11-15)
- **Fecha:** 2026-11-15 · **Mandato:** autorización explícita para implementar y verificar `010-base-android` como primera unidad atómica de arquitectura base.
- **Alcance:** ratificación de la matriz de versiones y herramientas (`M010-R01`), comprobación de custodia y reglas de ignorado de secretos/archivos locales en `.gitignore` (`M010-R02`), y ejecución reproducible de compilación, análisis estático y pruebas unitarias (`M010-R03`).
- **Entorno:** Windows 11 · JAVA_HOME = `C:\Users\demon\AppData\Local\Programs\Android Studio\jbr` (OpenJDK 21.0.10) · Gradle Wrapper 9.5.0 · AGP 9.3.3 · Kotlin 2.2.10 · Compose BOM 2026.02.01 · compileSdk 36 (minor 1) · minSdk 26 · targetSdk 36 · Java 11 source/target compatibility.
- **Comandos y resultados reales:**
  - `git check-ignore -v local.properties release.jks release.keystore .gradle/ build/ app/build/` → comprobado que `local.properties`, llaves `.jks`/`.keystore`, directorios `.gradle` y carpetas `build/` están debidamente ignorados por `.gitignore` y `app/.gitignore`. `git status -s` limpio.
  - `.\gradlew.bat testDebugUnitTest` → **BUILD SUCCESSFUL** in 3m 15s · 51 tests unitarios ejecutados · 0 fallos · 100% éxito (`AuthValidatorTest` 19, `LoginViewModelTest` 9, `MockAuthApiTest` 11, `RegisterViewModelTest` 11, `ExampleUnitTest` 1).
  - `.\gradlew.bat assembleDebug` → **BUILD SUCCESSFUL** in 1m 20s · 36 tareas accionables (4 ejecutadas, 32 up-to-date) · APK debug generado correctamente.
  - `.\gradlew.bat lintDebug` → **BUILD SUCCESSFUL** in 2m 58s · 0 errores · Reportes generados en `app/build/reports/lint-results-debug.html`.
- **Archivos verificados:** `gradle/wrapper/gradle-wrapper.properties`, `gradle/libs.versions.toml`, `build.gradle.kts`, `app/build.gradle.kts`, `.gitignore`, `app/.gitignore`.
- **Archivos modificados:** `specs/010-base-android/task.md` (3 tareas marcadas `[x]` con evidencia), `specs/README.md` (progreso actualizado para `010-base-android`), `memoria.md` (decisión M-D10, cierre de M-P01 y esta sección).
- **Decisiones ratificadas:** M-D10 (matriz técnica base ratificada formalmente).
- **Limitaciones y pendientes:**
  - Pruebas instrumentadas / emulador: no ejecutadas (sin dispositivo físico ni emulador conectado en sesión headless).
  - `git push`: no ejecutado (requiere autorización explícita independiente).
  - Siguiente paso según dependencias: [`020-nucleo-mvi`](specs/020-nucleo-mvi/spec.md) (depende de `010-base-android`).

## 36. Evidencia — Implementación y verificación de `020-nucleo-mvi` (2026-11-15)
- **Fecha:** 2026-11-15 · **Mandato:** autorización explícita para implementar y verificar `020-nucleo-mvi`.
- **Alcance:** abstracción mínima de estado inmutable (`UiState`), intenciones explícitas (`UiIntent`), efectos efímeros (`UiEffect`) y reductor funcional puro (`Reducer<S, I>`) (`M020-R01`); clase base `MviViewModel` con flujo unidireccional desacoplada de pantallas y de finanzas (`M020-R02`); separación estricta entre estado durable observable (`StateFlow`) y eventos efímeros de disparo único sin repetición (`SharedFlow`, `replay = 0`) (`M020-R03`).
- **Entorno:** Windows 11 · JAVA_HOME = `C:\Users\demon\AppData\Local\Programs\Android Studio\jbr` (OpenJDK 21.0.10) · Gradle Wrapper 9.5.0 · AGP 9.3.3 · Kotlin 2.2.10 · Compose BOM 2026.02.01.
- **Comandos y resultados reales:**
  - `.\gradlew.bat testDebugUnitTest` → **BUILD SUCCESSFUL** in 1m 11s · 57 tests unitarios ejecutados (6 nuevos de `MviCoreTest`) · 0 fallos · 100% éxito (`MviCoreTest` 6, `AuthValidatorTest` 19, `LoginViewModelTest` 9, `MockAuthApiTest` 11, `RegisterViewModelTest` 11, `ExampleUnitTest` 1).
  - `.\gradlew.bat assembleDebug` → **BUILD SUCCESSFUL** in 2m 36s · 46 tareas accionables (12 ejecutadas, 34 up-to-date) · APK debug generado.
  - `.\gradlew.bat lintDebug` → **BUILD SUCCESSFUL** · 0 errores.
- **Archivos creados (3):**
  - `app/src/main/java/mx/nexum/app/core/mvi/MviContracts.kt` (interfaces `UiState`, `UiIntent`, `UiEffect`, `Reducer`).
  - `app/src/main/java/mx/nexum/app/core/mvi/MviViewModel.kt` (ViewModel MVI base con `StateFlow` y `SharedFlow`).
  - `app/src/test/java/mx/nexum/app/core/mvi/MviCoreTest.kt` (6 pruebas sintéticas de inmutabilidad, UDF, acumuladores y efectos one-shot).
- **Archivos modificados (2):** `specs/020-nucleo-mvi/task.md` (3 tareas marcadas `[x]`), `specs/README.md` (progreso actualizado para `020-nucleo-mvi`).
- **Limitaciones y pendientes:**
  - Sin pruebas instrumentadas/emulador.
  - `git push`: no ejecutado (requiere autorización explícita independiente).
  - Siguiente paso según dependencias: [`030-di-navegacion-ui`](specs/030-di-navegacion-ui/spec.md) y [`040-cliente-http-contratos`](specs/040-cliente-http-contratos/spec.md) (dependen de `020-nucleo-mvi`).

## 37. Evidencia — Implementación y verificación de `030-di-navegacion-ui` (2026-11-15)
- **Fecha:** 2026-11-15 · **Mandato:** autorización explícita para implementar y verificar `030-di-navegacion-ui`.
- **Alcance:** diseño e implementación del contenedor de inyección de dependencias `AppContainer` y `DefaultAppContainer` para sustitución limpia de dobles en pruebas (`M030-R01`); abstracción de navegación desacoplada `AppDestination`, `TechnicalDestination` y coordinador `SimpleNavigator` (`M030-R02`); shell UI composable Material 3 `NexumAppShell` con accesibilidad (`contentDescription`), Scaffold y aislamiento de red en la interfaz gráfica (`M030-R03`).
- **Entorno:** Windows 11 · JAVA_HOME = `C:\Users\demon\AppData\Local\Programs\Android Studio\jbr` (OpenJDK 21.0.10) · Gradle Wrapper 9.5.0 · AGP 9.3.3 · Kotlin 2.2.10 · Compose BOM 2026.02.01.
- **Comandos y resultados reales:**
  - `.\gradlew.bat testDebugUnitTest` → **BUILD SUCCESSFUL** in 1m 12s · 64 tests unitarios ejecutados (7 nuevos entre `AppContainerTest` y `AppNavigatorTest`) · 0 fallos · 100% éxito (`AppContainerTest` 2, `AppNavigatorTest` 5, `MviCoreTest` 6, `AuthValidatorTest` 19, `LoginViewModelTest` 9, `MockAuthApiTest` 11, `RegisterViewModelTest` 11, `ExampleUnitTest` 1).
  - `.\gradlew.bat assembleDebug` → **BUILD SUCCESSFUL** in 2m 35s · 46 tareas accionables (12 ejecutadas, 34 up-to-date) · APK debug generado.
  - `.\gradlew.bat lintDebug` → **BUILD SUCCESSFUL** · 0 errores.
- **Archivos creados (5):**
  - `app/src/main/java/mx/nexum/app/core/di/AppContainer.kt` (interfaz `AppContainer` e implementación `DefaultAppContainer`).
  - `app/src/main/java/mx/nexum/app/core/navigation/NavigationContracts.kt` (contratos `AppDestination`, `TechnicalDestination`, `AppNavigator`, `SimpleNavigator`).
  - `app/src/main/java/mx/nexum/app/core/ui/NexumAppShell.kt` (`NexumAppShell` en Compose con Material 3 y accesibilidad).
  - `app/src/test/java/mx/nexum/app/core/di/AppContainerTest.kt` (2 pruebas de inyección y sustitución de dobles).
  - `app/src/test/java/mx/nexum/app/core/navigation/AppNavigatorTest.kt` (5 pruebas de pila de navegación y enrutamiento UDF).
- **Archivos modificados (2):** `specs/030-di-navegacion-ui/task.md` (3 tareas marcadas `[x]`), `specs/README.md` (progreso actualizado para `030-di-navegacion-ui`).
- **Decisiones ratificadas:** M-D11 (DI manual y navegación desacoplada).
- **Limitaciones y pendientes:**
  - Sin pruebas instrumentadas/emulador.
  - `git push`: no ejecutado (requiere autorización explícita independiente).
  - Siguiente paso según dependencias: [`040-cliente-http-contratos`](specs/040-cliente-http-contratos/spec.md) (depende de `020-nucleo-mvi`).

## 38. Evidencia — Implementación y verificación de `040-cliente-http-contratos` (2026-11-15)
- **Fecha:** 2026-11-15 · **Mandato:** autorización explícita para implementar y verificar `040-cliente-http-contratos`.
- **Alcance:** modelado canónico de errores HTTP e infraestructura (`ApiErrorDetail`, `ApiErrorResponse`, `HttpErrorType`, `NetworkErrorType`, `ApiResult<T>`) asegurando que fallos nunca generen éxito simulado (`M040-R01`); forzado de TLS/HTTPS mediante `network_security_config.xml` enlazado en `AndroidManifest.xml` y adaptador simulable `SimulatedHttpAdapter` (`M040-R02`); adaptador seguro `safeApiCall` que procesa deserialización Gson, códigos 400/401/403/404/409/422/429 (extrayendo cabecera `Retry-After`) y 5xx, así como captura de `SocketTimeoutException`, `UnknownHostException` y `JsonSyntaxException` (`M040-R03`).
- **Entorno:** Windows 11 · JAVA_HOME = `C:\Users\demon\AppData\Local\Programs\Android Studio\jbr` (OpenJDK 21.0.10) · Gradle Wrapper 9.5.0 · AGP 9.3.3 · Kotlin 2.2.10 · Compose BOM 2026.02.01.
- **Comandos y resultados reales:**
  - `.\gradlew.bat testDebugUnitTest` → **BUILD SUCCESSFUL** in 1m 42s · 75 tests unitarios ejecutados (11 nuevos de `HttpClientAdapterTest`) · 0 fallos · 100% éxito (`HttpClientAdapterTest` 11, `AppContainerTest` 2, `AppNavigatorTest` 5, `MviCoreTest` 6, `AuthValidatorTest` 19, `LoginViewModelTest` 9, `MockAuthApiTest` 11, `RegisterViewModelTest` 11, `ExampleUnitTest` 1).
  - `.\gradlew.bat assembleDebug` → **BUILD SUCCESSFUL** in 2m 48s · 46 tareas accionables (12 ejecutadas, 34 up-to-date) · APK debug generado.
  - `.\gradlew.bat lintDebug` → **BUILD SUCCESSFUL** · 0 errores.
- **Archivos creados (5):**
  - `app/src/main/res/xml/network_security_config.xml` (configuración de seguridad de red con TLS/HTTPS forzado).
  - `app/src/main/java/mx/nexum/app/core/network/HttpContracts.kt` (modelos de error y envoltorio `ApiResult`).
  - `app/src/main/java/mx/nexum/app/core/network/SafeApiCall.kt` (ejecutor seguro de llamadas de red con captura de excepciones).
  - `app/src/main/java/mx/nexum/app/core/network/SimulatedHttpAdapter.kt` (adaptador simulable para pruebas).
  - `app/src/test/java/mx/nexum/app/core/network/HttpClientAdapterTest.kt` (11 pruebas unitarias de HTTP, timeout, parsing y errores).
- **Archivos modificados (3):** `app/src/main/AndroidManifest.xml` (+`android:networkSecurityConfig`), `specs/040-cliente-http-contratos/task.md` (3 tareas marcadas `[x]`), `specs/README.md` (progreso actualizado para `040-cliente-http-contratos`).
- **Decisiones ratificadas:** M-D12 (adaptadores HTTP, contrato de errores y TLS), cierre de M-P02.
- **Limitaciones y pendientes:**
  - Sin pruebas instrumentadas/emulador.
  - `git push`: no ejecutado (requiere autorización explícita independiente).
  - Siguiente paso según dependencias: [`050-sesion-credenciales`](specs/050-sesion-credenciales/spec.md) (depende de `040-cliente-http-contratos`).

## 39. Evidencia — Implementación y verificación de `060-cache-offline` (2026-11-17)
- **Fecha:** 2026-11-17 · **Mandato:** autorización explícita para implementar y verificar `060-cache-offline`.
- **Alcance:** modelado y contratos técnicos de caché local y sincronización offline (`SyncStatus`, `CacheEntry<T>`, `CacheResult<T>`, `IdempotentOperation<T>`, `CacheStorage<T>`) asegurando aislamiento estricto por usuario y respeto riguroso de TTL e invalidación (`M060-R01`); distinción explícita e inequívoca entre estados de sincronización (`CONFIRMED`, `PENDING`, `STALE`), evitando presentar operaciones pendientes como definitivas (`M060-R02`); cola de operaciones pendientes con clave de idempotencia (`idempotencyKey`), deduplicación garantizada, registro de reintentos y actualización de estado (`M060-R03`); integración desacoplada en `AppContainer` y `DefaultAppContainer` sin agregar librerías externas fuera de `libs.versions.toml`.
- **Entorno:** Windows 11 · JAVA_HOME = `C:\Users\demon\AppData\Local\Programs\Android Studio\jbr` (OpenJDK 21.0.10) · Gradle Wrapper 9.5.0 · AGP 9.3.3 · Kotlin 2.2.10 · Compose BOM 2026.02.01.
- **Comandos y resultados reales:**
  - `.\gradlew.bat testDebugUnitTest` → **BUILD SUCCESSFUL** in 1m 39s · 88 tests unitarios ejecutados (10 nuevos de `OfflineCacheTest`, 2 actualizados en `AppContainerTest`) · 0 fallos · 100% éxito (`OfflineCacheTest` 10, `HttpClientAdapterTest` 11, `AppContainerTest` 2, `AppNavigatorTest` 5, `MviCoreTest` 6, `SessionManagerTest` 3, `AuthValidatorTest` 19, `LoginViewModelTest` 9, `MockAuthApiTest` 11, `RegisterViewModelTest` 11, `ExampleUnitTest` 1).
  - `.\gradlew.bat assembleDebug` → **BUILD SUCCESSFUL** in 49s · 36 tareas accionables (4 ejecutadas, 32 up-to-date) · APK debug generado.
- **Archivos creados (4):**
  - `app/src/main/java/mx/nexum/app/core/cache/CacheContracts.kt` (contratos `SyncStatus`, `CacheEntry`, `CacheResult`, `IdempotentOperation`, `CacheStorage`).
  - `app/src/main/java/mx/nexum/app/core/cache/InMemoryCacheStorage.kt` (almacenamiento en memoria con particionado concurrente por `userId`).
  - `app/src/main/java/mx/nexum/app/core/cache/OfflineCacheManager.kt` (gestor de caché, validación TTL, encolado idempotente y deduplicación).
  - `app/src/test/java/mx/nexum/app/core/cache/OfflineCacheTest.kt` (10 pruebas unitarias de aislamiento, expiración, estados y reintentos idempotentes).
- **Archivos modificados (4):**
  - `app/src/main/java/mx/nexum/app/core/di/AppContainer.kt` (incorporación de `cacheManager: OfflineCacheManager` en `AppContainer` y `DefaultAppContainer`).
  - `app/src/test/java/mx/nexum/app/core/di/AppContainerTest.kt` (pruebas de inyección y singleton de `cacheManager`).
  - `specs/060-cache-offline/task.md` (3 tareas marcadas `[x]` con evidencia y cierre).
  - `specs/README.md` (progreso actualizado para `060-cache-offline` y conteo de tests a 88 en verde).
- **Decisiones ratificadas:** M-D13 (arquitectura de caché técnica y sincronización offline sin dependencias no aprobadas).
- **Limitaciones y pendientes:**
  - Sin pruebas instrumentadas/emulador.
  - Persistencia física en SQLite/Room condicional a decisión futura cuando se requiera base de datos local en disco.
  - `git push`: no ejecutado (requiere autorización explícita independiente).
  - Siguiente paso según dependencias: [`070-permisos-privacidad-accesibilidad`](specs/070-permisos-privacidad-accesibilidad/spec.md) (depende de `030-di-navegacion-ui`).

## 40. Evidencia — Implementación y verificación de `070-permisos-privacidad-accesibilidad` (2026-11-17)
- **Fecha:** 2026-11-17 · **Mandato:** autorización explícita para implementar y verificar `070-permisos-privacidad-accesibilidad`.
- **Alcance:** auditoría y configuración de permisos mínimos en `AndroidManifest.xml` (únicamente `INTERNET`, sin solicitud de `RECORD_AUDIO` ni `POST_NOTIFICATIONS` prematuras) y transporte seguro en `network_security_config.xml` (texto claro deshabilitado por defecto y protección del dominio de la política de privacidad `nexumpr.github.io`) (`M070-R01`, `M070-R03`); diseño de componentes y utilidades accesibles en Material 3 (`AccessibilityConstants.MinTouchTargetSize = 48.dp`, `AccessibleButton`, `AccessibleIconButton`, `PermissionRationaleCard`, `PrivacyPolicyBanner`) con semántica y roles accesibles para lectores de pantalla (`M070-R02`); contratos y políticas de privacidad en `PrivacyContracts.kt` (`PermissionStatus`, `AlternativeInputMode`, `PermissionFallbackHandler`, `PrivacyPolicyConfig` con URL oficial `https://nexumpr.github.io/nexum-privacidad/` y sanitización de credenciales/tokens en registros) (`M070-R01`, `M070-R03`).
- **Entorno:** Windows 11 · JAVA_HOME = `C:\Users\demon\AppData\Local\Programs\Android Studio\jbr` (OpenJDK 21.0.10) · Gradle Wrapper 9.5.0 · AGP 9.3.3 · Kotlin 2.2.10 · Compose BOM 2026.02.01.
- **Comandos y resultados reales:**
  - `.\gradlew.bat lintDebug` → **BUILD SUCCESSFUL** in 2m 59s · 0 errores.
  - `.\gradlew.bat testDebugUnitTest` → **BUILD SUCCESSFUL** in 1m 24s · 95 tests unitarios ejecutados (7 nuevos de `PrivacyAndPermissionsTest`) · 0 fallos · 100% éxito (`PrivacyAndPermissionsTest` 7, `OfflineCacheTest` 10, `HttpClientAdapterTest` 11, `AppContainerTest` 2, `AppNavigatorTest` 5, `MviCoreTest` 6, `SessionManagerTest` 3, `AuthValidatorTest` 19, `LoginViewModelTest` 9, `MockAuthApiTest` 11, `RegisterViewModelTest` 11, `ExampleUnitTest` 1).
  - `.\gradlew.bat assembleDebug` → **BUILD SUCCESSFUL** in 24s · 36 tareas accionables (4 ejecutadas, 32 up-to-date) · APK debug generado.
- **Archivos creados (3):**
  - `app/src/main/java/mx/nexum/app/core/privacy/PrivacyContracts.kt` (contratos `PermissionStatus`, `AlternativeInputMode`, `PermissionFallbackHandler`, `PrivacyPolicyConfig`).
  - `app/src/main/java/mx/nexum/app/core/ui/AccessibleComponents.kt` (componentes accesibles Material 3: `AccessibleButton`, `AccessibleIconButton`, `PermissionRationaleCard`, `PrivacyPolicyBanner`).
  - `app/src/test/java/mx/nexum/app/core/privacy/PrivacyAndPermissionsTest.kt` (7 pruebas unitarias de fallback de permisos, accesibilidad 48dp, sanitización y auditoría de manifiesto / seguridad de red).
- **Archivos modificados (4):**
  - `app/src/main/res/xml/network_security_config.xml` (inclusión de dominio `nexumpr.github.io` con cleartext deshabilitado).
  - `specs/070-permisos-privacidad-accesibilidad/task.md` (3 tareas marcadas `[x]` con evidencia y cierre).
  - `specs/README.md` (progreso actualizado para `070-permisos-privacidad-accesibilidad` y conteo de tests a 95 en verde).
  - `memoria.md` (decisión M-D14, actualización M-P05 y esta sección).
- **Decisiones ratificadas:** M-D14 (permisos mínimos en manifiesto, accesibilidad 48dp y privacidad protegida).
- **Limitaciones y pendientes:**
  - Pruebas instrumentadas / emulador: no ejecutadas (sin dispositivo físico ni emulador conectado en sesión headless).
  - Funcionalidad de voz/grabación de audio y notificaciones push pospuestas hasta autorización explícita de sus specs funcionales.
  - `git push`: no ejecutado (requiere autorización explícita independiente).
  - Siguiente paso según dependencias: [`080-pruebas-mvi-compose`](specs/080-pruebas-mvi-compose/spec.md) (depende de `020-nucleo-mvi` y `030-di-navegacion-ui`).

## 41. Evidencia — Implementación y verificación de `080-pruebas-mvi-compose` (2026-11-17)
- **Fecha:** 2026-11-17 · **Mandato:** autorización explícita para implementar y verificar `080-pruebas-mvi-compose`.
- **Alcance:** diseño e implementación de fixtures con datos estrictamente sintéticos sin tocar APIs remotas ni datos financieros (`TestFixtures.kt`, `FakeTokenStorage`, `FakeCredentialStorage`) (`M080-R01`, `M080-R02`); pruebas de integración de transiciones MVI (`Idle`, `Loading`, `Content`, `Empty`, `Error`, `Offline`) y efectos efímeros (`MviStateIntegrationTest.kt`) garantizando que eventos efímeros `replay = 0` no se dupliquen tras recomposición (`M080-R01`, `M080-R03`); suite de pruebas de UI instrumentadas en Compose (`NexumAppShellComposeTest.kt`) con `createComposeRule()` validando jerarquía de componentes accesibles y unívocos clics en Material 3 (`M080-R02`, `M080-R03`).
- **Entorno:** Windows 11 · JAVA_HOME = `C:\Users\demon\AppData\Local\Programs\Android Studio\jbr` (OpenJDK 21.0.10) · Gradle Wrapper 9.5.0 · AGP 9.3.3 · Kotlin 2.2.10 · Compose BOM 2026.02.01 · compileSdk 36 · targetSdk 36 · minSdk 26.
- **Comandos y resultados reales:**
  - `.\gradlew.bat testDebugUnitTest` → **BUILD SUCCESSFUL** in 37s · 101 tests unitarios ejecutados (6 nuevos de `MviStateIntegrationTest`) · 0 fallos · 100% éxito (`MviStateIntegrationTest` 6, `PrivacyAndPermissionsTest` 7, `OfflineCacheTest` 10, `HttpClientAdapterTest` 11, `AppContainerTest` 2, `AppNavigatorTest` 5, `MviCoreTest` 6, `SessionManagerTest` 3, `AuthValidatorTest` 19, `LoginViewModelTest` 9, `MockAuthApiTest` 11, `RegisterViewModelTest` 11, `ExampleUnitTest` 1).
  - `.\gradlew.bat lintDebug` → **BUILD SUCCESSFUL** in 37s · 0 errores.
  - `.\gradlew.bat assembleDebug` → **BUILD SUCCESSFUL** in 6s · 36 tareas accionables (36 up-to-date) · APK debug generado.
  - `connectedDebugAndroidTest` → `NO EJECUTADO` (documentado con causa: runner headless sin dispositivo físico ni emulador Android conectado según `adb devices`).
- **Archivos creados (3):**
  - `app/src/test/java/mx/nexum/app/core/testing/TestFixtures.kt` (fixtures sintéticos: `syntheticUser`, `syntheticSession`, `syntheticValidationErrorResponse`, `FakeTokenStorage`, `FakeCredentialStorage`).
  - `app/src/test/java/mx/nexum/app/core/testing/MviStateIntegrationTest.kt` (6 pruebas unitarias de estados MVI: carga, contenido, vacío, error, offline y no repetición de efectos efímeros).
  - `app/src/androidTest/java/mx/nexum/app/core/ui/NexumAppShellComposeTest.kt` (pruebas instrumentadas de Compose con `createComposeRule()` para accesibilidad y no duplicación en `NexumAppShell`).
- **Archivos modificados (3):**
  - `specs/080-pruebas-mvi-compose/task.md` (3 tareas marcadas `[x]` con evidencia real y cierre).
  - `specs/README.md` (progreso actualizado para `080-pruebas-mvi-compose` y conteo de tests a 101 en verde).
  - `memoria.md` (decisión M-D15 y esta sección).
- **Decisiones ratificadas:** M-D15 (suite de pruebas MVI desacoplada, fixtures sintéticos y separación de suites de prueba).
- **Limitaciones y pendientes:**
  - Pruebas instrumentadas / emulador: `connectedDebugAndroidTest` queda en `NO EJECUTADO` por falta de dispositivo físico/emulador conectado.
  - Sin medición de cobertura de código (no inventar porcentajes sin herramienta de cobertura aprobada en el proyecto).
  - `git push`: no ejecutado (requiere autorización explícita independiente).
  - Siguiente paso según dependencias: [`090-devsecops-android`](specs/090-devsecops-android/spec.md) (depende de `010-base-android`) y [`100-ci-versiones-android`](specs/100-ci-versiones-android/spec.md).

## 42. Evidencia — Implementación y verificación de `090-devsecops-android` (2026-11-17)
- **Fecha:** 2026-11-17 · **Mandato:** autorización explícita para implementar y verificar `090-devsecops-android`.
- **Alcance:** diseño e implementación de la estrategia de quality & security gates progresivos en 3 niveles (Local/Pre-commit, PR/CI, Release) (`M090-R01`, `N090-P01`); gobernanza de severidades de Android Lint vía `app/lint.xml` forzando reglas de seguridad críticas como `fatal` (`BadHostnameVerifier`, `NetworkSecurityConfig`, `HardcodedDebugMode`, `InsecureBaseConfiguration`, `TrustAllX509TrustManager`, etc.) (`M090-R02`, `N090-P02`); suite automatizada de seguridad estática `DevSecOpsVerificationTest.kt` evaluando cero secretos en código (regex AWS, claves privadas PEM, tokens), verificación de `network_security_config.xml` (tráfico en texto claro deshabilitado) y verificación de `AndroidManifest.xml` (únicamente `INTERNET`, sin permisos invasivos) con pruebas negativas explícitas (`M090-R01`, `M090-R02`); matriz de clasificación de hallazgos (P0..P3), SLAs de remediación y gobernanza de excepciones temporales (`M090-R03`, `N090-P03`).
- **Entorno:** Windows 11 · JAVA_HOME = `C:\Users\demon\AppData\Local\Programs\Android Studio\jbr` (OpenJDK 21.0.10) · Gradle Wrapper 9.5.0 · AGP 9.3.3 · Kotlin 2.2.10 · Compose BOM 2026.02.01 · compileSdk 36 · targetSdk 36 · minSdk 26.
- **Comandos y resultados reales:**
  - `.\gradlew.bat testDebugUnitTest` → **BUILD SUCCESSFUL** in 59s · 109 tests unitarios ejecutados (8 nuevos de `DevSecOpsVerificationTest`) · 0 fallos · 100% éxito (`DevSecOpsVerificationTest` 8, `MviStateIntegrationTest` 6, `PrivacyAndPermissionsTest` 7, `OfflineCacheTest` 10, `HttpClientAdapterTest` 11, `AppContainerTest` 2, `AppNavigatorTest` 5, `MviCoreTest` 6, `SessionManagerTest` 3, `AuthValidatorTest` 19, `LoginViewModelTest` 9, `MockAuthApiTest` 11, `RegisterViewModelTest` 11, `ExampleUnitTest` 1).
  - `.\gradlew.bat lintDebug` → **BUILD SUCCESSFUL** in 1m 36s · 0 errores (reportes SARIF y HTML generados en `app/build/reports/`).
  - `.\gradlew.bat assembleDebug` → **BUILD SUCCESSFUL** in 5s · 36 tareas accionables (36 up-to-date) · APK debug generado.
  - `gitleaks` → `NO DISPONIBLE EN PATH LOCAL` (confirmado mediante `Get-Command gitleaks`, código 1; suplido de forma reproducible por la suite `DevSecOpsVerificationTest.kt` sin dependencias externas ni Docker).
- **Archivos creados (2):**
  - `app/lint.xml` (gobernanza de Android Lint forzando reglas críticas de seguridad como `fatal`).
  - `app/src/test/java/mx/nexum/app/core/security/DevSecOpsVerificationTest.kt` (8 pruebas unitarias de auditoría estática de secretos, manifiesto, seguridad de red y escenarios negativos).
- **Archivos modificados (3):**
  - `specs/090-devsecops-android/task.md` (3 tareas marcadas `[x]` con evidencia real y cierre).
  - `specs/README.md` (progreso actualizado para `090-devsecops-android` y conteo de tests a 109 en verde).
  - `memoria.md` (decisión M-D16 y esta sección).
- **Decisiones ratificadas:** M-D16 (arquitectura DevSecOps Android, gates progresivos, matriz de severidades y gobernanza de excepciones sin dependencias externas).
- **Limitaciones y pendientes:**
  - Herramientas externas (Detekt, CycloneDX) evaluadas y dejadas como propuestas formales sin modificar `libs.versions.toml`.
  - `git push`: no ejecutado (requiere autorización explícita independiente).
  - Siguiente paso según dependencias: [`100-ci-versiones-android`](specs/100-ci-versiones-android/spec.md) (depende de `080-pruebas-mvi-compose` y `090-devsecops-android`).

## 43. Evidencia — Implementación y verificación de `100-ci-versiones-android` (2026-11-17)
- **Fecha:** 2026-11-17 · **Mandato:** autorización explícita para implementar y verificar `100-ci-versiones-android`.
- **Alcance:** diseño e implementación del pipeline de CI reproducible en GitHub Actions (`.github/workflows/android-ci.yml`) con permisos mínimos de solo lectura (`permissions: contents: read`) (`M100-R01`, `N100-P02`); exclusión absoluta de secretos de firma de release (`keystore`, contraseñas) y de tareas de despliegue a Google Play o canales externos (`M100-R02`, `M100-R03`); formalización y verificación del esquema de versionado determinista (`versionCode = 2` local, `100 + GITHUB_RUN_NUMBER` en CI) y matriz SDK ratificada (`minSdk = 26`, `targetSdk = 36`, `compileSdk = 36`, JDK 21) (`M100-R01`, `N100-P01`); suite automatizada de pruebas `CiVersioningVerificationTest.kt` validando determinismo de versión, permisos mínimos del pipeline y escenarios negativos contra inyección de secretos o permisos excesivos (`M100-R01`, `M100-R02`); recolección y aislamiento de reportes de Lint (SARIF/HTML), tests JUnit y APK de depuración sin firma (`M100-R03`, `N100-P03`).
- **Entorno:** Windows 11 · JAVA_HOME = `C:\Users\demon\AppData\Local\Programs\Android Studio\jbr` (OpenJDK 21.0.10) · Gradle Wrapper 9.5.0 · AGP 9.3.3 · Kotlin 2.2.10 · Compose BOM 2026.02.01 · compileSdk 36 · targetSdk 36 · minSdk 26.
- **Comandos y resultados reales:**
  - `.\gradlew.bat testDebugUnitTest` → **BUILD SUCCESSFUL** in 40s · 115 tests unitarios ejecutados (6 nuevos de `CiVersioningVerificationTest`) · 0 fallos · 100% éxito (`CiVersioningVerificationTest` 6, `DevSecOpsVerificationTest` 8, `MviStateIntegrationTest` 6, `PrivacyAndPermissionsTest` 7, `OfflineCacheTest` 10, `HttpClientAdapterTest` 11, `AppContainerTest` 2, `AppNavigatorTest` 5, `MviCoreTest` 6, `SessionManagerTest` 3, `AuthValidatorTest` 19, `LoginViewModelTest` 9, `MockAuthApiTest` 11, `RegisterViewModelTest` 11, `ExampleUnitTest` 1).
  - `.\gradlew.bat lintDebug` → **BUILD SUCCESSFUL** in 1m 02s · 0 errores (reportes SARIF y HTML actualizados en `app/build/reports/`).
  - `.\gradlew.bat assembleDebug` → **BUILD SUCCESSFUL** in 7s · 36 tareas accionables (36 up-to-date) · APK debug generado limpiamente.
- **Archivos creados (2):**
  - `.github/workflows/android-ci.yml` (workflow de CI de solo lectura con JDK 21, `setup-gradle@v4`, ejecución de gates de Lint/Tests y generación de APK debug sin firma).
  - `app/src/test/java/mx/nexum/app/core/ci/CiVersioningVerificationTest.kt` (6 pruebas unitarias de cálculo de versionado determinista, matriz SDK, auditoría de permisos de CI y escenarios negativos).
- **Archivos modificados (3):**
  - `specs/100-ci-versiones-android/task.md` (3 tareas marcadas `[x]` con evidencia real y cierre).
  - `specs/README.md` (progreso actualizado para `100-ci-versiones-android` y conteo de tests a 115 en verde).
  - `memoria.md` (decisión M-D17 y esta sección).
- **Decisiones ratificadas:** M-D17 (pipeline CI de solo lectura, versionado determinista, sin secretos de firma embebidos).
- **Limitaciones y pendientes:**
  - Sin publicación externa a Google Play (pospuesta para `110-distribucion-google-play` / `151-github-actions` bajo autorización explícita independiente).
  - `git push`: no ejecutado (requiere autorización explícita independiente).
  - Siguiente paso según dependencias: [`110-distribucion-google-play`](specs/110-distribucion-google-play/spec.md) (depende de `100-ci-versiones-android`).

## 44. Evidencia — Documentación y verificación de `110-distribucion-google-play` (2026-11-17)
- **Fecha:** 2026-11-17 · **Mandato:** solicitud explícita de ejecutar `110-distribucion-google-play` con prohibición de publicar, firmar con llaves reales, pagar trámites, usar Docker, tocar otros repositorios o hacer push.
- **Alcance ejecutado:** documentación de gobierno de cuenta/firma (`M110-R01`, `N110-P01`, `N110-T01`), checklist de privacidad y calidad (`M110-R02`, `N110-P02`, `N110-T02`) y diseño de release/rollback (`M110-R03`, `N110-P03`, `N110-T03`). No se modificaron Gradle, workflows, manifiesto ni dependencias.
- **Entorno:** Windows/PowerShell 5.1 · Gradle Wrapper con caché de configuración reutilizada · sin compilación release · sin credenciales ni acceso a Play Console.
- **Archivos creados (3):**
  - `specs/110-distribucion-google-play/release-governance.md` (matriz de roles, registro administrativo/costos, política de firma y cinco decisiones pendientes `P110-D01…D05`).
  - `specs/110-distribucion-google-play/play-prepublication-checklist.md` (controles `CHK-P01…P08`, `CHK-A01…A09` y `CHK-H01…H04` con estado real).
  - `specs/110-distribucion-google-play/release-rollback-plan.md` (pipeline manual propuesto, compuertas, detención, promoción/retirada y reversibilidad).
- **Archivos modificados (3):**
  - `specs/110-distribucion-google-play/task.md` (tareas marcadas `[x]` solo por documentación y validación documental ejecutadas; ratificación humana sigue pendiente).
  - `specs/README.md` (progreso de esta unidad).
  - `memoria.md` (esta sección).
- **Comandos y resultados reales:**
  - `git status --short` → 3 archivos nuevos sin seguimiento; sin modificaciones previas en archivos versionados.
  - `git diff --check` → sin salida, sin errores de whitespace en el diff versionado.
  - `git grep -n -E 'r0adkll/upload-google-play|serviceAccountJsonPlainText|PLAY_SERVICE_ACCOUNT_JSON|ANDROID_KEYSTORE_BASE64' -- .github/workflows` → 7 coincidencias, todas en `.github/workflows/android-deploy.yml:42,48,49,53,100,102`; confirma riesgo preexistente. Archivo **no modificado ni ejecutado**.
  - `git grep -n -I -E 'BEGIN [A-Z ]*PRIVATE KEY|AKIA[0-9A-Z]{16}|gh[pousr]_[A-Za-z0-9_]{36,255}' -- .` → 2 coincidencias sintéticas en `DevSecOpsVerificationTest.kt:124,131`; sin secretos reales detectados.
  - `git check-ignore -v app/upload-key.jks app/release.pepk` → solo `.gitignore:16` ignora `*.jks`; `*.pepk` no aparece ignorado. `Test-Path app/proguard-rules.pro` → `False`.
  - `git grep -n -E 'applicationId|optimization|enable = false|proguard|mapping' -- app/build.gradle.kts` → `applicationId = "com.nexum.nexum"` (`:24`) y `optimization { enable = false }` (`:64-65`); sin reglas ni `mapping.txt`.
  - `git grep -n 'OFFICIAL_PRIVACY_URL' -- app/src/main` → `PrivacyContracts.kt:55` con `https://nexumpr.github.io/nexum-privacidad/`.
  - `.\gradlew.bat testDebugUnitTest; .\gradlew.bat lintDebug; .\gradlew.bat assembleDebug` → tres `BUILD SUCCESSFUL`; primera pasada de tests `UP-TO-DATE`.
  - `.\gradlew.bat testDebugUnitTest --rerun-tasks` → `BUILD SUCCESSFUL in 1m 57s`, 24 tareas ejecutadas; lectura de XML: 15 suites, 115 tests, 0 failures, 0 errors.
- **No ejecutado:** `bundleRelease` con firma real, generación de llaves, *.pepk, service accounts, subida de AAB/APK, pistas internas/cerradas, revisión humana de firma/consentimiento, trámites/costos, push, merge ni despliegue.
- **Pendientes materiales:** `P110-D01` workflow de deploy preexistente; `P110-D02` `applicationId` canónico; `P110-D03` responsables y aprobadores; `P110-D04` trámites/costos; `P110-D05` ignore de `*.pepk`. CHK-A03/A04 permanecen BLOQUEADOS hasta decidir R8/reglas de desofuscación.
- **Reversibilidad:** eliminar los 3 documentos creados y revertir mediante diff inverso los bloques propios de `task.md`, `specs/README.md` y esta sección; sin reset, push ni rewrite de historial.

## 45. Evidencia — Contratos end-to-end locales de `120-contratos-end-to-end` (2026-11-17)
- **Fecha:** 2026-11-17 · **Mandato:** solicitud explícita de ejecutar `120-contratos-end-to-end`; confirmación de la opción mínima (simulador HTTP en memoria sin nueva dependencia). No se autorizó Backend real, dependencias de mock ni cambios productivos de red.
- **Entorno:** Windows/PowerShell 5.1 · Gradle Wrapper con configuración cacheada · JDK 21 · datos 100% sintéticos · dominio reservado `contract.invalid` · sin red externa, credenciales ni secretos reales.
- **Alcance ejecutado:** `M120-R01/R02/R03`, planes `N120-P01/P02/P03` y tareas `N120-T01/T02/T03`. Se documentó el contrato y se construyeron pruebas JVM deterministas; **no** se cablearon reintentos, timeouts, refresh automático ni DTOs nuevos en producción.
- **Archivos creados (20):**
  - `specs/120-contratos-end-to-end/rest-contract.md` (rutas versionadas, cabeceras, sobre canónico, matriz de responsabilidades y pendientes Backend).
  - `specs/120-contratos-end-to-end/retry-and-fallback-matrix.md` (matriz propuesta de reintentos, `401`, fallback `STALE` e idempotencia).
  - `specs/120-contratos-end-to-end/fixture-catalog.md` (15 fixtures sintéticas y reglas de datos).
  - `app/src/test/java/mx/nexum/app/core/contract/FixtureHttpSimulator.kt` (interceptor OkHttp en memoria; registra método, ruta, cabeceras y `contentType` del cuerpo).
  - `app/src/test/java/mx/nexum/app/core/contract/RestContractTest.kt` (6 pruebas).
  - `app/src/test/java/mx/nexum/app/core/contract/RetryAndFallbackContractTest.kt` (6 pruebas).
  - `app/src/test/java/mx/nexum/app/core/contract/NoSensitiveDataContractTest.kt` (4 pruebas).
  - `app/src/test/resources/contract/` con `health_success.json`, `session_refresh_success.json`, `metadata_success.json`, `unknown_fields_success.json`, `register_success.json`, `error_400.json`, `error_401.json`, `error_403.json`, `error_404.json`, `error_409.json`, `error_422.json`, `error_429.json`, `error_500.json`, `error_503.json` y `malformed.json`.
- **Archivos modificados (3):** `specs/120-contratos-end-to-end/task.md`, `specs/README.md` y esta sección de `memoria.md`. Sin tocar `app/src/main`, Gradle, workflows ni specs de otras unidades.
- **Comandos y resultados reales:**
  - `.\gradlew.bat testDebugUnitTest --rerun-tasks` (primera pasada) → `BUILD FAILED`: 131 tests, 4 fallos por fixtures incorrectas y por `Content-Type` ausente en la capa de interceptor de aplicación.
  - Correcciones aplicadas: `unknown_fields_success.json` pasó a incluir `version`/`generatedAt`; `malformed.json` pasó a tener tipos incompatibles para disparar `JsonSyntaxException`; `RecordedRequest` incorporó `contentType = request.body?.contentType()?.toString()`; se eliminó el warning de `javaClass.classLoader` usando `FixtureHttpSimulator::class.java.classLoader?`.
  - `.\gradlew.bat testDebugUnitTest --rerun-tasks` (final) → `BUILD SUCCESSFUL in 1m 15s`, 24 tareas ejecutadas, sin warnings de compilación.
  - Lectura de XML con PowerShell → `ARCHIVOS=18 TESTS=131 FAILURES=0 ERRORS=0`.
  - `git diff --check` → sin errores de whitespace; único aviso: `specs/110-distribucion-google-play/task.md` convertirá LF a CRLF en el próximo `git add` (heredado de la unidad 110).
  - `git status --short` → cambios pendientes de `110` más los 7 archivos nuevos de `120` (3 documentos, 4 fuentes de test) y `app/src/test/resources/contract/`.
- **Decisiones registradas:** interceptor en memoria en lugar de `MockWebServer`; sin nueva dependencia; `Retry-After` se respeta cuando existe; `401` decide refresh, no reintento directo; sin conectividad decide fallback a caché `STALE`; `400/403/404/409/422`, TLS y JSON inválido se abortan; la cola de idempotencia reutiliza clave y conserva `retryCount`.
- **Riesgos y pendientes materiales:** Backend Go debe ratificar forma de `details` (mapa vs lista), `traceId`/`X-Request-ID`, catálogo de `code`, campos obligatorios, semántica de `409`, `Retry-After`, timeouts y `BASE_URL` por entorno. Existe divergencia real entre `HttpContracts.kt` y `AuthDtos.kt`. No existe `RetryPolicy` productiva: la matriz es solo una tabla de decisión probada. `050` sigue con `task.md` pendiente en el repo aunque se asumió como dependencia previa.
- **No ejecutado:** `MockWebServer`, llamadas a Backend Go, cambios en repositorio Go, `bundleRelease`, firma, publicación, Docker, push, merge ni despliegue.
- **Reversibilidad:** eliminar los 7 archivos nuevos de código/documentos y el directorio de fixtures, y revertir los bloques propios de `task.md`, `specs/README.md` y esta sección; sin reset, push ni rewrite de historial.
