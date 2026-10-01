package vn.io.litever.remind.features.reminders

import androidx.lifecycle.SavedStateHandle
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import vn.io.litever.remind.core.alarm.ReminderSyncManager
import vn.io.litever.remind.core.alarm.alert.ReminderAlertManager
import vn.io.litever.remind.core.alarm.notification.ReminderNotificationManager
import vn.io.litever.remind.core.common.util.PermissionChecker
import vn.io.litever.remind.core.common.util.ReminderPermissionType
import vn.io.litever.remind.core.datastore.AlarmPreferencesDataSource
import vn.io.litever.remind.core.domain.repository.ReminderRepository
import vn.io.litever.remind.core.domain.scheduler.ReminderScheduler
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderAction
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.core.model.TimeTrigger
import vn.io.litever.remind.features.reminders.viewmodel.ReminderEditViewModel
import vn.io.litever.remind.features.reminders.viewmodel.ReminderListViewModel
import vn.io.litever.remind.features.reminders.viewmodel.ReminderRingingViewModel
import java.time.LocalTime

/**
 * Empirical Challenger Test Suite for Milestone 4:
 * Permission UX Edge Cases (Save Anyway, List Toggle Gating, Permission Refresh/Restoration)
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ReminderPermissionChallengerTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repository = mockk<ReminderRepository>(relaxed = true)
    private val scheduler = mockk<ReminderScheduler>(relaxed = true)
    private val reminderSyncManager = mockk<ReminderSyncManager>(relaxed = true)
    private val permissionChecker = mockk<PermissionChecker>(relaxed = true)
    private val alertManager = mockk<ReminderAlertManager>(relaxed = true)
    private val notificationManager = mockk<ReminderNotificationManager>(relaxed = true)
    private val preferencesDataSource = mockk<AlarmPreferencesDataSource>(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { preferencesDataSource.is24HourFormat } returns flowOf(true)
        every { permissionChecker.hasNotificationPermission() } returns true
        every { permissionChecker.hasNonCriticalPermissions() } returns true
        every { alertManager.autoSilenceCountdown } returns MutableStateFlow(null)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // =========================================================================
    // SECTION 1: PERMISSION UX EDGE CASES
    // =========================================================================

    @Test
    fun challenge1_saveAnyway_newReminder_setsDisabled_andNeverCallsSchedule() = runTest(testDispatcher) {
        every { permissionChecker.getMissingPermissions(any()) } returns listOf(
            ReminderPermissionType.NOTIFICATION,
            ReminderPermissionType.EXACT_ALARM
        )
        coEvery { repository.insertReminder(any()) } returns 701L

        val savedStateHandle = SavedStateHandle(mapOf("reminderId" to 0L))
        val viewModel = ReminderEditViewModel(
            reminderRepository = repository,
            reminderScheduler = scheduler,
            reminderSyncManager = reminderSyncManager,
            permissionChecker = permissionChecker,
            preferencesDataSource = preferencesDataSource,
            savedStateHandle = savedStateHandle
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setTitle("Scheduled Task")
        viewModel.setTriggerType(ReminderTriggerType.FIXED_TIME)
        viewModel.setTime(LocalTime.of(8, 0))

        var saveAnywaySuccess = false
        viewModel.saveAnyway(onSuccess = { saveAnywaySuccess = true })
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue("Save callback must succeed on Save Anyway", saveAnywaySuccess)
        assertFalse("Permission dialog must be dismissed", viewModel.uiState.value.showPermissionDialog)

        // Strict assertion: isEnabled must be FALSE and scheduler must NEVER be invoked
        coVerify(exactly = 1) { repository.insertReminder(match { !it.isEnabled && it.title == "Scheduled Task" }) }
        coVerify(exactly = 0) { scheduler.schedule(any()) }

        // Second scenario: attempting standard saveReminder when permissions are missing
        val draft = Reminder(
            id = 0L,
            title = "Doctor Appointment",
            triggerType = ReminderTriggerType.FIXED_TIME,
            timeTrigger = TimeTrigger(time = listOf(LocalTime.of(9, 0)))
        )
        every { permissionChecker.hasPermissionsForReminder(any()) } returns false

        val savedStateHandle2 = SavedStateHandle(mapOf("reminderId" to 0L))
        val viewModel2 = ReminderEditViewModel(
            reminderRepository = repository,
            reminderScheduler = scheduler,
            reminderSyncManager = reminderSyncManager,
            permissionChecker = permissionChecker,
            preferencesDataSource = preferencesDataSource,
            savedStateHandle = savedStateHandle2
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel2.setTitle(draft.title)
        viewModel2.setTriggerType(draft.triggerType)
        viewModel2.setTime(LocalTime.of(9, 0))

        var saveReminderSuccess = false
        viewModel2.saveReminder(onSuccess = { saveReminderSuccess = true })
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(saveReminderSuccess)
        assertTrue(viewModel2.uiState.value.showPermissionDialog)
        assertEquals(2, viewModel2.uiState.value.missingPermissions.size)
        assertTrue(viewModel2.uiState.value.missingPermissions.contains(ReminderPermissionType.NOTIFICATION))
        assertTrue(viewModel2.uiState.value.missingPermissions.contains(ReminderPermissionType.EXACT_ALARM))

        coVerify(exactly = 0) { repository.insertReminder(match { it.title == "Doctor Appointment" }) }
    }

    @Test
    fun challenge2_reminder_permissionGranted_disablingCancelsScheduler() = runTest(testDispatcher) {
        val existingReminder = Reminder(
            id = 702L,
            title = "Morning Alarm",
            triggerType = ReminderTriggerType.FIXED_TIME,
            timeTrigger = TimeTrigger(time = listOf(LocalTime.of(7, 0))),
            isEnabled = true
        )
        coEvery { repository.getReminderById(702L) } returns existingReminder
        every { permissionChecker.getMissingPermissions(any()) } returns emptyList()
        every { permissionChecker.hasPermissionsForReminder(any()) } returns true

        val savedStateHandle = SavedStateHandle(mapOf("reminderId" to 702L))
        val viewModel = ReminderEditViewModel(
            reminderRepository = repository,
            reminderScheduler = scheduler,
            reminderSyncManager = reminderSyncManager,
            permissionChecker = permissionChecker,
            preferencesDataSource = preferencesDataSource,
            savedStateHandle = savedStateHandle
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setIsEnabled(false)

        var saveSuccess = false
        viewModel.saveReminder(onSuccess = { saveSuccess = true })
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(saveSuccess)
        coVerify(exactly = 1) {
            repository.updateReminder(match { reminder ->
                reminder.id == 702L && !reminder.isEnabled
            })
        }
        coVerify(exactly = 1) { scheduler.cancel(match { it.id == 702L }) }
    }

    @Test
    fun challenge3_listToggle_missingPermissions_strictlyBlocked() = runTest(testDispatcher) {
        val disabledReminder = Reminder(
            id = 703L,
            title = "Afternoon Meeting",
            triggerType = ReminderTriggerType.FIXED_TIME,
            timeTrigger = TimeTrigger(time = listOf(LocalTime.of(14, 0))),
            isEnabled = false
        )
        val allFlow = MutableStateFlow(listOf(disabledReminder))
        every { repository.getAllReminders() } returns allFlow
        every { permissionChecker.hasPermissionsForReminder(disabledReminder) } returns false
        every { permissionChecker.getMissingPermissions(disabledReminder) } returns listOf(ReminderPermissionType.EXACT_ALARM)

        val listViewModel = ReminderListViewModel(
            reminderRepository = repository,
            reminderScheduler = scheduler,
            reminderSyncManager = reminderSyncManager,
            permissionChecker = permissionChecker,
            preferencesDataSource = preferencesDataSource
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { listViewModel.uiState.collect() }
        testDispatcher.scheduler.advanceUntilIdle()

        listViewModel.toggleReminder(disabledReminder)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { repository.updateReminder(any()) }
        assertTrue("Permission warning must be shown", listViewModel.uiState.value.hasPermissionWarning)
    }

    @Test
    fun challenge4_permissionRestoration_clearsBannerWhenGranted() = runTest(testDispatcher) {
        val activeReminder = Reminder(
            id = 704L,
            title = "Morning Coffee",
            triggerType = ReminderTriggerType.FIXED_TIME,
            timeTrigger = TimeTrigger(time = listOf(LocalTime.of(7, 30))),
            isEnabled = true
        )
        val allFlow = MutableStateFlow(listOf(activeReminder))
        every { repository.getAllReminders() } returns allFlow
        every { permissionChecker.hasPermissionsForReminder(activeReminder) } returns false

        val listViewModel = ReminderListViewModel(
            reminderRepository = repository,
            reminderScheduler = scheduler,
            reminderSyncManager = reminderSyncManager,
            permissionChecker = permissionChecker,
            preferencesDataSource = preferencesDataSource
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { listViewModel.uiState.collect() }
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(listViewModel.uiState.value.hasPermissionWarning)

        every { permissionChecker.hasPermissionsForReminder(activeReminder) } returns true
        listViewModel.refreshPermissions()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(listViewModel.uiState.value.hasPermissionWarning)
    }

    @Test
    fun challenge5_editScreen_refreshPermissions_dismissesPermissionDialogWhenGranted() = runTest(testDispatcher) {
        val draft = Reminder(
            id = 0L,
            title = "Evening Medication",
            triggerType = ReminderTriggerType.FIXED_TIME,
            timeTrigger = TimeTrigger(time = listOf(LocalTime.of(20, 0)))
        )
        every { permissionChecker.getMissingPermissions(any()) } returns listOf(ReminderPermissionType.EXACT_ALARM)
        every { permissionChecker.hasPermissionsForReminder(any()) } returns false

        val savedStateHandle = SavedStateHandle(mapOf("reminderId" to 0L))
        val viewModel = ReminderEditViewModel(
            reminderRepository = repository,
            reminderScheduler = scheduler,
            reminderSyncManager = reminderSyncManager,
            permissionChecker = permissionChecker,
            preferencesDataSource = preferencesDataSource,
            savedStateHandle = savedStateHandle
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setTitle(draft.title)
        viewModel.setTriggerType(draft.triggerType)
        viewModel.setTime(LocalTime.of(20, 0))

        viewModel.saveReminder(onSuccess = {})
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.showPermissionDialog)

        every { permissionChecker.getMissingPermissions(any()) } returns emptyList()
        every { permissionChecker.hasPermissionsForReminder(any()) } returns true

        viewModel.refreshPermissions()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showPermissionDialog)
        assertTrue(viewModel.uiState.value.missingPermissions.isEmpty())
    }
}

