# Unidades SDD de arquitectura base — Nexum-Mobile

**Estado: VIGENTE, ejecutable de manera incremental bajo orden humana.** Constitución: [`.specify/memory/constitution.md`](../.specify/memory/constitution.md). No se implementa todo con un solo prompt.

## Convención
Cada unidad tiene `spec.md` (qué/aceptación), `plan.md` (cómo/orden/riesgos) y `task.md` (acciones, pruebas y evidencia). La unidad `000-arquitectura-base` conserva el marco transversal de la actividad universitaria y **no se ejecuta como un bloque**.

## Ruta sugerida (dependencias obligatorias, no calendario)
| Unidad | Resultado técnico esperado | Depende de |
| --- | --- | --- |
| [`010-base-android`](010-base-android/spec.md) | Base Android y compilación reproducible | Inicio |
| [`020-nucleo-mvi`](020-nucleo-mvi/spec.md) | Núcleo MVI y estado inmutable | `010-base-android` |
| [`030-di-navegacion-ui`](030-di-navegacion-ui/spec.md) | Inyección de dependencias y shell UI | `020-nucleo-mvi` |
| [`040-cliente-http-contratos`](040-cliente-http-contratos/spec.md) | Cliente HTTP y contratos de integración | `020-nucleo-mvi` |
| [`050-sesion-credenciales`](050-sesion-credenciales/spec.md) | Sesión y custodia de credenciales | `040-cliente-http-contratos` |
| [`060-cache-offline`](060-cache-offline/spec.md) | Caché local y sincronización controlada | `040-cliente-http-contratos`, `050-sesion-credenciales` |
| [`070-permisos-privacidad-accesibilidad`](070-permisos-privacidad-accesibilidad/spec.md) | Permisos, privacidad y accesibilidad base | `030-di-navegacion-ui` |
| [`080-pruebas-mvi-compose`](080-pruebas-mvi-compose/spec.md) | Pruebas de estado, UI e integración aislada | `020-nucleo-mvi`, `030-di-navegacion-ui` |
| [`090-devsecops-android`](090-devsecops-android/spec.md) | Seguridad estática y calidad Android | `010-base-android` |
| [`100-ci-versiones-android`](100-ci-versiones-android/spec.md) | CI Android y versión reproducible | `080-pruebas-mvi-compose`, `090-devsecops-android` |
| [`110-distribucion-google-play`](110-distribucion-google-play/spec.md) | Preparación para distribución segura | `100-ci-versiones-android` |
| [`120-contratos-end-to-end`](120-contratos-end-to-end/spec.md) | Integración técnica contractual con Go | `040-cliente-http-contratos`, `050-sesion-credenciales`, `060-cache-offline` |
| [`130-Registro-Usuario`](130-Registro-Usuario/spec.md) | Pantalla y flujo de registro (Sign Up) | `030-di-navegacion-ui`, `040-cliente-http-contratos` |
| [`140-Login`](140-Login/spec.md) | Pantalla y flujo de inicio de sesión | `030-di-navegacion-ui`, `040-cliente-http-contratos`, `050-sesion-credenciales` |
| [`150-Refresh-Token`](150-Refresh-Token/spec.md) | Renovación y rotación de tokens de sesión (NEXUM-E003) | `140-Login`, `040-cliente-http-contratos`, `050-sesion-credenciales` |
| [`151-github-actions`](151-github-actions.md) | Pipeline de release a Google Play con GitHub Actions | `100-ci-versiones-android`, `110-distribucion-google-play` |
| [`160-Logout`](160-Logout/spec.md) | Cierre de sesión y revocación en servidor (NEXUM-E004) | `140-Login`, `150-Refresh-Token`, `030-di-navegacion-ui`, `050-sesion-credenciales` |
| [`170-Recuperar-Contrasena`](170-Recuperar-Contrasena/spec.md) | Solicitud de recuperación de contraseña (NEXUM-E005) | `140-Login`, `030-di-navegacion-ui`, `040-cliente-http-contratos` |
| [`180-Restablecer-Contrasena`](180-Restablecer-Contrasena/spec.md) | Restablecimiento de contraseña con token (NEXUM-E006) | `170-Recuperar-Contrasena`, `140-Login`, `030-di-navegacion-ui`, `040-cliente-http-contratos` |
| [`190-Cambiar-Contrasena`](190-Cambiar-Contrasena/spec.md) | Cambio de contraseña autenticado con reautenticación (NEXUM-E007) | `140-Login`, `150-Refresh-Token`, `030-di-navegacion-ui`, `040-cliente-http-contratos` |
| [`200-Sesiones-Activas`](200-Sesiones-Activas/spec.md) | Consulta de sesiones y dispositivos activos (NEXUM-E008) | `140-Login`, `150-Refresh-Token`, `030-di-navegacion-ui`, `040-cliente-http-contratos` |
| [`210-Revocar-Sesion`](210-Revocar-Sesion/spec.md) | Revocación de sesión en dispositivo remoto (NEXUM-E009) | `200-Sesiones-Activas`, `140-Login`, `160-Logout`, `040-cliente-http-contratos` |
| [`220-Solicitud-Verificacion-Email`](220-Solicitud-Verificacion-Email/spec.md) | Solicitud y reenvío de verificación de correo (NEXUM-E010) | `130-Registro-Usuario`, `140-Login`, `040-cliente-http-contratos` |
| [`230-Confirmar-Verificacion-Email`](230-Confirmar-Verificacion-Email/spec.md) | Confirmación de verificación de correo con token (NEXUM-E011) | `220-Solicitud-Verificacion-Email`, `140-Login`, `040-cliente-http-contratos` |
| [`240-Perfil-Usuario`](240-Perfil-Usuario/spec.md) | Consulta de perfil del usuario autenticado (NEXUM-E012) | `140-Login`, `150-Refresh-Token`, `030-di-navegacion-ui`, `040-cliente-http-contratos` |
| [`250-Editar-Perfil`](250-Editar-Perfil/spec.md) | Edición de atributos de perfil del usuario (NEXUM-E013) | `240-Perfil-Usuario`, `140-Login`, `150-Refresh-Token`, `040-cliente-http-contratos` |
| [`260-Eliminar-Cuenta`](260-Eliminar-Cuenta/spec.md) | Eliminación definitiva de cuenta y datos del usuario (NEXUM-E014) | `240-Perfil-Usuario`, `140-Login`, `150-Refresh-Token`, `160-Logout`, `040-cliente-http-contratos`, `050-sesion-credenciales` |
| [`270-Aviso-Privacidad-Vigente`](270-Aviso-Privacidad-Vigente/spec.md) | Consulta del aviso de privacidad legalmente vigente (NEXUM-E015) | `130-Registro-Usuario`, `240-Perfil-Usuario`, `030-di-navegacion-ui`, `040-cliente-http-contratos`, `070-permisos-privacidad-accesibilidad` |
| [`280-Historial-Consentimientos`](280-Historial-Consentimientos/spec.md) | Consulta del historial de consentimientos del usuario (NEXUM-E016) | `270-Aviso-Privacidad-Vigente`, `240-Perfil-Usuario`, `140-Login`, `150-Refresh-Token`, `040-cliente-http-contratos` |
| [`290-Registrar-Consentimiento`](290-Registrar-Consentimiento/spec.md) | Registro de aceptación de nueva versión del aviso (NEXUM-E017) | `270-Aviso-Privacidad-Vigente`, `280-Historial-Consentimientos`, `140-Login`, `150-Refresh-Token`, `040-cliente-http-contratos` |
| [`300-Estado-Onboarding`](300-Estado-Onboarding/spec.md) | Consulta de estado y progreso de onboarding (NEXUM-E018) | `140-Login`, `150-Refresh-Token`, `030-di-navegacion-ui`, `040-cliente-http-contratos`, `070-permisos-privacidad-accesibilidad` |
| [`310-Completar-Onboarding`](310-Completar-Onboarding/spec.md) | Finalización de onboarding en modo completo o limitado (NEXUM-E019) | `300-Estado-Onboarding`, `140-Login`, `150-Refresh-Token`, `030-di-navegacion-ui`, `040-cliente-http-contratos`, `070-permisos-privacidad-accesibilidad` |
| [`320-Eliminar-Movimiento`](320-Eliminar-Movimiento/spec.md) | Eliminación y reversión atómica de movimiento financiero (NEXUM-E031) | `140-Login`, `150-Refresh-Token`, `030-di-navegacion-ui`, `040-cliente-http-contratos`, `050-sesion-credenciales` |
| [`330-Pagos-Recurrentes`](330-Pagos-Recurrentes/spec.md) | Gestión de pagos periódicos y equivalencia mensual (NEXUM-E032..E036) | `140-Login`, `150-Refresh-Token`, `030-di-navegacion-ui`, `040-cliente-http-contratos` |
| [`340-Registrar-Pago-Recurrente`](340-Registrar-Pago-Recurrente/spec.md) | Registro de pago recurrente realizado con check (✓) (NEXUM-E037) | `330-Pagos-Recurrentes`, `140-Login`, `040-cliente-http-contratos` |
| [`350-Metas-Ahorro`](350-Metas-Ahorro/spec.md) | Creación, listado y detalle de metas de ahorro (NEXUM-E038..E040) | `140-Login`, `150-Refresh-Token`, `030-di-navegacion-ui`, `040-cliente-http-contratos` |

## Regla de uso para OpenCode
1. Abrir OpenCode **dentro de este repositorio**, no desde la carpeta padre.
2. Solicitar una única unidad por identificador, no todas las fases. Usar @phase-implementer cuando esté reconocido o el agente principal con iguales restricciones.
3. Revisar si la unidad exige una decisión aún pendiente; no simular autenticación, costos, pruebas ni despliegues.
4. Probar, reportar y marcar únicamente tareas verificadas. Después detenerse y pedir aprobación para la siguiente unidad.
5. **No realizar push ni despliegue automáticamente.** Los requisitos de producto (ingresos, gastos, presupuestos, metas, voz, ML, etc.) llevarán specs funcionales independientes cuando se autoricen.

## Progreso
- `000-arquitectura-base`: marco SDD documental transversal de referencia (no ejecutable en bloque).
- `010-base-android`: **implementada y probada** el 2026-11-15 (evidencia en `memoria.md` §35 y en `specs/010-base-android/task.md`).
- `020-nucleo-mvi`: **implementada y probada** el 2026-11-15 (evidencia en `memoria.md` §36 y en `specs/020-nucleo-mvi/task.md`).
- `030-di-navegacion-ui`: **implementada y probada** el 2026-11-15 (evidencia en `memoria.md` §37 y en `specs/030-di-navegacion-ui/task.md`).
- `040-cliente-http-contratos`: **implementada y probada** el 2026-11-15 (evidencia en `memoria.md` §38 y en `specs/040-cliente-http-contratos/task.md`).
- `050-sesion-credenciales`: abstracciones base creadas (`CredentialStorage`, `SessionManager`, 3 tests unitarios).
- `060-cache-offline`: **implementada y probada** el 2026-11-17 (evidencia en `memoria.md` §39 y en `specs/060-cache-offline/task.md`).
- `070-permisos-privacidad-accesibilidad`: **implementada y probada** el 2026-11-17 (evidencia en `memoria.md` §40 y en `specs/070-permisos-privacidad-accesibilidad/task.md`).
- `080-pruebas-mvi-compose`: **implementada y probada** el 2026-11-17 (evidencia en `memoria.md` §41 y en `specs/080-pruebas-mvi-compose/task.md`).
- `090-devsecops-android`: **implementada y probada** el 2026-11-17 (evidencia en `memoria.md` §42 y en `specs/090-devsecops-android/task.md`).
- `100-ci-versiones-android`: **implementada y probada** el 2026-11-17 (evidencia en `memoria.md` §43 y en `specs/100-ci-versiones-android/task.md`).
- `110-distribucion-google-play`: **documentación verificada** el 2026-11-17 (evidencia en `memoria.md` §44 y en `specs/110-distribucion-google-play/task.md`); decisiones humanas de cuenta Play, firma, `applicationId`, R8 y workflow de deploy preexistente permanecen pendientes; sin publicación.
- `120-contratos-end-to-end`: **documentado y probado localmente** el 2026-11-17 (evidencia en `memoria.md` §45 y en `specs/120-contratos-end-to-end/task.md`); simulador HTTP en memoria y 16 tests sintéticos sin dependencias nuevas; aprobación de Backend Go y cambios productivos de reintentos/timeouts/DTOs siguen pendientes.
- `130-Registro-Usuario` y `140-Login`: **implementadas y probadas** el 2026-11-04 (evidencia en `memoria.md` §8 y en cada `task.md`); fuente mock sin Backend real.
- `150-Refresh-Token`: **implementada y probada** el 2026-11-19 (evidencia en `memoria.md` §46 y en `specs/150-Refresh-Token/task.md`); DTOs E003, rotación estricta, replay 409, familia revocada 401, 503 no destructivo, concurrencia single-flight real y `TokenAuthenticator` OkHttp con límite de 1 reintento.
- `160-Logout`: **implementada y probada** el 2026-11-19 (evidencia en `specs/160-Logout/task.md`); purga fail-secure en TokenStorage, contrato E004 sin body (204 No Content), aislamiento de Authenticator ante 401, diálogo Material 3 y emisión de `AuthEffect.NavigateToLogin`.
- `170-Recuperar-Contrasena`: **implementada y probada** el 2026-11-19 (evidencia en `memoria.md` §47 y en `specs/170-Recuperar-Contrasena/task.md`); contrato E005 (202 Accepted), respuesta neutral anti-enumeración, soporte 429 con `Retry-After` y cuenta regresiva de cooldown, validación previa en UI MVI y conexión con `AuthFlow`.
- `180-Restablecer-Contrasena`: **implementada y probada** el 2026-11-19 (evidencia en `memoria.md` §48 y en `specs/180-Restablecer-Contrasena/task.md`); contrato E006 (`POST /api/v1/auth/password/reset` 200 OK), token de un solo uso con 409 ante reuso, código inválido/expirado 400, reglas de contraseña 422, purga de sesión en TokenStorage y navegación a Login.
- `190-Cambiar-Contrasena`: **implementada y probada** el 2026-11-19 (evidencia en `memoria.md` §49 y en `specs/190-Cambiar-Contrasena/task.md`); contrato E007 (`POST /api/v1/auth/password/change` Bearer 200 OK), validación local previa (contraseña actual, nueva >= 12 chars y fuerte, distinta a la actual y coincidente), purga de contraseñas en memoria, invalidación de tokens y redirección obligatoria a Login.
- `200-Sesiones-Activas`: **implementada y probada** el 2026-11-20 (evidencia en `memoria.md` §50 y en `specs/200-Sesiones-Activas/task.md`); contrato E008 (`GET /api/v1/auth/sessions` Bearer 200 OK), DTOs estrictos de 5 campos sin datos de hardware/IP, paginación por cursor opaco (`8..2048` chars), ordenamiento de sesión actual al tope de la lista en página 1, MVI `SessionsViewModel`, pantalla Compose `ActiveSessionsScreen` con tema Aero-Futuristic, badge de sesión actual/revocada y botón "Cargar más sesiones", cableado en navegación desde Ajustes y 258 tests unitarios exitosos.
- `210-Revocar-Sesion`: **implementada y probada** el 2026-11-20 (evidencia en `memoria.md` §51 y en `specs/210-Revocar-Sesion/task.md`); contrato NEXUM-E009 (`DELETE /api/v1/auth/sessions/{sessionId}` Bearer 204 No Content sin body), validación UUID, respuesta 204 manejada con `Response<Unit>` sin deserializar JSON vacío, tratamiento diferenciado (si `current == true` purga `TokenStorage` y navega a Login sin llamar a E008; si `current == false` emite Snackbar y recarga E008), manejo de 404 y 409, simulación en `MockAuthApi` con idempotencia y centinelas, diálogo Material 3 `AlertDialog` en `ActiveSessionsScreen` y 275 tests unitarios exitosos.
- `220-Solicitud-Verificacion-Email`: **implementada y probada** el 2026-11-20 (evidencia en `memoria.md` §52 y en `specs/220-Solicitud-Verificacion-Email/task.md`); contrato NEXUM-E010, cooldown de 60 s, centinelas de cuota/503 y 305 tests unitarios en verde en ese momento.
- `230-Confirmar-Verificacion-Email`: **implementada y probada** el 2026-11-20 (evidencia en `memoria.md` §53 y en `specs/230-Confirmar-Verificacion-Email/task.md`); contrato NEXUM-E011 público con body `token`, `200` con `status`/`requestId`, `400`/`409` diferenciados, un solo uso en `MockAuthApi`, entrada manual desde E010, sin deep link y 329 tests unitarios en verde.
- `240-Perfil-Usuario` a `310-Completar-Onboarding`: **Fase A documental completada** (noviembre 2026); implementación técnica pendiente de mandato de Fase B.
- `320-Eliminar-Movimiento`: **Fase A documental completada** (2026-11-13); implementación técnica pendiente de mandato de Fase B.
- `330-Pagos-Recurrentes`: **Fase A documental completada** (2026-11-13); implementación técnica pendiente de mandato de Fase B.
- `340-Registrar-Pago-Recurrente`: **Fase A documental completada** (2026-11-13); implementación técnica pendiente de mandato de Fase B.
- `350-Metas-Ahorro`: **Fase A documental completada** (2026-11-13); implementación técnica pendiente de mandato de Fase B.
- Estado de tests: **329 unitarios en verde** (2026-11-20; 0 fallos, 0 errores, 0 omitidos); instrumentación **preparada (sin emulador activo)**; Android Lint **0 errores (SARIF/HTML generados)**; Pipeline CI **configurado con permisos mínimos sin secretos de firma**.
