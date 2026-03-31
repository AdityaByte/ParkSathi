package `in`.parksathi.app.ui.screens

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import `in`.parksathi.app.config.RetrofitClient
import `in`.parksathi.app.dto.PaymentResponse
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class PaymentUiState {
    object Idle : PaymentUiState()
    object Loading : PaymentUiState()
    data class Success(val paymentDetails: PaymentResponse) : PaymentUiState()
    data class Error(val message: String) : PaymentUiState()
    object PaymentDone : PaymentUiState()
}

class PaymentViewModel : ViewModel() {
    private val _uiState = mutableStateOf<PaymentUiState>(PaymentUiState.Idle)
    val uiState: State<PaymentUiState> = _uiState

    fun createPayment(bookingId: String) {
        viewModelScope.launch {
            _uiState.value = PaymentUiState.Loading
            try {
                val user = FirebaseAuth.getInstance().currentUser
                val tokenResult = user?.getIdToken(true)?.await()
                val idToken = tokenResult?.token

                if (idToken != null) {
                    val response = RetrofitClient.instance.createPayment(bookingId, "Bearer $idToken")
                    if (response.isSuccessful && response.body() != null) {
                        _uiState.value = PaymentUiState.Success(response.body()!!)
                    } else {
                        _uiState.value = PaymentUiState.Error("Failed to fetch payment details")
                    }
                } else {
                    _uiState.value = PaymentUiState.Error("Authentication failed")
                }
            } catch (e: Exception) {
                Log.e("PaymentViewModel", "Create Payment Exception: ${e.message}")
                _uiState.value = PaymentUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun makePayment(bookingId: String) {
        viewModelScope.launch {
            _uiState.value = PaymentUiState.Loading
            try {
                val user = FirebaseAuth.getInstance().currentUser
                val tokenResult = user?.getIdToken(true)?.await()
                val idToken = tokenResult?.token

                if (idToken != null) {
                    val response = RetrofitClient.instance.makePayment(bookingId, "Bearer $idToken")
                    if (response.isSuccessful && response.body()?.status == "DONE") {
                        _uiState.value = PaymentUiState.PaymentDone
                    } else {
                        _uiState.value = PaymentUiState.Error(response.body()?.message ?: "Payment failed")
                    }
                }
            } catch (e: Exception) {
                Log.e("PaymentViewModel", "Make Payment Exception: ${e.message}")
                _uiState.value = PaymentUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
