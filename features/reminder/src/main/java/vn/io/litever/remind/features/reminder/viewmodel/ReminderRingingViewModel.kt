package vn.io.litever.remind.features.reminder.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import vn.io.litever.remind.core.domain.scheduler.ReminderController
import javax.inject.Inject

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import vn.io.litever.remind.core.datastore.ReminderPreferencesDataSource

@HiltViewModel
class ReminderRingingViewModel @Inject constructor(
    private val reminderController: ReminderController,
    private val preferencesDataSource: ReminderPreferencesDataSource
) : ViewModel() {

    val is24HourFormat: StateFlow<Boolean> = preferencesDataSource.is24HourFormat
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    fun dismissReminder() {
        reminderController.dismissReminder()
    }

    fun snoozeReminder() {
        reminderController.snoozeReminder()
    }
}
