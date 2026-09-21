package com.vibeschedule.app.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Build
import android.os.SystemClock
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.app.NotificationCompat as MediaNotificationCompat
import com.vibeschedule.app.R
import com.vibeschedule.app.model.ScheduleRule
import com.vibeschedule.app.model.SoundMode
import com.vibeschedule.app.ui.MainActivity
import com.vibeschedule.app.ui.screens.getNotifHighPriority
import com.vibeschedule.app.widget.VibeWidgetProvider
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object NotificationHelper {
    const val CHANNEL_ID = "vibe_schedule_active_status_v5"
    const val OLD_CHANNEL_ID = "vibe_schedule_channel"
    const val NOTIFICATION_ID = 8823

    private var mediaSession: MediaSessionCompat? = null

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

        val highPriority = getNotifHighPriority(context)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(iconRes)
            .setContentTitle(title)
            .setContentText(timeContent)
            .setSubText(targetMode.displayName)
            .setOngoing(true)
            .setLocalOnly(true)
            .setShowWhen(false)
            .setSilent(true)
            .setOnlyAlertOnce(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setContentIntent(openAppPI)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        if (highPriority) {
            // Setup MediaSession so Android 16 and OriginOS 6 render this as a top-level Lock Screen Media Player card
            if (mediaSession == null) {
                mediaSession = MediaSessionCompat(context, "VibeScheduleMedia").apply {
                    setCallback(object : MediaSessionCompat.Callback() {
                        override fun onSkipToNext() {
                            try { skipPI.send() } catch (e: Exception) { }
                        }
                        override fun onStop() {
                            try { revertPI.send() } catch (e: Exception) { }
                        }
                        override fun onPause() {
                            try { revertPI.send() } catch (e: Exception) { }
                        }
                    })
                }
            }

            val session = mediaSession!!

            val totalMs = if (totalMinutes != null && totalMinutes > 0) totalMinutes * 60_000L else 3_600_000L
            val remainingMs = if (remainingMinutes != null && remainingMinutes > 0) remainingMinutes * 60_000L else totalMs
            val elapsedMs = (totalMs - remainingMs).coerceIn(0L, totalMs)

            val artBitmap = generateSoundModeArt(context, targetMode)

            val metadata = MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "VibeSchedule • $timeContent")
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, targetMode.displayName)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, totalMs)
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, artBitmap)
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ART, artBitmap)
                .build()
            session.setMetadata(metadata)

            val playbackState = PlaybackStateCompat.Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY_PAUSE or
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                    PlaybackStateCompat.ACTION_STOP or
                    PlaybackStateCompat.ACTION_PAUSE
                )
                .setState(
                    PlaybackStateCompat.STATE_PLAYING,
                    elapsedMs,
                    1.0f,
                    SystemClock.elapsedRealtime()
                )
                .build()
            session.setPlaybackState(playbackState)
            session.isActive = true

            builder.setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_TRANSPORT)

            val mediaStyle = MediaNotificationCompat.MediaStyle()
                .setMediaSession(session.sessionToken)

            if (canSkip) {
                builder.addAction(R.drawable.ic_widget_skip, "Skip :00", skipPI)
                builder.addAction(R.drawable.ic_widget_cancel, "End Now", revertPI)
                mediaStyle.setShowActionsInCompactView(0, 1)
            } else {
                builder.addAction(R.drawable.ic_widget_cancel, "End Now", revertPI)
                mediaStyle.setShowActionsInCompactView(0)
            }

            builder.setStyle(mediaStyle)
        } else {
            // Low/standard priority: release media session and use custom compact view
            try {
                mediaSession?.isActive = false
                mediaSession?.release()
                mediaSession = null
            } catch (e: Exception) { }

            builder.setCustomContentView(collapsedView)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
        }

        notificationManager.notify(NOTIFICATION_ID, builder.build())
        VibeWidgetProvider.updateAll(context)
    }

    fun dismissNotification(context: Context) {
        try {
            mediaSession?.isActive = false
            mediaSession?.release()
            mediaSession = null
        } catch (e: Exception) { }
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
        VibeWidgetProvider.updateAll(context)
    }

    private fun generateSoundModeArt(context: Context, targetMode: SoundMode): Bitmap {
        val size = 256
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Obsidian glass card background
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#141417")
            style = Paint.Style.FILL
        }
        val rect = RectF(0f, 0f, size.toFloat(), size.toFloat())
        canvas.drawRoundRect(rect, 44f, 44f, bgPaint)

        // Glass highlight border
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#323238")
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        canvas.drawRoundRect(rect, 44f, 44f, borderPaint)

        // Draw system sound icon
        val iconRes = when (targetMode) {
            SoundMode.SILENT -> android.R.drawable.ic_lock_silent_mode
            SoundMode.VIBRATE -> android.R.drawable.ic_lock_silent_mode_off
            SoundMode.NORMAL -> android.R.drawable.ic_dialog_info
        }
        val drawable = ContextCompat.getDrawable(context, iconRes)
        if (drawable != null) {
            val iconSize = 110
            val left = (size - iconSize) / 2
            val top = 40
            drawable.setBounds(left, top, left + iconSize, top + iconSize)
            androidx.core.graphics.drawable.DrawableCompat.setTint(
                drawable,
                android.graphics.Color.WHITE
            )
            drawable.draw(canvas)
        }

        // Draw mode badge text
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#EEEEEE")
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(targetMode.displayName.uppercase(Locale.getDefault()), size / 2f, 206f, textPaint)

        return bitmap
    }

    fun createNotificationChannel(context: Context, notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                notificationManager.deleteNotificationChannel(OLD_CHANNEL_ID)
                notificationManager.deleteNotificationChannel("vibe_schedule_active_status_v2")
                notificationManager.deleteNotificationChannel("vibe_schedule_active_status_v3")
                notificationManager.deleteNotificationChannel("vibe_schedule_active_status_v4")
            } catch (e: Exception) { }

            val highPriority = getNotifHighPriority(context)
            val importance = if (highPriority)
                NotificationManager.IMPORTANCE_HIGH
            else
                NotificationManager.IMPORTANCE_DEFAULT

            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.channel_name),
                importance
            ).apply {
                description = context.getString(R.string.channel_description)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                setSound(null, null)
                enableVibration(false)
                setShowBadge(false)
                if (highPriority) {
                    try {
                        setBypassDnd(true)
                    } catch (e: Exception) { }
                }
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}
