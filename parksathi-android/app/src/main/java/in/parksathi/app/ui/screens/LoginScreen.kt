package `in`.parksathi.app.ui.screens

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import `in`.parksathi.app.ui.navigation.Screen
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun LoginScreen(navController: NavController, context: Context) {
    val sharedPreferences = context.getSharedPreferences("parksathi_prefs", Context.MODE_PRIVATE)

    // The web client id is crucial for identifying google that the google sign in
    // request is coming from a trusted source as when we set the app in the firebase console
    // we get a web client id which is used for unique identification.
    val firebaseWebClientId = BuildConfig.FIREBASE_WEB_CLIENT_ID

    val scope = rememberCoroutineScope()
    val localContext = LocalContext.current
    val credentialManager = CredentialManager.create(localContext)
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to ParkSathi",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    if (firebaseWebClientId.isEmpty()) {
                        Toast.makeText(localContext, "Web Client ID is missing", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    scope.launch {
                        isLoading = true
                        val success = signInWithGoogle(localContext, credentialManager, firebaseWebClientId)
                        if (success) {
                            sharedPreferences.edit().putBoolean("isLoggedIn", true).apply()
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        } else {
                            Toast.makeText(localContext, "Login failed", Toast.LENGTH_SHORT).show()
                        }
                        isLoading = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            ) {
                Text(text = "Login with Google")
            }
        }
        
        if (firebaseWebClientId.isEmpty()) {
            Text(
                text = "Warning: Firebase Client ID not found in local.properties",
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

private suspend fun signInWithGoogle(context: Context, credentialManager: CredentialManager, webClientId: String): Boolean {

    // Making a configuration request to the Google oauth server.
    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(webClientId)
        .build()

    // Requesting for the credential manager as of we will see the account window.
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    return try {
        // When we click on the account and logs in we will get a Google token.
        val result = credentialManager.getCredential(context, request)
        val credential = result.credential
        
        if (credential is GoogleIdTokenCredential) {
            // Converting the Google token into firebase format.
            val firebaseCredential = GoogleAuthProvider.getCredential(credential.idToken, null)
            // Here we are doing like making a request to firebase auth and telling the Google to let the user in.
            // If it returns the user then we are logged in.
            val authResult = FirebaseAuth.getInstance().signInWithCredential(firebaseCredential).await()
            authResult.user != null
        } else {
            false
        }
    } catch (e: GetCredentialException) {
        Log.e("LoginScreen", "Google Sign-In failed", e)
        false
    } catch (e: Exception) {
        Log.e("LoginScreen", "Unexpected error: ${e.message}", e)
        false
    }
}
