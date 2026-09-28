package vn.io.litever.remind.features.reminders.ui

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
import vn.io.litever.remind.core.model.ChecklistItem
import vn.io.litever.remind.core.model.DayOfWeek
import vn.io.litever.remind.core.model.GeofenceTransitionType
import vn.io.litever.remind.core.model.NotificationStyle
import vn.io.litever.remind.core.model.ReminderTimeWindow
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.core.model.WifiTriggerType
import vn.io.litever.remind.features.reminders.R
import vn.io.litever.remind.features.reminders.ui.components.ReminderAlertConfigSection
import vn.io.litever.remind.features.reminders.ui.components.ReminderBasicInfoSection
import vn.io.litever.remind.features.reminders.ui.components.ReminderChecklistBuilderSection
import vn.io.litever.remind.features.reminders.ui.components.ReminderLocationConfigSection
import vn.io.litever.remind.features.reminders.ui.components.ReminderTimeConfigSection
import vn.io.litever.remind.features.reminders.ui.components.ReminderTriggerSection
import vn.io.litever.remind.features.reminders.ui.components.ReminderWifiConfigSection
import vn.io.litever.remind.features.reminders.viewmodel.ReminderEditUiState
import vn.io.litever.remind.features.reminders.viewmodel.ReminderEditViewModel
import vn.io.litever.remind.features.reminders.viewmodel.ReminderRepeatInterval
import vn.io.litever.remind.features.reminders.viewmodel.ReminderValidationErrorField
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderEditRoute(
    reminderId: Long,
    onBackClick: () -> Unit,
    onNavigateToRingtoneSelection: (String?) -> Unit,
    onNavigateToSnoozeSettings: (Boolean, Int, Int) -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToTriggerEdit: (Long) -> Unit = {},
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: ReminderEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    val currentBackStackEntry = navController.currentBackStackEntry
    val savedStateHandle = currentBackStackEntry?.savedStateHandle

    val returnedSnoozeEnabled by savedStateHandle?.getStateFlow<Boolean?>("snoozeEnabled", null)
        ?.collectAsState() ?: remember { mutableStateOf(null) }
    val returnedSnoozeInterval by savedStateHandle?.getStateFlow<Int?>("snoozeInterval", null)
        ?.collectAsState() ?: remember { mutableStateOf(null) }
    val returnedSnoozeRepeatCount by savedStateHandle?.getStateFlow<Int?>("snoozeRepeatCount", null)
        ?.collectAsState() ?: remember { mutableStateOf(null) }

    val returnedRingtoneUri by savedStateHandle?.getStateFlow<String?>("selectedRingtoneUri", null)
        ?.collectAsState() ?: remember { mutableStateOf(null) }
    val ringtoneResultWasSet by savedStateHandle?.getStateFlow("selectedRingtoneUri_set", false)
        ?.collectAsState() ?: remember { mutableStateOf(false) }

    LaunchedEffect(returnedSnoozeEnabled, returnedSnoozeInterval, returnedSnoozeRepeatCount) {
        if (returnedSnoozeEnabled != null || returnedSnoozeInterval != null || returnedSnoozeRepeatCount != null) {
            viewModel.updateSnoozeSettings(
                enabled = returnedSnoozeEnabled ?: uiState.snoozeEnabled,
                interval = returnedSnoozeInterval ?: uiState.snoozeIntervalMinutes,
                repeatCount = returnedSnoozeRepeatCount ?: uiState.snoozeRepeatCount
            )
            savedStateHandle?.remove<Boolean>("snoozeEnabled")
            savedStateHandle?.remove<Int>("snoozeInterval")
            savedStateHandle?.remove<Int>("snoozeRepeatCount")
        }
    }

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
        onDateChange = viewModel::setDate,
        onRepeatDaysChange = viewModel::setRepeatDays,
        onRepeatDayToggle = viewModel::toggleRepeatDay,
        onRepeatIntervalChange = viewModel::setRepeatInterval,
        onMonthlyDayToggle = viewModel::toggleMonthlyDay,
        onAddYearlyDate = viewModel::addYearlyDate,
        onRemoveYearlyDate = viewModel::removeYearlyDate,
        onLocationNameChange = viewModel::setLocationName,
        onLatitudeChange = viewModel::setLatitude,
        onLongitudeChange = viewModel::setLongitude,
        onRadiusChange = viewModel::setRadiusMeters,
        onTransitionTypeChange = viewModel::setGeofenceTransitionType,
        onLocationTimeWindowsChange = viewModel::setLocationTimeWindows,
        onAddLocationTimeWindow = viewModel::addLocationTimeWindow,
        onRemoveLocationTimeWindow = viewModel::removeLocationTimeWindow,
        onSetAllDayPresetLocation = viewModel::setAllDayPresetLocation,
        onWifiSsidChange = viewModel::setWifiSsid,
        onWifiBssidChange = viewModel::setWifiBssid,
        onWifiTriggerTypeChange = viewModel::setWifiTriggerType,
        onWifiTimeWindowsChange = viewModel::setWifiTimeWindows,
        onAddWifiTimeWindow = viewModel::addWifiTimeWindow,
        onRemoveWifiTimeWindow = viewModel::removeWifiTimeWindow,
        onSetAllDayPresetWifi = viewModel::setAllDayPresetWifi,
        onAddChecklistItem = viewModel::addChecklistItem,
        onUpdateChecklistItemText = viewModel::updateChecklistItemText,
        onToggleChecklistItemInitial = viewModel::toggleChecklistItemInitial,
        onDeleteChecklistItem = viewModel::deleteChecklistItem,
        onMoveChecklistItem = viewModel::moveChecklistItem,
        onNotificationStyleChange = viewModel::setNotificationStyle,
        onAutoSilenceMinutesChange = viewModel::setAutoSilenceMinutes,
        onUseAlarmStreamChange = viewModel::setUseAlarmStream,
        onRingtoneClick = {
            onNavigateToRingtoneSelection(uiState.ringtoneUri)
        },
        onVibrationChange = viewModel::setVibrationEnabled,
        onVolumeChange = viewModel::setVolume,
        onSnoozeSettingsClick = {
            onNavigateToSnoozeSettings(
                uiState.snoozeEnabled,
                uiState.snoozeIntervalMinutes,
                uiState.snoozeRepeatCount
            )
        },
        onNavigateToTriggerEdit = { onNavigateToTriggerEdit(uiState.id) },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
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
    onTriggerTypeSelected: (ReminderTriggerType) -> Unit = {},
    onTimeChange: (LocalTime) -> Unit = {},
    onDateChange: (LocalDate?) -> Unit = {},
    onRepeatDaysChange: (List<DayOfWeek>) -> Unit = {},
    onRepeatDayToggle: (DayOfWeek) -> Unit = {},
    onRepeatIntervalChange: (ReminderRepeatInterval) -> Unit = {},
    onMonthlyDayToggle: (Int) -> Unit = {},
    onAddYearlyDate: (Int, Int) -> Unit = { _, _ -> },
    onRemoveYearlyDate: (Int) -> Unit = {},
    onLocationNameChange: (String) -> Unit = {},
    onLatitudeChange: (Double) -> Unit = {},
    onLongitudeChange: (Double) -> Unit = {},
    onRadiusChange: (Float) -> Unit = {},
    onTransitionTypeChange: (GeofenceTransitionType) -> Unit = {},
    onLocationTimeWindowsChange: (List<ReminderTimeWindow>) -> Unit = {},
    onAddLocationTimeWindow: (ReminderTimeWindow) -> Unit = {},
    onRemoveLocationTimeWindow: (Int) -> Unit = {},
    onSetAllDayPresetLocation: () -> Unit = {},
    onWifiSsidChange: (String) -> Unit = {},
    onWifiBssidChange: (String?) -> Unit = {},
    onWifiTriggerTypeChange: (WifiTriggerType) -> Unit = {},
    onWifiTimeWindowsChange: (List<ReminderTimeWindow>) -> Unit = {},
    onAddWifiTimeWindow: (ReminderTimeWindow) -> Unit = {},
    onRemoveWifiTimeWindow: (Int) -> Unit = {},
    onSetAllDayPresetWifi: () -> Unit = {},
    onAddChecklistItem: (String) -> Unit,
    onUpdateChecklistItemText: (Int, String) -> Unit,
    onToggleChecklistItemInitial: (Int, Boolean) -> Unit,
    onDeleteChecklistItem: (Int) -> Unit,
    onMoveChecklistItem: (Int, Int) -> Unit,
    onNotificationStyleChange: (NotificationStyle) -> Unit,
    onAutoSilenceMinutesChange: (Int) -> Unit,
    onUseAlarmStreamChange: (Boolean) -> Unit,
    onRingtoneClick: () -> Unit,
    onVibrationChange: (Boolean) -> Unit,
    onVolumeChange: (Int) -> Unit,
    onSnoozeSettingsClick: () -> Unit,
    onNavigateToTriggerEdit: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val lazyListState = rememberLazyListState()
    val titleFocusRequester = remember { FocusRequester() }

    LaunchedEffect(uiState.validationErrorTrigger) {
        if (uiState.validationErrorTrigger > 0L) {
            when (uiState.validationErrorField) {
                ReminderValidationErrorField.TITLE -> {
                    lazyListState.animateScrollToItem(0)
                    try {
                        titleFocusRequester.requestFocus()
                    } catch (_: Exception) {}
                }
                ReminderValidationErrorField.LOCATION,
                ReminderValidationErrorField.WIFI_SSID -> {
                    lazyListState.animateScrollToItem(1)
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
            ReMindBottomBar {
                LiteverButton(
                    onClick = onSaveClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.action_save))
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

            // 2. Trigger Summary Section (Click to navigate to Trigger Edit Screen)
            item {
                ReminderTriggerSection(
                    uiState = uiState,
                    onClick = onNavigateToTriggerEdit,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 3. Checklist Builder Section
            item {
                ReminderChecklistBuilderSection(
                    checklist = uiState.checklist,
                    onAddItem = onAddChecklistItem,
                    onUpdateItemText = onUpdateChecklistItemText,
                    onToggleItemInitialCheck = onToggleChecklistItemInitial,
                    onDeleteItem = onDeleteChecklistItem,
                    onMoveItem = onMoveChecklistItem
                )
            }

            // 4. Alert & Notification Settings
            item {
                ReminderAlertConfigSection(
                    notificationStyle = uiState.notificationStyle,
                    onNotificationStyleChange = onNotificationStyleChange,
                    ringtoneTitle = uiState.ringtoneTitle,
                    onRingtoneClick = onRingtoneClick,
                    volume = uiState.volume,
                    onVolumeChange = onVolumeChange,
                    vibrationEnabled = uiState.vibrationEnabled,
                    onVibrationChange = onVibrationChange,
                    autoSilenceMinutes = uiState.autoSilenceMinutes,
                    onAutoSilenceMinutesChange = onAutoSilenceMinutesChange,
                    useAlarmStream = uiState.useAlarmStream,
                    onUseAlarmStreamChange = onUseAlarmStreamChange,
                    snoozeEnabled = uiState.snoozeEnabled,
                    snoozeIntervalMinutes = uiState.snoozeIntervalMinutes,
                    snoozeRepeatCount = uiState.snoozeRepeatCount,
                    onSnoozeSettingsClick = onSnoozeSettingsClick
                )
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
                    semantic = LvSemantic.Destructive
                ) {
                    Text(stringResource(R.string.action_discard))
                }
            },
            dismissButton = {
                LvButton(
                    onClick = onDismissDiscardDialog,
                    type = LvButtonType.Outlined,
                    semantic = LvSemantic.Secondary
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
                triggerType = ReminderTriggerType.TIME,
                time = LocalTime.of(18, 30),
                date = LocalDate.now(),
                repeatDays = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
                repeatInterval = ReminderRepeatInterval.WEEKLY,
                checklist = listOf(
                    ChecklistItem(id = 1L, text = "Organic Milk", isCompleted = false),
                    ChecklistItem(id = 2L, text = "Whole wheat bread", isCompleted = true)
                ),
                notificationStyle = NotificationStyle.STANDARD,
                ringtoneTitle = "Default",
                vibrationEnabled = true,
                volume = 80,
                snoozeEnabled = true,
                snoozeIntervalMinutes = 10,
                snoozeRepeatCount = 3,
                is24HourFormat = true
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
            onTriggerTypeSelected = {},
            onTimeChange = {},
            onDateChange = {},
            onRepeatDaysChange = {},
            onRepeatDayToggle = {},
            onRepeatIntervalChange = {},
            onMonthlyDayToggle = {},
            onAddYearlyDate = { _, _ -> },
            onRemoveYearlyDate = {},
            onLocationNameChange = {},
            onLatitudeChange = {},
            onLongitudeChange = {},
            onRadiusChange = {},
            onTransitionTypeChange = {},
            onLocationTimeWindowsChange = {},
            onAddLocationTimeWindow = {},
            onRemoveLocationTimeWindow = {},
            onSetAllDayPresetLocation = {},
            onWifiSsidChange = {},
            onWifiBssidChange = {},
            onWifiTriggerTypeChange = {},
            onWifiTimeWindowsChange = {},
            onAddWifiTimeWindow = {},
            onRemoveWifiTimeWindow = {},
            onSetAllDayPresetWifi = {},
            onAddChecklistItem = {},
            onUpdateChecklistItemText = { _, _ -> },
            onToggleChecklistItemInitial = { _, _ -> },
            onDeleteChecklistItem = {},
            onMoveChecklistItem = { _, _ -> },
            onNotificationStyleChange = {},
            onAutoSilenceMinutesChange = {},
            onUseAlarmStreamChange = {},
            onRingtoneClick = {},
            onVibrationChange = {},
            onVolumeChange = {},
            onSnoozeSettingsClick = {}
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
                triggerType = ReminderTriggerType.TIME,
                time = LocalTime.of(18, 30),
                date = LocalDate.now(),
                repeatDays = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
                repeatInterval = ReminderRepeatInterval.WEEKLY,
                checklist = listOf(
                    ChecklistItem(id = 1L, text = "Organic Milk", isCompleted = false),
                    ChecklistItem(id = 2L, text = "Whole wheat bread", isCompleted = true)
                ),
                notificationStyle = NotificationStyle.STANDARD,
                ringtoneTitle = "Default",
                vibrationEnabled = true,
                volume = 80,
                snoozeEnabled = true,
                snoozeIntervalMinutes = 10,
                snoozeRepeatCount = 3,
                is24HourFormat = true
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
            onTriggerTypeSelected = {},
            onTimeChange = {},
            onDateChange = {},
            onRepeatDaysChange = {},
            onRepeatDayToggle = {},
            onRepeatIntervalChange = {},
            onMonthlyDayToggle = {},
            onAddYearlyDate = { _, _ -> },
            onRemoveYearlyDate = {},
            onLocationNameChange = {},
            onLatitudeChange = {},
            onLongitudeChange = {},
            onRadiusChange = {},
            onTransitionTypeChange = {},
            onLocationTimeWindowsChange = {},
            onAddLocationTimeWindow = {},
            onRemoveLocationTimeWindow = {},
            onSetAllDayPresetLocation = {},
            onWifiSsidChange = {},
            onWifiBssidChange = {},
            onWifiTriggerTypeChange = {},
            onWifiTimeWindowsChange = {},
            onAddWifiTimeWindow = {},
            onRemoveWifiTimeWindow = {},
            onSetAllDayPresetWifi = {},
            onAddChecklistItem = {},
            onUpdateChecklistItemText = { _, _ -> },
            onToggleChecklistItemInitial = { _, _ -> },
            onDeleteChecklistItem = {},
            onMoveChecklistItem = { _, _ -> },
            onNotificationStyleChange = {},
            onAutoSilenceMinutesChange = {},
            onUseAlarmStreamChange = {},
            onRingtoneClick = {},
            onVibrationChange = {},
            onVolumeChange = {},
            onSnoozeSettingsClick = {}
        )
    }
}


