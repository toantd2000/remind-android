package vn.io.litever.remind.core.alarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import vn.io.litever.remind.core.alarm.alert.ReminderAlertManager
import vn.io.litever.remind.core.alarm.notification.ReminderNotificationManager
import vn.io.litever.remind.core.domain.repository.ReminderRepository
import vn.io.litever.remind.core.domain.scheduler.ReminderScheduler
import vn.io.litever.remind.core.model.ChecklistItem
import vn.io.litever.remind.core.model.DayOfWeek
import vn.io.litever.remind.core.model.NotificationStyle
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.core.model.TimeTrigger
import java.time.LocalTime

@RunWith(RobolectricTestRunner::class)
class ReminderReceiverTest {

    private val context = mockk<Context>(relaxed = true)
    private val repository = mockk<ReminderRepository>(relaxed = true)
    private val scheduler = mockk<ReminderScheduler>(relaxed = true)
    private val notificationManager = mockk<ReminderNotificationManager>(relaxed = true)
    private val alertManager = mockk<ReminderAlertManager>(relaxed = true)

    private lateinit var receiver: ReminderReceiver

    @Before
    fun setup() {
        every { context.packageName } returns "vn.io.litever.remind"

        receiver = ReminderReceiver().apply {
            reminderRepository = repository
            reminderScheduler = scheduler
            reminderNotificationManager = notificationManager
            reminderAlertManager = alertManager
        }
        markHiltInjected(receiver)
    }

    /**
     * Bypasses automatic Hilt dependency injection in unit tests with mocked dependencies.
     */
    private fun markHiltInjected(receiver: BroadcastReceiver) {
        var clazz: Class<*>? = receiver.javaClass
        while (clazz != null && clazz != Any::class.java) {
            try {
                val field = clazz.getDeclaredField("injected")
                field.isAccessible = true
                field.set(receiver, true)
                return
            } catch (e: NoSuchFieldException) {
                clazz = clazz.superclass
            } catch (e: Exception) {
                return
            }
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun onReceive_triggerOneTimeReminder_marksCompletedAndShowsNotificationAndFiresAlert() = runBlocking {
        val reminder = Reminder(
            id = 10L,
            title = "One-time Reminder",
            triggerType = ReminderTriggerType.TIME,
            timeTrigger = TimeTrigger(time = LocalTime.of(9, 0), repeatDays = emptyList()),
            isEnabled = true,
            isCompleted = false,
            notificationStyle = NotificationStyle.STANDARD,
            isPersistentAlert = false
        )

        coEvery { repository.getReminderById(10L) } returns reminder

        val intent = Intent(ReminderReceiver.ACTION_TRIGGER_REMINDER).apply {
            putExtra(ReminderReceiver.EXTRA_REMINDER_ID, 10L)
            putExtra(ReminderReceiver.EXTRA_IS_SNOOZE, false)
        }

        receiver.onReceive(context, intent)
        Thread.sleep(100) // allow async coroutine to finish

        val updatedSlot = slot<Reminder>()
        coVerify(atLeast = 1) { repository.updateReminder(capture(updatedSlot)) }

        assertTrue(updatedSlot.captured.isCompleted)
        assertFalse(updatedSlot.captured.isEnabled)
        assertNotNull(updatedSlot.captured.lastTriggeredTime)

        verify(atLeast = 1) { notificationManager.showNotification(match { it.id == 10L }) }

        verify(atLeast = 1) {
            context.sendBroadcast(match {
                it.action == ReminderReceiver.ACTION_FIRE_REMINDER_ALERT &&
                        it.getLongExtra(ReminderReceiver.EXTRA_REMINDER_ID, -1L) == 10L
            })
        }
    }

    @Test
    fun onReceive_triggerPersistentReminder_startsAlertInAlertManager() = runBlocking {
        val reminder = Reminder(
            id = 15L,
            title = "Persistent Urgent",
            triggerType = ReminderTriggerType.TIME,
            timeTrigger = TimeTrigger(time = LocalTime.of(8, 0), repeatDays = emptyList()),
            isEnabled = true,
            isCompleted = false,
            notificationStyle = NotificationStyle.FULL_SCREEN,
            isPersistentAlert = true
        )

        coEvery { repository.getReminderById(15L) } returns reminder

        val intent = Intent(ReminderReceiver.ACTION_TRIGGER_REMINDER).apply {
            putExtra(ReminderReceiver.EXTRA_REMINDER_ID, 15L)
            putExtra(ReminderReceiver.EXTRA_IS_SNOOZE, false)
        }

        receiver.onReceive(context, intent)
        Thread.sleep(100)

        verify(atLeast = 1) { notificationManager.showNotification(match { it.id == 15L }) }
        verify(atLeast = 1) { alertManager.startAlert(match { it.id == 15L }) }
    }

    @Test
    fun onReceive_triggerRecurringReminder_reschedulesNextOccurrence() = runBlocking {
        val reminder = Reminder(
            id = 20L,
            title = "Daily Standup",
            triggerType = ReminderTriggerType.TIME,
            timeTrigger = TimeTrigger(time = LocalTime.of(9, 0), repeatDays = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY)),
            isEnabled = true,
            isCompleted = false
        )

        coEvery { repository.getReminderById(20L) } returns reminder

        val intent = Intent(ReminderReceiver.ACTION_TRIGGER_REMINDER).apply {
            putExtra(ReminderReceiver.EXTRA_REMINDER_ID, 20L)
            putExtra(ReminderReceiver.EXTRA_IS_SNOOZE, false)
        }

        receiver.onReceive(context, intent)
        Thread.sleep(100)

        val updatedSlot = slot<Reminder>()
        coVerify(atLeast = 1) { repository.updateReminder(capture(updatedSlot)) }
        assertFalse(updatedSlot.captured.isCompleted)
        assertTrue(updatedSlot.captured.isEnabled)

        verify(atLeast = 1) { scheduler.schedule(any()) }
        verify(atLeast = 1) { notificationManager.showNotification(match { it.id == 20L }) }
    }

    @Test
    fun onReceive_snoozeAction_stopsAlertCancelsNotificationIncrementsCountAndSchedulesSnooze() = runBlocking {
        val reminder = Reminder(
            id = 30L,
            title = "Snoozeable",
            triggerType = ReminderTriggerType.TIME,
            snoozeEnabled = true,
            snoozeIntervalMinutes = 10,
            snoozeRepeatCount = 3,
            currentSnoozeCount = 1,
            checklist = listOf(
                ChecklistItem(id = 1L, reminderId = 30L, text = "Check 1", isCompleted = true),
                ChecklistItem(id = 2L, reminderId = 30L, text = "Check 2", isCompleted = false)
            )
        )

        coEvery { repository.getReminderById(30L) } returns reminder

        val intent = Intent(ReminderReceiver.ACTION_REMINDER_SNOOZE).apply {
            putExtra(ReminderReceiver.EXTRA_REMINDER_ID, 30L)
        }

        receiver.onReceive(context, intent)
        Thread.sleep(100)

        verify(atLeast = 1) { alertManager.stopAlert(30L) }
        verify(atLeast = 1) { notificationManager.cancelNotification(30L) }

        val updatedSlot = slot<Reminder>()
        coVerify(atLeast = 1) { repository.updateReminder(capture(updatedSlot)) }

        assertEquals(2, updatedSlot.captured.currentSnoozeCount)
        assertNotNull(updatedSlot.captured.snoozeNextTriggerTime)
        assertEquals(2, updatedSlot.captured.checklist.size)
        assertTrue(updatedSlot.captured.checklist[0].isCompleted)
        assertFalse(updatedSlot.captured.checklist[1].isCompleted)

        verify(atLeast = 1) { scheduler.scheduleSnooze(any(), any()) }
    }

    @Test
    fun onReceive_snoozeAction_whenMaxSnoozeReached_doesNotScheduleSnooze() = runBlocking {
        val reminder = Reminder(
            id = 35L,
            title = "Max Snoozed",
            triggerType = ReminderTriggerType.TIME,
            snoozeEnabled = true,
            snoozeIntervalMinutes = 10,
            snoozeRepeatCount = 2,
            currentSnoozeCount = 2
        )

        coEvery { repository.getReminderById(35L) } returns reminder

        val intent = Intent(ReminderReceiver.ACTION_REMINDER_SNOOZE).apply {
            putExtra(ReminderReceiver.EXTRA_REMINDER_ID, 35L)
        }

        receiver.onReceive(context, intent)
        Thread.sleep(100)

        verify(atLeast = 1) { alertManager.stopAlert(35L) }
        verify(atLeast = 1) { notificationManager.cancelNotification(35L) }

        coVerify(exactly = 0) { repository.updateReminder(match { it.id == 35L && it.currentSnoozeCount > 2 }) }
        verify(exactly = 0) { scheduler.scheduleSnooze(any(), any()) }
    }

    @Test
    fun onReceive_dismissAction_stopsAlertCancelsNotificationAndCancelsSnooze() = runBlocking {
        val reminder = Reminder(
            id = 40L,
            title = "Dismiss Me",
            triggerType = ReminderTriggerType.TIME,
            timeTrigger = TimeTrigger(time = LocalTime.of(12, 0), repeatDays = emptyList()),
            isEnabled = true,
            isCompleted = false
        )

        coEvery { repository.getReminderById(40L) } returns reminder

        val intent = Intent(ReminderReceiver.ACTION_REMINDER_DISMISS).apply {
            putExtra(ReminderReceiver.EXTRA_REMINDER_ID, 40L)
        }

        receiver.onReceive(context, intent)
        Thread.sleep(100)

        verify(atLeast = 1) { alertManager.stopAlert(40L) }
        verify(atLeast = 1) { notificationManager.cancelNotification(40L) }

        val updatedSlot = slot<Reminder>()
        coVerify(atLeast = 1) { repository.updateReminder(capture(updatedSlot)) }

        assertTrue(updatedSlot.captured.isCompleted)
        assertNull(updatedSlot.captured.snoozeNextTriggerTime)
        verify(atLeast = 1) { scheduler.cancelSnooze(reminder) }
    }

    @Test
    fun onReceive_checklistToggle_updatesItemStatusAndRefreshesNotification() = runBlocking {
        val reminder = Reminder(
            id = 50L,
            title = "Checklist Reminder",
            triggerType = ReminderTriggerType.TIME,
            checklist = listOf(
                ChecklistItem(id = 1L, reminderId = 50L, text = "Item 1", isCompleted = false)
            )
        )
        val refreshedReminder = reminder.copy(
            checklist = listOf(
                ChecklistItem(id = 1L, reminderId = 50L, text = "Item 1", isCompleted = true)
            )
        )

        coEvery { repository.getReminderById(50L) } returnsMany listOf(reminder, refreshedReminder)

        val intent = Intent(ReminderReceiver.ACTION_REMINDER_CHECKLIST_TOGGLE).apply {
            putExtra(ReminderReceiver.EXTRA_REMINDER_ID, 50L)
            putExtra(ReminderReceiver.EXTRA_CHECKLIST_ITEM_ID, 1L)
            putExtra(ReminderReceiver.EXTRA_CHECKLIST_IS_COMPLETED, true)
        }

        receiver.onReceive(context, intent)
        Thread.sleep(100)

        coVerify(atLeast = 1) { repository.updateChecklistItemCompletion(1L, true) }
        verify(atLeast = 1) { notificationManager.updateNotification(match { it.id == 50L && it.checklist[0].isCompleted }) }
    }
}
