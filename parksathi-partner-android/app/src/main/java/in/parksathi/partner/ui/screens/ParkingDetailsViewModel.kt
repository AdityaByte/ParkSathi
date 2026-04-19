package `in`.parksathi.partner.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import `in`.parksathi.partner.api.ApiService
import `in`.parksathi.partner.config.RetrofitClient
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

class ParkingDetailsViewModel : ViewModel() {

    var parkingName by mutableStateOf("")
    var address by mutableStateOf("")
    var phoneNumber by mutableStateOf("")
    var slots by mutableStateOf("")
    var idProof by mutableStateOf("")
    var hourlyRate by mutableStateOf("")
    var verificationUri by mutableStateOf<Uri?>(null)
    var verificationFileName by mutableStateOf<String?>(null)

    var latitude by mutableDoubleStateOf(0.0)
    var longitude by mutableDoubleStateOf(0.0)

    var isLoading by mutableStateOf(false)
    var isSuccess by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    val isFormValid: Boolean
        get() = parkingName.isNotBlank() &&
                address.isNotBlank() &&
                phoneNumber.isNotBlank() &&
                slots.isNotBlank() &&
                hourlyRate.isNotBlank() &&
                verificationUri != null &&
                latitude != 0.0

    fun onFileSelected(context: Context, uri: Uri?) {
        verificationUri = uri
        verificationFileName = uri?.let { getFileName(context, it) }
    }

    fun submitDetails(context: Context) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            
            val success = submitToApi(context)
            
            if (success) {
                isSuccess = true
            } else {
                errorMessage = "Registration Failed. Please try again."
            }
            isLoading = false
        }
    }

    private suspend fun submitToApi(context: Context): Boolean {
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
                val hourlyRatePart = hourlyRate.toRequestBody("text/plain".toMediaTypeOrNull())
                val latPart = latitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val lngPart = longitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())

                val file = getFileFromUri(context, verificationUri!!) ?: return@withContext false
                val requestFile = file.asRequestBody(context.contentResolver.getType(verificationUri!!)?.toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                val response = RetrofitClient.instance.submitParkingDetails(
                    parkingNamePart,
                    addressPart,
                    phonePart,
                    idProofPart,
                    slotsPart,
                    hourlyRatePart,
                    latPart,
                    lngPart,
                    body,
                    authHeader
                )

                if (response.isSuccessful) {
                    true
                } else {
                    Log.e("ParkingDetailsVM", "Submission failed: ${response.errorBody()?.string()}")
                    false
                }
            } catch (e: Exception) {
                Log.e("ParkingDetailsVM", "Error submitting: ${e.message}", e)
                false
            }
        }
    }

    private fun getFileFromUri(context: Context, uri: Uri): File? {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val fileName = getFileName(context, uri) ?: "temp_file"
        val tempFile = File(context.cacheDir, fileName)
        return try {
            inputStream?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
            tempFile
        } catch (e: Exception) {
            null
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
}
