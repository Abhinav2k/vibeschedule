package com.vibeschedule.app.ui.components

import android.app.TimePickerDialog
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vibeschedule.app.model.ScheduleRule
import com.vibeschedule.app.model.SoundMode
import com.vibeschedule.app.ui.theme.BricolageGrotesque
import com.vibeschedule.app.ui.util.Haptics
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScheduleDialog(
    initialRule: ScheduleRule?,
    onDismiss: () -> Unit,
    onSave: (ScheduleRule) -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current

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
            false
        ).show()
    }

    fun updateHourAmPm(currentHour: Int, toAm: Boolean): Int {
        return if (toAm) {
            if (currentHour >= 12) currentHour - 12 else currentHour
        } else {
            if (currentHour < 12) currentHour + 12 else currentHour
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(28.dp),
        title = {
            Text(
                text = if (initialRule == null) "New Schedule" else "Edit Schedule",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Schedule Name") },
                    placeholder = { Text("e.g., Deep Work, Lecture, Sleep") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MD3TimeBox(
                        label = "Starts",
                        hour = startHour,
                        minute = startMinute,
                        onTimeClick = {
                            Haptics.click(view)
                            showTimePicker(startHour, startMinute) { h, m ->
                                startHour = h
                                startMinute = m
                            }
                        },
                        onAmPmToggle = { toAm ->
                            Haptics.click(view)
                            startHour = updateHourAmPm(startHour, toAm)
                        },
                        modifier = Modifier.weight(1f)
                    )

                    MD3TimeBox(
                        label = "Ends",
                        hour = endHour,
                        minute = endMinute,
                        onTimeClick = {
                            Haptics.click(view)
                            showTimePicker(endHour, endMinute) { h, m ->
                                endHour = h
                                endMinute = m
                            }
                        },
                        onAmPmToggle = { toAm ->
                            Haptics.click(view)
                            endHour = updateHourAmPm(endHour, toAm)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    text = "Repeat Days",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

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

                    FilterChip(
                        selected = selectedDays.size == 7,
                        onClick = {
                            Haptics.click(view)
                            selectedDays = allDays
                        },
                        label = { Text("Daily", style = MaterialTheme.typography.labelSmall) },
                        shape = CircleShape
                    )
                    FilterChip(
                        selected = selectedDays.size == 5 && selectedDays.containsAll(weekdays),
                        onClick = {
                            Haptics.click(view)
                            selectedDays = weekdays
                        },
                        label = { Text("Weekdays", style = MaterialTheme.typography.labelSmall) },
                        shape = CircleShape
                    )
                    FilterChip(
                        selected = selectedDays.size == 2 && selectedDays.containsAll(weekends),
                        onClick = {
                            Haptics.click(view)
                            selectedDays = weekends
                        },
                        label = { Text("Weekends", style = MaterialTheme.typography.labelSmall) },
                        shape = CircleShape
                    )
                }

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
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.surfaceContainer
                                )
                                .clickable {
                                    Haptics.click(view)
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
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Text(
                    text = "Sound Mode During Schedule",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected = targetMode == SoundMode.VIBRATE,
                        onClick = {
                            Haptics.click(view)
                            targetMode = SoundMode.VIBRATE
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Vibration,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = { Text("Vibrate") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    FilterChip(
                        selected = targetMode == SoundMode.SILENT,
                        onClick = {
                            Haptics.click(view)
                            targetMode = SoundMode.SILENT
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.NotificationsOff,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = { Text("Silent (DND)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    Haptics.confirm(view)
                    val resolvedTitle = title.ifBlank { "Scheduled Quiet" }
                    val newRule = (initialRule ?: ScheduleRule(
                        title = resolvedTitle,
                        startHour = startHour,
                        startMinute = startMinute,
                        endHour = endHour,
                        endMinute = endMinute,
                        daysOfWeek = selectedDays,
                        targetMode = targetMode
                    )).copy(
                        title = resolvedTitle,
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
                shape = CircleShape
            ) {
                Text("Save Schedule", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = {
                Haptics.click(view)
                onDismiss()
            }) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun MD3TimeBox(
    label: String,
    hour: Int,
    minute: Int,
    onTimeClick: () -> Unit,
    onAmPmToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    val isAm = hour < 12
    val timeFormatted = String.format(Locale.getDefault(), "%d:%02d", displayHour, minute)

    OutlinedCard(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = timeFormatted,
                style = MaterialTheme.typography.headlineMedium,
                fontFamily = BricolageGrotesque,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onTimeClick() }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isAm) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent)
                        .clickable { onAmPmToggle(true) }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "AM",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isAm) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (!isAm) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent)
                        .clickable { onAmPmToggle(false) }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "PM",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (!isAm) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
