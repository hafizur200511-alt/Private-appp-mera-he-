package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FirebaseService
import com.example.model.UserProfile
import com.example.ui.components.AnimatedGlowingBackground
import com.example.ui.components.GlassCard
import com.example.ui.components.LuxuryButton
import com.example.ui.components.StatCard
import com.example.ui.screens.admin.customFieldColors
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.BackgroundDarkSecondary
import com.example.ui.theme.BackgroundSurface
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenLight
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.InstagramGradient
import com.example.ui.theme.LuxuryGold
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    currentUser: UserProfile?,
    onNavigateToAdmin: () -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authUser = FirebaseService.currentAuthUser

    val effectiveName = currentUser?.getEffectiveName(authUser) ?: "Member"
    val email = currentUser?.email?.ifBlank { authUser?.email ?: "" } ?: ""
    val isAdminUnlocked by FirebaseService.isAdminUnlocked.collectAsState()
    val isAdmin = isAdminUnlocked || currentUser?.isUserAdmin(authUser) == true || authUser?.email?.equals(FirebaseService.ADMIN_EMAIL, ignoreCase = true) == true

    var showEditNameDialog by remember { mutableStateOf(false) }
    var editNameInput by remember { mutableStateOf(effectiveName) }
    var isSavingName by remember { mutableStateOf(false) }

    var showAdminPinDialog by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }

    AnimatedGlowingBackground {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Account Profile",
                    color = TextWhite,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // User Identity Card
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp,
                    borderBrush = Brush.linearGradient(
                        listOf(AccentCyan.copy(alpha = 0.5f), Color(0x330072FE), EmeraldGreen.copy(alpha = 0.4f))
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar with initials
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(AccentCyan.copy(alpha = 0.3f), PrimaryBlue.copy(alpha = 0.4f))))
                                .border(2.dp, if (isAdmin) LuxuryGold else AccentCyan, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = effectiveName.take(1).uppercase(),
                                color = if (isAdmin) LuxuryGold else AccentCyan,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = effectiveName,
                                color = TextWhite,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = {
                                    editNameInput = effectiveName
                                    showEditNameDialog = true
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Name", tint = AccentCyan, modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = email,
                            color = TextMuted,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Role Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isAdmin) LuxuryGold.copy(alpha = 0.2f) else AccentCyan.copy(alpha = 0.15f))
                                .border(1.dp, if (isAdmin) LuxuryGold else AccentCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isAdmin) "ADMINISTRATOR" else "VIP MEMBER",
                                color = if (isAdmin) LuxuryGold else AccentCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Stats Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Wallet Balance",
                        value = currentUser?.formattedBalance ?: "₹0.00",
                        icon = Icons.Default.Person,
                        accentColor = EmeraldGreenLight,
                        modifier = Modifier.weight(1f)
                    )

                    StatCard(
                        title = "Total Orders",
                        value = "${currentUser?.totalOrders ?: 0}",
                        icon = Icons.Default.ShoppingBag,
                        accentColor = AccentCyan,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Admin Dashboard Portal Card (Always accessible for owner verification)
            item {
                if (isAdmin) {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderBrush = Brush.linearGradient(listOf(LuxuryGold, Color(0xFFD97706))),
                        onClick = onNavigateToAdmin
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(LuxuryGold.copy(alpha = 0.25f))
                                            .border(1.5.dp, LuxuryGold, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = LuxuryGold, modifier = Modifier.size(22.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Admin Dashboard", color = LuxuryGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(EmeraldGreen.copy(alpha = 0.2f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text("ACTIVE", color = EmeraldGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        Text("Services, Orders, Users & UPI Controls", color = TextMuted, fontSize = 12.sp)
                                    }
                                }
                                Icon(Icons.Default.Security, contentDescription = null, tint = LuxuryGold)
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = onNavigateToAdmin,
                                    colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold),
                                    modifier = Modifier.weight(1f).height(42.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Open Admin Panel 👑", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }

                                TextButton(
                                    onClick = {
                                        FirebaseService.lockAdmin(context)
                                        Toast.makeText(context, "Admin Mode Locked", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.height(42.dp)
                                ) {
                                    Text("Lock", color = TextDim, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                } else {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderBrush = Brush.linearGradient(listOf(Color(0x55EAB308), Color(0x22FFFFFF))),
                        onClick = {
                            enteredPin = ""
                            pinError = null
                            showAdminPinDialog = true
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0x22EAB308))
                                        .border(1.dp, Color(0x66EAB308), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = LuxuryGold, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Admin Access Portal", color = LuxuryGold, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text("Enter Master PIN (7860) to unlock management panel", color = TextMuted, fontSize = 11.sp)
                                }
                            }
                            Button(
                                onClick = {
                                    enteredPin = ""
                                    pinError = null
                                    showAdminPinDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("Unlock", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Instagram Official Support
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
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(InstagramGradient)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = TextWhite, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Official Support: @megasmmindia", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Fast response for orders, API & payments", color = TextMuted, fontSize = 11.sp)
                        }
                    }
                }
            }

            // Sign Out Button
            item {
                Button(
                    onClick = onSignOut,
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed.copy(alpha = 0.15f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .border(1.dp, ErrorRed.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign Out", color = ErrorRed, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("Update Profile Name", color = TextWhite) },
            text = {
                Column {
                    Text("Enter your real name to display across your account and receipts.", color = TextMuted, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = editNameInput,
                        onValueChange = { editNameInput = it },
                        label = { Text("Your Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = customFieldColors()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (editNameInput.isNotBlank()) {
                            isSavingName = true
                            scope.launch {
                                FirebaseService.updateProfileName(editNameInput.trim())
                                isSavingName = false
                                showEditNameDialog = false
                            }
                        }
                    }
                ) {
                    Text("Save", color = AccentCyan)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text("Cancel", color = TextDim)
                }
            },
            containerColor = BackgroundSurface
        )
    }

    if (showAdminPinDialog) {
        AlertDialog(
            onDismissRequest = { showAdminPinDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = LuxuryGold, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Admin Access Verification", color = LuxuryGold, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column {
                    Text(
                        "Enter Administrator PIN to unlock full management controls (Services, Orders, Users, Wallet, UPI Settings).",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = {
                            enteredPin = it
                            pinError = null
                        },
                        label = { Text("Admin PIN (Default: 7860)") },
                        placeholder = { Text("7860") },
                        isError = pinError != null,
                        modifier = Modifier.fillMaxWidth(),
                        colors = customFieldColors(),
                        singleLine = true
                    )
                    if (pinError != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(pinError!!, color = ErrorRed, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (FirebaseService.unlockAdminWithPin(context, enteredPin)) {
                            Toast.makeText(context, "Admin Mode Unlocked! 👑", Toast.LENGTH_SHORT).show()
                            showAdminPinDialog = false
                            onNavigateToAdmin()
                        } else {
                            pinError = "Incorrect PIN! (Default: 7860)"
                        }
                    }
                ) {
                    Text("Unlock Panel", color = LuxuryGold, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdminPinDialog = false }) {
                    Text("Cancel", color = TextDim)
                }
            },
            containerColor = BackgroundSurface
        )
    }
}
