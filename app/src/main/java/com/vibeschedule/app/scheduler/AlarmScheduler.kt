package com.vibeschedule.app.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.vibeschedule.app.model.ScheduleRule
import com.vibeschedule.app.receiver.AlarmReceiver
import java.util.Calendar

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleRule(rule: ScheduleRule) {
        if (!rule.isEnabled || rule.daysOfWeek.isEmpty()) {
            cancelRule(rule)
            return
        }

        // Schedule START alarm
        val nextStartMillis = calculateNextTriggerMillis(rule.startHour, rule.startMinute, rule.daysOfWeek)
        val startIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_SCHEDULE_START
            putExtra(AlarmReceiver.EXTRA_RULE_ID, rule.id)
            putExtra(AlarmReceiver.EXTRA_RULE_TITLE, rule.title)
            putExtra(AlarmReceiver.EXTRA_TARGET_MODE, rule.targetMode.name)
        }
        val startPendingIntent = PendingIntent.getBroadcast(
            context,
            getStartRequestCode(rule.id),
            startIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        setExactAlarm(nextStartMillis, startPendingIntent)

        // Schedule END alarm
        val nextEndMillis = calculateNextTriggerMillis(rule.endHour, rule.endMinute, rule.daysOfWeek)
        val endIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_SCHEDULE_END
            putExtra(AlarmReceiver.EXTRA_RULE_ID, rule.id)
            putExtra(AlarmReceiver.EXTRA_RULE_TITLE, rule.title)
            putExtra(AlarmReceiver.EXTRA_TARGET_MODE, rule.revertMode.name)
        }
        val endPendingIntent = PendingIntent.getBroadcast(
            context,
            getEndRequestCode(rule.id),
            endIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        setExactAlarm(nextEndMillis, endPendingIntent)

        Log.d("AlarmScheduler", "Scheduled '${rule.title}': Start=$nextStartMillis, End=$nextEndMillis")
    }

    fun cancelRule(rule: ScheduleRule) {
        val startIntent = Intent(context, AlarmReceiver::class.java)
        val startPI = PendingIntent.getBroadcast(
            context,
            getStartRequestCode(rule.id),
            startIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (startPI != null) {
            alarmManager.cancel(startPI)
            startPI.cancel()
        }

        val endIntent = Intent(context, AlarmReceiver::class.java)
        val endPI = PendingIntent.getBroadcast(
            context,
            getEndRequestCode(rule.id),
            endIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (endPI != null) {
            alarmManager.cancel(endPI)
            endPI.cancel()
        }
    }

    fun scheduleQuickMute(durationMinutes: Int) {
        val triggerAtMillis = System.currentTimeMillis() + (durationMinutes * 60 * 1000L)
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_QUICK_MUTE_END
        }
        val pi = PendingIntent.getBroadcast(
            context,
            999999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        setExactAlarm(triggerAtMillis, pi)
    }

    fun schedulePauseEnd(triggerAtMillis: Long) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_PAUSE_END
        }
        val pi = PendingIntent.getBroadcast(
            context,
            888888,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        setExactAlarm(triggerAtMillis, pi)
    }

    fun cancelPauseEnd() {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context,
            888888,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pi != null) {
            alarmManager.cancel(pi)
            pi.cancel()
        }
    }

    private fun setExactAlarm(triggerAtMillis: Long, pendingIntent: PendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    private fun calculateNextTriggerMillis(hour: Int, minute: Int, daysOfWeek: List<Int>): Long {
        val now = Calendar.getInstance()
        var bestCandidate: Calendar? = null

        for (day in daysOfWeek) {
            val candidate = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                set(Calendar.DAY_OF_WEEK, day)
            }

            if (candidate.before(now) || candidate.timeInMillis <= now.timeInMillis) {
                candidate.add(Calendar.WEEK_OF_YEAR, 1)
            }

            if (bestCandidate == null || candidate.before(bestCandidate)) {
                bestCandidate = candidate
            }
        }

        return bestCandidate?.timeInMillis ?: (now.timeInMillis + 60_000L)
    }

    private fun getStartRequestCode(id: String): Int = (Math.abs(id.hashCode()) % 1_000_000) * 2
    private fun getEndRequestCode(id: String): Int = getStartRequestCode(id) + 1
}
