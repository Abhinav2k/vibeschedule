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
import com.vibeschedule.app.util.NotificationHelper
import com.vibeschedule.app.util.SoundModeHelper
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
                val targetMode = try { SoundMode.valueOf(intent.getStringExtra(EXTRA_TARGET_MODE) ?: SoundMode.VIBRATE.name) } catch (e: Exception) { SoundMode.VIBRATE }
                SoundModeHelper.applySoundMode(context, targetMode, audioManager, notificationManager)
                val repo = ScheduleRepository(context)
                val rule = repo.getScheduleById(ruleId)
                if (rule != null) {
                    NotificationHelper.showActiveRuleNotification(context, rule)
                    AlarmScheduler(context).scheduleRule(rule)
                } else {
                    NotificationHelper.showActiveStatusNotification(context, ruleTitle, targetMode, canSkip = true)
                }
                VibeWidgetProvider.updateAll(context)
            }
            ACTION_SCHEDULE_END -> {
                val repo = ScheduleRepository(context)
                val currentlyActive = findCurrentActiveRule(repo.getAllSchedules())
                if (currentlyActive != null && currentlyActive.isEnabled) {
                    SoundModeHelper.applySoundMode(context, currentlyActive.targetMode, audioManager, notificationManager)
                    NotificationHelper.showActiveRuleNotification(context, currentlyActive)
                } else {
                    val revertMode = try { SoundMode.valueOf(intent.getStringExtra(EXTRA_TARGET_MODE) ?: SoundMode.NORMAL.name) } catch (e: Exception) { SoundMode.NORMAL }
                    SoundModeHelper.applySoundMode(context, revertMode, audioManager, notificationManager)
                    NotificationHelper.dismissNotification(context)
                }
                intent.getStringExtra(EXTRA_RULE_ID)?.let { repo.getScheduleById(it)?.let { rule -> AlarmScheduler(context).scheduleRule(rule) } }
                VibeWidgetProvider.updateAll(context)
            }
            ACTION_QUICK_MUTE_END -> {
                ScheduleRepository(context).setQuickMuteUntil(null)
                SoundModeHelper.applySoundMode(context, SoundMode.NORMAL, audioManager, notificationManager)
                NotificationHelper.dismissNotification(context)
                VibeWidgetProvider.updateAll(context)
                ScheduleRepository.notifyStateChanged()
                context.sendBroadcast(Intent(VibeWidgetProvider.ACTION_SYNC_APP_STATE).apply { setPackage(context.packageName) })
            }
            ACTION_PAUSE_END -> {
                ScheduleRepository(context).setPauseUntil(null)
                val activeRule = findCurrentActiveRule(ScheduleRepository(context).getAllSchedules())
                if (activeRule != null && activeRule.isEnabled) {
                    SoundModeHelper.applySoundMode(context, activeRule.targetMode, audioManager, notificationManager)
                    NotificationHelper.showActiveRuleNotification(context, activeRule)
                } else {
                    SoundModeHelper.applySoundMode(context, SoundMode.NORMAL, audioManager, notificationManager)
                    NotificationHelper.dismissNotification(context)
                }
                VibeWidgetProvider.updateAll(context)
                ScheduleRepository.notifyStateChanged()
                context.sendBroadcast(Intent(VibeWidgetProvider.ACTION_SYNC_APP_STATE).apply { setPackage(context.packageName) })
            }
        }
    }

    private fun findCurrentActiveRule(schedules: List<ScheduleRule>): ScheduleRule? {
        val now = Calendar.getInstance()
        val curDay = now.get(Calendar.DAY_OF_WEEK)
        val curMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val yesterdayDay = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }.get(Calendar.DAY_OF_WEEK)
        return schedules.firstOrNull { rule ->
            if (!rule.isEnabled || rule.daysOfWeek.isEmpty()) return@firstOrNull false
            val startMin = rule.startHour * 60 + rule.startMinute
            val endMin = rule.endHour * 60 + rule.endMinute
            if (startMin < endMin) rule.daysOfWeek.contains(curDay) && curMinutes in startMin until endMin
            else (rule.daysOfWeek.contains(curDay) && curMinutes >= startMin) || (rule.daysOfWeek.contains(yesterdayDay) && curMinutes < endMin)
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
    }
}
