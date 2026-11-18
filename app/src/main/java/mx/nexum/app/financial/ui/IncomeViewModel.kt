package mx.nexum.app.financial.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mx.nexum.app.auth.data.AuthResult
import mx.nexum.app.core.mvi.MviViewModel
import mx.nexum.app.core.mvi.UiEffect
import mx.nexum.app.core.mvi.UiIntent
import mx.nexum.app.core.mvi.UiState
import mx.nexum.app.financial.data.FinancialRepository

data class IncomeUiState(
    val amount: String = "",
    val frequency: String = "monthly",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
) : UiState

sealed interface IncomeIntent : UiIntent {
    data class AmountChanged(val amount: String) : IncomeIntent
    data class FrequencyChanged(val frequency: String) : IncomeIntent
    data object LoadIncome : IncomeIntent
    data object SubmitUpdate : IncomeIntent
}

sealed interface IncomeEffect : UiEffect {
    data class ShowToast(val message: String) : IncomeEffect
}

class IncomeViewModel(
    private val financialRepository: FinancialRepository
) : MviViewModel<IncomeUiState, IncomeIntent, IncomeEffect>(IncomeUiState()) {

    init {
        onIntent(IncomeIntent.LoadIncome)
    }

    override fun onIntent(intent: IncomeIntent) {
        when (intent) {
            is IncomeIntent.AmountChanged -> setState { copy(amount = intent.amount, errorMessage = null) }
            is IncomeIntent.FrequencyChanged -> setState { copy(frequency = intent.frequency) }
            IncomeIntent.LoadIncome -> loadIncome()
            IncomeIntent.SubmitUpdate -> updateIncome()
        }
    }

    private fun loadIncome() {
        viewModelScope.launch {
            setState { copy(isLoading = true, errorMessage = null) }
            when (val result = financialRepository.getIncome()) {
                is AuthResult.Success -> {
                    setState {
                        copy(
                            isLoading = false,
                            amount = result.data.amount.toString(),
                            frequency = result.data.frequency
                        )
                    }
                }
                is AuthResult.Failure -> {
                    setState { copy(isLoading = false, errorMessage = result.error.message) }
                }
            }
        }
    }

    private fun updateIncome() {
        val parsedAmount = currentState.amount.toDoubleOrNull()
        if (parsedAmount == null || parsedAmount < 0.0) {
            setState { copy(errorMessage = "Ingresa un monto válido.") }
            return
        }

        viewModelScope.launch {
            setState { copy(isLoading = true, errorMessage = null, successMessage = null) }
            when (val result = financialRepository.updateIncome(parsedAmount, currentState.frequency)) {
                is AuthResult.Success -> {
                    setState {
                        copy(
                            isLoading = false,
                            successMessage = "Ingreso actualizado exitosamente."
                        )
                    }
                    emitEffect(IncomeEffect.ShowToast("Ingreso actualizado exitosamente."))
                }
                is AuthResult.Failure -> {
                    setState { copy(isLoading = false, errorMessage = result.error.message) }
                }
            }
        }
    }
}
