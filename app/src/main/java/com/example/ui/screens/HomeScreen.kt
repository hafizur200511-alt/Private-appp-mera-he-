package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CurrencyUtils
import com.example.data.FirebaseService
import com.example.model.Order
import com.example.model.ServiceItem
import com.example.model.UserProfile
import com.example.ui.components.AnimatedGlowingBackground
import com.example.ui.components.GlassCard
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.BackgroundDarkSecondary
import com.example.ui.theme.BackgroundSurface
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenLight
import com.example.ui.theme.InstagramGradient
import com.example.ui.theme.LuxuryGold
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun HomeScreen(
    currentUser: UserProfile?,
    onNavigateToServices: () -> Unit,
    onNavigateToWallet: () -> Unit,
    onNavigateToOrders: () -> Unit
) {
    val context = LocalContext.current
    val authUser = FirebaseService.auth.currentUser
    val effectiveName = currentUser?.getEffectiveName(authUser) ?: "Member"

    val services by FirebaseService.getServicesFlow().collectAsState(initial = emptyList())
    val userOrders by FirebaseService.getUserOrdersFlow(currentUser?.uid ?: "").collectAsState(initial = emptyList())

    var selectedServiceForOrder by remember { mutableStateOf<ServiceItem?>(null) }

    AnimatedGlowingBackground {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                // Top User Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Hello, $effectiveName 👋",
                            color = TextWhite,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "MEGA SMM INDIA Panel",
                            color = AccentCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (currentUser?.isUserAdmin(authUser) == true) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(LuxuryGold.copy(alpha = 0.2f))
                                .border(1.dp, LuxuryGold, RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("ADMIN", color = LuxuryGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Wallet Balance Highlight Card
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp,
                    borderBrush = Brush.linearGradient(
                        listOf(EmeraldGreen.copy(alpha = 0.6f), AccentCyan.copy(alpha = 0.4f), Color(0x330072FE))
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Available Balance", color = TextMuted, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = currentUser?.formattedBalance ?: "₹0.00",
                                    color = TextWhite,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }

                            Button(
                                onClick = onNavigateToWallet,
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = TextWhite, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Funds", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(BackgroundSurface.copy(alpha = 0.6f))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.FlashOn, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Instant UPI QR Deposit (UTR in 12 digits)",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Quick Stats Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Total Orders",
                        value = "${currentUser?.totalOrders ?: 0}",
                        icon = Icons.Default.ShoppingBag,
                        accentColor = AccentCyan,
                        modifier = Modifier.weight(1f)
                    )

                    StatCard(
                        title = "Active Services",
                        value = "${services.count { it.status == "ACTIVE" }}",
                        icon = Icons.Default.Star,
                        accentColor = LuxuryGold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Instagram Official Support Banner
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val igIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/megasmmindia"))
                        context.startActivity(igIntent)
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(InstagramGradient)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, tint = TextWhite, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Need Assistance?", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("DM @megasmmindia on Instagram", color = AccentCyan, fontSize = 11.sp)
                            }
                        }

                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = TextDim, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Trending Services Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Trending Services",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Text(
                        text = "View All",
                        color = AccentCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onNavigateToServices() }
                    )
                }
            }

            val activeServices = services.filter { it.status == "ACTIVE" }.take(4)
            items(activeServices, key = { it.id }) { service ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { selectedServiceForOrder = service }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(PrimaryBlue.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(service.platform, color = AccentCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(service.category, color = TextMuted, fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(service.name, color = TextWhite, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(service.formattedPriceUnit, color = EmeraldGreenLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Tap to order", color = AccentCyan, fontSize = 11.sp)
                        }
                    }
                }
            }

            // Recent Orders Section
            if (userOrders.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Orders",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "History",
                            color = AccentCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { onNavigateToOrders() }
                        )
                    }
                }

                items(userOrders.take(3), key = { it.id }) { order ->
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("#${order.orderId}", color = AccentCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(order.serviceName, color = TextWhite, fontSize = 12.sp)
                            }
                            StatusBadge(order.status)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    if (selectedServiceForOrder != null) {
        NewOrderSheet(
            service = selectedServiceForOrder!!,
            currentUser = currentUser,
            onDismiss = { selectedServiceForOrder = null },
            onNavigateToWallet = {
                selectedServiceForOrder = null
                onNavigateToWallet()
            },
            onOrderPlaced = {
                selectedServiceForOrder = null
                onNavigateToOrders()
            }
        )
    }
}
