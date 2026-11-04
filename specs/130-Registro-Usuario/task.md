---
project: Nexum-Mobile
unit: 130-Registro-Usuario
kind: implementation-tasks
version: 2.0.0
status: ready
implementation: only_on_explicit_request
---

# TASK — Registro de Usuario (Sign Up)

**Estado:** tareas verificadas con evidencia real (Fase B, 2026-11-04); ver **Evidencia de ejecución** al final. Revisar dependencias en `specs/README.md` antes de cualquier reejecución.

**Dependencias:** `030-di-navegacion-ui`, `040-cliente-http-contratos` en trazabilidad; el alcance mock de esta unidad no las consume. **Regla:** no implementar funcionalidades financieras más allá de los datos de alta, ni cambios externos, sin aprobación adicional.

## Actividades verificables
- [x] **N130-T01 — Crear los modelos DTO de NEXUM-E001 con el fixture del ejemplo 4.1.**
  - **Requisito:** `M130-R02` · **Plan:** `N130-P01`.
  - **Aceptación:** `RegisterRequest`, `Income`, `RecurringExpense`, `RegisterResponse` y enums coinciden con el contrato; prueba contra el fixture 4.1.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** rutas creadas, comandos, salida real, fecha y limitaciones.
- [x] **N130-T02 — Implementar la validación local de registro.**
  - **Requisito:** `M130-R01`, `M130-R03`, `M130-R04` · **Plan:** `N130-P02`.
  - **Aceptación:** regex de email, contraseña ≥8 alfanumérica, coincidencia estricta, ingreso obligatorio, al menos un gasto recurrente, consentimiento versionado y bloqueo de envío sin checkbox.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew lintDebug`.
  - **Evidencia a registrar:** casos de prueba, salida real, fecha y limitaciones.
- [x] **N130-T03 — Definir la interfaz del repositorio y la fuente simulada con retardo de red.**
  - **Requisito:** `M130-R05`, `M130-R02` · **Plan:** `N130-P03`.
  - **Aceptación:** retardo 800–1500 ms, conflicto de correo, fallo de red determinista y mapeo a `AuthResult` sin exponer detalles técnicos a la UI.
  - **Pruebas:** `./gradlew testDebugUnitTest`.
  - **Evidencia a registrar:** archivos, comportamiento probado, salida real y limitaciones.
- [x] **N130-T04 — Construir el ViewModel MVI de registro.**
  - **Requisito:** `M130-R01`…`M130-R05` · **Plan:** `N130-P04`.
  - **Aceptación:** intenciones, estado inmutable, reducer, carga en curso y efectos de navegación/Snackbar sin repetición por recomposición.
  - **Pruebas:** `./gradlew testDebugUnitTest` (transiciones con repositorio falso); `./gradlew assembleDebug`.
  - **Evidencia a registrar:** transiciones cubiertas, salida real, fecha y limitaciones.
- [x] **N130-T05 — Implementar la pantalla de registro en Compose.**
  - **Requisito:** `M130-R01`, `M130-R03` · **Plan:** `N130-P05`.
  - **Aceptación:** campos completos, toggle de visibilidad, checkbox que bloquea el envío, errores en campo, indicador de carga y aviso de conflicto/red.
  - **Pruebas:** `./gradlew assembleDebug`; `./gradlew lintDebug`.
  - **Evidencia a registrar:** archivos, salida real, fecha y limitaciones.
- [x] **N130-T06 — Añadir pruebas unitarias del validador y del ViewModel.**
  - **Requisito:** `M130-R01`…`M130-R05` · **Plan:** `N130-P06`.
  - **Aceptación:** cobertura de éxito, conflicto, fallo de red y validaciones negativas con `kotlinx-coroutines-test`.
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
  - `gradlew testDebugUnitTest` → **BUILD SUCCESSFUL** · 50 tests · 0 fallos (`AuthValidatorTest` 19, `AuthViewModelTest` 10, `LoginViewModelTest` 9, `MockAuthApiTest` 11, `ExampleUnitTest` 1).
  - `gradlew lintDebug` → **BUILD SUCCESSFUL** · 0 errores · 21 avisos (versiones/recursos preexistentes; ninguno del código nuevo).
- **Archivos creados:** `auth/data/dto/AuthDtos.kt`, `auth/data/AuthApi.kt`, `auth/data/MockAuthApi.kt`, `auth/data/TokenStorage.kt`, `auth/data/AuthRepository.kt`, `auth/domain/AuthValidator.kt`, `auth/ui/AuthUiState.kt`, `auth/ui/AuthIntent.kt`, `auth/ui/AuthViewModel.kt`, `auth/ui/LoginViewModel.kt`, `auth/ui/LoginScreen.kt`, `auth/ui/RegisterScreen.kt`, `auth/ui/AuthFlow.kt`, más 4 clases de prueba en `app/src/test`.
- **Modificados:** `gradle/libs.versions.toml`, `app/build.gradle.kts` (+`lifecycle-viewmodel-compose`, +`kotlinx-coroutines-test`, M-D09), `MainActivity.kt`.
- **Incidencias corregidas durante la ejecución:** lambda composable en `RegisterScreen.kt` (elvis de `supportingText`); import erróneo `AuthIntent` → `RegisterIntent` en el test; carrera entre emisión de efectos y colector en tests → dispatchers `UnconfinedTestDispatcher`.
- **Limitaciones:** pruebas de instrumentación/emulador **NO EJECUTADAS** (sin dispositivo); verificación visual manual pendiente; fuente simulada sin Backend real; sin push.
