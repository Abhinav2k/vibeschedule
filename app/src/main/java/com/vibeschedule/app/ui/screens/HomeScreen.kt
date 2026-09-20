package com.vibeschedule.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vibeschedule.app.ui.MainViewModel
import com.vibeschedule.app.ui.components.GlassCard
import com.vibeschedule.app.ui.components.GlowingIndicator
import com.vibeschedule.app.ui.theme.AccentAmber
import com.vibeschedule.app.ui.theme.AccentPurple
import com.vibeschedule.app.ui.theme.AccentRed
import com.vibeschedule.app.ui.theme.AccentTeal
import com.vibeschedule.app.ui.theme.ActiveScheduleBorderBrush
import com.vibeschedule.app.ui.theme.GlassBorderBrush
import com.vibeschedule.app.ui.theme.TextPrimary
import com.vibeschedule.app.ui.theme.TextSecondary
import com.vibeschedule.app.ui.theme.TextTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToSchedules: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeSchedule by viewModel.activeSchedule.collectAsState()
    val activeRemainingMin by viewModel.activeRemainingMinutes.collectAsState()
    val upcomingSchedule by viewModel.upcomingSchedule.collectAsState()
    val isPauseEligible by viewModel.isPauseEligible.collectAsState()
    val pausedUntilMillis by viewModel.pausedUntilMillis.collectAsState()
    val quickMuteUntilMillis by viewModel.quickMuteUntilMillis.collectAsState()
    val quickMuteRemainingSec by viewModel.quickMuteRemainingSeconds.collectAsState()

    val isPaused = pausedUntilMillis != null
    val isQuickMuteActive = quickMuteUntilMillis != null

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // 1. Quick Mute Active Card (If Quick Mute is running)
        if (isQuickMuteActive) {
            val qmMinutes = quickMuteRemainingSec / 60
            val qmSeconds = quickMuteRemainingSec % 60
            val qmTimeStr = String.format(Locale.getDefault(), "%02d:%02d", qmMinutes, qmSeconds)
            val endClockStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(quickMuteUntilMillis!!))

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                backgroundColor = Color(0x2210B981),
                borderBrush = ActiveScheduleBorderBrush
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = AccentTeal,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "QUICK MUTE ACTIVE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = AccentTeal
                        )
                    }

                    IconButton(
                        onClick = { viewModel.cancelQuickMute() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel Quick Mute",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = qmTimeStr,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            letterSpacing = (-1).sp
                        )
                        Text(
                            text = "Ends at $endClockStr",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }

                    Button(
                        onClick = { viewModel.cancelQuickMute() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0x25FFFFFF),
                            contentColor = TextPrimary
                        ),
                        shape = CircleShape
                    ) {
                        Text("Cancel", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        // 2. Primary Status Card (Dynamic Hero)
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            backgroundColor = if (activeSchedule != null && !isPaused) Color(0x186366F1) else Color(0x10FFFFFF),
            borderBrush = if (activeSchedule != null && !isPaused) ActiveScheduleBorderBrush else GlassBorderBrush
        ) {
            // Header Row: Status Tag + Sound Mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GlowingIndicator(isActive = activeSchedule != null && !isPaused)
                    Text(
                        text = when {
                            isPaused -> "PAUSED"
                            activeSchedule != null -> "ACTIVE SCHEDULE"
                            upcomingSchedule != null && upcomingSchedule!!.second <= 20 -> "UPCOMING"
                            else -> "STANDBY"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = when {
                            isPaused -> AccentAmber
                            activeSchedule != null -> AccentTeal
                            upcomingSchedule != null && upcomingSchedule!!.second <= 20 -> AccentPurple
                            else -> TextSecondary
                        }
                    )
                }

                if (activeSchedule != null) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0x22FFFFFF))
                            .border(0.5.dp, Color(0x20FFFFFF), CircleShape)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = activeSchedule!!.targetMode.displayName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Body: Title & Remaining Time in Active Schedule
            if (activeSchedule != null) {
                Text(
                    text = activeSchedule!!.title,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.height(4.dp))

                val remMin = activeRemainingMin ?: 0
                val remStr = if (remMin >= 60) "${remMin / 60}h ${remMin % 60}m" else "${remMin}m"

                Text(
                    text = "$remStr remaining  •  ends ${activeSchedule!!.formatEndTime()}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AccentPurple
                )
            } else if (upcomingSchedule != null) {
                val (rule, minutesLeft) = upcomingSchedule!!
                Text(
                    text = rule.title,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (minutesLeft <= 20) "Starts in ${minutesLeft}m • ${rule.formatStartTime()}"
                    else "${rule.formatStartTime()} • In ${minutesLeft / 60}h ${minutesLeft % 60}m",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (minutesLeft <= 20) AccentPurple else TextSecondary
                )
            } else {
                Text(
                    text = "No Active Schedule",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Normal Ring",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
            }
        }

        // 3. Pause Action Card (Ultra-minimal)
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            backgroundColor = if (isPaused) Color(0x1AFF9F0A) else Color(0x10FFFFFF)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isPaused) "Pause Active" else "Pause Next :00",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isPauseEligible || isPaused) TextPrimary else TextTertiary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = when {
                            isPaused -> {
                                val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(pausedUntilMillis!!))
                                "Ring restored until $timeStr"
                            }
                            isPauseEligible -> "Ring on until next hour"
                            else -> "Active or ≤20m before start"
                        },
                        fontSize = 12.sp,
                        color = when {
                            isPaused -> AccentAmber
                            isPauseEligible -> TextSecondary
                            else -> TextTertiary
                        }
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                if (isPaused) {
                    Button(
                        onClick = { viewModel.cancelPause() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentAmber,
                            contentColor = Color.Black
                        ),
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Resume", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                } else {
                    Button(
                        onClick = { viewModel.pauseUntilNextOClock() },
                        enabled = isPauseEligible,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentPurple,
                            disabledContainerColor = Color(0x14FFFFFF),
                            contentColor = Color.White,
                            disabledContentColor = Color(0x35FFFFFF)
                        ),
                        shape = CircleShape
                    ) {
                        if (!isPauseEligible) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        } else {
                            Icon(
                                imageVector = Icons.Default.Pause,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text("Pause", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
