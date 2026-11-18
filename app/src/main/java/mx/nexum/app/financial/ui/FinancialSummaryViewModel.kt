package mx.nexum.app.financial.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mx.nexum.app.auth.data.AuthResult
import mx.nexum.app.core.mvi.MviViewModel
import mx.nexum.app.core.mvi.UiEffect
import mx.nexum.app.core.mvi.UiIntent
import mx.nexum.app.core.mvi.UiState
import mx.nexum.app.financial.data.FinancialRepository
import mx.nexum.app.financial.data.dto.FinancialSummaryResponse

data class FinancialSummaryUiState(
    val summary: FinancialSummaryResponse? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : UiState

sealed interface FinancialSummaryIntent : UiIntent {
    data object LoadSummary : FinancialSummaryIntent
}

@Suppress("unused")
sealed interface FinancialSummaryEffect : UiEffect {
    data class ShowToast(val message: String) : FinancialSummaryEffect
}

@Suppress("unused")
class FinancialSummaryViewModel(
    private val financialRepository: FinancialRepository
) : MviViewModel<FinancialSummaryUiState, FinancialSummaryIntent, FinancialSummaryEffect>(FinancialSummaryUiState()) {

    init {
        onIntent(FinancialSummaryIntent.LoadSummary)
    }

    override fun onIntent(intent: FinancialSummaryIntent) {
        when (intent) {
            FinancialSummaryIntent.LoadSummary -> loadSummary()
        }
    }

    private fun loadSummary() {
        viewModelScope.launch {
            setState { copy(isLoading = true, errorMessage = null) }
            when (val result = financialRepository.getFinancialSummary()) {
                is AuthResult.Success -> {
                    setState { copy(isLoading = false, summary = result.data) }
                }
                is AuthResult.Failure -> {
                    setState { copy(isLoading = false, errorMessage = result.error.message) }
                }
            }
        }
    }
}
