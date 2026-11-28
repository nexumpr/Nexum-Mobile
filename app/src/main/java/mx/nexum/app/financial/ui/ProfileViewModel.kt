package mx.nexum.app.financial.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mx.nexum.app.auth.data.AuthResult
import mx.nexum.app.core.mvi.MviViewModel
import mx.nexum.app.core.mvi.UiEffect
import mx.nexum.app.core.mvi.UiIntent
import mx.nexum.app.core.mvi.UiState
import mx.nexum.app.financial.data.FinancialRepository
import mx.nexum.app.financial.data.dto.UserExportResponse

data class ProfileUiState(
    val userId: String = "",
    val email: String = "",
    val displayName: String = "",
    val emailVerified: Boolean = false,
    val createdAt: String = "",
    val profileVersion: Int = 1,
    val language: String = "es",
    val timezone: String = "America/Mexico_City",
    val exportData: UserExportResponse? = null,
    val isExportLoading: Boolean = false,
    val exportError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
) : UiState

sealed interface ProfileIntent : UiIntent {
    data object LoadProfile : ProfileIntent
    data class DisplayNameChanged(val value: String) : ProfileIntent
    data class LanguageChanged(val value: String) : ProfileIntent
    data class TimezoneChanged(val value: String) : ProfileIntent
    data object SubmitUpdate : ProfileIntent
    data object OpenExport : ProfileIntent
}

@Suppress("unused")
sealed interface ProfileEffect : UiEffect {
    data class ShowToast(val message: String) : ProfileEffect
    data object OpenExportOverlay : ProfileEffect
}

@Suppress("unused")
class ProfileViewModel(
    private val financialRepository: FinancialRepository
) : MviViewModel<ProfileUiState, ProfileIntent, ProfileEffect>(ProfileUiState()) {

    var isLoaded = false
        private set

    init {
        onIntent(ProfileIntent.LoadProfile)
    }

    override fun onIntent(intent: ProfileIntent) {
        when (intent) {
            ProfileIntent.LoadProfile -> loadProfile()
            is ProfileIntent.DisplayNameChanged -> setState { copy(displayName = intent.value, errorMessage = null) }
            is ProfileIntent.LanguageChanged -> setState { copy(language = intent.value, errorMessage = null) }
            is ProfileIntent.TimezoneChanged -> setState { copy(timezone = intent.value, errorMessage = null) }
            ProfileIntent.SubmitUpdate -> updateProfile()
            ProfileIntent.OpenExport -> {
                loadExportData()
                emitEffect(ProfileEffect.OpenExportOverlay)
            }
        }
    }

    fun loadProfile() {
        isLoaded = true
        launchSafely(
            onError = {
                setState {
                    copy(
                        isLoading = false,
                        errorMessage = "No fue posible cargar el perfil."
                    )
                }
            }
        ) {
            setState { copy(isLoading = true, errorMessage = null) }
            val profileResult = financialRepository.getUserProfile()
            val prefResult = financialRepository.getPreferences()

            when (profileResult) {
                is AuthResult.Success -> {
                    setState {
                        copy(
                            isLoading = false,
                            userId = profileResult.data.id ?: "",
                            email = profileResult.data.email ?: "",
                            displayName = profileResult.data.displayName ?: "",
                            emailVerified = profileResult.data.emailVerified ?: false,
                            createdAt = profileResult.data.createdAt ?: "",
                            profileVersion = profileResult.data.version ?: 1
                        )
                    }
                }
                is AuthResult.Failure -> {
                    setState { copy(isLoading = false, errorMessage = profileResult.error.message) }
                }
            }

            if (prefResult is AuthResult.Success) {
                setState {
                    copy(
                        language = prefResult.data.resolvedLanguage(language),
                        timezone = prefResult.data.resolvedTimezone(timezone)
                    )
                }
            }
        }
    }

    private fun loadExportData() {
        viewModelScope.launch {
            setState { copy(isExportLoading = true, exportError = null) }
            when (val result = financialRepository.getUserExport()) {
                is AuthResult.Success -> {
                    setState { copy(isExportLoading = false, exportData = result.data) }
                }
                is AuthResult.Failure -> {
                    setState { copy(isExportLoading = false, exportError = result.error.message) }
                }
            }
        }
    }

    private fun updateProfile() {
        if (currentState.displayName.isBlank()) {
            setState { copy(errorMessage = "El nombre no puede estar vacío.") }
            return
        }

        viewModelScope.launch {
            setState { copy(isLoading = true, errorMessage = null, successMessage = null) }
            val updateResult = financialRepository.updateProfile(currentState.displayName)
            val prefResult = financialRepository.updatePreferences(currentState.language, currentState.timezone)

            if (updateResult is AuthResult.Success && prefResult is AuthResult.Success) {
                setState { copy(isLoading = false, successMessage = "Perfil y preferencias actualizados con éxito.") }
                emitEffect(ProfileEffect.ShowToast("Perfil actualizado con éxito."))
                loadProfile()
            } else {
                val errorMsg = (updateResult as? AuthResult.Failure)?.error?.message ?: (prefResult as? AuthResult.Failure)?.error?.message ?: "Error al actualizar perfil."
                setState { copy(isLoading = false, errorMessage = errorMsg) }
            }
        }
    }
}
