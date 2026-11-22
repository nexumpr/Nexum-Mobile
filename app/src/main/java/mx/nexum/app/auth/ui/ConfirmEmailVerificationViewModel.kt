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
import mx.nexum.app.auth.data.AuthError
import mx.nexum.app.auth.data.AuthErrorKind
import mx.nexum.app.auth.data.AuthRepository
import mx.nexum.app.auth.data.AuthResult
import mx.nexum.app.auth.data.TokenStorage

data class ConfirmEmailVerificationUiState(
    val token: String = "",
    val isLoading: Boolean = false,
    val isConfirmed: Boolean = false,
    val isTokenExpiredOrInvalid: Boolean = false,
    val isTokenAlreadyUsed: Boolean = false,
    val requestId: String? = null,
    val errorMessage: String? = null
)

sealed interface ConfirmEmailVerificationIntent {
    data class TokenChanged(val value: String) : ConfirmEmailVerificationIntent
    data object Submit : ConfirmEmailVerificationIntent
    data object NavigateToLogin : ConfirmEmailVerificationIntent
    data object NavigateToRequestVerification : ConfirmEmailVerificationIntent
}

class ConfirmEmailVerificationViewModel(
    private val repository: AuthRepository,
    private val tokenStorage: TokenStorage
) : ViewModel() {

    private val mutableState = MutableStateFlow(ConfirmEmailVerificationUiState())
    val state: StateFlow<ConfirmEmailVerificationUiState> = mutableState.asStateFlow()

    private val mutableEffects = MutableSharedFlow<AuthEffect>(
        replay = 0,
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effects: SharedFlow<AuthEffect> = mutableEffects.asSharedFlow()

    fun onIntent(intent: ConfirmEmailVerificationIntent) {
        when (intent) {
            is ConfirmEmailVerificationIntent.TokenChanged -> mutableState.update {
                it.copy(
                    token = intent.value,
                    isTokenExpiredOrInvalid = false,
                    isTokenAlreadyUsed = false,
                    errorMessage = null
                )
            }
            ConfirmEmailVerificationIntent.Submit -> submit()
            ConfirmEmailVerificationIntent.NavigateToLogin -> {
                mutableEffects.tryEmit(AuthEffect.NavigateToLogin)
            }
            ConfirmEmailVerificationIntent.NavigateToRequestVerification -> {
                mutableEffects.tryEmit(AuthEffect.NavigateToRequestVerification)
            }
        }
    }

    private fun submit() {
        val token = mutableState.value.token
        if (mutableState.value.isLoading || token.length < 10) return

        mutableState.update {
            it.copy(
                isLoading = true,
                isTokenExpiredOrInvalid = false,
                isTokenAlreadyUsed = false,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            when (val result = repository.confirmEmailVerification(token)) {
                is AuthResult.Success -> {
                    mutableState.update {
                        it.copy(
                            isLoading = false,
                            isConfirmed = true,
                            requestId = result.data.requestId,
                            isTokenExpiredOrInvalid = false,
                            isTokenAlreadyUsed = false,
                            errorMessage = null
                        )
                    }
                    mutableEffects.tryEmit(
                        if (tokenStorage.current() != null) AuthEffect.NavigateToApp else AuthEffect.NavigateToLogin
                    )
                }
                is AuthResult.Failure -> handleFailure(result.error)
            }
        }
    }

    private fun handleFailure(error: AuthError) {
        when {
            error.statusCode == 400 || error.kind == AuthErrorKind.INVALID_TOKEN -> mutableState.update {
                it.copy(
                    isLoading = false,
                    isTokenExpiredOrInvalid = true,
                    errorMessage = error.message
                )
            }
            error.statusCode == 409 || error.kind == AuthErrorKind.CONFLICT -> mutableState.update {
                it.copy(
                    isLoading = false,
                    isTokenAlreadyUsed = true,
                    errorMessage = error.message
                )
            }
            else -> {
                mutableState.update { it.copy(isLoading = false, errorMessage = error.message) }
                mutableEffects.tryEmit(AuthEffect.ShowSnackbar(error.message, error.retryable))
            }
        }
    }
}
