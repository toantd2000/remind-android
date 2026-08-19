package vn.io.litever.remind.core.alarm.reminder

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import vn.io.litever.remind.core.model.ChecklistItem
import vn.io.litever.remind.core.model.NotificationStyle
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderTriggerType

/**
 * Pure helper simulating Notification and Persistent Alert configuration logic for verification.
 */
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
        val channelId = if (reminder.notificationStyle == NotificationStyle.FULL_SCREEN || reminder.isPersistentAlert) {
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
            isFullScreen = reminder.notificationStyle == NotificationStyle.FULL_SCREEN,
            hasSnoozeAction = reminder.canSnooze(),
            hasDismissAction = true
        )
    }

    class PersistentAlertController(
        val reminder: Reminder,
        val autoSilenceMinutes: Int = 5
    ) {
        var isAlertLooping: Boolean = false
            private set
        var currentRampedVolume: Int = 0
            private set
        var isDismissed: Boolean = false
            private set
        var isSnoozed: Boolean = false
            private set

        fun startAlert() {
            isAlertLooping = true
            currentRampedVolume = if (reminder.volume > 0) reminder.volume else 100
        }

        fun onSnooze(): Boolean {
            if (!reminder.canSnooze()) return false
            isAlertLooping = false
            isSnoozed = true
            return true
        }

        fun onDismiss() {
            isAlertLooping = false
            isDismissed = true
        }

        fun onAutoSilenceTimeout() {
            isAlertLooping = false
        }
    }
}

/**
 * Comprehensive Unit Tests for Notification payloads and Persistent Alert Loop mechanics (Tiers 1 & 2).
 */
class ReminderNotificationAndAlertManagerTest {

    @Test
    fun standardNotification_payloadFormatting_withChecklist() {
        val items = listOf(
            ChecklistItem(id = 1, reminderId = 1, text = "Milk", isCompleted = true),
            ChecklistItem(id = 2, reminderId = 1, text = "Bread", isCompleted = false),
            ChecklistItem(id = 3, reminderId = 1, text = "Eggs", isCompleted = false)
        )
        val reminder = Reminder(
            id = 10L,
            title = "Grocery Run",
            notes = "Supermarket on 5th Ave",
            triggerType = ReminderTriggerType.LOCATION,
            checklist = items,
            notificationStyle = NotificationStyle.STANDARD,
            snoozeEnabled = true,
            snoozeRepeatCount = 3,
            currentSnoozeCount = 0
        )

        val payload = ReminderNotificationTestHelper.buildNotificationPayload(reminder)

        assertEquals(ReminderNotificationTestHelper.CHANNEL_ID_STANDARD, payload.channelId)
        assertEquals("Grocery Run", payload.title)
        assertTrue(payload.body.contains("Supermarket on 5th Ave"))
        assertTrue(payload.body.contains("Checklist: 1/3 completed"))
        assertFalse(payload.isFullScreen)
        assertTrue(payload.hasSnoozeAction)
        assertTrue(payload.hasDismissAction)
    }

    @Test
    fun fullScreenNotification_usesUrgentChannelAndFlag() {
        val reminder = Reminder(
            id = 20L,
            title = "Critical Alarm",
            triggerType = ReminderTriggerType.TIME,
            notificationStyle = NotificationStyle.FULL_SCREEN
        )

        val payload = ReminderNotificationTestHelper.buildNotificationPayload(reminder)

        assertEquals(ReminderNotificationTestHelper.CHANNEL_ID_URGENT, payload.channelId)
        assertTrue(payload.isFullScreen)
    }

    @Test
    fun notification_whenSnoozeLimitReached_hidesSnoozeAction() {
        val reminder = Reminder(
            id = 30L,
            title = "Doctor Appointment",
            triggerType = ReminderTriggerType.TIME,
            snoozeEnabled = true,
            snoozeRepeatCount = 2,
            currentSnoozeCount = 2 // max reached!
        )

        val payload = ReminderNotificationTestHelper.buildNotificationPayload(reminder)

        assertFalse(payload.hasSnoozeAction)
        assertTrue(payload.hasDismissAction)
    }

    @Test
    fun persistentAlert_start_setsLoopingAndVolume() {
        val reminder = Reminder(
            id = 40L,
            title = "Morning Medicine",
            triggerType = ReminderTriggerType.TIME,
            isPersistentAlert = true,
            volume = 90
        )
        val controller = ReminderNotificationTestHelper.PersistentAlertController(reminder)

        controller.startAlert()

        assertTrue(controller.isAlertLooping)
        assertEquals(90, controller.currentRampedVolume)
        assertFalse(controller.isDismissed)
        assertFalse(controller.isSnoozed)
    }

    @Test
    fun persistentAlert_onDismiss_stopsLoopImmediately() {
        val reminder = Reminder(
            id = 40L,
            title = "Morning Medicine",
            triggerType = ReminderTriggerType.TIME,
            isPersistentAlert = true
        )
        val controller = ReminderNotificationTestHelper.PersistentAlertController(reminder)

        controller.startAlert()
        assertTrue(controller.isAlertLooping)

        controller.onDismiss()
        assertFalse(controller.isAlertLooping)
        assertTrue(controller.isDismissed)
    }

    @Test
    fun persistentAlert_onSnooze_stopsLoopAndMarksSnoozed() {
        val reminder = Reminder(
            id = 40L,
            title = "Morning Medicine",
            triggerType = ReminderTriggerType.TIME,
            isPersistentAlert = true,
            snoozeEnabled = true,
            snoozeRepeatCount = 3,
            currentSnoozeCount = 1
        )
        val controller = ReminderNotificationTestHelper.PersistentAlertController(reminder)

        controller.startAlert()
        val snoozeResult = controller.onSnooze()

        assertTrue(snoozeResult)
        assertFalse(controller.isAlertLooping)
        assertTrue(controller.isSnoozed)
    }

    @Test
    fun persistentAlert_autoSilenceTimeout_silencesLoop() {
        val reminder = Reminder(
            id = 50L,
            title = "Unattended Reminder",
            triggerType = ReminderTriggerType.TIME,
            isPersistentAlert = true
        )
        val controller = ReminderNotificationTestHelper.PersistentAlertController(reminder, autoSilenceMinutes = 3)

        controller.startAlert()
        assertTrue(controller.isAlertLooping)

        controller.onAutoSilenceTimeout()
        assertFalse(controller.isAlertLooping)
    }
}
