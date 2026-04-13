package vn.io.litever.remind.features.reminder.ui

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
import vn.io.litever.remind.core.designsystem.components.*
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.features.reminder.R
import vn.io.litever.remind.features.reminder.ui.components.ReminderCard
import vn.io.litever.remind.features.reminder.ui.components.NextReminderHeader
import vn.io.litever.remind.features.reminder.ui.state.NextReminderUiState
import vn.io.litever.remind.features.reminder.viewmodel.ReminderListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderListRoute(
    onAddReminderClick: () -> Unit,
    onReminderClick: (Reminder) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReminderListViewModel = hiltViewModel()
) {
    val reminders by viewModel.reminders.collectAsState()
    val nextReminderTime by viewModel.nextReminderTime.collectAsState()
    val is24HourFormat by viewModel.is24HourFormat.collectAsState()
    
    ReminderListScreen(
        reminders = reminders,
        is24HourFormat = is24HourFormat,
        nextReminderState = nextReminderTime,
        onToggleReminder = viewModel::toggleReminder,
        onDeleteDisabledReminders = viewModel::deleteDisabledReminders,
        onAddReminderClick = onAddReminderClick,
        onReminderClick = onReminderClick,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderListScreen(
    reminders: List<Reminder>,
    is24HourFormat: Boolean,
    nextReminderState: NextReminderUiState,
    onToggleReminder: (Reminder) -> Unit,
    onDeleteDisabledReminders: () -> Unit,
    onAddReminderClick: () -> Unit,
    onReminderClick: (Reminder) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    ReMindScaffold(
        topBar = {
            MainReMindTopAppBar(
                actions = {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.delete_disabled_reminders)) },
                            onClick = {
                                onDeleteDisabledReminders()
                                showMenu = false
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ReMindFloatingActionButton(onClick = onAddReminderClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Reminder")
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (reminders.isNotEmpty()) {
                // Shared Next Reminder Header
                NextReminderHeader(state = nextReminderState)
            }

            if (reminders.isEmpty()) {
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
                    items(reminders, key = { it.id }) { reminder ->
                        ReminderCard(
                            reminder = reminder,
                            is24HourFormat = is24HourFormat,
                            onToggle = { onToggleReminder(reminder) },
                            onClick = { onReminderClick(reminder) }
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
            text = stringResource(R.string.no_reminders),
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = stringResource(R.string.empty_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
