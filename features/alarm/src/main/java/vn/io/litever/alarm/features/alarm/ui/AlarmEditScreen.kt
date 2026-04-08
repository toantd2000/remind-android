package vn.io.litever.alarm.features.alarm.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import vn.io.litever.alarm.core.designsystem.components.AlarmScaffold
import vn.io.litever.alarm.core.designsystem.components.AlarmTopAppBar
import vn.io.litever.alarm.core.model.DayOfWeek
import vn.io.litever.alarm.features.alarm.R
import vn.io.litever.alarm.features.alarm.ui.components.NextAlarmHeader
import vn.io.litever.alarm.features.alarm.ui.components.getRepeatSummaryText
import vn.io.litever.alarm.features.alarm.viewmodel.AlarmEditViewModel
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmEditRoute(
    alarmId: Long,
    onBackClick: () -> Unit,
    viewModel: AlarmEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val nextAlarmState by viewModel.nextAlarmState.collectAsState()

    LaunchedEffect(alarmId) {
        viewModel.loadAlarm(alarmId)
    }

    AlarmEditScreen(
        uiState = uiState,
        nextAlarmState = nextAlarmState,
        onBackClick = onBackClick,
        onSaveClick = {
            viewModel.saveAlarm(onBackClick)
        },
        onTimeChange = viewModel::updateTime,
        onLabelChange = viewModel::updateLabel,
        onRepeatDayToggle = viewModel::toggleRepeatDay,
        onVibrationToggle = viewModel::updateVibration,
        onRingtoneClick = { /* Handle Ringtone Picker */ }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmEditScreen(
    uiState: vn.io.litever.alarm.features.alarm.viewmodel.AlarmEditUiState,
    nextAlarmState: vn.io.litever.alarm.features.alarm.ui.state.NextAlarmUiState,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    onTimeChange: (LocalTime) -> Unit,
    onLabelChange: (String) -> Unit,
    onRepeatDayToggle: (DayOfWeek) -> Unit,
    onVibrationToggle: (Boolean) -> Unit,
    onRingtoneClick: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = uiState.time.hour,
        initialMinute = uiState.time.minute,
        is24Hour = true
    )

    // Sync state time when picker changes
    LaunchedEffect(timePickerState.hour, timePickerState.minute) {
        onTimeChange(LocalTime.of(timePickerState.hour, timePickerState.minute))
    }

    AlarmScaffold(
        topBar = {
            AlarmTopAppBar(
                title = stringResource(if (uiState.id == 0L) R.string.add_alarm_title else R.string.edit_alarm_title),
                onBackClick = onBackClick
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp, top = 8.dp)
            ) {
                item {
                    NextAlarmHeader(state = nextAlarmState)
                }

                item {
                    // Group 1: Time Selector
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            TimePicker(state = timePickerState)
                        }
                    }
                }

                item {
                    // Group 2: Repeat Selector (Separated)
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        RepeatDaySelector(
                            selectedDays = uiState.repeatDays,
                            time = uiState.time,
                            onDayToggle = onRepeatDayToggle,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                item {
                    // Group 3: Label (Separated)
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        OutlinedTextField(
                            value = uiState.label,
                            onValueChange = onLabelChange,
                            placeholder = { Text(stringResource(R.string.alarm_label_placeholder)) },
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            singleLine = true,
                            shape = MaterialTheme.shapes.large,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )
                    }
                }

                item {
                    // Group 4: Alert Settings
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            ListItem(
                                headlineContent = { Text(stringResource(R.string.vibration)) },
                                trailingContent = {
                                    Switch(
                                        checked = uiState.vibrationEnabled,
                                        onCheckedChange = onVibrationToggle
                                    )
                                },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                            )
                            
                            ListItem(
                                headlineContent = { Text(stringResource(R.string.sound)) },
                                supportingContent = { Text(uiState.ringtoneUri ?: "Default") },
                                modifier = Modifier.clickable { onRingtoneClick() },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                            )
                        }
                    }
                }
            }

            // Fixed Save Button at the Bottom
            Surface(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = onSaveClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .height(56.dp),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Text(stringResource(R.string.save), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
fun RepeatDaySelector(
    selectedDays: List<DayOfWeek>,
    time: java.time.LocalTime,
    onDayToggle: (DayOfWeek) -> Unit,
    modifier: Modifier = Modifier
) {
    val locale = java.util.Locale.getDefault()
    val allDays = DayOfWeek.entries
    
    val summary = getRepeatSummaryText(selectedDays, time)
    val finalSummary = if (selectedDays.isNotEmpty() && selectedDays.size < 7) {
        val prefix = if (locale.language == "vi") "Mỗi " else "Every "
        prefix + summary
    } else {
        summary
    }

    Column(modifier = modifier) {
        Text(
            text = finalSummary,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            allDays.forEach { day ->
                val javaDay = java.time.DayOfWeek.of(day.toJavaDayValue())
                val label = javaDay.getDisplayName(java.time.format.TextStyle.SHORT, locale)
                
                val isSelected = selectedDays.contains(day)
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer 
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                        .clickable { onDayToggle(day) }
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = if (label.length > 2) 10.sp else 11.sp
                        ),
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer 
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

// Helper mapping
private fun DayOfWeek.toJavaDayValue(): Int = when (this) {
    DayOfWeek.MONDAY -> 1
    DayOfWeek.TUESDAY -> 2
    DayOfWeek.WEDNESDAY -> 3
    DayOfWeek.THURSDAY -> 4
    DayOfWeek.FRIDAY -> 5
    DayOfWeek.SATURDAY -> 6
    DayOfWeek.SUNDAY -> 7
}
