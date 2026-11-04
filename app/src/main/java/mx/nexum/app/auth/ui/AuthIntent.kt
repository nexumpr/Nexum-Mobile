package mx.nexum.app.auth.ui

sealed interface RegisterIntent {
    data class NameChanged(val v: String) : RegisterIntent

    data class EmailChanged(val v: String) : RegisterIntent

    data class PasswordChanged(val v: String) : RegisterIntent

    data class ConfirmPasswordChanged(val v: String) : RegisterIntent

    data object TogglePasswordVisibility : RegisterIntent

    data class IncomeAmountChanged(val v: String) : RegisterIntent

    data class ExpenseChanged(val index: Int, val draft: ExpenseDraft) : RegisterIntent

    data object AddExpense : RegisterIntent

    data class RemoveExpense(val index: Int) : RegisterIntent

    data class TermsAccepted(val v: Boolean) : RegisterIntent

    data object Submit : RegisterIntent
}

sealed interface LoginIntent {
    data class EmailChanged(val v: String) : LoginIntent

    data class PasswordChanged(val v: String) : LoginIntent

    data object TogglePasswordVisibility : LoginIntent

    data object Submit : LoginIntent

    data object NavigateToRegister : LoginIntent

    data object NavigateToForgotPassword : LoginIntent
}

sealed interface AuthEffect {
    data object NavigateToLogin : AuthEffect

    data object NavigateToRegister : AuthEffect

    data object NavigateToApp : AuthEffect

    data class ShowSnackbar(val message: String, val retry: Boolean = false) : AuthEffect
}
