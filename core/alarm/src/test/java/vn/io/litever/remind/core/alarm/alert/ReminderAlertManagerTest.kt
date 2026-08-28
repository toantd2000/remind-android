package vn.io.litever.remind.core.alarm.alert

import org.robolectric.RobolectricTestRunner
import org.junit.runner.RunWith

import android.content.Context
import android.media.AudioManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import vn.io.litever.remind.core.model.NotificationStyle
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderTriggerType

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

import androidx.test.core.app.ApplicationProvider
import io.mockk.spyk

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class ReminderAlertManagerTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val appContext: Context = ApplicationProvider.getApplicationContext()
    private val context = spyk(appContext)
    private val audioManager = mockk<AudioManager>(relaxed = true)

    private lateinit var alertManager: ReminderAlertManagerImpl

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { context.getSystemService(Context.AUDIO_SERVICE) } returns audioManager
        every { audioManager.mode } returns AudioManager.MODE_NORMAL

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
            triggerType = ReminderTriggerType.TIME,
            isPersistentAlert = true,
            volume = 80,
            vibrationEnabled = true
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
            triggerType = ReminderTriggerType.TIME,
            notificationStyle = NotificationStyle.FULL_SCREEN,
            isPersistentAlert = true
        )

        alertManager.startAlert(reminder)
        testScheduler.runCurrent()

        val state = alertManager.playbackState.value
        assertTrue(state is AlertPlaybackState.Playing)
        val playingState = state as AlertPlaybackState.Playing
        assertTrue(playingState.isPersistent)
    }

    @Test
    fun startAlert_standardNotificationStyle_nonPersistent() = runTest {
        val reminder = Reminder(
            id = 102L,
            title = "Standard Style",
            triggerType = ReminderTriggerType.TIME,
            notificationStyle = NotificationStyle.STANDARD,
            isPersistentAlert = false
        )

        alertManager.startAlert(reminder)
        testScheduler.runCurrent()

        val state = alertManager.playbackState.value
        assertTrue(state is AlertPlaybackState.Playing)
        val playingState = state as AlertPlaybackState.Playing
        assertFalse(playingState.isPersistent)
    }

    @Test
    fun stopAlert_removesFromQueueAndSetsIdle() = runTest {
        val reminder = Reminder(
            id = 103L,
            title = "Meeting",
            triggerType = ReminderTriggerType.TIME,
            isPersistentAlert = true
        )

        alertManager.startAlert(reminder)
        testScheduler.runCurrent()
        assertTrue(alertManager.isAlerting(103L))

        alertManager.stopAlert(103L)
        testScheduler.runCurrent()

        assertFalse(alertManager.isAlerting(103L))
        assertEquals(AlertPlaybackState.Idle, alertManager.playbackState.value)
    }

    @Test
    fun stopAllAlerts_clearsAllActiveAlerts() = runTest {
        val reminder1 = Reminder(id = 104L, title = "Alert 1", triggerType = ReminderTriggerType.TIME, isPersistentAlert = true)
        val reminder2 = Reminder(id = 105L, title = "Alert 2", triggerType = ReminderTriggerType.TIME, isPersistentAlert = true)

        alertManager.startAlert(reminder1)
        alertManager.startAlert(reminder2)
        testScheduler.runCurrent()

        assertTrue(alertManager.isAlerting(104L))
        assertTrue(alertManager.isAlerting(105L))

        alertManager.stopAllAlerts()
        testScheduler.runCurrent()

        assertFalse(alertManager.isAlerting(104L))
        assertFalse(alertManager.isAlerting(105L))
        assertEquals(AlertPlaybackState.Idle, alertManager.playbackState.value)
    }

    @Test
    fun muteAndUnmute_controlsAlertPlayback() = runTest {
        val reminder = Reminder(id = 106L, title = "Mute Test", triggerType = ReminderTriggerType.TIME, isPersistentAlert = true)

        alertManager.startAlert(reminder)
        testScheduler.runCurrent()
        assertTrue(alertManager.isAlerting(106L))

        alertManager.mute(106L)
        testScheduler.runCurrent()
        // Alert is still in queue
        assertTrue(alertManager.isAlerting(106L))

        alertManager.unmute(106L)
        testScheduler.runCurrent()
        assertTrue(alertManager.isAlerting(106L))
    }

    @Test
    fun autoSilence_countdownTicksAndInvokesTimeoutCallback() = runTest {
        var timedOutReminderId: Long? = null
        alertManager.setAutoSilenceListener { id -> timedOutReminderId = id }

        val reminder = Reminder(id = 107L, title = "Unattended", triggerType = ReminderTriggerType.TIME, isPersistentAlert = true)
        alertManager.startAlert(reminder)
        testScheduler.advanceUntilIdle()

        // Advance 5 minutes + 1 second
        testScheduler.advanceTimeBy(ReminderAlertManagerImpl.DEFAULT_AUTO_SILENCE_MINUTES * 60 * 1000L + 1000L)
        testScheduler.advanceUntilIdle()

        assertEquals(107L, timedOutReminderId)
        assertFalse(alertManager.isAlerting(107L))
        assertEquals(AlertPlaybackState.Idle, alertManager.playbackState.value)
    }
}
