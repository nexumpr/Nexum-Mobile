package mx.nexum.app.financial.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mx.nexum.app.auth.data.AuthResult
import mx.nexum.app.core.mvi.MviViewModel
import mx.nexum.app.core.mvi.UiEffect
import mx.nexum.app.core.mvi.UiIntent
import mx.nexum.app.core.mvi.UiState
import mx.nexum.app.financial.data.FinancialRepository
import mx.nexum.app.financial.data.dto.BudgetUsageDto

data class BudgetsUiState(
    val period: String = "2026-11",
    val budgetUsageItems: List<BudgetUsageDto> = emptyList(),
    val etag: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
) : UiState

sealed interface BudgetsIntent : UiIntent {
    data object LoadBudgets : BudgetsIntent
    data class PeriodChanged(val period: String) : BudgetsIntent
}

@Suppress("unused")
sealed interface BudgetsEffect : UiEffect {
    data class ShowToast(val message: String) : BudgetsEffect
}

@Suppress("unused")
class BudgetsViewModel(
    private val financialRepository: FinancialRepository
) : MviViewModel<BudgetsUiState, BudgetsIntent, BudgetsEffect>(BudgetsUiState()) {

    init {
        onIntent(BudgetsIntent.LoadBudgets)
    }

    override fun onIntent(intent: BudgetsIntent) {
        when (intent) {
            BudgetsIntent.LoadBudgets -> loadBudgets()
            is BudgetsIntent.PeriodChanged -> {
                setState { copy(period = intent.period) }
                loadBudgets()
            }
        }
    }

    private fun loadBudgets() {
        viewModelScope.launch {
            setState { copy(isLoading = true, errorMessage = null) }
            val usageResult = financialRepository.getBudgetUsage(currentState.period)
            val budgetsResult = financialRepository.getBudgets(period = currentState.period)

            val usageItems = (usageResult as? AuthResult.Success)?.data?.items ?: emptyList()
            val etag = (budgetsResult as? AuthResult.Success)?.data?.second

            setState {
                copy(
                    isLoading = false,
                    budgetUsageItems = usageItems,
                    etag = etag
                )
            }
        }
    }
}
