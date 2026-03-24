package `in`.parksathi.app.ui.screens

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import `in`.parksathi.app.config.RetrofitClient
import `in`.parksathi.app.dto.NearbyParkingSpot
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ExploreViewModel: This mainly serves as the bridge in between the
 * view and the model it stores the data and the presentation is done by the view itself.
 */

// Mock data for places prediction - just using theme as of the billing account is not activated.
data class DummyPrediction(
    val placeId: String,
    val primaryText: String,
    val secondaryText: String,
    val fullText: String,
    val latLng: LatLng
)

class ExploreViewModel : ViewModel() {
    private val _nearbyParkingSpots = mutableStateOf<List<NearbyParkingSpot>>(emptyList())
    val nearbyParkingSpots: State<List<NearbyParkingSpot>> = _nearbyParkingSpots

    private val _selectedSpot = mutableStateOf<NearbyParkingSpot?>(null)
    val selectedSpot: State<NearbyParkingSpot?> = _selectedSpot

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    // Dummy predictions state
    private val _dummyPredictions = mutableStateOf<List<DummyPrediction>>(emptyList())
    val dummyPredictions: State<List<DummyPrediction>> = _dummyPredictions

    // Dummy data as of my home place.
    private val allDummyPlaces = listOf(
        DummyPrediction("1", "Betul Railway Station", "Railway Colony, Betul", "Betul Railway Station, Madhya Pradesh", LatLng(21.9161, 77.9015)),
        DummyPrediction("2", "District Hospital Betul", "Ganj, Betul", "District Hospital, Ganj, Betul, Madhya Pradesh", LatLng(21.9185, 77.9080)),
        DummyPrediction("3", "Sadar Bazar", "Main Market, Betul", "Sadar Bazar, Betul, Madhya Pradesh", LatLng(21.9120, 77.9150)),
        DummyPrediction("4", "Little Flower School", "Civil Lines, Betul", "Little Flower Higher Secondary School, Betul", LatLng(21.9085, 77.9125)),
        DummyPrediction("5", "Collectorate Office", "Civil Lines, Betul", "District Collectorate, Betul, Madhya Pradesh", LatLng(21.9068, 77.9092)),
        DummyPrediction("6", "Betul Ganj Post Office", "Ganj, Betul", "Head Post Office, Ganj, Betul", LatLng(21.9192, 77.9110)),
        DummyPrediction("7", "Kothi Bazaar", "Betul Town", "Kothi Bazaar area, Betul, Madhya Pradesh", LatLng(21.9155, 77.9180))
    )

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

    fun getDummyPredictions(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _dummyPredictions.value = emptyList()
                return@launch
            }
            delay(300) // Simulate network delay
            _dummyPredictions.value = allDummyPlaces.filter { 
                it.fullText.contains(query, ignoreCase = true) 
            }
        }
    }

    fun selectSpot(spot: NearbyParkingSpot?) {
        _selectedSpot.value = spot
    }

    fun clearPredictions() {
        _dummyPredictions.value = emptyList()
    }
}
