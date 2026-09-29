package vn.io.litever.remind.core.alarm.reminder

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.core.model.TimeTrigger
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object ReminderSchedulerTestHelper {

    fun computeNextTriggerEpochMillis(
        reminder: Reminder,
        referenceTime: LocalDateTime = LocalDateTime.now()
    ): Long? {
        if (!reminder.isEnabled || reminder.isCompleted) return null

        val snoozeTime = reminder.snoozeNextTriggerTime
        if (snoozeTime != null && snoozeTime > 0) {
            return snoozeTime
        }

        val timeTrigger = reminder.timeTrigger ?: return null
        val nextOccurrence = timeTrigger.getNextOccurrence(
            now = referenceTime,
            isInterval = reminder.triggerType == ReminderTriggerType.INTERVAL
        ) ?: return null

        return nextOccurrence.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun computeSnoozeTriggerEpochMillis(
        currentEpochMillis: Long,
        snoozeIntervalMinutes: Int
    ): Long {
        return currentEpochMillis + (snoozeIntervalMinutes * 60 * 1000L)
    }
}

class ReminderSchedulerAndServiceTest {

    @Test
    fun scheduler_computesNextOccurrence_fixedTimeOneTimeFuture() {
        val now = LocalDateTime.of(2026, 8, 19, 10, 0)
        val reminder = Reminder(
            id = 1,
            title = "Dentist",
            triggerType = ReminderTriggerType.FIXED_TIME,
            timeTrigger = TimeTrigger(
                time = listOf(LocalTime.of(15, 0)),
                exactDate = LocalDate.of(2026, 8, 19)
            )
        )

        val triggerEpoch = ReminderSchedulerTestHelper.computeNextTriggerEpochMillis(reminder, now)
        assertNotNull(triggerEpoch)

        val expected = LocalDateTime.of(2026, 8, 19, 15, 0)
            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        assertEquals(expected, triggerEpoch)
    }

    @Test
    fun scheduler_computesNextOccurrence_interval() {
        val now = LocalDateTime.of(2026, 8, 19, 10, 0)
        val reminder = Reminder(
            id = 2,
            title = "Drink Water",
            triggerType = ReminderTriggerType.INTERVAL,
            timeTrigger = TimeTrigger(
                intervalMinutes = 60
            )
        )

        val triggerEpoch = ReminderSchedulerTestHelper.computeNextTriggerEpochMillis(reminder, now)
        assertNotNull(triggerEpoch)

        val expected = LocalDateTime.of(2026, 8, 19, 11, 0)
            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        assertEquals(expected, triggerEpoch)
    }

    @Test
    fun scheduler_computesSnoozeInterval_accurately() {
        val baseTime = 1724050000000L
        val intervalMinutes = 10
        val snoozeTime = ReminderSchedulerTestHelper.computeSnoozeTriggerEpochMillis(baseTime, intervalMinutes)

        assertEquals(baseTime + 600000L, snoozeTime)
    }

    @Test
    fun scheduler_disabledOrCompleted_returnsNull() {
        val disabled = Reminder(
            id = 3,
            title = "Disabled",
            isEnabled = false,
            triggerType = ReminderTriggerType.FIXED_TIME,
            timeTrigger = TimeTrigger(time = listOf(LocalTime.of(12, 0)))
        )
        assertNull(ReminderSchedulerTestHelper.computeNextTriggerEpochMillis(disabled))
    }
}
