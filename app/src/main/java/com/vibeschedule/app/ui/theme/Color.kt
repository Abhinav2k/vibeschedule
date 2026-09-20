package com.vibeschedule.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Liquid Glass & Premium Dark Palette
val DeepDark = Color(0xFF090A0F)
val SurfaceDark = Color(0xFF131622)
val DeepDarkBg = DeepDark
val DarkNavyBg = SurfaceDark

val SurfaceGlass = Color(0x18FFFFFF)
val SurfaceGlassElevated = Color(0x24FFFFFF)
val SurfaceGlassActive = Color(0x35FFFFFF)

val GlassBorderLight = Color(0x40FFFFFF)
val GlassBorderDim = Color(0x14FFFFFF)

val AccentPurple = Color(0xFF8B5CF6)
val AccentPurpleGlow = Color(0x558B5CF6)
val AccentTeal = Color(0xFF10B981)
val AccentTealGlow = Color(0x5510B981)
val AccentAmber = Color(0xFFF59E0B)

val GlassBorderBrush = Brush.linearGradient(
    listOf(
        Color(0x4DFFFFFF),
        Color(0x1AFFFFFF),
        Color(0x05FFFFFF),
        Color(0x33FFFFFF)
    )
)

val LiquidGlassActiveBrush = Brush.linearGradient(
    listOf(
        Color(0x338B5CF6),
        Color(0x2210B981)
    )
)

val BackgroundMeshBrush = Brush.verticalGradient(
    listOf(
        Color(0xFF08090E),
        Color(0xFF111422),
        Color(0xFF090A10)
    )
)
