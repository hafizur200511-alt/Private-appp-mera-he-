package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.CurrencyUtils
import com.example.data.FirebaseService
import com.example.model.Order
import com.example.model.ServiceItem
import com.example.model.UserProfile
import com.example.ui.components.GlassCard
import com.example.ui.components.LuxuryButton
import com.example.ui.screens.admin.customFieldColors
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.BackgroundDarkSecondary
import com.example.ui.theme.BackgroundSurface
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenLight
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.LuxuryGold
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.WarningAmber
import kotlinx.coroutines.launch

@Composable
fun NewOrderSheet(
    service: ServiceItem,
    currentUser: UserProfile?,
    onDismiss: () -> Unit,
    onNavigateToWallet: () -> Unit,
    onOrderPlaced: (Order) -> Unit
) {
    val scope = rememberCoroutineScope()
    var targetLink by remember { mutableStateOf("") }
    var quantityText by remember { mutableStateOf(service.minQuantity.toString()) }
    var isPlacing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var placedOrder by remember { mutableStateOf<Order?>(null) }

    val quantity = quantityText.toLongOrNull() ?: 0L
    val totalPaise = if (quantity > 0) service.calculateTotalPaise(quantity) else 0L
    val walletBalancePaise = currentUser?.walletBalancePaise ?: 0L
    val hasSufficientBalance = walletBalancePaise >= totalPaise

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp))
                .background(BackgroundDarkSecondary)
                .border(
                    1.dp,
                    Brush.horizontalGradient(listOf(AccentCyan.copy(alpha = 0.5f), Color(0x330072FE))),
                    RoundedCornerShape(24.dp)
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "New Order",
                            color = TextWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${service.platform} • ${service.category}",
                            color = AccentCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextDim)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (placedOrder != null) {
                    // Success View
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldGreen,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Order Placed Successfully!",
                            color = TextWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Order ID: #${placedOrder!!.orderId}",
                            color = AccentCyan,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Deducted: ${placedOrder!!.formattedPrice} | Status: ${placedOrder!!.status}",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        LuxuryButton(
                            text = "View Orders",
                            onClick = {
                                val o = placedOrder!!
                                onOrderPlaced(o)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    // Service Details Card
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = service.name,
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = service.description,
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Rate: ${service.formattedPriceUnit}",
                                    color = EmeraldGreenLight,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Min: ${service.minQuantity} | Max: ${service.maxQuantity}",
                                    color = TextDim,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (errorMessage != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(ErrorRed.copy(alpha = 0.15f))
                                .border(1.dp, ErrorRed.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Text(errorMessage!!, color = ErrorRed, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Target Link Field
                    OutlinedTextField(
                        value = targetLink,
                        onValueChange = { targetLink = it },
                        label = { Text("Target Link / Username / URL") },
                        placeholder = { Text("e.g. https://instagram.com/p/...") },
                        leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = AccentCyan) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = customFieldColors()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quantity Field
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it },
                        label = { Text("Order Quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = customFieldColors()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Calculation & Balance Card
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Amount:", color = TextMuted, fontSize = 13.sp)
                                Text(
                                    text = CurrencyUtils.formatPaiseToRupee(totalPaise),
                                    color = EmeraldGreenLight,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Your Wallet Balance:", color = TextMuted, fontSize = 13.sp)
                                Text(
                                    text = CurrencyUtils.formatPaiseToRupee(walletBalancePaise),
                                    color = if (hasSufficientBalance) TextWhite else ErrorRed,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                            }

                            if (!hasSufficientBalance && totalPaise > 0) {
                                Spacer(modifier = Modifier.height(10.dp))
                                val needed = CurrencyUtils.formatPaiseToRupee(totalPaise - walletBalancePaise)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Need $needed more",
                                        color = WarningAmber,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Button(
                                        onClick = onNavigateToWallet,
                                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan.copy(alpha = 0.2f))
                                    ) {
                                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Recharge Now", color = AccentCyan, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    LuxuryButton(
                        text = if (hasSufficientBalance) "Submit Order" else "Recharge Wallet",
                        onClick = {
                            if (!hasSufficientBalance) {
                                onNavigateToWallet()
                                return@LuxuryButton
                            }

                            if (targetLink.isBlank()) {
                                errorMessage = "Please enter target link"
                                return@LuxuryButton
                            }

                            if (quantity < service.minQuantity || quantity > service.maxQuantity) {
                                errorMessage = "Quantity must be between ${service.minQuantity} and ${service.maxQuantity}"
                                return@LuxuryButton
                            }

                            isPlacing = true
                            errorMessage = null

                            scope.launch {
                                val result = FirebaseService.placeOrder(
                                    service = service,
                                    targetLink = targetLink.trim(),
                                    quantity = quantity
                                )
                                isPlacing = false
                                if (result.isSuccess) {
                                    placedOrder = result.getOrNull()
                                } else {
                                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to place order"
                                }
                            }
                        },
                        isLoading = isPlacing,
                        leadingIcon = Icons.Default.ShoppingCart,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
