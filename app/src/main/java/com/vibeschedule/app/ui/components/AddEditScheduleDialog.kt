package com.vibeschedule.app.ui.components

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vibeschedule.app.model.ScheduleRule
import com.vibeschedule.app.model.SoundMode
import com.vibeschedule.app.ui.theme.TextPrimary
import com.vibeschedule.app.ui.theme.TextSecondary
import com.vibeschedule.app.ui.theme.TextTertiary
import java.util.Calendar
import java.util.Locale

@Composable
fun AddEditScheduleDialog(
    initialRule: ScheduleRule?,
    onDismiss: () -> Unit,
    onSave: (ScheduleRule) -> Unit
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf(initialRule?.title ?: "") }
    var startHour by remember { mutableIntStateOf(initialRule?.startHour ?: 9) }
    var startMinute by remember { mutableIntStateOf(initialRule?.startMinute ?: 0) }
    var endHour by remember { mutableIntStateOf(initialRule?.endHour ?: 17) }
    var endMinute by remember { mutableIntStateOf(initialRule?.endMinute ?: 0) }
    var selectedDays by remember {
        mutableStateOf(
            initialRule?.daysOfWeek ?: listOf(
                Calendar.MONDAY,
                Calendar.TUESDAY,
                Calendar.WEDNESDAY,
                Calendar.THURSDAY,
                Calendar.FRIDAY
            )
        )
    }
    var targetMode by remember { mutableStateOf(initialRule?.targetMode ?: SoundMode.VIBRATE) }

    fun showTimePicker(initialH: Int, initialM: Int, onTimeSelected: (Int, Int) -> Unit) {
        TimePickerDialog(
            context,
            { _, h, m -> onTimeSelected(h, m) },
            initialH,
            initialM,
            true
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF141620),
        shape = RoundedCornerShape(26.dp),
        title = {
            Text(
                text = if (initialRule == null) "New Schedule" else "Edit Schedule",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Title Field
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("Label (e.g. Work, Lecture)", color = TextTertiary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color(0x25FFFFFF),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Time Pickers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TimeBox(
                        label = "Starts",
                        time = String.format(Locale.getDefault(), "%02d:%02d", startHour, startMinute),
                        onClick = {
                            showTimePicker(startHour, startMinute) { h, m ->
                                startHour = h
                                startMinute = m
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    TimeBox(
                        label = "Ends",
                        time = String.format(Locale.getDefault(), "%02d:%02d", endHour, endMinute),
                        onClick = {
                            showTimePicker(endHour, endMinute) { h, m ->
                                endHour = h
                                endMinute = m
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Days Selection Header
                Text(
                    text = "Repeat",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )

                // Quick Day Presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val allDays = listOf(
                        Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
                        Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY
                    )
                    val weekdays = listOf(
                        Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
                        Calendar.THURSDAY, Calendar.FRIDAY
                    )
                    val weekends = listOf(Calendar.SATURDAY, Calendar.SUNDAY)

                    PresetChip(text = "Daily", isSelected = selectedDays.size == 7) {
                        selectedDays = allDays
                    }
                    PresetChip(
                        text = "Weekdays",
                        isSelected = selectedDays.size == 5 && selectedDays.containsAll(weekdays)
                    ) {
                        selectedDays = weekdays
                    }
                    PresetChip(
                        text = "Weekends",
                        isSelected = selectedDays.size == 2 && selectedDays.containsAll(weekends)
                    ) {
                        selectedDays = weekends
                    }
                }

                // Individual Day Toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val daysList = listOf(
                        Calendar.MONDAY to "M",
                        Calendar.TUESDAY to "T",
                        Calendar.WEDNESDAY to "W",
                        Calendar.THURSDAY to "T",
                        Calendar.FRIDAY to "F",
                        Calendar.SATURDAY to "S",
                        Calendar.SUNDAY to "S"
                    )

                    daysList.forEach { (dayInt, label) ->
                        val isSelected = selectedDays.contains(dayInt)
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Color.White else Color(0x15FFFFFF))
                                .clickable {
                                    selectedDays = if (isSelected) {
                                        selectedDays - dayInt
                                    } else {
                                        selectedDays + dayInt
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else TextTertiary
                            )
                        }
                    }
                }

                // Sound Mode Selection
                Text(
                    text = "Mode",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = targetMode == SoundMode.VIBRATE,
                        onClick = { targetMode = SoundMode.VIBRATE },
                        label = { Text("Vibrate") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0x30FFFFFF),
                            selectedLabelColor = Color.White
                        ),
                        shape = CircleShape
                    )
                    FilterChip(
                        selected = targetMode == SoundMode.SILENT,
                        onClick = { targetMode = SoundMode.SILENT },
                        label = { Text("Silent (DND)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0x30FFFFFF),
                            selectedLabelColor = Color.White
                        ),
                        shape = CircleShape
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) title = "Scheduled Mode"
                    val newRule = (initialRule ?: ScheduleRule(
                        title = title,
                        startHour = startHour,
                        startMinute = startMinute,
                        endHour = endHour,
                        endMinute = endMinute,
                        daysOfWeek = selectedDays,
                        targetMode = targetMode
                    )).copy(
                        title = title,
                        startHour = startHour,
                        startMinute = startMinute,
                        endHour = endHour,
                        endMinute = endMinute,
                        daysOfWeek = selectedDays,
                        targetMode = targetMode
                    )
                    onSave(newRule)
                },
                enabled = selectedDays.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                shape = CircleShape
            ) {
                Text("Save", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun TimeBox(label: String, time: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Color(0x18FFFFFF)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 11.sp, color = TextTertiary)
            Spacer(modifier = Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(time, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
        }
    }
}

@Composable
private fun PresetChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (isSelected) Color(0x28FFFFFF) else Color(0x12FFFFFF))
            .border(0.5.dp, if (isSelected) Color(0x55FFFFFF) else Color.Transparent, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) Color.White else TextSecondary
        )
    }
}
