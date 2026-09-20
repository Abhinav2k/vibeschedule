package com.vibeschedule.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vibeschedule.app.model.ScheduleRule
import com.vibeschedule.app.model.SoundMode
import com.vibeschedule.app.ui.theme.AccentPurple
import com.vibeschedule.app.ui.theme.AccentRed
import com.vibeschedule.app.ui.theme.AccentTeal
import com.vibeschedule.app.ui.theme.TextPrimary
import com.vibeschedule.app.ui.theme.TextSecondary
import com.vibeschedule.app.ui.theme.TextTertiary
import java.util.Calendar

@Composable
fun ScheduleCard(
    rule: ScheduleRule,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp),
        shape = RoundedCornerShape(22.dp),
        backgroundColor = if (rule.isEnabled) Color(0x15FFFFFF) else Color(0x08FFFFFF)
    ) {
        // Header: Title + Time + Switch + Menu
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rule.title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.2).sp,
                    color = if (rule.isEnabled) TextPrimary else TextTertiary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${rule.formatStartTime()} — ${rule.formatEndTime()}",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp,
                    color = if (rule.isEnabled) AccentPurple else TextTertiary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = rule.isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = AccentTeal,
                        uncheckedTrackColor = Color(0x20FFFFFF),
                        uncheckedThumbColor = Color(0x88FFFFFF)
                    )
                )

                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreHoriz,
                            contentDescription = "Options",
                            tint = TextSecondary
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                menuExpanded = false
                                onEdit()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = AccentRed) },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = AccentRed, modifier = Modifier.size(18.dp))
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Footer: Mode Pill + Days Indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Mode indicator pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (rule.targetMode == SoundMode.VIBRATE) AccentPurple.copy(alpha = 0.18f)
                        else AccentRed.copy(alpha = 0.18f)
                    )
                    .border(
                        0.5.dp,
                        if (rule.targetMode == SoundMode.VIBRATE) AccentPurple.copy(alpha = 0.35f)
                        else AccentRed.copy(alpha = 0.35f),
                        CircleShape
                    )
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Icon(
                    imageVector = if (rule.targetMode == SoundMode.VIBRATE) Icons.Default.Vibration else Icons.Default.NotificationsOff,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = if (rule.targetMode == SoundMode.VIBRATE) AccentPurple else AccentRed
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = rule.targetMode.displayName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (rule.targetMode == SoundMode.VIBRATE) AccentPurple else AccentRed
                )
            }

            // Days of week row
            DaysOfWeekRow(daysOfWeek = rule.daysOfWeek, isEnabled = rule.isEnabled)
        }
    }
}

@Composable
private fun DaysOfWeekRow(daysOfWeek: List<Int>, isEnabled: Boolean) {
    val dayLabels = listOf(
        Calendar.MONDAY to "M",
        Calendar.TUESDAY to "T",
        Calendar.WEDNESDAY to "W",
        Calendar.THURSDAY to "T",
        Calendar.FRIDAY to "F",
        Calendar.SATURDAY to "S",
        Calendar.SUNDAY to "S"
    )

    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        dayLabels.forEach { (dayInt, label) ->
            val isSelected = daysOfWeek.contains(dayInt)
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            !isEnabled -> Color.Transparent
                            isSelected -> AccentTeal
                            else -> Color(0x12FFFFFF)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        !isEnabled && isSelected -> TextTertiary
                        isSelected -> Color.White
                        else -> TextTertiary
                    }
                )
            }
        }
    }
}
