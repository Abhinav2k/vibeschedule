package com.vibeschedule.app.util

import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import android.util.Log
import com.vibeschedule.app.model.SoundMode

object SoundModeHelper {
    private const val PREFS_NAME = "vibe_sound_prefs"
    private const val KEY_SAVED_RING_VOLUME = "saved_ring_volume"

    fun applySoundMode(
        context: Context,
        mode: SoundMode,
        audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager,
        notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    ) {
        try {
            when (mode) {
                SoundMode.VIBRATE -> {
                    val curVol = audioManager.getStreamVolume(AudioManager.STREAM_RING)
                    if (curVol > 0) {
                        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                            .edit().putInt(KEY_SAVED_RING_VOLUME, curVol).apply()
                    }
                    if (notificationManager.isNotificationPolicyAccessGranted) {
                        notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                    }
                    audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                    Log.d("SoundModeHelper", "Switched to VIBRATE (saved vol=$curVol)")
                }
                SoundMode.SILENT -> {
                    val curVol = audioManager.getStreamVolume(AudioManager.STREAM_RING)
                    if (curVol > 0) {
                        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                            .edit().putInt(KEY_SAVED_RING_VOLUME, curVol).apply()
                    }
                    if (notificationManager.isNotificationPolicyAccessGranted) {
                        notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
                    }
                    audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
                    Log.d("SoundModeHelper", "Switched to SILENT (saved vol=$curVol)")
                }
                SoundMode.NORMAL -> {
                    if (notificationManager.isNotificationPolicyAccessGranted) {
                        notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                    }
                    audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL

                    // Ensure ring stream has non-zero volume so the phone actually rings
                    val curVol = audioManager.getStreamVolume(AudioManager.STREAM_RING)
                    if (curVol == 0) {
                        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                        val savedVol = prefs.getInt(KEY_SAVED_RING_VOLUME, -1)
                        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING)
                        val targetVol = if (savedVol > 0) savedVol else (maxVol * 0.7).toInt().coerceAtLeast(1)
                        audioManager.setStreamVolume(AudioManager.STREAM_RING, targetVol, 0)
                        Log.d("SoundModeHelper", "Switched to NORMAL and restored ring volume to $targetVol (max was $maxVol)")
                    } else {
                        Log.d("SoundModeHelper", "Switched to NORMAL (current ring vol=$curVol)")
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("SoundModeHelper", "Permission denied toggling sound mode: ${e.message}")
        } catch (e: Exception) {
            Log.e("SoundModeHelper", "Error applying sound mode ${mode.name}: ${e.message}")
        }
    }
}
