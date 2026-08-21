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
import vn.io.litever.remind.core.model.TimeTrigger
import vn.io.litever.remind.core.model.WifiTrigger
import vn.io.litever.remind.core.model.WifiTriggerType
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

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
    val missingPermissions: List<ReminderPermissionType> = emptyList(),
    val isNewDraft: Boolean = true
)

@HiltViewModel
class ReminderEditViewModel @Inject constructor(
    private val reminderRepository: ReminderRepository,
    private val reminderScheduler: ReminderScheduler,
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
            if (reminderId > 0L) {
                val existing = reminderRepository.getReminderById(reminderId)
                if (existing != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            id = existing.id,
                            title = existing.title,
                            notes = existing.notes,
                            isEnabled = existing.isEnabled,
                            triggerType = existing.triggerType,
                            time = existing.timeTrigger?.time ?: LocalTime.now().plusMinutes(5).withSecond(0).withNano(0),
                            date = existing.timeTrigger?.date,
                            repeatDays = existing.timeTrigger?.repeatDays ?: emptyList(),
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
        _uiState.update { it.copy(time = time) }
    }

    fun setDate(date: LocalDate?) {
        _uiState.update {
            it.copy(
                date = date,
                repeatDays = if (date != null) emptyList() else it.repeatDays
            )
        }
    }

    fun setRepeatDays(days: List<DayOfWeek>) {
        _uiState.update {
            it.copy(
                repeatDays = days,
                date = if (days.isNotEmpty()) null else it.date
            )
        }
    }

    fun setLocationName(name: String) {
        _uiState.update { it.copy(locationName = name) }
    }

    fun setLatitude(lat: Double) {
        _uiState.update { it.copy(latitude = lat) }
    }

    fun setLongitude(lng: Double) {
        _uiState.update { it.copy(longitude = lng) }
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
        _uiState.update { it.copy(wifiSsid = ssid) }
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
            _uiState.update { it.copy(showTitleError = true) }
            return
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
