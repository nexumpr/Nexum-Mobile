package mx.nexum.app.financial.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mx.nexum.app.auth.data.AuthResult
import mx.nexum.app.core.mvi.MviViewModel
import mx.nexum.app.core.mvi.UiEffect
import mx.nexum.app.core.mvi.UiIntent
import mx.nexum.app.core.mvi.UiState
import mx.nexum.app.financial.data.FinancialRepository

data class ProfileUiState(
    val email: String = "",
    val displayName: String = "",
    val emailVerified: Boolean = false,
    val language: String = "es",
    val timezone: String = "America/Mexico_City",
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
}

@Suppress("unused")
sealed interface ProfileEffect : UiEffect {
    data class ShowToast(val message: String) : ProfileEffect
}

@Suppress("unused")
class ProfileViewModel(
    private val financialRepository: FinancialRepository
) : MviViewModel<ProfileUiState, ProfileIntent, ProfileEffect>(ProfileUiState()) {

    var isLoaded = false
        private set

    override fun onIntent(intent: ProfileIntent) {
        when (intent) {
            ProfileIntent.LoadProfile -> loadProfile()
            is ProfileIntent.DisplayNameChanged -> setState { copy(displayName = intent.value, errorMessage = null) }
            is ProfileIntent.LanguageChanged -> setState { copy(language = intent.value, errorMessage = null) }
            is ProfileIntent.TimezoneChanged -> setState { copy(timezone = intent.value, errorMessage = null) }
            ProfileIntent.SubmitUpdate -> updateProfile()
        }
    }

    fun loadProfile() {
        isLoaded = true
        viewModelScope.launch {
            setState { copy(isLoading = true, errorMessage = null) }
            val profileResult = financialRepository.getUserProfile()
            val prefResult = financialRepository.getPreferences()

            when (profileResult) {
                is AuthResult.Success -> {
                    setState {
                        copy(
                            isLoading = false,
                            email = profileResult.data.email,
                            displayName = profileResult.data.displayName,
                            emailVerified = profileResult.data.emailVerified
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
                        language = prefResult.data.language,
                        timezone = prefResult.data.timezone
                    )
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
            } else {
                val errorMsg = (updateResult as? AuthResult.Failure)?.error?.message ?: (prefResult as? AuthResult.Failure)?.error?.message ?: "Error al actualizar perfil."
                setState { copy(isLoading = false, errorMessage = errorMsg) }
            }
        }
    }
}
