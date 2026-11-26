package vn.io.litever.remind.features.reminders.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import vn.io.litever.remind.core.designsystem.components.ReMindSettingsGroup
import vn.io.litever.remind.core.designsystem.components.ReMindSettingsItem
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.designsystem.theme.spacing
import vn.io.litever.remind.core.model.ReminderTimeWindow
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.features.reminders.R
import vn.io.litever.remind.features.reminders.viewmodel.ReminderEditUiState
import java.time.LocalTime

@Composable
fun ReminderTriggerSection(
    modifier: Modifier = Modifier,
    uiState: ReminderEditUiState,
    onTriggerTypeSelected: (ReminderTriggerType) -> Unit,
    onTimeChange: (LocalTime) -> Unit,
    fixedTimes: List<LocalTime> = listOf(uiState.time),
    onAddFixedTime: (LocalTime) -> Unit = {},
    onRemoveFixedTime: (Int) -> Unit = {},
    onUpdateFixedTime: (Int, LocalTime) -> Unit = { _, t -> onTimeChange(t) },
    onIntervalMinutesChange: (String) -> Unit = {},
    onTimeWindowsChange: (List<ReminderTimeWindow>) -> Unit = {},
    onAddTimeWindow: (ReminderTimeWindow) -> Unit = {},
    onRemoveTimeWindow: (Int) -> Unit = {},
    is24HourFormat: Boolean = true,
    isPro: Boolean = false,
    onNavigateToPaywall: (Int) -> Unit = {},
    embedded: Boolean = false
) {
    val content: @Composable () -> Unit = {
        // Tab selector for trigger types (Fixed Time vs Interval)
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
                    is24HourFormat = is24HourFormat,
                    isPro = isPro,
                    onNavigateToPaywall = onNavigateToPaywall,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.spacing.medium)
                        .padding(horizontal = MaterialTheme.spacing.medium)
                )
            }

            ReminderTriggerType.INTERVAL -> {
                var showIntervalBottomSheet by remember { mutableStateOf(false) }
                val totalMins = uiState.intervalMinutes ?: 0
                val hours = totalMins / 60
                val minutes = totalMins % 60
                val displayDuration = when {
                    totalMins <= 0 -> stringResource(R.string.off)
                    hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
                    hours > 0 -> "${hours}h"
                    else -> "${minutes}m"
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                ) {
                    ReMindSettingsItem(
                        title = stringResource(R.string.interval_section_title),
                        subtitle = if (totalMins > 0) {
                            stringResource(R.string.interval_set_format, displayDuration)
                        } else {
                            stringResource(R.string.tap_to_set_interval)
                        },
                        icon = ReMindIcons.Timer,
                        iconTint = if (totalMins > 0) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        showChevron = true,
                        onClick = { showIntervalBottomSheet = true }
                    )

                    if (showIntervalBottomSheet) {
                        IntervalBottomSheet(
                            currentIntervalMinutes = totalMins,
                            onDismissRequest = { showIntervalBottomSheet = false },
                            onConfirm = { mins ->
                                onIntervalMinutesChange(if (mins > 0) mins.toString() else "")
                                showIntervalBottomSheet = false
                            }
                        )
                    }

                    // Multi-TimeWindow Configuration (Active daily time windows)
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
                        is24HourFormat = is24HourFormat,
                        isPro = isPro,
                        onNavigateToPaywall = onNavigateToPaywall,
                        modifier = Modifier
                            .padding(horizontal = MaterialTheme.spacing.medium)
                            .padding(bottom = MaterialTheme.spacing.medium)
                    )
                }
            }
        }
    }

    if (embedded) {
        Column(modifier = modifier.fillMaxWidth()) {
            content()
        }
    } else {
        ReMindSettingsGroup(
            title = stringResource(R.string.section_time_trigger),
            modifier = modifier
        ) {
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReminderTriggerSectionFixedTimePreview() {
    ReMindTheme {
        ReminderTriggerSection(
            uiState = ReminderEditUiState(
                triggerType = ReminderTriggerType.FIXED_TIME
            ),
            onTriggerTypeSelected = {},
            onTimeChange = {}
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
            onTimeChange = {}
        )
    }
}
