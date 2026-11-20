package mx.nexum.app.auth.ui

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import mx.nexum.app.auth.data.AuthRepository
import mx.nexum.app.auth.data.AuthRepositoryImpl
import mx.nexum.app.auth.data.InMemoryTokenStorage
import mx.nexum.app.financial.ui.MainAppShell
import mx.nexum.app.ui.theme.NexumTheme

enum class AuthScreen {
    Login,
    Register,
    ForgotPassword,
    ResetPassword,
    ChangePassword,
    ActiveSessions,
    ConfirmEmailVerification,
    App
}

class AuthFlowViewModel : ViewModel() {
    val tokenStorage: InMemoryTokenStorage = InMemoryTokenStorage()
    val repository: AuthRepository = AuthRepositoryImpl(
        mx.nexum.app.auth.data.RetrofitAuthRepositoryApi(),
        tokenStorage
    )
}

@Composable
fun AuthFlow(modifier: Modifier = Modifier) {
    val flowViewModel: AuthFlowViewModel = viewModel()
    val repository = flowViewModel.repository
    val registerViewModel: RegisterViewModel = viewModel { RegisterViewModel() }
    val loginViewModel: LoginViewModel = viewModel { LoginViewModel(repository) }
    val forgotPasswordViewModel: ForgotPasswordViewModel = viewModel { ForgotPasswordViewModel(repository) }
    val resetPasswordViewModel: ResetPasswordViewModel = viewModel { ResetPasswordViewModel(repository) }
    val changePasswordViewModel: ChangePasswordViewModel = viewModel { ChangePasswordViewModel(repository) }
    val sessionsViewModel: SessionsViewModel = viewModel { SessionsViewModel(repository) }
    val confirmEmailVerificationViewModel: ConfirmEmailVerificationViewModel = viewModel {
        ConfirmEmailVerificationViewModel(repository, flowViewModel.tokenStorage)
    }

    var screenName by rememberSaveable { mutableStateOf(AuthScreen.Login.name) }
    val screen = remember(screenName) { runCatching { AuthScreen.valueOf(screenName) }.getOrDefault(AuthScreen.Login) }

    val registerState by registerViewModel.state.collectAsState()
    val loginState by loginViewModel.state.collectAsState()
    val forgotPasswordState by forgotPasswordViewModel.state.collectAsState()
    val resetPasswordState by resetPasswordViewModel.state.collectAsState()
    val changePasswordState by changePasswordViewModel.state.collectAsState()
    val sessionsState by sessionsViewModel.state.collectAsState()
    val confirmEmailVerificationState by confirmEmailVerificationViewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(loginViewModel, registerViewModel, forgotPasswordViewModel, resetPasswordViewModel, changePasswordViewModel, sessionsViewModel, confirmEmailVerificationViewModel, repository, snackbarHostState) {
        launch {
            repository.sessionEffects.collect { effect ->
                when (effect) {
                    AuthEffect.NavigateToLogin -> screenName = AuthScreen.Login.name
                    else -> {}
                }
            }
        }
        launch {
            loginViewModel.effects.collect { effect ->
                when (effect) {
                    AuthEffect.NavigateToLogin -> screenName = AuthScreen.Login.name
                    AuthEffect.NavigateToRegister -> screenName = AuthScreen.Register.name
                    AuthEffect.NavigateToForgotPassword -> screenName = AuthScreen.ForgotPassword.name
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
                    is AuthEffect.NavigateToResetPassword -> {
                        resetPasswordViewModel.setUserEmail(effect.email)
                        screenName = AuthScreen.ResetPassword.name
                    }
                    is AuthEffect.ShowSnackbar -> {
                        snackbarHostState.showSnackbar(
                            message = effect.message,
                            withDismissAction = true
                        )
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
                        snackbarHostState.showSnackbar(
                            message = effect.message,
                            withDismissAction = true
                        )
                    }
                    else -> {}
                }
            }
        }
        launch {
            changePasswordViewModel.effects.collect { effect ->
                when (effect) {
                    AuthEffect.NavigateToLogin -> screenName = AuthScreen.Login.name
                    is AuthEffect.ShowSnackbar -> {
                        snackbarHostState.showSnackbar(
                            message = effect.message,
                            withDismissAction = true
                        )
                    }
                    else -> {}
                }
            }
        }
        launch {
            sessionsViewModel.effects.collect { effect ->
                when (effect) {
                    AuthEffect.NavigateToApp -> screenName = AuthScreen.App.name
                    AuthEffect.NavigateToLogin -> screenName = AuthScreen.Login.name
                    is AuthEffect.ShowSnackbar -> {
                        snackbarHostState.showSnackbar(
                            message = effect.message,
                            withDismissAction = true
                        )
                    }
                    else -> {}
                }
            }
        }
        launch {
            confirmEmailVerificationViewModel.effects.collect { effect ->
                when (effect) {
                    AuthEffect.NavigateToApp,
                    AuthEffect.NavigateToRequestVerification -> screenName = AuthScreen.App.name
                    AuthEffect.NavigateToLogin -> screenName = AuthScreen.Login.name
                    is AuthEffect.ShowSnackbar -> {
                        snackbarHostState.showSnackbar(
                            message = effect.message,
                            withDismissAction = true
                        )
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

                AuthScreen.ChangePassword -> ChangePasswordScreen(
                    state = changePasswordState,
                    onIntent = changePasswordViewModel::onIntent
                )

                AuthScreen.ActiveSessions -> ActiveSessionsScreen(
                    state = sessionsState,
                    onIntent = sessionsViewModel::onIntent
                )

                AuthScreen.ConfirmEmailVerification -> ConfirmEmailVerificationScreen(
                    state = confirmEmailVerificationState,
                    onIntent = confirmEmailVerificationViewModel::onIntent
                )

                AuthScreen.App -> MainAppShell(
                    tokenStorage = flowViewModel.tokenStorage,
                    authRepository = repository,
                    onLogout = { screenName = AuthScreen.Login.name },
                    onNavigateToActiveSessions = { screenName = AuthScreen.ActiveSessions.name },
                    onNavigateToConfirmVerification = {
                        screenName = AuthScreen.ConfirmEmailVerification.name
                    }
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
