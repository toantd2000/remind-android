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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.NotificationsPaused
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            if (hasAlarms || !hasCriticalPermissions) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    LvTopAppBar(
                        title = {
                            Text(
                                stringResource(R.string.alarms_title),
                                fontWeight = FontWeight.Bold
                            )
                        },
                        actions = {
                            if (hasAlarms) {
                                LvIconButton(onClick = { showTopMenu = !showTopMenu }) {
                                    Icon(
                                        Icons.Rounded.MoreVert,
                                        contentDescription = actionMoreDescription
                                    )
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
                        }
                    )
                    if (!hasCriticalPermissions) {
                        PermissionWarningBanner(
                            onClick = onNavigateToPermissions,
                            modifier = Modifier.padding(
                                start = LiteverTheme.spacing.medium,
                                end = LiteverTheme.spacing.medium,
                                bottom = LiteverTheme.spacing.small
                            )
                        )
                    } else if (hasAlarms) {
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
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.small),
        ) {
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
                        contentPadding = PaddingValues(
                            horizontal = LiteverTheme.spacing.mediumLarge,
                            vertical = LiteverTheme.spacing.small
                        ).let {
                            PaddingValues(
                                start = it.calculateStartPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
                                top = it.calculateTopPadding(),
                                end = it.calculateEndPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
                                bottom = it.calculateBottomPadding() + 80.dp
                            )
                        }
                    ) {
                        itemsIndexed(alarms, key = { _, alarm -> alarm.id }) { index, alarm ->
                            AlarmCard(
                                alarm = alarm,
                                is24HourFormat = is24HourFormat,
                                onToggle = { onToggleAlarm(alarm) },
                                onClick = { onAlarmClick(alarm) },
                                onMoreClick = { selectedAlarmForMenu = alarm },
                                modifier = Modifier.animateItem()
                            )
                            
                            if (index == 1 && alarms.size >= 3 && !isAdFreeActive) {
                                vn.io.litever.remind.core.ads.api.LocalAdManager.current.NativeAdView(
                                    placement = vn.io.litever.remind.core.ads.api.AdPlacement.ALARM_LIST_NATIVE,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = LiteverTheme.spacing.small)
                                        .animateItem()
                                )
                            }
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
            is24HourFormat = is24HourFormat,
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
    is24HourFormat: Boolean,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onSkipOnce: () -> Unit,
    onCancelSkip: () -> Unit,
    onPreview: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = LiteverTheme.colors.surface
    ) {
        AlarmActionBottomSheetContent(
            alarm = alarm,
            is24HourFormat = is24HourFormat,
            onDelete = onDelete,
            onDuplicate = onDuplicate,
            onSkipOnce = onSkipOnce,
            onCancelSkip = onCancelSkip,
            onPreview = onPreview
        )
    }
}

@Composable
private fun AlarmActionBottomSheetContent(
    alarm: Alarm,
    is24HourFormat: Boolean,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onSkipOnce: () -> Unit,
    onCancelSkip: () -> Unit,
    onPreview: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = LiteverTheme.spacing.small)
    ) {
        // Selected Alarm Info Summary
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(LiteverTheme.spacing.medium)
        ) {
            // Repeat summary
            val repeatText =
                vn.io.litever.remind.features.alarms.ui.components.getRepeatSummaryText(
                    alarm.repeatDays,
                    alarm.time,
                    alarm.date
                )
            Text(
                text = repeatText,
                style = LiteverTheme.typography.labelSmall,
                color = LiteverTheme.colors.primary,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.size(LiteverTheme.spacing.extraSmall))

            // Time + AM/PM + Mission Icons
            val (timeStr, amPm) = vn.io.litever.remind.core.common.util.TimeFormatUtils.formatTimeParts(
                alarm.time,
                is24HourFormat
            )
            androidx.compose.foundation.layout.Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    text = timeStr,
                    style = LiteverTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = LiteverTheme.colors.onSurface
                )
                if (amPm != null) {
                    Text(
                        text = amPm.uppercase(),
                        style = LiteverTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                        color = LiteverTheme.colors.onSurfaceVariant,
                        modifier = Modifier
                            .padding(
                                start = LiteverTheme.spacing.extraSmall,
                                bottom = LiteverTheme.spacing.small
                            )
                            .align(androidx.compose.ui.Alignment.Bottom)
                    )
                }

                vn.io.litever.remind.features.alarms.ui.components.MissionIcons(
                    missions = alarm.missions,
                    modifier = Modifier.padding(
                        start = LiteverTheme.spacing.small,
                        bottom = LiteverTheme.spacing.small
                    )
                )

                // Label if present
                if (alarm.label.isNotBlank()) {
                    Spacer(modifier = Modifier.size(LiteverTheme.spacing.small))
                    Text(
                        text = alarm.label,
                        style = LiteverTheme.typography.bodyMedium,
                        color = LiteverTheme.colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        HorizontalDivider(
            color = LiteverTheme.colors.onSurfaceVariant.copy(alpha = 0.12f),
            modifier = Modifier.padding(horizontal = LiteverTheme.spacing.medium)
        )

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

@Preview(showBackground = true)
@Composable
private fun AlarmActionBottomSheetPreview() {
    ReMindTheme {
        AlarmActionBottomSheetContent(
            alarm = Alarm(
                id = 1,
                time = LocalTime.of(7, 30),
                label = "Wake up",
                isEnabled = true,
                repeatDays = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
            ),
            is24HourFormat = false,
            onDelete = {},
            onDuplicate = {},
            onSkipOnce = {},
            onCancelSkip = {},
            onPreview = {}
        )
    }
}


@Composable
fun EmptyState(
    onAddAlarmClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val descriptionPrefix = stringResource(R.string.empty_description)

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
            Column(
                modifier = Modifier.wrapContentHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = descriptionPrefix,
                    style = LiteverTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                ReMindLogo(fontSize = LiteverTheme.typography.bodyMedium.fontSize)
            }
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

private val sampleAlarmsForPreview = listOf(
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

@Preview(
    name = "Alarm List - Normal With Next Alarm",
    showBackground = true,
    device = Devices.PIXEL_7
)
@Composable
fun AlarmListScreenPreview() {
    ReMindTheme {
        AlarmListScreen(
            alarms = sampleAlarmsForPreview,
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

@Preview(
    name = "Alarm List - No Critical Permissions",
    showBackground = true,
    device = Devices.PIXEL_7
)
@Composable
fun AlarmListScreenNoPermissionsPreview() {
    ReMindTheme {
        AlarmListScreen(
            alarms = sampleAlarmsForPreview,
            is24HourFormat = false,
            nextAlarmState = NextAlarmUiState.Remaining(days = 0, hours = 7, minutes = 15),
            hasCriticalPermissions = false,
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

@Preview(
    name = "Alarm List - All Alarms Off",
    showBackground = true,
    device = Devices.PIXEL_7
)
@Composable
fun AlarmListScreenAllOffPreview() {
    ReMindTheme {
        AlarmListScreen(
            alarms = sampleAlarmsForPreview.map { it.copy(isEnabled = false) },
            is24HourFormat = false,
            nextAlarmState = NextAlarmUiState.AllOff,
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

@Preview(
    name = "Alarm List - 24-Hour Format",
    showBackground = true,
    device = Devices.PIXEL_7
)
@Composable
fun AlarmListScreen24HourPreview() {
    ReMindTheme {
        AlarmListScreen(
            alarms = sampleAlarmsForPreview,
            is24HourFormat = true,
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

@Preview(
    name = "Alarm List - Dark Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    device = Devices.PIXEL_7,
    backgroundColor = 0xFF121212L
)
@Composable
fun AlarmListScreenDarkPreview() {
    ReMindTheme(darkTheme = true) {
        AlarmListScreen(
            alarms = sampleAlarmsForPreview,
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

@Preview(
    name = "Alarm List - Loading State",
    showBackground = true,
    device = Devices.PIXEL_7
)
@Composable
fun AlarmListScreenLoadingPreview() {
    ReMindTheme {
        AlarmListScreen(
            alarms = null,
            is24HourFormat = false,
            nextAlarmState = NextAlarmUiState.AllOff,
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

@Preview(
    name = "Alarm List - Empty With No Critical Permissions",
    showBackground = true,
    device = Devices.PIXEL_7
)
@Composable
fun AlarmListScreenEmptyNoPermissionsPreview() {
    ReMindTheme {
        AlarmListScreen(
            alarms = emptyList(),
            is24HourFormat = false,
            nextAlarmState = NextAlarmUiState.AllOff,
            hasCriticalPermissions = false,
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
