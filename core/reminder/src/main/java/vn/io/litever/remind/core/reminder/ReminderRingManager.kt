package vn.io.litever.remind.core.reminder

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRingManager @Inject constructor() {
    private val _ringingReminderId = MutableStateFlow<Long?>(null)
    val ringingReminderId: StateFlow<Long?> = _ringingReminderId.asStateFlow()

    private val _autoSilenceCountdown = MutableStateFlow<Int?>(null)
    val autoSilenceCountdown: StateFlow<Int?> = _autoSilenceCountdown.asStateFlow()

    fun setRinging(reminderId: Long?) {
        _ringingReminderId.value = reminderId
    }

    fun setAutoSilenceCountdown(seconds: Int?) {
        _autoSilenceCountdown.value = seconds
    }
}
