package vn.io.litever.remind.features.reminders.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vn.io.litever.designsystem.components.button.LvButton
import vn.io.litever.designsystem.components.button.LvButtonType
import vn.io.litever.designsystem.components.button.LvIconButton
import vn.io.litever.designsystem.components.core.LvSemantic
import vn.io.litever.designsystem.theme.LiteverTheme
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
    is24HourFormat: Boolean = true
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
                    modifier = Modifier
                        .padding(bottom = LiteverTheme.spacing.medium)
                        .padding(horizontal = LiteverTheme.spacing.medium)
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
                        .padding(horizontal = LiteverTheme.spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.medium)
                ) {
                    // Simple settings row (clickable card) opening IntervalBottomSheet
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(LiteverTheme.colors.surfaceContainer)
                            .clickable { showIntervalBottomSheet = true }
                            .padding(LiteverTheme.spacing.medium),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.interval_section_title),
                                style = LiteverTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = LiteverTheme.colors.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (totalMins > 0) displayDuration else stringResource(R.string.off),
                                style = LiteverTheme.typography.bodyMedium,
                                color = if (totalMins > 0) LiteverTheme.colors.primary else LiteverTheme.colors.onSurfaceVariant
                            )
                        }

                        Icon(
                            imageVector = ReMindIcons.ChevronRight,
                            contentDescription = null,
                            tint = LiteverTheme.colors.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
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
                        modifier = Modifier.padding(bottom = LiteverTheme.spacing.medium)
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