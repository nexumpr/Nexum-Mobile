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

    fun loadPayments() {
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

            val today = java.time.LocalDate.now()
            val startDateStr = today.withDayOfMonth(1).toString()
            val nextDueDateStr = today.plusMonths(1).withDayOfMonth(1).toString()

            val request = CreateRecurringPaymentOleada2Request(
                name = name,
                amount = formattedAmount,
                currency = "MXN",
                frequency = frequency,
                nextDueDate = nextDueDateStr,
                type = type,
                startDate = startDateStr,
                dueDay = 1
            )
            val idempotencyKey = "android-rec-" + UUID.randomUUID().toString()
            when (val result = financialRepository.createRecurringPaymentOleada2(request, idempotencyKey)) {
                is AuthResult.Success -> {
                    setState { copy(isLoading = false) }
                    emitEffect(RecurringPaymentsEffect.ShowToast("Regla recurrente creada en servidor."))
                    loadPayments()
                }
                is AuthResult.Failure -> {
                    setState { copy(isLoading = false, errorMessage = result.error.message) }
                }
            }
        }
    }

    private fun recordPayment(paymentId: String, amountStr: String) {
        viewModelScope.launch {
            setState { copy(isLoading = true, errorMessage = null) }
            val parsedDouble = amountStr.replace("$", "").trim().toDoubleOrNull() ?: 0.0
            val formattedAmount = String.format(Locale.US, "%.2f", parsedDouble)
            val now = java.time.Instant.now().toString()
            val paidAt = if (now.contains(".")) now.substringBefore(".") + "Z" else now
            val idempotencyKey = "android-pay-" + UUID.randomUUID().toString()

            val targetId = if (paymentId.length < 36 || !paymentId.contains("-")) {
                val today = java.time.LocalDate.now()
                val startDateStr = today.withDayOfMonth(1).toString()
                val nextDueDateStr = today.plusMonths(1).withDayOfMonth(1).toString()

                val createRequest = CreateRecurringPaymentOleada2Request(
                    name = "Pago Programado",
                    amount = formattedAmount,
                    currency = "MXN",
                    frequency = "monthly",
                    nextDueDate = nextDueDateStr,
                    type = "expense",
                    startDate = startDateStr,
                    dueDay = 1
                )
                val createIdempotencyKey = "android-rec-" + UUID.randomUUID().toString()
                when (val createResult = financialRepository.createRecurringPaymentOleada2(createRequest, createIdempotencyKey)) {
                    is AuthResult.Success -> {
                        loadPayments()
                        createResult.data.id
                    }
                    is AuthResult.Failure -> {
                        setState { copy(isLoading = false, errorMessage = createResult.error.message) }
                        return@launch
                    }
                }
            } else {
                paymentId
            }

            when (val result = financialRepository.recordRecurringPaymentPaymentOleada3(targetId, paidAt, formattedAmount, "MXN", idempotencyKey)) {
                is AuthResult.Success -> {
                    setState { copy(isLoading = false) }
                    emitEffect(RecurringPaymentsEffect.ShowToast("Pago registrado con éxito (✓)."))
                    loadPayments()
                }
                is AuthResult.Failure -> {
                    setState { copy(isLoading = false, errorMessage = result.error.message) }
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
