package com.vibeschedule.app.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.MoreVert
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
import com.vibeschedule.app.ui.theme.AccentTeal
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
            .padding(horizontal = 16.dp, vertical = 6.dp),
        backgroundColor = if (rule.isEnabled) Color(0x1AFFFFFF) else Color(0x0CFFFFFF)
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
                    color = if (rule.isEnabled) Color.White else Color(0x66FFFFFF)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${rule.formatStartTime()}  ➔  ${rule.formatEndTime()}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (rule.isEnabled) AccentPurple else Color(0x55FFFFFF)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = rule.isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = AccentTeal,
                        uncheckedTrackColor = Color(0x22FFFFFF)
                    )
                )

                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = Color(0x88FFFFFF)
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
                                Icon(Icons.Default.Edit, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
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
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (rule.targetMode == SoundMode.VIBRATE) AccentPurple.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = if (rule.targetMode == SoundMode.VIBRATE) Icons.Default.Vibration else Icons.Default.NotificationsOff,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                    tint = if (rule.targetMode == SoundMode.VIBRATE) AccentPurple else MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = rule.targetMode.displayName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (rule.targetMode == SoundMode.VIBRATE) AccentPurple else MaterialTheme.colorScheme.error
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

    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        dayLabels.forEach { (dayInt, label) ->
            val isSelected = daysOfWeek.contains(dayInt)
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            !isEnabled -> Color.Transparent
                            isSelected -> AccentTeal
                            else -> Color(0x18FFFFFF)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        !isEnabled && isSelected -> Color(0x55FFFFFF)
                        isSelected -> Color.White
                        else -> Color(0x44FFFFFF)
                    }
                )
            }
        }
    }
}
