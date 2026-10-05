package vn.io.litever.remind.features.reminders.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vn.io.litever.designsystem.components.LiteverFilterChip
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.model.ReminderMonthDay
import vn.io.litever.remind.core.model.ReminderTimeWindow
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.features.reminders.R
import vn.io.litever.remind.features.reminders.viewmodel.ReminderEditUiState
import vn.io.litever.remind.features.reminders.viewmodel.ReminderRepeatInterval
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReminderTriggerSection(
    uiState: ReminderEditUiState,
    onTriggerTypeSelected: (ReminderTriggerType) -> Unit,
    onTimeChange: (LocalTime) -> Unit,
    fixedTimes: List<LocalTime> = listOf(uiState.time),
    onAddFixedTime: (LocalTime) -> Unit = {},
    onRemoveFixedTime: (Int) -> Unit = {},
    onUpdateFixedTime: (Int, LocalTime) -> Unit = { _, t -> onTimeChange(t) },
    onDateChange: (LocalDate?) -> Unit,
    onRepeatDaysChange: (List<DayOfWeek>) -> Unit,
    onRepeatDayToggle: (DayOfWeek) -> Unit,
    onRepeatIntervalChange: (ReminderRepeatInterval) -> Unit,
    onMonthlyDayToggle: (Int) -> Unit,
    onAddYearlyDate: (Int, Int) -> Unit,
    onRemoveYearlyDate: (Int) -> Unit,
    onIntervalMinutesChange: (String) -> Unit = {},
    onTimeWindowsChange: (List<ReminderTimeWindow>) -> Unit = {},
    onAddTimeWindow: (ReminderTimeWindow) -> Unit = {},
    onRemoveTimeWindow: (Int) -> Unit = {},
    is24HourFormat: Boolean = true,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = LiteverTheme.shapes.large,
        color = LiteverTheme.colors.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(LiteverTheme.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.medium)
        ) {
            Text(
                text = stringResource(R.string.section_time_trigger),
                style = LiteverTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LiteverTheme.colors.onSurface
            )

            // Tab selector for trigger types
            ReminderTriggerTypeSelector(
            selectedTriggerType = uiState.triggerType,
            onTriggerTypeSelected = onTriggerTypeSelected
        )

        // Trigger Configuration Content
        when (uiState.triggerType) {
            ReminderTriggerType.FIXED_TIME -> {
                ReminderTimeConfigSection(
                    time = uiState.time,
                    onTimeChange = onTimeChange,
                    fixedTimes = fixedTimes,
                    onAddFixedTime = onAddFixedTime,
                    onRemoveFixedTime = onRemoveFixedTime,
                    onUpdateFixedTime = onUpdateFixedTime,
                    date = uiState.date,
                    onDateChange = onDateChange,
                    repeatDays = uiState.repeatDays,
                    onRepeatDaysChange = onRepeatDaysChange,
                    onRepeatDayToggle = onRepeatDayToggle,
                    repeatInterval = uiState.repeatInterval,
                    onRepeatIntervalChange = onRepeatIntervalChange,
                    monthlyDays = uiState.monthlyDays,
                    onMonthlyDayToggle = onMonthlyDayToggle,
                    yearlyDates = uiState.yearlyDates,
                    onAddYearlyDate = onAddYearlyDate,
                    onRemoveYearlyDate = onRemoveYearlyDate,
                    is24HourFormat = is24HourFormat
                )
            }
            ReminderTriggerType.INTERVAL -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.small)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(LiteverTheme.spacing.medium)
                        ) {
                            Text(
                                text = stringResource(R.string.interval_section_title),
                                style = LiteverTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = LiteverTheme.colors.onSurface
                            )

                            Spacer(modifier = Modifier.height(LiteverTheme.spacing.small))

                            OutlinedTextField(
                                value = uiState.intervalMinutes?.toString() ?: "",
                                onValueChange = { onIntervalMinutesChange(it.filter { char -> char.isDigit() }) },
                                label = { Text(stringResource(R.string.interval_minutes_label)) },
                                placeholder = { Text(stringResource(R.string.interval_minutes_hint)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(LiteverTheme.spacing.small))

                            // Quick interval presets
                            val presets = listOf(
                                15 to stringResource(R.string.interval_preset_15m),
                                30 to stringResource(R.string.interval_preset_30m),
                                60 to stringResource(R.string.interval_preset_1h),
                                120 to stringResource(R.string.interval_preset_2h),
                                180 to stringResource(R.string.interval_preset_3h),
                                240 to stringResource(R.string.interval_preset_4h)
                            )

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                presets.forEach { (minutes, label) ->
                                    LiteverFilterChip(
                                        selected = uiState.intervalMinutes == minutes,
                                        onClick = { onIntervalMinutesChange(minutes.toString()) },
                                        label = { Text(label, style = LiteverTheme.typography.labelSmall) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // 2. Unified Repeat Configuration (One-time, Weekly, Monthly, Yearly)
                            ReminderRepeatConfigSection(
                                repeatInterval = uiState.repeatInterval,
                                onRepeatIntervalChange = onRepeatIntervalChange,
                                date = uiState.date,
                                onDateChange = onDateChange,
                                repeatDays = uiState.repeatDays,
                                onRepeatDaysChange = onRepeatDaysChange,
                                onRepeatDayToggle = onRepeatDayToggle,
                                monthlyDays = uiState.monthlyDays,
                                onMonthlyDayToggle = onMonthlyDayToggle,
                                yearlyDates = uiState.yearlyDates,
                                onAddYearlyDate = onAddYearlyDate,
                                onRemoveYearlyDate = onRemoveYearlyDate
                            )
                        }
                    }

                    // Multi-TimeWindow Configuration
                    ReminderTimeWindowConfigSection(
                        timeWindows = uiState.timeWindows,
                        onTimeWindowsChange = onTimeWindowsChange,
                        onAddTimeWindow = onAddTimeWindow,
                        onRemoveTimeWindow = onRemoveTimeWindow,
                        onSetAllDayPreset = {
                            onTimeWindowsChange(
                                listOf(
                                    ReminderTimeWindow(
                                        startTime = LocalTime.of(0, 0),
                                        endTime = LocalTime.of(23, 59),
                                        activeDays = emptyList()
                                    )
                                )
                            )
                        },
                        is24HourFormat = is24HourFormat
                    )
                }
            }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReminderTriggerSectionFixedTimePreview() {
    ReMindTheme {
        ReminderTriggerSection(
            uiState = ReminderEditUiState(
                triggerType = ReminderTriggerType.FIXED_TIME,
                repeatInterval = ReminderRepeatInterval.WEEKLY,
                repeatDays = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
            ),
            onTriggerTypeSelected = {},
            onTimeChange = {},
            onDateChange = {},
            onRepeatDaysChange = {},
            onRepeatDayToggle = {},
            onRepeatIntervalChange = {},
            onMonthlyDayToggle = {},
            onAddYearlyDate = { _, _ -> },
            onRemoveYearlyDate = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ReminderTriggerSectionIntervalPreview() {
    ReMindTheme {
        ReminderTriggerSection(
            uiState = ReminderEditUiState(
                triggerType = ReminderTriggerType.INTERVAL,
                intervalMinutes = 60
            ),
            onTriggerTypeSelected = {},
            onTimeChange = {},
            onDateChange = {},
            onRepeatDaysChange = {},
            onRepeatDayToggle = {},
            onRepeatIntervalChange = {},
            onMonthlyDayToggle = {},
            onAddYearlyDate = { _, _ -> },
            onRemoveYearlyDate = {}
        )
    }
}