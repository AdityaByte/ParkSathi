package `in`.parksathi.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.maps.android.compose.*
import `in`.parksathi.app.BuildConfig
import `in`.parksathi.app.dto.NearbyParkingSpot
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun ExploreScreen(viewModel: ExploreViewModel = viewModel()) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Initialize Places SDK
    val placesClient = remember {
        if (!Places.isInitialized()) {
            Places.initialize(context, BuildConfig.MAPS_API_KEY)
        }
        Places.createClient(context)
    }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasLocationPermission = isGranted
        }
    )

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val defaultLocation = LatLng(20.5937, 78.9629)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 5f)
    }

    val parkingSpots by viewModel.nearbyParkingSpots
    val selectedSpot by viewModel.selectedSpot
    val isLoading by viewModel.isLoading

    // Search state
    var searchQuery by remember { mutableStateOf("") }
    var predictions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
            uiSettings = MapUiSettings(myLocationButtonEnabled = true),
            onMapClick = { 
                viewModel.selectSpot(null)
                isSearching = false
            }
        ) {
            parkingSpots.forEach { spot ->
                Marker(
                    state = MarkerState(position = LatLng(spot.coordinates.lat, spot.coordinates.lng)),
                    title = spot.parkingName,
                    onClick = {
                        viewModel.selectSpot(spot)
                        scope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(spot.coordinates.lat, spot.coordinates.lng),
                                    15f
                                )
                            )
                        }
                        true
                    }
                )
            }
        }

        // Search UI
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter)
        ) {
            SearchBar(
                query = searchQuery,
                onQueryChange = { query ->
                    searchQuery = query
                    if (query.isNotEmpty()) {
                        getPredictions(placesClient, query) { predictions = it }
                    } else {
                        predictions = emptyList()
                    }
                },
                onSearchActiveChange = { isSearching = it },
                active = isSearching,
                onClear = {
                    searchQuery = ""
                    predictions = emptyList()
                    isSearching = false
                }
            )

            if (isSearching && predictions.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 4.dp,
                    shadowElevation = 8.dp
                ) {
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(predictions) { prediction ->
                            ListItem(
                                headlineContent = { Text(prediction.getPrimaryText(null).toString()) },
                                supportingContent = { Text(prediction.getSecondaryText(null).toString()) },
                                leadingContent = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                                modifier = Modifier.clickable {
                                    val placeId = prediction.placeId
                                    fetchPlaceDetails(placesClient, placeId) { latLng ->
                                        searchQuery = prediction.getFullText(null).toString()
                                        isSearching = false
                                        scope.launch {
                                            cameraPositionState.animate(
                                                CameraUpdateFactory.newLatLngZoom(latLng, 14f)
                                            )
                                            viewModel.fetchNearbyParking(latLng.latitude, latLng.longitude)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // Parking Detail Card (Pop up)
        AnimatedVisibility(
            visible = selectedSpot != null,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            selectedSpot?.let { spot ->
                ParkingDetailCard(spot = spot, onBookNow = {
                    // Navigate to booking flow
                })
            }
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchActiveChange: (Boolean) -> Unit,
    active: Boolean,
    onClear: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(8.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { if (it.isFocused) onSearchActiveChange(true) },
            placeholder = { Text("Search for parking...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty() || active) {
                    IconButton(onClick = onClear) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            singleLine = true
        )
    }
}

@Composable
fun ParkingDetailCard(spot: NearbyParkingSpot, onBookNow: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = spot.parkingName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${String.format(Locale.getDefault(), "%.1f", spot.distance)} km",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = spot.address, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Available Slots",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        text = "${spot.availableSlots} / ${spot.slots}",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (spot.availableSlots > 5) Color(0xFF2E7D32) else Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Button(
                    onClick = onBookNow,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text("Book Now", fontSize = 16.sp)
                }
            }
        }
    }
}

// Helpers for Places API
private fun getPredictions(client: PlacesClient, query: String, onResult: (List<AutocompletePrediction>) -> Unit) {
    val request = FindAutocompletePredictionsRequest.builder()
        .setQuery(query)
        .build()
    client.findAutocompletePredictions(request)
        .addOnSuccessListener { response -> onResult(response.autocompletePredictions) }
        .addOnFailureListener { e -> Log.e("Places", "Prediction fetch failed", e) }
}

private fun fetchPlaceDetails(client: PlacesClient, placeId: String, onResult: (LatLng) -> Unit) {
    val placeFields = listOf(Place.Field.LOCATION)
    val request = FetchPlaceRequest.newInstance(placeId, placeFields)
    client.fetchPlace(request)
        .addOnSuccessListener { response -> response.place.location?.let { onResult(it) } }
        .addOnFailureListener { e -> Log.e("Places", "Place details fetch failed", e) }
}