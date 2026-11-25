package mx.nexum.app.financial.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mx.nexum.app.auth.data.AuthResult
import mx.nexum.app.core.mvi.MviViewModel
import mx.nexum.app.core.mvi.UiEffect
import mx.nexum.app.core.mvi.UiIntent
import mx.nexum.app.core.mvi.UiState
import mx.nexum.app.financial.data.FinancialRepository
import mx.nexum.app.financial.data.dto.CreateRecurringPaymentOleada2Request
import mx.nexum.app.financial.data.dto.RecurringPaymentOleada2Dto
import java.util.Locale
import java.util.UUID

data class RecurringPaymentsUiState(
    val payments: List<RecurringPaymentOleada2Dto> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : UiState

sealed interface RecurringPaymentsIntent : UiIntent {
    data object LoadPayments : RecurringPaymentsIntent
    data class CreatePayment(
        val name: String,
        val amount: String,
        val categoryId: String = "otros",
        val type: String = "expense",
        val frequency: String = "monthly"
    ) : RecurringPaymentsIntent
    data class RecordPayment(val paymentId: String, val amount: String) : RecurringPaymentsIntent
    data class DeletePayment(val paymentId: String) : RecurringPaymentsIntent
}

@Suppress("unused")
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
            is RecurringPaymentsIntent.CreatePayment -> createPayment(intent.name, intent.amount, intent.categoryId, intent.type, intent.frequency)
            is RecurringPaymentsIntent.RecordPayment -> recordPayment(intent.paymentId, intent.amount)
            is RecurringPaymentsIntent.DeletePayment -> deletePayment(intent.paymentId)
        }
    }

    private fun loadPayments() {
        viewModelScope.launch {
            setState { copy(isLoading = true, errorMessage = null) }
            when (val result = financialRepository.getRecurringPaymentsOleada2()) {
                is AuthResult.Success -> {
                    setState { copy(isLoading = false, payments = result.data) }
                }
                is AuthResult.Failure -> {
                    setState { copy(isLoading = false, errorMessage = result.error.message) }
                }
            }
        }
    }

    private fun createPayment(name: String, rawAmount: String, categoryId: String, type: String, frequency: String) {
        viewModelScope.launch {
            setState { copy(isLoading = true, errorMessage = null) }
            val parsedDouble = rawAmount.replace("$", "").trim().toDoubleOrNull() ?: 0.0
            val formattedAmount = String.format(Locale.US, "%.2f", parsedDouble)

            val request = CreateRecurringPaymentOleada2Request(
                name = name,
                amount = formattedAmount,
                currency = "MXN",
                frequency = frequency,
                nextDueDate = "2026-12-01",
                type = type,
                startDate = "2026-11-24"
            )
            val idempotencyKey = "android-rec-" + UUID.randomUUID().toString()
            when (val result = financialRepository.createRecurringPaymentOleada2(request, idempotencyKey)) {
                is AuthResult.Success -> {
                    emitEffect(RecurringPaymentsEffect.ShowToast("Regla recurrente creada en servidor."))
                    loadPayments()
                }
                is AuthResult.Failure -> {
                    setState { copy(isLoading = false, errorMessage = result.error.message) }
                }
            }
        }
    }

    private fun recordPayment(paymentId: String, amount: String) {
        viewModelScope.launch {
            val occurredAt = "2026-11-24T00:00:00Z"
            val idempotencyKey = "android-pay-" + UUID.randomUUID().toString()
            when (val result = financialRepository.recordRecurringPaymentPaymentOleada3(paymentId, occurredAt, amount, "MXN", idempotencyKey)) {
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
            when (val result = financialRepository.deleteRecurringPaymentOleada2(paymentId)) {
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
