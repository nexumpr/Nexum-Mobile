---
name: architecture-base
description: Guiar la redacción o revisión de spec, plan y task exclusivamente de arquitectura base Nexum-Mobile, con ejecución únicamente si hay unidad autorizada.
---

# Skill — Arquitectura base

**Cuándo usar:** ante solicitud humana de elaborar o ajustar documentación de arquitectura base.
**Fuente:** `.specify/memory/constitution.md` → `memoria.md` → `specs/000-arquitectura-base/`.

## Procedimiento
1. Identificar objetivo documental y exclusiones (nada de funcionalidades).
2. Separar decisiones aprobadas, propuestas, pendientes y ya implementadas (requiere evidencia).
3. Comprobar que el documento respeta Android, Kotlin, Compose, Material 3 y MVI y las obligaciones futuras de la rúbrica v2.
4. Mantener requisitos `M-AR-*`, referencias en planes y tareas, con criterios objetivos.
5. Mantener toda tarea como `- [ ]`.
6. Comunicar diferencias, riesgos y decisiones por validar, sin resolverlas por suposición.

**Salida:** archivos Markdown documentales autorizados + tabla breve de trazabilidad y dudas. No ejecutar tareas, comandos, servicios externos ni modificar código.

**Uso operativo:** para implementar, elegir `specs/<id>` mediante `specs/README.md`; ninguna skill autoriza ejecutar tareas por sí sola. Al finalizar adjuntar evidencia real de pruebas y cambios.
