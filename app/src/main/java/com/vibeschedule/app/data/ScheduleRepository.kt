package com.vibeschedule.app.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vibeschedule.app.model.ScheduleRule
import com.vibeschedule.app.model.SoundMode
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar

class ScheduleRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("vibe_schedules_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _schedules = MutableStateFlow<List<ScheduleRule>>(emptyList())
    val schedules: StateFlow<List<ScheduleRule>> = _schedules.asStateFlow()

    private val prefListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == "schedules_list") {
            loadSchedules()
        }
        _globalStateEvents.tryEmit(Unit)
    }

    init {
        loadSchedules()
        prefs.registerOnSharedPreferenceChangeListener(prefListener)
    }

    private fun loadSchedules() {
        val json = prefs.getString("schedules_list", null)
        if (json.isNullOrEmpty()) {
            // Seed a helpful sample rule if empty
            val defaultList = listOf(
                ScheduleRule(
                    title = "Work / College",
                    startHour = 9,
                    startMinute = 0,
                    endHour = 17,
                    endMinute = 0,
                    daysOfWeek = listOf(
                        Calendar.MONDAY,
                        Calendar.TUESDAY,
                        Calendar.WEDNESDAY,
                        Calendar.THURSDAY,
                        Calendar.FRIDAY
                    ),
                    targetMode = SoundMode.VIBRATE,
                    revertMode = SoundMode.NORMAL,
                    isEnabled = true
                )
            )
            saveSchedules(defaultList)
        } else {
            val type = object : TypeToken<List<ScheduleRule>>() {}.type
            val list: List<ScheduleRule> = try {
                gson.fromJson(json, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
            _schedules.value = list
        }
    }

    fun getAllSchedules(): List<ScheduleRule> = _schedules.value

    fun getScheduleById(id: String): ScheduleRule? {
        return _schedules.value.firstOrNull { it.id == id }
    }

    fun addSchedule(rule: ScheduleRule) {
        val updated = _schedules.value.toMutableList().apply {
            add(rule)
        }
        saveSchedules(updated)
    }

    fun updateSchedule(rule: ScheduleRule) {
        val updated = _schedules.value.map {
            if (it.id == rule.id) rule else it
        }
        saveSchedules(updated)
    }

    fun toggleSchedule(id: String, isEnabled: Boolean) {
        val updated = _schedules.value.map {
            if (it.id == id) it.copy(isEnabled = isEnabled) else it
        }
        saveSchedules(updated)
    }

    fun deleteSchedule(id: String) {
        val updated = _schedules.value.filter { it.id != id }
        saveSchedules(updated)
    }

    fun setQuickMuteUntil(millis: Long?) {
        if (millis == null) {
            prefs.edit().remove("quick_mute_until").apply()
        } else {
            prefs.edit().putLong("quick_mute_until", millis).apply()
        }
    }

    fun getQuickMuteUntil(): Long? {
        val v = prefs.getLong("quick_mute_until", 0L)
        return if (v > System.currentTimeMillis()) v else null
    }

    fun setPauseUntil(millis: Long?) {
        if (millis == null) {
            prefs.edit().remove("pause_until").apply()
        } else {
            prefs.edit().putLong("pause_until", millis).apply()
        }
    }

    fun getPauseUntil(): Long? {
        val v = prefs.getLong("pause_until", 0L)
        return if (v > System.currentTimeMillis()) v else null
    }

    fun dismissScheduleUntil(ruleId: String, untilMillis: Long) {
        prefs.edit().putLong("dismissed_rule_$ruleId", untilMillis).apply()
        _globalStateEvents.tryEmit(Unit)
    }

    fun isScheduleDismissed(ruleId: String): Boolean {
        val v = prefs.getLong("dismissed_rule_$ruleId", 0L)
        return v > System.currentTimeMillis()
    }

    fun clearScheduleDismissal(ruleId: String) {
        prefs.edit().remove("dismissed_rule_$ruleId").apply()
        _globalStateEvents.tryEmit(Unit)
    }

    fun setDismissedActiveUntil(millis: Long?) {
        if (millis == null) {
            prefs.edit().remove("dismissed_active_until").apply()
        } else {
            prefs.edit().putLong("dismissed_active_until", millis).apply()
        }
        _globalStateEvents.tryEmit(Unit)
    }

    fun getDismissedActiveUntil(): Long? {
        val v = prefs.getLong("dismissed_active_until", 0L)
        return if (v > System.currentTimeMillis()) v else null
    }

    fun clearDismissedActive() {
        prefs.edit().remove("dismissed_active_until").apply()
        _globalStateEvents.tryEmit(Unit)
    }

    fun clearAllDismissals() {
        val editor = prefs.edit().remove("dismissed_active_until")
        for (rule in _schedules.value) {
            editor.remove("dismissed_rule_${rule.id}")
        }
        editor.apply()
        _globalStateEvents.tryEmit(Unit)
    }

    private fun saveSchedules(list: List<ScheduleRule>) {
        _schedules.value = list
        val json = gson.toJson(list)
        prefs.edit().putString("schedules_list", json).apply()
        _globalStateEvents.tryEmit(Unit)
    }

    companion object {
        private val _globalStateEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 10)
        val globalStateEvents: SharedFlow<Unit> = _globalStateEvents.asSharedFlow()

        fun notifyStateChanged() {
            _globalStateEvents.tryEmit(Unit)
        }
    }
}
