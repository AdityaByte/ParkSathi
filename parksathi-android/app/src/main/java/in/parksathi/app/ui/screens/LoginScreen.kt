package `in`.parksathi.app.ui.screens

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.navigation.NavController
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import `in`.parksathi.app.BuildConfig
import `in`.parksathi.app.config.RetrofitClient
import `in`.parksathi.app.ui.navigation.Screen
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun LoginScreen(navController: NavController, context: Context) {
    val sharedPreferences = context.getSharedPreferences("parksathi_prefs", Context.MODE_PRIVATE)
    val firebaseWebClientId = BuildConfig.FIREBASE_WEB_CLIENT_ID

    val scope = rememberCoroutineScope()
    val localContext = LocalContext.current
    val credentialManager = CredentialManager.create(localContext)
    var isLoading by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome to ParkSathi",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Text(
                text = "Sign in to continue",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 8.dp, bottom = 48.dp)
            )

            if (isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            } else {
                OutlinedButton(
                    onClick = {
                        if (firebaseWebClientId.isEmpty()) {
                            Toast.makeText(localContext, "Web Client ID missing", Toast.LENGTH_SHORT).show()
                            return@OutlinedButton
                        }
                        scope.launch {
                            isLoading = true
                            try {
                                val success = signInWithGoogle(localContext, credentialManager, firebaseWebClientId)
                                if (success) {
                                    val user = FirebaseAuth.getInstance().currentUser
                                    val tokenResult = user?.getIdToken(true)?.await()
                                    val idToken = tokenResult?.token

                                    if (idToken != null) {
                                        val response = RetrofitClient.instance.createUser("Bearer $idToken")
                                        
                                        if (response.isSuccessful) {
                                            sharedPreferences.edit().putBoolean("isLoggedIn", true).apply()
                                            
                                            navController.navigate(Screen.Dashboard.route) {
                                                popUpTo(Screen.Login.route) { inclusive = true }
                                            }
                                        } else {
                                            val errorMsg = response.body()?.message ?: "Backend verification failed"
                                            Toast.makeText(localContext, errorMsg, Toast.LENGTH_LONG).show()
                                            FirebaseAuth.getInstance().signOut()
                                        }
                                    } else {
                                        Toast.makeText(localContext, "Failed to retrieve auth token", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(localContext, "Google Login failed", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Log.e("LoginScreen", "Login error", e)
                                Toast.makeText(localContext, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "G",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4285F4),
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Text(
                            text = "Sign in with Google",
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = {
                        Toast.makeText(localContext, "Microsoft Sign-In coming soon", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Column(modifier = Modifier.padding(end = 12.dp)) {
                            Row {
                                Box(modifier = Modifier.size(8.dp).background(Color(0xFFF25022)))
                                Spacer(modifier = Modifier.width(2.dp))
                                Box(modifier = Modifier.size(8.dp).background(Color(0xFF7FBA00)))
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Row {
                                Box(modifier = Modifier.size(8.dp).background(Color(0xFF00A4EF)))
                                Spacer(modifier = Modifier.width(2.dp))
                                Box(modifier = Modifier.size(8.dp).background(Color(0xFFFFB900)))
                            }
                        }
                        Text(
                            text = "Sign in with Microsoft",
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            
            if (firebaseWebClientId.isEmpty()) {
                Text(
                    text = "Warning: Firebase Client ID not found in local.properties",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}

private suspend fun signInWithGoogle(
    context: Context,
    credentialManager: CredentialManager,
    webClientId: String
): Boolean {
    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(webClientId)
        .setAutoSelectEnabled(true)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    return try {
        val result = credentialManager.getCredential(context, request)
        val credential = result.credential
        
        if (credential is GoogleIdTokenCredential) {
            val firebaseCredential = GoogleAuthProvider.getCredential(credential.idToken, null)
            val authResult = FirebaseAuth.getInstance().signInWithCredential(firebaseCredential).await()
            authResult.user != null
        } else {
            Log.e("LoginScreen", "Received unexpected credential type: ${credential.type}")
            false
        }
    } catch (e: GetCredentialException) {
        Log.e("LoginScreen", "Credential Manager Error (${e.type}): ${e.message}")
        false
    } catch (e: Exception) {
        Log.e("LoginScreen", "Unexpected error: ${e.message}", e)
        false
    }
}
