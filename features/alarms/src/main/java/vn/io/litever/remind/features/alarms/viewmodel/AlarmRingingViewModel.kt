package vn.io.litever.remind.features.alarms.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import vn.io.litever.remind.core.domain.scheduler.AlarmController
import javax.inject.Inject

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import vn.io.litever.remind.core.domain.repository.AlarmRepository
import vn.io.litever.remind.core.model.Alarm
import vn.io.litever.remind.core.datastore.AlarmPreferencesDataSource
import vn.io.litever.remind.core.alarm.AlarmRingManager
import kotlinx.coroutines.launch

@HiltViewModel
class AlarmRingingViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val alarmRepository: AlarmRepository,
    private val alarmController: AlarmController,
    private val preferencesDataSource: AlarmPreferencesDataSource,
    private val alarmRingManager: AlarmRingManager,
    private val weatherRepository: vn.io.litever.remind.core.domain.repository.WeatherRepository,
    private val reminderRepository: vn.io.litever.remind.core.domain.repository.ReminderRepository
) : ViewModel() {

    private val alarmId: Long = checkNotNull(savedStateHandle["alarmId"])

    val alarm: StateFlow<Alarm?> = alarmRepository.getAllAlarms()
        .map { alarms -> alarms.find { it.id == alarmId } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val is24HourFormat: StateFlow<Boolean> = preferencesDataSource.is24HourFormat
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val autoSilenceCountdown: StateFlow<Int?> = alarmRingManager.autoSilenceCountdown
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val weather: StateFlow<vn.io.litever.remind.core.model.WeatherResponse?> = weatherRepository.getRemindWeather()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val reminder: StateFlow<vn.io.litever.remind.core.model.ReminderResponse?> = reminderRepository.getReminder()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    init {
        viewModelScope.launch {
            launch { weatherRepository.refreshWeather() }
            launch { reminderRepository.refreshReminder() }
        }
    }

    fun dismissAlarm() {
        viewModelScope.launch {
            alarmRingManager.setAcknowledgingAlarmId(alarmId)
            alarmController.dismissAlarm(alarmId)
        }
    }

    fun onFinishMessage() {
        viewModelScope.launch {
            alarmRingManager.setAcknowledgingAlarmId(null)
            alarmController.dismissAlarm(alarmId)
        }
    }

    fun startMission() {
        viewModelScope.launch {
            alarmRingManager.enqueueAlarm(alarmId)
            alarmRingManager.mute(alarmId) // Silence while doing mission
            alarmController.cancelSnooze(alarmId)
        }
    }

    fun onAbandonMission() {
        viewModelScope.launch {
            alarmRingManager.unmute(alarmId) // Resume ringing
        }
    }

    fun snoozeAlarm() {
        viewModelScope.launch {
            alarmController.snoozeAlarm(alarmId)
        }
    }

    fun setRingingScreenVisible(isVisible: Boolean) {
        alarmRingManager.setRingingScreenVisible(isVisible)
    }
}
