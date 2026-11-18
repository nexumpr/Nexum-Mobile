package mx.nexum.app.core.cache

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

class OfflineCacheTest {

    private lateinit var cacheManager: OfflineCacheManager

    @Before
    fun setup() {
        cacheManager = OfflineCacheManager()
    }

    // --- M060-R01: Aislamiento por usuario y TTL ---

    @Test
    fun userIsolationPreventsCrossUserAccess() = runTest {
        val user1 = "user_alpha"
        val user2 = "user_beta"
        val key = "profile_summary"

        cacheManager.put(userId = user1, key = key, data = "Data Alpha", ttlSeconds = 300)
        cacheManager.put(userId = user2, key = key, data = "Data Beta", ttlSeconds = 300)

        val result1 = cacheManager.get<String>(userId = user1, key = key)
        val result2 = cacheManager.get<String>(userId = user2, key = key)

        assertTrue(result1 is CacheResult.Hit)
        assertTrue(result2 is CacheResult.Hit)
        assertEquals("Data Alpha", (result1 as CacheResult.Hit).data)
        assertEquals("Data Beta", (result2 as CacheResult.Hit).data)
        assertNotEquals(result1.data, result2.data)
    }

    @Test
    fun clearingUserDataDoesNotAffectOtherUsers() = runTest {
        val user1 = "user_alpha"
        val user2 = "user_beta"
        val key = "settings"

        cacheManager.put(userId = user1, key = key, data = "Config Alpha")
        cacheManager.put(userId = user2, key = key, data = "Config Beta")

        cacheManager.clearUser(user1)

        val result1 = cacheManager.get<String>(userId = user1, key = key)
        val result2 = cacheManager.get<String>(userId = user2, key = key)

        assertEquals(CacheResult.Miss, result1)
        assertTrue(result2 is CacheResult.Hit)
        assertEquals("Config Beta", (result2 as CacheResult.Hit).data)
    }

    @Test
    fun ttlExpirationTransitionsFromConfirmedToStale() = runTest {
        val user = "user_test"
        val key = "feed"
        val now = Instant.now()

        // TTL de 60 segundos creado en 'now'
        cacheManager.put(
            userId = user,
            key = key,
            data = "Feed Content",
            ttlSeconds = 60,
            status = SyncStatus.CONFIRMED,
            createdAt = now
        )

        // Consulta inmediata: debe ser CONFIRMED y estar fresco
        val freshResult = cacheManager.get<String>(userId = user, key = key, now = now.plusSeconds(30))
        assertTrue(freshResult is CacheResult.Hit)
        val hitFresh = freshResult as CacheResult.Hit
        assertEquals(SyncStatus.CONFIRMED, hitFresh.status)
        assertEquals("Feed Content", hitFresh.data)

        // Consulta tras 61 segundos: superó el TTL -> pasa a STALE
        val expiredResult = cacheManager.get<String>(userId = user, key = key, now = now.plusSeconds(61))
        assertTrue(expiredResult is CacheResult.Hit)
        val hitExpired = expiredResult as CacheResult.Hit
        assertEquals(SyncStatus.STALE, hitExpired.status)
        assertTrue(hitExpired.entry.isExpired(now.plusSeconds(61)))
    }

    @Test
    fun explicitInvalidationMarksDataAsStale() = runTest {
        val user = "user_test"
        val key = "dashboard"

        cacheManager.put(userId = user, key = key, data = "Dashboard Data", ttlSeconds = 600)
        cacheManager.invalidate(userId = user, key = key)

        val result = cacheManager.get<String>(userId = user, key = key)
        assertTrue(result is CacheResult.Hit)
        assertEquals(SyncStatus.STALE, (result as CacheResult.Hit).status)
    }

    // --- M060-R02: Distinción formal de estados (PENDING, CONFIRMED, STALE) ---

    @Test
    fun distinctStatesAreDifferentiableAndNotConfused() = runTest {
        val user = "user_states"

        cacheManager.put(userId = user, key = "confirmed_item", data = "100", status = SyncStatus.CONFIRMED)
        cacheManager.put(userId = user, key = "pending_item", data = "200", status = SyncStatus.PENDING)
        cacheManager.put(userId = user, key = "stale_item", data = "300", status = SyncStatus.STALE)

        val confirmedHit = cacheManager.get<String>(userId = user, key = "confirmed_item") as CacheResult.Hit
        val pendingHit = cacheManager.get<String>(userId = user, key = "pending_item") as CacheResult.Hit
        val staleHit = cacheManager.get<String>(userId = user, key = "stale_item") as CacheResult.Hit

        assertEquals(SyncStatus.CONFIRMED, confirmedHit.status)
        assertEquals(SyncStatus.PENDING, pendingHit.status)
        assertEquals(SyncStatus.STALE, staleHit.status)

        assertNotEquals(confirmedHit.status, pendingHit.status)
        assertNotEquals(pendingHit.status, staleHit.status)
        assertNotEquals(confirmedHit.status, staleHit.status)
    }

    // --- M060-R03: Idempotencia, sincronización y reintentos sin duplicados ---

    @Test
    fun enqueueingOperationCreatesPendingStatusWithIdempotencyKey() {
        val user = "user_sync"
        val idempotencyKey = "uuid-tx-001"

        val op = cacheManager.enqueueOperation(
            userId = user,
            actionType = "CREATE_PAYMENT",
            payload = mapOf("amount" to 500),
            idempotencyKey = idempotencyKey
        )

        assertEquals(idempotencyKey, op.idempotencyKey)
        assertEquals(user, op.userId)
        assertEquals(SyncStatus.PENDING, op.status)
        assertEquals(0, op.retryCount)

        val pending = cacheManager.getPendingOperations(user)
        assertEquals(1, pending.size)
        assertEquals(idempotencyKey, pending.first().idempotencyKey)
    }

    @Test
    fun deduplicationPreventsDuplicateOperationsWithSameKey() {
        val user = "user_sync"
        val idempotencyKey = "uuid-tx-duplicate"

        val op1 = cacheManager.enqueueOperation(
            userId = user,
            actionType = "SYNC_RECORD",
            payload = "First Call",
            idempotencyKey = idempotencyKey
        )

        // Segundo intento con la misma clave de idempotencia
        val op2 = cacheManager.enqueueOperation(
            userId = user,
            actionType = "SYNC_RECORD",
            payload = "Duplicate Call",
            idempotencyKey = idempotencyKey
        )

        assertEquals(op1.idempotencyKey, op2.idempotencyKey)
        assertEquals("First Call", op2.payload)

        val pending = cacheManager.getPendingOperations(user)
        assertEquals(1, pending.size)
    }

    @Test
    fun retryTrackingIncrementsCountAndRecordsLastError() {
        val user = "user_sync"
        val key = "uuid-retry-01"

        cacheManager.enqueueOperation(
            userId = user,
            actionType = "UPDATE_ITEM",
            payload = "Draft",
            idempotencyKey = key
        )

        cacheManager.recordOperationRetryFailure(user, key, "HTTP 503 Service Unavailable")
        var pending = cacheManager.getPendingOperations(user)
        assertEquals(1, pending.first().retryCount)
        assertEquals("HTTP 503 Service Unavailable", pending.first().lastError)
        assertEquals(SyncStatus.PENDING, pending.first().status)

        cacheManager.recordOperationRetryFailure(user, key, "Network Timeout")
        pending = cacheManager.getPendingOperations(user)
        assertEquals(2, pending.first().retryCount)
        assertEquals("Network Timeout", pending.first().lastError)
    }

    @Test
    fun confirmingOperationUpdatesStatusAndLeavesPendingQueue() {
        val user = "user_sync"
        val key = "uuid-confirm-01"

        cacheManager.enqueueOperation(
            userId = user,
            actionType = "SUBMIT_DATA",
            payload = "Payload",
            idempotencyKey = key
        )

        // Se marca como confirmada por el servidor
        cacheManager.markOperationConfirmed(user, key)

        // Ya no debe figurar como pendiente
        val pending = cacheManager.getPendingOperations(user)
        assertTrue(pending.isEmpty())
    }

    @Test
    fun clearAllPurgesCachesAndQueuesGlobally() = runTest {
        val user1 = "u1"
        val user2 = "u2"

        cacheManager.put(user1, "key1", "val1")
        cacheManager.put(user2, "key2", "val2")
        cacheManager.enqueueOperation(user1, "ACTION", "data")

        cacheManager.clearAll()

        assertEquals(CacheResult.Miss, cacheManager.get<String>(user1, "key1"))
        assertEquals(CacheResult.Miss, cacheManager.get<String>(user2, "key2"))
        assertTrue(cacheManager.getPendingOperations(user1).isEmpty())
    }
}
