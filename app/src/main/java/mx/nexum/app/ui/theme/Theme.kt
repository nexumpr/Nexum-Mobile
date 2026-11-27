package mx.nexum.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SkeuomorphicColorScheme = darkColorScheme(
    primary = HifiCyan,
    onPrimary = TitaniumBackground,
    primaryContainer = HifiCyan.copy(alpha = 0.25f),
    onPrimaryContainer = HifiCyan,
    secondary = HifiAmber,
    onSecondary = TitaniumBackground,
    secondaryContainer = HifiAmber.copy(alpha = 0.25f),
    onSecondaryContainer = HifiAmber,
    tertiary = HifiGreen,
    onTertiary = TitaniumBackground,
    background = TitaniumBackground,
    onBackground = TextPrimary,
    surface = MetallicSurface,
    onSurface = TextPrimary,
    surfaceVariant = MetallicCard,
    onSurfaceVariant = TextSecondary,
    error = NeonError,
    onError = TextPrimary
)

private val AeroLightColorScheme = lightColorScheme(
    primary = AeroCyanDark,
    onPrimary = Color.White,
    primaryContainer = AeroCyanDark.copy(alpha = 0.15f),
    onPrimaryContainer = AeroCyanDark,
    secondary = AeroPurpleDark,
    onSecondary = Color.White,
    secondaryContainer = AeroPurpleDark.copy(alpha = 0.15f),
    onSecondaryContainer = AeroPurpleDark,
    tertiary = HifiGreen,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightGlassCard,
    onSurfaceVariant = TextSecondaryLight,
    error = NeonError,
    onError = Color.White
)

@Composable
fun NexumTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) SkeuomorphicColorScheme else AeroLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
