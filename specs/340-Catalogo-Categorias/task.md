---
project: Nexum-Mobile
unit: 340-Catalogo-Categorias
kind: implementation-tasks
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# TASK — Catálogo de Categorías (Categories Catalog)

**Estado:** Fase A documental completada; todas las actividades pendientes de autorización expresa para ejecución (`Fase B`). Ver **Cierre y evidencia** al final. Revisar dependencias en `specs/README.md` antes de cualquier implementación.

**Dependencias:** [`030-di-navegacion-ui`](../030-di-navegacion-ui/spec.md), [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md), [`060-cache-offline`](../060-cache-offline/spec.md). **Regla:** no implementar código hasta recibir mandato explícito «implementa specs/340-Catalogo-Categorias».

## Actividades verificables
- [ ] **N340-T01 — Crear los modelos del contrato NEXUM-E022 (`CategoryResponse`, `Category`, `CategoryCatalog`).**
  - **Requisito:** `M340-R02` · **Plan:** `N340-P01`.
  - **Aceptación:** Modelos inmutables; `active` se mapea a `isActive`; `CategoryCatalog` conserva el origen `REMOTE`/`CACHE`/`BUNDLED`.
  - **Pruebas:** Pruebas unitarias de deserialización en `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** archivos creados, comandos, salida real, fecha y limitaciones.

- [ ] **N340-T02 — Ampliar `AuthApi` y `AuthRepository` con la consulta, filtrado y respaldo local.**
  - **Requisito:** `M340-R02`, `M340-R04` · **Plan:** `N340-P02`.
  - **Aceptación:** `suspend fun getCategories(locale: String? = null): List<CategoryResponse>` en `AuthApi` y `suspend fun getCategories(): AuthResult<CategoryCatalog>` en `AuthRepository`; se descartan entradas sin `id` o `name`; el error estándar conserva `traceId`.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** diff de interfaces, salida real de compilación y limitaciones.

- [ ] **N340-T03 — Implementar simulación en `MockAuthApi`.**
  - **Requisito:** `M340-R05` · **Plan:** `N340-P03`.
  - **Aceptación:** Retardo de 200–500 ms; nueve categorías documentadas (Antojos, Comida, Transporte, Servicios, Vivienda, Entretenimiento, Suscripciones, Compras personales, Otros) con `id` estable, `icon` y `active`; caso inactivo; centinela `net@error.test`.
  - **Pruebas:** Pruebas unitarias en `MockAuthApiTest`.
  - **Evidencia a registrar:** casos de prueba ejecutados, salida real, fecha y limitaciones.

- [ ] **N340-T04 — Construir `CategoryCatalogViewModel` MVI con selección por identificador.**
  - **Requisito:** `M340-R01`, `M340-R03`, `M340-R04` · **Plan:** `N340-P04`.
  - **Aceptación:** Reducer con `LoadCategories`, `SelectCategory`, `Retry` y `DismissError`; la selección guarda `Category.id`; estado vacío, error y respaldo local sin fingir vigencia remota.
  - **Pruebas:** Pruebas unitarias en `CategoryCatalogViewModelTest` con `kotlinx-coroutines-test`.
  - **Evidencia a registrar:** pruebas de ViewModel ejecutadas, salida real, fecha y limitaciones.

- [ ] **N340-T05 — Diseñar `CategorySelector` en Compose con Material 3 e integrarlo en los puntos de consumo.**
  - **Requisito:** `M340-R01`, `M340-R03` · **Plan:** `N340-P05`.
  - **Aceptación:** Selector de una sola categoría con nombre e icono, inactivas deshabilitadas, estado vacío y etiquetas accesibles; emite `id` y no el nombre.
  - **Pruebas:** Revisión de componentes Compose; `./gradlew assembleDebug`.
  - **Evidencia a registrar:** archivos de UI modificados, salida real y limitaciones.

- [ ] **N340-T06 — Añadir suite completa de pruebas unitarias del catálogo de categorías.**
  - **Requisito:** `M340-R01`…`M340-R06` · **Plan:** `N340-P06`.
  - **Aceptación:** Cobertura de carga, `locale`, selección, inactiva, vacío, respuesta malformada, `400`/`429`/`500`, sin conexión y respaldo local.
  - **Pruebas:** `./gradlew testDebugUnitTest`; `./gradlew lintDebug`.
  - **Evidencia a registrar:** reporte de pruebas unitarias, conteo total de tests, salida real, fecha y limitaciones.

## Cierre y evidencia
- **Fase A (documental):** especificación, plan técnico y actividades creadas. **Ninguna tarea implementada ni ejecutada en esta fase.**
- **Fase B (pendiente de mandato explícito):** requerirá orden humana expresa para modificar archivos `.kt`, ejecutar pruebas Gradle y registrar evidencia en `memoria.md`.
- No alterar código de producción, no realizar push ni despliegues en esta fase.
