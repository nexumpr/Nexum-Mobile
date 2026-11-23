package mx.nexum.app.financial.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mx.nexum.app.auth.data.AuthResult
import mx.nexum.app.core.mvi.MviViewModel
import mx.nexum.app.core.mvi.UiEffect
import mx.nexum.app.core.mvi.UiIntent
import mx.nexum.app.core.mvi.UiState
import mx.nexum.app.financial.data.FinancialRepository
import mx.nexum.app.financial.data.dto.CreateMovementOleada2Request
import mx.nexum.app.financial.data.dto.MovementOleada2Dto

data class MovementsListUiState(
    val movements: List<MovementOleada2Dto> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : UiState

sealed interface MovementsListIntent : UiIntent {
    data object LoadMovements : MovementsListIntent
    data class CreateMovement(
        val description: String,
        val amount: String,
        val categoryId: String,
        val type: String
    ) : MovementsListIntent
    data class DeleteMovement(val movementId: String) : MovementsListIntent
}

sealed interface MovementsListEffect : UiEffect {
    data class ShowToast(val message: String) : MovementsListEffect
}

@Suppress("unused")
class MovementsListViewModel(
    private val financialRepository: FinancialRepository
) : MviViewModel<MovementsListUiState, MovementsListIntent, MovementsListEffect>(MovementsListUiState()) {

    init {
        onIntent(MovementsListIntent.LoadMovements)
    }

    override fun onIntent(intent: MovementsListIntent) {
        when (intent) {
            MovementsListIntent.LoadMovements -> loadMovements()
            is MovementsListIntent.CreateMovement -> createMovement(intent.description, intent.amount, intent.categoryId, intent.type)
            is MovementsListIntent.DeleteMovement -> deleteMovement(intent.movementId)
        }
    }

    private fun loadMovements() {
        viewModelScope.launch {
            setState { copy(isLoading = true, errorMessage = null) }
            when (val result = financialRepository.getMovementsOleada2()) {
                is AuthResult.Success -> {
                    setState { copy(isLoading = false, movements = result.data) }
                }
                is AuthResult.Failure -> {
                    setState { copy(isLoading = false, errorMessage = result.error.message) }
                }
            }
        }
    }

    private fun createMovement(description: String, amount: String, categoryId: String, type: String) {
        viewModelScope.launch {
            setState { copy(isLoading = true, errorMessage = null) }
            val request = CreateMovementOleada2Request(
                type = type,
                amount = amount,
                currency = "MXN",
                categoryId = categoryId.ifBlank { "otros" },
                description = description,
                occurredAt = "2026-11-23T10:00:00Z"
            )
            when (val result = financialRepository.createMovementOleada2(request)) {
                is AuthResult.Success -> {
                    emitEffect(MovementsListEffect.ShowToast("Movimiento registrado en servidor."))
                    loadMovements()
                }
                is AuthResult.Failure -> {
                    setState { copy(isLoading = false, errorMessage = result.error.message) }
                }
            }
        }
    }

    private fun deleteMovement(movementId: String) {
        viewModelScope.launch {
            when (val result = financialRepository.deleteMovementOleada2(movementId)) {
                is AuthResult.Success -> {
                    emitEffect(MovementsListEffect.ShowToast("Movimiento eliminado exitosamente."))
                    loadMovements()
                }
                is AuthResult.Failure -> {
                    setState { copy(errorMessage = result.error.message) }
                }
            }
        }
    }
}
