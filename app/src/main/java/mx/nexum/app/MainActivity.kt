package mx.nexum.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import mx.nexum.app.auth.ui.AuthFlow
import mx.nexum.app.ui.theme.NexumTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val prefs = getSharedPreferences("nexum_theme_prefs", MODE_PRIVATE)
        setContent {
            var isDarkTheme by remember { mutableStateOf(prefs.getBoolean("is_dark_theme", true)) }
            NexumTheme(darkTheme = isDarkTheme) {
                AuthFlow(
                    isDarkTheme = isDarkTheme,
                    onThemeChanged = { newVal ->
                        isDarkTheme = newVal
                        prefs.edit().putBoolean("is_dark_theme", newVal).apply()
                    }
                )
            }
        }
    }
}
