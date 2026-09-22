package com.vibeschedule.app.ui.screens

import android.app.NotificationManager
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vibeschedule.app.ui.components.GlassCard
import com.vibeschedule.app.ui.theme.GlassBorderBrush
import com.vibeschedule.app.ui.theme.TextPrimary
import com.vibeschedule.app.ui.theme.TextSecondary
import com.vibeschedule.app.ui.theme.TextTertiary
import com.vibeschedule.app.util.NotificationHelper

const val PREFS_SETTINGS = "vibe_settings_prefs"
const val PREF_NOTIF_HIGH_PRIORITY = "notif_high_priority"

fun getNotifHighPriority(context: Context): Boolean {
    return context.getSharedPreferences(PREFS_SETTINGS, Context.MODE_PRIVATE)
        .getBoolean(PREF_NOTIF_HIGH_PRIORITY, true)
}

fun setNotifHighPriority(context: Context, value: Boolean) {
    context.getSharedPreferences(PREFS_SETTINGS, Context.MODE_PRIVATE)
        .edit().putBoolean(PREF_NOTIF_HIGH_PRIORITY, value).apply()
}

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var highPriority by remember { mutableStateOf(getNotifHighPriority(context)) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Notifications",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            color = TextTertiary,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )

        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            backgroundColor = Color(0x10FFFFFF),
            borderBrush = GlassBorderBrush
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                    Column {
                        Text(
                            text = "High Priority Notification",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Maximizes lock screen visibility with large interactive action buttons",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }

                Switch(
                    checked = highPriority,
                    onCheckedChange = { value ->
                        highPriority = value
                        setNotifHighPriority(context, value)
                        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        NotificationHelper.createNotificationChannel(context, nm)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = Color.White,
                        uncheckedTrackColor = Color(0x20FFFFFF),
                        uncheckedThumbColor = Color(0x66FFFFFF)
                    )
                )
            }
        }

        Text(
            text = "When enabled, the active schedule notification is delivered with top-tier priority and public visibility so it stays visible on your lock screen with instant one-tap Skip and End controls.",
            fontSize = 11.sp,
            color = TextTertiary,
            lineHeight = 16.sp,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "VibeSchedule v1.4.18 (Debug)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Optimized for Android 16 & OriginOS",
                fontSize = 10.sp,
                color = TextTertiary
            )
        }
    }
}
