package vn.io.litever.remind.core.model

import java.io.Serializable
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class TimeTrigger(
    val time: LocalTime,
    val date: LocalDate? = null,
    val repeatDays: List<DayOfWeek> = emptyList()
) : Serializable {

    fun getNextOccurrence(now: LocalDateTime = LocalDateTime.now()): LocalDateTime {
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
