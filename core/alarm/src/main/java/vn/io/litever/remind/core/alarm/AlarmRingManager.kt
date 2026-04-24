package vn.io.litever.remind.core.alarm

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.update

@Singleton
class AlarmRingManager @Inject constructor() {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _ringingQueue = MutableStateFlow<List<Long>>(emptyList())
    val ringingAlarmId: StateFlow<Long?> = _ringingQueue
        .map { it.firstOrNull() }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    fun enqueueAlarm(alarmId: Long) {
        val currentQueue = _ringingQueue.value
        if (!currentQueue.contains(alarmId)) {
            _ringingQueue.value = currentQueue + alarmId
        }
    }

    fun dequeueAlarm(alarmId: Long) {
        _ringingQueue.update { it - alarmId }
        _mutedAlarmIds.update { it - alarmId }
    }

    private val _mutedAlarmIds = MutableStateFlow<Set<Long>>(emptySet())
    val mutedAlarmIds = _mutedAlarmIds.asStateFlow()

    fun mute(alarmId: Long) {
        _mutedAlarmIds.value = _mutedAlarmIds.value + alarmId
    }

    fun unmute(alarmId: Long) {
        _mutedAlarmIds.value = _mutedAlarmIds.value - alarmId
    }

    private val _autoSilenceCountdown = MutableStateFlow<Int?>(null)
    val autoSilenceCountdown: StateFlow<Int?> = _autoSilenceCountdown.asStateFlow()

    fun setAutoSilenceCountdown(seconds: Int?) {
        _autoSilenceCountdown.value = seconds
    }

    private val _acknowledgingAlarmId = MutableStateFlow<Long?>(null)
    val acknowledgingAlarmId: StateFlow<Long?> = _acknowledgingAlarmId.asStateFlow()

    fun setAcknowledgingAlarmId(alarmId: Long?) {
        _acknowledgingAlarmId.value = alarmId
    }
}










