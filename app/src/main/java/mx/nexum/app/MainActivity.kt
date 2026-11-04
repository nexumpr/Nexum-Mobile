package mx.nexum.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import mx.nexum.app.auth.ui.AuthFlow
import mx.nexum.app.ui.theme.NexumTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NexumTheme {
                AuthFlow()
            }
        }
    }
}
