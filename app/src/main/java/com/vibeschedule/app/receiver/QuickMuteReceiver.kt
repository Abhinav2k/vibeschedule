package com.vibeschedule.app.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.util.Log

class QuickMuteReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == AlarmReceiver.ACTION_REVERT_NOW) {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            try {
                if (notificationManager.isNotificationPolicyAccessGranted) {
                    notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                }
                audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                notificationManager.cancel(8823) // Dismiss status notification
                Log.d("QuickMuteReceiver", "Reverted ringer mode to NORMAL")
            } catch (e: Exception) {
                Log.e("QuickMuteReceiver", "Error reverting sound mode: ${e.message}")
            }
        }
    }
}
