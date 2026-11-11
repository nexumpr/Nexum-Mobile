---
project: Nexum-Mobile
unit: 250-Editar-Perfil
kind: technical-spec
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# SPEC — Edición de Perfil de Usuario (Update Profile)

## Propósito
Permitir a un usuario autenticado actualizar sus atributos personales editables (nombre de perfil) bajo el contrato **NEXUM-E013** (`PATCH /api/v1/users/me`, soporte y consistencia `[S]`, catálogo V6.0.2), con validación local previa, actualización reactiva del estado de sesión en el dispositivo y sincronización sin recargar la cuenta.

## Lugar en el plan SDD
- **Constitución:** [`.specify/memory/constitution.md`](../../.specify/memory/constitution.md) (vigente: MO-PR04 Backend como fuente autoritativa, MO-PR06 Privacidad y seguridad desde el diseño: "no permitir inyección de identificadores ni cambios de rol no autorizados", MO-PR07 Usabilidad inclusiva Material 3, MO-PR08 Manejo explícito de conectividad).
- **Tecnología:** Kotlin / Jetpack Compose / MVI (Android nativo).
- **Contrato:** NEXUM-E013 del catálogo de endpoints de referencia V6.0.2 (`PATCH /api/v1/users/me` [S]).
- **Dependencias previas:** [`240-Perfil-Usuario`](../240-Perfil-Usuario/spec.md) (provee la visualización de perfil y punto de entrada a la edición), [`140-Login`](../140-Login/spec.md) (sesión activa requerida), [`150-Refresh-Token`](../150-Refresh-Token/spec.md) (renovación de token si expira durante la edición) y [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md) (cliente HTTP).
- **Naturaleza:** especificación técnica de mutación de perfil y consistencia (`[S]`).

## Alcance permitido
Pantalla/diálogo modal `EditProfileDialog` o `EditProfileScreen` en Compose accesible desde `ProfileScreen`, campo de texto para nombre (`name`), validación local previa (longitud de 2 a 50 caracteres, no vacío tras trim, detección de cambios frente al valor original), DTOs del contrato E013 (`UpdateProfileRequest`), ampliación de `AuthApi` y `AuthRepository`, implementación simulada en `MockAuthApi` con persistencia en memoria y respuesta `200 OK` con el perfil actualizado, sincronización del objeto `Session.user` en `TokenStorage`, y pruebas unitarias de la unidad.

## Exclusiones explícitas
No permitir mutación del identificador `id` ni de la fecha de creación `createdAt`; no permitir cambio de correo en este formulario (requiere flujo de verificación separado según regla del catálogo); no permitir inyección de roles o permisos; no conectar con backend real en esta etapa; no publicar ni desplegar.

## Requisitos y criterios verificables
| ID | Requisito obligatorio | Aceptación mínima |
| --- | --- | --- |
| **M250-R01** | **Formulario de Edición y Detección de Cambios:** Formulario accesible con campo de texto para nombre (`name`), botón "Guardar cambios" y botón "Cancelar". El botón de guardar debe permanecer deshabilitado si el valor no ha cambiado respecto al perfil actual o si la validación falla. | Composable probado con control reactivo de habilitación de botón y eventos de interacción. |
| **M250-R02** | **Validación Local de Nombre:** El nombre ingresado debe contener entre 2 y 50 caracteres no vacíos (`name.trim().length in 2..50`). Si incumple, se muestra error contextual en el campo de texto antes de disparar la petición HTTP. | Pruebas unitarias de validación local cubriendo nombres vacíos, demasiado cortos (>1) o excesivos (<51). |
| **M250-R03** | **Contrato y Payload E013:** Enviar `PATCH /api/v1/users/me` con cabecera `Authorization: Bearer <accessToken>` y payload `{"name": "..."}`. Recibir respuesta `200 OK` con el perfil actualizado (`UserProfileResponse`). | Modelado de DTOs acorde al catálogo V6.0.2 y verificación de serialización/deserialización en pruebas. |
| **M250-R04** | **Sincronización Atómica en `TokenStorage` (MO-PR04, MO-PR15):** Tras recibir `200 OK`, el cliente debe actualizar atómicamente el campo `name` en el objeto `Session.user` almacenado localmente en `TokenStorage`, propagando el nuevo nombre de inmediato a la pantalla de Perfil y a cualquier vista dependiente sin forzar re-login. | Pruebas unitarias que verifiquen que `TokenStorage.current()?.user?.name` refleja el nuevo valor tras el guardado. |
| **M250-R05** | **Simulación en `MockAuthApi` y Resiliencia de Red:** El mock debe actualizar el nombre del usuario en memoria, responder con retardo (400–800 ms), responder error ante `net@error.test` y preservar los datos ingresados en el formulario ante fallos de red para permitir reintento sin pérdida de información. | Pruebas unitarias en `MockAuthApiTest` y `EditProfileViewModelTest` cubriendo éxito y error de red. |

## Escenarios de prueba y evidencia
- **Caso favorable:** Usuario modifica su nombre de "Alex" a "Alex Morales" y presiona "Guardar cambios" → llamada `PATCH /api/v1/users/me` → respuesta `200` con nuevo nombre → `TokenStorage` actualizado → diálogo se cierra, `ProfileScreen` muestra "Alex Morales" y se despliega Snackbar de éxito.
- **Caso sin cambios:** Usuario abre edición y mantiene el mismo texto → botón "Guardar cambios" permanece deshabilitado; presionar "Cancelar" cierra el formulario sin llamadas de red.
- **Caso de validación fallida:** Usuario ingresa un solo carácter o espacios en blanco → error en campo "El nombre debe tener al menos 2 caracteres" y botón deshabilitado.
- **Caso de fallo de transporte:** Error de red al guardar → Snackbar con opción "Reintentar", conservando el texto modificado en el campo.
- **Regresión:** Compilación exitosa de `./gradlew assembleDebug` y aprobación continua de los 50 tests preexistentes de autenticación.
- **Comandos esperados (en Fase B):** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
- **Evidencia exigida:** ejecución real, reporte de cobertura de tests, trazabilidad a requisitos y registro en `memoria.md`.

## Riesgos y bloqueos
- **Falta de Backend real:** la actualización atómica en MongoDB Atlas (`users.name`) es responsabilidad del backend en Go; en el cliente móvil se garantiza la validez de la entrada y la sincronización visual inmediata.

## Criterio de salida de la unidad
Todas las tareas en `task.md` documentadas en estado pendiente (`- [ ]`); trazabilidad completa requisito → plan → tarea; ninguna ejecución de código en Fase A; sin cambios fuera del alcance autorizado.
