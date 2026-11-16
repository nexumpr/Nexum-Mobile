package mx.nexum.app.core.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppNavigatorTest {

    @Test
    fun navigatorInitializesWithDefaultOrCustomDestination() {
        val navigator = SimpleNavigator(TechnicalDestination.Auth)
        assertEquals(TechnicalDestination.Auth, navigator.currentDestination.value)
        assertEquals(listOf(TechnicalDestination.Auth), navigator.backStack)
    }

    @Test
    fun navigateToPushesDestinationOntoStackAndUpdatesCurrent() {
        val navigator = SimpleNavigator(TechnicalDestination.Auth)

        navigator.navigateTo(TechnicalDestination.ShellHome)

        assertEquals(TechnicalDestination.ShellHome, navigator.currentDestination.value)
        assertEquals(
            listOf(TechnicalDestination.Auth, TechnicalDestination.ShellHome),
            navigator.backStack
        )
    }

    @Test
    fun navigateBackPopsStackAndRestoresPreviousDestination() {
        val navigator = SimpleNavigator(TechnicalDestination.Auth)
        navigator.navigateTo(TechnicalDestination.ShellHome)

        val popped = navigator.navigateBack()

        assertTrue(popped)
        assertEquals(TechnicalDestination.Auth, navigator.currentDestination.value)
        assertEquals(listOf(TechnicalDestination.Auth), navigator.backStack)
    }

    @Test
    fun navigateBackReturnsFalseWhenAtRootDestination() {
        val navigator = SimpleNavigator(TechnicalDestination.Auth)

        val popped = navigator.navigateBack()

        assertFalse(popped)
        assertEquals(TechnicalDestination.Auth, navigator.currentDestination.value)
        assertEquals(listOf(TechnicalDestination.Auth), navigator.backStack)
    }

    @Test
    fun resetToClearsStackAndSetsNewRoot() {
        val navigator = SimpleNavigator(TechnicalDestination.Auth)
        navigator.navigateTo(TechnicalDestination.ShellHome)
        navigator.navigateTo(TechnicalDestination.Custom("detail"))

        assertEquals(3, navigator.backStack.size)

        navigator.resetTo(TechnicalDestination.Auth)

        assertEquals(1, navigator.backStack.size)
        assertEquals(TechnicalDestination.Auth, navigator.currentDestination.value)
    }
}
