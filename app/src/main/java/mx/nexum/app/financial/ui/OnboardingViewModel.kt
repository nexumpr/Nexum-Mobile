package mx.nexum.app.financial.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mx.nexum.app.auth.data.AuthResult
import mx.nexum.app.core.mvi.MviViewModel
import mx.nexum.app.core.mvi.UiEffect
import mx.nexum.app.core.mvi.UiIntent
import mx.nexum.app.core.mvi.UiState
import mx.nexum.app.financial.data.FinancialRepository

data class OnboardingUiState(
    val stepsCompleted: List<String> = emptyList(),
    val finishedAt: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isCompleted: Boolean = false
) : UiState

sealed interface OnboardingIntent : UiIntent {
    data object CheckStatus : OnboardingIntent
    data class Complete(val limitedMode: Boolean) : OnboardingIntent
}

@Suppress("unused")
sealed interface OnboardingEffect : UiEffect {
    data object NavigateToHome : OnboardingEffect
    data class ShowToast(val message: String) : OnboardingEffect
}

@Suppress("unused")
class OnboardingViewModel(
    private val financialRepository: FinancialRepository
) : MviViewModel<OnboardingUiState, OnboardingIntent, OnboardingEffect>(OnboardingUiState()) {

    init {
        onIntent(OnboardingIntent.CheckStatus)
    }

    override fun onIntent(intent: OnboardingIntent) {
        when (intent) {
            OnboardingIntent.CheckStatus -> checkStatus()
            is OnboardingIntent.Complete -> completeOnboarding(intent.limitedMode)
        }
    }

    private fun checkStatus() {
        viewModelScope.launch {
            setState { copy(isLoading = true, errorMessage = null) }
            when (val result = financialRepository.getOnboardingStatus()) {
                is AuthResult.Success -> {
                    val finished = !result.data.finishedAt.isNullOrBlank()
                    setState {
                        copy(
                            isLoading = false,
                            stepsCompleted = result.data.stepsCompleted,
                            finishedAt = result.data.finishedAt,
                            isCompleted = finished
                        )
                    }
                    if (finished) {
                        emitEffect(OnboardingEffect.NavigateToHome)
                    }
                }
                is AuthResult.Failure -> {
                    setState { copy(isLoading = false, errorMessage = result.error.message) }
                }
            }
        }
    }

    private fun completeOnboarding(limitedMode: Boolean) {
        viewModelScope.launch {
            setState { copy(isLoading = true, errorMessage = null) }
            when (val result = financialRepository.completeOnboarding(limitedMode)) {
                is AuthResult.Success -> {
                    setState {
                        copy(
                            isLoading = false,
                            isCompleted = true,
                            finishedAt = result.data.finishedAt
                        )
                    }
                    emitEffect(OnboardingEffect.ShowToast("¡Onboarding completado con éxito!"))
                    emitEffect(OnboardingEffect.NavigateToHome)
                }
                is AuthResult.Failure -> {
                    setState { copy(isLoading = false, errorMessage = result.error.message) }
                }
            }
        }
    }
}
