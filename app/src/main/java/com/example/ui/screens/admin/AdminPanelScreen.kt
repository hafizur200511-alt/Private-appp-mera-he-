package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CurrencyUtils
import com.example.data.FirebaseService
import com.example.model.ApiProvider
import com.example.model.CategoryItem
import com.example.model.Order
import com.example.model.PaymentRequest
import com.example.model.ServiceItem
import com.example.model.UserProfile
import com.example.ui.components.AnimatedGlowingBackground
import com.example.ui.components.GlassCard
import com.example.ui.components.LuxuryButton
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BackgroundDarkSecondary
import com.example.ui.theme.BackgroundSurface
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenLight
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.LuxuryGold
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.WarningAmber
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminPanelScreen(
    onNavigateBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(AdminTab.OVERVIEW) }
    val scope = rememberCoroutineScope()

    var showServiceDialog by remember { mutableStateOf(false) }
    var serviceToEdit by remember { mutableStateOf<ServiceItem?>(null) }

    var showCategoryDialog by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<CategoryItem?>(null) }

    var showProviderDialog by remember { mutableStateOf(false) }
    var providerToEdit by remember { mutableStateOf<ApiProvider?>(null) }

    // Refund Dialog
    var showRefundDialog by remember { mutableStateOf(false) }
    var orderToRefund by remember { mutableStateOf<Order?>(null) }
    var refundReason by remember { mutableStateOf("Order rejected/unfulfillable") }

    // Live Data collections
    val services by FirebaseService.getServicesFlow().collectAsState(initial = emptyList())
    val categories by FirebaseService.getCategoriesFlow().collectAsState(initial = emptyList())
    val providers by FirebaseService.getApiProvidersFlow().collectAsState(initial = emptyList())
    val orders by FirebaseService.getAllOrdersFlow().collectAsState(initial = emptyList())
    val recharges by FirebaseService.getAllRechargesFlow().collectAsState(initial = emptyList())
    val users by FirebaseService.getAllUsersFlow().collectAsState(initial = emptyList())

    // Admin Stats
    var stats by remember { mutableStateOf(FirebaseService.AdminStats()) }
    var isLoadingStats by remember { mutableStateOf(false) }

    val refreshStats: () -> Unit = {
        scope.launch {
            isLoadingStats = true
            stats = FirebaseService.getAdminStats()
            isLoadingStats = false
        }
    }

    LaunchedEffect(Unit) {
        refreshStats()
    }

    AnimatedGlowingBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BackgroundDarkSecondary.copy(alpha = 0.9f))
                        .padding(top = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onNavigateBack) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextWhite)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "MEGA SMM",
                                        color = TextWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(LuxuryGold.copy(alpha = 0.2f))
                                            .border(1.dp, LuxuryGold, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("ADMIN", color = LuxuryGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Text(
                                    text = "Authorized: ${FirebaseService.ADMIN_EMAIL}",
                                    color = TextDim,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        IconButton(onClick = refreshStats) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = AccentCyan)
                        }
                    }

                    // Tab bar (horizontal scrollable)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AdminTab.values().forEach { tab ->
                            val isSelected = selectedTab == tab
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) AccentCyan.copy(alpha = 0.2f) else BackgroundSurface)
                                    .border(1.dp, if (isSelected) AccentCyan else Color(0x22CBD5E1), RoundedCornerShape(12.dp))
                                    .clickable { selectedTab = tab }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = tab.title,
                                    color = if (isSelected) AccentCyan else TextMuted,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                when (selectedTab) {
                    AdminTab.OVERVIEW -> {
                        OverviewSection(
                            stats = stats,
                            onAddNewService = {
                                serviceToEdit = null
                                showServiceDialog = true
                            },
                            onTabSelected = { selectedTab = it }
                        )
                    }
                    AdminTab.SERVICES -> {
                        ServicesAdminSection(
                            services = services,
                            onAddNew = {
                                serviceToEdit = null
                                showServiceDialog = true
                            },
                            onEdit = {
                                serviceToEdit = it
                                showServiceDialog = true
                            },
                            onToggleStatus = { srv, newStatus ->
                                scope.launch {
                                    FirebaseService.updateServiceStatus(srv.id, newStatus)
                                }
                            }
                        )
                    }
                    AdminTab.CATEGORIES -> {
                        CategoriesAdminSection(
                            categories = categories,
                            onAddNew = {
                                categoryToEdit = null
                                showCategoryDialog = true
                            },
                            onDelete = { cat ->
                                scope.launch {
                                    FirebaseService.deleteCategory(cat.id)
                                }
                            }
                        )
                    }
                    AdminTab.PROVIDERS -> {
                        ProvidersAdminSection(
                            providers = providers,
                            onAddNew = {
                                providerToEdit = null
                                showProviderDialog = true
                            },
                            onEdit = {
                                providerToEdit = it
                                showProviderDialog = true
                            },
                            onDelete = { prov ->
                                scope.launch {
                                    FirebaseService.deleteApiProvider(prov.id)
                                }
                            }
                        )
                    }
                    AdminTab.ORDERS -> {
                        OrdersAdminSection(
                            orders = orders,
                            onUpdateStatus = { ord, st ->
                                scope.launch {
                                    FirebaseService.updateOrderStatus(ord.id, st)
                                }
                            },
                            onRefund = { ord ->
                                orderToRefund = ord
                                showRefundDialog = true
                            }
                        )
                    }
                    AdminTab.RECHARGES -> {
                        RechargesAdminSection(
                            recharges = recharges,
                            onApprove = { req ->
                                scope.launch {
                                    FirebaseService.approveRecharge(req.id)
                                    refreshStats()
                                }
                            },
                            onReject = { req, reason ->
                                scope.launch {
                                    FirebaseService.rejectRecharge(req.id, reason)
                                    refreshStats()
                                }
                            }
                        )
                    }
                    AdminTab.USERS -> {
                        UsersAdminSection(users = users)
                    }
                    AdminTab.WALLET -> {
                        WalletAdjustmentSection(users = users)
                    }
                    AdminTab.REVENUE -> {
                        RevenueReportSection(stats = stats, orders = orders, recharges = recharges)
                    }
                    AdminTab.SETTINGS -> {
                        AdminSettingsSection()
                    }
                }
            }
        }
    }

    if (showServiceDialog) {
        ServiceFormDialog(
            serviceToEdit = serviceToEdit,
            categories = categories,
            providers = providers,
            onDismiss = { showServiceDialog = false },
            onSaved = {
                showServiceDialog = false
                refreshStats()
            }
        )
    }

    if (showCategoryDialog) {
        CategoryManagerDialog(
            categoryToEdit = categoryToEdit,
            onDismiss = { showCategoryDialog = false },
            onSaved = { showCategoryDialog = false }
        )
    }

    if (showProviderDialog) {
        ApiProviderDialog(
            providerToEdit = providerToEdit,
            onDismiss = { showProviderDialog = false },
            onSaved = { showProviderDialog = false }
        )
    }

    if (showRefundDialog && orderToRefund != null) {
        val ord = orderToRefund!!
        AlertDialog(
            onDismissRequest = { showRefundDialog = false },
            title = { Text("Reject & Refund Order #${ord.orderId}?", color = TextWhite) },
            text = {
                Column {
                    Text(
                        "Refund Amount: ${ord.formattedPrice} to user wallet.",
                        color = EmeraldGreenLight,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = refundReason,
                        onValueChange = { refundReason = it },
                        label = { Text("Refund Reason") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = customFieldColors()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRefundDialog = false
                        scope.launch {
                            FirebaseService.refundAndCancelOrder(ord.id, refundReason)
                            refreshStats()
                        }
                    }
                ) {
                    Text("Refund & Cancel", color = ErrorRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRefundDialog = false }) {
                    Text("Cancel", color = TextDim)
                }
            },
            containerColor = BackgroundSurface
        )
    }
}

// ==========================================
// 1. OVERVIEW SECTION
// ==========================================
@Composable
fun OverviewSection(
    stats: FirebaseService.AdminStats,
    onAddNewService: () -> Unit,
    onTabSelected: (AdminTab) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Prominent Action Button
            LuxuryButton(
                text = "+ ADD NEW SERVICE",
                onClick = onAddNewService,
                leadingIcon = Icons.Default.Add,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    title = "Total Revenue",
                    value = CurrencyUtils.formatPaiseToRupee(stats.totalRevenuePaise),
                    icon = Icons.Default.AttachMoney,
                    accentColor = EmeraldGreen,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Approved Deposits",
                    value = CurrencyUtils.formatPaiseToRupee(stats.totalApprovedRechargePaise),
                    icon = Icons.Default.AttachMoney,
                    accentColor = AccentCyan,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    title = "Total Orders",
                    value = stats.totalOrders.toString(),
                    icon = Icons.Default.ShoppingBag,
                    subtitle = "${stats.completedOrders} completed",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Pending Orders",
                    value = stats.pendingOrders.toString(),
                    icon = Icons.Default.ShoppingBag,
                    accentColor = WarningAmber,
                    subtitle = "${stats.inProgressOrders} in progress",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    title = "Pending Recharges",
                    value = stats.pendingRecharges.toString(),
                    icon = Icons.Default.AttachMoney,
                    accentColor = if (stats.pendingRecharges > 0) WarningAmber else AccentCyan,
                    subtitle = "UPI verification queue",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Total Users",
                    value = stats.totalUsers.toString(),
                    icon = Icons.Default.People,
                    accentColor = PrimaryBlue,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    title = "Total Services",
                    value = stats.totalServices.toString(),
                    icon = Icons.Default.Layers,
                    subtitle = "${stats.activeServices} Active",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Failed Orders",
                    value = stats.failedOrders.toString(),
                    icon = Icons.Default.Close,
                    accentColor = ErrorRed,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ==========================================
// 2. SERVICES ADMIN SECTION
// ==========================================
@Composable
fun ServicesAdminSection(
    services: List<ServiceItem>,
    onAddNew: () -> Unit,
    onEdit: (ServiceItem) -> Unit,
    onToggleStatus: (ServiceItem, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            LuxuryButton(
                text = "+ ADD NEW SERVICE",
                onClick = onAddNew,
                leadingIcon = Icons.Default.Add,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (services.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No services found. Click '+ ADD NEW SERVICE' above.", color = TextMuted)
                }
            }
        }

        items(services, key = { it.id }) { srv ->
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(PrimaryBlue.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("#${srv.serviceId}", color = AccentCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${srv.platform} • ${srv.category}",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                        StatusBadge(srv.status)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = srv.name,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = srv.formattedPriceUnit,
                            color = EmeraldGreenLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )

                        Text(
                            text = "Min: ${srv.minQuantity} | Max: ${srv.maxQuantity}",
                            color = TextDim,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val nextStatus = if (srv.status == "ACTIVE") "INACTIVE" else "ACTIVE"
                        TextButton(onClick = { onToggleStatus(srv, nextStatus) }) {
                            Text(if (srv.status == "ACTIVE") "Deactivate" else "Activate", color = if (srv.status == "ACTIVE") WarningAmber else EmeraldGreen, fontSize = 12.sp)
                        }

                        Button(
                            onClick = { onEdit(srv) },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan.copy(alpha = 0.2f))
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit", color = AccentCyan, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// ==========================================
// 3. CATEGORIES ADMIN SECTION
// ==========================================
@Composable
fun CategoriesAdminSection(
    categories: List<CategoryItem>,
    onAddNew: () -> Unit,
    onDelete: (CategoryItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            LuxuryButton(
                text = "+ ADD CATEGORY",
                onClick = onAddNew,
                leadingIcon = Icons.Default.Add,
                modifier = Modifier.fillMaxWidth()
            )
        }

        items(categories, key = { it.id }) { cat ->
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(cat.name, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Platform: ${cat.platform} | Order: ${cat.displayOrder}", color = TextDim, fontSize = 12.sp)
                    }
                    IconButton(onClick = { onDelete(cat) }) {
                        Icon(Icons.Default.Close, contentDescription = "Delete", tint = ErrorRed)
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. API PROVIDERS ADMIN SECTION
// ==========================================
@Composable
fun ProvidersAdminSection(
    providers: List<ApiProvider>,
    onAddNew: () -> Unit,
    onEdit: (ApiProvider) -> Unit,
    onDelete: (ApiProvider) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            LuxuryButton(
                text = "+ ADD API PROVIDER",
                onClick = onAddNew,
                leadingIcon = Icons.Default.Add,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (providers.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No API Providers configured. Add one to enable automatic dispatch.", color = TextMuted)
                }
            }
        }

        items(providers, key = { it.id }) { prov ->
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(prov.name, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        StatusBadge(if (prov.enabled) "ACTIVE" else "INACTIVE")
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(prov.baseUrl, color = TextMuted, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = { onDelete(prov) }) {
                            Icon(Icons.Default.Close, contentDescription = "Delete", tint = ErrorRed)
                        }
                        IconButton(onClick = { onEdit(prov) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = AccentCyan)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. ORDERS ADMIN SECTION
// ==========================================
@Composable
fun OrdersAdminSection(
    orders: List<Order>,
    onUpdateStatus: (Order, String) -> Unit,
    onRefund: (Order) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text("All Platform Orders (${orders.size})", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        if (orders.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                    Text("No orders placed yet", color = TextMuted)
                }
            }
        }

        items(orders, key = { it.id }) { ord ->
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("#${ord.orderId}", color = AccentCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        StatusBadge(ord.status)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(ord.serviceName, color = TextWhite, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Link: ${ord.targetLink}", color = TextDim, fontSize = 11.sp)
                    Text("Qty: ${ord.quantity} | Total: ${ord.formattedPrice} | User: ${ord.userEmail}", color = TextMuted, fontSize = 11.sp)

                    if (ord.adminNotes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Note: ${ord.adminNotes}", color = WarningAmber, fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(dateFormat.format(Date(ord.createdAt)), color = TextDim, fontSize = 10.sp)

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (ord.status != "COMPLETED") {
                            TextButton(onClick = { onUpdateStatus(ord, "COMPLETED") }) {
                                Text("Complete", color = EmeraldGreen, fontSize = 12.sp)
                            }
                        }
                        if (ord.status == "PENDING" || ord.status == "AWAITING_ADMIN") {
                            TextButton(onClick = { onUpdateStatus(ord, "IN_PROGRESS") }) {
                                Text("In Progress", color = AccentCyan, fontSize = 12.sp)
                            }
                        }
                        if (ord.canBeCancelledOrRefunded) {
                            TextButton(onClick = { onRefund(ord) }) {
                                Text("Refund", color = ErrorRed, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 6. RECHARGES ADMIN SECTION
// ==========================================
@Composable
fun RechargesAdminSection(
    recharges: List<PaymentRequest>,
    onApprove: (PaymentRequest) -> Unit,
    onReject: (PaymentRequest, String) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
    var rejectingReq by remember { mutableStateOf<PaymentRequest?>(null) }
    var rejectReason by remember { mutableStateOf("Invalid UTR / Payment not received") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text("UPI Recharge Requests (${recharges.size})", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        if (recharges.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                    Text("No recharge requests found", color = TextMuted)
                }
            }
        }

        items(recharges, key = { it.id }) { req ->
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(req.formattedAmount, color = EmeraldGreenLight, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        StatusBadge(req.status)
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text("UTR: ${req.utrNumber}", color = AccentCyan, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text("User: ${req.userEmail}", color = TextMuted, fontSize = 12.sp)
                    Text("Submitted: ${dateFormat.format(Date(req.createdAt))}", color = TextDim, fontSize = 11.sp)

                    if (req.adminNotes.isNotBlank()) {
                        Text("Admin Note: ${req.adminNotes}", color = WarningAmber, fontSize = 11.sp)
                    }

                    if (req.status == "PENDING") {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { rejectingReq = req }) {
                                Text("Reject", color = ErrorRed, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { onApprove(req) },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                            ) {
                                Text("Approve & Credit", color = TextWhite, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    if (rejectingReq != null) {
        val target = rejectingReq!!
        AlertDialog(
            onDismissRequest = { rejectingReq = null },
            title = { Text("Reject Recharge Request?", color = TextWhite) },
            text = {
                Column {
                    Text("Rejecting UTR ${target.utrNumber} (${target.formattedAmount})", color = TextMuted)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        label = { Text("Reason for rejection") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = customFieldColors()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val req = rejectingReq!!
                        rejectingReq = null
                        onReject(req, rejectReason)
                    }
                ) {
                    Text("Confirm Reject", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { rejectingReq = null }) {
                    Text("Cancel", color = TextDim)
                }
            },
            containerColor = BackgroundSurface
        )
    }
}

// ==========================================
// 7. USERS ADMIN SECTION
// ==========================================
@Composable
fun UsersAdminSection(users: List<UserProfile>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Registered Users (${users.size})", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        items(users, key = { it.uid }) { user ->
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(user.getEffectiveName(), color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            if (user.role == "admin") {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ADMIN", color = LuxuryGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text(user.email, color = TextMuted, fontSize = 12.sp)
                        Text("UID: ${user.uid.take(12)}...", color = TextDim, fontSize = 10.sp)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(user.formattedBalance, color = EmeraldGreenLight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("${user.totalOrders} Orders", color = TextDim, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// ==========================================
// 8. WALLET ADJUSTMENT SECTION
// ==========================================
@Composable
fun WalletAdjustmentSection(users: List<UserProfile>) {
    var targetEmail by remember { mutableStateOf("") }
    var amountRupees by remember { mutableStateOf("") }
    var isCredit by remember { mutableStateOf(true) }
    var reason by remember { mutableStateOf("Admin adjustment") }
    var isProcessing by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Manual Wallet Credit / Debit", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("Directly adjust user wallet balance with full ledger record", color = TextMuted, fontSize = 12.sp)
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (statusMessage != null) {
                        Text(statusMessage!!, color = if (statusMessage!!.startsWith("Success")) EmeraldGreenLight else ErrorRed, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    OutlinedTextField(
                        value = targetEmail,
                        onValueChange = { targetEmail = it },
                        label = { Text("User Email or UID") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = customFieldColors()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = amountRupees,
                        onValueChange = { amountRupees = it },
                        label = { Text("Amount in Rupees (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        colors = customFieldColors()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { isCredit = true },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isCredit) EmeraldGreen else BackgroundDarkSecondary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+ Credit", color = TextWhite)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = { isCredit = false },
                            colors = ButtonDefaults.buttonColors(containerColor = if (!isCredit) ErrorRed else BackgroundDarkSecondary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("- Debit", color = TextWhite)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Reason / Reference Note") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = customFieldColors()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LuxuryButton(
                        text = if (isCredit) "Credit Wallet" else "Debit Wallet",
                        onClick = {
                            val parsedAmount = CurrencyUtils.rupeeStringToPaise(amountRupees)
                            if (targetEmail.isBlank()) {
                                statusMessage = "Please enter user email or UID"
                                return@LuxuryButton
                            }
                            if (parsedAmount == null || parsedAmount <= 0) {
                                statusMessage = "Please enter a valid amount"
                                return@LuxuryButton
                            }

                            val matchedUser = users.find {
                                it.email.equals(targetEmail.trim(), ignoreCase = true) || it.uid == targetEmail.trim()
                            }

                            if (matchedUser == null) {
                                statusMessage = "User not found with email/UID: $targetEmail"
                                return@LuxuryButton
                            }

                            isProcessing = true
                            scope.launch {
                                val res = FirebaseService.adminAdjustUserBalance(
                                    userId = matchedUser.uid,
                                    amountPaise = parsedAmount,
                                    isCredit = isCredit,
                                    reason = reason
                                )
                                isProcessing = false
                                if (res.isSuccess) {
                                    statusMessage = "Success! Balance updated for ${matchedUser.email}"
                                    amountRupees = ""
                                } else {
                                    statusMessage = res.exceptionOrNull()?.message
                                }
                            }
                        },
                        isLoading = isProcessing,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// ==========================================
// 9. REVENUE & REPORTS SECTION
// ==========================================
@Composable
fun RevenueReportSection(
    stats: FirebaseService.AdminStats,
    orders: List<Order>,
    recharges: List<PaymentRequest>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Revenue & Performance Analytics", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("FINANCIAL SUMMARY", color = AccentCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Approved Deposits:", color = TextMuted, fontSize = 13.sp)
                        Text(CurrencyUtils.formatPaiseToRupee(stats.totalApprovedRechargePaise), color = EmeraldGreenLight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Order Spend (Completed):", color = TextMuted, fontSize = 13.sp)
                        Text(CurrencyUtils.formatPaiseToRupee(stats.totalRevenuePaise), color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Pending Recharge Queue:", color = TextMuted, fontSize = 13.sp)
                        Text("${stats.pendingRecharges} requests", color = WarningAmber, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("ORDER METRICS", color = AccentCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Processed Orders:", color = TextMuted, fontSize = 13.sp)
                        Text("${stats.totalOrders}", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Fulfillment Success Rate:", color = TextMuted, fontSize = 13.sp)
                        val rate = if (stats.totalOrders > 0) (stats.completedOrders * 100 / stats.totalOrders) else 100
                        Text("$rate%", color = EmeraldGreenLight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

// ==========================================
// 10. APPLICATION SETTINGS SECTION
// ==========================================
@Composable
fun AdminSettingsSection() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text("System & Security Settings", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("ADMIN AUTHORIZATION & MASTER KEY", color = LuxuryGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Authorized Administrator: ${FirebaseService.ADMIN_EMAIL}", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Master Security PIN: 7860 (Use this PIN anywhere in the app to instantly unlock Admin Mode)", color = LuxuryGold, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Admin role is guarded on both server and client layer. You have full privileges to add/edit/delete services, manage orders, approve/reject UPI deposits, and adjust user balances.", color = TextMuted, fontSize = 12.sp)
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("UPI PAYMENT GATEWAY", color = AccentCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Payee VPA: rahmansmm@fam", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text("Payee Name: MEGA SMM INDIA", color = TextWhite, fontSize = 13.sp)
                    Text("Instagram Support: @megasmmindia", color = TextWhite, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Recharge submission requires strict 12-digit numeric UTR validation and Firestore duplicate collision check.", color = TextMuted, fontSize = 12.sp)
                }
            }
        }
    }
}
