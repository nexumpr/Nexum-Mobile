---
project: Nexum-Mobile
unit: 060-cache-offline
kind: implementation-tasks
version: 1.0.0
status: verified
implementation: completed
---

# TASK — Caché local y sincronización controlada

**Estado:** 3 de 3 tareas completadas y verificadas mediante pruebas unitarias reales y build exitoso.

**Dependencias:** `040-cliente-http-contratos`, `050-sesion-credenciales`. **Regla:** no implementar funcionalidades financieras ni cambios externos sin aprobación adicional.

## Actividades verificables
- [x] **N060-T01 — Documentar alcance de caché/offline con estados sintéticos.**
  - **Requisito:** `M060-R01` · **Plan:** `N060-P01`.
  - **Aceptación:** Modelo de caché `CacheEntry`, expiración por TTL (`isExpired`, `effectiveStatus`) y aislamiento estricto por `userId` implementado.
  - **Pruebas:** `./gradlew testDebugUnitTest` (`OfflineCacheTest` - `userIsolationPreventsCrossUserAccess`, `clearingUserDataDoesNotAffectOtherUsers`, `ttlExpirationTransitionsFromConfirmedToStale`, `explicitInvalidationMarksDataAsStale`).
  - **Evidencia registrada:** Creación de `CacheContracts.kt`, `InMemoryCacheStorage.kt`, `OfflineCacheManager.kt`; pruebas unitarias aprobadas en 1m 39s.
- [x] **N060-T02 — Implementar interfaces y storage solo tras aprobar opción.**
  - **Requisito:** `M060-R02` · **Plan:** `N060-P02`.
  - **Aceptación:** Contrato de estados distinguibles `SyncStatus` (`CONFIRMED`, `PENDING`, `STALE`), interfaz de almacenamiento `CacheStorage<T>` e implementación `InMemoryCacheStorage<T>` desacoplada de dependencias externas.
  - **Pruebas:** `./gradlew testDebugUnitTest` (`OfflineCacheTest` - `distinctStatesAreDifferentiableAndNotConfused`).
  - **Evidencia registrada:** Envoltorios de resultado `CacheResult`, estados inmutables en `CacheContracts.kt` integrados con `AppContainer`.
- [x] **N060-T03 — Probar aislamiento por usuario, caducidad y reintentos sin duplicados.**
  - **Requisito:** `M060-R03` · **Plan:** `N060-P03`.
  - **Aceptación:** Cola de operaciones pendientes con clave de idempotencia (`IdempotentOperation`), deduplicación garantizada, registro de reintentos y actualización a confirmada.
  - **Pruebas:** `./gradlew testDebugUnitTest` (`OfflineCacheTest` - `enqueueingOperationCreatesPendingStatusWithIdempotencyKey`, `deduplicationPreventsDuplicateOperationsWithSameKey`, `retryTrackingIncrementsCountAndRecordsLastError`, `confirmingOperationUpdatesStatusAndLeavesPendingQueue`, `clearAllPurgesCachesAndQueuesGlobally`).
  - **Evidencia registrada:** 10 pruebas unitarias en `OfflineCacheTest` ejecutadas con 0 fallos; `./gradlew assembleDebug` exitoso.

## Cierre y evidencia
- Pruebas ejecutadas: `.\gradlew.bat testDebugUnitTest` (BUILD SUCCESSFUL, 88 tests en total, 10 de `OfflineCacheTest`, 0 fallos) y `.\gradlew.bat assembleDebug` (BUILD SUCCESSFUL, APK generado).
- Tareas marcadas `[x]` con verificación real.
- Actualización registrada en `memoria.md` y `specs/README.md`.
- `git push`, merge, despliegue y operaciones destructivas no ejecutadas (requieren autorización separada).
