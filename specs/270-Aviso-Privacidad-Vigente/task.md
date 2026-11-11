---
project: Nexum-Mobile
unit: 270-Aviso-Privacidad-Vigente
kind: implementation-tasks
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# TASK — Consulta del Aviso de Privacidad Vigente (Current Privacy Notice)

**Estado:** Fase A documental completada; todas las actividades pendientes de autorización expresa para ejecución (`Fase B`). Ver **Cierre y evidencia** al final. Revisar dependencias en `specs/README.md` antes de cualquier implementación.

**Dependencias:** [`130-Registro-Usuario`](../130-Registro-Usuario/spec.md), [`240-Perfil-Usuario`](../240-Perfil-Usuario/spec.md), [`030-di-navegacion-ui`](../030-di-navegacion-ui/spec.md), [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md), [`070-permisos-privacidad-accesibilidad`](../070-permisos-privacidad-accesibilidad/spec.md). **Regla:** no implementar código hasta recibir mandato explícito «implementa specs/270-Aviso-Privacidad-Vigente».

## Actividades verificables
- [ ] **N270-T01 — Crear los modelos DTO de NEXUM-E015 (`PrivacyNoticeResponse`) y dominio (`PrivacyNotice`).**
  - **Requisito:** `M270-R02` · **Plan:** `N270-P01`.
  - **Aceptación:** Modelos inmutables en Kotlin con campos `version: String`, `title: String`, `body: String` y `publishedAt: String`.
  - **Pruebas:** Pruebas unitarias de serialización/deserialización en `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** archivos creados, comandos, salida real, fecha y limitaciones.

- [ ] **N270-T02 — Ampliar `AuthApi` y `AuthRepository` con la consulta del aviso y fallback local.**
  - **Requisito:** `M270-R02`, `M270-R04` · **Plan:** `N270-P02`.
  - **Aceptación:** Método `suspend fun getCurrentPrivacyNotice(locale: String? = null): PrivacyNoticeResponse` en `AuthApi` y `suspend fun getCurrentPrivacyNotice(locale: String = "es-MX"): AuthResult<PrivacyNotice>` en `AuthRepository` con soporte para copia local de respaldo.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** diff de interfaces, salida real de compilación y limitaciones.

- [ ] **N270-T03 — Implementar simulación en `MockAuthApi`.**
  - **Requisito:** `M270-R03`, `M270-R06` · **Plan:** `N270-P03`.
  - **Aceptación:** Retardo de 200–500 ms, retorno de aviso formal versionado (`2026-11-v1`) sin requerir JWT y centinela de fallo de red con `locale = "net_error"`.
  - **Pruebas:** Pruebas unitarias en `MockAuthApiTest`.
  - **Evidencia a registrar:** casos de prueba ejecutados, salida real, fecha y limitaciones.

- [ ] **N270-T04 — Construir `PrivacyNoticeViewModel` MVI con soporte de estados, reintento y propagación de versión.**
  - **Requisito:** `M270-R03`, `M270-R05` · **Plan:** `N270-P04`.
  - **Aceptación:** Reducer con transiciones a `Loading`, `Content`, manejo de `isFallback = true` ante fallos de red y preservación del campo `version` en memoria para el flujo de registro.
  - **Pruebas:** Pruebas unitarias en `PrivacyNoticeViewModelTest` con `kotlinx-coroutines-test`.
  - **Evidencia a registrar:** pruebas de ViewModel ejecutadas, salida real, fecha y limitaciones.

- [ ] **N270-T05 — Diseñar componente Composable `PrivacyNoticeScreen` en Jetpack Compose con Material 3.**
  - **Requisito:** `M270-R01`, `M270-R05` · **Plan:** `N270-P05`.
  - **Aceptación:** Pantalla accesible con scroll vertical fluido, barra superior con botón de cierre accesible, cabeceras semánticas para lectores de pantalla, banner discreto en caso de fallback local y botón de reintento ante errores de red.
  - **Pruebas:** Revisión de componentes Compose; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** archivos de UI creados/modificados, salida real y limitaciones.

- [ ] **N270-T06 — Añadir suite completa de pruebas unitarias de consulta del aviso de privacidad.**
  - **Requisito:** `M270-R01`…`M270-R06` · **Plan:** `N270-P06`.
  - **Aceptación:** Cobertura de respuesta exitosa, activación de fallback offline, soporte de locale y persistencia de scroll tras recomposiciones.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew lintDebug`.
  - **Evidencia a registrar:** reporte de pruebas unitarias, conteo total de tests, salida real, fecha y limitaciones.

## Cierre y evidencia
- **Fase A (documental):** especificación, plan técnico y actividades creadas. **Ninguna tarea implementada ni ejecutada en esta fase.**
- **Fase B (pendiente de mandato explícito):** requerirá orden humana expresa para modificar archivos `.kt`, ejecutar pruebas Gradle y registrar evidencia en `memoria.md`.
- No alterar código de producción, no realizar push ni despliegues en esta fase.
