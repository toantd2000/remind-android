package vn.io.litever.remind.features.reminders

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import vn.io.litever.remind.core.domain.util.ChecklistCycleUtil
import vn.io.litever.remind.core.model.ChecklistItem
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.core.model.RepeatMode
import vn.io.litever.remind.core.model.TimeTrigger
import java.time.LocalDateTime
import java.time.ZoneId

class ChecklistCycleUtilTest {

    private val zoneId = ZoneId.of("UTC")

    @Test
    fun shouldResetChecklist_dailyReminder_nextDay_returnsTrue() {
        val lastReset = LocalDateTime.of(2026, 9, 28, 8, 0)
        val lastResetMillis = lastReset.atZone(zoneId).toInstant().toEpochMilli()

        val reminder = Reminder(
            id = 1L,
            title = "Daily Vitamins",
            triggerType = ReminderTriggerType.FIXED_TIME,
            timeTrigger = TimeTrigger(
                repeatMode = RepeatMode.DAILY
            ),
            checklist = listOf(
                ChecklistItem(id = 1, text = "Vitamin C", isCompleted = true)
            ),
            lastChecklistResetTime = lastResetMillis
        )

        val nextDay = LocalDateTime.of(2026, 9, 29, 7, 0)
        assertTrue(ChecklistCycleUtil.shouldResetChecklist(reminder, nextDay, zoneId))
    }

    @Test
    fun shouldResetChecklist_dailyReminder_sameDay_returnsFalse() {
        val lastReset = LocalDateTime.of(2026, 9, 29, 8, 0)
        val lastResetMillis = lastReset.atZone(zoneId).toInstant().toEpochMilli()

        val reminder = Reminder(
            id = 1L,
            title = "Daily Vitamins",
            triggerType = ReminderTriggerType.FIXED_TIME,
            timeTrigger = TimeTrigger(
                repeatMode = RepeatMode.DAILY
            ),
            checklist = listOf(
                ChecklistItem(id = 1, text = "Vitamin C", isCompleted = true)
            ),
            lastChecklistResetTime = lastResetMillis
        )

        val sameDayLater = LocalDateTime.of(2026, 9, 29, 20, 0)
        assertFalse(ChecklistCycleUtil.shouldResetChecklist(reminder, sameDayLater, zoneId))
    }

    @Test
    fun shouldResetChecklist_monthlyReminder_nextMonth_returnsTrue() {
        val lastReset = LocalDateTime.of(2026, 9, 15, 10, 0)
        val lastResetMillis = lastReset.atZone(zoneId).toInstant().toEpochMilli()

        val reminder = Reminder(
            id = 2L,
            title = "Monthly Report",
            triggerType = ReminderTriggerType.FIXED_TIME,
            timeTrigger = TimeTrigger(
                repeatMode = RepeatMode.MONTHLY,
                monthlyDays = listOf(15)
            ),
            checklist = listOf(
                ChecklistItem(id = 1, text = "Send report", isCompleted = true)
            ),
            lastChecklistResetTime = lastResetMillis
        )

        val nextMonth = LocalDateTime.of(2026, 10, 1, 9, 0)
        assertTrue(ChecklistCycleUtil.shouldResetChecklist(reminder, nextMonth, zoneId))
    }

    @Test
    fun shouldResetChecklist_yearlyReminder_nextYear_returnsTrue() {
        val lastReset = LocalDateTime.of(2026, 5, 20, 9, 0)
        val lastResetMillis = lastReset.atZone(zoneId).toInstant().toEpochMilli()

        val reminder = Reminder(
            id = 3L,
            title = "Yearly Tax",
            triggerType = ReminderTriggerType.FIXED_TIME,
            timeTrigger = TimeTrigger(
                repeatMode = RepeatMode.YEARLY
            ),
            checklist = listOf(
                ChecklistItem(id = 1, text = "Prepare documents", isCompleted = true)
            ),
            lastChecklistResetTime = lastResetMillis
        )

        val nextYear = LocalDateTime.of(2027, 1, 1, 9, 0)
        assertTrue(ChecklistCycleUtil.shouldResetChecklist(reminder, nextYear, zoneId))
    }

    @Test
    fun shouldResetChecklist_intervalReminder_nextDay_returnsTrue() {
        val lastReset = LocalDateTime.of(2026, 9, 28, 14, 0)
        val lastResetMillis = lastReset.atZone(zoneId).toInstant().toEpochMilli()

        val reminder = Reminder(
            id = 4L,
            title = "Drink Water",
            triggerType = ReminderTriggerType.INTERVAL,
            timeTrigger = TimeTrigger(
                intervalMinutes = 120
            ),
            checklist = listOf(
                ChecklistItem(id = 1, text = "Drink 500ml", isCompleted = true)
            ),
            lastChecklistResetTime = lastResetMillis
        )

        val nextDay = LocalDateTime.of(2026, 9, 29, 8, 0)
        assertTrue(ChecklistCycleUtil.shouldResetChecklist(reminder, nextDay, zoneId))
    }

    @Test
    fun shouldResetChecklist_noCompletedItems_returnsFalse() {
        val lastReset = LocalDateTime.of(2026, 9, 28, 8, 0)
        val lastResetMillis = lastReset.atZone(zoneId).toInstant().toEpochMilli()

        val reminder = Reminder(
            id = 5L,
            title = "Clean Room",
            triggerType = ReminderTriggerType.FIXED_TIME,
            timeTrigger = TimeTrigger(
                repeatMode = RepeatMode.DAILY
            ),
            checklist = listOf(
                ChecklistItem(id = 1, text = "Vacuum", isCompleted = false)
            ),
            lastChecklistResetTime = lastResetMillis
        )

        val nextDay = LocalDateTime.of(2026, 9, 29, 8, 0)
        assertFalse(ChecklistCycleUtil.shouldResetChecklist(reminder, nextDay, zoneId))
    }

    @Test
    fun shouldResetChecklist_nonRecurringReminder_returnsFalse() {
        val lastReset = LocalDateTime.of(2026, 9, 28, 8, 0)
        val lastResetMillis = lastReset.atZone(zoneId).toInstant().toEpochMilli()

        val reminder = Reminder(
            id = 6L,
            title = "One-time Task",
            triggerType = ReminderTriggerType.FIXED_TIME,
            timeTrigger = TimeTrigger(
                repeatMode = RepeatMode.NONE
            ),
            checklist = listOf(
                ChecklistItem(id = 1, text = "Submit form", isCompleted = true)
            ),
            lastChecklistResetTime = lastResetMillis
        )

        val nextDay = LocalDateTime.of(2026, 9, 29, 8, 0)
        assertFalse(ChecklistCycleUtil.shouldResetChecklist(reminder, nextDay, zoneId))
    }
}
