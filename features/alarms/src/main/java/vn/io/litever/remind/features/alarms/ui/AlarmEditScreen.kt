package vn.io.litever.remind.features.alarms.ui

import vn.io.litever.brand.tokens.LiteverTokens
import vn.io.litever.remind.core.designsystem.theme.spacing


import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.ui.platform.LocalInspectionMode
import vn.io.litever.remind.core.ads.api.AdPlacement
import vn.io.litever.remind.core.ads.api.LocalAdManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.snapshotFlow
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import vn.io.litever.remind.core.designsystem.theme.errorColors
import vn.io.litever.remind.core.designsystem.components.MissionSelectionBottomSheet
import vn.io.litever.remind.core.designsystem.components.ReMindBottomBar
import vn.io.litever.remind.core.designsystem.components.ReMindGroupCard
import vn.io.litever.remind.core.designsystem.components.ReMindSettingIcon
import vn.io.litever.remind.core.designsystem.components.ReMindSettingsGroup
import vn.io.litever.remind.core.designsystem.components.LvTopAppBar
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import java.time.DayOfWeek
import vn.io.litever.remind.core.model.MissionType
import vn.io.litever.remind.features.alarms.R
import vn.io.litever.remind.features.alarms.ui.components.AlarmSettingRow
import vn.io.litever.remind.core.designsystem.components.AutoSilenceBottomSheet
import vn.io.litever.remind.core.designsystem.components.SnoozeBottomSheet
import vn.io.litever.remind.features.alarms.ui.components.AnnounceLabelPreviewBottomSheet
import vn.io.litever.remind.features.alarms.ui.components.GentleAlarmBottomSheet
import vn.io.litever.remind.features.alarms.ui.components.NextAlarmHeader
import vn.io.litever.remind.features.alarms.ui.components.getRepeatSummaryText
import vn.io.litever.remind.features.alarms.ui.state.NextAlarmUiState
import vn.io.litever.remind.features.alarms.viewmodel.AlarmEditUiState
import vn.io.litever.remind.features.alarms.viewmodel.AlarmEditViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmEditRoute(
    alarmId: Long,
    onBackClick: () -> Unit,
    onRingtoneSelectionClick: (String?) -> Unit,
    onNavigateToPermissions: () -> Unit,
    onMissionClick: (vn.io.litever.remind.core.model.Mission) -> Unit,
    onPreviewClick: (Long) -> Unit,
    onNavigateToPaywall: (Int) -> Unit = {},
    navController: androidx.navigation.NavController,
    viewModel: AlarmEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val is24HourFormat by viewModel.is24HourFormat.collectAsState()
    val isPro by viewModel.isPro.collectAsState()
    val nextAlarmState by viewModel.nextAlarmState.collectAsState()

    var showMissionSelection by androidx.compose.runtime.saveable.rememberSaveable {
        mutableStateOf(
            false
        )
    }
    var isNavigatingToConfig by androidx.compose.runtime.saveable.rememberSaveable {
        mutableStateOf(
            false
        )
    }

    // Observe results from other screens
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle

    val updatedMission by savedStateHandle?.getStateFlow<vn.io.litever.remind.core.model.Mission?>(
        "updatedMission",
        null
    )
        ?.collectAsState() ?: remember { mutableStateOf(null) }

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

    LaunchedEffect(updatedMission) {
        updatedMission?.let {
            if (isNavigatingToConfig || showMissionSelection) {
                viewModel.addMission(it)
                showMissionSelection = false
                isNavigatingToConfig = false
            } else {
                viewModel.updateMission(it)
            }
            savedStateHandle?.remove<vn.io.litever.remind.core.model.Mission>("updatedMission")
        }
    }

    LaunchedEffect(returnedSnoozeEnabled, returnedSnoozeInterval, returnedSnoozeRepeatCount) {
        if (returnedSnoozeEnabled != null || returnedSnoozeInterval != null || returnedSnoozeRepeatCount != null) {
            viewModel.updateSnoozeSettings(
                enabled = returnedSnoozeEnabled ?: uiState.snoozeEnabled,
                interval = returnedSnoozeInterval ?: uiState.snoozeInterval,
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

    val lifecycleOwner = LocalLifecycleOwner.current
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? android.app.Activity
    val adManager = vn.io.litever.remind.core.ads.api.LocalAdManager.current
    val isTtsSpeaking by viewModel.isTtsSpeaking.collectAsState()
    val isTtsLanguageMissing by viewModel.isTtsLanguageMissing.collectAsState()
    val ttsLanguage by viewModel.ttsLanguage.collectAsState()

    // Reset navigation flag when returning to screen
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshPermissions()
                if (isNavigatingToConfig && savedStateHandle?.contains("updatedMission") == false) {
                    isNavigatingToConfig = false
                }
                // Load Interstitial ad for saving alarm
                adManager.loadAd(vn.io.litever.remind.core.ads.api.AdPlacement.SAVE_ALARM_INTERSTITIAL)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(alarmId) {
        viewModel.loadAlarm(alarmId)
    }

    var showDiscardDialog by remember { mutableStateOf(false) }

    BackHandler {
        if (viewModel.hasChanges()) {
            showDiscardDialog = true
        } else {
            viewModel.discardChanges(onBackClick)
        }
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { if (showDiscardDialog) showDiscardDialog = false },
            title = { Text(stringResource(R.string.discard_changes_title)) },
            text = { Text(stringResource(R.string.discard_changes_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        if (showDiscardDialog) showDiscardDialog = false
                        viewModel.stopRingtonePlayback()
                        viewModel.saveAlarm {
                            if (activity != null) {
                                adManager.showAd(activity, vn.io.litever.remind.core.ads.api.AdPlacement.SAVE_ALARM_INTERSTITIAL) {
                                    onBackClick()
                                }
                            } else {
                                onBackClick()
                            }
                        }
                    }
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        if (showDiscardDialog) showDiscardDialog = false
                        viewModel.stopRingtonePlayback()
                        viewModel.discardChanges(onBackClick)
                    }
                ) {
                    Text(stringResource(R.string.action_discard))
                }
            }
        )
    }

    if (showMissionSelection) {
        MissionSelectionBottomSheet(
            onDismissRequest = { if (showMissionSelection) showMissionSelection = false },
            onMissionTypeSelected = { type ->
                if (showMissionSelection) showMissionSelection = false
                isNavigatingToConfig = true
                onMissionClick(
                    vn.io.litever.remind.core.model.Mission(
                        alarmId = uiState.id,
                        type = type,
                        order = -1 // Indication for adding
                    )
                )
            }
        )
    }

    AlarmEditScreen(
        uiState = uiState,
        nextAlarmState = nextAlarmState,
        is24HourFormat = is24HourFormat,
        onBackClick = {
            if (viewModel.hasChanges()) {
                showDiscardDialog = true
            } else {
                viewModel.discardChanges(onBackClick)
            }
        },
        onSaveClick = {
            viewModel.stopRingtonePlayback()
            viewModel.saveAlarm {
                if (activity != null) {
                    adManager.showAd(activity, vn.io.litever.remind.core.ads.api.AdPlacement.SAVE_ALARM_INTERSTITIAL) {
                        onBackClick()
                    }
                } else {
                    onBackClick()
                }
            }
        },
        onSaveAnyway = {
            viewModel.saveAnyway {
                if (activity != null) {
                    adManager.showAd(activity, vn.io.litever.remind.core.ads.api.AdPlacement.SAVE_ALARM_INTERSTITIAL) {
                        onBackClick()
                    }
                } else {
                    onBackClick()
                }
            }
        },
        onDismissPermissionDialog = viewModel::dismissPermissionDialog,
        onTimeChange = viewModel::updateTime,
        onLabelChange = viewModel::updateLabel,
        onMessageChange = viewModel::updateMessage,
        onRepeatDayToggle = viewModel::toggleRepeatDay,
        onVibrationToggle = viewModel::updateVibration,
        onRingtoneClick = {
            viewModel.stopRingtonePlayback()
            onRingtoneSelectionClick(uiState.ringtoneUri)
        },
        onSnoozeSettingsChange = viewModel::updateSnoozeSettings,
        onAutoSilenceChange = viewModel::updateAutoSilence,
        onNavigateToPermissions = onNavigateToPermissions,
        onVolumeChange = viewModel::updateVolume,
        onTogglePreview = viewModel::toggleRingtonePlayback,
        onDateChange = viewModel::updateDate,
        onGradualVolumeChange = viewModel::updateGradualVolumeDuration,
        onUseAlarmStreamChange = viewModel::updateUseAlarmStream,
        onOverrideDndChange = viewModel::updateOverrideDndEnabled,
        onAddMissionClick = {
            viewModel.stopRingtonePlayback()
            showMissionSelection = true
        },
        onMissionClick = {
            viewModel.stopRingtonePlayback()
            onMissionClick(it)
        },
        onMissionRemove = viewModel::removeMission,
        onPreviewClick = {
            viewModel.stopRingtonePlayback()
            viewModel.preparePreview()
            onPreviewClick(uiState.id)
        },
        onDismissDndPermissionDialog = {
            viewModel.dismissDndPermissionDialog()
            onBackClick() // navigate back when dialog is dismissed, as alarm is already saved
        },
        onNavigateToDndSettings = {
            viewModel.dismissDndPermissionDialog()
            val intent = android.content.Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            context.startActivity(intent)
        },
        onVolumeBoostSave = viewModel::updateVolumeBoostSettings,
        onPreviewGentle = viewModel::previewGentleAlarm,
        onPreviewExtraLoud = viewModel::previewExtraLoud,
        onPreviewTimeTts = viewModel::previewTimeAnnouncementTts,
        onStopPreview = viewModel::stopRingtonePlayback,
        onAnnounceLabelTtsChange = viewModel::updateAnnounceLabelTts,
        onPreviewLabelTts = { viewModel.previewLabelAnnouncementTts() },
        isTtsSpeaking = isTtsSpeaking,
        isTtsLanguageMissing = isTtsLanguageMissing,
        selectedTtsLanguage = ttsLanguage,
        onTtsLanguageSelected = viewModel::updateTtsLanguage,
        onInstallTtsVoiceData = {
            runCatching {
                context.startActivity(
                    android.content.Intent(android.speech.tts.TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA)
                )
            }.onFailure {
                runCatching {
                    context.startActivity(android.content.Intent("com.android.settings.TTS_SETTINGS"))
                }
            }
        },
        onOpenTtsSettings = {
            runCatching {
                context.startActivity(android.content.Intent("com.android.settings.TTS_SETTINGS"))
            }.onFailure {
                runCatching {
                    context.startActivity(
                        android.content.Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    )
                }
            }
        },
        onWakeUpCheckChange = viewModel::updateWakeUpCheckMinutes,
        isPro = isPro,
        onNavigateToPaywall = onNavigateToPaywall
    )
}

@OptIn(ExperimentalMaterial3Api::class)


@Composable
fun AlarmEditScreen(
    uiState: AlarmEditUiState,
    nextAlarmState: NextAlarmUiState,
    is24HourFormat: Boolean,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    onSaveAnyway: () -> Unit,
    onDismissPermissionDialog: () -> Unit,
    onTimeChange: (LocalTime) -> Unit,
    onLabelChange: (String) -> Unit,
    onMessageChange: (String) -> Unit,
    onRepeatDayToggle: (DayOfWeek) -> Unit,
    onVibrationToggle: (Boolean) -> Unit,
    onRingtoneClick: () -> Unit,
    onSnoozeSettingsChange: (Boolean, Int, Int) -> Unit,
    onAutoSilenceChange: (Int) -> Unit,
    onNavigateToPermissions: () -> Unit,
    onVolumeChange: (Int) -> Unit,
    onTogglePreview: () -> Unit,
    onDateChange: (LocalDate?) -> Unit,
    onGradualVolumeChange: (Int) -> Unit,
    onUseAlarmStreamChange: (Boolean) -> Unit,
    onOverrideDndChange: (Boolean) -> Unit,
    onAddMissionClick: () -> Unit,
    onMissionClick: (vn.io.litever.remind.core.model.Mission) -> Unit,
    onMissionRemove: (vn.io.litever.remind.core.model.Mission) -> Unit,
    onPreviewClick: () -> Unit,
    onDismissDndPermissionDialog: () -> Unit,
    onNavigateToDndSettings: () -> Unit,
    onVolumeBoostSave: (Int, Boolean, Boolean) -> Unit = { duration, _, _ -> onGradualVolumeChange(duration) },
    onPreviewGentle: (Int) -> Unit = {},
    onPreviewExtraLoud: () -> Unit = {},
    onPreviewTimeTts: () -> Unit = {},
    onStopPreview: () -> Unit = {},
    onAnnounceLabelTtsChange: (Boolean) -> Unit = {},
    onPreviewLabelTts: () -> Unit = {},
    isTtsSpeaking: Boolean = false,
    isTtsLanguageMissing: Boolean = false,
    selectedTtsLanguage: vn.io.litever.remind.core.common.audio.TtsLanguageOption = vn.io.litever.remind.core.common.audio.TtsLanguageOption.AUTO,
    onTtsLanguageSelected: (vn.io.litever.remind.core.common.audio.TtsLanguageOption) -> Unit = {},
    onInstallTtsVoiceData: () -> Unit = {},
    onOpenTtsSettings: () -> Unit = {},
    onWakeUpCheckChange: (Int) -> Unit = {},
    isPro: Boolean = false,
    onNavigateToPaywall: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        )
        return
    }

    val timePickerState = androidx.compose.runtime.key(uiState.id, is24HourFormat) {
        rememberTimePickerState(
            initialHour = uiState.time.hour,
            initialMinute = uiState.time.minute,
            is24Hour = is24HourFormat
        )
    }

    var showTimePicker by remember { mutableStateOf(false) }
    var showGradualVolumeSheet by remember { mutableStateOf(false) }
    var showLabelTtsPreviewSheet by remember { mutableStateOf(false) }
    val sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)

    if (showTimePicker) {
        TimePickerDialog(
            title = {
                Text(
                    stringResource(R.string.set_alarm_time),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(
                        bottom = MaterialTheme.spacing.medium,
                    )
                )
            },
            onDismissRequest = { if (showTimePicker) showTimePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        if (showTimePicker) showTimePicker = false
                    }
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        if (showTimePicker) showTimePicker = false
                    },
                    modifier = Modifier.padding(end = MaterialTheme.spacing.medium)
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        ) {
            TimePicker(
                state = timePickerState,
            )
        }
    }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = uiState.date?.atStartOfDay(ZoneId.systemDefault())?.toInstant()
            ?.toEpochMilli(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val date = Instant.ofEpochMilli(utcTimeMillis)
                    .atZone(ZoneId.of("UTC"))
                    .toLocalDate()
                return !date.isBefore(LocalDate.now())
            }
        }
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { if (showDatePicker) showDatePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.of("UTC"))
                                .toLocalDate()
                            onDateChange(date)
                        }
                        if (showDatePicker) showDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        if (showDatePicker) showDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                title = {
                    Text(
                        stringResource(R.string.set_alarm_date),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(
                            top = MaterialTheme.spacing.medium,
                            bottom = MaterialTheme.spacing.small,
                            start = MaterialTheme.spacing.medium,
                            end = MaterialTheme.spacing.medium
                        )
                    )
                }
            )
        }

    }

    // Sync state time when picker changes
    LaunchedEffect(timePickerState.hour, timePickerState.minute) {
        onTimeChange(LocalTime.of(timePickerState.hour, timePickerState.minute))
    }

    if (uiState.showPermissionDialog) {
        AlertDialog(
            onDismissRequest = onDismissPermissionDialog,
            title = { Text(stringResource(R.string.permission_dialog_title)) },
            text = { Text(stringResource(R.string.permission_dialog_message)) },
            confirmButton = {
                Button(onClick = onNavigateToPermissions) {
                    Text(stringResource(R.string.action_go_to_settings))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onSaveAnyway) {
                    Text(stringResource(R.string.action_save_anyway))
                }
            }
        )
    }

    if (uiState.showDndPermissionDialog) {
        AlertDialog(
            onDismissRequest = onDismissDndPermissionDialog,
            title = { Text(stringResource(R.string.dnd_permission_dialog_title)) },
            text = { Text(stringResource(R.string.dnd_permission_dialog_message)) },
            confirmButton = {
                Button(onClick = onNavigateToDndSettings) {
                    Text(stringResource(R.string.action_go_to_settings))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onDismissDndPermissionDialog) {
                    Text(stringResource(R.string.close_text))
                }
            }
        )
    }

    var showSnoozeSheet by remember { mutableStateOf(false) }

    if (showSnoozeSheet) {
        SnoozeBottomSheet(
            enabled = uiState.snoozeEnabled,
            interval = uiState.snoozeInterval,
            repeatCount = uiState.snoozeRepeatCount,
            onEnabledChange = {
                onSnoozeSettingsChange(
                    it,
                    uiState.snoozeInterval,
                    uiState.snoozeRepeatCount
                )
            },
            onIntervalChange = {
                onSnoozeSettingsChange(
                    uiState.snoozeEnabled,
                    it,
                    uiState.snoozeRepeatCount
                )
            },
            onRepeatCountChange = {
                onSnoozeSettingsChange(
                    uiState.snoozeEnabled,
                    uiState.snoozeInterval,
                    it
                )
            },
            onDismissRequest = { if (showSnoozeSheet) showSnoozeSheet = false }
        )
    }

    var showAutoSilenceSheet by remember { mutableStateOf(false) }

    if (showAutoSilenceSheet) {
        AutoSilenceBottomSheet(
            currentMinutes = uiState.autoSilenceMinutes,
            onMinutesSelect = {
                onAutoSilenceChange(it)
                if (showAutoSilenceSheet) showAutoSilenceSheet = false
            },
            onDismissRequest = { if (showAutoSilenceSheet) showAutoSilenceSheet = false }
        )
    }

    var showWakeUpCheckSheet by remember { mutableStateOf(false) }

    if (showWakeUpCheckSheet) {
        vn.io.litever.remind.features.alarms.ui.components.WakeUpCheckBottomSheet(
            currentMinutes = uiState.wakeUpCheckMinutes,
            isPro = isPro,
            onSelectMinutes = onWakeUpCheckChange,
            onNavigateToPaywall = onNavigateToPaywall,
            onDismissRequest = { showWakeUpCheckSheet = false }
        )
    }

    if (showGradualVolumeSheet) {
        GentleAlarmBottomSheet(
            currentDuration = uiState.gradualVolumeDurationSeconds,
            extraLoudEnabled = uiState.extraLoudEnabled,
            announceTimeTts = uiState.announceTimeTts,
            isPro = isPro,
            onPreviewGentle = onPreviewGentle,
            onPreviewExtraLoud = onPreviewExtraLoud,
            onPreviewTimeTts = onPreviewTimeTts,
            onStopPreview = onStopPreview,
            onSave = { duration, extraLoud, announceTime ->
                onVolumeBoostSave(duration, extraLoud, announceTime)
            },
            onNavigateToPaywall = onNavigateToPaywall,
            onDismissRequest = { if (showGradualVolumeSheet) showGradualVolumeSheet = false },
            sheetState = sheetState
        )
    }

    if (showLabelTtsPreviewSheet) {
        AnnounceLabelPreviewBottomSheet(
            label = uiState.label,
            isSpeaking = isTtsSpeaking,
            isLanguageDataMissing = isTtsLanguageMissing,
            selectedLanguage = selectedTtsLanguage,
            onLanguageSelected = onTtsLanguageSelected,
            onPreviewClick = onPreviewLabelTts,
            onStopClick = onStopPreview,
            onInstallVoiceDataClick = onInstallTtsVoiceData,
            onOpenTtsSettingsClick = onOpenTtsSettings,
            onDismissRequest = { if (showLabelTtsPreviewSheet) showLabelTtsPreviewSheet = false }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                LvTopAppBar(
                    title = stringResource(if (uiState.id == 0L) R.string.add_alarm_title else R.string.edit_alarm_title),
                    onBackClick = onBackClick
                )
                NextAlarmHeader(
                    modifier = Modifier.padding(
                        start = MaterialTheme.spacing.medium,
                        end = MaterialTheme.spacing.medium,
                        bottom = MaterialTheme.spacing.small
                    ),
                    state = nextAlarmState
                )
            }
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
                    OutlinedButton(onClick = onPreviewClick) {
                        Text(stringResource(R.string.action_preview))
                    }
                    Button(
                        onClick = onSaveClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.save))
                    }
                }
                if (!LocalInspectionMode.current) {
                    Spacer(modifier = Modifier.height(4.dp))
                    LocalAdManager.current.MicroAdStrip(
                        placement = AdPlacement.EDIT_BOTTOM_STRIP,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(all = MaterialTheme.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {

            item {
                // Group 1: Time Selector
                ReMindGroupCard {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.large)
                            .clickable { showTimePicker = true },

                        ) {
                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
                        Row(
                            verticalAlignment = Alignment.Bottom
                        ) {
                            val timeFormatter = remember(is24HourFormat) {
                                DateTimeFormatter.ofPattern(if (is24HourFormat) "HH:mm" else "hh:mm")
                            }
                            Text(
                                text = uiState.time.format(timeFormatter),
                                style = MaterialTheme.typography.displayLarge.copy(
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = (-2).sp
                                )
                            )
                            if (!is24HourFormat) {
                                Text(
                                    text = uiState.time.format(DateTimeFormatter.ofPattern("a"))
                                        .uppercase(),
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        color = MaterialTheme.colorScheme.secondary,),
                                    modifier = Modifier.padding(bottom = MaterialTheme.spacing.smallMedium)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = ReMindIcons.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(MaterialTheme.spacing.medium),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.width(MaterialTheme.spacing.extraSmall))
                            Text(
                                text = stringResource(R.string.tap_to_edit_time),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                        alpha = 0.6f
                                    )
                                )
                            )

                        }
                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
                    }
                }
            }

            item {
                // Group 2: Repeat Selector (Separated)
                ReMindGroupCard {
                    RepeatDaySelector(
                        selectedDays = uiState.repeatDays,
                        time = uiState.time,
                        date = uiState.date,
                        onDayToggle = onRepeatDayToggle,
                        onShowDatePicker = { showDatePicker = true },
                        modifier = Modifier.padding(MaterialTheme.spacing.medium)
                    )
                }
            }

            item {
                // Group 3: Content (Label & Message)
                ReMindGroupCard {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Label Field
                        Column {
                            Text(
                                text = stringResource(R.string.alarm_label_title),
                                style = LiteverTokens.typography.labelSmallEmphasized,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            val labelState = rememberTextFieldState(uiState.label)
                            LaunchedEffect(uiState.label) {
                                if (labelState.text.toString() != uiState.label) {
                                    labelState.setTextAndPlaceCursorAtEnd(uiState.label)
                                }
                            }
                            LaunchedEffect(labelState) {
                                snapshotFlow { labelState.text.toString() }.collect {
                                    if (it != uiState.label) {
                                        onLabelChange(it)
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BasicTextField(
                                    state = labelState,
                                    modifier = Modifier.weight(1f),
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                                        color = MaterialTheme.colorScheme.onSurface
                                    ),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Text,
                                        imeAction = ImeAction.Next
                                    ),
                                    lineLimits = TextFieldLineLimits.SingleLine,
                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                    decorator = { innerTextField ->
                                        Box(contentAlignment = Alignment.CenterStart) {
                                            if (labelState.text.isEmpty()) {
                                                Text(
                                                    text = stringResource(R.string.alarm_label_placeholder),
                                                    style = MaterialTheme.typography.bodyLarge.copy(
                                                        color = MaterialTheme.colorScheme.outlineVariant
                                                    )
                                                )
                                            }
                                            innerTextField()
                                        }
                                    }
                                )

                                if (labelState.text.isNotEmpty()) {
                                    IconButton(
                                        onClick = { labelState.clearText() },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = ReMindIcons.Close,
                                            contentDescription = "Clear label",
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = ReMindIcons.NewLabel,
                                        contentDescription = "Ghi chú",
                                        tint = MaterialTheme.colorScheme.outlineVariant
                                    )
                                }
                            }
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )

                        // Message Field
                        Column {
                            Text(
                                text = stringResource(R.string.alarm_message_title),
                                style = LiteverTokens.typography.labelSmallEmphasized,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            val messageState = rememberTextFieldState(uiState.message)
                            LaunchedEffect(uiState.message) {
                                if (messageState.text.toString() != uiState.message) {
                                    messageState.setTextAndPlaceCursorAtEnd(uiState.message)
                                }
                            }
                            LaunchedEffect(messageState) {
                                snapshotFlow { messageState.text.toString() }.collect {
                                    if (it != uiState.message) {
                                        onMessageChange(it)
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                BasicTextField(
                                    state = messageState,
                                    modifier = Modifier.weight(1f),
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                                        color = MaterialTheme.colorScheme.onSurface
                                    ),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Text,
                                        imeAction = ImeAction.Done
                                    ),
                                    lineLimits = TextFieldLineLimits.MultiLine(maxHeightInLines = 4),
                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                    decorator = { innerTextField ->
                                        Box(contentAlignment = Alignment.TopStart) {
                                            if (messageState.text.isEmpty()) {
                                                Text(
                                                    text = stringResource(R.string.alarm_message_placeholder),
                                                    style = MaterialTheme.typography.bodyLarge.copy(
                                                        color = MaterialTheme.colorScheme.outlineVariant
                                                    )
                                                )
                                            }
                                            innerTextField()
                                        }
                                    }
                                )

                                if (messageState.text.isNotEmpty()) {
                                    IconButton(
                                        onClick = { messageState.clearText() },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = ReMindIcons.Close,
                                            contentDescription = "Clear message",
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = ReMindIcons.EditNote,
                                        contentDescription = "Ghi chú",
                                        tint = MaterialTheme.colorScheme.outlineVariant,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .padding(top = 2.dp)
                                    )
                                }
                            }
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )

                        // Announce Label TTS (Pro)
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (!isPro && !uiState.announceLabelTts) {
                                            onNavigateToPaywall(2)
                                        } else {
                                            onAnnounceLabelTtsChange(!uiState.announceLabelTts)
                                        }
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = ReMindIcons.Speaker,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.announce_label_tts_title),
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        vn.io.litever.remind.core.designsystem.components.ReMindProDiamondTag()
                                    }
                                    Text(
                                        text = stringResource(R.string.announce_label_tts_desc),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = uiState.announceLabelTts,
                                    onCheckedChange = { enabled ->
                                        if (!isPro && enabled) {
                                            onNavigateToPaywall(2)
                                        } else {
                                            onAnnounceLabelTtsChange(enabled)
                                        }
                                    }
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                FilledTonalButton(
                                    onClick = { showLabelTtsPreviewSheet = true }
                                ) {
                                    Icon(
                                        imageVector = ReMindIcons.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = stringResource(R.string.action_preview),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                // Group 4: Alert Settings
                ReMindSettingsGroup(
                    title = stringResource(R.string.sound),
                ) {
                    // Row 1: Ringtone Row
                    AlarmSettingRow(
                        title = uiState.ringtoneTitle,
                        icon = ReMindIcons.MusicNote,
                        onClick = onRingtoneClick
                    )

                    // Row 2: Progress
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(MaterialTheme.spacing.small)
                            .padding(start = 56.dp, end = MaterialTheme.spacing.extraLarge)
                    ) {
                        if (uiState.isRingtonePlaying || uiState.ringtoneProgress > 0f) {
                            LinearProgressIndicator(
                                progress = { uiState.ringtoneProgress },
                                modifier = Modifier
                                    .fillMaxWidth(),
                            )
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3F),
                        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.extraLarge)
                    )

                    // Row 3: Volume
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = MaterialTheme.spacing.medium),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ReMindSettingIcon(
                            imageVector = if (uiState.volume == 0) ReMindIcons.VolumeOff else ReMindIcons.VolumeUp,
                            selected = false,
                            modifier = Modifier.padding(start = MaterialTheme.spacing.medium)
                        )

                        val volumeSliderState = remember(uiState.maxVolume) {
                            SliderState(
                                value = uiState.volume.toFloat(),
                                trackRange = 0f..uiState.maxVolume.toFloat(),
                                steps = (uiState.maxVolume - 1).coerceAtLeast(0)
                            )
                        }
                        LaunchedEffect(uiState.volume) {
                            if (volumeSliderState.value.roundToInt() != uiState.volume) {
                                volumeSliderState.value = uiState.volume.toFloat()
                            }
                        }
                        Slider(
                            state = volumeSliderState,
                            onValueChange = {
                                volumeSliderState.value = it
                                onVolumeChange(it.roundToInt())
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = MaterialTheme.spacing.smallMedium),
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )

                        ReMindSettingIcon(
                            imageVector = if (uiState.isRingtonePlaying) ReMindIcons.Pause else ReMindIcons.PlayArrow,
                            selected = uiState.isRingtonePlaying,
                            onClick = onTogglePreview,
                            modifier = Modifier.padding(end = MaterialTheme.spacing.medium)
                        )
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3F),
                        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.extraLarge)
                    )

                    // Row 4: Vibration
                    AlarmSettingRow(
                        title = stringResource(R.string.vibration),
                        icon = ReMindIcons.Vibration,
                        iconSelected = uiState.vibrationEnabled,
                        onClick = { onVibrationToggle(!uiState.vibrationEnabled) },
                        trailingContent = {
                            Switch(
                                checked = uiState.vibrationEnabled,
                                onCheckedChange = onVibrationToggle
                            )
                        }
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3F),
                        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.extraLarge)
                    )

                    // Row 4: Volume Boost (Gentle Alarm + Extra Loud + Time TTS)
                    val summaryParts = buildList {
                        if (uiState.gradualVolumeDurationSeconds > 0) {
                            add(stringResource(R.string.seconds_unit_short, uiState.gradualVolumeDurationSeconds))
                        }
                        if (uiState.extraLoudEnabled) {
                            add(stringResource(R.string.extra_loud_title))
                        }
                        if (uiState.announceTimeTts) {
                            add(stringResource(R.string.time_announcement_title))
                        }
                    }
                    val gentleAlarmSummary = if (summaryParts.isEmpty()) {
                        stringResource(R.string.off)
                    } else {
                        summaryParts.joinToString(" · ")
                    }

                    AlarmSettingRow(
                        title = stringResource(R.string.volume_boost_sheet_title),
                        subtitle = gentleAlarmSummary,
                        icon = ReMindIcons.GraphicEq,
                        iconSelected = summaryParts.isNotEmpty(),
                        onClick = { showGradualVolumeSheet = true }
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3F),
                        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.extraLarge)
                    )

                    // Row 5: Alarm Stream Toggle
                    AlarmSettingRow(
                        title = stringResource(R.string.use_alarm_stream_title),
                        subtitle = if (uiState.useAlarmStream)
                            stringResource(R.string.use_alarm_stream_desc)
                        else
                            stringResource(R.string.use_media_stream_desc),
                        icon = if (uiState.useAlarmStream) ReMindIcons.AlarmOn else ReMindIcons.MusicNote,
                        iconSelected = uiState.useAlarmStream,
                        onClick = { onUseAlarmStreamChange(!uiState.useAlarmStream) },
                        trailingContent = {
                            Switch(
                                checked = uiState.useAlarmStream,
                                onCheckedChange = onUseAlarmStreamChange
                            )
                        }
                    )

                    // Row 6: Override DND
                    AlarmSettingRow(
                        title = stringResource(R.string.override_dnd_title),
                        subtitle = stringResource(R.string.override_dnd_desc),
                        icon = if (uiState.overrideDndEnabled) ReMindIcons.DoNotDisturbOff else ReMindIcons.DoNotDisturbOn,
                        iconSelected = uiState.overrideDndEnabled,
                        onClick = { onOverrideDndChange(!uiState.overrideDndEnabled) },
                        trailingContent = {
                            Switch(
                                checked = uiState.overrideDndEnabled,
                                onCheckedChange = onOverrideDndChange
                            )
                        }
                    )
                }

            }

            item {
                // Group 5: Alarm specific settings
                ReMindSettingsGroup(
                    title = stringResource(R.string.alarm_settings)
                ) {
                    // Snooze Row
                    val snoozeSummary = if (uiState.snoozeEnabled) {
                        val repeatLabel = when (uiState.snoozeRepeatCount) {
                            -1 -> stringResource(R.string.forever)
                            1 -> stringResource(R.string.one_time)
                            else -> stringResource(
                                R.string.times_unit,
                                uiState.snoozeRepeatCount
                            )
                        }
                        stringResource(
                            R.string.snooze_summary,
                            stringResource(R.string.minutes_unit, uiState.snoozeInterval),
                            repeatLabel
                        )
                    } else {
                        stringResource(R.string.off)
                    }

                    AlarmSettingRow(
                        title = stringResource(R.string.snooze),
                        subtitle = snoozeSummary,
                        icon = ReMindIcons.Snooze,
                        onClick = { showSnoozeSheet = true }
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3F),
                        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.extraLarge)
                    )

                    // Auto Silence Row
                    AlarmSettingRow(
                        title = stringResource(R.string.auto_silence_title),
                        subtitle = stringResource(
                            R.string.minutes_unit,
                            uiState.autoSilenceMinutes
                        ),
                        icon = ReMindIcons.AlarmOff,
                        onClick = { showAutoSilenceSheet = true }
                    )
                }
            }

            item {
                // Group 6: Missions (5-slot horizontal row matching 1/5 upsell design)
                ReMindSettingsGroup(
                    title = stringResource(vn.io.litever.remind.core.designsystem.R.string.mission_title),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(MaterialTheme.spacing.medium),
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(vn.io.litever.remind.core.designsystem.R.string.mission_title),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${uiState.missions.size}/5",
                                style = LiteverTokens.typography.labelMediumEmphasized,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            repeat(5) { slotIndex ->
                                val mission = uiState.missions.getOrNull(slotIndex)
                                val isSlotUnlocked = isPro || slotIndex == 0
                                MissionSlotCard(
                                    mission = mission,
                                    isUnlocked = isSlotUnlocked,
                                    onFilledClick = { mission?.let(onMissionClick) },
                                    onRemoveClick = { mission?.let(onMissionRemove) },
                                    onAddClick = {
                                        if (isSlotUnlocked) {
                                            onAddMissionClick()
                                        } else {
                                            onNavigateToPaywall(1)
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3F),
                        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.extraLarge)
                    )

                    val wakeUpSummary = if (uiState.wakeUpCheckMinutes <= 0) {
                        stringResource(R.string.off)
                    } else {
                        stringResource(R.string.minutes_unit, uiState.wakeUpCheckMinutes)
                    }
                    AlarmSettingRow(
                        title = stringResource(R.string.wakeup_check_title),
                        subtitle = wakeUpSummary,
                        icon = ReMindIcons.AlarmOn,
                        iconSelected = uiState.wakeUpCheckMinutes > 0,
                        onClick = { showWakeUpCheckSheet = true }
                    )
                }
            }


            item {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
            }
        }
    }
}

@Composable
fun RepeatDaySelector(
    selectedDays: List<DayOfWeek>,
    time: LocalTime,
    date: LocalDate?,
    onDayToggle: (DayOfWeek) -> Unit,
    onShowDatePicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allDays = DayOfWeek.entries
    val label = getRepeatSummaryText(selectedDays, time, date, isShortMode = true)

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = onShowDatePicker,
                colors = if (date != null) {
                    IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                } else {
                    IconButtonDefaults.iconButtonColors()
                }
            ) {
                Icon(
                    imageVector = ReMindIcons.CalendarMonth,
                    contentDescription = stringResource(R.string.set_alarm_date),
                )
            }
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            allDays.forEach { day ->
                val dayLabel = when (day) {
                    DayOfWeek.MONDAY -> stringResource(R.string.day_mon)
                    DayOfWeek.TUESDAY -> stringResource(R.string.day_tue)
                    DayOfWeek.WEDNESDAY -> stringResource(R.string.day_wed)
                    DayOfWeek.THURSDAY -> stringResource(R.string.day_thu)
                    DayOfWeek.FRIDAY -> stringResource(R.string.day_fri)
                    DayOfWeek.SATURDAY -> stringResource(R.string.day_sat)
                    DayOfWeek.SUNDAY -> stringResource(R.string.day_sun)
                }

                val isSelected = selectedDays.contains(day)
                val dayContent: @Composable () -> Unit = {
                    Text(
                        text = dayLabel,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                        )
                    )
                }
                if (isSelected) {
                    FilledTonalIconButton(
                        onClick = { onDayToggle(day) },
                        modifier = Modifier.weight(1f),
                        content = dayContent
                    )
                } else {
                    OutlinedIconButton(
                        onClick = { onDayToggle(day) },
                        modifier = Modifier.weight(1f),
                        content = dayContent
                    )
                }
            }
        }
    }
}


@Composable
private fun MissionSlotCard(
    mission: vn.io.litever.remind.core.model.Mission?,
    isUnlocked: Boolean,
    onFilledClick: () -> Unit,
    onRemoveClick: () -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val slotShape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    Box(
        modifier = modifier.height(58.dp),
        contentAlignment = Alignment.Center
    ) {
        if (mission != null) {
            val icon = when (mission.type) {
                MissionType.TYPING -> ReMindIcons.Keyboard
                MissionType.MATH -> ReMindIcons.Calculate
                MissionType.MEMORY_FIND_COLOR_TILES -> ReMindIcons.GridView
                MissionType.SHAKE -> ReMindIcons.Vibration
                MissionType.BARCODE -> ReMindIcons.QrCodeScanner
                MissionType.RANDOM -> ReMindIcons.Refresh
            }
            androidx.compose.material3.Surface(
                onClick = onFilledClick,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 4.dp, end = 4.dp),
                shape = slotShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = mission.type.name,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            androidx.compose.material3.Surface(
                onClick = onRemoveClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(20.dp),
                shape = androidx.compose.foundation.shape.CircleShape,
                color = MaterialTheme.colorScheme.errorContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = ReMindIcons.Close,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        } else {
            val borderColor = if (isUnlocked) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
            }
            androidx.compose.material3.Surface(
                onClick = onAddClick,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 4.dp, end = 4.dp),
                shape = slotShape,
                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isUnlocked) ReMindIcons.Add else ReMindIcons.Lock,
                        contentDescription = if (isUnlocked) "Add Mission" else "Pro Locked",
                        tint = if (isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, heightDp = 1500)
@Composable
fun AlarmEditScreenPreview() {
    ReMindTheme {
        AlarmEditScreen(
            uiState = AlarmEditUiState(
                isLoading = false,
                time = LocalTime.of(10, 30),
                repeatDays = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
                label = "Gym session",
                message = "Don't forget your water bottle!"
            ),
            nextAlarmState = NextAlarmUiState.Remaining(days = 0, hours = 14, minutes = 30),
            is24HourFormat = true,
            onBackClick = {},
            onSaveClick = {},
            onSaveAnyway = {},
            onDismissPermissionDialog = {},
            onTimeChange = {},
            onLabelChange = {},
            onMessageChange = {},
            onRepeatDayToggle = {},
            onVibrationToggle = {},
            onRingtoneClick = {},
            onSnoozeSettingsChange = { _, _, _ -> },
            onAutoSilenceChange = {},
            onNavigateToPermissions = {},
            onVolumeChange = {},
            onTogglePreview = {},
            onDateChange = {},
            onGradualVolumeChange = {},
            onUseAlarmStreamChange = {},
            onOverrideDndChange = {},
            onAddMissionClick = {},
            onMissionClick = {},
            onMissionRemove = {},
            onPreviewClick = {},
            onDismissDndPermissionDialog = {},
            onNavigateToDndSettings = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212, heightDp = 1500)
@Composable
fun AlarmEditScreenDarkPreview() {
    ReMindTheme(darkTheme = true) {
        AlarmEditScreen(
            uiState = AlarmEditUiState(
                isLoading = false,
                time = LocalTime.of(10, 30),
                repeatDays = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
                label = "Gym session",
                message = "Don't forget your water bottle!"
            ),
            nextAlarmState = NextAlarmUiState.Remaining(days = 0, hours = 14, minutes = 30),
            is24HourFormat = true,
            onBackClick = {},
            onSaveClick = {},
            onSaveAnyway = {},
            onDismissPermissionDialog = {},
            onTimeChange = {},
            onLabelChange = {},
            onMessageChange = {},
            onRepeatDayToggle = {},
            onVibrationToggle = {},
            onRingtoneClick = {},
            onSnoozeSettingsChange = { _, _, _ -> },
            onAutoSilenceChange = {},
            onNavigateToPermissions = {},
            onVolumeChange = {},
            onTogglePreview = {},
            onDateChange = {},
            onGradualVolumeChange = {},
            onUseAlarmStreamChange = {},
            onOverrideDndChange = {},
            onAddMissionClick = {},
            onMissionClick = {},
            onMissionRemove = {},
            onPreviewClick = {},
            onDismissDndPermissionDialog = {},
            onNavigateToDndSettings = {}
        )
    }
}

@Composable
private fun MissionRow(
    mission: vn.io.litever.remind.core.model.Mission,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon = when (mission.type) {
        MissionType.TYPING -> ReMindIcons.Keyboard
        MissionType.MATH -> ReMindIcons.Calculate
        MissionType.MEMORY_FIND_COLOR_TILES -> ReMindIcons.GridView
        MissionType.SHAKE -> ReMindIcons.Vibration
        MissionType.BARCODE -> ReMindIcons.QrCodeScanner
        MissionType.RANDOM -> ReMindIcons.Refresh
    }

    val title = when (mission.type) {
        MissionType.TYPING -> stringResource(vn.io.litever.remind.core.designsystem.R.string.mission_typing)
        MissionType.MATH -> stringResource(vn.io.litever.remind.core.designsystem.R.string.mission_math)
        MissionType.MEMORY_FIND_COLOR_TILES -> stringResource(vn.io.litever.remind.core.designsystem.R.string.mission_memory_tiles)
        MissionType.SHAKE -> stringResource(vn.io.litever.remind.core.designsystem.R.string.mission_shake)
        MissionType.BARCODE -> stringResource(vn.io.litever.remind.core.designsystem.R.string.mission_barcode)
        MissionType.RANDOM -> stringResource(vn.io.litever.remind.core.designsystem.R.string.mission_random)
    }
    ListItem(
        modifier = modifier.fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .clickable(onClick = onClick),
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        ),
        supportingContent = {
            Text(
                text = stringResource(
                    vn.io.litever.remind.core.designsystem.R.string.times_unit,
                    mission.repeatCount
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        leadingContent = {
            ReMindSettingIcon(
                imageVector = icon,
                selected = true
            )
        },
        trailingContent = {
            IconButton(
                onClick = onDelete,
                colors = IconButtonDefaults.errorColors()
            ) {
                Icon(
                    imageVector = ReMindIcons.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(MaterialTheme.spacing.mediumLarge),
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
        )
    }
}
