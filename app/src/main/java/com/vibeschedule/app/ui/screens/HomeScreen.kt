package com.vibeschedule.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vibeschedule.app.ui.MainViewModel
import com.vibeschedule.app.ui.components.GlassCard
import com.vibeschedule.app.ui.components.GlowingIndicator
import com.vibeschedule.app.ui.theme.AccentAmber
import com.vibeschedule.app.ui.theme.AccentPurple
import com.vibeschedule.app.ui.theme.AccentTeal
import com.vibeschedule.app.ui.theme.GlassBorderBrush
import com.vibeschedule.app.ui.theme.LiquidGlassActiveBrush
import com.vibeschedule.app.ui.theme.SurfaceGlassElevated
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
    val upcomingSchedule by viewModel.upcomingSchedule.collectAsState()
    val isPauseEligible by viewModel.isPauseEligible.collectAsState()
    val pausedUntilMillis by viewModel.pausedUntilMillis.collectAsState()

    val isPaused = pausedUntilMillis != null

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // 1. Primary Active Timer Card (Liquid Glass)
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = if (activeSchedule != null) Color(0x248B5CF6) else Color(0x14FFFFFF),
            borderBrush = if (activeSchedule != null) {
                Brush.linearGradient(listOf(AccentPurple.copy(alpha = 0.6f), AccentTeal.copy(alpha = 0.4f)))
            } else GlassBorderBrush
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
                    GlowingIndicator(isActive = activeSchedule != null && !isPaused)
                    Text(
                        text = when {
                            isPaused -> "PAUSED"
                            activeSchedule != null -> "ACTIVE NOW"
                            upcomingSchedule != null && upcomingSchedule!!.second <= 20 -> "STARTING SOON"
                            else -> "STANDBY"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = when {
                            isPaused -> AccentAmber
                            activeSchedule != null -> AccentTeal
                            upcomingSchedule != null && upcomingSchedule!!.second <= 20 -> AccentPurple
                            else -> Color(0x88FFFFFF)
                        }
                    )
                }

                if (activeSchedule != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x338B5CF6))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = activeSchedule!!.targetMode.displayName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (activeSchedule != null) {
                Text(
                    text = activeSchedule!!.title,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Active: ${activeSchedule!!.formatStartTime()}  ➔  ${activeSchedule!!.formatEndTime()}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xCCFFFFFF)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Phone will stay on ${activeSchedule!!.targetMode.displayName} until ${activeSchedule!!.formatEndTime()}",
                    fontSize = 12.sp,
                    color = Color(0x88FFFFFF)
                )
            } else if (upcomingSchedule != null) {
                val (rule, minutesLeft) = upcomingSchedule!!
                Text(
                    text = rule.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (minutesLeft <= 20) "Begins in $minutesLeft min (${rule.formatStartTime()})"
                    else "Next up at ${rule.formatStartTime()} (in ${minutesLeft / 60}h ${minutesLeft % 60}m)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (minutesLeft <= 20) AccentPurple else Color(0xAAFFFFFF)
                )
            } else {
                Text(
                    text = "No Active Schedule",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Your ringer is set to Normal mode.",
                    fontSize = 13.sp,
                    color = Color(0x88FFFFFF)
                )
            }
        }

        // 2. "Pause" Action Card (Liquid Glass)
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = if (isPaused) Color(0x28F59E0B) else Color(0x16FFFFFF)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isPaused) Icons.Default.PlayCircle else Icons.Default.PauseCircle,
                            contentDescription = null,
                            tint = if (isPaused) AccentAmber else if (isPauseEligible) AccentPurple else Color(0x44FFFFFF),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (isPaused) "Pause Active" else "Pause Next :00",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPauseEligible || isPaused) Color.White else Color(0x55FFFFFF)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = when {
                            isPaused -> {
                                val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(pausedUntilMillis!!))
                                "Ring restored until $timeStr:00"
                            }
                            isPauseEligible -> "Un-mutes sound mode until the next o'clock"
                            else -> "Available when schedule is active or in 20 min"
                        },
                        fontSize = 12.sp,
                        color = when {
                            isPaused -> AccentAmber
                            isPauseEligible -> Color(0xBBFFFFFF)
                            else -> Color(0x55FFFFFF)
                        }
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                if (isPaused) {
                    // Resume Button
                    Button(
                        onClick = { viewModel.cancelPause() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentAmber,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Resume", fontWeight = FontWeight.Bold)
                    }
                } else {
                    // Pause Button (Disabled unless eligible)
                    Button(
                        onClick = { viewModel.pauseUntilNextOClock() },
                        enabled = isPauseEligible,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentPurple,
                            disabledContainerColor = Color(0x18FFFFFF),
                            contentColor = Color.White,
                            disabledContentColor = Color(0x44FFFFFF)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (!isPauseEligible) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text("Pause", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // 3. Quick Overview / Tip Pill
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToSchedules() },
            backgroundColor = Color(0x0EFFFFFF)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x228B5CF6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = AccentPurple,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Manage Schedules",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Text(
                            text = "View all rules and quick mute presets",
                            fontSize = 12.sp,
                            color = Color(0x77FFFFFF)
                        )
                    }
                }

                Text(
                    text = "➔",
                    fontSize = 16.sp,
                    color = Color(0x66FFFFFF)
                )
            }
        }
    }
}
