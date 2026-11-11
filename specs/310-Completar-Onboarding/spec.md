---
project: Nexum-Mobile
unit: 310-Completar-Onboarding
kind: technical-spec
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# SPEC — Finalización de Onboarding (Complete Onboarding)

## Propósito
Permitir a un usuario titular autenticado marcar el recorrido de bienvenida como completado en el backend bajo el contrato **NEXUM-E019** (`PATCH /api/v1/users/me/onboarding`, soporte y consistencia `[S]`, catálogo V6.0.2), tanto al otorgar permisos explicativos ("Continuar a Nexum") como al omitirlos ("Continuar con funciones limitadas"), garantizando que el avance del usuario no sea bloqueado por problemas de red y que ningún flag remoto suplante la gobernanza nativa de permisos en Android.

## Lugar en el plan SDD
- **Constitución:** [`.specify/memory/constitution.md`](../../.specify/memory/constitution.md) (vigente: MO-PR04 Backend como fuente autoritativa, MO-PR06 Privacidad y seguridad desde el diseño: "derivación por token sin aceptar userId ajeno", MO-PR07 Usabilidad inclusiva Material 3: "permitir uso continuo incluso si se omiten permisos opcionales", MO-PR08 Manejo explícito de conectividad y resiliencia offline, MO-PR09 Cuidado con permisos: "no tomar flags de permisos remotos como autorización de Android; el permiso real de micrófono y notificaciones se comprueba en el SO").
- **Tecnología:** Kotlin / Jetpack Compose / MVI (Android nativo).
- **Contrato:** NEXUM-E019 del catálogo de endpoints de referencia V6.0.2 (`PATCH /api/v1/users/me/onboarding` [S]).
- **Dependencias previas:** [`300-Estado-Onboarding`](../300-Estado-Onboarding/spec.md) (consulta y modelos de estado de onboarding), [`140-Login`](../140-Login/spec.md) (sesión activa requerida), [`150-Refresh-Token`](../150-Refresh-Token/spec.md) (renovación de token si expira durante el recorrido), [`030-di-navegacion-ui`](../030-di-navegacion-ui/spec.md) (shell de navegación hacia `HomeScreen`), [`040-cliente-http-contratos`](../040-cliente-http-contratos/spec.md) (cliente HTTP) y [`070-permisos-privacidad-accesibilidad`](../070-permisos-privacidad-accesibilidad/spec.md) (gestión de permisos nativos de Android).
- **Naturaleza:** especificación técnica de ciclo de vida de bienvenida, consistencia y soporte (`[S]`).

## Alcance permitido
Acciones de finalización en `OnboardingScreen` mediante botones accesibles ("Continuar a Nexum" y "Continuar con funciones limitadas"), modelado de DTOs del contrato E019 (`UpdateOnboardingRequest`), ampliación de `AuthApi` y `AuthRepository`, implementación simulada en `MockAuthApi` con persistencia en memoria y actualización de `finishedAt`, ViewModel MVI (`CompleteOnboardingViewModel`) con persistencia local incondicional (fail-open controlado) para no atrapar al usuario en caso de error de transporte, emisión de navegación unidireccional hacia la pantalla principal (`HomeScreen`) con desapilado del onboarding, y pruebas unitarias de la unidad.

## Exclusiones explícitas
No solicitar ni validar permisos nativos de Android directamente en este endpoint (la solicitud de permisos de micrófono o notificaciones se ejecuta exclusivamente en el cliente mediante APIs del sistema operativo conforme a `070`); no enviar ni aceptar `userId` por parámetro; no impedir el acceso a las funciones manuales centrales si el usuario elige "Continuar con funciones limitadas"; no conectarse a un backend real en esta etapa; no publicar ni desplegar.

## Requisitos y criterios verificables
| ID | Requisito obligatorio | Aceptación mínima |
| --- | --- | --- |
| **M310-R01** | **Disparadores en UI ("Continuar a Nexum" / "Continuar con funciones limitadas"):** Componente Compose en `OnboardingScreen` con dos acciones claramente diferenciadas: botón principal "Continuar a Nexum" (modo completo) y botón secundario accesible "Continuar con funciones limitadas" (cuando el usuario decide no activar micrófono o notificaciones). Ambas acciones conducen a la pantalla principal. | Composable probado con interacción en ambos botones y verificación de accesibilidad Material 3. |
| **M310-R02** | **Contrato y Payload E019:** Enviar `PATCH /api/v1/users/me/onboarding` con cabecera `Authorization: Bearer <accessToken>`, cabecera opcional `Idempotency-Key: <UUID>` y payload JSON `{"completed": true, "limitedMode": false/true}`. Recibir respuesta exitosa `200 OK` con el estado actualizado (`OnboardingStatusResponse`). | Modelado de DTOs acorde al catálogo V6.0.2 y verificación de serialización en pruebas. |
| **M310-R03** | **No Suplantación de Permisos de Android (MO-PR09):** La transmisión de `limitedMode: false` o `completed: true` al servidor registra únicamente la finalización del asistente en la cuenta; en ningún caso la app debe considerar concedidos los permisos de Android (`RECORD_AUDIO`, `POST_NOTIFICATIONS`) basándose en la respuesta del backend. La verificación nativa en tiempo de ejecución sigue siendo obligatoria. | Inspección de arquitectura y pruebas que confirmen la ausencia de bypass de permisos del SO. |
| **M310-R04** | **Avance No Bloqueante y Sincronización Local (MO-PR07, MO-PR08):** Si la petición de red falla por falta de conectividad o timeout, la aplicación DEBE registrar localmente la finalización del onboarding en el dispositivo (`finishedAt = now()`) y permitir al usuario ingresar a `HomeScreen`, encolando la sincronización o reintento sin atraparlo en un bucle de bienvenida. | Pruebas unitarias que demuestren avance a Inicio y guardado local ante fallo de red. |
| **M310-R05** | **Navegación Definitiva a Inicio y Limpieza de Backstack (MO-PR02, MO-PR03):** Al completarse el onboarding, se emite el efecto `OnboardingEffect.NavigateToHome`, reseteando el historial de navegación para que el gesto o botón "Atrás" de Android no devuelva al usuario al onboarding. | Prueba de emisión de efecto y desapilado en el enrutador de Compose. |
| **M310-R06** | **Simulación en `MockAuthApi` y Cobertura de Pruebas Unitarias:** La fuente simulada debe actualizar el estado del usuario en memoria marcando `finishedAt` con timestamp RFC 3339, devolver `200 OK` tras retardo (300–700 ms), soportar el centinela `net_error_onboarding` y contar con suite completa de pruebas unitarias (`CompleteOnboardingViewModelTest`, `MockAuthApiTest`) con `kotlinx-coroutines-test`. | Pruebas unitarias aprobadas cubriendo modo completo, modo limitado, resiliencia offline y centinelas. |

## Escenarios de prueba y evidencia
- **Caso favorable modo completo ("Continuar a Nexum"):** Usuario finaliza el asistente y pulsa "Continuar a Nexum" → llamada `PATCH /api/v1/users/me/onboarding` con `{"completed": true, "limitedMode": false}` → respuesta `200 OK` con `finishedAt` → actualización de caché local → navegación inmediata a `HomeScreen`.
- **Caso favorable modo limitado ("Continuar con funciones limitadas"):** Usuario rechaza o posterga permisos y pulsa "Continuar con funciones limitadas" → llamada con `{"completed": true, "limitedMode": true}` → respuesta `200 OK` → navegación fluida a `HomeScreen` con operatividad manual intacta.
- **Caso de fallo de transporte (Fail-open controlado):** Pérdida de red al pulsar continuar → la llamada HTTP falla → el repositorio atrapa el error, graba localmente `onboardingCompleted = true` y permite la navegación a `HomeScreen` sin frustrar al usuario.
- **Caso de doble pulsación:** Pulsaciones rápidas consecutivas en el botón → el estado `isSubmitting = true` deshabilita el botón reactivamente y la cabecera `Idempotency-Key` previene peticiones redundantes.
- **Regresión:** Compilación exitosa de `./gradlew assembleDebug` y aprobación continua de los 50 tests preexistentes de autenticación.
- **Comandos esperados (en Fase B):** `./gradlew testDebugUnitTest`; `./gradlew assembleDebug`.
- **Evidencia exigida:** ejecución real, reporte de cobertura de tests, trazabilidad a requisitos y registro en `memoria.md`.

## Riesgos y bloqueos
- **Falta de Backend real:** en MongoDB Atlas, el campo `onboarding.finishedAt` y `limitedMode` se actualizan en `profiles`; en el móvil se asegura el contrato DTO, la no duplicidad y la custodia local de la bandera de finalización.
- **Desincronización por fallo offline:** si el usuario completa el onboarding sin red y luego cambia de dispositivo, el nuevo equipo podría solicitarle el onboarding nuevamente hasta que el primer dispositivo sincronice con éxito; este comportamiento es tolerable y preserva la experiencia inmediata.

## Criterio de salida de la unidad
Todas las tareas en `task.md` documentadas en estado pendiente (`- [ ]`); trazabilidad completa requisito → plan → tarea; ninguna ejecución de código en Fase A; sin cambios fuera del alcance autorizado.
