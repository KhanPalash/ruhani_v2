package com.khanscreations.ruhani.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khanscreations.ruhani.R

// Serene Sanctuary Cartography Palette — Deep Mosque Emerald, Warm Ivory, Brushed Brass Gold
val MasjidEmeraldPrimary = Color(0xFF0F6B52)
private val MasjidEmeraldOnPrimary = Color(0xFFFFFFFF)
private val MasjidEmeraldPrimaryContainer = Color(0xFFCFECE1)
private val MasjidEmeraldOnPrimaryContainer = Color(0xFF06291F)

val MasjidBrassSecondary = Color(0xFFB57F1E)
private val MasjidBrassOnSecondary = Color(0xFFFFFFFF)
private val MasjidBrassSecondaryContainer = Color(0xFFF8E6C2)
private val MasjidBrassOnSecondaryContainer = Color(0xFF3A2500)

private val MasjidTealTertiary = Color(0xFF285F6B)
private val MasjidTealOnTertiary = Color(0xFFFFFFFF)
private val MasjidTealTertiaryContainer = Color(0xFFC6E9F2)
private val MasjidTealOnTertiaryContainer = Color(0xFF06262E)

private val MasjidIvoryBackground = Color(0xFFFAF8F4)
private val MasjidIvorySurface = Color(0xFFFAF8F4)
private val MasjidIvorySurfaceContainerLow = Color(0xFFF4F1EA)
private val MasjidIvorySurfaceContainer = Color(0xFFECE8DF)
private val MasjidIvorySurfaceContainerHigh = Color(0xFFE4DFD5)
private val MasjidInkOnSurface = Color(0xFF14211D)
private val MasjidInkOnSurfaceVariant = Color(0xFF475751)
private val MasjidWarmOutline = Color(0xFF77857F)
private val MasjidWarmOutlineVariant = Color(0xFFC8D0CB)

// Dark Theme Tokens — Night Prayer Charcoal & Luminous Emerald
private val MasjidEmeraldDarkPrimary = Color(0xFF68D7B2)
private val MasjidEmeraldDarkOnPrimary = Color(0xFF003829)
private val MasjidEmeraldDarkPrimaryContainer = Color(0xFF08523E)
private val MasjidEmeraldDarkOnPrimaryContainer = Color(0xFFCFECE1)

private val MasjidBrassDarkSecondary = Color(0xFFEBC16E)
private val MasjidBrassDarkOnSecondary = Color(0xFF422C00)
private val MasjidBrassDarkSecondaryContainer = Color(0xFF5F4100)
private val MasjidBrassDarkOnSecondaryContainer = Color(0xFFF8E6C2)

private val MasjidTealDarkTertiary = Color(0xFF94CFE0)
private val MasjidTealDarkOnTertiary = Color(0xFF003641)
private val MasjidTealDarkTertiaryContainer = Color(0xFF0B4E5C)
private val MasjidTealDarkOnTertiaryContainer = Color(0xFFC6E9F2)

private val MasjidNightBackground = Color(0xFF0B1512)
private val MasjidNightSurface = Color(0xFF0B1512)
private val MasjidNightSurfaceContainerLow = Color(0xFF121E1A)
private val MasjidNightSurfaceContainer = Color(0xFF182722)
private val MasjidNightSurfaceContainerHigh = Color(0xFF22332D)
private val MasjidNightOnSurface = Color(0xFFE3ECE8)
private val MasjidNightOnSurfaceVariant = Color(0xFFADC0B8)
private val MasjidNightOutline = Color(0xFF798C84)
private val MasjidNightOutlineVariant = Color(0xFF31443D)

val MasjidLivePulseGreen = Color(0xFF12B76A)

private val MasjidLightColorScheme = lightColorScheme(
    primary = MasjidEmeraldPrimary,
    onPrimary = MasjidEmeraldOnPrimary,
    primaryContainer = MasjidEmeraldPrimaryContainer,
    onPrimaryContainer = MasjidEmeraldOnPrimaryContainer,
    secondary = MasjidBrassSecondary,
    onSecondary = MasjidBrassOnSecondary,
    secondaryContainer = MasjidBrassSecondaryContainer,
    onSecondaryContainer = MasjidBrassOnSecondaryContainer,
    tertiary = MasjidTealTertiary,
    onTertiary = MasjidTealOnTertiary,
    tertiaryContainer = MasjidTealTertiaryContainer,
    onTertiaryContainer = MasjidTealOnTertiaryContainer,
    background = MasjidIvoryBackground,
    onBackground = MasjidInkOnSurface,
    surface = MasjidIvorySurface,
    onSurface = MasjidInkOnSurface,
    surfaceVariant = MasjidIvorySurfaceContainer,
    onSurfaceVariant = MasjidInkOnSurfaceVariant,
    surfaceContainerLow = MasjidIvorySurfaceContainerLow,
    surfaceContainer = MasjidIvorySurfaceContainer,
    surfaceContainerHigh = MasjidIvorySurfaceContainerHigh,
    outline = MasjidWarmOutline,
    outlineVariant = MasjidWarmOutlineVariant
)

private val MasjidDarkColorScheme = darkColorScheme(
    primary = MasjidEmeraldDarkPrimary,
    onPrimary = MasjidEmeraldDarkOnPrimary,
    primaryContainer = MasjidEmeraldDarkPrimaryContainer,
    onPrimaryContainer = MasjidEmeraldDarkOnPrimaryContainer,
    secondary = MasjidBrassDarkSecondary,
    onSecondary = MasjidBrassDarkOnSecondary,
    secondaryContainer = MasjidBrassDarkSecondaryContainer,
    onSecondaryContainer = MasjidBrassDarkOnSecondaryContainer,
    tertiary = MasjidTealDarkTertiary,
    onTertiary = MasjidTealDarkOnTertiary,
    tertiaryContainer = MasjidTealDarkTertiaryContainer,
    onTertiaryContainer = MasjidTealDarkOnTertiaryContainer,
    background = MasjidNightBackground,
    onBackground = MasjidNightOnSurface,
    surface = MasjidNightSurface,
    onSurface = MasjidNightOnSurface,
    surfaceVariant = MasjidNightSurfaceContainer,
    onSurfaceVariant = MasjidNightOnSurfaceVariant,
    surfaceContainerLow = MasjidNightSurfaceContainerLow,
    surfaceContainer = MasjidNightSurfaceContainer,
    surfaceContainerHigh = MasjidNightSurfaceContainerHigh,
    outline = MasjidNightOutline,
    outlineVariant = MasjidNightOutlineVariant
)

val OutfitFontFamily = FontFamily(
    Font(R.font.outfit, FontWeight.Normal),
    Font(R.font.outfit, FontWeight.Medium),
    Font(R.font.outfit, FontWeight.SemiBold),
    Font(R.font.outfit, FontWeight.Bold)
)

val MasjidTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.25).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleLarge = TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp
    ),
    titleSmall = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 19.sp,
        letterSpacing = 0.15.sp
    ),
    bodySmall = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.2.sp
    ),
    labelLarge = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.3.sp
    ),
    labelSmall = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.4.sp
    )
)

private val MasjidShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun MasjidNearTheme(
    themeMode: String = "SYSTEM",
    content: @Composable () -> Unit
) {
    val useDark = when (themeMode.uppercase()) {
        "LIGHT" -> false
        "DARK" -> true
        else -> isSystemInDarkTheme()
    }
    val colorScheme = if (useDark) MasjidDarkColorScheme else MasjidLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MasjidTypography,
        shapes = MasjidShapes,
        content = content
    )
}
