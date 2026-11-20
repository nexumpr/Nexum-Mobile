package mx.nexum.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AeroFuturisticColorScheme = darkColorScheme(
    primary = AeroCyan,
    onPrimary = ObsidianBackground,
    primaryContainer = AeroCyan.copy(alpha = 0.2f),
    onPrimaryContainer = AeroCyan,
    secondary = AeroPurple,
    onSecondary = TextPrimary,
    secondaryContainer = AeroPurple.copy(alpha = 0.2f),
    onSecondaryContainer = AeroPurple,
    tertiary = AeroGreen,
    onTertiary = ObsidianBackground,
    background = ObsidianBackground,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = GlassCard,
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
    tertiary = AeroGreen,
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
    val colorScheme = if (darkTheme) AeroFuturisticColorScheme else AeroLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
