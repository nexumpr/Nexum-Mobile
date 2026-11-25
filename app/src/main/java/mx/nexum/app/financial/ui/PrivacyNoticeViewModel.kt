package mx.nexum.app.financial.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mx.nexum.app.auth.data.AuthResult
import mx.nexum.app.core.mvi.MviViewModel
import mx.nexum.app.core.mvi.UiEffect
import mx.nexum.app.core.mvi.UiIntent
import mx.nexum.app.core.mvi.UiState
import mx.nexum.app.financial.data.FinancialRepository
import mx.nexum.app.financial.data.dto.ConsentItemDto
import mx.nexum.app.financial.data.dto.PrivacyNoticeDto

data class PrivacyNoticeUiState(
    val notice: PrivacyNoticeDto? = null,
    val consents: List<ConsentItemDto> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
) : UiState

sealed interface PrivacyNoticeIntent : UiIntent {
    data object LoadData : PrivacyNoticeIntent
    data class AcceptConsent(val noticeVersion: String) : PrivacyNoticeIntent
}

@Suppress("unused")
sealed interface PrivacyNoticeEffect : UiEffect {
    data class ShowToast(val message: String) : PrivacyNoticeEffect
}

@Suppress("unused")
class PrivacyNoticeViewModel(
    private val financialRepository: FinancialRepository
) : MviViewModel<PrivacyNoticeUiState, PrivacyNoticeIntent, PrivacyNoticeEffect>(PrivacyNoticeUiState()) {

    var isLoaded = false
        private set

    override fun onIntent(intent: PrivacyNoticeIntent) {
        when (intent) {
            PrivacyNoticeIntent.LoadData -> loadData()
            is PrivacyNoticeIntent.AcceptConsent -> acceptConsent(intent.noticeVersion)
        }
    }

    fun loadData() {
        isLoaded = true
        viewModelScope.launch {
            setState { copy(isLoading = true, errorMessage = null) }
            val noticeResult = financialRepository.getCurrentPrivacyNotice()
            val consentsResult = financialRepository.getUserConsents()

            val notice = (noticeResult as? AuthResult.Success)?.data
            val consents = (consentsResult as? AuthResult.Success)?.data ?: emptyList()

            setState {
                copy(
                    isLoading = false,
                    notice = notice,
                    consents = consents
                )
            }
        }
    }

    private fun acceptConsent(noticeVersion: String) {
        viewModelScope.launch {
            setState { copy(isLoading = true, errorMessage = null) }
            when (val result = financialRepository.recordConsent(noticeVersion)) {
                is AuthResult.Success -> {
                    setState { copy(isLoading = false, successMessage = "Consentimiento registrado exitosamente.") }
                    emitEffect(PrivacyNoticeEffect.ShowToast("Consentimiento registrado exitosamente."))
                    loadData()
                }
                is AuthResult.Failure -> {
                    setState { copy(isLoading = false, errorMessage = result.error.message) }
                }
            }
        }
    }
}
