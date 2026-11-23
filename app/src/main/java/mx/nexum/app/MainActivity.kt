package mx.nexum.app

import android.content.pm.ActivityInfo
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
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        enableEdgeToEdge()
        setContent {
            var isDarkTheme by remember { mutableStateOf(true) }
            NexumTheme(darkTheme = isDarkTheme) {
                AuthFlow(
                    isDarkTheme = isDarkTheme,
                    onThemeChanged = { isDarkTheme = it }
                )
            }
        }
    }
}
