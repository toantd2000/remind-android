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
import vn.io.litever.remind.core.alarm.alert.ReminderAlertManager
import vn.io.litever.remind.core.alarm.notification.ReminderNotificationManager
import vn.io.litever.remind.core.datastore.AlarmPreferencesDataSource
import vn.io.litever.remind.core.domain.repository.ReminderRepository
import vn.io.litever.remind.core.domain.scheduler.ReminderScheduler
import vn.io.litever.remind.core.model.ChecklistItem
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.features.reminders.viewmodel.ReminderRingingViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class ReminderRingingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repository = mockk<ReminderRepository>(relaxed = true)
    private val scheduler = mockk<ReminderScheduler>(relaxed = true)
    private val alertManager = mockk<ReminderAlertManager>(relaxed = true)
    private val notificationManager = mockk<ReminderNotificationManager>(relaxed = true)
    private val preferencesDataSource = mockk<AlarmPreferencesDataSource>(relaxed = true)

    private val reminderFlow = MutableStateFlow<Reminder?>(null)
    private val countdownFlow = MutableStateFlow<Int?>(null)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { repository.getReminderFlow(1L) } returns reminderFlow
        every { alertManager.autoSilenceCountdown } returns countdownFlow
        every { preferencesDataSource.is24HourFormat } returns flowOf(true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialLoad_observesReminderAndCountdown() = runTest(testDispatcher) {
        val reminder = Reminder(
            id = 1L,
            title = "Morning Medicine",
            triggerType = ReminderTriggerType.TIME,
            checklist = listOf(ChecklistItem(id = 10, reminderId = 1L, text = "Vitamin C"))
        )
        reminderFlow.value = reminder
        countdownFlow.value = 45

        val savedStateHandle = SavedStateHandle(mapOf("reminderId" to 1L))
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

        val state = viewModel.uiState.value
        assertEquals("Morning Medicine", state.reminder?.title)
        assertEquals(45, state.autoSilenceCountdown)
        assertTrue(state.isRinging)
    }

    @Test
    fun toggleChecklistItem_persistsToRepositoryAndUpdatesNotification() = runTest(testDispatcher) {
        val reminder = Reminder(
            id = 1L,
            title = "Morning Medicine",
            triggerType = ReminderTriggerType.TIME,
            checklist = listOf(ChecklistItem(id = 10, reminderId = 1L, text = "Vitamin C", isCompleted = false))
        )
        val refreshed = reminder.copy(
            checklist = listOf(ChecklistItem(id = 10, reminderId = 1L, text = "Vitamin C", isCompleted = true))
        )
        reminderFlow.value = reminder
        coEvery { repository.getReminderById(1L) } returns refreshed

        val savedStateHandle = SavedStateHandle(mapOf("reminderId" to 1L))
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

        viewModel.toggleChecklistItem(10L, true)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.updateChecklistItemCompletion(10L, true) }
        coVerify { notificationManager.updateNotification(refreshed) }
    }

    @Test
    fun snoozeAction_stopsAlert_schedulesSnooze_andFinishes() = runTest(testDispatcher) {
        val reminder = Reminder(
            id = 1L,
            title = "Morning Medicine",
            triggerType = ReminderTriggerType.TIME,
            snoozeEnabled = true,
            snoozeIntervalMinutes = 10,
            snoozeRepeatCount = 3,
            currentSnoozeCount = 0
        )
        reminderFlow.value = reminder
        coEvery { repository.getReminderById(1L) } returns reminder

        val savedStateHandle = SavedStateHandle(mapOf("reminderId" to 1L))
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

        var finishCalled = false
        viewModel.snooze(onFinished = { finishCalled = true })
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(finishCalled)
        assertFalse(viewModel.uiState.value.isRinging)
        coVerify { alertManager.stopAlert(1L) }
        coVerify { notificationManager.cancelNotification(1L) }
        coVerify {
            repository.updateReminder(match {
                it.id == 1L && it.currentSnoozeCount == 1 && it.snoozeNextTriggerTime != null
            })
        }
        coVerify { scheduler.scheduleSnooze(match { it.id == 1L }, any()) }
    }

    @Test
    fun dismissAction_stopsAlert_marksCompleted_cancelsScheduler_andFinishes() = runTest(testDispatcher) {
        val reminder = Reminder(
            id = 1L,
            title = "Morning Medicine",
            triggerType = ReminderTriggerType.TIME,
            isCompleted = false
        )
        reminderFlow.value = reminder
        coEvery { repository.getReminderById(1L) } returns reminder

        val savedStateHandle = SavedStateHandle(mapOf("reminderId" to 1L))
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

        var finishCalled = false
        viewModel.dismiss(onFinished = { finishCalled = true })
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(finishCalled)
        assertFalse(viewModel.uiState.value.isRinging)
        coVerify { alertManager.stopAlert(1L) }
        coVerify { notificationManager.cancelNotification(1L) }
        coVerify {
            repository.updateReminder(match {
                it.id == 1L && it.isCompleted && !it.isEnabled
            })
        }
        coVerify { scheduler.cancelSnooze(match { it.id == 1L }) }
    }
}
