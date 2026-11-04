# AGENTS.md — Nexum-Mobile | gobierno SDD operativo

## Jerarquía de fuentes
1. **Constitución vigente**: `.specify/memory/constitution.md` (única fuente); `constitution.md` raíz es índice.
2. `memoria.md`: decisiones aprobadas, riesgos, historial y evidencia; un pendiente no es una decisión.
3. `specs/README.md`: orden y dependencias de unidades.
4. `specs/<unidad>/spec.md` → `plan.md` → `task.md`: única unidad activa por solicitud humana.
5. Skills y agentes son instrucciones subordinadas; nunca superan la constitución.

## Alcance y permisos
- Tecnología aprobada: **Android Kotlin/Jetpack Compose MVI**. No confundir las tecnologías de los repos docentes con Nexum.
- La constitución y el índice **sí están vigentes**; las tareas `- [ ]` indican pendientes, **no** errores de documentación.
- Para **documentar** una unidad: leer, actualizar con aprobación y mantener trazabilidad, sin ejecutar.
- Para **implementar**: se requiere mandato explícito «implementa `specs/XXX-...`». Verifica dependencia, estado, decisiones bloqueantes y alcance; si falta una decisión material, detente en esa parte y pregunta solo por ella.
- El agente puede editar código y ejecutar comandos de build/pruebas **únicamente para la unidad autorizada**, solicitando permiso de edición/terminal conforme a OpenCode. No puede extrapolar autorización a otras unidades.
- **Nunca** realizar push, merge, despliegue, destrucción de recursos, instalación global de software, cambio de autenticación, acceso externo a otros repositorios o activación de servicios con costo sin aprobación independiente.
- No leer, registrar ni transmitir credenciales, datos financieros personales, bases reales ni tokens; usar datos sintéticos.
- No usar Docker como requisito local. No modificar el repositorio hermano ni GymSync.

## Protocolo de implementación de una unidad
1. Declarar nombre de la unidad, requisitos y tareas exactas; listar archivos que se crearán/modificarán.
2. Confirmar que las dependencias precedentes tienen evidencia; si no, trabajar en lo que no dependa de ellas o reportar el bloqueo.
3. Aplicar solo cambios estrictamente necesarios; respetar arquitectura y no inventar endpoints o pantallas financieras.
4. Ejecutar validaciones reales especificadas en `task.md`. Si faltan herramientas, registrar `NO EJECUTADO` y la razón; no fabricar resultados.
5. Actualizar `task.md` **solo para tareas verificadas** y registrar evidencia con entorno, comandos y resultados en `memoria.md`; el cumplimiento de una spec no se infiere de una compilación.
6. Detenerse al terminar la unidad. Presentar resumen, resultados, riesgos y solicitar aprobación antes de cualquier push o nueva unidad.

## Convenciones
- Especificaciones atómicas en `specs/NNN-nombre/` con `spec.md`, `plan.md`, `task.md` (singular).
- IDs de requisitos únicos por unidad; toda tarea referencia al menos un ID y un plan; toda spec tiene pruebas y criterios de aceptación.
- Se distinguen **decisión vigente**, **alternativa pendiente**, **implementado**, **probado** y **desplegado**.
- Características financieras, funcionalidades de voz, ML, IA, login funcional, landing y Google Play se especificarán aparte cuando sean autorizadas.
