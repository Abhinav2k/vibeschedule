package com.vibeschedule.app.ui

import android.app.Application
import android.content.Context
import android.media.AudioManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vibeschedule.app.data.ScheduleRepository
import com.vibeschedule.app.model.ScheduleRule
import com.vibeschedule.app.model.SoundMode
import com.vibeschedule.app.scheduler.AlarmScheduler
import com.vibeschedule.app.util.SoundModeHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar

data class QuickMuteConflict(
    val title: String,
    val message: String,
    val pendingMinutes: Int
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ScheduleRepository(application)
    private val scheduler = AlarmScheduler(application)
    private val audioManager = application.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    val schedules: StateFlow<List<ScheduleRule>> = repository.schedules

    private val _activeSchedule = MutableStateFlow<ScheduleRule?>(null)
    val activeSchedule: StateFlow<ScheduleRule?> = _activeSchedule.asStateFlow()

    private val _activeRemainingMinutes = MutableStateFlow<Int?>(null)
    val activeRemainingMinutes: StateFlow<Int?> = _activeRemainingMinutes.asStateFlow()

    private val _upcomingSchedule = MutableStateFlow<Pair<ScheduleRule, Int>?>(null)
    val upcomingSchedule: StateFlow<Pair<ScheduleRule, Int>?> = _upcomingSchedule.asStateFlow()

    private val _isPauseEligible = MutableStateFlow(false)
    val isPauseEligible: StateFlow<Boolean> = _isPauseEligible.asStateFlow()

    private val _pausedUntilMillis = MutableStateFlow<Long?>(null)
    val pausedUntilMillis: StateFlow<Long?> = _pausedUntilMillis.asStateFlow()

    private val _quickMuteUntilMillis = MutableStateFlow<Long?>(null)
    val quickMuteUntilMillis: StateFlow<Long?> = _quickMuteUntilMillis.asStateFlow()

    private val _quickMuteRemainingSeconds = MutableStateFlow<Int>(0)
    val quickMuteRemainingSeconds: StateFlow<Int> = _quickMuteRemainingSeconds.asStateFlow()

    private val _quickMuteConflictInfo = MutableStateFlow<QuickMuteConflict?>(null)
    val quickMuteConflictInfo: StateFlow<QuickMuteConflict?> = _quickMuteConflictInfo.asStateFlow()

    init {
        // Real-time ticker to evaluate status every 2 seconds
        viewModelScope.launch {
            while (isActive) {
                evaluateStatus()
                delay(2000)
            }
        }
    }

    private fun evaluateStatus() {
        val allSchedules = repository.getAllSchedules().filter { it.isEnabled }
        val now = Calendar.getInstance()
        val currentDay = now.get(Calendar.DAY_OF_WEEK)
        val curMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val yesterdayDay = yesterdayCal.get(Calendar.DAY_OF_WEEK)

        // 1. Check active schedule and calculate remaining minutes
        val active = allSchedules.firstOrNull { rule ->
            if (rule.daysOfWeek.isEmpty()) return@firstOrNull false
            val startMin = rule.startHour * 60 + rule.startMinute
            val endMin = rule.endHour * 60 + rule.endMinute

            if (startMin < endMin) {
                rule.daysOfWeek.contains(currentDay) && curMinutes in startMin until endMin
            } else {
                (rule.daysOfWeek.contains(currentDay) && curMinutes >= startMin) ||
                (rule.daysOfWeek.contains(yesterdayDay) && curMinutes < endMin)
            }
        }
        _activeSchedule.value = active

        if (active != null) {
            val startMin = active.startHour * 60 + active.startMinute
            val endMin = active.endHour * 60 + active.endMinute
            val remaining = if (startMin < endMin) {
                (endMin - curMinutes).coerceAtLeast(1)
            } else {
                val rem = (endMin + 1440 - curMinutes) % 1440
                rem.coerceAtLeast(1)
            }
            _activeRemainingMinutes.value = remaining
        } else {
            _activeRemainingMinutes.value = null
        }

        // 2. Check for upcoming schedule starting today
        var nearestUpcoming: Pair<ScheduleRule, Int>? = null
        for (rule in allSchedules) {
            if (!rule.daysOfWeek.contains(currentDay)) continue
            val startMin = rule.startHour * 60 + rule.startMinute
            val diff = startMin - curMinutes
            if (diff > 0) {
                if (nearestUpcoming == null || diff < nearestUpcoming.second) {
                    nearestUpcoming = Pair(rule, diff)
                }
            }
        }
        _upcomingSchedule.value = nearestUpcoming

        // 3. Pause eligibility: Active right now OR starting within 20 minutes
        val isStartingSoon = (nearestUpcoming != null && nearestUpcoming.second <= 20)
        _isPauseEligible.value = (active != null || isStartingSoon)

        // 4. Check if pause has expired
        val pausedUntil = _pausedUntilMillis.value
        if (pausedUntil != null && System.currentTimeMillis() >= pausedUntil) {
            _pausedUntilMillis.value = null
            if (active != null) {
                SoundModeHelper.applySoundMode(getApplication(), active.targetMode, audioManager)
            }
        }

        // 5. Check Quick Mute countdown
        val qmUntil = _quickMuteUntilMillis.value
        if (qmUntil != null) {
            val diffMillis = qmUntil - System.currentTimeMillis()
            if (diffMillis <= 0) {
                _quickMuteUntilMillis.value = null
                _quickMuteRemainingSeconds.value = 0
                // Restore sound mode
                if (active != null) {
                    SoundModeHelper.applySoundMode(getApplication(), active.targetMode, audioManager)
                } else {
                    SoundModeHelper.applySoundMode(getApplication(), SoundMode.NORMAL, audioManager)
                }
            } else {
                _quickMuteRemainingSeconds.value = (diffMillis / 1000L).toInt()
            }
        } else {
            _quickMuteRemainingSeconds.value = 0
        }
    }

    fun requestQuickMute(minutes: Int) {
        val active = _activeSchedule.value
        val isAlreadyVibrating = audioManager.ringerMode == AudioManager.RINGER_MODE_VIBRATE || audioManager.ringerMode == AudioManager.RINGER_MODE_SILENT

        if (active != null) {
            val remMin = _activeRemainingMinutes.value ?: 0
            val remStr = if (remMin >= 60) "${remMin / 60}h ${remMin % 60}m" else "${remMin}m"
            _quickMuteConflictInfo.value = QuickMuteConflict(
                title = "Schedule Already Active",
                message = "'${active.title}' is already active with $remStr remaining (ends at ${active.formatEndTime()}).",
                pendingMinutes = minutes
            )
        } else if (isAlreadyVibrating) {
            val currentModeName = if (audioManager.ringerMode == AudioManager.RINGER_MODE_VIBRATE) "Vibrate" else "Silent"
            _quickMuteConflictInfo.value = QuickMuteConflict(
                title = "Phone Already in $currentModeName",
                message = "Your phone is already in $currentModeName mode. Quick Mute will set a timer to restore ring in $minutes minutes.",
                pendingMinutes = minutes
            )
        } else {
            applyQuickMute(minutes)
        }
    }

    fun confirmQuickMuteOverride() {
        val pending = _quickMuteConflictInfo.value?.pendingMinutes ?: return
        _quickMuteConflictInfo.value = null
        applyQuickMute(pending)
    }

    fun dismissQuickMuteConflict() {
        _quickMuteConflictInfo.value = null
    }

    private fun applyQuickMute(minutes: Int) {
        val endMillis = System.currentTimeMillis() + (minutes * 60 * 1000L)
        _quickMuteUntilMillis.value = endMillis
        _quickMuteRemainingSeconds.value = minutes * 60

        try {
            SoundModeHelper.applySoundMode(getApplication(), SoundMode.VIBRATE, audioManager)
            scheduler.scheduleQuickMute(minutes)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelQuickMute() {
        _quickMuteUntilMillis.value = null
        _quickMuteRemainingSeconds.value = 0
        scheduler.cancelQuickMute()

        val notificationManager = getApplication<Application>().getSystemService(Context.NOTIFICATION_SERVICE) as? android.app.NotificationManager
        notificationManager?.cancel(8823)

        val active = _activeSchedule.value
        try {
            if (active != null) {
                SoundModeHelper.applySoundMode(getApplication(), active.targetMode, audioManager)
            } else {
                SoundModeHelper.applySoundMode(getApplication(), SoundMode.NORMAL, audioManager)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun pauseUntilNextOClock() {
        val nextHour = Calendar.getInstance().apply {
            add(Calendar.HOUR_OF_DAY, 1)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val targetMillis = nextHour.timeInMillis
        _pausedUntilMillis.value = targetMillis

        try {
            SoundModeHelper.applySoundMode(getApplication(), SoundMode.NORMAL, audioManager)
            scheduler.schedulePauseEnd(targetMillis)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelPause() {
        _pausedUntilMillis.value = null
        scheduler.cancelPauseEnd()

        val active = _activeSchedule.value
        if (active != null) {
            try {
                SoundModeHelper.applySoundMode(getApplication(), active.targetMode, audioManager)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addSchedule(rule: ScheduleRule) {
        viewModelScope.launch {
            repository.addSchedule(rule)
            if (rule.isEnabled) {
                scheduler.scheduleRule(rule)
            }
            evaluateStatus()
        }
    }

    fun updateSchedule(rule: ScheduleRule) {
        viewModelScope.launch {
            repository.updateSchedule(rule)
            if (rule.isEnabled) {
                scheduler.scheduleRule(rule)
            } else {
                scheduler.cancelRule(rule)
            }
            evaluateStatus()
        }
    }

    fun toggleSchedule(ruleId: String, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.toggleSchedule(ruleId, isEnabled)
            val updated = repository.getScheduleById(ruleId)
            if (updated != null) {
                if (isEnabled) {
                    scheduler.scheduleRule(updated)
                } else {
                    scheduler.cancelRule(updated)
                }
            }
            evaluateStatus()
        }
    }

    fun deleteSchedule(ruleId: String) {
        viewModelScope.launch {
            val rule = repository.getScheduleById(ruleId)
            if (rule != null) {
                scheduler.cancelRule(rule)
            }
            repository.deleteSchedule(ruleId)
            evaluateStatus()
        }
    }
}
