package com.vibeschedule.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vibeschedule.app.model.ScheduleRule
import com.vibeschedule.app.ui.MainViewModel
import com.vibeschedule.app.ui.theme.BricolageGrotesque
import com.vibeschedule.app.ui.util.Haptics
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class HomeUiState(
    val activeSchedule: ScheduleRule? = null,
    val activeRemainingMinutes: Int? = null,
    val upcomingSchedule: Pair<ScheduleRule, Int>? = null,
    val isPauseEligible: Boolean = false,
    val pausedUntilMillis: Long? = null,
    val hasDismissedSchedule: Boolean = false,
    val quickMuteUntilMillis: Long? = null,
    val hasSchedules: Boolean = true
)

interface HomeActions {
    fun onCancelQuickMute()
    fun onStopActiveSchedule()
    fun onRestoreSchedule()
    fun onSkipPeriod()
    fun onNavigateToSchedules()
}

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
    val hasDismissedSchedule by viewModel.hasDismissedSchedule.collectAsState()
    val quickMuteUntilMillis by viewModel.quickMuteUntilMillis.collectAsState()
    val quickMuteRemainingSec by viewModel.quickMuteRemainingSeconds.collectAsState()
    val schedules by viewModel.schedules.collectAsState()

    val uiState = HomeUiState(
        activeSchedule = activeSchedule,
        activeRemainingMinutes = activeRemainingMin,
        upcomingSchedule = upcomingSchedule,
        isPauseEligible = isPauseEligible,
        pausedUntilMillis = pausedUntilMillis,
        hasDismissedSchedule = hasDismissedSchedule,
        quickMuteUntilMillis = quickMuteUntilMillis,
        hasSchedules = schedules.isNotEmpty()
    )

    val actions = object : HomeActions {
        override fun onCancelQuickMute() = viewModel.cancelQuickMute()
        override fun onStopActiveSchedule() = viewModel.stopActiveSchedule()
        override fun onRestoreSchedule() = viewModel.restoreSchedule()
        override fun onSkipPeriod() = viewModel.skipPeriod()
        override fun onNavigateToSchedules() = onNavigateToSchedules()
    }

    HomeContent(
        state = uiState,
        quickMuteRemainingSeconds = quickMuteRemainingSec,
        actions = actions,
        modifier = modifier
    )
}

@Composable
fun HomeContent(
    state: HomeUiState,
    quickMuteRemainingSeconds: Int,
    actions: HomeActions,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val isPaused = state.pausedUntilMillis != null
    val isQuickMuteActive = state.quickMuteUntilMillis != null

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Quick Mute Active Card
        AnimatedVisibility(
            visible = isQuickMuteActive,
            enter = expandVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeIn(animationSpec = tween(200)),
            exit = shrinkVertically(animationSpec = tween(250)) + fadeOut(animationSpec = tween(200))
        ) {
            val qmMinutes = quickMuteRemainingSeconds / 60
            val qmSeconds = quickMuteRemainingSeconds % 60
            val qmTimeStr = String.format(Locale.getDefault(), "%02d:%02d", qmMinutes, qmSeconds)
            val endClockStr = if (state.quickMuteUntilMillis != null)
                SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(state.quickMuteUntilMillis)) else ""

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
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
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "QUICK MUTE",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                        FilledTonalIconButton(
                            onClick = {
                                Haptics.click(view)
                                actions.onCancelQuickMute()
                            },
                            modifier = Modifier.size(32.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f),
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel", modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = qmTimeStr,
                                style = MaterialTheme.typography.displayMedium,
                                fontFamily = BricolageGrotesque,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Ends at $endClockStr",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f)
                            )
                        }
                        Button(
                            onClick = {
                                Haptics.click(view)
                                actions.onCancelQuickMute()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary,
                                contentColor = MaterialTheme.colorScheme.onTertiary
                            ),
                            shape = CircleShape,
                            modifier = Modifier.height(34.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp)
                        ) {
                            Text("Cancel", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // 2. Hero Status Card (Active / Upcoming / Skipped / Stopped / Standby / Empty)
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = if (state.activeSchedule != null && !isPaused)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = if (state.activeSchedule != null && !isPaused)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurface
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                if (state.activeSchedule != null && !isPaused) {
                    val rule = state.activeSchedule
                    val remMin = state.activeRemainingMinutes ?: 0
                    val remStr = if (remMin >= 60) "${remMin / 60}h ${remMin % 60}m" else "${remMin}m"

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text(rule.title.ifBlank { "Scheduled Quiet" }, fontWeight = FontWeight.SemiBold) },
                            icon = {
                                Icon(
                                    imageVector = Icons.Rounded.VolumeOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                                labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                iconContentColor = MaterialTheme.colorScheme.primary
                            ),
                            border = null
                        )

                        FilledTonalButton(
                            onClick = {
                                Haptics.confirm(view)
                                actions.onStopActiveSchedule()
                            },
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.height(32.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text("End Now", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = remStr,
                        style = MaterialTheme.typography.displayLarge,
                        fontFamily = BricolageGrotesque,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-1).sp
                    )
                    Text(
                        text = "remaining",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val totalDuration = ((rule.endHour * 60 + rule.endMinute) - (rule.startHour * 60 + rule.startMinute)).let {
                        if (it <= 0) it + 1440 else it
                    }
                    val progress = if (totalDuration > 0) {
                        (1f - (remMin.toFloat() / totalDuration.toFloat())).coerceIn(0.05f, 1f)
                    } else 0.5f

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f),
                        strokeCap = StrokeCap.Round
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Ends at ${rule.formatEndTime()}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                    )

                } else if (state.upcomingSchedule != null && !isPaused) {
                    val (rule, minutesLeft) = state.upcomingSchedule

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "UPCOMING",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = rule.title.ifBlank { "Scheduled Quiet" },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (minutesLeft <= 20)
                            "Starts in ${minutesLeft}m • ${rule.formatStartTime()}"
                        else
                            "${rule.formatStartTime()} • In ${minutesLeft / 60}h ${minutesLeft % 60}m",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                } else if (!state.hasSchedules) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Bedtime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "STANDBY",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "No Schedules Configured",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tap Schedules below to add recurring quiet hours",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                } else {
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
                                imageVector = if (isPaused) Icons.Rounded.NotificationsActive else Icons.Rounded.Bedtime,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = when {
                                    isPaused -> "SKIPPED"
                                    state.hasDismissedSchedule -> "STOPPED"
                                    else -> "STANDBY"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        if (isPaused || state.hasDismissedSchedule) {
                            FilledTonalButton(
                                onClick = {
                                    Haptics.confirm(view)
                                    actions.onRestoreSchedule()
                                },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Restore", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = when {
                            isPaused -> "Schedule Skipped"
                            state.hasDismissedSchedule -> "Schedule Stopped"
                            else -> "No Active Schedule"
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = when {
                            isPaused -> "Normal ring on until next hour"
                            else -> "Normal ring"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 3. Skip Period Card
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isPaused) "Period Skipped" else "Skip Period",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (state.isPauseEligible || isPaused)
                            MaterialTheme.colorScheme.onSurface
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when {
                            isPaused -> {
                                val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(state.pausedUntilMillis!!))
                                "Ring on until $timeStr"
                            }
                            state.isPauseEligible -> "Unmute until next hour"
                            else -> "Available during active schedule"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isPaused || state.isPauseEligible)
                            MaterialTheme.colorScheme.onSurfaceVariant
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                if (isPaused) {
                    FilledTonalButton(
                        onClick = {
                            Haptics.confirm(view)
                            actions.onRestoreSchedule()
                        },
                        shape = CircleShape,
                        modifier = Modifier.height(34.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Restore")
                    }
                } else {
                    Button(
                        onClick = {
                            Haptics.confirm(view)
                            actions.onSkipPeriod()
                        },
                        enabled = state.isPauseEligible,
                        shape = CircleShape,
                        modifier = Modifier.height(34.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 2.dp)
                    ) {
                        if (!state.isPauseEligible) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                        } else {
                            Icon(imageVector = Icons.Default.SkipNext, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text("Skip")
                    }
                }
            }
        }

        // Clearance spacer for floating bottom dock
        Spacer(modifier = Modifier.height(110.dp))
    }
}
