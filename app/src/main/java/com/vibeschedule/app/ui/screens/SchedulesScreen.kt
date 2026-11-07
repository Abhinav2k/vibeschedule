package com.vibeschedule.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vibeschedule.app.model.ScheduleRule
import com.vibeschedule.app.ui.MainViewModel
import com.vibeschedule.app.ui.QuickMuteConflict
import com.vibeschedule.app.ui.components.PermissionBanner
import com.vibeschedule.app.ui.components.ScheduleCard
import com.vibeschedule.app.ui.theme.BricolageGrotesque
import com.vibeschedule.app.ui.util.Haptics
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun SchedulesScreen(
    viewModel: MainViewModel,
    onEditRule: (ScheduleRule) -> Unit,
    modifier: Modifier = Modifier
) {
    val schedules by viewModel.schedules.collectAsState()
    val conflictInfo by viewModel.quickMuteConflictInfo.collectAsState()
    val quickMuteUntilMillis by viewModel.quickMuteUntilMillis.collectAsState()
    val quickMuteRemainingSec by viewModel.quickMuteRemainingSeconds.collectAsState()

    SchedulesContent(
        schedules = schedules,
        conflictInfo = conflictInfo,
        activeRemainingSeconds = if (quickMuteUntilMillis != null) quickMuteRemainingSec else null,
        onQuickMute = { minutes -> viewModel.requestQuickMute(minutes) },
        onCancelQuickMute = { viewModel.cancelQuickMute() },
        onKeepConflict = { viewModel.dismissQuickMuteConflict() },
        onOverrideConflict = { viewModel.confirmQuickMuteOverride() },
        onToggleSchedule = { id, enabled -> viewModel.toggleSchedule(id, enabled) },
        onEditRule = onEditRule,
        onDeleteSchedule = { id -> viewModel.deleteSchedule(id) },
        modifier = modifier
    )
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun SchedulesContent(
    schedules: List<ScheduleRule>,
    conflictInfo: QuickMuteConflict?,
    activeRemainingSeconds: Int?,
    onQuickMute: (Int) -> Unit,
    onCancelQuickMute: () -> Unit,
    onKeepConflict: () -> Unit,
    onOverrideConflict: () -> Unit,
    onToggleSchedule: (String, Boolean) -> Unit,
    onEditRule: (ScheduleRule) -> Unit,
    onDeleteSchedule: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        PermissionBanner()

        AnimatedVisibility(
            visible = conflictInfo != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            if (conflictInfo != null) {
                QuickMuteConflictCard(
                    conflict = conflictInfo,
                    onKeep = onKeepConflict,
                    onOverride = onOverrideConflict,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }

        QuickMuteSection(
            activeRemainingSeconds = activeRemainingSeconds,
            onQuickMute = onQuickMute,
            onCancelQuickMute = onCancelQuickMute,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        if (schedules.isEmpty()) {
            EmptySchedulesPlaceholder()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 130.dp, top = 4.dp)
            ) {
                items(schedules, key = { it.id }) { rule ->
                    ScheduleCard(
                        rule = rule,
                        modifier = Modifier.animateItemPlacement(),
                        onToggle = { isEnabled -> onToggleSchedule(rule.id, isEnabled) },
                        onEdit = { onEditRule(rule) },
                        onDelete = { onDeleteSchedule(rule.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickMuteConflictCard(
    conflict: QuickMuteConflict,
    onKeep: () -> Unit,
    onOverride: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = conflict.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = conflict.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            Haptics.click(view)
                            onKeep()
                        },
                        shape = CircleShape
                    ) {
                        Text("Keep Current", style = MaterialTheme.typography.labelMedium)
                    }

                    Button(
                        onClick = {
                            Haptics.confirm(view)
                            onOverride()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onTertiary
                        ),
                        shape = CircleShape
                    ) {
                        Text(
                            "Override (${conflict.pendingMinutes}m)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickMuteSection(
    activeRemainingSeconds: Int?,
    onQuickMute: (Int) -> Unit,
    onCancelQuickMute: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var showCustomDialog by remember { mutableStateOf(false) }

    if (showCustomDialog) {
        CustomQuickMuteDialog(
            onDismiss = { showCustomDialog = false },
            onConfirm = { minutes ->
                showCustomDialog = false
                Haptics.confirm(view)
                onQuickMute(minutes)
            }
        )
    }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Quick Mute",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (activeRemainingSeconds != null && activeRemainingSeconds > 0) {
                    val min = activeRemainingSeconds / 60
                    val sec = activeRemainingSeconds % 60
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${min}m ${sec}s left",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Cancel",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    Haptics.click(view)
                                    onCancelQuickMute()
                                }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(15 to "15m", 30 to "30m", 60 to "1h", 120 to "2h").forEach { (min, label) ->
                    Surface(
                        onClick = {
                            Haptics.click(view)
                            onQuickMute(min)
                        },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Surface(
                    onClick = {
                        Haptics.click(view)
                        showCustomDialog = true
                    },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f),
                    modifier = Modifier
                        .weight(1.1f)
                        .height(32.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "Custom",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomQuickMuteDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    val view = LocalView.current
    var minutes by remember { mutableIntStateOf(45) }
    var textValue by remember { mutableStateOf("45") }

    val endMillis = System.currentTimeMillis() + (minutes * 60 * 1000L)
    val endTimeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(endMillis))

    val durationStr = if (minutes < 60) "${minutes}m" else {
        val h = minutes / 60
        val m = minutes % 60
        if (m > 0) "${h}h ${m}m" else "${h}h"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "Custom Quick Mute",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Set a custom duration for phone vibration. Normal ring restores at $endTimeStr.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Surface(
                                onClick = {
                                    Haptics.click(view)
                                    val next = (minutes - 15).coerceIn(1, 1440)
                                    minutes = next
                                    textValue = next.toString()
                                },
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                modifier = Modifier.height(28.dp)
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("-15m", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                            Surface(
                                onClick = {
                                    Haptics.click(view)
                                    val next = (minutes - 5).coerceIn(1, 1440)
                                    minutes = next
                                    textValue = next.toString()
                                },
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                modifier = Modifier.height(28.dp)
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "-5m", modifier = Modifier.size(14.dp))
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = durationStr,
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = BricolageGrotesque,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "$minutes mins",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Surface(
                                onClick = {
                                    Haptics.click(view)
                                    val next = (minutes + 5).coerceIn(1, 1440)
                                    minutes = next
                                    textValue = next.toString()
                                },
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                modifier = Modifier.height(28.dp)
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "+5m", modifier = Modifier.size(14.dp))
                                }
                            }
                            Surface(
                                onClick = {
                                    Haptics.click(view)
                                    val next = (minutes + 15).coerceIn(1, 1440)
                                    minutes = next
                                    textValue = next.toString()
                                },
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                modifier = Modifier.height(28.dp)
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("+15m", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(5 to "5m", 10 to "10m", 20 to "20m", 45 to "45m", 90 to "1.5h").forEach { (presetMin, label) ->
                        Surface(
                            onClick = {
                                Haptics.click(view)
                                minutes = presetMin
                                textValue = presetMin.toString()
                            },
                            shape = CircleShape,
                            color = if (minutes == presetMin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier
                                .weight(1f)
                                .height(26.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (minutes == presetMin) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = textValue,
                    onValueChange = { input ->
                        val filtered = input.filter { it.isDigit() }.take(4)
                        textValue = filtered
                        val parsed = filtered.toIntOrNull()
                        if (parsed != null && parsed > 0) {
                            minutes = parsed.coerceIn(1, 1440)
                        }
                    },
                    label = { Text("Exact minutes (1 - 1440)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(minutes) },
                shape = CircleShape,
                modifier = Modifier.height(36.dp)
            ) {
                Text("Start ($durationStr)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = CircleShape,
                modifier = Modifier.height(36.dp)
            ) {
                Text("Cancel", style = MaterialTheme.typography.labelMedium)
            }
        }
    )
}

@Composable
private fun EmptySchedulesPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsNone,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(56.dp)
            )
            Text(
                text = "No schedules",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Tap + to add a quiet hours schedule",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
