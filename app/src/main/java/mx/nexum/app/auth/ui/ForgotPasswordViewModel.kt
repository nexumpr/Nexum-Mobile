package mx.nexum.app.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
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
import mx.nexum.app.auth.domain.AuthValidator

class ForgotPasswordViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val mutableState = MutableStateFlow(ForgotPasswordUiState())
    val state: StateFlow<ForgotPasswordUiState> = mutableState.asStateFlow()

    private val mutableEffects = MutableSharedFlow<AuthEffect>(
        replay = 0,
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effects: SharedFlow<AuthEffect> = mutableEffects.asSharedFlow()

    private var cooldownJob: Job? = null

    fun onIntent(intent: ForgotPasswordIntent) {
        when (intent) {
            is ForgotPasswordIntent.EmailChanged -> mutableState.update {
                it.copy(email = intent.value, emailError = null)
            }
            ForgotPasswordIntent.Submit -> submit()
            ForgotPasswordIntent.NavigateToLogin -> mutableEffects.tryEmit(AuthEffect.NavigateToLogin)
            ForgotPasswordIntent.NavigateToResetPassword -> {
                val currentEmail = mutableState.value.email.trim()
                mutableEffects.tryEmit(AuthEffect.NavigateToResetPassword(currentEmail))
            }
        }
    }

    private fun submit() {
        val current = mutableState.value
        if (current.isLoading || current.cooldownSeconds > 0) return

        val emailError = AuthValidator.emailError(current.email)
        if (emailError != null) {
            mutableState.update { it.copy(emailError = emailError) }
            return
        }

        mutableState.update {
            it.copy(
                isLoading = true,
                emailError = null
            )
        }

        viewModelScope.launch {
            val emailToSend = current.email.trim().lowercase()
            when (val result = repository.requestPasswordReset(emailToSend)) {
                is AuthResult.Success -> {
                    mutableState.update {
                        it.copy(
                            isLoading = false,
                            isSubmitted = true,
                            requestId = result.data.requestId,
                            feedbackMessage = result.data.message
                        )
                    }
                    startCooldown(60)
                }
                is AuthResult.Failure -> {
                    val error = result.error
                    val cooldown = if (error.kind == AuthErrorKind.RATE_LIMITED) {
                        (error.retryAfterSeconds ?: 60).coerceAtLeast(1)
                    } else 0

                    mutableState.update {
                        it.copy(
                            isLoading = false,
                            feedbackMessage = if (error.kind == AuthErrorKind.RATE_LIMITED) error.message else it.feedbackMessage
                        )
                    }

                    if (cooldown > 0) {
                        startCooldown(cooldown)
                    }

                    mutableEffects.tryEmit(
                        AuthEffect.ShowSnackbar(
                            message = error.message,
                            retry = error.retryable && error.kind != AuthErrorKind.RATE_LIMITED
                        )
                    )
                }
            }
        }
    }

    private fun startCooldown(seconds: Int) {
        cooldownJob?.cancel()
        mutableState.update { it.copy(cooldownSeconds = seconds) }
        cooldownJob = viewModelScope.launch {
            while (mutableState.value.cooldownSeconds > 0) {
                delay(1000L)
                mutableState.update { it.copy(cooldownSeconds = (it.cooldownSeconds - 1).coerceAtLeast(0)) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        cooldownJob?.cancel()
    }
}
