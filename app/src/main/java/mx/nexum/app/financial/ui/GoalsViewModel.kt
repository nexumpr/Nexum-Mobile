package mx.nexum.app.financial.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mx.nexum.app.auth.data.AuthResult
import mx.nexum.app.core.mvi.MviViewModel
import mx.nexum.app.core.mvi.UiEffect
import mx.nexum.app.core.mvi.UiIntent
import mx.nexum.app.core.mvi.UiState
import mx.nexum.app.financial.data.FinancialRepository
import mx.nexum.app.financial.data.dto.CreateGoalRequest
import mx.nexum.app.financial.data.dto.GoalDto

data class GoalsUiState(
    val goals: List<GoalDto> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isCreating: Boolean = false
) : UiState

sealed interface GoalsIntent : UiIntent {
    data object LoadGoals : GoalsIntent
    data class CreateGoal(val name: String, val targetAmount: Double, val initialSaved: Double, val emoji: String?, val targetDate: String?) : GoalsIntent
}

sealed interface GoalsEffect : UiEffect {
    data class ShowToast(val message: String) : GoalsEffect
}

@Suppress("unused")
class GoalsViewModel(
    private val financialRepository: FinancialRepository
) : MviViewModel<GoalsUiState, GoalsIntent, GoalsEffect>(GoalsUiState()) {

    init {
        onIntent(GoalsIntent.LoadGoals)
    }

    override fun onIntent(intent: GoalsIntent) {
        when (intent) {
            GoalsIntent.LoadGoals -> loadGoals()
            is GoalsIntent.CreateGoal -> createGoal(intent.name, intent.targetAmount, intent.initialSaved, intent.emoji, intent.targetDate)
        }
    }

    private fun loadGoals() {
        launchSafely(
            onError = {
                setState {
                    copy(
                        isLoading = false,
                        errorMessage = "No fue posible cargar las metas."
                    )
                }
            }
        ) {
            setState { copy(isLoading = true, errorMessage = null) }
            when (val result = financialRepository.getGoals()) {
                is AuthResult.Success -> {
                    setState { copy(isLoading = false, goals = result.data) }
                }
                is AuthResult.Failure -> {
                    setState { copy(isLoading = false, errorMessage = result.error.message) }
                }
            }
        }
    }

    private fun createGoal(name: String, targetAmount: Double, initialSaved: Double, emoji: String?, targetDate: String?) {
        if (name.isBlank() || targetAmount <= 0.0) {
            setState { copy(errorMessage = "Nombre y monto objetivo válidos requeridos.") }
            return
        }

        viewModelScope.launch {
            setState { copy(isCreating = true, errorMessage = null) }
            val request = CreateGoalRequest(name, targetAmount, initialSaved, emoji, targetDate)
            when (val result = financialRepository.createGoal(request)) {
                is AuthResult.Success -> {
                    setState { copy(isCreating = false) }
                    emitEffect(GoalsEffect.ShowToast("Meta creada con éxito."))
                    loadGoals()
                }
                is AuthResult.Failure -> {
                    setState { copy(isCreating = false, errorMessage = result.error.message) }
                }
            }
        }
    }
}
