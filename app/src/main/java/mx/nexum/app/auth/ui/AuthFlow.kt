package mx.nexum.app.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import mx.nexum.app.auth.data.AuthRepository
import mx.nexum.app.auth.data.AuthRepositoryImpl
import mx.nexum.app.auth.data.InMemoryTokenStorage
import androidx.compose.ui.tooling.preview.Preview
import mx.nexum.app.ui.theme.NexumTheme

enum class AuthScreen { Login, Register, ForgotPassword, ResetPassword, App, ChangePassword }

class AuthFlowViewModel : ViewModel() {
    val repository: AuthRepository = AuthRepositoryImpl(
        mx.nexum.app.auth.data.RetrofitAuthRepositoryApi(),
        InMemoryTokenStorage()
    )
}

@Composable
fun AuthFlow(modifier: Modifier = Modifier) {
    val flowViewModel: AuthFlowViewModel = viewModel()
    val repository = flowViewModel.repository
    val registerViewModel: RegisterViewModel = viewModel { RegisterViewModel() }
    val loginViewModel: LoginViewModel = viewModel { LoginViewModel(repository) }
    val logoutViewModel: LogoutViewModel = viewModel { LogoutViewModel(repository) }
    val forgotPasswordViewModel: ForgotPasswordViewModel = viewModel { ForgotPasswordViewModel(repository) }
    val resetPasswordViewModel: ResetPasswordViewModel = viewModel { ResetPasswordViewModel(repository) }
    val changePasswordViewModel: ChangePasswordViewModel = viewModel { ChangePasswordViewModel(repository) }

    var screenName by rememberSaveable { mutableStateOf(AuthScreen.Login.name) }
    val screen = remember(screenName) { runCatching { AuthScreen.valueOf(screenName) }.getOrDefault(AuthScreen.Login) }

    val registerState by registerViewModel.state.collectAsState()
    val loginState by loginViewModel.state.collectAsState()
    val logoutState by logoutViewModel.state.collectAsState()
    val forgotPasswordState by forgotPasswordViewModel.state.collectAsState()
    val resetPasswordState by resetPasswordViewModel.state.collectAsState()
    val changePasswordState by changePasswordViewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(loginViewModel, registerViewModel, logoutViewModel, forgotPasswordViewModel, resetPasswordViewModel, changePasswordViewModel, repository, snackbarHostState) {
        launch {
            repository.sessionEffects.collect { effect ->
                when (effect) {
                    AuthEffect.NavigateToLogin -> screenName = AuthScreen.Login.name
                    AuthEffect.NavigateToRegister -> screenName = AuthScreen.Register.name
                    AuthEffect.NavigateToForgotPassword -> screenName = AuthScreen.ForgotPassword.name
                    is AuthEffect.NavigateToResetPassword -> {
                        resetPasswordViewModel.setUserEmail(effect.email)
                        screenName = AuthScreen.ResetPassword.name
                    }
                    AuthEffect.NavigateToApp -> screenName = AuthScreen.App.name
                    is AuthEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
        launch {
            logoutViewModel.effects.collect { effect ->
                when (effect) {
                    AuthEffect.NavigateToLogin -> screenName = AuthScreen.Login.name
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
                        val result = snackbarHostState.showSnackbar(
                            message = effect.message,
                            actionLabel = if (effect.retry) "Reintentar" else null,
                            withDismissAction = true
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            forgotPasswordViewModel.onIntent(ForgotPasswordIntent.Submit)
                        }
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
                        val result = snackbarHostState.showSnackbar(
                            message = effect.message,
                            actionLabel = if (effect.retry) "Reintentar" else null,
                            withDismissAction = true
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            resetPasswordViewModel.onIntent(ResetPasswordIntent.Submit)
                        }
                    }
                    else -> {}
                }
            }
        }
        launch {
            changePasswordViewModel.effects.collect { effect ->
                when (effect) {
                    AuthEffect.NavigateToLogin -> screenName = AuthScreen.Login.name
                    AuthEffect.NavigateToApp -> screenName = AuthScreen.App.name
                    is AuthEffect.ShowSnackbar -> {
                        val result = snackbarHostState.showSnackbar(
                            message = effect.message,
                            actionLabel = if (effect.retry) "Reintentar" else null,
                            withDismissAction = true
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            changePasswordViewModel.onIntent(ChangePasswordIntent.Submit)
                        }
                    }
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
                    is AuthEffect.NavigateToResetPassword -> {}
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
                }
            }
        }
        launch {
            registerViewModel.effects.collect { effect ->
                when (effect) {
                    AuthEffect.NavigateToLogin -> screenName = AuthScreen.Login.name
                    AuthEffect.NavigateToRegister -> screenName = AuthScreen.Register.name
                    AuthEffect.NavigateToForgotPassword -> screenName = AuthScreen.ForgotPassword.name
                    AuthEffect.NavigateToApp -> screenName = AuthScreen.App.name
                    is AuthEffect.NavigateToResetPassword -> {}
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

                AuthScreen.App -> AppPlaceholder(
                    onLogoutClick = { logoutViewModel.onIntent(LogoutIntent.RequestLogout) },
                    onChangePasswordClick = { screenName = AuthScreen.ChangePassword.name }
                )
            }

            LogoutConfirmDialog(
                isOpen = logoutState.isConfirmDialogOpen,
                isLoading = logoutState.isLoading,
                onConfirm = { logoutViewModel.onIntent(LogoutIntent.ConfirmLogout) },
                onDismiss = { logoutViewModel.onIntent(LogoutIntent.DismissDialog) }
            )
        }
    }
}

@Composable
private fun AppPlaceholder(
    onLogoutClick: () -> Unit = {},
    onChangePasswordClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Sesión iniciada",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onChangePasswordClick,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Text("Cambiar contraseña")
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onLogoutClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        ) {
            Text("Cerrar sesión")
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

@Preview(showBackground = true)
@Composable
private fun AppPlaceholderPreview() {
    NexumTheme {
        AppPlaceholder()
    }
}
