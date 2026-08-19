package vn.io.litever.remind.features.reminders

import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
    private val permissionChecker = mockk<PermissionChecker>(relaxed = true)
    private val alertManager = mockk<ReminderAlertManager>(relaxed = true)
    private val notificationManager = mockk<ReminderNotificationManager>(relaxed = true)
    private val preferencesDataSource = mockk<AlarmPreferencesDataSource>(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { preferencesDataSource.is24HourFormat } returns flowOf(true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // =========================================================================
    // SECTION 1: PERMISSION UX EDGE CASES
    // =========================================================================

    @Test
    fun challenge1_saveAnyway_newReminder_setsDisabled_andNeverCallsSchedule() = runTest {
        every { permissionChecker.getMissingPermissions(any()) } returns listOf(
            ReminderPermissionType.LOCATION,
            ReminderPermissionType.BACKGROUND_LOCATION
        )
        coEvery { repository.insertReminder(any()) } returns 701L

        val savedStateHandle = SavedStateHandle(mapOf("reminderId" to 0L))
        val viewModel = ReminderEditViewModel(
            reminderRepository = repository,
            reminderScheduler = scheduler,
            permissionChecker = permissionChecker,
            preferencesDataSource = preferencesDataSource,
            savedStateHandle = savedStateHandle
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setTitle("Office Geofence Alert")
        viewModel.setTriggerType(ReminderTriggerType.LOCATION)
        viewModel.setLatitude(21.0285)
        viewModel.setLongitude(105.8542)

        var saveSuccess = false
        viewModel.saveAnyway(onSuccess = { saveSuccess = true })
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue("Save callback must succeed on Save Anyway", saveSuccess)
        assertFalse("Permission dialog must be dismissed", viewModel.uiState.value.showPermissionDialog)

        // Strict assertion: isEnabled must be FALSE and scheduler must NEVER be invoked
        coVerify(exactly = 1) {
            repository.insertReminder(match { reminder ->
                reminder.title == "Office Geofence Alert" &&
                        !reminder.isEnabled &&
                        reminder.triggerType == ReminderTriggerType.LOCATION
            })
        }
        coVerify(exactly = 0) { scheduler.schedule(any()) }
    }

    @Test
    fun challenge2_saveAnyway_existingReminder_setsDisabled_cancelsExistingSchedule_andNeverSchedules() = runTest {
        val existing = Reminder(
            id = 702L,
            title = "Existing Lab Wi-Fi",
            triggerType = ReminderTriggerType.WIFI,
            wifiTrigger = WifiTrigger(ssid = "Lab_Net", triggerType = WifiTriggerType.ON_CONNECT),
            isEnabled = true
        )
        coEvery { repository.getReminderById(702L) } returns existing
        every { permissionChecker.getMissingPermissions(any()) } returns listOf(ReminderPermissionType.NEARBY_WIFI_DEVICES)

        val savedStateHandle = SavedStateHandle(mapOf("reminderId" to 702L))
        val viewModel = ReminderEditViewModel(
            reminderRepository = repository,
            reminderScheduler = scheduler,
            permissionChecker = permissionChecker,
            preferencesDataSource = preferencesDataSource,
            savedStateHandle = savedStateHandle
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setWifiSsid("Lab_Net_Secure")

        var saveSuccess = false
        viewModel.saveAnyway(onSuccess = { saveSuccess = true })
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(saveSuccess)
        coVerify(exactly = 1) {
            repository.updateReminder(match { reminder ->
                reminder.id == 702L &&
                        reminder.wifiTrigger?.ssid == "Lab_Net_Secure" &&
                        !reminder.isEnabled
            })
        }
        // When disabling an existing reminder, scheduler.cancel must be called and schedule must NOT be called
        coVerify(exactly = 1) { scheduler.cancel(match { it.id == 702L && !it.isEnabled }) }
        coVerify(exactly = 0) { scheduler.schedule(any()) }
    }

    @Test
    fun challenge3_listToggle_missingPermissions_strictlyBlocked_noRepositoryUpdate_andNoSchedule() = runTest {
        val disabledReminder = Reminder(
            id = 703L,
            title = "Client Office Geofence",
            triggerType = ReminderTriggerType.LOCATION,
            locationTrigger = LocationTrigger(latitude = 10.7769, longitude = 106.7009, radiusMeters = 150f),
            isEnabled = false,
            isCompleted = false
        )
        val allFlow = MutableStateFlow(listOf(disabledReminder))
        every { repository.getAllReminders() } returns allFlow
        every { permissionChecker.hasNotificationPermission() } returns true
        every { permissionChecker.hasPermissionsForReminder(disabledReminder) } returns false
        every { permissionChecker.getMissingPermissions(disabledReminder) } returns listOf(
            ReminderPermissionType.LOCATION,
            ReminderPermissionType.BACKGROUND_LOCATION
        )

        val listViewModel = ReminderListViewModel(
            reminderRepository = repository,
            reminderScheduler = scheduler,
            permissionChecker = permissionChecker,
            preferencesDataSource = preferencesDataSource
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // User attempts to toggle switch ON
        listViewModel.toggleReminder(disabledReminder)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify strictly blocked
        coVerify(exactly = 0) { repository.updateReminder(any()) }
        coVerify(exactly = 0) { scheduler.schedule(any()) }
        assertTrue("Permission warning must be shown", listViewModel.uiState.value.hasPermissionWarning)
    }

    @Test
    fun challenge4_permissionRestoration_refreshPermissions_clearsBannerWhenGranted() = runTest {
        val activeReminder = Reminder(
            id = 704L,
            title = "Morning Coffee",
            triggerType = ReminderTriggerType.LOCATION,
            locationTrigger = LocationTrigger(latitude = 21.0, longitude = 105.0, radiusMeters = 100f),
            isEnabled = true,
            isCompleted = false
        )
        val allFlow = MutableStateFlow(listOf(activeReminder))
        every { repository.getAllReminders() } returns allFlow
        every { permissionChecker.hasNotificationPermission() } returns true
        // Initially missing
        every { permissionChecker.hasPermissionsForReminder(activeReminder) } returns false

        val listViewModel = ReminderListViewModel(
            reminderRepository = repository,
            reminderScheduler = scheduler,
            permissionChecker = permissionChecker,
            preferencesDataSource = preferencesDataSource
        )
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(listViewModel.uiState.value.hasPermissionWarning)

        // User grants permission in system settings and returns to app
        every { permissionChecker.hasPermissionsForReminder(activeReminder) } returns true
        listViewModel.refreshPermissions()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse("Permission warning banner must disappear once permissions granted", listViewModel.uiState.value.hasPermissionWarning)
    }

    @Test
    fun challenge5_editScreen_refreshPermissions_dismissesPermissionDialogWhenGranted() = runTest {
        val draft = Reminder(
            id = 0L,
            title = "Home Wi-Fi Sync",
            triggerType = ReminderTriggerType.WIFI,
            wifiTrigger = WifiTrigger(ssid = "HomeNet")
        )
        // Initially missing
        every { permissionChecker.getMissingPermissions(any()) } returns listOf(ReminderPermissionType.NEARBY_WIFI_DEVICES)
        every { permissionChecker.hasPermissionsForReminder(any()) } returns false

        val savedStateHandle = SavedStateHandle(mapOf("reminderId" to 0L))
        val viewModel = ReminderEditViewModel(
            reminderRepository = repository,
            reminderScheduler = scheduler,
            permissionChecker = permissionChecker,
            preferencesDataSource = preferencesDataSource,
            savedStateHandle = savedStateHandle
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setTitle("Home Wi-Fi Sync")
        viewModel.setTriggerType(ReminderTriggerType.WIFI)
        viewModel.setWifiSsid("HomeNet")

        viewModel.saveReminder(onSuccess = {})
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showPermissionDialog)

        // User grants permission and onResume triggers refreshPermissions
        every { permissionChecker.hasPermissionsForReminder(any()) } returns true
        viewModel.refreshPermissions()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse("Permission dialog should auto-dismiss on permission restoration", viewModel.uiState.value.showPermissionDialog)
    }

    // =========================================================================
    // SECTION 2: CHECKLIST STATE PRESERVATION ACROSS SNOOZE & RINGING
    // =========================================================================

    @Test
    fun challenge6_checklistLiveTogglingDuringRinging_persistsImmediatelyToRepo() = runTest {
        val initialItems = listOf(
            ChecklistItem(id = 11L, reminderId = 801L, text = "Pack passport", isCompleted = false, orderIndex = 0),
            ChecklistItem(id = 12L, reminderId = 801L, text = "Pack charger", isCompleted = false, orderIndex = 1),
            ChecklistItem(id = 13L, reminderId = 801L, text = "Lock windows", isCompleted = false, orderIndex = 2)
        )
        val initialReminder = Reminder(
            id = 801L,
            title = "Flight Preparation",
            triggerType = ReminderTriggerType.TIME,
            checklist = initialItems
        )
        val reminderFlow = MutableStateFlow<Reminder?>(initialReminder)
        every { repository.getReminderFlow(801L) } returns reminderFlow
        coEvery { repository.getReminderById(801L) } returns initialReminder

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

        // User checks item 11 ("Pack passport") on ringing screen
        ringingViewModel.toggleChecklistItem(11L, true)
        testDispatcher.scheduler.advanceUntilIdle()

        // Must immediately persist to DB via updateChecklistItemCompletion
        coVerify(exactly = 1) { repository.updateChecklistItemCompletion(11L, true) }
        coVerify(exactly = 1) { notificationManager.updateNotification(any()) }
    }

    @Test
    fun challenge7_checklistPreservationAcrossMultiSnoozeCycles_andFinalDismissal() = runTest {
        // Step 1: Initial reminder with 3 checklist items
        val item1 = ChecklistItem(id = 101L, reminderId = 901L, text = "Drink water", isCompleted = false, orderIndex = 0)
        val item2 = ChecklistItem(id = 102L, reminderId = 901L, text = "Take pill A", isCompleted = false, orderIndex = 1)
        val item3 = ChecklistItem(id = 103L, reminderId = 901L, text = "Log symptoms", isCompleted = false, orderIndex = 2)

        val reminderV1 = Reminder(
            id = 901L,
            title = "Medication Checklist",
            triggerType = ReminderTriggerType.TIME,
            timeTrigger = TimeTrigger(time = LocalTime.of(8, 0)),
            checklist = listOf(item1, item2, item3),
            snoozeEnabled = true,
            snoozeIntervalMinutes = 10,
            snoozeRepeatCount = 3,
            currentSnoozeCount = 0
        )

        val reminderFlow = MutableStateFlow<Reminder?>(reminderV1)
        every { repository.getReminderFlow(901L) } returns reminderFlow
        coEvery { repository.getReminderById(901L) } returns reminderV1

        // =====================================================================
        // Ringing Cycle 1: First Ring
        // =====================================================================
        val vmCycle1 = ReminderRingingViewModel(
            reminderRepository = repository,
            reminderScheduler = scheduler,
            reminderAlertManager = alertManager,
            reminderNotificationManager = notificationManager,
            preferencesDataSource = preferencesDataSource,
            savedStateHandle = SavedStateHandle(mapOf("reminderId" to 901L))
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // User checks Item 1
        vmCycle1.toggleChecklistItem(101L, true)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify(exactly = 1) { repository.updateChecklistItemCompletion(101L, true) }

        // Simulate DB emission after item 1 updated
        val reminderV2 = reminderV1.copy(
            checklist = listOf(item1.copy(isCompleted = true), item2, item3)
        )
        reminderFlow.value = reminderV2
        coEvery { repository.getReminderById(901L) } returns reminderV2
        testDispatcher.scheduler.advanceUntilIdle()

        // User Snoozes Cycle 1
        var cycle1Finished = false
        vmCycle1.snooze(onFinished = { cycle1Finished = true })
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(cycle1Finished)
        coVerify(exactly = 1) { alertManager.stopAlert(901L) }
        coVerify(exactly = 1) { notificationManager.cancelNotification(901L) }
        coVerify(exactly = 1) {
            repository.updateReminder(match { updated ->
                updated.id == 901L &&
                        updated.currentSnoozeCount == 1 &&
                        updated.snoozeNextTriggerTime != null &&
                        updated.checklist[0].isCompleted &&
                        !updated.checklist[1].isCompleted &&
                        !updated.checklist[2].isCompleted
            })
        }
        coVerify(exactly = 1) { scheduler.scheduleSnooze(match { it.id == 901L }, any()) }

        // =====================================================================
        // Ringing Cycle 2: Snooze Fires 10 minutes later (New Ringing Screen)
        // =====================================================================
        val reminderV3 = reminderV2.copy(
            currentSnoozeCount = 1,
            snoozeNextTriggerTime = System.currentTimeMillis() + 600000L
        )
        reminderFlow.value = reminderV3
        coEvery { repository.getReminderById(901L) } returns reminderV3

        val vmCycle2 = ReminderRingingViewModel(
            reminderRepository = repository,
            reminderScheduler = scheduler,
            reminderAlertManager = alertManager,
            reminderNotificationManager = notificationManager,
            preferencesDataSource = preferencesDataSource,
            savedStateHandle = SavedStateHandle(mapOf("reminderId" to 901L))
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val cycle2State = vmCycle2.uiState.value
        assertNotNull(cycle2State.reminder)
        assertEquals(1, cycle2State.reminder!!.currentSnoozeCount)
        assertTrue("Item 1 must STILL be checked on 2nd ring", cycle2State.reminder!!.checklist[0].isCompleted)
        assertFalse("Item 2 must still be unchecked", cycle2State.reminder!!.checklist[1].isCompleted)
        assertFalse("Item 3 must still be unchecked", cycle2State.reminder!!.checklist[2].isCompleted)
        assertEquals(1, cycle2State.reminder!!.completedChecklistCount)

        // User checks Item 2
        vmCycle2.toggleChecklistItem(102L, true)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify(exactly = 1) { repository.updateChecklistItemCompletion(102L, true) }

        // Simulate DB update for item 2
        val reminderV4 = reminderV3.copy(
            checklist = listOf(item1.copy(isCompleted = true), item2.copy(isCompleted = true), item3)
        )
        reminderFlow.value = reminderV4
        coEvery { repository.getReminderById(901L) } returns reminderV4
        testDispatcher.scheduler.advanceUntilIdle()

        // User Snoozes Cycle 2
        var cycle2Finished = false
        vmCycle2.snooze(onFinished = { cycle2Finished = true })
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(cycle2Finished)
        coVerify(exactly = 1) {
            repository.updateReminder(match { updated ->
                updated.id == 901L &&
                        updated.currentSnoozeCount == 2 &&
                        updated.checklist[0].isCompleted &&
                        updated.checklist[1].isCompleted &&
                        !updated.checklist[2].isCompleted
            })
        }

        // =====================================================================
        // Ringing Cycle 3: Second Snooze Fires (Final Ring & Dismiss)
        // =====================================================================
        val reminderV5 = reminderV4.copy(
            currentSnoozeCount = 2,
            snoozeNextTriggerTime = System.currentTimeMillis() + 600000L
        )
        reminderFlow.value = reminderV5
        coEvery { repository.getReminderById(901L) } returns reminderV5

        val vmCycle3 = ReminderRingingViewModel(
            reminderRepository = repository,
            reminderScheduler = scheduler,
            reminderAlertManager = alertManager,
            reminderNotificationManager = notificationManager,
            preferencesDataSource = preferencesDataSource,
            savedStateHandle = SavedStateHandle(mapOf("reminderId" to 901L))
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val cycle3State = vmCycle3.uiState.value
        assertTrue(cycle3State.reminder!!.checklist[0].isCompleted)
        assertTrue(cycle3State.reminder!!.checklist[1].isCompleted)
        assertFalse(cycle3State.reminder!!.checklist[2].isCompleted)
        assertEquals(2, cycle3State.reminder!!.completedChecklistCount)

        // User checks item 3
        vmCycle3.toggleChecklistItem(103L, true)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify(exactly = 1) { repository.updateChecklistItemCompletion(103L, true) }

        val reminderV6 = reminderV5.copy(
            checklist = listOf(item1.copy(isCompleted = true), item2.copy(isCompleted = true), item3.copy(isCompleted = true))
        )
        reminderFlow.value = reminderV6
        coEvery { repository.getReminderById(901L) } returns reminderV6
        testDispatcher.scheduler.advanceUntilIdle()

        // User taps Dismiss
        var cycle3Dismissed = false
        vmCycle3.dismiss(onFinished = { cycle3Dismissed = true })
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(cycle3Dismissed)
        coVerify(exactly = 3) { alertManager.stopAlert(901L) }
        coVerify(exactly = 3) { notificationManager.cancelNotification(901L) }
        coVerify(exactly = 1) {
            repository.updateReminder(match { updated ->
                updated.id == 901L &&
                        updated.isCompleted &&
                        updated.checklist.all { it.isCompleted } &&
                        updated.snoozeNextTriggerTime == null
            })
        }
        coVerify(exactly = 1) { scheduler.cancelSnooze(match { it.id == 901L }) }
    }
}
