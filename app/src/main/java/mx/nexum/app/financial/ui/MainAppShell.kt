package mx.nexum.app.financial.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.nexum.app.auth.data.AuthRepository
import mx.nexum.app.auth.data.TokenStorage
import mx.nexum.app.auth.ui.EmailVerificationIntent
import mx.nexum.app.auth.ui.EmailVerificationRequestScreen
import mx.nexum.app.auth.ui.EmailVerificationViewModel
import mx.nexum.app.auth.data.network.NetworkModule
import mx.nexum.app.financial.data.FinancialRepository
import mx.nexum.app.financial.data.network.FinancialApiService
import androidx.compose.ui.unit.dp

sealed class MainTab(val title: String, val icon: ImageVector) {
    object Summary : MainTab("Resumen", Icons.Default.Home)
    object Movements : MainTab("Movimientos", Icons.Default.List)
    object Recurring : MainTab("Pagos", Icons.Default.CheckCircle)
    object Goals : MainTab("Metas", Icons.Default.Star)
    object Settings : MainTab("Ajustes", Icons.Default.Settings)
}

@Composable
fun MainAppShell(
    tokenStorage: TokenStorage,
    authRepository: AuthRepository,
    modifier: Modifier = Modifier,
    onLogout: () -> Unit = {},
    onNavigateToActiveSessions: () -> Unit = {},
    onNavigateToConfirmVerification: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf<MainTab>(MainTab.Summary) }

    val financialRepository = remember(tokenStorage) {
        FinancialRepository.create(
            NetworkModule.retrofit.create(FinancialApiService::class.java),
            tokenStorage
        )
    }

    val summaryViewModel: FinancialSummaryViewModel = viewModel { FinancialSummaryViewModel(financialRepository) }
    val movementsViewModel: MovementsListViewModel = viewModel { MovementsListViewModel(financialRepository) }
    val recurringViewModel: RecurringPaymentsViewModel = viewModel { RecurringPaymentsViewModel(financialRepository) }
    val goalsViewModel: GoalsViewModel = viewModel { GoalsViewModel(financialRepository) }
    val settingsViewModel: SettingsViewModel = viewModel { SettingsViewModel(tokenStorage) }
    val emailVerificationViewModel: EmailVerificationViewModel = viewModel {
        EmailVerificationViewModel(authRepository, tokenStorage)
    }

    val summaryState by summaryViewModel.uiState.collectAsState()
    val movementsState by movementsViewModel.uiState.collectAsState()
    val recurringState by recurringViewModel.uiState.collectAsState()
    val goalsState by goalsViewModel.uiState.collectAsState()
    val settingsState by settingsViewModel.uiState.collectAsState()
    val emailVerificationState by emailVerificationViewModel.uiState.collectAsState()

    LaunchedEffect(emailVerificationViewModel) {
        emailVerificationViewModel.onIntent(EmailVerificationIntent.RefreshStatus)
    }

    LaunchedEffect(settingsViewModel) {
        settingsViewModel.effects.collect { effect ->
            when (effect) {
                SettingsEffect.NavigateToLogin -> onLogout()
                SettingsEffect.NavigateToActiveSessions -> onNavigateToActiveSessions()
                is SettingsEffect.ShowToast -> {}
            }
        }
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            Column {
                // Neon Top Glow Line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary,
                                    Color.Transparent
                                )
                            )
                        )
                )
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    val tabs = listOf(MainTab.Summary, MainTab.Movements, MainTab.Recurring, MainTab.Goals, MainTab.Settings)
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            icon = { Icon(tab.icon, contentDescription = tab.title) },
                            label = { Text(tab.title) },
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                MainTab.Summary -> FinancialSummaryScreen(state = summaryState)
                MainTab.Movements -> MovementsScreen(state = movementsState, onIntent = movementsViewModel::onIntent)
                MainTab.Recurring -> RecurringPaymentsScreen(state = recurringState, onIntent = recurringViewModel::onIntent)
                MainTab.Goals -> GoalsScreen(state = goalsState, onIntent = goalsViewModel::onIntent)
                MainTab.Settings -> SettingsScreen(
                    state = settingsState,
                    onIntent = settingsViewModel::onIntent,
                    verificationContent = {
                        EmailVerificationRequestScreen(
                            state = emailVerificationState,
                            onIntent = emailVerificationViewModel::onIntent,
                            onNavigateToConfirmVerification = onNavigateToConfirmVerification
                        )
                    }
                )
            }
        }
    }
}
