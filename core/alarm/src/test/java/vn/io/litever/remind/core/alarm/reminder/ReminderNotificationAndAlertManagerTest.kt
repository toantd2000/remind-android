package vn.io.litever.remind.core.alarm.reminder

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import vn.io.litever.remind.core.model.ChecklistItem
import vn.io.litever.remind.core.model.NotificationStyle
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderAction
import vn.io.litever.remind.core.model.ReminderTriggerType

object ReminderNotificationTestHelper {

    const val CHANNEL_ID_STANDARD = "reminder_channel_standard"
    const val CHANNEL_ID_URGENT = "reminder_channel_urgent"

    data class NotificationPayload(
        val channelId: String,
        val title: String,
        val body: String,
        val isFullScreen: Boolean,
        val hasSnoozeAction: Boolean,
        val hasDismissAction: Boolean
    )

    fun buildNotificationPayload(reminder: Reminder): NotificationPayload {
        val channelId = if (reminder.action.notificationStyle == NotificationStyle.FULL_SCREEN || reminder.action.isPersistentAlert) {
            CHANNEL_ID_URGENT
        } else {
            CHANNEL_ID_STANDARD
        }

        val body = buildString {
            if (reminder.notes.isNotBlank()) {
                append(reminder.notes)
            }
            if (reminder.checklist.isNotEmpty()) {
                if (isNotEmpty()) append("\n")
                append("Checklist: ${reminder.completedChecklistCount}/${reminder.totalChecklistCount} completed")
            }
        }

        return NotificationPayload(
            channelId = channelId,
            title = reminder.title,
            body = body,
            isFullScreen = reminder.action.notificationStyle == NotificationStyle.FULL_SCREEN,
            hasSnoozeAction = reminder.canSnooze(),
            hasDismissAction = true
        )
    }

    class PersistentAlertController(
        val reminder: Reminder,
        val autoSilenceMinutes: Int = 5
    ) {
        var isPlaying: Boolean = false
            private set
        var isVibrating: Boolean = false
            private set
        var elapsedSeconds: Int = 0
            private set
        var isAutoSilenced: Boolean = false
            private set

        fun start() {
            isPlaying = true
            isVibrating = reminder.action.vibrationEnabled
            elapsedSeconds = 0
            isAutoSilenced = false
        }

        fun tickOneSecond() {
            if (!isPlaying) return
            elapsedSeconds++
            if (elapsedSeconds >= autoSilenceMinutes * 60) {
                isAutoSilenced = true
                stop()
            }
        }

        fun stop() {
            isPlaying = false
            isVibrating = false
        }
    }
}

class ReminderNotificationAndAlertManagerTest {

    @Test
    fun standardNotification_payload_buildsCorrectly() {
        val reminder = Reminder(
            id = 1,
            title = "Drink Water",
            notes = "Stay hydrated",
            triggerType = ReminderTriggerType.INTERVAL,
            action = ReminderAction(
                notificationStyle = NotificationStyle.STANDARD,
                snoozeEnabled = false
            )
        )

        val payload = ReminderNotificationTestHelper.buildNotificationPayload(reminder)

        assertEquals(ReminderNotificationTestHelper.CHANNEL_ID_STANDARD, payload.channelId)
        assertEquals("Drink Water", payload.title)
        assertEquals("Stay hydrated", payload.body)
        assertFalse(payload.isFullScreen)
        assertFalse(payload.hasSnoozeAction)
        assertTrue(payload.hasDismissAction)
    }

    @Test
    fun fullScreenNotification_payload_usesUrgentChannel() {
        val reminder = Reminder(
            id = 2,
            title = "Urgent Medication",
            notes = "Take pill A",
            triggerType = ReminderTriggerType.FIXED_TIME,
            action = ReminderAction(
                notificationStyle = NotificationStyle.FULL_SCREEN,
                snoozeEnabled = true,
                snoozeRepeatCount = 3
            )
        )

        val payload = ReminderNotificationTestHelper.buildNotificationPayload(reminder)

        assertEquals(ReminderNotificationTestHelper.CHANNEL_ID_URGENT, payload.channelId)
        assertTrue(payload.isFullScreen)
        assertTrue(payload.hasSnoozeAction)
    }

    @Test
    fun notification_withChecklistSummary_formatsBodyProperly() {
        val reminder = Reminder(
            id = 3,
            title = "Grocery",
            triggerType = ReminderTriggerType.FIXED_TIME,
            checklist = listOf(
                ChecklistItem(text = "Milk", isCompleted = true),
                ChecklistItem(text = "Eggs", isCompleted = false)
            ),
            action = ReminderAction(
                snoozeEnabled = true,
                snoozeRepeatCount = 3
            )
        )

        val payload = ReminderNotificationTestHelper.buildNotificationPayload(reminder)

        assertTrue(payload.body.contains("Checklist: 1/2 completed"))
    }

    @Test
    fun persistentAlert_controllerLifecycle_startsAndSilencesCorrectly() {
        val reminder = Reminder(
            id = 4,
            title = "Morning Alarm",
            triggerType = ReminderTriggerType.FIXED_TIME,
            action = ReminderAction(
                isPersistentAlert = true,
                volume = 80,
                vibrationEnabled = true
            )
        )

        val controller = ReminderNotificationTestHelper.PersistentAlertController(reminder, autoSilenceMinutes = 1)
        controller.start()

        assertTrue(controller.isPlaying)
        assertTrue(controller.isVibrating)

        repeat(59) { controller.tickOneSecond() }
        assertFalse(controller.isAutoSilenced)
        assertTrue(controller.isPlaying)

        controller.tickOneSecond()
        assertTrue(controller.isAutoSilenced)
        assertFalse(controller.isPlaying)
    }

    @Test
    fun persistentAlert_manualStop_haltsPlayback() {
        val reminder = Reminder(
            id = 5,
            title = "Meeting",
            triggerType = ReminderTriggerType.FIXED_TIME,
            action = ReminderAction(
                isPersistentAlert = true
            )
        )

        val controller = ReminderNotificationTestHelper.PersistentAlertController(reminder)
        controller.start()
        assertTrue(controller.isPlaying)

        controller.stop()
        assertFalse(controller.isPlaying)
        assertFalse(controller.isAutoSilenced)
    }
}
