package vn.io.litever.remind.features.alarms.ui

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.flow.collectLatest
import vn.io.litever.brand.assets.LiteverMascotState
import vn.io.litever.brand.tokens.LiteverTokens
import vn.io.litever.remind.core.designsystem.components.LvTopAppBar
import vn.io.litever.remind.core.designsystem.components.PermissionWarningBannerSection
import vn.io.litever.remind.core.designsystem.components.ReMindMascotEmptyState
import vn.io.litever.remind.core.designsystem.components.ReMindProTopBarButton
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.designsystem.theme.spacing
import vn.io.litever.remind.core.model.Alarm
import vn.io.litever.remind.features.alarms.R
import vn.io.litever.remind.features.alarms.ui.components.AlarmCard
import vn.io.litever.remind.features.alarms.ui.components.NextAlarmHeader
import vn.io.litever.remind.features.alarms.ui.components.QuickAlarmBottomSheet
import vn.io.litever.remind.features.alarms.ui.state.NextAlarmUiState
import vn.io.litever.remind.features.alarms.viewmodel.AlarmListViewModel
import java.time.DayOfWeek
import java.time.LocalTime

@Suppress("LocalContextGetResourceValueCall")
@Composable
fun AlarmListRoute(
    onAddAlarmClick: () -> Unit,
    onAlarmClick: (Alarm) -> Unit,
    onNavigateToPreview: (Long) -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToPaywall: (Int) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: AlarmListViewModel = hiltViewModel()
) {
    val alarms by viewModel.alarms.collectAsState()
    val nextAlarmTime by viewModel.nextAlarmTime.collectAsState()
    val is24HourFormat by viewModel.is24HourFormat.collectAsState()
    val hasCriticalPermissions by viewModel.hasCriticalPermissions.collectAsState()
    val hasNonCriticalPermissions by viewModel.hasNonCriticalPermissions.collectAsState()
    val isPermissionWarningDismissed by viewModel.isPermissionWarningDismissed.collectAsState()
    val isAdFreeActive by viewModel.isAdFreeActive.collectAsState()
    val isPro by viewModel.isPro.collectAsState()

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
        hasNonCriticalPermissions = hasNonCriticalPermissions,
        isPermissionWarningDismissed = isPermissionWarningDismissed,
        onDismissPermissionWarning = viewModel::dismissPermissionWarning,
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
        isPro = isPro,
        onNavigateToPaywall = onNavigateToPaywall,
        onCreateQuickAlarm = viewModel::createQuickAlarm,
        modifier = modifier
    )
}

@Composable
fun AlarmListScreen(
    alarms: List<Alarm>?,
    is24HourFormat: Boolean,
    nextAlarmState: NextAlarmUiState,
    hasCriticalPermissions: Boolean,
    hasNonCriticalPermissions: Boolean = true,
    isPermissionWarningDismissed: Boolean = false,
    onDismissPermissionWarning: () -> Unit = {},
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
    isPro: Boolean = false,
    onNavigateToPaywall: (Int) -> Unit = {},
    onCreateQuickAlarm: (minutes: Int, label: String, ringtoneUri: String?, volume: Int, vibrationEnabled: Boolean) -> Unit = { _, _, _, _, _ -> },
    modifier: Modifier = Modifier
) {
    var showTopMenu by remember { mutableStateOf(false) }
    var selectedAlarmForMenu by remember { mutableStateOf<Alarm?>(null) }
    var showFabMenu by remember { mutableStateOf(false) }
    var showQuickAlarmSheet by remember { mutableStateOf(false) }
    val actionMoreDescription = stringResource(R.string.action_more)
    val deleteDisabledAlarmsText = stringResource(R.string.delete_disabled_alarms)
    val actionAddDescription = stringResource(R.string.action_add)
    val hasAlarms = !alarms.isNullOrEmpty()

    Scaffold(
        modifier = modifier,
        topBar = {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                LvTopAppBar(
                    title = {
                        Text(text = stringResource(R.string.alarms_title))
                    },
                    actions = {
                        ReMindProTopBarButton(
                            isPro = isPro,
                            onClick = { onNavigateToPaywall(0) },
                            modifier = Modifier.padding(end = if (hasAlarms) MaterialTheme.spacing.none else MaterialTheme.spacing.small)
                        )
                        if (hasAlarms) {
                            IconButton(onClick = { showTopMenu = !showTopMenu }) {
                                Icon(
                                    ReMindIcons.MoreVert,
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
                val showPermissionBanner = !hasCriticalPermissions ||
                    (!hasNonCriticalPermissions && !isPermissionWarningDismissed)
                PermissionWarningBannerSection(
                    visible = showPermissionBanner,
                    isCritical = !hasCriticalPermissions,
                    onClick = onNavigateToPermissions,
                    onDismiss = onDismissPermissionWarning,
                    modifier = Modifier.padding(
                        start = MaterialTheme.spacing.medium,
                        end = MaterialTheme.spacing.medium,
                        bottom = MaterialTheme.spacing.small
                    )
                )
                if (hasCriticalPermissions && hasAlarms) {
                    NextAlarmHeader(
                        modifier = Modifier.padding(
                            start = MaterialTheme.spacing.medium,
                            end = MaterialTheme.spacing.medium,
                            bottom = MaterialTheme.spacing.small
                        ),
                        state = nextAlarmState
                    )
                }
            }
        },
        floatingActionButton = {
            if (hasAlarms) {
                FloatingActionButtonMenu(
                    expanded = showFabMenu,
                    button = {
                        ToggleFloatingActionButton(
                            checked = showFabMenu,
                            onCheckedChange = { showFabMenu = it },
                        ) {
                            val iconVector by remember {
                                derivedStateOf {
                                    if (checkedProgress > 0.5f) ReMindIcons.Close else ReMindIcons.Add
                                }
                            }
                            Icon(
                                painter = rememberVectorPainter(iconVector),
                                contentDescription = actionAddDescription,
                                modifier = Modifier.animateIcon({ checkedProgress }),
                            )
                        }
                    },
                ) {
                    FloatingActionButtonMenuItem(
                        onClick = {
                            showFabMenu = false
                            onAddAlarmClick()
                        },
                        icon = { Icon(ReMindIcons.Alarm, contentDescription = null) },
                        text = { Text(text = stringResource(R.string.alarm_option)) },
                    )
                    FloatingActionButtonMenuItem(
                        onClick = {
                            showFabMenu = false
                            showQuickAlarmSheet = true
                        },
                        icon = { Icon(ReMindIcons.FlashOn, contentDescription = null) },
                        text = { Text(text = stringResource(R.string.quick_alarm)) },
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
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
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                        contentPadding = PaddingValues(
                            start = MaterialTheme.spacing.medium,
                            top = MaterialTheme.spacing.small,
                            end = MaterialTheme.spacing.medium,
                            bottom = MaterialTheme.spacing.small + 80.dp
                        )
                    ) {
                        itemsIndexed(alarms, key = { _, alarm -> alarm.id }) { index, alarm ->
                            AlarmCard(
                                alarm = alarm,
                                is24HourFormat = is24HourFormat,
                                onToggle = { onToggleAlarm(alarm) },
                                onClick = {
                                    if (!alarm.isQuickAlarm) {
                                        onAlarmClick(alarm)
                                    }
                                },
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

    // Quick Alarm Bottom Sheet
    if (showQuickAlarmSheet) {
        QuickAlarmBottomSheet(
            onDismissRequest = { showQuickAlarmSheet = false },
            onSave = { minutes, label, ringtoneUri, volume, vibrationEnabled ->
                onCreateQuickAlarm(minutes, label, ringtoneUri, volume, vibrationEnabled)
                showQuickAlarmSheet = false
            },
            is24HourFormat = is24HourFormat
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
        containerColor = MaterialTheme.colorScheme.surface
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
            .padding(bottom = MaterialTheme.spacing.small)
    ) {
        // Selected Alarm Info Summary
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.medium)
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
                style = LiteverTokens.typography.labelSmallEmphasized,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.size(MaterialTheme.spacing.extraSmall))

            // Time + AM/PM + Mission Icons
            val (timeStr, amPm) = vn.io.litever.remind.core.common.util.TimeFormatUtils.formatTimeParts(
                alarm.time,
                is24HourFormat
            )
            androidx.compose.foundation.layout.Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.foundation.layout.Row {
                    Text(
                        text = timeStr,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontFeatureSettings = "tnum"
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.alignByBaseline()
                    )
                    if (amPm != null) {
                        Text(
                            text = amPm.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier
                                .padding(start = MaterialTheme.spacing.extraSmall)
                                .alignByBaseline()
                        )
                    }
                }

                vn.io.litever.remind.features.alarms.ui.components.MissionIcons(
                    missions = alarm.missions,
                    modifier = Modifier.padding(start = MaterialTheme.spacing.small)
                )

                // Label if present
                if (alarm.label.isNotBlank()) {
                    Spacer(modifier = Modifier.size(MaterialTheme.spacing.small))
                    Text(
                        text = alarm.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f),
            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium)
        )

        if (alarm.isEnabled && alarm.repeatDays.isNotEmpty()) {
            val isSkipped = alarm.skippedAt != null
            ListItem(
                leadingContent = {
                    Icon(
                        if (isSkipped) ReMindIcons.NotificationsPaused else ReMindIcons.SkipNext,
                        contentDescription = null
                    )
                },
                modifier = Modifier.clickable { if (isSkipped) onCancelSkip() else onSkipOnce() }
            ) {
                Text(stringResource(if (isSkipped) R.string.action_cancel_skip else R.string.action_skip_once))
            }
        }

        ListItem(
            leadingContent = { Icon(ReMindIcons.PlayArrow, contentDescription = null) },
            modifier = Modifier.clickable { onPreview() }
        ) {
            Text(stringResource(R.string.action_preview))
        }

        ListItem(
            leadingContent = { Icon(ReMindIcons.ContentCopy, contentDescription = null) },
            modifier = Modifier.clickable { onDuplicate() }
        ) {
            Text(stringResource(R.string.action_duplicate))
        }

        ListItem(
            leadingContent = {
                Icon(
                    ReMindIcons.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            modifier = Modifier.clickable { onDelete() }
        ) {
            Text(
                stringResource(R.string.action_delete),
                color = MaterialTheme.colorScheme.error
            )
        }
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
    ReMindMascotEmptyState(
        mascot = LiteverMascotState.IDLE,
        title = stringResource(R.string.no_alarms),
        description = stringResource(R.string.empty_description),
        actionLabel = stringResource(R.string.action_add_new_alarm),
        onActionClick = onAddAlarmClick,
        modifier = modifier
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
    ),
    Alarm(
        id = 3L,
        time = LocalTime.of(15, 45),
        label = "Power nap",
        isEnabled = true,
        isQuickAlarm = true
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
    name = "Alarm List - Non-Critical Permission Warning",
    showBackground = true,
    device = Devices.PIXEL_7
)
@Composable
fun AlarmListScreenNonCriticalPermissionsWarningPreview() {
    ReMindTheme {
        AlarmListScreen(
            alarms = sampleAlarmsForPreview,
            is24HourFormat = false,
            nextAlarmState = NextAlarmUiState.Remaining(days = 0, hours = 7, minutes = 15),
            hasCriticalPermissions = true,
            hasNonCriticalPermissions = false,
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
