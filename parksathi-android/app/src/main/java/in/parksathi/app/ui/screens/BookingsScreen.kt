package `in`.parksathi.app.ui.screens

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import `in`.parksathi.app.dto.BookingResponse
import `in`.parksathi.app.dto.BookingStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingsScreen(
    onNavigateToPayment: (String) -> Unit,
    viewModel: BookingsViewModel = viewModel()
) {
    val bookings by viewModel.bookings
    val isLoading by viewModel.isLoading
    val qrBookingId by viewModel.showQrDialog

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
                        BookingItem(
                            booking = booking,
                            onCancel = { viewModel.cancelBooking(booking.bookingId) },
                            onGenerateQr = { viewModel.showQr(booking.bookingId) },
                            onPay = { onNavigateToPayment(booking.bookingId) }
                        )
                    }
                }
            }
        }
    }

    qrBookingId?.let { bookingId ->
        QrCodeDialog(bookingId = bookingId, onDismiss = { viewModel.dismissQrDialog() })
    }
}

@Composable
fun BookingItem(
    booking: BookingResponse,
    onCancel: () -> Unit,
    onGenerateQr: () -> Unit,
    onPay: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = booking.parkingName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Parking Booking",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }

                StatusBadge(status = booking.bookingStatus)
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ConfirmationNumber,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "ID: ${booking.bookingId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            if (booking.acquiredAt != null) {
                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Acquired: ${booking.acquiredAt}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            if (booking.bookingStatus == BookingStatus.BOOKED) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cancel")
                    }

                    Button(
                        onClick = onGenerateQr,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.QrCode, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Show QR")
                    }
                }
            } else if (booking.bookingStatus == BookingStatus.ACQUIRED) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onPay,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Icon(Icons.Default.Payment, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pay Now", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: BookingStatus? = null) {
    val color = when (status) {
        BookingStatus.BOOKED -> Color(0xFF2E7D32)
        BookingStatus.EXPIRED -> Color(0xFFF57C00)
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

@Composable
fun QrCodeDialog(bookingId: String, onDismiss: () -> Unit) {
    val qrBitmap = remember(bookingId) {
        generateQrCode(bookingId, 512)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Booking QR Code",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                Surface(
                    modifier = Modifier.size(240.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color.LightGray),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "QR Code",
                        modifier = Modifier.fillMaxSize().padding(12.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = bookingId,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Show this QR to the parking owner to acquire your spot.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    }
}

fun generateQrCode(text: String, size: Int): Bitmap {
    val writer = QRCodeWriter()
    val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size)
    val width = bitMatrix.width
    val height = bitMatrix.height
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
    for (x in 0 until width) {
        for (y in 0 until height) {
            bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) AndroidColor.BLACK else AndroidColor.WHITE)
        }
    }
    return bitmap
}
