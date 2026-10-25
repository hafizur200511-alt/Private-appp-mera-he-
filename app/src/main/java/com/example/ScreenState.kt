package com.example

sealed class AppScreen {
    object Splash : AppScreen()
    object Auth : AppScreen()
    object Home : AppScreen()
    object Services : AppScreen()
    object Orders : AppScreen()
    object Wallet : AppScreen()
    object Profile : AppScreen()
    object Admin : AppScreen()
}
