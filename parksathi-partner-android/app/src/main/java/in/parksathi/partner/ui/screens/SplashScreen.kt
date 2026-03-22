package `in`.parksathi.partner.ui.screens

import android.content.Context
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import `in`.parksathi.partner.config.RetrofitClient
import `in`.parksathi.partner.enum.VerificationStatus
import `in`.parksathi.partner.ui.navigation.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

@Composable
fun SplashScreen(navController: NavController, context: Context) {
    val sharedPreferences = context.getSharedPreferences("parksathi_prefs", Context.MODE_PRIVATE)
    val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
    
    var startTextAnimation by remember { mutableStateOf(false) }

    val logoScale = remember { Animatable(0.6f) }
    val logoAlpha = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        delay(200)
        logoAlpha.animateTo(1f, animationSpec = tween(800))
        logoScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        
        delay(400)
        startTextAnimation = true
        
        delay(1500) 
        
        if (isLoggedIn) {
            try {
                val user = FirebaseAuth.getInstance().currentUser
                val token = user?.getIdToken(true)?.await()?.token
                
                if (token != null) {
                    val response = RetrofitClient.instance.verifyRole("Bearer $token")
                    Log.d("SplashScreen", "Verification response: $response")
                    if (response.isSuccessful) {
                        val roleData = response.body()
                        if (roleData?.verificationStatus != null) {
                            sharedPreferences.edit().putBoolean("isParkingFormSubmitted", true).apply()
                            
                            if (roleData.verificationStatus == VerificationStatus.PENDING) {
                                navController.navigate(Screen.PendingVerification.route) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
                            } else {
                                navController.navigate(Screen.Dashboard.route) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
                            }
                        } else {
                            sharedPreferences.edit().putBoolean("isParkingFormSubmitted", false).apply()
                            navController.navigate(Screen.ParkingDetails.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        }
                    } else if (response.code() == 404) {
                        // Backend returns 404 if no parking details found
                        sharedPreferences.edit().putBoolean("isParkingFormSubmitted", false).apply()
                        navController.navigate(Screen.ParkingDetails.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                } else {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            } catch (e: Exception) {
                Log.e("SplashScreen", "Verification failed: ${e.message}")
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
        } else {
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .scale(logoScale.value)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = logoAlpha.value)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "P",
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = logoAlpha.value),
                        fontSize = 44.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                AnimatedVisibility(
                    visible = startTextAnimation,
                    enter = fadeIn(animationSpec = tween(1000)) + 
                            expandHorizontally(animationSpec = tween(1000), expandFrom = Alignment.Start)
                ) {
                    Text(
                        text = "arkPartner",
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            AnimatedVisibility(
                visible = startTextAnimation,
                enter = fadeIn(animationSpec = tween(1200)) + slideInVertically(initialOffsetY = { 20 })
            ) {
                Text(
                    text = "Monetize your parking space smartly",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.2.sp
                )
            }
        }
    }
}