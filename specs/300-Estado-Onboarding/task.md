---
project: Nexum-Mobile
unit: 300-Estado-Onboarding
kind: implementation-tasks
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# TASK — Consulta de Estado de Onboarding (Get Onboarding Status)

**Estado:** Fase A documental completada; todas las actividades pendientes de autorización expresa para ejecución (`Fase B`). Ver **Cierre y evidencia** al final. Revisar dependencias en `specs/README.md` antes de cualquier implementación.

**Dependencias:** [`140-Login`](../140-Login/spec.md), [`150-Refresh-Token`](../150-Refresh-Token/spec.md), [`030-di-navegacion-ui`](../030-di-navegacion-ui/spec.md), [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md), [`070-permisos-privacidad-accesibilidad`](../070-permisos-privacidad-accesibilidad/spec.md). **Regla:** no implementar código hasta recibir mandato explícito «implementa specs/300-Estado-Onboarding».

## Actividades verificables
- [ ] **N300-T01 — Crear los modelos DTO de NEXUM-E018 (`OnboardingStatusResponse`) y dominio (`OnboardingStatus`).**
  - **Requisito:** `M300-R02` · **Plan:** `N300-P01`.
  - **Aceptación:** Modelos inmutables en Kotlin: `OnboardingStatusResponse(stepsCompleted, finishedAt)` y `OnboardingStatus(stepsCompleted, finishedAt, isFinished)`.
  - **Pruebas:** Pruebas unitarias de serialización/deserialización en `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** archivos creados, comandos, salida real, fecha y limitaciones.

- [ ] **N300-T02 — Ampliar `AuthApi` y `AuthRepository` con la consulta de onboarding y soporte de caché local.**
  - **Requisito:** `M300-R02`, `M300-R04` · **Plan:** `N300-P02`.
  - **Aceptación:** Método `suspend fun getOnboardingStatus(accessToken: String): OnboardingStatusResponse` en `AuthApi` y `suspend fun getOnboardingStatus(): AuthResult<OnboardingStatus>` en `AuthRepository` con persistencia en caché local.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** diff de interfaces, salida real de compilación y limitaciones.

- [ ] **N300-T03 — Implementar simulación en `MockAuthApi`.**
  - **Requisito:** `M300-R05`, `M300-R06` · **Plan:** `N300-P03`.
  - **Aceptación:** Retardo de 200–500 ms, soporte de perfiles con onboarding completado (`finishedAt != null`) e incompleto (`finishedAt = null`), y centinela de error de red `net@error.test`.
  - **Pruebas:** Pruebas unitarias en `MockAuthApiTest`.
  - **Evidencia a registrar:** casos de prueba ejecutados, salida real, fecha y limitaciones.

- [ ] **N300-T04 — Construir `OnboardingRoutingViewModel` MVI con resolución de destinos y fallback offline.**
  - **Requisito:** `M300-R01`, `M300-R04`, `M300-R05` · **Plan:** `N300-P04`.
  - **Aceptación:** Reducer con resolución a `RouteToHome` si `isFinished == true` o `RouteToOnboarding` si `isFinished == false`, uso de caché local ante fallo de red y soporte de reintento.
  - **Pruebas:** Pruebas unitarias en `OnboardingRoutingViewModelTest` con `kotlinx-coroutines-test`.
  - **Evidencia a registrar:** pruebas de ViewModel ejecutadas, salida real, fecha y limitaciones.

- [ ] **N300-T05 — Diseñar integración de enrutamiento inicial en `AppNavHost` / `AuthFlow`.**
  - **Requisito:** `M300-R01`, `M300-R03` · **Plan:** `N300-P05`.
  - **Aceptación:** Enrutamiento reactivo en Compose Navigation sin parpadeos visuales ni sobreescritura de permisos nativos de Android.
  - **Pruebas:** Revisión de componentes Compose; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** archivos de navegación modificados, salida real y limitaciones.

- [ ] **N300-T06 — Añadir suite completa de pruebas unitarias de consulta de onboarding.**
  - **Requisito:** `M300-R01`…`M300-R06` · **Plan:** `N300-P06`.
  - **Aceptación:** Cobertura de enrutamiento a Inicio para usuarios recurrentes, enrutamiento a Bienvenida para usuarios nuevos, resolución offline mediante caché y manejo de error de transporte.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew lintDebug`.
  - **Evidencia a registrar:** reporte de pruebas unitarias, conteo total de tests, salida real, fecha y limitaciones.

## Cierre y evidencia
- **Fase A (documental):** especificación, plan técnico y actividades creadas. **Ninguna tarea implementada ni ejecutada en esta fase.**
- **Fase B (pendiente de mandato explícito):** requerirá orden humana expresa para modificar archivos `.kt`, ejecutar pruebas Gradle y registrar evidencia en `memoria.md`.
- No alterar código de producción, no realizar push ni despliegues en esta fase.
