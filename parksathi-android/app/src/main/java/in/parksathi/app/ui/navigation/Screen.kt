package `in`.parksathi.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String? = null, val icon: ImageVector? = null) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    
    // Main Dashboard Tabs
    object Explore : Screen("explore", "Explore", Icons.Default.Map)
    object Bookings : Screen("bookings", "Bookings", Icons.Default.History)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
    
    companion object {
        val bottomNavItems = listOf(Explore, Bookings, Profile)
    }
}