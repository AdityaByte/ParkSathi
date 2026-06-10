package `in`.parksathi.app.ui.screens

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

data class ProfileUiState(
    val user: FirebaseUser? = null,
    val isLoading: Boolean = false
)

class ProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    
    private val _uiState = mutableStateOf(ProfileUiState(user = auth.currentUser))
    val uiState: State<ProfileUiState> = _uiState

    fun logout(onLogoutSuccess: () -> Unit) {
        auth.signOut()
        onLogoutSuccess()
    }
    
    fun refreshUser() {
        _uiState.value = _uiState.value.copy(user = auth.currentUser)
    }
}
