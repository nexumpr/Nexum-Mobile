---
name: sdd-traceability
description: Auditar manualmente trazabilidad y coherencia de requisitos M-AR entre spec, plan y task; reportar inconsistencias sin implementar fuera de la unidad autorizada.
---

# Skill — Trazabilidad SDD

**Cuándo:** al revisar un cambio documental de arquitectura base o antes de presentar la actividad.

## Procedimiento
1. Leer constitución y estado de ratificación.
2. Enumerar IDs `M-AR-*` de `spec.md`.
3. Para cada ID, localizar plan y al menos una tarea; detectar referencias inventadas y tareas sin requisito.
4. Verificar requisitos verificables, dependencias y criterios de aceptación documentales o futuros medibles.
5. Asegurar que `task.md` use exclusivamente `- [ ]` y que no hay rutas/funcionalidades de negocio.
6. Revisar fuente, riesgos, decisiones abiertas y ausencia de afirmaciones de pruebas ficticias.

**Salida:** matriz requisito → plan → tarea, hallazgos, estado `documental / pendiente`; no cambiar estados ni ejecutar tareas.
