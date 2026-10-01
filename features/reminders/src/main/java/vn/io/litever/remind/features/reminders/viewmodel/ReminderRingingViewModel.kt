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
import vn.io.litever.remind.core.domain.repository.ReminderRepository
import vn.io.litever.remind.core.domain.scheduler.ReminderScheduler
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderTriggerType
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
    private val reminderScheduler: ReminderScheduler,
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
                val snoozeIntervalMillis = (minutes ?: reminder.action.snoozeIntervalMinutes) * 60 * 1000L
                val nextTriggerTime = System.currentTimeMillis() + snoozeIntervalMillis

                val snoozedReminder = reminder.copy(
                    snoozeNextTriggerTime = nextTriggerTime
                )
                reminderRepository.updateReminder(snoozedReminder)
                reminderScheduler.scheduleSnooze(snoozedReminder, nextTriggerTime)
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
                val isRecurring = reminder.isRecurring
                val completedReminder = reminder.copy(
                    isCompleted = true,
                    isEnabled = if (isRecurring) reminder.isEnabled else false,
                    snoozeNextTriggerTime = null
                )
                reminderRepository.updateReminder(completedReminder)
                reminderScheduler.cancelSnooze(reminder)
            }

            _isRinging.value = false
            onFinished()
        }
    }
}
