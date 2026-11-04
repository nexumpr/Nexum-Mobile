---
unit: 000-arquitectura-base
project: Nexum-Mobile
kind: pending-architecture-tasks
version: 1.0.0
status: active-reference
implementation_authorized: only_for_explicitly_selected_phase
---

# TASK — Arquitectura base Nexum-Mobile

> **Tareas FUTURAS, ninguna ejecutada.** No autorizan implementar pantallas, servicios ni el resto de las funciones de Nexum.

- [ ] **M-T01 — Ratificar entorno Android base** (Plan M-P01; requisitos **M-AR-01**). **Dependencia:** constitución aprobada. **Aceptación:** documentar y verificar compatibilidad de Kotlin, Compose, Material 3, AGP, Gradle, min/target SDK; generar evidencia real cuando se autorice. **Evidencia futura:** matriz de versiones + reporte build.
- [ ] **M-T02 — Establecer límites de capas y MVI** (Plan M-P02; **M-AR-02, M-AR-03**). **Dependencia:** M-T01. **Aceptación:** diagramar UI, intenciones, estado y procesamiento con flujo unidireccional e inmutabilidad, sin mezclar red en Composables. **Evidencia:** diseño de responsabilidades revisado.
- [ ] **M-T03 — Precisar estrategia DI y pruebas sustituibles** (Plan M-P02; **M-AR-05**). **Dependencia:** M-T02. **Aceptación:** seleccionar DI/contratos técnicamente justificados con componentes aislables; no fijar Koin/Hilt sin aprobación. **Evidencia:** registro de decisión y casos de pruebas previstos.
- [ ] **M-T04 — Definir efectos/ciclo de vida y concurrencia** (Plan M-P03; **M-AR-04**). **Dependencia:** M-T02. **Aceptación:** política de `LaunchedEffect`, cancelación, eventos efímeros, reintentos y prevención de escrituras duplicadas. **Evidencia:** escenarios documentales y matriz de pruebas futuras.
- [ ] **M-T05 — Acordar contrato de red y sesión con Backend** (Plan M-P04; **M-AR-06, M-AR-07, M-AR-13**). **Dependencia:** responsables Backend y Mobile. **Aceptación:** contratos de HTTPS/JSON, versiones, errores, JWT, renovación/revocación/logout y seguridad de transporte aprobados por ambos equipos. **Evidencia:** especificación de interfaz técnica, sin endpoints de negocio.
- [ ] **M-T06 — Diseñar custodia local y caché** (Plan M-P05; **M-AR-08, M-AR-09**). **Dependencia:** M-T05. **Aceptación:** selección del almacenamiento autorizado, protección, TTL, invalidación, logout/borrado y representación de datos obsoletos. **Evidencia:** política de datos local verificable.
- [ ] **M-T07 — Acordar reintentos y sincronización** (Plan M-P05; **M-AR-10**). **Dependencia:** M-T05–M-T06. **Aceptación:** identificadores de idempotencia, estado pendiente/confirmado, reconciliación y conflictos acordados con Backend. **Evidencia:** casos de falla/reintento genéricos.
- [ ] **M-T08 — Definir UX accesible, permisos y privacidad** (Plan M-P06; **M-AR-11, M-AR-12, M-AR-17, M-AR-18**). **Dependencia:** M-T02. **Aceptación:** reglas Material 3, accesibilidad, permisos en contexto, avisos, limpieza de cuenta y tratamiento de contenido externo documentados. **Evidencia:** matriz de accesibilidad/privacidad y riesgos, sin pantallas funcionales.
- [ ] **M-T09 — Planificar pruebas/DevSecOps móvil** (Plan M-P07; **M-AR-14, M-AR-15**). **Dependencia:** decisiones M-T01–M-T08 definidas. **Aceptación:** matriz de pruebas/seguridad, Gradle/lint/secretos/SBOM con gates realistas; sin resultados inventados. **Evidencia:** plan de calidad verificable.
- [ ] **M-T10 — Definir estrategia de versiones y entrega** (Plan M-P08; **M-AR-16, M-AR-19**). **Dependencia:** M-T09. **Aceptación:** versión Android, firma, CI/publicación Play y requisitos posteriores de push/ML/IA tratados como capacidades futuras, no implementadas. **Evidencia:** plan de release sujeto a autorización.
- [ ] **M-T11 — Validación SDD cruzada** (Plan M-P09; **M-AR-20 y M-AR-01 a M-AR-19**). **Dependencia:** M-T01–M-T10 definidas. **Aceptación:** cada requisito aparece en plan/tarea, ninguna tarea ejecutada automáticamente, ninguna funcionalidad específica diseñada y constitución respetada. **Evidencia:** matriz de trazabilidad y revisión humana.

## Restricción de estado
Todas las tareas se conservan `- [ ]`. No generar código, endpoints, navegación, pantallas, conexiones, instalaciones, Git push ni publicación mientras el docente no autorice implementación.


## Estado en el SDD por fases (v1.0.0)
Este documento resume requisitos transversales y **no es una orden de implementación monolítica**. Las actividades técnicas se han dividido en `../010-*` a `../120-*`; seleccionar solo una unidad mediante `specs/README.md`. Las casillas anteriores no son evidencia de ejecución; siguen pendientes hasta verificarse por fase. No implementar funcionalidades específicas de Nexum en esta etapa.
