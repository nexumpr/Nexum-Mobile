package mx.nexum.app.core.ui

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import mx.nexum.app.ui.theme.NexumTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas instrumentadas de interfaz de usuario con Jetpack Compose (M080-R02, M080-R03).
 *
 * Configuración de entorno objetivo:
 * - compileSdk / targetSdk: 36
 * - minSdk: 26
 * - Requiere emulador o dispositivo físico Android activo con depuración USB/ADB habilitada.
 * - Ejecutable mediante `.\gradlew.bat connectedDebugAndroidTest` cuando haya dispositivo.
 */
@RunWith(AndroidJUnit4::class)
class NexumAppShellComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun appShellRendersTitleAndAccessibleComponents() {
        var buttonClicked = 0

        composeTestRule.setContent {
            NexumTheme {
                NexumAppShell(
                    title = "Pantalla de Prueba Sintética",
                    canNavigateBack = true,
                    onNavigateBack = {}
                ) {
                    AccessibleButton(
                        onClick = { buttonClicked++ },
                        contentDescriptionText = "Botón de acción accesible"
                    ) {
                        Text("Acción Accesible")
                    }
                }
            }
        }

        // 1. Verificar presencia accesible del Shell y la barra superior
        composeTestRule
            .onNodeWithContentDescription("Shell de aplicación: Pantalla de Prueba Sintética")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Pantalla de Prueba Sintética")
            .assertIsDisplayed()

        // 2. Verificar navegación accesible
        composeTestRule
            .onNodeWithContentDescription("Regresar a la pantalla anterior")
            .assertIsDisplayed()

        // 3. Verificar botón accesible y clic único sin duplicaciones
        val accessibleButton = composeTestRule.onNodeWithContentDescription("Botón de acción accesible")
        accessibleButton.assertIsDisplayed()
        accessibleButton.performClick()

        assertEquals(1, buttonClicked)
    }

    @Test
    fun privacyBannerDisplaysAndTriggersSecureLinkAction() {
        var openedUrl: String? = null

        composeTestRule.setContent {
            NexumTheme {
                PrivacyPolicyBanner(
                    onOpenPolicy = { url -> openedUrl = url }
                )
            }
        }

        // Verificar el banner y su descripción accesible
        composeTestRule
            .onNodeWithContentDescription("Aviso de privacidad de Nexum. Sus datos están protegidos bajo TLS/HTTPS.")
            .assertIsDisplayed()

        // Hacer clic en el enlace accesible
        composeTestRule
            .onNodeWithContentDescription("Leer aviso de privacidad en nexumpr.github.io")
            .assertIsDisplayed()
            .performClick()

        assertTrue(openedUrl != null && openedUrl!!.startsWith("https://"))
    }
}
