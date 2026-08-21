package vn.io.litever.remind.features.reminders.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import vn.io.litever.designsystem.components.LiteverButton
import vn.io.litever.designsystem.components.LiteverScaffold
import vn.io.litever.designsystem.components.LiteverTopAppBar
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.components.ReMindAlertDialog
import vn.io.litever.remind.core.designsystem.components.ReMindBottomBar
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
            CircularProgressIndicator(color = LiteverTheme.colors.primary)
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
        onMonthlyDayChange = viewModel::setMonthlyDay,
        onYearlyMonthAndDayChange = viewModel::setYearlyMonthAndDay,
        onLocationNameChange = viewModel::setLocationName,
        onLatitudeChange = viewModel::setLatitude,
        onLongitudeChange = viewModel::setLongitude,
        onRadiusChange = viewModel::setRadiusMeters,
        onTransitionTypeChange = viewModel::setGeofenceTransitionType,
        onLocationTimeWindowChange = viewModel::setLocationTimeWindow,
        onWifiSsidChange = viewModel::setWifiSsid,
        onWifiBssidChange = viewModel::setWifiBssid,
        onWifiTriggerTypeChange = viewModel::setWifiTriggerType,
        onWifiTimeWindowChange = viewModel::setWifiTimeWindow,
        onAddChecklistItem = viewModel::addChecklistItem,
        onUpdateChecklistItemText = viewModel::updateChecklistItemText,
        onToggleChecklistItemInitial = viewModel::toggleChecklistItemInitial,
        onDeleteChecklistItem = viewModel::deleteChecklistItem,
        onMoveChecklistItem = viewModel::moveChecklistItem,
        onNotificationStyleChange = viewModel::setNotificationStyle,
        onPersistentAlertChange = viewModel::setPersistentAlert,
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
    onTriggerTypeSelected: (ReminderTriggerType) -> Unit,
    onTimeChange: (LocalTime) -> Unit,
    onDateChange: (LocalDate?) -> Unit,
    onRepeatDaysChange: (List<DayOfWeek>) -> Unit,
    onRepeatDayToggle: (DayOfWeek) -> Unit,
    onRepeatIntervalChange: (ReminderRepeatInterval) -> Unit,
    onMonthlyDayChange: (Int) -> Unit,
    onYearlyMonthAndDayChange: (Int, Int) -> Unit,
    onLocationNameChange: (String) -> Unit,
    onLatitudeChange: (Double) -> Unit,
    onLongitudeChange: (Double) -> Unit,
    onRadiusChange: (Float) -> Unit,
    onTransitionTypeChange: (GeofenceTransitionType) -> Unit,
    onLocationTimeWindowChange: (ReminderTimeWindow?) -> Unit,
    onWifiSsidChange: (String) -> Unit,
    onWifiBssidChange: (String?) -> Unit,
    onWifiTriggerTypeChange: (WifiTriggerType) -> Unit,
    onWifiTimeWindowChange: (ReminderTimeWindow?) -> Unit,
    onAddChecklistItem: (String) -> Unit,
    onUpdateChecklistItemText: (Int, String) -> Unit,
    onToggleChecklistItemInitial: (Int, Boolean) -> Unit,
    onDeleteChecklistItem: (Int) -> Unit,
    onMoveChecklistItem: (Int, Int) -> Unit,
    onNotificationStyleChange: (NotificationStyle) -> Unit,
    onPersistentAlertChange: (Boolean) -> Unit,
    onRingtoneClick: () -> Unit,
    onVibrationChange: (Boolean) -> Unit,
    onVolumeChange: (Int) -> Unit,
    onSnoozeSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(stringResource(R.string.action_save))
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
        ) {
            // 1. Basic Info Section
            item {
                ReminderBasicInfoSection(
                    title = uiState.title,
                    onTitleChange = onTitleChange,
                    notes = uiState.notes,
                    onNotesChange = onNotesChange,
                    showTitleError = uiState.showTitleError
                )
            }

            // 2. Trigger Selector & Configuration (Visually combined)
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    ReminderTriggerSection(
                        selectedTriggerType = uiState.triggerType,
                        onTriggerTypeSelected = onTriggerTypeSelected
                    )

                    when (uiState.triggerType) {
                        ReminderTriggerType.TIME -> {
                            ReminderTimeConfigSection(
                                time = uiState.time,
                                onTimeChange = onTimeChange,
                                date = uiState.date,
                                onDateChange = onDateChange,
                                repeatDays = uiState.repeatDays,
                                onRepeatDaysChange = onRepeatDaysChange,
                                onRepeatDayToggle = onRepeatDayToggle,
                                repeatInterval = uiState.repeatInterval,
                                onRepeatIntervalChange = onRepeatIntervalChange,
                                monthlyDay = uiState.monthlyDay,
                                onMonthlyDayChange = onMonthlyDayChange,
                                yearlyMonth = uiState.yearlyMonth,
                                yearlyDay = uiState.yearlyDay,
                                onYearlyMonthAndDayChange = onYearlyMonthAndDayChange,
                                is24HourFormat = uiState.is24HourFormat
                            )
                        }
                        ReminderTriggerType.LOCATION -> {
                            ReminderLocationConfigSection(
                                locationName = uiState.locationName,
                                onLocationNameChange = onLocationNameChange,
                                latitude = uiState.latitude,
                                onLatitudeChange = onLatitudeChange,
                                longitude = uiState.longitude,
                                onLongitudeChange = onLongitudeChange,
                                radiusMeters = uiState.radiusMeters,
                                onRadiusChange = onRadiusChange,
                                transitionType = uiState.geofenceTransitionType,
                                onTransitionTypeChange = onTransitionTypeChange,
                                timeWindow = uiState.locationTimeWindow,
                                onTimeWindowChange = onLocationTimeWindowChange,
                                is24HourFormat = uiState.is24HourFormat
                            )
                        }
                        ReminderTriggerType.WIFI -> {
                            ReminderWifiConfigSection(
                                ssid = uiState.wifiSsid,
                                onSsidChange = onWifiSsidChange,
                                bssid = uiState.wifiBssid,
                                onBssidChange = onWifiBssidChange,
                                triggerType = uiState.wifiTriggerType,
                                onTriggerTypeChange = onWifiTriggerTypeChange,
                                timeWindow = uiState.wifiTimeWindow,
                                onTimeWindowChange = onWifiTimeWindowChange,
                                is24HourFormat = uiState.is24HourFormat
                            )
                        }
                    }
                }
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
                    isPersistentAlert = uiState.isPersistentAlert,
                    onPersistentAlertChange = onPersistentAlertChange,
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
        ReMindAlertDialog(
            onDismissRequest = onDismissPermissionDialog,
            title = stringResource(R.string.reminder_permission_dialog_title),
            text = stringResource(R.string.reminder_permission_dialog_message),
            confirmButtonText = stringResource(R.string.action_go_to_settings),
            onConfirmClick = onNavigateToPermissions,
            dismissButtonText = stringResource(R.string.action_save_anyway),
            onDismissClick = onSaveAnyway
        )
    }

    // Discard Changes Dialog
    if (uiState.showDiscardDialog) {
        ReMindAlertDialog(
            onDismissRequest = onDismissDiscardDialog,
            title = stringResource(R.string.discard_changes_title),
            text = stringResource(R.string.discard_changes_message),
            confirmButtonText = stringResource(R.string.action_discard),
            onConfirmClick = onConfirmDiscard,
            dismissButtonText = stringResource(R.string.action_cancel),
            onDismissClick = onDismissDiscardDialog
        )
    }
}

