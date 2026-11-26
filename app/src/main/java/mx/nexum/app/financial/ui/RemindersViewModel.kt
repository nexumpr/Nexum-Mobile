package mx.nexum.app.financial.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mx.nexum.app.auth.data.AuthResult
import mx.nexum.app.core.mvi.MviViewModel
import mx.nexum.app.core.mvi.UiEffect
import mx.nexum.app.core.mvi.UiIntent
import mx.nexum.app.core.mvi.UiState
import mx.nexum.app.financial.data.FinancialRepository
import mx.nexum.app.financial.data.dto.ReminderDto

data class RemindersUiState(
    val reminders: List<ReminderDto> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : UiState

sealed interface RemindersIntent : UiIntent {
    data object LoadReminders : RemindersIntent
    data class CreateReminder(val title: String, val dueAt: String, val frequency: String? = null) : RemindersIntent
    data class EditReminder(val reminderId: String, val version: Int, val title: String, val dueAt: String, val active: Boolean) : RemindersIntent
    data class DeleteReminder(val reminderId: String) : RemindersIntent
}

sealed interface RemindersEffect : UiEffect {
    data class ShowToast(val message: String) : RemindersEffect
}

@Suppress("unused")
class RemindersViewModel(
    private val financialRepository: FinancialRepository
) : MviViewModel<RemindersUiState, RemindersIntent, RemindersEffect>(RemindersUiState()) {

    var isLoaded = false
        private set

    override fun onIntent(intent: RemindersIntent) {
        when (intent) {
            RemindersIntent.LoadReminders -> loadReminders()
            is RemindersIntent.CreateReminder -> createReminder(intent.title, intent.dueAt, intent.frequency)
            is RemindersIntent.EditReminder -> editReminder(intent.reminderId, intent.version, intent.title, intent.dueAt, intent.active)
            is RemindersIntent.DeleteReminder -> deleteReminder(intent.reminderId)
        }
    }

    fun loadReminders() {
        isLoaded = true
        viewModelScope.launch {
            setState { copy(isLoading = true, errorMessage = null) }
            when (val result = financialRepository.getReminders()) {
                is AuthResult.Success -> {
                    setState { copy(isLoading = false, reminders = result.data.items) }
                }
                is AuthResult.Failure -> {
                    setState { copy(isLoading = false, errorMessage = result.error.message) }
                }
            }
        }
    }

    private fun createReminder(title: String, dueAt: String, frequency: String?) {
        viewModelScope.launch {
            setState { copy(isLoading = true, errorMessage = null) }
            when (val result = financialRepository.createReminder(title, dueAt, "America/Mexico_City", frequency)) {
                is AuthResult.Success -> {
                    setState { copy(isLoading = false) }
                    emitEffect(RemindersEffect.ShowToast("Recordatorio creado en servidor."))
                    loadReminders()
                }
                is AuthResult.Failure -> {
                    setState { copy(isLoading = false, errorMessage = result.error.message) }
                }
            }
        }
    }

    private fun editReminder(reminderId: String, version: Int, title: String, dueAt: String, active: Boolean) {
        viewModelScope.launch {
            setState { copy(isLoading = true, errorMessage = null) }
            val ifMatch = "\"v$version\""
            when (val result = financialRepository.updateReminder(reminderId, ifMatch, title, dueAt, active)) {
                is AuthResult.Success -> {
                    setState { copy(isLoading = false) }
                    emitEffect(RemindersEffect.ShowToast("Recordatorio actualizado (E066)."))
                    loadReminders()
                }
                is AuthResult.Failure -> {
                    setState { copy(isLoading = false, errorMessage = result.error.message) }
                }
            }
        }
    }

    private fun deleteReminder(reminderId: String) {
        viewModelScope.launch {
            setState { copy(isLoading = true, errorMessage = null) }
            when (val result = financialRepository.deleteReminder(reminderId)) {
                is AuthResult.Success -> {
                    setState { copy(isLoading = false) }
                    emitEffect(RemindersEffect.ShowToast("Recordatorio eliminado (E067)."))
                    loadReminders()
                }
                is AuthResult.Failure -> {
                    setState { copy(isLoading = false, errorMessage = result.error.message) }
                }
            }
        }
    }
}
