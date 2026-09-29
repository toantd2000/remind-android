package vn.io.litever.remind.core.alarm.alert

import android.content.Context
import android.media.AudioManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import vn.io.litever.remind.core.model.NotificationStyle
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderAction
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.core.model.UrgencyLevel

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class ReminderAlertManagerTest {

    private val context = mockk<Context>(relaxed = true)
    private val audioManager = mockk<AudioManager>(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var alertManager: ReminderAlertManagerImpl

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { context.getSystemService(Context.AUDIO_SERVICE) } returns audioManager
        every { context.packageName } returns "vn.io.litever.remind"

        alertManager = ReminderAlertManagerImpl(context)
    }

    @After
    fun tearDown() {
        alertManager.stopAllAlerts()
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun startAlert_persistentAlert_updatesPlaybackStateToPlaying() = runTest {
        val reminder = Reminder(
            id = 101L,
            title = "Urgent Medication",
            triggerType = ReminderTriggerType.FIXED_TIME,
            action = ReminderAction(
                urgencyLevel = UrgencyLevel.HARD,
                isPersistentAlert = true,
                volume = 80,
                vibrationEnabled = true
            )
        )

        alertManager.startAlert(reminder)
        testScheduler.runCurrent()

        val state = alertManager.playbackState.value
        assertTrue(state is AlertPlaybackState.Playing)
        val playingState = state as AlertPlaybackState.Playing
        assertEquals(101L, playingState.reminderId)
        assertTrue(playingState.isPersistent)
        assertEquals(80, playingState.volume)
        assertTrue(playingState.vibrationEnabled)
        assertTrue(alertManager.isAlerting(101L))
    }

    @Test
    fun startAlert_fullScreenNotificationStyle_withPersistentAlert_treatedAsPersistent() = runTest {
        val reminder = Reminder(
            id = 102L,
            title = "Alarm Style",
            triggerType = ReminderTriggerType.FIXED_TIME,
            action = ReminderAction(
                urgencyLevel = UrgencyLevel.HARD,
                notificationStyle = NotificationStyle.FULL_SCREEN,
                isPersistentAlert = true
            )
        )

        alertManager.startAlert(reminder)
        testScheduler.runCurrent()

        val state = alertManager.playbackState.value
        assertTrue(state is AlertPlaybackState.Playing)
        val playingState = state as AlertPlaybackState.Playing
        assertTrue(playingState.isPersistent)
    }

    @Test
    fun startAlert_softReminder_ignoredByAlertManager() = runTest {
        val reminder = Reminder(
            id = 103L,
            title = "Soft Hydration",
            triggerType = ReminderTriggerType.INTERVAL,
            action = ReminderAction(
                urgencyLevel = UrgencyLevel.SOFT
            )
        )

        alertManager.startAlert(reminder)
        testScheduler.runCurrent()

        assertEquals(AlertPlaybackState.Idle, alertManager.playbackState.value)
        assertFalse(alertManager.isAlerting(103L))
    }

    @Test
    fun stopAlert_removesFromQueueAndSetsIdle() = runTest {
        val reminder = Reminder(
            id = 104L,
            title = "Meeting",
            triggerType = ReminderTriggerType.FIXED_TIME,
            action = ReminderAction(
                urgencyLevel = UrgencyLevel.MEDIUM,
                isPersistentAlert = true
            )
        )

        alertManager.startAlert(reminder)
        testScheduler.runCurrent()
        assertTrue(alertManager.isAlerting(104L))

        alertManager.stopAlert(104L)
        testScheduler.runCurrent()

        assertFalse(alertManager.isAlerting(104L))
        assertEquals(AlertPlaybackState.Idle, alertManager.playbackState.value)
    }

    @Test
    fun stopAllAlerts_clearsAllActiveAlerts() = runTest {
        val reminder1 = Reminder(
            id = 105L,
            title = "Alert 1",
            triggerType = ReminderTriggerType.FIXED_TIME,
            action = ReminderAction(urgencyLevel = UrgencyLevel.MEDIUM, isPersistentAlert = true)
        )
        val reminder2 = Reminder(
            id = 106L,
            title = "Alert 2",
            triggerType = ReminderTriggerType.FIXED_TIME,
            action = ReminderAction(urgencyLevel = UrgencyLevel.MEDIUM, isPersistentAlert = true)
        )

        alertManager.startAlert(reminder1)
        alertManager.startAlert(reminder2)
        testScheduler.runCurrent()

        assertTrue(alertManager.isAlerting(105L))
        assertTrue(alertManager.isAlerting(106L))

        alertManager.stopAllAlerts()
        testScheduler.runCurrent()

        assertFalse(alertManager.isAlerting(105L))
        assertFalse(alertManager.isAlerting(106L))
        assertEquals(AlertPlaybackState.Idle, alertManager.playbackState.value)
    }
}
