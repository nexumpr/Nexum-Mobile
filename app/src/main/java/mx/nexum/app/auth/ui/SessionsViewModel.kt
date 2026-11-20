package mx.nexum.app.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.nexum.app.auth.data.AuthErrorKind
import mx.nexum.app.auth.data.AuthRepository
import mx.nexum.app.auth.data.AuthResult

class SessionsViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SessionsUiState())
    val state: StateFlow<SessionsUiState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<AuthEffect>(replay = 0)
    val effects: SharedFlow<AuthEffect> = _effects.asSharedFlow()

    init {
        onIntent(SessionsIntent.LoadSessions)
    }

    fun onIntent(intent: SessionsIntent) {
        when (intent) {
            SessionsIntent.LoadSessions -> loadInitialSessions()
            SessionsIntent.Refresh -> refreshSessions()
            SessionsIntent.LoadMore -> loadMoreSessions()
            SessionsIntent.DismissError -> _state.update { it.copy(errorMessage = null) }
            SessionsIntent.NavigateBack -> {
                viewModelScope.launch {
                    _effects.emit(AuthEffect.NavigateToApp)
                }
            }
            is SessionsIntent.RequestRevoke -> _state.update { it.copy(sessionToRevoke = intent.session) }
            SessionsIntent.DismissRevokeDialog -> _state.update { it.copy(sessionToRevoke = null) }
            SessionsIntent.ConfirmRevoke -> confirmRevoke()
        }
    }

    private fun loadInitialSessions() {
        if (_state.value.isLoading) return
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = repository.getSessions(limit = 25, cursor = null)) {
                is AuthResult.Success -> {
                    _state.update {
                        it.copy(
                            sessions = result.data.items,
                            nextCursor = result.data.nextCursor,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
                is AuthResult.Failure -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.error.message
                        )
                    }
                }
            }
        }
    }

    private fun refreshSessions() {
        if (_state.value.isRefreshing) return
        _state.update { it.copy(isRefreshing = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = repository.getSessions(limit = 25, cursor = null)) {
                is AuthResult.Success -> {
                    _state.update {
                        it.copy(
                            sessions = result.data.items,
                            nextCursor = result.data.nextCursor,
                            isRefreshing = false,
                            errorMessage = null
                        )
                    }
                }
                is AuthResult.Failure -> {
                    _state.update {
                        it.copy(
                            isRefreshing = false,
                            errorMessage = result.error.message
                        )
                    }
                }
            }
        }
    }

    private fun loadMoreSessions() {
        val cursor = _state.value.nextCursor ?: return
        if (_state.value.isLoadingMore) return
        _state.update { it.copy(isLoadingMore = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = repository.getSessions(limit = 25, cursor = cursor)) {
                is AuthResult.Success -> {
                    _state.update {
                        it.copy(
                            sessions = it.sessions + result.data.items,
                            nextCursor = result.data.nextCursor,
                            isLoadingMore = false,
                            errorMessage = null
                        )
                    }
                }
                is AuthResult.Failure -> {
                    _state.update {
                        it.copy(
                            isLoadingMore = false,
                            errorMessage = result.error.message
                        )
                    }
                }
            }
        }
    }

    private fun confirmRevoke() {
        val session = _state.value.sessionToRevoke ?: return
        _state.update {
            it.copy(
                sessionToRevoke = null,
                revokingSessionId = session.id,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            when (val result = repository.revokeSession(session.id, session.isCurrent)) {
                is AuthResult.Success -> {
                    _state.update { it.copy(revokingSessionId = null) }
                    if (session.isCurrent) {
                        _effects.emit(AuthEffect.NavigateToLogin)
                    } else {
                        _effects.emit(AuthEffect.ShowSnackbar("Sesión cerrada exitosamente"))
                        loadInitialSessions()
                    }
                }
                is AuthResult.Failure -> {
                    _state.update { it.copy(revokingSessionId = null) }
                    val isNotFound = result.error.kind == AuthErrorKind.NOT_FOUND || result.error.statusCode == 404
                    val isConflict = result.error.kind == AuthErrorKind.CONFLICT || result.error.statusCode == 409

                    if (isNotFound) {
                        _effects.emit(AuthEffect.ShowSnackbar("La sesión ya había expirado o no existe"))
                        loadInitialSessions()
                    } else if (isConflict) {
                        _state.update { it.copy(errorMessage = result.error.message) }
                        _effects.emit(AuthEffect.ShowSnackbar(result.error.message, retry = true))
                    } else {
                        _state.update { it.copy(errorMessage = result.error.message) }
                        _effects.emit(AuthEffect.ShowSnackbar(result.error.message, retry = result.error.retryable))
                    }
                }
            }
        }
    }
}
