package com.example.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Core User-Requested Schema: #14A673 (Halka Gradient), #F02828, #FFFFFF, #000000
val EmeraldPrimary = Color(0xFF14A673)
val SignalRed = Color(0xFFF02828)
val PureWhite = Color(0xFFFFFFFF)
val PureBlack = Color(0xFF000000)

// Soft ("Halka") Emerald Tints & Shades derived from #14A673, #FFFFFF, #000000
val EmeraldBright = Color(0xFF38D399)
val EmeraldHalkaLight = Color(0xFFE6F7F0)
val EmeraldHalkaMid = Color(0xFFB8EBDA)
val EmeraldDeep = Color(0xFF0D7550)
val EmeraldDarkContainer = Color(0xFF06291C)

val SignalRedSoft = Color(0xFFFFE5E5)
val SignalRedDark = Color(0xFFB81414)
val WarmGold = Color(0xFFF5B041)

// Dark Mode Surfaces anchored on #000000 & #14A673 Halka Tint
val NoorCanvas = Color(0xFF000000)
val NoorSurface1 = Color(0xFF07130E)
val NoorSurface2 = Color(0xFF0D2119)
val NoorSurface3 = Color(0xFF133024)
val NoorOutline = Color(0xFF1C4D39)
val NoorTextPrimary = Color(0xFFFFFFFF)
val NoorTextSecondary = Color(0xFFB8D8CB)

// Light Mode Surfaces anchored on #FFFFFF & #14A673 Halka Tint
val VibrantCanvas = Color(0xFFFFFFFF)
val VibrantSurface = Color(0xFFF4FBF8)
val VibrantSurfaceVariant = Color(0xFFE6F7F0)
val VibrantOutline = Color(0xFF9EDEC5)
val VibrantTextPrimary = Color(0xFF000000)
val VibrantTextSecondary = Color(0xFF0D593E)

// Semantic UI Aliases
val NoorEmeraldPrimary = EmeraldPrimary
val NoorEmeraldDark = EmeraldDeep
val NoorMintLuminous = EmeraldBright
val NoorRedAlert = SignalRed
val NoorGoldAccent = WarmGold
val NoorAmberWarning = SignalRed

// Reusable "Halka Gradient" Brushes (#14A673 soft gradients with #000000 / #FFFFFF)
val RuhaniHalkaDarkBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xFF14A673).copy(alpha = 0.34f),
        Color(0xFF0A3828),
        Color(0xFF000000)
    ),
    start = Offset(0f, 0f),
    end = Offset(900f, 900f)
)

val RuhaniHalkaHeroBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xFF14A673),
        Color(0xFF0D6B4A),
        Color(0xFF041B12)
    ),
    start = Offset(0f, 0f),
    end = Offset(1000f, 1000f)
)

val RuhaniHalkaCardBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF14A673).copy(alpha = 0.22f),
        Color(0xFF07140F),
        Color(0xFF000000)
    )
)

val RuhaniRedBadgeBrush = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFFF02828),
        Color(0xFFC81919)
    )
)
