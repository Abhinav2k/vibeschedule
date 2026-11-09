package com.vibeschedule.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp

@Composable
private fun SwatchRow(label: String, bg: Color, fg: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .background(bg, VibeShapes.sm)
            .border(Dimens.Hairline, VibeTheme.colors.hairline, VibeShapes.sm)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = fg)
    }
}

// region Previews
@PreviewLightDark
@Composable
private fun ThemeSwatchesPreview() {
    VibeScheduleTheme {
        val c = VibeTheme.colors
        Surface(color = c.bg) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.ScreenHorizontalMargin),
                verticalArrangement = Arrangement.spacedBy(Dimens.Space8)
            ) {
                Text("Quiet Dial Tokens", style = MaterialTheme.typography.headlineSmall, color = c.onSurface)
                SwatchRow("bg / onSurface", c.bg, c.onSurface)
                SwatchRow("surfaceLow / onSurfaceVariant", c.surfaceLow, c.onSurfaceVariant)
                SwatchRow("surface / dim", c.surface, c.dim)
                SwatchRow("surfaceHigh / onSurface", c.surfaceHigh, c.onSurface)
                SwatchRow("surfaceHighest / onSurface", c.surfaceHighest, c.onSurface)
                SwatchRow("chromeFill / onChromeFill", c.chromeFill, c.onChromeFill)
                SwatchRow("Ring container / accent", c.ring.container, c.ring.accent)
                SwatchRow("Vibrate container / accent", c.vibrate.container, c.vibrate.accent)
                SwatchRow("Silent container / accent", c.silent.container, c.silent.accent)
                SwatchRow("Error container / onErrorContainer", c.errorContainer, c.onErrorContainer)
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun ThemeTypographyPreview() {
    VibeScheduleTheme {
        val c = VibeTheme.colors
        Surface(color = c.bg) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.ScreenHorizontalMargin),
                verticalArrangement = Arrangement.spacedBy(Dimens.Space4)
            ) {
                Text("1h 20m", style = MaterialTheme.typography.displayLarge, color = c.onSurface)
                Text("in 4h 43m", style = MaterialTheme.typography.displayMedium, color = c.onSurface)
                Text("Schedules", style = MaterialTheme.typography.headlineLarge, color = c.onSurface)
                Text("9:00 – 12:00", style = MaterialTheme.typography.headlineMedium, color = c.onSurface)
                Text("New schedule", style = MaterialTheme.typography.headlineSmall, color = c.onSurface)
                Text("VibeSchedule", style = MaterialTheme.typography.titleLarge, color = c.onSurface)
                Text("Lock-screen controls", style = MaterialTheme.typography.titleMedium, color = c.onSurface)
                Text("Supporting body copy", style = MaterialTheme.typography.bodyMedium, color = c.onSurfaceVariant)
                Text("Skip to 7:00 PM", style = MaterialTheme.typography.labelLarge, color = c.onSurface)
            }
        }
    }
}
// endregion
