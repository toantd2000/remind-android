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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import vn.io.litever.remind.core.alarm.ReminderSyncManager
import vn.io.litever.remind.core.common.util.PermissionChecker
import vn.io.litever.remind.core.common.util.ReminderPermissionType
import vn.io.litever.remind.core.datastore.AlarmPreferencesDataSource
import vn.io.litever.remind.core.domain.repository.ReminderRepository
import vn.io.litever.remind.core.domain.scheduler.ReminderScheduler
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.core.model.TimeTrigger
import vn.io.litever.remind.features.reminders.viewmodel.ReminderListViewModel
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class ReminderListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repository = mockk<ReminderRepository>(relaxed = true)
    private val scheduler = mockk<ReminderScheduler>(relaxed = true)
    private val reminderSyncManager = mockk<ReminderSyncManager>(relaxed = true)
    private val permissionChecker = mockk<PermissionChecker>(relaxed = true)
    private val preferencesDataSource = mockk<AlarmPreferencesDataSource>(relaxed = true)

    private val allRemindersFlow = MutableStateFlow<List<Reminder>>(emptyList())
    private val is24HourFlow = MutableStateFlow(true)

    private lateinit var viewModel: ReminderListViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { repository.getAllReminders() } returns allRemindersFlow
        every { preferencesDataSource.is24HourFormat } returns is24HourFlow
        every { permissionChecker.hasNotificationPermission() } returns true
        every { permissionChecker.hasPermissionsForReminder(any()) } returns true
        every { permissionChecker.getMissingPermissions(any()) } returns emptyList()
        every { permissionChecker.hasNonCriticalPermissions() } returns true

        viewModel = ReminderListViewModel(
            reminderRepository = repository,
            reminderScheduler = scheduler,
            reminderSyncManager = reminderSyncManager,
            permissionChecker = permissionChecker,
            preferencesDataSource = preferencesDataSource
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_hasDefaultValues() = runTest(testDispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(emptyList<Reminder>(), state.reminders)
        assertFalse(state.hasPermissionWarning)
    }

    @Test
    fun reminders_sortedCorrectly_snoozedFirstThenCreatedAtDescending() = runTest(testDispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        val r1 = Reminder(id = 1, title = "Old", triggerType = ReminderTriggerType.FIXED_TIME, createdAt = 1000L)
        val r2 = Reminder(id = 2, title = "Newer", triggerType = ReminderTriggerType.FIXED_TIME, createdAt = 2000L)
        val rSnoozed = Reminder(id = 3, title = "Snoozed", triggerType = ReminderTriggerType.FIXED_TIME, snoozeNextTriggerTime = 5000L, createdAt = 1500L)

        allRemindersFlow.value = listOf(r1, r2, rSnoozed)
        testDispatcher.scheduler.advanceUntilIdle()

        val reminders = viewModel.uiState.value.reminders
        assertEquals(3, reminders.size)
        assertEquals(3L, reminders[0].id)
        assertEquals(2L, reminders[1].id)
        assertEquals(1L, reminders[2].id)
    }

    @Test
    fun toggleReminder_whenPermissionsGranted_updatesAndSchedules() = runTest(testDispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        val reminder = Reminder(id = 1, title = "Test", triggerType = ReminderTriggerType.FIXED_TIME, isEnabled = false)
        every { permissionChecker.getMissingPermissions(any()) } returns emptyList()

        viewModel.toggleReminder(reminder)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.updateReminder(match { it.id == 1L && it.isEnabled }) }
        coVerify { scheduler.schedule(match { it.id == 1L && it.isEnabled }) }
    }

    @Test
    fun toggleReminder_whenPermissionsMissing_blocksAndWarns() = runTest(testDispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        val reminder = Reminder(id = 1, title = "Test", triggerType = ReminderTriggerType.FIXED_TIME, isEnabled = false)
        every { permissionChecker.getMissingPermissions(any()) } returns listOf(ReminderPermissionType.EXACT_ALARM)

        viewModel.toggleReminder(reminder)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { repository.updateReminder(any()) }
        coVerify(exactly = 0) { scheduler.schedule(any()) }
        assertTrue(viewModel.uiState.value.hasPermissionWarning)
    }

    @Test
    fun deleteReminder_andUndo_cancelsAndRestores() = runTest(testDispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        val reminder = Reminder(id = 5, title = "To Delete", triggerType = ReminderTriggerType.FIXED_TIME, isEnabled = true)
        coEvery { repository.insertReminder(any()) } returns 55L

        viewModel.deleteReminder(reminder)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { scheduler.cancel(reminder) }
        coVerify { repository.deleteReminder(reminder) }

        // Undo
        viewModel.undoDelete()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.insertReminder(match { it.title == "To Delete" }) }
        coVerify { scheduler.schedule(match { it.id == 55L }) }
    }

    @Test
    fun deleteCompletedReminders_deletesOnlyCompleted() = runTest(testDispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        val active = Reminder(id = 1, title = "Active", triggerType = ReminderTriggerType.FIXED_TIME, isCompleted = false)
        val done1 = Reminder(id = 2, title = "Done 1", triggerType = ReminderTriggerType.FIXED_TIME, isCompleted = true)
        val done2 = Reminder(id = 3, title = "Done 2", triggerType = ReminderTriggerType.FIXED_TIME, isCompleted = true)

        allRemindersFlow.value = listOf(active, done1, done2)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.deleteCompletedReminders()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.deleteReminder(done1) }
        coVerify { repository.deleteReminder(done2) }
        coVerify(exactly = 0) { repository.deleteReminder(active) }
    }

    @Test
    fun duplicateReminder_createsDeepCopy() = runTest(testDispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        val original = Reminder(
            id = 10,
            title = "Original",
            triggerType = ReminderTriggerType.FIXED_TIME,
            timeTrigger = TimeTrigger(time = listOf(LocalTime.of(9, 0)))
        )
        coEvery { repository.insertReminder(any()) } returns 101L

        viewModel.duplicateReminder(original)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            repository.insertReminder(match {
                it.title == "Original (Copy)"
            })
        }
    }

    @Test
    fun toggleCompleted_marksCompletedAndCancelsSchedule() = runTest(testDispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        val active = Reminder(id = 1, title = "Active", triggerType = ReminderTriggerType.FIXED_TIME, isEnabled = true, isCompleted = false)

        viewModel.toggleCompleted(active)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.updateReminder(match { it.id == 1L && it.isCompleted && !it.isEnabled }) }
        coVerify { scheduler.cancel(match { it.id == 1L }) }
    }

    @Test
    fun snoozedReminders_pinnedToTopOfFilteredList() = runTest(testDispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        val normalOld = Reminder(id = 1, title = "Normal Old", triggerType = ReminderTriggerType.FIXED_TIME, createdAt = 1000L)
        val snoozedOld = Reminder(id = 2, title = "Snoozed Old", triggerType = ReminderTriggerType.FIXED_TIME, snoozeNextTriggerTime = 50000L, createdAt = 2000L)
        val normalNew = Reminder(id = 3, title = "Normal New", triggerType = ReminderTriggerType.FIXED_TIME, createdAt = 3000L)
        val snoozedNew = Reminder(id = 4, title = "Snoozed New", triggerType = ReminderTriggerType.FIXED_TIME, snoozeNextTriggerTime = 60000L, createdAt = 4000L)

        allRemindersFlow.value = listOf(normalOld, snoozedOld, normalNew, snoozedNew)
        testDispatcher.scheduler.advanceUntilIdle()

        val filtered = viewModel.uiState.value.filteredReminders
        assertEquals(4, filtered.size)
        // Snoozed items must be first, ordered by createdAt descending
        assertEquals(4L, filtered[0].id)
        assertEquals(2L, filtered[1].id)
        // Non-snoozed items follow, ordered by createdAt descending
        assertEquals(3L, filtered[2].id)
        assertEquals(1L, filtered[3].id)
    }

    @Test
    fun toggleReminder_callsSync() = runTest(testDispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        val reminder = Reminder(id = 1, title = "Active", triggerType = ReminderTriggerType.FIXED_TIME, isEnabled = true, isCompleted = false)

        viewModel.toggleReminder(reminder)
        testDispatcher.scheduler.advanceUntilIdle()

        io.mockk.coVerify(atLeast = 1) { reminderSyncManager.sync() }
    }

    @Test
    fun uiState_whenNonCriticalPermissionMissing_showsNonCriticalWarning() = runTest(testDispatcher) {
        every { permissionChecker.hasNonCriticalPermissions() } returns false
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.hasPermissionWarning)
        assertFalse("Should be non-critical warning", state.isCriticalPermissionWarning)
        assertFalse(state.isPermissionWarningDismissed)
    }

    @Test
    fun dismissPermissionWarning_setsDismissedToTrue() = runTest(testDispatcher) {
        every { permissionChecker.hasNonCriticalPermissions() } returns false
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.hasPermissionWarning)
        assertFalse(viewModel.uiState.value.isPermissionWarningDismissed)

        viewModel.dismissPermissionWarning()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isPermissionWarningDismissed)
    }
}

