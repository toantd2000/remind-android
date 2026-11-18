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

    @Test
    fun getNextOccurrence_interval_oneTimeExactDate_futureDate_startsOnExactDate() {
        val window = ReminderTimeWindow(startTime = LocalTime.of(8, 0), endTime = LocalTime.of(17, 0))
        val trigger = TimeTrigger(
            intervalMinutes = 60,
            repeatMode = RepeatMode.NONE,
            exactDate = LocalDate.of(2026, 8, 25),
            timeWindows = listOf(window)
        )

        // Today is 2026-08-19. Exact date is 2026-08-25.
        val now = LocalDateTime.of(2026, 8, 19, 10, 0)
        val next = trigger.getNextOccurrence(now, lastTriggeredTime = null, isInterval = true)

        assertEquals(LocalDateTime.of(2026, 8, 25, 8, 0, 0, 0), next)
    }

    @Test
    fun getNextOccurrence_interval_oneTimeExactDate_afterLastWindow_returnsNull() {
        val window = ReminderTimeWindow(startTime = LocalTime.of(8, 0), endTime = LocalTime.of(17, 0))
        val trigger = TimeTrigger(
            intervalMinutes = 60,
            repeatMode = RepeatMode.NONE,
            exactDate = LocalDate.of(2026, 8, 25),
            timeWindows = listOf(window)
        )

        // Today is 2026-08-25 16:30, lastTriggered is 16:30 -> next calculated is 17:30 (past window end 17:00).
        // Since RepeatMode.NONE, it should return null because no next valid date exists.
        val now = LocalDateTime.of(2026, 8, 25, 16, 30)
        val next = trigger.getNextOccurrence(now, lastTriggeredTime = now, isInterval = true)

        assertEquals(null, next)
    }

    @Test
    fun getNextOccurrence_interval_weekly_skipsNonSelectedDays() {
        val window = ReminderTimeWindow(startTime = LocalTime.of(9, 0), endTime = LocalTime.of(17, 0))
        val trigger = TimeTrigger(
            intervalMinutes = 60,
            repeatMode = RepeatMode.WEEKLY,
            repeatValues = listOf(1, 3, 5), // Mon (1), Wed (3), Fri (5)
            timeWindows = listOf(window)
        )

        // Wednesday 2026-08-19 at 16:30. Next interval is 17:30 (past 17:00).
        // Thursday 2026-08-20 is not selected (Thu = 4).
        // Friday 2026-08-21 is selected (Fri = 5).
        val now = LocalDateTime.of(2026, 8, 19, 16, 30)
        val next = trigger.getNextOccurrence(now, lastTriggeredTime = now, isInterval = true)

        assertEquals(LocalDateTime.of(2026, 8, 21, 9, 0, 0, 0), next)
    }

    @Test
    fun getNextOccurrence_interval_monthly_skipsNonSelectedDays() {
        val window = ReminderTimeWindow(startTime = LocalTime.of(8, 0), endTime = LocalTime.of(12, 0))
        val trigger = TimeTrigger(
            intervalMinutes = 60,
            repeatMode = RepeatMode.MONTHLY,
            monthlyDays = listOf(1, 15, 30),
            timeWindows = listOf(window)
        )

        // 2026-08-15 at 11:30. Next interval is 12:30 (past 12:00).
        // Next valid day of month is 30th -> 2026-08-30 08:00
        val now = LocalDateTime.of(2026, 8, 15, 11, 30)
        val next = trigger.getNextOccurrence(now, lastTriggeredTime = now, isInterval = true)

        assertEquals(LocalDateTime.of(2026, 8, 30, 8, 0, 0, 0), next)
    }

    @Test
    fun getNextOccurrence_interval_yearly_skipsNonSelectedDates() {
        val window = ReminderTimeWindow(startTime = LocalTime.of(9, 0), endTime = LocalTime.of(12, 0))
        val trigger = TimeTrigger(
            intervalMinutes = 60,
            repeatMode = RepeatMode.YEARLY,
            yearlyDates = listOf(
                ReminderMonthDay(month = 3, day = 8),
                ReminderMonthDay(month = 10, day = 20)
            ),
            timeWindows = listOf(window)
        )

        // 2026-03-08 at 11:30. Next interval 12:30 is past window. Next date is 2026-10-20 09:00.
        val now = LocalDateTime.of(2026, 3, 8, 11, 30)
        val next = trigger.getNextOccurrence(now, lastTriggeredTime = now, isInterval = true)

        assertEquals(LocalDateTime.of(2026, 10, 20, 9, 0, 0, 0), next)
    }

    @Test
    fun getNextOccurrence_fixedTime_multipleExactDates_selectsClosestFutureOccurrenceAndReturnsNullAfterLast() {
        val trigger = TimeTrigger(
            time = listOf(LocalTime.of(8, 0), LocalTime.of(18, 0)),
            exactDates = listOf(
                LocalDate.of(2026, 8, 20),
                LocalDate.of(2026, 8, 25)
            ),
            repeatMode = RepeatMode.NONE
        )

        // Before first date -> 2026-08-20 08:00
        assertEquals(
            LocalDateTime.of(2026, 8, 20, 8, 0),
            trigger.getNextOccurrence(LocalDateTime.of(2026, 8, 19, 10, 0))
        )
        // Between times on first date -> 2026-08-20 18:00
        assertEquals(
            LocalDateTime.of(2026, 8, 20, 18, 0),
            trigger.getNextOccurrence(LocalDateTime.of(2026, 8, 20, 9, 0))
        )
        // After last time on first date -> rolls to 2026-08-25 08:00
        assertEquals(
            LocalDateTime.of(2026, 8, 25, 8, 0),
            trigger.getNextOccurrence(LocalDateTime.of(2026, 8, 20, 19, 0))
        )
        // After last time on last date -> null (no yearly repeat)
        assertEquals(
            null,
            trigger.getNextOccurrence(LocalDateTime.of(2026, 8, 25, 19, 0))
        )
    }

    @Test
    fun getNextOccurrence_interval_multipleExactDates_rollsBetweenSelectedDatesAndStops() {
        val window = ReminderTimeWindow(startTime = LocalTime.of(8, 0), endTime = LocalTime.of(17, 0))
        val trigger = TimeTrigger(
            intervalMinutes = 60,
            exactDates = listOf(
                LocalDate.of(2026, 8, 20),
                LocalDate.of(2026, 8, 22)
            ),
            repeatMode = RepeatMode.NONE,
            timeWindows = listOf(window)
        )

        // Past window on first date -> jumps to 2026-08-22 08:00
        val onFirstDateEvening = LocalDateTime.of(2026, 8, 20, 16, 30)
        assertEquals(
            LocalDateTime.of(2026, 8, 22, 8, 0),
            trigger.getNextOccurrence(onFirstDateEvening, lastTriggeredTime = onFirstDateEvening, isInterval = true)
        )

        // Past window on last date -> null
        val onLastDateEvening = LocalDateTime.of(2026, 8, 22, 16, 30)
        assertEquals(
            null,
            trigger.getNextOccurrence(onLastDateEvening, lastTriggeredTime = onLastDateEvening, isInterval = true)
        )
    }

    @Test
    fun getNextOccurrence_interval_overnightWindow_evaluatesAcrossMidnight() {
        val overnightWindow = ReminderTimeWindow(
            startTime = LocalTime.of(22, 0),
            endTime = LocalTime.of(6, 0)
        )
        val trigger = TimeTrigger(
            intervalMinutes = 60,
            repeatMode = RepeatMode.DAILY,
            timeWindows = listOf(overnightWindow)
        )

        // 23:00 + 60m -> 00:00 next day
        val lateNight = LocalDateTime.of(2026, 8, 19, 23, 0)
        assertEquals(
            LocalDateTime.of(2026, 8, 20, 0, 0),
            trigger.getNextOccurrence(lateNight, lastTriggeredTime = lateNight, isInterval = true)
        )

        // 01:00 + 60m -> 02:00 same morning
        val earlyMorning = LocalDateTime.of(2026, 8, 20, 1, 0)
        assertEquals(
            LocalDateTime.of(2026, 8, 20, 2, 0),
            trigger.getNextOccurrence(earlyMorning, lastTriggeredTime = earlyMorning, isInterval = true)
        )

        // 05:30 + 60m = 06:30 (outside 22:00-06:00) -> rolls to 22:00 tonight
        val endOfShift = LocalDateTime.of(2026, 8, 20, 5, 30)
        assertEquals(
            LocalDateTime.of(2026, 8, 20, 22, 0),
            trigger.getNextOccurrence(endOfShift, lastTriggeredTime = endOfShift, isInterval = true)
        )
    }

    @Test
    fun getNextOccurrence_interval_allDayMidnightRollover_preservesFullInterval() {
        val trigger = TimeTrigger(
            intervalMinutes = 60,
            repeatMode = RepeatMode.DAILY
        )

        val now = LocalDateTime.of(2026, 8, 19, 23, 30)
        assertEquals(
            LocalDateTime.of(2026, 8, 20, 0, 30),
            trigger.getNextOccurrence(now, lastTriggeredTime = now, isInterval = true)
        )
    }
}

