package `in`.parksathi.partner.ui.screens

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

class BookingViewModel: ViewModel() {

    private val _bookings = mutableStateOf<List<Booking>>(emptyList())

    val bookings: State<List<Booking>> = _bookings

    init {
        dummyBookings()
    }

    fun dummyBookings() {
         _bookings.value = listOf(
            Booking("1", "John Doe", "MH12 AB 1234", "10:30 AM"),
            Booking("2", "Alice Smith", "DL01 CD 5678", "11:15 AM"),
            Booking("3", "Bob Wilson", "KA05 EF 9012", "12:00 PM"),
            Booking("4", "Emma Brown", "TS07 GH 3456", "01:45 PM"),
            Booking("5", "Charlie Davis", "UP14 IJ 7890", "02:30 PM"),
            Booking("6", "Rahul Verma", "BR01 XY 1122", "03:15 PM"),
            Booking("7", "Sonia Gandhi", "DL03 MS 9999", "04:00 PM")
        )
    }
}