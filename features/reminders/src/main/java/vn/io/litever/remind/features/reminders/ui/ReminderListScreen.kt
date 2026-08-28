package vn.io.litever.remind.features.reminders.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.flow.collectLatest
import vn.io.litever.designsystem.components.LiteverFloatingActionButton
import vn.io.litever.designsystem.components.LiteverIconButton
import vn.io.litever.designsystem.components.LiteverScaffold
import vn.io.litever.designsystem.components.LiteverSnackbarHost
import vn.io.litever.designsystem.components.LiteverTopAppBar
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.features.reminders.R
import vn.io.litever.remind.features.reminders.ui.components.ReminderActionBottomSheet
import vn.io.litever.remind.features.reminders.ui.components.ReminderCard
import vn.io.litever.remind.features.reminders.ui.components.ReminderEmptyState
import vn.io.litever.remind.features.reminders.ui.components.ReminderFilterChips
import vn.io.litever.remind.features.reminders.ui.components.ReminderPermissionWarningBanner
import vn.io.litever.remind.features.reminders.viewmodel.ReminderListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderListRoute(
    onAddReminderClick: () -> Unit,
    onReminderClick: (Reminder) -> Unit,
    onEditReminderClick: (Long) -> Unit,
    onNavigateToPermissions: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReminderListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

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

    LaunchedEffect(viewModel) {
        viewModel.uiMessage.collectLatest { messageResId ->
            val message = context.getString(messageResId)
            val actionLabel = if (messageResId == R.string.reminder_deleted) {
                context.getString(R.string.undo)
            } else null

            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = actionLabel,
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed && messageResId == R.string.reminder_deleted) {
                viewModel.undoDelete()
            }
        }
    }

    ReminderListScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onFilterSelected = viewModel::setFilter,
        onToggleReminder = viewModel::toggleReminder,
        onReminderClick = onReminderClick,
        onEditReminderClick = onEditReminderClick,
        onAddReminderClick = onAddReminderClick,
        onNavigateToPermissions = onNavigateToPermissions,
        onDeleteReminder = viewModel::deleteReminder,
        onDuplicateReminder = viewModel::duplicateReminder,
        onToggleCompleted = viewModel::toggleCompleted,
        onDeleteCompletedReminders = viewModel::deleteCompletedReminders,
        onDeleteDisabledReminders = viewModel::deleteDisabledReminders,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderListScreen(
    uiState: vn.io.litever.remind.features.reminders.viewmodel.ReminderListUiState,
    snackbarHostState: SnackbarHostState,
    onFilterSelected: (vn.io.litever.remind.features.reminders.ui.components.ReminderFilterType) -> Unit,
    onToggleReminder: (Reminder) -> Unit,
    onReminderClick: (Reminder) -> Unit,
    onEditReminderClick: (Long) -> Unit = {},
    onAddReminderClick: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onDeleteReminder: (Reminder) -> Unit,
    onDuplicateReminder: (Reminder) -> Unit,
    onToggleCompleted: (Reminder) -> Unit,
    onDeleteCompletedReminders: () -> Unit,
    onDeleteDisabledReminders: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showTopMenu by remember { mutableStateOf(false) }
    var selectedReminderForAction by remember { mutableStateOf<Reminder?>(null) }
    var reminderForSnoozePuzzle by remember { mutableStateOf<Reminder?>(null) }

    LiteverScaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            LiteverTopAppBar(
                title = stringResource(R.string.reminders_title),
                actions = {
                    Box {
                        LiteverIconButton(onClick = { showTopMenu = true }) {
                            Icon(
                                imageVector = Icons.Rounded.MoreVert,
                                contentDescription = stringResource(R.string.action_more),
                                tint = LiteverTheme.colors.onSurface
                            )
                        }
                        DropdownMenu(
                            expanded = showTopMenu,
                            onDismissRequest = { showTopMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.delete_completed_reminders)) },
                                onClick = {
                                    showTopMenu = false
                                    onDeleteCompletedReminders()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.delete_disabled_reminders)) },
                                onClick = {
                                    showTopMenu = false
                                    onDeleteDisabledReminders()
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            LiteverFloatingActionButton(
                onClick = onAddReminderClick
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = stringResource(R.string.add_reminder_title)
                )
            }
        },
        snackbarHost = {
            LiteverSnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Permission Warning Banner
            if (uiState.hasPermissionWarning) {
                ReminderPermissionWarningBanner(
                    onClick = onNavigateToPermissions
                )
            }

            // Filter Chips
            ReminderFilterChips(
                selectedFilter = uiState.selectedFilter,
                onFilterSelected = onFilterSelected,
                counts = uiState.filterCounts
            )

            // Content List or Empty State
            if (uiState.filteredReminders.isEmpty()) {
                ReminderEmptyState(
                    selectedFilter = uiState.selectedFilter,
                    modifier = Modifier.weight(1f)
                )
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
                    items(
                        items = uiState.filteredReminders,
                        key = { it.id }
                    ) { reminder ->
                        ReminderCard(
                            reminder = reminder,
                            is24HourFormat = uiState.is24HourFormat,
                            onToggle = {
                                if (reminder.snoozeNextTriggerTime != null) {
                                    reminderForSnoozePuzzle = reminder
                                } else {
                                    onToggleReminder(reminder)
                                }
                            },
                            onClick = { onReminderClick(reminder) },
                            onMoreClick = { selectedReminderForAction = reminder }
                        )
                    }
                }
            }
        }
    }

    // Action Bottom Sheet
    selectedReminderForAction?.let { reminder ->
        ReminderActionBottomSheet(
            reminder = reminder,
            onDismissRequest = { selectedReminderForAction = null },
            onEditClick = { onEditReminderClick(reminder.id) },
            onDuplicateClick = { onDuplicateReminder(reminder) },
            onToggleCompletedClick = { onToggleCompleted(reminder) },
            onDeleteClick = { onDeleteReminder(reminder) }
        )
    }

    // Snooze Cancel Confirmation Puzzle Dialog
    reminderForSnoozePuzzle?.let { snoozedReminder ->
        vn.io.litever.remind.features.reminders.ui.components.ReminderPuzzleDialog(
            onDismissRequest = { reminderForSnoozePuzzle = null },
            onConfirmed = {
                onToggleReminder(snoozedReminder)
                reminderForSnoozePuzzle = null
            }
        )
    }
}
