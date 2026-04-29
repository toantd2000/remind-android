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
        val enabledAlarms = alarms.filter { it.isEnabled }

        enabledAlarms.forEach { alarm ->
            // Logic to check if missed:
            // We look for occurrences between [lastCheck, now].
            // If the earliest occurrence starting from max(lastCheck, lastTriggered) is in the past (before now - grace), it was missed.
            
            val startTimeMillis = maxOf(lastCheck, alarm.lastTriggeredTime ?: 0L)
            val startDateTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(startTimeMillis),
                ZoneId.systemDefault()
            )

            val nextOccurrence = alarm.getActualNextOccurrence(startDateTime)
            val occurrenceMillis = nextOccurrence.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

            // If the next occurrence according to our last record is in the past, it was missed
            if (occurrenceMillis < nowMillis - 60000) { // 1 minute grace period
                missedAlarmRepository.insertMissedAlarm(
                    MissedAlarm(
                        alarmId = alarm.id,
                        alarmLabel = alarm.label,
                        scheduledTime = occurrenceMillis,
                        reason = MissedReason.POWER_OFF
                    )
                )

                // Update status: if one-time alarm, disable it
                if (alarm.repeatDays.isEmpty()) {
                    val updatedAlarm = alarm.copy(isEnabled = false)
                    alarmRepository.updateAlarm(updatedAlarm)
                    // No need to schedule if disabled
                } else {
                    // For recurring alarms, keep it enabled and reschedule for the NEXT occurrence
                    alarmScheduler.schedule(alarm)
                }
            } else {
                // Not missed, just normal rescheduling (e.g. after reboot)
                val snoozeTime = alarm.snoozeNextTriggerTime
                if (snoozeTime != null && snoozeTime > nowMillis) {
                    alarmScheduler.scheduleSnooze(alarm, snoozeTime)
                } else {
                    alarmScheduler.schedule(alarm)
                }
            }
        }
        
        // Update last check time to now
        preferencesDataSource.setLastMissedCheckTime(nowMillis)
    }
}
