package com.vibeschedule.app.model

import java.util.Locale
import java.util.UUID

data class ScheduleRule(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val daysOfWeek: List<Int>, // Calendar.SUNDAY (1) to Calendar.SATURDAY (7)
    val targetMode: SoundMode = SoundMode.VIBRATE,
    val revertMode: SoundMode = SoundMode.NORMAL,
    val isEnabled: Boolean = true
) {
    fun formatStartTime(): String = formatTime(startHour, startMinute)
    fun formatEndTime(): String = formatTime(endHour, endMinute)

    fun daysSummary(): String {
        if (daysOfWeek.size == 7) return "Everyday"
        if (daysOfWeek.size == 5 && !daysOfWeek.contains(1) && !daysOfWeek.contains(7)) return "Weekdays (Mon-Fri)"
        if (daysOfWeek.size == 2 && daysOfWeek.contains(1) && daysOfWeek.contains(7)) return "Weekends (Sat-Sun)"
        
        val dayNames = mapOf(
            1 to "Sun", 2 to "Mon", 3 to "Tue", 4 to "Wed",
            5 to "Thu", 6 to "Fri", 7 to "Sat"
        )
        return daysOfWeek.sorted().mapNotNull { dayNames[it] }.joinToString(", ")
    }

    companion object {
        fun formatTime(hour: Int, minute: Int): String {
            val displayHour = when {
                hour == 0 -> 12
                hour > 12 -> hour - 12
                else -> hour
            }
            val amPm = if (hour < 12) "AM" else "PM"
            return String.format(Locale.getDefault(), "%d:%02d %s", displayHour, minute, amPm)
        }
    }
}
