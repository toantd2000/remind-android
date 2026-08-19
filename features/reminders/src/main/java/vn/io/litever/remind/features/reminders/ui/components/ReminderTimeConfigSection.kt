package vn.io.litever.remind.features.reminders.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import vn.io.litever.designsystem.components.LiteverIconButton
import vn.io.litever.designsystem.components.LiteverOutlinedCard
import vn.io.litever.designsystem.components.LiteverTimePickerDialog
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.common.util.TimeFormatUtils
import vn.io.litever.remind.core.model.DayOfWeek
import vn.io.litever.remind.features.reminders.R
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReminderTimeConfigSection(
    time: LocalTime,
    onTimeChange: (LocalTime) -> Unit,
    date: LocalDate?,
    onDateChange: (LocalDate?) -> Unit,
    repeatDays: List<DayOfWeek>,
    onRepeatDaysChange: (List<DayOfWeek>) -> Unit,
    is24HourFormat: Boolean,
    modifier: Modifier = Modifier
) {
    var showTimePicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    LiteverOutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.section_time_trigger),
                style = LiteverTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LiteverTheme.colors.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Time Selector Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(LiteverTheme.shapes.medium)
                    .clickable { showTimePicker = true },
                color = LiteverTheme.colors.surfaceVariant.copy(alpha = 0.5f),
                shape = LiteverTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.AccessTime,
                            contentDescription = null,
                            tint = LiteverTheme.colors.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.select_time),
                            style = LiteverTheme.typography.bodyMedium,
                            color = LiteverTheme.colors.onSurface
                        )
                    }
                    Text(
                        text = TimeFormatUtils.formatTime(time, is24HourFormat),
                        style = LiteverTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = LiteverTheme.colors.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Date Selector (Optional)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(LiteverTheme.shapes.medium)
                    .clickable { showDatePicker = true },
                color = LiteverTheme.colors.surfaceVariant.copy(alpha = 0.5f),
                shape = LiteverTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.CalendarMonth,
                            contentDescription = null,
                            tint = LiteverTheme.colors.secondary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (date != null) {
                                date.format(DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy"))
                            } else {
                                stringResource(R.string.select_date)
                            },
                            style = LiteverTheme.typography.bodyMedium,
                            color = if (date != null) LiteverTheme.colors.onSurface else LiteverTheme.colors.onSurfaceVariant
                        )
                    }
                    if (date != null) {
                        LiteverIconButton(
                            onClick = { onDateChange(null) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Clear,
                                contentDescription = "Clear Date",
                                tint = LiteverTheme.colors.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Repeat Days Chips
            Text(
                text = stringResource(R.string.repeat_days_label),
                style = LiteverTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = LiteverTheme.colors.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                DayOfWeek.entries.forEach { day ->
                    val isSelected = repeatDays.contains(day)
                    val labelRes = when (day) {
                        DayOfWeek.MONDAY -> R.string.day_mon
                        DayOfWeek.TUESDAY -> R.string.day_tue
                        DayOfWeek.WEDNESDAY -> R.string.day_wed
                        DayOfWeek.THURSDAY -> R.string.day_thu
                        DayOfWeek.FRIDAY -> R.string.day_fri
                        DayOfWeek.SATURDAY -> R.string.day_sat
                        DayOfWeek.SUNDAY -> R.string.day_sun
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            val newRepeat = if (isSelected) {
                                repeatDays - day
                            } else {
                                (repeatDays + day).sortedBy { it.toJavaDayValue() }
                            }
                            onRepeatDaysChange(newRepeat)
                            if (newRepeat.isNotEmpty() && date != null) {
                                onDateChange(null) // Recurring clears specific single date
                            }
                        },
                        label = { Text(stringResource(labelRes)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LiteverTheme.colors.primary,
                            selectedLabelColor = LiteverTheme.colors.onPrimary
                        )
                    )
                }
            }
        }
    }

    // TimePicker Dialog
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = time.hour,
            initialMinute = time.minute,
            is24Hour = is24HourFormat
        )
        LiteverTimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            onConfirm = {
                onTimeChange(LocalTime.of(timePickerState.hour, timePickerState.minute))
                showTimePicker = false
            }
        ) {
            androidx.compose.material3.TimePicker(state = timePickerState)
        }
    }

    // DatePicker Dialog
    if (showDatePicker) {
        val initialEpochMillis = date?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
            ?: System.currentTimeMillis()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialEpochMillis)

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedMillis = datePickerState.selectedDateMillis
                        if (selectedMillis != null) {
                            val selectedDate = Instant.ofEpochMilli(selectedMillis)
                                .atZone(ZoneId.of("UTC"))
                                .toLocalDate()
                            onDateChange(selectedDate)
                            onRepeatDaysChange(emptyList()) // Specific date clears repeat days
                        }
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.action_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
