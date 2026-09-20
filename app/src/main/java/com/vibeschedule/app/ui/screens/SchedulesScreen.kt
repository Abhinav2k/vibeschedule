package com.vibeschedule.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vibeschedule.app.model.ScheduleRule
import com.vibeschedule.app.ui.MainViewModel
import com.vibeschedule.app.ui.components.GlassCard
import com.vibeschedule.app.ui.components.PermissionBanner
import com.vibeschedule.app.ui.components.ScheduleCard
import com.vibeschedule.app.ui.theme.AccentTeal
import com.vibeschedule.app.ui.theme.TextPrimary
import com.vibeschedule.app.ui.theme.TextSecondary
import com.vibeschedule.app.ui.theme.TextTertiary

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
                Toast.makeText(context, "Muted for $minutes min", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Schedule List
        if (schedules.isEmpty()) {
            EmptySchedulesPlaceholder()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp, top = 4.dp)
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
        shape = RoundedCornerShape(20.dp),
        backgroundColor = Color(0x10FFFFFF)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = null,
                tint = AccentTeal,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = "Quick Mute",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(15 to "15m", 30 to "30m", 60 to "1h", 120 to "2h").forEach { (min, label) ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .background(Color(0x18FFFFFF))
                        .border(0.5.dp, Color(0x1AFFFFFF), CircleShape)
                        .clickable { onQuickMute(min) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsNone,
                contentDescription = null,
                tint = TextTertiary,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "No Schedules",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = "Tap + to create a quiet hours schedule",
                fontSize = 13.sp,
                color = TextSecondary
            )
        }
    }
}
