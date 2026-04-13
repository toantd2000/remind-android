package vn.io.litever.remind.core.reminder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import vn.io.litever.alarm.core.domain.scheduler.AlarmScheduler.Companion.ACTION_TRIGGER_ALARM
import vn.io.litever.alarm.core.domain.scheduler.AlarmScheduler.Companion.EXTRA_ALARM_ID
import vn.io.litever.remind.core.reminder.service.ReminderService

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_TRIGGER_ALARM) {
            val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1L)
            if (alarmId != -1L) {
                val serviceIntent = Intent(context, ReminderService::class.java).apply {
                    putExtra(EXTRA_ALARM_ID, alarmId)
                }
                context.startForegroundService(serviceIntent)
            }
        }
    }
}
