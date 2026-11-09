package com.vibeschedule.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private fun VibeColors.toColorScheme(): ColorScheme {
    return if (isDark) {
        darkColorScheme(
            primary = chromeFill,
            onPrimary = onChromeFill,
            primaryContainer = surfaceHighest,
            onPrimaryContainer = onSurface,
            secondary = onSurfaceVariant,
            onSecondary = bg,
            secondaryContainer = surfaceHigh,
            onSecondaryContainer = onSurface,
            tertiary = vibrate.accent,
            onTertiary = onAccent,
            tertiaryContainer = vibrate.container,
            onTertiaryContainer = vibrate.onContainer,
            error = error,
            onError = onError,
            errorContainer = errorContainer,
            onErrorContainer = onErrorContainer,
            background = bg,
            onBackground = onSurface,
            surface = bg,
            onSurface = onSurface,
            surfaceVariant = surfaceHigh,
            onSurfaceVariant = onSurfaceVariant,
            outline = outline,
            outlineVariant = outlineVariant,
            surfaceContainerLowest = surfaceLowest,
            surfaceContainerLow = surfaceLow,
            surfaceContainer = surface,
            surfaceContainerHigh = surfaceHigh,
            surfaceContainerHighest = surfaceHighest,
            inverseSurface = onSurface,
            inverseOnSurface = bg,
            inversePrimary = vibrate.accent,
            surfaceTint = Color.Transparent,
            scrim = Color.Black
        )
    } else {
        lightColorScheme(
            primary = chromeFill,
            onPrimary = onChromeFill,
            primaryContainer = surfaceHighest,
            onPrimaryContainer = onSurface,
            secondary = onSurfaceVariant,
            onSecondary = bg,
            secondaryContainer = surfaceHigh,
            onSecondaryContainer = onSurface,
            tertiary = vibrate.accent,
            onTertiary = onAccent,
            tertiaryContainer = vibrate.container,
            onTertiaryContainer = vibrate.onContainer,
            error = error,
            onError = onError,
            errorContainer = errorContainer,
            onErrorContainer = onErrorContainer,
            background = bg,
            onBackground = onSurface,
            surface = bg,
            onSurface = onSurface,
            surfaceVariant = surfaceHigh,
            onSurfaceVariant = onSurfaceVariant,
            outline = outline,
            outlineVariant = outlineVariant,
            surfaceContainerLowest = surfaceLowest,
            surfaceContainerLow = surfaceLow,
            surfaceContainer = surface,
            surfaceContainerHigh = surfaceHigh,
            surfaceContainerHighest = surfaceHighest,
            inverseSurface = onSurface,
            inverseOnSurface = bg,
            inversePrimary = vibrate.accent,
            surfaceTint = Color.Transparent,
            scrim = Color.Black
        )
    }
}

private val DarkColorScheme = DarkVibeColors.toColorScheme()
private val LightColorScheme = LightVibeColors.toColorScheme()

object VibeTheme {
    val colors: VibeColors
        @Composable
        @ReadOnlyComposable
        get() = LocalVibeColors.current
}

@Composable
fun VibeScheduleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val baseVibeColors = if (darkTheme) DarkVibeColors else LightVibeColors

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val dyn = if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            dyn.copy(
                tertiary = baseVibeColors.vibrate.accent,
                onTertiary = baseVibeColors.onAccent,
                tertiaryContainer = baseVibeColors.vibrate.container,
                onTertiaryContainer = baseVibeColors.vibrate.onContainer,
                surfaceTint = Color.Transparent
            )
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    CompositionLocalProvider(LocalVibeColors provides baseVibeColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = AppShapes,
            content = content
        )
    }
}
