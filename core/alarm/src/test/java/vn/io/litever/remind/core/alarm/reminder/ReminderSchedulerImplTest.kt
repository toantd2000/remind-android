package vn.io.litever.remind.core.alarm.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import vn.io.litever.remind.core.alarm.receiver.ReminderReceiver
import vn.io.litever.remind.core.domain.scheduler.ReminderScheduler
import vn.io.litever.remind.core.model.NotificationStyle
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderAction
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.core.model.RepeatMode
import vn.io.litever.remind.core.model.TimeTrigger
import java.time.LocalDate
import java.time.LocalTime

import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ReminderSchedulerImplTest {

    private val context = mockk<Context>(relaxed = true)
    private val alarmManager = mockk<AlarmManager>(relaxed = true)
    private val pendingIntent = mockk<PendingIntent>(relaxed = true)

    private lateinit var scheduler: ReminderSchedulerImpl

    @Before
    fun setup() {
        every { context.getSystemService(Context.ALARM_SERVICE) } returns alarmManager
        every { context.packageName } returns "vn.io.litever.remind"
        every { alarmManager.canScheduleExactAlarms() } returns true

        mockkStatic(PendingIntent::class)
        every {
            PendingIntent.getBroadcast(
                any(),
                any(),
                any(),
                any()
            )
        } returns pendingIntent

        scheduler = ReminderSchedulerImpl(context)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun schedule_futureTimeReminder_schedulesAlarm() {
        val futureDate = LocalDate.now().plusDays(1)
        val reminder = Reminder(
            id = 101L,
            title = "Doctor",
            triggerType = ReminderTriggerType.FIXED_TIME,
            timeTrigger = TimeTrigger(time = listOf(LocalTime.of(10, 0)), exactDate = futureDate, repeatMode = RepeatMode.NONE),
            action = ReminderAction(notificationStyle = NotificationStyle.STANDARD)
        )

        scheduler.schedule(reminder)

        verify(atLeast = 1) {
            PendingIntent.getBroadcast(
                context,
                any(),
                match { intent ->
                    intent.action == ReminderScheduler.ACTION_TRIGGER_REMINDER &&
                            intent.getLongExtra(ReminderScheduler.EXTRA_REMINDER_ID, -1L) == 101L
                },
                any()
            )
        }
    }

    @Test
    fun schedule_fullScreenStyle_usesAlarmClock() {
        val futureDate = LocalDate.now().plusDays(1)
        val reminder = Reminder(
            id = 102L,
            title = "Urgent Call",
            triggerType = ReminderTriggerType.FIXED_TIME,
            timeTrigger = TimeTrigger(time = listOf(LocalTime.of(14, 0)), exactDate = futureDate, repeatMode = RepeatMode.NONE),
            action = ReminderAction(notificationStyle = NotificationStyle.FULL_SCREEN)
        )

        scheduler.schedule(reminder)

        verify(atLeast = 1) {
            alarmManager.setAlarmClock(any(), eq(pendingIntent))
        }
    }

    @Test
    fun schedule_disabledReminder_doesNotSchedule() {
        val disabled = Reminder(
            id = 103L,
            title = "Disabled",
            isEnabled = false,
            triggerType = ReminderTriggerType.FIXED_TIME,
            timeTrigger = TimeTrigger(time = listOf(LocalTime.of(10, 0)))
        )

        scheduler.schedule(disabled)

        verify(exactly = 0) {
            alarmManager.setAlarmClock(any(), any())
            alarmManager.setExactAndAllowWhileIdle(any(), any(), any())
        }
    }

    @Test
    fun schedule_intervalReminder_schedulesAlarm() {
        val intervalReminder = Reminder(
            id = 105L,
            title = "Drink Water",
            triggerType = ReminderTriggerType.INTERVAL,
            timeTrigger = TimeTrigger(intervalMinutes = 60),
            action = ReminderAction(notificationStyle = NotificationStyle.STANDARD)
        )

        scheduler.schedule(intervalReminder)

        verify(atLeast = 1) {
            alarmManager.setExactAndAllowWhileIdle(any(), any(), eq(pendingIntent))
        }
    }

    @Test
    fun scheduleSnooze_futureEpoch_schedulesSnoozeAlarm() {
        val reminder = Reminder(
            id = 201L,
            title = "Snooze Test",
            triggerType = ReminderTriggerType.FIXED_TIME
        )
        val futureTime = System.currentTimeMillis() + 600000L

        scheduler.scheduleSnooze(reminder, futureTime)

        verify(atLeast = 1) {
            PendingIntent.getBroadcast(
                context,
                any(),
                match { intent ->
                    intent.action == ReminderScheduler.ACTION_TRIGGER_REMINDER_SNOOZE &&
                            intent.getBooleanExtra(ReminderScheduler.EXTRA_IS_SNOOZE, false)
                },
                any()
            )
        }
    }

    @Test
    fun cancel_and_cancelSnooze_cancelsPendingIntents() {
        val reminder = Reminder(id = 301L, title = "Cancel Me", triggerType = ReminderTriggerType.FIXED_TIME)

        scheduler.cancel(reminder)
        scheduler.cancelSnooze(reminder)

        verify(atLeast = 2) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    @Test
    fun cancelAll_cancelsBothPrimaryAndSnooze() {
        val reminder = Reminder(id = 302L, title = "Cancel All", triggerType = ReminderTriggerType.FIXED_TIME)

        scheduler.cancelAll(reminder)

        verify(atLeast = 2) {
            alarmManager.cancel(pendingIntent)
        }
    }
}
