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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.nexum.app.auth.data.SharedTokenStorage
import mx.nexum.app.auth.data.TokenStorage
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

class FinancialViewModelFactory(
    private val financialRepository: FinancialRepository,
    private val tokenStorage: TokenStorage,
    private val onThemeChanged: (Boolean) -> Unit
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(FinancialSummaryViewModel::class.java) -> FinancialSummaryViewModel(financialRepository) as T
            modelClass.isAssignableFrom(MovementsListViewModel::class.java) -> MovementsListViewModel(financialRepository) as T
            modelClass.isAssignableFrom(RecurringPaymentsViewModel::class.java) -> RecurringPaymentsViewModel(financialRepository) as T
            modelClass.isAssignableFrom(GoalsViewModel::class.java) -> GoalsViewModel(financialRepository) as T
            modelClass.isAssignableFrom(BudgetsViewModel::class.java) -> BudgetsViewModel(financialRepository) as T
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> ProfileViewModel(financialRepository) as T
            modelClass.isAssignableFrom(PrivacyNoticeViewModel::class.java) -> PrivacyNoticeViewModel(financialRepository) as T
            modelClass.isAssignableFrom(HelpEducationViewModel::class.java) -> HelpEducationViewModel(financialRepository) as T
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> SettingsViewModel(tokenStorage, onThemeChanged, financialRepository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
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

    val factory = remember {
        FinancialViewModelFactory(financialRepository, SharedTokenStorage.instance, onThemeChanged)
    }

    val summaryViewModel: FinancialSummaryViewModel = viewModel(factory = factory)
    val movementsViewModel: MovementsListViewModel = viewModel(factory = factory)
    val recurringViewModel: RecurringPaymentsViewModel = viewModel(factory = factory)
    val goalsViewModel: GoalsViewModel = viewModel(factory = factory)
    val budgetsViewModel: BudgetsViewModel = viewModel(factory = factory)
    val profileViewModel: ProfileViewModel = viewModel(factory = factory)
    val settingsViewModel: SettingsViewModel = viewModel(factory = factory)
    val privacyViewModel: PrivacyNoticeViewModel = viewModel(factory = factory)
    val helpViewModel: HelpEducationViewModel = viewModel(factory = factory)

    val summaryState by summaryViewModel.uiState.collectAsState()
    val movementsState by movementsViewModel.uiState.collectAsState()
    val recurringState by recurringViewModel.uiState.collectAsState()
    val goalsState by goalsViewModel.uiState.collectAsState()
    val budgetsState by budgetsViewModel.uiState.collectAsState()
    val profileState by profileViewModel.uiState.collectAsState()
    val settingsState by settingsViewModel.uiState.collectAsState()
    val privacyState by privacyViewModel.uiState.collectAsState()
    val helpState by helpViewModel.uiState.collectAsState()

    // Refresh data whenever tab changes
    LaunchedEffect(selectedTab) {
        when (selectedTab) {
            MainTab.Summary -> {}
            MainTab.Movements -> movementsViewModel.onIntent(MovementsListIntent.LoadMovements)
            MainTab.Recurring -> recurringViewModel.onIntent(RecurringPaymentsIntent.LoadPayments)
            MainTab.Goals -> goalsViewModel.onIntent(GoalsIntent.LoadGoals)
            MainTab.Budgets -> budgetsViewModel.loadBudgets()
            MainTab.Profile -> profileViewModel.loadProfile()
            MainTab.Settings -> {}
        }
    }

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
