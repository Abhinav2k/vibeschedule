package com.vibeschedule.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ─── Material Design 3 Tonal Palette (Obsidian / Indigo-Slate Dark) ─────────
val md_theme_dark_primary                 = Color(0xFFBAC3FF)
val md_theme_dark_onPrimary               = Color(0xFF08218A)
val md_theme_dark_primaryContainer        = Color(0xFF283B9F)
val md_theme_dark_onPrimaryContainer      = Color(0xFFDEE0FF)

val md_theme_dark_secondary               = Color(0xFFC3C5DD)
val md_theme_dark_onSecondary             = Color(0xFF2D3042)
val md_theme_dark_secondaryContainer      = Color(0xFF434659)
val md_theme_dark_onSecondaryContainer    = Color(0xFFDFE1F9)

val md_theme_dark_tertiary                = Color(0xFFE5BAD8)
val md_theme_dark_onTertiary              = Color(0xFF45263F)
val md_theme_dark_tertiaryContainer       = Color(0xFF5D3C56)
val md_theme_dark_onTertiaryContainer     = Color(0xFFFFD7F3)

val md_theme_dark_error                   = Color(0xFFFFB4AB)
val md_theme_dark_onError                 = Color(0xFF690005)
val md_theme_dark_errorContainer          = Color(0xFF93000A)
val md_theme_dark_onErrorContainer        = Color(0xFFFFDAD6)

val md_theme_dark_background              = Color(0xFF111215)
val md_theme_dark_onBackground            = Color(0xFFE3E2E6)
val md_theme_dark_surface                 = Color(0xFF111215)
val md_theme_dark_onSurface               = Color(0xFFE3E2E6)

val md_theme_dark_surfaceVariant          = Color(0xFF44464F)
val md_theme_dark_onSurfaceVariant        = Color(0xFFC6C5D0)
val md_theme_dark_outline                 = Color(0xFF8F909A)
val md_theme_dark_outlineVariant          = Color(0xFF44464F)

val md_theme_dark_surfaceContainerLowest  = Color(0xFF0C0D10)
val md_theme_dark_surfaceContainerLow     = Color(0xFF17181B)
val md_theme_dark_surfaceContainer        = Color(0xFF1B1C20)
val md_theme_dark_surfaceContainerHigh     = Color(0xFF25262B)
val md_theme_dark_surfaceContainerHighest  = Color(0xFF303136)

// ─── MD3 Light Palette ───────────────────────────────────────────────────────
val md_theme_light_primary                = Color(0xFF4155BF)
val md_theme_light_onPrimary              = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer       = Color(0xFFDEE0FF)
val md_theme_light_onPrimaryContainer     = Color(0xFF001159)

val md_theme_light_secondary              = Color(0xFF5B5D72)
val md_theme_light_onSecondary            = Color(0xFFFFFFFF)
val md_theme_light_secondaryContainer     = Color(0xFFDFE1F9)
val md_theme_light_onSecondaryContainer   = Color(0xFF171B2C)

val md_theme_light_tertiary               = Color(0xFF75546F)
val md_theme_light_onTertiary             = Color(0xFFFFFFFF)
val md_theme_light_tertiaryContainer      = Color(0xFFFFD7F3)
val md_theme_light_onTertiaryContainer    = Color(0xFF2C1229)

val md_theme_light_surface                = Color(0xFFFEF7FF)
val md_theme_light_onSurface              = Color(0xFF1C1B1F)
val md_theme_light_surfaceContainer       = Color(0xFFF3EDF7)
val md_theme_light_outlineVariant         = Color(0xFFCAC4D0)

// ─── Semantic Aliases & Brushes ─────────────────────────────────────────────
val DeepDark            = md_theme_dark_background
val SurfaceDark         = md_theme_dark_surfaceContainer
val DeepDarkBg          = DeepDark
val DarkNavyBg          = SurfaceDark

val SurfaceGlass         = md_theme_dark_surfaceContainer
val SurfaceGlassElevated = md_theme_dark_surfaceContainerHigh
val SurfaceGlassActive   = md_theme_dark_primaryContainer

val AccentWhite      = Color(0xFFFFFFFF)
val AccentWhiteGlow  = Color(0x33BAC3FF)
val AccentGray       = md_theme_dark_onSurfaceVariant
val AccentDarkGray   = md_theme_dark_outline

val AccentPurple     = md_theme_dark_primary
val AccentPurpleGlow = AccentWhiteGlow
val AccentTeal       = md_theme_dark_secondary
val AccentTealGlow   = AccentWhiteGlow
val AccentAmber      = md_theme_dark_tertiary
val AccentRed        = md_theme_dark_error

val TextPrimary   = md_theme_dark_onSurface
val TextSecondary = md_theme_dark_onSurfaceVariant
val TextTertiary  = md_theme_dark_outline

val GlassBorderBrush = Brush.linearGradient(
    listOf(
        md_theme_dark_outlineVariant.copy(alpha = 0.5f),
        md_theme_dark_outlineVariant.copy(alpha = 0.15f)
    )
)

val ActiveScheduleBorderBrush = Brush.linearGradient(
    listOf(
        md_theme_dark_primary.copy(alpha = 0.8f),
        md_theme_dark_primaryContainer.copy(alpha = 0.4f)
    )
)

val LiquidGlassActiveBrush = Brush.linearGradient(
    listOf(
        md_theme_dark_surfaceContainerHigh,
        md_theme_dark_surfaceContainer
    )
)

val BackgroundMeshBrush = Brush.verticalGradient(
    listOf(
        Color(0xFF15161A),
        Color(0xFF0F1013),
        Color(0xFF0A0B0D)
    )
)

val Purple80      = md_theme_dark_primary
val PurpleGrey80  = md_theme_dark_secondary
val Pink80        = md_theme_dark_tertiary
val Purple40      = md_theme_light_primary
val PurpleGrey40  = md_theme_light_secondary
val Pink40        = md_theme_light_tertiary
