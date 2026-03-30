package `in`.parksathi.partner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel()) {
    val uiState by viewModel.uiState
    val slots by viewModel.slots
    var showScanner by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.startWebSocket()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Dashboard", 
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleLarge
                    ) 
                },
                actions = {
                    IconButton(onClick = { showScanner = true }) {
                        // Using a standard icon as QrCodeScanner requires extended icons dependency
                        Icon(
                            imageVector = Icons.Default.Add, 
                            contentDescription = "Scan QR"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SummaryCard(
                                "Total Income",
                                "₹ ${uiState.total_income}",
                                MaterialTheme.colorScheme.primaryContainer,
                                Modifier.weight(1f)
                            )
                            SummaryCard(
                                "Total Slots",
                                uiState.total_slots.toString(),
                                MaterialTheme.colorScheme.secondaryContainer,
                                Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SummaryCard(
                                "Booked",
                                uiState.booked_slots.toString(),
                                Color(0xFFFFE0B2),
                                Modifier.weight(1f)
                            )
                            SummaryCard(
                                "Empty",
                                uiState.available_slots.toString(),
                                Color(0xFFE0E0E0),
                                Modifier.weight(1f)
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "Parking Layout",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        LegendItem("Acquired", Color(0xFF2E7D32))
                        LegendItem("Booked", Color(0xFFF57C00))
                        LegendItem("Empty", Color(0xFFBDBDBD))
                    }
                }

                val columns = 5
                val chunkedSlots = slots.chunked(columns)

                items(chunkedSlots) { rowSlots ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowSlots.forEach { slot ->
                            SlotItem(slot, Modifier.weight(1f))
                        }
                        repeat(columns - rowSlots.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            if (showScanner) {
                // Dummy Scanner Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Scanner Active", color = Color.White, fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(24.dp))
                        Box(
                            modifier = Modifier
                                .size(250.dp)
                                .border(2.dp, Color.Green, RoundedCornerShape(12.dp))
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = { showScanner = false }) {
                            Text("Close Scanner")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, fontSize = 12.sp, color = Color.DarkGray)
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .background(color, RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun SlotItem(slot: Slot, modifier: Modifier = Modifier) {
    val (color, textColor) = when (slot.status) {
        SlotStatus.ACQUIRED -> Color(0xFF2E7D32) to Color.White
        SlotStatus.BOOKED -> Color(0xFFF57C00) to Color.White
        SlotStatus.EMPTY -> Color(0xFFF5F5F5) to Color.DarkGray
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .background(color, RoundedCornerShape(12.dp))
            .then(
                if (slot.status == SlotStatus.EMPTY)
                    Modifier.border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = (slot.id + 1).toString(),
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
