package com.vibeschedule.app.ui.theme

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

@Immutable
class VibeHaptics(private val view: View) {

    fun tick() {
        val constant = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE ->
                HapticFeedbackConstants.SEGMENT_TICK
            else ->
                HapticFeedbackConstants.CLOCK_TICK
        }
        view.performHapticFeedback(constant)
    }

    fun toggle(on: Boolean) {
        val constant = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE ->
                if (on) HapticFeedbackConstants.TOGGLE_ON else HapticFeedbackConstants.TOGGLE_OFF
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ->
                if (on) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.REJECT
            else ->
                HapticFeedbackConstants.VIRTUAL_KEY
        }
        view.performHapticFeedback(constant)
    }

    fun confirm() {
        val constant = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ->
                HapticFeedbackConstants.CONFIRM
            else ->
                HapticFeedbackConstants.LONG_PRESS
        }
        view.performHapticFeedback(constant)
    }

    fun reject() {
        val constant = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ->
                HapticFeedbackConstants.REJECT
            else ->
                HapticFeedbackConstants.LONG_PRESS
        }
        view.performHapticFeedback(constant)
    }
}

@Composable
fun rememberHaptics(): VibeHaptics {
    val view = LocalView.current
    return remember(view) { VibeHaptics(view) }
}
