package `in`.parksathi.partner.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.maps.android.compose.*
import `in`.parksathi.partner.config.RetrofitClient
import `in`.parksathi.partner.ui.navigation.Screen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParkingDetailsScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sharedPreferences = context.getSharedPreferences("parksathi_prefs", Context.MODE_PRIVATE)

    var parkingName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var twoWheelerSlots by remember { mutableStateOf("") }
    var idProof by remember { mutableStateOf("") }
    var licenseUri by remember { mutableStateOf<Uri?>(null) }
    
    var isLoading by remember { mutableStateOf(false) }

    // Coordinates
    var latitude by remember { mutableDoubleStateOf(0.0) }
    var longitude by remember { mutableDoubleStateOf(0.0) }
    
    var showMapDialog by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> licenseUri = uri }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            showMapDialog = true
        } else {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    val isFormValid = parkingName.isNotBlank() && 
                      address.isNotBlank() && 
                      phoneNumber.isNotBlank() && 
                      twoWheelerSlots.isNotBlank() &&
                      licenseUri != null &&
                      latitude != 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Register Your Parking") }
            )
        }
    ) { padding ->

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {

                ElevatedCard(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {

                        OutlinedTextField(
                            value = parkingName,
                            onValueChange = { parkingName = it },
                            label = { Text("Parking Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Address") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                IconButton(onClick = {
                                    permissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }) {
                                    Icon(Icons.Default.LocationOn, contentDescription = "Pick on map", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        )
                        
                        if (latitude != 0.0) {
                            Text(
                                text = "Selected Location: ${String.format("%.4f", latitude)}, ${String.format("%.4f", longitude)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }

                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = { Text("Phone Number") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )

                        OutlinedTextField(
                            value = twoWheelerSlots,
                            onValueChange = { if (it.all { char -> char.isDigit() }) twoWheelerSlots = it },
                            label = { Text("Available 2-Wheeler Slots") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            placeholder = { Text("e.g. 50") }
                        )

                        OutlinedTextField(
                            value = idProof,
                            onValueChange = { idProof = it },
                            label = { Text("ID Proof Name (e.g. Aadhaar)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                Text("Verification File", style = MaterialTheme.typography.titleMedium)

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    onClick = { launcher.launch("*/*") }
                ) {
                    Row(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        Icon(
                            imageVector = if (licenseUri != null) Icons.Default.CheckCircle else Icons.Default.Add,
                            contentDescription = null,
                            tint = if (licenseUri != null) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Column {
                            Text(
                                text = if (licenseUri != null) "File Uploaded" else "Upload Verification File",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "ID Proof document or License (Image/PDF)",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                licenseUri?.let {
                    Text(
                        text = "Selected: ${getFileName(context, it)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            val success = submitParkingDetails(
                                context = context,
                                parkingName = parkingName,
                                address = address,
                                phoneNumber = phoneNumber,
                                slots = twoWheelerSlots,
                                idProof = idProof,
                                lat = latitude,
                                lng = longitude,
                                fileUri = licenseUri!!
                            )
                            isLoading = false
                            if (success) {
                                sharedPreferences.edit().putBoolean("isParkingFormSubmitted", true).apply()
                                Toast.makeText(context, "Registration Successful!", Toast.LENGTH_SHORT).show()
                                navController.navigate(Screen.PendingVerification.route) {
                                    popUpTo(Screen.ParkingDetails.route) { inclusive = true }
                                }
                            } else {
                                Toast.makeText(context, "Registration Failed. Try again.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = isFormValid && !isLoading,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text("Submit Parking Details")
                    }
                }
            }
        }
    }

    if (showMapDialog) {
        LocationPickerDialog(
            onDismiss = { showMapDialog = false },
            onLocationSelected = { latLng, fetchedAddress ->
                latitude = latLng.latitude
                longitude = latLng.longitude
                if (fetchedAddress.isNotBlank()) {
                    address = fetchedAddress
                }
                showMapDialog = false
            }
        )
    }
}

private suspend fun submitParkingDetails(
    context: Context,
    parkingName: String,
    address: String,
    phoneNumber: String,
    slots: String,
    idProof: String,
    lat: Double,
    lng: Double,
    fileUri: Uri
): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            val user = FirebaseAuth.getInstance().currentUser
            val token = user?.getIdToken(true)?.await()?.token ?: return@withContext false
            val authHeader = "Bearer $token"

            val parkingNamePart = parkingName.toRequestBody("text/plain".toMediaTypeOrNull())
            val addressPart = address.toRequestBody("text/plain".toMediaTypeOrNull())
            val phonePart = phoneNumber.toRequestBody("text/plain".toMediaTypeOrNull())
            val idProofPart = idProof.toRequestBody("text/plain".toMediaTypeOrNull())
            val slotsPart = slots.toRequestBody("text/plain".toMediaTypeOrNull())
            val latPart = lat.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val lngPart = lng.toString().toRequestBody("text/plain".toMediaTypeOrNull())

            val file = getFileFromUri(context, fileUri) ?: return@withContext false
            val requestFile = file.asRequestBody(context.contentResolver.getType(fileUri)?.toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

            val response = RetrofitClient.instance.submitParkingDetails(
                parkingNamePart,
                addressPart,
                phonePart,
                idProofPart,
                slotsPart,
                latPart,
                lngPart,
                body,
                authHeader
            )

            if (response.isSuccessful) {
                Log.d("ParkingDetails", "Submission successful")
                true
            } else {
                Log.e("ParkingDetails", "Submission failed: ${response.errorBody()?.string()}")
                false
            }
        } catch (e: Exception) {
            Log.e("ParkingDetails", "Error submitting: ${e.message}", e)
            false
        }
    }
}

private fun getFileFromUri(context: Context, uri: Uri): File? {
    val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
    val fileName = getFileName(context, uri) ?: "temp_file"
    val tempFile = File(context.cacheDir, fileName)
    try {
        inputStream?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    } catch (e: Exception) {
        return null
    }
}

@SuppressLint("Range")
private fun getFileName(context: Context, uri: Uri): String? {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                result = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            }
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/') ?: -1
        if (cut != -1) {
            result = result?.substring(cut + 1)
        }
    }
    return result
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun LocationPickerDialog(
    onDismiss: () -> Unit,
    onLocationSelected: (LatLng, String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val defaultPos = LatLng(20.5937, 78.9629)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultPos, 5f)
    }
    
    var markerPosition by remember { mutableStateOf<LatLng?>(null) }
    var isFetchingAddress by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val currentLatLng = LatLng(it.latitude, it.longitude)
                cameraPositionState.position = CameraPosition.fromLatLngZoom(currentLatLng, 15f)
                markerPosition = currentLatLng
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    onMapClick = { markerPosition = it },
                    uiSettings = MapUiSettings(myLocationButtonEnabled = true),
                    properties = MapProperties(isMyLocationEnabled = true)
                ) {
                    markerPosition?.let {
                        Marker(
                            state = MarkerState(position = it),
                            title = "Parking Location"
                        )
                    }
                }

                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Select Location",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            maxLines = 1
                        )
                    },

                    navigationIcon = {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },

                    actions = {
                        IconButton(
                            onClick = {
                                markerPosition?.let { pos ->
                                    isFetchingAddress = true
                                    scope.launch {
                                        val addr = fetchAddress(context, pos)
                                        onLocationSelected(pos, addr)
                                        isFetchingAddress = false
                                    }
                                }
                            },
                            enabled = markerPosition != null && !isFetchingAddress,
                            modifier = Modifier.size(40.dp)
                        ) {
                            if (isFetchingAddress) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Confirm",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },

                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    ),

                    modifier = Modifier.shadow(4.dp)
                )
                
                if (markerPosition == null) {
                    Text(
                        "Tap on map to mark parking",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        color = Color.White
                    )
                }
            }
        }
    }
}

private suspend fun fetchAddress(context: Context, latLng: LatLng): String {
    return withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    addresses[0].getAddressLine(0) ?: ""
                } else ""
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    addresses[0].getAddressLine(0) ?: ""
                } else ""
            }
        } catch (e: Exception) {
            ""
        }
    }
}
