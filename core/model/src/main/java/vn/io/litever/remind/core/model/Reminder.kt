package vn.io.litever.remind.core.model

import java.time.LocalDateTime
import java.time.LocalTime

data class Reminder(
    val id: Long = 0,
    val time: LocalTime,
    val label: String = "",
    val isEnabled: Boolean = true,
    val repeatDays: List<DayOfWeek> = emptyList(),
    val date: java.time.LocalDate? = null,
    val vibrationEnabled: Boolean = true,
    val ringtoneUri: String? = null,
    val volume: Int = 100,
    val snoozeEnabled: Boolean = true,
    val snoozeInterval: Int = 5,
    val snoozeRepeatCount: Int = 3,
    val autoSilenceMinutes: Int = 3,
    val currentSnoozeCount: Int = 0,
    val snoozeNextTriggerTime: Long? = null,
    val isMissed: Boolean = false,
    val message: String = "",
    val skippedAt: LocalDateTime? = null
) {
    fun getNextOccurrence(now: LocalDateTime = LocalDateTime.now()): LocalDateTime {
        val rawNext = getActualNextOccurrence(now)
        return if (skippedAt != null && rawNext.isEqual(skippedAt)) {
            getActualNextOccurrence(rawNext.plusMinutes(1))
        } else {
            rawNext
        }
    }

    fun getActualNextOccurrence(now: LocalDateTime = LocalDateTime.now()): LocalDateTime {
        if (date != null) {
            return date.atTime(time).withSecond(0).withNano(0)
        }
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
