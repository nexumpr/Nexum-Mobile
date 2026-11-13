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

## 3. Requerimientos docentes para la solución final
- Fidelidad a la maqueta sin quitar funciones; Google Play como forma inicial de evaluación; pruebas, DevSecOps y CI/CD.
- Caché cliente, logout, sesión persistente mientras la identidad siga autorizada, permisos, aviso de privacidad en app/landing, eliminar cuenta y notificaciones push.
- Al menos una función ML y una IA generativa; tratamiento del contenido subido cuando aplique. La rúbrica también menciona analítica en landing page; deberá revisarse junto con aviso de privacidad y consentimiento antes de usar identificadores o seguimiento.
- SQLite/sincronización **condicional** si el proyecto requiere operación local; seguridad y trazabilidad obligatorias.
- Fecha de evaluación: 2026-12-02. **Estas exigencias no habilitan desarrollar funcionalidades hoy**.

## 4. Decisiones pendientes
| ID | Decisión | Responsable sugerido |
| --- | --- | --- |
| M-P01 | SDK target y compatibilidad versiones Kotlin/AGP/Compose; API min 26 provisional | Equipo Mobile. |
| M-P02 | DI, navegación, cliente HTTP y JSON | Equipo Mobile. |
| M-P03 | Caché cliente, almacén seguro, SQLite/Room offline y limpieza | Equipo Mobile + Backend. |
| M-P04 | JWT, renovación, expiración/revocación, logout y errores | Equipo Mobile + Backend. |
| M-P05 | Push, ML/IA generativa, voz, permisos y privacidad de terceros | Equipo Mobile. |
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
