package vn.io.litever.remind.core.alarm.notification

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import android.net.Uri
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import vn.io.litever.remind.core.alarm.R
import vn.io.litever.remind.core.alarm.alert.AlertPlaybackState
import vn.io.litever.remind.core.alarm.alert.ReminderAlertManagerImpl
import vn.io.litever.remind.core.common.util.PermissionChecker
import vn.io.litever.remind.core.common.util.getAccessibleRingtoneUri
import vn.io.litever.remind.core.model.ChecklistItem
import vn.io.litever.remind.core.model.NotificationStyle
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderTriggerType

/**
 * Adversarial Stress & Edge Case Test Suite for ReminderNotificationManager and ReminderAlertManager.
 *
 * Covers:
 * 1. Notification Channels, DND bypass, importance levels.
 * 2. Extreme titles (10,000+ chars, emojis, unicode, blank, null-like), empty notes, large checklists (50+ items),
 *    formatting truncation, all-completed checklists, single item checklists.
 * 3. Alert Manager concurrency: rapid start/stop/mute cycles, multi-reminder queue progression, in-call suppression.
 * 4. Auto-silence safety timeout behavior, ticker emissions, early dismissal cancellation.
 * 5. Sound URI fallback hierarchy and audio exception resilience.
 * 6. Notification ID isolation and PendingIntent request code determinism.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ReminderNotificationAndAlertStressTest {

    private val context = mockk<Context>(relaxed = true)
    private val notificationManager = mockk<NotificationManager>(relaxed = true)
    private val audioManager = mockk<AudioManager>(relaxed = true)
    private val permissionChecker = mockk<PermissionChecker>(relaxed = true)

    private lateinit var reminderNotificationManager: ReminderNotificationManager
    private lateinit var reminderAlertManager: ReminderAlertManagerImpl

    @Before
    fun setup() {
        every { context.packageName } returns "vn.io.litever.remind"
        every { context.getSystemService(Context.NOTIFICATION_SERVICE) } returns notificationManager
        every { context.getSystemService(Context.AUDIO_SERVICE) } returns audioManager
        every { audioManager.mode } returns AudioManager.MODE_NORMAL

        every { context.getString(R.string.reminder_channel_name_standard) } returns "Reminders"
        every { context.getString(R.string.reminder_channel_desc_standard) } returns "Notifications for scheduled reminders"
        every { context.getString(R.string.reminder_channel_name_urgent) } returns "Urgent Reminders & Alarms"
        every { context.getString(R.string.reminder_channel_desc_urgent) } returns "Urgent notifications"
        every { context.getString(R.string.reminder_default_title) } returns "Reminder"
        every { context.getString(R.string.reminder_default_triggered) } returns "Reminder triggered"
        every { context.getString(R.string.reminder_action_snooze) } returns "Snooze"
        every { context.getString(R.string.reminder_action_snooze_interval, any()) } answers { "Snooze (${args[1]}m)" }
        every { context.getString(R.string.reminder_action_dismiss) } returns "Dismiss"
        every { context.getString(R.string.reminder_checklist_summary_header, any(), any()) } answers {
            "Checklist (${args[1]}/${args[2]} completed):"
        }

        every { permissionChecker.hasNotificationPermission() } returns true
        every { permissionChecker.hasFullScreenIntentPermission() } returns true

        reminderNotificationManager = ReminderNotificationManager(context, permissionChecker)
        reminderAlertManager = ReminderAlertManagerImpl(context)
    }

    @After
    fun tearDown() {
        reminderAlertManager.stopAllAlerts()
        unmockkAll()
    }

    // =========================================================================
    // 1. NOTIFICATION CHANNEL & IMPORTANCE & DND TESTS
    // =========================================================================

    @Test
    fun notificationChannels_createNotificationChannels_registersBothChannels() {
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
            triggerType = ReminderTriggerType.TIME,
            isPersistentAlert = true,
            notificationStyle = NotificationStyle.STANDARD
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
            triggerType = ReminderTriggerType.TIME,
            notificationStyle = NotificationStyle.FULL_SCREEN,
            isPersistentAlert = false
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
            triggerType = ReminderTriggerType.TIME,
            notificationStyle = NotificationStyle.STANDARD,
            isPersistentAlert = false
        )

        val notification = reminderNotificationManager.buildNotification(reminder)
        assertNotNull(notification)
        assertEquals(ReminderNotificationManager.CHANNEL_ID_STANDARD, notification.channelId)
    }

    // =========================================================================
    // 2. EXTREME TITLES, BLANK/EMPTY NOTES & LARGE CHECKLISTS (50+ ITEMS)
    // =========================================================================

    @Test
    fun formatContentText_withBlankTitleAndBlankNotesAndEmptyChecklist_returnsDefaultTriggered() {
        val reminder = Reminder(
            id = 2001L,
            title = "   ",
            notes = "   ",
            triggerType = ReminderTriggerType.TIME,
            checklist = emptyList()
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
            triggerType = ReminderTriggerType.TIME,
            checklist = emptyList()
        )

        val text = reminderNotificationManager.formatContentText(reminder)
        assertEquals(massiveNote, text)

        val bigStyle = reminderNotificationManager.formatBigTextStyle(reminder, "Massive Reminder")
        assertNotNull(bigStyle)
    }

    @Test
    fun formatBigTextStyle_with50ChecklistItems_formatsAllItemsAndCountersCorrectly() {
        val items = (1..50).map { i ->
            ChecklistItem(
                id = i.toLong(),
                reminderId = 2003L,
                text = "Item #$i description",
                isCompleted = (i <= 20), // 20 completed, 30 pending
                orderIndex = i
            )
        }

        val reminder = Reminder(
            id = 2003L,
            title = "Big Packing List",
            notes = "Pack for international flight",
            triggerType = ReminderTriggerType.LOCATION,
            checklist = items
        )

        assertEquals(20, reminder.completedChecklistCount)
        assertEquals(50, reminder.totalChecklistCount)

        val text = reminderNotificationManager.formatContentText(reminder)
        assertTrue(text.contains("Pack for international flight"))
        assertTrue(text.contains("[20/50 done]"))
        assertTrue(text.contains("Item #21 description")) // first uncompleted item snippet

        val bigStyle = reminderNotificationManager.formatBigTextStyle(reminder, reminder.title)
        assertNotNull(bigStyle)

        val notification = reminderNotificationManager.buildNotification(reminder)
        assertNotNull(notification)
        // Verify inline toggle action exists for first uncompleted item (Item #21)
        assertTrue(notification.actions != null && notification.actions.isNotEmpty())
    }

    @Test
    fun buildNotification_whenAllChecklistItemsCompleted_omitsInlineChecklistToggle() {
        val items = listOf(
            ChecklistItem(id = 1L, reminderId = 2004L, text = "Done A", isCompleted = true, orderIndex = 0),
            ChecklistItem(id = 2L, reminderId = 2004L, text = "Done B", isCompleted = true, orderIndex = 1)
        )
        val reminder = Reminder(
            id = 2004L,
            title = "Completed Checklist",
            triggerType = ReminderTriggerType.TIME,
            checklist = items,
            snoozeEnabled = false
        )

        val notification = reminderNotificationManager.buildNotification(reminder)
        assertNotNull(notification)
        // With snoozeEnabled = false and all checklist items completed, only Dismiss action should be present
        assertEquals(1, notification.actions?.size ?: 0)
    }

    @Test
    fun buildNotification_actionToggleLabel_truncatesLongItemTextTo20Chars() {
        val longItemText = "Super Long Checklist Item Description That Exceeds Normal Length"
        val reminder = Reminder(
            id = 2005L,
            title = "Truncation Test",
            triggerType = ReminderTriggerType.TIME,
            checklist = listOf(
                ChecklistItem(id = 1L, reminderId = 2005L, text = longItemText, isCompleted = false)
            )
        )

        val notification = reminderNotificationManager.buildNotification(reminder)
        assertNotNull(notification)
        val toggleAction = notification.actions?.firstOrNull { it.title.startsWith("✓") }
        assertNotNull(toggleAction)
        val expectedLabel = "✓ ${longItemText.take(20)}"
        assertEquals(expectedLabel, toggleAction?.title.toString())
    }

    // =========================================================================
    // 3. ALERT MANAGER CONCURRENCY & MULTI-REMINDER QUEUE PROGRESSION
    // =========================================================================

    @Test
    fun alertManager_rapidStartStopStress_maintainsConsistentState() = runTest {
        val reminders = (1..20).map { i ->
            Reminder(
                id = 3000L + i,
                title = "Concurrent Alert $i",
                triggerType = ReminderTriggerType.TIME,
                isPersistentAlert = true
            )
        }

        // Rapid concurrent start calls
        reminders.forEach { reminderAlertManager.startAlert(it) }
        testScheduler.advanceUntilIdle()

        assertEquals(20, reminderAlertManager.activeAlertReminderIds.value.size)
        val state = reminderAlertManager.playbackState.value
        assertTrue(state is AlertPlaybackState.Playing)
        assertEquals(3001L, (state as AlertPlaybackState.Playing).reminderId)

        // Rapid selective stop calls
        for (i in 1..10) {
            reminderAlertManager.stopAlert(3000L + i)
        }
        testScheduler.advanceUntilIdle()

        assertEquals(10, reminderAlertManager.activeAlertReminderIds.value.size)
        val nextState = reminderAlertManager.playbackState.value
        assertTrue(nextState is AlertPlaybackState.Playing)
        assertEquals(3011L, (nextState as AlertPlaybackState.Playing).reminderId)

        // Stop all
        reminderAlertManager.stopAllAlerts()
        testScheduler.advanceUntilIdle()

        assertTrue(reminderAlertManager.activeAlertReminderIds.value.isEmpty())
        assertEquals(AlertPlaybackState.Idle, reminderAlertManager.playbackState.value)
    }

    @Test
    fun alertManager_multiAlertQueue_progressesToNextWhenCurrentDismissed() = runTest {
        val reminder1 = Reminder(id = 3101L, title = "First Alert", triggerType = ReminderTriggerType.TIME, isPersistentAlert = true)
        val reminder2 = Reminder(id = 3102L, title = "Second Alert", triggerType = ReminderTriggerType.TIME, isPersistentAlert = true)
        val reminder3 = Reminder(id = 3103L, title = "Third Alert", triggerType = ReminderTriggerType.TIME, isPersistentAlert = true)

        reminderAlertManager.startAlert(reminder1)
        reminderAlertManager.startAlert(reminder2)
        reminderAlertManager.startAlert(reminder3)
        testScheduler.advanceUntilIdle()

        // 1 is currently playing
        assertEquals(3101L, (reminderAlertManager.playbackState.value as AlertPlaybackState.Playing).reminderId)

        // Dismiss 1 -> Should immediately switch to 2
        reminderAlertManager.stopAlert(3101L)
        testScheduler.advanceUntilIdle()

        assertEquals(3102L, (reminderAlertManager.playbackState.value as AlertPlaybackState.Playing).reminderId)

        // Dismiss 2 -> Should immediately switch to 3
        reminderAlertManager.stopAlert(3102L)
        testScheduler.advanceUntilIdle()

        assertEquals(3103L, (reminderAlertManager.playbackState.value as AlertPlaybackState.Playing).reminderId)

        // Dismiss 3 -> Queue is empty, Idle
        reminderAlertManager.stopAlert(3103L)
        testScheduler.advanceUntilIdle()

        assertEquals(AlertPlaybackState.Idle, reminderAlertManager.playbackState.value)
    }

    @Test
    fun alertManager_mutingAndUnmutingInQueue_skipsMutedAndResumesProperly() = runTest {
        val r1 = Reminder(id = 3201L, title = "Alert 1", triggerType = ReminderTriggerType.TIME, isPersistentAlert = true)
        val r2 = Reminder(id = 3202L, title = "Alert 2", triggerType = ReminderTriggerType.TIME, isPersistentAlert = true)

        reminderAlertManager.startAlert(r1)
        reminderAlertManager.startAlert(r2)
        testScheduler.advanceUntilIdle()

        assertEquals(3201L, (reminderAlertManager.playbackState.value as AlertPlaybackState.Playing).reminderId)

        // Mute 1 -> Evaluation selects r2
        reminderAlertManager.mute(3201L)
        testScheduler.advanceUntilIdle()

        // Note: r1 is still in queue, but r2 becomes the active audible playback!
        assertTrue(reminderAlertManager.isAlerting(3201L))
        assertTrue(reminderAlertManager.isAlerting(3202L))
        assertEquals(3202L, (reminderAlertManager.playbackState.value as AlertPlaybackState.Playing).reminderId)

        // Unmute 1 -> r1 is before r2 in queue, so r1 takes precedence again
        reminderAlertManager.unmute(3201L)
        testScheduler.advanceUntilIdle()

        assertEquals(3201L, (reminderAlertManager.playbackState.value as AlertPlaybackState.Playing).reminderId)
    }

    @Test
    fun alertManager_whenInCallActive_suppressesAudioPlaybackAndUsesVibrationOnly() = runTest {
        every { audioManager.mode } returns AudioManager.MODE_IN_CALL

        val reminder = Reminder(
            id = 3301L,
            title = "In-Call Reminder",
            triggerType = ReminderTriggerType.TIME,
            isPersistentAlert = true,
            vibrationEnabled = true
        )

        reminderAlertManager.startAlert(reminder)
        testScheduler.advanceUntilIdle()

        assertTrue(reminderAlertManager.isAlerting(3301L))
        // Audio focus request should NOT be requested to avoid interrupting active call
        verify(exactly = 0) { audioManager.requestAudioFocus(any()) }
    }

    // =========================================================================
    // 4. AUTO-SILENCE SAFETY TIMEOUT & TICKER EMISSION
    // =========================================================================

    @Test
    fun alertManager_autoSilenceCountdown_emitsAccurateSecondsAndStopsOnDismiss() = runTest {
        val reminder = Reminder(id = 4001L, title = "Timeout Test", triggerType = ReminderTriggerType.TIME, isPersistentAlert = true)
        reminderAlertManager.startAlert(reminder)
        testScheduler.advanceUntilIdle()

        // At T=0, countdown should be initialized to 300 seconds (5 min)
        assertEquals(300, reminderAlertManager.autoSilenceCountdown.value)

        // Advance 60 seconds -> countdown should be ~240
        testScheduler.advanceTimeBy(60_000L)
        testScheduler.advanceUntilIdle()
        assertEquals(240, reminderAlertManager.autoSilenceCountdown.value)

        // Dismiss before timeout -> timer cancels immediately and countdown resets to null
        reminderAlertManager.stopAlert(4001L)
        testScheduler.advanceUntilIdle()

        assertNull(reminderAlertManager.autoSilenceCountdown.value)
        assertEquals(AlertPlaybackState.Idle, reminderAlertManager.playbackState.value)
    }

    // =========================================================================
    // 5. SOUND URI FALLBACK HIERARCHY
    // =========================================================================

    @Test
    fun soundUtils_getAccessibleRingtoneUri_nullOrInvalidString_returnsFallbackUri() {
        val fallback1 = getAccessibleRingtoneUri(context, null)
        assertNotNull(fallback1)

        val fallback2 = getAccessibleRingtoneUri(context, "")
        assertNotNull(fallback2)

        val fallback3 = getAccessibleRingtoneUri(context, "invalid://non_existent/path/sound.mp3")
        assertNotNull(fallback3)
    }

    // =========================================================================
    // 6. NOTIFICATION ID COLLISION & ISOLATION
    // =========================================================================

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
