package vn.io.litever.remind.core.alarm.reminder

import org.robolectric.RobolectricTestRunner
import org.junit.runner.RunWith

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import vn.io.litever.remind.core.domain.scheduler.ReminderScheduler
import vn.io.litever.remind.core.model.LocationTrigger
import vn.io.litever.remind.core.model.NotificationStyle
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.core.model.TimeTrigger
import java.time.LocalDate
import java.time.LocalTime

@RunWith(RobolectricTestRunner::class)
class ReminderSchedulerImplTest {

    private val context = mockk<Context>(relaxed = true)
    private val alarmManager = mockk<AlarmManager>(relaxed = true)
    private val pendingIntent = mockk<PendingIntent>(relaxed = true)

    private lateinit var scheduler: ReminderSchedulerImpl

    @Before
    fun setup() {
        mockkStatic(PendingIntent::class)
        every { PendingIntent.getBroadcast(any(), any(), any(), any()) } returns pendingIntent
        every { PendingIntent.getActivity(any(), any(), any(), any()) } returns pendingIntent
        every { context.getSystemService(Context.ALARM_SERVICE) } returns alarmManager
        every { alarmManager.canScheduleExactAlarms() } returns true

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
            triggerType = ReminderTriggerType.TIME,
            timeTrigger = TimeTrigger(time = LocalTime.of(10, 0), date = futureDate),
            notificationStyle = NotificationStyle.STANDARD
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
            triggerType = ReminderTriggerType.TIME,
            timeTrigger = TimeTrigger(time = LocalTime.of(14, 0), date = futureDate),
            notificationStyle = NotificationStyle.FULL_SCREEN
        )

        scheduler.schedule(reminder)

        verify(atLeast = 1) {
            alarmManager.setAlarmClock(any(), eq(pendingIntent))
        }
    }

    @Test
    fun schedule_disabledOrCompletedReminder_doesNotSchedule() {
        val disabled = Reminder(
            id = 103L,
            title = "Disabled",
            isEnabled = false,
            triggerType = ReminderTriggerType.TIME,
            timeTrigger = TimeTrigger(time = LocalTime.of(10, 0))
        )
        val completed = Reminder(
            id = 104L,
            title = "Completed",
            isCompleted = true,
            triggerType = ReminderTriggerType.TIME,
            timeTrigger = TimeTrigger(time = LocalTime.of(10, 0))
        )

        scheduler.schedule(disabled)
        scheduler.schedule(completed)

        verify(exactly = 0) {
            alarmManager.setAlarmClock(any(), any())
            alarmManager.setExactAndAllowWhileIdle(any(), any(), any())
        }
    }

    @Test
    fun schedule_locationOrWifiReminder_ignoredByTimeScheduler() {
        val locReminder = Reminder(
            id = 105L,
            title = "Location",
            triggerType = ReminderTriggerType.LOCATION,
            locationTrigger = LocationTrigger(latitude = 21.0, longitude = 105.0, radiusMeters = 100f)
        )

        scheduler.schedule(locReminder)

        verify(exactly = 0) {
            alarmManager.setAlarmClock(any(), any())
            alarmManager.setExactAndAllowWhileIdle(any(), any(), any())
        }
    }

    @Test
    fun scheduleSnooze_futureEpoch_schedulesSnoozeAlarm() {
        val reminder = Reminder(
            id = 201L,
            title = "Snooze Test",
            triggerType = ReminderTriggerType.TIME
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
        val reminder = Reminder(id = 301L, title = "Cancel Me", triggerType = ReminderTriggerType.TIME)

        scheduler.cancel(reminder)
        scheduler.cancelSnooze(reminder)

        verify(atLeast = 2) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    @Test
    fun cancelAll_cancelsBothPrimaryAndSnooze() {
        val reminder = Reminder(id = 302L, title = "Cancel All", triggerType = ReminderTriggerType.TIME)

        scheduler.cancelAll(reminder)

        verify(atLeast = 2) {
            alarmManager.cancel(pendingIntent)
        }
    }
}
