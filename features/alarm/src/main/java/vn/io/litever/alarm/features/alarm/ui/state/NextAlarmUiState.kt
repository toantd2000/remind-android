package vn.io.litever.alarm.features.alarm.ui.state

import vn.io.litever.alarm.core.model.Alarm
import vn.io.litever.alarm.core.model.DayOfWeek
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
    val nextOccurrences = enabledAlarms.mapNotNull { alarm ->
        calculateNextOccurrence(alarm, now)
    }
    
    val earliest = nextOccurrences.minOrNull() ?: return NextAlarmUiState.AllOff
    val duration = Duration.between(now, earliest)
    
    val totalMinutes = duration.toMinutes()
    val days = duration.toDays()
    val hours = duration.toHours() % 24
    val minutes = totalMinutes % 60
    
    return NextAlarmUiState.Remaining(days, hours, minutes)
}

private fun calculateNextOccurrence(alarm: Alarm, now: LocalDateTime): LocalDateTime? {
    val alarmTime = alarm.time
    
    if (alarm.repeatDays.isEmpty()) {
        val todayOccurrence = now.with(alarmTime).withSecond(0).withNano(0)
        return if (todayOccurrence.isAfter(now)) {
            todayOccurrence
        } else {
            todayOccurrence.plusDays(1)
        }
    } else {
        val todayDayValue = now.dayOfWeek.value
        val repeatDayValues = alarm.repeatDays.map { it.toJavaDayValue() }.sorted()
        
        if (repeatDayValues.contains(todayDayValue)) {
            val todayOccurrence = now.with(alarmTime).withSecond(0).withNano(0)
            if (todayOccurrence.isAfter(now)) return todayOccurrence
        }
        
        val nextDayInWeek = repeatDayValues.firstOrNull { it > todayDayValue }
        if (nextDayInWeek != null) {
            val daysDiff = (nextDayInWeek - todayDayValue).toLong()
            return now.with(alarmTime).plusDays(daysDiff).withSecond(0).withNano(0)
        }
        
        val firstDayNextWeek = repeatDayValues.first()
        val daysDiff = (7 - todayDayValue + firstDayNextWeek).toLong()
        return now.with(alarmTime).plusDays(daysDiff).withSecond(0).withNano(0)
    }
}

private fun DayOfWeek.toJavaDayValue(): Int = when (this) {
    DayOfWeek.MONDAY -> 1
    DayOfWeek.TUESDAY -> 2
    DayOfWeek.WEDNESDAY -> 3
    DayOfWeek.THURSDAY -> 4
    DayOfWeek.FRIDAY -> 5
    DayOfWeek.SATURDAY -> 6
    DayOfWeek.SUNDAY -> 7
}
