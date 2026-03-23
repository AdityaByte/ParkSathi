package `in`.parksathi.app.ui.screens

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.parksathi.app.config.RetrofitClient
import `in`.parksathi.app.dto.NearbyParkingSpot
import kotlinx.coroutines.launch

class ExploreViewModel : ViewModel() {
    private val _nearbyParkingSpots = mutableStateOf<List<NearbyParkingSpot>>(emptyList())
    val nearbyParkingSpots: State<List<NearbyParkingSpot>> = _nearbyParkingSpots

    private val _selectedSpot = mutableStateOf<NearbyParkingSpot?>(null)
    val selectedSpot: State<NearbyParkingSpot?> = _selectedSpot

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    fun fetchNearbyParking(lat: Double, lng: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            _selectedSpot.value = null 
            try {
                val response = RetrofitClient.instance.findNearbyParking(lat, lng)
                if (response.isSuccessful) {
                    val spots = response.body() ?: emptyList()
                    _nearbyParkingSpots.value = spots
                    
                    // Auto-select if only one spot is found
                    if (spots.size == 1) {
                        _selectedSpot.value = spots[0]
                    }
                    Log.d("ExploreViewModel", "Fetched ${spots.size} spots")
                } else {
                    Log.e("ExploreViewModel", "Error: ${response.code()} ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("ExploreViewModel", "Network Exception: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectSpot(spot: NearbyParkingSpot?) {
        _selectedSpot.value = spot
    }
}