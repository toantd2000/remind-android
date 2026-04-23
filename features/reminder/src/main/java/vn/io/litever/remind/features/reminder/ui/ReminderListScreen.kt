package vn.io.litever.remind.features.reminder.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.rounded.*
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
    var showTopMenu by remember { mutableStateOf(false) }
    var selectedReminderForMenu by remember { mutableStateOf<Reminder?>(null) }

    val actionMoreDescription = stringResource(R.string.action_more)
    val deleteDisabledRemindersText = stringResource(R.string.delete_disabled_reminders)
    val actionAddDescription = stringResource(R.string.action_add)

    ReMindScaffold(
        topBar = {
            MainReMindTopAppBar(
                actions = {
                    IconButton(onClick = { showTopMenu = !showTopMenu }) {
                        Icon(Icons.Rounded.MoreVert, contentDescription = actionMoreDescription)
                    }
                    DropdownMenu(
                        expanded = showTopMenu,
                        onDismissRequest = { showTopMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(deleteDisabledRemindersText) },
                            onClick = {
                                onDeleteDisabledReminders()
                                showTopMenu = false
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
                            onMoreClick = { selectedReminderForMenu = reminder }
                        )
                    }
                }
            }
        }
    }

    // Action Bottom Sheet
    if (selectedReminderForMenu != null) {
        ReminderActionBottomSheet(
            reminder = selectedReminderForMenu!!,
            onDismiss = { selectedReminderForMenu = null },
            onDelete = {
                onDeleteReminder(selectedReminderForMenu!!)
                selectedReminderForMenu = null
            },
            onDuplicate = {
                onDuplicateReminder(selectedReminderForMenu!!)
                selectedReminderForMenu = null
            },
            onSkipOnce = {
                onSkipOnce(selectedReminderForMenu!!)
                selectedReminderForMenu = null
            },
            onCancelSkip = {
                onCancelSkip(selectedReminderForMenu!!)
                selectedReminderForMenu = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderActionBottomSheet(
    reminder: Reminder,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onSkipOnce: () -> Unit,
    onCancelSkip: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            if (reminder.isEnabled) {
                val isSkipped = reminder.skippedAt != null
                ListItem(
                    headlineContent = { 
                        Text(stringResource(if (isSkipped) R.string.action_cancel_skip else R.string.action_skip_once)) 
                    },
                    leadingContent = { 
                        Icon(
                            if (isSkipped) Icons.Rounded.NotificationsPaused else Icons.Rounded.SkipNext, 
                            contentDescription = null
                        ) 
                    },
                    modifier = Modifier.clickable { if (isSkipped) onCancelSkip() else onSkipOnce() }
                )
            }
            
            ListItem(
                headlineContent = { Text(stringResource(R.string.action_duplicate)) },
                leadingContent = { Icon(Icons.Rounded.ContentCopy, contentDescription = null) },
                modifier = Modifier.clickable { onDuplicate() }
            )
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
            
            ListItem(
                headlineContent = { 
                    Text(
                        stringResource(R.string.action_delete), 
                        color = MaterialTheme.colorScheme.error
                    ) 
                },
                leadingContent = { 
                    Icon(
                        Icons.Rounded.Delete, 
                        contentDescription = null, 
                        tint = MaterialTheme.colorScheme.error
                    ) 
                },
                modifier = Modifier.clickable { onDelete() }
            )
        }
    }
}


@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
            shape = CircleShape,
            modifier = Modifier.size(100.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.Notifications,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = stringResource(R.string.no_reminders),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = stringResource(R.string.empty_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}


@androidx.compose.ui.tooling.preview.Preview(showBackground = true, device = androidx.compose.ui.tooling.preview.Devices.PIXEL_7)
@Composable
fun EmptyStatePreview() {
    vn.io.litever.remind.core.designsystem.theme.ReMindTheme {
        EmptyState()
    }
}

