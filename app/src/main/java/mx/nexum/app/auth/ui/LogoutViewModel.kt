package mx.nexum.app.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.nexum.app.auth.data.AuthRepository

data class LogoutUiState(
    val isConfirmDialogOpen: Boolean = false,
    val isLoading: Boolean = false,
    val userMessage: String? = null
)

sealed interface LogoutIntent {
    data object RequestLogout : LogoutIntent
    data object ConfirmLogout : LogoutIntent
    data object DismissDialog : LogoutIntent
}

class LogoutViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val mutableState = MutableStateFlow(LogoutUiState())
    val state: StateFlow<LogoutUiState> = mutableState.asStateFlow()

    private val mutableEffects = MutableSharedFlow<AuthEffect>(
        replay = 0,
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effects: SharedFlow<AuthEffect> = mutableEffects.asSharedFlow()

    fun onIntent(intent: LogoutIntent) {
        when (intent) {
            LogoutIntent.RequestLogout -> {
                mutableState.update { it.copy(isConfirmDialogOpen = true, userMessage = null) }
            }
            LogoutIntent.DismissDialog -> {
                if (!mutableState.value.isLoading) {
                    mutableState.update { it.copy(isConfirmDialogOpen = false) }
                }
            }
            LogoutIntent.ConfirmLogout -> confirmLogout()
        }
    }

    private fun confirmLogout() {
        if (mutableState.value.isLoading) return

        mutableState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            repository.logout()
            mutableState.update {
                it.copy(
                    isLoading = false,
                    isConfirmDialogOpen = false
                )
            }
            mutableEffects.tryEmit(AuthEffect.NavigateToLogin)
        }
    }
}
