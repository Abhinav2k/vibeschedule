package com.vibeschedule.app.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.vibeschedule.app.R
import com.vibeschedule.app.data.ScheduleRepository
import com.vibeschedule.app.model.ScheduleRule
import com.vibeschedule.app.model.SoundMode
import com.vibeschedule.app.scheduler.AlarmScheduler
import com.vibeschedule.app.widget.VibeWidgetProvider
import java.util.Calendar

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        when (action) {
            ACTION_SCHEDULE_START -> {
                val ruleId = intent.getStringExtra(EXTRA_RULE_ID) ?: return
                val ruleTitle = intent.getStringExtra(EXTRA_RULE_TITLE) ?: "Scheduled Event"
                val targetModeStr = intent.getStringExtra(EXTRA_TARGET_MODE) ?: SoundMode.VIBRATE.name
                val targetMode = SoundMode.valueOf(targetModeStr)

                applySoundMode(context, audioManager, notificationManager, targetMode)
                showStatusNotification(context, notificationManager, "VibeSchedule Active", "$ruleTitle: Switched to ${targetMode.displayName}")

                val repo = ScheduleRepository(context)
                repo.getScheduleById(ruleId)?.let { rule ->
                    AlarmScheduler(context).scheduleRule(rule)
                }
                VibeWidgetProvider.updateAll(context)
            }

            ACTION_SCHEDULE_END -> {
                val ruleId = intent.getStringExtra(EXTRA_RULE_ID) ?: return
                val revertModeStr = intent.getStringExtra(EXTRA_TARGET_MODE) ?: SoundMode.NORMAL.name
                val revertMode = SoundMode.valueOf(revertModeStr)

                applySoundMode(context, audioManager, notificationManager, revertMode)
                cancelStatusNotification(notificationManager)

                val repo = ScheduleRepository(context)
                repo.getScheduleById(ruleId)?.let { rule ->
                    AlarmScheduler(context).scheduleRule(rule)
                }
                VibeWidgetProvider.updateAll(context)
            }

            ACTION_QUICK_MUTE_END -> {
                applySoundMode(context, audioManager, notificationManager, SoundMode.NORMAL)
                cancelStatusNotification(notificationManager)
            }

            ACTION_PAUSE_END -> {
                // Check if any rule is currently active right now
                val repo = ScheduleRepository(context)
                val activeRule = findCurrentActiveRule(repo.getAllSchedules())
                if (activeRule != null && activeRule.isEnabled) {
                    applySoundMode(context, audioManager, notificationManager, activeRule.targetMode)
                    showStatusNotification(context, notificationManager, "Schedule Resumed", "${activeRule.title}: Switched to ${activeRule.targetMode.displayName}")
                } else {
                    applySoundMode(context, audioManager, notificationManager, SoundMode.NORMAL)
                    cancelStatusNotification(notificationManager)
                }
                VibeWidgetProvider.updateAll(context)
            }
        }
    }

    private fun findCurrentActiveRule(schedules: List<ScheduleRule>): ScheduleRule? {
        val now = Calendar.getInstance()
        val currentDay = now.get(Calendar.DAY_OF_WEEK)
        val curMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

        return schedules.firstOrNull { rule ->
            if (!rule.isEnabled || !rule.daysOfWeek.contains(currentDay)) return@firstOrNull false
            val startMin = rule.startHour * 60 + rule.startMinute
            val endMin = rule.endHour * 60 + rule.endMinute

            if (startMin < endMin) {
                curMinutes in startMin until endMin
            } else {
                // Overnight rule
                curMinutes >= startMin || curMinutes < endMin
            }
        }
    }

    private fun applySoundMode(
        context: Context,
        audioManager: AudioManager,
        notificationManager: NotificationManager,
        mode: SoundMode
    ) {
        try {
            when (mode) {
                SoundMode.VIBRATE -> {
                    if (notificationManager.isNotificationPolicyAccessGranted) {
                        notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                    }
                    audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                }
                SoundMode.SILENT -> {
                    if (notificationManager.isNotificationPolicyAccessGranted) {
                        notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
                    }
                    audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
                }
                SoundMode.NORMAL -> {
                    if (notificationManager.isNotificationPolicyAccessGranted) {
                        notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                    }
                    audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                }
            }
        } catch (e: SecurityException) {
            Log.e("AlarmReceiver", "Missing permission: ${e.message}")
        }
    }

    private fun showStatusNotification(
        context: Context,
        notificationManager: NotificationManager,
        title: String,
        content: String
    ) {
        createNotificationChannel(context, notificationManager)

        val revertIntent = Intent(context, QuickMuteReceiver::class.java).apply {
            action = ACTION_REVERT_NOW
        }
        val revertPI = PendingIntent.getBroadcast(
            context,
            1001,
            revertIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_silent_mode)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_revert, "Revert to Normal", revertPI)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun cancelStatusNotification(notificationManager: NotificationManager) {
        notificationManager.cancel(NOTIFICATION_ID)
    }

    private fun createNotificationChannel(context: Context, notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.channel_description)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val ACTION_SCHEDULE_START = "com.vibeschedule.app.ACTION_SCHEDULE_START"
        const val ACTION_SCHEDULE_END = "com.vibeschedule.app.ACTION_SCHEDULE_END"
        const val ACTION_QUICK_MUTE_END = "com.vibeschedule.app.ACTION_QUICK_MUTE_END"
        const val ACTION_PAUSE_END = "com.vibeschedule.app.ACTION_PAUSE_END"
        const val ACTION_REVERT_NOW = "com.vibeschedule.app.ACTION_REVERT_NOW"

        const val EXTRA_RULE_ID = "EXTRA_RULE_ID"
        const val EXTRA_RULE_TITLE = "EXTRA_RULE_TITLE"
        const val EXTRA_TARGET_MODE = "EXTRA_TARGET_MODE"

        private const val CHANNEL_ID = "vibe_schedule_channel"
        private const val NOTIFICATION_ID = 8823
    }
}
