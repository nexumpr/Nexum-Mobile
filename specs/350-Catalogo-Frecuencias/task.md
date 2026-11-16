---
project: Nexum-Mobile
unit: 350-Catalogo-Frecuencias
kind: implementation-tasks
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# TASK — Catálogo de Frecuencias (Frequencies Catalog)

**Estado:** Fase A documental completada; todas las actividades pendientes de autorización expresa para ejecución (`Fase B`). Ver **Cierre y evidencia** al final. Revisar dependencias en `specs/README.md` antes de cualquier implementación.

**Dependencias:** [`130-Registro-Usuario`](../130-Registro-Usuario/spec.md), [`340-Catalogo-Categorias`](../340-Catalogo-Categorias/spec.md), [`030-di-navegacion-ui`](../030-di-navegacion-ui/spec.md), [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md), [`060-cache-offline`](../060-cache-offline/spec.md). **Regla:** no implementar código hasta recibir mandato explícito «implementa specs/350-Catalogo-Frecuencias».

## Actividades verificables
- [ ] **N350-T01 — Crear los modelos del contrato NEXUM-E023 (`FrequencyContext`, `FrequencyResponse`, `FrequencyOption`, `FrequencyCatalog`).**
  - **Requisito:** `M350-R02` · **Plan:** `N350-P01`.
  - **Aceptación:** Modelos inmutables; `FrequencyContext` serializa a `income`/`recurring`/`movement`; `FrequencyCatalog` conserva el origen.
  - **Pruebas:** Pruebas unitarias de deserialización en `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** archivos creados, comandos, salida real, fecha y limitaciones.

- [ ] **N350-T02 — Ampliar `AuthApi` y `AuthRepository` con la consulta por contexto y el respaldo local.**
  - **Requisito:** `M350-R02`, `M350-R04` · **Plan:** `N350-P02`.
  - **Aceptación:** `suspend fun getFrequencies(context: FrequencyContext): List<FrequencyResponse>` en `AuthApi` y `suspend fun getFrequencies(context: FrequencyContext): AuthResult<FrequencyCatalog>` en `AuthRepository`; códigos desconocidos se descartan; el error estándar conserva `traceId`.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** diff de interfaces, salida real de compilación y limitaciones.

- [ ] **N350-T03 — Implementar simulación en `MockAuthApi`.**
  - **Requisito:** `M350-R05` · **Plan:** `N350-P03`.
  - **Aceptación:** Retardo de 200–500 ms; nueve códigos documentados con etiqueta y `validFor`; filtrado por `context`; limitación M-D08 en el alta; centinela `net@error.test`.
  - **Pruebas:** Pruebas unitarias en `MockAuthApiTest`.
  - **Evidencia a registrar:** casos de prueba ejecutados, salida real, fecha y limitaciones.

- [ ] **N350-T04 — Construir `FrequencyCatalogViewModel` MVI con cambio de contexto y restricción M-D08.**
  - **Requisito:** `M350-R01`, `M350-R03`, `M350-R04` · **Plan:** `N350-P04`.
  - **Aceptación:** Reducer con `ChangeContext`, `LoadFrequencies`, `SelectFrequency`, `Retry` y `DismissError`; la selección guarda el código; el alta solo habilita `monthly`; ningún factor genera fechas.
  - **Pruebas:** Pruebas unitarias en `FrequencyCatalogViewModelTest` con `kotlinx-coroutines-test`.
  - **Evidencia a registrar:** pruebas de ViewModel ejecutadas, salida real, fecha y limitaciones.

- [ ] **N350-T05 — Diseñar `FrequencySelector` en Compose con Material 3.**
  - **Requisito:** `M350-R01`, `M350-R03` · **Plan:** `N350-P05`.
  - **Aceptación:** Menú de una sola frecuencia con etiquetas del catálogo, opciones no válidas deshabilitadas y etiquetas accesibles; emite `code` y no la etiqueta.
  - **Pruebas:** Revisión de componentes Compose; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** archivos de UI modificados, salida real y limitaciones.

- [ ] **N350-T06 — Añadir suite completa de pruebas unitarias del catálogo de frecuencias.**
  - **Requisito:** `M350-R01`…`M350-R06` · **Plan:** `N350-P06`.
  - **Aceptación:** Cobertura de los nueve códigos, tres contextos, opción no válida, M-D08, `400`/`429`/`500`, sin conexión y respaldo local; verificación de que los factores no generan vencimientos.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew lintDebug`.
  - **Evidencia a registrar:** reporte de pruebas unitarias, conteo total de tests, salida real, fecha y limitaciones.

## Cierre y evidencia
- **Fase A (documental):** especificación, plan técnico y actividades creadas. **Ninguna tarea implementada ni ejecutada en esta fase.**
- **Fase B (pendiente de mandato explícito):** requerirá orden humana expresa para modificar archivos `.kt`, ejecutar pruebas Gradle y registrar evidencia en `memoria.md`.
- No alterar código de producción, no realizar push ni despliegues en esta fase.
