package mx.nexum.app.auth.data

interface TokenStorage {
    fun save(session: Session)

    fun current(): Session?

    fun clear()
}

class InMemoryTokenStorage : TokenStorage {

    private val lock = Any()
    private var session: Session? = null

    override fun save(session: Session) {
        synchronized(lock) {
            this.session = session
        }
    }

    override fun current(): Session? = synchronized(lock) { session }

    override fun clear() {
        synchronized(lock) {
            session = null
        }
    }
}
