package com.vibeschedule.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Premium Obsidian / Apple Dark Color Grading
val DeepDark = Color(0xFF000000)
val SurfaceDark = Color(0xFF0D0E12)
val DeepDarkBg = DeepDark
val DarkNavyBg = SurfaceDark

// Refined Translucent Glass Layers
val SurfaceGlass = Color(0x14FFFFFF)
val SurfaceGlassElevated = Color(0x1FFFFFFF)
val SurfaceGlassActive = Color(0x2AFFFFFF)

// Apple System Accents
val AccentPurple = Color(0xFF6366F1) // Refined Indigo / SF Accent
val AccentPurpleGlow = Color(0x356366F1)
val AccentTeal = Color(0xFF30D158)   // Apple Mint / Live Green
val AccentTealGlow = Color(0x3530D158)
val AccentAmber = Color(0xFFFF9F0A)  // Apple Amber / Pause
val AccentRed = Color(0xFFFF453A)    // Apple Coral Red

// Text Hierarchy (Apple Typography Tones)
val TextPrimary = Color(0xFFF5F5F7)
val TextSecondary = Color(0xFF86868B)
val TextTertiary = Color(0xFF55555C)

// Specular Glass Highlight Borders
val GlassBorderBrush = Brush.linearGradient(
    listOf(
        Color(0x38FFFFFF),
        Color(0x12FFFFFF),
        Color(0x06FFFFFF),
        Color(0x22FFFFFF)
    )
)

val ActiveScheduleBorderBrush = Brush.linearGradient(
    listOf(
        Color(0x806366F1),
        Color(0x4030D158),
        Color(0x15FFFFFF)
    )
)

val LiquidGlassActiveBrush = Brush.linearGradient(
    listOf(
        Color(0x256366F1),
        Color(0x1830D158)
    )
)

// Background: Smooth Deep Obsidian with subtle ambient top-glow
val BackgroundMeshBrush = Brush.verticalGradient(
    listOf(
        Color(0xFF0D0F18),
        Color(0xFF040508),
        Color(0xFF000000)
    )
)
