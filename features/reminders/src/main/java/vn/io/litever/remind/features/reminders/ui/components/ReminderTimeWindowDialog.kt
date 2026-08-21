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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import vn.io.litever.designsystem.components.LiteverOutlinedButton
import vn.io.litever.designsystem.components.LiteverTimePickerDialog
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.common.util.TimeFormatUtils
import vn.io.litever.remind.core.model.DayOfWeek
import vn.io.litever.remind.core.model.ReminderTimeWindow
import vn.io.litever.remind.features.reminders.R
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReminderTimeWindowDialog(
    initialTimeWindow: ReminderTimeWindow?,
    is24HourFormat: Boolean,
    onDismissRequest: () -> Unit,
    onConfirm: (ReminderTimeWindow?) -> Unit
) {
    var startTime by remember { mutableStateOf(initialTimeWindow?.startTime ?: LocalTime.of(8, 0)) }
    var endTime by remember { mutableStateOf(initialTimeWindow?.endTime ?: LocalTime.of(18, 0)) }
    var activeDays by remember { mutableStateOf(initialTimeWindow?.activeDays ?: emptyList()) }

    var isPickingStart by remember { mutableStateOf(false) }
    var isPickingEnd by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = stringResource(R.string.time_window_section_title),
                style = LiteverTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.time_window_desc),
                    style = LiteverTheme.typography.bodySmall,
                    color = LiteverTheme.colors.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Start Time
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(LiteverTheme.shapes.medium)
                        .clickable { isPickingStart = true },
                    color = LiteverTheme.colors.surfaceVariant.copy(alpha = 0.5f),
                    shape = LiteverTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.time_window_start_time),
                            style = LiteverTheme.typography.bodyMedium
                        )
                        Text(
                            text = TimeFormatUtils.formatTime(startTime, is24HourFormat),
                            style = LiteverTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = LiteverTheme.colors.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // End Time
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(LiteverTheme.shapes.medium)
                        .clickable { isPickingEnd = true },
                    color = LiteverTheme.colors.surfaceVariant.copy(alpha = 0.5f),
                    shape = LiteverTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.time_window_end_time),
                            style = LiteverTheme.typography.bodyMedium
                        )
                        Text(
                            text = TimeFormatUtils.formatTime(endTime, is24HourFormat),
                            style = LiteverTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = LiteverTheme.colors.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Schedule Type: All Days / One-time vs Selected Days
                val isRepeatingDays = activeDays.isNotEmpty()
                Text(
                    text = stringResource(R.string.time_window_active_days),
                    style = LiteverTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        onClick = { activeDays = emptyList() },
                        selected = !isRepeatingDays,
                        label = { Text(stringResource(R.string.time_window_all_days), style = LiteverTheme.typography.labelSmall) }
                    )
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        onClick = {
                            if (!isRepeatingDays) {
                                activeDays = DayOfWeek.entries
                            }
                        },
                        selected = isRepeatingDays,
                        label = { Text(stringResource(R.string.time_window_selected_days), style = LiteverTheme.typography.labelSmall) }
                    )
                }

                if (isRepeatingDays) {
                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        DayOfWeek.entries.forEach { day ->
                            val isSelected = activeDays.contains(day)
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
                                    activeDays = if (isSelected) {
                                        activeDays - day
                                    } else {
                                        (activeDays + day).sortedBy { it.toJavaDayValue() }
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
        },
        confirmButton = {
            LiteverButton(
                onClick = {
                    onConfirm(
                        ReminderTimeWindow(
                            startTime = startTime,
                            endTime = endTime,
                            activeDays = activeDays
                        )
                    )
                }
            ) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (initialTimeWindow != null) {
                    LiteverOutlinedButton(onClick = { onConfirm(null) }) {
                        Text(
                            text = stringResource(R.string.clear_time_window),
                            color = LiteverTheme.colors.error
                        )
                    }
                }
                LiteverOutlinedButton(onClick = onDismissRequest) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        }
    )

    if (isPickingStart) {
        val startPickerState = rememberTimePickerState(
            initialHour = startTime.hour,
            initialMinute = startTime.minute,
            is24Hour = is24HourFormat
        )
        LiteverTimePickerDialog(
            onDismissRequest = { isPickingStart = false },
            confirmButton = {
                LiteverButton(onClick = {
                    startTime = LocalTime.of(startPickerState.hour, startPickerState.minute)
                    isPickingStart = false
                }) { Text(stringResource(R.string.action_save)) }
            },
            dismissButton = {
                LiteverOutlinedButton(onClick = { isPickingStart = false }) { Text(stringResource(R.string.action_cancel)) }
            }
        ) {
            TimePicker(state = startPickerState)
        }
    }

    if (isPickingEnd) {
        val endPickerState = rememberTimePickerState(
            initialHour = endTime.hour,
            initialMinute = endTime.minute,
            is24Hour = is24HourFormat
        )
        LiteverTimePickerDialog(
            onDismissRequest = { isPickingEnd = false },
            confirmButton = {
                LiteverButton(onClick = {
                    endTime = LocalTime.of(endPickerState.hour, endPickerState.minute)
                    isPickingEnd = false
                }) { Text(stringResource(R.string.action_save)) }
            },
            dismissButton = {
                LiteverOutlinedButton(onClick = { isPickingEnd = false }) { Text(stringResource(R.string.action_cancel)) }
            }
        ) {
            TimePicker(state = endPickerState)
        }
    }
}
