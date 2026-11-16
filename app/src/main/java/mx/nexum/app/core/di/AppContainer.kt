package mx.nexum.app.core.di

// Core authentication API (refresh token, logout) used by SessionManager
import mx.nexum.app.core.network.AuthApi as CoreAuthApi
import mx.nexum.app.auth.data.RetrofitAuthApi

// Data authentication API (register, login) used by AuthRepository
import mx.nexum.app.auth.data.AuthApi as DataAuthApi
import mx.nexum.app.auth.data.RetrofitAuthRepositoryApi

import mx.nexum.app.auth.data.AuthRepository
import mx.nexum.app.auth.data.AuthRepositoryImpl
import mx.nexum.app.auth.data.InMemoryTokenStorage
import mx.nexum.app.auth.data.TokenStorage

import mx.nexum.app.core.session.CredentialStorage
import mx.nexum.app.core.session.InMemoryCredentialStorage
import mx.nexum.app.core.session.SessionManager

interface AppContainer {
    val credentialStorage: CredentialStorage
    val sessionManager: SessionManager
    val authRepository: AuthRepository
    val tokenStorage: TokenStorage
}

class DefaultAppContainer : AppContainer {
    // Core auth API for session management (refresh, logout)
    private val coreAuthApi: CoreAuthApi = RetrofitAuthApi()
    // Data auth API for registration & login used by AuthRepository
    private val dataAuthApi: DataAuthApi = RetrofitAuthRepositoryApi()

    override val tokenStorage: TokenStorage by lazy { InMemoryTokenStorage() }
    override val credentialStorage: CredentialStorage by lazy { InMemoryCredentialStorage() }
    override val sessionManager: SessionManager by lazy { SessionManager(credentialStorage, coreAuthApi) }
    override val authRepository: AuthRepository by lazy { AuthRepositoryImpl(api = dataAuthApi, tokenStorage = tokenStorage) }
}
