package mx.nexum.app.financial.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.nexum.app.auth.data.SharedTokenStorage
import mx.nexum.app.auth.data.network.NetworkModule
import mx.nexum.app.financial.data.FinancialRepository
import mx.nexum.app.financial.data.network.FinancialApiService
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

sealed class MainTab(val title: String, val icon: ImageVector) {
    object Summary : MainTab("Resumen", Icons.Default.Home)
    object Movements : MainTab("Movimientos", Icons.Default.List)
    object Recurring : MainTab("Pagos", Icons.Default.CheckCircle)
    object Goals : MainTab("Metas", Icons.Default.Star)
    object Budgets : MainTab("Presupuestos", Icons.Default.AccountBalance)
    object Profile : MainTab("Perfil", Icons.Default.Person)
    object Settings : MainTab("Ajustes", Icons.Default.Settings)
}

@Composable
fun MainAppShell(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = true,
    onThemeChanged: (Boolean) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf<MainTab>(MainTab.Summary) }
    var activeOverlay by remember { mutableStateOf<String?>(null) } // "privacy" or "help"

    BackHandler(enabled = activeOverlay != null || selectedTab != MainTab.Summary) {
        if (activeOverlay != null) {
            activeOverlay = null
        } else if (selectedTab != MainTab.Summary) {
            selectedTab = MainTab.Summary
        }
    }

    val financialRepository = remember {
        FinancialRepository.create(
            NetworkModule.retrofit.create(FinancialApiService::class.java),
            SharedTokenStorage.instance
        )
    }

    val summaryViewModel: FinancialSummaryViewModel = viewModel { FinancialSummaryViewModel(financialRepository) }
    val movementsViewModel: MovementsListViewModel = viewModel { MovementsListViewModel(financialRepository) }
    val recurringViewModel: RecurringPaymentsViewModel = viewModel { RecurringPaymentsViewModel(financialRepository) }
    val goalsViewModel: GoalsViewModel = viewModel { GoalsViewModel(financialRepository) }
    val budgetsViewModel: BudgetsViewModel = viewModel { BudgetsViewModel(financialRepository) }
    val profileViewModel: ProfileViewModel = viewModel { ProfileViewModel(financialRepository) }
    val settingsViewModel: SettingsViewModel = viewModel { SettingsViewModel(SharedTokenStorage.instance, onThemeChanged, financialRepository) }
    val privacyViewModel: PrivacyNoticeViewModel = viewModel { PrivacyNoticeViewModel(financialRepository) }
    val helpViewModel: HelpEducationViewModel = viewModel { HelpEducationViewModel(financialRepository) }

    val summaryState by summaryViewModel.uiState.collectAsState()
    val movementsState by movementsViewModel.uiState.collectAsState()
    val recurringState by recurringViewModel.uiState.collectAsState()
    val goalsState by goalsViewModel.uiState.collectAsState()
    val budgetsState by budgetsViewModel.uiState.collectAsState()
    val profileState by profileViewModel.uiState.collectAsState()
    val settingsState by settingsViewModel.uiState.collectAsState()
    val privacyState by privacyViewModel.uiState.collectAsState()
    val helpState by helpViewModel.uiState.collectAsState()

    // Initialize switch state
    LaunchedEffect(isDarkTheme) {
        settingsViewModel.onIntent(SettingsIntent.ToggleTheme(isDarkTheme))
    }

    LaunchedEffect(settingsViewModel) {
        settingsViewModel.effects.collect { effect ->
            when (effect) {
                SettingsEffect.NavigateToLogin -> onLogout()
                SettingsEffect.NavigateToPrivacy -> activeOverlay = "privacy"
                SettingsEffect.NavigateToHelp -> activeOverlay = "help"
                else -> {}
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
                    val tabs = listOf(MainTab.Summary, MainTab.Movements, MainTab.Recurring, MainTab.Goals, MainTab.Budgets, MainTab.Profile, MainTab.Settings)
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
                MainTab.Budgets -> BudgetsScreen(state = budgetsState, onIntent = budgetsViewModel::onIntent)
                MainTab.Profile -> ProfileScreen(state = profileState, onIntent = profileViewModel::onIntent)
                MainTab.Settings -> SettingsScreen(state = settingsState, onIntent = settingsViewModel::onIntent)
            }

            if (activeOverlay != null) {
                Dialog(onDismissRequest = { activeOverlay = null }) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(600.dp),
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Box(modifier = Modifier.weight(1f)) {
                                if (activeOverlay == "privacy") {
                                    PrivacyNoticeScreen(state = privacyState, onIntent = privacyViewModel::onIntent)
                                } else if (activeOverlay == "help") {
                                    HelpEducationScreen(state = helpState)
                                }
                            }
                            TextButton(
                                onClick = { activeOverlay = null },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                Text("Cerrar")
                            }
                        }
                    }
                }
            }
        }
    }
}
