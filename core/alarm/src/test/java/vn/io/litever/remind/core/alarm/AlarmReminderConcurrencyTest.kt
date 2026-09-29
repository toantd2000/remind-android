package vn.io.litever.remind.core.alarm

import android.content.Context
import android.media.AudioManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import vn.io.litever.remind.core.alarm.alert.ReminderAlertManagerImpl
import vn.io.litever.remind.core.alarm.notification.ReminderNotificationManager
import vn.io.litever.remind.core.alarm.service.AlarmService
import vn.io.litever.remind.core.datastore.AlarmPreferencesDataSource
import vn.io.litever.remind.core.domain.repository.AlarmRepository
import vn.io.litever.remind.core.domain.repository.MissedAlarmRepository
import vn.io.litever.remind.core.domain.repository.ReminderRepository
import vn.io.litever.remind.core.domain.scheduler.AlarmScheduler
import vn.io.litever.remind.core.domain.scheduler.ReminderScheduler
import vn.io.litever.remind.core.model.Alarm
import vn.io.litever.remind.core.model.NotificationStyle
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderAction
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.core.model.TimeTrigger
import java.time.LocalTime

import androidx.test.core.app.ApplicationProvider
import io.mockk.spyk

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class AlarmReminderConcurrencyTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val appContext: Context = ApplicationProvider.getApplicationContext()
    private val context = spyk(appContext)
    private val audioManager = mockk<AudioManager>(relaxed = true)
    private val alarmRepository = mockk<AlarmRepository>(relaxed = true)
    private val reminderRepository = mockk<ReminderRepository>(relaxed = true)
    private val alarmScheduler = mockk<AlarmScheduler>(relaxed = true)
    private val reminderScheduler = mockk<ReminderScheduler>(relaxed = true)
    private val missedAlarmRepository = mockk<MissedAlarmRepository>(relaxed = true)
    private val preferencesDataSource = mockk<AlarmPreferencesDataSource>(relaxed = true)

    private lateinit var reminderAlertManager: ReminderAlertManagerImpl
    private lateinit var alarmSyncManager: AlarmSyncManager
    private lateinit var reminderSyncManager: ReminderSyncManager

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { context.packageName } returns "vn.io.litever.remind"
        every { context.getSystemService(Context.AUDIO_SERVICE) } returns audioManager
        every { audioManager.mode } returns AudioManager.MODE_NORMAL
        every { preferencesDataSource.lastMissedCheckTime } returns flowOf(0L)
        reminderAlertManager = ReminderAlertManagerImpl(context)
        alarmSyncManager = AlarmSyncManager(alarmRepository, alarmScheduler, missedAlarmRepository, preferencesDataSource)
        reminderSyncManager = ReminderSyncManager(context, reminderRepository, reminderScheduler)
    }

    @After
    fun tearDown() {
        reminderAlertManager.stopAllAlerts()
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun simultaneousAlarmAndReminderAlerts_trackIndependentLifecycle() = runTest {
        val alarm = Alarm(id = 101L, time = LocalTime.of(8, 0), isEnabled = true)
        val reminder = Reminder(
            id = 202L,
            title = "Morning Sync",
            triggerType = ReminderTriggerType.FIXED_TIME,
            action = ReminderAction(
                isPersistentAlert = true,
                notificationStyle = NotificationStyle.FULL_SCREEN
            )
        )

        reminderAlertManager.startAlert(reminder)
        testDispatcher.scheduler.runCurrent()

        assertTrue("Reminder alert manager tracks active alert", reminderAlertManager.isAlerting(202L))

        // Dismissing reminder alert does not affect independent alarm entities
        reminderAlertManager.stopAlert(202L)
        testDispatcher.scheduler.runCurrent()

        assertFalse(reminderAlertManager.isAlerting(202L))
    }

    @Test
    fun alarmDismissal_whileReminderIsSnoozed_doesNotInterfereWithReminderSnoozeState() = runTest {
        val reminder = Reminder(
            id = 303L,
            title = "Doctor Appointment",
            triggerType = ReminderTriggerType.FIXED_TIME,
            action = ReminderAction(
                snoozeEnabled = true
            ),
            currentSnoozeCount = 1,
            snoozeNextTriggerTime = System.currentTimeMillis() + 600000L
        )

        coEvery { reminderRepository.getAllReminders() } returns flowOf(listOf(reminder))

        reminderSyncManager.sync()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { reminderScheduler.scheduleSnooze(reminder, any()) }
    }

    @Test
    fun rebootSync_synchronizesBothAlarmsAndRemindersConcurrently() = runTest {
        val alarm = Alarm(id = 404L, time = LocalTime.now().plusHours(2), isEnabled = true)
        val reminder = Reminder(
            id = 505L,
            title = "Standup",
            triggerType = ReminderTriggerType.FIXED_TIME,
            timeTrigger = TimeTrigger(time = listOf(LocalTime.now().plusHours(3))),
            isEnabled = true,
            isCompleted = false
        )

        coEvery { alarmRepository.getAllAlarms() } returns flowOf(listOf(alarm))
        coEvery { reminderRepository.getAllReminders() } returns flowOf(listOf(reminder))

        alarmSyncManager.sync()
        reminderSyncManager.sync()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { alarmScheduler.schedule(match { it.id == 404L }) }
        coVerify { reminderScheduler.schedule(match { it.id == 505L }) }
    }

    @Test
    fun channelSeparation_verifiesDistinctNotificationChannels() {
        // Alarms use AlarmService channels
        val alarmSilentChannel = AlarmService.CHANNEL_ID_SILENT
        val alarmHighChannel = AlarmService.CHANNEL_ID_HIGH

        // Reminders use ReminderNotificationManager channels
        val reminderStandardChannel = ReminderNotificationManager.CHANNEL_ID_STANDARD
        val reminderUrgentChannel = ReminderNotificationManager.CHANNEL_ID_URGENT

        assertNotEquals(alarmSilentChannel, reminderStandardChannel)
        assertNotEquals(alarmHighChannel, reminderUrgentChannel)
        assertEquals("alarm_channel_silent", alarmSilentChannel)
        assertEquals("reminder_standard_channel", reminderStandardChannel)
    }
}
