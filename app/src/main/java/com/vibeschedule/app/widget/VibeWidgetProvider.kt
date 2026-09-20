package com.vibeschedule.app.widget

import android.app.NotificationManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.widget.RemoteViews
import android.widget.Toast
import com.vibeschedule.app.R
import com.vibeschedule.app.data.ScheduleRepository
import com.vibeschedule.app.model.ScheduleRule
import com.vibeschedule.app.model.SoundMode
import com.vibeschedule.app.scheduler.AlarmScheduler
import com.vibeschedule.app.util.NotificationHelper
import com.vibeschedule.app.util.SoundModeHelper
import java.util.Calendar

class VibeWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val scheduler = AlarmScheduler(context)

        when (intent.action) {
            ACTION_WIDGET_CANCEL_ACTIVE -> {
                val activeRule = getActiveScheduleRule(context)
                val isPhoneMuted = isMutedOrVibrating(audioManager)
                val isNotificationActive = isStatusNotificationShowing(notificationManager)

                // If no active schedule, timer, or notification is running, do nothing
                if (activeRule == null && !isPhoneMuted && !isNotificationActive) {
                    Toast.makeText(context, "No active schedule or timer", Toast.LENGTH_SHORT).show()
                    return
                }

                // Cancel running schedule / quick mute and restore Normal sound
                try {
                    SoundModeHelper.applySoundMode(context, SoundMode.NORMAL, audioManager, notificationManager)
                    NotificationHelper.dismissNotification(context)
                    scheduler.cancelPauseEnd()
                    Toast.makeText(context, "Schedule Cancelled • Normal Ring", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                updateAll(context)
            }

            ACTION_WIDGET_SKIP_PERIOD -> {
                val activeRule = getActiveScheduleRule(context)
                val startingSoonRule = getStartingSoonRule(context)

                // If no active schedule or upcoming schedule starting soon, do nothing
                if (activeRule == null && startingSoonRule == null) {
                    Toast.makeText(context, "No active schedule to skip", Toast.LENGTH_SHORT).show()
                    return
                }

                // Skip / pause until next O'clock
                val nextHour = Calendar.getInstance().apply {
                    add(Calendar.HOUR_OF_DAY, 1)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                try {
                    SoundModeHelper.applySoundMode(context, SoundMode.NORMAL, audioManager, notificationManager)
                    scheduler.schedulePauseEnd(nextHour.timeInMillis)
                    val label = activeRule?.title ?: startingSoonRule?.title ?: "Schedule"
                    NotificationHelper.showActiveStatusNotification(
                        context = context,
                        title = "$label Paused",
                        targetMode = SoundMode.NORMAL,
                        endMillis = nextHour.timeInMillis,
                        canSkip = false
                    )
                    Toast.makeText(context, "Skipped $label until next :00", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                updateAll(context)
            }
        }
    }

    companion object {
        const val ACTION_WIDGET_CANCEL_ACTIVE = "com.vibeschedule.app.ACTION_WIDGET_CANCEL_ACTIVE"
        const val ACTION_WIDGET_SKIP_PERIOD = "com.vibeschedule.app.ACTION_WIDGET_SKIP_PERIOD"

        private fun getActiveScheduleRule(context: Context): ScheduleRule? {
            val repo = ScheduleRepository(context)
            val allSchedules = repo.getAllSchedules().filter { it.isEnabled }
            val now = Calendar.getInstance()
            val curDay = now.get(Calendar.DAY_OF_WEEK)
            val curMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
            val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
            val yesterdayDay = yesterdayCal.get(Calendar.DAY_OF_WEEK)

            return allSchedules.firstOrNull { rule ->
                if (rule.daysOfWeek.isEmpty()) return@firstOrNull false
                val startMin = rule.startHour * 60 + rule.startMinute
                val endMin = rule.endHour * 60 + rule.endMinute

                if (startMin < endMin) {
                    rule.daysOfWeek.contains(curDay) && curMinutes in startMin until endMin
                } else {
                    (rule.daysOfWeek.contains(curDay) && curMinutes >= startMin) ||
                    (rule.daysOfWeek.contains(yesterdayDay) && curMinutes < endMin)
                }
            }
        }

        private fun getStartingSoonRule(context: Context): ScheduleRule? {
            val repo = ScheduleRepository(context)
            val allSchedules = repo.getAllSchedules().filter { it.isEnabled }
            val now = Calendar.getInstance()
            val curDay = now.get(Calendar.DAY_OF_WEEK)
            val curMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

            for (rule in allSchedules) {
                if (!rule.daysOfWeek.contains(curDay)) continue
                val startMin = rule.startHour * 60 + rule.startMinute
                val diff = startMin - curMinutes
                if (diff in 1..25) {
                    return rule
                }
            }
            return null
        }

        private fun isMutedOrVibrating(audioManager: AudioManager): Boolean {
            return audioManager.ringerMode == AudioManager.RINGER_MODE_VIBRATE ||
                   audioManager.ringerMode == AudioManager.RINGER_MODE_SILENT
        }

        private fun isStatusNotificationShowing(notificationManager: NotificationManager): Boolean {
            return try {
                notificationManager.activeNotifications.any { it.id == 8823 }
            } catch (e: Exception) {
                false
            }
        }

        fun updateAll(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, VibeWidgetProvider::class.java)
            val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            for (id in allWidgetIds) {
                updateAppWidget(context, appWidgetManager, id)
            }
        }

        private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_vibe_schedule)

            // Button 1: Skip This Period (Pause until next :00)
            val skipIntent = Intent(context, VibeWidgetProvider::class.java).apply {
                action = ACTION_WIDGET_SKIP_PERIOD
            }
            val skipPendingIntent = PendingIntent.getBroadcast(
                context, 202, skipIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_widget_skip, skipPendingIntent)

            // Button 2: Cancel Running Schedule (Restore Normal ring)
            val cancelIntent = Intent(context, VibeWidgetProvider::class.java).apply {
                action = ACTION_WIDGET_CANCEL_ACTIVE
            }
            val cancelPendingIntent = PendingIntent.getBroadcast(
                context, 201, cancelIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_widget_cancel, cancelPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
