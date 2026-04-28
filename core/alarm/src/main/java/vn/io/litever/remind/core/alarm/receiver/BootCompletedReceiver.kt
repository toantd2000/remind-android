package vn.io.litever.remind.core.alarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import vn.io.litever.remind.core.domain.repository.AlarmRepository
import vn.io.litever.remind.core.domain.repository.MissedAlarmRepository
import vn.io.litever.remind.core.domain.scheduler.AlarmScheduler
import vn.io.litever.remind.core.model.MissedAlarm
import vn.io.litever.remind.core.model.MissedReason
import vn.io.litever.remind.core.datastore.AlarmPreferencesDataSource
import java.time.LocalDateTime
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {
    
    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    @Inject
    lateinit var alarmRepository: AlarmRepository

    @Inject
    lateinit var missedAlarmRepository: MissedAlarmRepository

    @Inject
    lateinit var preferencesDataSource: AlarmPreferencesDataSource

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val pendingResult = goAsync()
            scope.launch {
                try {
                    val lastCheck = preferencesDataSource.lastMissedCheckTime.first()
                    val nowMillis = System.currentTimeMillis()
                    
                    val alarms = alarmRepository.getAllAlarms().first()
                    val enabledAlarms = alarms.filter { it.isEnabled }
                    
                    enabledAlarms.forEach { alarm ->
                        // Check for missed alarms during power off
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
                        }

                        // Normal rescheduling logic
                        val snoozeTime = alarm.snoozeNextTriggerTime
                        if (snoozeTime != null && snoozeTime > nowMillis) {
                            alarmScheduler.scheduleSnooze(alarm, snoozeTime)
                        } else {
                            alarmScheduler.schedule(alarm)
                        }
                    }
                    preferencesDataSource.setLastMissedCheckTime(nowMillis)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}










