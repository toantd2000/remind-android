package vn.io.litever.remind.features.reminders.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import vn.io.litever.designsystem.components.LiteverButton
import vn.io.litever.designsystem.components.LiteverFilterChip
import vn.io.litever.designsystem.components.LiteverIconButton
import vn.io.litever.designsystem.components.LiteverOutlinedButton
import vn.io.litever.designsystem.components.LiteverSegmentedButton
import vn.io.litever.designsystem.components.LiteverSingleChoiceSegmentedButtonRow
import vn.io.litever.designsystem.components.LiteverSwitch
import vn.io.litever.designsystem.components.LiteverTimePickerDialog
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.common.util.TimeFormatUtils
import vn.io.litever.remind.core.designsystem.components.ReMindSettingIcon
import vn.io.litever.remind.core.model.DayOfWeek
import vn.io.litever.remind.core.model.ReminderTimeWindow
import vn.io.litever.remind.features.reminders.R
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReminderTimeWindowConfigSection(
    timeWindows: List<ReminderTimeWindow>,
    onTimeWindowsChange: (List<ReminderTimeWindow>) -> Unit,
    onAddTimeWindow: (ReminderTimeWindow) -> Unit,
    onRemoveTimeWindow: (Int) -> Unit,
    onSetAllDayPreset: () -> Unit,
    is24HourFormat: Boolean,
    modifier: Modifier = Modifier
) {
    val isEnabled = timeWindows.isNotEmpty()
    var editingWindowIndex by remember { mutableStateOf<Int?>(null) }
    var editingStartOrEnd by remember { mutableStateOf<Boolean?>(null) } // true: start, false: end

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(LiteverTheme.shapes.medium),
        color = LiteverTheme.colors.surfaceVariant.copy(alpha = 0.4f),
        shape = LiteverTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header with Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    ReMindSettingIcon(
                        imageVector = ReMindIcons.DateRange,
                        selected = isEnabled
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.time_window_section_title),
                            style = LiteverTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = LiteverTheme.colors.onSurface
                        )
                        val summary = if (isEnabled) {
                            timeWindows.joinToString("; ") { win ->
                                val s = TimeFormatUtils.formatTime(win.startTime, is24HourFormat)
                                val e = TimeFormatUtils.formatTime(win.endTime, is24HourFormat)
                                "$s - $e"
                            }
                        } else {
                            stringResource(R.string.time_window_24_7_desc)
                        }
                        Text(
                            text = summary,
                            style = LiteverTheme.typography.bodySmall,
                            color = if (isEnabled) LiteverTheme.colors.primary else LiteverTheme.colors.onSurfaceVariant
                        )
                    }
                }

                LiteverSwitch(
                    checked = isEnabled,
                    onCheckedChange = { checked ->
                        if (checked) {
                            onTimeWindowsChange(
                                listOf(
                                    ReminderTimeWindow(
                                        startTime = LocalTime.of(8, 0),
                                        endTime = LocalTime.of(18, 0),
                                        activeDays = emptyList()
                                    )
                                )
                            )
                        } else {
                            onTimeWindowsChange(emptyList())
                        }
                    }
                )
            }

            AnimatedVisibility(
                visible = isEnabled,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    // Quick Preset & Add Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LiteverOutlinedButton(
                            onClick = onSetAllDayPreset,
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                        ) {
                            Icon(
                                imageVector = ReMindIcons.WbSunny,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.preset_all_day),
                                style = LiteverTheme.typography.labelSmall
                            )
                        }

                        LiteverOutlinedButton(
                            onClick = {
                                onAddTimeWindow(
                                    ReminderTimeWindow(
                                        startTime = LocalTime.of(13, 0),
                                        endTime = LocalTime.of(17, 0),
                                        activeDays = emptyList()
                                    )
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                        ) {
                            Icon(
                                imageVector = ReMindIcons.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.add_time_window),
                                style = LiteverTheme.typography.labelSmall
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Windows list
                    timeWindows.forEachIndexed { index, window ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(LiteverTheme.shapes.medium),
                            color = LiteverTheme.colors.surface.copy(alpha = 0.7f),
                            shape = LiteverTheme.shapes.medium
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${stringResource(R.string.time_window_section_title)} ${index + 1}",
                                        style = LiteverTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = LiteverTheme.colors.primary
                                    )

                                    if (timeWindows.size > 1) {
                                        LiteverIconButton(
                                            onClick = { onRemoveTimeWindow(index) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = ReMindIcons.DeleteOutline,
                                                contentDescription = "Remove Time Window",
                                                tint = LiteverTheme.colors.error
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Start & End Times Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Start Time
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(LiteverTheme.shapes.small)
                                            .clickable {
                                                editingWindowIndex = index
                                                editingStartOrEnd = true
                                            },
                                        color = LiteverTheme.colors.surfaceVariant.copy(alpha = 0.5f),
                                        shape = LiteverTheme.shapes.small
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text(
                                                text = stringResource(R.string.time_window_start_time),
                                                style = LiteverTheme.typography.labelSmall,
                                                color = LiteverTheme.colors.onSurfaceVariant
                                            )
                                            Text(
                                                text = TimeFormatUtils.formatTime(window.startTime, is24HourFormat),
                                                style = LiteverTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = LiteverTheme.colors.onSurface
                                            )
                                        }
                                    }

                                    // End Time
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(LiteverTheme.shapes.small)
                                            .clickable {
                                                editingWindowIndex = index
                                                editingStartOrEnd = false
                                            },
                                        color = LiteverTheme.colors.surfaceVariant.copy(alpha = 0.5f),
                                        shape = LiteverTheme.shapes.small
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text(
                                                text = stringResource(R.string.time_window_end_time),
                                                style = LiteverTheme.typography.labelSmall,
                                                color = LiteverTheme.colors.onSurfaceVariant
                                            )
                                            Text(
                                                text = TimeFormatUtils.formatTime(window.endTime, is24HourFormat),
                                                style = LiteverTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = LiteverTheme.colors.onSurface
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Active Days Filter for this window
                                val isRepeatingDays = window.activeDays.isNotEmpty()
                                LiteverSingleChoiceSegmentedButtonRow(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    LiteverSegmentedButton(
                                        onClick = {
                                            val updated = timeWindows.toMutableList()
                                            updated[index] = window.copy(activeDays = emptyList())
                                            onTimeWindowsChange(updated)
                                        },
                                        selected = !isRepeatingDays,
                                        label = { Text(stringResource(R.string.time_window_all_days), style = LiteverTheme.typography.labelSmall) }
                                    )
                                    LiteverSegmentedButton(
                                        onClick = {
                                            if (!isRepeatingDays) {
                                                val updated = timeWindows.toMutableList()
                                                updated[index] = window.copy(activeDays = DayOfWeek.entries)
                                                onTimeWindowsChange(updated)
                                            }
                                        },
                                        selected = isRepeatingDays,
                                        label = { Text(stringResource(R.string.time_window_selected_days), style = LiteverTheme.typography.labelSmall) }
                                    )
                                }

                                if (isRepeatingDays) {
                                    Spacer(modifier = Modifier.height(6.dp))

                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        DayOfWeek.entries.forEach { day ->
                                            val isSelected = window.activeDays.contains(day)
                                            val labelRes = when (day) {
                                                DayOfWeek.MONDAY -> R.string.day_mon
                                                DayOfWeek.TUESDAY -> R.string.day_tue
                                                DayOfWeek.WEDNESDAY -> R.string.day_wed
                                                DayOfWeek.THURSDAY -> R.string.day_thu
                                                DayOfWeek.FRIDAY -> R.string.day_fri
                                                DayOfWeek.SATURDAY -> R.string.day_sat
                                                DayOfWeek.SUNDAY -> R.string.day_sun
                                            }

                                            LiteverFilterChip(
                                                selected = isSelected,
                                                onClick = {
                                                    val newDays = if (isSelected) {
                                                        window.activeDays - day
                                                    } else {
                                                        (window.activeDays + day).sortedBy { it.toJavaDayValue() }
                                                    }
                                                    val updated = timeWindows.toMutableList()
                                                    updated[index] = window.copy(activeDays = newDays)
                                                    onTimeWindowsChange(updated)
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
                        }
                    }
                }
            }
        }
    }

    // TimePicker for Start or End Time
    val activeIdx = editingWindowIndex
    val isStart = editingStartOrEnd
    if (activeIdx != null && isStart != null && activeIdx in timeWindows.indices) {
        val targetWindow = timeWindows[activeIdx]
        val initialTime = if (isStart) targetWindow.startTime else targetWindow.endTime

        val pickerState = rememberTimePickerState(
            initialHour = initialTime.hour,
            initialMinute = initialTime.minute,
            is24Hour = is24HourFormat
        )

        LiteverTimePickerDialog(
            onDismissRequest = {
                editingWindowIndex = null
                editingStartOrEnd = null
            },
            confirmButton = {
                LiteverButton(onClick = {
                    val newTime = LocalTime.of(pickerState.hour, pickerState.minute)
                    val updated = timeWindows.toMutableList()
                    updated[activeIdx] = if (isStart) {
                        targetWindow.copy(startTime = newTime)
                    } else {
                        targetWindow.copy(endTime = newTime)
                    }
                    onTimeWindowsChange(updated)
                    editingWindowIndex = null
                    editingStartOrEnd = null
                }) {
                    Text(stringResource(R.string.action_save))
                }
            },
            dismissButton = {
                LiteverOutlinedButton(onClick = {
                    editingWindowIndex = null
                    editingStartOrEnd = null
                }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        ) {
            TimePicker(state = pickerState)
        }
    }
}
