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
