package vn.io.litever.remind.features.alarms

import android.content.Context
import android.media.AudioManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
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
import vn.io.litever.remind.core.alarm.DraftAlarmStore
import vn.io.litever.remind.core.common.audio.AudioPlayer
import vn.io.litever.remind.core.common.util.PermissionChecker
import vn.io.litever.remind.core.datastore.AlarmPreferencesDataSource
import vn.io.litever.remind.core.domain.repository.AlarmRepository
import vn.io.litever.remind.core.domain.repository.MissionRepository
import vn.io.litever.remind.core.domain.scheduler.AlarmScheduler
import vn.io.litever.remind.core.model.Alarm
import vn.io.litever.remind.core.model.DayOfWeek
import vn.io.litever.remind.core.model.Mission
import vn.io.litever.remind.core.model.MissionType
import vn.io.litever.remind.features.alarms.viewmodel.AlarmEditViewModel
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class AlarmEditViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repository = mockk<AlarmRepository>(relaxed = true)
    private val missionRepository = mockk<MissionRepository>(relaxed = true)
    private val alarmScheduler = mockk<AlarmScheduler>(relaxed = true)
    private val preferencesDataSource = mockk<AlarmPreferencesDataSource>(relaxed = true)
    private val permissionChecker = mockk<PermissionChecker>(relaxed = true)
    private val draftAlarmStore = mockk<DraftAlarmStore>(relaxed = true)
    private val audioPlayer = mockk<AudioPlayer>(relaxed = true)
    private val context = mockk<Context>(relaxed = true)
    private val audioManager = mockk<AudioManager>(relaxed = true)

    private lateinit var viewModel: AlarmEditViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { context.getSystemService(Context.AUDIO_SERVICE) } returns audioManager
        every { audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM) } returns 15
        every { audioManager.getStreamVolume(AudioManager.STREAM_ALARM) } returns 10
        every { preferencesDataSource.is24HourFormat } returns flowOf(true)
        every { permissionChecker.hasCriticalPermissions() } returns true
        every { missionRepository.getMissionsForAlarm(any()) } returns flowOf(emptyList())

        viewModel = AlarmEditViewModel(
            repository = repository,
            missionRepository = missionRepository,
            alarmScheduler = alarmScheduler,
            preferencesDataSource = preferencesDataSource,
            permissionChecker = permissionChecker,
            draftAlarmStore = draftAlarmStore,
            audioPlayer = audioPlayer,
            context = context
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun loadAlarm_newDraft_insertsDisabledPlaceholderAndSetsNewDraft() = runTest(testDispatcher) {
        coEvery { repository.insertAlarm(any()) } returns 100L
        viewModel.loadAlarm(0L)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(100L, state.id)
        assertTrue(state.isNewDraft)
        assertFalse(state.isLoading)
    }

    @Test
    fun loadAlarm_existingAlarm_loadsPropertiesAndMissions() = runTest(testDispatcher) {
        val existing = Alarm(
            id = 50L,
            time = LocalTime.of(6, 45),
            label = "Workout",
            message = "Time to exercise",
            volume = 12,
            vibrationEnabled = true,
            snoozeEnabled = true,
            snoozeInterval = 10,
            snoozeRepeatCount = 2,
            autoSilenceMinutes = 5,
            gradualVolumeDurationSeconds = 10,
            useAlarmStream = true
        )
        val mission = Mission(id = 1L, alarmId = 50L, type = MissionType.MATH, order = 0)
        coEvery { repository.getAlarmById(50L) } returns existing
        every { missionRepository.getMissionsForAlarm(50L) } returns flowOf(listOf(mission))

        viewModel.loadAlarm(50L)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(50L, state.id)
        assertEquals("Workout", state.label)
        assertEquals("Time to exercise", state.message)
        assertEquals(LocalTime.of(6, 45), state.time)
        assertEquals(12, state.volume)
        assertEquals(1, state.missions.size)
        assertEquals(MissionType.MATH, state.missions[0].type)
        assertFalse(state.isNewDraft)
    }

    @Test
    fun updateTime_updateLabel_updateMessage_updatesUiState() = runTest(testDispatcher) {
        viewModel.loadAlarm(0L)
        testDispatcher.scheduler.advanceUntilIdle()

        val newTime = LocalTime.of(8, 15)
        viewModel.updateTime(newTime)
        viewModel.updateLabel("Team Meeting")
        viewModel.updateMessage("Prep slides")

        val state = viewModel.uiState.value
        assertEquals(newTime, state.time)
        assertEquals("Team Meeting", state.label)
        assertEquals("Prep slides", state.message)
    }

    @Test
    fun toggleRepeatDay_and_updateDate_mutuallyClear() = runTest(testDispatcher) {
        viewModel.loadAlarm(0L)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleRepeatDay(DayOfWeek.MONDAY)
        assertEquals(listOf(DayOfWeek.MONDAY), viewModel.uiState.value.repeatDays)
        assertNull(viewModel.uiState.value.date)

        val targetDate = LocalDate.of(2026, 12, 25)
        viewModel.updateDate(targetDate)
        assertEquals(targetDate, viewModel.uiState.value.date)
        assertTrue(viewModel.uiState.value.repeatDays.isEmpty())
    }

    @Test
    fun updateVibration_updateVolume_updateRingtone_updatesState() = runTest(testDispatcher) {
        viewModel.loadAlarm(0L)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateVibration(false)
        assertFalse(viewModel.uiState.value.vibrationEnabled)

        viewModel.updateVolume(8)
        assertEquals(8, viewModel.uiState.value.volume)

        viewModel.updateRingtone("content://media/internal/audio/media/1")
        assertEquals("content://media/internal/audio/media/1", viewModel.uiState.value.ringtoneUri)
    }

    @Test
    fun updateSnoozeSettings_and_updateAutoSilence_and_updateGradualVolume() = runTest(testDispatcher) {
        viewModel.loadAlarm(0L)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateSnoozeSettings(enabled = true, interval = 10, repeatCount = 5)
        assertEquals(true, viewModel.uiState.value.snoozeEnabled)
        assertEquals(10, viewModel.uiState.value.snoozeInterval)
        assertEquals(5, viewModel.uiState.value.snoozeRepeatCount)

        viewModel.updateAutoSilence(5)
        assertEquals(5, viewModel.uiState.value.autoSilenceMinutes)

        viewModel.updateGradualVolumeDuration(15)
        assertEquals(15, viewModel.uiState.value.gradualVolumeDurationSeconds)

        viewModel.updateUseAlarmStream(false)
        assertFalse(viewModel.uiState.value.useAlarmStream)
    }

    @Test
    fun missionOperations_add_update_remove_reorders() = runTest(testDispatcher) {
        viewModel.loadAlarm(0L)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.addMission(MissionType.MATH)
        viewModel.addMission(MissionType.TYPING)
        assertEquals(2, viewModel.uiState.value.missions.size)
        assertEquals(0, viewModel.uiState.value.missions[0].order)
        assertEquals(1, viewModel.uiState.value.missions[1].order)

        val firstMission = viewModel.uiState.value.missions[0]
        viewModel.removeMission(firstMission)
        assertEquals(1, viewModel.uiState.value.missions.size)
        assertEquals(MissionType.TYPING, viewModel.uiState.value.missions[0].type)
        assertEquals(0, viewModel.uiState.value.missions[0].order)
    }

    @Test
    fun addMission_enforcesMax5MissionsLimit() = runTest(testDispatcher) {
        viewModel.loadAlarm(0L)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.addMission(MissionType.MATH)
        viewModel.addMission(MissionType.TYPING)
        viewModel.addMission(MissionType.SHAKE)
        viewModel.addMission(MissionType.QR_CODE)
        viewModel.addMission(MissionType.MEMORY_FIND_COLOR_TILES)
        assertEquals(5, viewModel.uiState.value.missions.size)

        // 6th mission is rejected
        viewModel.addMission(MissionType.FIND_ITEM)
        assertEquals(5, viewModel.uiState.value.missions.size)
    }

    @Test
    fun saveAlarm_whenPermissionsMissing_showsPermissionDialog() = runTest(testDispatcher) {
        every { permissionChecker.hasCriticalPermissions() } returns false
        viewModel.loadAlarm(0L)
        testDispatcher.scheduler.advanceUntilIdle()

        var saved = false
        viewModel.saveAlarm(onSuccess = { saved = true })
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(saved)
        assertTrue(viewModel.uiState.value.showPermissionDialog)
    }

    @Test
    fun saveAlarm_whenPermissionsGranted_newDraftSavesAndSchedules() = runTest(testDispatcher) {
        coEvery { repository.insertAlarm(any()) } returns 300L
        viewModel.loadAlarm(0L)
        testDispatcher.scheduler.advanceUntilIdle()

        var saved = false
        viewModel.saveAlarm(onSuccess = { saved = true })
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(saved)
        assertFalse(viewModel.uiState.value.showPermissionDialog)
        coVerify { repository.updateAlarm(match { it.id == 300L && it.isEnabled }) }
        coVerify { alarmScheduler.schedule(match { it.id == 300L && it.isEnabled }) }
    }

    @Test
    fun saveAnyway_savesDisabledAlarm() = runTest(testDispatcher) {
        coEvery { repository.insertAlarm(any()) } returns 200L
        viewModel.loadAlarm(0L)
        testDispatcher.scheduler.advanceUntilIdle()

        var saved = false
        viewModel.saveAnyway(onSuccess = { saved = true })
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(saved)
        assertFalse(viewModel.uiState.value.showPermissionDialog)
        coVerify { repository.updateAlarm(match { it.id == 200L && !it.isEnabled }) }
    }

    @Test
    fun dismissPermissionDialog_hidesDialog() = runTest(testDispatcher) {
        every { permissionChecker.hasCriticalPermissions() } returns false
        viewModel.loadAlarm(0L)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.saveAlarm(onSuccess = {})
        assertTrue(viewModel.uiState.value.showPermissionDialog)

        viewModel.dismissPermissionDialog()
        assertFalse(viewModel.uiState.value.showPermissionDialog)
    }

    @Test
    fun discardChanges_forNewDraftDeletesPlaceholder() = runTest(testDispatcher) {
        val draftAlarm = Alarm(id = 400L, time = LocalTime.of(8, 0), isEnabled = false)
        coEvery { repository.insertAlarm(any()) } returns 400L
        coEvery { repository.getAlarmById(400L) } returns draftAlarm

        viewModel.loadAlarm(0L)
        testDispatcher.scheduler.advanceUntilIdle()

        var discarded = false
        viewModel.discardChanges(onDiscarded = { discarded = true })
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(discarded)
        coVerify { repository.deleteAlarm(match { it.id == 400L }) }
    }

    @Test
    fun hasChanges_detectsModificationsAgainstOriginal() = runTest(testDispatcher) {
        val existing = Alarm(id = 500L, time = LocalTime.of(7, 0), label = "Original")
        coEvery { repository.getAlarmById(500L) } returns existing

        viewModel.loadAlarm(500L)
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.hasChanges())

        viewModel.updateLabel("Modified Label")
        assertTrue(viewModel.hasChanges())
    }

    @Test
    fun preparePreview_setsDraftAlarmInStore() = runTest(testDispatcher) {
        viewModel.loadAlarm(0L)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.preparePreview()

        coVerify { draftAlarmStore.setDraft(any()) }
    }
}
