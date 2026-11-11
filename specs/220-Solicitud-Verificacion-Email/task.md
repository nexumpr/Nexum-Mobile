---
project: Nexum-Mobile
unit: 220-Solicitud-Verificacion-Email
kind: implementation-tasks
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# TASK — Solicitud de Verificación de Correo (Request Email Verification)

**Estado:** Fase A documental completada; todas las actividades pendientes de autorización expresa para ejecución (`Fase B`). Ver **Cierre y evidencia** al final. Revisar dependencias en `specs/README.md` antes de cualquier implementación.

**Dependencias:** [`130-Registro-Usuario`](../130-Registro-Usuario/spec.md), [`140-Login`](../140-Login/spec.md), [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md). **Regla:** no implementar código hasta recibir mandato explícito «implementa specs/220-Solicitud-Verificacion-Email».

## Actividades verificables
- [ ] **N220-T01 — Crear los modelos DTO de NEXUM-E010 (`EmailVerificationRequest`, `EmailVerificationResponse`) y `EmailVerificationOutcome`.**
  - **Requisito:** `M220-R02` · **Plan:** `N220-P01`.
  - **Aceptación:** Modelos inmutables en Kotlin con campo `email` opcional y mensaje neutro de respuesta.
  - **Pruebas:** Pruebas unitarias de instanciación en `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** archivos creados, comandos, salida real, fecha y limitaciones.

- [ ] **N220-T02 — Ampliar `AuthApi` y `AuthRepository` con la solicitud de verificación.**
  - **Requisito:** `M220-R02`, `M220-R03` · **Plan:** `N220-P02`.
  - **Aceptación:** Soporte dual en interfaces para emitir la petición con o sin token de acceso de sesión.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** diff de interfaces, salida real de compilación y limitaciones.

- [ ] **N220-T03 — Implementar simulación en `MockAuthApi` con respuesta neutra y centinelas.**
  - **Requisito:** `M220-R03`, `M220-R05` · **Plan:** `N220-P03`.
  - **Aceptación:** Retardo de 600–1000 ms, retorno `202 Accepted` invariable ante correos válidos, error `RATE_LIMITED` ante `rate_limited@error.test` y `NETWORK` ante `net@error.test`.
  - **Pruebas:** Pruebas unitarias en `MockAuthApiTest`.
  - **Evidencia a registrar:** casos de prueba ejecutados, salida real, fecha y limitaciones.

- [ ] **N220-T04 — Construir `EmailVerificationViewModel` MVI con cooldown antispam de 60 s.**
  - **Requisito:** `M220-R01`, `M220-R04` · **Plan:** `N220-P04`.
  - **Aceptación:** Reducer determinista con temporizador de cuenta regresiva, bloqueo de peticiones durante cooldown y manejo de `AuthErrorKind.RATE_LIMITED`.
  - **Pruebas:** Pruebas unitarias en `EmailVerificationViewModelTest` con `kotlinx-coroutines-test`.
  - **Evidencia a registrar:** pruebas de ViewModel ejecutadas, salida real, fecha y limitaciones.

- [ ] **N220-T05 — Implementar pantalla/banner `EmailVerificationRequestScreen` en Compose con Material 3.**
  - **Requisito:** `M220-R01`, `M220-R04` · **Plan:** `N220-P05`.
  - **Aceptación:** Composable accesible con botón "Reenviar correo", texto de cuenta regresiva ("Reenviar en Xs"), indicador de carga y feedback neutro de confirmación.
  - **Pruebas:** Revisión de componentes Compose; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** archivos de UI creados, salida real y limitaciones.

- [ ] **N220-T06 — Añadir suite completa de pruebas unitarias de solicitud de verificación.**
  - **Requisito:** `M220-R01`…`M220-R05` · **Plan:** `N220-P06`.
  - **Aceptación:** Cobertura de anti-enumeración (respuesta neutra), rate limiting (429), bucle de temporizador de cooldown, errores de red y envío con/sin sesión.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew lintDebug`.
  - **Evidencia a registrar:** reporte de pruebas unitarias, conteo total de tests, salida real, fecha y limitaciones.

## Cierre y evidencia
- **Fase A (documental):** especificación, plan técnico y actividades creadas. **Ninguna tarea implementada ni ejecutada en esta fase.**
- **Fase B (pendiente de mandato explícito):** requerirá orden humana expresa para modificar archivos `.kt`, ejecutar pruebas Gradle y registrar evidencia en `memoria.md`.
- No alterar código de producción, no realizar push ni despliegues en esta fase.
