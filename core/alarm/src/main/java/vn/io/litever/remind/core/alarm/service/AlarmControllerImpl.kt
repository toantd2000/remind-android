package vn.io.litever.remind.core.alarm.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import vn.io.litever.remind.core.alarm.AlarmRingManager
import vn.io.litever.remind.core.alarm.receiver.AlarmReceiver
import vn.io.litever.remind.core.domain.scheduler.AlarmController
import vn.io.litever.remind.core.domain.scheduler.AlarmScheduler
import vn.io.litever.remind.core.domain.repository.AlarmRepository
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AlarmControllerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alarmRingManager: AlarmRingManager,
    private val alarmRepository: AlarmRepository,
    private val alarmScheduler: AlarmScheduler
) : AlarmController {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override suspend fun dismissAlarm(alarmId: Long?) {
        val currentAlarmId = alarmId ?: alarmRingManager.ringingAlarmId.value
        if (currentAlarmId != null) {
            withContext(Dispatchers.IO) {
                val alarm = alarmRepository.getAlarmById(currentAlarmId)
                if (alarm != null) {
                    alarmScheduler.cancelSnooze(alarm)
                    
                    val updatedAlarm = alarm.copy(
                        currentSnoozeCount = 0,
                        snoozeNextTriggerTime = null,
                        isMissed = false
                    )
                    alarmRepository.updateAlarm(updatedAlarm)
                }
            }
        }
        currentAlarmId?.let { alarmRingManager.dequeueAlarm(it) }
        // Also cancel missed notification if it exists (we use ID = alarmId)
        currentAlarmId?.let { 
            alarmRingManager.dequeueAlarm(it)
            notificationManager.cancel(it.toInt()) 
        }
    }

    override suspend fun snoozeAlarm(alarmId: Long?) {
        val currentAlarmId = alarmId ?: alarmRingManager.ringingAlarmId.value
        if (currentAlarmId != null) {
            val alarm = withContext(Dispatchers.IO) { alarmRepository.getAlarmById(currentAlarmId) }
            if (alarm != null && alarm.snoozeEnabled && alarm.currentSnoozeCount < alarm.snoozeRepeatCount) {
                val interval = if (alarm.snoozeInterval > 0) alarm.snoozeInterval else 5
                val triggerTime = System.currentTimeMillis() + interval * 60 * 1000L

                val updatedAlarm = alarm.copy(
                    currentSnoozeCount = alarm.currentSnoozeCount + 1,
                    snoozeNextTriggerTime = triggerTime
                )
                withContext(Dispatchers.IO) { alarmRepository.updateAlarm(updatedAlarm) }
                
                alarmScheduler.scheduleSnooze(updatedAlarm, triggerTime)
            }
        }
        // NO OVERLAP RULE: Don't dequeue here. The alarm stays in queue during snooze
        // to prevent next alarm from ringing until this session is finished.
    }

    override suspend fun markAsMissed(alarmId: Long?) {
        val currentAlarmId = alarmId ?: alarmRingManager.ringingAlarmId.value
        if (currentAlarmId != null) {
            val alarm = withContext(Dispatchers.IO) { alarmRepository.getAlarmById(currentAlarmId) }
            if (alarm != null) {
                // AUTO-SNOOZE LOGIC: If we haven't reached snooze limit, snooze instead of marking as missed
                if (alarm.snoozeEnabled && alarm.currentSnoozeCount < alarm.snoozeRepeatCount) {
                    snoozeAlarm(currentAlarmId)
                    return
                }

                withContext(Dispatchers.IO) {
                    val updatedAlarm = alarm.copy(
                        isMissed = true,
                        snoozeNextTriggerTime = null
                    )
                    alarmRepository.updateAlarm(updatedAlarm)

                    // Deep link to message screen
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("app://remind/message/${alarm.id}")).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        alarm.id.hashCode(),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    // Show a silent notification
                    val notification = androidx.core.app.NotificationCompat.Builder(context, "alarm_channel")
                        .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                        .setContentTitle(context.getString(vn.io.litever.remind.core.alarm.R.string.missed_alarm_title))
                        .setContentText(alarm.label.ifEmpty { context.getString(vn.io.litever.remind.core.alarm.R.string.missed_alarm_text) })
                        .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .build()
                    notificationManager.notify(currentAlarmId.toInt(), notification)
                }
            }
        }
        currentAlarmId?.let { 
            alarmRingManager.setAcknowledgingAlarmId(it)
            alarmRingManager.dequeueAlarm(it) 
        }
    }

    override suspend fun cancelSnooze(alarmId: Long) {
        withContext(Dispatchers.IO) {
            val alarm = alarmRepository.getAlarmById(alarmId)
            if (alarm != null) {
                alarmScheduler.cancelSnooze(alarm)
                
                val updatedAlarm = alarm.copy(
                    currentSnoozeCount = 0,
                    snoozeNextTriggerTime = null,
                    isMissed = false
                )
                alarmRepository.updateAlarm(updatedAlarm)
            }
        }
    }
}










