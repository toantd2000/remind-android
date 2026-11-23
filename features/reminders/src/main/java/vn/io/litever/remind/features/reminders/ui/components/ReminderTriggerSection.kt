package vn.io.litever.remind.features.reminders.ui.components

import androidx.compose.material3.MaterialTheme
import vn.io.litever.remind.core.designsystem.theme.spacing
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import vn.io.litever.remind.core.designsystem.components.ReMindSettingsGroup
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
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
    onNavigateToPaywall: (Int) -> Unit = {}
) {
    ReMindSettingsGroup(
        title = stringResource(R.string.section_time_trigger),
        modifier = modifier
    ) {
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
                ) {
                    // Simple settings row (clickable card) opening IntervalBottomSheet
                    Surface(
                        onClick = { showIntervalBottomSheet = true },
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        ),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .padding(MaterialTheme.spacing.medium),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.interval_section_title),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = stringResource(R.string.tap_to_set_interval),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Text(
                                text = if (totalMins > 0) {
                                    stringResource(R.string.interval_set_format, displayDuration)
                                } else {
                                    stringResource(R.string.interval_not_set)
                                },
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = if (totalMins > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Icon(
                                imageVector = ReMindIcons.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

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
                        modifier = Modifier.padding(bottom = MaterialTheme.spacing.medium)
                    )
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