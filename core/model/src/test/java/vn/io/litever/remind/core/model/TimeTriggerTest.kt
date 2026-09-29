package vn.io.litever.remind.core.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class TimeTriggerTest {

    @Test
    fun getNextOccurrence_interval_noLastTrigger_returnsFromNow() {
        val trigger = TimeTrigger(
            intervalMinutes = 120,
            repeatMode = RepeatMode.NONE
        )

        val now = LocalDateTime.of(2026, 8, 19, 10, 0)
        val next = trigger.getNextOccurrence(now, lastTriggeredTime = null, isInterval = true)

        // now + 120 minutes
        assertEquals(LocalDateTime.of(2026, 8, 19, 12, 0, 0, 0), next)
    }

    @Test
    fun getNextOccurrence_interval_withLastTrigger_returnsShiftedTime() {
        val trigger = TimeTrigger(
            intervalMinutes = 120,
            repeatMode = RepeatMode.NONE
        )

        val now = LocalDateTime.of(2026, 8, 19, 10, 45) // user checks late
        val lastTriggered = LocalDateTime.of(2026, 8, 19, 10, 30) // user snoozed/completed at 10:30

        val next = trigger.getNextOccurrence(now, lastTriggeredTime = lastTriggered, isInterval = true)

        // lastTriggered + 120 minutes
        assertEquals(LocalDateTime.of(2026, 8, 19, 12, 30, 0, 0), next)
    }

    @Test
    fun getNextOccurrence_interval_multiTimeWindows_insideWindow_keepsCalculatedTime() {
        val window1 = ReminderTimeWindow(startTime = LocalTime.of(8, 0), endTime = LocalTime.of(11, 30))
        val window2 = ReminderTimeWindow(startTime = LocalTime.of(13, 30), endTime = LocalTime.of(17, 30))

        val trigger = TimeTrigger(
            intervalMinutes = 60,
            timeWindows = listOf(window1, window2)
        )

        val now = LocalDateTime.of(2026, 8, 19, 9, 0)
        val next = trigger.getNextOccurrence(now, lastTriggeredTime = now, isInterval = true)

        // 9:00 + 60 min = 10:00 (inside window1 8:00 - 11:30)
        assertEquals(LocalDateTime.of(2026, 8, 19, 10, 0, 0, 0), next)
    }

    @Test
    fun getNextOccurrence_interval_multiTimeWindows_inGap_shiftsToNextWindowStartTime() {
        val window1 = ReminderTimeWindow(startTime = LocalTime.of(8, 0), endTime = LocalTime.of(11, 30))
        val window2 = ReminderTimeWindow(startTime = LocalTime.of(13, 30), endTime = LocalTime.of(17, 30))

        val trigger = TimeTrigger(
            intervalMinutes = 60,
            timeWindows = listOf(window1, window2)
        )

        // Triggered at 11:00. Next 60 min is 12:00, which falls in the lunch gap (11:30 - 13:30)
        val lastTriggered = LocalDateTime.of(2026, 8, 19, 11, 0)
        val now = LocalDateTime.of(2026, 8, 19, 11, 0)
        val next = trigger.getNextOccurrence(now, lastTriggeredTime = lastTriggered, isInterval = true)

        // Should jump to window2 start time: 13:30!
        assertEquals(LocalDateTime.of(2026, 8, 19, 13, 30, 0, 0), next)
    }

    @Test
    fun getNextOccurrence_interval_multiTimeWindows_pastLastWindow_rollsToNextDayFirstWindow() {
        val window1 = ReminderTimeWindow(startTime = LocalTime.of(8, 0), endTime = LocalTime.of(11, 30))
        val window2 = ReminderTimeWindow(startTime = LocalTime.of(13, 30), endTime = LocalTime.of(17, 30))

        val trigger = TimeTrigger(
            intervalMinutes = 60,
            timeWindows = listOf(window1, window2)
        )

        // Triggered at 17:15. Next 60 min is 18:15 (past 17:30)
        val lastTriggered = LocalDateTime.of(2026, 8, 19, 17, 15)
        val now = LocalDateTime.of(2026, 8, 19, 17, 15)
        val next = trigger.getNextOccurrence(now, lastTriggeredTime = lastTriggered, isInterval = true)

        // Should roll over to tomorrow at window1 start: 08:00
        assertEquals(LocalDateTime.of(2026, 8, 20, 8, 0, 0, 0), next)
    }

    @Test
    fun getNextOccurrence_fixedTime_multipleTimes_selectsNextUpcoming() {
        val trigger = TimeTrigger(
            time = listOf(LocalTime.of(8, 0), LocalTime.of(12, 0), LocalTime.of(20, 0)),
            repeatMode = RepeatMode.DAILY
        )
        val now = LocalDateTime.of(2026, 8, 19, 10, 0)

        val next = trigger.getNextOccurrence(now)
        // 10:00 -> next is 12:00
        assertEquals(LocalDateTime.of(2026, 8, 19, 12, 0, 0, 0), next)
    }

    @Test
    fun getNextOccurrence_fixedTime_monthlyMultipleDays_selectsClosestDay() {
        val trigger = TimeTrigger(
            time = listOf(LocalTime.of(8, 0)),
            repeatMode = RepeatMode.MONTHLY,
            monthlyDays = listOf(1, 15, 30)
        )

        // Now is 10th of August at 9:00 -> next is 15th of August at 8:00
        val now1 = LocalDateTime.of(2026, 8, 10, 9, 0)
        val next1 = trigger.getNextOccurrence(now1)
        assertEquals(LocalDateTime.of(2026, 8, 15, 8, 0, 0, 0), next1)

        // Now is 30th of August at 9:00 (past 8:00) -> next is 1st of September at 8:00
        val now2 = LocalDateTime.of(2026, 8, 30, 9, 0)
        val next2 = trigger.getNextOccurrence(now2)
        assertEquals(LocalDateTime.of(2026, 9, 1, 8, 0, 0, 0), next2)
    }

    @Test
    fun getNextOccurrence_fixedTime_yearlyMultipleDates_selectsClosestDate() {
        val trigger = TimeTrigger(
            time = listOf(LocalTime.of(9, 0)),
            repeatMode = RepeatMode.YEARLY,
            yearlyDates = listOf(
                ReminderMonthDay(month = 3, day = 8),
                ReminderMonthDay(month = 10, day = 20)
            )
        )

        // Now is 2026-05-01 -> next is 2026-10-20
        val now1 = LocalDateTime.of(2026, 5, 1, 8, 0)
        val next1 = trigger.getNextOccurrence(now1)
        assertEquals(LocalDateTime.of(2026, 10, 20, 9, 0, 0, 0), next1)

        // Now is 2026-11-01 -> next rolls to next year 2027-03-08
        val now2 = LocalDateTime.of(2026, 11, 1, 8, 0)
        val next2 = trigger.getNextOccurrence(now2)
        assertEquals(LocalDateTime.of(2027, 3, 8, 9, 0, 0, 0), next2)
    }
}
