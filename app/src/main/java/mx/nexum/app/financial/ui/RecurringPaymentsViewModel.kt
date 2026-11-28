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
import mx.nexum.app.financial.data.dto.CreateRecurringPaymentOleada2Request
import mx.nexum.app.financial.data.dto.RecurringPaymentOleada2Dto
import mx.nexum.app.financial.data.dto.UpdateRecurringPaymentOleada2Request
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
        val startDate: String = "2026-11-01",
        val nextDueDate: String = "2026-12-01",
        val categoryId: String = "otros",
        val type: String = "expense",
        val frequency: String = "monthly"
    ) : RecurringPaymentsIntent
    data class EditPayment(
        val paymentId: String,
        val name: String,
        val amount: String,
        val nextDueDate: String,
        val version: Int
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
            is RecurringPaymentsIntent.CreatePayment -> createPayment(
                intent.name,
                intent.amount,
                intent.startDate,
                intent.nextDueDate,
                intent.categoryId,
                intent.type,
                intent.frequency
            )
            is RecurringPaymentsIntent.EditPayment -> editPayment(
                intent.paymentId,
                intent.name,
                intent.amount,
                intent.nextDueDate,
                intent.version
            )
            is RecurringPaymentsIntent.RecordPayment -> recordPayment(intent.paymentId, intent.amount)
            is RecurringPaymentsIntent.DeletePayment -> deletePayment(intent.paymentId)
        }
    }

    fun loadPayments() {
        launchSafely(
            onError = {
                setState {
                    copy(
                        isLoading = false,
                        errorMessage = "No fue posible cargar los pagos recurrentes."
                    )
                }
            }
        ) {
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

    private fun createPayment(
        name: String,
        rawAmount: String,
        customStartDate: String,
        customNextDueDate: String,
        categoryId: String,
        type: String,
        frequency: String
    ) {
        viewModelScope.launch {
            setState { copy(isLoading = true, errorMessage = null) }
            val parsedDouble = rawAmount.replace("$", "").trim().toDoubleOrNull() ?: 0.0
            val formattedAmount = String.format(Locale.US, "%.2f", parsedDouble)

            val today = java.time.LocalDate.now()
            val defaultStartDate = today.withDayOfMonth(1).toString()
            val defaultNextDueDate = today.plusMonths(1).withDayOfMonth(1).toString()

            val finalStartDate = customStartDate.ifBlank { defaultStartDate }
            val finalNextDueDate = customNextDueDate.ifBlank { defaultNextDueDate }
            val extractedDueDay = finalNextDueDate.split("-").lastOrNull()?.toIntOrNull() ?: 1

            val request = CreateRecurringPaymentOleada2Request(
                name = name,
                amount = formattedAmount,
                currency = "MXN",
                frequency = frequency,
                nextDueDate = finalNextDueDate,
                type = type,
                startDate = finalStartDate,
                dueDay = extractedDueDay
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

    private fun editPayment(
        paymentId: String,
        name: String,
        rawAmount: String,
        nextDueDate: String,
        version: Int
    ) {
        viewModelScope.launch {
            setState { copy(isLoading = true, errorMessage = null) }
            val parsedDouble = rawAmount.replace("$", "").trim().toDoubleOrNull() ?: 0.0
            val formattedAmount = String.format(Locale.US, "%.2f", parsedDouble)

            val request = UpdateRecurringPaymentOleada2Request(
                name = name,
                amount = formattedAmount,
                nextDueDate = nextDueDate
            )
            val ifMatch = "\"v$version\""
            when (val result = financialRepository.updateRecurringPaymentOleada2(paymentId, ifMatch, request)) {
                is AuthResult.Success -> {
                    setState { copy(isLoading = false) }
                    emitEffect(RecurringPaymentsEffect.ShowToast("Pago recurrente actualizado (E035)."))
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

            val targetPayment = currentState.payments.firstOrNull { it.id == paymentId }
            val descriptionName = targetPayment?.name ?: "Pago Recurrente"
            val paymentType = targetPayment?.type ?: "expense"

            // Directly record the payment as an expense movement in movements
            val request = CreateMovementOleada2Request(
                type = paymentType,
                amount = formattedAmount,
                currency = "MXN",
                categoryId = "otros",
                description = "Pago: $descriptionName",
                occurredAt = java.time.Instant.now().toString()
            )
            val idempotencyKey = "android-mov-" + UUID.randomUUID().toString()
            when (val result = financialRepository.createMovementOleada2(request, idempotencyKey)) {
                is AuthResult.Success -> {
                    setState { copy(isLoading = false) }
                    emitEffect(RecurringPaymentsEffect.ShowToast("Pago registrado exitosamente en movimientos (✓)."))
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
            setState { copy(isLoading = true, errorMessage = null) }
            if (paymentId.length < 36 || !paymentId.contains("-")) {
                // Local mock item removal
                setState {
                    copy(
                        isLoading = false,
                        payments = payments.filter { it.id != paymentId }
                    )
                }
                emitEffect(RecurringPaymentsEffect.ShowToast("Programación desactivada."))
                return@launch
            }

            when (val result = financialRepository.deleteRecurringPaymentOleada2(paymentId)) {
                is AuthResult.Success -> {
                    setState { copy(isLoading = false) }
                    emitEffect(RecurringPaymentsEffect.ShowToast("Programación desactivada."))
                    loadPayments()
                }
                is AuthResult.Failure -> {
                    setState { copy(isLoading = false, errorMessage = result.error.message) }
                }
            }
        }
    }
}
