package mx.nexum.app.core.cache

import java.time.Instant

/**
 * Estados distinguibles para entradas y transacciones de caché / sincronización offline.
 * Satisface M060-R02: Pendiente, confirmado y obsoleto deben ser estados distinguibles.
 */
enum class SyncStatus {
    /**
     * Dato sincronizado y confirmado por el servidor remoto (fresco y verificado).
     */
    CONFIRMED,

    /**
     * Modificación o dato preparado localmente, pendiente de confirmación en backend.
     * No debe representarse como operación definitiva.
     */
    PENDING,

    /**
     * Dato cuyo TTL ha expirado o ha sido invalidado; requiere revalidación remota.
     */
    STALE
}

/**
 * Envoltorio para un dato en caché que conserva metadatos de usuario, ciclo de vida y estado.
 * Satisface M060-R01 y M060-R02.
 */
data class CacheEntry<T>(
    val key: String,
    val userId: String,
    val data: T,
    val status: SyncStatus = SyncStatus.CONFIRMED,
    val createdAt: Instant = Instant.now(),
    val ttlSeconds: Long = 300L
) {
    /**
     * Evalúa si la entrada ha superado su tiempo de vida (TTL).
     */
    fun isExpired(now: Instant = Instant.now()): Boolean {
        return now.isAfter(createdAt.plusSeconds(ttlSeconds))
    }

    /**
     * Retorna el estado efectivo considerando expiración temporal.
     */
    fun effectiveStatus(now: Instant = Instant.now()): SyncStatus {
        return if (status == SyncStatus.CONFIRMED && isExpired(now)) {
            SyncStatus.STALE
        } else {
            status
        }
    }
}

/**
 * Resultado de consulta en caché que reporta explícitamente el estado de los datos.
 */
sealed interface CacheResult<out T> {
    data class Hit<T>(val data: T, val status: SyncStatus, val entry: CacheEntry<T>) : CacheResult<T>
    object Miss : CacheResult<Nothing>
}

/**
 * Representa una operación encolada offline sujeta a sincronización e idempotencia.
 * Satisface M060-R03: Sincronización y reintento con claves de idempotencia backend.
 */
data class IdempotentOperation<T>(
    val idempotencyKey: String,
    val userId: String,
    val actionType: String,
    val payload: T,
    val status: SyncStatus = SyncStatus.PENDING,
    val createdAt: Instant = Instant.now(),
    val retryCount: Int = 0,
    val lastError: String? = null
)

/**
 * Contrato de almacenamiento para entradas de caché desacoplado de implementaciones concretas.
 * Permite almacenamiento en memoria o persistencia sin dependencias externas no aprobadas.
 */
interface CacheStorage<T> {
    suspend fun put(entry: CacheEntry<T>)
    suspend fun get(userId: String, key: String): CacheEntry<T>?
    suspend fun remove(userId: String, key: String)
    suspend fun clearUser(userId: String)
    suspend fun clearAll()
}
