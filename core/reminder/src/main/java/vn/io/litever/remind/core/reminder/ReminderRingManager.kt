package vn.io.litever.remind.core.reminder

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
class ReminderRingManager @Inject constructor() {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _ringingQueue = MutableStateFlow<List<Long>>(emptyList())
    val ringingReminderId: StateFlow<Long?> = _ringingQueue
        .map { it.firstOrNull() }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    fun enqueueReminder(reminderId: Long) {
        val currentQueue = _ringingQueue.value
        if (!currentQueue.contains(reminderId)) {
            _ringingQueue.value = currentQueue + reminderId
        }
    }

    fun dequeueReminder(reminderId: Long) {
        _ringingQueue.update { it - reminderId }
        _mutedReminderIds.update { it - reminderId }
    }

    private val _mutedReminderIds = MutableStateFlow<Set<Long>>(emptySet())
    val mutedReminderIds = _mutedReminderIds.asStateFlow()

    fun mute(reminderId: Long) {
        _mutedReminderIds.value = _mutedReminderIds.value + reminderId
    }

    fun unmute(reminderId: Long) {
        _mutedReminderIds.value = _mutedReminderIds.value - reminderId
    }

    private val _autoSilenceCountdown = MutableStateFlow<Int?>(null)
    val autoSilenceCountdown: StateFlow<Int?> = _autoSilenceCountdown.asStateFlow()

    fun setAutoSilenceCountdown(seconds: Int?) {
        _autoSilenceCountdown.value = seconds
    }

    private val _acknowledgingReminderId = MutableStateFlow<Long?>(null)
    val acknowledgingReminderId: StateFlow<Long?> = _acknowledgingReminderId.asStateFlow()

    fun setAcknowledgingReminderId(reminderId: Long?) {
        _acknowledgingReminderId.value = reminderId
    }
}
