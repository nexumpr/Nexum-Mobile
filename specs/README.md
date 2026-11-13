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
| [`230-Confirmar-Verificacion-Email`](230-Confirmar-Verificacion-Email/spec.md) | Confirmación de verificación de correo con token (NEXUM-E011) | `220-Solicitud-Verificacion-Email`, `140-Login`, `030-di-navegacion-ui`, `040-cliente-http-contratos` |
| [`240-Perfil-Usuario`](240-Perfil-Usuario/spec.md) | Consulta de perfil del usuario autenticado (NEXUM-E012) | `140-Login`, `150-Refresh-Token`, `030-di-navegacion-ui`, `040-cliente-http-contratos` |
| [`250-Editar-Perfil`](250-Editar-Perfil/spec.md) | Edición de atributos de perfil del usuario (NEXUM-E013) | `240-Perfil-Usuario`, `140-Login`, `150-Refresh-Token`, `040-cliente-http-contratos` |
| [`260-Eliminar-Cuenta`](260-Eliminar-Cuenta/spec.md) | Eliminación definitiva de cuenta y datos del usuario (NEXUM-E014) | `240-Perfil-Usuario`, `140-Login`, `150-Refresh-Token`, `160-Logout`, `040-cliente-http-contratos`, `050-sesion-credenciales` |
| [`270-Aviso-Privacidad-Vigente`](270-Aviso-Privacidad-Vigente/spec.md) | Consulta del aviso de privacidad legalmente vigente (NEXUM-E015) | `130-Registro-Usuario`, `240-Perfil-Usuario`, `030-di-navegacion-ui`, `040-cliente-http-contratos`, `070-permisos-privacidad-accesibilidad` |
| [`280-Historial-Consentimientos`](280-Historial-Consentimientos/spec.md) | Consulta del historial de consentimientos del usuario (NEXUM-E016) | `270-Aviso-Privacidad-Vigente`, `240-Perfil-Usuario`, `140-Login`, `150-Refresh-Token`, `040-cliente-http-contratos` |
| [`290-Registrar-Consentimiento`](290-Registrar-Consentimiento/spec.md) | Registro de aceptación de nueva versión del aviso (NEXUM-E017) | `270-Aviso-Privacidad-Vigente`, `280-Historial-Consentimientos`, `140-Login`, `150-Refresh-Token`, `040-cliente-http-contratos` |
| [`300-Estado-Onboarding`](300-Estado-Onboarding/spec.md) | Consulta de estado y progreso de onboarding (NEXUM-E018) | `140-Login`, `150-Refresh-Token`, `030-di-navegacion-ui`, `040-cliente-http-contratos`, `070-permisos-privacidad-accesibilidad` |
| [`310-Completar-Onboarding`](310-Completar-Onboarding/spec.md) | Finalización de onboarding en modo completo o limitado (NEXUM-E019) | `300-Estado-Onboarding`, `140-Login`, `150-Refresh-Token`, `030-di-navegacion-ui`, `040-cliente-http-contratos`, `070-permisos-privacidad-accesibilidad` |
| [`320-Exportar-Datos-Usuario`](320-Exportar-Datos-Usuario/spec.md) | Exportación de datos personales del titular en JSON o CSV (NEXUM-E020) | `240-Perfil-Usuario`, `270-Aviso-Privacidad-Vigente`, `140-Login`, `150-Refresh-Token`, `030-di-navegacion-ui`, `040-cliente-http-contratos`, `070-permisos-privacidad-accesibilidad` |
| [`330-Bootstrap-Inicial`](330-Bootstrap-Inicial/spec.md) | Instantánea agregada de perfil, resumen, racha y conteos al entrar (NEXUM-E021) | `240-Perfil-Usuario`, `300-Estado-Onboarding`, `140-Login`, `150-Refresh-Token`, `030-di-navegacion-ui`, `040-cliente-http-contratos` |
| [`340-Catalogo-Categorias`](340-Catalogo-Categorias/spec.md) | Consulta del catálogo de categorías con identificadores estables (NEXUM-E022) | `030-di-navegacion-ui`, `040-cliente-http-contratos`, `060-cache-offline` |
| [`350-Catalogo-Frecuencias`](350-Catalogo-Frecuencias/spec.md) | Consulta del catálogo de frecuencias de ingreso, gasto y recurrencia (NEXUM-E023) | `130-Registro-Usuario`, `340-Catalogo-Categorias`, `030-di-navegacion-ui`, `040-cliente-http-contratos`, `060-cache-offline` |
| [`360-Consultar-Ingreso`](360-Consultar-Ingreso/spec.md) | Consulta del ingreso habitual y su equivalente mensual (NEXUM-E024) | `350-Catalogo-Frecuencias`, `240-Perfil-Usuario`, `140-Login`, `150-Refresh-Token`, `030-di-navegacion-ui`, `040-cliente-http-contratos` |
| [`370-Actualizar-Ingreso`](370-Actualizar-Ingreso/spec.md) | Reemplazo del ingreso habitual y recálculo del equivalente mensual (NEXUM-E025) | `360-Consultar-Ingreso`, `350-Catalogo-Frecuencias`, `140-Login`, `150-Refresh-Token`, `040-cliente-http-contratos` |
| [`380-Resumen-Financiero`](380-Resumen-Financiero/spec.md) | Consulta del resumen mensual: disponible, ingreso, obligatorios, hormiga y reservado (NEXUM-E026) | `360-Consultar-Ingreso`, `330-Bootstrap-Inicial`, `140-Login`, `150-Refresh-Token`, `030-di-navegacion-ui`, `040-cliente-http-contratos` |
| [`390-Listar-Movimientos`](390-Listar-Movimientos/spec.md) | Historial paginado de movimientos con filtros por tipo y rango (NEXUM-E027) | `340-Catalogo-Categorias`, `380-Resumen-Financiero`, `140-Login`, `150-Refresh-Token`, `030-di-navegacion-ui`, `040-cliente-http-contratos` |
| [`400-Registrar-Movimiento`](400-Registrar-Movimiento/spec.md) | Alta de un gasto efectivamente realizado, manual o por voz confirmada (NEXUM-E028) | `390-Listar-Movimientos`, `340-Catalogo-Categorias`, `380-Resumen-Financiero`, `140-Login`, `150-Refresh-Token`, `030-di-navegacion-ui`, `040-cliente-http-contratos` |
| [`410-Detalle-Movimiento`](410-Detalle-Movimiento/spec.md) | Consulta del detalle exacto de un movimiento (NEXUM-E029) | `390-Listar-Movimientos`, `140-Login`, `150-Refresh-Token`, `030-di-navegacion-ui`, `040-cliente-http-contratos` |
| [`420-Editar-Movimiento`](420-Editar-Movimiento/spec.md) | Corrección de concepto, monto, categoría o tipo de un gasto registrado (NEXUM-E030) | `410-Detalle-Movimiento`, `400-Registrar-Movimiento`, `340-Catalogo-Categorias`, `380-Resumen-Financiero`, `140-Login`, `150-Refresh-Token`, `040-cliente-http-contratos` |

## Regla de uso para OpenCode
1. Abrir OpenCode **dentro de este repositorio**, no desde la carpeta padre.
2. Solicitar una única unidad por identificador, no todas las fases. Usar @phase-implementer cuando esté reconocido o el agente principal con iguales restricciones.
3. Revisar si la unidad exige una decisión aún pendiente; no simular autenticación, costos, pruebas ni despliegues.
4. Probar, reportar y marcar únicamente tareas verificadas. Después detenerse y pedir aprobación para la siguiente unidad.
5. **No realizar push ni despliegue automáticamente.** Los requisitos de producto (ingresos, gastos, presupuestos, metas, voz, ML, etc.) llevarán specs funcionales independientes cuando se autoricen.

## Progreso
- `000-arquitectura-base`: marco SDD documental existente.
- `010-*` a `120-*`: preparados, **ninguna implementación acreditada por la mera existencia de archivos**.
- `130-Registro-Usuario` y `140-Login`: **implementadas y probadas** el 2026-11-04 (evidencia en `memoria.md` §8 y en cada `task.md`); fuente mock sin Backend real.
- `150-Refresh-Token`: **Fase A documental completada** (2026-11-10); implementación técnica pendiente de mandato de Fase B.
- `151-github-actions`: **Fase A documental completada** (2026-11-05); implementación en workflow/Gradle preparada.
- `160-Logout`: **Fase A documental completada** (2026-11-10); implementación técnica pendiente de mandato de Fase B.
- `170-Recuperar-Contrasena`: **Fase A documental completada** (2026-11-10); implementación técnica pendiente de mandato de Fase B.
- `180-Restablecer-Contrasena`: **Fase A documental completada** (2026-11-10); implementación técnica pendiente de mandato de Fase B.
- `190-Cambiar-Contrasena`: **Fase A documental completada** (2026-11-10); implementación técnica pendiente de mandato de Fase B.
- `200-Sesiones-Activas`: **Fase A documental completada** (2026-11-10); implementación técnica pendiente de mandato de Fase B.
- `210-Revocar-Sesion`: **Fase A documental completada** (2026-11-10); implementación técnica pendiente de mandato de Fase B.
- `220-Solicitud-Verificacion-Email`: **Fase A documental completada** (2026-11-11); implementación técnica pendiente de mandato de Fase B.
- `230-Confirmar-Verificacion-Email`: **Fase A documental completada** (2026-11-11); implementación técnica pendiente de mandato de Fase B.
- `240-Perfil-Usuario`: **Fase A documental completada** (2026-11-11); implementación técnica pendiente de mandato de Fase B.
- `250-Editar-Perfil`: **Fase A documental completada** (2026-11-11); implementación técnica pendiente de mandato de Fase B.
- `260-Eliminar-Cuenta`: **Fase A documental completada** (2026-11-11); implementación técnica pendiente de mandato de Fase B.
- `270-Aviso-Privacidad-Vigente`: **Fase A documental completada** (2026-11-11); implementación técnica pendiente de mandato de Fase B.
- `280-Historial-Consentimientos`: **Fase A documental completada** (2026-11-11); implementación técnica pendiente de mandato de Fase B.
- `290-Registrar-Consentimiento`: **Fase A documental completada** (2026-11-11); implementación técnica pendiente de mandato de Fase B.
- `300-Estado-Onboarding`: **Fase A documental completada** (2026-11-11); implementación técnica pendiente de mandato de Fase B.
- `310-Completar-Onboarding`: **Fase A documental completada** (2026-11-11); implementación técnica pendiente de mandato de Fase B.
- `320-Exportar-Datos-Usuario`: **Fase A documental completada** (2026-11-12); implementación técnica pendiente de mandato de Fase B.
- `330-Bootstrap-Inicial`: **Fase A documental completada** (2026-11-12); implementación técnica pendiente de mandato de Fase B.
- `340-Catalogo-Categorias`: **Fase A documental completada** (2026-11-12); implementación técnica pendiente de mandato de Fase B.
- `350-Catalogo-Frecuencias`: **Fase A documental completada** (2026-11-12); implementación técnica pendiente de mandato de Fase B.
- `360-Consultar-Ingreso`: **Fase A documental completada** (2026-11-12); implementación técnica pendiente de mandato de Fase B.
- `370-Actualizar-Ingreso`: **Fase A documental completada** (2026-11-12); implementación técnica pendiente de mandato de Fase B.
- `380-Resumen-Financiero`: **Fase A documental completada** (2026-11-12); implementación técnica pendiente de mandato de Fase B.
- `390-Listar-Movimientos`: **Fase A documental completada** (2026-11-12); implementación técnica pendiente de mandato de Fase B.
- `400-Registrar-Movimiento`: **Fase A documental completada** (2026-11-12); implementación técnica pendiente de mandato de Fase B.
- `410-Detalle-Movimiento`: **Fase A documental completada** (2026-11-12); implementación técnica pendiente de mandato de Fase B.
- `420-Editar-Movimiento`: **Fase A documental completada** (2026-11-12); implementación técnica pendiente de mandato de Fase B.
- Estado de tests: **50 unitarios en verde** (2026-11-04); instrumentación **no ejecutada**.
