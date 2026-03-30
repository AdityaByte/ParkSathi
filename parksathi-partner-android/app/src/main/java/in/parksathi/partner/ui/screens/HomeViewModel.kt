package `in`.parksathi.partner.ui.screens

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import `in`.parksathi.partner.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.*
import java.util.concurrent.TimeUnit

data class DashboardResponse(
    val total_income: Double = 0.0,
    val booked_slots: Int = 0,
    val total_slots: Int = 0,
    val acquired_slots: Int = 0,
    val available_slots: Int = 0
)

class HomeViewModel : ViewModel() {

    private val _uiState = mutableStateOf(DashboardResponse())
    val uiState: State<DashboardResponse> = _uiState

    private val _slots = mutableStateOf<List<Slot>>(emptyList())
    val slots: State<List<Slot>> = _slots

    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    private val gson = Gson()

    fun loadDummyData() {
        val total = 40
        val booked = 10
        val acquired = 15
        _uiState.value = DashboardResponse(
            total_income = 12450.0,
            booked_slots = booked,
            total_slots = total,
            acquired_slots = acquired,
            available_slots = total - booked - acquired
        )
        updateSlots(total, acquired, booked)
    }

    fun startWebSocket() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = FirebaseAuth.getInstance().currentUser
                val token = user?.getIdToken(true)?.await()?.token ?: return@launch
                
                val baseUrl = BuildConfig.BACKEND_ORIGIN.replace("http", "ws")
                val request = Request.Builder()
                    .url(baseUrl+ "ws/partner?token=$token")
                    .build()

                webSocket = client.newWebSocket(request, object : WebSocketListener() {
                    override fun onMessage(webSocket: WebSocket, text: String) {
                        try {
                            val response = gson.fromJson(text, DashboardResponse::class.java)
                            _uiState.value = response
                            updateSlots(response.total_slots, response.acquired_slots, response.booked_slots)
                        } catch (e: Exception) {
                            Log.e("HomeViewModel", "Error parsing WS message", e)
                        }
                    }

                    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                        Log.e("HomeViewModel", "WS Failure: ${t.message}")
                        viewModelScope.launch {
                            delay(5000)
                            startWebSocket()
                        }
                    }
                })
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error starting WebSocket", e)
            }
        }
    }

    private fun updateSlots(total: Int, acquired: Int, booked: Int) {
        _slots.value = List(total) { i ->
            when {
                i < acquired -> Slot(i, SlotStatus.ACQUIRED)
                i < acquired + booked -> Slot(i, SlotStatus.BOOKED)
                else -> Slot(i, SlotStatus.EMPTY)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        webSocket?.close(1000, "ViewModel Cleared")
    }
}
