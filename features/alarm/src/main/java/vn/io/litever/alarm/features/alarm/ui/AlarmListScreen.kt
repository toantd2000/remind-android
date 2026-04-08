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
import vn.io.litever.alarm.core.designsystem.components.*
import vn.io.litever.alarm.core.model.Alarm
import vn.io.litever.alarm.features.alarm.R
import vn.io.litever.alarm.features.alarm.ui.components.AlarmCard
import vn.io.litever.alarm.features.alarm.ui.components.NextAlarmHeader
import vn.io.litever.alarm.features.alarm.ui.state.NextAlarmUiState
import vn.io.litever.alarm.features.alarm.viewmodel.AlarmListViewModel

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
            AlarmTopAppBar(
                title = stringResource(R.string.alarms_title),
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
            // Shared Next Alarm Header
            NextAlarmHeader(state = nextAlarmState)

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
