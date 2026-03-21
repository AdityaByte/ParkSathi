package `in`.parksathi.app.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

enum class UserRole {
    DRIVER, OWNER
}

enum class SlotStatus {
    AVAILABLE, OCCUPIED, RESERVED, MAINTENANCE
}

data class ParkingSlot(val id: String, val name: String, val status: SlotStatus)

@Composable
fun DashboardScreen(navController: NavController, context: Context) {
    var userRole by remember { mutableStateOf(UserRole.DRIVER) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var showRoleSwitch by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            DashboardBottomNavigation(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                userRole = userRole
            )
        },
        floatingActionButton = {
            if (userRole == UserRole.OWNER) {
                FloatingActionButton(
                    onClick = { /* Handle Alert Action */ },
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.error
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = "Alerts")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (userRole == UserRole.DRIVER) {
                // DRIVER UI
                MapPlaceholder()
                TopSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onProfileClick = { showRoleSwitch = true }
                )
            } else {
                // OWNER UI
                OwnerDashboardContent()
                // Minimal top bar for role switching
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(16.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    IconButton(
                        onClick = { showRoleSwitch = true },
                        modifier = Modifier
                            .size(48.dp)
                            .shadow(4.dp, CircleShape)
                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                    ) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                }
            }

            // Role Switch Dialog
            if (showRoleSwitch) {
                AlertDialog(
                    onDismissRequest = { showRoleSwitch = false },
                    title = { Text("Switch Role") },
                    text = { Text("Currently acting as: ${userRole.name}") },
                    confirmButton = {
                        Button(onClick = {
                            userRole = if (userRole == UserRole.DRIVER) UserRole.OWNER else UserRole.DRIVER
                            selectedTab = 0
                            showRoleSwitch = false
                        }) {
                            Text("Switch to ${if (userRole == UserRole.DRIVER) "Owner" else "Driver"}")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showRoleSwitch = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun OwnerDashboardContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .statusBarsPadding()
    ) {
        Text(
            text = "Owner Dashboard",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        // Earnings & Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatsCard("Occupancy", "85%", Icons.Default.Info, Modifier.weight(1f))
            StatsCard("Earnings", "₹4,250", Icons.Default.ShoppingCart, Modifier.weight(1f))
            StatsCard("Alerts", "2", Icons.Default.Warning, Modifier.weight(1f), isAlert = true)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Slot Monitor",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Dummy Slots
        val dummySlots = remember {
            List(20) { i ->
                ParkingSlot(
                    id = "$i",
                    name = "S-${i + 1}",
                    status = SlotStatus.entries.toTypedArray().random()
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(dummySlots) { slot ->
                SlotItem(slot)
            }
        }
    }
}

@Composable
fun StatsCard(label: String, value: String, icon: ImageVector, modifier: Modifier, isAlert: Boolean = false) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isAlert) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(
                icon, 
                contentDescription = null, 
                tint = if (isAlert) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SlotItem(slot: ParkingSlot) {
    var showMenu by remember { mutableStateOf(false) }

    val bgColor = when (slot.status) {
        SlotStatus.AVAILABLE -> Color(0xFF4CAF50)
        SlotStatus.OCCUPIED -> Color(0xFFF44336)
        SlotStatus.RESERVED -> Color(0xFFFFC107)
        SlotStatus.MAINTENANCE -> Color(0xFF9E9E9E)
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable { showMenu = true },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = slot.name,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )

        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
            DropdownMenuItem(
                text = { Text("Release Slot") },
                onClick = { showMenu = false },
                leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null) }
            )
            DropdownMenuItem(
                text = { Text("Maintenance") },
                onClick = { showMenu = false },
                leadingIcon = { Icon(Icons.Default.Build, contentDescription = null) }
            )
        }
    }
}

@Composable
fun MapPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = "Driver View: Map",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun TopSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
                .shadow(8.dp, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(12.dp))
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp),
                    decorationBox = { innerTextField ->
                        if (query.isEmpty()) {
                            Text(
                                text = "Search for parking...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                fontSize = 16.sp
                            )
                        }
                        innerTextField()
                    }
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        IconButton(
            onClick = onProfileClick,
            modifier = Modifier
                .size(56.dp)
                .shadow(8.dp, CircleShape)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
        ) {
            Icon(Icons.Default.Person, contentDescription = "Profile", tint = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
fun DashboardBottomNavigation(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    userRole: UserRole
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        val items = if (userRole == UserRole.DRIVER) {
            listOf(
                NavigationItem("Home", Icons.Default.Home),
                NavigationItem("Bookings", Icons.Default.DateRange),
                NavigationItem("Wallet", Icons.Default.ShoppingCart)
            )
        } else {
            listOf(
                NavigationItem("Monitor", Icons.Default.Home),
                NavigationItem("Analytics", Icons.Default.Menu), // Replaced BarChart
                NavigationItem("Profile", Icons.Default.Person)
            )
        }

        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                label = { Text(text = item.label) },
                icon = { Icon(imageVector = item.icon, contentDescription = item.label) }
            )
        }
    }
}

data class NavigationItem(val label: String, val icon: ImageVector)
