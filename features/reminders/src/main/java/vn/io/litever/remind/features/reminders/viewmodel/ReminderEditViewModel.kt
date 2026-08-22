package vn.io.litever.remind.features.reminders.viewmodel

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import vn.io.litever.remind.core.common.util.PermissionChecker
import vn.io.litever.remind.core.common.util.ReminderPermissionType
import vn.io.litever.remind.core.datastore.AlarmPreferencesDataSource
import vn.io.litever.remind.core.domain.repository.ReminderRepository
import vn.io.litever.remind.core.domain.scheduler.ReminderScheduler
import vn.io.litever.remind.core.model.ChecklistItem
import vn.io.litever.remind.core.model.DayOfWeek
import vn.io.litever.remind.core.model.GeofenceTransitionType
import vn.io.litever.remind.core.model.LocationTrigger
import vn.io.litever.remind.core.model.NotificationStyle
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderTimeWindow
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.core.alarm.ReminderSyncManager
import vn.io.litever.remind.core.model.TimeTrigger
import vn.io.litever.remind.core.model.WifiTrigger
import vn.io.litever.remind.core.model.WifiTriggerType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

enum class ReminderRepeatInterval {
    ONCE,
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}

enum class ReminderValidationErrorField {
    TITLE,
    LOCATION,
    WIFI_SSID
}

data class ReminderEditUiState(
    val isLoading: Boolean = true,
    val id: Long = 0L,
    val title: String = "",
    val notes: String = "",
    val isEnabled: Boolean = true,
    val triggerType: ReminderTriggerType = ReminderTriggerType.TIME,

    // Time Trigger
    val time: LocalTime = LocalTime.now().plusMinutes(5).withSecond(0).withNano(0),
    val date: LocalDate? = null,
    val repeatDays: List<DayOfWeek> = emptyList(),
    val repeatInterval: ReminderRepeatInterval = ReminderRepeatInterval.ONCE,
    val monthlyDay: Int = 1,
    val yearlyMonth: Int = 1,
    val yearlyDay: Int = 1,

    // Location Trigger
    val locationName: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val radiusMeters: Float = 100f,
    val geofenceTransitionType: GeofenceTransitionType = GeofenceTransitionType.ENTER,
    val locationTimeWindow: ReminderTimeWindow? = null,

    // Wi-Fi Trigger
    val wifiSsid: String = "",
    val wifiBssid: String? = null,
    val wifiTriggerType: WifiTriggerType = WifiTriggerType.ON_CONNECT,
    val wifiTimeWindow: ReminderTimeWindow? = null,

    // Checklist
    val checklist: List<ChecklistItem> = emptyList(),

    // Alert & Notification Settings
    val notificationStyle: NotificationStyle = NotificationStyle.STANDARD,
    val isPersistentAlert: Boolean = false,
    val ringtoneUri: String? = null,
    val ringtoneTitle: String = "Default",
    val vibrationEnabled: Boolean = true,
    val volume: Int = 80,
    val snoozeEnabled: Boolean = true,
    val snoozeIntervalMinutes: Int = 5,
    val snoozeRepeatCount: Int = 3,

    // Preferences & UX Dialogs
    val is24HourFormat: Boolean = true,
    val showPermissionDialog: Boolean = false,
    val showDiscardDialog: Boolean = false,
    val showTitleError: Boolean = false,
    val showLocationError: Boolean = false,
    val showWifiError: Boolean = false,
    val validationErrorField: ReminderValidationErrorField? = null,
    val validationErrorTrigger: Long = 0L,
    val missingPermissions: List<ReminderPermissionType> = emptyList(),
    val isNewDraft: Boolean = true
)

@HiltViewModel
class ReminderEditViewModel @Inject constructor(
    private val reminderRepository: ReminderRepository,
    private val reminderScheduler: ReminderScheduler,
    private val reminderSyncManager: ReminderSyncManager,
    private val permissionChecker: PermissionChecker,
    private val preferencesDataSource: AlarmPreferencesDataSource,
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context? = null
) : ViewModel() {

    val reminderId: Long = savedStateHandle.get<Long>("reminderId") ?: 0L

    private val _uiState = MutableStateFlow(ReminderEditUiState())
    val uiState: StateFlow<ReminderEditUiState> = _uiState.asStateFlow()

    private var initialSnapshot: ReminderEditUiState? = null

    init {
        viewModelScope.launch {
            val is24h = preferencesDataSource.is24HourFormat.first()
            val now = LocalDate.now()
            if (reminderId > 0L) {
                val existing = reminderRepository.getReminderById(reminderId)
                if (existing != null) {
                    val timeTrigger = existing.timeTrigger
                    val initialRepeatInterval = when {
                        timeTrigger?.repeatDays?.size == 7 -> ReminderRepeatInterval.DAILY
                        !timeTrigger?.repeatDays.isNullOrEmpty() -> ReminderRepeatInterval.WEEKLY
                        timeTrigger?.date != null -> ReminderRepeatInterval.ONCE
                        else -> ReminderRepeatInterval.ONCE
                    }
                    val initialMonthlyDay = timeTrigger?.date?.dayOfMonth ?: now.dayOfMonth
                    val initialYearlyMonth = timeTrigger?.date?.monthValue ?: now.monthValue
                    val initialYearlyDay = timeTrigger?.date?.dayOfMonth ?: now.dayOfMonth

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            id = existing.id,
                            title = existing.title,
                            notes = existing.notes,
                            isEnabled = existing.isEnabled,
                            triggerType = existing.triggerType,
                            time = timeTrigger?.time ?: LocalTime.now().plusMinutes(5).withSecond(0).withNano(0),
                            date = timeTrigger?.date,
                            repeatDays = timeTrigger?.repeatDays ?: emptyList(),
                            repeatInterval = initialRepeatInterval,
                            monthlyDay = initialMonthlyDay,
                            yearlyMonth = initialYearlyMonth,
                            yearlyDay = initialYearlyDay,
                            locationName = existing.locationTrigger?.locationName ?: "",
                            latitude = existing.locationTrigger?.latitude ?: 0.0,
                            longitude = existing.locationTrigger?.longitude ?: 0.0,
                            radiusMeters = existing.locationTrigger?.radiusMeters ?: 100f,
                            geofenceTransitionType = existing.locationTrigger?.transitionType ?: GeofenceTransitionType.ENTER,
                            locationTimeWindow = existing.locationTrigger?.timeWindow,
                            wifiSsid = existing.wifiTrigger?.ssid ?: "",
                            wifiBssid = existing.wifiTrigger?.bssid,
                            wifiTriggerType = existing.wifiTrigger?.triggerType ?: WifiTriggerType.ON_CONNECT,
                            wifiTimeWindow = existing.wifiTrigger?.timeWindow,
                            checklist = existing.checklist,
                            notificationStyle = existing.notificationStyle,
                            isPersistentAlert = existing.isPersistentAlert,
                            ringtoneUri = existing.ringtoneUri,
                            ringtoneTitle = getRingtoneTitle(existing.ringtoneUri),
                            vibrationEnabled = existing.vibrationEnabled,
                            volume = existing.volume,
                            snoozeEnabled = existing.snoozeEnabled,
                            snoozeIntervalMinutes = existing.snoozeIntervalMinutes,
                            snoozeRepeatCount = existing.snoozeRepeatCount,
                            is24HourFormat = is24h,
                            isNewDraft = false
                        )
                    }
                    initialSnapshot = _uiState.value
                    return@launch
                }
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    is24HourFormat = is24h,
                    monthlyDay = now.dayOfMonth,
                    yearlyMonth = now.monthValue,
                    yearlyDay = now.dayOfMonth,
                    isNewDraft = true
                )
            }
            initialSnapshot = _uiState.value
        }
    }

    fun setTitle(title: String) {
        _uiState.update { it.copy(title = title, showTitleError = false) }
    }

    fun setNotes(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun setTriggerType(type: ReminderTriggerType) {
        _uiState.update { it.copy(triggerType = type) }
    }

    fun setTime(time: LocalTime) {
        _uiState.update { state ->
            val updatedDate = when (state.repeatInterval) {
                ReminderRepeatInterval.MONTHLY -> calculateNextMonthlyDate(state.monthlyDay, time)
                ReminderRepeatInterval.YEARLY -> calculateNextYearlyDate(state.yearlyMonth, state.yearlyDay, time)
                else -> state.date
            }
            state.copy(time = time, date = updatedDate)
        }
    }

    fun setDate(date: LocalDate?) {
        _uiState.update {
            it.copy(
                date = date,
                repeatInterval = if (date != null) ReminderRepeatInterval.ONCE else it.repeatInterval,
                repeatDays = if (date != null) emptyList() else it.repeatDays
            )
        }
    }

    fun setRepeatDays(days: List<DayOfWeek>) {
        _uiState.update {
            val interval = when {
                days.size == 7 -> ReminderRepeatInterval.DAILY
                days.isNotEmpty() -> ReminderRepeatInterval.WEEKLY
                else -> it.repeatInterval
            }
            it.copy(
                repeatDays = days,
                repeatInterval = interval,
                date = if (days.isNotEmpty()) null else it.date
            )
        }
    }

    fun setRepeatInterval(interval: ReminderRepeatInterval) {
        _uiState.update { state ->
            when (interval) {
                ReminderRepeatInterval.ONCE -> {
                    state.copy(
                        repeatInterval = interval,
                        repeatDays = emptyList(),
                        date = state.date ?: LocalDate.now()
                    )
                }
                ReminderRepeatInterval.DAILY -> {
                    state.copy(
                        repeatInterval = interval,
                        repeatDays = DayOfWeek.entries,
                        date = null
                    )
                }
                ReminderRepeatInterval.WEEKLY -> {
                    val currentWeeklyDays = if (state.repeatDays.isNotEmpty() && state.repeatDays.size < 7) {
                        state.repeatDays
                    } else {
                        listOf(DayOfWeek.MONDAY)
                    }
                    state.copy(
                        repeatInterval = interval,
                        repeatDays = currentWeeklyDays,
                        date = null
                    )
                }
                ReminderRepeatInterval.MONTHLY -> {
                    val targetDay = if (state.monthlyDay in 1..31) state.monthlyDay else (state.date?.dayOfMonth ?: LocalDate.now().dayOfMonth)
                    val calculatedDate = calculateNextMonthlyDate(targetDay, state.time)
                    state.copy(
                        repeatInterval = interval,
                        monthlyDay = targetDay,
                        repeatDays = emptyList(),
                        date = calculatedDate
                    )
                }
                ReminderRepeatInterval.YEARLY -> {
                    val targetMonth = if (state.yearlyMonth in 1..12) state.yearlyMonth else (state.date?.monthValue ?: LocalDate.now().monthValue)
                    val maxDayForMonth = java.time.Month.of(targetMonth).maxLength()
                    val targetDay = if (state.yearlyDay in 1..maxDayForMonth) state.yearlyDay else (state.date?.dayOfMonth ?: LocalDate.now().dayOfMonth).coerceIn(1, maxDayForMonth)
                    val calculatedDate = calculateNextYearlyDate(targetMonth, targetDay, state.time)
                    state.copy(
                        repeatInterval = interval,
                        yearlyMonth = targetMonth,
                        yearlyDay = targetDay,
                        repeatDays = emptyList(),
                        date = calculatedDate
                    )
                }
            }
        }
    }

    fun toggleRepeatDay(day: DayOfWeek) {
        _uiState.update { state ->
            val newDays = if (state.repeatDays.contains(day)) {
                state.repeatDays - day
            } else {
                (state.repeatDays + day).sortedBy { it.toJavaDayValue() }
            }
            val interval = when {
                newDays.size == 7 -> ReminderRepeatInterval.DAILY
                newDays.isNotEmpty() -> ReminderRepeatInterval.WEEKLY
                else -> state.repeatInterval
            }
            state.copy(
                repeatDays = newDays,
                repeatInterval = interval,
                date = null
            )
        }
    }

    fun setMonthlyDay(day: Int) {
        val clamped = day.coerceIn(1, 31)
        _uiState.update { state ->
            val nextDate = calculateNextMonthlyDate(clamped, state.time)
            state.copy(
                monthlyDay = clamped,
                date = nextDate,
                repeatDays = emptyList()
            )
        }
    }

    fun setYearlyMonthAndDay(month: Int, day: Int) {
        val clampedMonth = month.coerceIn(1, 12)
        val maxDaysForMonth = java.time.Month.of(clampedMonth).maxLength()
        val clampedDay = day.coerceIn(1, maxDaysForMonth)
        _uiState.update { state ->
            val nextDate = calculateNextYearlyDate(clampedMonth, clampedDay, state.time)
            state.copy(
                yearlyMonth = clampedMonth,
                yearlyDay = clampedDay,
                date = nextDate,
                repeatDays = emptyList()
            )
        }
    }

    private fun calculateNextMonthlyDate(dayOfMonth: Int, time: LocalTime): LocalDate {
        val now = LocalDateTime.now()
        val today = now.toLocalDate()
        val maxDaysThisMonth = today.lengthOfMonth()
        val clampedDay = dayOfMonth.coerceAtMost(maxDaysThisMonth)
        val thisMonthCandidate = today.withDayOfMonth(clampedDay)

        return if (thisMonthCandidate.atTime(time).isAfter(now)) {
            thisMonthCandidate
        } else {
            val nextMonth = today.plusMonths(1)
            val maxDaysNextMonth = nextMonth.lengthOfMonth()
            nextMonth.withDayOfMonth(dayOfMonth.coerceAtMost(maxDaysNextMonth))
        }
    }

    private fun calculateNextYearlyDate(month: Int, day: Int, time: LocalTime): LocalDate {
        val now = LocalDateTime.now()
        val today = now.toLocalDate()
        val targetMonth = java.time.Month.of(month)
        val maxDaysThisYear = targetMonth.length(java.time.Year.isLeap(today.year.toLong()))
        val clampedDayThisYear = day.coerceAtMost(maxDaysThisYear)
        val thisYearCandidate = LocalDate.of(today.year, month, clampedDayThisYear)

        return if (thisYearCandidate.atTime(time).isAfter(now)) {
            thisYearCandidate
        } else {
            val nextYear = today.year + 1
            val maxDaysNextYear = targetMonth.length(java.time.Year.isLeap(nextYear.toLong()))
            LocalDate.of(nextYear, month, day.coerceAtMost(maxDaysNextYear))
        }
    }

    fun setLocationName(name: String) {
        _uiState.update { it.copy(locationName = name, showLocationError = false) }
    }

    fun setLatitude(lat: Double) {
        _uiState.update { it.copy(latitude = lat, showLocationError = false) }
    }

    fun setLongitude(lng: Double) {
        _uiState.update { it.copy(longitude = lng, showLocationError = false) }
    }

    fun setRadiusMeters(radius: Float) {
        _uiState.update { it.copy(radiusMeters = radius) }
    }

    fun setGeofenceTransitionType(type: GeofenceTransitionType) {
        _uiState.update { it.copy(geofenceTransitionType = type) }
    }

    fun setLocationTimeWindow(timeWindow: ReminderTimeWindow?) {
        _uiState.update { it.copy(locationTimeWindow = timeWindow) }
    }

    fun setWifiSsid(ssid: String) {
        _uiState.update { it.copy(wifiSsid = ssid, showWifiError = false) }
    }

    fun setWifiBssid(bssid: String?) {
        _uiState.update { it.copy(wifiBssid = bssid) }
    }

    fun setWifiTriggerType(type: WifiTriggerType) {
        _uiState.update { it.copy(wifiTriggerType = type) }
    }

    fun setWifiTimeWindow(timeWindow: ReminderTimeWindow?) {
        _uiState.update { it.copy(wifiTimeWindow = timeWindow) }
    }

    fun addChecklistItem(text: String) {
        _uiState.update {
            val newItem = ChecklistItem(
                id = 0L,
                reminderId = it.id,
                text = text,
                isCompleted = false,
                orderIndex = it.checklist.size
            )
            it.copy(checklist = it.checklist + newItem)
        }
    }

    fun updateChecklistItemText(index: Int, text: String) {
        _uiState.update {
            if (index !in it.checklist.indices) return@update it
            val updatedList = it.checklist.toMutableList()
            updatedList[index] = updatedList[index].copy(text = text)
            it.copy(checklist = updatedList)
        }
    }

    fun toggleChecklistItemInitial(index: Int, isCompleted: Boolean) {
        _uiState.update {
            if (index !in it.checklist.indices) return@update it
            val updatedList = it.checklist.toMutableList()
            updatedList[index] = updatedList[index].copy(isCompleted = isCompleted)
            it.copy(checklist = updatedList)
        }
    }

    fun deleteChecklistItem(index: Int) {
        _uiState.update {
            if (index !in it.checklist.indices) return@update it
            val updatedList = it.checklist.toMutableList().apply { removeAt(index) }
            val reordered = updatedList.mapIndexed { idx, item -> item.copy(orderIndex = idx) }
            it.copy(checklist = reordered)
        }
    }

    fun moveChecklistItem(fromIndex: Int, toIndex: Int) {
        _uiState.update {
            if (fromIndex !in it.checklist.indices || toIndex !in it.checklist.indices) return@update it
            val list = it.checklist.toMutableList()
            val item = list.removeAt(fromIndex)
            list.add(toIndex, item)
            val reordered = list.mapIndexed { idx, itm -> itm.copy(orderIndex = idx) }
            it.copy(checklist = reordered)
        }
    }

    fun setNotificationStyle(style: NotificationStyle) {
        _uiState.update {
            it.copy(
                notificationStyle = style,
                isPersistentAlert = if (style == NotificationStyle.STANDARD) false else it.isPersistentAlert
            )
        }
    }

    fun setPersistentAlert(isPersistent: Boolean) {
        _uiState.update { it.copy(isPersistentAlert = isPersistent) }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        _uiState.update { it.copy(vibrationEnabled = enabled) }
    }

    fun setVolume(volume: Int) {
        _uiState.update { it.copy(volume = volume) }
    }

    fun setSnoozeEnabled(enabled: Boolean) {
        _uiState.update { it.copy(snoozeEnabled = enabled) }
    }

    fun setSnoozeIntervalMinutes(minutes: Int) {
        _uiState.update { it.copy(snoozeIntervalMinutes = minutes) }
    }

    fun setSnoozeRepeatCount(count: Int) {
        _uiState.update { it.copy(snoozeRepeatCount = count) }
    }

    fun updateRingtone(uri: String?) {
        val title = getRingtoneTitle(uri)
        _uiState.update { it.copy(ringtoneUri = uri, ringtoneTitle = title) }
    }

    fun updateSnoozeSettings(enabled: Boolean, interval: Int, repeatCount: Int) {
        _uiState.update {
            it.copy(
                snoozeEnabled = enabled,
                snoozeIntervalMinutes = interval,
                snoozeRepeatCount = repeatCount
            )
        }
    }

    private fun getRingtoneTitle(uriString: String?): String {
        if (uriString.isNullOrBlank()) return "Default"
        val ctx = context ?: return "Custom Ringtone"
        return try {
            val r = RingtoneManager.getRingtone(ctx, Uri.parse(uriString))
            r?.getTitle(ctx) ?: "Custom Ringtone"
        } catch (_: Exception) {
            "Custom Ringtone"
        }
    }

    fun dismissPermissionDialog() {
        _uiState.update { it.copy(showPermissionDialog = false) }
    }

    fun dismissDiscardDialog() {
        _uiState.update { it.copy(showDiscardDialog = false) }
    }

    fun createDraftReminder(): Reminder {
        val state = _uiState.value
        val timeTrigger = if (state.triggerType == ReminderTriggerType.TIME) {
            TimeTrigger(
                time = state.time,
                date = state.date,
                repeatDays = state.repeatDays
            )
        } else null

        val locationTrigger = if (state.triggerType == ReminderTriggerType.LOCATION) {
            LocationTrigger(
                latitude = state.latitude,
                longitude = state.longitude,
                radiusMeters = state.radiusMeters,
                locationName = state.locationName,
                transitionType = state.geofenceTransitionType,
                timeWindow = state.locationTimeWindow
            )
        } else null

        val wifiTrigger = if (state.triggerType == ReminderTriggerType.WIFI) {
            WifiTrigger(
                ssid = state.wifiSsid,
                bssid = state.wifiBssid,
                triggerType = state.wifiTriggerType,
                timeWindow = state.wifiTimeWindow
            )
        } else null

        return Reminder(
            id = state.id,
            title = state.title.trim(),
            notes = state.notes.trim(),
            isEnabled = state.isEnabled,
            triggerType = state.triggerType,
            timeTrigger = timeTrigger,
            locationTrigger = locationTrigger,
            wifiTrigger = wifiTrigger,
            checklist = state.checklist,
            notificationStyle = state.notificationStyle,
            isPersistentAlert = if (state.notificationStyle == NotificationStyle.FULL_SCREEN) state.isPersistentAlert else false,
            ringtoneUri = state.ringtoneUri,
            vibrationEnabled = state.vibrationEnabled,
            volume = state.volume,
            snoozeEnabled = state.snoozeEnabled,
            snoozeIntervalMinutes = state.snoozeIntervalMinutes,
            snoozeRepeatCount = state.snoozeRepeatCount,
            isCompleted = false
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

        if (_uiState.value.triggerType == ReminderTriggerType.LOCATION) {
            val state = _uiState.value
            val isLocationEmpty = state.locationName.isBlank() && state.latitude == 0.0 && state.longitude == 0.0
            if (isLocationEmpty) {
                _uiState.update {
                    it.copy(
                        showLocationError = true,
                        validationErrorField = ReminderValidationErrorField.LOCATION,
                        validationErrorTrigger = System.currentTimeMillis()
                    )
                }
                return
            }
        }

        if (_uiState.value.triggerType == ReminderTriggerType.WIFI) {
            val state = _uiState.value
            if (state.wifiSsid.isBlank()) {
                _uiState.update {
                    it.copy(
                        showWifiError = true,
                        validationErrorField = ReminderValidationErrorField.WIFI_SSID,
                        validationErrorTrigger = System.currentTimeMillis()
                    )
                }
                return
            }
        }

        val draft = createDraftReminder()
        val missing = permissionChecker.getMissingPermissions(draft)

        if (missing.isNotEmpty()) {
            _uiState.update { it.copy(showPermissionDialog = true, missingPermissions = missing) }
            return
        }

        performSave(draft.copy(isEnabled = true), onSuccess)
    }

    fun saveAnyway(onSuccess: () -> Unit) {
        _uiState.update { it.copy(showPermissionDialog = false) }
        val draft = createDraftReminder().copy(isEnabled = false)
        performSave(draft, onSuccess)
    }

    private fun performSave(reminderToSave: Reminder, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (reminderToSave.id == 0L) {
                val newId = reminderRepository.insertReminder(reminderToSave)
                val saved = reminderToSave.copy(id = newId)
                if (saved.isEnabled) {
                    reminderScheduler.schedule(saved)
                }
            } else {
                reminderRepository.updateReminder(reminderToSave)
                if (reminderToSave.isEnabled) {
                    reminderScheduler.schedule(reminderToSave)
                } else {
                    reminderScheduler.cancel(reminderToSave)
                }
            }
            reminderSyncManager.sync()
            onSuccess()
        }
    }

    fun onBackClick(onNavigateBack: () -> Unit) {
        if (hasUnsavedChanges()) {
            _uiState.update { it.copy(showDiscardDialog = true) }
        } else {
            onNavigateBack()
        }
    }

    fun confirmDiscard(onNavigateBack: () -> Unit) {
        _uiState.update { it.copy(showDiscardDialog = false) }
        onNavigateBack()
    }

    fun refreshPermissions() {
        val draft = createDraftReminder()
        if (permissionChecker.hasPermissionsForReminder(draft)) {
            _uiState.update { it.copy(showPermissionDialog = false) }
        }
    }

    private fun hasUnsavedChanges(): Boolean {
        val init = initialSnapshot ?: return false
        val curr = _uiState.value
        return curr.title != init.title ||
                curr.notes != init.notes ||
                curr.triggerType != init.triggerType ||
                curr.time != init.time ||
                curr.date != init.date ||
                curr.repeatDays != init.repeatDays ||
                curr.repeatInterval != init.repeatInterval ||
                curr.monthlyDay != init.monthlyDay ||
                curr.yearlyMonth != init.yearlyMonth ||
                curr.yearlyDay != init.yearlyDay ||
                curr.locationName != init.locationName ||
                curr.latitude != init.latitude ||
                curr.longitude != init.longitude ||
                curr.radiusMeters != init.radiusMeters ||
                curr.geofenceTransitionType != init.geofenceTransitionType ||
                curr.locationTimeWindow != init.locationTimeWindow ||
                curr.wifiSsid != init.wifiSsid ||
                curr.wifiBssid != init.wifiBssid ||
                curr.wifiTriggerType != init.wifiTriggerType ||
                curr.wifiTimeWindow != init.wifiTimeWindow ||
                curr.checklist != init.checklist ||
                curr.notificationStyle != init.notificationStyle ||
                curr.isPersistentAlert != init.isPersistentAlert ||
                curr.ringtoneUri != init.ringtoneUri ||
                curr.vibrationEnabled != init.vibrationEnabled ||
                curr.volume != init.volume ||
                curr.snoozeEnabled != init.snoozeEnabled ||
                curr.snoozeIntervalMinutes != init.snoozeIntervalMinutes ||
                curr.snoozeRepeatCount != init.snoozeRepeatCount
    }
}
