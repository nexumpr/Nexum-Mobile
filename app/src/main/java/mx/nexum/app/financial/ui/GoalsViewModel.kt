package mx.nexum.app.financial.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mx.nexum.app.auth.data.AuthResult
import mx.nexum.app.core.mvi.MviViewModel
import mx.nexum.app.core.mvi.UiEffect
import mx.nexum.app.core.mvi.UiIntent
import mx.nexum.app.core.mvi.UiState
import mx.nexum.app.financial.data.FinancialRepository
import mx.nexum.app.financial.data.dto.CreateGoalOleada2Request
import mx.nexum.app.financial.data.dto.GoalOleada2Dto
import mx.nexum.app.financial.data.dto.GoalTotalsDto
import java.util.Locale
import java.util.UUID

data class GoalsUiState(
    val goals: List<GoalOleada2Dto> = emptyList(),
    val totals: GoalTotalsDto? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isCreating: Boolean = false
) : UiState

sealed interface GoalsIntent : UiIntent {
    data object LoadGoals : GoalsIntent
    data class CreateGoal(val name: String, val targetAmount: String, val initialSaved: String, val targetDate: String) : GoalsIntent
}

@Suppress("unused")
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
            is GoalsIntent.CreateGoal -> createGoal(intent.name, intent.targetAmount, intent.initialSaved, intent.targetDate)
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
            when (val result = financialRepository.getGoalsOleada2()) {
                is AuthResult.Success -> {
                    setState { copy(isLoading = false, goals = result.data, totals = null) }
                }
                is AuthResult.Failure -> {
                    setState { copy(isLoading = false, errorMessage = result.error.message) }
                }
            }
        }
    }

    private fun createGoal(name: String, rawTarget: String, rawInitial: String, targetDate: String) {
        val targetVal = rawTarget.replace("$", "").trim().toDoubleOrNull() ?: 0.0
        if (name.isBlank() || targetVal <= 0.0) {
            setState { copy(errorMessage = "Nombre y monto objetivo válidos requeridos.") }
            return
        }

        val formattedTarget = String.format(Locale.US, "%.2f", targetVal)
        val initialVal = rawInitial.replace("$", "").trim().toDoubleOrNull() ?: 0.0
        val formattedInitial = String.format(Locale.US, "%.2f", initialVal)

        viewModelScope.launch {
            setState { copy(isCreating = true, errorMessage = null) }
            val request = CreateGoalOleada2Request(
                name = name,
                targetAmount = formattedTarget,
                currency = "MXN",
                targetDate = targetDate,
                initialSavedAmount = formattedInitial
            )
            val idempotencyKey = "android-goal-" + UUID.randomUUID().toString()
            when (val result = financialRepository.createGoalOleada2(request, idempotencyKey)) {
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
