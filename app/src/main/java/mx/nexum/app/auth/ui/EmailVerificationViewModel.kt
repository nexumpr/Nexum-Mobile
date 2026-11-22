package mx.nexum.app.auth.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mx.nexum.app.auth.data.AuthErrorKind
import mx.nexum.app.auth.data.AuthRepository
import mx.nexum.app.auth.data.AuthResult
import mx.nexum.app.auth.data.TokenStorage
import mx.nexum.app.core.mvi.MviViewModel
import mx.nexum.app.core.mvi.UiEffect
import mx.nexum.app.core.mvi.UiIntent
import mx.nexum.app.core.mvi.UiState

data class EmailVerificationUiState(
    val userEmail: String = "",
    val isEmailVerified: Boolean = false,
    val isLoading: Boolean = false,
    val isAccepted: Boolean = false,
    val lastRequestId: String? = null,
    val cooldownSeconds: Int = 0,
    val feedbackMessage: String? = null,
    val isError: Boolean = false
) : UiState

sealed interface EmailVerificationIntent : UiIntent {
    data object RequestVerification : EmailVerificationIntent
    data object DismissFeedback : EmailVerificationIntent
    data object RefreshStatus : EmailVerificationIntent
}

sealed interface EmailVerificationEffect : UiEffect

class EmailVerificationViewModel(
    private val repository: AuthRepository,
    private val tokenStorage: TokenStorage
) : MviViewModel<EmailVerificationUiState, EmailVerificationIntent, EmailVerificationEffect>(
    initialState = EmailVerificationUiState(
        userEmail = tokenStorage.current()?.user?.email.orEmpty(),
        isEmailVerified = tokenStorage.current()?.user?.emailVerified ?: false
    )
) {

    private var cooldownJob: Job? = null

    override fun onIntent(intent: EmailVerificationIntent) {
        when (intent) {
            EmailVerificationIntent.RequestVerification -> requestVerification()
            EmailVerificationIntent.DismissFeedback -> setState {
                copy(feedbackMessage = null, isError = false)
            }
            EmailVerificationIntent.RefreshStatus -> setState {
                copy(isEmailVerified = tokenStorage.current()?.user?.emailVerified ?: false)
            }
        }
    }

    private fun requestVerification() {
        val state = currentState
        if (state.isLoading || state.cooldownSeconds > 0 || state.isEmailVerified) return

        viewModelScope.launch {
            setState {
                copy(isLoading = true, isError = false, feedbackMessage = null, isAccepted = false)
            }
            when (val result = repository.requestEmailVerification()) {
                is AuthResult.Success -> {
                    setState {
                        copy(
                            isLoading = false,
                            isAccepted = true,
                            lastRequestId = result.data.requestId,
                            feedbackMessage = "Solicitud aceptada. Te enviamos un enlace de verificación con vigencia de 30 minutos.",
                            isError = false
                        )
                    }
                    startCooldown(60)
                }

                is AuthResult.Failure -> handleFailure(result.error.kind, result.error.message)
            }
        }
    }

    private fun handleFailure(kind: AuthErrorKind, message: String) {
        when (kind) {
            AuthErrorKind.CONFLICT -> {
                cooldownJob?.cancel()
                setState {
                    copy(
                        isLoading = false,
                        isEmailVerified = true,
                        isAccepted = false,
                        isError = false,
                        feedbackMessage = "Tu correo electrónico ya está verificado.",
                        cooldownSeconds = 0
                    )
                }
            }

            AuthErrorKind.RATE_LIMITED -> setState {
                copy(
                    isLoading = false,
                    isAccepted = false,
                    isError = true,
                    feedbackMessage = "Has alcanzado el límite de 3 solicitudes cada 15 minutos."
                )
            }

            else -> setState {
                copy(
                    isLoading = false,
                    isAccepted = false,
                    isError = true,
                    feedbackMessage = message.ifBlank { "No fue posible solicitar la verificación." }
                )
            }
        }
    }

    private fun startCooldown(seconds: Int) {
        cooldownJob?.cancel()
        setState { copy(cooldownSeconds = seconds) }
        cooldownJob = viewModelScope.launch {
            while (currentState.cooldownSeconds > 0) {
                delay(1000L)
                setState { copy(cooldownSeconds = (cooldownSeconds - 1).coerceAtLeast(0)) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        cooldownJob?.cancel()
    }
}
