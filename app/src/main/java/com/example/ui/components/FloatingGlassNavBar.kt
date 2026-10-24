package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.AppScreen
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.BackgroundDarkSecondary
import com.example.ui.theme.LuxuryGold
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextWhite

data class NavItem(
    val screen: AppScreen,
    val title: String,
    val icon: ImageVector,
    val isSpecial: Boolean = false
)

@Composable
fun FloatingGlassNavBar(
    currentScreen: AppScreen,
    isAdmin: Boolean,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = mutableListOf(
        NavItem(AppScreen.Home, "Home", Icons.Default.Home),
        NavItem(AppScreen.Services, "Services", Icons.Default.FormatListBulleted),
        NavItem(AppScreen.Orders, "Orders", Icons.Default.ShoppingBag),
        NavItem(AppScreen.Wallet, "Wallet", Icons.Default.AccountBalanceWallet),
        NavItem(AppScreen.Profile, "Profile", Icons.Default.Person)
    )

    if (isAdmin) {
        items.add(NavItem(AppScreen.Admin, "Admin", Icons.Default.AdminPanelSettings, isSpecial = true))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(16.dp, RoundedCornerShape(28.dp), spotColor = Color(0x6600F5FF))
                .clip(RoundedCornerShape(28.dp))
                .background(BackgroundDarkSecondary.copy(alpha = 0.92f))
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color(0x6600F5FF),
                            Color(0x22FFFFFF),
                            Color(0x440072FE)
                        )
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentScreen == item.screen
                val activeColor = if (item.isSpecial) LuxuryGold else AccentCyan

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onNavigate(item.screen) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .then(
                                if (isSelected) {
                                    Modifier
                                        .clip(CircleShape)
                                        .background(activeColor.copy(alpha = 0.18f))
                                        .border(1.dp, activeColor.copy(alpha = 0.4f), CircleShape)
                                } else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = if (isSelected) activeColor else TextDim,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = item.title,
                        color = if (isSelected) TextWhite else TextDim,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
