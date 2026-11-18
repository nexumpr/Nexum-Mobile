package mx.nexum.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

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

@Suppress("UNUSED_PARAMETER")
@Composable
fun NexumTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AeroFuturisticColorScheme,
        typography = Typography,
        content = content
    )
}
