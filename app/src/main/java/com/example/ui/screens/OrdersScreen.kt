package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FirebaseService
import com.example.model.UserProfile
import com.example.ui.components.AnimatedGlowingBackground
import com.example.ui.components.GlassCard
import com.example.ui.components.StatusBadge
import com.example.ui.screens.admin.customFieldColors
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.BackgroundSurface
import com.example.ui.theme.EmeraldGreenLight
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.WarningAmber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OrdersScreen(
    currentUser: UserProfile?
) {
    val clipboardManager = LocalClipboardManager.current
    val orders by FirebaseService.getUserOrdersFlow(currentUser?.uid ?: "").collectAsState(initial = emptyList())
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    var selectedFilter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }

    val filterOptions = listOf("All", "Pending", "In Progress", "Completed", "Cancelled")

    val filteredOrders = orders.filter { ord ->
        val matchesFilter = when (selectedFilter) {
            "Pending" -> ord.status == "PENDING" || ord.status == "AWAITING_ADMIN"
            "In Progress" -> ord.status == "IN_PROGRESS" || ord.status == "PROCESSING"
            "Completed" -> ord.status == "COMPLETED"
            "Cancelled" -> ord.status == "CANCELLED" || ord.status == "FAILED" || ord.status == "REFUNDED"
            else -> true
        }
        val matchesSearch = searchQuery.isBlank() ||
                ord.orderId.contains(searchQuery, ignoreCase = true) ||
                ord.serviceName.contains(searchQuery, ignoreCase = true) ||
                ord.targetLink.contains(searchQuery, ignoreCase = true)

        matchesFilter && matchesSearch
    }

    AnimatedGlowingBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "My Orders",
                color = TextWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Real-time delivery progress & order history",
                color = TextMuted,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by Order ID or Link...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AccentCyan) },
                modifier = Modifier.fillMaxWidth(),
                colors = customFieldColors(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Status Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filterOptions.forEach { filter ->
                    val isSelected = selectedFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) AccentCyan.copy(alpha = 0.2f) else BackgroundSurface)
                            .border(1.dp, if (isSelected) AccentCyan else Color(0x22CBD5E1), RoundedCornerShape(12.dp))
                            .clickable { selectedFilter = filter }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = filter,
                            color = if (isSelected) AccentCyan else TextMuted,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (filteredOrders.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No orders match your filter", color = TextMuted)
                        }
                    }
                }

                items(filteredOrders, key = { it.id }) { order ->
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "#${order.orderId}",
                                        color = AccentCyan,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(order.orderId))
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy Order ID",
                                            tint = TextDim,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }

                                StatusBadge(order.status)
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = order.serviceName,
                                color = TextWhite,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Target: ${order.targetLink}",
                                color = TextDim,
                                fontSize = 11.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Qty: ${order.quantity} | Total: ${order.formattedPrice}",
                                    color = EmeraldGreenLight,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )

                                Text(
                                    text = dateFormat.format(Date(order.createdAt)),
                                    color = TextDim,
                                    fontSize = 10.sp
                                )
                            }

                            if (order.adminNotes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Admin Note: ${order.adminNotes}",
                                    color = WarningAmber,
                                    fontSize = 11.sp
                                )
                            }

                            if (order.refundedAmountPaise > 0L) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Refunded: ${order.formattedRefunded} to your wallet",
                                    color = EmeraldGreenLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}
