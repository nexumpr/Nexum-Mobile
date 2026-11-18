package mx.nexum.app.core.cache

import java.util.concurrent.ConcurrentHashMap

/**
 * Implementación en memoria de [CacheStorage] segura para concurrencia.
 * Garantiza aislamiento estricto por identificador de usuario (`userId`).
 * Satisface M060-R01 (aislamiento y limpieza).
 */
class InMemoryCacheStorage<T> : CacheStorage<T> {

    // Mapa anidado: userId -> (key -> CacheEntry<T>)
    private val storage = ConcurrentHashMap<String, ConcurrentHashMap<String, CacheEntry<T>>>()

    override suspend fun put(entry: CacheEntry<T>) {
        val userMap = storage.computeIfAbsent(entry.userId) { ConcurrentHashMap() }
        userMap[entry.key] = entry
    }

    override suspend fun get(userId: String, key: String): CacheEntry<T>? {
        return storage[userId]?.get(key)
    }

    override suspend fun remove(userId: String, key: String) {
        storage[userId]?.remove(key)
    }

    override suspend fun clearUser(userId: String) {
        storage.remove(userId)
    }

    override suspend fun clearAll() {
        storage.clear()
    }
}
