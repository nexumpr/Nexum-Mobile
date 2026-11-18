package mx.nexum.app.core.cache

import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Gestor unificado de caché local y sincronización offline controlada.
 * Cumple con los requisitos:
 * - M060-R01: Aislamiento por usuario, respeto de TTL e invalidación.
 * - M060-R02: Distinción formal de estados (PENDING, CONFIRMED, STALE).
 * - M060-R03: Encolado de acciones con claves de idempotencia y prevención de duplicados.
 */
class OfflineCacheManager(
    private val defaultStorage: CacheStorage<Any> = InMemoryCacheStorage()
) {
    // Cola de operaciones offline por usuario: userId -> (idempotencyKey -> IdempotentOperation<Any>)
    private val pendingOperations = ConcurrentHashMap<String, ConcurrentHashMap<String, IdempotentOperation<Any>>>()

    /**
     * Almacena o actualiza una entrada en caché para un usuario específico con un TTL dado.
     */
    suspend fun <T : Any> put(
        userId: String,
        key: String,
        data: T,
        ttlSeconds: Long = 300L,
        status: SyncStatus = SyncStatus.CONFIRMED,
        createdAt: Instant = Instant.now()
    ) {
        require(userId.isNotBlank()) { "userId no puede ser vacío" }
        require(key.isNotBlank()) { "key no puede ser vacía" }

        val entry = CacheEntry(
            key = key,
            userId = userId,
            data = data as Any,
            status = status,
            createdAt = createdAt,
            ttlSeconds = ttlSeconds
        )
        defaultStorage.put(entry)
    }

    /**
     * Consulta una entrada de caché validando su TTL y estado efectivo.
     */
    @Suppress("UNCHECKED_CAST")
    suspend fun <T : Any> get(
        userId: String,
        key: String,
        now: Instant = Instant.now()
    ): CacheResult<T> {
        if (userId.isBlank() || key.isBlank()) return CacheResult.Miss

        val entry = defaultStorage.get(userId, key) ?: return CacheResult.Miss
        val effectiveStatus = entry.effectiveStatus(now)

        val updatedEntry = if (effectiveStatus != entry.status) {
            entry.copy(status = effectiveStatus)
        } else {
            entry
        }

        return CacheResult.Hit(
            data = entry.data as T,
            status = effectiveStatus,
            entry = updatedEntry as CacheEntry<T>
        )
    }

    /**
     * Invalida una clave específica para un usuario marcándola como STALE.
     */
    suspend fun invalidate(userId: String, key: String) {
        val entry = defaultStorage.get(userId, key) ?: return
        defaultStorage.put(entry.copy(status = SyncStatus.STALE))
    }

    /**
     * Elimina completamente una clave de caché para un usuario.
     */
    suspend fun remove(userId: String, key: String) {
        defaultStorage.remove(userId, key)
    }

    /**
     * Encola una operación offline asegurando una clave de idempotencia única.
     * Si la clave de idempotencia ya existe para el usuario, evita duplicación.
     */
    fun <T : Any> enqueueOperation(
        userId: String,
        actionType: String,
        payload: T,
        idempotencyKey: String = UUID.randomUUID().toString(),
        createdAt: Instant = Instant.now()
    ): IdempotentOperation<T> {
        require(userId.isNotBlank()) { "userId no puede ser vacío" }
        require(idempotencyKey.isNotBlank()) { "idempotencyKey no puede ser vacía" }

        val userQueue = pendingOperations.computeIfAbsent(userId) { ConcurrentHashMap() }

        // Evitar duplicados si la clave ya existe
        val existing = userQueue[idempotencyKey]
        if (existing != null) {
            @Suppress("UNCHECKED_CAST")
            return existing as IdempotentOperation<T>
        }

        val operation = IdempotentOperation(
            idempotencyKey = idempotencyKey,
            userId = userId,
            actionType = actionType,
            payload = payload as Any,
            status = SyncStatus.PENDING,
            createdAt = createdAt,
            retryCount = 0
        )
        userQueue[idempotencyKey] = operation

        @Suppress("UNCHECKED_CAST")
        return operation as IdempotentOperation<T>
    }

    /**
     * Obtiene todas las operaciones pendientes para un usuario.
     */
    fun getPendingOperations(userId: String): List<IdempotentOperation<Any>> {
        val userQueue = pendingOperations[userId] ?: return emptyList()
        return userQueue.values.filter { it.status == SyncStatus.PENDING }.toList()
    }

    /**
     * Marca una operación como confirmada por el servidor tras sincronización exitosa.
     */
    fun markOperationConfirmed(userId: String, idempotencyKey: String) {
        val userQueue = pendingOperations[userId] ?: return
        val current = userQueue[idempotencyKey] ?: return
        userQueue[idempotencyKey] = current.copy(status = SyncStatus.CONFIRMED)
    }

    /**
     * Registra un fallo de reintento en una operación encolada sin descartarla.
     */
    fun recordOperationRetryFailure(userId: String, idempotencyKey: String, error: String) {
        val userQueue = pendingOperations[userId] ?: return
        val current = userQueue[idempotencyKey] ?: return
        userQueue[idempotencyKey] = current.copy(
            retryCount = current.retryCount + 1,
            lastError = error
        )
    }

    /**
     * Elimina una operación encolada (por ejemplo tras confirmación o descarte explícito).
     */
    fun removeOperation(userId: String, idempotencyKey: String) {
        pendingOperations[userId]?.remove(idempotencyKey)
    }

    /**
     * Limpia completamente todos los datos y operaciones de un usuario (aislamiento y logout).
     */
    suspend fun clearUser(userId: String) {
        defaultStorage.clearUser(userId)
        pendingOperations.remove(userId)
    }

    /**
     * Limpia completamente todo el almacenamiento de caché y colas pendientes.
     */
    suspend fun clearAll() {
        defaultStorage.clearAll()
        pendingOperations.clear()
    }
}
