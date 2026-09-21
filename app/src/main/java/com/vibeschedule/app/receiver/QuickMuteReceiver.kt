package com.vibeschedule.app.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.util.Log
import com.vibeschedule.app.data.ScheduleRepository
import com.vibeschedule.app.model.SoundMode
import com.vibeschedule.app.util.SoundModeHelper
import com.vibeschedule.app.widget.VibeWidgetProvider

class QuickMuteReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == AlarmReceiver.ACTION_REVERT_NOW) {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            try {
                ScheduleRepository(context).setQuickMuteUntil(null)
                ScheduleRepository(context).setPauseUntil(null)
                SoundModeHelper.applySoundMode(context, SoundMode.NORMAL, audioManager, notificationManager)
                notificationManager.cancel(8823) // Dismiss status notification
                VibeWidgetProvider.updateAll(context)
                Log.d("QuickMuteReceiver", "Reverted ringer mode to NORMAL and restored volume")
            } catch (e: Exception) {
                Log.e("QuickMuteReceiver", "Error reverting sound mode: ${e.message}")
            }
        }
    }
}
