package vn.io.litever.remind.core.alarm

import android.os.SystemClock
import kotlinx.coroutines.flow.first
import vn.io.litever.remind.core.datastore.AlarmPreferencesDataSource
import vn.io.litever.remind.core.domain.repository.AlarmRepository
import vn.io.litever.remind.core.domain.repository.MissedAlarmRepository
import vn.io.litever.remind.core.domain.scheduler.AlarmScheduler
import vn.io.litever.remind.core.model.MissedAlarm
import vn.io.litever.remind.core.model.MissedReason
import java.time.Instant
import java.time.LocalDateTime
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
        val uptimeMillis = runCatching { SystemClock.elapsedRealtime() }.getOrDefault(0L)
        val bootTimeMillis = if (uptimeMillis > 0L) nowMillis - uptimeMillis else 0L
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
            if (occurrenceMillis < nowMillis - 60_000) { // 1 minute grace period
                // Distinguish genuine device power-off from background/OEM restriction without relying on BootReceiver
                val reason = if (bootTimeMillis > 0L && occurrenceMillis < bootTimeMillis) {
                    MissedReason.POWER_OFF
                } else {
                    MissedReason.UNKNOWN
                }

                missedAlarmRepository.insertMissedAlarm(
                    MissedAlarm(
                        alarmId = currentAlarm.id,
                        alarmLabel = currentAlarm.label,
                        scheduledTime = occurrenceMillis,
                        reason = reason
                    )
                )

                // Update status: if one-time alarm, disable it; always stamp lastTriggeredTime to avoid duplicates
                if (currentAlarm.repeatDays.isEmpty()) {
                    val updatedAlarm = currentAlarm.copy(
                        isEnabled = false,
                        lastTriggeredTime = nowMillis
                    )
                    alarmRepository.updateAlarm(updatedAlarm)
                } else {
                    val updatedAlarm = currentAlarm.copy(lastTriggeredTime = nowMillis)
                    alarmRepository.updateAlarm(updatedAlarm)
                    alarmScheduler.schedule(updatedAlarm)
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
