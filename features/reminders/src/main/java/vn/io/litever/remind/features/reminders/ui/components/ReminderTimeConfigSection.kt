package vn.io.litever.remind.features.reminders.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
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
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.features.reminders.R
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReminderTimeConfigSection(
    time: LocalTime,
    onTimeChange: (LocalTime) -> Unit,
    fixedTimes: List<LocalTime> = listOf(time),
    onAddFixedTime: (LocalTime) -> Unit = {},
    onRemoveFixedTime: (Int) -> Unit = {},
    onUpdateFixedTime: (Int, LocalTime) -> Unit = { _, t -> onTimeChange(t) },
    is24HourFormat: Boolean = true,
    modifier: Modifier = Modifier
) {
    var showAddTimePicker by remember { mutableStateOf(false) }
    var showEditTimePicker by remember { mutableStateOf(false) }
    var editingTimeIndex by remember { mutableStateOf(0) }
    var editingInitialTime by remember { mutableStateOf(time) }

    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.section_fixed_times_title),
                    style = LiteverTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = LiteverTheme.colors.onSurface
                )

                LiteverOutlinedButton(
                    onClick = { showAddTimePicker = true },
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(
                        imageVector = ReMindIcons.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.add_fixed_time),
                        style = LiteverTheme.typography.labelSmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(LiteverTheme.spacing.small))

            // Multi-Time Target Chips
            val timesList = if (fixedTimes.isNotEmpty()) fixedTimes else listOf(time)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                timesList.forEachIndexed { index, targetTime ->
                    InputChip(
                        selected = true,
                        onClick = {
                            editingTimeIndex = index
                            editingInitialTime = targetTime
                            showEditTimePicker = true
                        },
                        label = {
                            Text(
                                text = TimeFormatUtils.formatTime(targetTime, is24HourFormat),
                                style = LiteverTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = ReMindIcons.AccessTime,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        trailingIcon = if (timesList.size > 1) {
                            {
                                Icon(
                                    imageVector = ReMindIcons.Close,
                                    contentDescription = stringResource(R.string.remove_fixed_time),
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { onRemoveFixedTime(index) }
                                )
                            }
                        } else null,
                        colors = InputChipDefaults.inputChipColors(
                            selectedContainerColor = LiteverTheme.colors.primaryContainer,
                            selectedLabelColor = LiteverTheme.colors.onPrimaryContainer,
                            selectedLeadingIconColor = LiteverTheme.colors.onPrimaryContainer
                        )
                    )
                }
            }
        }
    }

    // Add TimePicker Dialog
    if (showAddTimePicker) {
        val addTimeState = rememberTimePickerState(
            initialHour = 12,
            initialMinute = 0,
            is24Hour = is24HourFormat
        )
        LiteverTimePickerDialog(
            onDismissRequest = { showAddTimePicker = false },
            confirmButton = {
                LiteverButton(onClick = {
                    onAddFixedTime(LocalTime.of(addTimeState.hour, addTimeState.minute))
                    showAddTimePicker = false
                }) { Text(stringResource(R.string.action_save)) }
            },
            dismissButton = {
                LiteverOutlinedButton(onClick = { showAddTimePicker = false }) { Text(stringResource(R.string.action_cancel)) }
            }
        ) {
            TimePicker(state = addTimeState)
        }
    }

    // Edit TimePicker Dialog
    if (showEditTimePicker) {
        val editTimeState = rememberTimePickerState(
            initialHour = editingInitialTime.hour,
            initialMinute = editingInitialTime.minute,
            is24Hour = is24HourFormat
        )
        LiteverTimePickerDialog(
            onDismissRequest = { showEditTimePicker = false },
            confirmButton = {
                LiteverButton(onClick = {
                    onUpdateFixedTime(editingTimeIndex, LocalTime.of(editTimeState.hour, editTimeState.minute))
                    showEditTimePicker = false
                }) { Text(stringResource(R.string.action_save)) }
            },
            dismissButton = {
                LiteverOutlinedButton(onClick = { showEditTimePicker = false }) { Text(stringResource(R.string.action_cancel)) }
            }
        ) {
            TimePicker(state = editTimeState)
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
            is24HourFormat = true
        )
    }
}
