---
project: Nexum-Mobile
unit: 130-Registro-Usuario
kind: functional-spec
version: 2.0.0
status: ready
implementation: only_on_explicit_request
---

# SPEC — Registro de Usuario (Sign Up)

## Propósito
Alta de cuenta con configuración financiera inicial bajo el contrato **NEXUM-E001** (`POST /api/v1/auth/register`, catálogo V6.0.2), con validación local de entrada y consentimiento versionado, sobre fuente simulada mientras no exista backend real.

## Lugar en el plan SDD
- **Constitución:** `.specify/memory/constitution.md` (vigente).
- **Tecnología:** Kotlin / Jetpack Compose / MVI.
- **Contrato:** NEXUM-E001 y ejemplo 4.1 del catálogo de endpoints (fixture de referencia).
- **Dependencias previas:** `030-di-navegacion-ui`, `040-cliente-http-contratos` (aún no implementadas). Esta unidad puede documentarse y avanzar en su alcance mock sin consumirlas.
- **Naturaleza:** especificación funcional de autenticación; los campos financieros son **datos de alta**, no funcionalidad financiera.

## Alcance permitido
UI de registro, validación local, DTOs del contrato E001, repositorio con fuente mock y retardo de red simulado, estados MVI de la pantalla y pruebas de esta unidad. Únicamente eso.

## Exclusiones explícitas
No calcular disponibles ni ejecutar lógica financiera con `income`/`recurringExpenses`; no usar backend real; no implementar inicio de sesión (unidad `140`); no consumir datos reales; no publicar ni desplegar.

## Requisitos y criterios verificables
| ID | Requisito obligatorio | Aceptación mínima |
| --- | --- | --- |
| **M130-R01** | Pantalla con nombre (`name`), email, contraseña y confirmación con botón de visibilidad, y checkbox de Términos. Validación local: email `^[A-Za-z0-9+_.-]+@(.+)$`, contraseña de mínimo 8 caracteres alfanuméricos, coincidencia estricta contraseña/confirmación, envío bloqueado sin checkbox marcado. | Pruebas unitarias de validación y revisión objetiva de los estados de error mostrados en la UI. |
| **M130-R02** | Enviar exactamente el payload E001: `name`, `email`, `password`, `income {amount, frequency}`, `recurringExpenses[]`, `privacyNoticeVersion`, `consentAcceptedAt`. Manejar `201` con `userId`, `onboardingStatus` y tokens o indicación de verificación; conflicto de correo → mensaje "El correo ya está registrado". | Prueba de DTOs contra el fixture 4.1 y prueba del mapeo de conflicto a mensaje. |
| **M130-R03** | El alta exige `income` con monto válido y **al menos un gasto recurrente**; `frequency` acepta únicamente `monthly` hasta catálogo confirmado con Backend (decisión M-D08). | Pruebas de validación de los campos financieros del alta. |
| **M130-R04** | Consentimiento versionado: `privacyNoticeVersion` fija (`2026-11-v1`, pendiente de ratificación por privacidad) y `consentAcceptedAt` en ISO-8601 UTC generado al aceptar el checkbox. | Prueba de generación del par de campos de consentimiento. |
| **M130-R05** | Sin backend real: repositorio con fuente simulada y retardo de red; los errores (conflicto, fallo de red) se traducen a estados y avisos de UI sin exponer detalles técnicos. | Prueba del ViewModel con repositorio falso cubriendo éxito y error. |

## Escenarios de prueba y evidencia
- **Caso favorable:** alta válida → `201` → efecto de navegación hacia Login.
- **Caso de fallo:** correo duplicado → alerta de conflicto; checkbox sin marcar → botón deshabilitado; contraseña distinta de la confirmación → error en campo; fallo de red → Snackbar con reintento.
- **Regresión:** el proyecto preexistente sigue compilando y no se añaden pantallas ni cálculos financieros.
- **Comandos/medios de comprobación esperados:** `./gradlew assembleDebug`; `./gradlew testDebugUnitTest`; `./gradlew lintDebug`.
- **Evidencia exigida:** salida real de comandos, tests añadidos, diff revisado y limitaciones; nunca marcar como aprobado por intuición.

## Riesgos / bloqueos
- Backend inexistente: el contrato vive en la fuente mock; su sustitución futura debe respetar E001.
- Catálogo incompleto: valores de `frequency` y `type`, y la forma exacta de tokens o verificación en el `201`, están pendientes de confirmación con Backend (M-D08).
- Compilación previa exitosa no sustituye una nueva verificación posterior a cambios.

## Criterio de salida de la unidad
Todas las tareas de `task.md` tienen resultado o quedan explícitamente pendientes; pruebas ejecutadas cuando el entorno lo permite; sin cambios fuera del alcance; revisión humana del resultado antes de iniciar la siguiente unidad.
