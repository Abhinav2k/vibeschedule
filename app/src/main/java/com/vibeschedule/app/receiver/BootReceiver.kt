package com.vibeschedule.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.vibeschedule.app.data.ScheduleRepository
import com.vibeschedule.app.scheduler.AlarmScheduler

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == "android.intent.action.QUICKBOOT_POWERON" ||
            action == "com.htc.intent.action.QUICKBOOT_POWERON"
        ) {
            Log.d("BootReceiver", "Device boot detected. Rescheduling active alarms...")
            val repo = ScheduleRepository(context)
            val scheduler = AlarmScheduler(context)

            val activeRules = repo.getAllSchedules().filter { it.isEnabled }
            for (rule in activeRules) {
                scheduler.scheduleRule(rule)
            }
            Log.d("BootReceiver", "Rescheduled ${activeRules.size} rules successfully.")
        }
    }
}
