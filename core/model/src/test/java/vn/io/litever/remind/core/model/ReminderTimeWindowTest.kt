package vn.io.litever.remind.core.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime
import java.time.LocalTime

class ReminderTimeWindowTest {

    @Test
    fun sameDayWindow_insideWindow_activeDay_returnsTrue() {
        val window = ReminderTimeWindow(
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(17, 0),
            activeDays = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
        )
        // 2026-08-17 is Monday
        val dateTime = LocalDateTime.of(2026, 8, 17, 12, 0)
        assertTrue(window.isWithinWindow(dateTime))
    }

    @Test
    fun sameDayWindow_beforeStartTime_returnsFalse() {
        val window = ReminderTimeWindow(
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(17, 0),
            activeDays = listOf(DayOfWeek.MONDAY)
        )
        // Monday 08:59
        val dateTime = LocalDateTime.of(2026, 8, 17, 8, 59)
        assertFalse(window.isWithinWindow(dateTime))
    }

    @Test
    fun sameDayWindow_afterEndTime_returnsFalse() {
        val window = ReminderTimeWindow(
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(17, 0),
            activeDays = listOf(DayOfWeek.MONDAY)
        )
        // Monday 17:01
        val dateTime = LocalDateTime.of(2026, 8, 17, 17, 1)
        assertFalse(window.isWithinWindow(dateTime))
    }

    @Test
    fun sameDayWindow_exactBoundary_returnsTrue() {
        val window = ReminderTimeWindow(
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(17, 0),
            activeDays = listOf(DayOfWeek.MONDAY)
        )
        // Monday 09:00:00
        assertTrue(window.isWithinWindow(LocalDateTime.of(2026, 8, 17, 9, 0, 0)))
        // Monday 17:00:00
        assertTrue(window.isWithinWindow(LocalDateTime.of(2026, 8, 17, 17, 0, 0)))
    }

    @Test
    fun sameDayWindow_insideTime_inactiveDay_returnsFalse() {
        val window = ReminderTimeWindow(
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(17, 0),
            activeDays = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
        )
        // 2026-08-22 is Saturday
        val dateTime = LocalDateTime.of(2026, 8, 22, 12, 0)
        assertFalse(window.isWithinWindow(dateTime))
    }

    @Test
    fun midnightCrossingWindow_eveningPortion_activeDay_returnsTrue() {
        val window = ReminderTimeWindow(
            startTime = LocalTime.of(22, 0),
            endTime = LocalTime.of(4, 0),
            activeDays = listOf(DayOfWeek.MONDAY)
        )
        // Monday 23:00
        val dateTime = LocalDateTime.of(2026, 8, 17, 23, 0)
        assertTrue(window.isWithinWindow(dateTime))
    }

    @Test
    fun midnightCrossingWindow_morningPortion_followingActiveDay_returnsTrue() {
        val window = ReminderTimeWindow(
            startTime = LocalTime.of(22, 0),
            endTime = LocalTime.of(4, 0),
            activeDays = listOf(DayOfWeek.MONDAY)
        )
        // Tuesday 02:00 (active because previous day was Monday)
        val dateTime = LocalDateTime.of(2026, 8, 18, 2, 0)
        assertTrue(window.isWithinWindow(dateTime))
    }

    @Test
    fun midnightCrossingWindow_eveningPortion_inactiveDay_returnsFalse() {
        val window = ReminderTimeWindow(
            startTime = LocalTime.of(22, 0),
            endTime = LocalTime.of(4, 0),
            activeDays = listOf(DayOfWeek.MONDAY)
        )
        // Tuesday 23:00 (Tuesday evening not active)
        val dateTime = LocalDateTime.of(2026, 8, 18, 23, 0)
        assertFalse(window.isWithinWindow(dateTime))
    }

    @Test
    fun midnightCrossingWindow_morningPortion_inactiveFollower_returnsFalse() {
        val window = ReminderTimeWindow(
            startTime = LocalTime.of(22, 0),
            endTime = LocalTime.of(4, 0),
            activeDays = listOf(DayOfWeek.MONDAY)
        )
        // Wednesday 02:00 (Yesterday was Tuesday, not Monday)
        val dateTime = LocalDateTime.of(2026, 8, 19, 2, 0)
        assertFalse(window.isWithinWindow(dateTime))
    }

    @Test
    fun midnightCrossingWindow_daytimeOutsideWindow_returnsFalse() {
        val window = ReminderTimeWindow(
            startTime = LocalTime.of(22, 0),
            endTime = LocalTime.of(4, 0),
            activeDays = listOf(DayOfWeek.MONDAY)
        )
        // Monday 12:00
        val dateTime = LocalDateTime.of(2026, 8, 17, 12, 0)
        assertFalse(window.isWithinWindow(dateTime))
    }

    @Test
    fun emptyActiveDays_activeEveryDay() {
        val window = ReminderTimeWindow(
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(17, 0),
            activeDays = emptyList()
        )
        // Saturday 12:00 -> should be true since activeDays is empty (all days active)
        val dateTime = LocalDateTime.of(2026, 8, 22, 12, 0)
        assertTrue(window.isWithinWindow(dateTime))
    }
}
