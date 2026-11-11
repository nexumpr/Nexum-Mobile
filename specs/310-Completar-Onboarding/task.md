---
project: Nexum-Mobile
unit: 310-Completar-Onboarding
kind: implementation-tasks
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# TASK — Finalización de Onboarding (Complete Onboarding)

**Estado:** Fase A documental completada; todas las actividades pendientes de autorización expresa para ejecución (`Fase B`). Ver **Cierre y evidencia** al final. Revisar dependencias en `specs/README.md` antes de cualquier implementación.

**Dependencias:** [`300-Estado-Onboarding`](../300-Estado-Onboarding/spec.md), [`140-Login`](../140-Login/spec.md), [`150-Refresh-Token`](../150-Refresh-Token/spec.md), [`030-di-navegacion-ui`](../030-di-navegacion-ui/spec.md), [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md), [`070-permisos-privacidad-accesibilidad`](../070-permisos-privacidad-accesibilidad/spec.md). **Regla:** no implementar código hasta recibir mandato explícito «implementa specs/310-Completar-Onboarding».

## Actividades verificables
- [ ] **N310-T01 — Crear los modelos DTO de NEXUM-E019 (`UpdateOnboardingRequest`).**
  - **Requisito:** `M310-R02` · **Plan:** `N310-P01`.
  - **Aceptación:** Modelo inmutable en Kotlin: `UpdateOnboardingRequest(completed, limitedMode)`.
  - **Pruebas:** Pruebas unitarias de serialización/deserialización en `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** archivos creados, comandos, salida real, fecha y limitaciones.

- [ ] **N310-T02 — Ampliar `AuthApi` y `AuthRepository` con la mutación de estado de onboarding.**
  - **Requisito:** `M310-R02`, `M310-R04` · **Plan:** `N310-P02`.
  - **Aceptación:** Método `suspend fun updateOnboardingStatus(accessToken: String, request: UpdateOnboardingRequest, idempotencyKey: String? = null): OnboardingStatusResponse` en `AuthApi` y `suspend fun completeOnboarding(limitedMode: Boolean): AuthResult<OnboardingStatus>` en `AuthRepository`.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** diff de interfaces, salida real de compilación y limitaciones.

- [ ] **N310-T03 — Implementar simulación en `MockAuthApi`.**
  - **Requisito:** `M310-R05`, `M310-R06` · **Plan:** `N310-P03`.
  - **Aceptación:** Retardo de 300–700 ms, persistencia en memoria de la finalización con timestamp RFC 3339, soporte de `limitedMode = true/false` y centinela de fallo de red con `net_error_onboarding`.
  - **Pruebas:** Pruebas unitarias en `MockAuthApiTest`.
  - **Evidencia a registrar:** casos de prueba ejecutados, salida real, fecha y limitaciones.

- [ ] **N310-T04 — Construir `CompleteOnboardingViewModel` MVI con soporte de modo limitado y avance resiliente.**
  - **Requisito:** `M310-R01`, `M310-R04`, `M310-R05` · **Plan:** `N310-P04`.
  - **Aceptación:** Reducer con soporte de intents `ContinueFull` y `ContinueLimited`, control de `isSubmitting = true`, fail-open local ante caída de red y emisión de efecto `NavigateToHome`.
  - **Pruebas:** Pruebas unitarias en `CompleteOnboardingViewModelTest` con `kotlinx-coroutines-test`.
  - **Evidencia a registrar:** pruebas de ViewModel ejecutadas, salida real, fecha y limitaciones.

- [ ] **N310-T05 — Integrar disparadores en Compose `OnboardingScreen` con navegación fluida a `HomeScreen`.**
  - **Requisito:** `M310-R01`, `M310-R03`, `M310-R05` · **Plan:** `N310-P05`.
  - **Aceptación:** Botón principal "Continuar a Nexum" y botón secundario "Continuar con funciones limitadas", desapilado completo del asistente del backstack sin suplantar permisos de Android.
  - **Pruebas:** Revisión de componentes Compose; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** archivos de UI y navegación modificados, salida real y limitaciones.

- [ ] **N310-T06 — Añadir suite completa de pruebas unitarias de finalización de onboarding.**
  - **Requisito:** `M310-R01`…`M310-R06` · **Plan:** `N310-P06`.
  - **Aceptación:** Cobertura de modo completo, modo limitado, control de doble toque, avance resiliente offline y reseteo del backstack.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew lintDebug`.
  - **Evidencia a registrar:** reporte de pruebas unitarias, conteo total de tests, salida real, fecha y limitaciones.

## Cierre y evidencia
- **Fase A (documental):** especificación, plan técnico y actividades creadas. **Ninguna tarea implementada ni ejecutada en esta fase.**
- **Fase B (pendiente de mandato explícito):** requerirá orden humana expresa para modificar archivos `.kt`, ejecutar pruebas Gradle y registrar evidencia en `memoria.md`.
- No alterar código de producción, no realizar push ni despliegues en esta fase.
