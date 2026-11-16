package mx.nexum.app.core.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Representa un destino técnico de navegación dentro del shell de la aplicación.
 */
interface AppDestination {
    val route: String
}

/**
 * Destinos base técnicos predefinidos para la arquitectura (sin entidades financieras).
 */
sealed class TechnicalDestination(override val route: String) : AppDestination {
    data object Auth : TechnicalDestination("technical_auth")
    data object ShellHome : TechnicalDestination("technical_shell_home")
    data class Custom(override val route: String) : TechnicalDestination(route)
}

/**
 * Coordinador desacoplado de navegación.
 * Expone un flujo de estado observable [currentDestination] y operaciones de navegación seguras (M030-R02).
 */
interface AppNavigator {
    val currentDestination: StateFlow<AppDestination>
    val backStack: List<AppDestination>
    fun navigateTo(destination: AppDestination)
    fun navigateBack(): Boolean
    fun resetTo(destination: AppDestination)
}

/**
 * Implementación determinista y testeable del navegador sin dependencias de Android framework.
 */
class SimpleNavigator(
    initialDestination: AppDestination = TechnicalDestination.Auth
) : AppNavigator {

    private val stack = mutableListOf(initialDestination)
    private val _currentDestination = MutableStateFlow(initialDestination)
    override val currentDestination: StateFlow<AppDestination> = _currentDestination.asStateFlow()

    override val backStack: List<AppDestination>
        get() = stack.toList()

    override fun navigateTo(destination: AppDestination) {
        stack.add(destination)
        _currentDestination.update { destination }
    }

    override fun navigateBack(): Boolean {
        if (stack.size > 1) {
            stack.removeAt(stack.lastIndex)
            val previous = stack.last()
            _currentDestination.update { previous }
            return true
        }
        return false
    }

    override fun resetTo(destination: AppDestination) {
        stack.clear()
        stack.add(destination)
        _currentDestination.update { destination }
    }
}
