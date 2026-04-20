package vn.io.litever.remind.features.reminder.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.flow.collectLatest
import vn.io.litever.remind.core.designsystem.components.*
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.features.reminder.R
import vn.io.litever.remind.features.reminder.ui.components.NextReminderHeader
import vn.io.litever.remind.features.reminder.ui.components.PermissionWarningBanner
import vn.io.litever.remind.features.reminder.ui.components.ReminderCard
import vn.io.litever.remind.features.reminder.ui.state.NextReminderUiState
import vn.io.litever.remind.features.reminder.viewmodel.ReminderListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderListRoute(
    onAddReminderClick: () -> Unit,
    onReminderClick: (Reminder) -> Unit,
    onNavigateToPermissions: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReminderListViewModel = hiltViewModel()
) {
    val reminders by viewModel.reminders.collectAsState()
    val nextReminderTime by viewModel.nextReminderTime.collectAsState()
    val is24HourFormat by viewModel.is24HourFormat.collectAsState()
    val hasCriticalPermissions by viewModel.hasCriticalPermissions.collectAsState()
    
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Refresh permissions on resume
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Handle UI messages (Snackbars)
    LaunchedEffect(viewModel.uiMessage) {
        viewModel.uiMessage.collectLatest { messageRes ->
            snackbarHostState.showSnackbar(context.getString(messageRes))
        }
    }

    // Handle Undo events
    LaunchedEffect(viewModel.undoEvent) {
        viewModel.undoEvent.collect { type ->
            val message = when (type) {
                ReminderListViewModel.UndoType.SINGLE -> context.getString(R.string.reminder_deleted)
                ReminderListViewModel.UndoType.MULTIPLE -> context.getString(R.string.disabled_reminders_deleted)
            }
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = context.getString(R.string.undo),
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoDelete()
            }
        }
    }

    ReminderListScreen(
        reminders = reminders,
        is24HourFormat = is24HourFormat,
        nextReminderState = nextReminderTime,
        hasCriticalPermissions = hasCriticalPermissions,
        snackbarHostState = snackbarHostState,
        onToggleReminder = viewModel::toggleReminder,
        onDeleteReminder = viewModel::deleteReminder,
        onDuplicateReminder = viewModel::duplicateReminder,
        onSkipOnce = viewModel::skipNextOccurrence,
        onCancelSkip = viewModel::cancelSkipOccurrence,
        onDeleteDisabledReminders = viewModel::deleteDisabledReminders,
        onAddReminderClick = onAddReminderClick,
        onReminderClick = onReminderClick,
        onNavigateToPermissions = onNavigateToPermissions,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderListScreen(
    reminders: List<Reminder>,
    is24HourFormat: Boolean,
    nextReminderState: NextReminderUiState,
    hasCriticalPermissions: Boolean,
    snackbarHostState: SnackbarHostState,
    onToggleReminder: (Reminder) -> Unit,
    onDeleteReminder: (Reminder) -> Unit,
    onDuplicateReminder: (Reminder) -> Unit,
    onSkipOnce: (Reminder) -> Unit,
    onCancelSkip: (Reminder) -> Unit,
    onDeleteDisabledReminders: () -> Unit,
    onAddReminderClick: () -> Unit,
    onReminderClick: (Reminder) -> Unit,
    onNavigateToPermissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    val actionMoreDescription = stringResource(R.string.action_more)
    val deleteDisabledRemindersText = stringResource(R.string.delete_disabled_reminders)
    val actionAddDescription = stringResource(R.string.action_add)

    ReMindScaffold(
        topBar = {
            MainReMindTopAppBar(
                actions = {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(Icons.Rounded.MoreVert, contentDescription = actionMoreDescription)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(deleteDisabledRemindersText) },
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
                Icon(Icons.Rounded.Add, contentDescription = actionAddDescription)
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (!hasCriticalPermissions) {
                PermissionWarningBanner(onClick = onNavigateToPermissions)
            }

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
                            onClick = { onReminderClick(reminder) },
                            onDelete = { onDeleteReminder(reminder) },
                            onDuplicate = { onDuplicateReminder(reminder) },
                            onSkipOnce = { onSkipOnce(reminder) },
                            onCancelSkip = { onCancelSkip(reminder) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.no_reminders),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
