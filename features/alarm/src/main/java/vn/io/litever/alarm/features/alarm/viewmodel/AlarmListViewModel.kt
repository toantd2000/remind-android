package vn.io.litever.alarm.features.alarm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import vn.io.litever.alarm.core.domain.repository.AlarmRepository
import vn.io.litever.alarm.core.domain.scheduler.AlarmScheduler
import vn.io.litever.alarm.core.model.Alarm
import vn.io.litever.alarm.features.alarm.ui.state.NextAlarmUiState
import vn.io.litever.alarm.features.alarm.ui.state.calculateNextAlarm
import javax.inject.Inject

import vn.io.litever.alarm.core.datastore.AlarmPreferencesDataSource

@HiltViewModel
class AlarmListViewModel @Inject constructor(
    private val repository: AlarmRepository,
    private val alarmScheduler: AlarmScheduler,
    private val preferencesDataSource: AlarmPreferencesDataSource
) : ViewModel() {

    val is24HourFormat: StateFlow<Boolean> = preferencesDataSource.is24HourFormat
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val alarms: StateFlow<List<Alarm>> = repository.getAllAlarms()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val nextAlarmTime: StateFlow<NextAlarmUiState> = alarms
        .map { list ->
            calculateNextAlarm(list.filter { it.isEnabled })
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NextAlarmUiState.AllOff
        )

    fun toggleAlarm(alarm: Alarm) {
        viewModelScope.launch {
            val updatedAlarm = alarm.copy(isEnabled = !alarm.isEnabled)
            repository.updateAlarm(updatedAlarm)
            if (updatedAlarm.isEnabled) {
                alarmScheduler.schedule(updatedAlarm)
            } else {
                alarmScheduler.cancel(updatedAlarm)
            }
        }
    }

    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            repository.deleteAlarm(alarm)
            alarmScheduler.cancel(alarm)
        }
    }

    fun deleteDisabledAlarms() {
        viewModelScope.launch {
            val disabledAlarms = alarms.value.filter { !it.isEnabled }
            disabledAlarms.forEach { alarm ->
                repository.deleteAlarm(alarm)
                alarmScheduler.cancel(alarm)
            }
        }
    }

    private fun calculateNextAlarm(enabledAlarms: List<Alarm>): NextAlarmUiState {
        return vn.io.litever.alarm.features.alarm.ui.state.calculateNextAlarm(enabledAlarms)
    }
}
