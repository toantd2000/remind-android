package vn.io.litever.remind.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

/**
 * Adversarial Challenger Stress Test for Checklist & Snooze semantics.
 */
class ChecklistAndSnoozeChallengerTest {

    @Test
    fun largeScaleChecklistProgress_computesAccurately() {
        val items = (1..1000).map { i ->
            ChecklistItem(
                id = i.toLong(),
                reminderId = 100L,
                text = "Item $i",
                isCompleted = (i <= 350),
                orderIndex = i
            )
        }

        val reminder = Reminder(
            id = 100L,
            title = "Massive Checklist",
            triggerType = ReminderTriggerType.TIME,
            checklist = items
        )

        assertEquals(1000, reminder.totalChecklistCount)
        assertEquals(350, reminder.completedChecklistCount)
        assertEquals(0.35f, reminder.checklistProgress, 0.0001f)
        assertFalse(reminder.isAllChecklistCompleted)
    }

    @Test
    fun allItemsCompleted_boundaryValidation() {
        val singleCompleted = Reminder(
            title = "One Item",
            triggerType = ReminderTriggerType.TIME,
            checklist = listOf(ChecklistItem(id = 1, text = "Only task", isCompleted = true))
        )
        assertEquals(1, singleCompleted.totalChecklistCount)
        assertEquals(1, singleCompleted.completedChecklistCount)
        assertEquals(1.0f, singleCompleted.checklistProgress, 0.0001f)
        assertTrue(singleCompleted.isAllChecklistCompleted)

        val singleIncomplete = Reminder(
            title = "One Item Incomplete",
            triggerType = ReminderTriggerType.TIME,
            checklist = listOf(ChecklistItem(id = 1, text = "Only task", isCompleted = false))
        )
        assertEquals(1, singleIncomplete.totalChecklistCount)
        assertEquals(0, singleIncomplete.completedChecklistCount)
        assertEquals(0.0f, singleIncomplete.checklistProgress, 0.0001f)
        assertFalse(singleIncomplete.isAllChecklistCompleted)
    }

    @Test
    fun snoozeProgressionAndExhaustion_stateTransitions() {
        var reminder = Reminder(
            title = "Snoozable Reminder",
            triggerType = ReminderTriggerType.TIME,
            snoozeEnabled = true,
            snoozeIntervalMinutes = 10,
            snoozeRepeatCount = 3,
            currentSnoozeCount = 0
        )

        // 0 of 3 snoozes used
        assertTrue(reminder.canSnooze())

        // 1st snooze
        reminder = reminder.copy(currentSnoozeCount = 1, snoozeNextTriggerTime = 10000L)
        assertTrue(reminder.canSnooze())
        assertEquals(1, reminder.currentSnoozeCount)

        // 2nd snooze
        reminder = reminder.copy(currentSnoozeCount = 2, snoozeNextTriggerTime = 20000L)
        assertTrue(reminder.canSnooze())
        assertEquals(2, reminder.currentSnoozeCount)

        // 3rd snooze (limit reached)
        reminder = reminder.copy(currentSnoozeCount = 3, snoozeNextTriggerTime = 30000L)
        assertFalse(reminder.canSnooze())
        assertEquals(3, reminder.currentSnoozeCount)

        // Beyond limit
        reminder = reminder.copy(currentSnoozeCount = 4)
        assertFalse(reminder.canSnooze())
    }

    @Test
    fun snoozeRepeatCountZero_cannotSnooze() {
        val reminder = Reminder(
            title = "No Snooze Allowed",
            triggerType = ReminderTriggerType.TIME,
            snoozeEnabled = true,
            snoozeRepeatCount = 0,
            currentSnoozeCount = 0
        )
        assertFalse(reminder.canSnooze())
    }

    @Test
    fun snoozeRepeatCountUnlimited_canAlwaysSnooze() {
        val reminder = Reminder(
            title = "Unlimited Snoozes Allowed",
            triggerType = ReminderTriggerType.TIME,
            snoozeEnabled = true,
            snoozeRepeatCount = -1,
            currentSnoozeCount = 100
        )
        assertTrue(reminder.canSnooze())
    }

    @Test
    fun javaSerializationRoundTrip_preservesFullStateAndChecklist() {
        val original = Reminder(
            id = 777L,
            title = "Serialization Test",
            notes = "Special notes",
            isEnabled = true,
            triggerType = ReminderTriggerType.LOCATION,
            locationTrigger = LocationTrigger(
                latitude = 37.7749,
                longitude = -122.4194,
                radiusMeters = 150f,
                locationName = "San Francisco Office",
                transitionType = GeofenceTransitionType.BOTH,
                timeWindow = ReminderTimeWindow(
                    startTime = java.time.LocalTime.of(8, 0),
                    endTime = java.time.LocalTime.of(18, 0),
                    activeDays = listOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY)
                )
            ),
            checklist = listOf(
                ChecklistItem(id = 1L, reminderId = 777L, text = "Badge", isCompleted = true, orderIndex = 0),
                ChecklistItem(id = 2L, reminderId = 777L, text = "Laptop", isCompleted = false, orderIndex = 1)
            ),
            notificationStyle = NotificationStyle.FULL_SCREEN,
            isPersistentAlert = true,
            snoozeIntervalMinutes = 15,
            snoozeRepeatCount = 5,
            currentSnoozeCount = 2,
            snoozeNextTriggerTime = 9999999L,
            lastTriggeredTime = 8888888L,
            isCompleted = false
        )

        // Serialize
        val baos = ByteArrayOutputStream()
        ObjectOutputStream(baos).use { it.writeObject(original) }

        // Deserialize
        val bais = ByteArrayInputStream(baos.toByteArray())
        val deserialized = ObjectInputStream(bais).use { it.readObject() as Reminder }

        assertEquals(original, deserialized)
        assertEquals(2, deserialized.checklist.size)
        assertEquals(0.5f, deserialized.checklistProgress, 0.0001f)
        assertTrue(deserialized.canSnooze())
    }
}
