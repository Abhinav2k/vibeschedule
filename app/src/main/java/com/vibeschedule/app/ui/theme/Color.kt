package com.vibeschedule.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.vibeschedule.app.model.SoundMode

@Immutable
data class ModePalette(
    val accent: Color,
    val container: Color,
    val onContainer: Color
)

@Immutable
data class VibeColors(
    val isDark: Boolean,
    val bg: Color,
    val surfaceLowest: Color,
    val surfaceLow: Color,
    val surface: Color,
    val surfaceHigh: Color,
    val surfaceHighest: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val dim: Color,
    val outline: Color,
    val outlineVariant: Color,
    val chromeFill: Color,
    val onChromeFill: Color,
    val onAccent: Color,
    val ring: ModePalette,
    val vibrate: ModePalette,
    val silent: ModePalette,
    val error: Color,
    val onError: Color,
    val errorContainer: Color,
    val onErrorContainer: Color,
    val scrim: Color,
    val track: Color,
    val hairline: Color
)

fun VibeColors.forMode(mode: SoundMode): ModePalette = when (mode) {
    SoundMode.NORMAL -> ring
    SoundMode.VIBRATE -> vibrate
    SoundMode.SILENT -> silent
}

val DarkVibeColors = VibeColors(
    isDark = true,
    bg = Color(0xFF0D1020),
    surfaceLowest = Color(0xFF090B17),
    surfaceLow = Color(0xFF121629),
    surface = Color(0xFF171C31),
    surfaceHigh = Color(0xFF1E243C),
    surfaceHighest = Color(0xFF272E49),
    onSurface = Color(0xFFE8EAFA),
    onSurfaceVariant = Color(0xFFA9B0CE),
    dim = Color(0xFF7C84A8),
    outline = Color(0xFF6E78A0),
    outlineVariant = Color(0xFF2A314D),
    chromeFill = Color(0xFFECEEFB),
    onChromeFill = Color(0xFF0D1020),
    onAccent = Color(0xFF0D1020),
    ring = ModePalette(
        accent = Color(0xFFFFC56B),
        container = Color(0xFF5B3C06),
        onContainer = Color(0xFFFFE6BF)
    ),
    vibrate = ModePalette(
        accent = Color(0xFFAEB8FF),
        container = Color(0xFF2B3380),
        onContainer = Color(0xFFE1E4FF)
    ),
    silent = ModePalette(
        accent = Color(0xFF5ED6CB),
        container = Color(0xFF0E4B49),
        onContainer = Color(0xFFC4F5F0)
    ),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF0D1020),
    errorContainer = Color(0xFF6E1C1C),
    onErrorContainer = Color(0xFFFFDAD6),
    scrim = Color(0x8C000000),
    track = Color(0x1AE8EAFA),
    hairline = Color(0xFF2A314D)
)

val LightVibeColors = VibeColors(
    isDark = false,
    bg = Color(0xFFF3F4FA),
    surfaceLowest = Color(0xFFFFFFFF),
    surfaceLow = Color(0xFFF8F9FD),
    surface = Color(0xFFEFF1F9),
    surfaceHigh = Color(0xFFE7EAF5),
    surfaceHighest = Color(0xFFDFE3F1),
    onSurface = Color(0xFF141729),
    onSurfaceVariant = Color(0xFF4A5070),
    dim = Color(0xFF5F6685),
    outline = Color(0xFF7A8099),
    outlineVariant = Color(0xFFD3D7E8),
    chromeFill = Color(0xFF141729),
    onChromeFill = Color(0xFFF3F4FA),
    onAccent = Color(0xFFFFFFFF),
    ring = ModePalette(
        accent = Color(0xFF8A5300),
        container = Color(0xFFFFE3B5),
        onContainer = Color(0xFF3A2300)
    ),
    vibrate = ModePalette(
        accent = Color(0xFF4350C9),
        container = Color(0xFFDFE2FF),
        onContainer = Color(0xFF0E1672)
    ),
    silent = ModePalette(
        accent = Color(0xFF0A766F),
        container = Color(0xFFC9F3EE),
        onContainer = Color(0xFF00302D)
    ),
    error = Color(0xFFB3261E),
    onError = Color(0xFFF3F4FA),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    scrim = Color(0x66000000),
    track = Color(0x14141729),
    hairline = Color(0xFFD3D7E8)
)

val LocalVibeColors = staticCompositionLocalOf { DarkVibeColors }
