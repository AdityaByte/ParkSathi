package `in`.parksathi.partner.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String? = null, val icon: ImageVector? = null) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object ParkingDetails : Screen("parking_details")
    object Dashboard : Screen("dashboard")
    object PendingVerification : Screen("pending_verification")
    object Scanner : Screen("scanner")

    object Home: Screen("home", "Home", Icons.Default.Home)

    object Booking: Screen("booking", "Booking", Icons.Default.DateRange)

    companion object {
        val bottomNavItems = listOf(Home, Booking)
    }
}
