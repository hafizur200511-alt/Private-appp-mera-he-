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
    val context = androidx.compose.ui.platform.LocalContext.current
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Splash) }
    val userProfile by FirebaseService.currentUserProfile.collectAsState()
    val authUser = FirebaseService.currentAuthUser
    val isAdminUnlocked by FirebaseService.isAdminUnlocked.collectAsState()
    val isAdmin = isAdminUnlocked || userProfile?.isUserAdmin(authUser) == true || authUser?.email?.equals(FirebaseService.ADMIN_EMAIL, ignoreCase = true) == true

    androidx.compose.runtime.LaunchedEffect(Unit) {
        FirebaseService.checkStoredAdminState(context)
    }

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
                            currentScreen = if (isLoggedIn || isAdmin) AppScreen.Home else AppScreen.Auth
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
                        onNavigateToOrders = { currentScreen = AppScreen.Orders },
                        onNavigateToAdmin = { currentScreen = AppScreen.Admin }
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
                            currentScreen = AppScreen.Admin
                        },
                        onSignOut = {
                            FirebaseService.lockAdmin(context)
                            FirebaseService.auth.signOut()
                            currentScreen = AppScreen.Auth
                        }
                    )
                }
                AppScreen.Admin -> {
                    AdminPanelScreen(
                        onNavigateBack = { currentScreen = AppScreen.Profile }
                    )
                }
            }
        }
    }
}
