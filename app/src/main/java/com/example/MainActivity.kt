package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.data.FirebaseService
import com.example.ui.components.FloatingGlassNavBar
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OrdersScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ServicesScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.WalletScreen
import com.example.ui.screens.admin.AdminPanelScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContent()
            }
        }
    }
}

@Composable
fun MainAppContent() {
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Splash) }
    val userProfile by FirebaseService.currentUserProfile.collectAsState()
    val authUser = FirebaseService.auth.currentUser
    val isAdmin = userProfile?.isUserAdmin(authUser) == true

    // System Back Press handling
    BackHandler(enabled = currentScreen != AppScreen.Home && currentScreen != AppScreen.Auth && currentScreen != AppScreen.Splash) {
        if (currentScreen == AppScreen.Admin) {
            currentScreen = AppScreen.Profile
        } else {
            currentScreen = AppScreen.Home
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            if (currentScreen != AppScreen.Splash && currentScreen != AppScreen.Auth && currentScreen != AppScreen.Admin) {
                FloatingGlassNavBar(
                    currentScreen = currentScreen,
                    isAdmin = isAdmin,
                    onNavigate = { currentScreen = it }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentScreen) {
                AppScreen.Splash -> {
                    SplashScreen(
                        onSplashFinished = { isLoggedIn ->
                            currentScreen = if (isLoggedIn) AppScreen.Home else AppScreen.Auth
                        }
                    )
                }
                AppScreen.Auth -> {
                    AuthScreen(
                        onLoginSuccess = {
                            currentScreen = AppScreen.Home
                        }
                    )
                }
                AppScreen.Home -> {
                    HomeScreen(
                        currentUser = userProfile,
                        onNavigateToServices = { currentScreen = AppScreen.Services },
                        onNavigateToWallet = { currentScreen = AppScreen.Wallet },
                        onNavigateToOrders = { currentScreen = AppScreen.Orders }
                    )
                }
                AppScreen.Services -> {
                    ServicesScreen(
                        currentUser = userProfile,
                        onNavigateToWallet = { currentScreen = AppScreen.Wallet },
                        onNavigateToOrders = { currentScreen = AppScreen.Orders }
                    )
                }
                AppScreen.Orders -> {
                    OrdersScreen(
                        currentUser = userProfile
                    )
                }
                AppScreen.Wallet -> {
                    WalletScreen(
                        currentUser = userProfile
                    )
                }
                AppScreen.Profile -> {
                    ProfileScreen(
                        currentUser = userProfile,
                        onNavigateToAdmin = {
                            if (isAdmin) currentScreen = AppScreen.Admin
                        },
                        onSignOut = {
                            FirebaseService.auth.signOut()
                            currentScreen = AppScreen.Auth
                        }
                    )
                }
                AppScreen.Admin -> {
                    if (isAdmin) {
                        AdminPanelScreen(
                            onNavigateBack = { currentScreen = AppScreen.Profile }
                        )
                    } else {
                        // Ordinary user safety redirect
                        currentScreen = AppScreen.Home
                    }
                }
            }
        }
    }
}
