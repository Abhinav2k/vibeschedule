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
        for (appWidgetId in appWidgetIds) updateAppWidget(context, appWidgetManager, appWidgetId)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val scheduler = AlarmScheduler(context)

        when (intent.action) {
            ACTION_WIDGET_CANCEL_ACTIVE -> {
                val repo = ScheduleRepository(context)
                val activeRule = getActiveScheduleRule(context)
                val isQuickMuteActive = repo.getQuickMuteUntil() != null
                val isPauseActive = repo.getPauseUntil() != null
                val isPhoneMuted = isMutedOrVibrating(audioManager)
                val isNotificationActive = isStatusNotificationShowing(notificationManager)
                if (activeRule == null && !isQuickMuteActive && !isPauseActive && !isPhoneMuted && !isNotificationActive) {
                    Toast.makeText(context, "No active schedule or timer", Toast.LENGTH_SHORT).show()
                    return
                }
                try {
                    if (activeRule != null) {
                        val endMillis = ScheduleRule.calculateRuleEndMillis(activeRule)
                        repo.dismissScheduleUntil(activeRule.id, endMillis)
                        repo.setDismissedActiveUntil(endMillis)
                    } else {
                        val endCal = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, 23)
                            set(Calendar.MINUTE, 59)
                            set(Calendar.SECOND, 59)
                        }
                        repo.setDismissedActiveUntil(endCal.timeInMillis)
                    }
                    repo.setQuickMuteUntil(null)
                    repo.setPauseUntil(null)
                    SoundModeHelper.applySoundMode(context, SoundMode.NORMAL, audioManager, notificationManager)
                    NotificationHelper.dismissNotification(context)
                    scheduler.cancelPauseEnd()
                    scheduler.cancelQuickMute()
                    ScheduleRepository.notifyStateChanged()
                    val syncIntent = Intent(ACTION_SYNC_APP_STATE).apply {
                        setPackage(context.packageName)
                    }
                    context.sendBroadcast(syncIntent)
                    Toast.makeText(context, "Cancelled • Normal Ring", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) { e.printStackTrace() }
                updateAll(context)
            }

            ACTION_WIDGET_SKIP_PERIOD -> {
                val repo = ScheduleRepository(context)
                val activeRule = getActiveScheduleRule(context)
                val startingSoonRule = getStartingSoonRule(context)
                if (activeRule == null && startingSoonRule == null) {
                    Toast.makeText(context, "No active schedule to skip", Toast.LENGTH_SHORT).show()
                    return
                }
                val nextHour = Calendar.getInstance().apply {
                    add(Calendar.HOUR_OF_DAY, 1)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                try {
                    repo.setPauseUntil(nextHour.timeInMillis)
                    SoundModeHelper.applySoundMode(context, SoundMode.NORMAL, audioManager, notificationManager)
                    scheduler.schedulePauseEnd(nextHour.timeInMillis)
                    val label = activeRule?.title ?: startingSoonRule?.title ?: "Schedule"
                    NotificationHelper.showActiveStatusNotification(context, "$label Paused", SoundMode.NORMAL, endMillis = nextHour.timeInMillis, canSkip = false)
                    ScheduleRepository.notifyStateChanged()
                    val syncIntent = Intent(ACTION_SYNC_APP_STATE).apply {
                        setPackage(context.packageName)
                    }
                    context.sendBroadcast(syncIntent)
                    Toast.makeText(context, "Skipped $label until next :00", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) { e.printStackTrace() }
                updateAll(context)
            }
        }
    }

    companion object {
        const val ACTION_WIDGET_CANCEL_ACTIVE = "com.vibeschedule.app.ACTION_WIDGET_CANCEL_ACTIVE"
        const val ACTION_WIDGET_SKIP_PERIOD = "com.vibeschedule.app.ACTION_WIDGET_SKIP_PERIOD"
        const val ACTION_WIDGET_SKIP_HOUR = "com.vibeschedule.app.ACTION_WIDGET_SKIP_PERIOD"
        const val ACTION_SYNC_APP_STATE = "com.vibeschedule.app.ACTION_SYNC_APP_STATE"

        private fun getActiveScheduleRule(context: Context): ScheduleRule? {
            val repo = ScheduleRepository(context)
            val dismissedActiveUntil = repo.getDismissedActiveUntil()
            if (dismissedActiveUntil != null && System.currentTimeMillis() < dismissedActiveUntil) {
                return null
            }
            val allSchedules = repo.getAllSchedules().filter { it.isEnabled }
            val now = Calendar.getInstance()
            val curDay = now.get(Calendar.DAY_OF_WEEK)
            val curMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
            val yesterdayDay = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }.get(Calendar.DAY_OF_WEEK)
            return allSchedules.firstOrNull { rule ->
                if (rule.daysOfWeek.isEmpty()) return@firstOrNull false
                if (repo.isScheduleDismissed(rule.id)) return@firstOrNull false
                val startMin = rule.startHour * 60 + rule.startMinute
                val endMin = rule.endHour * 60 + rule.endMinute
                if (startMin < endMin) rule.daysOfWeek.contains(curDay) && curMinutes in startMin until endMin
                else (rule.daysOfWeek.contains(curDay) && curMinutes >= startMin) ||
                    (rule.daysOfWeek.contains(yesterdayDay) && curMinutes < endMin)
            }
        }

        private fun getStartingSoonRule(context: Context): ScheduleRule? {
            val allSchedules = ScheduleRepository(context).getAllSchedules().filter { it.isEnabled }
            val now = Calendar.getInstance()
            val curDay = now.get(Calendar.DAY_OF_WEEK)
            val curMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
            return allSchedules.firstOrNull { rule ->
                rule.daysOfWeek.contains(curDay) && (rule.startHour * 60 + rule.startMinute - curMinutes) in 1..25
            }
        }

        private fun isMutedOrVibrating(audioManager: AudioManager) =
            audioManager.ringerMode == AudioManager.RINGER_MODE_VIBRATE || audioManager.ringerMode == AudioManager.RINGER_MODE_SILENT

        private fun isStatusNotificationShowing(notificationManager: NotificationManager) = try {
            notificationManager.activeNotifications.any { it.id == NotificationHelper.NOTIFICATION_ID }
        } catch (e: Exception) { false }

        private fun statusText(context: Context): String {
            val repo = ScheduleRepository(context)

            // 1. Check persistent Quick Mute timer
            val qmUntil = repo.getQuickMuteUntil()
            if (qmUntil != null && qmUntil > System.currentTimeMillis()) {
                val remSeconds = ((qmUntil - System.currentTimeMillis()) / 1000L).coerceAtLeast(0)
                val remMin = (remSeconds + 59) / 60
                return if (remMin > 0) "● Timer running (${remMin}m)" else "● Timer running"
            }

            // 2. Check persistent Pause
            val pauseUntil = repo.getPauseUntil()
            if (pauseUntil != null && pauseUntil > System.currentTimeMillis()) {
                return "● Schedule paused"
            }

            // 3. Active schedule rule
            val activeRule = getActiveScheduleRule(context)
            if (activeRule != null) return "● Schedule running"

            // 4. Fallback check: active notification
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val activeNotification = try {
                notificationManager.activeNotifications.firstOrNull { it.id == NotificationHelper.NOTIFICATION_ID }
            } catch (e: Exception) { null }
            val title = (activeNotification?.notification?.extras?.getCharSequence(androidx.core.app.NotificationCompat.EXTRA_TITLE)
                ?: activeNotification?.notification?.extras?.getCharSequence("android.title"))?.toString() ?: ""
            return when {
                title.contains("quick mute", ignoreCase = true) -> "● Timer running"
                title.contains("paused", ignoreCase = true) -> "● Schedule paused"
                activeNotification != null -> "● Schedule running"
                else -> "○ Nothing running"
            }
        }

        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, VibeWidgetProvider::class.java)
            manager.getAppWidgetIds(component).forEach { updateAppWidget(context, manager, it) }
        }

        private fun updateAppWidget(context: Context, manager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_vibe_schedule)
            views.setTextViewText(R.id.widget_status, statusText(context))
            val skipIntent = Intent(context, VibeWidgetProvider::class.java).apply { action = ACTION_WIDGET_SKIP_PERIOD }
            views.setOnClickPendingIntent(R.id.btn_widget_skip, PendingIntent.getBroadcast(context, 202, skipIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
            val cancelIntent = Intent(context, VibeWidgetProvider::class.java).apply { action = ACTION_WIDGET_CANCEL_ACTIVE }
            views.setOnClickPendingIntent(R.id.btn_widget_cancel, PendingIntent.getBroadcast(context, 201, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
            manager.updateAppWidget(appWidgetId, views)
        }
    }
}
