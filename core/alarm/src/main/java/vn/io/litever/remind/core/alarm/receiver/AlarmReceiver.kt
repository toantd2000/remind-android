package vn.io.litever.remind.core.alarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import vn.io.litever.remind.core.domain.repository.AlarmRepository
import vn.io.litever.remind.core.domain.repository.MissedAlarmRepository
import vn.io.litever.remind.core.model.MissedAlarm
import vn.io.litever.remind.core.model.MissedReason
import vn.io.litever.remind.core.domain.scheduler.AlarmScheduler
import vn.io.litever.remind.core.alarm.service.AlarmService
import javax.inject.Inject

import android.app.NotificationManager
import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import vn.io.litever.remind.core.alarm.R

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {
    
    @Inject
    lateinit var alarmRepository: AlarmRepository
    
    @Inject
    lateinit var missedAlarmRepository: MissedAlarmRepository

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == AlarmScheduler.ACTION_TRIGGER_ALARM || action == AlarmScheduler.ACTION_TRIGGER_SNOOZE) {
            val alarmId = intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1L)
            if (alarmId != -1L) {
                val pendingResult = goAsync()
                CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                    try {
                        // Check notification permission on Android 13+
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                recordMissed(context, alarmId, MissedReason.PERMISSION_MISSING, null)
                                return@launch
                            }
                        }
                        
                        val alarm = alarmRepository.getAlarmById(alarmId)
                        if (alarm != null) {
                            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                            val isDndActive = notificationManager.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL
                            
                            if (isDndActive && !alarm.overrideDndEnabled) {
                                recordMissed(context, alarmId, MissedReason.DND_ACTIVE, alarm)
                                return@launch
                            }
                        }
                        
                        val serviceIntent = Intent(context, AlarmService::class.java).apply {
                            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
                            putExtra(AlarmScheduler.EXTRA_IS_SNOOZE, intent.getBooleanExtra(AlarmScheduler.EXTRA_IS_SNOOZE, false))
                        }
                        
                        try {
                            context.startForegroundService(serviceIntent)
                        } catch (e: Exception) {
                            recordMissed(context, alarmId, MissedReason.PERMISSION_MISSING, alarm)
                        }
                    } finally {
                        pendingResult?.finish()
                    }
                }
            }
        }
    }

    private suspend fun recordMissed(context: Context, alarmId: Long, reason: MissedReason, alarmArg: vn.io.litever.remind.core.model.Alarm?) {
        val alarm = alarmArg ?: alarmRepository.getAlarmById(alarmId)
        missedAlarmRepository.insertMissedAlarm(
            MissedAlarm(
                alarmId = alarmId,
                alarmLabel = alarm?.label ?: "",
                scheduledTime = alarm?.time?.toSecondOfDay()?.toLong() ?: System.currentTimeMillis(),
                reason = reason
            )
        )

        if ((reason == MissedReason.DND_ACTIVE || reason == MissedReason.PERMISSION_MISSING) && alarm != null) {
            val contentTextRes = if (reason == MissedReason.DND_ACTIVE) {
                R.string.missed_dnd_notification_content
            } else {
                R.string.missed_permission_notification_content
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val packageManager = context.packageManager
            val launchIntent = packageManager.getLaunchIntentForPackage(context.packageName)
            val pendingIntent = launchIntent?.let {
                PendingIntent.getActivity(
                    context,
                    alarm.id.toInt(),
                    it,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }

            val notification = NotificationCompat.Builder(context, "alarm_missed_channel")
                .setSmallIcon(R.drawable.ic_remind_notification) // Use local R
                .setContentTitle(context.getString(R.string.missed_alarm_title))
                .setContentText(context.getString(contentTextRes, alarm.label.ifEmpty { alarm.time.toString() }))
                .apply {
                    if (pendingIntent != null) {
                        setContentIntent(pendingIntent)
                    }
                }
                .setAutoCancel(true)
                .build()

            manager.notify(alarm.id.toInt() + 1000, notification)
        }
    }
}










