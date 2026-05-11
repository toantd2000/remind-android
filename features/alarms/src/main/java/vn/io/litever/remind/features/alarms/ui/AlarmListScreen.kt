package vn.io.litever.remind.features.alarms.ui

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
import androidx.compose.runtime.CompositionLocalProvider
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
import vn.io.litever.remind.core.model.Alarm
import vn.io.litever.remind.features.alarms.R
import vn.io.litever.remind.features.alarms.ui.components.NextAlarmHeader
import vn.io.litever.remind.features.alarms.ui.components.PermissionWarningBanner
import vn.io.litever.remind.features.alarms.ui.components.AlarmCard
import vn.io.litever.remind.features.alarms.ui.state.NextAlarmUiState
import vn.io.litever.remind.features.alarms.viewmodel.AlarmListViewModel

@Suppress("LocalContextGetResourceValueCall")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmListRoute(
    onAddAlarmClick: () -> Unit,
    onAlarmClick: (Alarm) -> Unit,
    onNavigateToPreview: (Long) -> Unit,
    onNavigateToPermissions: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AlarmListViewModel = hiltViewModel()
) {
    val alarms by viewModel.alarms.collectAsState()
    val nextAlarmTime by viewModel.nextAlarmTime.collectAsState()
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

    val resources = LocalContext.current.resources
    // Handle UI messages (Snackbars)
    LaunchedEffect(viewModel.uiMessage) {
        viewModel.uiMessage.collectLatest { messageRes ->
            snackbarHostState.showSnackbar(resources.getString(messageRes))
        }
    }

    // Handle Undo events
    LaunchedEffect(viewModel.undoEvent) {
        viewModel.undoEvent.collect { type ->
            val message = when (type) {
                AlarmListViewModel.UndoType.SINGLE -> resources.getString(R.string.alarm_deleted)
                AlarmListViewModel.UndoType.MULTIPLE -> resources.getString(R.string.disabled_alarms_deleted)
            }
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = resources.getString(R.string.undo),
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoDelete()
            }
        }
    }

    AlarmListScreen(
        alarms = alarms,
        is24HourFormat = is24HourFormat,
        nextAlarmState = nextAlarmTime,
        hasCriticalPermissions = hasCriticalPermissions,
        snackbarHostState = snackbarHostState,
        onToggleAlarm = viewModel::toggleAlarm,
        onDeleteAlarm = viewModel::deleteAlarm,
        onDuplicateAlarm = viewModel::duplicateAlarm,
        onSkipOnce = viewModel::skipNextOccurrence,
        onCancelSkip = viewModel::cancelSkipOccurrence,
        onDeleteDisabledAlarms = viewModel::deleteDisabledAlarms,
        onAddAlarmClick = onAddAlarmClick,
        onAlarmClick = onAlarmClick,
        onPreviewClick = { alarm -> onNavigateToPreview(alarm.id) },
        onNavigateToPermissions = onNavigateToPermissions,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmListScreen(
    alarms: List<Alarm>?,
    is24HourFormat: Boolean,
    nextAlarmState: NextAlarmUiState,
    hasCriticalPermissions: Boolean,
    snackbarHostState: SnackbarHostState,
    onToggleAlarm: (Alarm) -> Unit,
    onDeleteAlarm: (Alarm) -> Unit,
    onDuplicateAlarm: (Alarm) -> Unit,
    onSkipOnce: (Alarm) -> Unit,
    onCancelSkip: (Alarm) -> Unit,
    onDeleteDisabledAlarms: () -> Unit,
    onAddAlarmClick: () -> Unit,
    onAlarmClick: (Alarm) -> Unit,
    onPreviewClick: (Alarm) -> Unit,
    onNavigateToPermissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showTopMenu by remember { mutableStateOf(false) }
    var selectedAlarmForMenu by remember { mutableStateOf<Alarm?>(null) }

    val actionMoreDescription = stringResource(R.string.action_more)
    val deleteDisabledAlarmsText = stringResource(R.string.delete_disabled_alarms)
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
                            text = { Text(deleteDisabledAlarmsText) },
                            onClick = {
                                onDeleteDisabledAlarms()
                                showTopMenu = false
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ReMindFloatingActionButton(onClick = onAddAlarmClick) {
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

            if (alarms == null) {
                // Show nothing while loading to avoid empty state flash
                Box(modifier = Modifier.weight(1f))
            } else {
                if (alarms.isNotEmpty()) {
                    // Shared Next Alarm Header
                    NextAlarmHeader(state = nextAlarmState)
                }

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
                                is24HourFormat = is24HourFormat,
                                onToggle = { onToggleAlarm(alarm) },
                                onClick = { onAlarmClick(alarm) },
                                onMoreClick = { selectedAlarmForMenu = alarm }
                            )
                        }
                    }
                }
            }
        }
    }

    // Action Bottom Sheet
    if (selectedAlarmForMenu != null) {
        AlarmActionBottomSheet(
            alarm = selectedAlarmForMenu!!,
            onDismiss = { selectedAlarmForMenu = null },
            onDelete = {
                onDeleteAlarm(selectedAlarmForMenu!!)
                selectedAlarmForMenu = null
            },
            onDuplicate = {
                onDuplicateAlarm(selectedAlarmForMenu!!)
                selectedAlarmForMenu = null
            },
            onSkipOnce = {
                onSkipOnce(selectedAlarmForMenu!!)
                selectedAlarmForMenu = null
            },
            onCancelSkip = {
                onCancelSkip(selectedAlarmForMenu!!)
                selectedAlarmForMenu = null
            },
            onPreview = {
                onPreviewClick(selectedAlarmForMenu!!)
                selectedAlarmForMenu = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlarmActionBottomSheet(
    alarm: Alarm,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onSkipOnce: () -> Unit,
    onCancelSkip: () -> Unit,
    onPreview: () -> Unit
) {
    val context = LocalContext.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        CompositionLocalProvider(LocalContext provides context) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
            if (alarm.isEnabled) {
                val isSkipped = alarm.skippedAt != null
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
                headlineContent = { Text(stringResource(R.string.action_preview)) },
                leadingContent = { Icon(Icons.Rounded.PlayArrow, contentDescription = null) },
                modifier = Modifier.clickable { onPreview() }
            )
            
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
            text = stringResource(R.string.no_alarms),
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











