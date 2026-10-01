package vn.io.litever.remind.core.alarm

import android.content.Context
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import vn.io.litever.remind.core.alarm.service.ReminderMonitorService
import vn.io.litever.remind.core.domain.repository.ReminderRepository
import vn.io.litever.remind.core.domain.scheduler.ReminderScheduler
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.core.model.RepeatMode
import vn.io.litever.remind.core.model.TimeTrigger
import java.time.LocalDate
import java.time.LocalTime

class ReminderSyncManagerTest {

    private val context = mockk<Context>(relaxed = true)
    private val repository = mockk<ReminderRepository>(relaxed = true)
    private val scheduler = mockk<ReminderScheduler>(relaxed = true)

    private lateinit var syncManager: ReminderSyncManager

    @Before
    fun setup() {
        mockkObject(ReminderMonitorService.Companion)
        every { ReminderMonitorService.sync(any()) } returns Unit
        every { ReminderMonitorService.stop(any()) } returns Unit

        syncManager = ReminderSyncManager(context, repository, scheduler)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun sync_futureTimeReminder_schedulesWithScheduler() = runBlocking {
        val futureDate = LocalDate.now().plusDays(1)
        val reminder = Reminder(
            id = 1L,
            title = "Meeting",
            triggerType = ReminderTriggerType.FIXED_TIME,
            timeTrigger = TimeTrigger(time = listOf(LocalTime.of(10, 0)), exactDate = futureDate, repeatMode = RepeatMode.NONE),
            isEnabled = true
        )

        coEvery { repository.getAllReminders() } returns flowOf(listOf(reminder))

        syncManager.sync()

        verify(atLeast = 1) { scheduler.schedule(reminder) }
    }

    @Test
    fun sync_activeSnooze_schedulesSnooze() = runBlocking {
        val futureSnooze = System.currentTimeMillis() + 300000L
        val reminder = Reminder(
            id = 2L,
            title = "Snooze Sync",
            triggerType = ReminderTriggerType.FIXED_TIME,
            snoozeNextTriggerTime = futureSnooze,
            isEnabled = true
        )

        coEvery { repository.getAllReminders() } returns flowOf(listOf(reminder))

        syncManager.sync()

        verify(atLeast = 1) { scheduler.scheduleSnooze(reminder, futureSnooze) }
    }

    @Test
    fun sync_intervalReminder_schedulesWithScheduler() = runBlocking {
        val reminder = Reminder(
            id = 3L,
            title = "Hydration Interval",
            triggerType = ReminderTriggerType.INTERVAL,
            timeTrigger = TimeTrigger(intervalMinutes = 120),
            isEnabled = true
        )

        coEvery { repository.getAllReminders() } returns flowOf(listOf(reminder))

        syncManager.sync()

        verify(atLeast = 1) { scheduler.schedule(reminder) }
    }
}
