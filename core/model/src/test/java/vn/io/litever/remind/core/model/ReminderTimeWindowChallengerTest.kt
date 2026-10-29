package vn.io.litever.remind.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Adversarial Challenger Stress Test for ReminderTimeWindow.
 * Exhaustively stress-tests single-day, midnight-crossing, edge boundaries,
 * month/year boundaries, and all 7-day combinations.
 */
class ReminderTimeWindowChallengerTest {

    @Test
    fun exhaustive2WeekHourlySimulation_sameDayWindow_matchesOracle() {
        val window = ReminderTimeWindow(
            startTime = LocalTime.of(8, 30),
            endTime = LocalTime.of(17, 30),
            activeDays = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        )

        val startDateTime = LocalDateTime.of(2026, 8, 17, 0, 0) // Monday
        for (minuteOffset in 0 until (14 * 24 * 60) step 15) {
            val dt = startDateTime.plusMinutes(minuteOffset.toLong())
            val actual = window.isWithinWindow(dt)

            val day = DayOfWeek.valueOf(dt.dayOfWeek.name)
            val time = dt.toLocalTime()
            val expectedDayActive = day in listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
            val expectedTimeActive = !time.isBefore(LocalTime.of(8, 30)) && !time.isAfter(LocalTime.of(17, 30))
            val expected = expectedDayActive && expectedTimeActive

            assertEquals("Failed at $dt (Day: $day, Time: $time)", expected, actual)
        }
    }

    @Test
    fun exhaustive2WeekHourlySimulation_midnightCrossingWindow_matchesOracle() {
        // Window: 21:00 -> 05:00 next day
        // Active days: Friday and Sunday
        val window = ReminderTimeWindow(
            startTime = LocalTime.of(21, 0),
            endTime = LocalTime.of(5, 0),
            activeDays = listOf(DayOfWeek.FRIDAY, DayOfWeek.SUNDAY)
        )

        val startDateTime = LocalDateTime.of(2026, 8, 17, 0, 0) // Monday
        for (minuteOffset in 0 until (14 * 24 * 60) step 15) {
            val dt = startDateTime.plusMinutes(minuteOffset.toLong())
            val actual = window.isWithinWindow(dt)

            val currentDay = DayOfWeek.valueOf(dt.dayOfWeek.name)
            val prevDay = DayOfWeek.valueOf(dt.minusDays(1).dayOfWeek.name)
            val time = dt.toLocalTime()

            val isEvening = !time.isBefore(LocalTime.of(21, 0)) && (currentDay in listOf(DayOfWeek.FRIDAY, DayOfWeek.SUNDAY))
            val isMorning = !time.isAfter(LocalTime.of(5, 0)) && (prevDay in listOf(DayOfWeek.FRIDAY, DayOfWeek.SUNDAY))
            val expected = isEvening || isMorning

            assertEquals("Failed at $dt (Day: $currentDay, Time: $time)", expected, actual)
        }
    }

    @Test
    fun yearEndAndLeapYearBoundaries_midnightCrossing() {
        val window = ReminderTimeWindow(
            startTime = LocalTime.of(23, 0),
            endTime = LocalTime.of(2, 0),
            activeDays = listOf(DayOfWeek.THURSDAY)
        )

        // 2026-12-31 is Thursday
        // 2026-12-31 23:30 -> Active (Thursday evening)
        assertTrue(window.isWithinWindow(LocalDateTime.of(2026, 12, 31, 23, 30)))
        // 2027-01-01 01:30 -> Active (Friday morning following Thursday)
        assertTrue(window.isWithinWindow(LocalDateTime.of(2027, 1, 1, 1, 30)))
        // 2027-01-01 02:00 -> Active (exact boundary)
        assertTrue(window.isWithinWindow(LocalDateTime.of(2027, 1, 1, 2, 0)))
        // 2027-01-01 02:01 -> Inactive
        assertFalse(window.isWithinWindow(LocalDateTime.of(2027, 1, 1, 2, 1)))
        // 2027-01-01 23:30 -> Inactive (Friday evening not active)
        assertFalse(window.isWithinWindow(LocalDateTime.of(2027, 1, 1, 23, 30)))
    }

    @Test
    fun wrapAroundSundayToMonday_midnightCrossing() {
        val window = ReminderTimeWindow(
            startTime = LocalTime.of(22, 0),
            endTime = LocalTime.of(3, 0),
            activeDays = listOf(DayOfWeek.SUNDAY)
        )

        // 2026-08-16 is Sunday
        // Sunday 22:00
        assertTrue(window.isWithinWindow(LocalDateTime.of(2026, 8, 16, 22, 0)))
        // Sunday 23:59
        assertTrue(window.isWithinWindow(LocalDateTime.of(2026, 8, 16, 23, 59)))
        // 2026-08-17 is Monday 00:00 (Midnight crossing)
        assertTrue(window.isWithinWindow(LocalDateTime.of(2026, 8, 17, 0, 0)))
        // Monday 02:59
        assertTrue(window.isWithinWindow(LocalDateTime.of(2026, 8, 17, 2, 59)))
        // Monday 03:00
        assertTrue(window.isWithinWindow(LocalDateTime.of(2026, 8, 17, 3, 0)))
        // Monday 03:01
        assertFalse(window.isWithinWindow(LocalDateTime.of(2026, 8, 17, 3, 1)))
        // Monday 22:30 (Monday evening not in activeDays)
        assertFalse(window.isWithinWindow(LocalDateTime.of(2026, 8, 17, 22, 30)))
    }

    @Test
    fun startEqualsEnd_evaluatesExactInstant() {
        val window = ReminderTimeWindow(
            startTime = LocalTime.of(12, 0),
            endTime = LocalTime.of(12, 0),
            activeDays = listOf(DayOfWeek.MONDAY)
        )

        // Monday 12:00:00 -> true
        assertTrue(window.isWithinWindow(LocalDateTime.of(2026, 8, 17, 12, 0, 0)))
        // Monday 11:59:59 -> false
        assertFalse(window.isWithinWindow(LocalDateTime.of(2026, 8, 17, 11, 59, 59)))
        // Monday 12:00:01 -> false
        assertFalse(window.isWithinWindow(LocalDateTime.of(2026, 8, 17, 12, 0, 1)))
    }

    @Test
    fun allDaysActive_emptyListVsFullList_areEquivalent() {
        val windowEmpty = ReminderTimeWindow(
            startTime = LocalTime.of(20, 0),
            endTime = LocalTime.of(6, 0),
            activeDays = emptyList()
        )
        val windowFull = ReminderTimeWindow(
            startTime = LocalTime.of(20, 0),
            endTime = LocalTime.of(6, 0),
            activeDays = DayOfWeek.values().toList()
        )

        val startDateTime = LocalDateTime.of(2026, 8, 17, 0, 0)
        for (hour in 0 until (7 * 24)) {
            val dt = startDateTime.plusHours(hour.toLong())
            assertEquals(
                "Discrepancy at $dt",
                windowEmpty.isWithinWindow(dt),
                windowFull.isWithinWindow(dt)
            )
        }
    }
}
