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

    val reminders: StateFlow<List<Reminder>> = repository.getAllReminders()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val nextReminderTime: StateFlow<NextReminderUiState> = reminders
        .map { list ->
            calculateNextReminder(list.filter { it.isEnabled })
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

            val updatedReminder = reminder.copy(isEnabled = isEnabling)
            repository.updateReminder(updatedReminder)
            if (updatedReminder.isEnabled) {
                reminderScheduler.schedule(updatedReminder)
            } else {
                reminderScheduler.cancel(updatedReminder)
            }
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
            reminderScheduler.cancel(reminder)
        }
    }

    fun deleteDisabledReminders() {
        viewModelScope.launch {
            val disabledReminders = reminders.value.filter { !it.isEnabled }
            disabledReminders.forEach { reminder ->
                repository.deleteReminder(reminder)
                reminderScheduler.cancel(reminder)
            }
        }
    }

    private fun calculateNextReminder(enabledReminders: List<Reminder>): NextReminderUiState {
        return vn.io.litever.remind.features.reminder.ui.state.calculateNextReminder(enabledReminders)
    }
}
