package `in`.parksathi.app.ui.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import `in`.parksathi.app.ui.screens.DashboardScreen
import `in`.parksathi.app.ui.screens.LoginScreen
import `in`.parksathi.app.ui.screens.PaymentScreen
import `in`.parksathi.app.ui.screens.SplashScreen

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

        composable(Screen.Dashboard.route) {
            DashboardScreen(navController, context)
        }

        composable(
            route = Screen.Payment.route,
            arguments = listOf(navArgument("bookingId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
            PaymentScreen(bookingId = bookingId, navController = navController)
        }
    }
}
