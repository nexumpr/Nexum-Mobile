# Plan de Integración Real del Endpoint de Login (E002)

Migrar el inicio de sesión (`Login`) de un entorno simulado (`MockAuthApi`) a una integración HTTP real consumiendo el endpoint **NEXUM-E002** (`POST /api/v1/auth/login`) en `https://d2lpmqy5w6x9es.cloudfront.net/api/v1/auth/login`, conforme a las especificaciones de payload, metadatos Android y manejo de errores HTTP (400, 401, 429, 500, 503).

## User Review Required

> [!IMPORTANT]
> Este cambio reemplaza la fuente simulada de login por llamadas HTTP reales a CloudFront, manteniendo la arquitectura MVI y el almacenamiento en `TokenStorage`.

- Se requiere verificar el manejo correcto de códigos de error HTTP estándar (`400`, `401`, `429`, `500`, `503`) parseando el cuerpo de error JSON `ApiErrorResponse`.

## Open Questions

- Ninguna pregunta pendiente; los contratos y códigos de error están definidos en el requerimiento.

## Proposed Changes

### Módulo de Autenticación (`mx.nexum.app.auth`)

#### [MODIFY] [AuthDtos.kt](file:///C:/Users/Rayqu/StudioProjects/Nexum-Mobile/app/src/main/java/mx/nexum/app/auth/data/dto/AuthDtos.kt)
- Añadir DTOs para `LoginRequestDto`, `LoginResponseDto`, `LoginUserDto`, `SessionIdDto`, `OnboardingConfigDto` y DTOs de error estándar (`ApiErrorResponse`, `ApiErrorPayload`).

#### [MODIFY] [AuthApiService.kt](file:///C:/Users/Rayqu/StudioProjects/Nexum-Mobile/app/src/main/java/mx/nexum/app/auth/data/network/AuthApiService.kt)
- Añadir el método `@POST("auth/login")` para consumir el endpoint E002.

#### [NEW] [RetrofitAuthApi.kt](file:///C:/Users/Rayqu/StudioProjects/Nexum-Mobile/app/src/main/java/mx/nexum/app/auth/data/RetrofitAuthApi.kt)
- Implementar `AuthApi` usando `AuthApiService`, realizando el mapeo de errores HTTP (`401`, `429`, `500`, etc.) a `AuthApiException` con `AuthErrorKind` correspondiente (`INVALID_CREDENTIALS`, `RATE_LIMITED`, `NETWORK`, `UNKNOWN`).

#### [MODIFY] [AuthFlow.kt](file:///C:/Users/Rayqu/StudioProjects/Nexum-Mobile/app/src/main/java/mx/nexum/app/auth/ui/AuthFlow.kt)
- Actualizar `AuthFlowViewModel` para instanciar `AuthRepositoryImpl` utilizando `RetrofitAuthApi` (manteniendo `MockAuthApi` en pruebas unitarias).

#### [MODIFY] [LoginViewModel.kt](file:///C:/Users/Rayqu/StudioProjects/Nexum-Mobile/app/src/main/java/mx/nexum/app/auth/ui/LoginViewModel.kt)
- Incluir opcionalmente `deviceId` ("installation-<uuid>") y `platform` ("android") en el request de login conforme a los metadatos especificados.

## Verification Plan

### Automated Tests
- Ejecutar `./gradlew testDebugUnitTest` para verificar que las pruebas existentes y nuevas pasen correctamente con el repositorio y el manejo de errores HTTP.
- Ejecutar `./gradlew assembleDebug` para validar que el proyecto compila sin errores.

### Manual Verification
- Compilar y desplegar en emulador/dispositivo para comprobar el flujo de inicio de sesión contra el servidor real o verificar la correcta construcción del request y manejo de errores.
