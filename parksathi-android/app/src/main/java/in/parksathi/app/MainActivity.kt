package `in`.parksathi.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import `in`.parksathi.app.ui.navigation.AppNavigation
import `in`.parksathi.app.ui.theme.ParksathiandroidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ParksathiandroidTheme {
                AppNavigation(this)
            }
        }
    }
}
