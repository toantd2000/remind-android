package vn.io.litever.alarm.features.alarm.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import vn.io.litever.alarm.core.domain.scheduler.AlarmController
import javax.inject.Inject

@HiltViewModel
class AlarmRingingViewModel @Inject constructor(
    private val alarmController: AlarmController
) : ViewModel() {

    fun dismissAlarm() {
        alarmController.dismissAlarm()
    }

    fun snoozeAlarm() {
        alarmController.snoozeAlarm()
    }
}
