package mx.nexum.app.auth.ui

import mx.nexum.app.auth.data.UserSession

data class SessionsUiState(
    val sessions: List<UserSession> = emptyList(),
    val nextCursor: String? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null
)

sealed interface SessionsIntent {
    data object LoadSessions : SessionsIntent
    data object Refresh : SessionsIntent
    data object LoadMore : SessionsIntent
    data object DismissError : SessionsIntent
    data object NavigateBack : SessionsIntent
}
