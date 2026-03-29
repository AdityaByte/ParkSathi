package `in`.parksathi.app.ui.screens

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import `in`.parksathi.app.config.RetrofitClient
import `in`.parksathi.app.dto.BookingResponse
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class BookingsViewModel : ViewModel() {
    private val _bookings = mutableStateOf<List<BookingResponse>>(emptyList())
    val bookings: State<List<BookingResponse>> = _bookings

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _showQrDialog = mutableStateOf<String?>(null)
    val showQrDialog: State<String?> = _showQrDialog

    init {
        fetchBookings()
    }

    fun fetchBookings() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = FirebaseAuth.getInstance().currentUser
                val tokenResult = user?.getIdToken(true)?.await()
                val idToken = tokenResult?.token

                if (idToken != null) {
                    val response = RetrofitClient.instance.getMyBookings("Bearer $idToken")
                    if (response.isSuccessful) {
                        _bookings.value = response.body() ?: emptyList()
                    } else {
                        Log.e("BookingsViewModel", "Error: ${response.code()} ${response.errorBody()?.string()}")
                    }
                }
            } catch (e: Exception) {
                Log.e("BookingsViewModel", "Exception: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun cancelBooking(bookingId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = FirebaseAuth.getInstance().currentUser
                val tokenResult = user?.getIdToken(true)?.await()
                val idToken = tokenResult?.token

                if (idToken != null) {
                    val response = RetrofitClient.instance.cancelBooking(bookingId, "Bearer $idToken")
                    if (response.isSuccessful) {
                        fetchBookings() // Refresh the list
                    } else {
                        Log.e("BookingsViewModel", "Cancel Error: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                Log.e("BookingsViewModel", "Cancel Exception: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun showQr(bookingId: String) {
        _showQrDialog.value = bookingId
    }

    fun dismissQrDialog() {
        _showQrDialog.value = null
    }
}
