package vn.io.litever.remind.features.alarms

import androidx.lifecycle.SavedStateHandle
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import vn.io.litever.remind.core.alarm.AlarmRingManager
import vn.io.litever.remind.core.datastore.AlarmPreferencesDataSource
import vn.io.litever.remind.core.domain.repository.AlarmRepository
import vn.io.litever.remind.core.domain.repository.TodayRepository
import vn.io.litever.remind.core.domain.repository.WeatherRepository
import vn.io.litever.remind.core.domain.scheduler.AlarmController
import vn.io.litever.remind.core.model.Alarm
import vn.io.litever.remind.features.alarms.viewmodel.AlarmRingingViewModel
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class AlarmRingingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val alarmRepository = mockk<AlarmRepository>(relaxed = true)
    private val alarmController = mockk<AlarmController>(relaxed = true)
    private val preferencesDataSource = mockk<AlarmPreferencesDataSource>(relaxed = true)
    private val alarmRingManager = mockk<AlarmRingManager>(relaxed = true)
    private val weatherRepository = mockk<WeatherRepository>(relaxed = true)
    private val todayRepository = mockk<TodayRepository>(relaxed = true)

    private val allAlarmsFlow = MutableStateFlow<List<Alarm>>(emptyList())
    private val countdownFlow = MutableStateFlow<Int?>(null)

    private lateinit var viewModel: AlarmRingingViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { alarmRepository.getAllAlarms() } returns allAlarmsFlow
        every { preferencesDataSource.is24HourFormat } returns flowOf(true)
        every { alarmRingManager.autoSilenceCountdown } returns countdownFlow
        every { weatherRepository.getRemindWeather() } returns flowOf(null)
        every { todayRepository.getTodayBriefing() } returns flowOf(null)

        val savedStateHandle = SavedStateHandle(mapOf("alarmId" to 42L))
        viewModel = AlarmRingingViewModel(
            savedStateHandle = savedStateHandle,
            alarmRepository = alarmRepository,
            alarmController = alarmController,
            preferencesDataSource = preferencesDataSource,
            alarmRingManager = alarmRingManager,
            weatherRepository = weatherRepository,
            TodayRepository = todayRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun initialLoad_observesAlarmAndCountdown() = runTest(testDispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.alarm.collect()
        }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.autoSilenceCountdown.collect()
        }
        val ringingAlarm = Alarm(id = 42L, time = LocalTime.of(7, 30), label = "Morning Call")
        allAlarmsFlow.value = listOf(ringingAlarm)
        countdownFlow.value = 180
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Morning Call", viewModel.alarm.value?.label)
        assertEquals(180, viewModel.autoSilenceCountdown.value)
    }

    @Test
    fun dismissAlarm_setsAcknowledgingAlarmIdAndDismisses() = runTest(testDispatcher) {
        viewModel.dismissAlarm()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { alarmRingManager.setAcknowledgingAlarmId(42L) }
        coVerify { alarmController.dismissAlarm(42L) }
    }

    @Test
    fun onFinishMessage_clearsAcknowledgingAlarmIdAndDismisses() = runTest(testDispatcher) {
        viewModel.onFinishMessage()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { alarmRingManager.setAcknowledgingAlarmId(null) }
        coVerify { alarmController.dismissAlarm(42L) }
    }

    @Test
    fun snoozeAlarm_triggersAlarmControllerSnooze() = runTest(testDispatcher) {
        viewModel.snoozeAlarm()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { alarmController.snoozeAlarm(42L) }
    }

    @Test
    fun startMission_enqueuesMutesAndCancelsSnooze() = runTest(testDispatcher) {
        viewModel.startMission()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { alarmRingManager.enqueueAlarm(42L) }
        coVerify { alarmRingManager.mute(42L) }
        coVerify { alarmController.cancelSnooze(42L) }
    }

    @Test
    fun onAbandonMission_unmutesAlarm() = runTest(testDispatcher) {
        viewModel.onAbandonMission()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { alarmRingManager.unmute(42L) }
    }

    @Test
    fun setRingingScreenVisible_delegatesToRingManager() = runTest(testDispatcher) {
        viewModel.setRingingScreenVisible(true)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { alarmRingManager.setRingingScreenVisible(true) }
    }
}
