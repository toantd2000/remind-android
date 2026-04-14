package vn.io.litever.remind.core.reminder.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import vn.io.litever.remind.core.reminder.ReminderRingManager
import vn.io.litever.remind.core.reminder.receiver.ReminderReceiver
import vn.io.litever.remind.core.domain.scheduler.ReminderController
import vn.io.litever.remind.core.domain.scheduler.ReminderScheduler.Companion.ACTION_TRIGGER_REMINDER
import vn.io.litever.remind.core.domain.scheduler.ReminderScheduler.Companion.EXTRA_REMINDER_ID
import vn.io.litever.remind.core.domain.scheduler.ReminderScheduler.Companion.EXTRA_IS_SNOOZE
import vn.io.litever.remind.core.domain.repository.ReminderRepository
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class ReminderControllerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val reminderRingManager: ReminderRingManager,
    private val reminderRepository: ReminderRepository
) : ReminderController {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun dismissReminder() {
        val intent = Intent(context, ReminderService::class.java)
        context.stopService(intent)
    }

    override fun snoozeReminder() {
        val currentReminderId = reminderRingManager.ringingReminderId.value
        if (currentReminderId != null) {
            val reminder = runBlocking { reminderRepository.getReminderById(currentReminderId) }
            if (reminder != null && reminder.snoozeEnabled && reminder.currentSnoozeCount < reminder.snoozeRepeatCount) {
                val updatedReminder = reminder.copy(currentSnoozeCount = reminder.currentSnoozeCount + 1)
                runBlocking { reminderRepository.updateReminder(updatedReminder) }

                val interval = if (reminder.snoozeInterval > 0) reminder.snoozeInterval else 5
                val triggerTime = System.currentTimeMillis() + interval * 60 * 1000L
                
                val intent = Intent(context, ReminderReceiver::class.java).apply {
                    action = ACTION_TRIGGER_REMINDER
                    putExtra(EXTRA_REMINDER_ID, currentReminderId)
                    putExtra(EXTRA_IS_SNOOZE, true)
                }
                
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    currentReminderId.hashCode(),
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
        }
        
        val intent = Intent(context, ReminderService::class.java)
        context.stopService(intent)
    }
}
