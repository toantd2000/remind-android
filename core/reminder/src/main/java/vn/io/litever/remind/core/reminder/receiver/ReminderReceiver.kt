package vn.io.litever.remind.core.reminder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import vn.io.litever.remind.core.domain.scheduler.ReminderScheduler.Companion.ACTION_TRIGGER_REMINDER
import vn.io.litever.remind.core.domain.scheduler.ReminderScheduler.Companion.EXTRA_REMINDER_ID
import vn.io.litever.remind.core.reminder.service.ReminderService

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_TRIGGER_REMINDER) {
            val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, -1L)
            if (reminderId != -1L) {
                val serviceIntent = Intent(context, ReminderService::class.java).apply {
                    putExtra(EXTRA_REMINDER_ID, reminderId)
                }
                context.startForegroundService(serviceIntent)
            }
        }
    }
}
