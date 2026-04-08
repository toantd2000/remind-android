package vn.io.litever.alarm.core.model

import java.time.LocalDateTime
import java.time.LocalTime

data class Alarm(
    val id: Long = 0,
    val time: LocalTime,
    val label: String = "",
    val isEnabled: Boolean = true,
    val repeatDays: List<DayOfWeek> = emptyList(),
    val vibrationEnabled: Boolean = true,
    val ringtoneUri: String? = null
) {
    fun getNextOccurrence(now: LocalDateTime = LocalDateTime.now()): LocalDateTime {
        if (repeatDays.isEmpty()) {
            val todayOccurrence = now.with(time).withSecond(0).withNano(0)
            return if (todayOccurrence.isAfter(now)) {
                todayOccurrence
            } else {
                todayOccurrence.plusDays(1)
            }
        } else {
            val todayDayValue = now.dayOfWeek.value
            val repeatDayValues = repeatDays.map { it.toJavaDayValue() }.sorted()
            
            if (repeatDayValues.contains(todayDayValue)) {
                val todayOccurrence = now.with(time).withSecond(0).withNano(0)
                if (todayOccurrence.isAfter(now)) return todayOccurrence
            }
            
            val nextDayInWeek = repeatDayValues.firstOrNull { it > todayDayValue }
            if (nextDayInWeek != null) {
                val daysDiff = (nextDayInWeek - todayDayValue).toLong()
                return now.with(time).plusDays(daysDiff).withSecond(0).withNano(0)
            }
            
            val firstDayNextWeek = repeatDayValues.first()
            val daysDiff = (7 - todayDayValue + firstDayNextWeek).toLong()
            return now.with(time).plusDays(daysDiff).withSecond(0).withNano(0)
        }
    }
}

enum class DayOfWeek {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;
    
    fun toJavaDayValue(): Int = when (this) {
        MONDAY -> 1
        TUESDAY -> 2
        WEDNESDAY -> 3
        THURSDAY -> 4
        FRIDAY -> 5
        SATURDAY -> 6
        SUNDAY -> 7
    }
}
