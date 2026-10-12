package vn.io.litever.remind.features.reminders.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vn.io.litever.designsystem.components.button.LvButton
import vn.io.litever.designsystem.components.button.LvButtonType
import vn.io.litever.designsystem.components.core.LvSemantic
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.common.util.TimeFormatUtils
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.features.reminders.R
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderTimeConfigSection(
    time: LocalTime,
    onTimeChange: (LocalTime) -> Unit,
    modifier: Modifier = Modifier,
    fixedTimes: List<LocalTime> = listOf(time),
    onAddFixedTime: (LocalTime) -> Unit = {},
    onRemoveFixedTime: (Int) -> Unit = {},
    onUpdateFixedTime: (Int, LocalTime) -> Unit = { _, t -> onTimeChange(t) },
    is24HourFormat: Boolean = true,
) {
    var showAddTimePicker by remember { mutableStateOf(false) }
    var showEditTimePicker by remember { mutableStateOf(false) }
    var editingTimeIndex by remember { mutableIntStateOf(0) }
    var editingInitialTime by remember { mutableStateOf(time) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.small)
    ) {
        Text(
            text = stringResource(R.string.section_fixed_times_title),
            style = LiteverTheme.typography.labelMedium,
            color = LiteverTheme.colors.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.extraSmall),
            verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.extraSmall)
        ) {
            val timesList = fixedTimes.ifEmpty { listOf(time) }
            timesList.forEachIndexed { index, targetTime ->
                val label = TimeFormatUtils.formatTime(targetTime, is24HourFormat)

                InputChip(
                    selected = true,
                    onClick = {
                        editingTimeIndex = index
                        editingInitialTime = targetTime
                        showEditTimePicker = true
                    },
                    label = { Text(label, style = LiteverTheme.typography.labelMedium) },
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
                        selectedContainerColor = LiteverTheme.colors.secondary,
                        selectedLabelColor = LiteverTheme.colors.onSecondary,
                        selectedTrailingIconColor = LiteverTheme.colors.onSecondary,
                        containerColor = LiteverTheme.colors.surfaceVariant,
                        labelColor = LiteverTheme.colors.onSurfaceVariant,
                        leadingIconColor = LiteverTheme.colors.onSurfaceVariant
                    ),
                    modifier = Modifier.height(32.dp)
                )
            }

            InputChip(
                selected = false,
                onClick = { showAddTimePicker = true },
                label = {
                    Text(
                        stringResource(R.string.add_fixed_time),
                        style = LiteverTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = ReMindIcons.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                colors = InputChipDefaults.inputChipColors(
                    containerColor = LiteverTheme.colors.surfaceVariant,
                    labelColor = LiteverTheme.colors.onSurfaceVariant
                ),
                modifier = Modifier.height(32.dp)
            )
        }
    }

    // Add TimePicker Dialog
    if (showAddTimePicker) {
        val addTimeState = rememberTimePickerState(
            initialHour = 12,
            initialMinute = 0,
            is24Hour = is24HourFormat
        )
        TimePickerDialog(
            title = {
                Text(
                    stringResource(R.string.set_reminder_time),
                    style = LiteverTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(
                        bottom = LiteverTheme.spacing.medium,
                    )
                )
            },
            onDismissRequest = { showAddTimePicker = false },
            confirmButton = {
                LvButton(
                    onClick = {
                        onAddFixedTime(LocalTime.of(addTimeState.hour, addTimeState.minute))
                        showAddTimePicker = false
                    },
                    semantic = LvSemantic.Primary
                ) {
                    Text(stringResource(R.string.action_save))
                }
            },
            dismissButton = {
                LvButton(
                    onClick = { showAddTimePicker = false },
                    type = LvButtonType.Outlined,
                    semantic = LvSemantic.Secondary,
                    modifier = Modifier.padding(end = LiteverTheme.spacing.medium)
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
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
        TimePickerDialog(
            title = {
                Text(
                    stringResource(R.string.set_reminder_time),
                    style = LiteverTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(
                        bottom = LiteverTheme.spacing.medium,
                    )
                )
            },
            onDismissRequest = { showEditTimePicker = false },
            confirmButton = {
                LvButton(
                    onClick = {
                        onUpdateFixedTime(editingTimeIndex, LocalTime.of(editTimeState.hour, editTimeState.minute))
                        showEditTimePicker = false
                    },
                    semantic = LvSemantic.Primary
                ) {
                    Text(stringResource(R.string.action_save))
                }
            },
            dismissButton = {
                LvButton(
                    onClick = { showEditTimePicker = false },
                    type = LvButtonType.Outlined,
                    semantic = LvSemantic.Secondary,
                    modifier = Modifier.padding(end = LiteverTheme.spacing.medium)
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
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
            fixedTimes = listOf(LocalTime.of(9, 0), LocalTime.of(14, 30)),
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
            fixedTimes = listOf(LocalTime.of(9, 0), LocalTime.of(14, 30)),
            is24HourFormat = true
        )
    }
}
