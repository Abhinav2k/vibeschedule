package com.vibeschedule.app.model

import android.media.AudioManager

enum class SoundMode(val ringerMode: Int, val displayName: String) {
    VIBRATE(AudioManager.RINGER_MODE_VIBRATE, "Vibrate"),
    SILENT(AudioManager.RINGER_MODE_SILENT, "Silent (DND)"),
    NORMAL(AudioManager.RINGER_MODE_NORMAL, "Normal (Ring)");

    companion object {
        fun fromRingerMode(mode: Int): SoundMode {
            return entries.firstOrNull { it.ringerMode == mode } ?: NORMAL
        }
    }
}
