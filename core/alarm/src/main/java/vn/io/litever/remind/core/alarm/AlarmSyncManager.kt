package vn.io.litever.remind.core.alarm

import vn.io.litever.remind.core.domain.repository.AlarmRepository
import vn.io.litever.remind.core.domain.repository.MissedAlarmRepository
import vn.io.litever.remind.core.domain.scheduler.AlarmScheduler
import vn.io.litever.remind.core.datastore.AlarmPreferencesDataSource
import vn.io.litever.remind.core.model.MissedAlarm
import vn.io.litever.remind.core.model.MissedReason
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmSyncManager @Inject constructor(
    private val alarmRepository: AlarmRepository,
    private val alarmScheduler: AlarmScheduler,
    private val missedAlarmRepository: MissedAlarmRepository,
    private val preferencesDataSource: AlarmPreferencesDataSource
) {
    suspend fun sync() {
        val nowMillis = System.currentTimeMillis()
        var lastCheck = preferencesDataSource.lastMissedCheckTime.first()
        
        // If first time, initialize to now and skip missed check to avoid false positives
        if (lastCheck == 0L) {
            preferencesDataSource.setLastMissedCheckTime(nowMillis)
            lastCheck = nowMillis
        }

        val alarms = alarmRepository.getAllAlarms().first()
        val now = LocalDateTime.now()

        alarms.forEach { alarm ->
            if (!alarm.isEnabled) return@forEach

            // 1. Logic to check if missed:
            // Use getNextOccurrence(startDateTime) with original alarm to respect skip during missed check
            val startTimeMillis = maxOf(lastCheck, alarm.lastTriggeredTime ?: 0L)
            val startDateTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(startTimeMillis),
                ZoneId.systemDefault()
            )

            val nextOccurrence = alarm.getNextOccurrence(startDateTime)
            val occurrenceMillis = nextOccurrence.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

            // 2. Prepare updated alarm: Clear expired skip status BEFORE rescheduling
            var currentAlarm = alarm
            if (alarm.isSkipExpired(now)) {
                currentAlarm = alarm.copy(skippedAt = null)
                alarmRepository.updateAlarm(currentAlarm)
            }

            // 3. Process Missed or Reschedule
            if (occurrenceMillis < nowMillis - 60000) { // 1 minute grace period
                missedAlarmRepository.insertMissedAlarm(
                    MissedAlarm(
                        alarmId = currentAlarm.id,
                        alarmLabel = currentAlarm.label,
                        scheduledTime = occurrenceMillis,
                        reason = MissedReason.POWER_OFF
                    )
                )

                // Update status: if one-time alarm, disable it
                if (currentAlarm.repeatDays.isEmpty()) {
                    val updatedAlarm = currentAlarm.copy(isEnabled = false)
                    alarmRepository.updateAlarm(updatedAlarm)
                } else {
                    // For recurring alarms, reschedule for the NEXT occurrence
                    alarmScheduler.schedule(currentAlarm)
                }
            } else {
                // Not missed, just normal rescheduling (e.g. after reboot)
                alarmScheduler.schedule(currentAlarm)
                
                // Restore snooze if active
                val snoozeTime = currentAlarm.snoozeNextTriggerTime
                if (snoozeTime != null && snoozeTime > nowMillis) {
                    alarmScheduler.scheduleSnooze(currentAlarm, snoozeTime)
                }
            }
        }
        
        // Update last check time to now
        preferencesDataSource.setLastMissedCheckTime(nowMillis)
    }
}
