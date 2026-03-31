package `in`.parksathi.partner.ui.screens

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import `in`.parksathi.partner.config.RetrofitClient
import `in`.parksathi.partner.dto.OwnerBookingResponse
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class BookingViewModel : ViewModel() {

    private val _bookings = mutableStateOf<List<OwnerBookingResponse>>(emptyList())
    val bookings: State<List<OwnerBookingResponse>> = _bookings

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    init {
        fetchBookings()
    }

    fun fetchBookings() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val user = FirebaseAuth.getInstance().currentUser
                val token = user?.getIdToken(true)?.await()?.token
                
                if (token != null) {
                    val response = RetrofitClient.instance.getOwnerBookings("Bearer $token")
                    if (response.isSuccessful) {
                        _bookings.value = response.body() ?: emptyList()
                    } else {
                        Log.e("BookingViewModel", "Fetch failed: ${response.code()} ${response.errorBody()?.string()}")
                        _errorMessage.value = "Failed to fetch bookings: ${response.code()}"
                    }
                } else {
                    _errorMessage.value = "User not authenticated"
                }
            } catch (e: Exception) {
                Log.e("BookingViewModel", "Error fetching bookings", e)
                _errorMessage.value = "An error occurred: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
