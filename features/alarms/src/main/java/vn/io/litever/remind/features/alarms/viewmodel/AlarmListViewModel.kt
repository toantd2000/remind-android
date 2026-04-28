package vn.io.litever.remind.features.alarms.viewmodel

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
import vn.io.litever.remind.core.domain.repository.AlarmRepository
import vn.io.litever.remind.core.domain.scheduler.AlarmScheduler
import vn.io.litever.remind.core.model.Alarm
import vn.io.litever.remind.features.alarms.ui.state.NextAlarmUiState
import vn.io.litever.remind.features.alarms.ui.state.calculateNextAlarm
import javax.inject.Inject

import vn.io.litever.remind.core.datastore.AlarmPreferencesDataSource

@HiltViewModel
class AlarmListViewModel @Inject constructor(
    private val repository: AlarmRepository,
    private val alarmScheduler: AlarmScheduler,
    private val preferencesDataSource: AlarmPreferencesDataSource,
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

    val alarms: StateFlow<List<Alarm>?> = repository.getAllAlarms()
        .map { list ->
            list.sortedWith(
                compareByDescending<Alarm> { it.isEnabled }
                    .thenBy { it.time }
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val nextAlarmTime: StateFlow<NextAlarmUiState> = alarms
        .map { list ->
            if (list == null) NextAlarmUiState.AllOff
            else calculateNextAlarm(list.filter { it.isEnabled })
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NextAlarmUiState.AllOff
        )

    fun toggleAlarm(alarm: Alarm) {
        viewModelScope.launch {
            val isEnabling = !alarm.isEnabled
            
            // Block enabling if permissions are missing
            if (isEnabling && !permissionChecker.hasCriticalPermissions()) {
                _hasCriticalPermissions.value = false
                _uiMessage.emit(vn.io.litever.remind.features.alarms.R.string.error_missing_permissions)
                return@launch
            }

            val updatedAlarm = alarm.copy(
                isEnabled = isEnabling,
                skippedAt = null // Clear skip status when toggling
            )
            repository.updateAlarm(updatedAlarm)
            if (updatedAlarm.isEnabled) {
                alarmScheduler.schedule(updatedAlarm)
            } else {
                alarmScheduler.cancel(updatedAlarm)
            }
        }
    }

    private val _undoEvent = MutableSharedFlow<UndoType>()
    val undoEvent = _undoEvent.asSharedFlow()

    private var lastDeletedAlarm: Alarm? = null
    private var lastDeletedAlarms: List<Alarm>? = null

    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            lastDeletedAlarm = alarm
            lastDeletedAlarms = null
            repository.deleteAlarm(alarm)
            alarmScheduler.cancel(alarm)
            _undoEvent.emit(UndoType.SINGLE)
        }
    }

    fun deleteDisabledAlarms() {
        viewModelScope.launch {
            val currentAlarms = alarms.value ?: return@launch
            val disabledAlarms = currentAlarms.filter { !it.isEnabled }
            if (disabledAlarms.isEmpty()) return@launch
            
            lastDeletedAlarms = disabledAlarms
            lastDeletedAlarm = null
            disabledAlarms.forEach { alarm ->
                repository.deleteAlarm(alarm)
                alarmScheduler.cancel(alarm)
            }
            _undoEvent.emit(UndoType.MULTIPLE)
        }
    }

    fun undoDelete() {
        viewModelScope.launch {
            lastDeletedAlarm?.let {
                repository.insertAlarm(it)
                if (it.isEnabled) alarmScheduler.schedule(it)
                lastDeletedAlarm = null
            }
            lastDeletedAlarms?.let { list ->
                list.forEach { 
                    repository.insertAlarm(it)
                    if (it.isEnabled) alarmScheduler.schedule(it)
                }
                lastDeletedAlarms = null
            }
        }
    }

    enum class UndoType { SINGLE, MULTIPLE }

    fun duplicateAlarm(alarm: Alarm) {
        viewModelScope.launch {
            // Create a copy with id = 0 to insert as a new record
            val duplicated = alarm.copy(
                id = 0,
                isEnabled = alarm.isEnabled,
                skippedAt = null,
                currentSnoozeCount = 0,
                snoozeNextTriggerTime = null
            )
            val newId = repository.insertAlarm(duplicated)
            if (duplicated.isEnabled) {
                val alarmWithId = duplicated.copy(id = newId)
                alarmScheduler.schedule(alarmWithId)
            }
        }
    }

    fun skipNextOccurrence(alarm: Alarm) {
        viewModelScope.launch {
            if (alarm.skippedAt != null) return@launch // Already skipped
            
            // Find the actual nearest next occurrence and mark it as skipped
            val nextTime = alarm.getActualNextOccurrence() 
            val updated = alarm.copy(skippedAt = nextTime)
            repository.updateAlarm(updated)
            
            if (updated.isEnabled) {
                alarmScheduler.schedule(updated)
            }
        }
    }

    fun cancelSkipOccurrence(alarm: Alarm) {
        viewModelScope.launch {
            val updated = alarm.copy(skippedAt = null)
            repository.updateAlarm(updated)
            
            if (updated.isEnabled) {
                alarmScheduler.schedule(updated)
            }
        }
    }


    private fun calculateNextAlarm(enabledAlarms: List<Alarm>): NextAlarmUiState {
        return vn.io.litever.remind.features.alarms.ui.state.calculateNextAlarm(enabledAlarms)
    }
}










