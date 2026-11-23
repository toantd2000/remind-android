package vn.io.litever.remind.features.reminders.ui.components

import androidx.compose.material3.MaterialTheme
import vn.io.litever.remind.core.designsystem.theme.spacing
import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
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
import vn.io.litever.remind.core.common.util.TimeFormatUtils
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.model.ReminderTimeWindow
import vn.io.litever.remind.features.reminders.R
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderTimeWindowConfigSection(
    timeWindows: List<ReminderTimeWindow>,
    onTimeWindowsChange: (List<ReminderTimeWindow>) -> Unit,
    onAddTimeWindow: (ReminderTimeWindow) -> Unit,
    onRemoveTimeWindow: (Int) -> Unit,
    modifier: Modifier = Modifier,
    @Suppress("UNUSED_PARAMETER") onSetAllDayPreset: () -> Unit = {},
    is24HourFormat: Boolean = true,
    isPro: Boolean = false,
    onNavigateToPaywall: (Int) -> Unit = {}
) {
    var showWindowDialog by remember { mutableStateOf(false) }
    var editingWindowIndex by remember { mutableStateOf<Int?>(null) }
    var editingStartTime by remember { mutableStateOf(LocalTime.of(8, 0)) }
    var editingEndTime by remember { mutableStateOf(LocalTime.of(18, 0)) }

    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
    ) {
        Text(
            text = stringResource(R.string.time_window_section_title),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall)
        ) {
            val displayWindows = timeWindows.ifEmpty { listOf(ReminderTimeWindow.ALL_DAY) }

            displayWindows.forEachIndexed { index, window ->
                val startFormatted = TimeFormatUtils.formatTime(window.startTime, is24HourFormat)
                val endFormatted = TimeFormatUtils.formatTime(window.endTime, is24HourFormat)
                val label = "$startFormatted - $endFormatted"

                InputChip(
                    selected = true,
                    onClick = {
                        editingWindowIndex = if (timeWindows.isNotEmpty()) index else null
                        editingStartTime = window.startTime
                        editingEndTime = window.endTime
                        showWindowDialog = true
                    },
                    label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                    trailingIcon = if (displayWindows.size > 1 && timeWindows.isNotEmpty()) {
                        {
                            Icon(
                                imageVector = ReMindIcons.Close,
                                contentDescription = "Remove Time Window",
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { onRemoveTimeWindow(index) }
                            )
                        }
                    } else null,
                    colors = InputChipDefaults.inputChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondary,
                        selectedLabelColor = MaterialTheme.colorScheme.onSecondary,
                        selectedTrailingIconColor = MaterialTheme.colorScheme.onSecondary,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        leadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.height(32.dp)
                )
            }

            val isLimitReached = !isPro && displayWindows.size >= 2
            InputChip(
                selected = false,
                onClick = {
                    if (isLimitReached) {
                        onNavigateToPaywall(5)
                    } else {
                        editingWindowIndex = null
                        editingStartTime = LocalTime.of(8, 0)
                        editingEndTime = LocalTime.of(18, 0)
                        showWindowDialog = true
                    }
                },
                label = {
                    Text(
                        stringResource(R.string.add_time_window),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = if (isLimitReached) ReMindIcons.Lock else ReMindIcons.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                colors = InputChipDefaults.inputChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.height(32.dp)
            )
        }
    }

    // Time Window Config Dialog (Start & End Time)
    if (showWindowDialog) {
        AlertDialog(
            onDismissRequest = { showWindowDialog = false },
            title = {
                Text(
                    text = if (editingWindowIndex != null) stringResource(R.string.time_window_section_title)
                    else stringResource(R.string.add_time_window)
                )
            },
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                ) {
                    // Start Time Selector
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showStartTimePicker = true },
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.time_window_start_time),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = TimeFormatUtils.formatTime(editingStartTime, is24HourFormat),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // End Time Selector
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showEndTimePicker = true },
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.time_window_end_time),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = TimeFormatUtils.formatTime(editingEndTime, is24HourFormat),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newWindow = ReminderTimeWindow(
                            startTime = editingStartTime,
                            endTime = editingEndTime,
                            activeDays = emptyList()
                        )
                        val activeIdx = editingWindowIndex
                        if (activeIdx != null && (activeIdx in timeWindows.indices)) {
                            val updated = timeWindows.toMutableList()
                            updated[activeIdx] = newWindow
                            onTimeWindowsChange(updated)
                        } else {
                            if (timeWindows.isEmpty()) {
                                onTimeWindowsChange(listOf(newWindow))
                            } else {
                                onAddTimeWindow(newWindow)
                            }
                        }
                        showWindowDialog = false
                    }
                ) {
                    Text(stringResource(R.string.action_save))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showWindowDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    // Start TimePicker Dialog
    if (showStartTimePicker) {
        val startPickerState = rememberTimePickerState(
            initialHour = editingStartTime.hour,
            initialMinute = editingStartTime.minute,
            is24Hour = is24HourFormat
        )
        TimePickerDialog(
            title = {
                Text(
                    stringResource(R.string.set_start_time),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(
                        bottom = MaterialTheme.spacing.medium,
                    )
                )
            },
            onDismissRequest = { showStartTimePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        editingStartTime = LocalTime.of(startPickerState.hour, startPickerState.minute)
                        showStartTimePicker = false
                    }
                ) {
                    Text(stringResource(R.string.action_save))
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showStartTimePicker = false },
                    modifier = Modifier.padding(end = MaterialTheme.spacing.medium)
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        ) {
            TimePicker(state = startPickerState)
        }
    }

    // End TimePicker Dialog
    if (showEndTimePicker) {
        val endPickerState = rememberTimePickerState(
            initialHour = editingEndTime.hour,
            initialMinute = editingEndTime.minute,
            is24Hour = is24HourFormat
        )
        TimePickerDialog(
            title = {
                Text(
                    stringResource(R.string.set_end_time),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(
                        bottom = MaterialTheme.spacing.medium,
                    )
                )
            },
            onDismissRequest = { showEndTimePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        editingEndTime = LocalTime.of(endPickerState.hour, endPickerState.minute)
                        showEndTimePicker = false
                    }
                ) {
                    Text(stringResource(R.string.action_save))
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showEndTimePicker = false },
                    modifier = Modifier.padding(end = MaterialTheme.spacing.medium)
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        ) {
            TimePicker(state = endPickerState)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReminderTimeWindowConfigSectionPreview() {
    ReMindTheme {
        ReminderTimeWindowConfigSection(
            timeWindows = listOf(
                ReminderTimeWindow(
                    startTime = LocalTime.of(8, 0),
                    endTime = LocalTime.of(12, 0),
                    activeDays = emptyList()
                ),
                ReminderTimeWindow(
                    startTime = LocalTime.of(13, 0),
                    endTime = LocalTime.of(17, 0),
                    activeDays = emptyList()
                )
            ),
            onTimeWindowsChange = {},
            onAddTimeWindow = {},
            onRemoveTimeWindow = {},
            is24HourFormat = true
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ReminderTimeWindowConfigSectionEmptyPreview() {
    ReMindTheme {
        ReminderTimeWindowConfigSection(
            timeWindows = emptyList(),
            onTimeWindowsChange = {},
            onAddTimeWindow = {},
            onRemoveTimeWindow = {},
            is24HourFormat = true
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ReminderTimeWindowConfigSectionDarkPreview() {
    ReMindTheme(darkTheme = true) {
        ReminderTimeWindowConfigSection(
            timeWindows = listOf(
                ReminderTimeWindow(
                    startTime = LocalTime.of(8, 0),
                    endTime = LocalTime.of(12, 0),
                    activeDays = emptyList()
                ),
                ReminderTimeWindow(
                    startTime = LocalTime.of(13, 0),
                    endTime = LocalTime.of(17, 0),
                    activeDays = emptyList()
                )
            ),
            onTimeWindowsChange = {},
            onAddTimeWindow = {},
            onRemoveTimeWindow = {},
            is24HourFormat = true
        )
    }
}

