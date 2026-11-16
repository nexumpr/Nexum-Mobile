package mx.nexum.app.core.session

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Almacén volátil usado en pruebas unitarias.
 * No persiste entre ejecuciones de la app.
 */
class InMemoryCredentialStorage : CredentialStorage {
    private var session: Session? = null
    private val lock = Mutex()

    override suspend fun save(session: Session) = lock.withLock { this.session = session }
    override suspend fun load(): Session? = lock.withLock { session }
    override suspend fun clear() = lock.withLock { session = null }
}
