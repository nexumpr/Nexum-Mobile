---
project: Nexum-Mobile
unit: 030-di-navegacion-ui
kind: implementation-tasks
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# TASK — Inyección de dependencias y shell UI

**Estado:** tareas verificadas e implementadas.

**Dependencias:** `020-nucleo-mvi`. **Regla:** no implementar funcionalidades financieras ni cambios externos sin aprobación adicional.

## Actividades verificables
- [x] **N030-T01 — Definir o seleccionar DI con justificación.**
  - **Requisito:** `M030-R01` · **Plan:** `N030-P01`.
  - **Aceptación:** Seleccionada arquitectura de Contenedor de Inyección de Dependencias manual (`AppContainer` / `DefaultAppContainer`) justificada por su costo cero de compilación (sin KSP/kapt ni generación de código pesada), sin reflexión, aislamiento limpio de `TokenStorage` y `AuthRepository`, y desacoplamiento para pruebas con dobles sin frameworks externos (MO-PR13).
  - **Pruebas:** `.\gradlew.bat testDebugUnitTest` (`AppContainerTest.appContainerAllowsInjectingTestDoubles`, `defaultAppContainerInitializesLazySingletons`).
  - **Evidencia a registrar:** `app/src/main/java/mx/nexum/app/core/di/AppContainer.kt`, `app/src/test/java/mx/nexum/app/core/di/AppContainerTest.kt`.
- [x] **N030-T02 — Implementar shell técnico y navegación sin pantallas de negocio.**
  - **Requisito:** `M030-R02` · **Plan:** `N030-P02`.
  - **Aceptación:** Creada abstracción de navegación técnica desacoplada (`AppDestination`, `TechnicalDestination`, `AppNavigator`, `SimpleNavigator`) y shell UI composable Material 3 (`NexumAppShell`) con Scaffold, TopAppBar, navegación accesible y aislamiento estricto de red sin pantallas ni flujos de negocio.
  - **Pruebas:** `.\gradlew.bat testDebugUnitTest` (`AppNavigatorTest` 5 pruebas passing). `assembleDebug` y `lintDebug` BUILD SUCCESSFUL.
  - **Evidencia a registrar:** `app/src/main/java/mx/nexum/app/core/navigation/NavigationContracts.kt`, `app/src/main/java/mx/nexum/app/core/ui/NexumAppShell.kt`, `app/src/test/java/mx/nexum/app/core/navigation/AppNavigatorTest.kt`.
- [x] **N030-T03 — Probar inyección de dobles y semántica de accesibilidad.**
  - **Requisito:** `M030-R03` · **Plan:** `N030-P03`.
  - **Aceptación:** Pruebas unitarias de sustitución con `FakeTokenStorage` y `FakeAuthRepository` verificando que la aplicación opera con dobles en tests; `NexumAppShell` estructurado con `contentDescription` en navegación, título y snackbars conforme a accesibilidad Material 3 (MO-PR07).
  - **Pruebas:** Suite de 64 tests unitarios ejecutados, 0 fallos, 100% de éxito.
  - **Evidencia a registrar:** `app/build/reports/tests/testDebugUnitTest/index.html`, `memoria.md` §37.

## Cierre y evidencia
- Reportar pruebas ejecutadas (**resultado real**) y no ejecutadas con causa. Para esta unidad: `./gradlew testDebugUnitTest`; pruebas Compose/instrumentadas en emulador si existe.
- Marcar `[x]` solo cuando se verifique la tarea correspondiente; conservar pendientes si no existe evidencia.
- Actualizar `memoria.md` con comandos, archivos, decisiones y riesgos; no avanzar a otra spec automáticamente.
- `git push`, merge, despliegue y operaciones destructivas siguen requiriendo autorización separada.
