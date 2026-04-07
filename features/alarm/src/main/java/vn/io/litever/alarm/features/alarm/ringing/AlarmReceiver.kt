package vn.io.litever.alarm.features.alarm.ringing

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import vn.io.litever.alarm.core.domain.scheduler.AlarmScheduler.Companion.ACTION_TRIGGER_ALARM
import vn.io.litever.alarm.core.domain.scheduler.AlarmScheduler.Companion.EXTRA_ALARM_ID

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_TRIGGER_ALARM) {
            val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1L)
            if (alarmId != -1L) {
                // Khởi động AlarmService (Foreground)
                val serviceIntent = Intent(context, AlarmService::class.java).apply {
                    putExtra(EXTRA_ALARM_ID, alarmId)
                }
                context.startForegroundService(serviceIntent)
            }
        }
    }
}
