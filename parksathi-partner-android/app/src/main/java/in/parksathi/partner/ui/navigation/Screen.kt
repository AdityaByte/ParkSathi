package `in`.parksathi.partner.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object ParkingDetails : Screen("parking_details")
    object Dashboard : Screen("dashboard")
    object PendingVerification : Screen("pending_verification")
}