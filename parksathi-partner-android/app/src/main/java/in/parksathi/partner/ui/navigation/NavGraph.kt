package `in`.parksathi.partner.ui.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import `in`.parksathi.partner.ui.screens.DashboardScreen
import `in`.parksathi.partner.ui.screens.LoginScreen
import `in`.parksathi.partner.ui.screens.ParkingDetailsScreen
import `in`.parksathi.partner.ui.screens.PendingVerificationScreen
import `in`.parksathi.partner.ui.screens.ScannerScreen
import `in`.parksathi.partner.ui.screens.SplashScreen

@Composable
fun AppNavigation(context: Context) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {

        composable(Screen.Splash.route) {
            SplashScreen(navController, context)
        }

        composable(Screen.Login.route) {
            LoginScreen(navController, context)
        }

        composable(Screen.ParkingDetails.route) {
            ParkingDetailsScreen(navController)
        }

        composable(Screen.PendingVerification.route) {
            PendingVerificationScreen(navController)
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(navController, context)
        }

        composable(Screen.Scanner.route) {
            ScannerScreen(navController)
        }
    }
}
