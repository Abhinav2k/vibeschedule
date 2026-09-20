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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ScheduleRepository(application)
    private val scheduler = AlarmScheduler(application)
    private val audioManager = application.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    val schedules: StateFlow<List<ScheduleRule>> = repository.schedules

    private val _activeSchedule = MutableStateFlow<ScheduleRule?>(null)
    val activeSchedule: StateFlow<ScheduleRule?> = _activeSchedule.asStateFlow()

    private val _upcomingSchedule = MutableStateFlow<Pair<ScheduleRule, Int>?>(null)
    val upcomingSchedule: StateFlow<Pair<ScheduleRule, Int>?> = _upcomingSchedule.asStateFlow()

    private val _isPauseEligible = MutableStateFlow(false)
    val isPauseEligible: StateFlow<Boolean> = _isPauseEligible.asStateFlow()

    private val _pausedUntilMillis = MutableStateFlow<Long?>(null)
    val pausedUntilMillis: StateFlow<Long?> = _pausedUntilMillis.asStateFlow()

    init {
        // Real-time ticker to evaluate active schedule and pause eligibility every 3 seconds
        viewModelScope.launch {
            while (isActive) {
                evaluateStatus()
                delay(3000)
            }
        }
    }

    private fun evaluateStatus() {
        val allSchedules = repository.getAllSchedules().filter { it.isEnabled }
        val now = Calendar.getInstance()
        val currentDay = now.get(Calendar.DAY_OF_WEEK)
        val curMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

        // 1. Check if any rule is currently active
        val active = allSchedules.firstOrNull { rule ->
            if (!rule.daysOfWeek.contains(currentDay)) return@firstOrNull false
            val startMin = rule.startHour * 60 + rule.startMinute
            val endMin = rule.endHour * 60 + rule.endMinute

            if (startMin < endMin) {
                curMinutes in startMin until endMin
            } else {
                curMinutes >= startMin || curMinutes < endMin
            }
        }
        _activeSchedule.value = active

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
                try {
                    audioManager.ringerMode = active.targetMode.ringerMode
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun pauseUntilNextOClock() {
        val now = Calendar.getInstance()
        val nextHour = Calendar.getInstance().apply {
            add(Calendar.HOUR_OF_DAY, 1)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val targetMillis = nextHour.timeInMillis
        _pausedUntilMillis.value = targetMillis

        try {
            audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
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
                audioManager.ringerMode = active.targetMode.ringerMode
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

    fun startQuickMute(minutes: Int) {
        viewModelScope.launch {
            try {
                audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                scheduler.scheduleQuickMute(minutes)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
