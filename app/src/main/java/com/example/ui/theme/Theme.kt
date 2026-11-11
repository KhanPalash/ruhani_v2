package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp

private val NoorDarkColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = PureWhite,
    primaryContainer = EmeraldDarkContainer,
    onPrimaryContainer = PureWhite,
    secondary = EmeraldBright,
    onSecondary = PureBlack,
    secondaryContainer = NoorSurface3,
    onSecondaryContainer = PureWhite,
    tertiary = SignalRed,
    onTertiary = PureWhite,
    tertiaryContainer = SignalRedDark,
    onTertiaryContainer = PureWhite,
    background = PureBlack,
    onBackground = PureWhite,
    surface = NoorSurface1,
    onSurface = PureWhite,
    surfaceVariant = NoorSurface2,
    onSurfaceVariant = NoorTextSecondary,
    outline = NoorOutline,
    error = SignalRed,
    onError = PureWhite
)

private val VibrantLightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = PureWhite,
    primaryContainer = EmeraldHalkaLight,
    onPrimaryContainer = PureBlack,
    secondary = EmeraldDeep,
    onSecondary = PureWhite,
    secondaryContainer = EmeraldHalkaMid,
    onSecondaryContainer = PureBlack,
    tertiary = SignalRed,
    onTertiary = PureWhite,
    tertiaryContainer = SignalRedSoft,
    onTertiaryContainer = PureBlack,
    background = PureWhite,
    onBackground = PureBlack,
    surface = VibrantSurface,
    onSurface = PureBlack,
    surfaceVariant = VibrantSurfaceVariant,
    onSurfaceVariant = VibrantTextSecondary,
    outline = VibrantOutline,
    error = SignalRed,
    onError = PureWhite
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

@Composable
fun ruhaniCardBrush(): Brush = if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) {
    RuhaniHalkaCardBrush
} else {
    RuhaniHalkaLightCardBrush
}
