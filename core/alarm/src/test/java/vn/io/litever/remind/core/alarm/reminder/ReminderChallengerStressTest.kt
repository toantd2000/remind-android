package vn.io.litever.remind.core.alarm.reminder

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import vn.io.litever.remind.core.model.ReminderTimeWindow
import vn.io.litever.remind.core.model.TimeTrigger
import java.time.LocalDateTime
import java.time.LocalTime

class ReminderChallengerStressTest {

    @Test
    fun intervalTrigger_multiTimeWindows_stressSimulationAcross7Days() {
        val windows = listOf(
            ReminderTimeWindow(startTime = LocalTime.of(8, 30), endTime = LocalTime.of(11, 30)),
            ReminderTimeWindow(startTime = LocalTime.of(13, 30), endTime = LocalTime.of(17, 30))
        )
        val trigger = TimeTrigger(
            intervalMinutes = 60,
            timeWindows = windows
        )

        var currentTime = LocalDateTime.of(2026, 8, 17, 8, 0) // Monday morning 8:00
        val observedOccurrences = mutableListOf<LocalDateTime>()

        for (step in 1..50) {
            val next = trigger.getNextOccurrence(
                now = currentTime,
                lastTriggeredTime = currentTime,
                isInterval = true
            ) ?: break

            observedOccurrences.add(next)
            // Assert that every scheduled occurrence either falls within a window or exactly at a window start
            val inWindow = windows.any { it.isWithinWindow(next) }
            val isWindowStart = windows.any { it.startTime == next.toLocalTime() }
            assertTrue("Occurrence $next must be inside window or at window start", inWindow || isWindowStart)

            currentTime = next
        }

        assertTrue("Should produce multiple scheduled occurrences", observedOccurrences.size >= 20)
    }

    @Test
    fun requestCodes_primaryAndSnooze_neverCollideForAnyId() {
        fun getReminderRequestCode(reminderId: Long, isSnooze: Boolean): Int {
            val baseHash = (reminderId.hashCode() and 0x0FFFFFFF)
            return if (isSnooze) 250000000 + baseHash else 200000000 + baseHash
        }

        val testIds = listOf(
            0L, 1L, 2L, 100L, 1000L, 999999L,
            Long.MAX_VALUE, Long.MIN_VALUE,
            123456789L, -123456789L,
            0x7FFFFFFFL, 0x10000000L
        )

        for (id in testIds) {
            val primaryReq = getReminderRequestCode(id, isSnooze = false)
            val snoozeReq = getReminderRequestCode(id, isSnooze = true)

            assertNotEquals("Primary and Snooze request codes must not collide for ID $id", primaryReq, snoozeReq)
            assertEquals("Snooze must differ from primary by exactly 50,000,000", 50000000, snoozeReq - primaryReq)
            assertTrue("Primary code in range: $primaryReq", primaryReq in 200000000..468435455)
            assertTrue("Snooze code in range: $snoozeReq", snoozeReq in 250000000..518435455)
        }
    }
}
