package vn.io.litever.remind.core.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class TimeTriggerTest {

    @Test
    fun getNextOccurrence_withSpecificDate_returnsExactDateTime() {
        val trigger = TimeTrigger(
            time = LocalTime.of(8, 30),
            date = LocalDate.of(2026, 8, 25)
        )

        val next = trigger.getNextOccurrence(LocalDateTime.of(2026, 8, 19, 10, 0))
        assertEquals(LocalDateTime.of(2026, 8, 25, 8, 30, 0, 0), next)
    }

    @Test
    fun getNextOccurrence_todayUpcoming_returnsToday() {
        val trigger = TimeTrigger(time = LocalTime.of(9, 0))
        val now = LocalDateTime.of(2026, 8, 19, 8, 0)

        val next = trigger.getNextOccurrence(now)
        assertEquals(LocalDateTime.of(2026, 8, 19, 9, 0, 0, 0), next)
    }

    @Test
    fun getNextOccurrence_todayPassed_returnsTomorrow() {
        val trigger = TimeTrigger(time = LocalTime.of(9, 0))
        val now = LocalDateTime.of(2026, 8, 19, 10, 0)

        val next = trigger.getNextOccurrence(now)
        assertEquals(LocalDateTime.of(2026, 8, 20, 9, 0, 0, 0), next)
    }

    @Test
    fun getNextOccurrence_repeatDays_nextDayInWeek() {
        // Monday = 2026-08-17
        val now = LocalDateTime.of(2026, 8, 17, 10, 0)
        val trigger = TimeTrigger(
            time = LocalTime.of(9, 0),
            repeatDays = listOf(DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        )

        val next = trigger.getNextOccurrence(now)
        // Wednesday = 2026-08-19
        assertEquals(LocalDateTime.of(2026, 8, 19, 9, 0, 0, 0), next)
    }

    @Test
    fun getNextOccurrence_repeatDays_wrapToNextWeek() {
        // Friday = 2026-08-21
        val now = LocalDateTime.of(2026, 8, 21, 10, 0)
        val trigger = TimeTrigger(
            time = LocalTime.of(9, 0),
            repeatDays = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY)
        )

        val next = trigger.getNextOccurrence(now)
        // Next Monday = 2026-08-24
        assertEquals(LocalDateTime.of(2026, 8, 24, 9, 0, 0, 0), next)
    }
}
