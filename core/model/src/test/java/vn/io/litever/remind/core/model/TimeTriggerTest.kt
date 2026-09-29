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
    fun getNextOccurrence_fixedTime_todayUpcoming_returnsToday() {
        val trigger = TimeTrigger(
            time = listOf(LocalTime.of(9, 0)),
            repeatMode = RepeatMode.DAILY
        )
        val now = LocalDateTime.of(2026, 8, 19, 8, 0)

        val next = trigger.getNextOccurrence(now)
        assertEquals(LocalDateTime.of(2026, 8, 19, 9, 0, 0, 0), next)
    }

    @Test
    fun getNextOccurrence_fixedTime_todayPassed_returnsTomorrow() {
        val trigger = TimeTrigger(
            time = listOf(LocalTime.of(9, 0)),
            repeatMode = RepeatMode.DAILY
        )
        val now = LocalDateTime.of(2026, 8, 19, 10, 0)

        val next = trigger.getNextOccurrence(now)
        assertEquals(LocalDateTime.of(2026, 8, 20, 9, 0, 0, 0), next)
    }
}
