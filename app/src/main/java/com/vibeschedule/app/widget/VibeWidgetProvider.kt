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
import com.vibeschedule.app.model.SoundMode
import com.vibeschedule.app.scheduler.AlarmScheduler
import com.vibeschedule.app.ui.MainActivity
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
                // Cancel running schedule / quick mute and restore Normal sound
                try {
                    SoundModeHelper.applySoundMode(context, SoundMode.NORMAL, audioManager, notificationManager)
                    notificationManager.cancel(8823) // dismiss status notification
                    Toast.makeText(context, "Schedule Cancelled • Normal Ring", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                updateAll(context)
            }

            ACTION_WIDGET_SKIP_PERIOD -> {
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
                    Toast.makeText(context, "Skipped until next :00", Toast.LENGTH_SHORT).show()
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
