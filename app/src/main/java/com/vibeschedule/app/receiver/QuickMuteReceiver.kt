package com.vibeschedule.app.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.util.Log
import com.vibeschedule.app.data.ScheduleRepository
import com.vibeschedule.app.model.SoundMode
import com.vibeschedule.app.util.NotificationHelper
import com.vibeschedule.app.util.SoundModeHelper
import com.vibeschedule.app.widget.VibeWidgetProvider

class QuickMuteReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == AlarmReceiver.ACTION_REVERT_NOW) {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            try {
                val repo = ScheduleRepository(context)
                repo.setQuickMuteUntil(null)
                repo.setPauseUntil(null)
                SoundModeHelper.applySoundMode(context, SoundMode.NORMAL, audioManager, notificationManager)
                NotificationHelper.dismissNotification(context)
                notificationManager.cancel(8823) // Dismiss status notification
                VibeWidgetProvider.updateAll(context)
                ScheduleRepository.notifyStateChanged()
                val syncIntent = Intent(VibeWidgetProvider.ACTION_SYNC_APP_STATE).apply {
                    setPackage(context.packageName)
                }
                context.sendBroadcast(syncIntent)
                Log.d("QuickMuteReceiver", "Reverted ringer mode to NORMAL and restored volume")
            } catch (e: Exception) {
                Log.e("QuickMuteReceiver", "Error reverting sound mode: ${e.message}")
            }
        }
    }
}
