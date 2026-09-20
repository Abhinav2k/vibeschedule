package com.vibeschedule.app.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.vibeschedule.app.model.ScheduleRule
import com.vibeschedule.app.receiver.AlarmReceiver
import com.vibeschedule.app.ui.MainActivity
import java.util.Calendar

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleRule(rule: ScheduleRule) {
        if (!rule.isEnabled || rule.daysOfWeek.isEmpty()) {
            cancelRule(rule)
            return
        }

        val now = Calendar.getInstance()
        val curDay = now.get(Calendar.DAY_OF_WEEK)
        val curMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

        val startMin = rule.startHour * 60 + rule.startMinute
        val endMin = rule.endHour * 60 + rule.endMinute

        // 1. Determine if this rule is ACTIVE right now
        val isSameDay = startMin < endMin
        val isActiveNow: Boolean
        val activeEndCal: Calendar?

        if (isSameDay) {
            if (rule.daysOfWeek.contains(curDay) && curMinutes in startMin until endMin) {
                isActiveNow = true
                activeEndCal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, rule.endHour)
                    set(Calendar.MINUTE, rule.endMinute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
            } else {
                isActiveNow = false
                activeEndCal = null
            }
        } else {
            // Overnight rule: e.g. 22:00 to 07:00
            val startedToday = rule.daysOfWeek.contains(curDay) && curMinutes >= startMin
            val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
            val yesterdayDay = yesterdayCal.get(Calendar.DAY_OF_WEEK)
            val startedYesterday = rule.daysOfWeek.contains(yesterdayDay) && curMinutes < endMin

            if (startedToday) {
                isActiveNow = true
                // Started today evening, ends tomorrow morning
                activeEndCal = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, 1)
                    set(Calendar.HOUR_OF_DAY, rule.endHour)
                    set(Calendar.MINUTE, rule.endMinute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
            } else if (startedYesterday) {
                isActiveNow = true
                // Started yesterday evening, ends today morning
                activeEndCal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, rule.endHour)
                    set(Calendar.MINUTE, rule.endMinute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
            } else {
                isActiveNow = false
                activeEndCal = null
            }
        }

        if (isActiveNow && activeEndCal != null) {
            // Session is ACTIVE: schedule its matching END alarm
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
            setExactAlarm(activeEndCal.timeInMillis, endPendingIntent)
            Log.d("AlarmScheduler", "Active rule '${rule.title}': END set for ${activeEndCal.time}")

            // Also schedule the NEXT start that occurs after activeEndCal
            val nextStartMillis = findNextStartMillis(rule, afterMillis = activeEndCal.timeInMillis)
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
            Log.d("AlarmScheduler", "Active rule '${rule.title}': next START set for ${java.util.Date(nextStartMillis)}")
        } else {
            // Session is IDLE: find the next upcoming START, and schedule matching END = START + duration
            val nextStartMillis = findNextStartMillis(rule, afterMillis = now.timeInMillis)

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

            // Duration in minutes (accurately accounts for overnight)
            val durationMinutes = if (isSameDay) {
                endMin - startMin
            } else {
                (24 * 60 - startMin) + endMin
            }
            val nextEndMillis = nextStartMillis + (durationMinutes * 60_000L)

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
            Log.d("AlarmScheduler", "Idle rule '${rule.title}': START=${java.util.Date(nextStartMillis)}, END=${java.util.Date(nextEndMillis)}")
        }
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
        val showIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val showPendingIntent = PendingIntent.getActivity(
            context,
            0,
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerAtMillis, showPendingIntent)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                }
            } else {
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            }
        } catch (e: SecurityException) {
            Log.e("AlarmScheduler", "Exact alarm permission error, falling back: ${e.message}")
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    private fun findNextStartMillis(rule: ScheduleRule, afterMillis: Long): Long {
        val baseCal = Calendar.getInstance().apply { timeInMillis = afterMillis }
        var earliestMillis: Long? = null

        // Check the next 8 days from baseCal
        for (dayOffset in 0..8) {
            val candidate = Calendar.getInstance().apply {
                timeInMillis = baseCal.timeInMillis
                add(Calendar.DAY_OF_YEAR, dayOffset)
                set(Calendar.HOUR_OF_DAY, rule.startHour)
                set(Calendar.MINUTE, rule.startMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val dayOfWeek = candidate.get(Calendar.DAY_OF_WEEK)
            if (rule.daysOfWeek.contains(dayOfWeek) && candidate.timeInMillis > afterMillis) {
                if (earliestMillis == null || candidate.timeInMillis < earliestMillis) {
                    earliestMillis = candidate.timeInMillis
                }
            }
        }

        return earliestMillis ?: (afterMillis + 24 * 3600_000L)
    }

    private fun getStartRequestCode(id: String): Int = ((id.hashCode() and 0x7FFFFFFF) % 1_000_000) * 2
    private fun getEndRequestCode(id: String): Int = getStartRequestCode(id) + 1
}
