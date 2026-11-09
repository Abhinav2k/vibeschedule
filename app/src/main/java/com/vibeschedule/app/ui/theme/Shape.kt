package com.vibeschedule.app.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

object VibeShapes {
    val xs: RoundedCornerShape = RoundedCornerShape(8.dp)
    val sm: RoundedCornerShape = RoundedCornerShape(12.dp)
    val md: RoundedCornerShape = RoundedCornerShape(16.dp)
    val lg: RoundedCornerShape = RoundedCornerShape(20.dp)
    val xl: RoundedCornerShape = RoundedCornerShape(24.dp)
    val sheet: RoundedCornerShape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    val dialog: RoundedCornerShape = RoundedCornerShape(28.dp)
    val full: Shape = CircleShape
}

val AppShapes = Shapes(
    extraSmall = VibeShapes.xs,
    small = VibeShapes.sm,
    medium = VibeShapes.md,
    large = VibeShapes.xl,
    extraLarge = VibeShapes.dialog
)
