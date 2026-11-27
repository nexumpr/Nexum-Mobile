package mx.nexum.app.financial.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun SkeuomorphicCard(
    modifier: Modifier = Modifier,
    accentColor: Color = Color(0xFF00E5FF),
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.97f else 1.0f, label = "CardPressScale")

    val isDark = MaterialTheme.colorScheme.background.red < 0.1f
    val topMetal = if (isDark) Color(0xFF333B4D) else Color(0xFFE2E8F0)
    val bottomMetal = if (isDark) Color(0xFF1C202B) else Color(0xFFCBD5E1)
    val lcdBackground = if (isDark) Color(0xFF07090F) else Color(0xFFFFFFFF)
    val specularAlpha = if (isDark) 0.3f else 0.7f

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .pointerInput(Unit) {
                if (onClick != null) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                            onClick()
                        }
                    )
                }
            },
        shape = RoundedCornerShape(14.dp),
        color = Color.Transparent,
        border = BorderStroke(1.2.dp, if (isDark) Color(0xFF475569) else Color(0xFF94A3B8)),
        tonalElevation = if (isPressed) 2.dp else 6.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(topMetal, bottomMetal)
                    )
                )
                .padding(5.dp) // Outer casing padding
        ) {
            // Top Specular Highlight
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = specularAlpha))
                    .align(Alignment.TopCenter)
            )

            // Recessed LCD Panel
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = lcdBackground,
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.6f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    content()
                }
            }
        }
    }
}
