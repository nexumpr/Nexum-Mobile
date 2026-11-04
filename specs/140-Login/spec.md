---
project: Nexum-Mobile
unit: 140-Login
kind: functional-spec
version: 2.0.0
status: ready
implementation: only_on_explicit_request
---

# SPEC — Inicio de Sesión (Login)

## Propósito
Autenticar usuarios existentes bajo el contrato **NEXUM-E002** (`POST /api/v1/auth/login`, catálogo V6.0.2), custodiar los tokens de sesión y navegar hacia el núcleo de la aplicación, sobre fuente simulada mientras no exista backend real.

## Lugar en el plan SDD
- **Constitución:** `.specify/memory/constitution.md` (vigente).
- **Tecnología:** Kotlin / Jetpack Compose / MVI.
- **Contrato:** NEXUM-E002 del catálogo de endpoints.
- **Dependencias previas:** `030-di-navegacion-ui`, `040-cliente-http-contratos` (aún no implementadas). El contrato compartido del repositorio (`AuthRepository`, `AuthResult`, `AuthEffect`) está definido en `specs/130-Registro-Usuario/plan.md`.
- **Naturaleza:** especificación funcional de autenticación y custodia de sesión.

## Alcance permitido
UI de login, validación local previa al request, DTOs del contrato E002, fuente simulada con límite de intentos, almacenamiento de token, estados MVI y pruebas de esta unidad. Únicamente eso.

## Exclusiones explícitas
No usar backend real; no implementar registro (unidad `130`); no implementar recuperación de contraseña (sin endpoint en el catálogo provisto: el enlace se muestra y su flujo queda pendiente de spec propia); no interpretar JWT en la UI; no iniciar sesión automática como invitado; no publicar ni desplegar.

## Requisitos y criterios verificables
| ID | Requisito obligatorio | Aceptación mínima |
| --- | --- | --- |
| **M140-R01** | Pantalla con email, contraseña, botón "Iniciar Sesión" y enlaces a "Olvidé mi contraseña" y "Registrarse". Validación local de campos no vacíos y formato de email **antes** del request HTTP. | Pruebas unitarias de validación y revisión objetiva de la UI. |
| **M140-R02** | Payload E002: `email`, `password`, `deviceName` opcional. Manejar `200` con `accessToken`, `refreshToken`, `expiresIn` y `user`, persistiendo la sesión mediante `TokenStorage`. | Prueba de DTOs y prueba de guardado de la sesión. |
| **M140-R03** | Credenciales incorrectas → `401` con mensaje genérico "Correo o contraseña incorrectos"; límite de 5 intentos → aviso de límite alcanzado; jamás sesión automática de invitado. | Pruebas de los tres caminos de error y del éxito. |
| **M140-R04** | El token se guarda solo tras `200`. Implementación actual: `TokenStorage` en memoria; **`EncryptedSharedPreferences` es obligatorio al integrar en la unidad `050-sesion-credenciales`**. | Prueba de la interfaz de almacenamiento y revisión del punto de sustitución. |
| **M140-R05** | Tras el inicio de sesión exitoso se emite un efecto de navegación que limpia el historial (shell temporal en esta unidad; backstack real en la unidad `030`). | Prueba de emisión del efecto sin repetición por recomposición. |

## Escenarios de prueba y evidencia
- **Caso favorable:** credenciales válidas → `200` → sesión guardada → efecto hacia el núcleo de la app.
- **Caso de fallo:** credenciales inválidas → mensaje genérico; 5 fallos consecutivos → límite de intentos; correo `net@error.test` → error de red con Snackbar de reintento.
- **Regresión:** el proyecto preexistente sigue compilando y no se añaden pantallas financieras ni datos reales.
- **Comandos/medios de comprobación esperados:** `./gradlew assembleDebug`; `./gradlew testDebugUnitTest`; `./gradlew lintDebug`.
- **Evidencia exigida:** salida real de comandos, tests añadidos, diff revisado y limitaciones; nunca marcar como aprobado por intuición.

## Riesgos / bloqueos
- Backend inexistente: el contrato vive en la fuente mock; su sustitución futura debe respetar E002.
- Campos exactos del objeto `user` sin confirmar con Backend; se modelan `id`, `name`, `email` como supuesto.
- La forma real de `EncryptedSharedPreferences` y la renovación de tokens dependen de la unidad `050` (M-P04).
- Compilación previa exitosa no sustituye una nueva verificación posterior a cambios.

## Criterio de salida de la unidad
Todas las tareas de `task.md` tienen resultado o quedan explícitamente pendientes; pruebas ejecutadas cuando el entorno lo permite; sin cambios fuera del alcance; revisión humana del resultado antes de iniciar la siguiente unidad.
