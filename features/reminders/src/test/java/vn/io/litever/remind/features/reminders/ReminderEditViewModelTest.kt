package vn.io.litever.remind.features.reminders

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import vn.io.litever.remind.core.common.audio.AudioPlayer
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
import java.time.DayOfWeek
import vn.io.litever.remind.core.model.NotificationStyle
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.core.model.TimeTrigger
import vn.io.litever.remind.features.reminders.viewmodel.ReminderEditViewModel
import java.time.LocalDate
import java.time.LocalTime
import vn.io.litever.remind.core.model.ReminderTimeWindow
import vn.io.litever.remind.core.model.UrgencyLevel

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
        assertEquals(ReminderTriggerType.FIXED_TIME, state.triggerType)
    }

    @Test
    fun init_existingReminder_loadsStateFromRepository() = runTest {
        val existing = Reminder(
            id = 42L,
            title = "Dentist Appointment",
            notes = "Bring insurance card",
            triggerType = ReminderTriggerType.FIXED_TIME,
            timeTrigger = TimeTrigger(
                time = listOf(LocalTime.of(9, 30)),
                exactDate = LocalDate.of(2026, 10, 15)
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
        assertEquals(ReminderTriggerType.FIXED_TIME, state.triggerType)
        assertEquals(LocalTime.of(9, 30), state.time)
        assertEquals(LocalDate.of(2026, 10, 15), state.date)
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
        viewModel.setTriggerType(ReminderTriggerType.FIXED_TIME)
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
        every { permissionChecker.getMissingPermissions(any()) } returns listOf(ReminderPermissionType.EXACT_ALARM)
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

        viewModel.setTitle("Scheduled Alarm")
        viewModel.setTriggerType(ReminderTriggerType.FIXED_TIME)
        viewModel.setTime(LocalTime.of(8, 30))

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

        viewModel.setTitle("Important Task")
        viewModel.setTriggerType(ReminderTriggerType.FIXED_TIME)
        viewModel.setTime(LocalTime.of(12, 0))

        var saved = false
        viewModel.saveAnyway(onSuccess = { saved = true })
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(saved)
        assertFalse(viewModel.uiState.value.showPermissionDialog)
        coVerify { repository.insertReminder(match { it.title == "Important Task" && !it.isEnabled }) }
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
        viewModel.updateSnoozeSettings(enabled = true, interval = 10)
        assertTrue(viewModel.uiState.value.snoozeEnabled)
        assertEquals(10, viewModel.uiState.value.snoozeIntervalMinutes)
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
        assertFalse(draft.action.isPersistentAlert)
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

        // Change Time Windows
        val window = ReminderTimeWindow(startTime = LocalTime.of(9, 0), endTime = LocalTime.of(17, 0))
        viewModel.setTimeWindows(listOf(window))
        var backNavigated = false
        viewModel.onBackClick(onNavigateBack = { backNavigated = true })

        assertTrue(viewModel.uiState.value.showDiscardDialog)
        assertFalse(backNavigated)
    }

    @Test
    fun intervalConfigUpdates_createsExpectedDraft() = runTest {
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

        viewModel.setTriggerType(ReminderTriggerType.INTERVAL)
        viewModel.setIntervalMinutes(90)
        viewModel.setTimeWindows(listOf(ReminderTimeWindow(startTime = LocalTime.of(8, 0), endTime = LocalTime.of(17, 0))))

        viewModel.setTitle("Drink Water")
        var saved = false
        viewModel.saveReminder(onSuccess = { saved = true })
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(saved)
        coVerify {
            repository.insertReminder(match {
                it.triggerType == ReminderTriggerType.INTERVAL &&
                        it.timeTrigger?.intervalMinutes == 90 &&
                        it.timeTrigger?.timeWindows?.size == 1
            })
        }
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
        assertEquals(50, draft.action.volume)
        assertFalse(draft.action.vibrationEnabled)
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
        assertEquals(10, draft.action.autoSilenceMinutes)
        assertFalse(draft.action.useAlarmStream)
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

        viewModel.setTriggerType(ReminderTriggerType.INTERVAL)
        viewModel.setTimeWindows(listOf(ReminderTimeWindow.ALL_DAY))

        val state = viewModel.uiState.value
        assertEquals(1, state.timeWindows.size)
        assertEquals(LocalTime.of(0, 0), state.timeWindows[0].startTime)
        assertEquals(LocalTime.of(23, 59), state.timeWindows[0].endTime)
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
    fun urgencyLevel_updatesUiStateAndDraft() = runTest {
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

        viewModel.setUrgencyLevel(UrgencyLevel.HARD)
        assertEquals(UrgencyLevel.HARD, viewModel.uiState.value.urgencyLevel)

        viewModel.setTitle("Emergency Call")
        var saved = false
        viewModel.saveReminder(onSuccess = { saved = true })
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(saved)
        coVerify {
            repository.insertReminder(match {
                it.action.urgencyLevel == UrgencyLevel.HARD
            })
        }
    }

    @Test
    fun multiTimeWindows_addsAndRemovesWindows() = runTest {
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

        val w1 = ReminderTimeWindow(startTime = LocalTime.of(8, 0), endTime = LocalTime.of(12, 0))
        val w2 = ReminderTimeWindow(startTime = LocalTime.of(13, 0), endTime = LocalTime.of(17, 0))

        viewModel.addTimeWindow(w1)
        viewModel.addTimeWindow(w2)
        assertEquals(2, viewModel.uiState.value.timeWindows.size)

        viewModel.removeTimeWindow(0)
        assertEquals(1, viewModel.uiState.value.timeWindows.size)
        assertEquals(w2, viewModel.uiState.value.timeWindows.first())
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
        assertEquals(NotificationStyle.STANDARD, draft.action.notificationStyle)
        assertFalse(draft.action.isPersistentAlert)
    }

    @Test
    fun fixedTimes_addRemoveAndUpdate_managesListAndDraftCorrectly() = runTest {
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

        viewModel.setTime(LocalTime.of(8, 0))
        assertEquals(listOf(LocalTime.of(8, 0)), viewModel.uiState.value.fixedTimes)

        // Add 20:00 and 12:00
        viewModel.addFixedTime(LocalTime.of(20, 0))
        viewModel.addFixedTime(LocalTime.of(12, 0))

        val expectedSorted = listOf(LocalTime.of(8, 0), LocalTime.of(12, 0), LocalTime.of(20, 0))
        assertEquals(expectedSorted, viewModel.uiState.value.fixedTimes)

        var draft = viewModel.createDraftReminder()
        assertEquals(expectedSorted, draft.timeTrigger?.time)

        // Update 12:00 to 13:00
        viewModel.updateFixedTime(1, LocalTime.of(13, 0))
        val expectedUpdated = listOf(LocalTime.of(8, 0), LocalTime.of(13, 0), LocalTime.of(20, 0))
        assertEquals(expectedUpdated, viewModel.uiState.value.fixedTimes)

        // Remove 13:00
        viewModel.removeFixedTime(1)
        val expectedAfterRemove = listOf(LocalTime.of(8, 0), LocalTime.of(20, 0))
        assertEquals(expectedAfterRemove, viewModel.uiState.value.fixedTimes)

        draft = viewModel.createDraftReminder()
        assertEquals(expectedAfterRemove, draft.timeTrigger?.time)
    }

    @Test
    fun loadExistingReminder_withMultipleFixedTimes_populatesUiState() = runTest {
        val reminderId = 42L
        val existingTimes = listOf(LocalTime.of(7, 30), LocalTime.of(11, 45), LocalTime.of(19, 15))
        val existingReminder = Reminder(
            id = reminderId,
            title = "Medicine Reminder",
            triggerType = ReminderTriggerType.FIXED_TIME,
            timeTrigger = TimeTrigger(
                time = existingTimes,
                repeatMode = vn.io.litever.remind.core.model.RepeatMode.DAILY
            )
        )
        coEvery { repository.getReminderById(reminderId) } returns existingReminder

        val savedStateHandle = SavedStateHandle(mapOf("reminderId" to reminderId))
        val viewModel = ReminderEditViewModel(
            reminderRepository = repository,
            reminderScheduler = scheduler,
            reminderSyncManager = reminderSyncManager,
            permissionChecker = permissionChecker,
            preferencesDataSource = preferencesDataSource,
            savedStateHandle = savedStateHandle
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(existingTimes, viewModel.uiState.value.fixedTimes)
        assertEquals(existingTimes.first(), viewModel.uiState.value.time)
    }

    @Test
    fun toggleRingtonePlayback_togglesPlaybackStateAndCallsAudioPlayer() = runTest {
        io.mockk.mockkStatic("vn.io.litever.remind.core.common.util.SoundUtilsKt")
        val mockUri = io.mockk.mockk<android.net.Uri>(relaxed = true)
        io.mockk.every { vn.io.litever.remind.core.common.util.getAccessibleRingtoneUri(any(), any()) } returns mockUri
        try {
            val mockContext = mockk<Context>(relaxed = true)
            val mockAudioPlayer = mockk<AudioPlayer>(relaxed = true)
            val viewModel = ReminderEditViewModel(
                context = mockContext,
                reminderRepository = repository,
                reminderScheduler = scheduler,
                reminderSyncManager = reminderSyncManager,
                permissionChecker = permissionChecker,
                preferencesDataSource = preferencesDataSource,
                audioPlayer = mockAudioPlayer
            )
            testDispatcher.scheduler.advanceUntilIdle()

            assertFalse(viewModel.uiState.value.isRingtonePlaying)

            viewModel.toggleRingtonePlayback()
            assertTrue(viewModel.uiState.value.isRingtonePlaying)
            verify(exactly = 1) { mockAudioPlayer.play(any(), any(), any(), any(), any(), any(), any()) }

            viewModel.toggleRingtonePlayback()
            assertFalse(viewModel.uiState.value.isRingtonePlaying)
            verify(atLeast = 1) { mockAudioPlayer.stop() }
        } finally {
            io.mockk.unmockkStatic("vn.io.litever.remind.core.common.util.SoundUtilsKt")
        }
    }

    @Test
    fun updateRingtone_stopsPlayback() = runTest {
        io.mockk.mockkStatic("vn.io.litever.remind.core.common.util.SoundUtilsKt")
        val mockUri = io.mockk.mockk<android.net.Uri>(relaxed = true)
        io.mockk.every { vn.io.litever.remind.core.common.util.getAccessibleRingtoneUri(any(), any()) } returns mockUri
        try {
            val mockContext = mockk<Context>(relaxed = true)
            val mockAudioPlayer = mockk<AudioPlayer>(relaxed = true)
            val viewModel = ReminderEditViewModel(
                context = mockContext,
                reminderRepository = repository,
                reminderScheduler = scheduler,
                reminderSyncManager = reminderSyncManager,
                permissionChecker = permissionChecker,
                preferencesDataSource = preferencesDataSource,
                audioPlayer = mockAudioPlayer
            )
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.toggleRingtonePlayback()
            assertTrue(viewModel.uiState.value.isRingtonePlaying)

            viewModel.updateRingtone("content://settings/system/alarm_alert")
            assertFalse(viewModel.uiState.value.isRingtonePlaying)
            verify(atLeast = 1) { mockAudioPlayer.stop() }
        } finally {
            io.mockk.unmockkStatic("vn.io.litever.remind.core.common.util.SoundUtilsKt")
        }
    }

    @Test
    fun exactDates_addUpdateAndRemove_managesSortedDistinctListAndDraft() = runTest {
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

        val d1 = LocalDate.of(2026, 12, 1)
        val d2 = LocalDate.of(2026, 12, 10)
        val d3 = LocalDate.of(2026, 12, 5)

        viewModel.setDate(d1)
        viewModel.addExactDate(d2)
        viewModel.addExactDate(d3)

        assertEquals(listOf(d1, d3, d2), viewModel.uiState.value.exactDates)

        val dUpdated = LocalDate.of(2026, 12, 20)
        viewModel.updateExactDate(1, dUpdated)
        assertEquals(listOf(d1, d2, dUpdated), viewModel.uiState.value.exactDates)

        viewModel.removeExactDate(0)
        assertEquals(listOf(d2, dUpdated), viewModel.uiState.value.exactDates)

        val draft = viewModel.createDraftReminder()
        assertEquals(listOf(d2, dUpdated), draft.timeTrigger?.effectiveExactDates)
    }

    @Test
    fun repeatInterval_daily_clearsExactDatesAndSetsDailyMode() = runTest {
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

        viewModel.setRepeatInterval(vn.io.litever.remind.features.reminders.viewmodel.ReminderRepeatInterval.DAILY)
        assertNull(viewModel.uiState.value.date)
        assertTrue(viewModel.uiState.value.exactDates.isEmpty())

        val draft = viewModel.createDraftReminder()
        assertEquals(vn.io.litever.remind.core.model.RepeatMode.DAILY, draft.timeTrigger?.repeatMode)
    }

    @Test
    fun editExistingReminder_preservesCreatedAtAndLastTriggeredTime_andSyncs24HourFormat() = runTest {
        every { preferencesDataSource.is24HourFormat } returns flowOf(false)
        val originalCreatedAt = 1_700_000_000_000L
        val originalLastTriggered = 1_700_000_123_456L
        val existing = Reminder(
            id = 88L,
            title = "Existing Task",
            triggerType = ReminderTriggerType.INTERVAL,
            timeTrigger = TimeTrigger(intervalMinutes = 45),
            lastTriggeredTime = originalLastTriggered,
            createdAt = originalCreatedAt
        )
        coEvery { repository.getReminderById(88L) } returns existing

        val savedStateHandle = SavedStateHandle(mapOf("reminderId" to 88L))
        val viewModel = ReminderEditViewModel(
            reminderRepository = repository,
            reminderScheduler = scheduler,
            reminderSyncManager = reminderSyncManager,
            permissionChecker = permissionChecker,
            preferencesDataSource = preferencesDataSource,
            savedStateHandle = savedStateHandle
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.is24HourFormat)

        viewModel.setTitle("Existing Task Updated")
        val draft = viewModel.createDraftReminder()
        assertEquals(originalCreatedAt, draft.createdAt)
        assertEquals(originalLastTriggered, draft.lastTriggeredTime)
    }

    @Test
    fun switchToInterval_defaultsIntervalMinutesTo60AndPreservesOnDraft() = runTest {
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

        viewModel.setTriggerType(ReminderTriggerType.INTERVAL)
        assertEquals(60, viewModel.uiState.value.intervalMinutes)

        viewModel.setIntervalMinutes(null)
        val draft = viewModel.createDraftReminder()
        assertEquals(60, draft.timeTrigger?.intervalMinutes)
    }
}


