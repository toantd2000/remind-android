package vn.io.litever.remind.features.reminder.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import vn.io.litever.remind.core.domain.scheduler.ReminderController
import javax.inject.Inject

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import vn.io.litever.remind.core.domain.repository.ReminderRepository
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.datastore.ReminderPreferencesDataSource
import vn.io.litever.remind.core.reminder.ReminderRingManager
import kotlinx.coroutines.launch

@HiltViewModel
class ReminderRingingViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val reminderRepository: ReminderRepository,
    private val reminderController: ReminderController,
    private val preferencesDataSource: ReminderPreferencesDataSource,
    private val reminderRingManager: ReminderRingManager
) : ViewModel() {

    private val reminderId: Long = checkNotNull(savedStateHandle["reminderId"])

    val reminder: StateFlow<Reminder?> = reminderRepository.getAllReminders()
        .map { reminders -> reminders.find { it.id == reminderId } }
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

    val autoSilenceCountdown: StateFlow<Int?> = reminderRingManager.autoSilenceCountdown
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun dismissReminder() {
        viewModelScope.launch {
            reminderRingManager.setAcknowledgingReminderId(reminderId)
            reminderController.dismissReminder(reminderId)
        }
    }

    fun onFinishMessage() {
        viewModelScope.launch {
            reminderRingManager.setAcknowledgingReminderId(null)
            reminderController.dismissReminder(reminderId)
        }
    }

    fun startMission() {
        viewModelScope.launch {
            reminderRingManager.enqueueReminder(reminderId)
            reminderRingManager.mute(reminderId) // Silence while doing mission
            reminderController.cancelSnooze(reminderId)
        }
    }

    fun onAbandonMission() {
        viewModelScope.launch {
            reminderRingManager.unmute(reminderId) // Resume ringing
        }
    }

    fun snoozeReminder() {
        viewModelScope.launch {
            reminderController.snoozeReminder(reminderId)
        }
    }
}
