---
unit: 000-arquitectura-base
project: Nexum-Mobile
kind: architecture-foundation
version: 1.0.0
status: active-reference
approved_by: null
implementation_authorized: only_for_explicitly_selected_phase
---

# SPEC — Arquitectura base Nexum-Mobile

## Propósito
Definir los requisitos **técnicos y de calidad** de la base Android para Nexum, sin describir ni implementar pantallas de finanzas, navegación funcional, entidades de negocio o conexiones reales.

## Estado y límites
Proyecto base en Android Studio con Kotlin/Compose compilado (`assembleDebug`, evidencia inicial). La compilación no acredita funcionalidades. Fuente constitucional `.specify/memory/constitution.md`. **No ejecutar tareas.**

## Requisitos arquitectónicos

| ID | Requisito | Criterio verificable en etapa futura |
| --- | --- | --- |
| **M-AR-01** | Mantener aplicación Android nativa Kotlin, Jetpack Compose y Material 3. | Build reproducible compatible con versiones aprobadas; sin sustituir tecnologías. |
| **M-AR-02** | Arquitectura principal MVI con estado inmutable y flujo unidireccional. | Diseño de intenciones/reductor/estado y pruebas de transiciones sin mutación desde UI. |
| **M-AR-03** | Separar UI, presentación, datos/adaptadores y composición de dependencias. | Diagrama e interfaces técnicas; Compose sin acceso directo a servicios remotos. |
| **M-AR-04** | Controlar lifecycle, efectos y concurrencia; no repetir escrituras por recomposición. | Pruebas de estados, rotación, cancelaciones y efectos asociados a claves. |
| **M-AR-05** | Adoptar DI o mecanismo explícito de sustitución para pruebas. | Dependencias sustituibles sin instanciar implementaciones sensibles directamente en UI. |
| **M-AR-06** | Integrar con backend únicamente mediante REST/HTTPS/JSON con contrato de errores/versiones. | Contrato con Backend aprobado; respuestas interpretadas coherentemente. |
| **M-AR-07** | Gestionar autenticación, renovación, sesión persistente autorizada y logout seguro. | Casos de expiración/revocación/cambio de cuenta y logout verificables; sin JWT eterno. |
| **M-AR-08** | Aislar datos personales y credenciales en almacenamiento local seguro. | Sin secretos embebidos, respaldo y limpieza de datos según política. |
| **M-AR-09** | Definir caché móvil, caducidad, aislamiento, invalidación y estados offline honestos. | Se distinguen confirmado/pendiente/obsoleto y no hay mezcla de usuarios. |
| **M-AR-10** | Acordar idempotencia y sincronización de reintentos con el backend. | Duplicados evitados en pruebas de pérdida/reintentos de conectividad. |
| **M-AR-11** | Ajustarse al sistema de diseño Material 3 y accesibilidad del prototipo. | Semántica, contraste, lectura de pantalla, tamaños de toque y estados verificables. |
| **M-AR-12** | Gestionar permisos peligrosos solo en contexto, con alternativa sin permiso cuando sea posible. | Flujo revisado ante permiso concedido/denegado, manifiesto y privacidad. |
| **M-AR-13** | Proteger transporte TLS y establecer Network Security Configuration por entorno. | Sin tráfico claro de producción no aprobado; dominios reales configurados y validados. |
| **M-AR-14** | Preparar pruebas unitarias, Compose/UI, integración y seguridad adecuadas al cliente. | Matriz requisito→test y reportes reales, sin cobertura inventada. |
| **M-AR-15** | Planificar DevSecOps móvil: lint, análisis, secretos, dependencias/SBOM y CI. | Política de gates y flujos seleccionados y justificables antes de publicar. |
| **M-AR-16** | Establecer versionado Android y estrategia de firmado/Google Play. | Matriz `versionName/versionCode` y custodia de firma; publicación solo con permiso. |
| **M-AR-17** | Contemplar protección de datos, avisos, eliminación de cuenta y coordinación landing/backend. | Política de datos y contratos aprobados, sin implementar funcionalidades en esta unidad. |
| **M-AR-18** | Prever seguridad y validación de contenidos según su naturaleza, sin filtrar información personal a terceros. | Diseño de permisos, minimización y evaluación de terceros antes de integración. |
| **M-AR-19** | Reservar integración futura de notificaciones, ML e IA generativa según rúbrica, sin imponer SDKs ahora. | Decisiones de arquitectura modular, permisos y coste documentados sin funciones creadas. |
| **M-AR-20** | La documentación SDD debe conservar coherencia constitucional y trazabilidad. | `spec/plan/task` enlazados, ninguna tarea ejecutada automáticamente y aprobación humana pendiente. |

## Criterios transversales de aceptación documental
1. Solo arquitectura base, sin rutas específicas, modelos financieros ni pantallas o flujos funcionales.
2. Requisitos `M-AR-*` numerados y verificables, vinculados a plan y tareas futuras.
3. Privacidad, seguridad, contratos, caché, sesión y accesibilidad acordes con la constitución.
4. Diferenciación explícita entre requisitos docentes de entrega final y capacidades implementadas.
5. Todas las tareas `- [ ]`, sin tests, instalaciones ni despliegues.

## Riesgos y acuerdos pendientes
- SDK y versiones; elección de DI/red/JSON/navegación y mecanismo de almacenamiento seguro.
- Caché y sincronización/offline; diseño de idempotencia coordinado con Backend.
- Proveedor de identidad, JWT y logout; notificaciones, ML/IA, privacidad, contenido y Google Play.
- Fecha de evaluación y factibilidad de publicación; no convertir la fecha en evidencia de progreso.


## Estado en el SDD por fases (v1.0.0)
Este documento resume requisitos transversales y **no es una orden de implementación monolítica**. Las actividades técnicas se han dividido en `../010-*` a `../120-*`; seleccionar solo una unidad mediante `specs/README.md`. Las casillas anteriores no son evidencia de ejecución; siguen pendientes hasta verificarse por fase. No implementar funcionalidades específicas de Nexum en esta etapa.
