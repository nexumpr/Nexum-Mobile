# Tareas — Migración de Login Mock a Endpoint Real (E002)

- `[x]` **N140-Real-T01** — Definir DTOs de solicitud (`LoginRequestDto`) y respuesta (`LoginResponseDto`, `LoginUserDto`, `SessionIdDto`, `OnboardingConfigDto`) junto con el manejo de errores estándar (`ApiErrorResponse`).
- `[x]` **N140-Real-T02** — Actualizar `AuthApiService` para declarar el endpoint `POST auth/login`.
- `[x]` **N140-Real-T03** — Implementar `RetrofitAuthApi` con mapeo de errores HTTP (`400`, `401`, `429`, `500`, `503`) a `AuthApiException`.
- `[x]` **N140-Real-T04** — Actualizar `AuthFlowViewModel` para cablear `AuthRepositoryImpl` con `RetrofitAuthApi`.
- `[x]` **N140-Real-T05** — Actualizar `LoginViewModel` para incluir metadatos de Android (`deviceId`, `deviceName`, `platform`) en el request.
- `[x]` **N140-Real-T06** — Actualizar y ejecutar pruebas unitarias con `./gradlew testDebugUnitTest` y `./gradlew assembleDebug`.
- `[x]` **N140-Real-T07** — Registrar evidencia y actualizaciones en `memoria.md`.
