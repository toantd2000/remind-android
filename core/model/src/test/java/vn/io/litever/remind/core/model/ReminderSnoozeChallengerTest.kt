package vn.io.litever.remind.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.time.DayOfWeek
import java.time.LocalTime

/**
 * Adversarial Challenger Stress Test for Snooze semantics.
 */
class ReminderSnoozeChallengerTest {

    @Test
    fun snoozeProgressionAndExhaustion_stateTransitions() {
        var reminder = Reminder(
            title = "Snoozable Reminder",
            triggerType = ReminderTriggerType.FIXED_TIME,
            action = ReminderAction(
                snoozeEnabled = true,
                snoozeIntervalMinutes = 10,
                snoozeRepeatCount = 3
            ),
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
            triggerType = ReminderTriggerType.FIXED_TIME,
            action = ReminderAction(
                snoozeEnabled = true,
                snoozeRepeatCount = 0
            ),
            currentSnoozeCount = 0
        )
        assertFalse(reminder.canSnooze())
    }

    @Test
    fun snoozeRepeatCountUnlimited_canAlwaysSnooze() {
        val reminder = Reminder(
            title = "Unlimited Snoozes Allowed",
            triggerType = ReminderTriggerType.FIXED_TIME,
            action = ReminderAction(
                snoozeEnabled = true,
                snoozeRepeatCount = -1
            ),
            currentSnoozeCount = 100
        )
        assertTrue(reminder.canSnooze())
    }

    @Test
    fun javaSerializationRoundTrip_preservesFullState() {
        val original = Reminder(
            id = 777L,
            title = "Serialization Test",
            notes = "Special notes",
            isEnabled = true,
            triggerType = ReminderTriggerType.FIXED_TIME,
            timeTrigger = TimeTrigger(
                time = listOf(LocalTime.of(8, 0)),
                repeatMode = RepeatMode.DAILY,
                timeWindow = ReminderTimeWindow(
                    startTime = java.time.LocalTime.of(8, 0),
                    endTime = java.time.LocalTime.of(18, 0),
                    activeDays = listOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY)
                )
            ),
            action = ReminderAction(
                notificationStyle = NotificationStyle.FULL_SCREEN,
                isPersistentAlert = true,
                snoozeIntervalMinutes = 15,
                snoozeRepeatCount = 5
            ),
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
        assertTrue(deserialized.canSnooze())
    }
}
