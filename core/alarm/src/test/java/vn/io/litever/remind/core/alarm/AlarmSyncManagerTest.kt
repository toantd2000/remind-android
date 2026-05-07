package vn.io.litever.remind.core.alarm

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import vn.io.litever.remind.core.domain.repository.AlarmRepository
import vn.io.litever.remind.core.domain.repository.MissedAlarmRepository
import vn.io.litever.remind.core.domain.scheduler.AlarmScheduler
import vn.io.litever.remind.core.datastore.AlarmPreferencesDataSource
import vn.io.litever.remind.core.model.Alarm
import vn.io.litever.remind.core.model.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class AlarmSyncManagerTest {
    private val alarmRepository = mockk<AlarmRepository>(relaxed = true)
    private val alarmScheduler = mockk<AlarmScheduler>(relaxed = true)
    private val missedAlarmRepository = mockk<MissedAlarmRepository>(relaxed = true)
    private val preferencesDataSource = mockk<AlarmPreferencesDataSource>(relaxed = true)
    
    private lateinit var syncManager: AlarmSyncManager

    @Before
    fun setup() {
        syncManager = AlarmSyncManager(
            alarmRepository,
            alarmScheduler,
            missedAlarmRepository,
            preferencesDataSource
        )
    }

    @Test
    fun `sync should not mark skipped alarm as missed`() = runTest {
        // Given: An alarm at 7:00 AM today, which was skipped
        val now = LocalDateTime.of(2024, 1, 1, 8, 0) // It's 8:00 AM
        val alarmTime = LocalTime.of(7, 0)
        val skippedTime = LocalDateTime.of(2024, 1, 1, 7, 0)
        
        val alarm = Alarm(
            id = 1,
            time = alarmTime,
            isEnabled = true,
            repeatDays = listOf(DayOfWeek.MONDAY), // Assume 2024-01-01 is Monday
            skippedAt = skippedTime
        )
        
        coEvery { alarmRepository.getAllAlarms() } returns flowOf(listOf(alarm))
        every { preferencesDataSource.lastMissedCheckTime } returns flowOf(
            LocalDateTime.of(2024, 1, 1, 6, 0)
                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )

        // When: Syncing
        syncManager.sync()

        // Then: missedAlarmRepository should NOT have any inserts for this alarm
        coVerify(exactly = 0) { 
            missedAlarmRepository.insertMissedAlarm(any()) 
        }

        // And: skippedAt should be cleared eventually because it's expired
        coVerify(exactly = 1) {
            alarmRepository.updateAlarm(match { it.id == 1L && it.skippedAt == null })
        }
    }
}
