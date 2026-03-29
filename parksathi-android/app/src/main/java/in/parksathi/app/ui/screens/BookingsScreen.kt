package `in`.parksathi.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import `in`.parksathi.app.dto.BookingResponse
import `in`.parksathi.app.dto.BookingStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingsScreen(viewModel: BookingsViewModel = viewModel()) {
    val bookings by viewModel.bookings
    val isLoading by viewModel.isLoading

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Bookings") }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (bookings.isEmpty()) {
                Text(
                    text = "No bookings found",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(bookings) { booking ->
                        BookingItem(booking)
                    }
                }
            }
        }
    }
}

@Composable
fun BookingItem(booking: BookingResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = booking.parkingName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                StatusBadge(status = booking.bookingStatus)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Booking ID: ${booking.bookingId}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            
            if (booking.acquiredAt != null) {
                Text(
                    text = "Acquired at: ${booking.acquiredAt}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun StatusBadge(status: BookingStatus? = null) {
    val color = when (status) {
        BookingStatus.CONFIRMED -> Color(0xFF2E7D32)
        BookingStatus.PENDING -> Color(0xFFF57C00)
        BookingStatus.CANCELLED -> Color(0xFFD32F2F)
        BookingStatus.COMPLETED -> Color(0xFF1976D2)
        BookingStatus.ACQUIRED -> Color(0xFF7B1FA2)
        else -> Color.Gray
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = status?.name ?: "UNKNOWN",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}
