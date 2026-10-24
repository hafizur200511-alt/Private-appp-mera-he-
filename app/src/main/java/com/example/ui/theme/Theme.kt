package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = TextWhite,
    secondary = AccentCyan,
    onSecondary = BackgroundDark,
    tertiary = EmeraldGreen,
    onTertiary = TextWhite,
    background = BackgroundDark,
    onBackground = TextWhite,
    surface = BackgroundSurface,
    onSurface = TextWhite,
    surfaceVariant = BackgroundDarkSecondary,
    onSurfaceVariant = TextMuted,
    error = ErrorRed,
    onError = TextWhite
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

