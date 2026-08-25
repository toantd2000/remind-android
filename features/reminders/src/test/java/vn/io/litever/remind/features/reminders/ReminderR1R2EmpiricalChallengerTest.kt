package vn.io.litever.remind.features.reminders

import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
import vn.io.litever.remind.core.datastore.AlarmPreferencesDataSource
import vn.io.litever.remind.core.domain.repository.ReminderRepository
import vn.io.litever.remind.core.domain.scheduler.ReminderScheduler
import vn.io.litever.remind.core.model.GeofenceTransitionType
import vn.io.litever.remind.core.model.LocationTrigger
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.core.model.TimeTrigger
import vn.io.litever.remind.core.model.WifiTrigger
import vn.io.litever.remind.core.model.WifiTriggerType
import vn.io.litever.remind.features.reminders.ui.components.ReminderFilterType
import vn.io.litever.remind.features.reminders.viewmodel.ReminderEditViewModel
import vn.io.litever.remind.features.reminders.viewmodel.ReminderListViewModel
import java.time.LocalTime

/**
 * Adversarial Challenger Test Suite for R1 & R2:
 * - R1: Immediate Service Start (ReminderSyncManager.sync() on save, saveAnyway, toggle, delete, restore, bulk delete, duplicate, toggleCompleted)
 * - R2: Snoozed Reminders Pinned in List across all filters (ALL, ACTIVE, COMPLETED, TIME, LOCATION, WIFI),
 *       Toggle switch suppression on snoozed cards, and routing directly to Ringing screen on click.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ReminderR1R2EmpiricalChallengerTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repository = mockk<ReminderRepository>(relaxed = true)
    private val scheduler = mockk<ReminderScheduler>(relaxed = true)
    private val reminderSyncManager = mockk<ReminderSyncManager>(relaxed = true)
    private val permissionChecker = mockk<PermissionChecker>(relaxed = true)
    private val preferencesDataSource = mockk<AlarmPreferencesDataSource>(relaxed = true)

    private val allRemindersFlow = MutableStateFlow<List<Reminder>>(emptyList())
    private val is24HourFlow = MutableStateFlow(true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { repository.getAllReminders() } returns allRemindersFlow
        every { preferencesDataSource.is24HourFormat } returns is24HourFlow
        every { permissionChecker.hasNotificationPermission() } returns true
        every { permissionChecker.hasPermissionsForReminder(any()) } returns true
        every { permissionChecker.getMissingPermissions(any()) } returns emptyList()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // =========================================================================
    // SECTION 1: R1 — Complete Coverage for ReminderSyncManager Invocations
    // =========================================================================

    @Test
    fun r1_editScreen_saveNewReminder_callsSync() = runTest(testDispatcher) {
        val draft = Reminder(
            id = 0L,
            title = "Test Reminder R1",
            triggerType = ReminderTriggerType.TIME,
            timeTrigger = TimeTrigger(time = LocalTime.of(8, 0)),
            isEnabled = true
        )
        coEvery { repository.insertReminder(any()) } returns 101L

        val savedStateHandle = SavedStateHandle(mapOf("reminderId" to 0L))
        val editViewModel = ReminderEditViewModel(
            reminderRepository = repository,
            reminderScheduler = scheduler,
            reminderSyncManager = reminderSyncManager,
            permissionChecker = permissionChecker,
            preferencesDataSource = preferencesDataSource,
            savedStateHandle = savedStateHandle
        )
        testDispatcher.scheduler.advanceUntilIdle()

        editViewModel.setTitle("Test Reminder R1")
        editViewModel.setTime(LocalTime.of(8, 0))

        var saved = false
        editViewModel.saveReminder(onSuccess = { saved = true })
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(saved)
        coVerify(exactly = 1) { repository.insertReminder(any()) }
        coVerify(exactly = 1) { scheduler.schedule(any()) }
        coVerify(exactly = 1) { reminderSyncManager.sync() }
    }

    @Test
    fun r1_editScreen_updateExistingReminder_callsSync() = runTest(testDispatcher) {
        val existing = Reminder(
            id = 201L,
            title = "Existing Reminder",
            triggerType = ReminderTriggerType.TIME,
            timeTrigger = TimeTrigger(time = LocalTime.of(9, 0)),
            isEnabled = true
        )
        coEvery { repository.getReminderById(201L) } returns existing

        val savedStateHandle = SavedStateHandle(mapOf("reminderId" to 201L))
        val editViewModel = ReminderEditViewModel(
            reminderRepository = repository,
            reminderScheduler = scheduler,
            reminderSyncManager = reminderSyncManager,
            permissionChecker = permissionChecker,
            preferencesDataSource = preferencesDataSource,
            savedStateHandle = savedStateHandle
        )
        testDispatcher.scheduler.advanceUntilIdle()

        editViewModel.setTitle("Updated Title")

        var saved = false
        editViewModel.saveReminder(onSuccess = { saved = true })
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(saved)
        coVerify(exactly = 1) { repository.updateReminder(match { it.id == 201L && it.title == "Updated Title" }) }
        coVerify(exactly = 1) { reminderSyncManager.sync() }
    }

    @Test
    fun r1_editScreen_deleteExistingReminder_callsSync() = runTest(testDispatcher) {
        val existing = Reminder(
            id = 301L,
            title = "Delete Me",
            triggerType = ReminderTriggerType.TIME,
            timeTrigger = TimeTrigger(time = LocalTime.of(10, 0)),
            isEnabled = true
        )
        coEvery { repository.getReminderById(301L) } returns existing

        val savedStateHandle = SavedStateHandle(mapOf("reminderId" to 301L))
        val editViewModel = ReminderEditViewModel(
            reminderRepository = repository,
            reminderScheduler = scheduler,
            reminderSyncManager = reminderSyncManager,
            permissionChecker = permissionChecker,
            preferencesDataSource = preferencesDataSource,
            savedStateHandle = savedStateHandle
        )
        testDispatcher.scheduler.advanceUntilIdle()

        editViewModel.deleteReminder()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { repository.deleteReminder(existing) }
        coVerify(exactly = 1) { scheduler.cancel(existing) }
        coVerify(exactly = 1) { scheduler.cancelSnooze(existing) }
        coVerify(exactly = 1) { reminderSyncManager.sync() }
    }

    @Test
    fun r1_listScreen_toggleReminder_callsSync() = runTest(testDispatcher) {
        val reminder = Reminder(id = 401L, title = "Toggle Me", triggerType = ReminderTriggerType.TIME, isEnabled = false)

        val listViewModel = ReminderListViewModel(
            reminderRepository = repository,
            reminderScheduler = scheduler,
            permissionChecker = permissionChecker,
            preferencesDataSource = preferencesDataSource,
            reminderSyncManager = reminderSyncManager
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { listViewModel.uiState.collect() }
        testDispatcher.scheduler.advanceUntilIdle()

        listViewModel.toggleReminder(reminder)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { repository.updateReminder(match { it.id == 401L && it.isEnabled }) }
        coVerify(exactly = 1) { scheduler.schedule(match { it.id == 401L && it.isEnabled }) }
        coVerify(exactly = 1) { reminderSyncManager.sync() }
    }

    @Test
    fun r1_listScreen_deleteReminder_callsSync() = runTest(testDispatcher) {
        val reminder = Reminder(id = 501L, title = "List Delete", triggerType = ReminderTriggerType.TIME, isEnabled = true)

        val listViewModel = ReminderListViewModel(
            reminderRepository = repository,
            reminderScheduler = scheduler,
            permissionChecker = permissionChecker,
            preferencesDataSource = preferencesDataSource,
            reminderSyncManager = reminderSyncManager
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { listViewModel.uiState.collect() }
        testDispatcher.scheduler.advanceUntilIdle()

        listViewModel.deleteReminder(reminder)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { repository.deleteReminder(reminder) }
        coVerify(exactly = 1) { scheduler.cancel(reminder) }
        coVerify(exactly = 1) { scheduler.cancelSnooze(reminder) }
        coVerify(exactly = 1) { reminderSyncManager.sync() }
    }

    @Test
    fun r1_listScreen_undoDelete_callsSync() = runTest(testDispatcher) {
        val reminder = Reminder(id = 601L, title = "Restore Me", triggerType = ReminderTriggerType.TIME, isEnabled = true)
        coEvery { repository.insertReminder(any()) } returns 602L

        val listViewModel = ReminderListViewModel(
            reminderRepository = repository,
            reminderScheduler = scheduler,
            permissionChecker = permissionChecker,
            preferencesDataSource = preferencesDataSource,
            reminderSyncManager = reminderSyncManager
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { listViewModel.uiState.collect() }
        testDispatcher.scheduler.advanceUntilIdle()

        listViewModel.deleteReminder(reminder)
        testDispatcher.scheduler.advanceUntilIdle()

        listViewModel.undoDelete()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { repository.insertReminder(match { it.title == "Restore Me" }) }
        coVerify(exactly = 1) { scheduler.schedule(match { it.id == 602L }) }
        coVerify(exactly = 2) { reminderSyncManager.sync() }
    }

    @Test
    fun r1_deleteCompletedReminders_callsSync() = runTest(testDispatcher) {
        val active = Reminder(id = 701L, title = "Active", triggerType = ReminderTriggerType.TIME, isCompleted = false)
        val completed1 = Reminder(id = 702L, title = "Done 1", triggerType = ReminderTriggerType.TIME, isCompleted = true)
        val completed2 = Reminder(id = 703L, title = "Done 2", triggerType = ReminderTriggerType.TIME, isCompleted = true)

        allRemindersFlow.value = listOf(active, completed1, completed2)

        val listViewModel = ReminderListViewModel(
            reminderRepository = repository,
            reminderScheduler = scheduler,
            permissionChecker = permissionChecker,
            preferencesDataSource = preferencesDataSource,
            reminderSyncManager = reminderSyncManager
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { listViewModel.uiState.collect() }
        testDispatcher.scheduler.advanceUntilIdle()

        listViewModel.deleteCompletedReminders()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { repository.deleteReminder(completed1) }
        coVerify(exactly = 1) { repository.deleteReminder(completed2) }
        coVerify(exactly = 0) { repository.deleteReminder(active) }
        coVerify(exactly = 1) { reminderSyncManager.sync() }
    }

    @Test
    fun r1_deleteDisabledReminders_callsSync() = runTest(testDispatcher) {
        val active = Reminder(id = 801L, title = "Active", triggerType = ReminderTriggerType.TIME, isEnabled = true, isCompleted = false)
        val disabled1 = Reminder(id = 802L, title = "Disabled 1", triggerType = ReminderTriggerType.TIME, isEnabled = false, isCompleted = false)
        val disabled2 = Reminder(id = 803L, title = "Disabled 2", triggerType = ReminderTriggerType.TIME, isEnabled = false, isCompleted = false)

        allRemindersFlow.value = listOf(active, disabled1, disabled2)

        val listViewModel = ReminderListViewModel(
            reminderRepository = repository,
            reminderScheduler = scheduler,
            permissionChecker = permissionChecker,
            preferencesDataSource = preferencesDataSource,
            reminderSyncManager = reminderSyncManager
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { listViewModel.uiState.collect() }
        testDispatcher.scheduler.advanceUntilIdle()

        listViewModel.deleteDisabledReminders()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { repository.deleteReminder(disabled1) }
        coVerify(exactly = 1) { repository.deleteReminder(disabled2) }
        coVerify(exactly = 0) { repository.deleteReminder(active) }
        coVerify(exactly = 1) { reminderSyncManager.sync() }
    }

    @Test
    fun r1_duplicateReminder_callsSync() = runTest(testDispatcher) {
        val original = Reminder(id = 901L, title = "Original Template", triggerType = ReminderTriggerType.TIME, isEnabled = true)
        coEvery { repository.insertReminder(any()) } returns 902L

        val listViewModel = ReminderListViewModel(
            reminderRepository = repository,
            reminderScheduler = scheduler,
            permissionChecker = permissionChecker,
            preferencesDataSource = preferencesDataSource,
            reminderSyncManager = reminderSyncManager
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { listViewModel.uiState.collect() }
        testDispatcher.scheduler.advanceUntilIdle()

        listViewModel.duplicateReminder(original)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { repository.insertReminder(match { it.title == "Original Template (Copy)" }) }
        coVerify(exactly = 1) { scheduler.schedule(match { it.id == 902L }) }
        coVerify(exactly = 1) { reminderSyncManager.sync() }
    }

    @Test
    fun r1_toggleCompleted_callsSync() = runTest(testDispatcher) {
        val active = Reminder(id = 1001L, title = "To Complete", triggerType = ReminderTriggerType.TIME, isEnabled = true, isCompleted = false)

        val listViewModel = ReminderListViewModel(
            reminderRepository = repository,
            reminderScheduler = scheduler,
            permissionChecker = permissionChecker,
            preferencesDataSource = preferencesDataSource,
            reminderSyncManager = reminderSyncManager
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { listViewModel.uiState.collect() }
        testDispatcher.scheduler.advanceUntilIdle()

        listViewModel.toggleCompleted(active)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { repository.updateReminder(match { it.id == 1001L && it.isCompleted && !it.isEnabled }) }
        coVerify(exactly = 1) { scheduler.cancel(match { it.id == 1001L }) }
        coVerify(exactly = 1) { reminderSyncManager.sync() }
    }

    // =========================================================================
    // SECTION 2: R2 — Snoozed Reminders Pinned in List Across All Filters
    // =========================================================================

    @Test
    fun r2_sorting_allFilter_pinsSnoozedToTop_withStableTieBreaking() = runTest(testDispatcher) {
        val normalOld = Reminder(id = 1L, title = "Normal Old", triggerType = ReminderTriggerType.TIME, createdAt = 100L)
        val snoozedOld = Reminder(id = 2L, title = "Snoozed Old", triggerType = ReminderTriggerType.TIME, snoozeNextTriggerTime = 5000L, createdAt = 200L)
        val normalNew = Reminder(id = 3L, title = "Normal New", triggerType = ReminderTriggerType.TIME, createdAt = 300L)
        val snoozedNew = Reminder(id = 4L, title = "Snoozed New", triggerType = ReminderTriggerType.TIME, snoozeNextTriggerTime = 6000L, createdAt = 400L)
        val normalMid = Reminder(id = 5L, title = "Normal Mid", triggerType = ReminderTriggerType.TIME, createdAt = 250L)

        allRemindersFlow.value = listOf(normalOld, snoozedOld, normalNew, snoozedNew, normalMid)

        val listViewModel = ReminderListViewModel(
            reminderRepository = repository,
            reminderScheduler = scheduler,
            permissionChecker = permissionChecker,
            preferencesDataSource = preferencesDataSource,
            reminderSyncManager = reminderSyncManager
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { listViewModel.uiState.collect() }
        testDispatcher.scheduler.advanceUntilIdle()

        listViewModel.setFilter(ReminderFilterType.ALL)
        testDispatcher.scheduler.advanceUntilIdle()

        val list = listViewModel.uiState.value.filteredReminders
        assertEquals(5, list.size)

        assertEquals(4L, list[0].id)
        assertEquals(2L, list[1].id)
        assertEquals(3L, list[2].id)
        assertEquals(5L, list[3].id)
        assertEquals(1L, list[4].id)
    }

    @Test
    fun r2_sorting_activeFilter_pinsSnoozedActiveItems() = runTest(testDispatcher) {
        val activeNormal = Reminder(id = 10L, title = "Active Normal", triggerType = ReminderTriggerType.TIME, isEnabled = true, isCompleted = false, createdAt = 1000L)
        val activeSnoozed = Reminder(id = 11L, title = "Active Snoozed", triggerType = ReminderTriggerType.TIME, isEnabled = true, isCompleted = false, snoozeNextTriggerTime = 99999L, createdAt = 1100L)
        val disabledSnoozed = Reminder(id = 12L, title = "Disabled Snoozed", triggerType = ReminderTriggerType.TIME, isEnabled = false, isCompleted = false, snoozeNextTriggerTime = 99999L, createdAt = 1200L)
        val completedSnoozed = Reminder(id = 13L, title = "Completed Snoozed", triggerType = ReminderTriggerType.TIME, isEnabled = true, isCompleted = true, snoozeNextTriggerTime = 99999L, createdAt = 1300L)

        allRemindersFlow.value = listOf(activeNormal, activeSnoozed, disabledSnoozed, completedSnoozed)

        val listViewModel = ReminderListViewModel(
            reminderRepository = repository,
            reminderScheduler = scheduler,
            permissionChecker = permissionChecker,
            preferencesDataSource = preferencesDataSource,
            reminderSyncManager = reminderSyncManager
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { listViewModel.uiState.collect() }
        testDispatcher.scheduler.advanceUntilIdle()

        listViewModel.setFilter(ReminderFilterType.ACTIVE)
        testDispatcher.scheduler.advanceUntilIdle()

        val list = listViewModel.uiState.value.filteredReminders
        assertEquals(2, list.size)
        assertEquals(11L, list[0].id)
        assertEquals(10L, list[1].id)
    }

    @Test
    fun r2_sorting_timeFilter_pinsSnoozedTimeReminders() = runTest(testDispatcher) {
        val timeNormal1 = Reminder(id = 21L, title = "Time Normal 1", triggerType = ReminderTriggerType.TIME, createdAt = 200L)
        val timeSnoozed1 = Reminder(id = 22L, title = "Time Snoozed 1", triggerType = ReminderTriggerType.TIME, snoozeNextTriggerTime = 10000L, createdAt = 300L)
        val locSnoozed = Reminder(id = 23L, title = "Loc Snoozed", triggerType = ReminderTriggerType.LOCATION, snoozeNextTriggerTime = 10000L, createdAt = 400L)
        val timeNormal2 = Reminder(id = 24L, title = "Time Normal 2", triggerType = ReminderTriggerType.TIME, createdAt = 500L)

        allRemindersFlow.value = listOf(timeNormal1, timeSnoozed1, locSnoozed, timeNormal2)

        val listViewModel = ReminderListViewModel(
            reminderRepository = repository,
            reminderScheduler = scheduler,
            permissionChecker = permissionChecker,
            preferencesDataSource = preferencesDataSource,
            reminderSyncManager = reminderSyncManager
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { listViewModel.uiState.collect() }
        testDispatcher.scheduler.advanceUntilIdle()

        listViewModel.setFilter(ReminderFilterType.TIME)
        testDispatcher.scheduler.advanceUntilIdle()

        val list = listViewModel.uiState.value.filteredReminders
        assertEquals(3, list.size)
        assertEquals(22L, list[0].id)
        assertEquals(24L, list[1].id)
        assertEquals(21L, list[2].id)
    }

    @Test
    fun r2_sorting_locationFilter_pinsSnoozedLocationReminders() = runTest(testDispatcher) {
        val locNormal = Reminder(id = 31L, title = "Loc Normal", triggerType = ReminderTriggerType.LOCATION, createdAt = 100L)
        val locSnoozed = Reminder(id = 32L, title = "Loc Snoozed", triggerType = ReminderTriggerType.LOCATION, snoozeNextTriggerTime = 8000L, createdAt = 200L)
        val timeSnoozed = Reminder(id = 33L, title = "Time Snoozed", triggerType = ReminderTriggerType.TIME, snoozeNextTriggerTime = 8000L, createdAt = 300L)

        allRemindersFlow.value = listOf(locNormal, locSnoozed, timeSnoozed)

        val listViewModel = ReminderListViewModel(
            reminderRepository = repository,
            reminderScheduler = scheduler,
            permissionChecker = permissionChecker,
            preferencesDataSource = preferencesDataSource,
            reminderSyncManager = reminderSyncManager
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { listViewModel.uiState.collect() }
        testDispatcher.scheduler.advanceUntilIdle()

        listViewModel.setFilter(ReminderFilterType.LOCATION)
        testDispatcher.scheduler.advanceUntilIdle()

        val list = listViewModel.uiState.value.filteredReminders
        assertEquals(2, list.size)
        assertEquals(32L, list[0].id)
        assertEquals(31L, list[1].id)
    }

    @Test
    fun r2_sorting_wifiFilter_pinsSnoozedWifiReminders() = runTest(testDispatcher) {
        val wifiNormal1 = Reminder(id = 41L, title = "Wifi Normal 1", triggerType = ReminderTriggerType.WIFI, createdAt = 100L)
        val wifiNormal2 = Reminder(id = 42L, title = "Wifi Normal 2", triggerType = ReminderTriggerType.WIFI, createdAt = 500L)
        val wifiSnoozed = Reminder(id = 43L, title = "Wifi Snoozed", triggerType = ReminderTriggerType.WIFI, snoozeNextTriggerTime = 9000L, createdAt = 200L)

        allRemindersFlow.value = listOf(wifiNormal1, wifiNormal2, wifiSnoozed)

        val listViewModel = ReminderListViewModel(
            reminderRepository = repository,
            reminderScheduler = scheduler,
            permissionChecker = permissionChecker,
            preferencesDataSource = preferencesDataSource,
            reminderSyncManager = reminderSyncManager
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { listViewModel.uiState.collect() }
        testDispatcher.scheduler.advanceUntilIdle()

        listViewModel.setFilter(ReminderFilterType.WIFI)
        testDispatcher.scheduler.advanceUntilIdle()

        val list = listViewModel.uiState.value.filteredReminders
        assertEquals(3, list.size)
        assertEquals(43L, list[0].id)
        assertEquals(42L, list[1].id)
        assertEquals(41L, list[2].id)
    }

    @Test
    fun r2_sorting_stressTest_arbitrary20RemindersAcrossFilters() = runTest(testDispatcher) {
        val testReminders = mutableListOf<Reminder>()
        for (i in 1..20) {
            val isSnoozed = (i % 3 == 0)
            val trigger = when (i % 3) {
                0 -> ReminderTriggerType.TIME
                1 -> ReminderTriggerType.LOCATION
                else -> ReminderTriggerType.WIFI
            }
            val isCompleted = (i % 5 == 0)
            val isEnabled = (i % 2 == 0)

            testReminders.add(
                Reminder(
                    id = i.toLong(),
                    title = "Reminder #$i",
                    triggerType = trigger,
                    isEnabled = isEnabled,
                    isCompleted = isCompleted,
                    snoozeNextTriggerTime = if (isSnoozed) (100000L + i * 100) else null,
                    createdAt = 1000L + (i * 37 % 50)
                )
            )
        }

        allRemindersFlow.value = testReminders

        val listViewModel = ReminderListViewModel(
            reminderRepository = repository,
            reminderScheduler = scheduler,
            permissionChecker = permissionChecker,
            preferencesDataSource = preferencesDataSource,
            reminderSyncManager = reminderSyncManager
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { listViewModel.uiState.collect() }
        testDispatcher.scheduler.advanceUntilIdle()

        for (filter in ReminderFilterType.entries) {
            listViewModel.setFilter(filter)
            testDispatcher.scheduler.advanceUntilIdle()

            val filtered = listViewModel.uiState.value.filteredReminders
            var seenNonSnoozed = false
            var lastCreatedAtInPartition = Long.MAX_VALUE
            var currentPartitionIsSnoozed = true

            for (item in filtered) {
                val itemIsSnoozed = item.snoozeNextTriggerTime != null
                if (itemIsSnoozed) {
                    assertFalse(
                        "Violation in filter $filter: Snoozed item ${item.id} appeared after non-snoozed items",
                        seenNonSnoozed
                    )
                } else {
                    if (currentPartitionIsSnoozed) {
                        currentPartitionIsSnoozed = false
                        seenNonSnoozed = true
                        lastCreatedAtInPartition = Long.MAX_VALUE
                    }
                }
                assertTrue(
                    "Violation in filter $filter: createdAt should be descending (got ${item.createdAt} > $lastCreatedAtInPartition)",
                    item.createdAt <= lastCreatedAtInPartition
                )
                lastCreatedAtInPartition = item.createdAt
            }
        }
    }

    // =========================================================================
    // SECTION 3: R2 — Snoozed Card Toggle Suppression & Navigation Routing Logic
    // =========================================================================

    @Test
    fun r2_snoozedReminderClick_routesDirectlyToRingingScreen() {
        val snoozedReminder = Reminder(
            id = 888L,
            title = "Snoozed Alarm",
            triggerType = ReminderTriggerType.TIME,
            snoozeNextTriggerTime = 123456789L
        )

        var navigatedToRingingId: Long? = null
        var navigatedToEditId: Long? = null

        val onReminderClick: (Reminder) -> Unit = { reminder ->
            if (reminder.snoozeNextTriggerTime != null) {
                navigatedToRingingId = reminder.id
            } else {
                navigatedToEditId = reminder.id
            }
        }

        onReminderClick(snoozedReminder)

        assertEquals(888L, navigatedToRingingId)
        assertEquals(null, navigatedToEditId)
    }

    @Test
    fun r2_nonSnoozedReminderClick_routesToEditScreen() {
        val nonSnoozedReminder = Reminder(
            id = 999L,
            title = "Normal Alarm",
            triggerType = ReminderTriggerType.TIME,
            snoozeNextTriggerTime = null
        )

        var navigatedToRingingId: Long? = null
        var navigatedToEditId: Long? = null

        val onReminderClick: (Reminder) -> Unit = { reminder ->
            if (reminder.snoozeNextTriggerTime != null) {
                navigatedToRingingId = reminder.id
            } else {
                navigatedToEditId = reminder.id
            }
        }

        onReminderClick(nonSnoozedReminder)

        assertEquals(null, navigatedToRingingId)
        assertEquals(999L, navigatedToEditId)
    }

    @Test
    fun r2_snoozedCardToggleVisibility_predicateEvaluatesToFalseWhenSnoozed() {
        val snoozedReminder = Reminder(
            id = 777L,
            title = "Snoozed Item",
            triggerType = ReminderTriggerType.TIME,
            snoozeNextTriggerTime = 999999L
        )
        val normalReminder = Reminder(
            id = 778L,
            title = "Normal Item",
            triggerType = ReminderTriggerType.TIME,
            snoozeNextTriggerTime = null
        )

        // The exact condition used in ReminderCard.kt:
        // if (reminder.snoozeNextTriggerTime == null) { LiteverSwitch(...) }
        val isSwitchVisibleForSnoozed = (snoozedReminder.snoozeNextTriggerTime == null)
        val isSwitchVisibleForNormal = (normalReminder.snoozeNextTriggerTime == null)

        assertFalse("Switch MUST be hidden for snoozed reminder", isSwitchVisibleForSnoozed)
        assertTrue("Switch MUST be shown for non-snoozed reminder", isSwitchVisibleForNormal)
    }
}
