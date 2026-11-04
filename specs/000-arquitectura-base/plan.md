---
unit: 000-arquitectura-base
project: Nexum-Mobile
kind: architecture-plan
version: 1.0.0
status: active-reference
implementation_authorized: only_for_explicitly_selected_phase
---

# PLAN — Arquitectura base Nexum-Mobile

> **Solo planificación documental para trabajo posterior. No ejecutar.** Basado en constitución canónica y `spec.md` de arquitectura base.

## Punto de partida
Android Studio generó proyecto Kotlin/Compose/Material 3 con `mx.nexum.app`; `./gradlew assembleDebug` terminó correctamente. No se presupone MVI implementado, red funcional, persistencia local, pruebas UI ni despliegue Play.

## Diseño lógico propuesto
- **UI:** composables Material 3 sin efectos de negocio, renderizando estado.
- **Presentación/MVI:** intenciones, procesadores/reductor, estados inmutables, coordinación de efectos y eventos efímeros.
- **Aplicación/datos:** contratos para servicios/red/caché y adaptadores DTO internos sin invadir UI.
- **Composición:** DI seleccionada por costo de mantenimiento y testabilidad, sin herramientas impuestas por ejemplos del profesor.
- **Almacenamiento local:** elegir estrategias según riesgo, privacidad, consistencia y necesidad offline real.

## Etapas futuras y trazabilidad
| Etapa | Diseño documental requerido | Referencias `spec.md` | Evidencia futura |
| --- | --- | --- | --- |
| **M-P01** Base Android | Ratificar versiones Kotlin, AGP, Gradle, SDK y Material 3. | M-AR-01 | Matriz de compatibilidad y build reproducible. |
| **M-P02** Capas y MVI | Definir límites de UI, presentación, estado, intenciones y DI. | M-AR-02,03,05 | Diagramas y contratos sustituidos en pruebas. |
| **M-P03** Estado y efectos | Diseñar lifecycle, concurrencia, eventos únicos y seguridad frente a recomposición. | M-AR-04 | Casos futuros de rotación/reintento/cancelación. |
| **M-P04** Integración | Acordar REST/HTTPS/JSON, errores/versiones, sesión/logout con Backend. | M-AR-06,07,13 | Contrato técnico compartido y revisión de transporte. |
| **M-P05** Datos/caché | Seleccionar almacenamiento seguro, caché offline, invalidación, idempotencia. | M-AR-08,09,10 | Política y pruebas de no mezclar usuarios ni duplicar. |
| **M-P06** UX segura | Definir accesibilidad Material 3, permisos, privacidad/contenido y normas de maqueta. | M-AR-11,12,17,18 | Lista de controles verificables sin pantallas funcionales. |
| **M-P07** Calidad | Definir pruebas, lint, controles DevSecOps, secretos y SBOM. | M-AR-14,15 | Matriz de calidad por fase y criterios de aceptación. |
| **M-P08** Entrega | Establecer versiones, firma, publicación y previsión de push/ML/IA como integraciones futuras. | M-AR-16,19 | Plan de release y acuerdos pendientes sin subir artefactos. |
| **M-P09** Gobierno SDD | Revisar decisiones y traza de toda la unidad. | M-AR-20 | Auditoría de spec/plan/task y aprobación humana. |

## Decisiones y trade-offs
- **MVI vs ejemplo docente MVVM:** MVI se mantiene; extraer del profesor solo disciplina SDD y de pruebas.
- **Offline:** no imponer SQLite/Room antes de que el requisito de sincronización lo justifique; sí cumplir caché cliente de rúbrica.
- **Persistencia sesión:** restaurar mientras sea autorizada; expiración/revocación y borrado siempre prevalecen.
- **Proveedores externos:** elegirlos tras analizar permisos, coste, privacidad y dependencia; no codificar Vertex AI/voz/push ahora.
- **CI y Play:** son metas de entrega final; no crear pipelines ni subir APK/AAB en etapa documental.

## Dependencias
M-P01 precede a fijar librerías. M-P02 y M-P03 se definen antes de pantallas reales. M-P04 exige contrato con Backend. M-P05 necesita política de sesión y datos. M-P07 define evidencia para M-P08; todas las etapas requieren revisión M-P09.

## No objetivos en esta fase
No implementar MVI, no instalar librerías, no tocar Gradle, no escribir pantallas, no pedir permisos al móvil, no iniciar servicios, no configurar Google Play. Solo preparar documentación de arquitectura base.

## Matriz de trazabilidad exhaustiva (IDs explícitos)

| Requisito SPEC | Paso PLAN | Tareas futuras |
| --- | --- | --- |
| **M-AR-01** | M-P01 | M-T01 |
| **M-AR-02** | M-P02 | M-T02, M-T03 |
| **M-AR-03** | M-P02 | M-T02, M-T03 |
| **M-AR-05** | M-P02 | M-T02, M-T03 |
| **M-AR-04** | M-P03 | M-T04 |
| **M-AR-06** | M-P04 | M-T05 |
| **M-AR-07** | M-P04 | M-T05 |
| **M-AR-13** | M-P04 | M-T05 |
| **M-AR-08** | M-P05 | M-T06, M-T07 |
| **M-AR-09** | M-P05 | M-T06, M-T07 |
| **M-AR-10** | M-P05 | M-T06, M-T07 |
| **M-AR-11** | M-P06 | M-T08 |
| **M-AR-12** | M-P06 | M-T08 |
| **M-AR-17** | M-P06 | M-T08 |
| **M-AR-18** | M-P06 | M-T08 |
| **M-AR-14** | M-P07 | M-T09 |
| **M-AR-15** | M-P07 | M-T09 |
| **M-AR-16** | M-P08 | M-T10 |
| **M-AR-19** | M-P08 | M-T10 |
| **M-AR-20** | M-P09 | M-T11 |


## Estado en el SDD por fases (v1.0.0)
Este documento resume requisitos transversales y **no es una orden de implementación monolítica**. Las actividades técnicas se han dividido en `../010-*` a `../120-*`; seleccionar solo una unidad mediante `specs/README.md`. Las casillas anteriores no son evidencia de ejecución; siguen pendientes hasta verificarse por fase. No implementar funcionalidades específicas de Nexum en esta etapa.
