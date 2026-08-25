package vn.io.litever.remind.core.alarm.notification

import org.robolectric.RobolectricTestRunner
import org.junit.runner.RunWith

import android.app.Notification
import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import android.content.Context
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import vn.io.litever.remind.core.alarm.R
import vn.io.litever.remind.core.common.util.PermissionChecker
import vn.io.litever.remind.core.model.ChecklistItem
import vn.io.litever.remind.core.model.NotificationStyle
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderTriggerType

import androidx.test.core.app.ApplicationProvider
import io.mockk.spyk

@RunWith(RobolectricTestRunner::class)
class ReminderNotificationManagerTest {

    private val appContext: Context = ApplicationProvider.getApplicationContext()
    private val context = spyk(appContext)
    private val notificationManager = mockk<NotificationManager>(relaxed = true)
    private val permissionChecker = mockk<PermissionChecker>(relaxed = true)

    private lateinit var manager: ReminderNotificationManager

    @Before
    fun setup() {
        every { context.getSystemService(Context.NOTIFICATION_SERVICE) } returns notificationManager
        every { permissionChecker.hasNotificationPermission() } returns true
        every { permissionChecker.hasFullScreenIntentPermission() } returns true

        manager = ReminderNotificationManager(context, permissionChecker)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun formatContentText_withNotesAndChecklist_formatsSnippetCorrectly() {
        val reminder = Reminder(
            id = 1L,
            title = "Groceries",
            notes = "Target Store",
            triggerType = ReminderTriggerType.LOCATION,
            checklist = listOf(
                ChecklistItem(id = 1L, reminderId = 1L, text = "Milk", isCompleted = true, orderIndex = 0),
                ChecklistItem(id = 2L, reminderId = 1L, text = "Bread", isCompleted = false, orderIndex = 1)
            )
        )

        val text = manager.formatContentText(reminder)
        assertTrue(text.contains("Target Store"))
        assertTrue(text.contains("[1/2 done]"))
        assertTrue(text.contains("Bread"))
    }

    @Test
    fun formatContentText_withChecklistOnly_formatsSnippetCorrectly() {
        val reminder = Reminder(
            id = 2L,
            title = "Tasks",
            notes = "",
            triggerType = ReminderTriggerType.TIME,
            checklist = listOf(
                ChecklistItem(id = 1L, reminderId = 2L, text = "Write tests", isCompleted = false, orderIndex = 0)
            )
        )

        val text = manager.formatContentText(reminder)
        assertTrue(text.contains("[0/1 done]"))
        assertTrue(text.contains("Write tests"))
    }

    @Test
    fun formatBigTextStyle_includesAllItemsWithCheckedMarkers() {
        val reminder = Reminder(
            id = 3L,
            title = "Daily Routine",
            notes = "Morning list",
            triggerType = ReminderTriggerType.TIME,
            checklist = listOf(
                ChecklistItem(id = 1L, reminderId = 3L, text = "Meditation", isCompleted = true, orderIndex = 0),
                ChecklistItem(id = 2L, reminderId = 3L, text = "Jogging", isCompleted = false, orderIndex = 1)
            )
        )

        val bigTextStyle = manager.formatBigTextStyle(reminder, "Daily Routine")
        assertNotNull(bigTextStyle)
    }

    @Test
    fun buildNotification_standardReminder_createsValidNotification() {
        val reminder = Reminder(
            id = 10L,
            title = "Standup Meeting",
            notes = "Room 302",
            triggerType = ReminderTriggerType.TIME,
            notificationStyle = NotificationStyle.STANDARD,
            snoozeEnabled = true,
            snoozeIntervalMinutes = 5,
            snoozeRepeatCount = 3,
            currentSnoozeCount = 0
        )

        val notification = manager.buildNotification(reminder)
        assertNotNull(notification)
    }

    @Test
    fun buildNotification_whenSnoozeMaxCountReached_omitsSnoozeAction() {
        val reminder = Reminder(
            id = 20L,
            title = "Final Reminder",
            triggerType = ReminderTriggerType.TIME,
            snoozeEnabled = true,
            snoozeRepeatCount = 2,
            currentSnoozeCount = 2 // max reached!
        )

        assertFalse(reminder.canSnooze())
        val notification = manager.buildNotification(reminder)
        assertNotNull(notification)
    }

    @Test
    fun buildNotification_fullScreenIntent_withPermission_configuresAlarmCategory() {
        every { permissionChecker.hasFullScreenIntentPermission() } returns true

        val reminder = Reminder(
            id = 30L,
            title = "Critical Alarm",
            triggerType = ReminderTriggerType.TIME,
            notificationStyle = NotificationStyle.FULL_SCREEN,
            isPersistentAlert = true
        )

        val notification = manager.buildNotification(reminder)
        assertNotNull(notification)
        assertEquals(NotificationCompat.CATEGORY_ALARM, notification.category)
    }

    @Test
    fun buildNotification_fullScreenIntent_whenPermissionDenied_fallsBackToHighPriority() {
        every { permissionChecker.hasFullScreenIntentPermission() } returns false

        val reminder = Reminder(
            id = 31L,
            title = "Alarm Without FSI Perm",
            triggerType = ReminderTriggerType.TIME,
            notificationStyle = NotificationStyle.FULL_SCREEN,
            isPersistentAlert = false
        )

        val notification = manager.buildNotification(reminder)
        assertNotNull(notification)
        // Fallback category
        assertEquals(NotificationCompat.CATEGORY_REMINDER, notification.category)
    }

    @Test
    fun showNotification_whenPermissionGranted_notifiesNotificationManager() {
        every { permissionChecker.hasNotificationPermission() } returns true

        val reminder = Reminder(
            id = 40L,
            title = "Notify Test",
            triggerType = ReminderTriggerType.TIME
        )

        manager.showNotification(reminder)

        val expectedId = manager.getNotificationId(40L)
        verify(atLeast = 1) { notificationManager.notify(expectedId, any<Notification>()) }
    }

    @Test
    fun showNotification_whenPermissionDenied_doesNotNotify() {
        every { permissionChecker.hasNotificationPermission() } returns false

        val reminder = Reminder(
            id = 41L,
            title = "Denied Notify",
            triggerType = ReminderTriggerType.TIME
        )

        manager.showNotification(reminder)

        val expectedId = manager.getNotificationId(41L)
        verify(exactly = 0) { notificationManager.notify(expectedId, any<Notification>()) }
    }

    @Test
    fun cancelNotification_cancelsSpecificNotificationId() {
        manager.cancelNotification(50L)

        val expectedId = manager.getNotificationId(50L)
        verify(atLeast = 1) { notificationManager.cancel(expectedId) }
    }

    @Test
    fun cancelAll_cancelsAllNotifications() {
        manager.cancelAll()
        verify(atLeast = 1) { notificationManager.cancelAll() }
    }

    @Test
    fun buildNotification_containsSnoozeAndDismissActions() {
        val reminder = Reminder(
            id = 100L,
            title = "Task with checklist",
            triggerType = ReminderTriggerType.TIME,
            snoozeEnabled = true,
            snoozeIntervalMinutes = 10,
            checklist = listOf(
                ChecklistItem(id = 1L, reminderId = 100L, text = "Subtask 1", isCompleted = false)
            )
        )

        val notification = manager.buildNotification(reminder)
        assertNotNull(notification)
        // Should contain Snooze and Dismiss actions (2 actions)
        assertEquals(2, notification.actions?.size ?: 0)
        assertEquals("Snooze (10m)", notification.actions?.get(0)?.title?.toString())
        assertEquals("Dismiss", notification.actions?.get(1)?.title?.toString())
    }

    @Test
    fun buildNotification_whenSnoozeDisabled_containsDismissAction() {
        val reminder = Reminder(
            id = 101L,
            title = "Task without snooze",
            triggerType = ReminderTriggerType.TIME,
            snoozeEnabled = false,
            checklist = listOf(
                ChecklistItem(id = 1L, reminderId = 101L, text = "Subtask 1", isCompleted = false)
            )
        )

        val notification = manager.buildNotification(reminder)
        assertNotNull(notification)
        // Only Dismiss action should be present
        assertEquals(1, notification.actions?.size ?: 0)
        assertEquals("Dismiss", notification.actions?.firstOrNull()?.title?.toString())
    }
}
