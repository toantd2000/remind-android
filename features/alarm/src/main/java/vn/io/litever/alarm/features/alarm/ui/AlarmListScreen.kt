package vn.io.litever.alarm.features.alarm.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import vn.io.litever.alarm.core.designsystem.components.AlarmFloatingActionButton
import vn.io.litever.alarm.core.designsystem.components.AlarmScaffold
import vn.io.litever.alarm.core.model.Alarm
import vn.io.litever.alarm.features.alarm.R
import vn.io.litever.alarm.features.alarm.ui.components.AlarmCard
import vn.io.litever.alarm.features.alarm.viewmodel.AlarmListViewModel
import vn.io.litever.alarm.features.alarm.viewmodel.NextAlarmUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmListRoute(
    onAddAlarmClick: () -> Unit,
    onAlarmClick: (Alarm) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AlarmListViewModel = hiltViewModel()
) {
    val alarms by viewModel.alarms.collectAsState()
    val nextAlarmTime by viewModel.nextAlarmTime.collectAsState()
    
    AlarmListScreen(
        alarms = alarms,
        nextAlarmState = nextAlarmTime,
        onToggleAlarm = viewModel::toggleAlarm,
        onDeleteDisabledAlarms = viewModel::deleteDisabledAlarms,
        onAddAlarmClick = onAddAlarmClick,
        onAlarmClick = onAlarmClick,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmListScreen(
    alarms: List<Alarm>,
    nextAlarmState: NextAlarmUiState,
    onToggleAlarm: (Alarm) -> Unit,
    onDeleteDisabledAlarms: () -> Unit,
    onAddAlarmClick: () -> Unit,
    onAlarmClick: (Alarm) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    AlarmScaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alarms") },
                actions = {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.delete_disabled_alarms)) },
                            onClick = {
                                onDeleteDisabledAlarms()
                                showMenu = false
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            AlarmFloatingActionButton(onClick = onAddAlarmClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Alarm")
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Next Alarm Text
            Text(
                text = formatNextAlarmText(nextAlarmState),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )

            if (alarms.isEmpty()) {
                EmptyState(modifier = Modifier.weight(1f))
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp).let { 
                        PaddingValues(
                            start = it.calculateStartPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
                            top = it.calculateTopPadding(),
                            end = it.calculateEndPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
                            bottom = it.calculateBottomPadding() + 80.dp
                        )
                    }
                ) {
                    items(alarms, key = { it.id }) { alarm ->
                        AlarmCard(
                            alarm = alarm,
                            onToggle = { onToggleAlarm(alarm) },
                            onClick = { onAlarmClick(alarm) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.no_alarms),
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = stringResource(R.string.empty_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
@Composable
private fun formatNextAlarmText(state: NextAlarmUiState): String {
    return when (state) {
        NextAlarmUiState.AllOff -> stringResource(R.string.all_alarms_off)
        is NextAlarmUiState.Remaining -> {
            val content = when {
                state.days > 0 -> stringResource(R.string.days_hours, state.days, state.hours)
                state.hours > 0 -> stringResource(R.string.hours_minutes, state.hours, state.minutes)
                state.minutes > 0 -> stringResource(R.string.hours_minutes, 0, state.minutes) // This will use minutes part
                else -> stringResource(R.string.less_than_one_minute)
            }
            
            // Special handling for only minutes if hours == 0 but string expects %1$d hours %2$d mins
            val finalContent = if (state.days == 0L && state.hours == 0L && state.minutes > 0) {
                "${state.minutes} ${stringResource(R.string.hours_minutes, 0, 0).split(" ").last()}" // heuristic
                // Actually, let's just make it simple if it's just minutes
                "${state.minutes}m" 
                // Wait, user request was specific for "a giờ b phút", so 0 giờ b phút is fine or just reuse strings.
            } else content

            val timeDescription = when {
                state.days > 0 -> stringResource(R.string.days_hours, state.days, state.hours)
                state.hours > 0 -> stringResource(R.string.hours_minutes, state.hours, state.minutes)
                state.minutes > 0 -> stringResource(R.string.just_minutes, state.minutes)
                else -> stringResource(R.string.less_than_one_minute)
            }
            
            stringResource(R.string.next_alarm_prefix, timeDescription)
        }
    }
}
