package vn.io.litever.remind.features.reminders.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vn.io.litever.designsystem.components.LiteverFilterChip
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = LiteverTheme.spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.medium)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.small)
                    ) {
                        Text(
                            text = stringResource(R.string.interval_section_title),
                            style = LiteverTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = LiteverTheme.colors.onSurface
                        )

                        // Inspired by QuickAlarmBottomSheet: Prominent interval display & Reset button
                        val totalMins = uiState.intervalMinutes ?: 0
                        val hours = totalMins / 60
                        val minutes = totalMins % 60
                        val displayDuration = when {
                            totalMins <= 0 -> "0m"
                            hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
                            hours > 0 -> "${hours}h"
                            else -> "${minutes}m"
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(LiteverTheme.colors.surfaceContainer)
                                .padding(
                                    horizontal = LiteverTheme.spacing.medium,
                                    vertical = LiteverTheme.spacing.smallMedium
                                )
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.interval_minutes_label),
                                    style = LiteverTheme.typography.bodySmall,
                                    color = LiteverTheme.colors.onSurfaceVariant
                                )
                                Text(
                                    text = displayDuration,
                                    style = LiteverTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.ExtraBold
                                    ),
                                    color = LiteverTheme.colors.primary
                                )
                            }

                            if (totalMins > 0) {
                                LvIconButton(
                                    onClick = { onIntervalMinutesChange("") },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = ReMindIcons.Refresh,
                                        contentDescription = stringResource(R.string.reset),
                                        tint = LiteverTheme.colors.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Quick interval presets arranged in structured grid cells for easy tapping
                        val presets = listOf(
                            15 to stringResource(R.string.interval_preset_15m),
                            30 to stringResource(R.string.interval_preset_30m),
                            60 to stringResource(R.string.interval_preset_1h),
                            120 to stringResource(R.string.interval_preset_2h),
                            180 to stringResource(R.string.interval_preset_3h),
                            240 to stringResource(R.string.interval_preset_4h)
                        )

                        val chunkedPresets = presets.chunked(3)
                        chunkedPresets.forEach { rowPresets ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.small)
                            ) {
                                rowPresets.forEach { (minutes, label) ->
                                    val isSelected = uiState.intervalMinutes == minutes
                                    LvButton(
                                        onClick = { onIntervalMinutesChange(minutes.toString()) },
                                        modifier = Modifier.weight(1f),
                                        type = if (isSelected) LvButtonType.Tonal else LvButtonType.Outlined,
                                        semantic = if (isSelected) LvSemantic.Primary else LvSemantic.Secondary
                                    ) {
                                        Text(
                                            text = label,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                                repeat(3 - rowPresets.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
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
                        modifier = Modifier.padding(horizontal = LiteverTheme.spacing.medium)
                            .padding(bottom = LiteverTheme.spacing.medium)
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