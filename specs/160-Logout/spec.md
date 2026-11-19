---
project: Nexum-Mobile
unit: 160-Logout
kind: functional-spec
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# SPEC — Cierre de Sesión (Logout)

## Propósito
Cierre seguro de la sesión de usuario y revocación del token de actualización (`refreshToken`) bajo el contrato **NEXUM-E004** (`POST /api/v1/auth/logout`, catálogo V6.0.2, función de maqueta `[M]`), purgando incondicionalmente las credenciales locales en `TokenStorage`, limpiando el estado de presentación y redirigiendo al usuario a la pantalla de acceso con invalidación del historial de navegación.

## Lugar en el plan SDD
- **Constitución:** [`.specify/memory/constitution.md`](../../.specify/memory/constitution.md) (vigente: MO-PR04 Backend autoritativo, MO-PR06 Privacidad y seguridad desde el diseño, MO-PR10 Cero secretos, MO-PR15 Seguridad de sesión responsable: "Logout debe limpiar o invalidar credenciales en el cliente y coordinar la revocación/expiración del servidor").
- **Tecnología:** Kotlin / Jetpack Compose / MVI (Android nativo).
- **Contrato:** NEXUM-E004 del catálogo de endpoints de referencia V6.0.2 (`POST /api/v1/auth/logout` [M]).
- **Dependencias previas:** [`140-Login`](../140-Login/spec.md) (creación de sesión y `TokenStorage`), [`150-Refresh-Token`](../150-Refresh-Token/spec.md) (ciclo de tokens y revocación), [`030-di-navegacion-ui`](../030-di-navegacion-ui/spec.md) (limpieza de backstack) y [`050-sesion-credenciales`](../050-sesion-credenciales/spec.md) (almacén seguro de credenciales).
- **Naturaleza:** especificación funcional de autenticación y seguridad de sesión.

## Alcance permitido
Definición de DTOs del contrato E004 (`LogoutRequest`), ampliación de contratos en `AuthApi` y `AuthRepository`, implementación en `MockAuthApi` con revocación en servidor simulado y respuesta HTTP `204 No Content`, purga garantizada e incondicional de `TokenStorage`, componente composable de confirmación de salida en interfaz ("Perfil > Salir"), emisión del efecto `AuthEffect.NavigateToLogin`, y pruebas unitarias de la unidad.

## Exclusiones explícitas
No conectarse a un backend real en esta etapa; no alterar pantallas financieras ni lógica de eliminación definitiva de cuenta (unidad `DELETE /users/me`, NEXUM-E014); no persistir tokens tras la confirmación de logout; no omitir la purga local si la red falla; no publicar ni desplegar.

## Requisitos y criterios verificables
| ID | Requisito obligatorio | Aceptación mínima |
| --- | --- | --- |
| **M160-R01** | **Disparador y Confirmación en UI:** Acción visible de salida ("Perfil > Salir"). La pulsación debe presentar un diálogo de confirmación claro ("¿Cerrar sesión?") para prevenir cierres accidentales. | Composable de diálogo de confirmación probado con eventos de aceptación y cancelación. |
| **M160-R02** | **Contrato E004 (Sin Body y 204 No Content):** Al confirmar, enviar `POST /api/v1/auth/logout` con header `Authorization: Bearer <accessToken>` y sin body. Manejar respuesta exitosa `204 No Content` sin intentar parsear JSON (`Response<Unit>`). Prohibido reintentar refresh ante 401. | Contrato conforme a la guía de integración E004 (catálogo V6.0.2) y verificación de cabeceras y ausencia de body en pruebas. |
| **M160-R03** | **Purga Local Incondicional (Fail-Secure):** Sea que el servidor responda `204`, `401` (sesión ya revocada) o que ocurra una falla de red o timeout, el cliente **debe ejecutar obligatoriamente `TokenStorage.clear()`** en `finally` y limpiar el estado de sesión en memoria, retornando éxito para la aplicación. | Pruebas unitarias que verifiquen que `TokenStorage` queda vacío tanto en éxito como en 401 o fallo de red. |
| **M160-R04** | **Navegación Definitiva y Limpieza de Historial:** Tras el cierre de sesión, se emite el efecto `AuthEffect.NavigateToLogin` y se restablece el flujo a la pantalla de Login, impidiendo regresar a pantallas autenticadas con el botón "Atrás" de Android. | Prueba de emisión de efecto y verificación del reseteo de estado en `AuthFlow`. |
| **M160-R05** | **Simulación en `MockAuthApi`:** La fuente simulada debe registrar de forma durable el token de acceso y refresh como revocados (provocando 401 en llamadas posteriores), responder `204 No Content` tras retardo (300–800 ms), devolver `401` si ya estaba revocado, y simular error determinista con token centinela `net_error_logout`. | Pruebas de integración del mock con revocación efectiva del token y centinelas. |

## Escenarios de prueba y evidencia
- **Caso favorable:** Usuario confirma salida → llamada `POST /api/v1/auth/logout` → respuesta `204` → `TokenStorage.clear()` → redirección a Login sin datos en memoria.
- **Caso con fallo de red:** Usuario confirma salida sin conexión → llamada falla con error de transporte → el repositorio atrapa el error pero ejecuta `TokenStorage.clear()` de todas formas → redirección a Login con aviso informativo ("Sesión cerrada localmente").
- **Caso de cancelación:** Usuario abre diálogo de confirmación y presiona "Cancelar" → diálogo se cierra, sesión permanece intacta en `TokenStorage` y el usuario sigue en la pantalla actual.
- **Caso de token ya expirado (401 en logout):** El servidor devuelve 401 porque el token ya no existía → el cliente limpia credenciales locales y redirige a Login normalmente.
- **Regresión:** Compilación exitosa de `./gradlew assembleDebug` y aprobación continua de los 50 tests preexistentes de autenticación.
- **Comandos esperados (en Fase B):** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
- **Evidencia exigida:** ejecución real, reporte de cobertura de tests, trazabilidad a requisitos y registro en `memoria.md`.

## Riesgos y bloqueos
- **Falta de Backend real:** la revocación vive en el estado de `MockAuthApi`; su posterior integración con Go en `120-contratos-end-to-end` debe mantener la regla de purga incondicional.
- **Navegación con shell provisional:** la limpieza exhaustiva de backstack depende de la formalización de Compose Navigation en `030-di-navegacion-ui`; mientras tanto, se aísla mediante el estado reactivo `screenName` en `AuthFlow`.
- **Caché y datos locales residuales:** cuando se incorpore persistencia local con Room (`060-cache-offline`), el proceso de logout deberá coordinar también la limpieza de tablas privadas del usuario.

## Criterio de salida de la unidad
Todas las tareas en `task.md` documentadas en estado pendiente (`- [ ]`); trazabilidad completa requisito → plan → tarea; ninguna ejecución de código en Fase A; sin cambios fuera del alcance autorizado.
