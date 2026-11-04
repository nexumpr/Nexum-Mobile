---
project: Nexum-Mobile
document: constitution
version: 1.0.0
status: vigente
ratified: 2026-10-29
last_updated: 2026-10-29
methodology: SDD
source_of_truth: .specify/memory/constitution.md
scope: mobile
---

# Constitución del proyecto — Nexum Mobile

> **Documento rector del cliente Android.** Esta es la fuente canónica de la arquitectura móvil, basada en Kotlin, Jetpack Compose, Material 3 y MVI. La constitución está **vigente** y establece las reglas para el trabajo por fases. Su vigencia **no acredita funcionalidades implementadas**.
>
> **Alcance operativo:** documentar y, cuando el responsable lo solicite para una unidad concreta, ejecutar tareas **de arquitectura base** con pruebas; no desarrollar funcionalidades financieras, acceso a datos reales, operaciones de nube, ML o IA sin especificaciones funcionales posteriores. Las capas técnicas de red y almacenamiento sí pueden prepararse dentro de sus unidades de arquitectura aprobadas.

## Protocolo de ejecución por especificación (vigente)

1. `specs/README.md` contiene el mapa de unidades independientes; `000-arquitectura-base` es una referencia transversal, no una orden de construir todo.
2. La orden «implementa `specs/<id>`» habilita **solo esa unidad** y sus pruebas. Antes de ejecutar, el agente verifica dependencias, decisiones pendientes y riesgos; detiene lo que dependa de aprobaciones de nube, identidad, costos o tratamiento de datos.
3. El agente presenta alcance y archivos previstos, realiza cambios mínimos, ejecuta las verificaciones pertinentes, registra resultados auténticos, y detiene su trabajo ante fallos que excedan el alcance. No declara tareas completas sin evidencia.
4. Se permite generar código, pruebas y configuración **únicamente** en las unidades autorizadas; no se permite implementar módulos financieros funcionales por inferencia.
5. Nunca se autoriza automáticamente `git push`, merge, destrucción de datos, despliegue público, secretos, facturación cloud o publicación en Google Play. Estas operaciones requieren aprobación específica.
6. Una regla de la rúbrica que todavía no puede cumplirse se documenta como **pendiente o riesgo**, nunca se simula su cumplimiento. Se conserva la prohibición de requerir Docker local.

## 1. Propósito y alcance

### 1.1 Objetivo y límites

Nexum es una aplicación Android para seguimiento de finanzas personales registradas por el usuario. Presenta datos, solicita operaciones al backend y muestra resultados confirmados o pendientes. **No es una entidad bancaria**, no realiza transferencias ni obtiene automáticamente saldos reales. El cliente nunca accederá directamente a MongoDB Atlas. Los importes disponibles son **estimaciones** derivadas de registros personales y deben mostrarse como tales.

La arquitectura móvil tiene responsabilidad sobre presentación, interacción, accesibilidad, conectividad, protección local y sincronización del estado visual. El servidor conserva autoridad sobre identidad, autorización, validación definitiva, integridad de datos y confirmación de operaciones.

### 1.2 Alcance documental y real

- Esta constitución gobierna únicamente `Nexum-Mobile`, su app Android, contratos con Backend, calidad, seguridad, pruebas y entrega futura.
- El proyecto base **ya existe** en Android Studio con `mx.nexum.app`, Kotlin y Compose; `./gradlew assembleDebug` terminó correctamente en la verificación inicial. Ello **no** prueba módulos de negocio, pruebas de seguridad, publicación ni integración.
- El equipo responsable del móvil aprueba sus decisiones y coordina contratos comunes con Backend.
- La descripción natural de Nexum sirve como contexto y límite; los flujos particulares se especificarán **solo cuando lo autorice el profesor**.

### 1.3 Lenguaje normativo

- **DEBE / NO DEBE:** exigencia constitucional vigente.
- **DEBERÍA:** recomendación fuerte cuya excepción exige justificativo registrado.
- **PUEDE:** posibilidad a evaluar, no elección automática.
- **Acordado, candidato, pendiente, implementado, verificado, aprobado** son estados diferentes; no se utilizan como sinónimos.
- Cuando una decisión documental y la rúbrica docente estén en tensión, se registra la discrepancia y se pide resolución humana antes de comprometer implementación.

## 2. Principios no negociables

**MO-PR01 — Spec-first.** No iniciar una funcionalidad, dependencia sustancial, pantalla de negocio, servicio externo ni cambio de contrato sin `spec.md`, `plan.md` y `task.md` aprobados para esa unidad. En la actividad actual toda tarea permanece pendiente.

**MO-PR02 — Arquitectura MVI.** La UI debe construirse sobre **Modelo–Vista–Intención**: flujo unidireccional, estado de presentación inmutable y representación reactiva a partir del estado. No sustituir MVI por MVVM u otro patrón como arquitectura principal sin enmienda explícita.

**MO-PR03 — Efectos controlados.** Las solicitudes y escrituras sensibles deben gestionarse en la lógica de presentación/aplicación, no dispararse por recomposición. `LaunchedEffect` se limita a efectos de Compose según claves y ciclo de vida; **no** garantiza una única ejecución global.

**MO-PR04 — Backend como fuente autoritativa.** Ninguna escritura local, caché u operación pendiente se presenta como confirmación del servidor. Android no debe omitir su autorización ni acceder directamente a Atlas.

**MO-PR05 — Veracidad del dinero.** Distinguir estimaciones, proyecciones y registros confirmados; no presentar datos simulados como reales ni afirmar que Nexum mueve dinero.

**MO-PR06 — Privacidad y seguridad desde diseño.** Minimizar datos y permisos, proteger tokens, prevenir exposición en logs/capturas, usar HTTPS/TLS y validar respuestas sin confiar en entradas remotas.

**MO-PR07 — Usabilidad inclusiva.** Usar Material 3 de forma coherente con accesibilidad, tamaños de toque apropiados, contraste, lectura por tecnologías asistivas y estados visibles sin depender solo del color.

**MO-PR08 — Manejo explícito de conectividad.** Distinguir estados sin conexión, vacío, carga, error, pendiente, confirmado y conflictivo; la sincronización sin duplicados depende de contratos con Backend.

**MO-PR09 — Cuidado con permisos.** Solicitar permisos peligrosos en el contexto de la función y justificar su necesidad. Si se rechazan micrófono o notificaciones, el flujo manual central debe permanecer utilizable cuando corresponda.

**MO-PR10 — Cero secretos.** No incluir contraseñas, tokens, llaves de firma, claves de API, `local.properties`, keystores ni información privada en Git, logs, prompts de IA o artefactos compartidos.

**MO-PR11 — Pruebas y evidencia.** Compilar no prueba seguridad ni funcionalidad. Toda afirmación «terminado» exige pruebas adecuadas y resultados reproducibles; los fallos u omisiones se documentan.

**MO-PR12 — Responsabilidades desacopladas.** Separar UI Compose, estado/intenciones, lógica de presentación, contratos de dominio, datos/red y persistencia local; ninguna capa de UI debe implementar autorización final ni gestionar MongoDB.

**MO-PR13 — Complejidad proporcional.** Las librerías de DI, HTTP, almacenamiento local, ML o IA deben estar justificadas y aprobadas; no introducir capas o frameworks por simple imitación de ejemplos docentes.

**MO-PR14 — Sin Docker local.** Android Studio, Gradle y el flujo de pruebas locales no dependerán de Docker ni de imágenes locales obligatorias.

**MO-PR15 — Seguridad de sesión responsable.** La rúbrica exige conservar la sesión salvo logout; ello se implementará posteriormente mediante renovación segura y restauración controlada **mientras el acceso siga autorizado**. Expiración irreversible, revocación, eliminación de cuenta o bloqueo de seguridad deben prevalecer; nunca mantener un JWT caducado como válido.

**MO-PR16 — Control humano de agentes.** Los agentes no hacen push, borrados, despliegues, publicación en tienda, cambios de permisos o instalaciones importantes sin autorización explícita; no se permiten modificaciones fuera del proyecto.

**MO-PR17 — Diseño fiel al prototipo aprobado.** La rúbrica exige respetar la maqueta y no eliminar funcionalidades aprobadas. Las ampliaciones requieren análisis de impacto y especificación, no cambios improvisados.

**MO-PR18 — Trazabilidad honesta.** Cada requisito debe relacionarse con un plan, tareas y evidencia. «Documentado», «implementado» y «publicado» no implican lo mismo.

## 3. Stack, plataforma y decisiones abiertas

### 3.1 Tecnologías aprobadas

| Área | Decisión | Implicación |
| --- | --- | --- |
| Plataforma | Android nativo | Herramientas y SDK de Android; compatibilidad objetivo por ratificar. |
| Lenguaje | Kotlin | Código Kotlin idiomático y compatible con Gradle. |
| IDE | Android Studio en Arch Linux | Proyecto base existente y reproducible. |
| UI | Jetpack Compose | Pantallas declarativas, estado observable y efectos seguros. |
| Diseño | Material 3 | Sistema visual compartido, accesible y testeable. |
| Arquitectura | MVI | Estado inmutable, eventos/intenciones y flujo de datos unidireccional. |
| Integración | API REST/HTTPS/JSON | Solo comunicación con el backend aprobado. |
| Identidad | Credenciales de sesión emitidas/verificadas conforme al contrato | Custodia y renovación seguras por definir. |
| Calidad | SDD + DevSecOps | Controles progresivos documentados y verificables. |

### 3.2 Selecciones que **no** se fijan arbitrariamente

- Versiones concretas de Kotlin, AGP, Gradle, Compose BOM y SDK mínimo/objetivo: conservar las generadas actualmente como **estado inicial**, documentar cambios y probar compatibilidad antes de modificar.
- Biblioteca de navegación, DI (p. ej., Hilt o Koin), red, JSON y almacenamiento local: selección bajo especificación aprobada; no convertir ejemplos en dependencias obligatorias.
- Persistencia local: evaluar SQLite/Room y cifrado cuando haya requisito de offline real, respetando el alcance y las capacidades del dispositivo.
- Proveedor push, proveedor ML, IA generativa, reconocimiento de voz y SDKs adicionales: decisión funcional posterior con privacidad, costo y alternativas.
- Estrategia de caché: **obligatoria para la entrega final en cliente**, pero su herramienta, TTL e invalidación siguen pendientes.

### 3.3 Requisitos docentes v2 para la entrega final (implementación progresiva)

El documento «Requerimientos para el desarrollo del proyecto Móvil v2» exige como resultado final aplicación evaluable mediante **Google Play**, fidelidad a la maqueta, mejores prácticas SDD y DevSecOps, CI/CD, pruebas unitarias/seguridad/integración, caché a nivel frontend, logout, versionado previo a despliegues, notificaciones push, aviso de privacidad enlazado con landing page, eliminación de cuenta, gestión correcta de permisos, al menos **una función de ML y una de IA generativa**, y manejo seguro de sesión. También pide respetar controles de moderación para contenido subido cuando aplique, y considerar SQLite/sincronización si el proyecto lo requiere.

- Son **requisitos de producto para etapas posteriores**, no especificaciones funcionales autorizadas ahora.
- La evaluación está señalada para el **2 de diciembre de 2026**. El plan temporal deberá ajustarse al trabajo real, no inventar tareas como ejecutadas. La rúbrica pide entrega en `.rar` y script de base de datos del proyecto integral; esto no autoriza crear una base de datos móvil o documentación funcional hoy.
- Se coordinará con Backend, nube, BD y landing page como componentes del proyecto integral. La analítica solicitada para la landing page deberá evaluarse con políticas de privacidad y consentimiento aplicables; no implica rastreo oculto desde Android.
- El instructor sugiere el despliegue desde GitHub a Google Play; es recomendación, no autorización de acceso o publicación.

## 4. Arquitectura y flujo MVI

### 4.1 Estructura lógica futura (no carpetas implementadas)

1. **UI Compose:** presenta estado, envía intenciones, expone semántica de accesibilidad; no interpreta JWT ni conecta a Atlas.
2. **Estado de presentación:** representa `Loading`, `Content`, `Empty`, `Error`, `Offline`, `Pending` y demás variantes según requisito; es inmutable.
3. **Procesador de intenciones / reductor:** transforma entradas y resultados en estados, coordina efectos a través de dependencias explícitas y evita condiciones de carrera.
4. **Aplicación/dominio:** invariantes de cliente, formatos visuales y coordinación de acciones; no suplanta reglas autoritativas del servidor.
5. **Datos/adaptadores:** integra HTTP/JSON, autenticación cliente, caché segura y persistencia local aprobada; transforma DTO a modelos internos sin filtrar detalles de red hacia la UI.
6. **Composición de dependencias:** crea contratos e implementaciones con DI o mecanismo equivalente aprobado; evita singletons ocultos con estado mutable sensible.

Esta separación no impone Clean Architecture completa ni múltiples módulos Gradle desde el inicio. La decisión de carpetas/módulos se documentará en el plan de arquitectura base.

### 4.2 Flujo y concurrencia

- Flujo: intención de UI → procesamiento/caso de uso → efecto de red o almacenamiento autorizado → resultado → nuevo estado inmutable → recomposición UI.
- La palabra **unidireccional** no limita a una sola petición simultánea. Debe definirse política para cancelación, tareas concurrentes, cargas repetidas y eventos de una sola vez.
- Los mensajes, navegación, diálogos y avisos efímeros se modelarán sin mezclarlos con estado duradero de forma que se repitan por rotación o recomposición.
- Manejar ciclo de vida, pérdida de red y recreación de actividades sin repetir escrituras confirmadas.

### 4.3 Patrones auxiliares

- **Observer** describe observación reactiva de estados; no exige implementar manualmente un observador cuando Compose/Flows ya satisfacen la necesidad.
- **Adapter** traduce contratos remotos y representaciones internas; protege límites de arquitectura.
- **Command** puede encapsular acciones y sus metadatos cuando aporte trazabilidad o control; no sustituye por sí solo a MVI.
- **Strategy** permite algoritmos alternativos cuando hay variantes reales.
- **Builder** se usa únicamente si la construcción de objetos complejos lo justifica; clases inmutables Kotlin y `copy` suelen resolver casos sencillos.
- Un patrón no es obligatorio en toda pantalla. Su adopción requiere objetivo y pruebas.

### 4.4 Cache y experiencia offline

- La rúbrica solicita caché en el cliente. Para el móvil, decidir qué datos pueden conservarse, tiempo de vigencia, límite de almacenamiento, cifrado, invalidación y limpieza tras logout/eliminación de cuenta.
- **No** mostrar un saldo cacheado como confirmado si el servidor informa una actualización incompatible. Indicar antigüedad y estado de actualización cuando corresponda.
- Las escrituras offline, si se autorizan, necesitan identificadores de operación y reglas acordadas con Backend sobre idempotencia, reconciliación, conflictos y expiración.
- Evitar mezclar datos de cuentas distintas al alternar sesión; el almacenamiento privado de una cuenta debe aislarse y limpiarse conforme a política.

## 5. Calidad de código, interacción y accesibilidad

### 5.1 Convenciones Kotlin/Compose

- Kotlin idiomático, nombres expresivos, funciones de responsabilidad acotada y estados modelados explícitamente.
- `@Composable` debe ser principalmente declarativo; no guardar movimientos ni iniciar procesos externos por efecto accidental de una recomposición.
- Preservar estado que corresponda según ciclo de vida, configuración y finalidad; no persistir tokens en mecanismos de estado de UI o texto legible.
- Proteger llamadas asíncronas frente a cancelación y resultados tardíos: no actualizar una pantalla de cuenta diferente con respuestas de una sesión anterior.
- Los componentes comunes deben conservar semántica y estilo; no recrear componentes de diseño por gusto si Material 3 resuelve el requisito.

### 5.2 Accesibilidad y validación

- Etiquetas legibles, escalado de fuente, navegación por lector de pantalla, áreas táctiles apropiadas y avisos de error asociables al campo relevante.
- Los indicadores financieros no se codifican solo por colores o animaciones; incluir texto y valores claros.
- Todas las pantallas con red deberán distinguir `cargando`, `vacío`, `error` y `sin conexión`, sin fingir éxito.
- Formularios: validación orientativa en cliente; validación definitiva en Backend. Ante errores de red, evitar doble toque, pero **no** asumir que deshabilitar un botón sustituye idempotencia del servidor.
- Respetar idiomas, fechas, formatos monetarios y accesibilidad regional; las reglas de redondeo y moneda las define contrato con backend.

## 6. Seguridad, privacidad y DevSecOps Android

### 6.1 Modelo de amenazas

- Amenazas: dispositivos perdidos, tokens filtrados, copias de seguridad inseguras, tráfico claro, almacenamiento accidental de datos, capturas y logs, manipulación de respuestas, suplantación de sesión, software malicioso y errores de sincronización.
- Tratar las entradas de UI, datos locales y respuestas remotas como no confiables; no confiar en validaciones solamente del cliente.
- No solicitar permisos de Android antes de necesitarlos. Micrófono, notificaciones y otros permisos deben explicarse en contexto cuando correspondan.

### 6.2 Sesión, autenticación y borrado

- Autenticación, renovación, logout y eliminación de cuenta deben acordarse con Backend antes de integrarse. Token vencido o revocado no se considerará válido.
- «Sesión siempre viva salvo logout» de la rúbrica significa **persistencia de experiencia mientras exista autorización válida**, no aceptar credenciales infinitas. La sesión se restaura y renueva de manera segura cuando sea posible; un usuario revocado debe perder acceso.
- Las credenciales no se almacenarán en `SharedPreferences` de texto claro, recursos, `BuildConfig` públicos ni repositorios. Seleccionar almacenamiento protegido y políticas de respaldo.
- Logout debe limpiar o invalidar credenciales en el cliente y coordinar la revocación/expiración del servidor; eliminación de cuenta debe implicar limpieza del dispositivo y tratamiento de datos según política.
- El modo demo permanece aislado, con información ficticia y sin acceso a operaciones de cuentas reales.

### 6.3 Transporte, contenidos y terceros

- Usar HTTPS/TLS y validación de certificados; prohibir tráfico HTTP claro en producción salvo excepción formal justificada por el entorno.
- `network_security_config.xml` y permisos de red se configurarán al implementar integraciones, con dominio y entornos reales; no fijar dominios inventados.
- Si se envían texto, imagen o video para almacenamiento, contemplar la evaluación de contenidos inapropiados conforme a rúbrica, proporcional al dato y con privacidad: no exponer movimientos financieros innecesariamente al proveedor de moderación.
- Voz/IA: consentimiento y transparencia sobre terceros; la voz reconocida propone texto editable, **no** confirma una operación financiera por sí sola.
- No incrustar claves de API privilegiadas dentro del APK. El backend intermedia integraciones que deban proteger secretos.

### 6.4 DevSecOps y cadena de suministro

| Control | Herramienta/estrategia candidata | Cuándo es exigible |
| --- | --- | --- |
| Build base | `./gradlew assembleDebug` | Desde proyecto inicial. |
| Unitarias | `./gradlew testDebugUnitTest` / JUnit | Al existir pruebas. |
| Lint | `./gradlew lintDebug` | Desde configuración aprobada. |
| Compose/UI | AndroidX Test, pruebas Compose y estados MVI | Al desarrollar capa/pantalla correspondiente. |
| Estilo | Detekt, si se aprueba | Después de evaluar mantenimiento/costo. |
| Secretos | Gitleaks o equivalente | Antes de publicar cambios. |
| Dependencias/SBOM | Auditoría Gradle + CycloneDX Gradle, según disponibilidad | Al consolidar dependencias y CI. |
| Permisos y privacidad | Revisión de manifiesto, flujo runtime, almacenamiento | Antes de implementar permisos/datos reales. |
| Integración | Pruebas sobre contratos REST/JSON y reintentos | Antes de conectar cliente con backend. |
| CI | GitHub Actions con mínimo privilegio | Tras SDD y aprobación del flujo. |
| Entrega | Firma y publicación Google Play en cuenta autorizada | Antes de evaluación final. |

- Nunca copiar directamente herramientas .NET/MSSQL, contraseñas o jobs Docker locales de proyectos docentes ajenos al stack Android.
- No inventar metas de cobertura, mutación o lints «en verde» sin ejecutar pruebas y registrar resultados.
- Fallo u omisión de pruebas: informar de forma verificable; las excepciones requieren riesgo, justificación, propietario y revisión.

## 7. Pruebas y criterios de aceptación

- **Unitarias:** transformación de intenciones a estados, reducciones deterministas, formateo y validación de cliente.
- **Compose/UI:** recomposición, estado, accesibilidad, mensajes, recuperación tras rotación y eventos efímeros.
- **Integración:** decodificación JSON, errores HTTP, autorización, límites de red, políticas de caché y sincronización, utilizando entornos de prueba y datos ficticios.
- **Seguridad:** tokens vencidos/revocados, cambios de cuenta, recuperación tras errores, privacidad local, permisos denegados y falta de conectividad.
- **Regresión:** la maqueta funcional aprobada no se reduce sin especificación/autorización; cada modificación tiene evidencia.
- Registrar objetivo, comando, entorno, resultado, cobertura real y limitaciones. Un test omitido se marca **no ejecutado**, nunca aprobado.

## 8. Versionado, CI/CD y publicación

- Definir matriz de versionado **antes de desplegar**: `versionName`, `versionCode`, compatibilidad de SDK, versión de contrato backend y changelog; la matriz todavía no existe.
- Repositorio de Nexum en cuenta organizadora `nexumpr`; Git local y entornos de otros proyectos se mantienen independientes.
- CI posterior: compilación, tests/lint, protección de secretos y controles acordados; no agregar workflows en la fase documental.
- Publicación a Google Play: firma de lanzamiento protegida, acceso mínimo a consola, revisión de permisos, aviso de privacidad y cumplimiento de políticas vigentes, con autorización humana. No declarar la app disponible en Google Play hasta probar el lanzamiento.
- Los despliegues, firmas y subida de bundles no pueden iniciarse por agentes sin instrucción explícita.

## 9. Observabilidad, resiliencia y recuperación

- Diagnóstico seguro de fallas del cliente: registros técnicos minimizados; no incluir tokens, credenciales, datos financieros identificables ni transcripciones privadas.
- Errores recuperables con orientación útil; no mostrar detalles internos de servicios.
- Definir límites de reintento/backoff y expiración; las operaciones de escritura requieren garantía acordada de idempotencia.
- Verificar conservación/limpieza de cachés y restauración de sesión; no simular éxito cuando la operación queda pendiente.
- La disponibilidad depende del backend y de la red; la app debe mantener comportamiento honesto sin conectividad.

## 10. Memoria, integración y decisiones pendientes

### 10.1 Registro de decisiones

`memoria.md` registra decisiones acordadas, propuestas, alternativas, responsables y estado; no duplica la constitución ni declara implementado el trabajo pendiente.

### 10.2 Contrato con Backend

- Compartir reglas de versión de API, JSON, errores, autenticación, renovación/logout, paginación, idempotencia, fechas y estados de confirmación.
- Mantener una sola fuente de verdad por contrato versionado; una modificación incompatible requiere revisión de los dos equipos.
- Coordinación de caché y sincronización: el cliente no debe mostrar información de otro usuario ni resultados obsoletos como confirmados.

### 10.3 Registro de decisiones abiertas

| ID | Decisión | Requiere aprobación |
| --- | --- | --- |
| M-D01 | API mínima y SDK target, Kotlin/AGP/Compose BOM | Equipo móvil. |
| M-D02 | Librerías de DI, red, JSON y navegación | Equipo móvil. |
| M-D03 | Persistencia local, cifrado, política offline y sincronización | Equipo móvil + Backend. |
| M-D04 | Proveedor de autenticación, custodia y renovación | Equipo móvil + Backend. |
| M-D05 | Formato de errores, idempotencia y contrato de versión | Equipo móvil + Backend. |
| M-D06 | Proveedor push y reglas de consentimiento | Equipo móvil. |
| M-D07 | ML e IA generativa, finalidad, privacidad, costo y evaluación | Equipo móvil + Backend. |
| M-D08 | Configuración firma y publicación Google Play, versionado | Responsable de release. |
| M-D09 | Reglas de contenido, avisos/landing y derechos de usuario | Equipo + revisor de privacidad. |
| M-D10 | Política y tecnología de caché cliente | Equipo móvil + Backend. |

## 11. Gobernanza Spec Driven Development

### 11.1 Precedencia

1. Constitución vigente **ratificada**, archivo canónico `.specify/memory/constitution.md`.
2. `spec.md` aprobado de una unidad y sus criterios verificables.
3. `plan.md` aprobado vinculado con la especificación.
4. `task.md` aprobado vinculado al plan.
5. `AGENTS.md`, subagentes, skills y plantillas: instrucciones de ejecución **subordinadas**.
6. `memoria.md`: registro, estado y decisiones, sin autoridad para derogación silenciosa.

Esta constitución está vigente. Cada implementación necesita autorización de la unidad concreta; los documentos no equivalen a pruebas realizadas.

### 11.2 Flujo obligatorio SDD

1. Revisar alcance, requisitos y constitución de la unidad seleccionada.
2. Revisar `spec.md`, `plan.md` y `task.md` de esa unidad y dependencias.
3. Solicitar aprobación explícita para **esa** implementación; sin permiso se trabaja solo documentalmente.
4. Ejecutar únicamente tareas autorizadas con pruebas reales y registrar evidencia.
5. Detenerse tras la unidad; una nueva unidad exige otra autorización.

### 11.3 Estructura documental elegida

```text
Nexum-Mobile/
├── app/                      # proyecto Android; modificar solo según unidad autorizada
├── gradle/                   # generado por Android Studio
├── constitution.md          # puntero, NO copia de reglas
├── AGENTS.md
├── memoria.md
├── .specify/
│   ├── memory/
│   │   └── constitution.md   # única constitución normativa
│   └── templates/
├── .opencode/
│   ├── agents/
│   └── skills/
└── specs/
    ├── README.md                 # índice, orden y dependencias
    ├── 000-arquitectura-base/     # referencia general
    └── 010-* ... 120-*/          # unidades por fase con spec/plan/task
```

### 11.4 Criterios de documentos

- `spec.md`: alcance y exclusiones, requisitos identificados, verificación, riesgos y decisiones pendientes.
- `plan.md`: propuesta estructural, trade-offs, trazabilidad, calidad, seguridad y dependencias, sin código.
- `task.md`: actividades pequeñas, IDs, correspondencia con requisitos y plan, criterios de aceptación y evidencias esperadas, todas pendientes.
- `AGENTS.md`: reglas de trabajo e información necesaria para los agentes, no reemplaza esta constitución.
- `memoria.md`: hechos comprobados, acuerdos y pendientes; nunca inventar decisiones.
- Agentes/skills: definidos bajo `.opencode` con permisos acotados y sin invocación durante la preparación de documentos.

### 11.5 Enmienda y ratificación

Cambiar una regla requiere identificar problema, texto anterior, nueva propuesta, impacto, riesgos, aprobación humana, versión y fecha. Reflejar efectos en `spec/plan/task` y contrato con Backend cuando aplique. No modificar documentos anteriores silenciosamente.

### 11.6 Controles de coherencia y seguimiento

- [ ] Constitución revisada y ratificada por responsables.
- [ ] `AGENTS.md`, `memoria.md`, subagentes y skills consistentes.
- [ ] `spec.md`, `plan.md` y `task.md` contienen **únicamente arquitectura base**.
- [ ] Trazabilidad de requisitos y todas las tareas sin marcar.
- [ ] Las decisiones abiertas permanecen visibles.
- [ ] No se alteró el proyecto Android, no se instalaron dependencias, no se realizaron despliegues ni push.

---

## Fuentes de referencia

- Revisión corregida de arquitectura Nexum, 29-10-2026 (propuesta; no infraestructura instalada).
- Descripción natural de Nexum Mobile: Kotlin, Compose, Material 3, MVI, accesibilidad y flujos de conectividad.
- «Requerimientos para el desarrollo del proyecto Móvil v2», septiembre–diciembre de 2026, pp. 1–4.
- Metodología docente observada en `MovilSDD`, `API_SDD`, `WebSDD` y `WebAPIDevSecOpsScallingSDD`. Sus patrones distintos de MVI no son obligatorios para Nexum.


> **Estado de adopción:** constitución vigente desde 2026-10-29 por instrucción del responsable del proyecto. Las alternativas pendientes siguen sin aprobación técnica; las casillas internas de verificación son controles de seguimiento, no cambian su vigencia.
