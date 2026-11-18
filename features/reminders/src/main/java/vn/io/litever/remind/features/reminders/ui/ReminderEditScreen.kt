package vn.io.litever.remind.features.reminders.ui

import android.content.res.Configuration
import android.media.RingtoneManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.ui.platform.LocalInspectionMode
import vn.io.litever.remind.core.ads.api.AdPlacement
import vn.io.litever.remind.core.ads.api.LocalAdManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import vn.io.litever.designsystem.components.LiteverButton
import vn.io.litever.designsystem.components.LiteverCircularProgressIndicator
import vn.io.litever.designsystem.components.LiteverScaffold
import vn.io.litever.designsystem.components.LiteverTopAppBar
import vn.io.litever.designsystem.components.button.LvButton
import vn.io.litever.designsystem.components.button.LvButtonType
import vn.io.litever.designsystem.components.core.LvSemantic
import vn.io.litever.designsystem.components.dialog.LvAlertDialog
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.components.ReMindBottomBar
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.model.NotificationStyle
import vn.io.litever.remind.core.model.ReminderTimeWindow
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.features.reminders.R
import vn.io.litever.remind.features.reminders.ui.components.ReminderAlertConfigSection
import vn.io.litever.remind.features.reminders.ui.components.ReminderBasicInfoSection
import vn.io.litever.remind.features.reminders.ui.components.ReminderRepeatConfigSection
import vn.io.litever.remind.features.reminders.ui.components.ReminderTriggerSection
import vn.io.litever.remind.features.reminders.viewmodel.ReminderEditUiState
import vn.io.litever.remind.features.reminders.viewmodel.ReminderEditViewModel
import vn.io.litever.remind.features.reminders.viewmodel.ReminderRepeatInterval
import vn.io.litever.remind.features.reminders.viewmodel.ReminderValidationErrorField
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun ReminderEditRoute(
    onBackClick: () -> Unit,
    onNavigateToRingtoneSelection: (String?) -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToPaywall: (Int) -> Unit = {},
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: ReminderEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    val currentBackStackEntry = navController.currentBackStackEntry
    val savedStateHandle = currentBackStackEntry?.savedStateHandle

    val returnedRingtoneUri by savedStateHandle?.getStateFlow<String?>("selectedRingtoneUri", null)
        ?.collectAsState() ?: remember { mutableStateOf(null) }
    val ringtoneResultWasSet by savedStateHandle?.getStateFlow("selectedRingtoneUri_set", false)
        ?.collectAsState() ?: remember { mutableStateOf(false) }

    LaunchedEffect(returnedRingtoneUri, ringtoneResultWasSet) {
        if (ringtoneResultWasSet) {
            viewModel.updateRingtone(returnedRingtoneUri)
            savedStateHandle?.remove<String>("selectedRingtoneUri")
            savedStateHandle?.remove<Boolean>("selectedRingtoneUri_set")
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            viewModel.stopRingtonePlayback()
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    BackHandler {
        viewModel.onBackClick(onBackClick)
    }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            LiteverCircularProgressIndicator(color = LiteverTheme.colors.primary)
        }
        return
    }

    ReminderEditScreen(
        uiState = uiState,
        onBackClick = { viewModel.onBackClick(onBackClick) },
        onSaveClick = { viewModel.saveReminder(onSuccess = onBackClick) },
        onSaveAnyway = { viewModel.saveAnyway(onSuccess = onBackClick) },
        onDismissPermissionDialog = viewModel::dismissPermissionDialog,
        onNavigateToPermissions = {
            viewModel.dismissPermissionDialog()
            onNavigateToPermissions()
        },
        onDismissDiscardDialog = viewModel::dismissDiscardDialog,
        onConfirmDiscard = { viewModel.confirmDiscard(onBackClick) },
        onTitleChange = viewModel::setTitle,
        onNotesChange = viewModel::setNotes,
        onTriggerTypeSelected = viewModel::setTriggerType,
        onTimeChange = viewModel::setTime,
        fixedTimes = uiState.fixedTimes,
        onAddFixedTime = viewModel::addFixedTime,
        onRemoveFixedTime = viewModel::removeFixedTime,
        onUpdateFixedTime = viewModel::updateFixedTime,
        onDateChange = viewModel::setDate,
        onAddExactDate = viewModel::addExactDate,
        onRemoveExactDate = viewModel::removeExactDate,
        onUpdateExactDate = viewModel::updateExactDate,
        onRepeatDaysChange = viewModel::setRepeatDays,
        onRepeatDayToggle = viewModel::toggleRepeatDay,
        onRepeatIntervalChange = viewModel::setRepeatInterval,
        onMonthlyDayToggle = viewModel::toggleMonthlyDay,
        onAddYearlyDate = viewModel::addYearlyDate,
        onRemoveYearlyDate = viewModel::removeYearlyDate,
        onIntervalMinutesChange = { str -> viewModel.setIntervalMinutes(str.toIntOrNull()) },
        onTimeWindowsChange = viewModel::setTimeWindows,
        onAddTimeWindow = viewModel::addTimeWindow,
        onRemoveTimeWindow = viewModel::removeTimeWindow,
        onNotificationStyleChange = viewModel::setNotificationStyle,
        onAutoSilenceMinutesChange = viewModel::setAutoSilenceMinutes,
        onUseAlarmStreamChange = viewModel::setUseAlarmStream,
        onRingtoneClick = {
            viewModel.stopRingtonePlayback()
            onNavigateToRingtoneSelection(uiState.ringtoneUri)
        },
        onVibrationChange = viewModel::setVibrationEnabled,
        onVolumeChange = viewModel::setVolume,
        modifier = modifier,
        onTogglePreview = viewModel::toggleRingtonePlayback,
        onSnoozeEnabledChange = viewModel::setSnoozeEnabled,
        onSnoozeIntervalChange = viewModel::setSnoozeIntervalMinutes,
        onToggleEnabled = viewModel::setIsEnabled,
        onExtraLoudChange = viewModel::setExtraLoudEnabled,
        onAnnounceTitleTtsChange = viewModel::setAnnounceTitleTts,
        onNavigateToPaywall = onNavigateToPaywall
    )
}

@Composable
fun ReminderEditScreen(
    uiState: ReminderEditUiState,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    onSaveAnyway: () -> Unit,
    onDismissPermissionDialog: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onDismissDiscardDialog: () -> Unit,
    onConfirmDiscard: () -> Unit,
    onTitleChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onNotificationStyleChange: (NotificationStyle) -> Unit,
    onAutoSilenceMinutesChange: (Int) -> Unit,
    onUseAlarmStreamChange: (Boolean) -> Unit,
    onRingtoneClick: () -> Unit,
    onVibrationChange: (Boolean) -> Unit,
    onVolumeChange: (Int) -> Unit,
    onExtraLoudChange: (Boolean) -> Unit = {},
    onAnnounceTitleTtsChange: (Boolean) -> Unit = {},
    onNavigateToPaywall: (Int) -> Unit = {},
    modifier: Modifier = Modifier,
    onTriggerTypeSelected: (ReminderTriggerType) -> Unit = {},
    onTimeChange: (LocalTime) -> Unit = {},
    fixedTimes: List<LocalTime> = listOf(uiState.time),
    onAddFixedTime: (LocalTime) -> Unit = {},
    onRemoveFixedTime: (Int) -> Unit = {},
    onUpdateFixedTime: (Int, LocalTime) -> Unit = { _, t -> onTimeChange(t) },
    onDateChange: (LocalDate?) -> Unit = {},
    onAddExactDate: (LocalDate) -> Unit = {},
    onRemoveExactDate: (Int) -> Unit = {},
    onUpdateExactDate: (Int, LocalDate) -> Unit = { _, d -> onDateChange(d) },
    onRepeatDaysChange: (List<DayOfWeek>) -> Unit = {},
    onRepeatDayToggle: (DayOfWeek) -> Unit = {},
    onRepeatIntervalChange: (ReminderRepeatInterval) -> Unit = {},
    onMonthlyDayToggle: (Int) -> Unit = {},
    onAddYearlyDate: (Int, Int) -> Unit = { _, _ -> },
    onRemoveYearlyDate: (Int) -> Unit = {},
    onIntervalMinutesChange: (String) -> Unit = {},
    onTimeWindowsChange: (List<ReminderTimeWindow>) -> Unit = {},
    onAddTimeWindow: (ReminderTimeWindow) -> Unit = {},
    onRemoveTimeWindow: (Int) -> Unit = {},
    onTogglePreview: () -> Unit = {},
    onSnoozeEnabledChange: (Boolean) -> Unit = {},
    onSnoozeIntervalChange: (Int) -> Unit = {},
    onToggleEnabled: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val lazyListState = rememberLazyListState()
    val titleFocusRequester = remember { FocusRequester() }
    val defaultRingtone = stringResource(R.string.default_ringtone)

    val ringtoneTitle = remember(uiState.ringtoneUri, defaultRingtone) {
        if (uiState.ringtoneUri.isNullOrBlank()) {
            defaultRingtone
        } else {
            try {
                val uri = uiState.ringtoneUri.toUri()
                RingtoneManager.getRingtone(context, uri)?.getTitle(context)
                    ?: defaultRingtone
            } catch (_: Exception) {
                defaultRingtone
            }
        }
    }

    LaunchedEffect(uiState.validationErrorTrigger) {
        if (uiState.validationErrorTrigger > 0L) {
            when (uiState.validationErrorField) {
                ReminderValidationErrorField.TITLE -> {
                    lazyListState.animateScrollToItem(0)
                    try {
                        titleFocusRequester.requestFocus()
                    } catch (_: Exception) {}
                }
                null -> {}
            }
        }
    }

    LiteverScaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            LiteverTopAppBar(
                title = stringResource(if (uiState.id == 0L) R.string.add_reminder_title else R.string.edit_reminder_title),
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavigationBarDefaults.containerColor)
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(
                            WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
                        )
                    )
            ) {
                ReMindBottomBar(
                    windowInsets = WindowInsets(0, 0, 0, 0)
                ) {
                    LiteverButton(
                        onClick = onSaveClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.action_save))
                    }
                }
                if (!LocalInspectionMode.current) {
                    LocalAdManager.current.MicroAdStrip(
                        placement = AdPlacement.EDIT_BOTTOM_STRIP,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = LiteverTheme.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.small)
        ) {
            // 1. Basic Info Section
            item {
                ReminderBasicInfoSection(
                    title = uiState.title,
                    onTitleChange = onTitleChange,
                    notes = uiState.notes,
                    onNotesChange = onNotesChange,
                    showTitleError = uiState.showTitleError,
                    titleFocusRequester = titleFocusRequester
                )
            }

            // 2. Repeat Configuration Section (Day schedule: One-time, Weekly, Monthly, Yearly)
            item {
                ReminderRepeatConfigSection(
                    repeatInterval = uiState.repeatInterval,
                    onRepeatIntervalChange = onRepeatIntervalChange,
                    date = uiState.date,
                    onDateChange = onDateChange,
                    exactDates = uiState.exactDates,
                    onAddExactDate = onAddExactDate,
                    onRemoveExactDate = onRemoveExactDate,
                    onUpdateExactDate = onUpdateExactDate,
                    repeatDays = uiState.repeatDays,
                    onRepeatDaysChange = onRepeatDaysChange,
                    onRepeatDayToggle = onRepeatDayToggle,
                    monthlyDays = uiState.monthlyDays,
                    onMonthlyDayToggle = onMonthlyDayToggle,
                    yearlyDates = uiState.yearlyDates,
                    onAddYearlyDate = onAddYearlyDate,
                    onRemoveYearlyDate = onRemoveYearlyDate,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 3. Trigger Configuration Section (Within-day trigger: Fixed time vs Interval)
            item {
                ReminderTriggerSection(
                    uiState = uiState,
                    onTriggerTypeSelected = onTriggerTypeSelected,
                    onTimeChange = onTimeChange,
                    fixedTimes = fixedTimes,
                    onAddFixedTime = onAddFixedTime,
                    onRemoveFixedTime = onRemoveFixedTime,
                    onUpdateFixedTime = onUpdateFixedTime,
                    onIntervalMinutesChange = onIntervalMinutesChange,
                    onTimeWindowsChange = onTimeWindowsChange,
                    onAddTimeWindow = onAddTimeWindow,
                    onRemoveTimeWindow = onRemoveTimeWindow,
                    is24HourFormat = uiState.is24HourFormat,
                    isPro = uiState.isPro,
                    onNavigateToPaywall = onNavigateToPaywall,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 3. Alert & Notification Settings
            item {
                ReminderAlertConfigSection(
                    notificationStyle = uiState.notificationStyle,
                    onNotificationStyleChange = onNotificationStyleChange,
                    ringtoneTitle = ringtoneTitle,
                    onRingtoneClick = onRingtoneClick,
                    volume = uiState.volume,
                    onVolumeChange = onVolumeChange,
                    vibrationEnabled = uiState.vibrationEnabled,
                    onVibrationChange = onVibrationChange,
                    isPlaying = uiState.isRingtonePlaying,
                    onTogglePlay = onTogglePreview,
                    autoSilenceMinutes = uiState.autoSilenceMinutes,
                    onAutoSilenceMinutesChange = onAutoSilenceMinutesChange,
                    useAlarmStream = uiState.useAlarmStream,
                    onUseAlarmStreamChange = onUseAlarmStreamChange,
                    snoozeEnabled = uiState.snoozeEnabled,
                    onSnoozeEnabledChange = onSnoozeEnabledChange,
                    snoozeIntervalMinutes = uiState.snoozeIntervalMinutes,
                    onSnoozeIntervalChange = onSnoozeIntervalChange,
                    extraLoudEnabled = uiState.extraLoudEnabled,
                    onExtraLoudChange = onExtraLoudChange,
                    announceTitleTts = uiState.announceTitleTts,
                    onAnnounceTitleTtsChange = onAnnounceTitleTtsChange,
                    isPro = uiState.isPro,
                    onNavigateToPaywall = onNavigateToPaywall
                )
            }

            // 4. Toggle Enabled Action Button (Only for existing reminders)
            if (uiState.id != 0L) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    LvButton(
                        onClick = { onToggleEnabled(!uiState.isEnabled) },
                        type = LvButtonType.Outlined,
                        semantic = if (uiState.isEnabled) LvSemantic.Destructive else LvSemantic.Primary,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (uiState.isEnabled) {
                                stringResource(R.string.action_turn_off)
                            } else {
                                stringResource(R.string.action_turn_on)
                            }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Permission Warning "Save Anyway" Dialog
    if (uiState.showPermissionDialog) {
        LvAlertDialog(
            onDismissRequest = onDismissPermissionDialog,
            title = { Text(stringResource(R.string.reminder_permission_dialog_title)) },
            text = { Text(stringResource(R.string.reminder_permission_dialog_message)) },
            confirmButton = {
                LvButton(onClick = onNavigateToPermissions) {
                    Text(stringResource(R.string.action_go_to_settings))
                }
            },
            dismissButton = {
                LvButton(
                    onClick = onSaveAnyway,
                    type = LvButtonType.Outlined,
                    semantic = LvSemantic.Secondary
                ) {
                    Text(stringResource(R.string.action_save_anyway))
                }
            }
        )
    }

    // Discard Changes Dialog
    if (uiState.showDiscardDialog) {
        LvAlertDialog(
            onDismissRequest = onDismissDiscardDialog,
            title = { Text(stringResource(R.string.discard_changes_title)) },
            text = { Text(stringResource(R.string.discard_changes_message)) },
            confirmButton = {
                LvButton(
                    onClick = onConfirmDiscard,
                ) {
                    Text(stringResource(R.string.action_discard))
                }
            },
            dismissButton = {
                LvButton(
                    onClick = onDismissDiscardDialog,
                    type = LvButtonType.Outlined,
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ReminderEditScreenPreview() {
    ReMindTheme {
        ReminderEditScreen(
            uiState = ReminderEditUiState(
                isLoading = false,
                id = 1L,
                title = "Buy groceries",
                notes = "Milk, Eggs, Bread, Cheese",
                triggerType = ReminderTriggerType.FIXED_TIME,
                time = LocalTime.of(18, 30),
                date = LocalDate.now(),
                repeatDays = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
                repeatInterval = ReminderRepeatInterval.WEEKLY,
                notificationStyle = NotificationStyle.STANDARD,
                vibrationEnabled = true,
                volume = 80,
                snoozeEnabled = true,
                snoozeIntervalMinutes = 10
            ),
            onBackClick = {},
            onSaveClick = {},
            onSaveAnyway = {},
            onDismissPermissionDialog = {},
            onNavigateToPermissions = {},
            onDismissDiscardDialog = {},
            onConfirmDiscard = {},
            onTitleChange = {},
            onNotesChange = {},
            onNotificationStyleChange = {},
            onAutoSilenceMinutesChange = {},
            onUseAlarmStreamChange = {},
            onRingtoneClick = {},
            onVibrationChange = {},
            onVolumeChange = {},
            onSnoozeEnabledChange = {},
            onSnoozeIntervalChange = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ReminderEditScreenDarkPreview() {
    ReMindTheme(darkTheme = true) {
        ReminderEditScreen(
            uiState = ReminderEditUiState(
                isLoading = false,
                id = 1L,
                title = "Buy groceries",
                notes = "Milk, Eggs, Bread, Cheese",
                triggerType = ReminderTriggerType.FIXED_TIME,
                time = LocalTime.of(18, 30),
                date = LocalDate.now(),
                repeatDays = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
                repeatInterval = ReminderRepeatInterval.WEEKLY,
                notificationStyle = NotificationStyle.STANDARD,
                vibrationEnabled = true,
                volume = 80,
                snoozeEnabled = true,
                snoozeIntervalMinutes = 10
            ),
            onBackClick = {},
            onSaveClick = {},
            onSaveAnyway = {},
            onDismissPermissionDialog = {},
            onNavigateToPermissions = {},
            onDismissDiscardDialog = {},
            onConfirmDiscard = {},
            onTitleChange = {},
            onNotesChange = {},
            onNotificationStyleChange = {},
            onAutoSilenceMinutesChange = {},
            onUseAlarmStreamChange = {},
            onRingtoneClick = {},
            onVibrationChange = {},
            onVolumeChange = {},
            onSnoozeEnabledChange = {},
            onSnoozeIntervalChange = {}
        )
    }
}
