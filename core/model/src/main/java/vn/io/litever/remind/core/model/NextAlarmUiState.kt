package vn.io.litever.remind.core.model

import java.time.Duration
import java.time.LocalDateTime

sealed interface NextAlarmUiState {
    object AllOff : NextAlarmUiState
    data class Remaining(
        val days: Long,
        val hours: Long,
        val minutes: Long,
        val alarm: Alarm
    ) : NextAlarmUiState
}

fun calculateNextAlarm(enabledAlarms: List<Alarm>): NextAlarmUiState {
    if (enabledAlarms.isEmpty()) return NextAlarmUiState.AllOff

    val now = LocalDateTime.now()
    val earliestAlarm = enabledAlarms.minByOrNull { alarm ->
        alarm.getNextOccurrence(now)
    } ?: return NextAlarmUiState.AllOff

    val earliest = earliestAlarm.getNextOccurrence(now)
    val duration = Duration.between(now, earliest)

    val totalMinutes = duration.toMinutes()
    val days = duration.toDays()
    val hours = duration.toHours() % 24
    val minutes = totalMinutes % 60

    return NextAlarmUiState.Remaining(days, hours, minutes, earliestAlarm)
}
