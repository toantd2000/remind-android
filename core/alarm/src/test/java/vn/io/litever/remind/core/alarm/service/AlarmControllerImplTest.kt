package vn.io.litever.remind.core.alarm.service

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import vn.io.litever.remind.core.alarm.AlarmRingManager
import vn.io.litever.remind.core.analytics.AnalyticsLogger
import vn.io.litever.remind.core.domain.repository.AlarmRepository
import vn.io.litever.remind.core.domain.repository.MissedAlarmRepository
import vn.io.litever.remind.core.domain.scheduler.AlarmScheduler
import vn.io.litever.remind.core.model.Alarm
import java.time.LocalTime

class AlarmControllerImplTest {

    private val context = mockk<Context>(relaxed = true)
    private val alarmRingManager = mockk<AlarmRingManager>(relaxed = true)
    private val alarmRepository = mockk<AlarmRepository>(relaxed = true)
    private val alarmScheduler = mockk<AlarmScheduler>(relaxed = true)
    private val missedAlarmRepository = mockk<MissedAlarmRepository>(relaxed = true)
    private val analyticsLogger = mockk<AnalyticsLogger>(relaxed = true)

    private val alarmManager = mockk<AlarmManager>(relaxed = true)
    private val notificationManager = mockk<NotificationManager>(relaxed = true)

    private lateinit var controller: AlarmControllerImpl

    @Before
    fun setup() {
        every { context.getSystemService(Context.ALARM_SERVICE) } returns alarmManager
        every { context.getSystemService(Context.NOTIFICATION_SERVICE) } returns notificationManager
        every { alarmRingManager.ringingAlarmId } returns MutableStateFlow<Long?>(null)

        controller = AlarmControllerImpl(
            context = context,
            alarmRingManager = alarmRingManager,
            alarmRepository = alarmRepository,
            alarmScheduler = alarmScheduler,
            missedAlarmRepository = missedAlarmRepository,
            analyticsLogger = analyticsLogger
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun dismissAlarm_whenRegularAlarm_resetsSnoozeAndUpdates() = runTest {
        val alarm = Alarm(
            id = 10L,
            time = LocalTime.of(8, 0),
            isEnabled = true,
            isQuickAlarm = false,
            currentSnoozeCount = 2,
            snoozeNextTriggerTime = 12345L
        )
        coEvery { alarmRepository.getAlarmById(10L) } returns alarm

        controller.dismissAlarm(10L)

        coVerify { alarmScheduler.cancelSnooze(alarm) }
        coVerify { alarmRepository.updateAlarm(match { it.id == 10L && it.currentSnoozeCount == 0 && it.snoozeNextTriggerTime == null }) }
        coVerify(exactly = 0) { alarmRepository.deleteAlarm(any()) }
        coVerify { alarmRingManager.dequeueAlarm(10L) }
    }

    @Test
    fun dismissAlarm_whenQuickAlarm_deletesAlarmAndCancelsSchedule() = runTest {
        val quickAlarm = Alarm(
            id = 20L,
            time = LocalTime.of(9, 30),
            isEnabled = true,
            isQuickAlarm = true
        )
        coEvery { alarmRepository.getAlarmById(20L) } returns quickAlarm

        controller.dismissAlarm(20L)

        coVerify { alarmScheduler.cancelSnooze(quickAlarm) }
        coVerify { alarmScheduler.cancel(quickAlarm) }
        coVerify { alarmRepository.deleteAlarm(quickAlarm) }
        coVerify(exactly = 0) { alarmRepository.updateAlarm(any()) }
        coVerify { alarmRingManager.dequeueAlarm(20L) }
    }
}
