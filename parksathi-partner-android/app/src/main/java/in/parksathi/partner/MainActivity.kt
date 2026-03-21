package `in`.parksathi.partner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import `in`.parksathi.partner.ui.navigation.AppNavigation
import `in`.parksathi.partner.ui.theme.ParksathipartnerandroidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ParksathipartnerandroidTheme {
                AppNavigation(this)
            }
        }
    }
}
