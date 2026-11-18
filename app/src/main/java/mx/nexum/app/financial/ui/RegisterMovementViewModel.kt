package mx.nexum.app.financial.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mx.nexum.app.auth.data.AuthResult
import mx.nexum.app.core.mvi.MviViewModel
import mx.nexum.app.core.mvi.UiEffect
import mx.nexum.app.core.mvi.UiIntent
import mx.nexum.app.core.mvi.UiState
import mx.nexum.app.financial.data.FinancialRepository
import mx.nexum.app.financial.data.dto.CreateMovementRequest

data class RegisterMovementUiState(
    val concept: String = "",
    val amount: String = "",
    val categoryId: String = "general",
    val type: String = "expense",
    val frequency: String? = null,
    val goalId: String? = null,
    val occurredAt: String = "2026-11-18T00:00:00Z",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val success: Boolean = false
) : UiState

sealed interface RegisterMovementIntent : UiIntent {
    data class ConceptChanged(val concept: String) : RegisterMovementIntent
    data class AmountChanged(val amount: String) : RegisterMovementIntent
    data class CategoryChanged(val categoryId: String) : RegisterMovementIntent
    data class TypeChanged(val type: String) : RegisterMovementIntent
    data object SubmitMovement : RegisterMovementIntent
}

sealed interface RegisterMovementEffect : UiEffect {
    data class ShowToast(val message: String) : RegisterMovementEffect
    data object NavigateBack : RegisterMovementEffect
}

@Suppress("unused")
class RegisterMovementViewModel(
    private val financialRepository: FinancialRepository
) : MviViewModel<RegisterMovementUiState, RegisterMovementIntent, RegisterMovementEffect>(RegisterMovementUiState()) {

    override fun onIntent(intent: RegisterMovementIntent) {
        when (intent) {
            is RegisterMovementIntent.ConceptChanged -> setState { copy(concept = intent.concept, errorMessage = null) }
            is RegisterMovementIntent.AmountChanged -> setState { copy(amount = intent.amount, errorMessage = null) }
            is RegisterMovementIntent.CategoryChanged -> setState { copy(categoryId = intent.categoryId) }
            is RegisterMovementIntent.TypeChanged -> setState { copy(type = intent.type) }
            RegisterMovementIntent.SubmitMovement -> submitMovement()
        }
    }

    private fun submitMovement() {
        val parsedAmount = currentState.amount.toDoubleOrNull()
        if (currentState.concept.isBlank() || parsedAmount == null || parsedAmount <= 0.0) {
            setState { copy(errorMessage = "Por favor ingresa un concepto y un monto válido.") }
            return
        }

        viewModelScope.launch {
            setState { copy(isLoading = true, errorMessage = null) }
            val request = CreateMovementRequest(
                concept = currentState.concept,
                amount = parsedAmount,
                categoryId = currentState.categoryId,
                type = currentState.type,
                frequency = currentState.frequency,
                goalId = currentState.goalId,
                occurredAt = currentState.occurredAt
            )
            when (val result = financialRepository.createMovement(request)) {
                is AuthResult.Success -> {
                    setState { copy(isLoading = false, success = true) }
                    emitEffect(RegisterMovementEffect.ShowToast("Movimiento registrado con éxito."))
                    emitEffect(RegisterMovementEffect.NavigateBack)
                }
                is AuthResult.Failure -> {
                    setState { copy(isLoading = false, errorMessage = result.error.message) }
                }
            }
        }
    }
}
