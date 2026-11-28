package mx.nexum.app.financial.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mx.nexum.app.auth.data.AuthResult
import mx.nexum.app.core.mvi.MviViewModel
import mx.nexum.app.core.mvi.UiEffect
import mx.nexum.app.core.mvi.UiIntent
import mx.nexum.app.core.mvi.UiState
import mx.nexum.app.financial.data.FinancialRepository
import mx.nexum.app.financial.data.dto.NotificationDto
import mx.nexum.app.financial.data.dto.NotificationPreferencesDto
import mx.nexum.app.financial.data.dto.UpdateNotificationPreferencesRequest

data class NotificationsUiState(
    val notifications: List<NotificationDto> = emptyList(),
    val unreadCount: Int = 0,
    val preferences: NotificationPreferencesDto? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : UiState

sealed interface NotificationsIntent : UiIntent {
    data object LoadNotifications : NotificationsIntent
    data class MarkRead(val notificationId: String) : NotificationsIntent
    data object MarkAllRead : NotificationsIntent
    data class RegisterPushToken(val fcmToken: String) : NotificationsIntent
    data class UpdatePreferences(val pushEnabled: Boolean, val emailEnabled: Boolean) : NotificationsIntent
}

sealed interface NotificationsEffect : UiEffect {
    data class ShowToast(val message: String) : NotificationsEffect
}

@Suppress("unused")
class NotificationsViewModel(
    private val financialRepository: FinancialRepository
) : MviViewModel<NotificationsUiState, NotificationsIntent, NotificationsEffect>(NotificationsUiState()) {

    var isLoaded = false
        private set

    override fun onIntent(intent: NotificationsIntent) {
        when (intent) {
            NotificationsIntent.LoadNotifications -> loadNotifications()
            is NotificationsIntent.MarkRead -> markRead(intent.notificationId)
            NotificationsIntent.MarkAllRead -> markAllRead()
            is NotificationsIntent.RegisterPushToken -> registerPushToken(intent.fcmToken)
            is NotificationsIntent.UpdatePreferences -> updatePreferences(intent.pushEnabled, intent.emailEnabled)
        }
    }

    fun loadNotifications() {
        isLoaded = true
        viewModelScope.launch {
            setState { copy(isLoading = true, errorMessage = null) }
            val notifsRes = financialRepository.getNotifications()
            val prefsRes = financialRepository.getNotificationPreferences()

            val items = (notifsRes as? AuthResult.Success)?.data?.items ?: emptyList()
            val unread = (notifsRes as? AuthResult.Success)?.data?.unreadCount ?: 0
            val prefs = (prefsRes as? AuthResult.Success)?.data

            setState {
                copy(
                    isLoading = false,
                    notifications = items,
                    unreadCount = unread,
                    preferences = prefs
                )
            }
        }
    }

    private fun markRead(notificationId: String) {
        viewModelScope.launch {
            when (financialRepository.markNotificationRead(notificationId)) {
                is AuthResult.Success -> {
                    emitEffect(NotificationsEffect.ShowToast("Notificación leída."))
                    loadNotifications()
                }
                is AuthResult.Failure -> {}
            }
        }
    }

    private fun markAllRead() {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            when (financialRepository.markAllNotificationsRead()) {
                is AuthResult.Success -> {
                    setState { copy(isLoading = false) }
                    emitEffect(NotificationsEffect.ShowToast("Todas las notificaciones leídas (E063)."))
                    loadNotifications()
                }
                is AuthResult.Failure -> {
                    setState { copy(isLoading = false) }
                }
            }
        }
    }

    private fun registerPushToken(token: String) {
        viewModelScope.launch {
            financialRepository.registerPushToken(token)
        }
    }

    private fun updatePreferences(pushEnabled: Boolean, emailEnabled: Boolean) {
        viewModelScope.launch {
            setState { copy(isLoading = true, errorMessage = null) }
            val req = UpdateNotificationPreferencesRequest(
                pushEnabled = pushEnabled,
                emailEnabled = emailEnabled,
                reminderEnabled = true,
                timeZone = "America/Mexico_City"
            )
            when (val result = financialRepository.updateNotificationPreferences(req)) {
                is AuthResult.Success -> {
                    setState { copy(isLoading = false, preferences = result.data) }
                    emitEffect(NotificationsEffect.ShowToast("Preferencias de notificación guardadas."))
                }
                is AuthResult.Failure -> {
                    setState { copy(isLoading = false, errorMessage = result.error.message) }
                }
            }
        }
    }
}
