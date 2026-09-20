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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ScheduleRepository(application)
    private val scheduler = AlarmScheduler(application)
    private val audioManager = application.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    val schedules: StateFlow<List<ScheduleRule>> = repository.schedules

    fun addSchedule(rule: ScheduleRule) {
        viewModelScope.launch {
            repository.addSchedule(rule)
            if (rule.isEnabled) {
                scheduler.scheduleRule(rule)
            }
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
        }
    }

    fun deleteSchedule(ruleId: String) {
        viewModelScope.launch {
            val rule = repository.getScheduleById(ruleId)
            if (rule != null) {
                scheduler.cancelRule(rule)
            }
            repository.deleteSchedule(ruleId)
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

    fun testSetMode(mode: SoundMode) {
        try {
            audioManager.ringerMode = mode.ringerMode
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
