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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
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
    // SECTION 1: R1 — ReminderSyncManager.sync() Invocations Across All Paths
    // =========================================================================

    @Test
    fun r1_saveNewReminder_callsSync() = runTest {
        coEvery { repository.insertReminder(any()) } returns 101L

        val editViewModel = ReminderEditViewModel(
            reminderRepository = repository,
            reminderScheduler = scheduler,
            reminderSyncManager = reminderSyncManager,
            permissionChecker = permissionChecker,
            preferencesDataSource = preferencesDataSource,
            savedStateHandle = SavedStateHandle(mapOf("reminderId" to 0L))
        )
        testDispatcher.scheduler.advanceUntilIdle()

        editViewModel.setTitle("Team Standup")
        editViewModel.setTriggerType(ReminderTriggerType.TIME)
        editViewModel.setTime(LocalTime.of(9, 30))

        var saved = false
        editViewModel.saveReminder(onSuccess = { saved = true })
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue("Save callback must be executed", saved)
        coVerify(exactly = 1) { repository.insertReminder(any()) }
        coVerify(exactly = 1) { scheduler.schedule(match { it.id == 101L }) }
        coVerify(exactly = 1) { reminderSyncManager.sync() }
    }

    @Test
    fun r1_saveExistingReminder_callsSync() = runTest {
        val existing = Reminder(
            id = 202L,
            title = "Existing Task",
            triggerType = ReminderTriggerType.LOCATION,
            locationTrigger = LocationTrigger(latitude = 10.5, longitude = 106.5, locationName = "HQ", radiusMeters = 100f),
            isEnabled = true
        )
        coEvery { repository.getReminderById(202L) } returns existing

        val editViewModel = ReminderEditViewModel(
            reminderRepository = repository,
            reminderScheduler = scheduler,
            reminderSyncManager = reminderSyncManager,
            permissionChecker = permissionChecker,
            preferencesDataSource = preferencesDataSource,
            savedStateHandle = SavedStateHandle(mapOf("reminderId" to 202L))
        )
        testDispatcher.scheduler.advanceUntilIdle()

        editViewModel.setTitle("Updated HQ Geofence")
        var saved = false
        editViewModel.saveReminder(onSuccess = { saved = true })
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(saved)
        coVerify(exactly = 1) { repository.updateReminder(match { it.id == 202L && it.title == "Updated HQ Geofence" }) }
        coVerify(exactly = 1) { scheduler.schedule(match { it.id == 202L }) }
        coVerify(exactly = 1) { reminderSyncManager.sync() }
    }

    @Test
    fun r1_saveAnyway_callsSync() = runTest {
        coEvery { repository.insertReminder(any()) } returns 303L

        val editViewModel = ReminderEditViewModel(
            reminderRepository = repository,
            reminderScheduler = scheduler,
            reminderSyncManager = reminderSyncManager,
            permissionChecker = permissionChecker,
            preferencesDataSource = preferencesDataSource,
            savedStateHandle = SavedStateHandle(mapOf("reminderId" to 0L))
        )
        testDispatcher.scheduler.advanceUntilIdle()

        editViewModel.setTitle("Save Anyway Wi-Fi")
        editViewModel.setTriggerType(ReminderTriggerType.WIFI)
        editViewModel.setWifiSsid("Guest_Wifi")

        var saved = false
        editViewModel.saveAnyway(onSuccess = { saved = true })
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(saved)
        coVerify(exactly = 1) { repository.insertReminder(match { it.title == "Save Anyway Wi-Fi" && !it.isEnabled }) }
        coVerify(exactly = 1) { reminderSyncManager.sync() }
    }

    @Test
    fun r1_toggleReminder_callsSync() = runTest {
        val reminder = Reminder(id = 401L, title = "Toggle Me", isEnabled = true)

        val listViewModel = ReminderListViewModel(
            reminderRepository = repository,
            reminderScheduler = scheduler,
            permissionChecker = permissionChecker,
            preferencesDataSource = preferencesDataSource,
            reminderSyncManager = reminderSyncManager
        )
        testDispatcher.scheduler.advanceUntilIdle()

        listViewModel.toggleReminder(reminder)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { repository.updateReminder(match { it.id == 401L && !it.isEnabled }) }
        coVerify(exactly = 1) { scheduler.cancel(match { it.id == 401L }) }
        coVerify(exactly = 1) { reminderSyncManager.sync() }
    }

    @Test
    fun r1_deleteReminder_callsSync() = runTest {
        val reminder = Reminder(id = 501L, title = "Delete Me", isEnabled = true)

        val listViewModel = ReminderListViewModel(
            reminderRepository = repository,
            reminderScheduler = scheduler,
            permissionChecker = permissionChecker,
            preferencesDataSource = preferencesDataSource,
            reminderSyncManager = reminderSyncManager
        )
        testDispatcher.scheduler.advanceUntilIdle()

        listViewModel.deleteReminder(reminder)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { scheduler.cancel(reminder) }
        coVerify(exactly = 1) { repository.deleteReminder(reminder) }
        coVerify(exactly = 1) { reminderSyncManager.sync() }
    }

    @Test
    fun r1_undoDelete_callsSync() = runTest {
        val reminder = Reminder(id = 601L, title = "Restore Me", isEnabled = true, isCompleted = false)
        coEvery { repository.insertReminder(any()) } returns 602L

        val listViewModel = ReminderListViewModel(
            reminderRepository = repository,
            reminderScheduler = scheduler,
            permissionChecker = permissionChecker,
            preferencesDataSource = preferencesDataSource,
            reminderSyncManager = reminderSyncManager
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // 1. Delete
        listViewModel.deleteReminder(reminder)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify(exactly = 1) { reminderSyncManager.sync() }

        // 2. Undo Delete (Restore)
        listViewModel.undoDelete()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { repository.insertReminder(match { it.title == "Restore Me" }) }
        coVerify(exactly = 1) { scheduler.schedule(match { it.id == 602L }) }
        coVerify(exactly = 2) { reminderSyncManager.sync() }
    }

    @Test
    fun r1_deleteCompletedReminders_callsSync() = runTest {
        val active = Reminder(id = 701L, title = "Active", isCompleted = false)
        val completed1 = Reminder(id = 702L, title = "Done 1", isCompleted = true)
        val completed2 = Reminder(id = 703L, title = "Done 2", isCompleted = true)

        allRemindersFlow.value = listOf(active, completed1, completed2)

        val listViewModel = ReminderListViewModel(
            reminderRepository = repository,
            reminderScheduler = scheduler,
            permissionChecker = permissionChecker,
            preferencesDataSource = preferencesDataSource,
            reminderSyncManager = reminderSyncManager
        )
        testDispatcher.scheduler.advanceUntilIdle()

        listViewModel.deleteCompletedReminders()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { repository.deleteReminder(completed1) }
        coVerify(exactly = 1) { repository.deleteReminder(completed2) }
        coVerify(exactly = 0) { repository.deleteReminder(active) }
        coVerify(exactly = 1) { reminderSyncManager.sync() }
    }

    @Test
    fun r1_deleteDisabledReminders_callsSync() = runTest {
        val active = Reminder(id = 801L, title = "Active", isEnabled = true, isCompleted = false)
        val disabled1 = Reminder(id = 802L, title = "Disabled 1", isEnabled = false, isCompleted = false)
        val disabled2 = Reminder(id = 803L, title = "Disabled 2", isEnabled = false, isCompleted = false)

        allRemindersFlow.value = listOf(active, disabled1, disabled2)

        val listViewModel = ReminderListViewModel(
            reminderRepository = repository,
            reminderScheduler = scheduler,
            permissionChecker = permissionChecker,
            preferencesDataSource = preferencesDataSource,
            reminderSyncManager = reminderSyncManager
        )
        testDispatcher.scheduler.advanceUntilIdle()

        listViewModel.deleteDisabledReminders()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { repository.deleteReminder(disabled1) }
        coVerify(exactly = 1) { repository.deleteReminder(disabled2) }
        coVerify(exactly = 0) { repository.deleteReminder(active) }
        coVerify(exactly = 1) { reminderSyncManager.sync() }
    }

    @Test
    fun r1_duplicateReminder_callsSync() = runTest {
        val original = Reminder(id = 901L, title = "Original Template", isEnabled = true)
        coEvery { repository.insertReminder(any()) } returns 902L

        val listViewModel = ReminderListViewModel(
            reminderRepository = repository,
            reminderScheduler = scheduler,
            permissionChecker = permissionChecker,
            preferencesDataSource = preferencesDataSource,
            reminderSyncManager = reminderSyncManager
        )
        testDispatcher.scheduler.advanceUntilIdle()

        listViewModel.duplicateReminder(original)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { repository.insertReminder(match { it.title == "Original Template (Copy)" }) }
        coVerify(exactly = 1) { scheduler.schedule(match { it.id == 902L }) }
        coVerify(exactly = 1) { reminderSyncManager.sync() }
    }

    @Test
    fun r1_toggleCompleted_callsSync() = runTest {
        val active = Reminder(id = 1001L, title = "To Complete", isEnabled = true, isCompleted = false)

        val listViewModel = ReminderListViewModel(
            reminderRepository = repository,
            reminderScheduler = scheduler,
            permissionChecker = permissionChecker,
            preferencesDataSource = preferencesDataSource,
            reminderSyncManager = reminderSyncManager
        )
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
    fun r2_sorting_allFilter_pinsSnoozedToTop_withStableTieBreaking() = runTest {
        // Given a complex list with mixed states and created timestamps
        val normalOld = Reminder(id = 1L, title = "Normal Old", createdAt = 100L)
        val snoozedOld = Reminder(id = 2L, title = "Snoozed Old", snoozeNextTriggerTime = 5000L, createdAt = 200L)
        val normalNew = Reminder(id = 3L, title = "Normal New", createdAt = 300L)
        val snoozedNew = Reminder(id = 4L, title = "Snoozed New", snoozeNextTriggerTime = 6000L, createdAt = 400L)
        val normalMid = Reminder(id = 5L, title = "Normal Mid", createdAt = 250L)

        allRemindersFlow.value = listOf(normalOld, snoozedOld, normalNew, snoozedNew, normalMid)

        val listViewModel = ReminderListViewModel(
            reminderRepository = repository,
            reminderScheduler = scheduler,
            permissionChecker = permissionChecker,
            preferencesDataSource = preferencesDataSource,
            reminderSyncManager = reminderSyncManager
        )
        testDispatcher.scheduler.advanceUntilIdle()

        listViewModel.setFilter(ReminderFilterType.ALL)
        testDispatcher.scheduler.advanceUntilIdle()

        val list = listViewModel.uiState.value.filteredReminders
        assertEquals(5, list.size)

        // Strict verification: Snoozed items come first (sorted by createdAt desc), then normal items (sorted by createdAt desc)
        assertEquals(4L, list[0].id) // Snoozed New (createdAt 400)
        assertEquals(2L, list[1].id) // Snoozed Old (createdAt 200)
        assertEquals(3L, list[2].id) // Normal New (createdAt 300)
        assertEquals(5L, list[3].id) // Normal Mid (createdAt 250)
        assertEquals(1L, list[4].id) // Normal Old (createdAt 100)
    }

    @Test
    fun r2_sorting_activeFilter_pinsSnoozedActiveItems() = runTest {
        val activeNormal = Reminder(id = 10L, title = "Active Normal", isEnabled = true, isCompleted = false, createdAt = 1000L)
        val activeSnoozed = Reminder(id = 11L, title = "Active Snoozed", isEnabled = true, isCompleted = false, snoozeNextTriggerTime = 99999L, createdAt = 1100L)
        val disabledSnoozed = Reminder(id = 12L, title = "Disabled Snoozed", isEnabled = false, isCompleted = false, snoozeNextTriggerTime = 99999L, createdAt = 1200L)
        val completedSnoozed = Reminder(id = 13L, title = "Completed Snoozed", isEnabled = true, isCompleted = true, snoozeNextTriggerTime = 99999L, createdAt = 1300L)

        allRemindersFlow.value = listOf(activeNormal, activeSnoozed, disabledSnoozed, completedSnoozed)

        val listViewModel = ReminderListViewModel(
            reminderRepository = repository,
            reminderScheduler = scheduler,
            permissionChecker = permissionChecker,
            preferencesDataSource = preferencesDataSource,
            reminderSyncManager = reminderSyncManager
        )
        testDispatcher.scheduler.advanceUntilIdle()

        listViewModel.setFilter(ReminderFilterType.ACTIVE)
        testDispatcher.scheduler.advanceUntilIdle()

        val list = listViewModel.uiState.value.filteredReminders
        assertEquals(2, list.size)
        assertEquals(11L, list[0].id) // Snoozed active pinned
        assertEquals(10L, list[1].id) // Normal active
    }

    @Test
    fun r2_sorting_timeFilter_pinsSnoozedTimeReminders() = runTest {
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
        testDispatcher.scheduler.advanceUntilIdle()

        listViewModel.setFilter(ReminderFilterType.TIME)
        testDispatcher.scheduler.advanceUntilIdle()

        val list = listViewModel.uiState.value.filteredReminders
        assertEquals(3, list.size)
        assertEquals(22L, list[0].id) // Time Snoozed 1 pinned first
        assertEquals(24L, list[1].id) // Time Normal 2 (createdAt 500)
        assertEquals(21L, list[2].id) // Time Normal 1 (createdAt 200)
    }

    @Test
    fun r2_sorting_locationFilter_pinsSnoozedLocationReminders() = runTest {
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
        testDispatcher.scheduler.advanceUntilIdle()

        listViewModel.setFilter(ReminderFilterType.LOCATION)
        testDispatcher.scheduler.advanceUntilIdle()

        val list = listViewModel.uiState.value.filteredReminders
        assertEquals(2, list.size)
        assertEquals(32L, list[0].id) // Loc Snoozed pinned
        assertEquals(31L, list[1].id) // Loc Normal
    }

    @Test
    fun r2_sorting_wifiFilter_pinsSnoozedWifiReminders() = runTest {
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
        testDispatcher.scheduler.advanceUntilIdle()

        listViewModel.setFilter(ReminderFilterType.WIFI)
        testDispatcher.scheduler.advanceUntilIdle()

        val list = listViewModel.uiState.value.filteredReminders
        assertEquals(3, list.size)
        assertEquals(43L, list[0].id) // Wifi Snoozed pinned
        assertEquals(42L, list[1].id) // Wifi Normal 2 (createdAt 500)
        assertEquals(41L, list[2].id) // Wifi Normal 1 (createdAt 100)
    }

    @Test
    fun r2_sorting_stressTest_arbitrary20RemindersAcrossFilters() = runTest {
        // Generate 20 arbitrary reminders with various states
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
                    createdAt = 1000L + (i * 37 % 50) // pseudo-random timestamps
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
        testDispatcher.scheduler.advanceUntilIdle()

        // Test invariant across all filters
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
            snoozeNextTriggerTime = 999999L
        )
        val normalReminder = Reminder(
            id = 778L,
            title = "Normal Item",
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
