package vn.io.litever.alarm.features.alarm.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import vn.io.litever.alarm.core.designsystem.components.AlarmFloatingActionButton
import vn.io.litever.alarm.core.designsystem.components.AlarmScaffold
import vn.io.litever.alarm.core.designsystem.components.AlarmSwitch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import vn.io.litever.alarm.core.model.Alarm
import vn.io.litever.alarm.features.alarm.viewmodel.AlarmListViewModel
import java.time.format.DateTimeFormatter

@Composable
fun AlarmListRoute(
    onAddAlarmClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AlarmListViewModel = hiltViewModel()
) {
    val alarms by viewModel.alarms.collectAsState()
    
    AlarmListScreen(
        alarms = alarms,
        onToggleAlarm = viewModel::toggleAlarm,
        onAddAlarmClick = onAddAlarmClick,
        modifier = modifier
    )
}

@Composable
fun AlarmListScreen(
    alarms: List<Alarm>,
    onToggleAlarm: (Alarm) -> Unit,
    onAddAlarmClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlarmScaffold(
        floatingActionButton = {
            AlarmFloatingActionButton(onClick = onAddAlarmClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Alarm")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(alarms, key = { it.id }) { alarm ->
                AlarmItem(
                    alarm = alarm,
                    onToggle = { onToggleAlarm(alarm) }
                )
            }
        }
    }
}

@Composable
fun AlarmItem(
    alarm: Alarm,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = alarm.time.format(timeFormatter),
                style = MaterialTheme.typography.headlineMedium
            )
            if (alarm.label.isNotEmpty()) {
                Text(
                    text = alarm.label,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        AlarmSwitch(
            checked = alarm.isEnabled,
            onCheckedChange = { onToggle() }
        )
    }
}
