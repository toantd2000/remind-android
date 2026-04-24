package vn.io.litever.remind.features.alarms.ui.state

import vn.io.litever.remind.core.model.Alarm
import java.time.Duration
import java.time.LocalDateTime

sealed interface NextAlarmUiState {
    object AllOff : NextAlarmUiState
    data class Remaining(
        val days: Long,
        val hours: Long,
        val minutes: Long
    ) : NextAlarmUiState
}

fun calculateNextAlarm(enabledAlarms: List<Alarm>): NextAlarmUiState {
    if (enabledAlarms.isEmpty()) return NextAlarmUiState.AllOff
    
    val now = LocalDateTime.now()
    val nextOccurrences = enabledAlarms.map { alarm ->
        alarm.getNextOccurrence(now)
    }
    
    val earliest = nextOccurrences.minOrNull() ?: return NextAlarmUiState.AllOff
    val duration = Duration.between(now, earliest)
    
    val totalMinutes = duration.toMinutes()
    val days = duration.toDays()
    val hours = duration.toHours() % 24
    val minutes = totalMinutes % 60
    
    return NextAlarmUiState.Remaining(days, hours, minutes)
}










