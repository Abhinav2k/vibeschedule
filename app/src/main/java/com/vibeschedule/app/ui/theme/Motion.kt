package com.vibeschedule.app.ui.theme

import android.provider.Settings
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

object VibeMotion {
    fun <T> spatial(visibilityThreshold: T? = null): SpringSpec<T> =
        spring(dampingRatio = 0.80f, stiffness = 380f, visibilityThreshold = visibilityThreshold)

    fun <T> spatialFast(visibilityThreshold: T? = null): SpringSpec<T> =
        spring(dampingRatio = 0.85f, stiffness = 700f, visibilityThreshold = visibilityThreshold)

    fun <T> effects(visibilityThreshold: T? = null): SpringSpec<T> =
        spring(dampingRatio = 1.00f, stiffness = 1600f, visibilityThreshold = visibilityThreshold)

    const val DurationShort = 150
    const val DurationMedium = 250
    const val DurationLong = 400
    const val DurationIntro = 700

    val EmphasizedDecelerate: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
    val EmphasizedAccelerate: Easing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)
}

@Composable
fun rememberReduceMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        runCatching {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            ) == 0f
        }.getOrDefault(false)
    }
}
