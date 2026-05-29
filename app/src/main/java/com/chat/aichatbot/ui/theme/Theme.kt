package com.chat.aichatbot.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Premium Cosmic Space & Cyberpunk theme colors
val CosmicDark = Color(0xFF070B13)
val SlateDeep = Color(0xFF0F172A)
val IndigoNeon = Color(0xFF7C3AED)
val CyanNeon = Color(0xFF06B6D4)
val VioletSoft = Color(0xFFC084FC)
val GreyCard = Color(0xFF1E293B)

private val DarkColorScheme = darkColorScheme(
    primary = IndigoNeon,
    secondary = CyanNeon,
    tertiary = VioletSoft,
    background = CosmicDark,
    surface = SlateDeep,
    onPrimary = Color.White,
    onSecondary = CosmicDark,
    onBackground = Color(0xFFE2E8F0),
    onSurface = Color(0xFFF1F5F9)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6366F1),
    secondary = Color(0xFF0EA5E9),
    tertiary = Color(0xFFA855F7),
    background = Color(0xFFF8FAFC),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF1E293B)
)

@Composable
fun ChatBotTheme(
    darkTheme: Boolean = true, // Default to stunning dark theme!
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
