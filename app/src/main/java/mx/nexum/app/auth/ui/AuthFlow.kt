package mx.nexum.app.auth.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import mx.nexum.app.auth.data.AuthRepository
import mx.nexum.app.auth.data.AuthRepositoryImpl
import mx.nexum.app.auth.data.SharedTokenStorage
import androidx.compose.ui.tooling.preview.Preview
import mx.nexum.app.financial.ui.MainAppShell
import mx.nexum.app.ui.theme.NexumTheme

enum class AuthScreen { Login, Register, ForgotPassword, ResetPassword, App }

class AuthFlowViewModel : ViewModel() {
    val repository: AuthRepository = AuthRepositoryImpl(
        mx.nexum.app.auth.data.RetrofitAuthRepositoryApi(),
        SharedTokenStorage.instance
    )
}

@Composable
fun AuthFlow(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = true,
    onThemeChanged: (Boolean) -> Unit = {}
) {
    val flowViewModel: AuthFlowViewModel = viewModel()
    val repository = flowViewModel.repository
    val registerViewModel: RegisterViewModel = viewModel { RegisterViewModel() }
    val loginViewModel: LoginViewModel = viewModel { LoginViewModel(repository) }
    val forgotPasswordViewModel: ForgotPasswordViewModel = viewModel { ForgotPasswordViewModel(repository) }
    val resetPasswordViewModel: ResetPasswordViewModel = viewModel { ResetPasswordViewModel(repository) }

    var screenName by rememberSaveable { mutableStateOf(AuthScreen.Login.name) }
    val screen = remember(screenName) { runCatching { AuthScreen.valueOf(screenName) }.getOrDefault(AuthScreen.Login) }

    val registerState by registerViewModel.state.collectAsState()
    val loginState by loginViewModel.state.collectAsState()
    val forgotPasswordState by forgotPasswordViewModel.state.collectAsState()
    val resetPasswordState by resetPasswordViewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    BackHandler(enabled = screen != AuthScreen.Login) {
        if (screen != AuthScreen.Login && screen != AuthScreen.App) {
            screenName = AuthScreen.Login.name
        }
    }

    LaunchedEffect(loginViewModel, registerViewModel, forgotPasswordViewModel, resetPasswordViewModel, snackbarHostState) {
        launch {
            loginViewModel.effects.collect { effect ->
                when (effect) {
                    AuthEffect.NavigateToLogin -> screenName = AuthScreen.Login.name
                    AuthEffect.NavigateToRegister -> screenName = AuthScreen.Register.name
                    AuthEffect.NavigateToForgotPassword -> screenName = AuthScreen.ForgotPassword.name
                    is AuthEffect.NavigateToResetPassword -> screenName = AuthScreen.ResetPassword.name
                    AuthEffect.NavigateToApp -> screenName = AuthScreen.App.name
                    is AuthEffect.ShowSnackbar -> {
                        val result = snackbarHostState.showSnackbar(
                            message = effect.message,
                            actionLabel = if (effect.retry) "Reintentar" else null,
                            withDismissAction = true
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            loginViewModel.onIntent(LoginIntent.Submit)
                        }
                    }
                    else -> {}
                }
            }
        }
        launch {
            registerViewModel.effects.collect { effect ->
                when (effect) {
                    AuthEffect.NavigateToLogin -> screenName = AuthScreen.Login.name
                    AuthEffect.NavigateToRegister -> screenName = AuthScreen.Register.name
                    AuthEffect.NavigateToForgotPassword -> screenName = AuthScreen.ForgotPassword.name
                    is AuthEffect.NavigateToResetPassword -> screenName = AuthScreen.ResetPassword.name
                    AuthEffect.NavigateToApp -> screenName = AuthScreen.App.name
                    is AuthEffect.ShowSnackbar -> {
                        val result = snackbarHostState.showSnackbar(
                            message = effect.message,
                            actionLabel = if (effect.retry) "Reintentar" else null,
                            withDismissAction = true
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            registerViewModel.onIntent(RegisterIntent.Submit)
                        }
                    }
                    else -> {}
                }
            }
        }
        launch {
            forgotPasswordViewModel.effects.collect { effect ->
                when (effect) {
                    AuthEffect.NavigateToLogin -> screenName = AuthScreen.Login.name
                    is AuthEffect.NavigateToResetPassword -> screenName = AuthScreen.ResetPassword.name
                    is AuthEffect.ShowSnackbar -> {
                        snackbarHostState.showSnackbar(effect.message)
                    }
                    else -> {}
                }
            }
        }
        launch {
            resetPasswordViewModel.effects.collect { effect ->
                when (effect) {
                    AuthEffect.NavigateToLogin -> screenName = AuthScreen.Login.name
                    AuthEffect.NavigateToForgotPassword -> screenName = AuthScreen.ForgotPassword.name
                    is AuthEffect.ShowSnackbar -> {
                        snackbarHostState.showSnackbar(effect.message)
                    }
                    else -> {}
                }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (screen) {
                AuthScreen.Login -> LoginScreen(
                    state = loginState,
                    onIntent = loginViewModel::onIntent
                )

                AuthScreen.Register -> RegisterScreen(
                    state = registerState,
                    onIntent = registerViewModel::onIntent
                )

                AuthScreen.ForgotPassword -> ForgotPasswordScreen(
                    state = forgotPasswordState,
                    onIntent = forgotPasswordViewModel::onIntent
                )

                AuthScreen.ResetPassword -> ResetPasswordScreen(
                    state = resetPasswordState,
                    onIntent = resetPasswordViewModel::onIntent
                )

                AuthScreen.App -> MainAppShell(
                    isDarkTheme = isDarkTheme,
                    onThemeChanged = onThemeChanged,
                    onLogout = { screenName = AuthScreen.Login.name }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AuthFlowPreview() {
    NexumTheme {
        AuthFlow()
    }
}
