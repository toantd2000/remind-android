package vn.io.litever.remind.core.reminder

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRingManager @Inject constructor() {
    private val _ringingAlarmId = MutableStateFlow<Long?>(null)
    val ringingAlarmId: StateFlow<Long?> = _ringingAlarmId.asStateFlow()

    fun setRinging(alarmId: Long?) {
        _ringingAlarmId.value = alarmId
    }
}
