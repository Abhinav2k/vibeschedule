package com.vibeschedule.app.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.vibeschedule.app.R
import com.vibeschedule.app.model.ScheduleRule
import com.vibeschedule.app.model.SoundMode
import com.vibeschedule.app.ui.MainActivity
import com.vibeschedule.app.widget.VibeWidgetProvider
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object NotificationHelper {
    const val CHANNEL_ID = "vibe_schedule_active_status_v4"
    const val OLD_CHANNEL_ID = "vibe_schedule_channel"
    const val NOTIFICATION_ID = 8823

    /**
     * Helper to show active status notification directly for a ScheduleRule
     * computing remaining and total minutes automatically.
     */
    fun showActiveRuleNotification(context: Context, rule: ScheduleRule) {
        val now = Calendar.getInstance()
        val curMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val startMin = rule.startHour * 60 + rule.startMinute
        val endMin = rule.endHour * 60 + rule.endMinute

        val totalMinutes = if (startMin < endMin) endMin - startMin else (endMin + 1440 - startMin)
        val remainingMinutes = if (startMin < endMin) {
            (endMin - curMinutes).coerceAtLeast(1)
        } else {
            ((endMin + 1440 - curMinutes) % 1440).coerceAtLeast(1)
        }

        val endCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, rule.endHour)
            set(Calendar.MINUTE, rule.endMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (startMin >= endMin && curMinutes >= startMin) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        showActiveStatusNotification(
            context = context,
            title = rule.title,
            targetMode = rule.targetMode,
            remainingMinutes = remainingMinutes,
            totalMinutes = totalMinutes,
            endMillis = endCal.timeInMillis,
            canSkip = true
        )
    }

    /**
     * Displays a rich, lock-screen-visible status notification with an active progress bar
     * and interactive [End Now] & [Skip to :00] action buttons visible directly without expanding.
     */
    fun showActiveStatusNotification(
        context: Context,
        title: String,
        targetMode: SoundMode,
        remainingMinutes: Int? = null,
        totalMinutes: Int? = null,
        endMillis: Long? = null,
        canSkip: Boolean = true
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(context, notificationManager)

        // Open app on body tap
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPI = PendingIntent.getActivity(
            context,
            1000,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action 1: End Now / Revert to Normal (Cancel)
        val revertIntent = Intent(context, VibeWidgetProvider::class.java).apply {
            action = VibeWidgetProvider.ACTION_WIDGET_CANCEL_ACTIVE
        }
        val revertPI = PendingIntent.getBroadcast(
            context,
            1001,
            revertIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action 2: Skip to next :00
        val skipIntent = Intent(context, VibeWidgetProvider::class.java).apply {
            action = VibeWidgetProvider.ACTION_WIDGET_SKIP_PERIOD
        }
        val skipPI = PendingIntent.getBroadcast(
            context,
            1002,
            skipIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val endClockStr = if (endMillis != null && endMillis > 0) {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(endMillis))
        } else null

        val timeContent = when {
            remainingMinutes != null && remainingMinutes > 0 -> {
                val hrs = remainingMinutes / 60
                val mins = remainingMinutes % 60
                val remStr = if (hrs > 0) "${hrs}h ${mins}m left" else "${mins}m left"
                if (endClockStr != null) "$remStr • Ends at $endClockStr" else remStr
            }
            endClockStr != null -> "Ends at $endClockStr"
            else -> "Sound Mode: ${targetMode.displayName}"
        }

        val iconRes = when (targetMode) {
            SoundMode.SILENT -> android.R.drawable.ic_lock_silent_mode
            SoundMode.VIBRATE -> android.R.drawable.ic_lock_silent_mode_off
            SoundMode.NORMAL -> android.R.drawable.ic_dialog_info
        }

        // 1. Collapsed View (always shows skip & stop widget buttons directly without expanding!)
        val collapsedView = RemoteViews(context.packageName, R.layout.notification_vibe_collapsed).apply {
            setTextViewText(R.id.notif_title, title)
            setTextViewText(R.id.notif_time_text, timeContent)
            setTextViewText(R.id.notif_mode_badge, targetMode.displayName.uppercase(Locale.getDefault()))

            if (remainingMinutes != null && totalMinutes != null && totalMinutes > 0) {
                val elapsed = (totalMinutes - remainingMinutes).coerceIn(0, totalMinutes)
                setViewVisibility(R.id.notif_progress, View.VISIBLE)
                setProgressBar(R.id.notif_progress, totalMinutes, elapsed, false)
            } else {
                setViewVisibility(R.id.notif_progress, View.GONE)
            }

            if (canSkip) {
                setViewVisibility(R.id.notif_btn_skip, View.VISIBLE)
                setOnClickPendingIntent(R.id.notif_btn_skip, skipPI)
            } else {
                setViewVisibility(R.id.notif_btn_skip, View.GONE)
            }
            setOnClickPendingIntent(R.id.notif_btn_cancel, revertPI)
            setOnClickPendingIntent(R.id.notif_root, openAppPI)
        }

        // 2. Expanded View (shows larger widget-styled cards with text labels)
        val expandedView = RemoteViews(context.packageName, R.layout.notification_vibe_expanded).apply {
            setTextViewText(R.id.notif_expanded_title, title)
            setTextViewText(R.id.notif_expanded_badge, targetMode.displayName.uppercase(Locale.getDefault()))
            setTextViewText(R.id.notif_expanded_time_text, timeContent)

            if (remainingMinutes != null && totalMinutes != null && totalMinutes > 0) {
                val elapsed = (totalMinutes - remainingMinutes).coerceIn(0, totalMinutes)
                setViewVisibility(R.id.notif_expanded_progress, View.VISIBLE)
                setProgressBar(R.id.notif_expanded_progress, totalMinutes, elapsed, false)
            } else {
                setViewVisibility(R.id.notif_expanded_progress, View.GONE)
            }

            if (canSkip) {
                setViewVisibility(R.id.notif_expanded_btn_skip, View.VISIBLE)
                setOnClickPendingIntent(R.id.notif_expanded_btn_skip, skipPI)
            } else {
                setViewVisibility(R.id.notif_expanded_btn_skip, View.GONE)
            }
            setOnClickPendingIntent(R.id.notif_expanded_btn_cancel, revertPI)
            setOnClickPendingIntent(R.id.notif_expanded_root, openAppPI)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(iconRes)
            .setContentTitle(title)
            .setContentText(timeContent)
            .setSubText(targetMode.displayName)
            .setCustomContentView(collapsedView)
            .setCustomBigContentView(expandedView)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setPriority(NotificationCompat.PRIORITY_MAX) // Top priority
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Always visible on lock screen
            .setCategory(NotificationCompat.CATEGORY_ALARM) // Alarm category ensures lockscreen presence
            .setOngoing(true)
            .setShowWhen(false)
            .setSilent(true) // Silent: no audio beeps when progress updates
            .setOnlyAlertOnce(true)
            .setContentIntent(openAppPI)
            .addAction(R.drawable.ic_widget_cancel, "End Now", revertPI)

        if (canSkip) {
            builder.addAction(R.drawable.ic_widget_skip, "Skip to :00", skipPI)
        }

        // Set public version so secure lock screen renders the same interactive layout
        val builtNotification = builder.build()
        builder.setPublicVersion(builtNotification)

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    fun dismissNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }

    private fun createNotificationChannel(context: Context, notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Delete previous lower-importance channels so system replaces them with high-importance
            try {
                notificationManager.deleteNotificationChannel(OLD_CHANNEL_ID)
                notificationManager.deleteNotificationChannel("vibe_schedule_active_status_v2")
                notificationManager.deleteNotificationChannel("vibe_schedule_active_status_v3")
            } catch (e: Exception) {
                // Ignore
            }

            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_HIGH // High importance ensures it shows even when non-important ones are hidden
            ).apply {
                description = context.getString(R.string.channel_description)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                setSound(null, null) // Silent: do not ring
                enableVibration(false) // Silent: do not buzz
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}
