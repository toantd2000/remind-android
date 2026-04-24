package vn.io.litever.remind.features.reminder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import vn.io.litever.remind.core.common.util.PermissionChecker
import vn.io.litever.remind.core.domain.repository.ReminderRepository
import vn.io.litever.remind.core.domain.scheduler.ReminderScheduler
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.features.reminder.ui.state.NextReminderUiState
import vn.io.litever.remind.features.reminder.ui.state.calculateNextReminder
import javax.inject.Inject

import vn.io.litever.remind.core.datastore.ReminderPreferencesDataSource

@HiltViewModel
class ReminderListViewModel @Inject constructor(
    private val repository: ReminderRepository,
    private val reminderScheduler: ReminderScheduler,
    private val preferencesDataSource: ReminderPreferencesDataSource,
    private val permissionChecker: vn.io.litever.remind.core.common.util.PermissionChecker
) : ViewModel() {

    private val _hasCriticalPermissions = MutableStateFlow(true)
    val hasCriticalPermissions: StateFlow<Boolean> = _hasCriticalPermissions.asStateFlow()

    private val _uiMessage = MutableSharedFlow<Int>()
    val uiMessage = _uiMessage.asSharedFlow()

    init {
        refreshPermissions()
    }

    fun refreshPermissions() {
        _hasCriticalPermissions.value = permissionChecker.hasCriticalPermissions()
    }

    val is24HourFormat: StateFlow<Boolean> = preferencesDataSource.is24HourFormat
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val reminders: StateFlow<List<Reminder>?> = repository.getAllReminders()
        .map { list ->
            list.sortedWith(
                compareByDescending<Reminder> { it.isEnabled }
                    .thenBy { it.time }
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val nextReminderTime: StateFlow<NextReminderUiState> = reminders
        .map { list ->
            if (list == null) NextReminderUiState.AllOff
            else calculateNextReminder(list.filter { it.isEnabled })
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NextReminderUiState.AllOff
        )

    fun toggleReminder(reminder: Reminder) {
        viewModelScope.launch {
            val isEnabling = !reminder.isEnabled
            
            // Block enabling if permissions are missing
            if (isEnabling && !permissionChecker.hasCriticalPermissions()) {
                _hasCriticalPermissions.value = false
                _uiMessage.emit(vn.io.litever.remind.features.reminder.R.string.error_missing_permissions)
                return@launch
            }

            val updatedReminder = reminder.copy(
                isEnabled = isEnabling,
                skippedAt = null // Clear skip status when toggling
            )
            repository.updateReminder(updatedReminder)
            if (updatedReminder.isEnabled) {
                reminderScheduler.schedule(updatedReminder)
            } else {
                reminderScheduler.cancel(updatedReminder)
            }
        }
    }

    private val _undoEvent = MutableSharedFlow<UndoType>()
    val undoEvent = _undoEvent.asSharedFlow()

    private var lastDeletedReminder: Reminder? = null
    private var lastDeletedReminders: List<Reminder>? = null

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            lastDeletedReminder = reminder
            lastDeletedReminders = null
            repository.deleteReminder(reminder)
            reminderScheduler.cancel(reminder)
            _undoEvent.emit(UndoType.SINGLE)
        }
    }

    fun deleteDisabledReminders() {
        viewModelScope.launch {
            val currentReminders = reminders.value ?: return@launch
            val disabledReminders = currentReminders.filter { !it.isEnabled }
            if (disabledReminders.isEmpty()) return@launch
            
            lastDeletedReminders = disabledReminders
            lastDeletedReminder = null
            disabledReminders.forEach { reminder ->
                repository.deleteReminder(reminder)
                reminderScheduler.cancel(reminder)
            }
            _undoEvent.emit(UndoType.MULTIPLE)
        }
    }

    fun undoDelete() {
        viewModelScope.launch {
            lastDeletedReminder?.let {
                repository.insertReminder(it)
                if (it.isEnabled) reminderScheduler.schedule(it)
                lastDeletedReminder = null
            }
            lastDeletedReminders?.let { list ->
                list.forEach { 
                    repository.insertReminder(it)
                    if (it.isEnabled) reminderScheduler.schedule(it)
                }
                lastDeletedReminders = null
            }
        }
    }

    enum class UndoType { SINGLE, MULTIPLE }

    fun duplicateReminder(reminder: Reminder) {
        viewModelScope.launch {
            // Create a copy with id = 0 to insert as a new record
            val duplicated = reminder.copy(
                id = 0,
                isEnabled = reminder.isEnabled,
                skippedAt = null,
                isMissed = false,
                currentSnoozeCount = 0,
                snoozeNextTriggerTime = null
            )
            val newId = repository.insertReminder(duplicated)
            if (duplicated.isEnabled) {
                val reminderWithId = duplicated.copy(id = newId)
                reminderScheduler.schedule(reminderWithId)
            }
        }
    }

    fun skipNextOccurrence(reminder: Reminder) {
        viewModelScope.launch {
            if (reminder.skippedAt != null) return@launch // Already skipped
            
            // Find the actual nearest next occurrence and mark it as skipped
            val nextTime = reminder.getActualNextOccurrence() 
            val updated = reminder.copy(skippedAt = nextTime)
            repository.updateReminder(updated)
            
            if (updated.isEnabled) {
                reminderScheduler.schedule(updated)
            }
        }
    }

    fun cancelSkipOccurrence(reminder: Reminder) {
        viewModelScope.launch {
            val updated = reminder.copy(skippedAt = null)
            repository.updateReminder(updated)
            
            if (updated.isEnabled) {
                reminderScheduler.schedule(updated)
            }
        }
    }


    private fun calculateNextReminder(enabledReminders: List<Reminder>): NextReminderUiState {
        return vn.io.litever.remind.features.reminder.ui.state.calculateNextReminder(enabledReminders)
    }
}
