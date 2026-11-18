package vn.io.litever.remind.features.reminders.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
import vn.io.litever.remind.core.designsystem.components.PermissionWarningBanner
import vn.io.litever.remind.features.reminders.ui.components.ReminderPuzzleDialog
import vn.io.litever.remind.features.reminders.viewmodel.ReminderListUiState
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
        onToggleReminder = viewModel::toggleReminder,
        onReminderClick = onReminderClick,
        onEditReminderClick = onEditReminderClick,
        onAddReminderClick = onAddReminderClick,
        onNavigateToPermissions = onNavigateToPermissions,
        onDismissPermissionWarning = viewModel::dismissPermissionWarning,
        onDeleteReminder = viewModel::deleteReminder,
        onDuplicateReminder = viewModel::duplicateReminder,
        onDeleteDisabledReminders = viewModel::deleteDisabledReminders,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderListScreen(
    uiState: ReminderListUiState,
    snackbarHostState: SnackbarHostState,
    onToggleReminder: (Reminder) -> Unit,
    onReminderClick: (Reminder) -> Unit,
    onEditReminderClick: (Long) -> Unit = {},
    onAddReminderClick: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onDismissPermissionWarning: () -> Unit = {},
    onDeleteReminder: (Reminder) -> Unit,
    onDuplicateReminder: (Reminder) -> Unit,
    onDeleteDisabledReminders: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showTopMenu by remember { mutableStateOf(false) }
    var selectedReminderForAction by remember { mutableStateOf<Reminder?>(null) }
    var reminderForSnoozePuzzle by remember { mutableStateOf<Reminder?>(null) }
    val hasReminders = uiState.reminders.isNotEmpty()

    LiteverScaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            LiteverTopAppBar(
                title = stringResource(R.string.reminders_title),
                actions = {
                    if (hasReminders) {
                        Box {
                            LiteverIconButton(onClick = { showTopMenu = true }) {
                                Icon(
                                    imageVector = ReMindIcons.MoreVert,
                                    contentDescription = stringResource(R.string.action_more),
                                    tint = LiteverTheme.colors.onSurface
                                )
                            }
                            DropdownMenu(
                                expanded = showTopMenu,
                                onDismissRequest = { showTopMenu = false }
                            ) {
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
                }
            )
        },
        floatingActionButton = {
            if (hasReminders) {
                LiteverFloatingActionButton(
                    onClick = onAddReminderClick
                ) {
                    Icon(
                        imageVector = ReMindIcons.Add,
                        contentDescription = stringResource(R.string.add_reminder_title)
                    )
                }
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
                if (uiState.isCriticalPermissionWarning) {
                    PermissionWarningBanner(
                        title = stringResource(R.string.reminder_permission_warning_banner_title),
                        message = stringResource(R.string.reminder_permission_warning_banner_desc),
                        isCritical = true,
                        onClick = onNavigateToPermissions,
                        modifier = Modifier.padding(
                            horizontal = LiteverTheme.spacing.medium,
                            vertical = LiteverTheme.spacing.small
                        )
                    )
                } else if (!uiState.isPermissionWarningDismissed) {
                    PermissionWarningBanner(
                        title = stringResource(R.string.reminder_permission_warning_banner_non_critical_title),
                        message = stringResource(R.string.reminder_permission_warning_banner_non_critical_desc),
                        isCritical = false,
                        onClick = onNavigateToPermissions,
                        dismissLabel = stringResource(R.string.permission_warning_banner_remind_later),
                        onDismiss = onDismissPermissionWarning,
                        modifier = Modifier.padding(
                            horizontal = LiteverTheme.spacing.medium,
                            vertical = LiteverTheme.spacing.small
                        )
                    )
                }
            }

            // Content List or Empty State
            if (uiState.isLoading) {
                // Show nothing while loading to avoid empty state flash
                Box(modifier = Modifier.weight(1f))
            } else if (uiState.reminders.isEmpty()) {
                ReminderEmptyState(
                    onAddReminderClick = onAddReminderClick,
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.small),
                    contentPadding = PaddingValues(LiteverTheme.spacing.medium).let {
                        PaddingValues(
                            start = it.calculateStartPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
                            top = it.calculateTopPadding(),
                            end = it.calculateEndPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
                            bottom = it.calculateBottomPadding() + 80.dp
                        )
                    }
                ) {
                    items(
                        items = uiState.reminders,
                        key = { it.id },
                        contentType = { "reminder_card" }
                    ) { reminder ->
                        ReminderCard(
                            reminder = reminder,
                            is24HourFormat = uiState.is24HourFormat,
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
            onToggleEnabledClick = {
                if (reminder.isEnabled && reminder.snoozeNextTriggerTime != null) {
                    reminderForSnoozePuzzle = reminder
                } else {
                    onToggleReminder(reminder)
                }
            },
            onDeleteClick = { onDeleteReminder(reminder) }
        )
    }

    // Snooze Cancel Confirmation Puzzle Dialog
    reminderForSnoozePuzzle?.let { snoozedReminder ->
        ReminderPuzzleDialog(
            onDismissRequest = { reminderForSnoozePuzzle = null },
            onConfirmed = {
                onToggleReminder(snoozedReminder)
                reminderForSnoozePuzzle = null
            }
        )
    }
}
