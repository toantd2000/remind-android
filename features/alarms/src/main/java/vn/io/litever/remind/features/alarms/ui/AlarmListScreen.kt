package vn.io.litever.remind.features.alarms.ui

import android.app.Activity
import android.content.ContextWrapper
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.NotificationsPaused
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.flow.collectLatest
import vn.io.litever.designsystem.components.FeedbackStateType
import vn.io.litever.designsystem.components.FeedbackStateView
import vn.io.litever.designsystem.components.button.LvButton
import vn.io.litever.designsystem.components.button.LvIconButton
import vn.io.litever.designsystem.components.core.LvSemantic
import vn.io.litever.designsystem.components.snackbar.LvSnackbarHost
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.components.LvTopAppBar
import vn.io.litever.remind.core.designsystem.components.ReMindLogo
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.model.Alarm
import vn.io.litever.remind.core.model.DayOfWeek
import vn.io.litever.remind.features.alarms.R
import vn.io.litever.remind.features.alarms.ui.components.AlarmCard
import vn.io.litever.remind.features.alarms.ui.components.ExitAppDialog
import vn.io.litever.remind.features.alarms.ui.components.NextAlarmHeader
import vn.io.litever.remind.features.alarms.ui.components.PermissionWarningBanner
import vn.io.litever.remind.features.alarms.ui.state.NextAlarmUiState
import vn.io.litever.remind.features.alarms.viewmodel.AlarmListViewModel
import java.time.LocalTime

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
    val isAdFreeActive by viewModel.isAdFreeActive.collectAsState()

    LocalContext.current
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

    val context = LocalContext.current
    val resources = context.resources
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
        onRewardGranted = viewModel::disableAdsFor24Hours,
        isAdFreeActive = isAdFreeActive,
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
    onRewardGranted: () -> Unit,
    isAdFreeActive: Boolean,
    modifier: Modifier = Modifier
) {
    var showTopMenu by remember { mutableStateOf(false) }
    var selectedAlarmForMenu by remember { mutableStateOf<Alarm?>(null) }
    var showExitDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    BackHandler {
        showExitDialog = true
    }

    if (showExitDialog) {
        ExitAppDialog(
            onDismissRequest = { if (showExitDialog) showExitDialog = false },
            onConfirmExit = {
                if (showExitDialog) showExitDialog = false
                var currentContext = context
                while (currentContext is ContextWrapper) {
                    if (currentContext is Activity) {
                        currentContext.finish()
                        return@ExitAppDialog
                    }
                    currentContext = currentContext.baseContext
                }
            },
            isAdFreeActive = isAdFreeActive
        )
    }

    val actionMoreDescription = stringResource(R.string.action_more)
    val deleteDisabledAlarmsText = stringResource(R.string.delete_disabled_alarms)
    val actionAddDescription = stringResource(R.string.action_add)
    val hasAlarms = !alarms.isNullOrEmpty()

    Scaffold(
        topBar = {
            if (hasAlarms) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    LvTopAppBar(
                        title = {
                            ReMindLogo()
                        },
                        actions = {
                            LvIconButton(onClick = { showTopMenu = !showTopMenu }) {
                                Icon(Icons.Rounded.MoreVert, contentDescription = actionMoreDescription)
                            }
                            DropdownMenu(
                                expanded = showTopMenu,
                                onDismissRequest = { if (showTopMenu) showTopMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(deleteDisabledAlarmsText) },
                                    onClick = {
                                        onDeleteDisabledAlarms()
                                        if (showTopMenu) showTopMenu = false
                                    }
                                )
                            }
                        }
                    )
                    NextAlarmHeader(
                        modifier = Modifier.padding(
                            start = LiteverTheme.spacing.medium,
                            end = LiteverTheme.spacing.medium,
                            bottom = LiteverTheme.spacing.small
                        ),
                        state = nextAlarmState
                    )
                }
            }
        },
        floatingActionButton = {
            if (hasAlarms) {
                FloatingActionButton(onClick = onAddAlarmClick) {
                    Icon(Icons.Rounded.Add, contentDescription = actionAddDescription)
                }
            }
        },
        snackbarHost = { LvSnackbarHost(snackbarHostState) }
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
                if (alarms.isEmpty()) {
                    EmptyState(
                        onAddAlarmClick = onAddAlarmClick,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.small),
                        contentPadding = PaddingValues(horizontal = LiteverTheme.spacing.mediumLarge, vertical = LiteverTheme.spacing.small).let {
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
                                onMoreClick = { selectedAlarmForMenu = alarm },
                                modifier = Modifier.animateItem()
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
            onDismiss = { if (selectedAlarmForMenu != null) selectedAlarmForMenu = null },
            onDelete = {
                onDeleteAlarm(selectedAlarmForMenu!!)
                if (selectedAlarmForMenu != null) selectedAlarmForMenu = null
            },
            onDuplicate = {
                onDuplicateAlarm(selectedAlarmForMenu!!)
                if (selectedAlarmForMenu != null) selectedAlarmForMenu = null
            },
            onSkipOnce = {
                onSkipOnce(selectedAlarmForMenu!!)
                if (selectedAlarmForMenu != null) selectedAlarmForMenu = null
            },
            onCancelSkip = {
                onCancelSkip(selectedAlarmForMenu!!)
                if (selectedAlarmForMenu != null) selectedAlarmForMenu = null
            },
            onPreview = {
                onPreviewClick(selectedAlarmForMenu!!)
                if (selectedAlarmForMenu != null) selectedAlarmForMenu = null
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
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = LiteverTheme.spacing.extraLarge)
        ) {
            if (alarm.isEnabled && alarm.repeatDays.isNotEmpty()) {
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
fun EmptyState(
    onAddAlarmClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val descriptionPrefix = stringResource(R.string.empty_description)
    val reColor = LiteverTheme.colors.onSurfaceVariant
    val mindColor = LiteverTheme.colors.primary
    val displayFontFamily = LiteverTheme.typography.displayLarge.fontFamily

    val annotatedDescription = remember(descriptionPrefix, reColor, mindColor, displayFontFamily) {
        buildAnnotatedString {
            append("$descriptionPrefix ")
            withStyle(
                style = SpanStyle(
                    color = reColor,
                    fontWeight = FontWeight.Light,
                    fontFamily = displayFontFamily
                )
            ) {
                append("Re")
            }
            withStyle(
                style = SpanStyle(
                    color = mindColor,
                    fontWeight = FontWeight.Bold,
                    fontFamily = displayFontFamily
                )
            ) {
                append("Mind")
            }
            append(".")
        }
    }

    FeedbackStateView(
        title = stringResource(R.string.no_alarms),
        type = FeedbackStateType.EMPTY,
        modifier = modifier,
        illustration = {
            val isDark = !LiteverTheme.colors.isLight
            val illustrationRes = if (isDark) {
                R.drawable.no_alarm_illustration_dark
            } else {
                R.drawable.no_alarm_illustration
            }
            Card(
                modifier = Modifier.size(200.dp),
                shape = LiteverTheme.shapes.extraLarge
            ) {
                Image(
                    painter = painterResource(illustrationRes),
                    contentDescription = null,
                )
            }
        },
        descriptionSlot = {
            Text(
                text = annotatedDescription,
                style = LiteverTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 280.dp)
            )
        },
        action = {
            LvButton(
                onClick = onAddAlarmClick,
                semantic = LvSemantic.Primary
            ) {
                Text(
                    text = stringResource(R.string.action_add_new_alarm)
                )
            }
        }
    )
}


@Preview(
    name = "Empty State - Light",
    showBackground = true,
    device = Devices.PIXEL_7
)
@Composable
fun EmptyStatePreview() {
    ReMindTheme {
        EmptyState(onAddAlarmClick = {})
    }
}

@Preview(
    name = "Empty State - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    device = Devices.PIXEL_7,
    backgroundColor = 0xFF121212L,
)
@Composable
fun EmptyStateDarkPreview() {
    ReMindTheme(darkTheme = true) {
        EmptyState(onAddAlarmClick = {})
    }
}

@Preview(
    showBackground = true,
    device = Devices.PIXEL_7
)
@Composable
fun AlarmListScreenPreview() {
    val sampleAlarms = listOf(
        Alarm(
            id = 1L,
            time = LocalTime.of(7, 0),
            label = "Morning Alarm",
            isEnabled = true,
            repeatDays = listOf(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY
            )
        ),
        Alarm(
            id = 2L,
            time = LocalTime.of(8, 30),
            label = "Weekend Workout",
            isEnabled = false,
            repeatDays = listOf(
                DayOfWeek.SATURDAY,
                DayOfWeek.SUNDAY
            )
        )
    )

    ReMindTheme {
        AlarmListScreen(
            alarms = sampleAlarms,
            is24HourFormat = false,
            nextAlarmState = NextAlarmUiState.Remaining(days = 0, hours = 7, minutes = 15),
            hasCriticalPermissions = true,
            snackbarHostState = remember { SnackbarHostState() },
            onToggleAlarm = {},
            onDeleteAlarm = {},
            onDuplicateAlarm = {},
            onSkipOnce = {},
            onCancelSkip = {},
            onDeleteDisabledAlarms = {},
            onAddAlarmClick = {},
            onAlarmClick = {},
            onPreviewClick = {},
            onNavigateToPermissions = {},
            onRewardGranted = {},
            isAdFreeActive = false
        )
    }
}












