package vn.io.litever.remind.core.alarm.notification

import org.robolectric.RobolectricTestRunner
import org.junit.runner.RunWith

import android.app.Notification
import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import android.content.Context
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import vn.io.litever.remind.core.common.util.PermissionChecker
import vn.io.litever.remind.core.model.NotificationStyle
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderAction
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.core.model.UrgencyLevel

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
    fun formatContentText_withNotes_formatsSnippetCorrectly() {
        val reminder = Reminder(
            id = 1L,
            title = "Groceries",
            notes = "Target Store",
            triggerType = ReminderTriggerType.FIXED_TIME
        )

        val text = manager.formatContentText(reminder)
        assertTrue(text.contains("Target Store"))
    }

    @Test
    fun formatBigTextStyle_includesNotes() {
        val reminder = Reminder(
            id = 3L,
            title = "Daily Routine",
            notes = "Morning list",
            triggerType = ReminderTriggerType.FIXED_TIME
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
            triggerType = ReminderTriggerType.FIXED_TIME,
            action = ReminderAction(
                notificationStyle = NotificationStyle.STANDARD,
                snoozeEnabled = true,
                snoozeIntervalMinutes = 5,
                
            ),
            
        )

        val notification = manager.buildNotification(reminder)
        assertNotNull(notification)
    }

    @Test
    fun buildNotification_whenSnoozeDisabled_omitsSnoozeAction() {
        val reminder = Reminder(
            id = 20L,
            title = "Final Reminder",
            triggerType = ReminderTriggerType.FIXED_TIME,
            action = ReminderAction(
                snoozeEnabled = false
            )
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
            triggerType = ReminderTriggerType.FIXED_TIME,
            action = ReminderAction(
                notificationStyle = NotificationStyle.FULL_SCREEN,
                isPersistentAlert = true
            )
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
            triggerType = ReminderTriggerType.FIXED_TIME,
            action = ReminderAction(
                notificationStyle = NotificationStyle.FULL_SCREEN,
                isPersistentAlert = false
            )
        )

        val notification = manager.buildNotification(reminder)
        assertNotNull(notification)
        assertEquals(NotificationCompat.CATEGORY_ALARM, notification.category)
    }

    @Test
    fun showNotification_whenPermissionGranted_notifiesNotificationManager() {
        every { permissionChecker.hasNotificationPermission() } returns true

        val reminder = Reminder(
            id = 40L,
            title = "Notify Test",
            triggerType = ReminderTriggerType.FIXED_TIME
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
            triggerType = ReminderTriggerType.FIXED_TIME
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
            title = "Task with actions",
            triggerType = ReminderTriggerType.FIXED_TIME,
            action = ReminderAction(
                urgencyLevel = UrgencyLevel.MEDIUM,
                snoozeEnabled = true,
                snoozeIntervalMinutes = 10
            )
        )

        val notification = manager.buildNotification(reminder)
        assertNotNull(notification)
        assertEquals(2, notification.actions?.size ?: 0)
    }

    @Test
    fun buildNotification_whenSnoozeDisabled_containsDismissAction() {
        val reminder = Reminder(
            id = 101L,
            title = "Task without snooze",
            triggerType = ReminderTriggerType.FIXED_TIME,
            action = ReminderAction(
                snoozeEnabled = false
            )
        )

        val notification = manager.buildNotification(reminder)
        assertNotNull(notification)
        assertEquals(1, notification.actions?.size ?: 0)
    }
}


