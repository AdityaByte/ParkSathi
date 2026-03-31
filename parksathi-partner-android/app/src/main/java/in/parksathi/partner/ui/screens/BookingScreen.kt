package `in`.parksathi.partner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import `in`.parksathi.partner.dto.OwnerBookingResponse
import `in`.parksathi.partner.enum.BookingStatus
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Composable
fun BookingScreen(viewModel: BookingViewModel = viewModel()) {
    val bookings by viewModel.bookings
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (errorMessage != null) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { viewModel.fetchBookings() }) {
                    Text("Retry")
                }
            }
        } else if (bookings.isEmpty()) {
            Text(
                text = "No bookings found",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(bookings) { booking ->
                    BookingCard(booking, onCancel = { /* TODO: Implement cancel logic */ })
                }
            }
        }
    }
}

@Composable
fun BookingCard(
    booking: OwnerBookingResponse,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(getStatusColor(booking.bookingStatus))
            )

            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = booking.userName,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    StatusChip(status = booking.bookingStatus)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "ID: ${booking.bookingId}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {

                    InfoRow("Created", formatDate(booking.createdAt))

                    booking.acquiredAt?.let {
                        if (it.isNotBlank()) InfoRow("Acquired", formatDate(it))
                    }

                    booking.completedAt?.let {
                        if (it.isNotBlank()) InfoRow("Completed", formatDate(it))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (booking.bookingStatus == BookingStatus.BOOKED ||
                    booking.bookingStatus == BookingStatus.ACQUIRED
                ) {
                    Button(
                        onClick = onCancel,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Cancel Booking",
                            color = MaterialTheme.colorScheme.onError,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: BookingStatus) {
    val color = getStatusColor(status)

    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = status.name,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun getStatusColor(status: BookingStatus): Color {
    return when (status) {
        BookingStatus.BOOKED -> Color(0xFF2196F3)
        BookingStatus.ACQUIRED -> Color(0xFF4CAF50)
        BookingStatus.COMPLETED -> Color(0xFF9E9E9E)
        BookingStatus.CANCELLED -> Color(0xFFF44336)
    }
}

fun formatDate(dateString: String): String {
    if (dateString.isBlank()) return ""
    
    val inputFormats = arrayOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSS",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd'T'HH:mm:ssXXX"
    )
    
    val outputFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    
    for (format in inputFormats) {
        try {
            val sdf = SimpleDateFormat(format, Locale.getDefault())
            if (format.endsWith("'Z'")) {
                sdf.timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = sdf.parse(dateString)
            if (date != null) {
                return outputFormat.format(date)
            }
        } catch (e: Exception) {
            continue
        }
    }
    
    return dateString
}
