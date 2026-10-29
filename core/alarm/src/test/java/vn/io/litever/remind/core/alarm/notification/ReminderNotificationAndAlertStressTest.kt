package vn.io.litever.remind.core.alarm.notification

import org.robolectric.RobolectricTestRunner
import org.junit.runner.RunWith

import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import androidx.test.core.app.ApplicationProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import vn.io.litever.remind.core.alarm.alert.AlertPlaybackState
import vn.io.litever.remind.core.alarm.alert.ReminderAlertManagerImpl
import vn.io.litever.remind.core.common.util.PermissionChecker
import vn.io.litever.remind.core.common.util.getAccessibleRingtoneUri
import vn.io.litever.remind.core.model.NotificationStyle
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderAction
import vn.io.litever.remind.core.model.ReminderTriggerType

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class ReminderNotificationAndAlertStressTest {

    private val appContext: Context = ApplicationProvider.getApplicationContext()
    private val context = spyk(appContext)
    private val notificationManager = mockk<NotificationManager>(relaxed = true)
    private val permissionChecker = mockk<PermissionChecker>(relaxed = true)
    private val audioManager = mockk<AudioManager>(relaxed = true)

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var reminderNotificationManager: ReminderNotificationManager
    private lateinit var reminderAlertManager: ReminderAlertManagerImpl

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        every { context.getSystemService(Context.NOTIFICATION_SERVICE) } returns notificationManager
        every { context.getSystemService(Context.AUDIO_SERVICE) } returns audioManager
        every { permissionChecker.hasNotificationPermission() } returns true
        every { permissionChecker.hasFullScreenIntentPermission() } returns true

        reminderNotificationManager = ReminderNotificationManager(context, permissionChecker)
        reminderAlertManager = ReminderAlertManagerImpl(context)
    }

    @After
    fun tearDown() {
        reminderAlertManager.stopAllAlerts()
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun createNotificationChannels_createsStandardAndUrgentChannelsWithCorrectImportance() {
        reminderNotificationManager.createNotificationChannels()

        verify(atLeast = 1) {
            notificationManager.createNotificationChannel(match {
                it.id == ReminderNotificationManager.CHANNEL_ID_STANDARD &&
                        it.importance == NotificationManager.IMPORTANCE_HIGH
            })
        }

        verify(atLeast = 1) {
            notificationManager.createNotificationChannel(match {
                it.id == ReminderNotificationManager.CHANNEL_ID_URGENT &&
                        it.importance == NotificationManager.IMPORTANCE_HIGH
            })
        }
    }

    @Test
    fun buildNotification_urgentPersistentStyle_routesToUrgentChannel() {
        val reminder = Reminder(
            id = 1001L,
            title = "Urgent Medication",
            triggerType = ReminderTriggerType.FIXED_TIME,
            action = ReminderAction(
                isPersistentAlert = true,
                notificationStyle = NotificationStyle.STANDARD
            )
        )

        val notification = reminderNotificationManager.buildNotification(reminder)
        assertNotNull(notification)
        assertEquals(ReminderNotificationManager.CHANNEL_ID_URGENT, notification.channelId)
    }

    @Test
    fun buildNotification_fullScreenStyle_routesToUrgentChannel() {
        val reminder = Reminder(
            id = 1002L,
            title = "Full Screen Alert",
            triggerType = ReminderTriggerType.FIXED_TIME,
            action = ReminderAction(
                notificationStyle = NotificationStyle.FULL_SCREEN,
                isPersistentAlert = false
            )
        )

        val notification = reminderNotificationManager.buildNotification(reminder)
        assertNotNull(notification)
        assertEquals(ReminderNotificationManager.CHANNEL_ID_URGENT, notification.channelId)
    }

    @Test
    fun buildNotification_standardStyle_routesToStandardChannel() {
        val reminder = Reminder(
            id = 1003L,
            title = "Normal Note",
            triggerType = ReminderTriggerType.FIXED_TIME,
            action = ReminderAction(
                notificationStyle = NotificationStyle.STANDARD,
                isPersistentAlert = false
            )
        )

        val notification = reminderNotificationManager.buildNotification(reminder)
        assertNotNull(notification)
        assertEquals(ReminderNotificationManager.CHANNEL_ID_STANDARD, notification.channelId)
    }

    @Test
    fun formatContentText_withBlankTitleAndBlankNotes_returnsDefaultTriggered() {
        val reminder = Reminder(
            id = 2001L,
            title = "   ",
            notes = "   ",
            triggerType = ReminderTriggerType.FIXED_TIME
        )

        val text = reminderNotificationManager.formatContentText(reminder)
        assertEquals("Reminder triggered", text)

        val notification = reminderNotificationManager.buildNotification(reminder)
        assertNotNull(notification)
    }

    @Test
    fun formatContentText_withMassive10kCharNotes_doesNotCrash() {
        val massiveNote = "A".repeat(10_000)
        val reminder = Reminder(
            id = 2002L,
            title = "Massive Reminder",
            notes = massiveNote,
            triggerType = ReminderTriggerType.FIXED_TIME
        )

        val text = reminderNotificationManager.formatContentText(reminder)
        assertEquals(massiveNote, text)

        val bigStyle = reminderNotificationManager.formatBigTextStyle(reminder, "Massive Reminder")
        assertNotNull(bigStyle)
    }

    @Test
    fun alertManager_rapidStartStopStress_maintainsConsistentState() = runTest {
        val reminders = (1..20).map { i ->
            Reminder(
                id = 3000L + i,
                title = "Concurrent Alert $i",
                triggerType = ReminderTriggerType.FIXED_TIME,
                action = ReminderAction(isPersistentAlert = true)
            )
        }

        reminders.forEach { reminderAlertManager.startAlert(it) }
        testScheduler.runCurrent()

        assertEquals(20, reminderAlertManager.activeAlertReminderIds.value.size)
        val state = reminderAlertManager.playbackState.value
        assertTrue(state is AlertPlaybackState.Playing)
        assertEquals(3001L, (state as AlertPlaybackState.Playing).reminderId)

        for (i in 1..10) {
            reminderAlertManager.stopAlert(3000L + i)
        }
        testScheduler.runCurrent()

        assertEquals(10, reminderAlertManager.activeAlertReminderIds.value.size)
        val nextState = reminderAlertManager.playbackState.value
        assertTrue(nextState is AlertPlaybackState.Playing)
        assertEquals(3011L, (nextState as AlertPlaybackState.Playing).reminderId)

        reminderAlertManager.stopAllAlerts()
        testScheduler.runCurrent()

        assertTrue(reminderAlertManager.activeAlertReminderIds.value.isEmpty())
        assertEquals(AlertPlaybackState.Idle, reminderAlertManager.playbackState.value)
    }

    @Test
    fun alertManager_autoSilenceCountdown_emitsAccurateSecondsAndStopsOnDismiss() = runTest {
        val reminder = Reminder(
            id = 4001L,
            title = "Timeout Test",
            triggerType = ReminderTriggerType.FIXED_TIME,
            action = ReminderAction(isPersistentAlert = true)
        )
        reminderAlertManager.startAlert(reminder)
        testScheduler.runCurrent()

        assertEquals(300, reminderAlertManager.autoSilenceCountdown.value)

        testScheduler.advanceTimeBy(60_000L)
        testScheduler.runCurrent()
        assertEquals(240, reminderAlertManager.autoSilenceCountdown.value)

        reminderAlertManager.stopAlert(4001L)
        testScheduler.runCurrent()

        assertNull(reminderAlertManager.autoSilenceCountdown.value)
        assertEquals(AlertPlaybackState.Idle, reminderAlertManager.playbackState.value)
    }

    @Test
    fun soundUtils_getAccessibleRingtoneUri_nullOrInvalidString_returnsFallbackUri() {
        val fallback1 = getAccessibleRingtoneUri(context, null)
        assertNotNull(fallback1)

        val fallback2 = getAccessibleRingtoneUri(context, "")
        assertNotNull(fallback2)

        val fallback3 = getAccessibleRingtoneUri(context, "invalid://non_existent/path/sound.mp3")
        assertNotNull(fallback3)
    }

    @Test
    fun notificationId_uniqueAcrossReminderIds() {
        val id1 = reminderNotificationManager.getNotificationId(1L)
        val id2 = reminderNotificationManager.getNotificationId(2L)
        val id1000 = reminderNotificationManager.getNotificationId(1000L)

        assertTrue(id1 >= ReminderNotificationManager.NOTIFICATION_ID_OFFSET)
        assertTrue(id2 >= ReminderNotificationManager.NOTIFICATION_ID_OFFSET)
        assertTrue(id1 != id2)
        assertTrue(id1 != id1000)
    }
}
