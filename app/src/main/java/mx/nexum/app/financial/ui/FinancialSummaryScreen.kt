package mx.nexum.app.financial.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.nexum.app.financial.data.dto.FinancialSummaryResponse
import mx.nexum.app.ui.theme.NexumTheme

@Composable
fun FinancialSummaryScreen(
    state: FinancialSummaryUiState,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.1f

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = if (isDark) {
                        listOf(Color(0xFF1E222B), Color(0xFF10131E))
                    } else {
                        listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1))
                    }
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Camera / Hi-Fi Device Viewfinder Header Bezel
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isDark) Color(0xFF080A0E) else Color(0xFFFFFFFF),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF475569) else Color(0xFF94A3B8)),
                tonalElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFFEF4444), RoundedCornerShape(4.dp)) // REC Red dot
                    )
                    Text(
                        text = "CAM_VIEWFINDER // [ ● REC ] // ISO_800",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "PANEL FINANCIERO ESQUEUMÓRFICO",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = if (isDark) Color.White else Color(0xFF0F172A)
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (state.errorMessage != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.errorMessage, color = MaterialTheme.colorScheme.error)
                }
            } else {
                val summary = state.summary ?: FinancialSummaryResponse(0.0, 0.0, 0.0, 0.0, 0.0)

                // Available Balance Card (Brushed Metallic Chassis + Recessed LCD Viewfinder)
                var isBalancePressed by remember { mutableStateOf(false) }
                val balanceScale by animateFloatAsState(targetValue = if (isBalancePressed) 0.97f else 1.0f, label = "BalanceScale")

                val topMetal = if (isDark) Color(0xFF3D465A) else Color(0xFFF1F5F9)
                val midMetal = if (isDark) Color(0xFF222733) else Color(0xFFE2E8F0)
                val botMetal = if (isDark) Color(0xFF14171E) else Color(0xFFCBD5E1)
                val lcdBg = if (isDark) Color(0xFF07090F) else Color(0xFFFFFFFF)
                val textColor = if (isDark) Color.White else Color(0xFF0F172A)

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer(scaleX = balanceScale, scaleY = balanceScale)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    isBalancePressed = true
                                    tryAwaitRelease()
                                    isBalancePressed = false
                                }
                            )
                        },
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Transparent,
                    border = BorderStroke(1.5.dp, if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)),
                    tonalElevation = if (isBalancePressed) 4.dp else 12.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(topMetal, midMetal, botMetal)
                                )
                            )
                            .padding(6.dp)
                    ) {
                        // Top Specular Highlight on Chassis
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.5.dp)
                                .background(Color.White.copy(alpha = if (isDark) 0.4f else 0.8f))
                                .align(Alignment.TopCenter)
                        )

                        // Recessed LCD Screen Viewfinder
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = lcdBg,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                Text(text = "[ + ]", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), modifier = Modifier.align(Alignment.TopStart))
                                Text(text = "[ + ]", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), modifier = Modifier.align(Alignment.TopEnd))
                                Text(text = "[ + ]", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), modifier = Modifier.align(Alignment.BottomStart))
                                Text(text = "[ + ]", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), modifier = Modifier.align(Alignment.BottomEnd))

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "SALDO LÍQUIDO // LCD_VIEWFINDER",
                                        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.5.sp),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "$${summary.available}",
                                        style = MaterialTheme.typography.headlineLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 38.sp
                                        ),
                                        color = textColor
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Breakdown Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    SummaryCard(
                        title = "INGRESOS",
                        amount = summary.income,
                        accentColor = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.weight(1f),
                        isDark = isDark
                    )
                    SummaryCard(
                        title = "AHORROS",
                        amount = summary.savings,
                        accentColor = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f),
                        isDark = isDark
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    SummaryCard(
                        title = "GASTOS FIJOS",
                        amount = summary.fixedExpenses,
                        accentColor = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f),
                        isDark = isDark
                    )
                    SummaryCard(
                        title = "GASTOS VARIABLES",
                        amount = summary.variableExpenses,
                        accentColor = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.weight(1f),
                        isDark = isDark
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    amount: Double,
    accentColor: Color,
    modifier: Modifier = Modifier,
    isDark: Boolean = true
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1.0f, label = "CardPressScale")

    val topMetal = if (isDark) Color(0xFF333B4D) else Color(0xFFF1F5F9)
    val botMetal = if (isDark) Color(0xFF1C202B) else Color(0xFFCBD5E1)
    val lcdBg = if (isDark) Color(0xFF07090F) else Color(0xFFFFFFFF)
    val textColor = if (isDark) Color.White else Color(0xFF0F172A)
    val specularAlpha = if (isDark) 0.3f else 0.7f

    Surface(
        modifier = modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
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
                        colors = listOf(topMetal, botMetal)
                    )
                )
                .padding(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = specularAlpha))
                    .align(Alignment.TopCenter)
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = lcdBg,
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.6f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "[$title]",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = accentColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$$amount",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            ),
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FinancialSummaryPreview() {
    NexumTheme {
        FinancialSummaryScreen(
            state = FinancialSummaryUiState(
                summary = FinancialSummaryResponse(
                    income = 20000.0,
                    fixedExpenses = 5000.0,
                    variableExpenses = 3000.0,
                    savings = 4000.0,
                    available = 8000.0
                )
            )
        )
    }
}
