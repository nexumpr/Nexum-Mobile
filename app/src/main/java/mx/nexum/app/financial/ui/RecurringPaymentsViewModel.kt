package mx.nexum.app.financial.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mx.nexum.app.auth.data.AuthResult
import mx.nexum.app.core.mvi.MviViewModel
import mx.nexum.app.core.mvi.UiEffect
import mx.nexum.app.core.mvi.UiIntent
import mx.nexum.app.core.mvi.UiState
import mx.nexum.app.financial.data.FinancialRepository
import mx.nexum.app.financial.data.dto.RecurringPaymentDto
import java.util.UUID

data class RecurringPaymentsUiState(
    val payments: List<RecurringPaymentDto> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : UiState

sealed interface RecurringPaymentsIntent : UiIntent {
    data object LoadPayments : RecurringPaymentsIntent
    data class RecordPayment(val paymentId: String, val amount: Double?) : RecurringPaymentsIntent
    data class DeletePayment(val paymentId: String) : RecurringPaymentsIntent
}

sealed interface RecurringPaymentsEffect : UiEffect {
    data class ShowToast(val message: String) : RecurringPaymentsEffect
}

@Suppress("unused")
class RecurringPaymentsViewModel(
    private val financialRepository: FinancialRepository
) : MviViewModel<RecurringPaymentsUiState, RecurringPaymentsIntent, RecurringPaymentsEffect>(RecurringPaymentsUiState()) {

    init {
        onIntent(RecurringPaymentsIntent.LoadPayments)
    }

    override fun onIntent(intent: RecurringPaymentsIntent) {
        when (intent) {
            RecurringPaymentsIntent.LoadPayments -> loadPayments()
            is RecurringPaymentsIntent.RecordPayment -> recordPayment(intent.paymentId, intent.amount)
            is RecurringPaymentsIntent.DeletePayment -> deletePayment(intent.paymentId)
        }
    }

    private fun loadPayments() {
        viewModelScope.launch {
            setState { copy(isLoading = true, errorMessage = null) }
            when (val result = financialRepository.getRecurringPayments()) {
                is AuthResult.Success -> {
                    setState { copy(isLoading = false, payments = result.data) }
                }
                is AuthResult.Failure -> {
                    setState { copy(isLoading = false, errorMessage = result.error.message) }
                }
            }
        }
    }

    private fun recordPayment(paymentId: String, amount: Double?) {
        viewModelScope.launch {
            val idempotencyKey = UUID.randomUUID().toString()
            val occurredAt = "2026-11-18T00:00:00Z"
            when (val result = financialRepository.recordRecurringPaymentPayment(paymentId, occurredAt, amount, idempotencyKey)) {
                is AuthResult.Success -> {
                    emitEffect(RecurringPaymentsEffect.ShowToast("Pago registrado con éxito (✓)."))
                    loadPayments()
                }
                is AuthResult.Failure -> {
                    setState { copy(errorMessage = result.error.message) }
                }
            }
        }
    }

    private fun deletePayment(paymentId: String) {
        viewModelScope.launch {
            when (val result = financialRepository.deleteRecurringPayment(paymentId)) {
                is AuthResult.Success -> {
                    emitEffect(RecurringPaymentsEffect.ShowToast("Programación desactivada."))
                    loadPayments()
                }
                is AuthResult.Failure -> {
                    setState { copy(errorMessage = result.error.message) }
                }
            }
        }
    }
}
