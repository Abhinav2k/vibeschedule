package com.vibeschedule.app.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vibeschedule.app.model.ScheduleRule
import com.vibeschedule.app.ui.MainViewModel
import com.vibeschedule.app.ui.components.GlassCard
import com.vibeschedule.app.ui.components.PermissionBanner
import com.vibeschedule.app.ui.components.ScheduleCard
import com.vibeschedule.app.ui.theme.AccentTeal

@Composable
fun SchedulesScreen(
    viewModel: MainViewModel,
    onEditRule: (ScheduleRule) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val schedules by viewModel.schedules.collectAsState()

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Permission Banner (if needed)
        PermissionBanner()

        // Quick Mute Glass Bar
        QuickMuteSection(
            onQuickMute = { minutes ->
                viewModel.startQuickMute(minutes)
                Toast.makeText(context, "Vibrate enabled for $minutes min", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Schedule List
        if (schedules.isEmpty()) {
            EmptySchedulesPlaceholder()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 90.dp, top = 4.dp)
            ) {
                items(schedules, key = { it.id }) { rule ->
                    ScheduleCard(
                        rule = rule,
                        onToggle = { isEnabled ->
                            viewModel.toggleSchedule(rule.id, isEnabled)
                        },
                        onEdit = { onEditRule(rule) },
                        onDelete = {
                            viewModel.deleteSchedule(rule.id)
                            Toast.makeText(context, "Deleted '${rule.title}'", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickMuteSection(
    onQuickMute: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = Color(0x12FFFFFF)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = null,
                tint = AccentTeal,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "Quick Mute (One-Tap)",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(15 to "15m", 30 to "30m", 60 to "1h", 120 to "2h").forEach { (min, label) ->
                AssistChip(
                    onClick = { onQuickMute(min) },
                    label = { Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Color(0x18FFFFFF),
                        labelColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
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
                imageVector = Icons.Default.NotificationsActive,
                contentDescription = null,
                tint = Color(0x44FFFFFF),
                modifier = Modifier.size(60.dp)
            )
            Text(
                text = "No schedules yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Tap the + button to schedule your phone to automatically switch into Vibrate mode.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0x77FFFFFF),
                textAlign = TextAlign.Center
            )
        }
    }
}
