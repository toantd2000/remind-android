package vn.io.litever.remind.core.reminder.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import vn.io.litever.remind.core.reminder.ReminderRingManager
import vn.io.litever.remind.core.reminder.receiver.ReminderReceiver
import vn.io.litever.alarm.core.domain.scheduler.AlarmController
import vn.io.litever.alarm.core.domain.scheduler.AlarmScheduler.Companion.ACTION_TRIGGER_ALARM
import vn.io.litever.alarm.core.domain.scheduler.AlarmScheduler.Companion.EXTRA_ALARM_ID
import javax.inject.Inject

class ReminderControllerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val reminderRingManager: ReminderRingManager
) : AlarmController {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun dismissAlarm() {
        val intent = Intent(context, ReminderService::class.java)
        context.stopService(intent)
    }

    override fun snoozeAlarm() {
        val currentAlarmId = reminderRingManager.ringingAlarmId.value
        if (currentAlarmId != null) {
            val triggerTime = System.currentTimeMillis() + 5 * 60 * 1000 // 5 mins later
            
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                action = ACTION_TRIGGER_ALARM
                putExtra(EXTRA_ALARM_ID, currentAlarmId)
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                currentAlarmId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        }
        
        val intent = Intent(context, ReminderService::class.java)
        context.stopService(intent)
    }
}
