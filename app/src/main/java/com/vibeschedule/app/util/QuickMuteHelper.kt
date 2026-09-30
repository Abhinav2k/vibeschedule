package com.vibeschedule.app.util

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import com.vibeschedule.app.data.ScheduleRepository
import com.vibeschedule.app.model.SoundMode
import com.vibeschedule.app.scheduler.AlarmScheduler
import com.vibeschedule.app.widget.VibeWidgetProvider

object QuickMuteHelper {

    fun startQuickMute(context: Context, minutes: Int) {
        val endMillis = System.currentTimeMillis() + (minutes * 60 * 1000L)
        val repo = ScheduleRepository(context)
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val scheduler = AlarmScheduler(context)

        repo.setQuickMuteUntil(endMillis)

        try {
            SoundModeHelper.applySoundMode(context, SoundMode.VIBRATE, audioManager, notificationManager)
            scheduler.scheduleQuickMute(minutes)
            NotificationHelper.showActiveStatusNotification(
                context = context,
                title = "Quick Mute",
                targetMode = SoundMode.VIBRATE,
                remainingMinutes = minutes,
                totalMinutes = minutes,
                endMillis = endMillis,
                canSkip = false
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        ScheduleRepository.notifyStateChanged()
        VibeWidgetProvider.updateAll(context)

        val syncIntent = Intent(VibeWidgetProvider.ACTION_SYNC_APP_STATE).apply {
            setPackage(context.packageName)
        }
        context.sendBroadcast(syncIntent)
    }
}
