package vn.io.litever.alarm.features.alarm.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import vn.io.litever.alarm.core.designsystem.components.AlarmScaffold
import vn.io.litever.alarm.core.model.DayOfWeek
import vn.io.litever.alarm.features.alarm.viewmodel.AlarmEditViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmEditRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AlarmEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    AlarmEditScreen(
        time = uiState.time,
        label = uiState.label,
        repeatDays = uiState.repeatDays,
        onTimeChange = viewModel::updateTime,
        onLabelChange = viewModel::updateLabel,
        onDayToggle = viewModel::toggleRepeatDay,
        onSave = { viewModel.saveAlarm(onSuccess = onNavigateBack) },
        onCancel = onNavigateBack,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmEditScreen(
    time: java.time.LocalTime,
    label: String,
    repeatDays: List<DayOfWeek>,
    onTimeChange: (Int, Int) -> Unit,
    onLabelChange: (String) -> Unit,
    onDayToggle: (DayOfWeek) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timePickerState = rememberTimePickerState(
        initialHour = time.hour,
        initialMinute = time.minute,
        is24Hour = true
    )

    AlarmScaffold(
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TimePicker(state = timePickerState)

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = label,
                onValueChange = onLabelChange,
                label = { Text("Label") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Day selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DayOfWeek.entries.forEach { day ->
                    val isSelected = repeatDays.contains(day)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onDayToggle(day) },
                        label = { Text(day.name.take(1)) }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onCancel) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { 
                    onTimeChange(timePickerState.hour, timePickerState.minute)
                    onSave() 
                }) {
                    Text("Save")
                }
            }
        }
    }
}
