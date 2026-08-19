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
import vn.io.litever.remind.core.common.util.PermissionChecker
import vn.io.litever.remind.core.common.util.ReminderPermissionType
import vn.io.litever.remind.core.datastore.AlarmPreferencesDataSource
import vn.io.litever.remind.core.domain.repository.ReminderRepository
import vn.io.litever.remind.core.domain.scheduler.ReminderScheduler
import vn.io.litever.remind.core.model.ChecklistItem
import vn.io.litever.remind.core.model.DayOfWeek
import vn.io.litever.remind.core.model.GeofenceTransitionType
import vn.io.litever.remind.core.model.LocationTrigger
import vn.io.litever.remind.core.model.NotificationStyle
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.core.model.TimeTrigger
import vn.io.litever.remind.core.model.WifiTrigger
import vn.io.litever.remind.core.model.WifiTriggerType
import vn.io.litever.remind.features.reminders.viewmodel.ReminderEditViewModel
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class ReminderEditViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repository = mockk<ReminderRepository>(relaxed = true)
    private val scheduler = mockk<ReminderScheduler>(relaxed = true)
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
            permissionChecker = permissionChecker,
            preferencesDataSource = preferencesDataSource,
            savedStateHandle = savedStateHandle
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setTitle("Office Arrival")
        viewModel.setTriggerType(ReminderTriggerType.LOCATION)

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
}
