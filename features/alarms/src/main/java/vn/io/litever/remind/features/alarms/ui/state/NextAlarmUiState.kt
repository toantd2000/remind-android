package vn.io.litever.remind.features.alarms.ui.state

import vn.io.litever.remind.core.model.Alarm
import vn.io.litever.remind.core.model.NextAlarmUiState as CoreNextAlarmUiState

sealed interface NextAlarmUiState {
    object AllOff : NextAlarmUiState
    data class Remaining(
        val days: Long,
        val hours: Long,
        val minutes: Long,
        val alarm: Alarm? = null
    ) : NextAlarmUiState

    fun toCore(): CoreNextAlarmUiState = when (this) {
        AllOff -> CoreNextAlarmUiState.AllOff
        is Remaining -> {
            if (alarm != null) {
                CoreNextAlarmUiState.Remaining(days, hours, minutes, alarm)
            } else {
                CoreNextAlarmUiState.Remaining(days, hours, minutes, Alarm(time = java.time.LocalTime.MIDNIGHT))
            }
        }
    }

    companion object {
        fun fromCore(core: CoreNextAlarmUiState): NextAlarmUiState = when (core) {
            CoreNextAlarmUiState.AllOff -> AllOff
            is CoreNextAlarmUiState.Remaining -> Remaining(core.days, core.hours, core.minutes, core.alarm)
        }
    }
}

fun calculateNextAlarm(enabledAlarms: List<Alarm>): NextAlarmUiState {
    return NextAlarmUiState.fromCore(vn.io.litever.remind.core.model.calculateNextAlarm(enabledAlarms))
}
