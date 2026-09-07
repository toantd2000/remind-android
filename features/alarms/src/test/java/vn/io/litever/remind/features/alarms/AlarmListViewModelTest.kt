package vn.io.litever.remind.features.alarms

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
import vn.io.litever.remind.core.datastore.AlarmPreferencesDataSource
import vn.io.litever.remind.core.domain.repository.AlarmRepository
import vn.io.litever.remind.core.domain.repository.MissionRepository
import vn.io.litever.remind.core.domain.scheduler.AlarmScheduler
import vn.io.litever.remind.core.model.Alarm
import vn.io.litever.remind.core.model.DayOfWeek
import vn.io.litever.remind.core.model.Mission
import vn.io.litever.remind.core.model.MissionType
import vn.io.litever.remind.core.model.Phrase
import vn.io.litever.remind.core.model.TypingMissionConfig
import vn.io.litever.remind.features.alarms.ui.state.NextAlarmUiState
import vn.io.litever.remind.features.alarms.viewmodel.AlarmListViewModel
import java.time.LocalDateTime
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class AlarmListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repository = mockk<AlarmRepository>(relaxed = true)
    private val missionRepository = mockk<MissionRepository>(relaxed = true)
    private val alarmScheduler = mockk<AlarmScheduler>(relaxed = true)
    private val preferencesDataSource = mockk<AlarmPreferencesDataSource>(relaxed = true)
    private val permissionChecker = mockk<PermissionChecker>(relaxed = true)

    private val allAlarmsFlow = MutableStateFlow<List<Alarm>>(emptyList())
    private val is24HourFlow = MutableStateFlow(true)
    private val adsDisabledUntilFlow = MutableStateFlow(0L)

    private lateinit var viewModel: AlarmListViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { repository.getAllAlarms() } returns allAlarmsFlow
        every { preferencesDataSource.is24HourFormat } returns is24HourFlow
        every { preferencesDataSource.adsDisabledUntil } returns adsDisabledUntilFlow
        every { permissionChecker.hasCriticalPermissions() } returns true

        viewModel = AlarmListViewModel(
            repository = repository,
            missionRepository = missionRepository,
            alarmScheduler = alarmScheduler,
            preferencesDataSource = preferencesDataSource,
            permissionChecker = permissionChecker
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun initialLoad_emptyAlarms_stateReflectsEmpty() = runTest(testDispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.alarms.collect()
        }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.nextAlarmTime.collect()
        }
        testDispatcher.scheduler.advanceUntilIdle()
        val alarms = viewModel.alarms.value
        assertEquals(emptyList<Alarm>(), alarms)
        assertTrue(viewModel.hasCriticalPermissions.value)
        assertEquals(NextAlarmUiState.AllOff, viewModel.nextAlarmTime.value)
    }

    @Test
    fun initialLoad_sortsAlarmsByEnabledDescendingThenTime() = runTest(testDispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.alarms.collect()
        }
        val alarm1 = Alarm(id = 1L, time = LocalTime.of(9, 0), isEnabled = false)
        val alarm2 = Alarm(id = 2L, time = LocalTime.of(8, 0), isEnabled = true)
        val alarm3 = Alarm(id = 3L, time = LocalTime.of(6, 30), isEnabled = true)
        allAlarmsFlow.value = listOf(alarm1, alarm2, alarm3)
        testDispatcher.scheduler.advanceUntilIdle()

        val alarms = viewModel.alarms.value
        assertNotNull(alarms)
        assertEquals(3, alarms?.size)
        assertEquals(3L, alarms?.get(0)?.id) // 6:30 Enabled
        assertEquals(2L, alarms?.get(1)?.id) // 8:00 Enabled
        assertEquals(1L, alarms?.get(2)?.id) // 9:00 Disabled
    }

    @Test
    fun toggleAlarm_whenPermissionGranted_enablesAndSchedules() = runTest(testDispatcher) {
        val alarm = Alarm(id = 1L, time = LocalTime.of(7, 0), isEnabled = false)
        viewModel.toggleAlarm(alarm)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.updateAlarm(match { it.id == 1L && it.isEnabled && it.skippedAt == null }) }
        coVerify { alarmScheduler.schedule(match { it.id == 1L && it.isEnabled }) }
    }

    @Test
    fun toggleAlarm_whenPermissionMissing_blocksAndEmitsError() = runTest(testDispatcher) {
        every { permissionChecker.hasCriticalPermissions() } returns false
        val alarm = Alarm(id = 2L, time = LocalTime.of(8, 0), isEnabled = false)

        viewModel.toggleAlarm(alarm)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { repository.updateAlarm(any()) }
        coVerify(exactly = 0) { alarmScheduler.schedule(any()) }
        assertFalse(viewModel.hasCriticalPermissions.value)
    }

    @Test
    fun toggleAlarm_whenDisabling_disablesAndCancelsSchedule() = runTest(testDispatcher) {
        val alarm = Alarm(id = 3L, time = LocalTime.of(7, 0), isEnabled = true)
        viewModel.toggleAlarm(alarm)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.updateAlarm(match { it.id == 3L && !it.isEnabled }) }
        coVerify { alarmScheduler.cancel(match { it.id == 3L && !it.isEnabled }) }
    }

    @Test
    fun deleteAlarm_cancelsAndDeletes_emitsSingleUndoEvent() = runTest(testDispatcher) {
        val alarm = Alarm(id = 4L, time = LocalTime.of(6, 30), isEnabled = true)
        viewModel.deleteAlarm(alarm)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.deleteAlarm(alarm) }
        coVerify { alarmScheduler.cancel(alarm) }

        viewModel.undoDelete()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.insertAlarm(alarm) }
        coVerify { alarmScheduler.schedule(alarm) }
    }

    @Test
    fun deleteDisabledAlarms_deletesOnlyDisabledAlarms() = runTest(testDispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.alarms.collect()
        }
        val active = Alarm(id = 10L, time = LocalTime.of(7, 0), isEnabled = true)
        val disabled = Alarm(id = 11L, time = LocalTime.of(8, 0), isEnabled = false)
        allAlarmsFlow.value = listOf(active, disabled)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.deleteDisabledAlarms()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.deleteAlarm(disabled) }
        coVerify { alarmScheduler.cancel(disabled) }
        coVerify(exactly = 0) { repository.deleteAlarm(active) }

        // Undo batch delete
        viewModel.undoDelete()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.insertAlarm(disabled) }
    }

    @Test
    fun duplicateAlarm_duplicatesAlarmAndMissionsAndPrivatePhrases() = runTest(testDispatcher) {
        val alarm = Alarm(id = 50L, time = LocalTime.of(6, 0), isEnabled = true)
        val mission = Mission(id = 1L, alarmId = 50L, type = MissionType.TYPING, order = 0, config = TypingMissionConfig(selectedPhraseIds = listOf(100L)))
        val phrase = Phrase(id = 100L, alarmId = 50L, content = "Rise and shine", categoryId = "custom", isCustom = true, isShared = false)

        coEvery { repository.getMissionsForAlarm(50L) } returns listOf(mission)
        every { missionRepository.getCustomPhrases(50L) } returns flowOf(listOf(phrase))
        coEvery { repository.insertAlarm(any()) } returns 51L
        coEvery { missionRepository.savePhrase(any()) } returns 200L

        viewModel.duplicateAlarm(alarm)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.insertAlarm(match { it.id == 0L && it.time == LocalTime.of(6, 0) }) }
        coVerify { missionRepository.savePhrase(match { it.alarmId == 51L && it.content == "Rise and shine" }) }
        coVerify { missionRepository.saveMission(match { it.alarmId == 51L && (it.config as? TypingMissionConfig)?.selectedPhraseIds == listOf(200L) }) }
        coVerify { alarmScheduler.schedule(match { it.id == 51L && it.isEnabled }) }
    }

    @Test
    fun skipNextOccurrence_setsSkippedAtAndReschedules() = runTest(testDispatcher) {
        val alarm = Alarm(id = 20L, time = LocalTime.of(7, 0), isEnabled = true, repeatDays = listOf(DayOfWeek.MONDAY))
        viewModel.skipNextOccurrence(alarm)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.updateAlarm(match { it.id == 20L && it.skippedAt != null }) }
        coVerify { alarmScheduler.schedule(match { it.id == 20L && it.skippedAt != null }) }
    }

    @Test
    fun cancelSkipOccurrence_clearsSkippedAtAndReschedules() = runTest(testDispatcher) {
        val alarm = Alarm(id = 21L, time = LocalTime.of(7, 0), isEnabled = true, skippedAt = LocalDateTime.now().plusDays(1))
        viewModel.cancelSkipOccurrence(alarm)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.updateAlarm(match { it.id == 21L && it.skippedAt == null }) }
        coVerify { alarmScheduler.schedule(match { it.id == 21L && it.skippedAt == null }) }
    }

    @Test
    fun disableAdsFor24Hours_setsAdsDisabledUntilInPreferences() = runTest(testDispatcher) {
        viewModel.disableAdsFor24Hours()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { preferencesDataSource.setAdsDisabledUntil(match { it > System.currentTimeMillis() }) }
    }

    @Test
    fun refreshPermissions_updatesHasCriticalPermissions() = runTest(testDispatcher) {
        every { permissionChecker.hasCriticalPermissions() } returns false
        viewModel.refreshPermissions()
        assertFalse(viewModel.hasCriticalPermissions.value)

        every { permissionChecker.hasCriticalPermissions() } returns true
        viewModel.refreshPermissions()
        assertTrue(viewModel.hasCriticalPermissions.value)
    }
}
