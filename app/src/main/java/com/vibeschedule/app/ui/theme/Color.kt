package com.vibeschedule.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ─── Neutral Monochrome Palette ───────────────────────────────────────────────
val DeepDark        = Color(0xFF000000)
val SurfaceDark     = Color(0xFF0D0D0D)
val DeepDarkBg      = DeepDark
val DarkNavyBg      = SurfaceDark

// Glass layers (white-on-black translucency)
val SurfaceGlass         = Color(0x14FFFFFF)
val SurfaceGlassElevated = Color(0x1FFFFFFF)
val SurfaceGlassActive   = Color(0x2AFFFFFF)

// Accent: pure white & grays (replaces purple/teal/red/amber)
val AccentWhite      = Color(0xFFFFFFFF)
val AccentWhiteGlow  = Color(0x55FFFFFF)
val AccentGray       = Color(0xFFAAAAAA)
val AccentDarkGray   = Color(0xFF555555)

// Keep legacy names pointing to monochrome equivalents so nothing breaks
val AccentPurple     = AccentWhite
val AccentPurpleGlow = AccentWhiteGlow
val AccentTeal       = AccentWhite
val AccentTealGlow   = AccentWhiteGlow
val AccentAmber      = AccentGray
val AccentRed        = AccentGray

// Text
val TextPrimary   = Color(0xFFF5F5F5)
val TextSecondary = Color(0xFF888888)
val TextTertiary  = Color(0xFF444444)

// Border brushes — white glass highlights only
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
        Color(0x55FFFFFF),
        Color(0x20FFFFFF),
        Color(0x08FFFFFF)
    )
)

val LiquidGlassActiveBrush = Brush.linearGradient(
    listOf(
        Color(0x22FFFFFF),
        Color(0x10FFFFFF)
    )
)

// Background: Deep obsidian black
val BackgroundMeshBrush = Brush.verticalGradient(
    listOf(
        Color(0xFF111111),
        Color(0xFF060606),
        Color(0xFF000000)
    )
)

// Purple80 etc — kept to avoid Material3 compile errors
val Purple80      = Color(0xFFE0E0E0)
val PurpleGrey80  = Color(0xFFCCCCCC)
val Pink80        = Color(0xFFBBBBBB)
val Purple40      = Color(0xFF666666)
val PurpleGrey40  = Color(0xFF555555)
val Pink40        = Color(0xFF444444)
