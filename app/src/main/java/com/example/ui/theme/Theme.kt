package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val NoorDarkColorScheme = darkColorScheme(
    primary = EmeraldBright,
    onPrimary = Color(0xFF003824),
    primaryContainer = EmeraldPrimary,
    onPrimaryContainer = Color.White,
    secondary = EmeraldPrimary,
    onSecondary = Color.White,
    secondaryContainer = NoorSurface3,
    onSecondaryContainer = EmeraldBright,
    tertiary = SignalRed,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF3B1214),
    onTertiaryContainer = Color(0xFFFFB4AB),
    background = NoorCanvas,
    onBackground = NoorTextPrimary,
    surface = NoorSurface1,
    onSurface = NoorTextPrimary,
    surfaceVariant = NoorSurface2,
    onSurfaceVariant = NoorTextSecondary,
    outline = NoorOutline,
    error = SignalRed,
    onError = Color.White
)

private val VibrantLightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6F7E8),
    onPrimaryContainer = Color(0xFF003824),
    secondary = EmeraldDeep,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE5F6EE),
    onSecondaryContainer = EmeraldDeep,
    tertiary = SignalRed,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFECEB),
    onTertiaryContainer = Color(0xFF8C0009),
    background = VibrantCanvas,
    onBackground = VibrantTextPrimary,
    surface = VibrantSurface,
    onSurface = VibrantTextPrimary,
    surfaceVariant = VibrantSurfaceVariant,
    onSurfaceVariant = VibrantTextSecondary,
    outline = VibrantOutline,
    error = SignalRed,
    onError = Color.White
)

val RuhaniShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun RuhaniTheme(
    themeMode: String = "DARK",
    content: @Composable () -> Unit
) {
    val useDark = when (themeMode.uppercase()) {
        "LIGHT" -> false
        "SYSTEM" -> isSystemInDarkTheme()
        else -> true
    }
    val colorScheme = if (useDark) NoorDarkColorScheme else VibrantLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = RuhaniShapes,
        content = content
    )
}
