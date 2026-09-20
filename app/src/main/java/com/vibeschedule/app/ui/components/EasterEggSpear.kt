package com.vibeschedule.app.ui.components

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vibeschedule.app.model.SoundMode
import com.vibeschedule.app.ui.theme.AccentAmber
import com.vibeschedule.app.ui.theme.AccentPurple
import com.vibeschedule.app.ui.theme.AccentTeal
import com.vibeschedule.app.ui.theme.GlassBorderBrush
import com.vibeschedule.app.ui.theme.TextPrimary
import com.vibeschedule.app.ui.theme.TextSecondary
import com.vibeschedule.app.util.SoundModeHelper

/** Custom Aerodynamic Rounded Spear Shape */
val RoundedSpearShape = object : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.5f, h * 0.04f)
            cubicTo(w * 0.78f, h * 0.28f, w * 0.96f, h * 0.58f, w * 0.82f, h * 0.84f)
            cubicTo(w * 0.68f, h * 0.98f, w * 0.32f, h * 0.98f, w * 0.18f, h * 0.84f)
            cubicTo(w * 0.04f, h * 0.58f, w * 0.22f, h * 0.28f, w * 0.5f, h * 0.04f)
            close()
        }
        return Outline.Generic(path)
    }
}

/** Helper function to trigger hardware vibration */
fun triggerQuickVibration(context: Context, durationMillis: Long = 40) {
    try {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(durationMillis, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(durationMillis)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/** Rich dual-pulse vibration when activating mode */
fun triggerActivateVibration(context: Context) {
    try {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 80, 50, 150), -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(longArrayOf(0, 80, 50, 150), -1)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * Top-bar App Icon with 5x Double-Tap Easter Egg:
 * 1. Double tapping says "one more..." 5 times.
 * 2. Icon morphs into a rounded spear shape and rotates on outer orbit.
 * 3. Hold & drag triggers a quick vibration and enlarges into the full/partial screen mode.
 */
@Composable
fun EasterEggAppIcon(
    isSpearUnlocked: Boolean,
    onUnlockSpear: () -> Unit,
    onExpandModal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var tapCounter by remember { mutableIntStateOf(0) }

    // Outer rotation animation once spear is unlocked
    val infiniteTransition = rememberInfiniteTransition(label = "spearSpin")
    val outerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outerAngle"
    )

    Box(
        modifier = modifier.size(46.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isSpearUnlocked) {
            // Rotating outer glowing aura orbit
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .rotate(outerRotation)
                    .border(
                        width = 1.5.dp,
                        brush = Brush.sweepGradient(
                            listOf(
                                AccentPurple,
                                Color.Transparent,
                                AccentAmber,
                                Color.Transparent,
                                AccentTeal,
                                AccentPurple
                            )
                        ),
                        shape = CircleShape
                    )
            )

            // The Rounded Spear Shaped Icon
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedSpearShape)
                    .background(Color(0xE6241838))
                    .border(1.2.dp, Brush.linearGradient(listOf(AccentPurple, AccentAmber)), RoundedSpearShape)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = {
                                triggerQuickVibration(context, 45)
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                if (dragAmount.y > 15f || dragAmount.x > 15f || dragAmount.y < -15f || dragAmount.x < -15f) {
                                    triggerQuickVibration(context, 60)
                                    onExpandModal()
                                }
                            }
                        )
                    }
                    .clickable {
                        triggerQuickVibration(context, 45)
                        onExpandModal()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Vibration,
                    contentDescription = "Spear Core",
                    tint = AccentAmber,
                    modifier = Modifier.size(20.dp)
                )
            }
        } else {
            // Standard rounded icon before Easter Egg unlock
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0x18FFFFFF))
                    .border(1.dp, GlassBorderBrush, CircleShape)
                    .pointerInput(tapCounter) {
                        detectTapGestures(
                            onDoubleTap = {
                                val nextCount = tapCounter + 1
                                tapCounter = nextCount

                                if (nextCount < 5) {
                                    triggerQuickVibration(context, 25)
                                    Toast.makeText(context, "one more...", Toast.LENGTH_SHORT).show()
                                } else {
                                    triggerQuickVibration(context, 80)
                                    Toast.makeText(context, "Spear Mode Unlocked! ⚡", Toast.LENGTH_SHORT).show()
                                    onUnlockSpear()
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Vibration,
                    contentDescription = "VibeSchedule",
                    tint = AccentPurple,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

/**
 * Enlarged Full/Partial Screen Modal:
 * Interactive giant spear powerhouse that turns on Vibration mode when tapped!
 */
@Composable
fun EasterEggExpandedOverlay(
    visible: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    var isVibrateActive by remember {
        mutableStateOf(audioManager.ringerMode == AudioManager.RINGER_MODE_VIBRATE)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulseAnimation")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val spearSpin by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin"
    )

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(250)) + scaleIn(spring(dampingRatio = 0.8f)),
        exit = fadeOut(tween(200)) + scaleOut(tween(180))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xD906060A))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            // Main Glassmorphism Card (Partial/Full Screen ~85% width)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color(0xF216141F))
                    .border(
                        width = 1.25.dp,
                        brush = Brush.linearGradient(
                            listOf(Color(0x809C27B0), Color(0x33FF9F0A), Color(0x8000E5FF))
                        ),
                        shape = RoundedCornerShape(32.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {} // Consume clicks inside card
                    )
                    .padding(28.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Header with title and close button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚡ TACTILE SPEAR CORE",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            color = AccentAmber
                        )
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Giant Rotating & Pulsing Spear Button
                    Box(
                        modifier = Modifier
                            .size(170.dp)
                            .scale(pulseScale),
                        contentAlignment = Alignment.Center
                    ) {
                        // Outer orbit halo
                        Box(
                            modifier = Modifier
                                .size(170.dp)
                                .rotate(spearSpin)
                                .border(
                                    width = 2.dp,
                                    brush = Brush.sweepGradient(
                                        listOf(
                                            AccentTeal,
                                            Color.Transparent,
                                            AccentAmber,
                                            Color.Transparent,
                                            AccentPurple,
                                            AccentTeal
                                        )
                                    ),
                                    shape = CircleShape
                                )
                        )

                        // Giant Interactive Spear Core Button
                        Box(
                            modifier = Modifier
                                .size(136.dp)
                                .clip(RoundedSpearShape)
                                .background(
                                    if (isVibrateActive) Color(0xFA1E122C) else Color(0xF217151D)
                                )
                                .border(
                                    width = 2.dp,
                                    brush = Brush.verticalGradient(
                                        if (isVibrateActive)
                                            listOf(AccentTeal, AccentPurple)
                                        else
                                            listOf(AccentPurple, AccentAmber)
                                    ),
                                    shape = RoundedSpearShape
                                )
                                .clickable {
                                    // Turn ON Vibration Mode
                                    triggerActivateVibration(context)
                                    SoundModeHelper.applySoundMode(context, SoundMode.VIBRATE)
                                    isVibrateActive = true
                                    Toast.makeText(context, "Vibration Mode Activated! ⚡", Toast.LENGTH_SHORT).show()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Vibration,
                                    contentDescription = "Vibrate Mode",
                                    tint = if (isVibrateActive) AccentTeal else AccentAmber,
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (isVibrateActive) "ACTIVE" else "TAP",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    color = if (isVibrateActive) AccentTeal else Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Text(
                        text = "Tap Spear to Turn On Vibration Mode",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (isVibrateActive)
                            "Device Ringer is set to VIBRATE"
                        else
                            "Instant override without schedule delay",
                        fontSize = 13.sp,
                        color = if (isVibrateActive) AccentTeal else TextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
