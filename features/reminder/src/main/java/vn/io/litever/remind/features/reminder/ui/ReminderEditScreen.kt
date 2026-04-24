package vn.io.litever.remind.features.reminder.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material3.OutlinedButton
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Snooze
import androidx.compose.material.icons.rounded.AlarmOff
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.foundation.BorderStroke
import vn.io.litever.remind.features.mission.ui.components.MissionSelectionBottomSheet
import vn.io.litever.remind.core.model.MissionType
import androidx.compose.material.icons.rounded.Snooze
import androidx.compose.material.icons.rounded.AlarmOff
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.ListItem
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.features.reminder.ui.state.NextReminderUiState
import vn.io.litever.remind.features.reminder.viewmodel.ReminderEditUiState
import vn.io.litever.remind.core.designsystem.components.*
import vn.io.litever.remind.core.model.DayOfWeek
import androidx.compose.material3.TimePicker
import vn.io.litever.remind.features.reminder.R
import vn.io.litever.remind.features.reminder.ui.components.NextReminderHeader
import vn.io.litever.remind.features.reminder.ui.components.getRepeatSummaryText
import vn.io.litever.remind.features.reminder.viewmodel.ReminderEditViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderEditRoute(
    reminderId: Long,
    onBackClick: () -> Unit,
    onRingtoneSelectionClick: (String?) -> Unit,
    onSnoozeSettingsClick: (Boolean, Int, Int) -> Unit,
    onNavigateToPermissions: () -> Unit,
    onAddMissionClick: () -> Unit,
    onMissionClick: (vn.io.litever.remind.core.model.Mission) -> Unit,
    navController: androidx.navigation.NavController,
    viewModel: ReminderEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val is24HourFormat by viewModel.is24HourFormat.collectAsState()
    val nextReminderState by viewModel.nextReminderState.collectAsState()

    var showMissionSelection by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }
    var isNavigatingToConfig by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }

    // Observe result from TypingMissionConfig
    val updatedMission by navController.currentBackStackEntry?.savedStateHandle
        ?.getStateFlow<vn.io.litever.remind.core.model.Mission?>("updatedMission", null)
        ?.collectAsState() ?: remember { mutableStateOf(null) }

    LaunchedEffect(updatedMission) {
        updatedMission?.let {
            if (isNavigatingToConfig || showMissionSelection) {
                viewModel.addMission(it)
                showMissionSelection = false
                isNavigatingToConfig = false
            } else {
                viewModel.updateMission(it)
            }
            navController.currentBackStackEntry?.savedStateHandle?.remove<vn.io.litever.remind.core.model.Mission>("updatedMission")
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    // Update snooze settings if returned from screen
    val returnedSnoozeEnabled =
        navController.currentBackStackEntry?.savedStateHandle?.getStateFlow<Boolean?>(
            "snoozeEnabled",
            null
        )?.collectAsState()
    val returnedSnoozeInterval =
        navController.currentBackStackEntry?.savedStateHandle?.getStateFlow<Int?>(
            "snoozeInterval",
            null
        )?.collectAsState()
    val returnedSnoozeRepeatCount =
        navController.currentBackStackEntry?.savedStateHandle?.getStateFlow<Int?>(
            "snoozeRepeatCount",
            null
        )?.collectAsState()

    LaunchedEffect(
        returnedSnoozeEnabled?.value,
        returnedSnoozeInterval?.value,
        returnedSnoozeRepeatCount?.value
    ) {
        if (returnedSnoozeEnabled?.value != null || returnedSnoozeInterval?.value != null || returnedSnoozeRepeatCount?.value != null) {
            viewModel.updateSnoozeSettings(
                enabled = returnedSnoozeEnabled?.value ?: uiState.snoozeEnabled,
                interval = returnedSnoozeInterval?.value ?: uiState.snoozeInterval,
                repeatCount = returnedSnoozeRepeatCount?.value ?: uiState.snoozeRepeatCount
            )
            // Clear the handle
            navController.currentBackStackEntry?.savedStateHandle?.remove<Boolean>("snoozeEnabled")
            navController.currentBackStackEntry?.savedStateHandle?.remove<Int>("snoozeInterval")
            navController.currentBackStackEntry?.savedStateHandle?.remove<Int>("snoozeRepeatCount")
        }
    }

    // Update ringtone if selected from picker
    val returnedRingtoneUri =
        navController.currentBackStackEntry?.savedStateHandle?.getStateFlow<String?>(
            "selectedRingtoneUri",
            null
        )?.collectAsState()
    
    // Use a special key to detect if a result was SENT at all, even if it's null
    val resultWasSet = navController.currentBackStackEntry?.savedStateHandle?.get<Boolean>("selectedRingtoneUri_set") ?: false

    LaunchedEffect(returnedRingtoneUri?.value, resultWasSet) {
        if (resultWasSet) {
            viewModel.updateRingtone(returnedRingtoneUri?.value)
            // Clear the handle
            navController.currentBackStackEntry?.savedStateHandle?.remove<String>("selectedRingtoneUri")
            navController.currentBackStackEntry?.savedStateHandle?.remove<Boolean>("selectedRingtoneUri_set")
        }
    }

    // Reset navigation flag when returning to screen
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshPermissions()
                if (isNavigatingToConfig) {
                    val hasUpdatedMission = navController.currentBackStackEntry?.savedStateHandle?.contains("updatedMission") == true
                    if (!hasUpdatedMission) {
                        isNavigatingToConfig = false
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(reminderId) {
        viewModel.loadReminder(reminderId)
    }

    if (showMissionSelection) {
        MissionSelectionBottomSheet(
            onDismissRequest = { 
                if (!isNavigatingToConfig) {
                    showMissionSelection = false 
                }
            },
            onMissionTypeSelected = { type ->
                showMissionSelection = false
                if (type == vn.io.litever.remind.core.model.MissionType.TYPING) {
                    isNavigatingToConfig = true
                    onMissionClick(
                        vn.io.litever.remind.core.model.Mission(
                            reminderId = reminderId,
                            type = type,
                            order = -1 // Indication for adding
                        )
                    )
                } else {
                    viewModel.addMission(type)
                }
            }
        )
    }

    ReminderEditScreen(
        uiState = uiState,
        nextReminderState = nextReminderState,
        is24HourFormat = is24HourFormat,
        onBackClick = {
            viewModel.stopRingtonePlayback()
            onBackClick()
        },
        onSaveClick = {
            viewModel.stopRingtonePlayback()
            viewModel.saveReminder(onBackClick)
        },
        onSaveAnyway = {
            viewModel.saveAnyway(onBackClick)
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
        onSnoozeSettingsClick = {
            viewModel.stopRingtonePlayback()
            onSnoozeSettingsClick(
                uiState.snoozeEnabled,
                uiState.snoozeInterval,
                uiState.snoozeRepeatCount
            )
        },
        onAutoSilenceChange = viewModel::updateAutoSilence,
        onNavigateToPermissions = onNavigateToPermissions,
        onVolumeChange = viewModel::updateVolume,
        onTogglePreview = viewModel::toggleRingtonePlayback,
        onDateChange = viewModel::updateDate,
        onGradualVolumeChange = viewModel::updateGradualVolumeDuration,
        onAddMissionClick = { 
            viewModel.stopRingtonePlayback()
            showMissionSelection = true 
        },
        onMissionClick = {
            viewModel.stopRingtonePlayback()
            onMissionClick(it)
        },
        onMissionRemove = viewModel::removeMission
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderEditScreen(
    uiState: ReminderEditUiState,
    nextReminderState: NextReminderUiState,
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
    onSnoozeSettingsClick: () -> Unit,
    onAutoSilenceChange: (Int) -> Unit,
    onNavigateToPermissions: () -> Unit,
    onVolumeChange: (Int) -> Unit,
    onTogglePreview: () -> Unit,
    onDateChange: (LocalDate?) -> Unit,
    onGradualVolumeChange: (Int) -> Unit,
    onAddMissionClick: () -> Unit,
    onMissionClick: (vn.io.litever.remind.core.model.Mission) -> Unit,
    onMissionRemove: (vn.io.litever.remind.core.model.Mission) -> Unit,
    modifier: Modifier = Modifier
) {
    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
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
    val sheetState = rememberModalBottomSheetState()

    if (showTimePicker) {
        ReMindTimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = uiState.date?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
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
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.of("UTC"))
                            .toLocalDate()
                        onDateChange(date)
                    }
                    showDatePicker = false
                }) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Sync state time when picker changes
    LaunchedEffect(timePickerState.hour, timePickerState.minute) {
        onTimeChange(LocalTime.of(timePickerState.hour, timePickerState.minute))
    }

    if (uiState.showPermissionDialog) {
        ReMindAlertDialog(
            onDismissRequest = onDismissPermissionDialog,
            title = stringResource(R.string.permission_dialog_title),
            text = stringResource(R.string.permission_dialog_message),
            confirmButtonText = stringResource(R.string.action_go_to_settings),
            onConfirmClick = onNavigateToPermissions,
            dismissButtonText = stringResource(R.string.action_save_anyway),
            onDismissClick = onSaveAnyway
        )
    }

    var showAutoSilenceSheet by remember { mutableStateOf(false) }

    if (showAutoSilenceSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAutoSilenceSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            AutoSilenceBottomSheetContent(
                currentMinutes = uiState.autoSilenceMinutes,
                onMinutesSelect = {
                    onAutoSilenceChange(it)
                    showAutoSilenceSheet = false
                }
            )
        }
    }

    val context = LocalContext.current

    if (showGradualVolumeSheet) {
        ModalBottomSheet(
            onDismissRequest = { showGradualVolumeSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            GentleReminderBottomSheetContent(
                currentDuration = uiState.gradualVolumeDurationSeconds,
                onDurationSelect = {
                    onGradualVolumeChange(it)
                    showGradualVolumeSheet = false
                }
            )
        }
    }

    ReMindScaffold(
        topBar = {
            ReMindTopAppBar(
                title = stringResource(if (uiState.id == 0L) R.string.add_reminder_title else R.string.edit_reminder_title),
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            ReMindBottomBar {
                ReMindButton(
                    onClick = onSaveClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(R.string.save),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                NextReminderHeader(state = nextReminderState)
            }

                item {
                    // Group 1: Time Selector
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clip(MaterialTheme.shapes.extraLarge)
                            .clickable { showTimePicker = true },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                alpha = 0.3f
                            )
                        ),
                        shape = MaterialTheme.shapes.extraLarge,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    val timeFormatter = remember(is24HourFormat) {
                                        DateTimeFormatter.ofPattern(if (is24HourFormat) "HH:mm" else "hh:mm")
                                    }
                                    Text(
                                        text = uiState.time.format(timeFormatter),
                                        style = MaterialTheme.typography.displayLarge.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary,
                                            letterSpacing = (-2).sp
                                        )
                                    )
                                    if (!is24HourFormat) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = uiState.time.format(DateTimeFormatter.ofPattern("a")).uppercase(),
                                            style = MaterialTheme.typography.headlineSmall.copy(
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                                fontWeight = FontWeight.Bold
                                            ),
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Edit,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = stringResource(R.string.tap_to_edit_time),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    // Group 2: Repeat Selector (Separated)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                alpha = 0.3f
                            )
                        ),
                        shape = MaterialTheme.shapes.extraLarge,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                    ) {
                        RepeatDaySelector(
                            selectedDays = uiState.repeatDays,
                            time = uiState.time,
                            date = uiState.date,
                            onDayToggle = onRepeatDayToggle,
                            onShowDatePicker = { showDatePicker = true },
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                item {
                    // Group 3: Content (Label & Message)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                alpha = 0.3f
                            )
                        ),
                        shape = MaterialTheme.shapes.extraLarge,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(R.string.reminder_content_group_title),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            
                            ReMindTextField(
                                value = uiState.label,
                                onValueChange = onLabelChange,
                                label = stringResource(R.string.reminder_label_title),
                                placeholder = stringResource(R.string.reminder_label_placeholder),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                onClearClick = { onLabelChange("") }
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            ReMindTextField(
                                value = uiState.message,
                                onValueChange = onMessageChange,
                                label = stringResource(R.string.reminder_message_title),
                                placeholder = stringResource(R.string.reminder_message_placeholder),
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2,
                                maxLines = 4,
                                onClearClick = { onMessageChange("") }
                            )
                        }
                    }
                }

                item {
                    // Group 4: Alert Settings
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                alpha = 0.3f
                            )
                        ),
                        shape = MaterialTheme.shapes.extraLarge,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(R.string.sound),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            // Row 1: Ringtone Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(MaterialTheme.shapes.medium)
                                    .clickable { onRingtoneClick() },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                androidx.compose.material3.FilledTonalIconButton(
                                    onClick = onTogglePreview,
                                    modifier = Modifier.size(44.dp),
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    Icon(
                                        imageVector = if (uiState.isRingtonePlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                        contentDescription = null
                                    )
                                }

                                Column(modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 12.dp)) {
                                    Text(
                                        text = uiState.ringtoneTitle,
                                        style = MaterialTheme.typography.bodyLarge,
                                        maxLines = 1
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Rounded.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }

                            // Row 2: Progress
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .padding(start = 56.dp, end = 32.dp)
                            ) {
                                if (uiState.isRingtonePlaying || uiState.ringtoneProgress > 0f) {
                                    LinearProgressIndicator(
                                        progress = { uiState.ringtoneProgress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(2.dp)
                                            .clip(RoundedCornerShape(1.dp)),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Row 3: Volume & Vibration
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(MaterialTheme.shapes.medium)
                                        .background(
                                            MaterialTheme.colorScheme.surfaceVariant.copy(
                                                alpha = 0.5f
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (uiState.volume == 0) Icons.Rounded.VolumeOff else Icons.Rounded.VolumeUp,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Slider(
                                    value = uiState.volume.toFloat(),
                                    onValueChange = { onVolumeChange(it.roundToInt()) },
                                    valueRange = 0f..uiState.maxVolume.toFloat(),
                                    steps = uiState.maxVolume - 1,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 12.dp),
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary,
                                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                )

                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(MaterialTheme.shapes.medium)
                                        .background(
                                            if (uiState.vibrationEnabled) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        )
                                        .clickable { onVibrationToggle(!uiState.vibrationEnabled) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Vibration,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = if (uiState.vibrationEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Row 4: Gentle Reminder (Increasing Volume)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(MaterialTheme.shapes.medium)
                                    .clickable { showGradualVolumeSheet = true }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(MaterialTheme.shapes.medium)
                                        .background(
                                            if (uiState.gradualVolumeDurationSeconds > 0) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.GraphicEq,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = if (uiState.gradualVolumeDurationSeconds > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 12.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.gentle_reminder_title),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    val summary = if (uiState.gradualVolumeDurationSeconds == 0) {
                                        stringResource(R.string.off)
                                    } else if (uiState.gradualVolumeDurationSeconds < 60) {
                                        stringResource(R.string.seconds_unit_short, uiState.gradualVolumeDurationSeconds)
                                    } else {
                                        stringResource(R.string.minutes_unit_short, uiState.gradualVolumeDurationSeconds / 60)
                                    }
                                    Text(
                                        text = summary,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Rounded.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }

                item {
                    // Group 5: Alarm specific settings
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                alpha = 0.3f
                            )
                        ),
                        shape = MaterialTheme.shapes.extraLarge,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(R.string.alarm_settings),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            // Snooze Row
                            val snoozeSummary = if (uiState.snoozeEnabled) {
                                val repeatLabel = if (uiState.snoozeRepeatCount == -1) {
                                    stringResource(R.string.forever)
                                } else if (uiState.snoozeRepeatCount == 1) {
                                    stringResource(R.string.one_time)
                                } else {
                                    stringResource(R.string.times_unit, uiState.snoozeRepeatCount)
                                }
                                stringResource(
                                    R.string.snooze_summary,
                                    stringResource(R.string.minutes_unit, uiState.snoozeInterval),
                                    repeatLabel
                                )
                            } else {
                                stringResource(R.string.off)
                            }

                            ReminderSettingRow(
                                title = stringResource(R.string.snooze),
                                subtitle = snoozeSummary,
                                icon = Icons.Rounded.Snooze,
                                onClick = onSnoozeSettingsClick
                            )



                            // Auto Silence Row
                            ReminderSettingRow(
                                title = stringResource(R.string.auto_silence_title),
                                subtitle = stringResource(
                                    R.string.minutes_unit,
                                    uiState.autoSilenceMinutes
                                ),
                                icon = Icons.Rounded.AlarmOff,
                                onClick = { showAutoSilenceSheet = true }
                            )
                        }
                    }
                }

                item {
                    // Group 6: Missions
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                alpha = 0.3f
                            )
                        ),
                        shape = MaterialTheme.shapes.extraLarge,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(vn.io.litever.remind.core.designsystem.R.string.mission_title),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${uiState.missions.size}/5",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            uiState.missions.forEach { mission ->
                                MissionRow(
                                    mission = mission,
                                    onClick = { onMissionClick(mission) },
                                    onDelete = { onMissionRemove(mission) }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            
                            if (uiState.missions.size < 5) {
                                OutlinedButton(
                                    onClick = onAddMissionClick,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = MaterialTheme.shapes.medium,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp, 
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Add, 
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(vn.io.litever.remind.core.designsystem.R.string.add_mission))
                                }
                            }
                        }
                    }
                }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun RepeatDaySelector(
    selectedDays: List<DayOfWeek>,
    time: java.time.LocalTime,
    date: java.time.LocalDate?,
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
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            
            IconButton(onClick = onShowDatePicker) {
                Icon(
                    imageVector = Icons.Rounded.CalendarMonth,
                    contentDescription = stringResource(R.string.select_date),
                    tint = if (date != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .clickable { onDayToggle(day) },
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = MaterialTheme.shapes.medium,
                    border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = dayLabel,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                            ),
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReminderSettingRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun ReminderEditScreenPreview() {
    ReMindTheme {
        ReminderEditScreen(
            uiState = ReminderEditUiState(
                time = LocalTime.of(10, 30),
                repeatDays = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
                label = "Gym session",
                message = "Don't forget your water bottle!"
            ),
            nextReminderState = NextReminderUiState.Remaining(days = 0, hours = 14, minutes = 30),
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
            onSnoozeSettingsClick = {},
            onAutoSilenceChange = {},
            onNavigateToPermissions = {},
            onVolumeChange = {},
            onTogglePreview = {},
            onDateChange = {},
            onGradualVolumeChange = {},
            onAddMissionClick = {},
            onMissionClick = {},
            onMissionRemove = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun ReminderEditScreenDarkPreview() {
    ReMindTheme(darkTheme = true) {
        ReminderEditScreen(
            uiState = ReminderEditUiState(
                time = LocalTime.of(10, 30),
                repeatDays = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
                label = "Gym session",
                message = "Don't forget your water bottle!"
            ),
            nextReminderState = NextReminderUiState.Remaining(days = 0, hours = 14, minutes = 30),
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
            onSnoozeSettingsClick = {},
            onAutoSilenceChange = {},
            onNavigateToPermissions = {},
            onVolumeChange = {},
            onTogglePreview = {},
            onDateChange = {},
            onGradualVolumeChange = {},
            onAddMissionClick = {},
            onMissionClick = {},
            onMissionRemove = {}
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
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = when (mission.type) {
                vn.io.litever.remind.core.model.MissionType.TYPING -> Icons.Rounded.Keyboard
                vn.io.litever.remind.core.model.MissionType.MATH -> Icons.Rounded.Calculate
                vn.io.litever.remind.core.model.MissionType.SHAKE -> Icons.Rounded.Smartphone
                vn.io.litever.remind.core.model.MissionType.QR_CODE -> Icons.Rounded.QrCodeScanner
                else -> Icons.Rounded.Extension
            }

            val title = when (mission.type) {
                vn.io.litever.remind.core.model.MissionType.TYPING -> stringResource(vn.io.litever.remind.core.designsystem.R.string.mission_typing)
                vn.io.litever.remind.core.model.MissionType.MATH -> stringResource(vn.io.litever.remind.core.designsystem.R.string.mission_math)
                vn.io.litever.remind.core.model.MissionType.SHAKE -> stringResource(vn.io.litever.remind.core.designsystem.R.string.mission_shake)
                vn.io.litever.remind.core.model.MissionType.QR_CODE -> stringResource(vn.io.litever.remind.core.designsystem.R.string.mission_qr_code)
                else -> mission.type.name
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = stringResource(vn.io.litever.remind.core.designsystem.R.string.times_unit, mission.repeatCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun GentleReminderBottomSheetContent(
    currentDuration: Int,
    onDurationSelect: (Int) -> Unit
) {
    val options = listOf(0, 15, 30, 60)
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = stringResource(R.string.gentle_reminder_title),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(16.dp)
        )
        Text(
            text = stringResource(R.string.gentle_reminder_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        options.forEach { option ->
            val label = if (option == 0) {
                stringResource(R.string.off)
            } else if (option < 60) {
                stringResource(R.string.seconds_unit, option)
            } else {
                stringResource(R.string.minutes_unit, option / 60)
            }
            
            ListItem(
                headlineContent = { 
                    Text(
                        label,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                    ) 
                },
                leadingContent = {
                    RadioButton(
                        selected = currentDuration == option,
                        onClick = { onDurationSelect(option) }
                    )
                },
                modifier = Modifier.clickable { onDurationSelect(option) }
            )
        }
    }
}

@Composable
fun AutoSilenceBottomSheetContent(
    currentMinutes: Int,
    onMinutesSelect: (Int) -> Unit
) {
    val options = listOf(1, 3, 5, 10, 30)
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = stringResource(R.string.auto_silence_title),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(16.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        options.forEach { option ->
            ListItem(
                headlineContent = { 
                    Text(
                        stringResource(R.string.minutes_unit, option),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                    ) 
                },
                leadingContent = {
                    RadioButton(
                        selected = currentMinutes == option,
                        onClick = { onMinutesSelect(option) }
                    )
                },
                modifier = Modifier.clickable { onMinutesSelect(option) }
            )
        }
    }
}
