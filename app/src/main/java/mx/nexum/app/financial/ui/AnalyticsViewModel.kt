package mx.nexum.app.financial.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mx.nexum.app.auth.data.AuthResult
import mx.nexum.app.core.mvi.MviViewModel
import mx.nexum.app.core.mvi.UiEffect
import mx.nexum.app.core.mvi.UiIntent
import mx.nexum.app.core.mvi.UiState
import mx.nexum.app.financial.data.FinancialRepository
import mx.nexum.app.financial.data.dto.CategoryAnalyticDto
import mx.nexum.app.financial.data.dto.InsightDto
import mx.nexum.app.financial.data.dto.RecommendationDto
import mx.nexum.app.financial.data.dto.SavingsStreakDto
import mx.nexum.app.financial.data.dto.SpendingPatternDto

data class AnalyticsUiState(
    val period: String = "2026-11",
    val insights: List<InsightDto> = emptyList(),
    val categories: List<CategoryAnalyticDto> = emptyList(),
    val patterns: List<SpendingPatternDto> = emptyList(),
    val recommendations: List<RecommendationDto> = emptyList(),
    val savingsStreak: SavingsStreakDto? = null,
    val hasEnoughData: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : UiState

sealed interface AnalyticsIntent : UiIntent {
    data object LoadAnalytics : AnalyticsIntent
    data class PeriodChanged(val period: String) : AnalyticsIntent
}

sealed interface AnalyticsEffect : UiEffect {
    data class ShowToast(val message: String) : AnalyticsEffect
}

@Suppress("unused")
class AnalyticsViewModel(
    private val financialRepository: FinancialRepository
) : MviViewModel<AnalyticsUiState, AnalyticsIntent, AnalyticsEffect>(AnalyticsUiState()) {

    var isLoaded = false
        private set

    override fun onIntent(intent: AnalyticsIntent) {
        when (intent) {
            AnalyticsIntent.LoadAnalytics -> loadAnalytics()
            is AnalyticsIntent.PeriodChanged -> {
                setState { copy(period = intent.period) }
                loadAnalytics()
            }
        }
    }

    fun loadAnalytics() {
        isLoaded = true
        viewModelScope.launch {
            setState { copy(isLoading = true, errorMessage = null) }

            val insightsRes = financialRepository.getInsights(currentState.period)
            val categoriesRes = financialRepository.getCategoryAnalytics(currentState.period)
            val patternsRes = financialRepository.getSpendingPatterns(currentState.period)
            val recsRes = financialRepository.getRecommendations(currentState.period)
            val streakRes = financialRepository.getSavingsStreak()

            val insights = (insightsRes as? AuthResult.Success)?.data?.items ?: emptyList()
            val categories = (categoriesRes as? AuthResult.Success)?.data?.items ?: emptyList()
            val patterns = (patternsRes as? AuthResult.Success)?.data?.items ?: emptyList()
            val recs = (recsRes as? AuthResult.Success)?.data?.items ?: emptyList()
            val hasEnoughData = (recsRes as? AuthResult.Success)?.data?.hasEnoughData ?: true
            val streak = (streakRes as? AuthResult.Success)?.data

            setState {
                copy(
                    isLoading = false,
                    insights = insights,
                    categories = categories,
                    patterns = patterns,
                    recommendations = recs,
                    hasEnoughData = hasEnoughData,
                    savingsStreak = streak
                )
            }
        }
    }
}
