package vn.io.litever.remind.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

class ReminderTest {
    @Test
    fun defaultValues_areSetCorrectly() {
        val reminder = Reminder(
            title = "Test Reminder",
            triggerType = ReminderTriggerType.FIXED_TIME
        )

        assertEquals(0L, reminder.id)
        assertEquals("Test Reminder", reminder.title)
        assertEquals("", reminder.notes)
        assertTrue(reminder.isEnabled)
        assertEquals(ReminderTriggerType.FIXED_TIME, reminder.triggerType)
        assertNull(reminder.timeTrigger)
        assertTrue(reminder.checklist.isEmpty())
        assertEquals(UrgencyLevel.MEDIUM, reminder.action.urgencyLevel)
        assertEquals(NotificationStyle.STANDARD, reminder.action.notificationStyle)
        assertEquals(0, reminder.currentSnoozeCount)
        assertNull(reminder.snoozeNextTriggerTime)
        assertNull(reminder.lastTriggeredTime)
        assertFalse(reminder.isCompleted)
        assertTrue(reminder.createdAt > 0)
    }

    @Test
    fun checklistCalculations_emptyList_returnsZero() {
        val reminder = Reminder(
            title = "Empty checklist",
            triggerType = ReminderTriggerType.FIXED_TIME
        )

        assertEquals(0, reminder.totalChecklistCount)
        assertEquals(0, reminder.completedChecklistCount)
        assertEquals(0.0f, reminder.checklistProgress, 0.001f)
        assertFalse(reminder.isAllChecklistCompleted)
    }

    @Test
    fun checklistCalculations_partialCompletion_computesCorrectly() {
        val items = listOf(
            ChecklistItem(id = 1, reminderId = 1, text = "Task 1", isCompleted = true, orderIndex = 0),
            ChecklistItem(id = 2, reminderId = 1, text = "Task 2", isCompleted = false, orderIndex = 1)
        )
        val reminder = Reminder(
            title = "Partial checklist",
            triggerType = ReminderTriggerType.FIXED_TIME,
            checklist = items
        )

        assertEquals(2, reminder.totalChecklistCount)
        assertEquals(1, reminder.completedChecklistCount)
        assertEquals(0.5f, reminder.checklistProgress, 0.001f)
        assertFalse(reminder.isAllChecklistCompleted)
    }

    @Test
    fun checklistCalculations_allCompleted_computesCorrectly() {
        val items = listOf(
            ChecklistItem(id = 1, reminderId = 1, text = "Task 1", isCompleted = true, orderIndex = 0),
            ChecklistItem(id = 2, reminderId = 1, text = "Task 2", isCompleted = true, orderIndex = 1)
        )
        val reminder = Reminder(
            title = "All done",
            triggerType = ReminderTriggerType.FIXED_TIME,
            checklist = items
        )

        assertEquals(2, reminder.totalChecklistCount)
        assertEquals(2, reminder.completedChecklistCount)
        assertEquals(1.0f, reminder.checklistProgress, 0.001f)
        assertTrue(reminder.isAllChecklistCompleted)
    }

    @Test
    fun canSnooze_whenEnabledAndCountBelowRepeatCount_returnsTrue() {
        val reminder = Reminder(
            title = "Snooze test",
            triggerType = ReminderTriggerType.FIXED_TIME,
            action = ReminderAction(
                snoozeEnabled = true,
                snoozeRepeatCount = 3
            ),
            currentSnoozeCount = 0
        )
        assertTrue(reminder.canSnooze())

        val snoozedTwice = reminder.copy(currentSnoozeCount = 2)
        assertTrue(snoozedTwice.canSnooze())
    }

    @Test
    fun canSnooze_whenCountReachesRepeatCount_returnsFalse() {
        val reminder = Reminder(
            title = "Snooze limit",
            triggerType = ReminderTriggerType.FIXED_TIME,
            action = ReminderAction(
                snoozeEnabled = true,
                snoozeRepeatCount = 3
            ),
            currentSnoozeCount = 3
        )
        assertFalse(reminder.canSnooze())
    }

    @Test
    fun canSnooze_whenSnoozeDisabled_returnsFalse() {
        val reminder = Reminder(
            title = "No snooze",
            triggerType = ReminderTriggerType.FIXED_TIME,
            action = ReminderAction(
                snoozeEnabled = false,
                snoozeRepeatCount = 3
            ),
            currentSnoozeCount = 0
        )
        assertFalse(reminder.canSnooze())
    }
    
    @Test
    fun canSnooze_whenUrgencyIsSoft_returnsFalse() {
        val reminder = Reminder(
            title = "Soft reminder",
            triggerType = ReminderTriggerType.FIXED_TIME,
            action = ReminderAction(
                urgencyLevel = UrgencyLevel.SOFT,
                snoozeEnabled = true,
                snoozeRepeatCount = 3
            ),
            currentSnoozeCount = 0
        )
        assertFalse(reminder.canSnooze())
    }

    @Test
    fun timeTrigger_oneTime_properties() {
        val time = LocalTime.of(14, 30)
        val date = LocalDate.of(2026, 8, 20)
        val trigger = TimeTrigger(
            time = listOf(time),
            exactDate = date,
            repeatMode = RepeatMode.NONE
        )

        assertEquals(time, trigger.time.first())
        assertEquals(date, trigger.exactDate)
        assertEquals(RepeatMode.NONE, trigger.repeatMode)
        assertFalse(trigger.isRecurring)
    }

    @Test
    fun timeTrigger_recurring_properties() {
        val time = LocalTime.of(8, 0)
        val repeatDays = listOf(DayOfWeek.MONDAY.value, DayOfWeek.WEDNESDAY.value, DayOfWeek.FRIDAY.value)
        val trigger = TimeTrigger(
            time = listOf(time),
            exactDate = null,
            repeatMode = RepeatMode.WEEKLY,
            repeatValues = repeatDays
        )

        assertEquals(time, trigger.time.first())
        assertNull(trigger.exactDate)
        assertEquals(3, trigger.repeatValues.size)
        assertTrue(trigger.isRecurring)
    }
}
