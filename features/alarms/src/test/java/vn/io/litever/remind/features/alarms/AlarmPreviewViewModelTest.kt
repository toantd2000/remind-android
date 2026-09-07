package vn.io.litever.remind.features.alarms

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.unmockkStatic
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import vn.io.litever.remind.core.alarm.AlarmRingManager
import vn.io.litever.remind.core.alarm.DraftAlarmStore
import vn.io.litever.remind.core.common.audio.AudioPlayer
import vn.io.litever.remind.core.common.util.getAccessibleRingtoneUri
import vn.io.litever.remind.core.datastore.AlarmPreferencesDataSource
import vn.io.litever.remind.core.domain.repository.AlarmRepository
import vn.io.litever.remind.core.model.Alarm
import vn.io.litever.remind.features.alarms.viewmodel.AlarmPreviewViewModel
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AlarmPreviewViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val alarmRepository = mockk<AlarmRepository>(relaxed = true)
    private val preferencesDataSource = mockk<AlarmPreferencesDataSource>(relaxed = true)
    private val draftAlarmStore = mockk<DraftAlarmStore>(relaxed = true)
    private val alarmRingManager = mockk<AlarmRingManager>(relaxed = true)
    private val audioPlayer = mockk<AudioPlayer>(relaxed = true)
    private val context = mockk<Context>(relaxed = true)

    private val allAlarmsFlow = MutableStateFlow<List<Alarm>>(emptyList())
    private val mutedAlarmIdsFlow = MutableStateFlow<Set<Long>>(emptySet())

    private lateinit var viewModel: AlarmPreviewViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic("vn.io.litever.remind.core.common.util.SoundUtilsKt")
        val mockUri = mockk<Uri>(relaxed = true)
        every { getAccessibleRingtoneUri(any(), any()) } returns mockUri
        every { context.packageName } returns "vn.io.litever.remind"
        every { alarmRepository.getAllAlarms() } returns allAlarmsFlow
        every { preferencesDataSource.is24HourFormat } returns flowOf(true)
        every { draftAlarmStore.getDraft() } returns null
        every { alarmRingManager.mutedAlarmIds } returns mutedAlarmIdsFlow

        val savedStateHandle = SavedStateHandle(mapOf("alarmId" to 99L))
        viewModel = AlarmPreviewViewModel(
            savedStateHandle = savedStateHandle,
            alarmRepository = alarmRepository,
            preferencesDataSource = preferencesDataSource,
            draftAlarmStore = draftAlarmStore,
            alarmRingManager = alarmRingManager,
            audioPlayer = audioPlayer,
            context = context
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
        unmockkStatic("vn.io.litever.remind.core.common.util.SoundUtilsKt")
    }

    @Test
    fun initialLoad_playsPreviewRingtoneAndSetsAutoSilence() = runTest(testDispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.alarm.collect()
        }
        val alarm = Alarm(id = 99L, time = LocalTime.of(7, 0), label = "Preview Alarm", autoSilenceMinutes = 3, volume = 10)
        allAlarmsFlow.value = listOf(alarm)
        testScheduler.runCurrent()

        verify {
            audioPlayer.play(
                uri = any(),
                useAlarmStream = any(),
                volume = 10,
                gradualVolumeDurationSeconds = any(),
                vibrationEnabled = any()
            )
        }
        assertEquals(180, viewModel.autoSilenceCountdown.value)
    }

    @Test
    fun initialLoad_usesDraftFromStoreIfPresent() = runTest(testDispatcher) {
        val draft = Alarm(id = 0L, time = LocalTime.of(6, 30), label = "Draft Preview", volume = 8)
        every { draftAlarmStore.getDraft() } returns draft

        val savedStateHandle = SavedStateHandle(mapOf("alarmId" to 0L))
        val draftVm = AlarmPreviewViewModel(
            savedStateHandle = savedStateHandle,
            alarmRepository = alarmRepository,
            preferencesDataSource = preferencesDataSource,
            draftAlarmStore = draftAlarmStore,
            alarmRingManager = alarmRingManager,
            audioPlayer = audioPlayer,
            context = context
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            draftVm.alarm.collect()
        }
        testScheduler.runCurrent()

        assertEquals("Draft Preview", draftVm.alarm.value?.label)
    }

    @Test
    fun stopPreview_stopsAudioCancelsAutoSilenceAndClearsDraft() = runTest(testDispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.alarm.collect()
        }
        val alarm = Alarm(id = 99L, time = LocalTime.of(7, 0), autoSilenceMinutes = 3)
        allAlarmsFlow.value = listOf(alarm)
        testScheduler.runCurrent()

        viewModel.stopPreview()
        testScheduler.runCurrent()

        verify { audioPlayer.stop() }
        verify { draftAlarmStore.setDraft(null) }
        coVerify { alarmRingManager.unmute(99L) }
    }

    @Test
    fun startMissionPreview_mutesAlarmInRingManager() = runTest(testDispatcher) {
        viewModel.startMissionPreview()
        testScheduler.runCurrent()

        coVerify { alarmRingManager.mute(99L) }
    }

    @Test
    fun mutedAlarmsFlow_adjustsVolumeToZeroWhenMuted() = runTest(testDispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.alarm.collect()
        }
        val alarm = Alarm(id = 99L, time = LocalTime.of(7, 0), volume = 12)
        allAlarmsFlow.value = listOf(alarm)
        testScheduler.runCurrent()

        // Mute alarm
        mutedAlarmIdsFlow.value = setOf(99L)
        testScheduler.runCurrent()

        verify { audioPlayer.setVolume(any(), 0) }

        // Unmute alarm
        mutedAlarmIdsFlow.value = emptySet()
        testScheduler.runCurrent()

        verify { audioPlayer.setVolume(any(), 12) }
    }
}
