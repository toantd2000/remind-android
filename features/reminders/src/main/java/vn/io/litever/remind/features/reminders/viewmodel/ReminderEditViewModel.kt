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
import vn.io.litever.remind.core.alarm.alert.ReminderAlertManager
import vn.io.litever.remind.core.alarm.notification.ReminderNotificationManager
import vn.io.litever.remind.core.alarm.ReminderSyncManager
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
import vn.io.litever.remind.core.model.ReminderMonthDay
import vn.io.litever.remind.core.model.ReminderTimeWindow
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.core.model.TimeTrigger
import vn.io.litever.remind.core.model.WifiTrigger
import vn.io.litever.remind.core.model.WifiTriggerType
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

enum class ReminderRepeatInterval {
    ONCE,
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
    val time: LocalTime = LocalTime.now().plusMinutes(5).withSecond(0).withNano(0),
    val date: LocalDate? = LocalDate.now(),
    val repeatDays: List<DayOfWeek> = emptyList(),
    val repeatInterval: ReminderRepeatInterval = ReminderRepeatInterval.ONCE,
    val monthlyDays: List<Int> = emptyList(),
    val yearlyDates: List<ReminderMonthDay> = emptyList(),
    val locationName: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val radiusMeters: Float = 100f,
    val geofenceTransitionType: GeofenceTransitionType = GeofenceTransitionType.ENTER,
    val locationTimeWindow: ReminderTimeWindow? = null,
    val locationTimeWindows: List<ReminderTimeWindow> = emptyList(),
    val wifiSsid: String = "",
    val wifiBssid: String? = null,
    val wifiTriggerType: WifiTriggerType = WifiTriggerType.ON_CONNECT,
    val wifiTimeWindow: ReminderTimeWindow? = null,
    val wifiTimeWindows: List<ReminderTimeWindow> = emptyList(),
    val checklist: List<ChecklistItem> = emptyList(),
    val notificationStyle: NotificationStyle = NotificationStyle.STANDARD,
    val isPersistentAlert: Boolean = false,
    val autoSilenceMinutes: Int = 5,
    val useAlarmStream: Boolean = true,
    val ringtoneUri: String? = null,
    val ringtoneTitle: String = "Default",
    val vibrationEnabled: Boolean = true,
    val volume: Int = 100,
    val snoozeEnabled: Boolean = true,
    val snoozeIntervalMinutes: Int = 5,
    val snoozeRepeatCount: Int = 3,
    val showDiscardDialog: Boolean = false,
    val showPermissionDialog: Boolean = false,
    val showTitleError: Boolean = false,
    val showLocationError: Boolean = false,
    val showWifiError: Boolean = false,
    val validationErrorField: ReminderValidationErrorField? = null,
    val validationErrorTrigger: Long = 0L,
    val is24HourFormat: Boolean = true,
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
    @ApplicationContext private val context: Context? = null,
    private val reminderAlertManager: ReminderAlertManager? = null,
    private val reminderNotificationManager: ReminderNotificationManager? = null
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
                    val locTrigger = existing.locationTrigger
                    val wifiTrigger = existing.wifiTrigger

                    val activeTriggerDate = when (existing.triggerType) {
                        ReminderTriggerType.TIME -> timeTrigger?.date
                        ReminderTriggerType.LOCATION -> locTrigger?.date
                        ReminderTriggerType.WIFI -> wifiTrigger?.date
                    }
                    val activeRepeatDays = when (existing.triggerType) {
                        ReminderTriggerType.TIME -> timeTrigger?.repeatDays ?: emptyList()
                        ReminderTriggerType.LOCATION -> locTrigger?.repeatDays ?: emptyList()
                        ReminderTriggerType.WIFI -> wifiTrigger?.repeatDays ?: emptyList()
                    }
                    val activeMonthlyDays = when (existing.triggerType) {
                        ReminderTriggerType.TIME -> timeTrigger?.monthlyDays ?: emptyList()
                        ReminderTriggerType.LOCATION -> locTrigger?.monthlyDays ?: emptyList()
                        ReminderTriggerType.WIFI -> wifiTrigger?.monthlyDays ?: emptyList()
                    }
                    val activeYearlyDates = when (existing.triggerType) {
                        ReminderTriggerType.TIME -> timeTrigger?.yearlyDates ?: emptyList()
                        ReminderTriggerType.LOCATION -> locTrigger?.yearlyDates ?: emptyList()
                        ReminderTriggerType.WIFI -> wifiTrigger?.yearlyDates ?: emptyList()
                    }

                    val initialRepeatInterval = when {
                        activeYearlyDates.isNotEmpty() -> ReminderRepeatInterval.YEARLY
                        activeMonthlyDays.isNotEmpty() -> ReminderRepeatInterval.MONTHLY
                        activeRepeatDays.isNotEmpty() -> ReminderRepeatInterval.WEEKLY
                        activeTriggerDate != null -> ReminderRepeatInterval.ONCE
                        else -> ReminderRepeatInterval.ONCE
                    }
                    val initialMonthlyDays = if (activeMonthlyDays.isNotEmpty()) {
                        activeMonthlyDays
                    } else {
                        listOf(activeTriggerDate?.dayOfMonth ?: now.dayOfMonth)
                    }
                    val initialYearlyDates = if (activeYearlyDates.isNotEmpty()) {
                        activeYearlyDates
                    } else {
                        listOf(ReminderMonthDay(activeTriggerDate?.monthValue ?: now.monthValue, activeTriggerDate?.dayOfMonth ?: now.dayOfMonth))
                    }

                    val locWindows = locTrigger?.activeTimeWindows ?: emptyList()
                    val wifiWindows = wifiTrigger?.activeTimeWindows ?: emptyList()

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            id = existing.id,
                            title = existing.title,
                            notes = existing.notes,
                            isEnabled = existing.isEnabled,
                            triggerType = existing.triggerType,
                            time = timeTrigger?.time ?: LocalTime.now().plusMinutes(5).withSecond(0).withNano(0),
                            date = activeTriggerDate,
                            repeatDays = activeRepeatDays,
                            repeatInterval = initialRepeatInterval,
                            monthlyDays = initialMonthlyDays,
                            yearlyDates = initialYearlyDates,
                            locationName = existing.locationTrigger?.locationName ?: "",
                            latitude = existing.locationTrigger?.latitude ?: 0.0,
                            longitude = existing.locationTrigger?.longitude ?: 0.0,
                            radiusMeters = existing.locationTrigger?.radiusMeters ?: 100f,
                            geofenceTransitionType = existing.locationTrigger?.transitionType ?: GeofenceTransitionType.ENTER,
                            locationTimeWindow = existing.locationTrigger?.timeWindow,
                            locationTimeWindows = locWindows,
                            wifiSsid = existing.wifiTrigger?.ssid ?: "",
                            wifiBssid = existing.wifiTrigger?.bssid,
                            wifiTriggerType = existing.wifiTrigger?.triggerType ?: WifiTriggerType.ON_CONNECT,
                            wifiTimeWindow = existing.wifiTrigger?.timeWindow,
                            wifiTimeWindows = wifiWindows,
                            checklist = existing.checklist,
                            notificationStyle = existing.notificationStyle,
                            isPersistentAlert = existing.isPersistentAlert,
                            autoSilenceMinutes = existing.autoSilenceMinutes,
                            useAlarmStream = existing.useAlarmStream,
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
                    monthlyDays = listOf(now.dayOfMonth),
                    yearlyDates = listOf(ReminderMonthDay(now.monthValue, now.dayOfMonth)),
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
            state.copy(time = time)
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
            it.copy(
                repeatDays = days,
                repeatInterval = if (days.isNotEmpty()) ReminderRepeatInterval.WEEKLY else it.repeatInterval,
                date = if (days.isNotEmpty()) null else it.date
            )
        }
    }

    fun setRepeatInterval(interval: ReminderRepeatInterval) {
        _uiState.update { state ->
            val now = LocalDate.now()
            when (interval) {
                ReminderRepeatInterval.ONCE -> {
                    state.copy(
                        repeatInterval = interval,
                        repeatDays = emptyList(),
                        date = state.date ?: now
                    )
                }
                ReminderRepeatInterval.WEEKLY -> {
                    val currentWeeklyDays = if (state.repeatDays.isNotEmpty()) {
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
                    val currentDays = if (state.monthlyDays.isNotEmpty()) state.monthlyDays else listOf(now.dayOfMonth)
                    state.copy(
                        repeatInterval = interval,
                        monthlyDays = currentDays,
                        repeatDays = emptyList(),
                        date = null
                    )
                }
                ReminderRepeatInterval.YEARLY -> {
                    val currentDates = if (state.yearlyDates.isNotEmpty()) state.yearlyDates else listOf(ReminderMonthDay(now.monthValue, now.dayOfMonth))
                    state.copy(
                        repeatInterval = interval,
                        yearlyDates = currentDates,
                        repeatDays = emptyList(),
                        date = null
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
            state.copy(
                repeatDays = newDays,
                repeatInterval = ReminderRepeatInterval.WEEKLY,
                date = null
            )
        }
    }

    fun setMonthlyDays(days: List<Int>) {
        val clamped = days.map { it.coerceIn(1, 31) }.distinct().sorted()
        _uiState.update { it.copy(monthlyDays = clamped, date = null, repeatDays = emptyList()) }
    }

    fun toggleMonthlyDay(day: Int) {
        val clamped = day.coerceIn(1, 31)
        _uiState.update { state ->
            val current = state.monthlyDays
            val updated = if (current.contains(clamped)) {
                if (current.size > 1) current - clamped else current
            } else {
                (current + clamped).sorted()
            }
            state.copy(monthlyDays = updated, date = null, repeatDays = emptyList())
        }
    }

    fun setYearlyDates(dates: List<ReminderMonthDay>) {
        _uiState.update { it.copy(yearlyDates = dates, date = null, repeatDays = emptyList()) }
    }

    fun addYearlyDate(month: Int, day: Int) {
        val m = month.coerceIn(1, 12)
        val maxDays = java.time.Month.of(m).maxLength()
        val d = day.coerceIn(1, maxDays)
        val newEntry = ReminderMonthDay(m, d)
        _uiState.update { state ->
            if (state.yearlyDates.none { it.month == m && it.day == d }) {
                state.copy(yearlyDates = state.yearlyDates + newEntry, date = null, repeatDays = emptyList())
            } else {
                state
            }
        }
    }

    fun removeYearlyDate(index: Int) {
        _uiState.update { state ->
            if (index in state.yearlyDates.indices && state.yearlyDates.size > 1) {
                val updated = state.yearlyDates.toMutableList().apply { removeAt(index) }
                state.copy(yearlyDates = updated)
            } else state
        }
    }

    fun setLocationName(name: String) {
        _uiState.update { it.copy(locationName = name, showLocationError = false) }
    }

    fun setLocationCoordinates(lat: Double, lng: Double) {
        _uiState.update { it.copy(latitude = lat, longitude = lng, showLocationError = false) }
    }

    fun setIsEnabled(enabled: Boolean) {
        _uiState.update { it.copy(isEnabled = enabled) }
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
        _uiState.update {
            it.copy(
                locationTimeWindow = timeWindow,
                locationTimeWindows = if (timeWindow != null) listOf(timeWindow) else emptyList()
            )
        }
    }

    fun setLocationTimeWindows(windows: List<ReminderTimeWindow>) {
        _uiState.update {
            it.copy(
                locationTimeWindows = windows,
                locationTimeWindow = windows.firstOrNull()
            )
        }
    }

    fun addLocationTimeWindow(window: ReminderTimeWindow) {
        _uiState.update {
            val updated = it.locationTimeWindows + window
            it.copy(locationTimeWindows = updated, locationTimeWindow = updated.firstOrNull())
        }
    }

    fun removeLocationTimeWindow(index: Int) {
        _uiState.update {
            if (index in it.locationTimeWindows.indices) {
                val updated = it.locationTimeWindows.toMutableList().apply { removeAt(index) }
                it.copy(locationTimeWindows = updated, locationTimeWindow = updated.firstOrNull())
            } else it
        }
    }

    fun setAllDayPresetLocation() {
        setLocationTimeWindows(listOf(ReminderTimeWindow.ALL_DAY))
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
        _uiState.update {
            it.copy(
                wifiTimeWindow = timeWindow,
                wifiTimeWindows = if (timeWindow != null) listOf(timeWindow) else emptyList()
            )
        }
    }

    fun setWifiTimeWindows(windows: List<ReminderTimeWindow>) {
        _uiState.update {
            it.copy(
                wifiTimeWindows = windows,
                wifiTimeWindow = windows.firstOrNull()
            )
        }
    }

    fun addWifiTimeWindow(window: ReminderTimeWindow) {
        _uiState.update {
            val updated = it.wifiTimeWindows + window
            it.copy(wifiTimeWindows = updated, wifiTimeWindow = updated.firstOrNull())
        }
    }

    fun removeWifiTimeWindow(index: Int) {
        _uiState.update {
            if (index in it.wifiTimeWindows.indices) {
                val updated = it.wifiTimeWindows.toMutableList().apply { removeAt(index) }
                it.copy(wifiTimeWindows = updated, wifiTimeWindow = updated.firstOrNull())
            } else it
        }
    }

    fun setAllDayPresetWifi() {
        setWifiTimeWindows(listOf(ReminderTimeWindow.ALL_DAY))
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

    fun setAutoSilenceMinutes(minutes: Int) {
        _uiState.update { it.copy(autoSilenceMinutes = minutes) }
    }

    fun setUseAlarmStream(useAlarmStream: Boolean) {
        _uiState.update { it.copy(useAlarmStream = useAlarmStream) }
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
                date = if (state.repeatInterval == ReminderRepeatInterval.ONCE) state.date else null,
                repeatDays = if (state.repeatInterval == ReminderRepeatInterval.WEEKLY) state.repeatDays else emptyList(),
                monthlyDays = if (state.repeatInterval == ReminderRepeatInterval.MONTHLY) state.monthlyDays else emptyList(),
                yearlyDates = if (state.repeatInterval == ReminderRepeatInterval.YEARLY) state.yearlyDates else emptyList()
            )
        } else null

        val locationTrigger = if (state.triggerType == ReminderTriggerType.LOCATION) {
            val windows = state.locationTimeWindows.ifEmpty {
                if (state.locationTimeWindow != null) listOf(state.locationTimeWindow) else emptyList()
            }
            LocationTrigger(
                latitude = state.latitude,
                longitude = state.longitude,
                radiusMeters = state.radiusMeters,
                locationName = state.locationName,
                transitionType = state.geofenceTransitionType,
                timeWindow = windows.firstOrNull(),
                timeWindows = windows,
                date = if (state.repeatInterval == ReminderRepeatInterval.ONCE) state.date else null,
                repeatDays = if (state.repeatInterval == ReminderRepeatInterval.WEEKLY) state.repeatDays else emptyList(),
                monthlyDays = if (state.repeatInterval == ReminderRepeatInterval.MONTHLY) state.monthlyDays else emptyList(),
                yearlyDates = if (state.repeatInterval == ReminderRepeatInterval.YEARLY) state.yearlyDates else emptyList()
            )
        } else null

        val wifiTrigger = if (state.triggerType == ReminderTriggerType.WIFI) {
            val windows = state.wifiTimeWindows.ifEmpty {
                if (state.wifiTimeWindow != null) listOf(state.wifiTimeWindow) else emptyList()
            }
            WifiTrigger(
                ssid = state.wifiSsid,
                bssid = state.wifiBssid,
                triggerType = state.wifiTriggerType,
                timeWindow = windows.firstOrNull(),
                timeWindows = windows,
                date = if (state.repeatInterval == ReminderRepeatInterval.ONCE) state.date else null,
                repeatDays = if (state.repeatInterval == ReminderRepeatInterval.WEEKLY) state.repeatDays else emptyList(),
                monthlyDays = if (state.repeatInterval == ReminderRepeatInterval.MONTHLY) state.monthlyDays else emptyList(),
                yearlyDates = if (state.repeatInterval == ReminderRepeatInterval.YEARLY) state.yearlyDates else emptyList()
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
            autoSilenceMinutes = state.autoSilenceMinutes,
            useAlarmStream = state.useAlarmStream,
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

        performSave(draft, onSuccess)
    }

    fun saveAnyway(onSuccess: () -> Unit) {
        _uiState.update { it.copy(showPermissionDialog = false) }
        val draft = createDraftReminder().copy(isEnabled = false)
        performSave(draft, onSuccess)
    }

    fun deleteReminder(onSuccess: () -> Unit = {}) {
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

    private fun performSave(reminderToSave: Reminder, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (reminderToSave.id == 0L) {
                val newId = reminderRepository.insertReminder(reminderToSave)
                val saved = reminderToSave.copy(id = newId)
                if (saved.isEnabled) {
                    reminderScheduler.schedule(saved)
                }
            } else {
                reminderScheduler.cancelSnooze(reminderToSave)
                reminderAlertManager?.stopAlert(reminderToSave.id)
                reminderNotificationManager?.cancelNotification(reminderToSave.id)
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
        val missing = permissionChecker.getMissingPermissions(draft)
        _uiState.update {
            it.copy(
                missingPermissions = missing,
                showPermissionDialog = it.showPermissionDialog && missing.isNotEmpty()
            )
        }
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
                curr.locationName != init.locationName ||
                curr.latitude != init.latitude ||
                curr.longitude != init.longitude ||
                curr.radiusMeters != init.radiusMeters ||
                curr.geofenceTransitionType != init.geofenceTransitionType ||
                curr.locationTimeWindow != init.locationTimeWindow ||
                curr.locationTimeWindows != init.locationTimeWindows ||
                curr.wifiSsid != init.wifiSsid ||
                curr.wifiBssid != init.wifiBssid ||
                curr.wifiTriggerType != init.wifiTriggerType ||
                curr.wifiTimeWindow != init.wifiTimeWindow ||
                curr.wifiTimeWindows != init.wifiTimeWindows ||
                curr.checklist != init.checklist ||
                curr.notificationStyle != init.notificationStyle ||
                curr.isPersistentAlert != init.isPersistentAlert ||
                curr.autoSilenceMinutes != init.autoSilenceMinutes ||
                curr.useAlarmStream != init.useAlarmStream ||
                curr.ringtoneUri != init.ringtoneUri ||
                curr.vibrationEnabled != init.vibrationEnabled ||
                curr.volume != init.volume ||
                curr.snoozeEnabled != init.snoozeEnabled ||
                curr.snoozeIntervalMinutes != init.snoozeIntervalMinutes ||
                curr.snoozeRepeatCount != init.snoozeRepeatCount
    }
}
