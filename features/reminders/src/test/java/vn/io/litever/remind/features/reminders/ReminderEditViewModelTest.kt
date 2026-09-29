package vn.io.litever.remind.features.reminders

import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
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
import vn.io.litever.remind.core.common.util.PermissionChecker
import vn.io.litever.remind.core.common.util.ReminderPermissionType
import vn.io.litever.remind.core.datastore.AlarmPreferencesDataSource
import vn.io.litever.remind.core.domain.repository.ReminderRepository
import vn.io.litever.remind.core.domain.scheduler.ReminderScheduler
import vn.io.litever.remind.core.model.ChecklistItem
import java.time.DayOfWeek
import vn.io.litever.remind.core.model.GeofenceTransitionType
import vn.io.litever.remind.core.model.LocationTrigger
import vn.io.litever.remind.core.model.NotificationStyle
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.core.model.TimeTrigger
import vn.io.litever.remind.core.model.WifiTrigger
import vn.io.litever.remind.core.model.WifiTriggerType
import vn.io.litever.remind.features.reminders.viewmodel.ReminderEditViewModel
import vn.io.litever.remind.features.reminders.viewmodel.ReminderValidationErrorField
import java.time.LocalDate
import java.time.LocalTime
import vn.io.litever.remind.core.model.ReminderTimeWindow

@OptIn(ExperimentalCoroutinesApi::class)
class ReminderEditViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repository = mockk<ReminderRepository>(relaxed = true)
    private val scheduler = mockk<ReminderScheduler>(relaxed = true)
    private val reminderSyncManager = mockk<ReminderSyncManager>(relaxed = true)
    private val permissionChecker = mockk<PermissionChecker>(relaxed = true)
    private val preferencesDataSource = mockk<AlarmPreferencesDataSource>(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { preferencesDataSource.is24HourFormat } returns flowOf(true)
        every { permissionChecker.hasPermissionsForReminder(any()) } returns true
        every { permissionChecker.getMissingPermissions(any()) } returns emptyList()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_newDraft_setsDefaultValues() = runTest {
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

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(0L, state.id)
        assertEquals("", state.title)
        assertEquals(ReminderTriggerType.TIME, state.triggerType)
        assertTrue(state.isNewDraft)
    }

    @Test
    fun init_existingReminder_loadsStateFromRepository() = runTest {
        val existing = Reminder(
            id = 42L,
            title = "Dentist Appointment",
            notes = "Bring insurance card",
            triggerType = ReminderTriggerType.LOCATION,
            locationTrigger = LocationTrigger(
                latitude = 21.03,
                longitude = 105.85,
                radiusMeters = 200f,
                locationName = "Clinic",
                transitionType = GeofenceTransitionType.ENTER
            ),
            checklist = listOf(
                ChecklistItem(id = 1, reminderId = 42L, text = "Card", isCompleted = false)
            )
        )
        coEvery { repository.getReminderById(42L) } returns existing

        val savedStateHandle = SavedStateHandle(mapOf("reminderId" to 42L))
        val viewModel = ReminderEditViewModel(
            reminderRepository = repository,
            reminderScheduler = scheduler,
            reminderSyncManager = reminderSyncManager,
            permissionChecker = permissionChecker,
            preferencesDataSource = preferencesDataSource,
            savedStateHandle = savedStateHandle
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(42L, state.id)
        assertEquals("Dentist Appointment", state.title)
        assertEquals("Bring insurance card", state.notes)
        assertEquals(ReminderTriggerType.LOCATION, state.triggerType)
        assertEquals("Clinic", state.locationName)
        assertEquals(200f, state.radiusMeters)
        assertEquals(1, state.checklist.size)
        assertFalse(state.isNewDraft)
    }

    @Test
    fun validation_blankTitle_showsErrorAndBlocksSave() = runTest {
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

        var onSuccessCalled = false
        viewModel.setTitle("   ")
        viewModel.saveReminder(onSuccess = { onSuccessCalled = true })
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showTitleError)
        assertFalse(onSuccessCalled)
        coVerify(exactly = 0) { repository.insertReminder(any()) }
    }

    @Test
    fun checklistOperations_add_edit_toggle_delete_move() = runTest {
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

        // 1. Add
        viewModel.addChecklistItem("Milk")
        viewModel.addChecklistItem("Eggs")
        viewModel.addChecklistItem("Bread")
        assertEquals(3, viewModel.uiState.value.checklist.size)

        // 2. Edit
        viewModel.updateChecklistItemText(1, "Fresh Eggs")
        assertEquals("Fresh Eggs", viewModel.uiState.value.checklist[1].text)

        // 3. Toggle Initial
        viewModel.toggleChecklistItemInitial(0, true)
        assertTrue(viewModel.uiState.value.checklist[0].isCompleted)

        // 4. Move Bread (index 2) to index 0
        viewModel.moveChecklistItem(2, 0)
        assertEquals("Bread", viewModel.uiState.value.checklist[0].text)

        // 5. Delete item at index 1
        viewModel.deleteChecklistItem(1)
        assertEquals(2, viewModel.uiState.value.checklist.size)
    }

    @Test
    fun saveReminder_whenPermissionsGranted_insertsAndSchedules() = runTest {
        coEvery { repository.insertReminder(any()) } returns 100L
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

        viewModel.setTitle("Buy Coffee")
        viewModel.setTriggerType(ReminderTriggerType.TIME)
        viewModel.setTime(LocalTime.of(8, 30))

        var saved = false
        viewModel.saveReminder(onSuccess = { saved = true })
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(saved)
        coVerify { repository.insertReminder(match { it.title == "Buy Coffee" && it.isEnabled }) }
        coVerify { scheduler.schedule(match { it.id == 100L }) }
    }

    @Test
    fun saveReminder_whenPermissionsMissing_showsPermissionDialog() = runTest {
        every { permissionChecker.getMissingPermissions(any()) } returns listOf(ReminderPermissionType.LOCATION)
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

        viewModel.setTitle("Office Arrival")
        viewModel.setTriggerType(ReminderTriggerType.LOCATION)
        viewModel.setLocationCoordinates(37.7749, -122.4194)
        viewModel.setLocationName("Office")

        var saved = false
        viewModel.saveReminder(onSuccess = { saved = true })
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(saved)
        assertTrue(viewModel.uiState.value.showPermissionDialog)
        coVerify(exactly = 0) { repository.insertReminder(any()) }
    }

    @Test
    fun saveAnyway_savesReminderAsDisabled() = runTest {
        coEvery { repository.insertReminder(any()) } returns 200L
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

        viewModel.setTitle("Wi-Fi Sync")
        viewModel.setTriggerType(ReminderTriggerType.WIFI)

        var saved = false
        viewModel.saveAnyway(onSuccess = { saved = true })
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(saved)
        assertFalse(viewModel.uiState.value.showPermissionDialog)
        coVerify { repository.insertReminder(match { it.title == "Wi-Fi Sync" && !it.isEnabled }) }
        coVerify(exactly = 0) { scheduler.schedule(any()) }
    }

    @Test
    fun updateRingtone_and_updateSnoozeSettings_updatesUiState() = runTest {
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

        // 1. Ringtone
        viewModel.updateRingtone("content://media/internal/audio/media/42")
        assertEquals("content://media/internal/audio/media/42", viewModel.uiState.value.ringtoneUri)

        // 2. Snooze settings
        viewModel.updateSnoozeSettings(enabled = true, interval = 10, repeatCount = 5)
        assertTrue(viewModel.uiState.value.snoozeEnabled)
        assertEquals(10, viewModel.uiState.value.snoozeIntervalMinutes)
        assertEquals(5, viewModel.uiState.value.snoozeRepeatCount)
    }

    @Test
    fun setNotificationStyle_standard_resetsPersistentAlert() = runTest {
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

        viewModel.setNotificationStyle(NotificationStyle.FULL_SCREEN)
        viewModel.setPersistentAlert(true)
        assertTrue(viewModel.uiState.value.isPersistentAlert)

        viewModel.setNotificationStyle(NotificationStyle.STANDARD)
        assertFalse(viewModel.uiState.value.isPersistentAlert)

        val draft = viewModel.createDraftReminder()
        assertFalse(draft.isPersistentAlert)
    }

    @Test
    fun date_and_repeatDays_mutuallyClearEachOther() = runTest {
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

        // Setting repeat days clears specific date
        viewModel.setDate(LocalDate.of(2026, 9, 1))
        assertEquals(LocalDate.of(2026, 9, 1), viewModel.uiState.value.date)

        viewModel.setRepeatDays(listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY))
        assertNull(viewModel.uiState.value.date)
        assertEquals(2, viewModel.uiState.value.repeatDays.size)

        // Setting specific date clears repeat days
        viewModel.setDate(LocalDate.of(2026, 9, 5))
        assertEquals(LocalDate.of(2026, 9, 5), viewModel.uiState.value.date)
        assertTrue(viewModel.uiState.value.repeatDays.isEmpty())
    }

    @Test
    fun onBackClick_withAlertSettingsChanges_showsDiscardDialog() = runTest {
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

        // Change ringtone
        viewModel.updateRingtone("content://media/test")
        var backNavigated = false
        viewModel.onBackClick(onNavigateBack = { backNavigated = true })

        assertTrue(viewModel.uiState.value.showDiscardDialog)
        assertFalse(backNavigated)
    }

    @Test
    fun onBackClick_withTimeWindowChanges_showsDiscardDialog() = runTest {
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

        // Change Location Time Window
        val window = ReminderTimeWindow(startTime = LocalTime.of(9, 0), endTime = LocalTime.of(17, 0))
        viewModel.setLocationTimeWindow(window)
        var backNavigated = false
        viewModel.onBackClick(onNavigateBack = { backNavigated = true })

        assertTrue(viewModel.uiState.value.showDiscardDialog)
        assertFalse(backNavigated)
    }

    @Test
    fun moveChecklistItem_reordersItemsCorrectly() = runTest {
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

        viewModel.addChecklistItem("Task 1")
        viewModel.addChecklistItem("Task 2")
        viewModel.addChecklistItem("Task 3")

        assertEquals("Task 1", viewModel.uiState.value.checklist[0].text)
        assertEquals("Task 2", viewModel.uiState.value.checklist[1].text)
        assertEquals("Task 3", viewModel.uiState.value.checklist[2].text)

        // Move Task 1 (index 0) to index 2
        viewModel.moveChecklistItem(0, 2)

        val updated = viewModel.uiState.value.checklist
        assertEquals("Task 2", updated[0].text)
        assertEquals(0, updated[0].orderIndex)
        assertEquals("Task 3", updated[1].text)
        assertEquals(1, updated[1].orderIndex)
        assertEquals("Task 1", updated[2].text)
        assertEquals(2, updated[2].orderIndex)
    }

    @Test
    fun wifiConfigUpdates_createsExpectedDraft() = runTest {
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

        viewModel.setTriggerType(ReminderTriggerType.WIFI)
        viewModel.setWifiSsid("MyOffice_5G")
        viewModel.setWifiBssid("00:11:22:33:44:55")
        viewModel.setWifiTriggerType(WifiTriggerType.ON_DISCONNECT)

        val draft = viewModel.createDraftReminder()
        assertEquals(ReminderTriggerType.WIFI, draft.triggerType)
        assertEquals("MyOffice_5G", draft.wifiTrigger?.ssid)
        assertEquals("00:11:22:33:44:55", draft.wifiTrigger?.bssid)
        assertEquals(WifiTriggerType.ON_DISCONNECT, draft.wifiTrigger?.triggerType)
    }

    @Test
    fun repeatInterval_weekly_togglesDaysCorrectly() = runTest {
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

        viewModel.setRepeatInterval(vn.io.litever.remind.features.reminders.viewmodel.ReminderRepeatInterval.WEEKLY)
        assertTrue(viewModel.uiState.value.repeatDays.contains(DayOfWeek.MONDAY))

        viewModel.toggleRepeatDay(DayOfWeek.WEDNESDAY)
        assertTrue(viewModel.uiState.value.repeatDays.contains(DayOfWeek.MONDAY))
        assertTrue(viewModel.uiState.value.repeatDays.contains(DayOfWeek.WEDNESDAY))
    }

    @Test
    fun repeatInterval_monthly_togglesDays() = runTest {
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

        viewModel.setRepeatInterval(vn.io.litever.remind.features.reminders.viewmodel.ReminderRepeatInterval.MONTHLY)
        viewModel.toggleMonthlyDay(15)

        val state = viewModel.uiState.value
        assertEquals(vn.io.litever.remind.features.reminders.viewmodel.ReminderRepeatInterval.MONTHLY, state.repeatInterval)
        assertTrue(state.monthlyDays.contains(15))
        assertNull(state.date)
    }

    @Test
    fun repeatInterval_yearly_addsAndRemovesDates() = runTest {
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

        viewModel.setRepeatInterval(vn.io.litever.remind.features.reminders.viewmodel.ReminderRepeatInterval.YEARLY)
        viewModel.addYearlyDate(12, 25)

        val state = viewModel.uiState.value
        assertEquals(vn.io.litever.remind.features.reminders.viewmodel.ReminderRepeatInterval.YEARLY, state.repeatInterval)
        assertTrue(state.yearlyDates.any { it.month == 12 && it.day == 25 })
        assertNull(state.date)
    }

    @Test
    fun volume_and_vibration_updatesCorrectly() = runTest {
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

        viewModel.setVolume(50)
        viewModel.setVibrationEnabled(false)

        val state = viewModel.uiState.value
        assertEquals(50, state.volume)
        assertFalse(state.vibrationEnabled)

        val draft = viewModel.createDraftReminder()
        assertEquals(50, draft.volume)
        assertFalse(draft.vibrationEnabled)
    }

    @Test
    fun autoSilence_and_useAlarmStream_updatesCorrectly() = runTest {
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

        viewModel.setAutoSilenceMinutes(10)
        viewModel.setUseAlarmStream(false)

        val state = viewModel.uiState.value
        assertEquals(10, state.autoSilenceMinutes)
        assertFalse(state.useAlarmStream)

        val draft = viewModel.createDraftReminder()
        assertEquals(10, draft.autoSilenceMinutes)
        assertFalse(draft.useAlarmStream)
    }

    @Test
    fun multiTimeWindows_and_allDayPreset_updatesCorrectly() = runTest {
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

        viewModel.setTriggerType(ReminderTriggerType.LOCATION)
        viewModel.setAllDayPresetLocation()

        val state = viewModel.uiState.value
        assertEquals(1, state.locationTimeWindows.size)
        assertEquals(LocalTime.of(0, 0), state.locationTimeWindows[0].startTime)
        assertEquals(LocalTime.of(23, 59), state.locationTimeWindows[0].endTime)
    }

    @Test
    fun onBackClick_withRepeatIntervalChange_showsDiscardDialog() = runTest {
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

        viewModel.setRepeatInterval(vn.io.litever.remind.features.reminders.viewmodel.ReminderRepeatInterval.WEEKLY)
        viewModel.toggleRepeatDay(DayOfWeek.FRIDAY)
        var backNavigated = false
        viewModel.onBackClick(onNavigateBack = { backNavigated = true })

        assertTrue(viewModel.uiState.value.showDiscardDialog)
        assertFalse(backNavigated)
    }

    @Test
    fun onBackClick_withVolumeChange_showsDiscardDialog() = runTest {
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

        viewModel.setVolume(20)
        var backNavigated = false
        viewModel.onBackClick(onNavigateBack = { backNavigated = true })

        assertTrue(viewModel.uiState.value.showDiscardDialog)
        assertFalse(backNavigated)
    }

    @Test
    fun onBackClick_withVibrationChange_showsDiscardDialog() = runTest {
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

        viewModel.setVibrationEnabled(!viewModel.uiState.value.vibrationEnabled)
        var backNavigated = false
        viewModel.onBackClick(onNavigateBack = { backNavigated = true })

        assertTrue(viewModel.uiState.value.showDiscardDialog)
        assertFalse(backNavigated)
    }

    @Test
    fun onBackClick_withMonthlyDayChange_showsDiscardDialog() = runTest {
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

        viewModel.setRepeatInterval(vn.io.litever.remind.features.reminders.viewmodel.ReminderRepeatInterval.MONTHLY)
        viewModel.toggleMonthlyDay(28)
        var backNavigated = false
        viewModel.onBackClick(onNavigateBack = { backNavigated = true })

        assertTrue(viewModel.uiState.value.showDiscardDialog)
        assertFalse(backNavigated)
    }

    @Test
    fun onBackClick_withYearlyMonthChange_showsDiscardDialog() = runTest {
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

        viewModel.setRepeatInterval(vn.io.litever.remind.features.reminders.viewmodel.ReminderRepeatInterval.YEARLY)
        viewModel.addYearlyDate(11, 20)
        var backNavigated = false
        viewModel.onBackClick(onNavigateBack = { backNavigated = true })

        assertTrue(viewModel.uiState.value.showDiscardDialog)
        assertFalse(backNavigated)
    }

    @Test
    fun saveReminder_callsSyncOnSave() = runTest {
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

        viewModel.setTitle("New Reminder")
        var saved = false
        viewModel.saveReminder(onSuccess = { saved = true })
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(saved)
        io.mockk.coVerify(exactly = 1) { reminderSyncManager.sync() }
    }

    @Test
    fun saveReminder_locationTrigger_emptyLocation_showsLocationError() = runTest {
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

        viewModel.setTitle("Location Reminder")
        viewModel.setTriggerType(ReminderTriggerType.LOCATION)
        var saved = false
        viewModel.saveReminder(onSuccess = { saved = true })
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(saved)
        assertTrue(viewModel.uiState.value.showLocationError)
        assertEquals(ReminderValidationErrorField.LOCATION, viewModel.uiState.value.validationErrorField)
        assertTrue(viewModel.uiState.value.validationErrorTrigger > 0L)
    }

    @Test
    fun saveReminder_wifiTrigger_emptySsid_showsWifiError() = runTest {
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

        viewModel.setTitle("Wi-Fi Reminder")
        viewModel.setTriggerType(ReminderTriggerType.WIFI)
        var saved = false
        viewModel.saveReminder(onSuccess = { saved = true })
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(saved)
        assertTrue(viewModel.uiState.value.showWifiError)
        assertEquals(ReminderValidationErrorField.WIFI_SSID, viewModel.uiState.value.validationErrorField)
        assertTrue(viewModel.uiState.value.validationErrorTrigger > 0L)
    }

    @Test
    fun setLocationName_clearsLocationError() = runTest {
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

        viewModel.setTitle("Location Reminder")
        viewModel.setTriggerType(ReminderTriggerType.LOCATION)
        viewModel.saveReminder(onSuccess = {})
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.showLocationError)

        viewModel.setLocationName("Supermarket")
        assertFalse(viewModel.uiState.value.showLocationError)
    }

    @Test
    fun setWifiSsid_clearsWifiError() = runTest {
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

        viewModel.setTitle("Wi-Fi Reminder")
        viewModel.setTriggerType(ReminderTriggerType.WIFI)
        viewModel.saveReminder(onSuccess = {})
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.showWifiError)

        viewModel.setWifiSsid("Home_Network")
        assertFalse(viewModel.uiState.value.showWifiError)
    }

    @Test
    fun timeWindow_toggle_nullRepresents24SevenMode() = runTest {
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

        // By default, Location and Wi-Fi time window is null (24/7)
        viewModel.setTriggerType(ReminderTriggerType.LOCATION)
        assertNull(viewModel.uiState.value.locationTimeWindow)
        val draft247 = viewModel.createDraftReminder()
        assertNull(draft247.locationTrigger?.timeWindow)

        // Toggle Time Window ON
        val window = ReminderTimeWindow(startTime = LocalTime.of(8, 0), endTime = LocalTime.of(18, 0))
        viewModel.setLocationTimeWindow(window)
        assertNotNull(viewModel.uiState.value.locationTimeWindow)
        val draftWithWindow = viewModel.createDraftReminder()
        assertNotNull(draftWithWindow.locationTrigger?.timeWindow)
        assertEquals(LocalTime.of(8, 0), draftWithWindow.locationTrigger?.timeWindow?.startTime)

        // Toggle Time Window OFF (back to 24/7)
        viewModel.setLocationTimeWindow(null)
        assertNull(viewModel.uiState.value.locationTimeWindow)
        val draftReset247 = viewModel.createDraftReminder()
        assertNull(draftReset247.locationTrigger?.timeWindow)
    }

    @Test
    fun standardAlertMode_hidesAndResetsPersistentAlert() = runTest {
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

        // Set to Full-Screen Alarm with persistent alert ON
        viewModel.setNotificationStyle(NotificationStyle.FULL_SCREEN)
        viewModel.setPersistentAlert(true)
        assertTrue(viewModel.uiState.value.isPersistentAlert)

        // Switch to Standard Notification mode
        viewModel.setNotificationStyle(NotificationStyle.STANDARD)
        assertEquals(NotificationStyle.STANDARD, viewModel.uiState.value.notificationStyle)
        assertFalse(viewModel.uiState.value.isPersistentAlert)

        val draft = viewModel.createDraftReminder()
        assertEquals(NotificationStyle.STANDARD, draft.notificationStyle)
        assertFalse(draft.isPersistentAlert)
    }
}

