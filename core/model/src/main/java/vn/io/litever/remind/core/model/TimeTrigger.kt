package vn.io.litever.remind.core.model

import java.io.Serializable
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class ReminderMonthDay(
    val month: Int,
    val day: Int
) : Serializable

data class TimeTrigger(
    val time: LocalTime,
    val date: LocalDate? = null,
    val repeatDays: List<DayOfWeek> = emptyList(),
    val monthlyDays: List<Int> = emptyList(),
    val yearlyDates: List<ReminderMonthDay> = emptyList()
) : Serializable {

    val isRecurring: Boolean
        get() = repeatDays.isNotEmpty() || monthlyDays.isNotEmpty() || yearlyDates.isNotEmpty()

    fun getNextOccurrence(now: LocalDateTime = LocalDateTime.now()): LocalDateTime {
        if (yearlyDates.isNotEmpty()) {
            val candidates = yearlyDates.map { ym ->
                val month = ym.month.coerceIn(1, 12)
                val thisYear = now.year
                val maxDaysThisYear = java.time.Month.of(month).length(java.time.Year.isLeap(thisYear.toLong()))
                val dayThisYear = ym.day.coerceIn(1, maxDaysThisYear)
                val candidateThisYear = LocalDate.of(thisYear, month, dayThisYear).atTime(time).withSecond(0).withNano(0)

                if (candidateThisYear.isAfter(now)) {
                    candidateThisYear
                } else {
                    val nextYear = thisYear + 1
                    val maxDaysNextYear = java.time.Month.of(month).length(java.time.Year.isLeap(nextYear.toLong()))
                    val dayNextYear = ym.day.coerceIn(1, maxDaysNextYear)
                    LocalDate.of(nextYear, month, dayNextYear).atTime(time).withSecond(0).withNano(0)
                }
            }
            return candidates.minOrNull() ?: now.with(time).plusDays(1).withSecond(0).withNano(0)
        }

        if (monthlyDays.isNotEmpty()) {
            val candidates = monthlyDays.map { mDay ->
                val today = now.toLocalDate()
                val maxDaysThisMonth = today.lengthOfMonth()
                val dayThisMonth = mDay.coerceIn(1, maxDaysThisMonth)
                val candidateThisMonth = today.withDayOfMonth(dayThisMonth).atTime(time).withSecond(0).withNano(0)

                if (candidateThisMonth.isAfter(now)) {
                    candidateThisMonth
                } else {
                    val nextMonth = today.plusMonths(1)
                    val maxDaysNextMonth = nextMonth.lengthOfMonth()
                    val dayNextMonth = mDay.coerceIn(1, maxDaysNextMonth)
                    nextMonth.withDayOfMonth(dayNextMonth).atTime(time).withSecond(0).withNano(0)
                }
            }
            return candidates.minOrNull() ?: now.with(time).plusDays(1).withSecond(0).withNano(0)
        }

        if (date != null) {
            return date.atTime(time).withSecond(0).withNano(0)
        }

        if (repeatDays.isEmpty()) {
            val rawOccurrence = now.with(time)
            return if (rawOccurrence.isAfter(now)) {
                rawOccurrence.withSecond(0).withNano(0)
            } else {
                rawOccurrence.plusDays(1).withSecond(0).withNano(0)
            }
        } else {
            val todayDayValue = now.dayOfWeek.value
            val repeatDayValues = repeatDays.map { it.toJavaDayValue() }.sorted()

            if (repeatDayValues.contains(todayDayValue)) {
                val rawOccurrence = now.with(time)
                if (rawOccurrence.isAfter(now)) return rawOccurrence.withSecond(0).withNano(0)
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
