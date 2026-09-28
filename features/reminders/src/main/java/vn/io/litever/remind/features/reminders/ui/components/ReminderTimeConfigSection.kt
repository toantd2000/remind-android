package vn.io.litever.remind.features.reminders.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vn.io.litever.designsystem.components.LiteverButton
import vn.io.litever.designsystem.components.LiteverOutlinedButton
import vn.io.litever.designsystem.components.LiteverTimePickerDialog
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.common.util.TimeFormatUtils
import vn.io.litever.remind.core.designsystem.components.ReMindSettingIcon
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.model.DayOfWeek
import vn.io.litever.remind.core.model.ReminderMonthDay
import vn.io.litever.remind.features.reminders.R
import vn.io.litever.remind.features.reminders.viewmodel.ReminderRepeatInterval
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReminderTimeConfigSection(
    time: LocalTime,
    onTimeChange: (LocalTime) -> Unit,
    date: LocalDate?,
    onDateChange: (LocalDate?) -> Unit,
    repeatDays: List<DayOfWeek>,
    onRepeatDaysChange: (List<DayOfWeek>) -> Unit,
    onRepeatDayToggle: (DayOfWeek) -> Unit,
    repeatInterval: ReminderRepeatInterval,
    onRepeatIntervalChange: (ReminderRepeatInterval) -> Unit,
    monthlyDays: List<Int>,
    onMonthlyDayToggle: (Int) -> Unit,
    yearlyDates: List<ReminderMonthDay>,
    onAddYearlyDate: (Int, Int) -> Unit,
    onRemoveYearlyDate: (Int) -> Unit,
    is24HourFormat: Boolean,
    modifier: Modifier = Modifier
) {
    var showTimePicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showAddYearlyDateDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = LiteverTheme.colors.surfaceContainerLow,
        shape = LiteverTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(LiteverTheme.spacing.medium)
        ) {
            Text(
                text = stringResource(R.string.section_time_trigger),
                style = LiteverTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LiteverTheme.colors.onSurface
            )

            Spacer(modifier = Modifier.height(LiteverTheme.spacing.medium))

            // 1. Time Setting Row
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = LiteverTheme.colors.surfaceContainer,
                shape = LiteverTheme.shapes.medium,
                onClick = { showTimePicker = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        ReMindSettingIcon(
                            imageVector = Icons.Rounded.AccessTime,
                            selected = false
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.select_time),
                                style = LiteverTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = LiteverTheme.colors.onSurface
                            )
                            Text(
                                text = TimeFormatUtils.formatTime(time, is24HourFormat),
                                style = LiteverTheme.typography.bodyMedium,
                                color = LiteverTheme.colors.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                        tint = LiteverTheme.colors.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Unified Repeat Configuration
            ReminderRepeatConfigSection(
                repeatInterval = repeatInterval,
                onRepeatIntervalChange = onRepeatIntervalChange,
                date = date,
                onDateChange = onDateChange,
                repeatDays = repeatDays,
                onRepeatDaysChange = onRepeatDaysChange,
                onRepeatDayToggle = onRepeatDayToggle,
                monthlyDays = monthlyDays,
                onMonthlyDayToggle = onMonthlyDayToggle,
                yearlyDates = yearlyDates,
                onAddYearlyDate = onAddYearlyDate,
                onRemoveYearlyDate = onRemoveYearlyDate
            )
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
            confirmButton = {
                LiteverButton(onClick = {
                    onTimeChange(LocalTime.of(timePickerState.hour, timePickerState.minute))
                    showTimePicker = false
                }) { Text(stringResource(R.string.action_save)) }
            },
            dismissButton = {
                LiteverOutlinedButton(onClick = { showTimePicker = false }) { Text(stringResource(R.string.action_cancel)) }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReminderTimeConfigSectionPreview() {
    ReMindTheme {
        ReminderTimeConfigSection(
            time = LocalTime.of(9, 0),
            onTimeChange = {},
            date = LocalDate.now(),
            onDateChange = {},
            repeatDays = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
            onRepeatDaysChange = {},
            onRepeatDayToggle = {},
            repeatInterval = ReminderRepeatInterval.WEEKLY,
            onRepeatIntervalChange = {},
            monthlyDays = emptyList(),
            onMonthlyDayToggle = {},
            yearlyDates = emptyList(),
            onAddYearlyDate = { _, _ -> },
            onRemoveYearlyDate = {},
            is24HourFormat = true
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ReminderTimeConfigSectionDarkPreview() {
    ReMindTheme(darkTheme = true) {
        ReminderTimeConfigSection(
            time = LocalTime.of(9, 0),
            onTimeChange = {},
            date = LocalDate.now(),
            onDateChange = {},
            repeatDays = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
            onRepeatDaysChange = {},
            onRepeatDayToggle = {},
            repeatInterval = ReminderRepeatInterval.WEEKLY,
            onRepeatIntervalChange = {},
            monthlyDays = emptyList(),
            onMonthlyDayToggle = {},
            yearlyDates = emptyList(),
            onAddYearlyDate = { _, _ -> },
            onRemoveYearlyDate = {},
            is24HourFormat = true
        )
    }
}


