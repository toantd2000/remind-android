package vn.io.litever.remind.core.model.reminder

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import vn.io.litever.remind.core.model.DayOfWeek
import vn.io.litever.remind.core.model.ReminderTimeWindow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Comprehensive Unit Tests for ReminderTimeWindow Evaluation Logic (Tiers 1 & 2).
 * Covers standard daytime windows, overnight midnight crossing, active days masking, and boundary conditions.
 */
class ReminderTimeWindowTest {

    @Test
    fun standardWindow_insideInterval_returnsTrue() {
        val window = ReminderTimeWindow(
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(17, 0)
        )
        val testTime = LocalDateTime.of(2026, 8, 19, 14, 30) // Wednesday 14:30

        assertTrue(window.isWithinWindow(testTime))
    }

    @Test
    fun standardWindow_beforeStartTime_returnsFalse() {
        val window = ReminderTimeWindow(
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(17, 0)
        )
        val testTime = LocalDateTime.of(2026, 8, 19, 8, 59, 59)

        assertFalse(window.isWithinWindow(testTime))
    }

    @Test
    fun standardWindow_afterEndTime_returnsFalse() {
        val window = ReminderTimeWindow(
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(17, 0)
        )
        val testTime = LocalDateTime.of(2026, 8, 19, 17, 0, 1)

        assertFalse(window.isWithinWindow(testTime))
    }

    @Test
    fun standardWindow_exactStartAndEndBoundaries_areInclusive() {
        val window = ReminderTimeWindow(
            startTime = LocalTime.of(9, 0, 0),
            endTime = LocalTime.of(17, 0, 0)
        )
        val atStart = LocalDateTime.of(2026, 8, 19, 9, 0, 0)
        val atEnd = LocalDateTime.of(2026, 8, 19, 17, 0, 0)

        assertTrue(window.isWithinWindow(atStart))
        assertTrue(window.isWithinWindow(atEnd))
    }

    @Test
    fun midnightCrossingWindow_eveningInterval_returnsTrue() {
        val window = ReminderTimeWindow(
            startTime = LocalTime.of(22, 0),
            endTime = LocalTime.of(6, 0)
        )
        val testTime = LocalDateTime.of(2026, 8, 19, 23, 15) // Wednesday 23:15

        assertTrue(window.isWithinWindow(testTime))
    }

    @Test
    fun midnightCrossingWindow_morningInterval_returnsTrue() {
        val window = ReminderTimeWindow(
            startTime = LocalTime.of(22, 0),
            endTime = LocalTime.of(6, 0)
        )
        val testTime = LocalDateTime.of(2026, 8, 20, 2, 45) // Thursday 02:45

        assertTrue(window.isWithinWindow(testTime))
    }

    @Test
    fun midnightCrossingWindow_outsideDaytime_returnsFalse() {
        val window = ReminderTimeWindow(
            startTime = LocalTime.of(22, 0),
            endTime = LocalTime.of(6, 0)
        )
        val testTime = LocalDateTime.of(2026, 8, 19, 14, 0) // Wednesday 14:00

        assertFalse(window.isWithinWindow(testTime))
    }

    @Test
    fun midnightCrossingWindow_exactBoundaries_areInclusive() {
        val window = ReminderTimeWindow(
            startTime = LocalTime.of(22, 0, 0),
            endTime = LocalTime.of(6, 0, 0)
        )
        val atEveningStart = LocalDateTime.of(2026, 8, 19, 22, 0, 0)
        val atMorningEnd = LocalDateTime.of(2026, 8, 20, 6, 0, 0)

        assertTrue(window.isWithinWindow(atEveningStart))
        assertTrue(window.isWithinWindow(atMorningEnd))
    }

    @Test
    fun activeDaysFilter_matchingDay_returnsTrue() {
        val window = ReminderTimeWindow(
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(17, 0),
            activeDays = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        )
        // 2026-08-19 is Wednesday
        val wednesdayTime = LocalDateTime.of(2026, 8, 19, 12, 0)
        assertTrue(window.isWithinWindow(wednesdayTime))
    }

    @Test
    fun activeDaysFilter_nonMatchingDay_returnsFalse() {
        val window = ReminderTimeWindow(
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(17, 0),
            activeDays = listOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY)
        )
        // 2026-08-19 is Wednesday
        val wednesdayTime = LocalDateTime.of(2026, 8, 19, 12, 0)
        assertFalse(window.isWithinWindow(wednesdayTime))
    }

    @Test
    fun midnightCrossing_activeDays_fridayNightToSaturdayMorning() {
        // Active ONLY on Friday night (from 22:00 Friday to 04:00 Saturday)
        val window = ReminderTimeWindow(
            startTime = LocalTime.of(22, 0),
            endTime = LocalTime.of(4, 0),
            activeDays = listOf(DayOfWeek.FRIDAY)
        )

        // 2026-08-21 is Friday
        val fridayEvening = LocalDateTime.of(2026, 8, 21, 23, 0)
        // 2026-08-22 is Saturday 02:00 (spillover of Friday's window)
        val saturdayMorning = LocalDateTime.of(2026, 8, 22, 2, 0)
        // 2026-08-22 is Saturday 23:00 (Saturday night is NOT active)
        val saturdayEvening = LocalDateTime.of(2026, 8, 22, 23, 0)
        // 2026-08-23 is Sunday 02:00 (Saturday night spillover is NOT active)
        val sundayMorning = LocalDateTime.of(2026, 8, 23, 2, 0)

        assertTrue("Friday evening must be active", window.isWithinWindow(fridayEvening))
        assertTrue("Saturday morning spillover from Friday must be active", window.isWithinWindow(saturdayMorning))
        assertFalse("Saturday evening must NOT be active", window.isWithinWindow(saturdayEvening))
        assertFalse("Sunday morning must NOT be active", window.isWithinWindow(sundayMorning))
    }

    @Test
    fun boundary_emptyActiveDays_activeEveryDay() {
        val window = ReminderTimeWindow(
            startTime = LocalTime.of(10, 0),
            endTime = LocalTime.of(12, 0),
            activeDays = emptyList()
        )

        // Test every day of week (Monday through Sunday)
        for (dayOffset in 0..6) {
            val dateTime = LocalDateTime.of(2026, 8, 17 + dayOffset, 11, 0)
            assertTrue("Day offset $dayOffset should be active", window.isWithinWindow(dateTime))
        }
    }

    @Test
    fun boundary_instantaneousWindow_startTimeEqualsEndTime() {
        val window = ReminderTimeWindow(
            startTime = LocalTime.of(12, 0, 0),
            endTime = LocalTime.of(12, 0, 0)
        )
        val exactTime = LocalDateTime.of(2026, 8, 19, 12, 0, 0)
        val oneSecondBefore = LocalDateTime.of(2026, 8, 19, 11, 59, 59)
        val oneSecondAfter = LocalDateTime.of(2026, 8, 19, 12, 0, 1)

        assertTrue(window.isWithinWindow(exactTime))
        assertFalse(window.isWithinWindow(oneSecondBefore))
        assertFalse(window.isWithinWindow(oneSecondAfter))
    }

    @Test
    fun boundary_midnightToMidnight_fullDayWindow() {
        val window = ReminderTimeWindow(
            startTime = LocalTime.of(0, 0, 0),
            endTime = LocalTime.of(23, 59, 59)
        )
        val morning = LocalDateTime.of(2026, 8, 19, 0, 0, 0)
        val noon = LocalDateTime.of(2026, 8, 19, 12, 30, 0)
        val night = LocalDateTime.of(2026, 8, 19, 23, 59, 59)

        assertTrue(window.isWithinWindow(morning))
        assertTrue(window.isWithinWindow(noon))
        assertTrue(window.isWithinWindow(night))
    }

    @Test
    fun boundary_tightMidnightCrossing_2359to0001() {
        val window = ReminderTimeWindow(
            startTime = LocalTime.of(23, 59, 0),
            endTime = LocalTime.of(0, 1, 0)
        )
        val justBeforeMidnight = LocalDateTime.of(2026, 8, 19, 23, 59, 30)
        val exactMidnight = LocalDateTime.of(2026, 8, 20, 0, 0, 0)
        val justAfterMidnight = LocalDateTime.of(2026, 8, 20, 0, 0, 30)
        val twoMinutesAfter = LocalDateTime.of(2026, 8, 20, 0, 2, 0)

        assertTrue(window.isWithinWindow(justBeforeMidnight))
        assertTrue(window.isWithinWindow(exactMidnight))
        assertTrue(window.isWithinWindow(justAfterMidnight))
        assertFalse(window.isWithinWindow(twoMinutesAfter))
    }
}
