package vn.io.litever.remind.features.reminders.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import vn.io.litever.remind.core.alarm.alert.ReminderAlertManager
import vn.io.litever.remind.core.alarm.notification.ReminderNotificationManager
import vn.io.litever.remind.core.datastore.AlarmPreferencesDataSource
import vn.io.litever.remind.core.domain.handler.ReminderActionHandler
import vn.io.litever.remind.core.domain.repository.ReminderRepository
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.core.model.RepeatMode
import java.time.LocalDateTime
import javax.inject.Inject

data class ReminderRingingUiState(
    val reminder: Reminder? = null,
    val is24HourFormat: Boolean = true,
    val autoSilenceCountdown: Int? = null,
    val isRinging: Boolean = true
)

@HiltViewModel
class ReminderRingingViewModel @Inject constructor(
    private val reminderRepository: ReminderRepository,
    private val reminderActionHandler: ReminderActionHandler,
    private val reminderAlertManager: ReminderAlertManager,
    private val reminderNotificationManager: ReminderNotificationManager,
    preferencesDataSource: AlarmPreferencesDataSource,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val reminderId: Long = savedStateHandle.get<Long>("reminderId") ?: -1L

    private val _isRinging = MutableStateFlow(true)

    val uiState: StateFlow<ReminderRingingUiState> = combine(
        reminderRepository.getReminderFlow(reminderId),
        preferencesDataSource.is24HourFormat,
        reminderAlertManager.autoSilenceCountdown,
        _isRinging
    ) { reminder, is24h, countdown, isRinging ->
        ReminderRingingUiState(
            reminder = reminder,
            is24HourFormat = is24h,
            autoSilenceCountdown = countdown,
            isRinging = isRinging
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReminderRingingUiState()
    )


    fun snooze(minutes: Int? = null, onFinished: () -> Unit) {
        viewModelScope.launch {
            val reminder = uiState.value.reminder ?: reminderRepository.getReminderById(reminderId)
            reminderAlertManager.stopAlert(reminderId)
            reminderNotificationManager.cancelNotification(reminderId)

            if (reminder != null && reminder.canSnooze()) {
                val snoozeMinutes = minutes ?: reminder.action.snoozeIntervalMinutes
                reminderActionHandler.handleSnooze(reminder, snoozeMinutes)
            }

            _isRinging.value = false
            onFinished()
        }
    }

    fun dismiss(onFinished: () -> Unit) {
        viewModelScope.launch {
            val reminder = uiState.value.reminder ?: reminderRepository.getReminderById(reminderId)
            reminderAlertManager.stopAlert(reminderId)
            reminderNotificationManager.cancelNotification(reminderId)

            if (reminder != null) {
                if (reminder.triggerType == ReminderTriggerType.INTERVAL) {
                    // Acknowledging an INTERVAL reminder shifts the cycle and schedules the next trigger
                    reminderActionHandler.handleComplete(reminder)
                } else {
                    val now = LocalDateTime.now()
                    val trigger = reminder.timeTrigger
                    val hasNextExactOccurrence = trigger != null &&
                        trigger.repeatMode == RepeatMode.NONE &&
                        trigger.effectiveExactDates.isNotEmpty() &&
                        trigger.getNextOccurrence(now = now, lastTriggeredTime = now, isInterval = false) != null
                    val shouldStayEnabled = reminder.isRecurring || hasNextExactOccurrence
                    val updatedReminder = reminder.copy(
                        isEnabled = if (shouldStayEnabled) reminder.isEnabled else false,
                        snoozeNextTriggerTime = null,
                        lastTriggeredTime = if (shouldStayEnabled) null else reminder.lastTriggeredTime
                    )
                    reminderActionHandler.handleDismiss(updatedReminder)
                }
            }

            _isRinging.value = false
            onFinished()
        }
    }
}
