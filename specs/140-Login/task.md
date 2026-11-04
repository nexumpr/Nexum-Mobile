---
project: Nexum-Mobile
unit: 140-Login
kind: implementation-tasks
version: 2.0.0
status: ready
implementation: only_on_explicit_request
---

# TASK — Inicio de Sesión (Login)

**Estado:** tareas verificadas con evidencia real (Fase B, 2026-11-04); ver **Evidencia de ejecución** al final. Revisar dependencias en `specs/README.md` antes de cualquier reejecución.

**Dependencias:** `030-di-navegacion-ui`, `040-cliente-http-contratos` en trazabilidad; contrato compartido en `specs/130-Registro-Usuario/plan.md`. **Regla:** no implementar funcionalidades financieras ni cambios externos sin aprobación adicional.

## Actividades verificables
- [x] **N140-T01 — Crear los modelos DTO de NEXUM-E002, `Session` e interfaz `TokenStorage`.**
  - **Requisito:** `M140-R02`, `M140-R04` · **Plan:** `N140-P01`.
  - **Aceptación:** `LoginRequest` (`email`, `password`, `deviceName?`) y `LoginResponse` (`accessToken`, `refreshToken`, `expiresIn`, `user`) coinciden con el contrato; `TokenStorage` aísla la custodia del token.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** rutas creadas, comandos, salida real, fecha y limitaciones.
- [x] **N140-T02 — Implementar la validación local de login.**
  - **Requisito:** `M140-R01` · **Plan:** `N140-P02`.
  - **Aceptación:** campos no vacíos y formato de email verificados antes de cualquier request.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew lintDebug`.
  - **Evidencia a registrar:** casos de prueba, salida real, fecha y limitaciones.
- [x] **N140-T03 — Definir la fuente simulada de login con retardo y límite de intentos.**
  - **Requisito:** `M140-R03` · **Plan:** `N140-P03`.
  - **Aceptación:** retardo 800–1500 ms, mensaje genérico ante credenciales incorrectas, límite de 5 intentos y fallo de red determinista.
  - **Pruebas:** `./gradlew testDebugUnitTest`.
  - **Evidencia a registrar:** archivos, comportamiento probado, salida real y limitaciones.
- [x] **N140-T04 — Construir el ViewModel MVI de login.**
  - **Requisito:** `M140-R01`…`M140-R05` · **Plan:** `N140-P04`.
  - **Aceptación:** intenciones, estado inmutable, reducer, carga en curso, guardado de sesión tras `200` y efecto de navegación que limpia el historial sin repetición por recomposición.
  - **Pruebas:** `./gradlew testDebugUnitTest` (transiciones con repositorio falso); `./gradlew assembleDebug`.
  - **Evidencia a registrar:** transiciones cubiertas, salida real, fecha y limitaciones.
- [x] **N140-T05 — Implementar la pantalla de login en Compose.**
  - **Requisito:** `M140-R01`, `M140-R03` · **Plan:** `N140-P05`.
  - **Aceptación:** campos, botón con indicador de carga, enlaces "Olvidé mi contraseña" y "Registrarse", mensaje genérico de credenciales y aviso de límite/red.
  - **Pruebas:** `./gradlew assembleDebug`; `./gradlew lintDebug`.
  - **Evidencia a registrar:** archivos, salida real, fecha y limitaciones.
- [x] **N140-T06 — Añadir pruebas unitarias del validador y del ViewModel.**
  - **Requisito:** `M140-R01`…`M140-R05` · **Plan:** `N140-P06`.
  - **Aceptación:** cobertura de éxito, 401 genérico, límite de intentos, error de red y validaciones negativas con `kotlinx-coroutines-test`.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`; `./gradlew lintDebug`.
  - **Evidencia a registrar:** reporte de tests, salida real, fecha y limitaciones.

## Cierre y evidencia
- Reportar pruebas ejecutadas (**resultado real**) y no ejecutadas con causa. Para esta unidad: `./gradlew assembleDebug`; `./gradlew testDebugUnitTest`; `./gradlew lintDebug`.
- Marcar `[x]` solo cuando se verifique la tarea correspondiente; conservar pendientes si no existe evidencia.
- Actualizar `memoria.md` con comandos, archivos, decisiones y riesgos; no avanzar a otra spec automáticamente.
- `git push`, merge, despliegue y operaciones destructivas siguen requiriendo autorización separada.

## Evidencia de ejecución (2026-11-04)
- **Entorno:** Windows · JAVA_HOME = Android Studio JBR (OpenJDK 21.0.10) · Gradle 9.5.0 (wrapper) · AGP 9.3.3 · Kotlin 2.2.10.
- **Comandos y resultados reales:**
  - `gradlew assembleDebug` → **BUILD SUCCESSFUL**.
  - `gradlew testDebugUnitTest` → **BUILD SUCCESSFUL** · 50 tests · 0 fallos (`LoginViewModelTest` 9, `MockAuthApiTest` 11, `AuthValidatorTest` 19, `AuthViewModelTest` 10, `ExampleUnitTest` 1).
  - `gradlew lintDebug` → **BUILD SUCCESSFUL** · 0 errores · 21 avisos (versiones/recursos preexistentes; ninguno del código nuevo).
- **Archivos creados (esta unidad):** `auth/data/dto/AuthDtos.kt` (DTOs E002), `auth/data/AuthApi.kt`, `auth/data/MockAuthApi.kt`, `auth/data/TokenStorage.kt` (`InMemoryTokenStorage`), `auth/data/AuthRepository.kt` (`Session`, `AuthResult`, contrato compartido), `auth/ui/LoginViewModel.kt`, `auth/ui/LoginScreen.kt`; `AuthFlow.kt` y `AuthValidator.kt` compartidos con `130`.
- **Modificados:** `gradle/libs.versions.toml`, `app/build.gradle.kts`, `MainActivity.kt`.
- **Verificado:** payload `email/password/deviceName?`, `200 {accessToken, refreshToken, expiresIn, user}` con guardado en `TokenStorage`, `401` genérico, límite de 5 intentos, error de red reintentable y efecto `NavigateToApp` (sin invitado automático).
- **Limitaciones:** `EncryptedSharedPreferences` **NO EJECUTADO** aquí → obligatorio en `050-sesion-credenciales`; recuperación de contraseña sin endpoint (enlace informativo); instrumentación/emulador **NO EJECUTADA**; sin push.
