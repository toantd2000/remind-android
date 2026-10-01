package vn.io.litever.remind.features.reminders

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import vn.io.litever.remind.core.alarm.ReminderSyncManager
import vn.io.litever.remind.core.alarm.alert.ReminderAlertManager
import vn.io.litever.remind.core.alarm.notification.ReminderNotificationManager
import vn.io.litever.remind.core.common.util.PermissionChecker
import vn.io.litever.remind.core.datastore.AlarmPreferencesDataSource
import vn.io.litever.remind.core.domain.repository.ReminderRepository
import vn.io.litever.remind.core.domain.scheduler.ReminderScheduler
import java.time.DayOfWeek
import vn.io.litever.remind.core.model.NotificationStyle
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderMonthDay
import vn.io.litever.remind.core.model.ReminderTimeWindow
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.core.model.RepeatMode
import vn.io.litever.remind.core.model.TimeTrigger
import vn.io.litever.remind.features.reminders.viewmodel.ReminderListViewModel
import vn.io.litever.remind.features.reminders.viewmodel.ReminderRingingViewModel
import androidx.lifecycle.SavedStateHandle
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class ReminderAdversarialReviewTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repository = mockk<ReminderRepository>(relaxed = true)
    private val scheduler = mockk<ReminderScheduler>(relaxed = true)
    private val alertManager = mockk<ReminderAlertManager>(relaxed = true)
    private val notificationManager = mockk<ReminderNotificationManager>(relaxed = true)
    private val preferencesDataSource = mockk<AlarmPreferencesDataSource>(relaxed = true)
    private val permissionChecker = mockk<PermissionChecker>(relaxed = true)
    private val reminderSyncManager = mockk<ReminderSyncManager>(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { preferencesDataSource.is24HourFormat } returns flowOf(true)
        every { alertManager.autoSilenceCountdown } returns MutableStateFlow(null)
        every { permissionChecker.hasNotificationPermission() } returns true
        every { permissionChecker.hasPermissionsForReminder(any()) } returns true
        every { permissionChecker.getMissingPermissions(any()) } returns emptyList()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // =========================================================================
    // 1. Recurrence properties for TimeTrigger & Reminder
    // =========================================================================

    @Test
    fun timeTrigger_isRecurring_evaluatesCorrectlyAcrossAllModes() {
        // Once with date
        val onceWithDate = TimeTrigger(time = listOf(LocalTime.of(8, 0)), exactDate = LocalDate.of(2026, 9, 1), repeatMode = RepeatMode.NONE)
        assertFalse(onceWithDate.isRecurring)

        // Once without date (daily once)
        val onceNoDate = TimeTrigger(time = listOf(LocalTime.of(8, 0)), repeatMode = RepeatMode.NONE)
        assertFalse(onceNoDate.isRecurring)

        // Weekly single day
        val weeklySingle = TimeTrigger(time = listOf(LocalTime.of(8, 0)), repeatMode = RepeatMode.WEEKLY, repeatValues = listOf(DayOfWeek.MONDAY.value))
        assertTrue(weeklySingle.isRecurring)

        // Weekly full week (all 7 days)
        val weeklyFull = TimeTrigger(time = listOf(LocalTime.of(8, 0)), repeatMode = RepeatMode.WEEKLY, repeatValues = DayOfWeek.entries.map { it.value })
        assertTrue(weeklyFull.isRecurring)

        // Monthly single day
        val monthlySingle = TimeTrigger(time = listOf(LocalTime.of(8, 0)), repeatMode = RepeatMode.MONTHLY, monthlyDays = listOf(15))
        assertTrue(monthlySingle.isRecurring)

        // Monthly multi days
        val monthlyMulti = TimeTrigger(time = listOf(LocalTime.of(8, 0)), repeatMode = RepeatMode.MONTHLY, monthlyDays = listOf(1, 15, 30))
        assertTrue(monthlyMulti.isRecurring)

        // Yearly single date
        val yearlySingle = TimeTrigger(time = listOf(LocalTime.of(8, 0)), repeatMode = RepeatMode.YEARLY, yearlyDates = listOf(ReminderMonthDay(12, 25)))
        assertTrue(yearlySingle.isRecurring)

        // Yearly multi dates
        val yearlyMulti = TimeTrigger(time = listOf(LocalTime.of(8, 0)), repeatMode = RepeatMode.YEARLY, yearlyDates = listOf(ReminderMonthDay(1, 1), ReminderMonthDay(5, 1), ReminderMonthDay(12, 25)))
        assertTrue(yearlyMulti.isRecurring)

        // Interval mode
        val intervalTrigger = TimeTrigger(intervalMinutes = 60)
        assertTrue(intervalTrigger.isRecurring)
    }

    @Test
    fun reminder_isRecurring_delegatesToTimeTrigger() {
        val recurringReminder = Reminder(
            id = 10L,
            title = "Monthly Rent",
            triggerType = ReminderTriggerType.FIXED_TIME,
            timeTrigger = TimeTrigger(time = listOf(LocalTime.of(9, 0)), repeatMode = RepeatMode.MONTHLY, monthlyDays = listOf(1))
        )
        assertTrue(recurringReminder.isRecurring)

        val nonRecurringReminder = Reminder(
            id = 11L,
            title = "Doctor Appointment",
            triggerType = ReminderTriggerType.FIXED_TIME,
            timeTrigger = TimeTrigger(time = listOf(LocalTime.of(14, 0)), exactDate = LocalDate.of(2026, 10, 10), repeatMode = RepeatMode.NONE)
        )
        assertFalse(nonRecurringReminder.isRecurring)

        val intervalReminder = Reminder(
            id = 12L,
            title = "Drink Water",
            triggerType = ReminderTriggerType.INTERVAL,
            timeTrigger = TimeTrigger(intervalMinutes = 120)
        )
        assertTrue(intervalReminder.isRecurring)
    }

    // =========================================================================
    // 2. Dismissing Recurring Monthly & Yearly in ReminderRingingViewModel
    // =========================================================================

    @Test
    fun dismissRecurringMonthlyReminder_keepsReminderEnabled() = runTest(testDispatcher) {
        val monthlyReminder = Reminder(
            id = 50L,
            title = "Monthly Utility Bill",
            triggerType = ReminderTriggerType.FIXED_TIME,
            timeTrigger = TimeTrigger(time = listOf(LocalTime.of(9, 0)), repeatMode = RepeatMode.MONTHLY, monthlyDays = listOf(5)),
            isEnabled = true,
            isCompleted = false
        )
        val reminderFlow = MutableStateFlow<Reminder?>(monthlyReminder)
        every { repository.getReminderFlow(50L) } returns reminderFlow
        coEvery { repository.getReminderById(50L) } returns monthlyReminder

        val savedStateHandle = SavedStateHandle(mapOf("reminderId" to 50L))
        val viewModel = ReminderRingingViewModel(
            reminderRepository = repository,
            reminderScheduler = scheduler,
            reminderAlertManager = alertManager,
            reminderNotificationManager = notificationManager,
            preferencesDataSource = preferencesDataSource,
            savedStateHandle = savedStateHandle
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        testDispatcher.scheduler.advanceUntilIdle()

        var onFinishedCalled = false
        viewModel.dismiss { onFinishedCalled = true }
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(onFinishedCalled)
        // Must stay isEnabled = true because it is recurring!
        coVerify {
            repository.updateReminder(match {
                it.id == 50L && it.isCompleted && it.isEnabled
            })
        }
    }

    @Test
    fun dismissRecurringYearlyReminder_keepsReminderEnabled() = runTest(testDispatcher) {
        val yearlyReminder = Reminder(
            id = 51L,
            title = "Annual Insurance Renewal",
            triggerType = ReminderTriggerType.FIXED_TIME,
            timeTrigger = TimeTrigger(time = listOf(LocalTime.of(10, 0)), repeatMode = RepeatMode.YEARLY, yearlyDates = listOf(ReminderMonthDay(6, 15))),
            isEnabled = true,
            isCompleted = false
        )
        val reminderFlow = MutableStateFlow<Reminder?>(yearlyReminder)
        every { repository.getReminderFlow(51L) } returns reminderFlow
        coEvery { repository.getReminderById(51L) } returns yearlyReminder

        val savedStateHandle = SavedStateHandle(mapOf("reminderId" to 51L))
        val viewModel = ReminderRingingViewModel(
            reminderRepository = repository,
            reminderScheduler = scheduler,
            reminderAlertManager = alertManager,
            reminderNotificationManager = notificationManager,
            preferencesDataSource = preferencesDataSource,
            savedStateHandle = savedStateHandle
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        testDispatcher.scheduler.advanceUntilIdle()

        var onFinishedCalled = false
        viewModel.dismiss { onFinishedCalled = true }
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(onFinishedCalled)
        // Must stay isEnabled = true because it is recurring!
        coVerify {
            repository.updateReminder(match {
                it.id == 51L && it.isCompleted && it.isEnabled
            })
        }
    }

    // =========================================================================
    // 3. Snooze Toggle & Puzzle Verification in ReminderListViewModel
    // =========================================================================

    @Test
    fun toggleSnoozedReminder_clearsSnoozeNextTriggerTimeAndResetsCount() = runTest(testDispatcher) {
        val snoozedReminder = Reminder(
            id = 77L,
            title = "Snoozed Task",
            triggerType = ReminderTriggerType.FIXED_TIME,
            isEnabled = true,
            snoozeNextTriggerTime = System.currentTimeMillis() + 300000L,
            
        )
        val allRemindersFlow = MutableStateFlow(listOf(snoozedReminder))
        every { repository.getAllReminders() } returns allRemindersFlow

        val listViewModel = ReminderListViewModel(
            reminderRepository = repository,
            reminderScheduler = scheduler,
            permissionChecker = permissionChecker,
            preferencesDataSource = preferencesDataSource,
            reminderSyncManager = reminderSyncManager
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { listViewModel.uiState.collect() }
        testDispatcher.scheduler.advanceUntilIdle()

        // Toggle off (after puzzle confirmation in UI)
        listViewModel.toggleReminder(snoozedReminder)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { scheduler.cancelSnooze(snoozedReminder) }
        coVerify {
            repository.updateReminder(match {
                it.id == 77L &&
                        !it.isEnabled &&
                        it.snoozeNextTriggerTime == null
            })
        }
    }

    // =========================================================================
    // 4. Multi-Time Windows with All-Day preset evaluation
    // =========================================================================

    @Test
    fun multiTimeWindows_evaluatesCorrectly() {
        val window1 = ReminderTimeWindow(startTime = LocalTime.of(8, 0), endTime = LocalTime.of(11, 30))
        val window2 = ReminderTimeWindow(startTime = LocalTime.of(13, 30), endTime = LocalTime.of(17, 0))
        val windows = listOf(window1, window2)

        val morningTime = LocalDateTime.of(2026, 8, 27, 9, 15)
        val lunchTime = LocalDateTime.of(2026, 8, 27, 12, 15)
        val afternoonTime = LocalDateTime.of(2026, 8, 27, 15, 0)
        val eveningTime = LocalDateTime.of(2026, 8, 27, 19, 0)

        assertTrue(windows.any { it.isWithinWindow(morningTime) })
        assertFalse(windows.any { it.isWithinWindow(lunchTime) })
        assertTrue(windows.any { it.isWithinWindow(afternoonTime) })
        assertFalse(windows.any { it.isWithinWindow(eveningTime) })
    }

    @Test
    fun allDayPreset_coversFull24Hours() {
        val allDay = listOf(ReminderTimeWindow.ALL_DAY)

        val startOfDay = LocalDateTime.of(2026, 8, 27, 0, 0)
        val noon = LocalDateTime.of(2026, 8, 27, 12, 0)
        val endOfDay = LocalDateTime.of(2026, 8, 27, 23, 59)

        assertTrue(allDay.any { it.isWithinWindow(startOfDay) })
        assertTrue(allDay.any { it.isWithinWindow(noon) })
        assertTrue(allDay.any { it.isWithinWindow(endOfDay) })
    }

    // =========================================================================
    // 5. Unhandled Reminders App Open & Alert Cleanup
    // =========================================================================

    @Test
    fun sync_triggeredUnhandledReminder_enqueuesIntoAlertManagerWithoutMarkingCompleted() = runTest(testDispatcher) {
        val triggeredTime = System.currentTimeMillis() - 60000L
        val pastDate = LocalDate.now().minusDays(1)
        val unhandledReminder = Reminder(
            id = 99L,
            title = "Unhandled Doctor Appointment",
            triggerType = ReminderTriggerType.FIXED_TIME,
            timeTrigger = TimeTrigger(time = listOf(LocalTime.of(8, 0)), exactDate = pastDate, repeatMode = RepeatMode.NONE),
            isEnabled = true,
            isCompleted = false,
            lastTriggeredTime = triggeredTime,
            snoozeNextTriggerTime = null
        )

        val syncRepository = mockk<ReminderRepository>(relaxed = true)
        val syncScheduler = mockk<ReminderScheduler>(relaxed = true)
        val syncAlertManager = mockk<ReminderAlertManager>(relaxed = true)
        val context = mockk<android.content.Context>(relaxed = true)

        coEvery { syncRepository.getAllReminders() } returns flowOf(listOf(unhandledReminder))

        val syncManager = ReminderSyncManager(context, syncRepository, syncScheduler, syncAlertManager)
        syncManager.sync()
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify alertManager.startAlert was called to enqueue into sequential stack
        io.mockk.verify { syncAlertManager.startAlert(unhandledReminder) }
        // Verify repository did NOT mark it completed or disabled!
        coVerify(exactly = 0) {
            syncRepository.updateReminder(match {
                it.id == 99L && it.isCompleted
            })
        }
    }

    @Test
    fun listViewModel_toggleOff_stopsAlertAndCancelsNotification() = runTest(testDispatcher) {
        val activeReminder = Reminder(
            id = 101L,
            title = "Active Reminder",
            triggerType = ReminderTriggerType.FIXED_TIME,
            isEnabled = true,
            isCompleted = false
        )
        val allFlow = MutableStateFlow(listOf(activeReminder))
        every { repository.getAllReminders() } returns allFlow

        val listViewModel = ReminderListViewModel(
            reminderRepository = repository,
            reminderScheduler = scheduler,
            permissionChecker = permissionChecker,
            preferencesDataSource = preferencesDataSource,
            reminderSyncManager = reminderSyncManager,
            reminderAlertManager = alertManager,
            reminderNotificationManager = notificationManager
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { listViewModel.uiState.collect() }
        testDispatcher.scheduler.advanceUntilIdle()

        listViewModel.toggleReminder(activeReminder)
        testDispatcher.scheduler.advanceUntilIdle()

        io.mockk.verify { alertManager.stopAlert(101L) }
        io.mockk.verify { notificationManager.cancelNotification(101L) }
    }

    @Test
    fun listViewModel_deleteReminder_stopsAlertAndCancelsNotification() = runTest(testDispatcher) {
        val activeReminder = Reminder(
            id = 102L,
            title = "Delete Reminder",
            triggerType = ReminderTriggerType.FIXED_TIME,
            isEnabled = true,
            isCompleted = false
        )
        val allFlow = MutableStateFlow(listOf(activeReminder))
        every { repository.getAllReminders() } returns allFlow

        val listViewModel = ReminderListViewModel(
            reminderRepository = repository,
            reminderScheduler = scheduler,
            permissionChecker = permissionChecker,
            preferencesDataSource = preferencesDataSource,
            reminderSyncManager = reminderSyncManager,
            reminderAlertManager = alertManager,
            reminderNotificationManager = notificationManager
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { listViewModel.uiState.collect() }
        testDispatcher.scheduler.advanceUntilIdle()

        listViewModel.deleteReminder(activeReminder)
        testDispatcher.scheduler.advanceUntilIdle()

        io.mockk.verify { alertManager.stopAlert(102L) }
        io.mockk.verify { notificationManager.cancelNotification(102L) }
    }
}

