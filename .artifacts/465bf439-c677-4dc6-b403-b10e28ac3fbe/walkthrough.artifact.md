# Walkthrough — Migración de Login Mock a Endpoint Real (E002)

Se ha migrado exitosamente el inicio de sesión (`Login`) de la fuente simulada (`MockAuthApi`) al endpoint real **NEXUM-E002** (`POST https://d2lpmqy5w6x9es.cloudfront.net/api/v1/auth/login`).

## Cambios Realizados

### Autenticación y Red (`mx.nexum.app.auth`)

#### [NEW] [RetrofitAuthApi.kt](file:///C:/Users/Rayqu/StudioProjects/Nexum-Mobile/app/src/main/java/mx/nexum/app/auth/data/RetrofitAuthApi.kt)
- Implementación real de `AuthApi` consumiendo `AuthApiService.login` y `AuthApiService.register`.
- Mapeo de códigos de error HTTP (`401`, `429`, `500`, etc.) a `AuthApiException` con sus respectivos `AuthErrorKind`.

#### [MODIFY] [AuthDtos.kt](file:///C:/Users/Rayqu/StudioProjects/Nexum-Mobile/app/src/main/java/mx/nexum/app/auth/data/dto/AuthDtos.kt)
- Añadidos `LoginRequestDto`, `LoginResponseDto`, `LoginUserDto`, `SessionIdDto`, `OnboardingConfigDto` y DTOs de error estándar (`ApiErrorResponse`, `ApiErrorPayload`, `ApiErrorDetail`).

#### [MODIFY] [AuthApiService.kt](file:///C:/Users/Rayqu/StudioProjects/Nexum-Mobile/app/src/main/java/mx/nexum/app/auth/data/network/AuthApiService.kt)
- Declaración del endpoint `@POST("auth/login")`.

#### [MODIFY] [LoginViewModel.kt](file:///C:/Users/Rayqu/StudioProjects/Nexum-Mobile/app/src/main/java/mx/nexum/app/auth/ui/LoginViewModel.kt)
- Inclusión automática de metadatos Android en el request (`deviceId`, `deviceName`, `platform`).

#### [MODIFY] [AuthFlow.kt](file:///C:/Users/Rayqu/StudioProjects/Nexum-Mobile/app/src/main/java/mx/nexum/app/auth/ui/AuthFlow.kt)
- Cableado del repositorio con `RetrofitAuthApi(NetworkModule.authApiService)`.

## Verificación y Pruebas

- **Compilación:** `./gradlew assembleDebug` $\rightarrow$ **BUILD SUCCESSFUL**.
- **Pruebas Unitarias:** `./gradlew testDebugUnitTest` $\rightarrow$ **BUILD SUCCESSFUL** (51 pruebas pasadas sin errores).
- **Linting:** `./gradlew lintDebug` $\rightarrow$ **BUILD SUCCESSFUL**.
- **Memoria:** Registro actualizado en `memoria.md` (Sección 29).
