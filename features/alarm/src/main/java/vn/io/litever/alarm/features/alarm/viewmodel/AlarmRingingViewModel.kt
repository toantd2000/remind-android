package vn.io.litever.alarm.features.alarm.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import vn.io.litever.alarm.core.domain.scheduler.AlarmController
import javax.inject.Inject

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import vn.io.litever.alarm.core.datastore.AlarmPreferencesDataSource

@HiltViewModel
class AlarmRingingViewModel @Inject constructor(
    private val alarmController: AlarmController,
    private val preferencesDataSource: AlarmPreferencesDataSource
) : ViewModel() {

    val is24HourFormat: StateFlow<Boolean> = preferencesDataSource.is24HourFormat
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    fun dismissAlarm() {
        alarmController.dismissAlarm()
    }

    fun snoozeAlarm() {
        alarmController.snoozeAlarm()
    }
}
