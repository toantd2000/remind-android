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
import kotlinx.coroutines.flow.toList
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
import vn.io.litever.remind.core.model.ChecklistItem
import vn.io.litever.remind.core.model.GeofenceTransitionType
import vn.io.litever.remind.core.model.LocationTrigger
import vn.io.litever.remind.core.model.NotificationStyle
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.core.model.TimeTrigger
import vn.io.litever.remind.core.model.WifiTrigger
import vn.io.litever.remind.core.model.WifiTriggerType
import vn.io.litever.remind.features.reminders.viewmodel.ReminderEditViewModel
import vn.io.litever.remind.features.reminders.viewmodel.ReminderListViewModel
import vn.io.litever.remind.features.reminders.viewmodel.ReminderRingingViewModel
import java.time.LocalTime

/**
 * Empirical Challenger Test Suite for Milestone 4:
 * 1. Permission UX Edge Cases (Save Anyway, List Toggle Gating, Permission Refresh/Restoration)
 * 2. Checklist State Preservation Across Snooze & Ringing Events
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ReminderPermissionAndChecklistChallengerTest {

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
            ReminderPermissionType.LOCATION,
            ReminderPermissionType.BACKGROUND_LOCATION
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

        viewModel.setTitle("Office Geofence Alert")
        viewModel.setTriggerType(ReminderTriggerType.LOCATION)
        viewModel.setLatitude(21.0285)
        viewModel.setLongitude(105.8542)

        var saveAnywaySuccess = false
        viewModel.saveAnyway(onSuccess = { saveAnywaySuccess = true })
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue("Save callback must succeed on Save Anyway", saveAnywaySuccess)
        assertFalse("Permission dialog must be dismissed", viewModel.uiState.value.showPermissionDialog)

        // Strict assertion: isEnabled must be FALSE and scheduler must NEVER be invoked
        coVerify(exactly = 1) { repository.insertReminder(match { !it.isEnabled && it.title == "Office Geofence Alert" }) }
        coVerify(exactly = 0) { scheduler.schedule(any()) }

        // Second scenario: attempting standard saveReminder when permissions are missing
        val draft = Reminder(
            id = 0L,
            title = "Supermarket",
            triggerType = ReminderTriggerType.LOCATION,
            locationTrigger = LocationTrigger(latitude = 37.7749, longitude = -122.4194, locationName = "Supermarket")
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
        viewModel2.setLocationCoordinates(37.7749, -122.4194)
        viewModel2.setLocationName("Supermarket")

        var saveReminderSuccess = false
        viewModel2.saveReminder(onSuccess = { saveReminderSuccess = true })
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(saveReminderSuccess)
        assertTrue(viewModel2.uiState.value.showPermissionDialog)
        assertEquals(2, viewModel2.uiState.value.missingPermissions.size)
        assertTrue(viewModel2.uiState.value.missingPermissions.contains(ReminderPermissionType.LOCATION))
        assertTrue(viewModel2.uiState.value.missingPermissions.contains(ReminderPermissionType.BACKGROUND_LOCATION))

        coVerify(exactly = 0) { repository.insertReminder(match { it.title == "Supermarket" }) }
    }

    @Test
    fun challenge2_wifiReminder_permissionGranted_disablingCancelsScheduler() = runTest(testDispatcher) {
        val existingReminder = Reminder(
            id = 702L,
            title = "Office Wi-Fi Connection",
            triggerType = ReminderTriggerType.WIFI,
            wifiTrigger = WifiTrigger(ssid = "Lab_Net_Secure", triggerType = WifiTriggerType.WHILE_CONNECTED),
            isEnabled = true,
            isCompleted = false
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
            title = "Client Office Geofence",
            triggerType = ReminderTriggerType.LOCATION,
            isEnabled = false
        )
        val allFlow = MutableStateFlow(listOf(disabledReminder))
        every { repository.getAllReminders() } returns allFlow
        every { permissionChecker.hasPermissionsForReminder(disabledReminder) } returns false
        every { permissionChecker.getMissingPermissions(disabledReminder) } returns listOf(ReminderPermissionType.LOCATION)

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
            triggerType = ReminderTriggerType.TIME,
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
            title = "Home Wi-Fi Sync",
            triggerType = ReminderTriggerType.WIFI,
            wifiTrigger = WifiTrigger(ssid = "HomeNet")
        )
        every { permissionChecker.getMissingPermissions(any()) } returns listOf(ReminderPermissionType.WIFI)
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
        viewModel.setWifiSsid("HomeNet")

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

    @Test
    fun challenge6_checklistLiveToggleInNotification_persistsDirectlyWithoutTriggeringReschedule() = runTest(testDispatcher) {
        val reminder = Reminder(
            id = 801L,
            title = "Airport Departure Checklist",
            triggerType = ReminderTriggerType.LOCATION,
            checklist = listOf(
                ChecklistItem(id = 11L, reminderId = 801L, text = "Pack passport", isCompleted = false, orderIndex = 0)
            ),
            isEnabled = true
        )
        val reminderFlow = MutableStateFlow<Reminder?>(reminder)
        every { repository.getReminderFlow(801L) } returns reminderFlow
        coEvery { repository.getReminderById(801L) } returns reminder.copy(checklist = listOf(ChecklistItem(id = 11L, reminderId = 801L, text = "Pack passport", isCompleted = true, orderIndex = 0)))

        val savedStateHandle = SavedStateHandle(mapOf("reminderId" to 801L))
        val ringingViewModel = ReminderRingingViewModel(
            reminderRepository = repository,
            reminderScheduler = scheduler,
            reminderAlertManager = alertManager,
            reminderNotificationManager = notificationManager,
            preferencesDataSource = preferencesDataSource,
            savedStateHandle = savedStateHandle
        )
        testDispatcher.scheduler.advanceUntilIdle()

        ringingViewModel.toggleChecklistItem(11L, true)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { repository.updateChecklistItemCompletion(11L, true) }
    }

    @Test
    fun challenge7_checklistPreservationAcrossMultiSnoozeCycles() = runTest(testDispatcher) {
        val item1 = ChecklistItem(id = 101L, reminderId = 901L, text = "Drink water", isCompleted = false, orderIndex = 0)
        val reminderV1 = Reminder(
            id = 901L,
            title = "Medication",
            triggerType = ReminderTriggerType.TIME,
            checklist = listOf(item1),
            snoozeEnabled = true
        )

        val reminderFlow = MutableStateFlow<Reminder?>(reminderV1)
        every { repository.getReminderFlow(901L) } returns reminderFlow
        coEvery { repository.getReminderById(901L) } returns reminderV1

        val vmCycle1 = ReminderRingingViewModel(
            reminderRepository = repository,
            reminderScheduler = scheduler,
            reminderAlertManager = alertManager,
            reminderNotificationManager = notificationManager,
            preferencesDataSource = preferencesDataSource,
            savedStateHandle = SavedStateHandle(mapOf("reminderId" to 901L))
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vmCycle1.uiState.collect() }
        testDispatcher.scheduler.advanceUntilIdle()

        vmCycle1.toggleChecklistItem(101L, true)
        testDispatcher.scheduler.advanceUntilIdle()

        reminderFlow.value = reminderV1.copy(checklist = listOf(item1.copy(isCompleted = true)))
        vmCycle1.snooze(onFinished = {})
        testDispatcher.scheduler.advanceUntilIdle()

        val vmCycle2 = ReminderRingingViewModel(
            reminderRepository = repository,
            reminderScheduler = scheduler,
            reminderAlertManager = alertManager,
            reminderNotificationManager = notificationManager,
            preferencesDataSource = preferencesDataSource,
            savedStateHandle = SavedStateHandle(mapOf("reminderId" to 901L))
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vmCycle2.uiState.collect() }
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(vmCycle2.uiState.value.reminder!!.checklist[0].isCompleted)

        val vmCycle3 = ReminderRingingViewModel(
            reminderRepository = repository,
            reminderScheduler = scheduler,
            reminderAlertManager = alertManager,
            reminderNotificationManager = notificationManager,
            preferencesDataSource = preferencesDataSource,
            savedStateHandle = SavedStateHandle(mapOf("reminderId" to 901L))
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vmCycle3.uiState.collect() }
        testDispatcher.scheduler.advanceUntilIdle()

        vmCycle3.dismiss(onFinished = {})
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(atLeast = 1) { repository.updateReminder(any()) }
    }
}
