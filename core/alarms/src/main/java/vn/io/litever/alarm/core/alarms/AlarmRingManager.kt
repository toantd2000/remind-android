package vn.io.litever.alarm.core.alarms

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmRingManager @Inject constructor() {
    private val _ringingAlarmId = MutableStateFlow<Long?>(null)
    val ringingAlarmId: StateFlow<Long?> = _ringingAlarmId.asStateFlow()

    fun setRinging(alarmId: Long?) {
        _ringingAlarmId.value = alarmId
    }
}
