package vn.io.litever.remind.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderTest {

    @Test
    fun defaultValues_initializedProperly() {
        val reminder = Reminder(
            title = "Test Reminder",
            triggerType = ReminderTriggerType.TIME
        )

        assertEquals(0L, reminder.id)
        assertEquals("Test Reminder", reminder.title)
        assertEquals("", reminder.notes)
        assertTrue(reminder.isEnabled)
        assertEquals(ReminderTriggerType.TIME, reminder.triggerType)
        assertEquals(emptyList<ChecklistItem>(), reminder.checklist)
        assertEquals(NotificationStyle.STANDARD, reminder.notificationStyle)
        assertFalse(reminder.isPersistentAlert)
        assertTrue(reminder.snoozeEnabled)
        assertEquals(5, reminder.snoozeIntervalMinutes)
        assertEquals(3, reminder.snoozeRepeatCount)
        assertEquals(0, reminder.currentSnoozeCount)
        assertFalse(reminder.isCompleted)
    }

    @Test
    fun checklistMetrics_emptyChecklist_progressIsZero() {
        val reminder = Reminder(
            title = "No checklist",
            triggerType = ReminderTriggerType.TIME,
            checklist = emptyList()
        )

        assertEquals(0, reminder.totalChecklistCount)
        assertEquals(0, reminder.completedChecklistCount)
        assertEquals(0f, reminder.checklistProgress, 0.001f)
        assertFalse(reminder.isAllChecklistCompleted)
    }

    @Test
    fun checklistMetrics_partialCompleted_calculatesCorrectFraction() {
        val checklist = listOf(
            ChecklistItem(id = 1, reminderId = 1, text = "Task 1", isCompleted = true, orderIndex = 0),
            ChecklistItem(id = 2, reminderId = 1, text = "Task 2", isCompleted = false, orderIndex = 1),
            ChecklistItem(id = 3, reminderId = 1, text = "Task 3", isCompleted = false, orderIndex = 2),
            ChecklistItem(id = 4, reminderId = 1, text = "Task 4", isCompleted = false, orderIndex = 3)
        )
        val reminder = Reminder(
            title = "Partial checklist",
            triggerType = ReminderTriggerType.TIME,
            checklist = checklist
        )

        assertEquals(4, reminder.totalChecklistCount)
        assertEquals(1, reminder.completedChecklistCount)
        assertEquals(0.25f, reminder.checklistProgress, 0.001f)
        assertFalse(reminder.isAllChecklistCompleted)
    }

    @Test
    fun checklistMetrics_allCompleted_returnsTrue() {
        val checklist = listOf(
            ChecklistItem(id = 1, reminderId = 1, text = "Task 1", isCompleted = true, orderIndex = 0),
            ChecklistItem(id = 2, reminderId = 1, text = "Task 2", isCompleted = true, orderIndex = 1),
            ChecklistItem(id = 3, reminderId = 1, text = "Task 3", isCompleted = true, orderIndex = 2)
        )
        val reminder = Reminder(
            title = "All done",
            triggerType = ReminderTriggerType.TIME,
            checklist = checklist
        )

        assertEquals(3, reminder.totalChecklistCount)
        assertEquals(3, reminder.completedChecklistCount)
        assertEquals(1.0f, reminder.checklistProgress, 0.001f)
        assertTrue(reminder.isAllChecklistCompleted)
    }

    @Test
    fun canSnooze_whenUnderMaxRepeatCount_returnsTrue() {
        val reminder = Reminder(
            title = "Snooze test",
            triggerType = ReminderTriggerType.TIME,
            snoozeEnabled = true,
            snoozeRepeatCount = 3,
            currentSnoozeCount = 2
        )

        assertTrue(reminder.canSnooze())
    }

    @Test
    fun canSnooze_whenMaxRepeatCountReached_returnsFalse() {
        val reminder = Reminder(
            title = "Max snooze test",
            triggerType = ReminderTriggerType.TIME,
            snoozeEnabled = true,
            snoozeRepeatCount = 3,
            currentSnoozeCount = 3
        )

        assertFalse(reminder.canSnooze())
    }

    @Test
    fun canSnooze_whenSnoozeDisabled_returnsFalse() {
        val reminder = Reminder(
            title = "Disabled snooze test",
            triggerType = ReminderTriggerType.TIME,
            snoozeEnabled = false,
            snoozeRepeatCount = 3,
            currentSnoozeCount = 0
        )

        assertFalse(reminder.canSnooze())
    }
}
