package vn.io.litever.remind.features.reminders.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
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
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.features.reminders.R
import vn.io.litever.remind.features.reminders.ui.components.ReminderFilterType
import javax.inject.Inject

data class ReminderListUiState(
    val reminders: List<Reminder>? = null,
    val filteredReminders: List<Reminder> = emptyList(),
    val selectedFilter: ReminderFilterType = ReminderFilterType.ALL,
    val filterCounts: Map<ReminderFilterType, Int> = emptyMap(),
    val is24HourFormat: Boolean = true,
    val hasPermissionWarning: Boolean = false,
    val missingPermissions: List<ReminderPermissionType> = emptyList()
)

@HiltViewModel
class ReminderListViewModel @Inject constructor(
    private val reminderRepository: ReminderRepository,
    private val reminderScheduler: ReminderScheduler,
    private val permissionChecker: PermissionChecker,
    private val preferencesDataSource: AlarmPreferencesDataSource,
    private val reminderSyncManager: ReminderSyncManager? = null,
    private val reminderAlertManager: ReminderAlertManager? = null,
    private val reminderNotificationManager: ReminderNotificationManager? = null
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(ReminderFilterType.ALL)
    private val _hasPermissionWarning = MutableStateFlow(false)
    private val _permissionCheckTrigger = MutableStateFlow(0)
    private val _uiMessage = MutableSharedFlow<Int>()
    val uiMessage: SharedFlow<Int> = _uiMessage.asSharedFlow()

    private var recentlyDeletedReminder: Reminder? = null

    init {
        viewModelScope.launch {
            try {
                reminderRepository.checkAndResetRecurringChecklists()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val uiState: StateFlow<ReminderListUiState> = combine(
        reminderRepository.getAllReminders(),
        preferencesDataSource.is24HourFormat,
        _selectedFilter,
        _hasPermissionWarning,
        _permissionCheckTrigger
    ) { allReminders, is24Hour, filter, permissionWarning, _ ->
        val counts = mapOf(
            ReminderFilterType.ALL to allReminders.size,
            ReminderFilterType.ACTIVE to allReminders.count { it.isEnabled && !it.isCompleted },
            ReminderFilterType.COMPLETED to allReminders.count { it.isCompleted },
            ReminderFilterType.TIME to allReminders.count { it.triggerType == ReminderTriggerType.FIXED_TIME || it.triggerType == ReminderTriggerType.INTERVAL },
            ReminderFilterType.LOCATION to allReminders.count { false },
            ReminderFilterType.WIFI to allReminders.count { false }
        )

        val filtered = when (filter) {
            ReminderFilterType.ALL -> allReminders
            ReminderFilterType.ACTIVE -> allReminders.filter { it.isEnabled && !it.isCompleted }
            ReminderFilterType.COMPLETED -> allReminders.filter { it.isCompleted }
            ReminderFilterType.TIME -> allReminders.filter { it.triggerType == ReminderTriggerType.FIXED_TIME || it.triggerType == ReminderTriggerType.INTERVAL }
            ReminderFilterType.LOCATION -> allReminders.filter { false }
            ReminderFilterType.WIFI -> allReminders.filter { false }
        }

        val sortedFiltered = filtered.sortedWith(
            compareByDescending<Reminder> { it.snoozeNextTriggerTime != null }
                .thenByDescending { it.createdAt }
        )

        val hasMissingForActive = allReminders.filter { it.isEnabled && !it.isCompleted }
            .any { !permissionChecker.hasPermissionsForReminder(it) }
        val warningVisible = permissionWarning || !permissionChecker.hasNotificationPermission() || hasMissingForActive

        ReminderListUiState(
            reminders = allReminders,
            filteredReminders = sortedFiltered,
            selectedFilter = filter,
            filterCounts = counts,
            is24HourFormat = is24Hour,
            hasPermissionWarning = warningVisible
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReminderListUiState()
    )

    fun setFilter(filter: ReminderFilterType) {
        _selectedFilter.value = filter
    }

    fun toggleReminder(reminder: Reminder) {
        viewModelScope.launch {
            val isEnabling = !reminder.isEnabled
            if (isEnabling) {
                val missing = permissionChecker.getMissingPermissions(reminder)
                if (missing.isNotEmpty()) {
                    _hasPermissionWarning.value = true
                    _uiMessage.emit(R.string.error_missing_permissions_reminder)
                    return@launch
                }
            }

            val updated = reminder.copy(
                isEnabled = isEnabling,
                snoozeNextTriggerTime = null,
                currentSnoozeCount = if (!isEnabling) 0 else reminder.currentSnoozeCount
            )
            reminderScheduler.cancelSnooze(reminder)
            reminderRepository.updateReminder(updated)

            if (updated.isEnabled && !updated.isCompleted) {
                reminderScheduler.schedule(updated)
            } else {
                reminderScheduler.cancel(updated)
                reminderAlertManager?.stopAlert(reminder.id)
                reminderNotificationManager?.cancelNotification(reminder.id)
            }
            reminderSyncManager?.sync()
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            recentlyDeletedReminder = reminder
            reminderScheduler.cancel(reminder)
            reminderScheduler.cancelSnooze(reminder)
            reminderAlertManager?.stopAlert(reminder.id)
            reminderNotificationManager?.cancelNotification(reminder.id)
            reminderRepository.deleteReminder(reminder)
            reminderSyncManager?.sync()
            _uiMessage.emit(R.string.reminder_deleted)
        }
    }

    fun undoDelete() {
        viewModelScope.launch {
            recentlyDeletedReminder?.let { reminder ->
                val restoredId = reminderRepository.insertReminder(reminder.copy(id = 0))
                val restored = reminder.copy(id = restoredId)
                if (restored.isEnabled && !restored.isCompleted) {
                    reminderScheduler.schedule(restored)
                }
                recentlyDeletedReminder = null
                reminderSyncManager?.sync()
            }
        }
    }

    fun deleteCompletedReminders() {
        viewModelScope.launch {
            val allReminders = reminderRepository.getAllReminders().first()
            val completed = allReminders.filter { it.isCompleted }
            completed.forEach { reminder ->
                reminderScheduler.cancel(reminder)
                reminderScheduler.cancelSnooze(reminder)
                reminderAlertManager?.stopAlert(reminder.id)
                reminderNotificationManager?.cancelNotification(reminder.id)
                reminderRepository.deleteReminder(reminder)
            }
            reminderSyncManager?.sync()
        }
    }

    fun deleteDisabledReminders() {
        viewModelScope.launch {
            val allReminders = reminderRepository.getAllReminders().first()
            val disabled = allReminders.filter { !it.isEnabled && !it.isCompleted }
            disabled.forEach { reminder ->
                reminderScheduler.cancel(reminder)
                reminderScheduler.cancelSnooze(reminder)
                reminderAlertManager?.stopAlert(reminder.id)
                reminderNotificationManager?.cancelNotification(reminder.id)
                reminderRepository.deleteReminder(reminder)
            }
            reminderSyncManager?.sync()
        }
    }

    fun duplicateReminder(reminder: Reminder) {
        viewModelScope.launch {
            val duplicatedChecklist = reminder.checklist.map { item ->
                item.copy(id = 0, reminderId = 0, isCompleted = false)
            }
            val duplicated = reminder.copy(
                id = 0,
                title = "${reminder.title} (Copy)",
                isEnabled = true,
                isCompleted = false,
                currentSnoozeCount = 0,
                snoozeNextTriggerTime = null,
                checklist = duplicatedChecklist,
                createdAt = System.currentTimeMillis()
            )
            val newId = reminderRepository.insertReminder(duplicated)
            val scheduledCopy = duplicated.copy(id = newId)
            if (scheduledCopy.isEnabled && permissionChecker.hasPermissionsForReminder(scheduledCopy)) {
                reminderScheduler.schedule(scheduledCopy)
            }
            reminderSyncManager?.sync()
        }
    }

    fun toggleCompleted(reminder: Reminder) {
        viewModelScope.launch {
            val willBeCompleted = !reminder.isCompleted
            val updated = if (willBeCompleted) {
                reminder.copy(
                    isCompleted = true,
                    isEnabled = false,
                    snoozeNextTriggerTime = null
                )
            } else {
                reminder.copy(
                    isCompleted = false,
                    isEnabled = true,
                    snoozeNextTriggerTime = null
                )
            }

            reminderScheduler.cancelSnooze(reminder)
            reminderRepository.updateReminder(updated)

            if (updated.isEnabled && !updated.isCompleted) {
                if (permissionChecker.hasPermissionsForReminder(updated)) {
                    reminderScheduler.schedule(updated)
                }
            } else {
                reminderScheduler.cancel(updated)
            }
            reminderSyncManager?.sync()
        }
    }

    fun toggleChecklistItem(itemId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            reminderRepository.updateChecklistItemCompletion(itemId, isCompleted)
        }
    }

    fun refreshPermissions() {
        viewModelScope.launch {
            try {
                reminderRepository.checkAndResetRecurringChecklists()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        val activeReminders = uiState.value.reminders?.filter { it.isEnabled && !it.isCompleted } ?: emptyList()
        val hasMissingForActive = activeReminders.any { !permissionChecker.hasPermissionsForReminder(it) }
        _hasPermissionWarning.value = !permissionChecker.hasNotificationPermission() || hasMissingForActive
        _permissionCheckTrigger.update { it + 1 }
    }
}
