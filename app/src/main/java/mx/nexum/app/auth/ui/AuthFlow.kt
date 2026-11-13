package mx.nexum.app.auth.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import mx.nexum.app.auth.data.AuthRepository
import mx.nexum.app.auth.data.AuthRepositoryImpl
import mx.nexum.app.auth.data.InMemoryTokenStorage
import androidx.compose.ui.tooling.preview.Preview
import mx.nexum.app.ui.theme.NexumTheme

enum class AuthScreen { Login, Register, App }

class AuthFlowViewModel : ViewModel() {
    val repository: AuthRepository = AuthRepositoryImpl(
        mx.nexum.app.auth.data.RetrofitAuthApi(mx.nexum.app.auth.data.network.NetworkModule.authApiService),
        InMemoryTokenStorage()
    )
}

@Composable
fun AuthFlow(modifier: Modifier = Modifier) {
    val flowViewModel: AuthFlowViewModel = viewModel()
    val repository = flowViewModel.repository
    val registerViewModel: RegisterViewModel = viewModel { RegisterViewModel() }
    val loginViewModel: LoginViewModel = viewModel { LoginViewModel(repository) }

    var screenName by rememberSaveable { mutableStateOf(AuthScreen.Login.name) }
    val screen = remember(screenName) { runCatching { AuthScreen.valueOf(screenName) }.getOrDefault(AuthScreen.Login) }

    val registerState by registerViewModel.state.collectAsState()
    val loginState by loginViewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(loginViewModel, registerViewModel, snackbarHostState) {
        launch {
            loginViewModel.effects.collect { effect ->
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

                AuthScreen.App -> AppPlaceholder()
            }
        }
    }
}

@Composable
private fun AppPlaceholder(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "Sesión iniciada",
            style = MaterialTheme.typography.headlineSmall
        )
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
