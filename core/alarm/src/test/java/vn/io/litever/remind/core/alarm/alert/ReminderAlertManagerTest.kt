package vn.io.litever.remind.core.alarm.alert

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

@OptIn(ExperimentalCoroutinesApi::class)
class ReminderAlertManagerTest {

    private val context = mockk<Context>(relaxed = true)
    private val audioManager = mockk<AudioManager>(relaxed = true)

    private lateinit var alertManager: ReminderAlertManagerImpl

    @Before
    fun setup() {
        every { context.packageName } returns "vn.io.litever.remind"
        every { context.getSystemService(Context.AUDIO_SERVICE) } returns audioManager
        every { audioManager.mode } returns AudioManager.MODE_NORMAL

        alertManager = ReminderAlertManagerImpl(context)
    }

    @After
    fun tearDown() {
        alertManager.stopAllAlerts()
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
        testScheduler.advanceUntilIdle()

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
    fun startAlert_fullScreenNotificationStyle_treatedAsPersistent() = runTest {
        val reminder = Reminder(
            id = 102L,
            title = "Alarm Style",
            triggerType = ReminderTriggerType.TIME,
            notificationStyle = NotificationStyle.FULL_SCREEN,
            isPersistentAlert = false
        )

        alertManager.startAlert(reminder)
        testScheduler.advanceUntilIdle()

        val state = alertManager.playbackState.value
        assertTrue(state is AlertPlaybackState.Playing)
        val playingState = state as AlertPlaybackState.Playing
        assertTrue(playingState.isPersistent)
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
        testScheduler.advanceUntilIdle()
        assertTrue(alertManager.isAlerting(103L))

        alertManager.stopAlert(103L)
        testScheduler.advanceUntilIdle()

        assertFalse(alertManager.isAlerting(103L))
        assertEquals(AlertPlaybackState.Idle, alertManager.playbackState.value)
    }

    @Test
    fun stopAllAlerts_clearsAllActiveAlerts() = runTest {
        val reminder1 = Reminder(id = 104L, title = "Alert 1", triggerType = ReminderTriggerType.TIME, isPersistentAlert = true)
        val reminder2 = Reminder(id = 105L, title = "Alert 2", triggerType = ReminderTriggerType.TIME, isPersistentAlert = true)

        alertManager.startAlert(reminder1)
        alertManager.startAlert(reminder2)
        testScheduler.advanceUntilIdle()

        assertTrue(alertManager.isAlerting(104L))
        assertTrue(alertManager.isAlerting(105L))

        alertManager.stopAllAlerts()
        testScheduler.advanceUntilIdle()

        assertFalse(alertManager.isAlerting(104L))
        assertFalse(alertManager.isAlerting(105L))
        assertEquals(AlertPlaybackState.Idle, alertManager.playbackState.value)
    }

    @Test
    fun muteAndUnmute_controlsAlertPlayback() = runTest {
        val reminder = Reminder(id = 106L, title = "Mute Test", triggerType = ReminderTriggerType.TIME, isPersistentAlert = true)

        alertManager.startAlert(reminder)
        testScheduler.advanceUntilIdle()
        assertTrue(alertManager.isAlerting(106L))

        alertManager.mute(106L)
        testScheduler.advanceUntilIdle()
        // Alert is still in queue
        assertTrue(alertManager.isAlerting(106L))

        alertManager.unmute(106L)
        testScheduler.advanceUntilIdle()
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
