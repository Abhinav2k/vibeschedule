package com.vibeschedule.app.ui

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vibeschedule.app.data.ScheduleRepository
import com.vibeschedule.app.model.ScheduleRule
import com.vibeschedule.app.model.SoundMode
import com.vibeschedule.app.scheduler.AlarmScheduler
import com.vibeschedule.app.util.NotificationHelper
import com.vibeschedule.app.util.SoundModeHelper
import com.vibeschedule.app.widget.VibeWidgetProvider
import com.vibeschedule.app.widget.WidgetRefresher
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

    private val _hasDismissedSchedule = MutableStateFlow(false)
    val hasDismissedSchedule: StateFlow<Boolean> = _hasDismissedSchedule.asStateFlow()

    private val _quickMuteUntilMillis = MutableStateFlow<Long?>(null)
    val quickMuteUntilMillis: StateFlow<Long?> = _quickMuteUntilMillis.asStateFlow()

    private val _quickMuteRemainingSeconds = MutableStateFlow<Int>(0)
    val quickMuteRemainingSeconds: StateFlow<Int> = _quickMuteRemainingSeconds.asStateFlow()

    private val _quickMuteConflictInfo = MutableStateFlow<QuickMuteConflict?>(null)
    val quickMuteConflictInfo: StateFlow<QuickMuteConflict?> = _quickMuteConflictInfo.asStateFlow()

    private var lastNotificationMinute: Int = -1

    private val syncReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            evaluateStatus()
        }
    }

    init {
        // Restore persistent quick mute and pause states
        val persistedQm = repository.getQuickMuteUntil()
        if (persistedQm != null && persistedQm > System.currentTimeMillis()) {
            _quickMuteUntilMillis.value = persistedQm
            _quickMuteRemainingSeconds.value = ((persistedQm - System.currentTimeMillis()) / 1000L).toInt()
        }
        val persistedPause = repository.getPauseUntil()
        if (persistedPause != null && persistedPause > System.currentTimeMillis()) {
            _pausedUntilMillis.value = persistedPause
        }

        // Register for immediate state updates from widgets and notifications
        try {
            val filter = IntentFilter(VibeWidgetProvider.ACTION_SYNC_APP_STATE)
            ContextCompat.registerReceiver(
                application,
                syncReceiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Collect global repository events immediately
        viewModelScope.launch {
            ScheduleRepository.globalStateEvents.collect {
                evaluateStatus()
            }
        }

        // Real-time ticker to evaluate status and count down every second
        viewModelScope.launch {
            while (isActive) {
                evaluateStatus()
                delay(1000)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            getApplication<Application>().unregisterReceiver(syncReceiver)
        } catch (e: Exception) { }
    }

    fun evaluateStatus() {
        val allSchedules = repository.getAllSchedules().filter { it.isEnabled }
        val now = Calendar.getInstance()
        val currentDay = now.get(Calendar.DAY_OF_WEEK)
        val curMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val yesterdayDay = yesterdayCal.get(Calendar.DAY_OF_WEEK)

        // Sync repository Quick Mute state immediately
        val qmUntil = repository.getQuickMuteUntil()
        _quickMuteUntilMillis.value = qmUntil
        if (qmUntil != null) {
            val diffMillis = qmUntil - System.currentTimeMillis()
            if (diffMillis <= 0) {
                _quickMuteUntilMillis.value = null
                _quickMuteRemainingSeconds.value = 0
                repository.setQuickMuteUntil(null)
                WidgetRefresher.refresh(getApplication())
            } else {
                _quickMuteRemainingSeconds.value = (diffMillis / 1000L).toInt()
            }
        } else {
            _quickMuteRemainingSeconds.value = 0
        }

        // Sync repository Pause state immediately
        val pausedUntil = repository.getPauseUntil()
        if (pausedUntil != null && System.currentTimeMillis() >= pausedUntil) {
            _pausedUntilMillis.value = null
            repository.setPauseUntil(null)
            WidgetRefresher.refresh(getApplication())
        } else {
            _pausedUntilMillis.value = pausedUntil
        }

        // Check if active schedule was dismissed by user
        val dismissedUntil = repository.getDismissedActiveUntil()
        val isGlobalDismissed = dismissedUntil != null && System.currentTimeMillis() < dismissedUntil
        _hasDismissedSchedule.value = isGlobalDismissed

        // 1. Check active schedule and calculate remaining minutes
        val active = if (isGlobalDismissed) {
            null
        } else {
            allSchedules.firstOrNull { rule ->
                if (rule.daysOfWeek.isEmpty()) return@firstOrNull false
                if (repository.isScheduleDismissed(rule.id)) return@firstOrNull false
                val startMin = rule.startHour * 60 + rule.startMinute
                val endMin = rule.endHour * 60 + rule.endMinute

                if (startMin < endMin) {
                    rule.daysOfWeek.contains(currentDay) && curMinutes in startMin until endMin
                } else {
                    (rule.daysOfWeek.contains(currentDay) && curMinutes >= startMin) ||
                    (rule.daysOfWeek.contains(yesterdayDay) && curMinutes < endMin)
                }
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

        // 4. Keep notification progress in sync while active
        if (_pausedUntilMillis.value == null && _quickMuteUntilMillis.value == null && active != null) {
            if (curMinutes != lastNotificationMinute) {
                lastNotificationMinute = curMinutes
                NotificationHelper.showActiveRuleNotification(getApplication(), active)
            }
        } else if (_quickMuteUntilMillis.value == null && _pausedUntilMillis.value == null && active == null) {
            lastNotificationMinute = -1
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
        repository.setQuickMuteUntil(endMillis)

        try {
            SoundModeHelper.applySoundMode(getApplication(), SoundMode.VIBRATE, audioManager)
            scheduler.scheduleQuickMute(minutes)
            NotificationHelper.showActiveStatusNotification(
                context = getApplication(),
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
        WidgetRefresher.refresh(getApplication())
    }

    fun cancelQuickMute() {
        _quickMuteUntilMillis.value = null
        _quickMuteRemainingSeconds.value = 0
        repository.setQuickMuteUntil(null)
        scheduler.cancelQuickMute()

        val active = _activeSchedule.value
        try {
            if (active != null) {
                SoundModeHelper.applySoundMode(getApplication(), active.targetMode, audioManager)
                NotificationHelper.showActiveRuleNotification(getApplication(), active)
            } else {
                SoundModeHelper.applySoundMode(getApplication(), SoundMode.NORMAL, audioManager)
                NotificationHelper.dismissNotification(getApplication())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        WidgetRefresher.refresh(getApplication())
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
        repository.setPauseUntil(targetMillis)
        val active = _activeSchedule.value
        val label = active?.title ?: "Schedule"

        try {
            SoundModeHelper.applySoundMode(getApplication(), SoundMode.NORMAL, audioManager)
            scheduler.schedulePauseEnd(targetMillis)
            NotificationHelper.showActiveStatusNotification(
                context = getApplication(),
                title = "$label Skipped",
                targetMode = SoundMode.NORMAL,
                endMillis = targetMillis,
                canSkip = false
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        WidgetRefresher.refresh(getApplication())
    }

    fun skipPeriod() {
        pauseUntilNextOClock()
    }

    fun restoreSchedule() {
        repository.clearDismissedActive()
        repository.clearAllDismissals()
        repository.setPauseUntil(null)
        _pausedUntilMillis.value = null
        scheduler.cancelPauseEnd()
        evaluateStatus()
        val active = _activeSchedule.value
        if (active != null) {
            try {
                SoundModeHelper.applySoundMode(getApplication(), active.targetMode, audioManager)
                NotificationHelper.showActiveRuleNotification(getApplication(), active)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        WidgetRefresher.refresh(getApplication())
    }

    fun cancelPause() {
        restoreSchedule()
    }

    fun stopActiveSchedule() {
        val active = _activeSchedule.value
        val endMillis = if (active != null) {
            ScheduleRule.calculateRuleEndMillis(active)
        } else {
            System.currentTimeMillis() + 3600000L
        }
        repository.setDismissedActiveUntil(endMillis)
        if (active != null) {
            repository.dismissScheduleUntil(active.id, endMillis)
        }
        repository.setQuickMuteUntil(null)
        repository.setPauseUntil(null)
        scheduler.cancelPauseEnd()
        scheduler.cancelQuickMute()
        SoundModeHelper.applySoundMode(getApplication(), SoundMode.NORMAL, audioManager)
        NotificationHelper.dismissNotification(getApplication())
        WidgetRefresher.refresh(getApplication())
        evaluateStatus()
    }

    fun addSchedule(rule: ScheduleRule) {
        viewModelScope.launch {
            repository.clearDismissedActive()
            repository.clearScheduleDismissal(rule.id)
            repository.addSchedule(rule)
            if (rule.isEnabled) {
                scheduler.scheduleRule(rule)
            }
            evaluateStatus()
        }
    }

    fun updateSchedule(rule: ScheduleRule) {
        viewModelScope.launch {
            repository.clearDismissedActive()
            repository.clearScheduleDismissal(rule.id)
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
            if (!isEnabled && _activeSchedule.value?.id == ruleId) {
                NotificationHelper.dismissNotification(getApplication())
            }
            if (isEnabled) {
                repository.clearDismissedActive()
                repository.clearScheduleDismissal(ruleId)
            }
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
            if (_activeSchedule.value?.id == ruleId) {
                NotificationHelper.dismissNotification(getApplication())
            }
            val rule = repository.getScheduleById(ruleId)
            if (rule != null) {
                scheduler.cancelRule(rule)
            }
            repository.deleteSchedule(ruleId)
            evaluateStatus()
        }
    }
}
