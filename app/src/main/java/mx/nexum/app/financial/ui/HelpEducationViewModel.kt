package mx.nexum.app.financial.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mx.nexum.app.auth.data.AuthResult
import mx.nexum.app.core.mvi.MviViewModel
import mx.nexum.app.core.mvi.UiEffect
import mx.nexum.app.core.mvi.UiIntent
import mx.nexum.app.core.mvi.UiState
import mx.nexum.app.financial.data.FinancialRepository
import mx.nexum.app.financial.data.dto.EducationTipDto
import mx.nexum.app.financial.data.dto.FaqItemDto

data class HelpEducationUiState(
    val faqs: List<FaqItemDto> = emptyList(),
    val tips: List<EducationTipDto> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : UiState

sealed interface HelpEducationIntent : UiIntent {
    data object LoadHelpData : HelpEducationIntent
}

@Suppress("unused")
sealed interface HelpEducationEffect : UiEffect {
    data class ShowToast(val message: String) : HelpEducationEffect
}

@Suppress("unused")
class HelpEducationViewModel(
    private val financialRepository: FinancialRepository
) : MviViewModel<HelpEducationUiState, HelpEducationIntent, HelpEducationEffect>(HelpEducationUiState()) {

    var isLoaded = false
        private set

    override fun onIntent(intent: HelpEducationIntent) {
        when (intent) {
            HelpEducationIntent.LoadHelpData -> loadData()
        }
    }

    private fun loadData() {
        isLoaded = true
        viewModelScope.launch {
            setState { copy(isLoading = true, errorMessage = null) }
            val faqResult = financialRepository.getFaq()
            val tipsResult = financialRepository.getEducationTips()

            val faqs = (faqResult as? AuthResult.Success)?.data ?: emptyList()
            val tips = (tipsResult as? AuthResult.Success)?.data ?: emptyList()

            setState {
                copy(
                    isLoading = false,
                    faqs = faqs,
                    tips = tips
                )
            }
        }
    }
}
