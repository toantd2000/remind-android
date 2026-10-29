package vn.io.litever.remind.features.reminders.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import vn.io.litever.remind.core.domain.scheduler.ReminderScheduler
import vn.io.litever.remind.core.alarm.ReminderSyncManager
import vn.io.litever.remind.core.alarm.alert.ReminderAlertManager
import vn.io.litever.remind.core.alarm.notification.ReminderNotificationManager
import vn.io.litever.remind.core.datastore.AlarmPreferencesDataSource
import vn.io.litever.remind.core.domain.repository.ReminderRepository
import vn.io.litever.remind.core.model.NotificationStyle

import vn.io.litever.remind.core.model.Reminder

import vn.io.litever.remind.core.model.ReminderTimeWindow

import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.core.model.UrgencyLevel
import vn.io.litever.remind.core.model.RepeatMode

import vn.io.litever.remind.core.model.ReminderMonthDay
import vn.io.litever.remind.core.model.ReminderAction
import vn.io.litever.remind.core.model.TimeTrigger
import vn.io.litever.remind.core.common.util.PermissionChecker
import vn.io.litever.remind.core.common.util.ReminderPermissionType
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import vn.io.litever.remind.core.common.audio.AudioPlayer
import vn.io.litever.remind.core.common.util.getAccessibleRingtoneUri
import java.time.LocalDate
import java.time.LocalTime
import java.time.DayOfWeek
import javax.inject.Inject

enum class ReminderRepeatInterval {
    NONE, DAILY, WEEKLY, MONTHLY, YEARLY, CUSTOM
}

enum class ReminderValidationErrorField {
    TITLE
}

data class ReminderEditUiState(
    val id: Long = 0,
    val title: String = "",
    val notes: String = "",
    val isEnabled: Boolean = true,
    val triggerType: ReminderTriggerType = ReminderTriggerType.FIXED_TIME,
    val time: LocalTime = LocalTime.now().plusMinutes(5).withSecond(0).withNano(0),
    val fixedTimes: List<LocalTime> = listOf(time),
    val date: LocalDate? = LocalDate.now(),
    val repeatInterval: ReminderRepeatInterval = ReminderRepeatInterval.NONE,
    val repeatDays: List<DayOfWeek> = emptyList(),
    val monthlyDays: List<Int> = emptyList(),
    val yearlyDates: List<ReminderMonthDay> = emptyList(),
    val intervalMinutes: Int? = null,
    val timeWindows: List<ReminderTimeWindow> = emptyList(),
    val urgencyLevel: UrgencyLevel = UrgencyLevel.MEDIUM,
    val notificationStyle: NotificationStyle = NotificationStyle.FULL_SCREEN,
    val isPersistentAlert: Boolean = false,
    val autoSilenceMinutes: Int = 5,
    val useAlarmStream: Boolean = true,
    val ringtoneUri: String? = null,
    val vibrationEnabled: Boolean = true,
    val volume: Int = 100,
    val isRingtonePlaying: Boolean = false,
    val snoozeEnabled: Boolean = true,
    val snoozeIntervalMinutes: Int = 5,
    val showPermissionDialog: Boolean = false,
    val missingPermissions: List<ReminderPermissionType> = emptyList(),
    val showDiscardDialog: Boolean = false,
    val isLoading: Boolean = false,
    val showTitleError: Boolean = false,
    val validationErrorField: ReminderValidationErrorField? = null,
    val validationErrorTrigger: Long = 0L
) {
    val timeWindow: ReminderTimeWindow?
        get() = timeWindows.firstOrNull()
}

@HiltViewModel
class ReminderEditViewModel @Inject constructor(
    @ApplicationContext private val context: Context? = null,
    savedStateHandle: SavedStateHandle = SavedStateHandle(),
    private val reminderRepository: ReminderRepository,
    private val preferencesDataSource: AlarmPreferencesDataSource,
    private val reminderScheduler: ReminderScheduler,
    private val reminderAlertManager: ReminderAlertManager? = null,
    private val reminderNotificationManager: ReminderNotificationManager? = null,
    private val permissionChecker: PermissionChecker,
    private val reminderSyncManager: ReminderSyncManager,
    private val audioPlayer: AudioPlayer? = null,
) : ViewModel() {

    private val reminderId: Long = savedStateHandle.get<Long>("reminderId") ?: 0L
    private val _uiState = MutableStateFlow(ReminderEditUiState())
    val uiState: StateFlow<ReminderEditUiState> = _uiState.asStateFlow()
    private var initialSnapshot: ReminderEditUiState? = null

    init {
        viewModelScope.launch {
            if (reminderId > 0L) {
                val existing = reminderRepository.getReminderById(reminderId)
                if (existing != null) {
                    val timeTrigger = existing.timeTrigger
                    val activeTriggerDate = timeTrigger?.exactDate ?: LocalDate.now()
                    
                    val repeatInterval = when (timeTrigger?.repeatMode) {
                        RepeatMode.NONE -> ReminderRepeatInterval.NONE
                        RepeatMode.DAILY -> ReminderRepeatInterval.DAILY
                        RepeatMode.WEEKLY -> ReminderRepeatInterval.WEEKLY
                        RepeatMode.MONTHLY -> ReminderRepeatInterval.MONTHLY
                        RepeatMode.YEARLY -> ReminderRepeatInterval.YEARLY
                        null -> ReminderRepeatInterval.NONE
                    }
                    
                    val existingTimes = timeTrigger?.time ?: emptyList()
                    val primaryTime = existingTimes.firstOrNull() ?: LocalTime.now()

                    _uiState.value = ReminderEditUiState(
                        id = existing.id,
                        title = existing.title,
                        notes = existing.notes,
                        isEnabled = existing.isEnabled,
                        triggerType = existing.triggerType,
                        time = primaryTime,
                        fixedTimes = if (existingTimes.isNotEmpty()) existingTimes else listOf(primaryTime),
                        date = activeTriggerDate,
                        repeatInterval = repeatInterval,
                        repeatDays = if (repeatInterval == ReminderRepeatInterval.WEEKLY) (timeTrigger?.repeatValues?.mapNotNull { v -> DayOfWeek.values().find { it.value == v } } ?: emptyList()) else emptyList(),
                        monthlyDays = timeTrigger?.monthlyDays ?: emptyList(),
                        yearlyDates = timeTrigger?.yearlyDates ?: emptyList(),
                        intervalMinutes = timeTrigger?.intervalMinutes,
                        timeWindows = timeTrigger?.timeWindows ?: (timeTrigger?.timeWindow?.let { listOf(it) } ?: emptyList()),
                        urgencyLevel = existing.action.urgencyLevel,
                        notificationStyle = existing.action.notificationStyle,
                        isPersistentAlert = existing.action.isPersistentAlert,
                        autoSilenceMinutes = existing.action.autoSilenceMinutes,
                        useAlarmStream = existing.action.useAlarmStream,
                        ringtoneUri = existing.action.ringtoneUri,
                        vibrationEnabled = existing.action.vibrationEnabled,
                        volume = existing.action.volume,
                        snoozeEnabled = existing.action.snoozeEnabled,
                        snoozeIntervalMinutes = existing.action.snoozeIntervalMinutes
                    )
                }
            }
            initialSnapshot = _uiState.value
        }
    }

    internal fun createDraftReminder(): Reminder {
        val state = _uiState.value
        val now = System.currentTimeMillis()

        val timeTrigger = if (state.triggerType == ReminderTriggerType.FIXED_TIME || state.triggerType == ReminderTriggerType.INTERVAL) {
            val repeatMode = when (state.repeatInterval) {
                ReminderRepeatInterval.NONE -> RepeatMode.NONE
                ReminderRepeatInterval.DAILY -> RepeatMode.DAILY
                ReminderRepeatInterval.WEEKLY -> RepeatMode.WEEKLY
                ReminderRepeatInterval.MONTHLY -> RepeatMode.MONTHLY
                ReminderRepeatInterval.YEARLY -> RepeatMode.YEARLY
                ReminderRepeatInterval.CUSTOM -> RepeatMode.NONE
            }
            val timeList = if (state.fixedTimes.isNotEmpty()) state.fixedTimes else listOf(state.time)
            TimeTrigger(
                time = timeList,
                intervalMinutes = state.intervalMinutes,
                exactDate = if (repeatMode == RepeatMode.NONE) state.date else null,
                repeatMode = repeatMode,
                repeatValues = if (repeatMode == RepeatMode.WEEKLY) state.repeatDays.map { it.value } else emptyList(),
                monthlyDays = if (repeatMode == RepeatMode.MONTHLY) state.monthlyDays else emptyList(),
                yearlyDates = if (repeatMode == RepeatMode.YEARLY) state.yearlyDates else emptyList(),
                timeWindows = state.timeWindows
            )
        } else null

        val action = ReminderAction(
            urgencyLevel = state.urgencyLevel,
            snoozeEnabled = state.snoozeEnabled,
            snoozeIntervalMinutes = state.snoozeIntervalMinutes,
            volume = state.volume,
            vibrationEnabled = state.vibrationEnabled,
            isPersistentAlert = if (state.notificationStyle == NotificationStyle.FULL_SCREEN) state.isPersistentAlert else false,
            useAlarmStream = state.useAlarmStream,
            autoSilenceMinutes = state.autoSilenceMinutes,
            ringtoneUri = state.ringtoneUri,
            notificationStyle = state.notificationStyle
        )

        return Reminder(
            id = state.id,
            title = state.title.trim(),
            notes = state.notes.trim(),
            triggerType = state.triggerType,
            timeTrigger = timeTrigger,
            action = action,
            createdAt = now,
            isEnabled = state.isEnabled
        )
    }

    fun saveReminder(onSuccess: () -> Unit) {
        if (_uiState.value.title.isBlank()) {
            _uiState.update {
                it.copy(
                    showTitleError = true,
                    validationErrorField = ReminderValidationErrorField.TITLE,
                    validationErrorTrigger = System.currentTimeMillis()
                )
            }
            return
        }

        val draft = createDraftReminder()
        val missing = permissionChecker.getMissingPermissions(draft)

        if (missing.isNotEmpty()) {
            _uiState.update { it.copy(showPermissionDialog = true, missingPermissions = missing) }
            return
        }

        performSave(draft, onSuccess)
    }

    fun saveAnyway(onSuccess: () -> Unit) {
        _uiState.update { it.copy(showPermissionDialog = false) }
        val draft = createDraftReminder().copy(isEnabled = false)
        performSave(draft, onSuccess)
    }

    private fun performSave(reminderToSave: Reminder, onSuccess: () -> Unit) {
        stopRingtonePlayback()
        viewModelScope.launch {
            if (reminderToSave.id == 0L) {
                val newId = reminderRepository.insertReminder(reminderToSave)
                val saved = reminderToSave.copy(id = newId)
                if (saved.isEnabled) reminderScheduler.schedule(saved)
            } else {
                reminderScheduler.cancelSnooze(reminderToSave)
                reminderAlertManager?.stopAlert(reminderToSave.id)
                reminderNotificationManager?.cancelNotification(reminderToSave.id)
                reminderRepository.updateReminder(reminderToSave)
                if (reminderToSave.isEnabled) reminderScheduler.schedule(reminderToSave)
                else reminderScheduler.cancel(reminderToSave)
            }
            reminderSyncManager.sync()
            onSuccess()
        }
    }

    fun deleteReminder(onSuccess: () -> Unit = {}) {
        stopRingtonePlayback()
        viewModelScope.launch {
            if (reminderId > 0L) {
                val existing = reminderRepository.getReminderById(reminderId)
                if (existing != null) {
                    reminderScheduler.cancel(existing)
                    reminderScheduler.cancelSnooze(existing)
                    reminderAlertManager?.stopAlert(existing.id)
                    reminderNotificationManager?.cancelNotification(existing.id)
                    reminderRepository.deleteReminder(existing)
                    reminderSyncManager.sync()
                }
            }
            onSuccess()
        }
    }

    fun onBackClick(onNavigateBack: () -> Unit) {
        if (hasUnsavedChanges()) {
            _uiState.update { it.copy(showDiscardDialog = true) }
        } else {
            stopRingtonePlayback()
            onNavigateBack()
        }
    }

    fun confirmDiscard(onNavigateBack: () -> Unit) {
        stopRingtonePlayback()
        _uiState.update { it.copy(showDiscardDialog = false) }
        onNavigateBack()
    }

    fun dismissDiscardDialog() {
        _uiState.update { it.copy(showDiscardDialog = false) }
    }

    fun refreshPermissions() {
        val draft = createDraftReminder()
        val missing = permissionChecker.getMissingPermissions(draft)
        _uiState.update {
            it.copy(missingPermissions = missing, showPermissionDialog = it.showPermissionDialog && missing.isNotEmpty())
        }
    }

    fun dismissPermissionDialog() {
        _uiState.update { it.copy(showPermissionDialog = false) }
    }

    private fun hasUnsavedChanges(): Boolean {
        val init = initialSnapshot ?: return false
        val curr = _uiState.value
        return curr.title != init.title ||
                curr.notes != init.notes ||
                curr.isEnabled != init.isEnabled ||
                curr.triggerType != init.triggerType ||
                curr.time != init.time ||
                curr.date != init.date ||
                curr.repeatDays != init.repeatDays ||
                curr.repeatInterval != init.repeatInterval ||
                curr.monthlyDays != init.monthlyDays ||
                curr.yearlyDates != init.yearlyDates ||
                curr.intervalMinutes != init.intervalMinutes ||
                curr.timeWindows != init.timeWindows ||
                curr.urgencyLevel != init.urgencyLevel ||
                curr.notificationStyle != init.notificationStyle ||
                curr.isPersistentAlert != init.isPersistentAlert ||
                curr.autoSilenceMinutes != init.autoSilenceMinutes ||
                curr.useAlarmStream != init.useAlarmStream ||
                curr.ringtoneUri != init.ringtoneUri ||
                curr.vibrationEnabled != init.vibrationEnabled ||
                curr.volume != init.volume ||
                curr.snoozeEnabled != init.snoozeEnabled ||
                curr.fixedTimes != init.fixedTimes ||
                curr.snoozeIntervalMinutes != init.snoozeIntervalMinutes
    }

    fun updateSnoozeSettings(enabled: Boolean, interval: Int) {
        _uiState.update { it.copy(snoozeEnabled = enabled, snoozeIntervalMinutes = interval) }
    }
    fun setSnoozeEnabled(enabled: Boolean) {
        _uiState.update { it.copy(snoozeEnabled = enabled) }
    }
    fun setSnoozeIntervalMinutes(interval: Int) {
        _uiState.update { it.copy(snoozeIntervalMinutes = interval) }
    }
    fun updateRingtone(uri: String?) {
        stopRingtonePlayback()
        _uiState.update { it.copy(ringtoneUri = uri) }
    }
    fun setTitle(title: String) { _uiState.update { it.copy(title = title, showTitleError = false) } }
    fun setNotes(notes: String) { _uiState.update { it.copy(notes = notes) } }
    fun setTriggerType(type: ReminderTriggerType) { _uiState.update { it.copy(triggerType = type) } }
    
    fun setTime(time: LocalTime) {
        _uiState.update { state ->
            val updatedFixedTimes = if (state.fixedTimes.size <= 1) listOf(time) else state.fixedTimes
            state.copy(time = time, fixedTimes = updatedFixedTimes)
        }
    }

    fun addFixedTime(time: LocalTime) {
        _uiState.update { state ->
            if (state.fixedTimes.contains(time)) state
            else {
                val updated = (state.fixedTimes + time).sorted()
                state.copy(fixedTimes = updated, time = updated.first())
            }
        }
    }

    fun removeFixedTime(index: Int) {
        _uiState.update { state ->
            val list = state.fixedTimes.toMutableList()
            if (index in list.indices && list.size > 1) {
                list.removeAt(index)
                state.copy(fixedTimes = list, time = list.first())
            } else {
                state
            }
        }
    }

    fun updateFixedTime(index: Int, time: LocalTime) {
        _uiState.update { state ->
            val list = state.fixedTimes.toMutableList()
            if (index in list.indices) {
                list[index] = time
                val sorted = list.distinct().sorted()
                state.copy(fixedTimes = sorted, time = sorted.first())
            } else {
                state
            }
        }
    }
    fun setDate(date: LocalDate?) {
        _uiState.update { it.copy(date = date, repeatDays = if (date != null) emptyList() else it.repeatDays) }
    }
    fun setRepeatDays(days: List<DayOfWeek>) {
        _uiState.update { it.copy(repeatDays = days, date = if (days.isNotEmpty()) null else it.date) }
    }
    fun toggleRepeatDay(day: DayOfWeek) {
        _uiState.update {
            val days = if (it.repeatDays.contains(day)) it.repeatDays - day else it.repeatDays + day
            it.copy(repeatDays = days.sorted())
        }
    }
    fun setRepeatInterval(interval: ReminderRepeatInterval) {
        _uiState.update { state ->
            when (interval) {
                ReminderRepeatInterval.NONE -> state.copy(
                    repeatInterval = interval,
                    date = state.date ?: LocalDate.now()
                )
                ReminderRepeatInterval.DAILY -> state.copy(
                    repeatInterval = interval,
                    date = null,
                    repeatDays = emptyList()
                )
                ReminderRepeatInterval.WEEKLY -> state.copy(
                    repeatInterval = interval,
                    date = null,
                    repeatDays = if (state.repeatDays.isEmpty()) listOf(DayOfWeek.MONDAY) else state.repeatDays
                )
                ReminderRepeatInterval.MONTHLY -> state.copy(
                    repeatInterval = interval,
                    date = null
                )
                ReminderRepeatInterval.YEARLY -> state.copy(
                    repeatInterval = interval,
                    date = null,
                    yearlyDates = if (state.yearlyDates.isEmpty()) {
                        val today = LocalDate.now()
                        listOf(ReminderMonthDay(month = today.monthValue, day = today.dayOfMonth))
                    } else state.yearlyDates
                )
                ReminderRepeatInterval.CUSTOM -> state.copy(
                    repeatInterval = interval
                )
            }
        }
    }
    
    fun toggleMonthlyDay(day: Int) {
        _uiState.update { state ->
            val updated = if (state.monthlyDays.contains(day)) {
                state.monthlyDays - day
            } else {
                (state.monthlyDays + day).sorted()
            }
            state.copy(monthlyDays = updated)
        }
    }

    fun addYearlyDate(month: Int, day: Int) {
        _uiState.update { state ->
            val newItem = ReminderMonthDay(month = month, day = day)
            if (state.yearlyDates.any { it.month == month && it.day == day }) {
                state
            } else {
                val updated = (state.yearlyDates + newItem).sortedWith(compareBy({ it.month }, { it.day }))
                state.copy(yearlyDates = updated)
            }
        }
    }

    fun removeYearlyDate(index: Int) {
        _uiState.update { state ->
            val list = state.yearlyDates.toMutableList()
            if (index in list.indices) {
                list.removeAt(index)
            }
            state.copy(yearlyDates = list)
        }
    }

    fun setTimeWindows(windows: List<ReminderTimeWindow>) {
        _uiState.update { it.copy(timeWindows = windows) }
    }

    fun addTimeWindow(window: ReminderTimeWindow) {
        _uiState.update { it.copy(timeWindows = it.timeWindows + window) }
    }

    fun removeTimeWindow(index: Int) {
        _uiState.update { state ->
            val list = state.timeWindows.toMutableList()
            if (index in list.indices) {
                list.removeAt(index)
            }
            state.copy(timeWindows = list)
        }
    }

    fun setIntervalMinutes(v: Int?) { _uiState.update { it.copy(intervalMinutes = v) } }
    fun setUrgencyLevel(level: UrgencyLevel) { _uiState.update { it.copy(urgencyLevel = level) } }
    
    fun setNotificationStyle(style: NotificationStyle) {
        stopRingtonePlayback()
        _uiState.update {
            it.copy(
                notificationStyle = style,
                isPersistentAlert = if (style == NotificationStyle.FULL_SCREEN) it.isPersistentAlert else false
            )
        }
    }
    fun setAutoSilenceMinutes(m: Int) { _uiState.update { it.copy(autoSilenceMinutes = m) } }
    fun setUseAlarmStream(use: Boolean) { _uiState.update { it.copy(useAlarmStream = use) } }
    fun setVibrationEnabled(enabled: Boolean) {
        stopRingtonePlayback()
        _uiState.update { it.copy(vibrationEnabled = enabled) }
    }
    fun setVolume(v: Int) {
        _uiState.update { it.copy(volume = v) }
        audioPlayer?.setVolume(
            if (_uiState.value.useAlarmStream) android.media.AudioAttributes.USAGE_ALARM else android.media.AudioAttributes.USAGE_MEDIA,
            v
        )
    }
    fun setIsEnabled(enabled: Boolean) { _uiState.update { it.copy(isEnabled = enabled) } }
    fun setPersistentAlert(persistent: Boolean) { _uiState.update { it.copy(isPersistentAlert = persistent) } }

    fun toggleRingtonePlayback() {
        if (_uiState.value.isRingtonePlaying) {
            stopRingtonePlayback()
        } else {
            startRingtonePlayback()
        }
    }

    fun startRingtonePlayback() {
        val ctx = context ?: return
        val player = audioPlayer ?: return
        _uiState.update { it.copy(isRingtonePlaying = true) }
        val uri = getAccessibleRingtoneUri(ctx, _uiState.value.ringtoneUri)
        player.play(
            uri = uri,
            useAlarmStream = _uiState.value.useAlarmStream,
            loop = true,
            volume = _uiState.value.volume,
            vibrationEnabled = _uiState.value.vibrationEnabled
        )
    }

    fun stopRingtonePlayback() {
        _uiState.update { it.copy(isRingtonePlaying = false) }
        audioPlayer?.stop()
    }

    override fun onCleared() {
        super.onCleared()
        stopRingtonePlayback()
    }
}
