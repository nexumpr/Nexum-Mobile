
package mx.nexum.app.core.session

import kotlinx.coroutines.test.runTest
import mx.nexum.app.core.network.AuthApi
import mx.nexum.app.core.network.RefreshResponse
import mx.nexum.app.core.network.RefreshTokenRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class SessionManagerTest {

    private val fakeApi = object : AuthApi {
        override suspend fun refreshToken(request: RefreshTokenRequest): RefreshResponse {
            return RefreshResponse(
                accessToken = "newAccess",
                refreshToken = "newRefresh",
                expiresIn = 3600
            )
        }
        override suspend fun logout(authHeader: String) {
            // No op for test
        }
    }

    @Test
    fun `save and load session`() = runTest {
        val storage = InMemoryCredentialStorage()
        val manager = SessionManager(storage, fakeApi)
        val session = Session(
            accessToken = "abc",
            refreshToken = "def",
            expiresAt = Instant.now().plusSeconds(300)
        )
        manager.setSession(session)
        assertEquals(session, storage.load())
        assertEquals(session, manager.sessionFlow.value)
    }

    @Test
    fun `refresh updates tokens`() = runTest {
        val storage = InMemoryCredentialStorage()
        val manager = SessionManager(storage, fakeApi)
        val old = Session(
            accessToken = "old",
            refreshToken = "ref",
            expiresAt = Instant.now().minusSeconds(10)
        )
        manager.setSession(old)
        manager.refresh()
        val updated = manager.sessionFlow.value!!
        assertEquals("newAccess", updated.accessToken)
        assertEquals("newRefresh", updated.refreshToken)
        assertTrue(!updated.isExpired)
    }

    @Test
    fun `logout clears session even on network error`() = runTest {
        val failingApi = object : AuthApi {
            override suspend fun refreshToken(request: RefreshTokenRequest): RefreshResponse {
                throw IllegalStateException("should not be called")
            }
            override suspend fun logout(authHeader: String) {
                throw RuntimeException("network failure")
            }
        }
        val storage = InMemoryCredentialStorage()
        val manager = SessionManager(storage, failingApi)
        val session = Session(
            accessToken = "token",
            refreshToken = "refresh",
            expiresAt = Instant.now().plusSeconds(300)
        )
        manager.setSession(session)
        manager.logout()
        assertNull(storage.load())
        assertNull(manager.sessionFlow.value)
    }
}
