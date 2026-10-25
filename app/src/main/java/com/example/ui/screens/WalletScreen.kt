package com.example.ui.screens

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CurrencyUtils
import com.example.data.FirebaseService
import com.example.data.QrCodeGenerator
import com.example.model.UserProfile
import com.example.ui.components.AnimatedGlowingBackground
import com.example.ui.components.GlassCard
import com.example.ui.components.LuxuryButton
import com.example.ui.components.StatusBadge
import com.example.ui.screens.admin.customFieldColors
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.BackgroundSurface
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenLight
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.WarningAmber
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WalletScreen(
    currentUser: UserProfile?
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

    var selectedTab by remember { mutableStateOf(0) } // 0: Recharge, 1: Requests, 2: Passbook
    val tabTitles = listOf("Add Funds", "Deposit Requests", "Passbook")

    val upiId = "rahmansmm@fam"
    val payeeName = "MEGA SMM INDIA"

    var rechargeAmountText by remember { mutableStateOf("100") }
    var utrInput by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var submitSuccessMessage by remember { mutableStateOf<String?>(null) }
    var submitErrorMessage by remember { mutableStateOf<String?>(null) }

    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Live streams
    val myRecharges by FirebaseService.getUserRechargesFlow(currentUser?.uid ?: "").collectAsState(initial = emptyList())
    val myLedger by FirebaseService.getUserLedgerFlow(currentUser?.uid ?: "").collectAsState(initial = emptyList())

    LaunchedEffect(rechargeAmountText) {
        val amount = rechargeAmountText.ifBlank { "0" }
        qrBitmap = QrCodeGenerator.generateUpiQrBitmap(
            upiId = upiId,
            payeeName = payeeName,
            amountInRupees = amount,
            transactionNote = "MEGA SMM Wallet Recharge",
            size = 512
        )
    }

    AnimatedGlowingBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Wallet Header & Balance
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 22.dp,
                borderBrush = Brush.linearGradient(
                    listOf(EmeraldGreen.copy(alpha = 0.5f), AccentCyan.copy(alpha = 0.4f))
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Current Balance", color = TextMuted, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentUser?.formattedBalance ?: "₹0.00",
                            color = TextWhite,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(EmeraldGreen.copy(alpha = 0.15f))
                            .border(1.dp, EmeraldGreen.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = EmeraldGreenLight,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tab Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabTitles.forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) AccentCyan.copy(alpha = 0.2f) else BackgroundSurface)
                            .border(1.dp, if (isSelected) AccentCyan else Color(0x22CBD5E1), RoundedCornerShape(12.dp))
                            .clickable { selectedTab = index }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            color = if (isSelected) AccentCyan else TextMuted,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedTab) {
                0 -> {
                    // Recharge Tab
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Scan QR with PhonePe / GPay / Paytm",
                                        color = TextWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // QR Code Container
                                    Box(
                                        modifier = Modifier
                                            .size(200.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(Color.White)
                                            .padding(10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (qrBitmap != null) {
                                            Image(
                                                bitmap = qrBitmap!!.asImageBitmap(),
                                                contentDescription = "UPI QR Code",
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // UPI ID Copy Row
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(BackgroundSurface)
                                            .border(1.dp, Color(0x22CBD5E1), RoundedCornerShape(12.dp))
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("UPI ID", color = TextDim, fontSize = 10.sp)
                                            Text(upiId, color = AccentCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }
                                        IconButton(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString(upiId))
                                                Toast.makeText(context, "UPI ID copied: $upiId", Toast.LENGTH_SHORT).show()
                                            }
                                        ) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AccentCyan, modifier = Modifier.size(18.dp))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Quick Amount Chips
                                    Text("Quick Select Amount (₹)", color = TextMuted, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        listOf("50", "100", "200", "500", "1000").forEach { preset ->
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (rechargeAmountText == preset) EmeraldGreen else BackgroundSurface)
                                                    .clickable { rechargeAmountText = preset }
                                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                            ) {
                                                Text("₹$preset", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // UTR Submission Card
                        item {
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Step 2: Enter 12-Digit UTR / Reference ID",
                                        color = TextWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "After sending payment, copy the 12-digit UTR from your UPI app receipt",
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    if (submitErrorMessage != null) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(ErrorRed.copy(alpha = 0.15f))
                                                .border(1.dp, ErrorRed.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                                .padding(8.dp)
                                        ) {
                                            Text(submitErrorMessage!!, color = ErrorRed, fontSize = 12.sp)
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }

                                    if (submitSuccessMessage != null) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(EmeraldGreen.copy(alpha = 0.15f))
                                                .border(1.dp, EmeraldGreen.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                                .padding(8.dp)
                                        ) {
                                            Text(submitSuccessMessage!!, color = EmeraldGreenLight, fontSize = 12.sp)
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }

                                    OutlinedTextField(
                                        value = rechargeAmountText,
                                        onValueChange = { rechargeAmountText = it },
                                        label = { Text("Recharge Amount (₹)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = customFieldColors()
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    val cleanUtr = utrInput.replace(" ", "")
                                    OutlinedTextField(
                                        value = utrInput,
                                        onValueChange = {
                                            if (it.length <= 16) utrInput = it
                                        },
                                        label = { Text("12-Digit UTR Number (${cleanUtr.length}/12)") },
                                        placeholder = { Text("e.g. 429182749102") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = customFieldColors()
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    LuxuryButton(
                                        text = "Verify & Submit UTR",
                                        onClick = {
                                            val parsedAmount = CurrencyUtils.rupeeStringToPaise(rechargeAmountText)
                                            val sanitizedUtr = utrInput.replace(" ", "").trim()

                                            if (parsedAmount == null || parsedAmount < 1000L) {
                                                submitErrorMessage = "Minimum deposit amount is ₹10.00"
                                                submitSuccessMessage = null
                                                return@LuxuryButton
                                            }

                                            if (sanitizedUtr.length != 12 || !sanitizedUtr.all { it.isDigit() }) {
                                                submitErrorMessage = "Invalid UTR: Must be exactly 12 numeric digits (currently ${sanitizedUtr.length})"
                                                submitSuccessMessage = null
                                                return@LuxuryButton
                                            }

                                            isSubmitting = true
                                            submitErrorMessage = null
                                            submitSuccessMessage = null

                                            scope.launch {
                                                val res = FirebaseService.submitUpiRecharge(
                                                    amountPaise = parsedAmount,
                                                    utrNumber = sanitizedUtr
                                                )
                                                isSubmitting = false
                                                if (res.isSuccess) {
                                                    submitSuccessMessage = "UTR #$sanitizedUtr submitted successfully! Your balance will be credited promptly upon admin verification."
                                                    utrInput = ""
                                                } else {
                                                    submitErrorMessage = res.exceptionOrNull()?.message ?: "Submission failed. Please check UTR and try again."
                                                }
                                            }
                                        },
                                        isLoading = isSubmitting,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
                1 -> {
                    // Deposit Requests History
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (myRecharges.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No recharge requests submitted yet", color = TextMuted)
                                }
                            }
                        }

                        items(myRecharges, key = { it.id }) { req ->
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(req.formattedAmount, color = EmeraldGreenLight, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Text("UTR: ${req.utrNumber}", color = AccentCyan, fontSize = 12.sp)
                                        Text(dateFormat.format(Date(req.createdAt)), color = TextDim, fontSize = 10.sp)
                                        if (req.adminNotes.isNotBlank()) {
                                            Text(req.adminNotes, color = WarningAmber, fontSize = 11.sp)
                                        }
                                    }

                                    StatusBadge(req.status)
                                }
                            }
                        }

                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
                2 -> {
                    // Wallet Ledger Passbook
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (myLedger.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No transaction history recorded yet", color = TextMuted)
                                }
                            }
                        }

                        items(myLedger, key = { it.id }) { entry ->
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(entry.description, color = TextWhite, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Text(dateFormat.format(Date(entry.createdAt)), color = TextDim, fontSize = 10.sp)
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        val isCredit = entry.type == "CREDIT" || entry.type == "REFUND"
                                        Text(
                                            text = entry.formattedAmount,
                                            color = if (isCredit) EmeraldGreenLight else ErrorRed,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "Bal: ${CurrencyUtils.formatPaiseToRupee(entry.balanceAfterPaise)}",
                                            color = TextMuted,
                                            fontSize = 11.sp
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
    }
}
